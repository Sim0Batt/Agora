#!/usr/bin/env python3
"""Mostra su un OLED SSD1306 (128x64, I2C) lo stato delle applicazioni del Raspberry e la RAM usata.

Ogni applicazione ha un endpoint /status che risponde "OK": qualsiasi altra risposta o mancata
risposta viene mostrata come FAILURE. Lo stato viene riletto ogni minuto.
Per aggiungere un'applicazione basta una riga in APPS.
"""
import os
import signal
import time
import urllib.request

from luma.core.interface.serial import i2c
from luma.core.render import canvas
from luma.oled.device import ssd1306

# nome mostrato -> URL dell'endpoint di stato
APPS = {
    "Agora": "http://localhost:18035/status",
}

PID_FILE = "/home/gaia/display.pid"
REFRESH_SECONDS = 60
HTTP_TIMEOUT = 2
COLUMNS = 2
CELL_HEIGHT = 22
GRID_TOP = 1
MAX_CHARS = 10  # larghezza cella (64 px) / 6 px per carattere del font di default


def init_display():
    serial = i2c(port=1, address=0x3C)
    return ssd1306(serial)


def check_app(url):
    try:
        with urllib.request.urlopen(url, timeout=HTTP_TIMEOUT) as resp:
            body = resp.read().decode().strip().strip('"')
            return "OK" if 200 <= resp.status < 300 and body.upper() == "OK" else "FAILURE"
    except Exception:
        return "FAILURE"


def ram_usage():
    """Ritorna (usata_MB, totale_MB, percentuale) leggendo /proc/meminfo."""
    info = {}
    with open("/proc/meminfo") as f:
        for line in f:
            key, value = line.split(":", 1)
            info[key] = int(value.split()[0])  # kB
    total = info["MemTotal"]
    used = total - info["MemAvailable"]
    return used // 1024, total // 1024, used * 100 // total


def draw_screen(device, states):
    used, total, percent = ram_usage()
    width, height = device.width, device.height
    cell_width = width // COLUMNS
    rows_available = (height - 20 - GRID_TOP) // CELL_HEIGHT

    with canvas(device) as draw:
        for i, (name, state) in enumerate(states.items()):
            row, col = divmod(i, COLUMNS)
            if row >= rows_available:
                break
            x, y = col * cell_width, GRID_TOP + row * CELL_HEIGHT
            draw.rectangle((x, y, x + cell_width - 1, y + CELL_HEIGHT - 1), outline="white")
            draw.text((x + 3, y + 1), name[:MAX_CHARS], fill="white")
            draw.text((x + 3, y + 11), state, fill="white")

        # in basso: etichetta RAM e barra di riempimento
        draw.text((0, height - 20), f"RAM {used}/{total}MB {percent}%", fill="white")
        draw.rectangle((0, height - 8, width - 1, height - 2), outline="white")
        draw.rectangle((0, height - 8, (width - 1) * percent // 100, height - 2), fill="white")


def main():
    # SIGTERM (systemctl stop, o kill da Agora) deve passare dal cleanup per spegnere il display
    signal.signal(signal.SIGTERM, lambda *_: (_ for _ in ()).throw(SystemExit(0)))

    device = init_display()
    with open(PID_FILE, "w") as f:
        f.write(str(os.getpid()))
    try:
        while True:
            states = {name: check_app(url) for name, url in APPS.items()}
            draw_screen(device, states)
            time.sleep(REFRESH_SECONDS)
    except (KeyboardInterrupt, SystemExit):
        pass
    finally:
        device.cleanup()
        try:
            os.remove(PID_FILE)
        except FileNotFoundError:
            pass


if __name__ == "__main__":
    main()
