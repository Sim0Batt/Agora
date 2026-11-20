export const formatDuration = (seconds: number) => {
  const m = Math.floor(seconds / 60);
  const s = Math.floor(seconds % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
};

export const smallBtn =
  'inline-flex min-h-11 shrink-0 items-center justify-center gap-1.5 rounded-md border border-border px-3 py-1 text-sm transition-colors hover:border-accent hover:text-accent sm:min-h-0';
export const dangerBtn =
  'inline-flex min-h-11 shrink-0 items-center justify-center rounded-md border border-border px-3 py-1 text-sm transition-colors hover:border-red-400 hover:text-red-400 sm:min-h-0';

export const errorMessage = (e: unknown, fallback: string) => {
  const err = e as { response?: { data?: unknown; status?: number }; code?: string; message?: string };
  if (err?.code === 'ERR_BACKEND_FAILURE') return fallback;
  if (typeof err?.response?.data === 'string' && err.response.data) return err.response.data;
  const detail = err?.response?.status ? `HTTP ${err.response.status}` : [err?.code, err?.message].filter(Boolean).join(' – ');
  return detail ? `${fallback} (${detail})` : fallback;
};

// Colore stabile ricavato dal testo, usato al posto della copertina (il backend non ne ha).
export const coverGradient = (seed: string) => {
  let h = 0;
  for (let i = 0; i < seed.length; i++) h = (h * 31 + seed.charCodeAt(i)) % 360;
  return `linear-gradient(135deg, hsl(${h} 55% 38%), hsl(${(h + 50) % 360} 60% 22%))`;
};
