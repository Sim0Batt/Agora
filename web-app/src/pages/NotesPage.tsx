import { useState } from 'react';
import { DashBoard } from '../components/Dashboard';
import { NotesScrollableList } from '../components/NotesScrollableList';
import { CreateNoteDialog } from '../components/CreateNoteDialog';

export const NotesPage = () => {
    const [dialogOpen, setDialogOpen] = useState(false);
    const [refreshKey, setRefreshKey] = useState(0);

    return (
        <div className="min-h-screen flex flex-col">
            <DashBoard />
            <div className="p-10">
                <button
                    onClick={() => setDialogOpen(true)}
                    className="bg-accent text-white px-4 py-2 rounded mt-4 hover:bg-accent-hover transition-colors"
                >
                    Crea Nuova Nota
                </button>
            </div>
            <div className="flex-1 p-10">
                <NotesScrollableList refreshKey={refreshKey} />
            </div>
            {dialogOpen && (
                <CreateNoteDialog
                    onClose={() => setDialogOpen(false)}
                    onCreated={() => setRefreshKey((k) => k + 1)}
                />
            )}
        </div>
    );
};
