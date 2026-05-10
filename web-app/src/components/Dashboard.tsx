import React from 'react';
import { useNavigate } from 'react-router-dom';

export const DashBoard = () => {
    const links: Record<string, string> = {
        'Home Page': '/',
        'Notes': '/dashboard/licenses',
        'Latex Ediot': '/latex',
        'Settings': '/dashboard/user',
    };

    const navigate = useNavigate();
    const goToPage = (componentName: string) => {
        navigate(componentName);
    };

    return (
        <header className="sticky top-0 z-50 w-full border-b border-border bg-surface-alt">
            <nav className="mx-auto flex h-16 max-w-6xl items-center justify-between gap-4 px-4">
                <DashBoard />

                <ul className="m-0 flex list-none items-center gap-5 p-0 font-medium text-text-muted">
                    {Object.keys(links).map((item) => (
                        <li
                            key={item}
                            className="cursor-pointer transition-colors hover:text-accent-hover"
                        >
                            <button onClick={() => goToPage(links[item])}>{item}</button>
                        </li>
                    ))}
                </ul>

                {/* <button
          type="button"
          className="rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white shadow-md transition-all hover:bg-blue-700"
        >
          Nuova richiesta
        </button> */}
            </nav>
        </header>
    );
};
