package com.potatoadventure.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

/**
 * 大地图环境粒子层（对应原型 {@code js/map.js} 的 "Weather particles"）。
 *
 * <p>原型的行为（每帧最多补 1 颗、总数上限 35、越界即回收）：
 *
 * <ul>
 *   <li><b>village</b>：落叶/花粉，从画面上方落下（{@code vy 1.0~2.5}），
 *       颜色 {@code #82d93e} 落叶绿 / {@code #ffd264} 花粉金，尺寸 2~5；</li>
 *   <li><b>forest</b>：发光孢子，从画面下方上飘（{@code vy -0.6~-1.8}），
 *       颜色 {@code #5dade2} / {@code #9b59b6}，尺寸 2~6；</li>
 *   <li><b>dungeon</b>：火星与烟，从画面底部上窜（{@code vy -1.0~-2.8}），
 *       颜色 {@code #e74c3c} / {@code #f39c12}，尺寸 1~4。</li>
 * </ul>
 *
 * <p>与原型的两点差异（都是有意的）：
 * <ol>
 *   <li>原型的速度是“每帧像素”，本类按秒计（{@code ×60}），因此在任何帧率下观感一致；</li>
 *   <li>本类用固定容量的并行数组 + 单个固定种子 {@link Random}，<b>每帧零分配</b>，
 *       也不会因为重新播种而抖动（对应 M1 的“不要逐帧随机闪烁”要求）。</li>
 * </ol>
 */
public final class WeatherParticles {

    /** 群系（决定粒子外观与运动方向）。 */
    public enum Biome {
        VILLAGE, FOREST, DUNGEON
    }

    /** 上限与原型一致：35 颗。 */
    private static final int MAX = 35;

    private static final Color[] VILLAGE_COLORS = {Color.web("#82D93E"), Color.web("#FFD264")};
    private static final Color[] FOREST_COLORS = {Color.web("#5DADE2"), Color.web("#9B59B6")};
    private static final Color[] DUNGEON_COLORS = {Color.web("#E74C3C"), Color.web("#F39C12")};

    private final double[] x = new double[MAX];
    private final double[] y = new double[MAX];
    private final double[] vx = new double[MAX];
    private final double[] vy = new double[MAX];
    private final double[] size = new double[MAX];
    private final Color[] color = new Color[MAX];

    private int count;

    /** 固定种子：同一台机器上每次启动的粒子轨迹一致，便于截图比对。 */
    private final Random rng = new Random(0x9E3779B97F4A7C15L);

    /**
     * 推进一帧。
     *
     * @param dt    本帧秒数
     * @param biome 当前群系；只影响新生成的粒子（已在飞的保持原样，与原型一致）
     */
    public void update(double dt, Biome biome) {
        if (dt <= 0) {
            return;
        }
        // 原型：每帧最多补 1 颗
        if (count < MAX) {
            spawn(biome);
        }

        double w = Assets.DESIGN_WIDTH;
        double h = Assets.DESIGN_HEIGHT;
        for (int i = count - 1; i >= 0; i--) {
            x[i] += vx[i] * dt;
            y[i] += vy[i] * dt;
            if (y[i] > h + 10 || y[i] < -20 || x[i] > w + 20 || x[i] < -20) {
                removeAt(i);
            }
        }
    }

    private void spawn(Biome biome) {
        int i = count++;
        switch (biome) {
            case FOREST -> {
                // 从画面底部上飘的发光孢子
                x[i] = rng.nextDouble() * Assets.DESIGN_WIDTH;
                y[i] = Assets.DESIGN_HEIGHT + 10;
                vx[i] = (rng.nextDouble() - 0.5) * 1.0 * 60;
                vy[i] = -(rng.nextDouble() * 1.2 + 0.6) * 60;
                size[i] = rng.nextDouble() * 4 + 2;
                color[i] = FOREST_COLORS[rng.nextBoolean() ? 0 : 1];
            }
            case DUNGEON -> {
                // 从画面底部上窜的火星
                x[i] = rng.nextDouble() * Assets.DESIGN_WIDTH;
                y[i] = 650;
                vx[i] = (rng.nextDouble() - 0.5) * 0.8 * 60;
                vy[i] = -(rng.nextDouble() * 1.8 + 1.0) * 60;
                size[i] = rng.nextDouble() * 3 + 1;
                color[i] = DUNGEON_COLORS[rng.nextBoolean() ? 0 : 1];
            }
            default -> {
                // 从画面上方飘落的落叶/花粉
                x[i] = rng.nextDouble() * Assets.DESIGN_WIDTH;
                y[i] = -10;
                vx[i] = (rng.nextDouble() * 1.5 + 0.5) * 60;
                vy[i] = (rng.nextDouble() * 1.5 + 1.0) * 60;
                size[i] = rng.nextDouble() * 3 + 2;
                color[i] = VILLAGE_COLORS[rng.nextBoolean() ? 0 : 1];
            }
        }
    }

    /** 尾部元素填补空洞（顺序无关，O(1) 且不分配）。 */
    private void removeAt(int i) {
        int last = --count;
        if (i != last) {
            x[i] = x[last];
            y[i] = y[last];
            vx[i] = vx[last];
            vy[i] = vy[last];
            size[i] = size[last];
            color[i] = color[last];
        }
    }

    /** 画在实体之上、HUD 之下（与原型绘制顺序一致）。 */
    public void render(GraphicsContext gc) {
        for (int i = 0; i < count; i++) {
            gc.setFill(color[i]);
            double s = Math.max(1, Math.round(size[i]));
            gc.fillRect(Math.round(x[i]), Math.round(y[i]), s, s);
        }
    }

    /** 清空（换图/读档时调用，避免旧粒子飘进新场景）。 */
    public void clear() {
        count = 0;
    }

    public int count() {
        return count;
    }
}
