#!/usr/bin/env python3
"""Elenca le canzoni di una playlist YouTube.

Uso: python3 yt_playlist.py "url_playlist"
Output (stdout): un array JSON di oggetti compatibili con SongJson:
  {"id", "title", "author", "duration", "url"}
  (id = id del video YouTube; "url" è un campo in più, ignorabile con ignoreUnknownKeys)
Video privati/eliminati vengono saltati.
Errori: messaggio su stderr, exit code 1.
"""
import json
import shutil
import subprocess
import sys
from typing import Optional
from urllib.parse import parse_qs, urlparse

ALLOWED_HOSTS = {"youtube.com", "www.youtube.com", "m.youtube.com", "music.youtube.com"}


def playlist_url(url: str) -> Optional[str]:
    parsed = urlparse(url)
    if parsed.scheme not in ("http", "https") or parsed.hostname not in ALLOWED_HOSTS:
        return None
    list_id = parse_qs(parsed.query).get("list", [None])[0]
    if not list_id:
        return None
    return f"https://www.youtube.com/playlist?list={list_id}"


def main() -> int:
    if len(sys.argv) != 2 or not sys.argv[1].strip():
        print('Uso: python3 yt_playlist.py "url_playlist"', file=sys.stderr)
        return 1
    if shutil.which("yt-dlp") is None:
        print("yt-dlp non trovato nel PATH", file=sys.stderr)
        return 1

    url = playlist_url(sys.argv[1].strip())
    if url is None:
        print("URL non valido: serve un link a una playlist YouTube (con parametro list=)", file=sys.stderr)
        return 1

    try:
        proc = subprocess.run(
            ["yt-dlp", url, "--flat-playlist", "--dump-json", "--no-warnings"],
            capture_output=True,
            text=True,
            timeout=300,
        )
    except subprocess.TimeoutExpired:
        print("Timeout durante la lettura della playlist", file=sys.stderr)
        return 1
    if proc.returncode != 0:
        print(proc.stderr.strip() or "Lettura playlist fallita", file=sys.stderr)
        return 1

    results = []
    for line in proc.stdout.splitlines():
        if not line.strip():
            continue
        info = json.loads(line)
        video_id = info.get("id")
        title = info.get("title") or ""
        if not video_id or title in ("[Private video]", "[Deleted video]"):
            continue
        author = info.get("channel") or info.get("uploader") or ""
        results.append(
            {
                "id": video_id,
                "title": title,
                "author": author.removesuffix(" - Topic"),
                "duration": int(info.get("duration") or 0),
                "url": f"https://www.youtube.com/watch?v={video_id}",
            }
        )

    print(json.dumps(results, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    sys.exit(main())
