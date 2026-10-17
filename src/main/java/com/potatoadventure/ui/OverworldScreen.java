package com.potatoadventure.ui;

import com.potatoadventure.model.Enemy;
import com.potatoadventure.model.GameMap;
import com.potatoadventure.model.Player;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * 屏幕 3：大地图探索 HUD（对应原型 {@code #screen-overworld} 的固定 UI 层）。
 *
 * <p>世界层（地形、实体）由引擎按摄像机裁剪绘制，本类只画画面上那一圈固定 UI，
 * 全部几何取自 {@code css/style.css}：
 *
 * <ul>
 *   <li>左上状态框 {@code top:15; left:15; 290×100}（头像格 {@code 74×80} 金边 +
 *       名字 14px + 三条 {@code 16px} 高血/蓝/经验条，条高与内边距都与 CSS 一致）；</li>
 *   <li>顶部区域横幅 {@code top:20} 居中，2px 金边；</li>
 *   <li>右上圆形雷达小地图 {@code top:15; right:15; 150×150}（半径裁切）；</li>
 *   <li>左下提示面板 {@code bottom:20; left:15; width:270}（标题条 {@code #2C3A47}）；</li>
 *   <li>底部六格快捷栏 {@code bottom:20} 居中，格子 {@code 50×50}、间隔 8、
 *       图标 {@code 32×32}、左上角键位 10px、右下角角标 10px；</li>
 *   <li>交互提示 {@code bottom:95} 居中。</li>
 * </ul>
 */
public final class OverworldScreen {

    private static final double MINIMAP_SIZE = 150;

    /**
     * 生物群系快捷选择栏（对应原型 {@code .hud-biome-switcher}：{@code top:125; left:15; gap:6}，
     * 按钮 {@code 12px} 字 + {@code 4px 10px} 内边距 + 2px 边框 → 72×25）。
     *
     * <p>M1 的地图只有一张 60×45 程序化地图，没有“切群系”玩法，所以这三个按钮只做
     * 视觉呈现（与原型一致的高亮态），不接受点击——点击后会让人以为能换地图。
     */
    private static final String[] BIOMES = {"平原村庄", "迷雾森林", "暗黑地牢"};
    private static final double BIOME_X = 15;
    private static final double BIOME_Y = 125;
    private static final double BIOME_W = 72;
    private static final double BIOME_H = 25;
    private static final double BIOME_GAP = 6;

    /** 委托面板（对应原型 {@code .hud-quest-panel}：{@code bottom:20; left:15; width:270}）。 */
    private static final double QUEST_W = 270;
    private static final double QUEST_HEADER_H = 26;
    private static final double QUEST_CONTENT_H = 82;

    /**
     * 委托文案：与原型 {@code #hud-quest-panel} 完全一致的三行。
     *
     * <p><b>目前是纯展示</b>：M1 没有任务系统，这三行不绑定任何真实进度；
     * 等 M3 做任务系统时再换成真实数据源（届时这里应改为从任务模型读取）。
     */
    private static final String[] QUEST_LINES = {
            "• 讨伐迷雾史莱姆 (2/3)",
            "• 与村长托马斯对话商讨对策",
            "• 探访铁匠雷恩打磨阔剑"
    };

    private OverworldScreen() {
    }

    /** 快捷栏一格的定义。 */
    private record Slot(String key, String icon, String badge, boolean enabled) {
    }

    private static final Slot[] SLOTS = {
            new Slot("空格", "items/item_sword.png", "普攻", true),
            new Slot("2", "items/item_potion_hp.png", "战斗", false),
            new Slot("2", "items/item_staff_fire.png", "战斗", false),
            new Slot("B", "items/item_potato_roast.png", "行囊", false),
            new Slot("K", "items/item_sword_epic.png", "技能", false),
            new Slot("P", "ui/stone_slate.png", "暂停", true)
    };

    /**
     * @param areaName       区域名（显示在顶部横幅）
     * @param banner         临时提示（保存/读档/逃跑等，非空时替换横幅内容）
     * @param interactPrompt 交互提示文案（为 {@code null} 时不显示）
     */
    public static void renderHud(GraphicsContext gc, Player player, GameMap map, List<Enemy> enemies,
                                 String areaName, String banner, String interactPrompt) {
        drawStatusFrame(gc, player);
        drawBiomeSwitcher(gc, areaName);
        drawBanner(gc, banner == null || banner.isEmpty() ? areaName : banner,
                banner != null && !banner.isEmpty());
        drawMinimap(gc, player, map, enemies);
        drawQuestPanel(gc);
        drawHotbar(gc, enemies);
        if (interactPrompt != null) {
            drawInteractPrompt(gc, interactPrompt);
        }
    }

    // ---------------------------------------------------------------- 群系切换栏

    /**
     * 三枚群系按钮；高亮态按区域名关键字推断（找不到关键字时高亮第一枚，与原型默认一致）。
     */
    private static void drawBiomeSwitcher(GraphicsContext gc, String areaName) {
        int active = 0;
        if (areaName != null) {
            if (areaName.contains("森林") || areaName.contains("迷雾")) {
                active = 1;
            } else if (areaName.contains("地牢") || areaName.contains("暗黑")) {
                active = 2;
            }
        }

        for (int i = 0; i < BIOMES.length; i++) {
            double bx = BIOME_X + i * (BIOME_W + BIOME_GAP);
            boolean on = i == active;

            // .hud-biome-btn：rgba(26,34,42,.9) 底 + 2px #788796 边；active/hover 为
            // rgba(45,58,71,.95) 底 + #FFD264 边与字
            UiKit.fill(gc, on ? Color.rgb(45, 58, 71, 0.95) : Color.rgb(26, 34, 42, 0.9),
                    bx, BIOME_Y, BIOME_W, BIOME_H);
            UiKit.stroke(gc, on ? Color.web("#FFD264") : Color.web("#788796"), 2,
                    bx, BIOME_Y, BIOME_W, BIOME_H);
            UiKit.textCenter(gc, BIOMES[i], bx + BIOME_W / 2.0, BIOME_Y + 17,
                    on ? Color.web("#FFD264") : Color.web("#D6E0EA"), 12);
        }
    }

    // ---------------------------------------------------------------- 状态框

    private static void drawStatusFrame(GraphicsContext gc, Player player) {
        double x = 15;
        double y = 15;

        UiKit.glassPanel(gc, Color.rgb(26, 34, 42, 0.92), x, y, 290, 100,
                Color.web("#14100C"), Color.web("#788796"), Color.web("#1E232D"));

        // 头像格 74×80（金边），内含 64×64 职业头像精灵
        double avatarX = x + 8 + 1;
        double avatarY = y + 8;
        UiKit.fill(gc, Color.web("#0F141A"), avatarX, avatarY, 74, 80);
        UiKit.stroke(gc, Color.web("#D6A848"), 2, avatarX, avatarY, 74, 80);
        Image portrait = Assets.sprite("hero_" + player.getHeroClass() + ".png");
        if (portrait != null) {
            gc.drawImage(portrait, 0, 0, portrait.getWidth(), portrait.getHeight(),
                    avatarX + 5, avatarY + 8, 64, 64);
        }

        double colX = avatarX + 74 + 10;
        double colW = 290 - (colX - x) - 8;

        // 名字行（14px 金 + 右侧小字 11px）
        UiKit.text(gc, player.getName() + " Lv." + player.getLevel(), colX, y + 8 + 14,
                Color.web("#FFD264"), 14);
        UiKit.text(gc, "土豆阶", colX + colW - 34, y + 8 + 14, Color.web("#A0B0C0"), 11);

        double barY = y + 8 + 18;
        drawHudBar(gc, colX, barY, colW, 16, player.getHp(), player.getMaxHp(),
                Color.web("#FF6B5B"), Color.web("#C0392B"), "HP", "ui/heart_crystal.png");
        drawHudBar(gc, colX, barY + 21, colW, 16, player.getMp(), player.getMaxMp(),
                Color.web("#5DADE2"), Color.web("#2980B9"), "MP", "ui/mana_crystal.png");
        drawHudBar(gc, colX, barY + 42, colW, 16, player.getExp(), player.getExpToNextLevel(),
                Color.web("#F7DC6F"), Color.web("#D4AC0D"), "EXP", "ui/gold_coin.png");
    }

    private static void drawHudBar(GraphicsContext gc, double x, double y, double w, double h,
                                   int value, int max, Color top, Color bottom,
                                   String label, String iconPath) {
        double ratio = max <= 0 ? 0 : Math.min(1.0, (double) value / max);
        UiKit.bar(gc, x, y, w, h, ratio, top, bottom, Color.web("#282014"));

        Image icon = Assets.ui(iconPath.substring(iconPath.indexOf('/') + 1));
        double textX = x + 3;
        if (icon != null) {
            gc.drawImage(icon, 0, 0, icon.getWidth(), icon.getHeight(), x + 2, y + 1, 12, 12);
            textX = x + 16;
        }
        UiKit.text(gc, label + " " + value + " / " + max, textX, y + h - 4,
                Color.WHITE, 10, Palette.TEXT_SHADOW, true);
    }

    // ---------------------------------------------------------------- 横幅

    /**
     * 顶部区域横幅：{@code top:20} 居中，{@code padding:6px 24px} + 2px 金边 + 16px 粗体，
     * 因此高度固定 32px（量得 20..51），宽度随文案长度变化。
     */
    private static void drawBanner(GraphicsContext gc, String text, boolean highlight) {
        double textW = 0;
        for (int i = 0; i < text.length(); i++) {
            // Unifont 等宽：中文/全角 1em，ASCII 半角
            textW += text.charAt(i) < 0x2000 ? 8 : 16;
        }
        double w = Math.max(200, textW + 48 + 4);
        double x = Math.floor((Assets.DESIGN_WIDTH - w) / 2);
        double y = 20;
        double h = 32;

        UiKit.fill(gc, Color.rgb(18, 24, 30, 0.88), x, y, w, h);
        UiKit.stroke(gc, highlight ? Color.web("#FFD264") : Color.web("#D6A848"), 2, x, y, w, h);
        UiKit.textCenter(gc, text, x + w / 2.0, y + 21,
                highlight ? Color.web("#FFFDE6") : Color.web("#FFD264"), 16);
    }

    // ---------------------------------------------------------------- 小地图

    private static void drawMinimap(GraphicsContext gc, Player player, GameMap map, List<Enemy> enemies) {
        double size = MINIMAP_SIZE;
        double x = Assets.DESIGN_WIDTH - 15 - size;
        double y = 15;
        double cx = x + size / 2;
        double cy = y + size / 2;
        double radius = size / 2 - 3;

        UiKit.fill(gc, Color.web("#0F141A"), x, y, size, size);

        double scale = size / (double) Math.max(map.getPixelWidth(), map.getPixelHeight());
        double mapW = map.getPixelWidth() * scale;
        double mapH = map.getPixelHeight() * scale;
        double originX = x + (size - mapW) / 2;
        double originY = y + (size - mapH) / 2;

        // 圆形裁切（对应 CSS border-radius:50% + overflow:hidden）
        gc.save();
        gc.beginPath();
        gc.arc(cx, cy, radius, radius, 0, 360);
        gc.closePath();
        gc.clip();

        double cell = GameMap.TILE_SIZE * scale;
        for (int tx = 0; tx < map.getWidth(); tx++) {
            for (int ty = 0; ty < map.getHeight(); ty++) {
                Color color = switch (map.getTile(tx, ty)) {
                    case GameMap.TILE_WALL -> Color.web("#4B5A69");
                    case GameMap.TILE_TREE -> Color.web("#385E38");
                    case GameMap.TILE_WATER -> Color.web("#2980B9");
                    default -> Color.web("#557A46");
                };
                gc.setFill(color);
                gc.fillRect(Math.floor(originX + tx * cell), Math.floor(originY + ty * cell),
                        Math.ceil(cell), Math.ceil(cell));
            }
        }

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) {
                continue;
            }
            gc.setFill(Color.web("#E74C3C"));
            double ex = Math.floor(originX + enemy.getX() * cell);
            double ey = Math.floor(originY + enemy.getY() * cell);
            gc.fillRect(ex, ey, 3, 3);
        }

        gc.setFill(Color.web("#FFD264"));
        double px = Math.floor(originX + player.getX() * cell);
        double py = Math.floor(originY + player.getY() * cell);
        gc.fillRect(px - 1, py - 1, 4, 4);
        gc.setFill(Color.web("#FFFDE6"));
        gc.fillRect(px, py, 2, 2);

        gc.restore();

        // 金边 + 内阴影（裁切之后再描边，边缘干净）
        UiKit.stroke(gc, Color.web("#D6A848"), 3, x, y, size, size);
        UiKit.stroke(gc, Color.web("#8FA0AD"), 1, x + 3, y + 3, size - 6, size - 6);
    }

    // ---------------------------------------------------------------- 委托面板

    /**
     * 左下委托面板：头部 26px（{@code #2C3A47} 底 + 14px 金字 + 右侧 11px 折叠提示），
     * 内容区 82px（10px 上下内边距 + 3 行 13px、行高 20.8 → 基线间距 21）。
     *
     * <p>面板总高 2+26+82+2 = 112，与原型截图量得的 588..699 完全对齐。
     */
    private static void drawQuestPanel(GraphicsContext gc) {
        double x = 15;
        double w = QUEST_W;
        double h = QUEST_HEADER_H + QUEST_CONTENT_H + 4;
        double y = Assets.DESIGN_HEIGHT - 20 - h;

        UiKit.fill(gc, Color.rgb(22, 28, 36, 0.92), x, y, w, h);
        UiKit.stroke(gc, Color.web("#4A5C6D"), 2, x, y, w, h);

        UiKit.fill(gc, Color.web("#2C3A47"), x + 2, y + 2, w - 4, QUEST_HEADER_H);
        UiKit.text(gc, "当前委托 (Q)", x + 12, y + 2 + 17, Color.web("#FFD264"), 14);

        String fold = "[折叠/展开]";
        UiKit.text(gc, fold, x + w - 14 - fold.length() * 6, y + 2 + 17,
                Color.web("#D6E0EA"), 11);

        double lineY = y + 2 + QUEST_HEADER_H + 10 + 16;
        for (String line : QUEST_LINES) {
            UiKit.text(gc, line, x + 12, lineY, Color.web("#CAD5E0"), 13);
            lineY += 21;
        }
    }

    // ---------------------------------------------------------------- 快捷栏

    private static void drawHotbar(GraphicsContext gc, List<Enemy> enemies) {
        int live = 0;
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                live++;
            }
        }

        double slot = 50;
        double gap = 8;
        double padX = 10;
        double padY = 6;
        double barW = SLOTS.length * slot + (SLOTS.length - 1) * gap + padX * 2;
        double barH = slot + padY * 2;
        double x = (Assets.DESIGN_WIDTH - barW) / 2;
        double y = Assets.DESIGN_HEIGHT - 20 - barH;

        UiKit.fill(gc, Color.rgb(20, 25, 32, 0.95), x, y, barW, barH);
        UiKit.stroke(gc, Color.web("#14100C"), 3, x, y, barW, barH);
        UiKit.fill(gc, Color.web("#788796"), x + 3, y + 3, barW - 6, 1);
        UiKit.fill(gc, Color.web("#788796"), x + 3, y + barH - 4, barW - 6, 1);

        for (int i = 0; i < SLOTS.length; i++) {
            Slot s = SLOTS[i];
            double sx = x + padX + i * (slot + gap);
            double sy = y + padY;

            UiKit.fill(gc, Color.web("#10141A"), sx, sy, slot, slot);
            UiKit.stroke(gc, s.enabled() ? Color.web("#5A6E82") : Color.web("#31404E"), 2, sx, sy, slot, slot);

            Image icon = Assets.img(s.icon());
            if (icon != null) {
                gc.drawImage(icon, 0, 0, icon.getWidth(), icon.getHeight(),
                        sx + 9, sy + 9, 32, 32);
            }
            UiKit.text(gc, "[" + s.key() + "]", sx + 3, sy + 13, Color.web("#FFD264"), 10);

            String badge = s.badge();
            if (i == 4 && live > 0) {
                badge = "×" + live;
            }
            UiKit.text(gc, badge, sx + slot - 3 - badge.length() * 9, sy + slot - 3,
                    s.enabled() ? Color.WHITE : Color.web("#8FA0AD"), 10);
        }
    }

    private static void drawInteractPrompt(GraphicsContext gc, String text) {
        double w = Math.max(120, text.length() * 14.0 + 36);
        double h = 32;
        double x = (Assets.DESIGN_WIDTH - w) / 2;
        double y = Assets.DESIGN_HEIGHT - 95 - h;

        UiKit.fill(gc, Color.rgb(20, 25, 32, 0.9), x, y, w, h);
        UiKit.stroke(gc, Color.web("#FFD264"), 2, x, y, w, h);
        UiKit.textCenter(gc, text, Assets.DESIGN_WIDTH / 2.0, UiKit.baseline(y, h, 14),
                Color.WHITE, 14);
    }
}
