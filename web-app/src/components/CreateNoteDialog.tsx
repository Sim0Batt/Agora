import React, { useState } from 'react';
import { noteApi } from '../api/note';

interface CreateNoteDialogProps {
    onClose: () => void;
    onCreated: () => void;
}

export const CreateNoteDialog: React.FC<CreateNoteDialogProps> = ({ onClose, onCreated }) => {
    const [title, setTitle] = useState('');
    const [type, setType] = useState('');
    const [userId, setUserId] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        try {
            await noteApi.createNote({ title, content: '', type, userId: Number(userId) });
            onCreated();
            onClose();
        } catch {
            setError('Errore durante la creazione della nota. Riprova.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60">
            <div className="bg-surface-alt border border-border rounded-lg shadow-xl w-full max-w-lg p-6">
                <h2 className="text-xl font-semibold mb-4 text-text">Crea Nuova Nota</h2>
                <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                    <div className="flex flex-col gap-1">
                        <label className="text-sm font-medium text-text-muted" htmlFor="note-title">
                            Titolo
                        </label>
                        <input
                            id="note-title"
                            type="text"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            required
                            className="bg-surface border border-border rounded px-3 py-2 text-sm text-text placeholder-text-muted focus:outline-none focus:ring-2 focus:ring-accent"
                            placeholder="Inserisci il titolo"
                        />
                    </div>
                    <div className="flex flex-col gap-1">
                        <label className="text-sm font-medium text-text-muted" htmlFor="note-type">
                            Tipo
                        </label>
                        <input
                            id="note-type"
                            type="text"
                            value={type}
                            onChange={(e) => setType(e.target.value)}
                            required
                            className="bg-surface border border-border rounded px-3 py-2 text-sm text-text placeholder-text-muted focus:outline-none focus:ring-2 focus:ring-accent"
                            placeholder="Inserisci il tipo"
                        />
                    </div>
                    <div className="flex flex-col gap-1">
                        <label className="text-sm font-medium text-text-muted" htmlFor="note-userid">
                            User ID
                        </label>
                        <input
                            id="note-userid"
                            type="number"
                            value={userId}
                            onChange={(e) => setUserId(e.target.value)}
                            required
                            min={1}
                            className="bg-surface border border-border rounded px-3 py-2 text-sm text-text placeholder-text-muted focus:outline-none focus:ring-2 focus:ring-accent"
                            placeholder="Inserisci l'ID utente"
                        />
                    </div>
                    {error && <p className="text-red-500 text-sm">{error}</p>}
                    <div className="flex justify-end gap-2 mt-2">
                        <button
                            type="button"
                            onClick={onClose}
                            disabled={loading}
                            className="px-4 py-2 rounded border border-border text-sm text-text-muted hover:text-text hover:border-text-muted transition-colors"
                        >
                            Annulla
                        </button>
                        <button
                            type="submit"
                            disabled={loading}
                            className="px-4 py-2 rounded bg-accent text-white text-sm hover:bg-accent-hover disabled:opacity-50 transition-colors"
                        >
                            {loading ? 'Creazione...' : 'Crea'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};
