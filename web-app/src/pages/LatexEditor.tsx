import React, { useState, useRef } from 'react';
import Editor, { type Monaco } from '@monaco-editor/react';
import type { editor as MonacoEditor } from 'monaco-editor';
import ReactMarkdown from 'react-markdown';
import remarkMath from 'remark-math';
import rehypeKatex from 'rehype-katex';
import 'katex/dist/katex.min.css';

// ─── CUSTOM KEYBOARD SHORTCUTS ─────────────────────────────────────────────
// Aggiungi qui i tuoi shortcut. Ogni shortcut viene registrato in Monaco Editor.
//
//   - keys:   combinazione di tasti (es. "Ctrl+B", "Ctrl+Shift+F")
//   - insert: testo inserito alla posizione del cursore
//   - wrap:   avvolge la selezione corrente con [prima, dopo]
//             se non c'è selezione, inserisce "prima + dopo" e posiziona il cursore in mezzo
//   - run:    funzione custom (side-effect libero)
//
// ESEMPI già funzionanti: Ctrl+B → \textbf{},  Ctrl+I → \textit{}
// Per aggiungerne uno nuovo basta copiare il pattern e cambiare keys/wrap/insert.

interface Shortcut {
    label: string;
    keys: string;
    insert?: string;
    wrap?: [string, string];
    run?: () => void;
}

const CUSTOM_SHORTCUTS: Shortcut[] = [
    // ── I tuoi shortcut ─────────────────────────────────────────────────
    {
        label: 'Bold (textbf)',
        keys: 'Ctrl+B',
        wrap: ['\\textbf{', '}'],
    },
    {
        label: 'Italic (textit)',
        keys: 'Ctrl+I',
        wrap: ['\\textit{', '}'],
    },
    // Per aggiungere un nuovo shortcut, copia il blocco qui sotto e modifica:
    // {
    //     label: 'Nome descrittivo',
    //     keys: 'Ctrl+Shift+X',       // la combinazione di tasti
    //     wrap: ['\\comando{', '}'],   // oppure usa insert: '\\comando{} '
    // },
    // ── Fine shortcut ───────────────────────────────────────────────────
];

// Converte la stringa "Ctrl+Shift+F" nel formato Monaco keybinding
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

export default function LatexEditor() {
    const [code, setCode] = useState(
        '# Titolo della Nota\n\nScrivi qui i tuoi appunti. Puoi inserire la matematica inline così $a^2 + b^2 = c^2$, oppure in un blocco separato:\n\n$$ x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a} $$'
    );
    const editorRef = useRef<MonacoEditor.IStandaloneCodeEditor | null>(null);

    const salvaNota = () => {
        console.log('Testo inviato al backend:', code);
    };

    const handleEditorMount = (editor: MonacoEditor.IStandaloneCodeEditor, monaco: Monaco) => {
        editorRef.current = editor;

        // Registra ogni shortcut custom nell'editor Monaco
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
                        ed.executeEdits('shortcut', [{
                            range: sel,
                            text: `${shortcut.wrap[0]}${selected}${shortcut.wrap[1]}`,
                        }]);
                    } else if (shortcut.insert) {
                        ed.executeEdits('shortcut', [{
                            range: sel,
                            text: shortcut.insert,
                        }]);
                    } else if (shortcut.run) {
                        shortcut.run();
                    }
                },
            });
        }
    };

    return (
        <div className="flex flex-col h-screen bg-surface text-text">
            {/* Toolbar */}
            <div className="flex items-center justify-between px-5 py-3 bg-surface-alt border-b border-border">
                <h2 className="text-base font-semibold tracking-wide">Agorà — LaTeX Editor</h2>
                <div className="flex items-center gap-3">
                    <button
                        onClick={salvaNota}
                        className="px-4 py-1.5 text-sm font-medium rounded bg-accent text-white hover:bg-accent-hover transition-colors cursor-pointer"
                    >
                        Salva
                    </button>
                </div>
            </div>

            {/* Editor + Preview */}
            <div className="flex flex-1 overflow-hidden">
                {/* Monaco Editor */}
                <div className="w-1/2 border-r border-border">
                    <Editor
                        height="100%"
                        defaultLanguage="markdown"
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
                            renderLineHighlight: 'none',
                            overviewRulerBorder: false,
                            hideCursorInOverviewRuler: true,
                            scrollbar: { verticalScrollbarSize: 6, horizontalScrollbarSize: 6 },
                        }}
                    />
                </div>

                {/* Preview */}
                <div className="w-1/2 overflow-y-auto p-8 bg-surface prose-invert
                    [&_h1]:text-2xl [&_h1]:font-semibold [&_h1]:border-b [&_h1]:border-border [&_h1]:pb-3 [&_h1]:mb-4 [&_h1]:text-text
                    [&_h2]:text-xl [&_h2]:font-semibold [&_h2]:text-text [&_h2]:mt-6 [&_h2]:mb-2
                    [&_p]:text-base [&_p]:leading-relaxed [&_p]:text-text-muted [&_p]:my-3
                    [&_code]:text-accent [&_code]:text-sm
                    [&_.katex]:text-text">
                    <ReactMarkdown
                        remarkPlugins={[remarkMath]}
                        rehypePlugins={[rehypeKatex]}
                    >
                        {code}
                    </ReactMarkdown>
                </div>
            </div>
        </div>
    );
}