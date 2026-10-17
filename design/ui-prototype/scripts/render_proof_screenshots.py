#!/usr/bin/env python3
"""
Render pixel-perfect proof screenshots for Potato Adventure UI Prototype
Generates 1280x720 screenshots for all 13 screens/states into the proof directory.
"""

import os
import math
from PIL import Image, ImageDraw, ImageFont

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
ASSETS_DIR = os.path.join(BASE_DIR, "assets")
PROOF_DIR = "/home/Yuhang/potato-adventure/firstmate/data/potato-ui-prototype-b1/proof"
os.makedirs(PROOF_DIR, exist_ok=True)

FONT_PATH = os.path.join(ASSETS_DIR, "fonts", "unifont.otf")

def get_font(size=16):
    try:
        return ImageFont.truetype(FONT_PATH, size)
    except Exception:
        return ImageFont.load_default()

font12 = get_font(12)
font16 = get_font(16)
font20 = get_font(20)
font24 = get_font(24)
font32 = get_font(32)

def load_img(rel_path):
    p = os.path.join(ASSETS_DIR, rel_path)
    if os.path.exists(p):
        return Image.open(p).convert("RGBA")
    return None

def draw_pixel_button(draw, x, y, w, h, text, is_gold=False, font=font16):
    bg_col = (44, 58, 71, 255) if not is_gold else (214, 168, 72, 255)
    border_col = (100, 120, 140, 255) if not is_gold else (255, 235, 140, 255)
    shadow_col = (18, 24, 30, 255) if not is_gold else (122, 88, 40, 255)
    text_col = (240, 243, 245, 255) if not is_gold else (25, 20, 10, 255)

    # Beveled box
    draw.rectangle([x, y, x + w - 1, y + h - 1], fill=bg_col)
    draw.line([(x, y), (x + w - 1, y)], fill=border_col, width=2)
    draw.line([(x, y), (x, y + h - 1)], fill=border_col, width=2)
    draw.line([(x + w - 1, y), (x + w - 1, y + h - 1)], fill=shadow_col, width=2)
    draw.line([(x, y + h - 1), (x + w - 1, y + h - 1)], fill=shadow_col, width=2)

    bbox = font.getbbox(text)
    tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
    tx = x + (w - tw) // 2
    ty = y + (h - th) // 2 - 2
    draw.text((tx, ty), text, font=font, fill=text_col)

def draw_dock(img):
    draw = ImageDraw.Draw(img)
    dock_items = [
        "1.主菜单", "2.职业选择", "3.大地图HUD", "4.回合战斗",
        "5.背包装备", "6.NPC对话", "7.技能树", "8.存档管理",
        "9.系统设置", "10.战报通关", "11.游戏结束"
    ]
    total_w = len(dock_items) * 88
    sx = (1280 - total_w) // 2
    y = 688
    draw.rectangle([sx - 10, y - 4, sx + total_w + 10, y + 26], fill=(14, 18, 24, 235), outline=(214, 168, 72, 255), width=2)
    for i, it in enumerate(dock_items):
        ix = sx + i * 88
        draw.rectangle([ix, y, ix + 80, y + 22], fill=(32, 42, 53, 255), outline=(74, 92, 109, 255))
        draw.text((ix + 6, y + 4), it, font=font12, fill=(214, 224, 234, 255))

# ----------------- 1. Title Menu -----------------
def render_01_title():
    bg = load_img("backgrounds/bg_title.png")
    draw = ImageDraw.Draw(bg)
    logo = load_img("ui/logo_banner.png")
    if logo:
        lw, lh = logo.size
        bg.paste(logo, ((1280 - lw) // 2, 80), logo)

    # Buttons
    btn_labels = ["[ 新 的 冒 险 ]", "[ 继 续 游 戏 ]", "[ 存 档 管 理 ]", "[ 系 统 设 置 ]", "[ 关 于 游 戏 ]"]
    by = 310
    for i, label in enumerate(btn_labels):
        draw_pixel_button(draw, 510, by + i * 62, 260, 48, label, is_gold=(i == 0))

    draw.text((25, 655), "v2.0.0-PRO | 规范：Part C 像素级架构 | 引擎：Java 21 + JavaFX 21", font=font12, fill=(160, 175, 190, 255))
    draw_dock(bg)
    bg.save(os.path.join(PROOF_DIR, "01_title_menu.png"))

# ----------------- 2. Character Select -----------------
def render_02_charselect():
    bg = Image.new("RGBA", (1280, 720), (25, 32, 38, 255))
    draw = ImageDraw.Draw(bg)
    # Wood background tint
    draw.rectangle([0, 0, 1280, 720], fill=(20, 26, 32, 255))
    draw.text((450, 40), "❖ 选 择 你 的 土 豆 勇 士 ❖", font=font24, fill=(255, 210, 100, 255))

    classes = [
        ("berserker", "土豆狂战士", "近战强攻", "难度: ★☆☆ (推荐新人)", "portrait_berserker.png", "初始技能【旋风斩】: 造成 140% 物理狂暴伤害", True),
        ("mage", "土豆大魔导", "元素爆破", "难度: ★★☆ (法术爆发)", "portrait_mage.png", "初始技能【爆裂火球】: 造成 180% 烈焰附带灼烧", False),
        ("ranger", "土豆游侠", "游击穿透", "难度: ★★★ (敏捷暴击)", "portrait_ranger.png", "初始技能【连环快箭】: 连射三支破空穿甲箭", False)
    ]

    for i, (cid, cname, cbadge, cdiff, cport, cdesc, is_sel) in enumerate(classes):
        cx = 100 + i * 380
        cy = 100
        border_col = (255, 210, 100, 255) if is_sel else (70, 85, 100, 255)
        bg_col = (30, 42, 54, 255) if is_sel else (24, 32, 40, 255)
        draw.rectangle([cx, cy, cx + 320, cy + 500], fill=bg_col, outline=border_col, width=3 if is_sel else 2)

        # Badge
        draw.rectangle([cx + 10, cy + 10, cx + 80, cy + 30], fill=(231, 76, 60, 255) if i == 0 else ((41, 128, 185, 255) if i == 1 else (39, 174, 96, 255)))
        draw.text((cx + 16, cy + 13), cbadge, font=font12, fill=(255, 255, 255, 255))

        # Portrait
        pimg = load_img(f"sprites/{cport}")
        if pimg:
            pw, ph = pimg.size
            bg.paste(pimg, (cx + (320 - pw) // 2, cy + 40), pimg)

        draw.text((cx + (320 - len(cname)*16)//2, cy + 180), cname, font=font20, fill=(255, 235, 150, 255))
        draw.text((cx + 35, cy + 215), cdiff, font=font12, fill=(180, 200, 220, 255))

        # Mini Hex Radar Chart
        rcx, rcy, rradius = cx + 160, cy + 300, 50
        # Draw hexagon background
        pts = []
        for a in range(6):
            rad = math.radians(a * 60 - 30)
            pts.append((rcx + rradius * math.cos(rad), rcy + rradius * math.sin(rad)))
        draw.polygon(pts, fill=(20, 25, 35, 200), outline=(80, 100, 120, 255))
        # Stat polygon
        stat_weights = [0.9, 0.4, 0.9, 0.8, 0.5, 0.6] if i == 0 else ([0.5, 0.95, 0.85, 0.4, 0.9, 0.5] if i == 1 else [0.6, 0.6, 0.75, 0.5, 0.8, 0.95])
        spts = []
        for a, w in enumerate(stat_weights):
            rad = math.radians(a * 60 - 30)
            spts.append((rcx + rradius * w * math.cos(rad), rcy + rradius * w * math.sin(rad)))
        draw.polygon(spts, fill=(255, 210, 80, 120), outline=(255, 215, 0, 255), width=2)
        draw.text((rcx - 24, rcy + 55), "属性六边形", font=font12, fill=(200, 215, 230, 255))

        # Skill preview
        draw.rectangle([cx + 15, cy + 395, cx + 305, cy + 480], fill=(16, 22, 28, 255), outline=(50, 65, 80, 255))
        draw.text((cx + 25, cy + 405), cdesc[:16], font=font12, fill=(255, 215, 80, 255))
        draw.text((cx + 25, cy + 425), cdesc[16:], font=font12, fill=(200, 210, 220, 255))

    # Bottom bar
    draw_pixel_button(draw, 300, 620, 160, 48, "« 返 回")
    draw.rectangle([500, 620, 780, 668], fill=(14, 18, 24, 255), outline=(214, 168, 72, 255), width=2)
    draw.text((540, 634), "角色名: [ 土豆勇士 ]", font=font16, fill=(255, 255, 255, 255))
    draw_pixel_button(draw, 820, 620, 180, 48, "踏 上 征 程 »", is_gold=True)

    draw_dock(bg)
    bg.save(os.path.join(PROOF_DIR, "02_character_select.png"))

# ----------------- 3, 4, 5. Overworld Biomes -----------------
def render_overworld(biome_name, out_name, area_title):
    bg_map = {
        "village": "backgrounds/bg_village.png",
        "forest": "backgrounds/bg_mist_forest.png",
        "dungeon": "backgrounds/bg_dungeon.png"
    }
    bg = load_img(bg_map[biome_name])
    draw = ImageDraw.Draw(bg)

    # Draw Entities
    hero = load_img("sprites/hero_berserker.png")
    if hero:
        bg.paste(hero, (400, 480), hero)
        draw.text((395, 460), "土豆勇士", font=font12, fill=(255, 255, 255, 255))

    if biome_name == "village":
        npc1 = load_img("sprites/npc_thomas.png")
        if npc1: bg.paste(npc1, (280, 480), npc1)
        draw.text((280, 460), "村长 托马斯", font=font12, fill=(255, 215, 80, 255))
        npc2 = load_img("sprites/npc_raine.png")
        if npc2: bg.paste(npc2, (680, 475), npc2)
        draw.text((680, 455), "铁匠 雷恩", font=font12, fill=(255, 215, 80, 255))
        mob = load_img("sprites/enemy_slime.png")
        if mob: bg.paste(mob, (1020, 500), mob)
        draw.text((1015, 480), "迷雾史莱姆", font=font12, fill=(230, 80, 80, 255))
    elif biome_name == "forest":
        mob1 = load_img("sprites/enemy_slime.png")
        if mob1: bg.paste(mob1, (320, 500), mob1)
        mob2 = load_img("sprites/enemy_goblin.png")
        if mob2: bg.paste(mob2, (820, 480), mob2)
        draw.text((815, 460), "哥布林斥候", font=font12, fill=(230, 80, 80, 255))
    elif biome_name == "dungeon":
        mob1 = load_img("sprites/enemy_goblin.png")
        if mob1: bg.paste(mob1, (420, 490), mob1)
        mob2 = load_img("sprites/boss_dark_potato.png")
        if mob2: bg.paste(mob2, (880, 450), mob2)
        draw.text((880, 430), "暗黑土豆领主 [BOSS]", font=font12, fill=(230, 40, 40, 255))

    # HUD Status Frame (Top Left)
    draw.rectangle([15, 15, 290, 115], fill=(30, 39, 44, 235), outline=(214, 168, 72, 255), width=2)
    draw.rectangle([25, 25, 65, 65], fill=(20, 26, 32, 255), outline=(100, 120, 140, 255))
    avatar = load_img("sprites/hero_berserker.png")
    if avatar:
        av_small = avatar.resize((36, 36), Image.NEAREST)
        bg.paste(av_small, (27, 27), av_small)
    draw.text((75, 25), "土豆勇士 Lv.3  战士阶", font=font12, fill=(255, 235, 150, 255))

    # HP Bar
    draw.rectangle([75, 45, 275, 59], fill=(50, 20, 20, 255), outline=(10, 10, 10, 255))
    draw.rectangle([76, 46, 274, 58], fill=(231, 76, 60, 255))
    draw.text((130, 45), "HP 170 / 170", font=font12, fill=(255, 255, 255, 255))

    # MP Bar
    draw.rectangle([75, 64, 275, 78], fill=(20, 30, 50, 255), outline=(10, 10, 10, 255))
    draw.rectangle([76, 65, 274, 77], fill=(41, 128, 185, 255))
    draw.text((135, 64), "MP 50 / 50", font=font12, fill=(255, 255, 255, 255))

    # EXP Bar
    draw.rectangle([75, 83, 275, 93], fill=(30, 30, 30, 255), outline=(10, 10, 10, 255))
    draw.rectangle([76, 84, 145, 92], fill=(243, 156, 18, 255))
    draw.text((150, 83), "EXP 35%", font=font12, fill=(255, 255, 255, 255))

    # Biome Selector (Top Center)
    draw.rectangle([500, 15, 780, 50], fill=(20, 26, 34, 230), outline=(214, 168, 72, 255), width=2)
    b_items = [("平原村庄", "village"), ("迷雾森林", "forest"), ("暗黑地牢", "dungeon")]
    for bi, (btxt, bkey) in enumerate(b_items):
        is_cur = (bkey == biome_name)
        bx = 510 + bi * 88
        draw.rectangle([bx, 20, bx + 80, 45], fill=(214, 168, 72, 255) if is_cur else (35, 45, 55, 255))
        draw.text((bx + 8, 26), btxt, font=font12, fill=(20, 15, 10, 255) if is_cur else (210, 220, 230, 255))

    # Area Banner
    draw.text((540, 60), area_title, font=font16, fill=(255, 235, 140, 255))

    # Minimap Radar (Top Right)
    draw.rectangle([1115, 15, 1265, 165], fill=(18, 24, 30, 235), outline=(214, 168, 72, 255), width=2)
    draw.text((1140, 20), "• 场景雷达 •", font=font12, fill=(214, 168, 72, 255))
    draw.ellipse([1180, 85, 1192, 97], fill=(46, 204, 113, 255), outline=(0, 0, 0, 255)) # Player
    draw.ellipse([1140, 110, 1150, 120], fill=(231, 76, 60, 255)) # Mob
    draw.ellipse([1230, 75, 1240, 85], fill=(52, 152, 219, 255)) # NPC

    # Quest Tracker (Bottom Left)
    draw.rectangle([15, 540, 275, 660], fill=(20, 26, 32, 235), outline=(70, 85, 100, 255), width=2)
    draw.rectangle([15, 540, 275, 565], fill=(32, 42, 52, 255))
    draw.text((25, 546), "当前委托 (Q) [折叠/展开]", font=font12, fill=(255, 210, 100, 255))
    draw.text((25, 575), "• 讨伐迷雾史莱姆 (2/3)", font=font12, fill=(220, 230, 240, 255))
    draw.text((25, 600), "• 与村长托马斯商讨对策", font=font12, fill=(220, 230, 240, 255))
    draw.text((25, 625), "• 探访铁匠雷恩打磨阔剑", font=font12, fill=(220, 230, 240, 255))

    # Action Hotbar (Bottom Center)
    draw.rectangle([460, 615, 820, 672], fill=(20, 26, 34, 245), outline=(214, 168, 72, 255), width=2)
    hotbar_items = [
        ("[1]", "items/item_potion_hp.png", "×5"),
        ("[2]", "items/item_potion_mp.png", "×3"),
        ("[B]", "items/item_potato_roast.png", ""),
        ("[K]", "items/item_sword_epic.png", ""),
        ("[⚔]", "sprites/enemy_slime.png", ""),
        ("[P]", "ui/stone_slate.png", "")
    ]
    for hi, (hkey, hicon, hcount) in enumerate(hotbar_items):
        hx = 472 + hi * 56
        draw.rectangle([hx, 622, hx + 48, 666], fill=(35, 45, 55, 255), outline=(70, 85, 100, 255))
        draw.text((hx + 2, 623), hkey, font=font12, fill=(255, 215, 100, 255))
        ic = load_img(hicon)
        if ic:
            ic_s = ic.resize((28, 28), Image.NEAREST)
            bg.paste(ic_s, (hx + 10, 632), ic_s)
        if hcount:
            draw.text((hx + 26, 650), hcount, font=font12, fill=(255, 255, 255, 255))

    draw_dock(bg)
    bg.save(os.path.join(PROOF_DIR, out_name))

# ----------------- 6. Battle Screen -----------------
def render_06_battle():
    bg = load_img("backgrounds/bg_mist_forest.png")
    draw = ImageDraw.Draw(bg)

    # Turn Order Timeline Gauge
    draw.rectangle([340, 15, 940, 55], fill=(20, 26, 32, 235), outline=(214, 168, 72, 255), width=2)
    draw.text((355, 26), "行动队列 »", font=font12, fill=(255, 210, 100, 255))
    draw.rectangle([440, 20, 472, 50], fill=(214, 168, 72, 255), outline=(255, 255, 255, 255), width=2)
    draw.rectangle([485, 20, 517, 50], fill=(45, 55, 65, 255))
    draw.text((540, 26), "当前行动：土豆勇士 (回合 1)", font=font12, fill=(255, 235, 150, 255))

    # Hero Spot
    draw.text((220, 190), "土豆勇士 Lv.3", font=font16, fill=(255, 255, 255, 255))
    draw.rectangle([220, 215, 380, 227], fill=(50, 20, 20, 255))
    draw.rectangle([220, 215, 380, 227], fill=(231, 76, 60, 255))
    hero = load_img("sprites/hero_berserker.png")
    if hero:
        hero_lg = hero.resize((96, 96), Image.NEAREST)
        bg.paste(hero_lg, (230, 240), hero_lg)

    # Enemy Spot
    draw.text((860, 180), "迷雾史莱姆 Lv.2", font=font16, fill=(255, 100, 100, 255))
    draw.rectangle([860, 205, 1020, 217], fill=(50, 20, 20, 255))
    draw.rectangle([860, 205, 970, 217], fill=(231, 76, 60, 255))
    slime = load_img("sprites/enemy_slime.png")
    if slime:
        slime_lg = slime.resize((96, 96), Image.NEAREST)
        bg.paste(slime_lg, (870, 230), slime_lg)

    # Damage Floating Number
    draw.text((910, 150), "-42 HP", font=font24, fill=(255, 70, 70, 255))

    # Lower Tactical Console Deck
    draw.rectangle([0, 470, 1280, 720], fill=(25, 32, 38, 255), outline=(122, 88, 40, 255), width=3)

    # 4 Action Cards (Left)
    cmds = [("⚔ 普通攻击 [Z/1]", 30, 485), ("✨ 战术技能 [X/2]", 195, 485), ("🧪 背包道具 [C/3]", 30, 580), ("🛡 防御逃跑 [V/4]", 195, 580)]
    for ctxt, cx, cy in cmds:
        draw_pixel_button(draw, cx, cy, 150, 85, ctxt, is_gold=("战术技能" in ctxt))

    # Middle Subdeck (Skills)
    draw.rectangle([370, 485, 820, 665], fill=(18, 24, 30, 255), outline=(74, 92, 109, 255), width=2)
    draw.text((385, 495), "可用战术技能：", font=font12, fill=(255, 210, 100, 255))
    skills = [
        ("1. 旋风斩 (MP 15)", "对敌阵造成 65 点物理狂暴伤害"),
        ("2. 烈焰突刺 (MP 20)", "炎刃贯穿造成 85 点烈焰伤害"),
        ("3. 铁壁战阵 (MP 10)", "恢复 35 点生命并强化物理防御"),
        ("4. 狂暴巨力 (MP 25)", "极限一击造成 110 点暴击物理伤害")
    ]
    for si, (sname, sdesc) in enumerate(skills):
        sy = 520 + si * 34
        draw.rectangle([385, sy, 805, sy + 30], fill=(28, 38, 48, 255))
        draw.text((395, sy + 6), sname, font=font12, fill=(255, 235, 140, 255))
        draw.text((540, sy + 6), sdesc, font=font12, fill=(180, 195, 210, 255))

    # Combat Log (Right)
    draw.rectangle([840, 485, 1250, 665], fill=(245, 230, 190, 255), outline=(145, 115, 75, 255), width=2)
    draw.text((855, 495), "【战斗实况日志】", font=font12, fill=(95, 65, 20, 255))
    logs = [
        "遭遇了强敌【迷雾史莱姆】！",
        "土豆勇士发动【旋风斩】！",
        "对迷雾史莱姆造成 42 点伤害！",
        "迷雾史莱姆准备反击..."
    ]
    for li, log_text in enumerate(logs):
        draw.text((855, 525 + li * 28), log_text, font=font12, fill=(40, 24, 15, 255))

    draw_dock(bg)
    bg.save(os.path.join(PROOF_DIR, "06_battle_screen.png"))

# ----------------- 7. Inventory Modal -----------------
def render_07_inventory():
    bg = load_img("backgrounds/bg_village.png")
    # Overlay dark dim
    dim = Image.new("RGBA", (1280, 720), (10, 14, 20, 190))
    bg.alpha_composite(dim)
    draw = ImageDraw.Draw(bg)

    # Modal Box
    mx, my, mw, mh = 160, 60, 960, 600
    draw.rectangle([mx, my, mx + mw, my + mh], fill=(30, 39, 44, 255), outline=(214, 168, 72, 255), width=3)
    draw.text((mx + 280, my + 15), "❖ 冒 险 者 行 囊 与 装 备 (Inventory) ❖", font=font20, fill=(255, 210, 100, 255))

    # Left: Paper Doll & Live Stats
    draw.rectangle([mx + 20, my + 60, mx + 380, my + 575], fill=(20, 26, 32, 255), outline=(74, 92, 109, 255), width=2)
    hero = load_img("sprites/hero_berserker.png")
    if hero:
        hero_b = hero.resize((120, 120), Image.NEAREST)
        bg.paste(hero_b, (mx + 40, my + 80), hero_b)

    # 3 Equipment Slots
    slots = [("武器", "items/item_sword.png"), ("防具", "items/item_armor_iron.png"), ("饰品", "items/item_ring_power.png")]
    for si, (slabel, sitem) in enumerate(slots):
        sy = my + 80 + si * 55
        draw.rectangle([mx + 200, sy, mx + 248, sy + 48], fill=(35, 45, 55, 255), outline=(214, 168, 72, 255), width=2)
        it = load_img(sitem)
        if it:
            it_s = it.resize((32, 32), Image.NEAREST)
            bg.paste(it_s, (mx + 208, sy + 8), it_s)
        draw.text((mx + 260, sy + 15), slabel, font=font12, fill=(255, 235, 150, 255))

    # Stats List
    stats = [
        ("生命上限 HP", "170"), ("魔力上限 MP", "50"),
        ("物理攻击 ATK", "35"), ("物理防御 DEF", "30"),
        ("暴击几率 CRIT", "10%"), ("移动速度 SPD", "105")
    ]
    for sti, (st_name, st_val) in enumerate(stats):
        sty = my + 280 + sti * 42
        draw.rectangle([mx + 35, sty, mx + 365, sty + 34], fill=(28, 38, 48, 255))
        draw.text((mx + 45, sty + 8), st_name, font=font12, fill=(200, 215, 230, 255))
        draw.text((mx + 300, sty + 8), st_val, font=font12, fill=(255, 215, 80, 255))

    # Right: 24 Grid Bag
    draw.rectangle([mx + 400, my + 60, mx + 940, my + 575], fill=(20, 26, 32, 255), outline=(74, 92, 109, 255), width=2)
    # Tabs
    tabs = ["全 部 (24)", "消 耗 品", "装 备", "材 料"]
    for ti, tname in enumerate(tabs):
        tx = mx + 415 + ti * 125
        draw.rectangle([tx, my + 75, tx + 115, my + 105], fill=(214, 168, 72, 255) if ti == 0 else (35, 45, 55, 255))
        draw.text((tx + 18, my + 82), tname, font=font12, fill=(20, 15, 10, 255) if ti == 0 else (210, 220, 230, 255))

    # 6x4 Grid
    items_preview = [
        "items/item_sword_epic.png", "items/item_staff_fire.png", "items/item_bow.png",
        "items/item_armor_robe.png", "items/item_armor_leather.png", "items/item_boots_haste.png",
        "items/item_amulet_shield.png", "items/item_potion_hp.png", "items/item_potion_mp.png",
        "items/item_potato_roast.png", "items/item_slime_gel.png", "items/item_goblin_coins.png",
        "items/item_dark_core.png"
    ]
    for row in range(4):
        for col in range(6):
            gx = mx + 420 + col * 84
            gy = my + 125 + row * 84
            draw.rectangle([gx, gy, gx + 72, gy + 72], fill=(28, 38, 48, 255), outline=(74, 92, 109, 255), width=2)
            idx = row * 6 + col
            if idx < len(items_preview):
                it = load_img(items_preview[idx])
                if it:
                    it_s = it.resize((48, 48), Image.NEAREST)
                    bg.paste(it_s, (gx + 12, gy + 12), it_s)

    # Tooltip preview (hovering over Dark Slayer Sword)
    draw.rectangle([mx + 620, my + 260, mx + 880, my + 440], fill=(15, 20, 26, 250), outline=(155, 89, 182, 255), width=2)
    draw.text((mx + 635, my + 275), "暗黑斩魔剑 [史诗品质]", font=font12, fill=(155, 89, 182, 255))
    draw.text((mx + 635, my + 305), "装备部位: 武器槽", font=font12, fill=(200, 210, 220, 255))
    draw.text((mx + 635, my + 330), "物理攻击: +45 (+30 比当前高)", font=font12, fill=(46, 204, 113, 255))
    draw.text((mx + 635, my + 355), "暴击几率: +15%", font=font12, fill=(241, 196, 15, 255))
    draw.text((mx + 635, my + 385), "蕴藏深渊魔能的诅咒巨刃...", font=font12, fill=(160, 175, 190, 255))

    # Bottom Gold
    draw.text((mx + 420, my + 540), "金币: 850 G   负重容量: 13 / 24", font=font16, fill=(255, 215, 80, 255))

    draw_dock(bg)
    bg.save(os.path.join(PROOF_DIR, "07_inventory_modal.png"))

# ----------------- 8. NPC Dialogue -----------------
def render_08_dialogue():
    bg = load_img("backgrounds/bg_village.png")
    dim = Image.new("RGBA", (1280, 720), (10, 14, 20, 120))
    bg.alpha_composite(dim)
    draw = ImageDraw.Draw(bg)

    # Dialogue Parchment Box
    dx, dy, dw, dh = 140, 480, 1000, 200
    draw.rectangle([dx, dy, dx + dw, dy + dh], fill=(245, 230, 190, 255), outline=(125, 80, 50, 255), width=4)

    # NPC Portrait
    npc = load_img("sprites/npc_thomas.png")
    if npc:
        npc_lg = npc.resize((140, 140), Image.NEAREST)
        bg.paste(npc_lg, (dx + 25, dy + 20), npc_lg)

    draw.text((dx + 185, dy + 25), "【村长 托马斯】", font=font20, fill=(125, 80, 40, 255))
    dialogue_lines = [
        "欢迎来到土豆村，年轻的勇士！最近迷雾森林异动频频，",
        "甚至有村民目击到了暗影怪物的踪迹。我们需要你的力量！"
    ]
    for li, line in enumerate(dialogue_lines):
        draw.text((dx + 185, dy + 65 + li * 28), line, font=font16, fill=(40, 24, 15, 255))

    # Choices
    choices = ["[1. 接受委托 (讨伐史莱姆)]", "[2. 询问迷雾森林线索]", "[3. 告辞离开]"]
    for ci, ctext in enumerate(choices):
        cx = dx + 185 + ci * 260
        draw_pixel_button(draw, cx, dy + 135, 240, 42, ctext, is_gold=(ci == 0), font=font12)

    draw_dock(bg)
    bg.save(os.path.join(PROOF_DIR, "08_npc_dialogue.png"))

# ----------------- 9. Skill Tree -----------------
def render_09_skilltree():
    bg = load_img("backgrounds/bg_dungeon.png")
    dim = Image.new("RGBA", (1280, 720), (15, 20, 28, 220))
    bg.alpha_composite(dim)
    draw = ImageDraw.Draw(bg)

    sx, sy, sw, sh = 160, 60, 960, 600
    draw.rectangle([sx, sy, sx + sw, sy + sh], fill=(25, 32, 40, 255), outline=(214, 168, 72, 255), width=3)
    draw.text((sx + 260, sy + 20), "❖ 勇 士 技 能 树 潜 能 (Skill Talents) ❖", font=font20, fill=(255, 210, 100, 255))
    draw.text((sx + 680, sy + 22), "剩余技能点: 3 SP", font=font16, fill=(46, 204, 113, 255))

    # Skill Tree Nodes & Lines
    nodes = [
        ("强击", sx + 120, sy + 260, True, "基础剑术 120% 伤害"),
        ("旋风斩", sx + 340, sy + 160, True, "旋风群体伤害"),
        ("铁壁战甲", sx + 340, sy + 360, False, "提升 15% 护甲"),
        ("破甲重击", sx + 580, sy + 160, False, "降低敌人 30% 防御"),
        ("狂暴之怒", sx + 580, sy + 360, False, "低血量暴击 +50%"),
        ("泰坦裂地奥义", sx + 800, sy + 260, False, "终极奥义 350% 暴击伤害")
    ]
    # Draw connecting lines
    lines = [(0, 1), (0, 2), (1, 3), (2, 4), (3, 5)]
    for n1, n2 in lines:
        draw.line([(nodes[n1][1] + 40, nodes[n1][2] + 40), (nodes[n2][1] + 40, nodes[n2][2] + 40)], fill=(214, 168, 72, 255) if nodes[n1][3] and nodes[n2][3] else (74, 92, 109, 255), width=3)

    for nname, nx, ny, unl, ndesc in nodes:
        draw.rectangle([nx, ny, nx + 80, ny + 80], fill=(214, 168, 72, 255) if unl else (35, 45, 55, 255), outline=(255, 235, 140, 255) if unl else (100, 120, 140, 255), width=2)
        draw.text((nx + 10, ny + 30), nname[:4], font=font12, fill=(20, 15, 10, 255) if unl else (200, 210, 220, 255))
        draw.text((nx - 20, ny + 90), ndesc[:8], font=font12, fill=(180, 195, 210, 255))

    draw_dock(bg)
    bg.save(os.path.join(PROOF_DIR, "09_skill_tree.png"))

# ----------------- 10. Save / Load -----------------
def render_10_saveload():
    bg = load_img("backgrounds/bg_title.png")
    dim = Image.new("RGBA", (1280, 720), (10, 15, 22, 210))
    bg.alpha_composite(dim)
    draw = ImageDraw.Draw(bg)

    draw.text((450, 60), "❖ 冒 险 纪 事 录 (Save & Load Slots) ❖", font=font24, fill=(255, 210, 100, 255))

    # 3 Slots
    slots_data = [
        ("存档槽位 01", "土豆狂战士 · 瓦伦丁", "Lv.12", "04小时28分", "迷雾森林深处", "2026-10-17 14:32", "backgrounds/bg_village.png"),
        ("存档槽位 02", "土豆大魔导 · 莫甘娜", "Lv.25", "12小时40分", "暗黑地牢回廊", "2026-10-16 21:05", "backgrounds/bg_dungeon.png"),
        ("空白存档槽位 03", "崭新冒险记录", "--", "--", "--", "--", None)
    ]
    for si, (slabel, shero, slv, stime, smap, sdate, sthumb) in enumerate(slots_data):
        sx = 100 + si * 380
        sy = 130
        draw.rectangle([sx, sy, sx + 330, sy + 480], fill=(25, 33, 42, 255), outline=(214, 168, 72, 255), width=2)
        draw.text((sx + 15, sy + 15), slabel, font=font16, fill=(255, 235, 140, 255))

        if sthumb:
            th = load_img(sthumb)
            if th:
                th_s = th.resize((300, 160), Image.NEAREST)
                bg.paste(th_s, (sx + 15, sy + 45))
            draw.text((sx + 15, sy + 225), f"勇士: {shero}", font=font12, fill=(220, 230, 240, 255))
            draw.text((sx + 15, sy + 255), f"等级: {slv} | 时长: {stime}", font=font12, fill=(220, 230, 240, 255))
            draw.text((sx + 15, sy + 285), f"场景: {smap}", font=font12, fill=(220, 230, 240, 255))
            draw.text((sx + 15, sy + 315), f"保存时间: {sdate}", font=font12, fill=(160, 175, 190, 255))
            draw_pixel_button(draw, sx + 15, sy + 380, 85, 40, "读取", is_gold=True)
            draw_pixel_button(draw, sx + 115, sy + 380, 85, 40, "覆盖")
            draw_pixel_button(draw, sx + 215, sy + 380, 85, 40, "删除")
        else:
            draw.text((sx + 110, sy + 180), "+", font=font32, fill=(100, 120, 140, 255))
            draw.text((sx + 85, sy + 240), "可新建存档保存", font=font16, fill=(160, 175, 190, 255))
            draw_pixel_button(draw, sx + 65, sy + 380, 200, 48, "[ 新 建 保 存 ]", is_gold=True)

    draw_dock(bg)
    bg.save(os.path.join(PROOF_DIR, "10_save_load.png"))

# ----------------- 11. Settings -----------------
def render_11_settings():
    bg = load_img("backgrounds/bg_title.png")
    dim = Image.new("RGBA", (1280, 720), (10, 15, 22, 220))
    bg.alpha_composite(dim)
    draw = ImageDraw.Draw(bg)

    sx, sy, sw, sh = 280, 70, 720, 580
    draw.rectangle([sx, sy, sx + sw, sy + sh], fill=(25, 33, 42, 255), outline=(214, 168, 72, 255), width=3)
    draw.text((sx + 200, sy + 25), "❖ 系 统 设 置 (System Settings) ❖", font=font20, fill=(255, 210, 100, 255))

    settings_rows = [
        ("主音量 (Master Volume)", "70% [======----]"),
        ("战斗音效 (SFX Volume)", "80% [========--]"),
        ("画面比例基准", "1280 × 720 (16:9 像素等比防形变)"),
        ("渲染滤波 (Texture Filter)", "点阵硬边缘 (Nearest-Neighbor, Pixelated)"),
        ("环境天气粒子", "全开 (落叶 / 孢子 / 火花)")
    ]
    for ri, (stitle, sval) in enumerate(settings_rows):
        ry = sy + 80 + ri * 48
        draw.text((sx + 40, ry), stitle, font=font16, fill=(220, 230, 240, 255))
        draw.text((sx + 380, ry), sval, font=font16, fill=(255, 215, 80, 255))

    draw.line([(sx + 30, sy + 340), (sx + sw - 30, sy + 340)], fill=(74, 92, 109, 255), width=1)
    draw.text((sx + 40, sy + 360), "常用按键映射：", font=font16, fill=(255, 210, 100, 255))
    keybinds = [
        ("角色移动: W A S D / 方向键", "交互/确认: E / 空格"),
        ("打开背包: B / I", "技能天赋: K"),
        ("普通攻击: Z / 1", "释放技能: X / 2")
    ]
    for ki, (k1, k2) in enumerate(keybinds):
        ky = sy + 395 + ki * 36
        draw.text((sx + 50, ky), k1, font=font12, fill=(200, 215, 230, 255))
        draw.text((sx + 380, ky), k2, font=font12, fill=(200, 215, 230, 255))

    draw_pixel_button(draw, sx + 180, sy + 510, 160, 44, "恢复默认")
    draw_pixel_button(draw, sx + 380, sy + 510, 160, 44, "保存并关闭", is_gold=True)

    draw_dock(bg)
    bg.save(os.path.join(PROOF_DIR, "11_settings_modal.png"))

# ----------------- 12. Victory Screen -----------------
def render_12_victory():
    bg = load_img("backgrounds/bg_victory.png")
    draw = ImageDraw.Draw(bg)

    vx, vy, vw, vh = 300, 120, 680, 480
    draw.rectangle([vx, vy, vx + vw, vy + vh], fill=(18, 24, 32, 240), outline=(255, 210, 80, 255), width=3)
    draw.text((vx + 160, vy + 30), "★ 传 奇 土 豆 守 护 者 ★", font=font24, fill=(255, 235, 140, 255))
    draw.text((vx + 110, vy + 75), "暗黑土豆领主已被净化！丰收阳光再度普照整片大陆！", font=font16, fill=(46, 204, 113, 255))

    vstats = [
        ("最终通关等级", "Lv.30 (巅峰)"),
        ("总冒险时长", "14 小时 28 分"),
        ("收集神话装备", "4 件"),
        ("Boss 斩杀评级", "SSS 级 (完胜)")
    ]
    for vi, (vt, vv) in enumerate(vstats):
        vy_s = vy + 125 + vi * 45
        draw.rectangle([vx + 60, vy_s, vx + vw - 60, vy_s + 36], fill=(28, 38, 48, 255))
        draw.text((vx + 80, vy_s + 8), vt, font=font16, fill=(200, 215, 230, 255))
        draw.text((vx + 450, vy_s + 8), vv, font=font16, fill=(255, 215, 80, 255))

    draw.text((vx + 100, vy + 330), "感谢体验《土豆大冒险》！技术路线：Java 21 + JavaFX 21", font=font12, fill=(160, 175, 190, 255))
    draw_pixel_button(draw, vx + 200, vy + 390, 280, 50, "[ 凯 旋 归 来 (返回主菜单) ]", is_gold=True)

    draw_dock(bg)
    bg.save(os.path.join(PROOF_DIR, "12_victory_screen.png"))

# ----------------- 13. Game Over Screen -----------------
def render_13_gameover():
    bg = load_img("backgrounds/bg_game_over.png")
    draw = ImageDraw.Draw(bg)

    gx, gy, gw, gh = 300, 120, 680, 480
    draw.rectangle([gx, gy, gx + gw, gy + gh], fill=(24, 14, 18, 240), outline=(231, 76, 60, 255), width=3)
    draw.text((gx + 160, gy + 30), "☠ 胜 败 乃 兵 家 常 事 ☠", font=font24, fill=(231, 76, 60, 255))
    draw.text((gx + 170, gy + 75), "土豆勇士力竭倒在了暗影迷雾之中...", font=font16, fill=(220, 200, 200, 255))

    gstats = [
        ("本次坚持回合", "14 回合"),
        ("累计造成伤害", "1,240 点"),
        ("击溃敌怪数量", "6 只"),
        ("当前所获经验", "+420 EXP")
    ]
    for gi, (gt, gv) in enumerate(gstats):
        gy_s = gy + 125 + gi * 45
        draw.rectangle([gx + 60, gy_s, gx + gw - 60, gy_s + 36], fill=(40, 20, 25, 255))
        draw.text((gx + 80, gy_s + 8), gt, font=font16, fill=(220, 200, 200, 255))
        draw.text((gx + 450, gy_s + 8), gv, font=font16, fill=(231, 76, 60, 255))

    draw_pixel_button(draw, gx + 60, gy + 390, 160, 48, "[ 读取最近存档 ]", is_gold=True)
    draw_pixel_button(draw, gx + 255, gy + 390, 160, 48, "[ 重生于村庄 ]")
    draw_pixel_button(draw, gx + 450, gy + 390, 160, 48, "[ 返回主菜单 ]")

    draw_dock(bg)
    bg.save(os.path.join(PROOF_DIR, "13_game_over.png"))

if __name__ == "__main__":
    print("=== Rendering Proof Screenshots for All 13 Screens ===")
    render_01_title()
    render_02_charselect()
    render_03_overworld_village = lambda: render_overworld("village", "03_overworld_village.png", "【和平原野 · 土豆村落】")
    render_03_overworld_village()
    render_04_overworld_forest = lambda: render_overworld("forest", "04_overworld_forest.png", "【迷雾之森 · 幽暗古道】")
    render_04_overworld_forest()
    render_05_overworld_dungeon = lambda: render_overworld("dungeon", "05_overworld_dungeon.png", "【暗黑地牢 · 领主回廊】")
    render_05_overworld_dungeon()
    render_06_battle()
    render_07_inventory()
    render_08_dialogue()
    render_09_skilltree()
    render_10_saveload()
    render_11_settings()
    render_12_victory()
    render_13_gameover()
    print("=== All 13 Proof Screenshots Rendered to:", PROOF_DIR, "===")
