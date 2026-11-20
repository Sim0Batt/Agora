import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { DashBoard } from '../components/Dashboard';
import { Cover } from '../components/Cover';
import { musicApi } from '../api/music';
import { CloseIcon, PlayIcon, QueueIcon, ShuffleIcon } from '../components/Icons';
import { useMusic } from '../context/musicContext';
import { dangerBtn, formatDuration, smallBtn } from '../components/musicUi';
import type { Playlist, Song } from '../types/models';

export const PlaylistDetailPage = () => {
  const { id = '' } = useParams();
  const navigate = useNavigate();
  const { queue, index, play, enqueue, playNext } = useMusic();
  const [playlist, setPlaylist] = useState<Playlist | null>(null);
  const [songs, setSongs] = useState<Song[]>([]);
  const [error, setError] = useState(false);
  const [loaded, setLoaded] = useState(false);
  const [filter, setFilter] = useState('');
  const [actionError, setActionError] = useState<string | null>(null);
  const [confirmDelete, setConfirmDelete] = useState(false);

  const load = useCallback(() => {
    Promise.all([musicApi.listPlaylists(), musicApi.listPlaylistSongs(id)])
      .then(([playlists, data]) => {
        setPlaylist(playlists.find((p) => p.id === id) ?? null);
        setSongs(data);
        setError(false);
      })
      .catch(() => setError(true))
      .finally(() => setLoaded(true));
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  const currentId = queue[index]?.song.id;
  const totalSeconds = useMemo(() => songs.reduce((sum, s) => sum + s.duration, 0), [songs]);

  const visible = useMemo(() => {
    const q = filter.trim().toLowerCase();
    if (!q) return songs.map((song, i) => ({ song, i }));
    return songs
      .map((song, i) => ({ song, i }))
      .filter(({ song }) => song.title.toLowerCase().includes(q) || song.author.toLowerCase().includes(q));
  }, [songs, filter]);

  const handleRemove = async (song: Song) => {
    setActionError(null);
    try {
      await musicApi.removeSongFromPlaylist(id, song.id);
      load();
    } catch {
      setActionError('Rimozione del brano dalla playlist fallita.');
    }
  };

  const handleDelete = async () => {
    setConfirmDelete(false);
    try {
      await musicApi.deletePlaylist(id);
      navigate('/music/playlists');
    } catch {
      setActionError('Eliminazione della playlist fallita.');
    }
  };

  const title = playlist?.title ?? 'Playlist';
  const minutes = Math.round(totalSeconds / 60);

  return (
    <div className="min-h-screen bg-surface text-text">
      <DashBoard />
      <main className="mx-auto max-w-4xl px-4 py-6 sm:py-10">
        <Link to="/music/playlists" className="text-sm text-text-muted transition-colors hover:text-accent">
          ← Playlist
        </Link>

        {error ? (
          <p className="mt-6 text-sm text-text-muted">Impossibile caricare la playlist.</p>
        ) : !loaded ? null : !playlist ? (
          <p className="mt-6 text-sm text-text-muted">Playlist non trovata.</p>
        ) : (
          <>
            <header className="mt-4 flex flex-col items-center gap-5 sm:flex-row sm:items-end">
              <Cover seed={title} className="h-44 w-44 sm:h-52 sm:w-52" iconClass="h-1/3 w-1/3" />
              <div className="min-w-0 text-center sm:text-left">
                <p className="text-xs font-semibold uppercase tracking-widest text-text-muted">Playlist</p>
                <h1 className="mt-1 break-words text-3xl font-bold sm:text-5xl">{title}</h1>
                <p className="mt-2 text-sm text-text-muted">
                  {songs.length} {songs.length === 1 ? 'brano' : 'brani'}
                  {songs.length > 0 && ` · ${minutes} min`}
                </p>
              </div>
            </header>

            <div className="mt-6 flex flex-wrap items-center gap-3">
              <button
                onClick={() => play(songs, 0, false)}
                disabled={songs.length === 0}
                aria-label="Riproduci playlist"
                className="flex h-14 w-14 items-center justify-center rounded-full bg-accent text-white shadow-lg transition-colors hover:bg-accent-hover disabled:opacity-40"
              >
                <PlayIcon className="h-7 w-7" />
              </button>
              <button
                onClick={() => play(songs, Math.floor(Math.random() * songs.length), true)}
                disabled={songs.length === 0}
                aria-label="Riproduci in ordine casuale"
                className="flex h-11 w-11 items-center justify-center rounded-full border border-border transition-colors hover:border-accent hover:text-accent disabled:opacity-40"
              >
                <ShuffleIcon className="h-5 w-5" />
              </button>
              <button
                onClick={() => enqueue(songs)}
                disabled={songs.length === 0}
                aria-label="Accoda playlist"
                className="flex h-11 w-11 items-center justify-center rounded-full border border-border transition-colors hover:border-accent hover:text-accent disabled:opacity-40"
              >
                <QueueIcon className="h-5 w-5" />
              </button>
              <button onClick={() => setConfirmDelete(true)} className={`${dangerBtn} ml-auto`}>
                Elimina playlist
              </button>
            </div>

            <input
              type="search"
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
              placeholder="Cerca nella playlist"
              className="mt-6 w-full rounded-lg border border-border bg-surface-alt px-4 py-2 text-text outline-none focus:border-accent"
            />
            {actionError && <p className="mt-3 text-sm text-red-400">{actionError}</p>}

            {songs.length === 0 ? (
              <p className="mt-6 text-sm text-text-muted">Playlist vuota.</p>
            ) : visible.length === 0 ? (
              <p className="mt-6 text-sm text-text-muted">Nessun brano corrisponde alla ricerca.</p>
            ) : (
              <ul className="mt-4 divide-y divide-border/50">
                {visible.map(({ song, i }) => {
                  const current = song.id === currentId;
                  return (
                    <li
                      key={song.id}
                      className={`flex items-center gap-3 rounded-md px-2 py-2 transition-colors hover:bg-surface-alt ${current ? 'bg-accent/10' : ''}`}
                    >
                      <button
                        onClick={() => play(songs, i)}
                        aria-label={`Riproduci ${song.title}`}
                        className="flex min-w-0 flex-1 items-center gap-3 text-left"
                      >
                        <span className="w-5 shrink-0 text-center text-sm text-text-muted">
                          {current ? <PlayIcon className="mx-auto h-4 w-4 text-accent" /> : i + 1}
                        </span>
                        <Cover seed={song.title} className="h-11 w-11" iconClass="h-5 w-5" />
                        <span className="min-w-0">
                          <span className={`block truncate font-medium ${current ? 'text-accent' : ''}`}>
                            {song.title}
                          </span>
                          <span className="block truncate text-sm text-text-muted">{song.author}</span>
                        </span>
                      </button>
                      <span className="hidden shrink-0 font-mono text-sm text-text-muted sm:inline">
                        {formatDuration(song.duration)}
                      </span>
                      <button onClick={() => playNext([song])} className={`${smallBtn} hidden sm:inline-flex`}>
                        Dopo
                      </button>
                      <button
                        onClick={() => enqueue([song])}
                        aria-label={`Accoda ${song.title}`}
                        className={smallBtn}
                      >
                        <QueueIcon className="h-3.5 w-3.5" />
                      </button>
                      <button
                        onClick={() => handleRemove(song)}
                        aria-label={`Rimuovi ${song.title} dalla playlist`}
                        className={dangerBtn}
                      >
                        <CloseIcon className="h-3.5 w-3.5" />
                      </button>
                    </li>
                  );
                })}
              </ul>
            )}
          </>
        )}
      </main>

      {confirmDelete && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4"
          onClick={() => setConfirmDelete(false)}
        >
          <div
            onClick={(e) => e.stopPropagation()}
            className="w-full max-w-sm space-y-4 rounded-xl border border-border bg-surface-alt p-6"
          >
            <p>Eliminare la playlist "{title}"?</p>
            <div className="flex justify-end gap-2">
              <button
                onClick={() => setConfirmDelete(false)}
                className="rounded-lg border border-border px-4 py-2 transition-colors hover:border-accent"
              >
                Annulla
              </button>
              <button
                onClick={handleDelete}
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
