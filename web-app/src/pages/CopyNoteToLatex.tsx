import { useState, useEffect, useMemo } from 'react';
import { DashBoard } from '../components/Dashboard';
import { apiClient } from '../api/client';
import LatexEditorPanel from '../components/LatexEditorPanel';
import NoteEditorPanel from '../components/NoteEditorPanel';

interface NoteListItem {
    id: number;
    title: string;
    type: string;
    length?: number;
}

function NoteSelector({ notes, search, onSearchChange, onSelect, label }: {
    notes: NoteListItem[];
    search: string;
    onSearchChange: (v: string) => void;
    onSelect: (id: number) => void;
    label: string;
}) {
    const typeColor: Record<string, string> = {
        University: 'bg-blue-500/15 text-blue-400 border-blue-500/30',
        Work: 'bg-green-500/15 text-green-400 border-green-500/30',
    };

    const filtered = useMemo(() => {
        const q = search.trim().toUpperCase();
        if (!q) return notes;
        return notes.filter((n) => n.title.toUpperCase().includes(q));
    }, [notes, search]);

    return (
        <div className="flex-1 flex flex-col">
            <div className="px-4 py-2 border-b border-border">
                <span className="text-xs font-semibold uppercase tracking-wider text-text-muted">
                    {label}
                </span>
                <input
                    type="text"
                    value={search}
                    onChange={(e) => onSearchChange(e.target.value)}
                    placeholder="Cerca per titolo..."
                    className="mt-2 w-full border border-border bg-surface text-text rounded-lg px-3 py-1.5 text-sm placeholder-text-muted focus:outline-none focus:ring-2 focus:ring-accent"
                />
            </div>
            <div className="flex-1 overflow-y-auto p-3">
                {filtered.length === 0 ? (
                    <p className="text-center text-text-muted py-8 text-sm">Nessuna nota trovata.</p>
                ) : (
                    <ul className="grid gap-2">
                        {filtered.map((note) => (
                            <li
                                key={note.id}
                                onClick={() => onSelect(note.id)}
                                className="flex items-center justify-between gap-2 p-3 rounded-lg cursor-pointer border border-border bg-surface hover:border-accent hover:shadow-lg hover:shadow-accent/5 transition-all"
                            >
                                <span className="text-sm font-medium text-text truncate">{note.title}</span>
                                <span className={`shrink-0 text-[10px] font-semibold uppercase px-2 py-0.5 rounded-full border ${typeColor[note.type] ?? 'bg-gray-500/15 text-gray-400 border-gray-500/30'}`}>
                                    {note.type}
                                </span>
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </div>
    );
}

export default function CopyNoteToLatex() {
    // ── Note normali (destra, read-only) ────────────────────────────────
    const [notes, setNotes] = useState<NoteListItem[]>([]);
    const [selectedNoteId, setSelectedNoteId] = useState<number | null>(null);
    const [noteContent, setNoteContent] = useState('');
    const [noteSearch, setNoteSearch] = useState('');

    // ── Note LaTeX (sinistra, editabile) ────────────────────────────────
    const [latexNotes, setLatexNotes] = useState<NoteListItem[]>([]);
    const [selectedLatexId, setSelectedLatexId] = useState<number | null>(null);
    const [latexCode, setLatexCode] = useState('');
    const [latexSearch, setLatexSearch] = useState('');

    // ── Salvataggio ─────────────────────────────────────────────────────
    const [saving, setSaving] = useState(false);
    const [saveLog, setSaveLog] = useState<string | null>(null);

    // ── Fetch lista note normali ────────────────────────────────────────
    useEffect(() => {
        const fetchNotes = async () => {
            const response = await apiClient.get('/note', {
                responseType: 'text',
                transformResponse: [(data: string) => data],
            });
            const text = typeof response.data === 'string' ? response.data.trim() : '';
            const items: NoteListItem[] = [];
            if (text) {
                for (const chunk of text.split(';')) {
                    const trimmed = chunk.trim();
                    if (!trimmed) continue;
                    try {
                        const obj = JSON.parse(trimmed);
                        items.push({
                            id: Number(obj.id),
                            title: obj.title,
                            type: obj.type,
                            length: Number(obj.length),
                        });
                    } catch { /* skip malformed chunks */ }
                }
            }
            setNotes(items);
        };
        fetchNotes();
    }, []);

    // ── Fetch lista note LaTeX ──────────────────────────────────────────
    useEffect(() => {
        const fetchLatexNotes = async () => {
            const response = await apiClient.get('/latex/list/1');
            const items: NoteListItem[] = response.data.map((item: { id: string; title: string; type: string }) => ({
                id: Number(item.id),
                title: item.title,
                type: item.type,
            }));
            setLatexNotes(items);
        };
        fetchLatexNotes();
    }, []);

    // ── Fetch contenuto nota selezionata (read-only) ────────────────────
    useEffect(() => {
        if (selectedNoteId === null) { setNoteContent(''); return; }
        const fetchContent = async () => {
            const response = await apiClient.post('/content/' + selectedNoteId);
            setNoteContent(response.data.content ?? '');
        };
        fetchContent();
    }, [selectedNoteId]);

    // ── Fetch contenuto LaTeX selezionata ───────────────────────────────
    useEffect(() => {
        if (selectedLatexId === null) { setLatexCode(''); return; }
        const fetchContent = async () => {
            const response = await apiClient.get('/latex/' + selectedLatexId, {
                responseType: 'text',
                transformResponse: [(data: string) => data],
            });
            setLatexCode(response.data);
        };
        fetchContent();
    }, [selectedLatexId]);

    // ── Salva contenuto sulla nota LaTeX esistente ──────────────────────
    const saveLatex = async () => {
        if (selectedLatexId === null) return;
        setSaving(true);
        setSaveLog(null);
        try {
            await apiClient.post('/latex/update', {
                noteId: selectedLatexId,
                length: 0,
                content: latexCode,
            });
            setSaveLog('Salvato con successo!');
        } catch (err: unknown) {
            const msg =
                (err as { response?: { data?: { error?: string } } })?.response?.data?.error ??
                'Errore di connessione al server.';
            setSaveLog(`ERRORE: ${msg}`);
        } finally {
            setSaving(false);
        }
    };

    const selectedNote = notes.find((n) => n.id === selectedNoteId);
    const selectedLatex = latexNotes.find((n) => n.id === selectedLatexId);

    return (
        <div className="flex flex-col h-screen bg-surface text-text">
            <DashBoard />

            {/* ── Toolbar ──────────────────────────────────────────────── */}
            <div className="flex items-center justify-end px-5 py-2 bg-surface border-b border-border gap-3">
                {saveLog && (
                    <span className={`text-sm ${saveLog.startsWith('ERRORE') ? 'text-red-400' : 'text-green-400'}`}>
                        {saveLog}
                    </span>
                )}
                <button
                    onClick={saveLatex}
                    disabled={saving || selectedLatexId === null}
                    className="px-4 py-1.5 text-sm font-medium rounded bg-accent text-white hover:bg-accent-hover transition-colors cursor-pointer disabled:opacity-50"
                >
                    {saving ? 'Salvando…' : 'Salva LaTeX'}
                </button>
            </div>

            {/* ── Split layout 50/50 ───────────────────────────────────── */}
            <div className="flex flex-1 overflow-hidden">

                {/* ── LEFT (50%): Nota LaTeX editabile ────────────────── */}
                <div className="w-1/2 flex flex-col border-r border-border">
                    {selectedLatexId === null ? (
                        <NoteSelector
                            notes={latexNotes}
                            search={latexSearch}
                            onSearchChange={setLatexSearch}
                            onSelect={setSelectedLatexId}
                            label="Seleziona nota LaTeX di destinazione"
                        />
                    ) : (
                        <>
                            <div className="flex items-center justify-between px-4 py-2 bg-surface-alt border-b border-border">
                                <span className="text-sm font-medium text-text truncate">
                                    {selectedLatex?.title}
                                </span>
                                <button
                                    onClick={() => setSelectedLatexId(null)}
                                    className="text-xs text-text-muted hover:text-text cursor-pointer px-2 py-1 rounded border border-border hover:bg-surface transition-colors"
                                >
                                    ← Cambia
                                </button>
                            </div>
                            <div className="flex-1 overflow-hidden">
                                <LatexEditorPanel
                                    value={latexCode}
                                    onChange={setLatexCode}
                                    onSave={saveLatex}
                                />
                            </div>
                        </>
                    )}
                </div>

                {/* ── RIGHT (50%): Nota normale read-only ─────────────── */}
                <div className="w-1/2 flex flex-col">
                    {selectedNoteId === null ? (
                        <NoteSelector
                            notes={notes}
                            search={noteSearch}
                            onSearchChange={setNoteSearch}
                            onSelect={setSelectedNoteId}
                            label="Seleziona nota da copiare"
                        />
                    ) : (
                        <>
                            <div className="flex items-center justify-between px-4 py-2 bg-surface-alt border-b border-border">
                                <span className="text-sm font-medium text-text truncate">
                                    {selectedNote?.title}
                                </span>
                                <button
                                    onClick={() => setSelectedNoteId(null)}
                                    className="text-xs text-text-muted hover:text-text cursor-pointer px-2 py-1 rounded border border-border hover:bg-surface transition-colors"
                                >
                                    ← Cambia
                                </button>
                            </div>
                            <div className="flex-1 overflow-hidden">
                                <NoteEditorPanel
                                    value={noteContent}
                                    readOnly
                                />
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
}
