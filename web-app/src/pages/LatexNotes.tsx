import { useState } from 'react'
import { DashBoard } from '../components/Dashboard';
import { LatexNotesScrollableList } from '../components/LatexNotesScrollableList';
import { CreateLatexNoteDialog } from '../components/CreateLatexNoteDialog';

export const LatexNotes = () => {
    const [dialogOpen, setDialogOpen] = useState(false);

    return (
        <div className="min-h-screen flex flex-col">
            <DashBoard />
            <div className="p-10">
                <button
                    onClick={() => setDialogOpen(true)}
                    className="bg-blue-600 text-white px-4 py-2 rounded mt-4"
                >
                    Crea Nuova Nota
                </button>
            </div>
            <div className="flex-1 p-10">
                <LatexNotesScrollableList />
            </div>
            {dialogOpen && (
                <CreateLatexNoteDialog
                    onClose={() => setDialogOpen(false)}
                    onCreated={() => setDialogOpen(false)}
                />
            )}
        </div>
    )
}
