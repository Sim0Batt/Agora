import { useState, useRef, useCallback, useEffect } from 'react';
import Editor, { type Monaco } from '@monaco-editor/react';
import type { editor as MonacoEditor, languages, IDisposable } from 'monaco-editor';
import { DashBoard } from '../components/Dashboard';
import { useParams } from 'react-router-dom';
import { apiClient } from '../api/client';

// ─── CUSTOM KEYBOARD SHORTCUTS ─────────────────────────────────────────────
//   - keys:   combinazione di tasti (es. "Ctrl+B", "Ctrl+Shift+F")
//   - insert: testo inserito alla posizione del cursore
//   - wrap:   avvolge la selezione corrente con [prima, dopo]
//   - run:    funzione custom

interface Shortcut {
    label: string;
    keys: string;
    insert?: string;
    wrap?: [string, string];
    run?: () => void;
}

const CUSTOM_SHORTCUTS: Shortcut[] = [
    { label: 'Bold (textbf)', keys: 'Ctrl+B', wrap: ['\\textbf{', '}'] },
    { label: 'Italic (textit)', keys: 'Ctrl+I', wrap: ['\\textit{', '}'] },
    { label: 'Underline', keys: 'Ctrl+U', wrap: ['\\underline{', '}'] },
    // Aggiungi qui altri shortcut…
];

// ─── LATEX COMPLETIONS ─────────────────────────────────────────────────────
// Suggerimenti LaTeX mostrati mentre scrivi dopo `\`
const LATEX_COMPLETIONS: { label: string; insertText: string; detail: string }[] = [
    // Struttura documento
    { label: '\\documentclass', insertText: '\\documentclass{${1:article}}', detail: 'Document class' },
    { label: '\\usepackage', insertText: '\\usepackage{${1:package}}', detail: 'Use package' },
    { label: '\\begin', insertText: '\\begin{${1:environment}}\n\t$0\n\\end{${1:environment}}', detail: 'Begin environment' },
    { label: '\\end', insertText: '\\end{${1:environment}}', detail: 'End environment' },
    { label: '\\title', insertText: '\\title{${1:title}}', detail: 'Title' },
    { label: '\\author', insertText: '\\author{${1:name}}', detail: 'Author' },
    { label: '\\date', insertText: '\\date{${1:\\today}}', detail: 'Date' },
    { label: '\\maketitle', insertText: '\\maketitle', detail: 'Make title' },
    // Sezioni
    { label: '\\section', insertText: '\\section{${1:title}}', detail: 'Section' },
    { label: '\\subsection', insertText: '\\subsection{${1:title}}', detail: 'Subsection' },
    { label: '\\subsubsection', insertText: '\\subsubsection{${1:title}}', detail: 'Subsubsection' },
    { label: '\\paragraph', insertText: '\\paragraph{${1:title}}', detail: 'Paragraph' },
    { label: '\\chapter', insertText: '\\chapter{${1:title}}', detail: 'Chapter' },
    // Formattazione
    { label: '\\textbf', insertText: '\\textbf{${1:text}}', detail: 'Bold text' },
    { label: '\\textit', insertText: '\\textit{${1:text}}', detail: 'Italic text' },
    { label: '\\underline', insertText: '\\underline{${1:text}}', detail: 'Underline' },
    { label: '\\emph', insertText: '\\emph{${1:text}}', detail: 'Emphasis' },
    { label: '\\texttt', insertText: '\\texttt{${1:text}}', detail: 'Monospace' },
    { label: '\\textsc', insertText: '\\textsc{${1:text}}', detail: 'Small caps' },
    // Matematica
    { label: '\\frac', insertText: '\\frac{${1:num}}{${2:den}}', detail: 'Fraction' },
    { label: '\\sqrt', insertText: '\\sqrt{${1:x}}', detail: 'Square root' },
    { label: '\\sum', insertText: '\\sum_{${1:i=0}}^{${2:n}}', detail: 'Sum' },
    { label: '\\int', insertText: '\\int_{${1:a}}^{${2:b}}', detail: 'Integral' },
    { label: '\\prod', insertText: '\\prod_{${1:i=0}}^{${2:n}}', detail: 'Product' },
    { label: '\\lim', insertText: '\\lim_{${1:x \\to \\infty}}', detail: 'Limit' },
    { label: '\\infty', insertText: '\\infty', detail: '∞' },
    { label: '\\alpha', insertText: '\\alpha', detail: 'α' },
    { label: '\\beta', insertText: '\\beta', detail: 'β' },
    { label: '\\gamma', insertText: '\\gamma', detail: 'γ' },
    { label: '\\delta', insertText: '\\delta', detail: 'δ' },
    { label: '\\epsilon', insertText: '\\epsilon', detail: 'ε' },
    { label: '\\lambda', insertText: '\\lambda', detail: 'λ' },
    { label: '\\mu', insertText: '\\mu', detail: 'μ' },
    { label: '\\sigma', insertText: '\\sigma', detail: 'σ' },
    { label: '\\omega', insertText: '\\omega', detail: 'ω' },
    { label: '\\theta', insertText: '\\theta', detail: 'θ' },
    { label: '\\pi', insertText: '\\pi', detail: 'π' },
    { label: '\\phi', insertText: '\\phi', detail: 'φ' },
    { label: '\\psi', insertText: '\\psi', detail: 'ψ' },
    { label: '\\nabla', insertText: '\\nabla', detail: '∇' },
    { label: '\\partial', insertText: '\\partial', detail: '∂' },
    { label: '\\left', insertText: '\\left${1:(} ${0} \\right${2:)}', detail: 'Left delimiter' },
    { label: '\\right', insertText: '\\right${1:)}', detail: 'Right delimiter' },
    { label: '\\mathbb', insertText: '\\mathbb{${1:R}}', detail: 'Blackboard bold' },
    { label: '\\mathcal', insertText: '\\mathcal{${1:L}}', detail: 'Calligraphic' },
    { label: '\\mathrm', insertText: '\\mathrm{${1:text}}', detail: 'Roman in math' },
    // Ambienti
    { label: '\\begin{equation}', insertText: '\\begin{equation}\n\t$0\n\\end{equation}', detail: 'Equation env' },
    { label: '\\begin{align}', insertText: '\\begin{align}\n\t$0\n\\end{align}', detail: 'Align env' },
    { label: '\\begin{itemize}', insertText: '\\begin{itemize}\n\t\\item $0\n\\end{itemize}', detail: 'Itemize env' },
    { label: '\\begin{enumerate}', insertText: '\\begin{enumerate}\n\t\\item $0\n\\end{enumerate}', detail: 'Enumerate env' },
    { label: '\\begin{figure}', insertText: '\\begin{figure}[${1:htbp}]\n\t\\centering\n\t\\includegraphics[width=${2:0.8}\\textwidth]{${3:file}}\n\t\\caption{${4:caption}}\n\t\\label{fig:${5:label}}\n\\end{figure}', detail: 'Figure env' },
    { label: '\\begin{table}', insertText: '\\begin{table}[${1:htbp}]\n\t\\centering\n\t\\begin{tabular}{${2:ll}}\n\t\t$0\n\t\\end{tabular}\n\t\\caption{${3:caption}}\n\t\\label{tab:${4:label}}\n\\end{table}', detail: 'Table env' },
    // Riferimenti
    { label: '\\label', insertText: '\\label{${1:label}}', detail: 'Label' },
    { label: '\\ref', insertText: '\\ref{${1:label}}', detail: 'Reference' },
    { label: '\\cite', insertText: '\\cite{${1:key}}', detail: 'Citation' },
    { label: '\\footnote', insertText: '\\footnote{${1:text}}', detail: 'Footnote' },
    { label: '\\includegraphics', insertText: '\\includegraphics[width=${1:0.8}\\textwidth]{${2:file}}', detail: 'Include image' },
    { label: '\\item', insertText: '\\item ', detail: 'List item' },
    { label: '\\newcommand', insertText: '\\newcommand{\\${1:cmd}}[${2:0}]{${3:def}}', detail: 'New command' },
    { label: '\\renewcommand', insertText: '\\renewcommand{\\${1:cmd}}[${2:0}]{${3:def}}', detail: 'Renew command' },
    { label: '\\input', insertText: '\\input{${1:file}}', detail: 'Input file' },
    { label: '\\include', insertText: '\\include{${1:file}}', detail: 'Include file' },
    { label: '\\tableofcontents', insertText: '\\tableofcontents', detail: 'Table of contents' },
    { label: '\\newpage', insertText: '\\newpage', detail: 'New page' },
    { label: '\\clearpage', insertText: '\\clearpage', detail: 'Clear page' },
    { label: '\\hspace', insertText: '\\hspace{${1:1cm}}', detail: 'Horizontal space' },
    { label: '\\vspace', insertText: '\\vspace{${1:1cm}}', detail: 'Vertical space' },
];

// ─── HELPERS ────────────────────────────────────────────────────────────────

function parseKeybinding(keys: string, monaco: Monaco): number {
    const parts = keys.split('+').map((p) => p.trim());
    let mod = 0;
    let key = 0;
    for (const part of parts) {
        switch (part.toLowerCase()) {
            case 'ctrl': mod |= monaco.KeyMod.CtrlCmd; break;
            case 'shift': mod |= monaco.KeyMod.Shift; break;
            case 'alt': mod |= monaco.KeyMod.Alt; break;
            case 'meta': mod |= monaco.KeyMod.WinCtrl; break;
            default: {
                const keyName = `Key${part.toUpperCase()}` as keyof typeof monaco.KeyCode;
                key = monaco.KeyCode[keyName] ?? 0;
            }
        }
    }
    return mod | key;
}

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
    const editorRef = useRef<MonacoEditor.IStandaloneCodeEditor | null>(null);
    const disposablesRef = useRef<IDisposable[]>([]);

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
    // ── Registra lingua LaTeX + completions + shortcut ──────────────────
    const handleEditorMount = useCallback((editor: MonacoEditor.IStandaloneCodeEditor, monaco: Monaco) => {
        editorRef.current = editor;

        // Pulisci eventuali disposables precedenti
        disposablesRef.current.forEach(d => d.dispose());
        disposablesRef.current = [];

        // Registra il linguaggio "latex" se non esiste
        const registered = monaco.languages.getLanguages().some((l: { id: string }) => l.id === 'latex');
        if (!registered) {
            monaco.languages.register({ id: 'latex', extensions: ['.tex', '.sty', '.cls'], aliases: ['LaTeX', 'latex'] });

            // Syntax highlighting basilare per LaTeX
            monaco.languages.setMonarchTokensProvider('latex', {
                tokenizer: {
                    root: [
                        [/%.*$/, 'comment'],
                        [/\\[a-zA-Z@]+/, 'keyword'],
                        [/\{/, 'delimiter.curly'],
                        [/\}/, 'delimiter.curly'],
                        [/\[/, 'delimiter.square'],
                        [/\]/, 'delimiter.square'],
                        [/\$\$/, 'string'],
                        [/\$/, 'string'],
                        [/\\\\/, 'keyword'],
                        [/&/, 'operator'],
                    ],
                },
            } as languages.IMonarchLanguage);
        }

        // Autocompletamento LaTeX
        const completionDisposable = monaco.languages.registerCompletionItemProvider('latex', {
            triggerCharacters: ['\\'],
            provideCompletionItems: (model: MonacoEditor.ITextModel, position: { lineNumber: number; column: number }) => {
                const wordInfo = model.getWordUntilPosition(position);
                // Controlla se c'è un backslash prima della parola
                const lineContent = model.getLineContent(position.lineNumber);
                const charBefore = lineContent[wordInfo.startColumn - 2];
                const startCol = charBefore === '\\' ? wordInfo.startColumn - 1 : wordInfo.startColumn;

                const range = {
                    startLineNumber: position.lineNumber,
                    startColumn: startCol,
                    endLineNumber: position.lineNumber,
                    endColumn: wordInfo.endColumn,
                };

                const suggestions: languages.CompletionItem[] = LATEX_COMPLETIONS.map(c => ({
                    label: c.label,
                    kind: monaco.languages.CompletionItemKind.Snippet,
                    insertText: c.insertText,
                    insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                    detail: c.detail,
                    range,
                }));

                return { suggestions };
            },
        });
        disposablesRef.current.push(completionDisposable);

        // Shortcut custom
        for (const shortcut of CUSTOM_SHORTCUTS) {
            const kb = parseKeybinding(shortcut.keys, monaco);
            editor.addAction({
                id: `custom-${shortcut.label.toLowerCase().replace(/\s/g, '-')}`,
                label: shortcut.label,
                keybindings: [kb],
                run: (ed) => {
                    const sel = ed.getSelection();
                    if (!sel) return;
                    if (shortcut.wrap) {
                        const selected = ed.getModel()?.getValueInRange(sel) ?? '';
                        ed.executeEdits('shortcut', [{ range: sel, text: `${shortcut.wrap[0]}${selected}${shortcut.wrap[1]}` }]);
                    } else if (shortcut.insert) {
                        ed.executeEdits('shortcut', [{ range: sel, text: shortcut.insert }]);
                    } else if (shortcut.run) {
                        shortcut.run();
                    }
                },
            });
        }

        // Ctrl+S / Cmd+S → compila
        editor.addAction({
            id: 'compile-latex',
            label: 'Compila LaTeX (pdflatex)',
            keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS],
            run: () => { compilePdf(); },
        });

        // Focus sull'editor
        editor.focus();
    }, [compilePdf]);


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
                <Editor
                    height="100%"
                    defaultLanguage="latex"
                    theme="vs-dark"
                    value={code}
                    onChange={(value) => setCode(value || '')}
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
                        suggestOnTriggerCharacters: true,
                        quickSuggestions: true,
                        snippetSuggestions: 'top',
                        tabCompletion: 'on',
                        bracketPairColorization: { enabled: true },
                        autoClosingBrackets: 'always',
                    }}
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