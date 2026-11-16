import { useEffect, useState } from 'react';
import { API_BASE_URL } from '../api/config';
import { useMusic } from '../context/musicContext';
import { QueuePanel } from './QueuePanel';
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

const btn =
  'inline-flex min-h-11 items-center justify-center rounded-md border border-border px-3 py-1 text-sm transition-colors hover:border-accent hover:text-accent disabled:opacity-40 sm:min-h-0';
const btnActive = 'border-accent text-accent';
const btnDanger = 'hover:!border-red-400 hover:!text-red-400';

export const MusicPlayer = ({ compact }: MusicPlayerProps) => {
  const { queue, index, shuffle, repeat, hidden, next, prev, toggleShuffle, cycleRepeat, setHidden, close } =
    useMusic();
  const [failedUid, setFailedUid] = useState<number | null>(null);
  const [playing, setPlaying] = useState(false);
  const [audio, setAudio] = useState<HTMLAudioElement | null>(null);
  const [queueOpen, setQueueOpen] = useState(false);

  const item = queue[index];
  const song = item?.song;
  const hasPrev = index > 0;
  const hasNext = index < queue.length - 1 || (repeat === 'all' && queue.length > 1);
  const error = failedUid === item?.uid;

  useEffect(() => {
    if (!song || !('mediaSession' in navigator)) return;
    navigator.mediaSession.metadata = new MediaMetadata({ title: song.title, artist: song.author });
    navigator.mediaSession.setActionHandler('previoustrack', hasPrev ? prev : null);
    navigator.mediaSession.setActionHandler('nexttrack', hasNext ? next : null);
    return () => {
      navigator.mediaSession.setActionHandler('previoustrack', null);
      navigator.mediaSession.setActionHandler('nexttrack', null);
    };
  }, [song, hasPrev, hasNext, prev, next]);

  if (!song) return null;

  const handleClose = () => {
    setQueueOpen(false);
    close();
  };

  const togglePlay = () => {
    if (!audio) return;
    if (audio.paused) void audio.play();
    else audio.pause();
  };

  const handleEnded = (e: React.SyntheticEvent<HTMLAudioElement>) => {
    if (repeat === 'one' || (repeat === 'all' && queue.length === 1)) {
      const el = e.currentTarget;
      el.currentTime = 0;
      void el.play();
    } else if (hasNext) {
      next();
    }
  };

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

  // Un solo <audio>: resta montato quando si passa tra barra piena e pillola.
  // La key per uid fa ripartire il brano anche se lo stesso titolo è due volte in coda.
  const audioEl = (
    <audio
      ref={setAudio}
      key={item.uid}
      src={streamUrl(song.id)}
      controls={!compact}
      autoPlay
      preload="auto"
      onPlay={() => setPlaying(true)}
      onPause={() => setPlaying(false)}
      onError={() => setFailedUid(item.uid)}
      onEnded={handleEnded}
      className={compact || hidden ? 'hidden' : 'order-3 h-10 w-full basis-full sm:flex-[2] sm:basis-64'}
    />
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
        {audioEl}
      </>
    );
  }

  if (compact) {
    return (
      <div className="fixed bottom-3 right-3 z-40 flex max-w-[calc(100vw-1.5rem)] items-center gap-2 rounded-full border border-border bg-surface-alt/90 px-3 py-1.5 opacity-60 shadow-lg backdrop-blur transition-opacity focus-within:opacity-100 hover:opacity-100">
        {queueOpen && <QueuePanel
            onClear={handleClose}
            className="absolute bottom-full right-0 mb-2 w-80 max-w-[calc(100vw-1.5rem)]"
          />}
        <span className="hidden max-w-40 truncate text-xs text-text sm:inline" title={`${song.title} – ${song.author}`}>
          {error ? 'Errore di riproduzione' : song.title}
        </span>
        <button onClick={prev} disabled={!hasPrev} aria-label="Precedente" className={btn}>
          <PrevIcon />
        </button>
        <button onClick={togglePlay} aria-label={playing ? 'Pausa' : 'Riproduci'} className={btn}>
          {playing ? <PauseIcon /> : <PlayIcon />}
        </button>
        <button onClick={next} disabled={!hasNext} aria-label="Successivo" className={btn}>
          <NextIcon />
        </button>
        {shuffleBtn('')}
        {repeatBtn('')}
        {queueBtn('')}
        {hideBtn('')}
        <button onClick={handleClose} aria-label="Chiudi player" className={`${btn} ${btnDanger}`}>
          <CloseIcon />
        </button>
        {audioEl}
      </div>
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
        <div className="order-1 min-w-0 flex-1 basis-0 sm:basis-48">
          <p className="truncate font-medium text-text">{song.title}</p>
          <p className="truncate text-sm text-text-muted">{song.author}</p>
          {error && <p className="text-sm text-red-400">Impossibile riprodurre il brano.</p>}
        </div>
        {hideBtn('order-2 sm:order-8')}
        <button
          onClick={handleClose}
          aria-label="Chiudi player"
          className={`${btn} ${btnDanger} order-2 sm:order-9`}
        >
          <CloseIcon />
        </button>
        {audioEl}
        <button
          onClick={prev}
          disabled={!hasPrev}
          aria-label="Precedente"
          className={`${btn} order-4 flex-1 sm:order-2 sm:flex-none`}
        >
          <PrevIcon />
        </button>
        <button
          onClick={next}
          disabled={!hasNext}
          aria-label="Successivo"
          className={`${btn} order-5 flex-1 sm:order-4 sm:flex-none`}
        >
          <NextIcon />
        </button>
        {shuffleBtn('order-6 flex-1 sm:order-5 sm:flex-none')}
        {repeatBtn('order-6 flex-1 sm:order-6 sm:flex-none')}
        {queueBtn('order-7 flex-1 sm:order-7 sm:flex-none')}
      </div>
    </div>
  );
};
