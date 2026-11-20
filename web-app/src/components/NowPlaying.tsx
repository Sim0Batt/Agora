import { useEffect, useState } from 'react';
import { musicApi } from '../api/music';
import { AddToPlaylist } from './AddToPlaylist';
import { Cover } from './Cover';
import {
  ChevronDownIcon,
  CloseIcon,
  NextIcon,
  PauseIcon,
  PlayIcon,
  PrevIcon,
  QueueIcon,
  RepeatIcon,
  ShuffleIcon,
} from './Icons';
import { QueuePanel } from './QueuePanel';
import { formatDuration } from './musicUi';
import type { RepeatMode } from '../context/musicContext';
import type { Playlist, Song } from '../types/models';

interface NowPlayingProps {
  song: Song;
  playing: boolean;
  error: boolean;
  time: number;
  duration: number;
  hasPrev: boolean;
  hasNext: boolean;
  shuffle: boolean;
  repeat: RepeatMode;
  queueCount: number;
  onSeek: (seconds: number) => void;
  onTogglePlay: () => void;
  onPrev: () => void;
  onNext: () => void;
  onToggleShuffle: () => void;
  onCycleRepeat: () => void;
  onStop: () => void;
  onClose: () => void;
}

const sideBtn = (active: boolean) =>
  `flex h-12 w-12 items-center justify-center rounded-full transition-colors ${active ? 'text-accent' : 'text-text-muted hover:text-text'}`;

export const NowPlaying = (p: NowPlayingProps) => {
  const { onClose } = p;
  const [queueOpen, setQueueOpen] = useState(false);
  const [playlistsOpen, setPlaylistsOpen] = useState(false);
  const [playlists, setPlaylists] = useState<Playlist[]>([]);

  useEffect(() => {
    if (!playlistsOpen) return;
    musicApi
      .listPlaylists()
      .then(setPlaylists)
      .catch(() => setPlaylists([]));
  }, [playlistsOpen]);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);

  const remaining = Math.max(0, p.duration - p.time);

  return (
    <div
      className="fixed inset-0 z-[60] flex items-stretch justify-center bg-black/70 sm:items-center sm:p-6"
      onClick={p.onClose}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-label="In riproduzione"
        className="flex h-full w-full flex-col overflow-y-auto bg-gradient-to-b from-[#3a3d4d] to-surface px-6 pb-[calc(1.5rem+env(safe-area-inset-bottom))] pt-[calc(1rem+env(safe-area-inset-top))] sm:h-auto sm:max-h-full sm:max-w-md sm:rounded-2xl sm:border sm:border-border sm:shadow-2xl"
      >
        <div className="flex items-center justify-between">
          <button
            onClick={p.onClose}
            aria-label="Chiudi"
            className="-ml-2 flex h-11 w-11 items-center justify-center rounded-full transition-colors hover:text-accent"
          >
            <ChevronDownIcon className="h-7 w-7" />
          </button>
          <span className="text-sm font-semibold">In riproduzione</span>
          <button
            onClick={p.onStop}
            aria-label="Ferma e chiudi player"
            title="Ferma e chiudi player"
            className="-mr-2 flex h-11 w-11 items-center justify-center rounded-full text-text-muted transition-colors hover:text-red-400"
          >
            <CloseIcon className="h-5 w-5" />
          </button>
        </div>

        <div className="my-6 flex min-h-0 flex-1 items-center justify-center sm:my-5 sm:flex-none">
          {queueOpen ? (
            <QueuePanel onClear={p.onStop} className="w-full" />
          ) : (
            <Cover
              seed={p.song.title}
              className="aspect-square w-full max-w-sm !rounded-xl"
              iconClass="h-1/4 w-1/4"
            />
          )}
        </div>

        <div className="flex items-center justify-between gap-4">
          <div className="min-w-0">
            <h2 className="truncate text-2xl font-bold">{p.song.title}</h2>
            <p className="truncate text-text-muted">{p.song.author}</p>
            {p.error && <p className="text-sm text-red-400">Impossibile riprodurre il brano.</p>}
          </div>
          <button
            onClick={() => setPlaylistsOpen((v) => !v)}
            aria-label="Aggiungi a una playlist"
            aria-pressed={playlistsOpen}
            className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-full border text-2xl leading-none transition-colors ${playlistsOpen ? 'border-accent text-accent' : 'border-text-muted text-text hover:border-accent hover:text-accent'}`}
          >
            +
          </button>
        </div>
        {playlistsOpen && (
          <div className="mt-3">
            <AddToPlaylist key={p.song.id} songId={p.song.id} playlists={playlists} onAdded={() => undefined} />
          </div>
        )}

        <div className="mt-5">
          <input
            type="range"
            min={0}
            max={p.duration || 0}
            step={1}
            value={Math.min(p.time, p.duration || 0)}
            onChange={(e) => p.onSeek(Number(e.target.value))}
            aria-label="Posizione nel brano"
            className="h-6 w-full cursor-pointer accent-accent"
          />
          <div className="flex justify-between font-mono text-xs text-text-muted">
            <span>{formatDuration(p.time)}</span>
            <span>-{formatDuration(remaining)}</span>
          </div>
        </div>

        <div className="mt-2 flex items-center justify-between">
          <button onClick={p.onToggleShuffle} aria-pressed={p.shuffle} aria-label="Riproduzione casuale" className={sideBtn(p.shuffle)}>
            <ShuffleIcon className="h-6 w-6" />
          </button>
          <button
            onClick={p.onPrev}
            disabled={!p.hasPrev}
            aria-label="Precedente"
            className="flex h-14 w-14 items-center justify-center rounded-full transition-colors hover:text-accent disabled:opacity-30"
          >
            <PrevIcon className="h-9 w-9" />
          </button>
          <button
            onClick={p.onTogglePlay}
            aria-label={p.playing ? 'Pausa' : 'Riproduci'}
            className="flex h-20 w-20 items-center justify-center rounded-full bg-white text-black shadow-lg transition-transform hover:scale-105"
          >
            {p.playing ? <PauseIcon className="h-9 w-9" /> : <PlayIcon className="h-9 w-9" />}
          </button>
          <button
            onClick={p.onNext}
            disabled={!p.hasNext}
            aria-label="Successivo"
            className="flex h-14 w-14 items-center justify-center rounded-full transition-colors hover:text-accent disabled:opacity-30"
          >
            <NextIcon className="h-9 w-9" />
          </button>
          <button
            onClick={p.onCycleRepeat}
            aria-pressed={p.repeat !== 'off'}
            aria-label="Ripetizione"
            title={p.repeat === 'one' ? 'Ripeti brano' : p.repeat === 'all' ? 'Ripeti coda' : 'Ripetizione disattivata'}
            className={sideBtn(p.repeat !== 'off')}
          >
            <RepeatIcon one={p.repeat === 'one'} className="h-6 w-6" />
          </button>
        </div>

        <div className="mt-4 flex justify-end">
          <button
            onClick={() => setQueueOpen((v) => !v)}
            aria-pressed={queueOpen}
            aria-label="Mostra coda"
            className={`inline-flex min-h-11 items-center gap-2 rounded-full px-3 transition-colors ${queueOpen ? 'text-accent' : 'text-text-muted hover:text-text'}`}
          >
            <QueueIcon className="h-6 w-6" />
            <span className="text-sm">{p.queueCount}</span>
          </button>
        </div>
      </div>
    </div>
  );
};
