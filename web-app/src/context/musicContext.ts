import { createContext, useContext } from 'react';
import type { Song } from '../types/models';

export interface QueueItem {
  uid: number;
  song: Song;
}

export type RepeatMode = 'off' | 'all' | 'one';

export interface MusicContextValue {
  queue: QueueItem[];
  index: number;
  shuffle: boolean;
  repeat: RepeatMode;
  /** Player nascosto (la riproduzione continua). */
  hidden: boolean;
  /** Sostituisce la coda. `shuffle` assente = mantiene la modalità corrente. */
  play: (songs: Song[], index?: number, shuffle?: boolean) => void;
  enqueue: (songs: Song[]) => void;
  playNext: (songs: Song[]) => void;
  jumpTo: (index: number) => void;
  next: () => void;
  prev: () => void;
  removeAt: (index: number) => void;
  toggleShuffle: () => void;
  cycleRepeat: () => void;
  setHidden: (hidden: boolean) => void;
  close: () => void;
}

export const MusicContext = createContext<MusicContextValue | null>(null);

export const useMusic = (): MusicContextValue => {
  const ctx = useContext(MusicContext);
  if (!ctx) throw new Error('useMusic deve essere usato dentro MusicProvider');
  return ctx;
};
