import { useCallback, useEffect, useRef, useState } from 'react';
import { API_BASE_URL } from '../api/config';
import { useMusic, type QueueItem } from '../context/musicContext';
import { QueuePanel } from './QueuePanel';
import { NowPlaying } from './NowPlaying';
import { Cover } from './Cover';
import {
  ChevronDownIcon,
  CloseIcon,
  MusicIcon,
  NextIcon,
  PauseIcon,
  PlayIcon,
  PrevIcon,
  QueueIcon,
  RepeatIcon,
  ShuffleIcon,
} from './Icons';

interface MusicPlayerProps {
  compact: boolean;
}

const streamUrl = (songId: string) => `${API_BASE_URL}/songs/${encodeURIComponent(songId)}/stream`;

// Un solo elemento audio per tutta l'app, fuori da React: non viene mai rimontato, così iOS
// permette di cambiare brano a schermo bloccato / da Dynamic Island.
const player = new Audio();
player.preload = 'auto';

const loadItem = (item: QueueItem) => {
  if (player.dataset.uid === String(item.uid)) return;
  player.dataset.uid = String(item.uid);
  player.src = streamUrl(item.song.id);
  void player.play().catch(() => undefined);
};

const unload = () => {
  player.pause();
  delete player.dataset.uid;
  player.removeAttribute('src');
  player.load();
};

const formatTime = (seconds: number) => {
  if (!Number.isFinite(seconds)) return '0:00';
  const m = Math.floor(seconds / 60);
  const sec = Math.floor(seconds % 60).toString().padStart(2, '0');
  return `${m}:${sec}`;
};

const btn =
  'inline-flex min-h-11 items-center justify-center rounded-md border border-border px-3 py-1 text-sm transition-colors hover:border-accent hover:text-accent disabled:opacity-40 sm:min-h-0';
const btnActive = 'border-accent text-accent';
const btnDanger = 'hover:!border-red-400 hover:!text-red-400';

export const MusicPlayer = ({ compact }: MusicPlayerProps) => {
  const { queue, index, shuffle, repeat, hidden, next, prev, toggleShuffle, cycleRepeat, setHidden, close } =
    useMusic();
  const [failedUid, setFailedUid] = useState<number | null>(null);
  const failures = useRef(0);
  const [playing, setPlaying] = useState(false);
  const [time, setTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [queueOpen, setQueueOpen] = useState(false);
  const [expanded, setExpanded] = useState(false);

  const item = queue[index];
  const song = item?.song;
  const hasPrev = index > 0;
  const nextItem = queue[index + 1] ?? ((repeat === 'all' || shuffle) && queue.length > 1 ? queue[0] : undefined);
  const hasNext = !!nextItem;
  const error = failedUid === item?.uid;

  // Il brano va caricato in modo sincrono dentro il gesto dell'utente (o del tasto di sistema),
  // poi si aggiorna lo stato React.
  // L'elemento da caricare lo decide il provider (stato sincrono), non il render corrente: con
  // l'app in background i render possono arrivare in ritardo e gli skip veloci userebbero dati vecchi.
  const goNext = useCallback(() => {
    const target = next();
    if (target) loadItem(target);
  }, [next]);
  const goPrev = useCallback(() => {
    const target = prev();
    if (target) loadItem(target);
  }, [prev]);

  // Allinea l'audio alla coda (play(), jumpTo, rimozioni...). Se è già caricato non fa nulla.
  useEffect(() => {
    if (item) loadItem(item);
    else unload();
  }, [item]);

  useEffect(() => {
    const onTime = () => {
      setTime(player.currentTime);
      if ('mediaSession' in navigator && Number.isFinite(player.duration) && player.duration > 0) {
        try {
          navigator.mediaSession.setPositionState({
            duration: player.duration,
            position: Math.min(player.currentTime, player.duration),
            playbackRate: player.playbackRate,
          });
        } catch {
          // alcuni browser rifiutano valori fuori range
        }
      }
    };
    const onDuration = () => setDuration(Number.isFinite(player.duration) ? player.duration : 0);
    const onPlay = () => setPlaying(true);
    const onPause = () => setPlaying(false);
    const onError = () => {
      const uid = Number(player.dataset.uid);
      setFailedUid(uid);
      // Passa al brano successivo, ma si ferma se tutta la coda è fallita di fila o non c'è un successivo
      failures.current += 1;
      if (failures.current >= queue.length) return;
      const target = next();
      if (target && target.uid !== uid) loadItem(target);
    };
    const onPlaying = () => {
      failures.current = 0;
    };
    const onEnded = () => {
      if (repeat === 'one' || ((repeat === 'all' || shuffle) && queue.length === 1)) {
        player.currentTime = 0;
        void player.play();
      } else {
        goNext();
      }
    };
    player.addEventListener('timeupdate', onTime);
    player.addEventListener('durationchange', onDuration);
    player.addEventListener('play', onPlay);
    player.addEventListener('pause', onPause);
    player.addEventListener('error', onError);
    player.addEventListener('playing', onPlaying);
    player.addEventListener('ended', onEnded);
    return () => {
      player.removeEventListener('timeupdate', onTime);
      player.removeEventListener('durationchange', onDuration);
      player.removeEventListener('play', onPlay);
      player.removeEventListener('pause', onPause);
      player.removeEventListener('error', onError);
      player.removeEventListener('playing', onPlaying);
      player.removeEventListener('ended', onEnded);
    };
  }, [repeat, shuffle, queue.length, goNext, next]);

  useEffect(() => {
    if (!song || !('mediaSession' in navigator)) return;
    navigator.mediaSession.metadata = new MediaMetadata({ title: song.title, artist: song.author });
  }, [song]);

  useEffect(() => {
    if (!song || !('mediaSession' in navigator)) return;
    const ms = navigator.mediaSession;
    ms.setActionHandler('play', () => void player.play());
    ms.setActionHandler('pause', () => player.pause());
    ms.setActionHandler('previoustrack', goPrev);
    ms.setActionHandler('nexttrack', goNext);
    // Niente seekbackward/seekforward: su iOS sostituirebbero i tasti precedente/successivo
    ms.setActionHandler('seekto', (d) => {
      if (d.seekTime != null) player.currentTime = d.seekTime;
    });
    return () => {
      for (const a of ['play', 'pause', 'previoustrack', 'nexttrack', 'seekto'] as const) {
        ms.setActionHandler(a, null);
      }
    };
  }, [song, goPrev, goNext]);

  if (!song) return null;

  const handleClose = () => {
    setQueueOpen(false);
    setExpanded(false);
    close();
  };

  const togglePlay = () => {
    if (player.paused) void player.play();
    else player.pause();
  };

  const nowPlaying = expanded ? (
    <NowPlaying
      song={song}
      playing={playing}
      error={error}
      time={time}
      duration={duration}
      hasPrev={hasPrev}
      hasNext={hasNext}
      shuffle={shuffle}
      repeat={repeat}
      queueCount={queue.length}
      onSeek={(t) => {
        player.currentTime = t;
        setTime(t);
      }}
      onTogglePlay={togglePlay}
      onPrev={goPrev}
      onNext={goNext}
      onToggleShuffle={toggleShuffle}
      onCycleRepeat={cycleRepeat}
      onStop={handleClose}
      onClose={() => setExpanded(false)}
    />
  ) : null;

  const hide = () => {
    setQueueOpen(false);
    setHidden(true);
  };

  const repeatBtn = (extra: string) => (
    <button
      onClick={cycleRepeat}
      aria-pressed={repeat !== 'off'}
      aria-label="Ripetizione"
      title={repeat === 'one' ? 'Ripeti brano' : repeat === 'all' ? 'Ripeti coda' : 'Ripetizione disattivata'}
      className={`${btn} ${repeat !== 'off' ? btnActive : ''} ${extra}`}
    >
      <RepeatIcon one={repeat === 'one'} />
    </button>
  );

  const hideBtn = (extra: string) => (
    <button onClick={hide} aria-label="Nascondi player" title="Nascondi" className={`${btn} ${extra}`}>
      <ChevronDownIcon />
    </button>
  );

  const shuffleBtn = (extra: string) => (
    <button
      onClick={toggleShuffle}
      aria-pressed={shuffle}
      aria-label="Riproduzione casuale"
      title={shuffle ? 'Casuale attivo' : 'Casuale disattivato'}
      className={`${btn} ${shuffle ? btnActive : ''} ${extra}`}
    >
      <ShuffleIcon />
    </button>
  );

  const queueBtn = (extra: string) => (
    <button
      onClick={() => setQueueOpen((v) => !v)}
      aria-pressed={queueOpen}
      aria-label="Mostra coda"
      className={`${btn} ${queueOpen ? btnActive : ''} ${extra}`}
    >
      <QueueIcon />
      <span className="ml-1.5 text-xs">{queue.length}</span>
    </button>
  );

  const seekBar = (
    <div className="order-3 flex w-full basis-full sm:order-4 items-center gap-2 sm:flex-[2] sm:basis-64">
      <span className="w-10 shrink-0 text-right font-mono text-xs text-text-muted">{formatTime(time)}</span>
      <input
        type="range"
        min={0}
        max={duration || 0}
        step={1}
        value={Math.min(time, duration || 0)}
        onChange={(e) => {
          player.currentTime = Number(e.target.value);
          setTime(player.currentTime);
        }}
        aria-label="Posizione nel brano"
        className="h-6 min-w-0 flex-1 cursor-pointer accent-accent"
      />
      <span className="w-10 shrink-0 font-mono text-xs text-text-muted">{formatTime(duration)}</span>
    </div>
  );

  const playBtn = (extra: string) => (
    <button
      onClick={togglePlay}
      aria-label={playing ? 'Pausa' : 'Riproduci'}
      className={`${btn} ${extra}`}
    >
      {playing ? <PauseIcon /> : <PlayIcon />}
    </button>
  );

  if (hidden) {
    return (
      <>
        <button
          onClick={() => setHidden(false)}
          aria-label="Mostra player"
          title={`${song.title} – ${song.author}`}
          className="fixed bottom-[calc(0.75rem+env(safe-area-inset-bottom))] right-3 z-40 inline-flex h-11 w-11 items-center justify-center rounded-full border border-border bg-surface-alt/90 text-text-muted shadow-lg backdrop-blur transition-colors hover:border-accent hover:text-accent"
        >
          {playing ? <MusicIcon className="h-5 w-5 text-accent" /> : <MusicIcon className="h-5 w-5" />}
        </button>
      </>
    );
  }

  if (compact) {
    return (
      <>
      <div className="fixed bottom-3 right-3 z-40 flex max-w-[calc(100vw-1.5rem)] items-center gap-2 rounded-full border border-border bg-surface-alt/90 px-3 py-1.5 opacity-60 shadow-lg backdrop-blur transition-opacity focus-within:opacity-100 hover:opacity-100">
        {queueOpen && <QueuePanel
            onClear={handleClose}
            className="absolute bottom-full right-0 mb-2 w-80 max-w-[calc(100vw-1.5rem)]"
          />}
        <button
          onClick={() => setExpanded(true)}
          aria-label="Apri schermata di riproduzione"
          className="hidden max-w-40 truncate text-xs text-text hover:text-accent sm:inline"
          title={`${song.title} – ${song.author}`}
        >
          {error ? 'Errore di riproduzione' : song.title}
        </button>
        <button onClick={() => setExpanded(true)} aria-label="Apri schermata di riproduzione" className={btn}>
          <ChevronDownIcon className="h-4 w-4 rotate-180" />
        </button>
        <button onClick={goPrev} disabled={!hasPrev} aria-label="Precedente" className={btn}>
          <PrevIcon />
        </button>
        <button onClick={togglePlay} aria-label={playing ? 'Pausa' : 'Riproduci'} className={btn}>
          {playing ? <PauseIcon /> : <PlayIcon />}
        </button>
        <button onClick={goNext} disabled={!hasNext} aria-label="Successivo" className={btn}>
          <NextIcon />
        </button>
        {shuffleBtn('')}
        {repeatBtn('')}
        {queueBtn('')}
        {hideBtn('')}
        <button onClick={handleClose} aria-label="Chiudi player" className={`${btn} ${btnDanger}`}>
          <CloseIcon />
        </button>
      </div>
      {nowPlaying}
      </>
    );
  }

  return (
    <div className="fixed inset-x-0 bottom-0 z-40 border-t border-border bg-surface-alt px-4 pt-3 pb-[calc(0.75rem+env(safe-area-inset-bottom))] shadow-xl">
      {queueOpen && (
        <QueuePanel
          onClear={handleClose}
          className="absolute bottom-full inset-x-3 mb-2 sm:inset-x-auto sm:right-4 sm:w-full sm:max-w-md"
        />
      )}
      {/* Mobile: info + chiudi, audio a tutta larghezza, poi i pulsanti. Desktop: tutto su una riga. */}
      <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-2 sm:gap-4">
        <button
          onClick={() => setExpanded(true)}
          aria-label="Apri schermata di riproduzione"
          className="order-1 flex min-w-0 flex-1 basis-0 items-center gap-3 text-left sm:basis-48"
        >
          <Cover seed={song.title} className="h-12 w-12" iconClass="h-5 w-5" />
          <span className="min-w-0">
            <span className="block truncate font-medium text-text">{song.title}</span>
            <span className="block truncate text-sm text-text-muted">{song.author}</span>
            {error && <span className="block text-sm text-red-400">Impossibile riprodurre il brano.</span>}
          </span>
        </button>
        {hideBtn('order-2 sm:order-10')}
        <button
          onClick={handleClose}
          aria-label="Chiudi player"
          className={`${btn} ${btnDanger} order-2 sm:order-11`}
        >
          <CloseIcon />
        </button>
        {seekBar}
        <button
          onClick={goPrev}
          disabled={!hasPrev}
          aria-label="Precedente"
          className={`${btn} order-4 flex-1 sm:order-2 sm:flex-none`}
        >
          <PrevIcon />
        </button>
        {playBtn('order-5 flex-1 sm:order-3 sm:flex-none')}
        <button
          onClick={goNext}
          disabled={!hasNext}
          aria-label="Successivo"
          className={`${btn} order-5 flex-1 sm:order-5 sm:flex-none`}
        >
          <NextIcon />
        </button>
        {shuffleBtn('order-6 flex-1 sm:order-6 sm:flex-none')}
        {repeatBtn('order-6 flex-1 sm:order-7 sm:flex-none')}
        {queueBtn('order-7 flex-1 sm:order-8 sm:flex-none')}
      </div>
      {nowPlaying}
    </div>
  );
};
