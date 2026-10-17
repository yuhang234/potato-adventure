package com.potatoadventure.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

/**
 * 像素 UI 工具箱 —— 把设计原型 {@code css/style.css} 里的视觉规则翻译成 JavaFX Canvas 绘制调用。
 *
 * <p>对应关系：
 *
 * <ul>
 *   <li>{@code .pixel-panel-wood / -stone / -parchment} → {@link #panel}：贴图平铺 + 4px 描边 +
 *       内 2px 明暗倒角；</li>
 *   <li>{@code .pixel-btn / .btn-gold / .btn-red} → {@link #button}：三段渐层 + 内倒角 + 文字投影；</li>
 *   <li>{@code .hud-bar-container / .fighter-hp-bar} → {@link #bar}：深底槽 + 渐层填充 + 描边；</li>
 *   <li>{@code text-shadow: 1px 1px 0 #000} → 所有文字都先画一遍偏移 1px 的暗色拷贝。</li>
 * </ul>
 *
 * <p>同时承担“这一帧哪些矩形是可点的”登记工作：屏幕在渲染时用 {@link #hot} 登记热区，
 * 引擎把鼠标事件交给 {@link #dispatchClick} / {@link #pointer} 即可，无需为每个按钮建 Node，
 * 渲染与命中判定天然来自同一组几何数字。
 *
 * <p>只在 JavaFX 应用线程使用（引擎主循环与鼠标事件都在该线程）。
 */
public final class UiKit {

    /** 一帧内登记的可点区域。 */
    private record Hot(double x, double y, double w, double h, Runnable action) {
        boolean contains(double px, double py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }
    }

    private static final List<Hot> HOTS = new ArrayList<>();

    private static double pointerX = -1;
    private static double pointerY = -1;
    private static boolean pointerTracked;

    private UiKit() {
    }

    // ------------------------------------------------------------------ 交互登记

    /** 每帧渲染开始前清空上一帧的热区。 */
    public static void beginFrame() {
        HOTS.clear();
    }

    /** 更新指针位置（鼠标在窗口内移动时调用；{@code tracked=false} 表示指针已离开）。 */
    public static void pointer(double x, double y, boolean tracked) {
        pointerX = x;
        pointerY = y;
        pointerTracked = tracked;
    }

    /** 指针当前位置（用于悬停绘制）。 */
    public static double pointerX() {
        return pointerX;
    }

    public static double pointerY() {
        return pointerY;
    }

    /**
     * 登记一个可点矩形。
     *
     * @return 指针当前是否悬停在该矩形内（渲染时用来画 hover 态）
     */
    public static boolean hot(double x, double y, double w, double h, Runnable action) {
        HOTS.add(new Hot(x, y, w, h, action));
        return pointerTracked && pointerX >= x && pointerX < x + w && pointerY >= y && pointerY < y + h;
    }

    /**
     * 把一次鼠标点击派发给命中的热区。
     *
     * @return 是否命中了某个热区
     */
    public static boolean dispatchClick(double x, double y) {
        // 逆序：后登记的（更上层）优先
        for (int i = HOTS.size() - 1; i >= 0; i--) {
            Hot hot = HOTS.get(i);
            if (hot.contains(x, y)) {
                hot.action().run();
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ 基础绘制

    public static void fill(GraphicsContext gc, Color color, double x, double y, double w, double h) {
        gc.setFill(color);
        gc.fillRect(x, y, w, h);
    }

    public static void stroke(GraphicsContext gc, Color color, double width,
                              double x, double y, double w, double h) {
        gc.setStroke(color);
        gc.setLineWidth(width);
        gc.strokeRect(x + width / 2, y + width / 2, w - width, h - width);
        gc.setLineWidth(1);
    }

    /**
     * 贴图平铺（等价于 CSS {@code background: url(...) repeat}）。
     */
    public static void tiled(GraphicsContext gc, Image texture, double x, double y, double w, double h,
                             double tile) {
        if (texture == null || w <= 0 || h <= 0) {
            return;
        }
        for (double dy = 0; dy < h; dy += tile) {
            for (double dx = 0; dx < w; dx += tile) {
                double tw = Math.min(tile, w - dx);
                double th = Math.min(tile, h - dy);
                gc.drawImage(texture, 0, 0, tw, th, x + dx, y + dy, tw, th);
            }
        }
    }

    /**
     * 像素面板：贴图底 + 外描边 + 内倒角，等价于 {@code .pixel-panel-*} 与
     * {@code .hud-status-frame / .battle-console-deck} 的边框写法。
     */
    public static void panel(GraphicsContext gc, Image texture,
                             double x, double y, double w, double h,
                             Color border, Color bevelLight, Color bevelDark) {
        if (texture != null) {
            tiled(gc, texture, x, y, w, h, texture.getWidth());
        }
        bevel(gc, x, y, w, h, border, bevelLight, bevelDark, 2);
    }

    /** 描边 + 内 2px 倒角（左上受光、右下暗）。 */
    public static void bevel(GraphicsContext gc, double x, double y, double w, double h,
                             Color border, Color light, Color dark, double inset) {
        stroke(gc, border, 3, x, y, w, h);
        if (light != null) {
            fill(gc, light, x + 3, y + 3, w - 6, inset);
            fill(gc, light, x + 3, y + 3, inset, h - 6);
        }
        if (dark != null) {
            fill(gc, dark, x + 3, y + h - 3 - inset, w - 6, inset);
            fill(gc, dark, x + w - 3 - inset, y + 3, inset, h - 6);
        }
    }

    /** 半透明深色面板（{@code rgba(26,34,42,0.92)} 这一类的写法）。 */
    public static void glassPanel(GraphicsContext gc, Color fillColor,
                                  double x, double y, double w, double h,
                                  Color border, Color light, Color dark) {
        fill(gc, fillColor, x, y, w, h);
        bevel(gc, x, y, w, h, border, light, dark, 2);
    }

    // ------------------------------------------------------------------ 文本

    /** 带 1px 投影的文字（CSS {@code text-shadow: 1px 1px 0 #000}）。 */
    public static void text(GraphicsContext gc, String value, double x, double baseline,
                            Color color, int size) {
        text(gc, value, x, baseline, color, size, Palette.TEXT_SHADOW, true);
    }

    public static void text(GraphicsContext gc, String value, double x, double baseline,
                            Color color, int size, Color shadow, boolean shadowed) {
        gc.setFontSmoothingType(FontSmoothingType.GRAY);
        gc.setFont(Assets.font(size));
        gc.setTextAlign(TextAlignment.LEFT);
        if (shadowed) {
            gc.setFill(shadow);
            gc.fillText(value, x + 1, baseline + 1);
        }
        gc.setFill(color);
        gc.fillText(value, x, baseline);
    }

    /** 居中文字。 */
    public static void textCenter(GraphicsContext gc, String value, double centerX, double baseline,
                                  Color color, int size) {
        textCenter(gc, value, centerX, baseline, color, size, Palette.TEXT_SHADOW, true);
    }

    public static void textCenter(GraphicsContext gc, String value, double centerX, double baseline,
                                  Color color, int size, Color shadow, boolean shadowed) {
        gc.setFontSmoothingType(FontSmoothingType.GRAY);
        gc.setFont(Assets.font(size));
        gc.setTextAlign(TextAlignment.CENTER);
        if (shadowed) {
            gc.setFill(shadow);
            gc.fillText(value, centerX + 1, baseline + 1);
        }
        gc.setFill(color);
        gc.fillText(value, centerX, baseline);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    /** 竖直居中于 {@code [y, y+h]} 的文本基线。 */
    public static double baseline(double y, double h, int size) {
        return y + h / 2 + size * 0.36;
    }

    /** 依据宽度估算字符数上限的截断（Unifont 为等宽字体，中文按 1 个字符宽）。 */
    public static String ellipsize(String value, int maxChars) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxChars) {
            return value;
        }
        return value.substring(0, Math.max(0, maxChars - 1)) + "…";
    }

    // ------------------------------------------------------------------ 控件

    /** 进度条：深底槽 + 竖向渐层填充 + 描边（{@code .hud-bar-*} / {@code .fighter-hp-bar}）。 */
    public static void bar(GraphicsContext gc, double x, double y, double w, double h, double ratio,
                           Color top, Color bottom, Color border) {
        fill(gc, Color.web("#14100C"), x, y, w, h);
        double clamped = Math.max(0, Math.min(1, ratio));
        double fillW = w * clamped;
        if (fillW > 0) {
            for (int i = 0; i < (int) Math.round(fillW); i++) {
                double t = fillW <= 1 ? 0 : i / (fillW - 1.0);
                gc.setFill(top.interpolate(bottom, t));
                gc.fillRect(x + i, y + 1, 1, h - 2);
            }
        }
        stroke(gc, border, 2, x, y, w, h);
    }

    /** 木质像素按钮（{@code .pixel-btn}）。 */
    public static void button(GraphicsContext gc, double x, double y, double w, double h, String label,
                              boolean hovered, boolean gold) {
        button(gc, x, y, w, h, label, hovered, gold, true);
    }

    /** 木质像素按钮（{@code .pixel-btn}）。 */
    public static void button(GraphicsContext gc, double x, double y, double w, double h, String label,
                              boolean hovered, boolean gold, boolean enabled) {
        if (!enabled) {
            fill(gc, Color.web("#2A2018"), x, y, w, h);
            stroke(gc, Color.web("#14100C"), 3, x, y, w, h);
            textCenter(gc, label, x + w / 2, baseline(y, h, 16), Color.web("#6A6156"), 16);
            return;
        }
        if (gold) {
            gradientColumn(gc, x, y, w, h, hovered ? Color.web("#FFD264") : Color.web("#E6B84C"),
                    hovered ? Color.web("#BE9132") : Color.web("#A07828"));
            textCenter(gc, label, x + w / 2, baseline(y, h, 18), Color.web("#1A1405"), 18);
        } else {
            gradientColumn(gc, x, y, w, h, hovered ? Color.web("#B47D4B") : Color.web("#96643C"),
                    hovered ? Color.web("#7D4B2D") : Color.web("#643C23"));
            textCenter(gc, label, x + w / 2, baseline(y, h, 18), Color.web("#F5F5F5"), 18);
        }
        Color border = hovered ? Color.web("#D6A848") : Color.web("#14100C");
        stroke(gc, border, 3, x, y, w, h);
        fill(gc, hovered ? Color.web("#FFEB96") : Color.web("#C89664"), x + 3, y + 3, w - 6, 2);
        fill(gc, Color.web("#371E12"), x + 3, y + h - 5, w - 6, 2);
    }

    /** 竖向两段渐层（CSS {@code linear-gradient(180deg, a, b)} 的逐行等价物）。 */
    public static void gradientColumn(GraphicsContext gc, double x, double y, double w, double h,
                                      Color top, Color bottom) {
        int rows = (int) Math.round(h);
        for (int i = 0; i < rows; i++) {
            double t = rows <= 1 ? 0 : i / (rows - 1.0);
            gc.setFill(top.interpolate(bottom, t));
            gc.fillRect(x, y + i, w, 1);
        }
    }

    /** 品质光晕边框（背包/掉落物用）。 */
    public static void rarityGlow(GraphicsContext gc, double x, double y, double w, double h, Color color) {
        stroke(gc, color.deriveColor(0, 1, 1, 0.55), 4, x - 1, y - 1, w + 2, h + 2);
    }
}
