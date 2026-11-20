import { useSyncExternalStore } from 'react';
import { musicApi } from '../api/music';

export interface ImportJob {
  id: number;
  title: string;
  status: 'running' | 'done' | 'error';
  error?: string;
}

// Lo stato vive a livello di modulo: l'importazione continua anche cambiando pagina.
let jobs: ImportJob[] = [];
let nextId = 1;
const listeners = new Set<() => void>();
const finishListeners = new Set<() => void>();

const setJobs = (updater: (prev: ImportJob[]) => ImportJob[]) => {
  jobs = updater(jobs);
  listeners.forEach((l) => l());
};

const errorMessage = (e: unknown) => {
  const err = e as { response?: { data?: unknown; status?: number }; code?: string; message?: string };
  if (err?.code === 'ERR_BACKEND_FAILURE') return 'Importazione fallita';
  if (typeof err?.response?.data === 'string' && err.response.data) return err.response.data;
  const detail = err?.response?.status ? `HTTP ${err.response.status}` : [err?.code, err?.message].filter(Boolean).join(' – ');
  return detail ? `Importazione fallita (${detail})` : 'Importazione fallita';
};

export const startImport = (title: string, url: string) => {
  const id = nextId++;
  setJobs((prev) => [...prev, { id, title, status: 'running' }]);
  musicApi
    .importPlaylist(title, url)
    .then(() => setJobs((prev) => prev.map((j) => (j.id === id ? { ...j, status: 'done' } : j))))
    .catch((err) =>
      setJobs((prev) => prev.map((j) => (j.id === id ? { ...j, status: 'error', error: errorMessage(err) } : j))),
    )
    .finally(() => finishListeners.forEach((l) => l()));
};

export const dismissImport = (id: number) => setJobs((prev) => prev.filter((j) => j.id !== id));

export const onImportFinished = (listener: () => void) => {
  finishListeners.add(listener);
  return () => {
    finishListeners.delete(listener);
  };
};

const subscribe = (listener: () => void) => {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
};

export const useImportJobs = () => useSyncExternalStore(subscribe, () => jobs);
