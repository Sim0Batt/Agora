import type { ReactNode } from 'react';

interface IconProps {
  className?: string;
}

const Svg = ({ className = 'h-4 w-4', children }: IconProps & { children: ReactNode }) => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth={1.75}
    strokeLinecap="round"
    strokeLinejoin="round"
    className={className}
    aria-hidden="true"
  >
    {children}
  </svg>
);

export const PlayIcon = (p: IconProps) => (
  <Svg {...p}>
    <path d="M7 5v14l11-7z" />
  </Svg>
);

export const PauseIcon = (p: IconProps) => (
  <Svg {...p}>
    <path d="M9 5v14M15 5v14" />
  </Svg>
);

export const PrevIcon = (p: IconProps) => (
  <Svg {...p}>
    <path d="M7 5v14M18 5v14l-9-7z" />
  </Svg>
);

export const NextIcon = (p: IconProps) => (
  <Svg {...p}>
    <path d="M17 5v14M6 5v14l9-7z" />
  </Svg>
);

export const ShuffleIcon = (p: IconProps) => (
  <Svg {...p}>
    <path d="M3 7h3a5 5 0 0 1 4 2l4 6a5 5 0 0 0 4 2h3M3 17h3a5 5 0 0 0 4-2M14 9a5 5 0 0 1 4-2h3M18 4l3 3-3 3M18 14l3 3-3 3" />
  </Svg>
);

export const RepeatIcon = ({ one, ...p }: IconProps & { one?: boolean }) => (
  <Svg {...p}>
    <path d="M17 2l3 3-3 3M4 11V9a4 4 0 0 1 4-4h12M7 22l-3-3 3-3M20 13v2a4 4 0 0 1-4 4H4" />
    {one && <path d="M11 10l1.5-1v6" />}
  </Svg>
);

export const QueueIcon = (p: IconProps) => (
  <Svg {...p}>
    <path d="M4 6h12M4 12h12M4 18h6M17 15v5l4-2.5z" />
  </Svg>
);

export const PlusIcon = (p: IconProps) => (
  <Svg {...p}>
    <path d="M12 5v14M5 12h14" />
  </Svg>
);

export const CloseIcon = (p: IconProps) => (
  <Svg {...p}>
    <path d="M6 6l12 12M18 6L6 18" />
  </Svg>
);

export const ChevronDownIcon = (p: IconProps) => (
  <Svg {...p}>
    <path d="M6 9l6 6 6-6" />
  </Svg>
);

export const MusicIcon = (p: IconProps) => (
  <Svg {...p}>
    <path d="M9 18V5l11-2v13M9 18a3 3 0 1 1-3-3 3 3 0 0 1 3 3zM20 16a3 3 0 1 1-3-3 3 3 0 0 1 3 3z" />
  </Svg>
);
