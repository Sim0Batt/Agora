import { useRef, useCallback } from 'react';
import Editor, { type Monaco } from '@monaco-editor/react';
import { editor as MonacoEditor, type languages, type IDisposable } from 'monaco-editor';

// ─── CUSTOM KEYBOARD SHORTCUTS ─────────────────────────────────────────────

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
    { label: 'Bold Item', keys: 'Ctrl+Shift+L', wrap: ['\\item \\textbf{', '}'] },
    { label: 'Itemize', keys: 'Ctrl+L', wrap: ['\\begin{itemize}\n    \\item ', '\n\\end{itemize}'] },
    { label: 'Insert Graphics', keys: 'Ctrl+G', wrap: ['\\begin{center}\n    %\\includegraphics[width=0.8\\textwidth]{img/', '}\n\\end{center}'] },
    { label: 'Equation', keys: 'Ctrl+P', wrap: ['$', '$'] },
];

// ─── LATEX COMPLETIONS ─────────────────────────────────────────────────────

const LATEX_COMPLETIONS: { label: string; insertText: string; detail: string }[] = [
    { label: '\\documentclass', insertText: '\\documentclass{${1:article}}', detail: 'Document class' },
    { label: '\\usepackage', insertText: '\\usepackage{${1:package}}', detail: 'Use package' },
    { label: '\\begin', insertText: '\\begin{${1:environment}}\n\t$0\n\\end{${1:environment}}', detail: 'Begin environment' },
    { label: '\\end', insertText: '\\end{${1:environment}}', detail: 'End environment' },
    { label: '\\title', insertText: '\\title{${1:title}}', detail: 'Title' },
    { label: '\\author', insertText: '\\author{${1:name}}', detail: 'Author' },
    { label: '\\date', insertText: '\\date{${1:\\today}}', detail: 'Date' },
    { label: '\\maketitle', insertText: '\\maketitle', detail: 'Make title' },
    { label: '\\section', insertText: '\\section{${1:title}}', detail: 'Section' },
    { label: '\\subsection', insertText: '\\subsection{${1:title}}', detail: 'Subsection' },
    { label: '\\subsubsection', insertText: '\\subsubsection{${1:title}}', detail: 'Subsubsection' },
    { label: '\\paragraph', insertText: '\\paragraph{${1:title}}', detail: 'Paragraph' },
    { label: '\\chapter', insertText: '\\chapter{${1:title}}', detail: 'Chapter' },
    { label: '\\textbf', insertText: '\\textbf{${1:text}}', detail: 'Bold text' },
    { label: '\\textit', insertText: '\\textit{${1:text}}', detail: 'Italic text' },
    { label: '\\underline', insertText: '\\underline{${1:text}}', detail: 'Underline' },
    { label: '\\emph', insertText: '\\emph{${1:text}}', detail: 'Emphasis' },
    { label: '\\texttt', insertText: '\\texttt{${1:text}}', detail: 'Monospace' },
    { label: '\\textsc', insertText: '\\textsc{${1:text}}', detail: 'Small caps' },
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
    { label: '\\begin{equation}', insertText: '\\begin{equation}\n\t$0\n\\end{equation}', detail: 'Equation env' },
    { label: '\\begin{align}', insertText: '\\begin{align}\n\t$0\n\\end{align}', detail: 'Align env' },
    { label: '\\begin{itemize}', insertText: '\\begin{itemize}\n\t\\item $0\n\\end{itemize}', detail: 'Itemize env' },
    { label: '\\begin{enumerate}', insertText: '\\begin{enumerate}\n\t\\item $0\n\\end{enumerate}', detail: 'Enumerate env' },
    { label: '\\begin{figure}', insertText: '\\begin{figure}[${1:htbp}]\n\t\\centering\n\t\\includegraphics[width=${2:0.8}\\textwidth]{${3:file}}\n\t\\caption{${4:caption}}\n\t\\label{fig:${5:label}}\n\\end{figure}', detail: 'Figure env' },
    { label: '\\begin{table}', insertText: '\\begin{table}[${1:htbp}]\n\t\\centering\n\t\\begin{tabular}{${2:ll}}\n\t\t$0\n\t\\end{tabular}\n\t\\caption{${3:caption}}\n\t\\label{tab:${4:label}}\n\\end{table}', detail: 'Table env' },
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

interface LatexEditorPanelProps {
    value: string;
    onChange: (value: string) => void;
    onSave?: () => void;
}

export default function LatexEditorPanel({ value, onChange, onSave }: LatexEditorPanelProps) {
    const editorRef = useRef<MonacoEditor.IStandaloneCodeEditor | null>(null);
    const disposablesRef = useRef<IDisposable[]>([]);
    const onSaveRef = useRef(onSave);
    onSaveRef.current = onSave;

    const handleEditorMount = useCallback((editor: MonacoEditor.IStandaloneCodeEditor, monaco: Monaco) => {
        editorRef.current = editor;

        // Sync external value changes imperatively
        editor.onDidChangeModelContent(() => {
            const currentValue = editor.getValue();
            onChange(currentValue);
        });

        disposablesRef.current.forEach(d => d.dispose());
        disposablesRef.current = [];

        // Registra il linguaggio "latex" se non esiste
        const registered = monaco.languages.getLanguages().some((l: { id: string }) => l.id === 'latex');
        if (!registered) {
            monaco.languages.register({ id: 'latex', extensions: ['.tex', '.sty', '.cls'], aliases: ['LaTeX', 'latex'] });

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
                        if (!selected) {
                            ed.setPosition({ lineNumber: sel.startLineNumber, column: sel.startColumn + shortcut.wrap[0].length });
                        }
                    } else if (shortcut.insert) {
                        ed.executeEdits('shortcut', [{ range: sel, text: shortcut.insert }]);
                    } else if (shortcut.run) {
                        shortcut.run();
                    }
                },
            });
        }

        // Ctrl+S / Cmd+S → salva
        editor.addAction({
            id: 'save-latex',
            label: 'Salva LaTeX',
            keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS],
            run: () => { onSaveRef.current?.(); },
        });

        editor.focus();
    }, []);

    // Sync external value changes (e.g. from parent) without resetting cursor
    const prevValueRef = useRef(value);
    if (editorRef.current && value !== prevValueRef.current && value !== editorRef.current.getValue()) {
        editorRef.current.setValue(value);
    }
    prevValueRef.current = value;

    return (
        <Editor
            height="100%"
            defaultLanguage="latex"
            theme="vs-dark"
            defaultValue={value}
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
                quickSuggestions: false,
                snippetSuggestions: 'top',
                tabCompletion: 'on',
                stickyScroll: { enabled: false },
                bracketPairColorization: { enabled: true },
                autoClosingBrackets: 'always',
            }}
        />
    );
}
