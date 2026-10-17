import { useState, useCallback, useEffect, useRef } from 'react';
import { DashBoard } from '../components/Dashboard';
import { useParams } from 'react-router-dom';
import { apiClient } from '../api/client';
import { latexApi } from '../api/latex';
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
    const [noteTitle, setNoteTitle] = useState('');
    const [compiling, setCompiling] = useState(false);
    const [uploadingImages, setUploadingImages] = useState(false);
    const [downloadingPdf, setDownloadingPdf] = useState(false);
    const [compileLog, setCompileLog] = useState<string | null>(null);
    const [logVisible, setLogVisible] = useState(false);
    const imageInputRef = useRef<HTMLInputElement | null>(null);

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

    const downloadPdf = useCallback(async () => {
        if (!id) return;

        setDownloadingPdf(true);
        try {
            const pdfBlob = await latexApi.downloadPdf(Number(id));
            const fileUrl = URL.createObjectURL(pdfBlob);
            const anchor = document.createElement('a');
            anchor.href = fileUrl;
            anchor.download = `${(noteTitle || `latex-${id}`).replace(/[^a-z0-9-_]+/gi, '_')}.pdf`;
            document.body.appendChild(anchor);
            anchor.click();
            anchor.remove();
            window.setTimeout(() => URL.revokeObjectURL(fileUrl), 1000);
        } finally {
            setDownloadingPdf(false);
        }
    }, [id, noteTitle]);

    const openImagePicker = useCallback(() => {
        imageInputRef.current?.click();
    }, []);

    const handleImagesSelected = useCallback(async (event: React.ChangeEvent<HTMLInputElement>) => {
        const files = Array.from(event.target.files ?? []);
        event.target.value = '';

        if (!files.length || !id) return;

        setUploadingImages(true);
        try {
            await latexApi.uploadImages(noteTitle || `latex-${id}`, files);
        } finally {
            setUploadingImages(false);
        }
    }, [id, noteTitle]);

    useEffect(() => {
        const fetchContent = async () => {
            const [contentResponse, notesResponse] = await Promise.all([
                apiClient.get('/latex/' + id, { responseType: 'text', transformResponse: [(data: string) => data] }),
                apiClient.get('/latex/list/1'),
            ]);

            setCode(contentResponse.data);

            const currentNote = notesResponse.data.find((item: { id: string; title: string }) => Number(item.id) === Number(id));
            setNoteTitle(currentNote?.title ?? '');
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
                    onClick={openImagePicker}
                    disabled={uploadingImages}
                    className="px-4 py-1.5 text-sm font-medium rounded border border-border hover:bg-surface-alt transition-colors cursor-pointer disabled:opacity-50"
                >
                    {uploadingImages ? 'Caricamento…' : 'Carica immagini'}
                </button>
                <input
                    ref={imageInputRef}
                    type="file"
                    accept="image/*"
                    multiple
                    className="hidden"
                    onChange={handleImagesSelected}
                />
                <button
                    onClick={downloadPdf}
                    disabled={downloadingPdf}
                    className="px-4 py-1.5 text-sm font-medium rounded border border-border hover:bg-surface-alt transition-colors cursor-pointer disabled:opacity-50"
                >
                    {downloadingPdf ? 'Download…' : 'Scarica PDF'}
                </button>
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