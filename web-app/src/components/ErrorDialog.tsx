import { CloseIcon } from './Icons';

export const ErrorDialog = ({ title, message, onClose }: { title: string; message: string; onClose: () => void }) => (
  <div className="fixed inset-0 z-[70] flex items-center justify-center bg-black/60 px-4" onClick={onClose}>
    <div
      onClick={(e) => e.stopPropagation()}
      role="alertdialog"
      aria-label={title}
      className="flex max-h-[80vh] w-full max-w-2xl flex-col rounded-xl border border-red-500/40 bg-surface-alt"
    >
      <div className="flex items-center justify-between border-b border-border px-5 py-3">
        <h2 className="text-lg font-semibold text-red-400">{title}</h2>
        <button onClick={onClose} aria-label="Chiudi" className="-mr-2 p-2 text-text-muted hover:text-text">
          <CloseIcon className="h-5 w-5" />
        </button>
      </div>
      <pre className="min-h-0 flex-1 overflow-auto whitespace-pre-wrap break-words px-5 py-4 font-mono text-xs text-text-muted">
        {message}
      </pre>
      <div className="flex justify-end border-t border-border px-5 py-3">
        <button
          onClick={onClose}
          className="rounded-lg border border-border px-4 py-2 transition-colors hover:border-accent"
        >
          Chiudi
        </button>
      </div>
    </div>
  </div>
);
