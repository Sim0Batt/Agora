import { useCallback, useMemo, useState, type ReactNode } from 'react';
import type { Song } from '../types/models';
import { MusicContext, type QueueItem, type RepeatMode } from './musicContext';

interface State {
  items: QueueItem[]; // ordine di riproduzione
  original: QueueItem[]; // ordine senza shuffle, per ripristinarlo
  index: number;
  shuffle: boolean;
  repeat: RepeatMode;
  hidden: boolean;
}

const EMPTY: State = { items: [], original: [], index: 0, shuffle: false, repeat: 'off', hidden: false };

let nextUid = 0;
const wrap = (song: Song): QueueItem => ({ uid: ++nextUid, song });

const shuffled = <T,>(list: T[]): T[] => {
  const copy = [...list];
  for (let i = copy.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [copy[i], copy[j]] = [copy[j], copy[i]];
  }
  return copy;
};

// Il brano scelto resta per primo, il resto viene mescolato
const shuffleAround = (list: QueueItem[], first: number): QueueItem[] => [
  list[first],
  ...shuffled(list.filter((_, i) => i !== first)),
];

export const MusicProvider = ({ children }: { children: ReactNode }) => {
  const [state, setState] = useState<State>(EMPTY);

  const play = useCallback((songs: Song[], index = 0, shuffleOverride?: boolean) => {
    setState((prev) => {
      if (songs.length === 0) return prev;
      const shuffle = shuffleOverride ?? prev.shuffle;
      const start = Math.min(Math.max(index, 0), songs.length - 1);
      const original = songs.map(wrap);
      return shuffle
        ? { ...prev, items: shuffleAround(original, start), original, index: 0, shuffle, hidden: false }
        : { ...prev, items: original, original, index: start, shuffle, hidden: false };
    });
  }, []);

  const enqueue = useCallback(
    (songs: Song[]) => {
      if (songs.length === 0) return;
      setState((prev) => {
        if (prev.items.length === 0) {
          const original = songs.map(wrap);
          return { ...prev, items: original, original, index: 0 };
        }
        const added = songs.map(wrap);
        return { ...prev, items: [...prev.items, ...added], original: [...prev.original, ...added] };
      });
    },
    [],
  );

  const playNext = useCallback((songs: Song[]) => {
    if (songs.length === 0) return;
    setState((prev) => {
      const added = songs.map(wrap);
      if (prev.items.length === 0) return { ...prev, items: added, original: added, index: 0 };
      const items = [...prev.items.slice(0, prev.index + 1), ...added, ...prev.items.slice(prev.index + 1)];
      const currentUid = prev.items[prev.index].uid;
      const at = prev.original.findIndex((it) => it.uid === currentUid) + 1;
      const original = [...prev.original.slice(0, at), ...added, ...prev.original.slice(at)];
      return { ...prev, items, original };
    });
  }, []);

  const jumpTo = useCallback((index: number) => {
    setState((prev) =>
      index < 0 || index >= prev.items.length ? prev : { ...prev, index },
    );
  }, []);

  const next = useCallback(() => {
    setState((prev) => {
      if (prev.index < prev.items.length - 1) return { ...prev, index: prev.index + 1 };
      return prev.repeat === 'all' && prev.items.length > 1 ? { ...prev, index: 0 } : prev;
    });
  }, []);

  const prev = useCallback(() => {
    setState((p) => (p.index > 0 ? { ...p, index: p.index - 1 } : p));
  }, []);

  const removeAt = useCallback((index: number) => {
    setState((prev) => {
      const target = prev.items[index];
      if (!target) return prev;
      const items = prev.items.filter((_, i) => i !== index);
      if (items.length === 0) return { ...EMPTY, shuffle: prev.shuffle, repeat: prev.repeat };
      const original = prev.original.filter((it) => it.uid !== target.uid);
      const nextIndex = index < prev.index ? prev.index - 1 : Math.min(prev.index, items.length - 1);
      return { ...prev, items, original, index: nextIndex };
    });
  }, []);

  const toggleShuffle = useCallback(() => {
    setState((prev) => {
      if (prev.items.length === 0) return { ...prev, shuffle: !prev.shuffle };
      if (!prev.shuffle) {
        return { ...prev, shuffle: true, items: shuffleAround(prev.items, prev.index), index: 0 };
      }
      const currentUid = prev.items[prev.index].uid;
      const restored = prev.original.findIndex((it) => it.uid === currentUid);
      return { ...prev, shuffle: false, items: prev.original, index: Math.max(restored, 0) };
    });
  }, []);

  const cycleRepeat = useCallback(() => {
    const order: RepeatMode[] = ['off', 'all', 'one'];
    setState((prev) => ({ ...prev, repeat: order[(order.indexOf(prev.repeat) + 1) % order.length] }));
  }, []);

  const setHidden = useCallback((hidden: boolean) => setState((prev) => ({ ...prev, hidden })), []);

  const close = useCallback(
    () => setState((prev) => ({ ...EMPTY, shuffle: prev.shuffle, repeat: prev.repeat })),
    [],
  );

  const value = useMemo(
    () => ({
      queue: state.items,
      index: state.index,
      shuffle: state.shuffle,
      repeat: state.repeat,
      hidden: state.hidden,
      play,
      enqueue,
      playNext,
      jumpTo,
      next,
      prev,
      removeAt,
      toggleShuffle,
      cycleRepeat,
      setHidden,
      close,
    }),
    [state, play, enqueue, playNext, jumpTo, next, prev, removeAt, toggleShuffle, cycleRepeat, setHidden, close],
  );

  return <MusicContext.Provider value={value}>{children}</MusicContext.Provider>;
};
