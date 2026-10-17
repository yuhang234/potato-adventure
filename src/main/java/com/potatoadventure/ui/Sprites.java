package com.potatoadventure.ui;

import com.potatoadventure.model.Enemy;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * 实体精灵绘制 —— 统一把 {@code design/ui-prototype/assets/sprites} 下的已批准像素图
 * 按“脚底对齐格子底边、水平居中对齐格子中线”的规则贴到世界坐标上。
 *
 * <p>尺寸规则（相对 32px 地块）：英雄 1.5 格高、小怪 1.25 格、野狼等比放宽、NPC 1.5 格。
 * 精灵一律用最近邻放大（{@code gc.setImageSmoothing(false)}，由引擎在主循环里设定），
 * 因此不会出现插值糊边。资产缺失时退化为同色占位方块，保证不崩且位置可读。
 */
public final class Sprites {

    /** 英雄贴图绘制边长（像素）。 */
    public static final double HERO_SIZE = 48;
    /** 小怪贴图绘制高度。 */
    public static final double ENEMY_SIZE = 40;
    /** NPC 贴图绘制边长。 */
    public static final double NPC_SIZE = 48;

    private Sprites() {
    }

    /** 落地阴影（椭圆，半透明黑）。 */
    public static void shadow(GraphicsContext gc, double centerX, double bottomY, double width) {
        gc.setFill(Color.rgb(0, 0, 0, 0.28));
        gc.fillOval(centerX - width / 2, bottomY - 5, width, 6);
    }

    /**
     * 英雄精灵。
     *
     * @param centerX   格子中线像素 X（世界坐标 - 摄像机偏移）
     * @param tileBottom 格子底边像素 Y
     * @param heroClass  {@code berserker} / {@code mage} / {@code ranger}
     */
    public static void hero(GraphicsContext gc, double centerX, double tileBottom, String heroClass) {
        Image image = Assets.sprite("hero_" + heroClass + ".png");
        double size = HERO_SIZE;
        shadow(gc, centerX, tileBottom, size * 0.62);
        if (image == null) {
            placeholder(gc, Color.web("#D2B48C"), centerX, tileBottom, size * 0.72, size);
            return;
        }
        gc.drawImage(image, 0, 0, image.getWidth(), image.getHeight(),
                centerX - size / 2, tileBottom - size + 2, size, size);
    }

    /** 敌人精灵（按类型选图，野狼保持 3:2 横宽）。 */
    public static void enemy(GraphicsContext gc, Enemy.EnemyType type, double centerX, double tileBottom) {
        String file;
        double w = ENEMY_SIZE;
        double h = ENEMY_SIZE;
        switch (type) {
            case GOBLIN -> file = "enemy_goblin.png";
            case WOLF -> {
                file = "enemy_wolf.png";
                w = 52;
                h = 35;
            }
            default -> file = "enemy_slime.png";
        }
        Image image = Assets.sprite(file);
        shadow(gc, centerX, tileBottom, w * 0.66);
        if (image == null) {
            placeholder(gc, type.getColor(), centerX, tileBottom, w * 0.7, h);
            return;
        }
        gc.drawImage(image, 0, 0, image.getWidth(), image.getHeight(),
                centerX - w / 2, tileBottom - h + 2, w, h);
    }

    /** 战斗舞台上的放大精灵（100×100 展示框，等比贴合）。 */
    public static void battleSprite(GraphicsContext gc, Image image, double centerX, double centerY, double box) {
        if (image == null) {
            return;
        }
        gc.drawImage(image, 0, 0, image.getWidth(), image.getHeight(),
                centerX - box / 2, centerY - box / 2, box, box);
    }

    /** NPC 精灵（村长托马斯 / 铁匠雷恩）。 */
    public static void npc(GraphicsContext gc, String npcName, double centerX, double tileBottom) {
        Image image = Assets.sprite(npcName);
        shadow(gc, centerX, tileBottom, NPC_SIZE * 0.62);
        if (image == null) {
            placeholder(gc, Color.web("#C3AA78"), centerX, tileBottom, NPC_SIZE * 0.72, NPC_SIZE);
            return;
        }
        gc.drawImage(image, 0, 0, image.getWidth(), image.getHeight(),
                centerX - NPC_SIZE / 2, tileBottom - NPC_SIZE + 2, NPC_SIZE, NPC_SIZE);
    }

    private static void placeholder(GraphicsContext gc, Color color, double centerX, double bottomY,
                                    double width, double height) {
        gc.setFill(color);
        gc.fillRect(centerX - width / 2, bottomY - height + 2, width, height);
        gc.setStroke(Palette.FRAME_SHADOW);
        gc.setLineWidth(1);
        gc.strokeRect(centerX - width / 2, bottomY - height + 2, width, height);
    }
}
