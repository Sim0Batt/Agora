import { dismissImport, useImportJobs } from '../context/importJobs';

export const ImportStatus = () => {
  const jobs = useImportJobs();
  if (jobs.length === 0) return null;

  return (
    <div className="fixed right-4 top-4 z-40 flex w-72 max-w-[calc(100vw-2rem)] flex-col gap-2">
      {jobs.map((job) => (
        <div
          key={job.id}
          className="flex items-start gap-3 rounded-lg border border-border bg-surface-alt px-4 py-3 text-sm shadow-lg"
        >
          {job.status === 'running' && (
            <span className="mt-0.5 h-4 w-4 shrink-0 animate-spin rounded-full border-2 border-border border-t-accent" />
          )}
          <div className="min-w-0 flex-1">
            <p className="truncate font-semibold">{job.title}</p>
            <p
              className={
                job.status === 'error' ? 'text-red-400' : job.status === 'done' ? 'text-green-400' : 'text-text-muted'
              }
            >
              {job.status === 'running'
                ? 'Importazione in background…'
                : job.status === 'done'
                  ? 'Playlist importata.'
                  : job.error}
            </p>
          </div>
          {job.status !== 'running' && (
            <button
              onClick={() => dismissImport(job.id)}
              aria-label="Chiudi"
              className="shrink-0 text-text-muted hover:text-accent"
            >
              ✕
            </button>
          )}
        </div>
      ))}
    </div>
  );
};
