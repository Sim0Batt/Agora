import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../api/client';
import { DashBoard } from '../components/Dashboard';
import { TodoWidget } from '../components/TodoWidget';

interface RecentNote {
  id: number;
  title: string;
  type: string;
}

const NAV_CARDS = [
  {
    label: 'Note LaTeX',
    description: 'Crea e gestisci le tue note in formato LaTeX.',
    path: '/latex',
    icon: (
      <svg xmlns="http://www.w3.org/2000/svg" className="h-7 w-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 0 0-3.375-3.375h-1.5A1.125 1.125 0 0 1 13.5 7.125v-1.5a3.375 3.375 0 0 0-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 0 0-9-9Z" />
      </svg>
    ),
  },
  {
    label: 'Note',
    description: 'Crea e gestisci le tue note di testo.',
    path: '/notes',
    icon: (
      <svg xmlns="http://www.w3.org/2000/svg" className="h-7 w-7" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L10.582 16.07a4.5 4.5 0 0 1-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 0 1 1.13-1.897l8.932-8.931Zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0 1 15.75 21H5.25A2.25 2.25 0 0 1 3 18.75V8.25A2.25 2.25 0 0 1 5.25 6H10" />
      </svg>
    ),
  },
];

function useClock() {
  const [now, setNow] = useState(new Date());
  useEffect(() => {
    const id = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(id);
  }, []);
  return now;
}

export const HomePage = () => {
  const navigate = useNavigate();
  const now = useClock();
  const [recentNotes, setRecentNotes] = useState<RecentNote[]>([]);
  const [notesError, setNotesError] = useState(false);

  useEffect(() => {
    apiClient
      .get('/latex/list/1')
      .then((res) => {
        const data: RecentNote[] = res.data.map((item: { id: string; title: string; type: string }) => ({
          id: Number(item.id),
          title: item.title,
          type: item.type,
        }));
        setRecentNotes(data.slice(0, 5));
      })
      .catch(() => setNotesError(true));
  }, []);

  const timeStr = now.toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  const dateStr = now.toLocaleDateString('it-IT', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });

  const typeColor: Record<string, string> = {
    University: 'bg-blue-500/15 text-blue-400 border-blue-500/30',
    Work: 'bg-green-500/15 text-green-400 border-green-500/30',
  };

  return (
    <div className="min-h-screen bg-surface text-text">
      <DashBoard />


      <main className="mx-auto max-w-6xl px-4 py-10 space-y-10">
        <section>
          <h1 className="text-3xl font-bold text-text">Benvenuto in Agorà</h1>
          <p className="mt-1 text-text-muted">Seleziona una sezione o consulta i widget qui sotto.</p>
        </section>

        <section>
          <h2 className="mb-4 text-sm font-semibold uppercase tracking-widest text-text-muted">Sezioni</h2>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {NAV_CARDS.map((card) => (
              <button
                key={card.path}
                onClick={() => navigate(card.path)}
                className="flex items-start gap-4 rounded-lg border border-border bg-surface-alt p-5 text-left transition-colors hover:border-accent hover:bg-surface"
              >
                <span className="mt-0.5 text-accent">{card.icon}</span>
                <div>
                  <p className="font-semibold text-text">{card.label}</p>
                  <p className="mt-0.5 text-sm text-text-muted">{card.description}</p>
                </div>
              </button>
            ))}
          </div>
        </section>

        <section>
          <h2 className="mb-4 text-sm font-semibold uppercase tracking-widest text-text-muted">Widget</h2>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">

            <div className="flex flex-col justify-center rounded-lg border border-border bg-surface-alt p-6">
              <p className="text-4xl font-mono font-bold text-text tabular-nums">{timeStr}</p>
              <p className="mt-1 capitalize text-sm text-text-muted">{dateStr}</p>
            </div>

            <div className="rounded-lg border border-border bg-surface-alt p-5">
              <div className="mb-3 flex items-center justify-between">
                <p className="font-semibold text-text">Note recenti</p>
                <button
                  onClick={() => navigate('/latex')}
                  className="text-xs text-accent hover:text-accent-hover transition-colors"
                >
                  Vedi tutte →
                </button>
              </div>

              {notesError ? (
                <p className="text-sm text-text-muted">Impossibile caricare le note.</p>
              ) : recentNotes.length === 0 ? (
                <p className="text-sm text-text-muted">Nessuna nota trovata.</p>
              ) : (
                <ul className="space-y-2">
                  {recentNotes.map((note) => (
                    <li key={note.id}>
                      <button
                        onClick={() => navigate(`/latex/editor/${note.id}`)}
                        className="flex w-full items-center justify-between rounded-md px-3 py-2 text-sm transition-colors hover:bg-surface"
                      >
                        <span className="truncate text-text">{note.title}</span>
                        {note.type && (
                          <span className={`ml-2 shrink-0 rounded-full border px-2 py-0.5 text-xs ${typeColor[note.type] ?? 'bg-surface text-text-muted border-border'}`}>
                            {note.type}
                          </span>
                        )}
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </div>

            <div className="md:col-span-2">
              <TodoWidget />
            </div>

          </div>
        </section>
      </main>
    </div>
  );
};
