import { useEffect, useMemo, useState } from 'react';
import { todoApi } from '../api/todo';
import type { Todo, TodoType } from '../types/models';

const TYPES: TodoType[] = ['Work', 'University', 'Other'];
const TYPE_LABELS: Record<TodoType, string> = { Work: 'Lavoro', University: 'Università', Other: 'Altro' };

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

const tomorrow = () => {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

export const TodoWidget = () => {
  const [todos, setTodos] = useState<Todo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [filter, setFilter] = useState<TodoType | 'All'>('All');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState<{ title: string; description: string; date: string; type: TodoType }>({ title: '', description: '', date: '', type: 'Work' });
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');

  const load = () =>
    todoApi
      .listTodos()
      .then((t) => {
        setTodos(t);
        setError(false);
      })
      .catch(() => setError(true))
      .finally(() => setLoading(false));

  useEffect(() => {
    load();
  }, []);

  const closeForm = () => {
    setShowForm(false);
    setForm({ title: '', description: '', date: '', type: 'Work' });
    setFormError('');
  };

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setFormError('');
    try {
      await todoApi.addTodo({
        title: form.title.trim(),
        description: form.description.trim(),
        type: form.type,
        expirationDate: `${form.date || tomorrow()} 23:59:59.0`,
      });
      closeForm();
      await load();
    } catch {
      setFormError('Impossibile salvare il todo.');
    } finally {
      setSaving(false);
    }
  };

  const remove = async (id?: number) => {
    if (id === undefined) return;
    try {
      await todoApi.removeTodo(id);
      setTodos((prev) => prev.filter((t) => t.id !== id));
    } catch {
      alert('Impossibile eliminare il todo.');
    }
  };

  const items = useMemo(() => {
    const now = Date.now();
    return todos
      .filter((t) => filter === 'All' || (TYPES.includes(t.type) ? t.type : 'Other') === filter)
      .map((t) => {
        const date = parseDate(t.expirationDate);
        return { ...t, date, days: Math.floor((date.getTime() - now) / DAY_MS) };
      })
      .sort((a, b) => a.date.getTime() - b.date.getTime());
  }, [todos, filter]);

  return (
    <div className="rounded-lg border border-border bg-surface-alt p-5">
      <div className="mb-4 flex items-center justify-between">
        <p className="font-semibold text-text">Todo</p>
        {!loading && !error && <span className="text-xs text-text-muted">{items.length} attività</span>}
      </div>

      <button
        type="button"
        onClick={() => setShowForm(true)}
        className="mb-4 w-full rounded-md border border-dashed border-border px-3 py-2 text-sm font-medium text-text transition-colors hover:border-accent"
      >
        + Aggiungi todo
      </button>

      <div className="mb-4 flex flex-wrap gap-2">
        {(['All', ...TYPES] as const).map((f) => (
          <button
            key={f}
            type="button"
            onClick={() => setFilter(f)}
            className={`rounded-full border px-3 py-1 text-xs transition-colors ${
              filter === f ? 'border-accent bg-accent/15 text-text' : 'border-border text-text-muted hover:border-accent'
            }`}
          >
            {f === 'All' ? 'Tutti' : TYPE_LABELS[f]}
          </button>
        ))}
      </div>

      {showForm && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4"
          onClick={closeForm}
        >
          <form
            onSubmit={submit}
            onClick={(e) => e.stopPropagation()}
            className="w-full max-w-md space-y-4 rounded-lg border border-border bg-surface-alt p-6"
          >
            <p className="text-lg font-semibold text-text">Nuovo todo</p>
            <label className="block text-sm text-text-muted">
              Titolo
              <input
                required
                maxLength={255}
                value={form.title}
                onChange={(e) => setForm({ ...form, title: e.target.value })}
                className="mt-1 w-full rounded-md border border-border bg-surface px-3 py-2 text-text"
              />
            </label>
            <label className="block text-sm text-text-muted">
              Descrizione
              <textarea
                rows={3}
                value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
                className="mt-1 w-full rounded-md border border-border bg-surface px-3 py-2 text-text"
              />
            </label>
            <label className="block text-sm text-text-muted">
              Tipo
              <select
                value={form.type}
                onChange={(e) => setForm({ ...form, type: e.target.value as TodoType })}
                className="mt-1 w-full rounded-md border border-border bg-surface px-3 py-2 text-text"
              >
                {TYPES.map((ty) => (
                  <option key={ty} value={ty}>
                    {TYPE_LABELS[ty]}
                  </option>
                ))}
              </select>
            </label>
            <label className="block text-sm text-text-muted">
              Scadenza (default: domani)
              <input
                type="date"
                value={form.date}
                onChange={(e) => setForm({ ...form, date: e.target.value })}
                className="mt-1 w-full rounded-md border border-border bg-surface px-3 py-2 text-text"
              />
            </label>
            {formError && <p className="text-sm text-red-400">{formError}</p>}
            <div className="flex justify-end gap-2">
              <button
                type="button"
                onClick={closeForm}
                className="rounded-md border border-border px-4 py-2 text-sm text-text hover:border-accent"
              >
                Annulla
              </button>
              <button
                type="submit"
                disabled={saving}
                className="rounded-md bg-accent px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
              >
                {saving ? 'Salvataggio…' : 'Salva'}
              </button>
            </div>
          </form>
        </div>
      )}

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
                key={t.id ?? `${t.title}-${i}`}
                className="relative flex flex-col overflow-hidden rounded-md border border-border bg-surface p-4 pl-5 transition-colors hover:border-accent"
              >
                <span className={`absolute inset-y-0 left-0 w-1 ${u.bar}`} />
                <div className="flex items-start justify-between gap-2">
                  <p className="font-medium text-text">{t.title}</p>
                  <div className="flex shrink-0 items-center gap-2">
                    <span className={`rounded-full border px-2 py-0.5 text-xs ${u.cls}`}>{u.label}</span>
                    <button
                      type="button"
                      onClick={() => remove(t.id)}
                      title="Elimina"
                      aria-label="Elimina todo"
                      className="text-text-muted transition-colors hover:text-red-400"
                    >
                      🗑
                    </button>
                  </div>
                </div>
                <p className="mt-1 text-xs text-text-muted">{TYPE_LABELS[t.type] ?? t.type}</p>
                <p className="mt-1 flex-1 text-sm text-text-muted">{t.description ?? ''}</p>
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
