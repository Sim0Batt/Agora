import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { DashBoard } from '../components/Dashboard';
import { Cover } from '../components/Cover';
import { musicApi } from '../api/music';
import { onImportFinished, startImport } from '../context/importJobs';
import { PlayIcon } from '../components/Icons';
import { useMusic } from '../context/musicContext';
import { errorMessage } from '../components/musicUi';
import type { Playlist, Song } from '../types/models';

export const PlaylistsPage = () => {
  const navigate = useNavigate();
  const { play } = useMusic();
  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [error, setError] = useState(false);
  const [newPlaylist, setNewPlaylist] = useState('');
  const [creating, setCreating] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const [importOpen, setImportOpen] = useState(false);
  const [importTitle, setImportTitle] = useState('');
  const [importUrl, setImportUrl] = useState('');

  const load = useCallback(() => {
    musicApi
      .listPlaylists()
      .then((data) => {
        setPlaylists(data);
        setError(false);
      })
      .catch(() => setError(true));
  }, []);

  useEffect(() => {
    load();
    return onImportFinished(load);
  }, [load]);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    const title = newPlaylist.trim();
    if (!title || creating) return;
    setCreating(true);
    setActionError(null);
    try {
      await musicApi.createPlaylist(title);
      setNewPlaylist('');
      load();
    } catch (err) {
      setActionError(errorMessage(err, 'Creazione della playlist fallita.'));
    } finally {
      setCreating(false);
    }
  };

  const handleImport = (e: React.FormEvent) => {
    e.preventDefault();
    const title = importTitle.trim();
    const url = importUrl.trim();
    if (!title || !url) return;
    startImport(title, url);
    setImportTitle('');
    setImportUrl('');
    setImportOpen(false);
  };

  const quickPlay = async (playlist: Playlist) => {
    setActionError(null);
    try {
      const songs: Song[] = await musicApi.listPlaylistSongs(playlist.id);
      if (songs.length === 0) setActionError(`La playlist "${playlist.title}" è vuota.`);
      else play(songs, 0, false);
    } catch {
      setActionError('Impossibile caricare i brani della playlist.');
    }
  };

  return (
    <div className="min-h-screen bg-surface text-text">
      <DashBoard />
      <main className="mx-auto max-w-6xl space-y-6 px-4 py-6 sm:py-10">
        <div className="flex flex-wrap items-end justify-between gap-3">
          <div>
            <Link to="/music" className="text-sm text-text-muted transition-colors hover:text-accent">
              ← Musica
            </Link>
            <h1 className="text-3xl font-bold">Playlist</h1>
          </div>
          <button
            type="button"
            onClick={() => setImportOpen(true)}
            className="rounded-lg border border-border px-5 py-2 font-semibold transition-colors hover:border-accent hover:text-accent"
          >
            Importa da YouTube
          </button>
        </div>

        <form onSubmit={handleCreate} className="flex gap-2">
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
        {actionError && <p className="text-sm text-red-400">{actionError}</p>}

        {error ? (
          <p className="text-sm text-text-muted">Impossibile caricare le playlist.</p>
        ) : playlists.length === 0 ? (
          <p className="text-sm text-text-muted">Nessuna playlist.</p>
        ) : (
          <ul className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5">
            {playlists.map((playlist) => (
              <li
                key={playlist.id}
                className="group relative rounded-xl bg-surface-alt p-3 transition-colors hover:bg-border/40"
              >
                <button
                  onClick={() => navigate(`/music/playlists/${playlist.id}`)}
                  className="block w-full text-left"
                >
                  <Cover seed={playlist.title} className="aspect-square w-full" iconClass="h-1/3 w-1/3" />
                  <p className="mt-3 truncate font-semibold">{playlist.title}</p>
                  <p className="truncate text-sm text-text-muted">
                    {playlist.count} {playlist.count === 1 ? 'brano' : 'brani'}
                  </p>
                </button>
                <button
                  onClick={() => quickPlay(playlist)}
                  aria-label={`Riproduci ${playlist.title}`}
                  className="absolute right-5 top-[calc(100%-5.5rem)] flex h-11 w-11 items-center justify-center rounded-full bg-accent text-white shadow-lg transition-all sm:translate-y-2 sm:opacity-0 sm:group-hover:translate-y-0 sm:group-hover:opacity-100"
                >
                  <PlayIcon className="h-5 w-5" />
                </button>
              </li>
            ))}
          </ul>
        )}
      </main>

      {importOpen && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4"
          onClick={() => setImportOpen(false)}
        >
          <form
            onSubmit={handleImport}
            onClick={(e) => e.stopPropagation()}
            className="w-full max-w-md space-y-4 rounded-xl border border-border bg-surface-alt p-6"
          >
            <h2 className="text-lg font-semibold">Importa da Playlist</h2>
            <input
              value={importTitle}
              onChange={(e) => setImportTitle(e.target.value)}
              placeholder="Titolo della playlist"
              maxLength={100}
              autoFocus
              className="w-full rounded-lg border border-border bg-surface px-4 py-2 text-text outline-none focus:border-accent"
            />
            <input
              value={importUrl}
              onChange={(e) => setImportUrl(e.target.value)}
              placeholder="URL della playlist YouTube"
              className="w-full rounded-lg border border-border bg-surface px-4 py-2 text-text outline-none focus:border-accent"
            />
            <p className="text-sm text-text-muted">L'importazione prosegue in background mentre usi l'app.</p>
            <div className="flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setImportOpen(false)}
                className="rounded-lg border border-border px-4 py-2 transition-colors hover:border-accent"
              >
                Annulla
              </button>
              <button
                type="submit"
                disabled={!importTitle.trim() || !importUrl.trim()}
                className="rounded-lg bg-accent px-5 py-2 font-semibold text-white transition-colors hover:bg-accent-hover disabled:opacity-50"
              >
                Importa
              </button>
            </div>
          </form>
        </div>
      )}

    </div>
  );
};
