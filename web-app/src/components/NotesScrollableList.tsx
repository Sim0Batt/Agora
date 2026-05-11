import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../api/client';

interface NoteListItem {
    id: number;
    title: string;
    type: string;
    length: number;
}

export const NotesScrollableList = () => {
    const [notes, setNotes] = useState<NoteListItem[]>([]);
    const [titleSearch, setTitleSearch] = useState('');
    const navigate = useNavigate();

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

    const filteredRecords = useMemo(() => {
        const q = titleSearch.trim().toUpperCase();
        if (!q) return notes;
        return notes.filter((record) => record.title.toUpperCase().includes(q));
    }, [notes, titleSearch]);

    const typeColor: Record<string, string> = {
        University: 'bg-blue-500/15 text-blue-400 border-blue-500/30',
        Work: 'bg-green-500/15 text-green-400 border-green-500/30',
    };

    return (
        <div className="overflow-y-auto p-4">
            <div className="mb-4 flex justify-end">
                <input
                    type="text"
                    value={titleSearch}
                    onChange={(e) => setTitleSearch(e.target.value)}
                    placeholder="Cerca per titolo..."
                    className="w-full max-w-sm border border-border bg-surface-alt text-text rounded-lg px-3 py-2 text-sm placeholder-text-muted focus:outline-none focus:ring-2 focus:ring-accent"
                />
            </div>

            {notes.length === 0 ? (
                <p className="text-center text-text-muted py-8">Nessuna nota trovata.</p>
            ) : filteredRecords.length === 0 ? (
                <p className="text-center text-text-muted py-8">Nessun risultato per la ricerca.</p>
            ) : (
                <ul className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                    {filteredRecords.map((record) => (
                        <li
                            key={record.id}
                            onClick={() => navigate(`/notes/editor/${record.id}`)}
                            className="group flex flex-col gap-3 p-4 rounded-xl cursor-pointer border border-border bg-surface-alt hover:border-accent hover:shadow-lg hover:shadow-accent/5 transition-all"
                        >
                            <div className="flex items-start justify-between gap-2">
                                <span className="font-semibold text-text group-hover:text-accent transition-colors line-clamp-2">
                                    {record.title}
                                </span>
                                {record.type && (
                                    <span
                                        className={`shrink-0 text-xs font-medium px-2 py-0.5 rounded-full border ${typeColor[record.type] ?? 'bg-gray-500/15 text-gray-400 border-gray-500/30'}`}
                                    >
                                        {record.type}
                                    </span>
                                )}
                            </div>
                            <div className="mt-auto flex items-center justify-between">
                                <span className="text-xs text-text-muted">
                                    {record.length} caratteri
                                </span>
                                <span className="text-xs text-text-muted group-hover:text-accent transition-colors">
                                    Apri →
                                </span>
                            </div>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};
