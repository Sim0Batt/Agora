import { useState, useCallback, useEffect } from 'react';
import { DashBoard } from '../components/Dashboard';
import { useParams } from 'react-router-dom';
import { apiClient } from '../api/client';
import LatexEditorPanel from '../components/LatexEditorPanel';

// ─── COMPONENT ──────────────────────────────────────────────────────────────

const DEFAULT_LATEX = `\\documentclass{article}
\\usepackage[utf8]{inputenc}
\\usepackage[italian]{babel}
\\usepackage{amsmath, amssymb}

\\title{Il mio documento}
\\author{Nome Cognome}
\\date{\\today}

\\begin{document}
\\maketitle

\\section{Introduzione}
Scrivi qui il tuo testo. Formula inline: $a^2 + b^2 = c^2$.

\\begin{equation}
    x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}
\\end{equation}

\\end{document}
`;

export default function LatexEditor() {
    const { id } = useParams();
    const [code, setCode] = useState(DEFAULT_LATEX);
    const [compiling, setCompiling] = useState(false);
    const [compileLog, setCompileLog] = useState<string | null>(null);
    const [logVisible, setLogVisible] = useState(false);

    // ── Compilazione: salva + pdflatex ──────────────────────────────────
    const compilePdf = useCallback(async () => {
        setCompiling(true);
        setCompileLog(null);
        try {
            const res = await apiClient.post('/latex/update', { noteId: Number(id), length: 0, content: code });
            setCompileLog(res.data.log ?? 'Salvato con successo.');
            setLogVisible(true);
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { error?: string } } })?.response?.data?.error
                ?? 'Errore di connessione al server.';
            setCompileLog(`ERRORE: ${msg}`);
            setLogVisible(true);
        } finally {
            setCompiling(false);
        }
    }, [code, id]);

    useEffect(() => {
        const fetchContent = async () => {
            const response = await apiClient.get('/latex/' + id, { responseType: 'text', transformResponse: [(data: string) => data] });
            setCode(response.data);
        };
        if (id) fetchContent();
    }, [id]);

    return (
        <div className="flex flex-col h-screen bg-surface text-text">
            {/* ── Dashboard / Navbar ─────────────────────────────────── */}
            <DashBoard />

            {/* ── Toolbar editor ──────────────────────────────────────── */}
            <div className="flex items-center justify-end px-5 py-2 bg-surface border-b border-border gap-3">
                {compileLog && (
                    <button
                        onClick={() => setLogVisible(v => !v)}
                        className="px-3 py-1.5 text-sm rounded border border-border hover:bg-surface-alt transition-colors cursor-pointer"
                    >
                        {logVisible ? 'Nascondi log' : 'Mostra log'}
                    </button>
                )}
                <button
                    onClick={compilePdf}
                    disabled={compiling}
                    className="px-4 py-1.5 text-sm font-medium rounded bg-accent text-white hover:bg-accent-hover transition-colors cursor-pointer disabled:opacity-50"
                >
                    {compiling ? 'Compilando…' : 'Salva e Compila (⌘S)'}
                </button>
            </div>

            {/* ── Editor a tutto schermo ──────────────────────────────── */}
            <div className="flex-1 overflow-hidden relative">
                <LatexEditorPanel
                    value={code}
                    onChange={setCode}
                    onSave={compilePdf}
                />
            </div>

            {/* ── Log di compilazione ─────────────────────────────────── */}
            {logVisible && compileLog && (
                <div className="border-t border-border bg-surface-alt max-h-48 overflow-y-auto">
                    <div className="flex items-center justify-between px-4 py-2 bg-surface-alt border-b border-border">
                        <span className="text-xs font-semibold uppercase tracking-wider text-text-muted">Output compilazione</span>
                        <button onClick={() => setLogVisible(false)} className="text-xs text-text-muted hover:text-text cursor-pointer">✕</button>
                    </div>
                    <pre className="px-4 py-3 text-xs font-mono text-text-muted whitespace-pre-wrap">{compileLog}</pre>
                </div>
            )}
        </div>
    );
}