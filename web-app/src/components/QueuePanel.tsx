import { useMusic } from '../context/musicContext';
import { CloseIcon, PlayIcon } from './Icons';

const formatDuration = (seconds: number) => {
  const m = Math.floor(seconds / 60);
  const s = Math.floor(seconds % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
};

export const QueuePanel = ({ className, onClear }: { className: string; onClear: () => void }) => {
  const { queue, index, jumpTo, removeAt } = useMusic();

  return (
    <div className={`overflow-hidden rounded-lg border border-border bg-surface-alt shadow-xl ${className}`}>
      <div className="flex items-center justify-between border-b border-border px-3 py-2">
        <span className="text-xs font-semibold uppercase tracking-wider text-text-muted">
          Coda · {queue.length} {queue.length === 1 ? 'brano' : 'brani'}
        </span>
        <button onClick={onClear} className="-my-2 py-2 text-xs text-text-muted transition-colors hover:text-red-400">
          Svuota
        </button>
      </div>
      <ul className="max-h-64 divide-y divide-border overflow-y-auto">
        {queue.map((item, i) => (
          <li
            key={item.uid}
            className={`flex items-center gap-2 px-3 py-2.5 sm:py-2 ${i === index ? 'bg-accent/15' : ''}`}
          >
            <button
              onClick={() => jumpTo(i)}
              className="flex min-w-0 flex-1 items-center gap-2 text-left transition-colors hover:text-accent"
            >
              <span className="w-4 shrink-0 text-center text-xs text-text-muted">{i === index ? <PlayIcon className="mx-auto h-3 w-3 text-accent" /> : i + 1}</span>
              <span className="min-w-0">
                <span className="block truncate text-sm text-text">{item.song.title}</span>
                <span className="block truncate text-xs text-text-muted">{item.song.author}</span>
              </span>
            </button>
            <span className="shrink-0 font-mono text-xs text-text-muted">{formatDuration(item.song.duration)}</span>
            <button
              onClick={() => removeAt(i)}
              aria-label={`Rimuovi ${item.song.title} dalla coda`}
              className="-m-2 shrink-0 p-2 text-xs text-text-muted transition-colors hover:text-red-400"
            >
              <CloseIcon className="h-3.5 w-3.5" />
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
};
