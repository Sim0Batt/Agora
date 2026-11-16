#!/usr/bin/env python3
"""Cerca una canzone su YouTube e stampa i primi 10 risultati.

Uso: python3 yt_search.py "nome canzone"
Output (stdout): un array JSON di oggetti compatibili con SongJson:
  {"id", "title", "author", "duration", "url"}
  (id = id del video YouTube; "url" è un campo in più, ignorabile con ignoreUnknownKeys)
Errori: messaggio su stderr, exit code 1.
"""
import json
import shutil
import subprocess
import sys

RESULTS = 10


def main() -> int:
    if len(sys.argv) != 2 or not sys.argv[1].strip():
        print('Uso: python3 yt_search.py "nome canzone"', file=sys.stderr)
        return 1
    if shutil.which("yt-dlp") is None:
        print("yt-dlp non trovato nel PATH", file=sys.stderr)
        return 1

    query = sys.argv[1].strip()
    proc = subprocess.run(
        ["yt-dlp", f"ytsearch{RESULTS}:{query}", "--flat-playlist", "--dump-json", "--no-warnings"],
        capture_output=True,
        text=True,
        timeout=60,
    )
    if proc.returncode != 0:
        print(proc.stderr.strip() or "Ricerca fallita", file=sys.stderr)
        return 1

    results = []
    for line in proc.stdout.splitlines():
        if not line.strip():
            continue
        info = json.loads(line)
        video_id = info.get("id")
        if not video_id:
            continue
        author = info.get("channel") or info.get("uploader") or ""
        results.append(
            {
                "id": video_id,
                "title": info.get("title") or "",
                "author": author.removesuffix(" - Topic"),
                "duration": int(info.get("duration") or 0),
                "url": info.get("url") or f"https://www.youtube.com/watch?v={video_id}",
            }
        )

    print(json.dumps(results, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    sys.exit(main())
