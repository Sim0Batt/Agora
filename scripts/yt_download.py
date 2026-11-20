#!/usr/bin/env python3
"""Scarica l'audio di un video YouTube (miglior audio, di solito Opus/.webm).

Uso: python3 yt_download.py "url" "cartella_destinazione"
Output (stdout): "OK" se il download è riuscito, altrimenti "FAILURE" (senza a capo finale:
il backend confronta l'intero output con la stringa "FAILURE").
Il file viene salvato come <id_video>.<ext> nella cartella indicata.
I dettagli dell'errore (se presenti) vanno su stderr; exit code 0 = OK, 1 = FAILURE.
"""
import shutil
import subprocess
import sys
import time
from pathlib import Path
from urllib.parse import urlparse

ALLOWED_HOSTS = {"youtube.com", "www.youtube.com", "m.youtube.com", "music.youtube.com", "youtu.be"}


def is_youtube_url(url: str) -> bool:
    parsed = urlparse(url)
    return parsed.scheme in ("http", "https") and parsed.hostname in ALLOWED_HOSTS


MAX_ATTEMPTS = 3


def js_runtime_args() -> list:
    # Senza runtime JS yt-dlp può non trovare tutti i formati (e ricevere 403)
    if shutil.which("deno"):
        return []
    if shutil.which("node"):
        return ["--js-runtimes", "node"]
    return []


def download(url: str, dest: Path) -> None:
    if shutil.which("yt-dlp") is None:
        raise RuntimeError("yt-dlp non trovato nel PATH")
    if not is_youtube_url(url):
        raise ValueError("URL non valido: è accettato solo YouTube")

    dest.mkdir(parents=True, exist_ok=True)
    last_error = "Download fallito"
    for attempt in range(1, MAX_ATTEMPTS + 1):
        proc = subprocess.run(
            [
                "yt-dlp",
                url,
                "--no-playlist",
                "-f", "bestaudio[ext=webm]/bestaudio",
                "-o", str(dest / "%(id)s.%(ext)s"),
                "--print", "after_move:filepath",
                "--no-warnings",
                "--quiet",
                "--retries", "5",
                "--fragment-retries", "5",
                *js_runtime_args(),
            ],
            capture_output=True,
            text=True,
            timeout=600,
        )
        # yt-dlp può uscire con errore parziale o senza file: si considera riuscito solo se il file esiste
        paths = [Path(line.strip()) for line in proc.stdout.splitlines() if line.strip()]
        if proc.returncode == 0 and paths and all(f.is_file() and f.stat().st_size > 0 for f in paths):
            return
        last_error = proc.stderr.strip() or "Download fallito: nessun file prodotto"
        if attempt < MAX_ATTEMPTS:
            time.sleep(2 * attempt)
    raise RuntimeError(last_error)


def main() -> int:
    if len(sys.argv) != 3 or not sys.argv[1].strip() or not sys.argv[2].strip():
        print('Uso: python3 yt_download.py "url" "cartella_destinazione"', file=sys.stderr)
        print("FAILURE", end="")
        return 1
    try:
        download(sys.argv[1].strip(), Path(sys.argv[2]).expanduser().resolve())
    except Exception as e:
        print(e, file=sys.stderr)
        print("FAILURE", end="")
        return 1
    print("OK", end="")
    return 0


if __name__ == "__main__":
    sys.exit(main())
