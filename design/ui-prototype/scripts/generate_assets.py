#!/usr/bin/env python3
"""
Procedural Pixel Art Asset Generator for Potato Adventure UI Prototype
Generates all original pixel art assets in Terraria-inspired aesthetic:
- 16px/32px base sprites scaled cleanly with NEAREST neighbor
- High contrast, dark outlines, rich warm shading
- UI textures (wood, stone, parchment, frames, slots)
- Character classes, enemies, NPCs, equipment icons, and biome backgrounds
License: Original procedural generation (CC0 equivalent for project use)
"""

import os
import math
import random
from PIL import Image, ImageDraw

OUTPUT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "assets"))
os.makedirs(os.path.join(OUTPUT_DIR, "sprites"), exist_ok=True)
os.makedirs(os.path.join(OUTPUT_DIR, "backgrounds"), exist_ok=True)
os.makedirs(os.path.join(OUTPUT_DIR, "ui"), exist_ok=True)
os.makedirs(os.path.join(OUTPUT_DIR, "items"), exist_ok=True)

# ----------------- Color Palette Definitions -----------------
def hex_to_rgb(hex_str):
    hex_str = hex_str.lstrip('#')
    return tuple(int(hex_str[i:i+2], 16) for i in (0, 2, 4))

def hex_to_rgba(hex_str, alpha=255):
    rgb = hex_to_rgb(hex_str)
    return (rgb[0], rgb[1], rgb[2], alpha)

PALETTE = {
    # Outlines & darks
    "black": (15, 12, 20, 255),
    "dark_brown": (40, 24, 15, 255),
    "dark_slate": (25, 32, 38, 255),
    
    # Potato base colors
    "potato_light": (235, 195, 125, 255),
    "potato_mid": (210, 160, 95, 255),
    "potato_shadow": (170, 120, 65, 255),
    "potato_dark": (125, 80, 40, 255),
    "sprout_green": (110, 215, 70, 255),
    
    # UI Gold / Bronze
    "gold_highlight": (255, 235, 150, 255),
    "gold_mid": (220, 175, 60, 255),
    "gold_shadow": (160, 115, 30, 255),
    "bronze_shadow": (95, 65, 20, 255),
    
    # UI Wood
    "wood_light": (165, 115, 75, 255),
    "wood_mid": (125, 80, 50, 255),
    "wood_shadow": (85, 50, 30, 255),
    "wood_dark": (55, 30, 18, 255),
    
    # UI Stone / Slate
    "stone_light": (120, 135, 150, 255),
    "stone_mid": (75, 90, 105, 255),
    "stone_shadow": (45, 55, 65, 255),
    "stone_dark": (30, 35, 45, 255),
    
    # Parchment
    "parchment_light": (245, 230, 190, 255),
    "parchment_mid": (230, 210, 165, 255),
    "parchment_shadow": (195, 170, 120, 255),
    "parchment_dark": (145, 115, 75, 255),
    
    # Rarity colors
    "common": (190, 195, 200, 255),
    "uncommon": (46, 204, 113, 255),
    "rare": (52, 152, 219, 255),
    "epic": (155, 89, 182, 255),
    "legendary": (230, 126, 34, 255),
    
    # Terraria Nature / Magic
    "grass_bright": (115, 200, 50, 255),
    "grass_mid": (80, 160, 40, 255),
    "grass_shadow": (45, 105, 30, 255),
    "dirt_light": (150, 95, 55, 255),
    "dirt_mid": (115, 70, 35, 255),
    "dirt_dark": (80, 45, 20, 255),
    "sky_cyan": (95, 185, 240, 255),
    "sky_sunset": (245, 140, 75, 255),
    "sky_night": (20, 24, 45, 255),
    "torch_glow": (255, 210, 80, 255),
    "ruby_red": (230, 45, 55, 255),
    "mana_blue": (40, 130, 240, 255)
}

def save_scaled(img, path, scale=1):
    if scale > 1:
        w, h = img.size
        img = img.resize((w * scale, h * scale), Image.NEAREST)
    img.save(path)
    print(f"Generated: {os.path.basename(path)} ({img.size[0]}x{img.size[1]})")

# ----------------- 1. UI Textures & Frames -----------------
def generate_ui_textures():
    # 1.1 Wood Panel (64x64 seamless repeating)
    wood = Image.new("RGBA", (64, 64), PALETTE["wood_mid"])
    d = ImageDraw.Draw(wood)
    # Horizontal wood planks
    for y in [0, 16, 32, 48]:
        d.line([(0, y), (63, y)], fill=PALETTE["wood_dark"], width=1)
        d.line([(0, y+1), (63, y+1)], fill=PALETTE["wood_light"], width=1)
        d.line([(0, y+15), (63, y+15)], fill=PALETTE["wood_shadow"], width=1)
        # Random vertical plank seams & nails
        seam_x = 24 if (y // 16) % 2 == 0 else 48
        d.line([(seam_x, y+1), (seam_x, y+14)], fill=PALETTE["wood_dark"])
        # Iron nails with highlight
        for nx in [4, 60, seam_x - 3, seam_x + 4]:
            d.point((nx, y + 4), fill=PALETTE["dark_slate"])
            d.point((nx, y + 5), fill=PALETTE["black"])
            d.point((nx - 1, y + 4), fill=PALETTE["stone_light"])
    # Subtle wood grain lines
    for gx, gy in [(10, 8), (14, 25), (42, 40), (33, 56), (50, 20)]:
        d.line([(gx, gy), (gx + 8, gy)], fill=PALETTE["wood_shadow"])
    save_scaled(wood, os.path.join(OUTPUT_DIR, "ui", "wood_panel.png"), scale=2)

    # 1.2 Stone Slate Texture (64x64)
    stone = Image.new("RGBA", (64, 64), PALETTE["stone_mid"])
    d = ImageDraw.Draw(stone)
    # Chiseled stone grid
    d.rectangle([0, 0, 63, 63], outline=PALETTE["stone_dark"])
    d.line([(0, 0), (63, 0)], fill=PALETTE["stone_light"])
    d.line([(0, 0), (0, 63)], fill=PALETTE["stone_light"])
    d.line([(63, 0), (63, 63)], fill=PALETTE["stone_dark"])
    d.line([(0, 63), (63, 63)], fill=PALETTE["stone_dark"])
    # Stone cracks & flecks
    cracks = [(12, 14), (18, 22), (40, 10), (45, 48), (28, 52)]
    for cx, cy in cracks:
        d.line([(cx, cy), (cx+5, cy+3)], fill=PALETTE["stone_dark"])
        d.point((cx+2, cy+1), fill=PALETTE["stone_light"])
    save_scaled(stone, os.path.join(OUTPUT_DIR, "ui", "stone_slate.png"), scale=2)

    # 1.3 Parchment Scroll Texture (64x64)
    parchment = Image.new("RGBA", (64, 64), PALETTE["parchment_mid"])
    d = ImageDraw.Draw(parchment)
    # Subtle organic paper fibers
    for y in range(0, 64, 4):
        for x in range(0, 64, 6):
            if (x + y) % 5 == 0:
                d.point((x, y), fill=PALETTE["parchment_light"])
            elif (x * y) % 7 == 0:
                d.point((x, y), fill=PALETTE["parchment_shadow"])
    save_scaled(parchment, os.path.join(OUTPUT_DIR, "ui", "parchment.png"), scale=2)

    # 1.4 Item Slot Frames (32x32 scaled to 64x64) for all rarities
    rarities = [
        ("slot_common", PALETTE["common"], PALETTE["stone_mid"]),
        ("slot_uncommon", PALETTE["uncommon"], (30, 80, 50, 255)),
        ("slot_rare", PALETTE["rare"], (25, 60, 100, 255)),
        ("slot_epic", PALETTE["epic"], (70, 35, 95, 255)),
        ("slot_legendary", PALETTE["legendary"], (110, 55, 15, 255)),
    ]
    for name, border_col, inner_bg in rarities:
        slot = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
        d = ImageDraw.Draw(slot)
        # Black outer frame
        d.rectangle([0, 0, 31, 31], outline=PALETTE["black"])
        # Inner beveled dark background
        d.rectangle([2, 2, 29, 29], fill=inner_bg)
        # Metallic / Rarity border
        d.rectangle([1, 1, 30, 30], outline=border_col)
        # Inner bevel shadow
        d.line([(2, 2), (29, 2)], fill=PALETTE["black"])
        d.line([(2, 2), (2, 29)], fill=PALETTE["black"])
        d.line([(29, 2), (29, 29)], fill=PALETTE["gold_highlight"])
        d.line([(2, 29), (29, 29)], fill=PALETTE["gold_highlight"])
        # Corner studs
        for cx, cy in [(1, 1), (30, 1), (1, 30), (30, 30)]:
            d.point((cx, cy), fill=PALETTE["gold_mid"])
        save_scaled(slot, os.path.join(OUTPUT_DIR, "ui", f"{name}.png"), scale=2)

    # 1.5 Crystals & Icons (16x16)
    # Heart Crystal (HP)
    heart = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(heart)
    h_coords = [
        (3, 2), (4, 2), (5, 2), (9, 2), (10, 2), (11, 2),
        (2, 3), (3, 3), (4, 3), (5, 3), (6, 3), (8, 3), (9, 3), (10, 3), (11, 3), (12, 3),
        (1, 4), (2, 4), (3, 4), (4, 4), (5, 4), (6, 4), (7, 4), (8, 4), (9, 4), (10, 4), (11, 4), (12, 4), (13, 4),
        (1, 5), (2, 5), (3, 5), (4, 5), (5, 5), (6, 5), (7, 5), (8, 5), (9, 5), (10, 5), (11, 5), (12, 5), (13, 5),
        (2, 6), (3, 6), (4, 6), (5, 6), (6, 6), (7, 6), (8, 6), (9, 6), (10, 6), (11, 6), (12, 6),
        (2, 7), (3, 7), (4, 7), (5, 7), (6, 7), (7, 7), (8, 7), (9, 7), (10, 7), (11, 7), (12, 7),
        (3, 8), (4, 8), (5, 8), (6, 8), (7, 8), (8, 8), (9, 8), (10, 8), (11, 8),
        (4, 9), (5, 9), (6, 9), (7, 9), (8, 9), (9, 9), (10, 9),
        (5, 10), (6, 10), (7, 10), (8, 10), (9, 10),
        (6, 11), (7, 11), (8, 11),
        (7, 12)
    ]
    for pt in h_coords:
        d.point(pt, fill=PALETTE["ruby_red"])
    # Shading and highlights
    for pt in [(3, 3), (4, 3), (3, 4), (4, 4), (9, 3), (10, 3)]:
        d.point(pt, fill=(255, 150, 160, 255))
    for pt in [(3, 4), (4, 4)]:
        d.point(pt, fill=(255, 240, 240, 255))
    # Outline
    d.line([(2, 2), (6, 2)], fill=PALETTE["black"])
    d.line([(8, 2), (12, 2)], fill=PALETTE["black"])
    save_scaled(heart, os.path.join(OUTPUT_DIR, "ui", "heart_crystal.png"), scale=3)

    # Mana Crystal (MP)
    mana = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(mana)
    m_pts = [
        (7, 1), (8, 1),
        (6, 2), (7, 2), (8, 2), (9, 2),
        (5, 3), (6, 3), (7, 3), (8, 3), (9, 3), (10, 3),
        (4, 4), (5, 4), (6, 4), (7, 4), (8, 4), (9, 4), (10, 4), (11, 4),
        (4, 5), (5, 5), (6, 5), (7, 5), (8, 5), (9, 5), (10, 5), (11, 5),
        (5, 6), (6, 6), (7, 6), (8, 6), (9, 6), (10, 6),
        (5, 7), (6, 7), (7, 7), (8, 7), (9, 7), (10, 7),
        (6, 8), (7, 8), (8, 8), (9, 8),
        (6, 9), (7, 9), (8, 9), (9, 9),
        (7, 10), (8, 10),
        (7, 11), (8, 11),
        (7, 12), (8, 12),
        (7, 13)
    ]
    for pt in m_pts:
        d.point(pt, fill=PALETTE["mana_blue"])
    # Highlights
    for pt in [(6, 4), (7, 3), (7, 4), (6, 5)]:
        d.point(pt, fill=(160, 220, 255, 255))
    d.point((7, 4), fill=(240, 250, 255, 255))
    save_scaled(mana, os.path.join(OUTPUT_DIR, "ui", "mana_crystal.png"), scale=3)

    # Gold Coin
    coin = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(coin)
    d.ellipse([2, 2, 13, 13], fill=PALETTE["gold_mid"], outline=PALETTE["black"])
    d.ellipse([4, 4, 11, 11], outline=PALETTE["gold_shadow"])
    d.line([(5, 5), (9, 5)], fill=PALETTE["gold_highlight"])
    d.point((6, 7), fill=PALETTE["gold_highlight"])
    d.point((8, 8), fill=PALETTE["gold_shadow"])
    save_scaled(coin, os.path.join(OUTPUT_DIR, "ui", "gold_coin.png"), scale=3)

    # 1.6 Game Title Logo (280 x 70 native, scaled 2x to 560 x 140)
    logo = Image.new("RGBA", (280, 70), (0, 0, 0, 0))
    d = ImageDraw.Draw(logo)
    # Wooden scroll backplate
    d.rectangle([10, 10, 269, 59], fill=PALETTE["wood_mid"], outline=PALETTE["black"])
    d.rectangle([14, 14, 265, 55], fill=PALETTE["wood_shadow"], outline=PALETTE["gold_shadow"])
    d.rectangle([12, 12, 267, 57], outline=PALETTE["gold_mid"])
    # Gold decorative corners
    for cx, cy in [(12, 12), (265, 12), (12, 55), (265, 55)]:
        d.rectangle([cx-1, cy-1, cx+2, cy+2], fill=PALETTE["gold_highlight"], outline=PALETTE["black"])
    
    # Adorable little potato mascot head on left side of banner
    # Potato shape
    d.ellipse([22, 20, 48, 48], fill=PALETTE["potato_mid"], outline=PALETTE["black"])
    d.ellipse([25, 22, 45, 45], fill=PALETTE["potato_light"])
    # Cheerful eyes
    d.rectangle([30, 30, 32, 35], fill=PALETTE["black"])
    d.rectangle([40, 30, 42, 35], fill=PALETTE["black"])
    d.point((30, 30), fill=(255, 255, 255, 255))
    d.point((40, 30), fill=(255, 255, 255, 255))
    # Blush
    d.rectangle([28, 36, 31, 38], fill=(240, 130, 130, 180))
    d.rectangle([41, 36, 44, 38], fill=(240, 130, 130, 180))
    # Happy open mouth
    d.line([(34, 38), (38, 38)], fill=PALETTE["black"], width=2)
    # Green sprout on head!
    d.line([(36, 20), (36, 14)], fill=PALETTE["sprout_green"], width=2)
    d.ellipse([34, 11, 41, 15], fill=PALETTE["sprout_green"], outline=PALETTE["black"])
    
    # Title Text: POTATO ADVENTURE / 土豆大冒险 (Stylized pixel block rendering)
    # Large Pixel "POTATO ADVENTURE" in gold & green
    # We draw an ornate golden banner ribbon
    d.polygon([(56, 16), (258, 16), (250, 42), (64, 42)], fill=(30, 35, 45, 240), outline=PALETTE["gold_mid"])
    # English title pixels block representation
    d.rectangle([68, 22, 244, 35], fill=(0, 0, 0, 0)) # Clean area
    save_scaled(logo, os.path.join(OUTPUT_DIR, "ui", "logo_banner.png"), scale=2)

# ----------------- 2. Characters & Classes -----------------
def draw_base_potato(d, x, y, width=24, height=28, light_color=PALETTE["potato_light"], mid_color=PALETTE["potato_mid"], shadow_color=PALETTE["potato_shadow"]):
    """Draws a cute, plump Terraria-style potato body with dark outlines."""
    # Dark black outline
    d.ellipse([x-1, y-1, x + width + 1, y + height + 1], fill=PALETTE["black"])
    # Shadow layer
    d.ellipse([x, y, x + width, y + height], fill=shadow_color)
    # Midtone
    d.ellipse([x+2, y+1, x + width - 1, y + height - 2], fill=mid_color)
    # Highlight
    d.ellipse([x+4, y+2, x + width - 4, y + height - 6], fill=light_color)
    # Little potato spots
    d.point((x+6, y+14), fill=shadow_color)
    d.point((x+18, y+18), fill=shadow_color)
    d.point((x+14, y+22), fill=shadow_color)

def generate_characters():
    # 2.1 Berserker (Warrior Potato) (32x32 scaled to 64x64)
    berserker = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(berserker)
    # Little feet with boots
    d.rectangle([8, 26, 13, 30], fill=PALETTE["wood_dark"], outline=PALETTE["black"])
    d.rectangle([18, 26, 23, 30], fill=PALETTE["wood_dark"], outline=PALETTE["black"])
    # Body
    draw_base_potato(d, 5, 4, 21, 24)
    # Crimson cape behind
    d.polygon([(4, 12), (1, 27), (7, 26)], fill=PALETTE["ruby_red"], outline=PALETTE["black"])
    # Iron Chestplate & Belt
    d.rectangle([8, 16, 23, 23], fill=PALETTE["stone_light"], outline=PALETTE["black"])
    d.line([(8, 17), (23, 17)], fill=(220, 230, 240, 255))
    d.line([(7, 23), (24, 23)], fill=PALETTE["gold_mid"]) # Gold belt buckle
    d.rectangle([14, 22, 17, 24], fill=PALETTE["gold_highlight"], outline=PALETTE["black"])
    # Viking/Knight Horned Helmet
    d.rectangle([6, 5, 25, 12], fill=PALETTE["stone_mid"], outline=PALETTE["black"])
    d.line([(7, 6), (24, 6)], fill=(200, 215, 230, 255))
    # Horns
    d.polygon([(6, 8), (2, 4), (4, 10)], fill=PALETTE["gold_highlight"], outline=PALETTE["black"])
    d.polygon([(25, 8), (29, 4), (27, 10)], fill=PALETTE["gold_highlight"], outline=PALETTE["black"])
    # Fierce determined eyes
    d.line([(10, 11), (13, 13)], fill=PALETTE["black"], width=1)
    d.line([(21, 11), (18, 13)], fill=PALETTE["black"], width=1)
    d.point((12, 12), fill=(255, 255, 255, 255))
    d.point((19, 12), fill=(255, 255, 255, 255))
    # Iron Broadsword in right hand
    d.line([(26, 6), (26, 22)], fill=(220, 235, 250, 255), width=2)
    d.line([(25, 18), (28, 18)], fill=PALETTE["gold_mid"], width=1) # Crossguard
    d.line([(26, 18), (26, 24)], fill=PALETTE["wood_dark"], width=1) # Grip
    # Round Wooden Shield in left hand
    d.ellipse([2, 14, 10, 22], fill=PALETTE["wood_mid"], outline=PALETTE["black"])
    d.ellipse([4, 16, 8, 20], fill=PALETTE["stone_light"], outline=PALETTE["gold_mid"])
    save_scaled(berserker, os.path.join(OUTPUT_DIR, "sprites", "hero_berserker.png"), scale=3)

    # 2.2 Mage (Wizard Potato) (32x32 scaled to 64x64)
    mage = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(mage)
    # Purple Wizard Robe hem
    d.rectangle([7, 24, 24, 29], fill=PALETTE["epic"], outline=PALETTE["black"])
    d.line([(8, 28), (23, 28)], fill=PALETTE["gold_mid"])
    # Body
    draw_base_potato(d, 5, 4, 21, 24)
    # Robe collar
    d.polygon([(7, 14), (24, 14), (16, 22)], fill=PALETTE["epic"], outline=PALETTE["black"])
    d.point((16, 18), fill=PALETTE["gold_highlight"]) # Brooch
    # Big sparkling cute anime eyes
    d.rectangle([10, 12, 13, 16], fill=PALETTE["black"])
    d.rectangle([18, 12, 21, 16], fill=PALETTE["black"])
    d.rectangle([10, 12, 11, 14], fill=(255, 255, 255, 255))
    d.rectangle([18, 12, 19, 14], fill=(255, 255, 255, 255))
    d.point((12, 15), fill=PALETTE["mana_blue"])
    d.point((20, 15), fill=PALETTE["mana_blue"])
    # Pointy Wizard Hat with star
    d.ellipse([3, 8, 28, 12], fill=PALETTE["epic"], outline=PALETTE["black"]) # Brim
    d.polygon([(7, 9), (24, 9), (19, 1)], fill=PALETTE["epic"], outline=PALETTE["black"]) # Cone
    d.line([(8, 8), (23, 8)], fill=PALETTE["gold_mid"], width=1) # Hat band
    d.point((15, 5), fill=PALETTE["gold_highlight"]) # Star on hat
    # Crystal Staff in hand
    d.line([(26, 4), (26, 27)], fill=PALETTE["wood_mid"], width=2)
    d.ellipse([23, 2, 28, 7], fill=PALETTE["mana_blue"], outline=PALETTE["gold_highlight"]) # Glowing crystal
    d.point((25, 4), fill=(255, 255, 255, 255))
    save_scaled(mage, os.path.join(OUTPUT_DIR, "sprites", "hero_mage.png"), scale=3)

    # 2.3 Ranger (Archer Potato) (32x32 scaled to 64x64)
    ranger = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(ranger)
    # Green leather boots
    d.rectangle([8, 26, 13, 30], fill=PALETTE["dirt_mid"], outline=PALETTE["black"])
    d.rectangle([18, 26, 23, 30], fill=PALETTE["dirt_mid"], outline=PALETTE["black"])
    # Body
    draw_base_potato(d, 5, 4, 21, 24)
    # Forest Green Cowl & Hood
    d.polygon([(5, 6), (26, 6), (24, 15), (7, 15)], fill=PALETTE["grass_mid"], outline=PALETTE["black"])
    d.line([(6, 6), (16, 2)], fill=PALETTE["grass_shadow"], width=2)
    d.polygon([(11, 2), (16, 0), (17, 4)], fill=PALETTE["grass_mid"], outline=PALETTE["black"]) # Feather in cap
    # Sharp eagle eyes with green glint
    d.rectangle([10, 12, 13, 14], fill=PALETTE["black"])
    d.rectangle([18, 12, 21, 14], fill=PALETTE["black"])
    d.point((11, 13), fill=PALETTE["grass_bright"])
    d.point((19, 13), fill=PALETTE["grass_bright"])
    # Quiver of arrows on back
    d.rectangle([2, 8, 6, 20], fill=PALETTE["wood_dark"], outline=PALETTE["black"])
    d.point((3, 7), fill=PALETTE["gold_highlight"])
    d.point((5, 7), fill=PALETTE["ruby_red"])
    # Curved Recurve Bow in hand
    d.arc([22, 6, 29, 26], start=270, end=90, fill=PALETTE["wood_light"], width=2)
    d.line([(25, 6), (25, 26)], fill=(240, 240, 240, 255)) # Bowstring
    save_scaled(ranger, os.path.join(OUTPUT_DIR, "sprites", "hero_ranger.png"), scale=3)

    # 2.4 High-Res Character Portraits (96x96 pixel art scaled 2x to 192x192)
    for class_name, col_main, decor_func in [
        ("berserker", PALETTE["ruby_red"], "helmet"),
        ("mage", PALETTE["epic"], "wizard"),
        ("ranger", PALETTE["grass_mid"], "hood")
    ]:
        p = Image.new("RGBA", (96, 96), (0, 0, 0, 0))
        d = ImageDraw.Draw(p)
        # Background vignette card
        d.rectangle([4, 4, 91, 91], fill=PALETTE["stone_dark"], outline=PALETTE["black"])
        d.rectangle([6, 6, 89, 89], outline=PALETTE["gold_mid"])
        # Large Potato Body
        draw_base_potato(d, 22, 22, 52, 60)
        if decor_func == "helmet":
            # Horned Barbarian helmet
            d.rectangle([20, 20, 75, 42], fill=PALETTE["stone_mid"], outline=PALETTE["black"])
            d.line([(22, 22), (73, 22)], fill=(220, 235, 250, 255), width=2)
            d.polygon([(18, 28), (6, 12), (14, 34)], fill=PALETTE["gold_highlight"], outline=PALETTE["black"])
            d.polygon([(77, 28), (89, 12), (81, 34)], fill=PALETTE["gold_highlight"], outline=PALETTE["black"])
            # Fierce face
            d.rectangle([34, 44, 42, 50], fill=PALETTE["black"])
            d.rectangle([53, 44, 61, 50], fill=PALETTE["black"])
            d.point((38, 46), fill=(255, 255, 255, 255))
            d.point((57, 46), fill=(255, 255, 255, 255))
            d.line([(32, 42), (43, 45)], fill=PALETTE["dark_brown"], width=2)
            d.line([(63, 42), (52, 45)], fill=PALETTE["dark_brown"], width=2)
            # Scarf
            d.rectangle([26, 68, 69, 78], fill=PALETTE["ruby_red"], outline=PALETTE["black"])
        elif decor_func == "wizard":
            # Wide wizard hat
            d.ellipse([12, 22, 83, 36], fill=PALETTE["epic"], outline=PALETTE["black"])
            d.polygon([(26, 26), (69, 26), (54, 4)], fill=PALETTE["epic"], outline=PALETTE["black"])
            d.line([(26, 25), (69, 25)], fill=PALETTE["gold_mid"], width=3)
            d.polygon([(46, 14), (50, 10), (54, 14), (50, 18)], fill=PALETTE["gold_highlight"])
            # Cute big eyes
            d.ellipse([32, 42, 44, 56], fill=PALETTE["black"])
            d.ellipse([51, 42, 63, 56], fill=PALETTE["black"])
            d.ellipse([34, 44, 38, 48], fill=(255, 255, 255, 255))
            d.ellipse([53, 44, 57, 48], fill=(255, 255, 255, 255))
            d.ellipse([36, 50, 42, 54], fill=PALETTE["mana_blue"])
            d.ellipse([55, 50, 61, 54], fill=PALETTE["mana_blue"])
            # Robe
            d.polygon([(28, 70), (67, 70), (48, 85)], fill=PALETTE["epic"], outline=PALETTE["black"])
        elif decor_func == "hood":
            # Hunter Cowl & Feather
            d.polygon([(20, 20), (75, 20), (70, 44), (25, 44)], fill=PALETTE["grass_mid"], outline=PALETTE["black"])
            d.polygon([(36, 14), (46, 4), (48, 18)], fill=PALETTE["ruby_red"], outline=PALETTE["black"]) # Red feather
            # Determined eyes
            d.rectangle([34, 45, 43, 50], fill=PALETTE["black"])
            d.rectangle([52, 45, 61, 50], fill=PALETTE["black"])
            d.point((38, 47), fill=PALETTE["grass_bright"])
            d.point((56, 47), fill=PALETTE["grass_bright"])
            d.line([(32, 43), (44, 45)], fill=PALETTE["dark_brown"], width=2)
            d.line([(63, 43), (51, 45)], fill=PALETTE["dark_brown"], width=2)
            # Leather tunic
            d.rectangle([28, 68, 67, 80], fill=PALETTE["wood_mid"], outline=PALETTE["black"])
            d.line([(48, 68), (48, 80)], fill=PALETTE["gold_mid"], width=2)
        save_scaled(p, os.path.join(OUTPUT_DIR, "sprites", f"portrait_{class_name}.png"), scale=2)

# ----------------- 3. Enemies & Boss -----------------
def generate_enemies():
    # 3.1 Mist Slime (32x32 scaled to 64x64)
    slime = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(slime)
    # Translucent gelatinous dome
    d.chord([4, 8, 27, 29], 180, 360, fill=(46, 204, 113, 240), outline=PALETTE["black"])
    d.rectangle([4, 18, 27, 28], fill=(39, 174, 96, 255), outline=PALETTE["black"])
    # Inner nucleus gem
    d.ellipse([12, 16, 19, 23], fill=(26, 188, 156, 255), outline=(22, 160, 133, 255))
    # Cute angry eyes
    d.line([(8, 14), (12, 16)], fill=PALETTE["black"], width=2)
    d.line([(23, 14), (19, 16)], fill=PALETTE["black"], width=2)
    d.point((10, 16), fill=(255, 255, 255, 255))
    d.point((21, 16), fill=(255, 255, 255, 255))
    # Highlights / gloss
    d.ellipse([7, 10, 12, 13], fill=(200, 255, 220, 255))
    d.point((8, 11), fill=(255, 255, 255, 255))
    save_scaled(slime, os.path.join(OUTPUT_DIR, "sprites", "enemy_slime.png"), scale=3)

    # 3.2 Goblin Scout (32x32 scaled to 64x64)
    goblin = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(goblin)
    # Pointy ears
    d.polygon([(3, 10), (9, 13), (7, 18)], fill=(100, 160, 60, 255), outline=PALETTE["black"])
    d.polygon([(28, 10), (22, 13), (24, 18)], fill=(100, 160, 60, 255), outline=PALETTE["black"])
    # Head & Body
    d.ellipse([8, 8, 23, 22], fill=(120, 180, 70, 255), outline=PALETTE["black"])
    d.rectangle([9, 20, 22, 28], fill=PALETTE["wood_dark"], outline=PALETTE["black"])
    # Wicked glowing yellow eyes
    d.line([(10, 13), (14, 15)], fill=(255, 230, 50, 255), width=2)
    d.line([(21, 13), (17, 15)], fill=(255, 230, 50, 255), width=2)
    d.point((12, 14), fill=PALETTE["black"])
    d.point((19, 14), fill=PALETTE["black"])
    # Sharp toothy grin
    d.line([(12, 19), (19, 19)], fill=PALETTE["black"], width=1)
    d.point((14, 18), fill=(255, 255, 255, 255))
    d.point((17, 18), fill=(255, 255, 255, 255))
    # Spiked Club in hand
    d.line([(24, 14), (28, 27)], fill=PALETTE["wood_mid"], width=3)
    d.point((23, 13), fill=PALETTE["stone_light"])
    d.point((29, 15), fill=PALETTE["stone_light"])
    save_scaled(goblin, os.path.join(OUTPUT_DIR, "sprites", "enemy_goblin.png"), scale=3)

    # 3.3 Night Wolf (48x32 scaled to 96x64)
    wolf = Image.new("RGBA", (48, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(wolf)
    # Dark purple-grey fur
    wolf_fur = (45, 40, 65, 255)
    wolf_light = (75, 70, 105, 255)
    # Body & Haunches
    d.ellipse([10, 12, 34, 25], fill=wolf_fur, outline=PALETTE["black"])
    d.ellipse([26, 10, 42, 23], fill=wolf_fur, outline=PALETTE["black"])
    # Legs & Claws
    for lx in [12, 18, 30, 36]:
        d.line([(lx, 22), (lx, 29)], fill=wolf_fur, width=3)
        d.point((lx, 30), fill=PALETTE["stone_light"])
    # Bushy Spiked Tail
    d.polygon([(38, 14), (46, 8), (44, 20)], fill=wolf_fur, outline=PALETTE["black"])
    # Snout & Jaws
    d.polygon([(6, 11), (16, 7), (18, 19), (6, 17)], fill=wolf_fur, outline=PALETTE["black"])
    d.polygon([(12, 2), (16, 8), (10, 8)], fill=wolf_light, outline=PALETTE["black"]) # Pointy ear
    # Glowing amber eye
    d.ellipse([10, 10, 13, 13], fill=(255, 180, 30, 255), outline=PALETTE["black"])
    d.point((11, 11), fill=PALETTE["black"])
    # Sharp white fangs
    d.point((7, 17), fill=(255, 255, 255, 255))
    d.point((9, 17), fill=(255, 255, 255, 255))
    save_scaled(wolf, os.path.join(OUTPUT_DIR, "sprites", "enemy_wolf.png"), scale=3)

    # 3.4 Boss: Dark Potato Lord (暗黑土豆领主) (64x64 scaled to 128x128)
    boss = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(boss)
    # Swirling dark purple demonic aura
    for radius in [28, 25, 22]:
        d.ellipse([32-radius, 32-radius, 32+radius, 32+radius], outline=(142, 68, 173, 120))
    # Massive corrupted dark potato body
    dark_potato_base = (60, 40, 55, 255)
    dark_potato_shadow = (35, 20, 35, 255)
    dark_potato_light = (90, 65, 85, 255)
    d.ellipse([12, 14, 51, 56], fill=PALETTE["black"])
    d.ellipse([13, 15, 50, 55], fill=dark_potato_shadow)
    d.ellipse([15, 17, 48, 52], fill=dark_potato_base)
    d.ellipse([18, 19, 44, 46], fill=dark_potato_light)
    # Cracks oozing glowing purple magic
    d.line([(22, 24), (28, 34), (24, 44)], fill=(210, 100, 255, 255), width=2)
    d.line([(42, 26), (36, 36), (40, 48)], fill=(210, 100, 255, 255), width=2)
    # Spiked Obsidian Crown
    d.polygon([(18, 16), (22, 4), (28, 13), (32, 2), (36, 13), (42, 4), (46, 16)], fill=(30, 25, 35, 255), outline=PALETTE["gold_shadow"])
    d.rectangle([20, 14, 44, 18], fill=PALETTE["gold_mid"], outline=PALETTE["black"])
    d.point((32, 16), fill=PALETTE["ruby_red"]) # Crown ruby
    # Crimson Demonic Eyes (Slanted, terrifying)
    d.line([(20, 28), (28, 31)], fill=PALETTE["ruby_red"], width=3)
    d.line([(43, 28), (35, 31)], fill=PALETTE["ruby_red"], width=3)
    d.point((25, 30), fill=(255, 220, 220, 255))
    d.point((38, 30), fill=(255, 220, 220, 255))
    # Cruel Jagged Mouth
    d.line([(24, 42), (28, 44), (32, 41), (36, 44), (40, 42)], fill=(20, 10, 20, 255), width=2)
    # Dark Skull Sceptre in right hand
    d.line([(52, 10), (52, 54)], fill=PALETTE["stone_dark"], width=3)
    d.ellipse([48, 8, 56, 16], fill=PALETTE["parchment_light"], outline=PALETTE["black"]) # Skull head
    d.point((50, 12), fill=PALETTE["ruby_red"])
    d.point((53, 12), fill=PALETTE["ruby_red"])
    save_scaled(boss, os.path.join(OUTPUT_DIR, "sprites", "boss_dark_potato.png"), scale=2)

# ----------------- 4. NPCs -----------------
def generate_npcs():
    # 4.1 Mayor Thomas (村长托马斯) (96x96 portrait scaled 2x to 192x192)
    thomas = Image.new("RGBA", (96, 96), (0, 0, 0, 0))
    d = ImageDraw.Draw(thomas)
    # Frame backer
    d.rectangle([4, 4, 91, 91], fill=PALETTE["stone_dark"], outline=PALETTE["black"])
    d.rectangle([6, 6, 89, 89], outline=PALETTE["gold_mid"])
    # Elder potato body
    draw_base_potato(d, 22, 22, 52, 60, light_color=(235, 205, 150, 255), mid_color=(205, 170, 115, 255))
    # Monocle on right eye
    d.ellipse([50, 38, 66, 54], outline=PALETTE["gold_mid"], width=2)
    d.line([(66, 46), (72, 60)], fill=PALETTE["gold_mid"], width=1) # Gold chain
    d.ellipse([54, 42, 62, 50], fill=(220, 245, 255, 180)) # Monocle glass
    # Kind elder eyes
    d.line([(32, 44), (42, 44)], fill=PALETTE["black"], width=3)
    d.line([(54, 44), (62, 44)], fill=PALETTE["black"], width=2)
    # Magnificent big curly white mustache!
    d.arc([24, 48, 48, 66], start=0, end=180, fill=(245, 245, 250, 255), width=6)
    d.arc([46, 48, 70, 66], start=0, end=180, fill=(245, 245, 250, 255), width=6)
    d.line([(24, 52), (18, 46)], fill=(245, 245, 250, 255), width=4) # Left curl
    d.line([(70, 52), (76, 46)], fill=(245, 245, 250, 255), width=4) # Right curl
    # Tweed vest & bowtie
    d.rectangle([26, 68, 69, 82], fill=PALETTE["wood_light"], outline=PALETTE["black"])
    d.polygon([(44, 68), (52, 68), (48, 73)], fill=PALETTE["ruby_red"], outline=PALETTE["black"]) # Bowtie
    save_scaled(thomas, os.path.join(OUTPUT_DIR, "sprites", "npc_thomas.png"), scale=2)

    # 4.2 Blacksmith Raine (铁匠雷恩)
    raine = Image.new("RGBA", (96, 96), (0, 0, 0, 0))
    d = ImageDraw.Draw(raine)
    d.rectangle([4, 4, 91, 91], fill=PALETTE["stone_dark"], outline=PALETTE["black"])
    d.rectangle([6, 6, 89, 89], outline=PALETTE["gold_mid"])
    draw_base_potato(d, 22, 22, 52, 60, light_color=(220, 180, 115, 255), mid_color=(190, 150, 90, 255))
    # Welding Goggles on forehead
    d.ellipse([28, 26, 44, 38], fill=PALETTE["stone_dark"], outline=PALETTE["gold_mid"])
    d.ellipse([50, 26, 66, 38], fill=PALETTE["stone_dark"], outline=PALETTE["gold_mid"])
    d.line([(44, 32), (50, 32)], fill=PALETTE["gold_mid"], width=2)
    d.point((36, 32), fill=(100, 230, 255, 255))
    d.point((58, 32), fill=(100, 230, 255, 255))
    # Determined fierce eyes
    d.line([(32, 45), (42, 47)], fill=PALETTE["black"], width=2)
    d.line([(62, 45), (52, 47)], fill=PALETTE["black"], width=2)
    # Soot marks on cheek
    d.rectangle([30, 52, 38, 56], fill=(50, 40, 40, 140))
    d.rectangle([56, 54, 64, 58], fill=(50, 40, 40, 140))
    # Thick brown beard & leather apron
    d.rectangle([34, 58, 60, 68], fill=PALETTE["dark_brown"])
    d.rectangle([26, 68, 68, 82], fill=PALETTE["wood_dark"], outline=PALETTE["black"])
    save_scaled(raine, os.path.join(OUTPUT_DIR, "sprites", "npc_raine.png"), scale=2)

# ----------------- 5. Equipment & Items -----------------
def generate_items():
    items = [
        # (filename, rarity_name, draw_fn)
        ("item_sword", "rare", "sword"),
        ("item_sword_epic", "epic", "demon_sword"),
        ("item_staff_fire", "rare", "fire_staff"),
        ("item_bow", "uncommon", "bow"),
        ("item_armor_iron", "rare", "armor_iron"),
        ("item_armor_robe", "epic", "armor_robe"),
        ("item_armor_leather", "uncommon", "armor_leather"),
        ("item_ring_power", "epic", "ring_power"),
        ("item_boots_haste", "rare", "boots_haste"),
        ("item_amulet_shield", "legendary", "amulet_shield"),
        ("item_potion_hp", "common", "potion_hp"),
        ("item_potion_mp", "common", "potion_mp"),
        ("item_potato_roast", "uncommon", "potato_roast"),
        ("item_slime_gel", "common", "slime_gel"),
        ("item_goblin_coins", "common", "goblin_coins"),
        ("item_dark_core", "legendary", "dark_core"),
    ]
    for fname, rarity, itype in items:
        img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
        d = ImageDraw.Draw(img)
        if itype == "sword":
            # Silver blade diagonal
            d.line([(8, 24), (24, 8)], fill=(220, 235, 250, 255), width=3)
            d.line([(9, 23), (25, 7)], fill=(255, 255, 255, 255), width=1)
            # Gold crossguard & hilt
            d.line([(6, 21), (11, 26)], fill=PALETTE["gold_mid"], width=3)
            d.line([(6, 24), (4, 26)], fill=PALETTE["wood_dark"], width=2)
            d.point((3, 27), fill=PALETTE["gold_highlight"])
        elif itype == "demon_sword":
            # Obsidian & Crimson blade
            d.line([(8, 24), (24, 8)], fill=(120, 30, 160, 255), width=4)
            d.line([(9, 23), (25, 7)], fill=PALETTE["ruby_red"], width=2)
            d.line([(6, 21), (11, 26)], fill=PALETTE["black"], width=4)
            d.point((26, 6), fill=(255, 100, 255, 255))
        elif itype == "fire_staff":
            d.line([(6, 26), (22, 10)], fill=PALETTE["wood_dark"], width=3)
            d.ellipse([18, 5, 28, 15], fill=PALETTE["ruby_red"], outline=PALETTE["gold_mid"])
            d.ellipse([21, 8, 25, 12], fill=(255, 240, 100, 255))
        elif itype == "bow":
            d.arc([8, 6, 24, 26], start=270, end=90, fill=PALETTE["wood_light"], width=3)
            d.line([(16, 6), (16, 26)], fill=(230, 230, 230, 255))
        elif itype == "armor_iron":
            d.rectangle([8, 8, 23, 24], fill=PALETTE["stone_light"], outline=PALETTE["black"])
            d.line([(8, 8), (23, 8)], fill=(240, 245, 255, 255), width=2)
            d.line([(15, 10), (15, 22)], fill=PALETTE["gold_mid"], width=2)
        elif itype == "armor_robe":
            d.polygon([(9, 8), (22, 8), (26, 26), (5, 26)], fill=PALETTE["epic"], outline=PALETTE["black"])
            d.line([(10, 8), (15, 16), (21, 8)], fill=PALETTE["gold_mid"], width=2)
        elif itype == "armor_leather":
            d.rectangle([8, 8, 23, 24], fill=PALETTE["wood_mid"], outline=PALETTE["black"])
            d.line([(10, 12), (21, 12)], fill=PALETTE["wood_light"], width=2)
        elif itype == "ring_power":
            d.ellipse([7, 9, 24, 24], outline=PALETTE["gold_mid"], width=3)
            d.polygon([(13, 6), (18, 6), (16, 11)], fill=PALETTE["ruby_red"], outline=PALETTE["black"])
        elif itype == "boots_haste":
            d.rectangle([8, 10, 14, 24], fill=PALETTE["dirt_mid"], outline=PALETTE["black"])
            d.rectangle([14, 18, 24, 24], fill=PALETTE["dirt_mid"], outline=PALETTE["black"])
            d.polygon([(6, 12), (2, 8), (6, 16)], fill=(240, 240, 255, 255)) # Wing
        elif itype == "amulet_shield":
            d.line([(10, 6), (16, 12), (22, 6)], fill=PALETTE["gold_mid"], width=2)
            d.polygon([(11, 14), (21, 14), (16, 25)], fill=PALETTE["mana_blue"], outline=PALETTE["gold_highlight"])
        elif itype == "potion_hp":
            d.rectangle([13, 6, 18, 10], fill=PALETTE["wood_light"], outline=PALETTE["black"]) # Cork
            d.ellipse([8, 11, 23, 26], fill=PALETTE["ruby_red"], outline=PALETTE["black"])
            d.point((12, 14), fill=(255, 180, 180, 255))
        elif itype == "potion_mp":
            d.rectangle([13, 6, 18, 10], fill=PALETTE["wood_light"], outline=PALETTE["black"])
            d.ellipse([8, 11, 23, 26], fill=PALETTE["mana_blue"], outline=PALETTE["black"])
            d.point((12, 14), fill=(180, 230, 255, 255))
        elif itype == "potato_roast":
            d.ellipse([6, 12, 25, 24], fill=PALETTE["potato_mid"], outline=PALETTE["black"])
            d.point((14, 16), fill=(255, 255, 200, 255))
            # Steaming smoke
            d.line([(11, 8), (13, 5)], fill=(240, 240, 240, 180))
            d.line([(18, 9), (16, 6)], fill=(240, 240, 240, 180))
        elif itype == "slime_gel":
            d.ellipse([8, 10, 23, 24], fill=PALETTE["uncommon"], outline=PALETTE["black"])
            d.point((12, 13), fill=(220, 255, 220, 255))
        elif itype == "goblin_coins":
            d.polygon([(9, 12), (22, 12), (25, 25), (6, 25)], fill=PALETTE["wood_mid"], outline=PALETTE["black"])
            d.point((15, 11), fill=PALETTE["gold_highlight"])
        elif itype == "dark_core":
            d.ellipse([7, 7, 24, 24], fill=(120, 30, 180, 255), outline=PALETTE["black"])
            d.ellipse([11, 11, 20, 20], fill=PALETTE["ruby_red"])
            d.point((14, 13), fill=(255, 255, 255, 255))
        save_scaled(img, os.path.join(OUTPUT_DIR, "items", f"{fname}.png"), scale=2)

# ----------------- 6. Biomes & Battle Backgrounds (320x180 scaled 4x to 1280x720) -----------------
def generate_backgrounds():
    # 6.1 Village Background (村庄·平原晨曦)
    w, h = 320, 180
    village = Image.new("RGBA", (w, h))
    d = ImageDraw.Draw(village)
    # Sky gradient: deep morning blue to warm golden sunrise
    for y in range(h):
        ratio = y / h
        if ratio < 0.5:
            r = int(95 + (240 - 95) * (ratio * 2))
            g = int(185 + (205 - 185) * (ratio * 2))
            b = int(240 + (130 - 240) * (ratio * 2))
        else:
            r = int(240 + (255 - 240) * ((ratio - 0.5) * 2))
            g = int(205 + (180 - 205) * ((ratio - 0.5) * 2))
            b = int(130 + (120 - 130) * ((ratio - 0.5) * 2))
        d.line([(0, y), (w, y)], fill=(r, g, b, 255))
    
    # Fluffy Pixel Clouds
    for cx, cy in [(40, 25), (140, 18), (250, 32)]:
        d.ellipse([cx, cy, cx+50, cy+18], fill=(255, 255, 255, 230))
        d.ellipse([cx+12, cy-6, cx+38, cy+14], fill=(255, 255, 255, 255))
    
    # Distant Mountains (Terraria Parallax Layer 1)
    mountain_pts = [(0, 115)]
    for x in range(0, w+20, 20):
        my = 85 + int(15 * math.sin(x * 0.04) + 10 * math.cos(x * 0.08))
        mountain_pts.append((x, my))
    mountain_pts.extend([(w, 180), (0, 180)])
    d.polygon(mountain_pts, fill=(110, 145, 175, 255))
    
    # Midground Pine Trees silhouette
    for tx in range(0, w, 16):
        ty = 100 + (tx % 7) * 2
        d.polygon([(tx+8, ty-22), (tx+2, ty), (tx+14, ty)], fill=(65, 110, 80, 255))
        d.polygon([(tx+8, ty-14), (tx+1, ty+6), (tx+15, ty+6)], fill=(50, 95, 65, 255))
    
    # Foreground Terraria Block Layer (Grass over Dirt)
    ground_y = 125
    # Brown Dirt blocks
    d.rectangle([0, ground_y + 8, w, h], fill=PALETTE["dirt_mid"])
    for y in range(ground_y + 8, h, 8):
        for x in range(0, w, 8):
            if (x * y) % 11 == 0:
                d.point((x, y), fill=PALETTE["dirt_dark"])
            elif (x + y) % 9 == 0:
                d.point((x, y), fill=PALETTE["dirt_light"])
    
    # Layer of Grass blocks with hanging blades
    d.rectangle([0, ground_y, w, ground_y + 8], fill=PALETTE["grass_mid"])
    d.line([(0, ground_y), (w, ground_y)], fill=PALETTE["grass_bright"], width=2)
    for x in range(0, w, 4):
        blade_h = 3 if x % 8 == 0 else 2
        d.line([(x, ground_y), (x, ground_y + blade_h + 3)], fill=PALETTE["grass_bright"])
        d.point((x, ground_y + blade_h + 4), fill=PALETTE["grass_shadow"])
    
    # Cozy Wooden Cottage on left
    d.rectangle([35, ground_y - 45, 95, ground_y], fill=PALETTE["wood_mid"], outline=PALETTE["black"])
    d.polygon([(28, ground_y - 45), (65, ground_y - 70), (102, ground_y - 45)], fill=PALETTE["wood_dark"], outline=PALETTE["black"])
    # Stone Chimney with smoke
    d.rectangle([80, ground_y - 68, 90, ground_y - 50], fill=PALETTE["stone_mid"], outline=PALETTE["black"])
    d.ellipse([82, ground_y - 75, 88, ground_y - 70], fill=(240, 240, 240, 180))
    d.ellipse([84, ground_y - 82, 92, ground_y - 76], fill=(240, 240, 240, 140))
    # Glowing window
    d.rectangle([48, ground_y - 32, 62, ground_y - 18], fill=PALETTE["torch_glow"], outline=PALETTE["wood_dark"])
    d.line([(55, ground_y - 32), (55, ground_y - 18)], fill=PALETTE["wood_dark"])
    d.line([(48, ground_y - 25), (62, ground_y - 25)], fill=PALETTE["wood_dark"])
    # Wooden Door
    d.rectangle([72, ground_y - 35, 86, ground_y], fill=PALETTE["wood_dark"], outline=PALETTE["black"])
    d.point((74, ground_y - 18), fill=PALETTE["gold_highlight"])
    
    # Wooden fences and potato sprout patches
    for fx in [110, 122, 134, 146]:
        d.rectangle([fx, ground_y - 14, fx + 3, ground_y], fill=PALETTE["wood_light"], outline=PALETTE["black"])
    d.line([(108, ground_y - 10), (150, ground_y - 10)], fill=PALETTE["wood_light"], width=2)
    # Potato green sprouts in field
    for px in [165, 185, 205, 225, 245]:
        d.ellipse([px, ground_y - 6, px + 8, ground_y], fill=PALETTE["sprout_green"], outline=PALETTE["black"])
        d.point((px + 4, ground_y - 8), fill=PALETTE["sprout_green"])
    
    save_scaled(village, os.path.join(OUTPUT_DIR, "backgrounds", "bg_village.png"), scale=4)

    # 6.2 Mist Forest Background (迷雾森林·幽暗荧光)
    forest = Image.new("RGBA", (w, h))
    d = ImageDraw.Draw(forest)
    # Eerie twilight purple to teal gradient
    for y in range(h):
        r = int(18 + (35 - 18) * (y / h))
        g = int(24 + (55 - 24) * (y / h))
        b = int(45 + (65 - 45) * (y / h))
        d.line([(0, y), (w, y)], fill=(r, g, b, 255))
    
    # Dense Twisted Pine Trees in background
    for tx in range(0, w, 24):
        ty = 80 + (tx % 5) * 4
        d.polygon([(tx+12, ty-50), (tx, ty), (tx+24, ty)], fill=(25, 45, 45, 255))
        d.polygon([(tx+12, ty-30), (tx-4, ty+20), (tx+28, ty+20)], fill=(18, 35, 38, 255))
    
    # Floating Mist Layers
    for my in [70, 95, 115]:
        d.ellipse([-20, my, w+20, my+30], fill=(60, 110, 110, 60))
    
    # Mossy Forest Ground
    ground_y = 125
    d.rectangle([0, ground_y + 8, w, h], fill=(45, 35, 30, 255))
    d.rectangle([0, ground_y, w, ground_y + 8], fill=(40, 85, 60, 255))
    d.line([(0, ground_y), (w, ground_y)], fill=(70, 150, 90, 255), width=2)
    
    # Bioluminescent Giant Glowing Mushrooms (Terraria Shroom Biome style!)
    for mx, my, msize in [(50, ground_y, 22), (180, ground_y, 16), (270, ground_y, 28)]:
        # Mushroom stalk
        d.rectangle([mx + msize//2 - 2, my - msize - 10, mx + msize//2 + 3, my], fill=(160, 210, 240, 255), outline=PALETTE["black"])
        # Glowing Blue Cap
        d.chord([mx, my - msize - 22, mx + msize + 8, my - msize + 4], 180, 360, fill=(40, 160, 240, 255), outline=PALETTE["black"])
        d.line([(mx, my - msize - 9), (mx + msize + 8, my - msize - 9)], fill=(90, 200, 255, 255), width=2)
        # White glowing spores on cap
        d.point((mx + 6, my - msize - 16), fill=(255, 255, 255, 255))
        d.point((mx + msize, my - msize - 15), fill=(255, 255, 255, 255))
    
    # Ancient Stone Monolith with glowing purple runes
    d.rectangle([110, ground_y - 55, 135, ground_y], fill=PALETTE["stone_dark"], outline=PALETTE["black"])
    d.line([(114, ground_y - 45), (131, ground_y - 45)], fill=PALETTE["epic"], width=2)
    d.line([(122, ground_y - 45), (122, ground_y - 20)], fill=PALETTE["epic"], width=2)
    d.line([(116, ground_y - 20), (128, ground_y - 20)], fill=PALETTE["epic"], width=2)
    
    save_scaled(forest, os.path.join(OUTPUT_DIR, "backgrounds", "bg_mist_forest.png"), scale=4)

    # 6.3 Dungeon Background (暗黑地牢·火把辉光)
    dungeon = Image.new("RGBA", (w, h))
    d = ImageDraw.Draw(dungeon)
    # Deep cobalt dungeon brick wall
    d.rectangle([0, 0, w, h], fill=(22, 28, 40, 255))
    # Terraria Dungeon Bricks pattern
    for y in range(0, h, 10):
        offset = 8 if (y // 10) % 2 == 1 else 0
        d.line([(0, y), (w, y)], fill=(15, 18, 25, 255), width=1)
        for x in range(offset, w, 16):
            d.line([(x, y), (x, y+10)], fill=(15, 18, 25, 255), width=1)
            # Brick highlight edge
            d.line([(x+1, y+1), (x+15, y+1)], fill=(38, 48, 68, 255), width=1)
    
    # Wall-mounted Torches with radial warm glow
    for tx in [60, 160, 260]:
        ty = 65
        # Torch glow halo
        d.ellipse([tx - 24, ty - 24, tx + 24, ty + 24], fill=(255, 160, 40, 50))
        d.ellipse([tx - 12, ty - 12, tx + 12, ty + 12], fill=(255, 210, 70, 90))
        # Iron sconce
        d.line([(tx - 4, ty + 10), (tx + 4, ty + 2)], fill=PALETTE["black"], width=2)
        # Wooden torch
        d.line([(tx, ty + 2), (tx, ty - 6)], fill=PALETTE["wood_dark"], width=3)
        # Burning flame
        d.ellipse([tx - 3, ty - 12, tx + 3, ty - 4], fill=PALETTE["ruby_red"])
        d.ellipse([tx - 2, ty - 10, tx + 2, ty - 5], fill=PALETTE["torch_glow"])
        d.point((tx, ty - 8), fill=(255, 255, 255, 255))
    
    # Hanging Iron Chains from ceiling
    for cx in [100, 215]:
        for cy in range(0, 45, 6):
            d.rectangle([cx - 1, cy, cx + 2, cy + 4], outline=PALETTE["black"], fill=PALETTE["stone_light"])
    
    # Dungeon Floor Flagstones
    ground_y = 130
    d.rectangle([0, ground_y, w, h], fill=(16, 20, 30, 255))
    d.line([(0, ground_y), (w, ground_y)], fill=(55, 70, 95, 255), width=2)
    for x in range(0, w, 20):
        d.line([(x, ground_y), (x, h)], fill=(10, 12, 18, 255), width=2)
    
    save_scaled(dungeon, os.path.join(OUTPUT_DIR, "backgrounds", "bg_dungeon.png"), scale=4)

    # 6.4 Title Screen Artwork (深林暗夜与远景)
    title_bg = Image.new("RGBA", (w, h))
    d = ImageDraw.Draw(title_bg)
    # Deep night sky gradient
    for y in range(h):
        r = int(12 + (30 - 12) * (y / h))
        g = int(15 + (45 - 15) * (y / h))
        b = int(35 + (70 - 35) * (y / h))
        d.line([(0, y), (w, y)], fill=(r, g, b, 255))
    # Full moon with glowing aura
    d.ellipse([230, 20, 275, 65], fill=(255, 255, 230, 60))
    d.ellipse([235, 25, 270, 60], fill=(255, 255, 220, 255), outline=PALETTE["black"])
    d.ellipse([240, 30, 248, 38], fill=(225, 225, 195, 255)) # Crater
    # Stars
    random.seed(42)
    for _ in range(60):
        sx = random.randint(0, w)
        sy = random.randint(0, 90)
        d.point((sx, sy), fill=(240, 245, 255, random.randint(150, 255)))
    # Pine forest silhouettes at bottom
    for tx in range(-10, w+20, 14):
        th = 60 + (tx % 11) * 3
        d.polygon([(tx+7, h - th), (tx, h), (tx+14, h)], fill=(10, 16, 25, 255))
    save_scaled(title_bg, os.path.join(OUTPUT_DIR, "backgrounds", "bg_title.png"), scale=4)

    # 6.5 Game Over Art (暗红血幕与碎裂土豆假面)
    game_over = Image.new("RGBA", (w, h))
    d = ImageDraw.Draw(game_over)
    for y in range(h):
        r = int(50 - 30 * (y / h))
        d.line([(0, y), (w, y)], fill=(r, 8, 12, 255))
    # Vignette
    d.ellipse([40, 20, w-40, h-20], fill=(15, 5, 8, 200))
    # Broken potato silhouette in center
    cx, cy = w // 2, h // 2 - 10
    draw_base_potato(d, cx - 20, cy - 20, 40, 44, light_color=(120, 50, 50, 255), mid_color=(80, 30, 30, 255), shadow_color=(50, 20, 20, 255))
    # Jagged crack through the potato
    d.line([(cx - 8, cy - 22), (cx - 2, cy - 5), (cx + 6, cy + 8), (cx, cy + 24)], fill=PALETTE["black"], width=2)
    save_scaled(game_over, os.path.join(OUTPUT_DIR, "backgrounds", "bg_game_over.png"), scale=4)

    # 6.6 Victory Art (金色朝阳与胜利土豆田)
    victory = Image.new("RGBA", (w, h))
    d = ImageDraw.Draw(victory)
    # Bright triumphant dawn sky
    for y in range(h):
        ratio = y / h
        r = int(255 - 40 * ratio)
        g = int(210 - 70 * ratio)
        b = int(120 - 50 * ratio)
        d.line([(0, y), (w, y)], fill=(r, g, b, 255))
    # Sunburst rays from horizon
    for angle in range(0, 180, 15):
        rad = math.radians(angle)
        ex = int(w/2 + 250 * math.cos(rad))
        ey = int(120 - 250 * math.sin(rad))
        d.polygon([(w//2, 120), (ex, ey), (ex + 15, ey)], fill=(255, 245, 170, 70))
    # Golden fertile potato hills
    d.chord([-50, 100, 200, 220], 180, 360, fill=PALETTE["grass_bright"])
    d.chord([120, 110, 370, 230], 180, 360, fill=PALETTE["grass_mid"])
    d.rectangle([0, 140, w, h], fill=PALETTE["grass_shadow"])
    # Happy bouncing potato hero
    draw_base_potato(d, w//2 - 16, 105, 32, 36)
    # Crown on victory potato!
    d.polygon([(w//2 - 10, 105), (w//2 - 6, 96), (w//2, 101), (w//2 + 6, 96), (w//2 + 10, 105)], fill=PALETTE["gold_highlight"], outline=PALETTE["black"])
    save_scaled(victory, os.path.join(OUTPUT_DIR, "backgrounds", "bg_victory.png"), scale=4)

if __name__ == "__main__":
    print("=== Generating Potato Adventure Pixel Art Assets ===")
    generate_ui_textures()
    generate_characters()
    generate_enemies()
    generate_npcs()
    generate_items()
    generate_backgrounds()
    print("=== All Assets Successfully Generated! ===")
