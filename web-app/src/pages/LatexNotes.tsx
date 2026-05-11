import React from 'react'
import { DashBoard } from '../components/Dashboard';
import { LatexNotesScrollableList } from '../components/LatexNotesScrollableList';
import { useNavigate } from 'react-router-dom';

export const LatexNotes = () => {
    const navigate = useNavigate();
    return (
        <div className="min-h-screen flex flex-col">
            <DashBoard />
            <div className="p-10">
                <button
                    onClick={() => {
                        navigate('/dashboard/permit/new');
                    }}
                    className="bg-blue-600 text-white px-4 py-2 rounded mt-4"
                >
                    Crea Nuova Nota
                </button>
            </div>
            <div className="flex-1 p-10">
                <LatexNotesScrollableList />
            </div>
        </div>
    )
}
