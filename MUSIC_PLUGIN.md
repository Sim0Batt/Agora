# Piano: plugin musicale per Agora

Idea: ascoltare **qualsiasi canzone di un catalogo** direttamente da Agora, in streaming e senza tenere la libreria sul server (che gira su un Raspberry Pi, raggiunto di solito via Tailscale). Le fonti sono plugin; gli MP3 locali restano una fonte tra le altre.

## 0. Principio: il server non deve diventare un deposito di audio

Due modi di fare streaming senza archiviare:

1. **Il client riproduce direttamente dalla fonte (preferito)**: il server fa solo ricerca e metadati e restituisce un URL di stream. Sul Pi non passa audio e non si scrive su disco.
2. **Proxy pass-through**: il server inoltra i byte (inoltrando anche il Range a monte) senza salvarli. Serve quando la fonte richiede header/token o ha problemi di CORS. Costa banda doppia sul Pi, anche sul tunnel Tailscale.

Eccezione: Telegram (sezione 2.3) richiede per forza una cache su disco.

## 1. MP3 locali (fonte opzionale)

**Server (Ktor)**
- Cartella configurabile nell'XML (es. `musicDir`).
- `MusicManager` (sul modello di `TodoManager`) che scansiona la cartella e legge i tag ID3 con `jaudiotagger` (titolo, artista, album, durata, copertina).
- Route:
  - `GET /music` – lista tracce
  - `GET /music/{id}/stream` – file con supporto **Range** (plugin `PartialContent` di Ktor), necessario per seek e buffering
  - `POST /music` – upload (opzionale)
- Tabella opzionale `Tracks` (id, path, title, artist, userId) in `agora_boostrap.sql`, oppure scansione al volo.

**Web-app**
- Componente `MusicPlayer` con `<audio src="/api/music/{id}/stream">`.
- Playlist, coda, volume, shuffle e repeat gestiti con lo stato React.
- Player montato in un layout globale, così la musica continua al cambio pagina.
- Media Session API per tasti multimediali e notifiche di sistema.

**Client KMP**
- Desktop: `javazoom` o VLCJ. Android: Media3/ExoPlayer. Multipiattaforma: `compose-multiplatform-media-player`.
- Stesso URL di stream del web.

## 2. Fonti esterne

### 2.1 Cataloghi legali con URL diretti (stream senza passare dal Pi)
Jamendo (CC, API gratuita con chiave), Audius (catalogo ampio, API gratuita), Internet Archive, Free Music Archive, radio web (Radio Browser, Icecast/SHOUTcast), podcast RSS, Navidrome/Subsonic (URL firmati).

### 2.2 YouTube (catalogo più ampio)
- La **Data API v3 è gratuita**: quota di default 10.000 unità/giorno, `search.list` costa 100 unità (~100 ricerche/giorno, aumentabile con richiesta di audit). Verificare i valori sulla documentazione ufficiale.
- La Data API dà solo ricerca e metadati, **non l'audio**.
- Riproduzione:
  - **IFrame Player ufficiale**: conforme ai ToS, ma video visibile, niente estrazione audio, limiti in background su mobile, non funziona con embedding disabilitato.
  - **`yt-dlp` / Piped / Invidious**: solo audio e background, ma viola i ToS ed è fragile. Da considerare fonte sperimentale, eseguita dal Pi.
- Spotify: le API non danno l'audio. Si può solo fare da telecomando (Spotify Connect, Premium) oppure usare il Web Playback SDK (Premium, solo browser, DRM).

### 2.3 Telegram + `@DeezerMusicBot` (sperimentale)
- Il bot (autore `@Torturets`, canale aggiornamenti `@DeezerMusicNews`) **non è ufficiale Deezer**: gratuità, limiti e continuità non sono garantiti (non verificati). Materiale protetto da copyright, quindi stato legale grigio.
- Un bot non può scrivere a un altro bot: serve un **account utente via MTProto** (`api_id`/`api_hash` da my.telegram.org). Usare un **account secondario**; il file di sessione è una credenziale e va tenuto fuori dalla repo.
- Da Kotlin: **`tdlight-java`** (wrapper TDLib; verificare i binari Linux ARM64) integrato in Ktor con coroutine. Alternativa: sidecar Python (Telethon/Pyrogram) con API HTTP locale.
- Flusso:
  1. `SendMessage` al bot (o modalità inline, se supportata).
  2. Handler su `UpdateNewMessage` per leggere risposte e bottoni inline; click con `GetCallbackQueryAnswer`.
  3. `DownloadFile` del messaggio audio in una **cache su disco**, poi servito via `/stream` con Range. Possibile scaricare a pezzi e iniziare a servire prima della fine.
  4. Cache con limite di spazio ed eliminazione dei file più vecchi (la SD del Pi è limitata).
- Comportamento osservato del bot: la ricerca (testo libero) restituisce un messaggio con **inline keyboard** (`1. Titolo`, `2. Titolo`, ..., più `Get all tracks` e `Close`); il click su un titolo fa inviare un **messaggio audio** da scaricare.
- Automazione con TDLib:
  1. `SendMessage(chatId, query)`.
  2. Su `UpdateNewMessage` dal bot leggere `replyMarkup` (`ReplyMarkupInlineKeyboard`): ogni bottone ha testo e `callbackData`. Il `search()` può esporre questa lista come risultati (solo metadati, nessun download).
  3. Alla scelta della traccia: `GetCallbackQueryAnswer(chatId, messageId, CallbackQueryPayloadData(data))`, equivalente al tocco.
  4. Attendere un `MessageAudio` (fileId, titolo, artista, durata, copertina) e fare `DownloadFile(fileId, ..., synchronous = true)` nella cache.
  5. Opzionale: premere `Close` per tenere pulita la chat.
- Note: salvare `messageId` della lista e i `callbackData` nello stato della richiesta; ascoltare anche `UpdateMessageEdited`/`UpdateMessageContent` (il bot modifica il messaggio per paginazione); una richiesta alla volta per chat, con timeout; **non** usare `Get all tracks` (scarica tutto, brucia quota e cache); verificare con un prototipo esterno alla repo cosa contiene davvero `replyMarkup`; se i `callbackData` sono opachi, fallback inviando numero/testo del bottone.
- Punti critici: `FloodWait` (coda, una richiesta alla volta), correlazione richiesta/risposta asincrona, latenza di qualche secondo (prefetch della traccia successiva in coda), RAM di TDLib sul Pi, primo login interattivo (codice/2FA), parser fragile se il bot cambia messaggi.
- Da isolare dietro `MusicSource` e considerare usa e getta.

## 3. Architettura a plugin

```kotlin
interface MusicSource {
    suspend fun search(query: String): List<Track>
    suspend fun resolve(trackId: String): StreamTarget
}

sealed interface StreamTarget {
    data class Redirect(val url: String) : StreamTarget      // il client legge dalla fonte
    data class Proxy(val url: String, val headers: Map<String, String>) : StreamTarget // pass-through
    data class CachedFile(val path: String) : StreamTarget   // es. Telegram
}
```

Implementazioni: `JamendoSource`, `AudiusSource`, `YouTubeSource`, `TelegramSource`, `LocalSource`. Esposte sotto `/music/{source}/...`, così i client non cambiano. Il server salva al massimo i metadati.

## 4. Qualità e aspettative

- Streaming **bit-perfect**: nessuna ricodifica, la qualità è quella dei file.
- MP3 256-320 kbps è praticamente equivalente a Spotify; 128 kbps suona peggio.
- Formati supportabili: MP3, FLAC, AAC/M4A, OGG, Opus (Safari non legge OGG/Opus).

**Ottieni**: libreria, ricerca, playlist, coda, copertine, seek istantaneo, riproduzione continua, accesso da più dispositivi (VPN/Tailscale per l'esterno).

**Non ottieni (o con più lavoro)**: catalogo illimitato, raccomandazioni, gapless/crossfade perfetti (richiede Web Audio API), testi sincronizzati (possibile con LRCLIB), download offline (serve client KMP o PWA).

**Nota**: l'assenza di autenticazione rende il servizio accessibile a chiunque se esposto fuori dalla rete locale.

## 5. Ordine consigliato

1. Interfaccia `MusicSource` + `<audio>` nella web-app, con una fonte semplice a URL diretto (Jamendo o Audius, `Redirect`).
2. Playlist e coda (solo metadati sul server).
3. YouTube (ricerca con Data API; IFrame sul web, `yt-dlp` sperimentale).
4. `TelegramSource` con `@DeezerMusicBot` come esperimento, con cache limitata.
5. `LocalSource` + `PartialContent` se serve anche la libreria locale.

In alternativa, usare **Navidrome** (gratuito, open source) come backend musicale e integrarlo in Agora come fonte.
