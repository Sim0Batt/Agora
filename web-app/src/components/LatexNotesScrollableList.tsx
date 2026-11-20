import { useEffect, useMemo, useState, type MouseEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../api/client';
import { latexApi } from '../api/latex';
import { ErrorDialog } from './ErrorDialog';

interface NoteRecord {
    id: number;
    title: string;
    type: string;
}

export const LatexNotesScrollableList = ({ refreshKey = 0 }: { refreshKey?: number }) => {
    const [notes, setNotes] = useState<NoteRecord[]>([]);
    const [titleSearch, setTitleSearch] = useState('');
    const [downloadingId, setDownloadingId] = useState<number | null>(null);
    const [downloadError, setDownloadError] = useState<string | null>(null);
    const [confirmDelete, setConfirmDelete] = useState<NoteRecord | null>(null);
    const [deleting, setDeleting] = useState(false);
    const [deleteError, setDeleteError] = useState<string | null>(null);
    const navigate = useNavigate();

    const handleDelete = async () => {
        if (!confirmDelete || deleting) return;
        setDeleting(true);
        try {
            await latexApi.deleteNote(confirmDelete.id);
            setNotes((prev) => prev.filter((n) => n.id !== confirmDelete.id));
            setConfirmDelete(null);
        } catch (err) {
            setConfirmDelete(null);
            setDeleteError(err instanceof Error ? err.message : 'Eliminazione della nota fallita.');
        } finally {
            setDeleting(false);
        }
    };

    useEffect(() => {
        const fetchNotes = async () => {
            const response = await apiClient.get('/latex/list');
            const formattedData: NoteRecord[] = response.data.map((item: { id: string; title: string; type: string }) => ({
                id: Number(item.id),
                title: item.title,
                type: item.type,
            }));
            setNotes(formattedData);
        };
        fetchNotes();
    }, [refreshKey]);

    const filteredRecords = useMemo(() => {
        const q = titleSearch.trim().toUpperCase();
        if (!q) return notes;
        return notes.filter((record) =>
            record.title.toUpperCase().includes(q)
        );
    }, [notes, titleSearch]);

    const handleDownload = async (e: MouseEvent, record: NoteRecord) => {
        e.stopPropagation();
        setDownloadingId(record.id);
        try {
            const pdfBlob = await latexApi.downloadPdf(record.id);
            const fileUrl = URL.createObjectURL(pdfBlob);
            const anchor = document.createElement('a');
            anchor.href = fileUrl;
            anchor.download = `${(record.title || `latex-${record.id}`).replace(/[^a-z0-9-_]+/gi, '_')}.pdf`;
            document.body.appendChild(anchor);
            anchor.click();
            anchor.remove();
            window.setTimeout(() => URL.revokeObjectURL(fileUrl), 1000);
        } catch (err) {
            setDownloadError(err instanceof Error ? err.message : 'Errore durante il download del file.');
        } finally {
            setDownloadingId(null);
        }
    };

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
                <p className="text-center text-text-muted py-8">
                    Nessuna nota trovata.
                </p>
            ) : filteredRecords.length === 0 ? (
                <p className="text-center text-text-muted py-8">
                    Nessun risultato per la ricerca.
                </p>
            ) : (
                <ul className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                    {filteredRecords.map((record) => (
                        <li
                            key={record.id}
                            onClick={() => navigate(`/latex/editor/${record.id}`)}
                            className="group flex flex-col gap-3 p-4 rounded-xl cursor-pointer border border-border bg-surface-alt hover:border-accent hover:shadow-lg hover:shadow-accent/5 transition-all"
                        >
                            <div className="flex items-start justify-between gap-2">
                                <span className="font-semibold text-text group-hover:text-accent transition-colors line-clamp-2">
                                    {record.title}
                                </span>
                                {record.type && (
                                    <span className={`shrink-0 text-xs font-medium px-2 py-0.5 rounded-full border ${typeColor[record.type] ?? 'bg-gray-500/15 text-gray-400 border-gray-500/30'}`}>
                                        {record.type}
                                    </span>
                                )}
                            </div>
                            <div className="mt-auto flex items-center justify-between">
                                <button
                                    onClick={(e) => handleDownload(e, record)}
                                    disabled={downloadingId === record.id}
                                    className="px-3 py-1 text-xs font-medium rounded border border-border hover:bg-surface transition-colors cursor-pointer disabled:opacity-50"
                                >
                                    {downloadingId === record.id ? 'Download…' : 'Scarica PDF'}
                                </button>
                                <button
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        setConfirmDelete(record);
                                    }}
                                    className="px-3 py-1 text-xs font-medium rounded border border-border text-text-muted hover:border-red-400 hover:text-red-400 transition-colors cursor-pointer"
                                >
                                    Elimina
                                </button>
                            </div>
                        </li>
                    ))}
                </ul>
            )}
            {confirmDelete && (
                <div
                    className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4"
                    onClick={() => !deleting && setConfirmDelete(null)}
                >
                    <div
                        onClick={(e) => e.stopPropagation()}
                        className="w-full max-w-sm space-y-4 rounded-xl border border-border bg-surface-alt p-6"
                    >
                        <p>Eliminare la nota "{confirmDelete.title}"? L'operazione non è reversibile.</p>
                        <div className="flex justify-end gap-2">
                            <button
                                onClick={() => setConfirmDelete(null)}
                                disabled={deleting}
                                className="rounded-lg border border-border px-4 py-2 transition-colors hover:border-accent disabled:opacity-50"
                            >
                                Annulla
                            </button>
                            <button
                                onClick={handleDelete}
                                disabled={deleting}
                                className="rounded-lg bg-red-500 px-5 py-2 font-semibold text-white transition-colors hover:bg-red-600 disabled:opacity-50"
                            >
                                {deleting ? 'Eliminazione…' : 'Elimina'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
            {deleteError && (
                <ErrorDialog title="Eliminazione fallita" message={deleteError} onClose={() => setDeleteError(null)} />
            )}
            {downloadError && (
                <ErrorDialog title="Download PDF fallito" message={downloadError} onClose={() => setDownloadError(null)} />
            )}
        </div>
    );
};
