import { useCallback, useEffect, useMemo, useState } from 'react';
import { DashBoard } from '../components/Dashboard';
import { musicApi } from '../api/music';
import { AddToPlaylist } from '../components/AddToPlaylist';
import { ChevronDownIcon, PlayIcon, QueueIcon, ShuffleIcon } from '../components/Icons';
import { useMusic } from '../context/musicContext';
import { Link } from 'react-router-dom';
import { onImportFinished } from '../context/importJobs';
import { dangerBtn, errorMessage, formatDuration, smallBtn } from '../components/musicUi';
import type { Playlist, SearchSong, Song } from '../types/models';

export const MusicPage = () => {
  const [query, setQuery] = useState('');
  const [searching, setSearching] = useState(false);
  const [result, setResult] = useState<SearchSong[] | null>(null);
  const [searchError, setSearchError] = useState<string | null>(null);

  const [downloadingId, setDownloadingId] = useState<string | null>(null);
  const [downloaded, setDownloaded] = useState<SearchSong | null>(null);
  const [downloadError, setDownloadError] = useState<string | null>(null);

  const [songs, setSongs] = useState<Song[]>([]);
  const [songsError, setSongsError] = useState(false);
  const [confirmAction, setConfirmAction] = useState<{ message: string; run: () => void } | null>(null);
  const [libraryOpen, setLibraryOpen] = useState(true);
  const [libraryFilter, setLibraryFilter] = useState('');

  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [actionError, setActionError] = useState<string | null>(null);

  const { play, enqueue, playNext } = useMusic();

  const visibleSongs = useMemo(() => {
    const q = libraryFilter.trim().toLowerCase();
    const all = songs.map((song, i) => ({ song, i }));
    if (!q) return all;
    return all.filter(({ song }) => song.title.toLowerCase().includes(q) || song.author.toLowerCase().includes(q));
  }, [songs, libraryFilter]);

  const loadPlaylists = useCallback(() => {
    musicApi
      .listPlaylists()
      .then(setPlaylists)
      .catch(() => setPlaylists([]));
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

  useEffect(() => onImportFinished(() => {
    loadPlaylists();
    loadSongs();
  }), [loadPlaylists, loadSongs]);

  const handleDeleteSong = (song: Song) =>
    setConfirmAction({
      message: `Eliminare "${song.title}" dalla libreria?`,
      run: () => deleteSong(song),
    });

  const deleteSong = async (song: Song) => {
    setActionError(null);
    try {
      await musicApi.deleteSong(song.id);
      loadSongs();
      loadPlaylists();
    } catch {
      setActionError('Eliminazione del brano fallita.');
    }
  };

  const handleAddedToPlaylist = () => loadPlaylists();

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

  const handleDownload = async (song: SearchSong) => {
    if (downloadingId !== null) return;
    setDownloadingId(song.id);
    setDownloadError(null);
    setDownloaded(null);
    try {
      await musicApi.download(song);
      setDownloaded(song);
      loadSongs();
    } catch (err) {
      setDownloadError(errorMessage(err, 'Download fallito, riprova.'));
    } finally {
      setDownloadingId(null);
    }
  };

  return (
    <div className="min-h-screen bg-surface text-text">
      <DashBoard />
      <main className="mx-auto max-w-6xl space-y-8 px-4 py-6 sm:space-y-10 sm:py-10">
        <section>
          <h1 className="text-3xl font-bold">Musica</h1>
          <p className="mt-1 text-text-muted">Cerca un brano, scegli il risultato e scaricalo sul server.</p>
          <Link
            to="/music/playlists"
            className="mt-4 inline-flex items-center rounded-lg border border-border px-5 py-2 font-semibold transition-colors hover:border-accent hover:text-accent"
          >
            Playlist
          </Link>
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
              Scaricato: {downloaded.author} – {downloaded.title} ({formatDuration(downloaded.duration)})
            </p>
          )}
          {result && (
            <ul className="divide-y divide-border rounded-lg border border-border bg-surface-alt">
              {result.length === 0 && (
                <li className="px-4 py-3 text-sm text-text-muted">Nessun risultato.</li>
              )}
              {result.map((item) => (
                <li key={item.id} className="flex items-center justify-between gap-4 px-4 py-3">
                  <span className="truncate">
                    {item.title} <span className="text-text-muted">– {item.author} ({formatDuration(item.duration)})</span>
                  </span>
                  <button
                    onClick={() => handleDownload(item)}
                    disabled={downloadingId !== null}
                    className={`${smallBtn} disabled:opacity-50`}
                  >
                    {downloadingId === item.id ? 'Download…' : 'Scarica'}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section>
          <div className="mb-4 flex flex-wrap items-center justify-between gap-2 sm:gap-4">
            <button
              onClick={() => setLibraryOpen((o) => !o)}
              aria-expanded={libraryOpen}
              className="inline-flex items-center gap-2 text-sm font-semibold uppercase tracking-widest text-text-muted transition-colors hover:text-accent"
            >
              Libreria
              <span className="font-normal normal-case tracking-normal">
                ({songs.length} {songs.length === 1 ? 'brano' : 'brani'})
              </span>
              <ChevronDownIcon className={`h-4 w-4 transition-transform ${libraryOpen ? 'rotate-180' : ''}`} />
            </button>
            {songs.length > 0 && (
              <div className="flex items-center gap-2">
                <button onClick={() => play(songs, 0, false)} className={smallBtn}>
                  <PlayIcon className="h-3.5 w-3.5" /> Riproduci tutto
                </button>
                <button onClick={() => play(songs, 0, true)} aria-label="Riproduci tutto in ordine casuale" className={smallBtn}>
                  <ShuffleIcon className="h-3.5 w-3.5" />
                </button>
              </div>
            )}
          </div>
          {actionError && <p className="mb-3 text-sm text-red-400">{actionError}</p>}
          {libraryOpen && songs.length > 0 && (
            <input
              type="search"
              value={libraryFilter}
              onChange={(e) => setLibraryFilter(e.target.value)}
              placeholder="Cerca nella libreria"
              className="mb-4 w-full rounded-lg border border-border bg-surface-alt px-4 py-2 text-text outline-none focus:border-accent"
            />
          )}
          {!libraryOpen ? null : songsError ? (
            <p className="text-sm text-text-muted">Impossibile caricare le canzoni.</p>
          ) : songs.length === 0 ? (
            <p className="text-sm text-text-muted">Nessuna canzone scaricata.</p>
          ) : visibleSongs.length === 0 ? (
            <p className="text-sm text-text-muted">Nessun brano corrisponde alla ricerca.</p>
          ) : (
            <ul className="divide-y divide-border rounded-lg border border-border bg-surface-alt">
              {visibleSongs.map(({ song, i }) => (
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
                      <PlayIcon className="h-3.5 w-3.5" />
                    </button>
                    <button onClick={() => playNext([song])} className={smallBtn}>
                      Dopo
                    </button>
                    <button onClick={() => enqueue([song])} className={smallBtn}>
                      <QueueIcon className="h-3.5 w-3.5" /> Coda
                    </button>
                    <AddToPlaylist
                      songId={song.id}
                      playlists={playlists}
                      onAdded={handleAddedToPlaylist}
                    />
                    <span className="shrink-0 font-mono text-sm text-text-muted">{formatDuration(song.duration)}</span>
                    <button onClick={() => handleDeleteSong(song)} className={dangerBtn}>
                      Elimina
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>
      </main>

      {confirmAction && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4"
          onClick={() => setConfirmAction(null)}
        >
          <div
            onClick={(e) => e.stopPropagation()}
            className="w-full max-w-sm space-y-4 rounded-xl border border-border bg-surface-alt p-6"
          >
            <p>{confirmAction.message}</p>
            <div className="flex justify-end gap-2">
              <button
                onClick={() => setConfirmAction(null)}
                className="rounded-lg border border-border px-4 py-2 transition-colors hover:border-accent"
              >
                Annulla
              </button>
              <button
                autoFocus
                onClick={() => {
                  const { run } = confirmAction;
                  setConfirmAction(null);
                  run();
                }}
                className="rounded-lg bg-red-500 px-5 py-2 font-semibold text-white transition-colors hover:bg-red-600"
              >
                Elimina
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
