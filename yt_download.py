#!/usr/bin/env python3
"""Scarica l'audio di un video YouTube (miglior audio, di solito Opus/.webm).

Uso: python3 yt_download.py "url" "cartella_destinazione"
Output (stdout): "OK" se il download è riuscito, altrimenti "FAILURE".
Il file viene salvato come <id_video>.<ext> nella cartella indicata.
I dettagli dell'errore (se presenti) vanno su stderr; exit code 0 = OK, 1 = FAILURE.
"""
import shutil
import subprocess
import sys
from pathlib import Path
from urllib.parse import urlparse

ALLOWED_HOSTS = {"youtube.com", "www.youtube.com", "m.youtube.com", "music.youtube.com", "youtu.be"}


def is_youtube_url(url: str) -> bool:
    parsed = urlparse(url)
    return parsed.scheme in ("http", "https") and parsed.hostname in ALLOWED_HOSTS


def download(url: str, dest: Path) -> None:
    if shutil.which("yt-dlp") is None:
        raise RuntimeError("yt-dlp non trovato nel PATH")
    if not is_youtube_url(url):
        raise ValueError("URL non valido: è accettato solo YouTube")

    dest.mkdir(parents=True, exist_ok=True)
    proc = subprocess.run(
        [
            "yt-dlp",
            url,
            "--no-playlist",
            "-f", "bestaudio[ext=webm]/bestaudio",
            "-o", str(dest / "%(id)s.%(ext)s"),
            "--no-warnings",
            "--quiet",
        ],
        capture_output=True,
        text=True,
        timeout=600,
    )
    if proc.returncode != 0:
        raise RuntimeError(proc.stderr.strip() or "Download fallito")


def main() -> int:
    if len(sys.argv) != 3 or not sys.argv[1].strip() or not sys.argv[2].strip():
        print('Uso: python3 yt_download.py "url" "cartella_destinazione"', file=sys.stderr)
        print("FAILURE")
        return 1
    try:
        download(sys.argv[1].strip(), Path(sys.argv[2]).expanduser().resolve())
    except Exception as e:
        print(e, file=sys.stderr)
        print("FAILURE")
        return 1
    print("OK")
    return 0


if __name__ == "__main__":
    sys.exit(main())
