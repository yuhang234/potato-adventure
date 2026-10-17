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
        drawBanner(gc, banner == null || banner.isEmpty() ? areaName : banner,
                banner != null && !banner.isEmpty());
        drawMinimap(gc, player, map, enemies);
        drawHintPanel(gc);
        drawHotbar(gc, enemies);
        if (interactPrompt != null) {
            drawInteractPrompt(gc, interactPrompt);
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

    private static void drawBanner(GraphicsContext gc, String text, boolean highlight) {
        double textW = text.length() * 16.0;
        double w = Math.max(220, textW + 48);
        double x = (Assets.DESIGN_WIDTH - w) / 2;
        double y = 20;
        double h = 34;

        UiKit.fill(gc, Color.rgb(18, 24, 30, 0.88), x, y, w, h);
        UiKit.stroke(gc, highlight ? Color.web("#FFD264") : Color.web("#D6A848"), 2, x, y, w, h);
        UiKit.textCenter(gc, text, Assets.DESIGN_WIDTH / 2.0, UiKit.baseline(y, h, 16),
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

    // ---------------------------------------------------------------- 提示面板

    private static void drawHintPanel(GraphicsContext gc) {
        double x = 15;
        double w = 270;
        double h = 108;
        double y = Assets.DESIGN_HEIGHT - 20 - h;

        UiKit.fill(gc, Color.rgb(22, 28, 36, 0.92), x, y, w, h);
        UiKit.stroke(gc, Color.web("#4A5C6D"), 2, x, y, w, h);

        UiKit.fill(gc, Color.web("#2C3A47"), x + 2, y + 2, w - 4, 28);
        UiKit.text(gc, "操作提示 (Q)", x + 12, y + 2 + 20, Color.web("#FFD264"), 14);
        UiKit.text(gc, "[折叠/展开]", x + w - 82, y + 2 + 20, Color.web("#A0B0C0"), 11);

        double line = y + 46;
        UiKit.text(gc, "• WASD / 方向键 移动，空格 近身攻击", x + 12, line, Color.web("#CAD5E0"), 13);
        UiKit.text(gc, "• 走上敌人所在格 进入回合制战斗", x + 12, line + 20, Color.web("#CAD5E0"), 13);
        UiKit.text(gc, "• F5 保存 / F9 读取 / P 暂停 / F3 调试", x + 12, line + 40,
                Color.web("#CAD5E0"), 13);
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
