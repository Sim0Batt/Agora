import { coverGradient } from './musicUi';
import { MusicIcon } from './Icons';

export const Cover = ({ seed, className = 'h-12 w-12', iconClass = 'h-1/2 w-1/2' }: {
  seed: string;
  className?: string;
  iconClass?: string;
}) => (
  <div
    className={`flex shrink-0 items-center justify-center rounded-md text-white/70 shadow-md ${className}`}
    style={{ background: coverGradient(seed) }}
    aria-hidden="true"
  >
    <MusicIcon className={iconClass} />
  </div>
);
