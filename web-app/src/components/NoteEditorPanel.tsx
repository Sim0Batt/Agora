import { useRef, useCallback } from 'react';
import Editor, { type Monaco } from '@monaco-editor/react';
import type { editor as MonacoEditor, IDisposable } from 'monaco-editor';

interface NoteEditorPanelProps {
    value: string;
    onChange?: (value: string) => void;
    onSave?: () => void;
    readOnly?: boolean;
}

export default function NoteEditorPanel({ value, onChange, onSave, readOnly = false }: NoteEditorPanelProps) {
    const editorRef = useRef<MonacoEditor.IStandaloneCodeEditor | null>(null);
    const disposablesRef = useRef<IDisposable[]>([]);

    const handleEditorMount = useCallback(
        (editor: MonacoEditor.IStandaloneCodeEditor, monaco: Monaco) => {
            editorRef.current = editor;

            disposablesRef.current.forEach((d) => d.dispose());
            disposablesRef.current = [];

            if (onSave && !readOnly) {
                editor.addAction({
                    id: 'save-note',
                    label: 'Salva nota',
                    keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS],
                    run: () => {
                        onSave();
                    },
                });
            }

            editor.focus();
        },
        [onSave, readOnly],
    );

    return (
        <Editor
            height="100%"
            defaultLanguage="plaintext"
            theme="vs-dark"
            value={value}
            onChange={(v) => onChange?.(v || '')}
            onMount={handleEditorMount}
            options={{
                readOnly,
                domReadOnly: readOnly,
                minimap: { enabled: false },
                wordWrap: 'on',
                fontSize: 15,
                lineHeight: 1.6,
                padding: { top: 16 },
                scrollBeyondLastLine: false,
                renderLineHighlight: readOnly ? 'none' : 'gutter',
                overviewRulerBorder: false,
                hideCursorInOverviewRuler: true,
                scrollbar: { verticalScrollbarSize: 6, horizontalScrollbarSize: 6 },
            }}
        />
    );
}
