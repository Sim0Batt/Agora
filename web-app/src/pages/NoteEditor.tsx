import { useState, useRef, useCallback, useEffect } from 'react';
import Editor, { type Monaco } from '@monaco-editor/react';
import type { editor as MonacoEditor, IDisposable } from 'monaco-editor';
import { DashBoard } from '../components/Dashboard';
import { useParams } from 'react-router-dom';
import { apiClient } from '../api/client';

export default function NoteEditor() {
    const { id } = useParams();
    const [content, setContent] = useState('');
    const [saving, setSaving] = useState(false);
    const [saveLog, setSaveLog] = useState<string | null>(null);
    const [logVisible, setLogVisible] = useState(false);
    const editorRef = useRef<MonacoEditor.IStandaloneCodeEditor | null>(null);
    const disposablesRef = useRef<IDisposable[]>([]);

    // ── Salvataggio ─────────────────────────────────────────────────────
    const saveNote = useCallback(async () => {
        setSaving(true);
        setSaveLog(null);
        try {
            await apiClient.post('/updateNote', {
                noteId: Number(id),
                content,
                length: content.length
            });
            setSaveLog('Salvato con successo.');
            setLogVisible(true);
        } catch (err: unknown) {
            const msg =
                (err as { response?: { data?: { error?: string } } })?.response?.data?.error ??
                'Errore di connessione al server.';
            setSaveLog(`ERRORE: ${msg}`);
            setLogVisible(true);
        } finally {
            setSaving(false);
        }
    }, [content, id]);

    // ── Editor mount ────────────────────────────────────────────────────
    const handleEditorMount = useCallback(
        (editor: MonacoEditor.IStandaloneCodeEditor, monaco: Monaco) => {
            editorRef.current = editor;

            disposablesRef.current.forEach((d) => d.dispose());
            disposablesRef.current = [];

            // Ctrl+S / Cmd+S → salva
            editor.addAction({
                id: 'save-note',
                label: 'Salva nota',
                keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS],
                run: () => {
                    saveNote();
                },
            });

            editor.focus();
        },
        [saveNote],
    );

    // ── Fetch contenuto ─────────────────────────────────────────────────
    useEffect(() => {
        const fetchContent = async () => {
            const response = await apiClient.post('/content/' + id);
            setContent(response.data.content ?? '');
        };
        if (id) fetchContent();
    }, [id]);

    return (
        <div className="flex flex-col h-screen bg-surface text-text">
            <DashBoard />

            {/* ── Toolbar ──────────────────────────────────────────────── */}
            <div className="flex items-center justify-end px-5 py-2 bg-surface border-b border-border gap-3">
                {saveLog && (
                    <button
                        onClick={() => setLogVisible((v) => !v)}
                        className="px-3 py-1.5 text-sm rounded border border-border hover:bg-surface-alt transition-colors cursor-pointer"
                    >
                        {logVisible ? 'Nascondi log' : 'Mostra log'}
                    </button>
                )}
                <button
                    onClick={saveNote}
                    disabled={saving}
                    className="px-4 py-1.5 text-sm font-medium rounded bg-accent text-white hover:bg-accent-hover transition-colors cursor-pointer disabled:opacity-50"
                >
                    {saving ? 'Salvando…' : 'Salva (⌘S)'}
                </button>
            </div>

            {/* ── Editor ───────────────────────────────────────────────── */}
            <div className="flex-1 overflow-hidden relative">
                <Editor
                    height="100%"
                    defaultLanguage="plaintext"
                    theme="vs-dark"
                    value={content}
                    onChange={(value) => setContent(value || '')}
                    onMount={handleEditorMount}
                    options={{
                        minimap: { enabled: false },
                        wordWrap: 'on',
                        fontSize: 15,
                        lineHeight: 1.6,
                        padding: { top: 16 },
                        scrollBeyondLastLine: false,
                        renderLineHighlight: 'gutter',
                        overviewRulerBorder: false,
                        hideCursorInOverviewRuler: true,
                        scrollbar: { verticalScrollbarSize: 6, horizontalScrollbarSize: 6 },
                    }}
                />
            </div>

            {/* ── Log ──────────────────────────────────────────────────── */}
            {logVisible && saveLog && (
                <div className="border-t border-border bg-surface-alt max-h-48 overflow-y-auto">
                    <div className="flex items-center justify-between px-4 py-2 bg-surface-alt border-b border-border">
                        <span className="text-xs font-semibold uppercase tracking-wider text-text-muted">
                            Output
                        </span>
                        <button
                            onClick={() => setLogVisible(false)}
                            className="text-xs text-text-muted hover:text-text cursor-pointer"
                        >
                            ✕
                        </button>
                    </div>
                    <pre className="px-4 py-3 text-xs font-mono text-text-muted whitespace-pre-wrap">
                        {saveLog}
                    </pre>
                </div>
            )}
        </div>
    );
}
