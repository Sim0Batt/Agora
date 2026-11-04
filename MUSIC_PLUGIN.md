# Piano: plugin musicale per Agora

Idea: riprodurre musica (MP3 locali, in futuro fonti esterne) direttamente da Agora, con un'esperienza simile a Spotify.

## 1. MP3 locali (metodo base, gratuito)

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

- **Telegram**: serve la MTProto API (TDLib/`tdlight` o Telethon) con `api_id`/`api_hash` gratuiti da my.telegram.org. Il server scarica in cache i file audio del canale e li serve via `/stream`. I bot normali leggono solo i canali di cui sono admin. Attenzione al copyright: molti canali distribuiscono materiale protetto.
- **Alternative legali e gratuite**: Internet Archive, Jamendo (Creative Commons), Free Music Archive, radio web Icecast/SHOUTcast, podcast RSS, Navidrome/Subsonic.

## 3. Architettura a plugin

```kotlin
interface MusicSource {
    suspend fun list(query: String?): List<Track>
    suspend fun stream(trackId: String): InputStream
}
```

Implementazioni: `LocalSource`, `TelegramSource`, `JamendoSource`. Esposte sotto `/music/{source}/...`, così i client non cambiano.

## 4. Qualità e aspettative

- Streaming **bit-perfect**: nessuna ricodifica, la qualità è quella dei file.
- MP3 256-320 kbps è praticamente equivalente a Spotify; 128 kbps suona peggio.
- Formati supportabili: MP3, FLAC, AAC/M4A, OGG, Opus (Safari non legge OGG/Opus).

**Ottieni**: libreria, ricerca, playlist, coda, copertine, seek istantaneo, riproduzione continua, accesso da più dispositivi (VPN/Tailscale per l'esterno).

**Non ottieni (o con più lavoro)**: catalogo illimitato, raccomandazioni, gapless/crossfade perfetti (richiede Web Audio API), testi sincronizzati (possibile con LRCLIB), download offline (serve client KMP o PWA).

**Nota**: l'assenza di autenticazione rende il servizio accessibile a chiunque se esposto fuori dalla rete locale.

## 5. Ordine consigliato

1. `LocalSource` + `PartialContent` + `<audio>` nella web-app.
2. Playlist e coda.
3. Fonti esterne (Jamendo, radio), poi Telegram se serve.

In alternativa, usare **Navidrome** (gratuito, open source) come backend musicale e integrarlo in Agora come fonte.
