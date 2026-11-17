import { useState } from 'react';
import { apiClient } from '../api/client';
import { DashBoard } from '../components/Dashboard';

export const SettingsPage = () => {
  // Il backend espone solo un toggle senza stato: si parte dal presupposto che il display sia acceso
  const [displayOn, setDisplayOn] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(false);

  const toggleDisplay = async () => {
    setBusy(true);
    setError(false);
    try {
      await apiClient.get('/display');
      setDisplayOn((v) => !v);
    } catch {
      setError(true);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="min-h-screen bg-surface text-text">
      <DashBoard />
      <main className="mx-auto max-w-6xl px-4 py-8">
        <h1 className="mb-6 text-2xl font-semibold text-text">Impostazioni</h1>
        <section className="rounded-lg border border-border bg-surface-alt p-4">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div>
              <h2 className="font-medium text-text">Display del Raspberry</h2>
              <p className="text-sm text-text-muted">
                Stato: {displayOn ? 'acceso' : 'spento'}
              </p>
              {error && <p className="text-sm text-red-400">Operazione non riuscita, riprova.</p>}
            </div>
            <button
              onClick={toggleDisplay}
              disabled={busy}
              className="inline-flex min-h-11 items-center justify-center rounded-md border border-border px-4 py-1 text-sm transition-colors hover:border-accent hover:text-accent disabled:opacity-40"
            >
              {busy ? '...' : displayOn ? 'Spegni display' : 'Accendi display'}
            </button>
          </div>
        </section>
      </main>
    </div>
  );
};
