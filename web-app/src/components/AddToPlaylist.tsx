import { useState } from 'react';
import { musicApi } from '../api/music';
import type { Playlist } from '../types/models';

interface AddToPlaylistProps {
  songId: string;
  playlists: Playlist[];
  onAdded: (playlistId: string) => void;
}

export const AddToPlaylist = ({ songId, playlists, onAdded }: AddToPlaylistProps) => {
  const [playlistId, setPlaylistId] = useState('');
  const [busy, setBusy] = useState(false);
  const [status, setStatus] = useState<'ok' | 'error' | null>(null);

  const handleAdd = async () => {
    if (!playlistId || busy) return;
    setBusy(true);
    setStatus(null);
    try {
      await musicApi.addSongToPlaylist(playlistId, songId);
      setStatus('ok');
      onAdded(playlistId);
    } catch {
      // Fallisce anche se la canzone è già nella playlist (chiave primaria duplicata)
      setStatus('error');
    } finally {
      setBusy(false);
    }
  };

  if (playlists.length === 0) {
    return <span className="text-sm text-text-muted">Crea prima una playlist</span>;
  }

  return (
    <div className="flex flex-wrap items-center gap-2">
      <select
        value={playlistId}
        onChange={(e) => {
          setPlaylistId(e.target.value);
          setStatus(null);
        }}
        className="min-h-11 rounded-md border border-border bg-surface px-2 py-1 text-base text-text outline-none focus:border-accent sm:min-h-0 sm:text-sm"
      >
        <option value="">Playlist…</option>
        {playlists.map((p) => (
          <option key={p.id} value={p.id}>
            {p.title}
          </option>
        ))}
      </select>
      <button
        onClick={handleAdd}
        disabled={!playlistId || busy}
        className="inline-flex min-h-11 items-center justify-center rounded-md border border-border px-3 py-1 text-sm transition-colors hover:border-accent hover:text-accent disabled:opacity-50 sm:min-h-0"
      >
        {busy ? 'Aggiunta…' : 'Aggiungi'}
      </button>
      {status === 'ok' && <span className="text-sm text-green-400">Aggiunta</span>}
      {status === 'error' && <span className="text-sm text-red-400">Errore o già presente</span>}
    </div>
  );
};
