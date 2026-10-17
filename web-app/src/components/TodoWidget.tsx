import { useEffect, useMemo, useState } from 'react';
import { todoApi } from '../api/todo';
import type { Todo } from '../types/models';

const DAY_MS = 24 * 60 * 60 * 1000;

// Backend format: "2026-11-01 23:59:59.0"
const parseDate = (s: string) => new Date(s.replace(' ', 'T').replace(/\.\d+$/, ''));

function urgency(days: number) {
  if (days < 0) return { label: 'Scaduto', cls: 'bg-red-500/15 text-red-400 border-red-500/30', bar: 'bg-red-500' };
  if (days <= 7) return { label: 'Urgente', cls: 'bg-orange-500/15 text-orange-400 border-orange-500/30', bar: 'bg-orange-500' };
  if (days <= 30) return { label: 'In arrivo', cls: 'bg-yellow-500/15 text-yellow-400 border-yellow-500/30', bar: 'bg-yellow-500' };
  return { label: 'Tranquillo', cls: 'bg-green-500/15 text-green-400 border-green-500/30', bar: 'bg-green-500' };
}

function remaining(days: number) {
  if (days < 0) return `Scaduto da ${Math.abs(days)} g`;
  if (days === 0) return 'Scade oggi';
  if (days === 1) return 'Scade domani';
  return `Tra ${days} giorni`;
}

export const TodoWidget = () => {
  const [todos, setTodos] = useState<Todo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    todoApi
      .listTodos()
      .then(setTodos)
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, []);

  const items = useMemo(() => {
    const now = Date.now();
    return todos
      .map((t) => {
        const date = parseDate(t.expirationDate);
        return { ...t, date, days: Math.floor((date.getTime() - now) / DAY_MS) };
      })
      .sort((a, b) => a.date.getTime() - b.date.getTime());
  }, [todos]);

  return (
    <div className="rounded-lg border border-border bg-surface-alt p-5">
      <div className="mb-4 flex items-center justify-between">
        <p className="font-semibold text-text">Todo</p>
        {!loading && !error && <span className="text-xs text-text-muted">{items.length} attività</span>}
      </div>

      {loading ? (
        <p className="text-sm text-text-muted">Caricamento…</p>
      ) : error ? (
        <p className="text-sm text-text-muted">Impossibile caricare i todo.</p>
      ) : items.length === 0 ? (
        <p className="text-sm text-text-muted">Nessun todo, ottimo lavoro! 🎉</p>
      ) : (
        <ul className="grid grid-cols-1 gap-3 md:grid-cols-2 lg:grid-cols-3">
          {items.map((t, i) => {
            const u = urgency(t.days);
            return (
              <li
                key={`${t.title}-${i}`}
                className="relative flex flex-col overflow-hidden rounded-md border border-border bg-surface p-4 pl-5 transition-colors hover:border-accent"
              >
                <span className={`absolute inset-y-0 left-0 w-1 ${u.bar}`} />
                <div className="flex items-start justify-between gap-2">
                  <p className="font-medium text-text">{t.title}</p>
                  <span className={`shrink-0 rounded-full border px-2 py-0.5 text-xs ${u.cls}`}>{u.label}</span>
                </div>
                <p className="mt-1 flex-1 text-sm text-text-muted">{t.description}</p>
                <div className="mt-3 flex items-center justify-between text-xs text-text-muted">
                  <span>
                    📅 {t.date.toLocaleDateString('it-IT', { day: 'numeric', month: 'short', year: 'numeric' })}
                  </span>
                  <span className="font-medium">{remaining(t.days)}</span>
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
};
