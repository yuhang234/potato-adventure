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

    /** 弹窗几何（与 CSS {@code .endgame-modal-box} 一致）。 */
    private static final double BOX_W = 680;
    private static final double BORDER = 4;
    private static final double PAD = 30;
    /** {@code .endgame-stats-grid} 量得高度 70（2px 边 + 15 内边距 + 2 行）。 */
    private static final double GRID_H = 70;
    /** 13px 文字在网格里的两行基线偏移（原型量得 +29 / +54）。 */
    private static final double GRID_ROW1 = 29;
    private static final double GRID_ROW2 = 54;
    /** 12px 说明文字的行高（{@code line-height:1.6} → 19.2）。 */
    private static final double NOTE_LINE = 19.2;

    private ResultScreens() {
    }

    /**
     * 通关结算。
     *
     * <p>垂直排布严格按原型：4px 边 + 30 内边距 → 标题 28/下边距 16 → 副标题 15/下边距 15 →
     * 统计网格 70/下边距 20 → 12px 说明两行（行高 19.2）/下边距 20 → 按钮 44。
     * 合计内容 266.4 → 弹窗总高 334.4，与原型量得的 193..526（334）一致。
     *
     * @param stats 两列统计项（偶数个），形如 {@code {"最终通关等级", "Lv.5"}}
     */
    public static void renderVictory(GraphicsContext gc, String[][] stats, Runnable onReturn) {
        drawBackdrop(gc, "bg_victory.png", "#1A1208");

        double contentH = 28 + 16 + 15 + 15 + GRID_H + 20 + NOTE_LINE * 2 + 20 + 44;
        double boxH = BORDER * 2 + PAD * 2 + contentH;
        double boxX = (Assets.DESIGN_WIDTH - BOX_W) / 2;
        double boxY = Math.round((Assets.DESIGN_HEIGHT - boxH) / 2);

        box(gc, boxX, boxY, BOX_W, boxH, Color.web("#FFD264"));

        double y = boxY + BORDER + PAD;
        UiKit.textCenter(gc, "★ 传 奇 土 豆 守 护 者 ★", Assets.DESIGN_WIDTH / 2.0, y + 29,
                Color.web("#FFD264"), 28);
        y += 28 + 16;
        UiKit.textCenter(gc, "暗黑土豆领主已被净化！丰收阳光再度普照整片大陆！",
                Assets.DESIGN_WIDTH / 2.0, y + 15, Color.web("#F0F8FF"), 15);
        y += 15 + 15;

        drawStatsGrid(gc, boxX + BORDER + PAD, y, BOX_W - BORDER * 2 - PAD * 2, stats);
        y += GRID_H + 20;

        UiKit.textCenter(gc, "感谢体验《土豆大冒险》！",
                Assets.DESIGN_WIDTH / 2.0, y + 14, Color.web("#A0B0C0"), 12);
        y += NOTE_LINE;
        UiKit.textCenter(gc, "架构：Java 21 + JavaFX 21 | 美术：泰拉瑞亚像素风",
                Assets.DESIGN_WIDTH / 2.0, y + 14, Color.web("#A0B0C0"), 12);
        y += NOTE_LINE + 20;

        double btnW = 340;
        double btnX = (Assets.DESIGN_WIDTH - btnW) / 2;
        boolean hover = UiKit.hot(btnX, y, btnW, 44, onReturn);
        UiKit.button(gc, btnX, y, btnW, 44, "[ 凯 旋 归 来 (返回主菜单) ]", hover, true, true);
    }

    /**
     * 战败战报。
     *
     * <p>与通关结算同样的排布，但<b>没有</b> 12px 说明段落，所以总高
     * 208 + 68 = 276，与原型量得的 222..497（276）一致。
     *
     * @param stats            两列统计项（偶数个）
     * @param onLoadLatest     读取最近存档
     * @param onReviveInVillage 重生于村庄
     * @param onReturn         返回主菜单
     */
    public static void renderGameOver(GraphicsContext gc, String[][] stats,
                                      Runnable onLoadLatest, Runnable onReviveInVillage, Runnable onReturn) {
        drawBackdrop(gc, "bg_game_over.png", "#160C0C");

        double contentH = 28 + 16 + 15 + 15 + GRID_H + 20 + 44;
        double boxH = BORDER * 2 + PAD * 2 + contentH;
        double boxX = (Assets.DESIGN_WIDTH - BOX_W) / 2;
        double boxY = Math.round((Assets.DESIGN_HEIGHT - boxH) / 2);

        box(gc, boxX, boxY, BOX_W, boxH, Color.web("#E74C3C"));

        double y = boxY + BORDER + PAD;
        UiKit.textCenter(gc, "☠ 胜 败 乃 兵 家 常 事 ☠", Assets.DESIGN_WIDTH / 2.0, y + 29,
                Color.web("#FF4757"), 28);
        y += 28 + 16;
        UiKit.textCenter(gc, "土豆勇士力竭倒在了暗影迷雾之中……",
                Assets.DESIGN_WIDTH / 2.0, y + 15, Color.web("#E0D0D0"), 15);
        y += 15 + 15;

        drawStatsGrid(gc, boxX + BORDER + PAD, y, BOX_W - BORDER * 2 - PAD * 2, stats);
        y += GRID_H + 20;

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
            boolean hover = UiKit.hot(x, y, widths[i], 44, actions[i]);
            UiKit.button(gc, x, y, widths[i], 44, labels[i], hover, i == 0, true);
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

    /**
     * 统计网格：{@code #121820} 底 + 2px {@code #2C3A47} 边 + 15 内边距，两列、行距按原型量得
     * （两行基线 +29 / +54，列宽 {@code (w-30)/2}）。
     */
    private static void drawStatsGrid(GraphicsContext gc, double x, double y, double w, String[][] stats) {
        UiKit.fill(gc, Color.web("#121820"), x, y, w, GRID_H);
        UiKit.stroke(gc, Color.web("#2C3A47"), 2, x, y, w, GRID_H);
        double colW = (w - 30) / 2;
        for (int i = 0; i < stats.length; i++) {
            int col = i % 2;
            int row = i / 2;
            double tx = x + 15 + col * colW;
            double ty = y + (row == 0 ? GRID_ROW1 : GRID_ROW2);
            UiKit.text(gc, stats[i][0], tx, ty, Color.web("#CAD5E0"), 13);
            UiKit.text(gc, stats[i][1], tx + 160, ty, Color.web("#FFD264"), 13);
        }
    }
}
