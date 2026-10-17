package com.potatoadventure.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * 屏幕 10 / 11：通关结算与战败战报（原型 {@code #screen-victory} / {@code #screen-gameover}）。
 *
 * <p>几何取自 {@code css/style.css}：全屏背景铺满 → {@code .endgame-modal-box}
 * 居中 {@code 680} 宽、{@code rgba(18,24,32,0.94)} 底、4px 金/红描边、内边距 30；
 * 标题 28px；副标题 15px；{@code .endgame-stats-grid} 两列、间隔 12、{@code #121820} 底、
 * 2px {@code #2C3A47} 描边、内边距 15；按钮行居中、间隔 20。
 */
public final class ResultScreens {

    private ResultScreens() {
    }

    /**
     * 通关结算。
     *
     * @param stats 两列统计项（偶数个），形如 {@code {"最终通关等级", "Lv.5"}}
     */
    public static void renderVictory(GraphicsContext gc, String[][] stats, Runnable onReturn) {
        drawBackdrop(gc, "bg_victory.png", "#1A1208");
        double boxW = 680;
        double boxH = 330;
        double boxX = (Assets.DESIGN_WIDTH - boxW) / 2;
        double boxY = (Assets.DESIGN_HEIGHT - boxH) / 2;

        box(gc, boxX, boxY, boxW, boxH, Color.web("#FFD264"));

        double cursor = boxY + 30;
        UiKit.textCenter(gc, "★ 传 奇 土 豆 守 护 者 ★", Assets.DESIGN_WIDTH / 2.0, cursor + 24,
                Color.web("#FFD264"), 28);
        cursor += 46;
        UiKit.textCenter(gc, "荒野上的魔物已被全部击退！丰收的阳光再度普照整片土豆大陆！",
                Assets.DESIGN_WIDTH / 2.0, cursor + 14, Color.web("#F0F8FF"), 15);
        cursor += 32;

        drawStatsGrid(gc, boxX + 30, cursor, boxW - 60, stats);
        cursor += 74 + 20;

        double btnW = 340;
        double btnX = (Assets.DESIGN_WIDTH - btnW) / 2;
        boolean hover = UiKit.hot(btnX, cursor, btnW, 44, onReturn);
        UiKit.button(gc, btnX, cursor, btnW, 44, "[ 凯 旋 归 来 (返回主菜单) ]", hover, true, true);
    }

    /**
     * 战败战报。
     *
     * @param stats            两列统计项（偶数个）
     * @param onLoadLatest     读取最近存档
     * @param onReviveInVillage 重生于村庄
     * @param onReturn         返回主菜单
     */
    public static void renderGameOver(GraphicsContext gc, String[][] stats,
                                      Runnable onLoadLatest, Runnable onReviveInVillage, Runnable onReturn) {
        drawBackdrop(gc, "bg_game_over.png", "#160C0C");
        double boxW = 680;
        double boxH = 330;
        double boxX = (Assets.DESIGN_WIDTH - boxW) / 2;
        double boxY = (Assets.DESIGN_HEIGHT - boxH) / 2;

        box(gc, boxX, boxY, boxW, boxH, Color.web("#E74C3C"));

        double cursor = boxY + 30;
        UiKit.textCenter(gc, "☠ 胜 败 乃 兵 家 常 事 ☠", Assets.DESIGN_WIDTH / 2.0, cursor + 24,
                Color.web("#FF4757"), 28);
        cursor += 46;
        UiKit.textCenter(gc, "土豆勇士力竭倒在了暗影迷雾之中……",
                Assets.DESIGN_WIDTH / 2.0, cursor + 14, Color.web("#E0D0D0"), 15);
        cursor += 32;

        drawStatsGrid(gc, boxX + 30, cursor, boxW - 60, stats);
        cursor += 74 + 20;

        double[] widths = {230, 200, 190};
        String[] labels = {"[ 读取最近存档 ]", "[ 重生于村庄 ]", "[ 返回主菜单 ]"};
        Runnable[] actions = {onLoadLatest, onReviveInVillage, onReturn};
        double gap = 20;
        double total = 0;
        for (double w : widths) {
            total += w;
        }
        total += gap * (widths.length - 1);
        double x = (Assets.DESIGN_WIDTH - total) / 2;
        for (int i = 0; i < widths.length; i++) {
            boolean hover = UiKit.hot(x, cursor, widths[i], 44, actions[i]);
            UiKit.button(gc, x, cursor, widths[i], 44, labels[i], hover, i == 0, true);
            x += widths[i] + gap;
        }
    }

    private static void drawBackdrop(GraphicsContext gc, String background, String fallback) {
        Image image = Assets.background(background);
        if (image != null) {
            gc.drawImage(image, 0, 0, Assets.DESIGN_WIDTH, Assets.DESIGN_HEIGHT);
        } else {
            UiKit.fill(gc, Color.web(fallback), 0, 0, Assets.DESIGN_WIDTH, Assets.DESIGN_HEIGHT);
        }
    }

    private static void box(GraphicsContext gc, double x, double y, double w, double h, Color border) {
        UiKit.fill(gc, Color.rgb(18, 24, 32, 0.94), x, y, w, h);
        // 外发光（用两圈半透明描边近似 CSS box-shadow: 0 0 35px）
        UiKit.stroke(gc, border.deriveColor(0, 1, 1, 0.18), 10, x - 8, y - 8, w + 16, h + 16);
        UiKit.stroke(gc, border.deriveColor(0, 1, 1, 0.32), 6, x - 5, y - 5, w + 10, h + 10);
        UiKit.stroke(gc, border, 4, x, y, w, h);
    }

    private static void drawStatsGrid(GraphicsContext gc, double x, double y, double w, String[][] stats) {
        UiKit.fill(gc, Color.web("#121820"), x, y, w, 74);
        UiKit.stroke(gc, Color.web("#2C3A47"), 2, x, y, w, 74);
        double colW = (w - 30) / 2;
        for (int i = 0; i < stats.length; i++) {
            int col = i % 2;
            int row = i / 2;
            double tx = x + 15 + col * colW;
            double ty = y + 22 + row * 22;
            UiKit.text(gc, stats[i][0], tx, ty, Color.web("#CAD5E0"), 13);
            UiKit.text(gc, stats[i][1], tx + 160, ty, Color.web("#FFD264"), 13);
        }
    }
}
