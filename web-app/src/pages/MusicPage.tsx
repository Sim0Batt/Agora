import { useCallback, useEffect, useState } from 'react';
import { DashBoard } from '../components/Dashboard';
import { musicApi } from '../api/music';
import type { PickedTrack, SearchResult, Song } from '../types/models';

const formatDuration = (seconds: number) => {
  const m = Math.floor(seconds / 60);
  const s = Math.floor(seconds % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
};

const errorMessage = (e: unknown, fallback: string) => {
  const message = (e as { response?: { data?: unknown } })?.response?.data;
  return typeof message === 'string' && message ? message : fallback;
};

export const MusicPage = () => {
  const [query, setQuery] = useState('');
  const [searching, setSearching] = useState(false);
  const [result, setResult] = useState<SearchResult | null>(null);
  const [searchError, setSearchError] = useState<string | null>(null);

  const [downloadingIdx, setDownloadingIdx] = useState<number | null>(null);
  const [downloaded, setDownloaded] = useState<PickedTrack | null>(null);
  const [downloadError, setDownloadError] = useState<string | null>(null);

  const [songs, setSongs] = useState<Song[]>([]);
  const [songsError, setSongsError] = useState(false);

  const loadSongs = useCallback(() => {
    musicApi
      .listSongs()
      .then((data) => {
        setSongs(data);
        setSongsError(false);
      })
      .catch(() => setSongsError(true));
  }, []);

  useEffect(() => {
    loadSongs();
  }, [loadSongs]);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    const title = query.trim();
    if (!title || searching) return;
    setSearching(true);
    setSearchError(null);
    setDownloaded(null);
    setDownloadError(null);
    setResult(null);
    try {
      setResult(await musicApi.search(title));
    } catch (err) {
      setSearchError(errorMessage(err, 'Ricerca fallita o scaduta, riprova.'));
    } finally {
      setSearching(false);
    }
  };

  const handleDownload = async (index: number) => {
    if (!result || downloadingIdx !== null) return;
    setDownloadingIdx(index);
    setDownloadError(null);
    setDownloaded(null);
    try {
      setDownloaded(await musicApi.download(result.messageId, index));
      loadSongs();
    } catch (err) {
      setDownloadError(errorMessage(err, 'Download fallito. I risultati potrebbero essere scaduti: rifai la ricerca.'));
    } finally {
      setDownloadingIdx(null);
    }
  };

  return (
    <div className="min-h-screen bg-surface text-text">
      <DashBoard />
      <main className="mx-auto max-w-6xl space-y-10 px-4 py-10">
        <section>
          <h1 className="text-3xl font-bold">Musica</h1>
          <p className="mt-1 text-text-muted">Cerca un brano, scegli il risultato e scaricalo sul server.</p>
        </section>

        <section className="space-y-4">
          <form onSubmit={handleSearch} className="flex gap-2">
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Titolo o artista"
              className="flex-1 rounded-lg border border-border bg-surface-alt px-4 py-2 text-text outline-none focus:border-accent"
            />
            <button
              type="submit"
              disabled={searching || !query.trim()}
              className="rounded-lg bg-accent px-5 py-2 font-semibold text-white transition-colors hover:bg-accent-hover disabled:opacity-50"
            >
              {searching ? 'Ricerca…' : 'Cerca'}
            </button>
          </form>

          {searchError && <p className="text-sm text-red-400">{searchError}</p>}
          {downloadError && <p className="text-sm text-red-400">{downloadError}</p>}
          {downloaded && (
            <p className="rounded-lg border border-green-500/30 bg-green-500/15 px-4 py-2 text-sm text-green-400">
              Scaricato: {downloaded.artist} – {downloaded.title} ({formatDuration(downloaded.duration)})
            </p>
          )}

          {result && (
            <ul className="divide-y divide-border rounded-lg border border-border bg-surface-alt">
              {result.items.length === 0 && (
                <li className="px-4 py-3 text-sm text-text-muted">Nessun risultato.</li>
              )}
              {result.items.map((item) => (
                <li key={item.index} className="flex items-center justify-between gap-4 px-4 py-3">
                  <span className="truncate">{item.title}</span>
                  <button
                    onClick={() => handleDownload(item.index)}
                    disabled={downloadingIdx !== null}
                    className="shrink-0 rounded-md border border-border px-3 py-1 text-sm transition-colors hover:border-accent hover:text-accent disabled:opacity-50"
                  >
                    {downloadingIdx === item.index ? 'Download…' : 'Scarica'}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section>
          <h2 className="mb-4 text-sm font-semibold uppercase tracking-widest text-text-muted">Libreria</h2>
          {songsError ? (
            <p className="text-sm text-text-muted">Impossibile caricare le canzoni.</p>
          ) : songs.length === 0 ? (
            <p className="text-sm text-text-muted">Nessuna canzone scaricata.</p>
          ) : (
            <ul className="divide-y divide-border rounded-lg border border-border bg-surface-alt">
              {songs.map((song) => (
                <li key={song.id} className="flex items-center justify-between gap-4 px-4 py-3">
                  <div className="min-w-0">
                    <p className="truncate font-medium">{song.title}</p>
                    <p className="truncate text-sm text-text-muted">{song.author}</p>
                  </div>
                  <span className="shrink-0 font-mono text-sm text-text-muted">{formatDuration(song.duration)}</span>
                </li>
              ))}
            </ul>
          )}
        </section>
      </main>
    </div>
  );
};
