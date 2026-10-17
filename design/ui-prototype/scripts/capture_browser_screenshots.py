#!/usr/bin/env python3
"""
Capture genuine browser screenshots of all screens using Playwright's Chromium binary.
Generates 1280x720 PNG screenshots directly into the proof directory.
"""

import os
import subprocess
from PIL import Image

CHROME_BIN = "/home/Yuhang/.cache/ms-playwright/chromium-1217/chrome-linux64/chrome"
HTML_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "index.html"))
PROOF_DIR = "/home/Yuhang/potato-adventure/firstmate/data/potato-ui-prototype-b1/proof"
os.makedirs(PROOF_DIR, exist_ok=True)

TARGETS = [
    ("01_title_menu.png", ""),
    ("02_character_select.png", "?screen=charselect"),
    ("03_overworld_village.png", "?screen=overworld&biome=village"),
    ("04_overworld_forest.png", "?screen=overworld&biome=forest"),
    ("05_overworld_dungeon.png", "?screen=overworld&biome=dungeon"),
    ("06_battle_screen.png", "?screen=battle"),
    ("07_inventory_modal.png", "?screen=overworld&modal=modal-inventory"),
    ("08_npc_dialogue.png", "?screen=overworld&dialogue=thomas"),
    ("09_skill_tree.png", "?screen=overworld&modal=modal-skilltree"),
    ("10_save_load.png", "?modal=modal-saveload"),
    ("11_settings_modal.png", "?modal=modal-settings"),
    ("12_victory_screen.png", "?screen=victory"),
    ("13_game_over.png", "?screen=gameover"),
]

def main():
    print(f"=== Capturing Genuine Chromium Screenshots using {CHROME_BIN} ===")
    for filename, query in TARGETS:
        url = f"file://{HTML_PATH}{query}"
        out_path = os.path.join(PROOF_DIR, filename)
        cmd = [
            CHROME_BIN,
            "--headless=new",
            "--disable-gpu",
            "--no-sandbox",
            "--window-size=1280,720",
            f"--screenshot={out_path}",
            "--virtual-time-budget=2000",
            url
        ]
        print(f"Capturing {filename} from {query or '/'}")
        res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        if not os.path.exists(out_path):
            print(f"ERROR: Failed to capture {out_path}!\nStderr: {res.stderr}")
            exit(1)
        
        with Image.open(out_path) as im:
            size = im.size
            mode = im.mode
        file_size = os.path.getsize(out_path)
        print(f"  -> Captured: {filename} ({size[0]}x{size[1]} {mode}, {file_size} bytes)")

    print(f"=== Successfully captured all {len(TARGETS)} screenshots to {PROOF_DIR} ===")

if __name__ == "__main__":
    main()
