import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useImportJobs } from '../context/importJobs';

export const DashBoard = () => {
    const links: Record<string, string> = {
        'Home Page': '/',
        'Notes': '/notes',
        'Latex Editor': '/latex',
        'Musica': '/music',
        'Playlist': '/music/playlists',
        'Copia in LaTeX': '/notes/copy-to-latex',
        'Settings': '/dashboard/user',
    };

    const navigate = useNavigate();
    const importing = useImportJobs().filter((j) => j.status === 'running').length;
    const [menuOpen, setMenuOpen] = useState(false);
    const goToPage = (componentName: string) => {
        setMenuOpen(false);
        navigate(componentName);
    };

    return (
        <header className="sticky top-0 z-50 w-full border-b border-border bg-surface-alt">
            <nav className="mx-auto flex h-16 max-w-6xl items-center justify-between gap-4 px-4">
                <span className="text-base font-semibold tracking-wide text-text">Agorà</span>

                {importing > 0 && (
                    <button
                        type="button"
                        onClick={() => navigate('/music/playlists')}
                        title="Importazione playlist in corso in background"
                        className="inline-flex items-center gap-2 rounded-full border border-accent/40 bg-accent/10 px-3 py-1 text-xs font-medium text-accent"
                    >
                        <span className="h-3 w-3 animate-spin rounded-full border-2 border-accent/30 border-t-accent" />
                        Importazione{importing > 1 ? ` (${importing})` : ''} in corso…
                    </button>
                )}

                <ul className="m-0 hidden list-none items-center gap-5 p-0 font-medium text-text-muted md:flex">
                    {Object.keys(links).map((item) => (
                        <li
                            key={item}
                            className="cursor-pointer transition-colors hover:text-accent-hover"
                        >
                            <button onClick={() => goToPage(links[item])}>{item}</button>
                        </li>
                    ))}
                </ul>

                <button
                    type="button"
                    onClick={() => setMenuOpen((v) => !v)}
                    aria-label={menuOpen ? 'Chiudi menu' : 'Apri menu'}
                    aria-expanded={menuOpen}
                    className="-mr-2 flex h-11 w-11 items-center justify-center rounded-md text-xl text-text transition-colors hover:text-accent-hover md:hidden"
                >
                    {menuOpen ? '✕' : '☰'}
                </button>

                {/* <button
          type="button"
          className="rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white shadow-md transition-all hover:bg-blue-700"
        >
          Nuova richiesta
        </button> */}
            </nav>

            {menuOpen && (
                <ul className="m-0 list-none border-t border-border bg-surface-alt p-0 md:hidden">
                    {Object.keys(links).map((item) => (
                        <li key={item} className="border-b border-border last:border-b-0">
                            <button
                                onClick={() => goToPage(links[item])}
                                className="flex min-h-12 w-full items-center px-4 text-left font-medium text-text-muted transition-colors hover:text-accent-hover"
                            >
                                {item}
                            </button>
                        </li>
                    ))}
                </ul>
            )}
        </header>
    );
};
