import { useCallback, useEffect, useState } from 'react';
import { DashBoard } from '../components/Dashboard';
import { musicApi } from '../api/music';
import { AddToPlaylist } from '../components/AddToPlaylist';
import { useMusic } from '../context/musicContext';
import type { PickedTrack, Playlist, SearchResult, Song } from '../types/models';

const formatDuration = (seconds: number) => {
  const m = Math.floor(seconds / 60);
  const s = Math.floor(seconds % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
};

const smallBtn =
  'inline-flex min-h-11 shrink-0 items-center justify-center rounded-md border border-border px-3 py-1 text-sm transition-colors hover:border-accent hover:text-accent sm:min-h-0';
const dangerBtn =
  'inline-flex min-h-11 shrink-0 items-center justify-center rounded-md border border-border px-3 py-1 text-sm transition-colors hover:border-red-400 hover:text-red-400 sm:min-h-0';

const errorMessage = (e: unknown, fallback: string) => {
  const message = (e as { response?: { data?: unknown } })?.response?.data;
  return typeof message === 'string' && message ? message : fallback;
};

export const MusicPage = () => {
  const [query, setQuery] = useState('');
  const [searching, setSearching] = useState(false);
  const [result, setResult] = useState<SearchResult | null>(null);
  const [searchError, setSearchError] = useState<string | null>(null);

  const [downloadingIdx, setDownloadingIdx] = useState<number | null>(null);
  const [downloaded, setDownloaded] = useState<PickedTrack | null>(null);
  const [downloadError, setDownloadError] = useState<string | null>(null);

  const [songs, setSongs] = useState<Song[]>([]);
  const [songsError, setSongsError] = useState(false);

  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [playlistsError, setPlaylistsError] = useState(false);
  const [newPlaylist, setNewPlaylist] = useState('');
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [selectedPlaylist, setSelectedPlaylist] = useState<Playlist | null>(null);
  const [playlistSongs, setPlaylistSongs] = useState<Song[]>([]);
  const [playlistSongsError, setPlaylistSongsError] = useState(false);

  const { play, enqueue, playNext } = useMusic();

  const loadPlaylists = useCallback(() => {
    musicApi
      .listPlaylists()
      .then((data) => {
        setPlaylists(data);
        setPlaylistsError(false);
      })
      .catch(() => setPlaylistsError(true));
  }, []);

  const loadPlaylistSongs = useCallback((playlistId: string) => {
    musicApi
      .listPlaylistSongs(playlistId)
      .then((data) => {
        setPlaylistSongs(data);
        setPlaylistSongsError(false);
      })
      .catch(() => setPlaylistSongsError(true));
  }, []);

  const loadSongs = useCallback(() => {
    musicApi
      .listSongs()
      .then((data) => {
        setSongs(data);
        setSongsError(false);
      })
      .catch(() => setSongsError(true));
  }, []);

  useEffect(() => {
    loadSongs();
    loadPlaylists();
  }, [loadSongs, loadPlaylists]);

  const handleSelectPlaylist = (playlist: Playlist) => {
    if (selectedPlaylist?.id === playlist.id) {
      setSelectedPlaylist(null);
      return;
    }
    setSelectedPlaylist(playlist);
    setPlaylistSongs([]);
    loadPlaylistSongs(playlist.id);
  };

  const handleCreatePlaylist = async (e: React.FormEvent) => {
    e.preventDefault();
    const title = newPlaylist.trim();
    if (!title || creating) return;
    setCreating(true);
    setCreateError(null);
    try {
      await musicApi.createPlaylist(title);
      setNewPlaylist('');
      loadPlaylists();
    } catch {
      setCreateError('Creazione della playlist fallita.');
    } finally {
      setCreating(false);
    }
  };

  const handleDeletePlaylist = async (playlist: Playlist) => {
    if (!window.confirm(`Eliminare la playlist "${playlist.title}"?`)) return;
    setActionError(null);
    try {
      await musicApi.deletePlaylist(playlist.id);
      if (selectedPlaylist?.id === playlist.id) setSelectedPlaylist(null);
      loadPlaylists();
    } catch {
      setActionError('Eliminazione della playlist fallita.');
    }
  };

  const handleRemoveSong = async (playlistId: string, songId: string) => {
    setActionError(null);
    try {
      await musicApi.removeSongFromPlaylist(playlistId, songId);
      loadPlaylistSongs(playlistId);
      loadPlaylists();
    } catch {
      setActionError('Rimozione del brano dalla playlist fallita.');
    }
  };

  const withPlaylistSongs = async (playlist: Playlist, action: (songs: Song[]) => void) => {
    setActionError(null);
    try {
      const songs = await musicApi.listPlaylistSongs(playlist.id);
      if (songs.length === 0) setActionError(`La playlist "${playlist.title}" è vuota.`);
      else action(songs);
    } catch {
      setActionError('Impossibile caricare i brani della playlist.');
    }
  };

  const handleAddedToPlaylist = (playlistId: string) => {
    loadPlaylists();
    if (selectedPlaylist?.id === playlistId) loadPlaylistSongs(playlistId);
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    const title = query.trim();
    if (!title || searching) return;
    setSearching(true);
    setSearchError(null);
    setDownloaded(null);
    setDownloadError(null);
    setResult(null);
    try {
      setResult(await musicApi.search(title));
    } catch (err) {
      setSearchError(errorMessage(err, 'Ricerca fallita o scaduta, riprova.'));
    } finally {
      setSearching(false);
    }
  };

  const handleDownload = async (index: number) => {
    if (!result || downloadingIdx !== null) return;
    setDownloadingIdx(index);
    setDownloadError(null);
    setDownloaded(null);
    try {
      setDownloaded(await musicApi.download(result.messageId, index));
      loadSongs();
    } catch (err) {
      setDownloadError(errorMessage(err, 'Download fallito. I risultati potrebbero essere scaduti: rifai la ricerca.'));
    } finally {
      setDownloadingIdx(null);
    }
  };

  return (
    <div className="min-h-screen bg-surface text-text">
      <DashBoard />
      <main className="mx-auto max-w-6xl space-y-8 px-4 py-6 sm:space-y-10 sm:py-10">
        <section>
          <h1 className="text-3xl font-bold">Musica</h1>
          <p className="mt-1 text-text-muted">Cerca un brano, scegli il risultato e scaricalo sul server.</p>
        </section>

        <section className="space-y-4">
          <form onSubmit={handleSearch} className="flex gap-2">
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Titolo o artista"
              className="flex-1 rounded-lg border border-border bg-surface-alt px-4 py-2 text-text outline-none focus:border-accent"
            />
            <button
              type="submit"
              disabled={searching || !query.trim()}
              className="rounded-lg bg-accent px-5 py-2 font-semibold text-white transition-colors hover:bg-accent-hover disabled:opacity-50"
            >
              {searching ? 'Ricerca…' : 'Cerca'}
            </button>
          </form>

          {searchError && <p className="text-sm text-red-400">{searchError}</p>}
          {downloadError && <p className="text-sm text-red-400">{downloadError}</p>}
          {downloaded && (
            <p className="rounded-lg border border-green-500/30 bg-green-500/15 px-4 py-2 text-sm text-green-400">
              Scaricato: {downloaded.artist} – {downloaded.title} ({formatDuration(downloaded.duration)})
            </p>
          )}
          {downloaded && (
            <div className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-border bg-surface-alt px-4 py-3">
              <span className="text-sm text-text-muted">Aggiungi alla playlist</span>
              <AddToPlaylist
                key={downloaded.id}
                songId={downloaded.id}
                playlists={playlists}
                onAdded={handleAddedToPlaylist}
              />
            </div>
          )}

          {result && (
            <ul className="divide-y divide-border rounded-lg border border-border bg-surface-alt">
              {result.items.length === 0 && (
                <li className="px-4 py-3 text-sm text-text-muted">Nessun risultato.</li>
              )}
              {result.items.map((item) => (
                <li key={item.index} className="flex items-center justify-between gap-4 px-4 py-3">
                  <span className="truncate">{item.title}</span>
                  <button
                    onClick={() => handleDownload(item.index)}
                    disabled={downloadingIdx !== null}
                    className={`${smallBtn} disabled:opacity-50`}
                  >
                    {downloadingIdx === item.index ? 'Download…' : 'Scarica'}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="space-y-4">
          <h2 className="text-sm font-semibold uppercase tracking-widest text-text-muted">Playlist</h2>
          <form onSubmit={handleCreatePlaylist} className="flex gap-2">
            <input
              value={newPlaylist}
              onChange={(e) => setNewPlaylist(e.target.value)}
              placeholder="Nome nuova playlist"
              maxLength={100}
              className="flex-1 rounded-lg border border-border bg-surface-alt px-4 py-2 text-text outline-none focus:border-accent"
            />
            <button
              type="submit"
              disabled={creating || !newPlaylist.trim()}
              className="rounded-lg bg-accent px-5 py-2 font-semibold text-white transition-colors hover:bg-accent-hover disabled:opacity-50"
            >
              {creating ? 'Creazione…' : 'Crea'}
            </button>
          </form>
          {createError && <p className="text-sm text-red-400">{createError}</p>}
          {actionError && <p className="text-sm text-red-400">{actionError}</p>}

          {playlistsError ? (
            <p className="text-sm text-text-muted">Impossibile caricare le playlist.</p>
          ) : playlists.length === 0 ? (
            <p className="text-sm text-text-muted">Nessuna playlist.</p>
          ) : (
            <ul className="divide-y divide-border rounded-lg border border-border bg-surface-alt">
              {playlists.map((playlist) => {
                const open = selectedPlaylist?.id === playlist.id;
                return (
                  <li key={playlist.id}>
                    <div className="flex flex-col sm:flex-row sm:items-center">
                      <button
                        onClick={() => handleSelectPlaylist(playlist)}
                        className="flex min-h-12 min-w-0 flex-1 items-center justify-between gap-4 px-4 py-3 text-left transition-colors hover:text-accent"
                      >
                        <span className="truncate font-medium">{playlist.title}</span>
                        <span className="shrink-0 text-sm text-text-muted">
                          {playlist.count} {playlist.count === 1 ? 'brano' : 'brani'} {open ? '▲' : '▼'}
                        </span>
                      </button>
                      <div className="flex flex-wrap items-center gap-2 px-4 pb-3 sm:pb-0 sm:pl-0">
                        <button
                          onClick={() => withPlaylistSongs(playlist, (songs) => play(songs, 0, false))}
                          aria-label={`Riproduci ${playlist.title}`}
                          className={smallBtn}
                        >
                          ▶ Riproduci
                        </button>
                        <button
                          onClick={() => withPlaylistSongs(playlist, (songs) => play(songs, 0, true))}
                          aria-label={`Riproduci ${playlist.title} in ordine casuale`}
                          className={smallBtn}
                        >
                          🔀
                        </button>
                        <button
                          onClick={() => withPlaylistSongs(playlist, enqueue)}
                          aria-label={`Accoda ${playlist.title}`}
                          className={smallBtn}
                        >
                          ＋ Coda
                        </button>
                        <button onClick={() => handleDeletePlaylist(playlist)} className={dangerBtn}>
                          Elimina
                        </button>
                      </div>
                    </div>
                    {open && (
                      <div className="border-t border-border bg-surface px-4 py-2">
                        {playlistSongsError ? (
                          <p className="py-2 text-sm text-text-muted">Impossibile caricare i brani.</p>
                        ) : playlistSongs.length === 0 ? (
                          <p className="py-2 text-sm text-text-muted">Playlist vuota.</p>
                        ) : (
                          <ul className="divide-y divide-border">
                            {playlistSongs.map((song, i) => (
                              <li
                                key={song.id}
                                className="flex flex-col gap-2 py-3 sm:flex-row sm:items-center sm:justify-between sm:gap-4 sm:py-2"
                              >
                                <div className="min-w-0">
                                  <p className="truncate">{song.title}</p>
                                  <p className="truncate text-sm text-text-muted">{song.author}</p>
                                </div>
                                <div className="flex flex-wrap items-center gap-2 sm:shrink-0 sm:gap-4">
                                  <span className="font-mono text-sm text-text-muted">
                                    {formatDuration(song.duration)}
                                  </span>
                                  <button
                                    onClick={() => play(playlistSongs, i)}
                                    aria-label={`Riproduci ${song.title}`}
                                    className={smallBtn}
                                  >
                                    ▶
                                  </button>
                                  <button onClick={() => playNext([song])} className={smallBtn}>
                                    Dopo
                                  </button>
                                  <button onClick={() => enqueue([song])} className={smallBtn}>
                                    ＋ Coda
                                  </button>
                                  <button
                                    onClick={() => handleRemoveSong(playlist.id, song.id)}
                                    className={dangerBtn}
                                  >
                                    Rimuovi
                                  </button>
                                </div>
                              </li>
                            ))}
                          </ul>
                        )}
                      </div>
                    )}
                  </li>
                );
              })}
            </ul>
          )}
        </section>

        <section>
          <div className="mb-4 flex flex-wrap items-center justify-between gap-2 sm:gap-4">
            <h2 className="text-sm font-semibold uppercase tracking-widest text-text-muted">Libreria</h2>
            {songs.length > 0 && (
              <div className="flex items-center gap-2">
                <button onClick={() => play(songs, 0, false)} className={smallBtn}>
                  ▶ Riproduci tutto
                </button>
                <button onClick={() => play(songs, 0, true)} aria-label="Riproduci tutto in ordine casuale" className={smallBtn}>
                  🔀
                </button>
              </div>
            )}
          </div>
          {songsError ? (
            <p className="text-sm text-text-muted">Impossibile caricare le canzoni.</p>
          ) : songs.length === 0 ? (
            <p className="text-sm text-text-muted">Nessuna canzone scaricata.</p>
          ) : (
            <ul className="divide-y divide-border rounded-lg border border-border bg-surface-alt">
              {songs.map((song, i) => (
                <li
                  key={song.id}
                  className="flex flex-col gap-2 px-4 py-3 sm:flex-row sm:items-center sm:justify-between sm:gap-4"
                >
                  <div className="min-w-0">
                    <p className="truncate font-medium">{song.title}</p>
                    <p className="truncate text-sm text-text-muted">{song.author}</p>
                  </div>
                  <div className="flex flex-wrap items-center gap-2 sm:gap-4">
                    <button
                      onClick={() => play(songs, i)}
                      aria-label={`Riproduci ${song.title}`}
                      className={smallBtn}
                    >
                      ▶
                    </button>
                    <button onClick={() => playNext([song])} className={smallBtn}>
                      Dopo
                    </button>
                    <button onClick={() => enqueue([song])} className={smallBtn}>
                      ＋ Coda
                    </button>
                    <AddToPlaylist
                      songId={song.id}
                      playlists={playlists}
                      onAdded={handleAddedToPlaylist}
                    />
                    <span className="shrink-0 font-mono text-sm text-text-muted">{formatDuration(song.duration)}</span>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>
      </main>
    </div>
  );
};
