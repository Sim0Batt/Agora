import { useCallback, useEffect, useRef, useState } from 'react';
import { latexApi } from '../api/latex';
import { CloseIcon } from './Icons';

export const LatexImagesDialog = ({ title, onClose }: { title: string; onClose: () => void }) => {
  const [images, setImages] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [listError, setListError] = useState(false);
  const [pending, setPending] = useState<File[]>([]);
  const [dragging, setDragging] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [copied, setCopied] = useState<string | null>(null);
  const inputRef = useRef<HTMLInputElement | null>(null);

  const load = useCallback(() => {
    setLoading(true);
    latexApi
      .listImages(title)
      .then((data) => {
        setImages(data);
        setListError(false);
      })
      .catch(() => {
        // La cartella non esiste finché non si carica la prima immagine
        setImages([]);
        setListError(true);
      })
      .finally(() => setLoading(false));
  }, [title]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);

  // Le selezioni successive si sommano; un file con lo stesso nome sostituisce il precedente
  const addFiles = (incoming: File[]) => {
    const images = incoming.filter((f) => f.type.startsWith('image/') || /\.(jpe?g|png|img)$/i.test(f.name));
    if (!images.length) return;
    setUploadError(null);
    setPending((prev) => {
      const names = new Set(images.map((f) => f.name));
      return [...prev.filter((f) => !names.has(f.name)), ...images];
    });
  };

  const handleSelected = (e: React.ChangeEvent<HTMLInputElement>) => {
    addFiles(Array.from(e.target.files ?? []));
    e.target.value = '';
  };

  const handleUpload = async () => {
    if (!pending.length || uploading) return;
    setUploading(true);
    setUploadError(null);
    try {
      await latexApi.uploadImages(title, pending);
      setPending([]);
      load();
    } catch {
      setUploadError('Caricamento fallito, riprova.');
    } finally {
      setUploading(false);
    }
  };

  const copySnippet = (name: string) => {
    void navigator.clipboard
      ?.writeText(`\\includegraphics[width=0.8\\textwidth]{${name}}`)
      .then(() => {
        setCopied(name);
        setTimeout(() => setCopied((c) => (c === name ? null : c)), 1500);
      })
      .catch(() => undefined);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4" onClick={onClose}>
      <div
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-label="Immagini"
        className="flex max-h-[85vh] w-full max-w-lg flex-col rounded-xl border border-border bg-surface-alt"
      >
        <div className="flex items-center justify-between border-b border-border px-5 py-3">
          <div className="min-w-0">
            <h2 className="text-lg font-semibold">Immagini</h2>
            <p className="truncate text-xs text-text-muted">{title}</p>
          </div>
          <button onClick={onClose} aria-label="Chiudi" className="-mr-2 p-2 text-text-muted hover:text-text">
            <CloseIcon className="h-5 w-5" />
          </button>
        </div>

        <div className="min-h-0 flex-1 overflow-y-auto px-5 py-3">
          {loading ? (
            <p className="text-sm text-text-muted">Caricamento…</p>
          ) : images.length === 0 ? (
            <p className="text-sm text-text-muted">
              {listError ? 'Nessuna immagine caricata o elenco non disponibile.' : 'Nessuna immagine caricata.'}
            </p>
          ) : (
            <ul className="divide-y divide-border">
              {images.map((name) => (
                <li key={name} className="flex items-center justify-between gap-3 py-2">
                  <span className="truncate font-mono text-sm" title={name}>
                    {name}
                  </span>
                  <button
                    onClick={() => copySnippet(name)}
                    className="shrink-0 rounded-md border border-border px-3 py-1 text-xs transition-colors hover:border-accent hover:text-accent"
                  >
                    {copied === name ? 'Copiato' : 'Copia \\includegraphics'}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div
          onDragOver={(e) => {
            e.preventDefault();
            setDragging(true);
          }}
          onDragLeave={() => setDragging(false)}
          onDrop={(e) => {
            e.preventDefault();
            setDragging(false);
            addFiles(Array.from(e.dataTransfer.files));
          }}
          className={`space-y-2 border-t px-5 py-3 ${dragging ? 'border-accent bg-accent/10' : 'border-border'}`}
        >
          {pending.length > 0 && (
            <ul className="max-h-32 divide-y divide-border overflow-y-auto rounded-md border border-border">
              {pending.map((f) => (
                <li key={f.name} className="flex items-center justify-between gap-2 px-3 py-1.5 text-sm">
                  <span className="truncate font-mono" title={f.name}>
                    {f.name}
                  </span>
                  <button
                    onClick={() => setPending((prev) => prev.filter((x) => x !== f))}
                    disabled={uploading}
                    aria-label={`Rimuovi ${f.name}`}
                    className="shrink-0 text-text-muted hover:text-red-400"
                  >
                    <CloseIcon className="h-4 w-4" />
                  </button>
                </li>
              ))}
            </ul>
          )}
          {uploadError && <p className="text-sm text-red-400">{uploadError}</p>}
          <input ref={inputRef} type="file" accept="image/*" multiple className="hidden" onChange={handleSelected} />
          <div className="flex gap-2">
            <button
              onClick={() => inputRef.current?.click()}
              disabled={uploading}
              className="flex-1 rounded-lg border border-border px-4 py-2 font-semibold transition-colors hover:border-accent hover:text-accent disabled:opacity-50"
            >
              {pending.length ? 'Aggiungi altre' : 'Seleziona immagini'}
            </button>
            {pending.length > 0 && (
              <button
                onClick={handleUpload}
                disabled={uploading}
                className="flex-1 rounded-lg bg-accent px-4 py-2 font-semibold text-white transition-colors hover:bg-accent-hover disabled:opacity-50"
              >
                {uploading ? 'Caricamento…' : `Carica ${pending.length} ${pending.length === 1 ? 'immagine' : 'immagini'}`}
              </button>
            )}
          </div>
          <p className="text-xs text-text-muted">Puoi anche trascinare qui più immagini.</p>
        </div>
      </div>
    </div>
  );
};
