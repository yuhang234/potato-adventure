package com.potatoadventure.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.function.IntConsumer;

/**
 * 屏幕 1：主菜单（对应原型 {@code #screen-title}）。
 *
 * <p>几何全部取自 {@code css/style.css}：
 *
 * <ul>
 *   <li>背景 {@code bg_title.png} 铺满 1280×720（{@code background-size: cover}）；</li>
 *   <li>LOGO {@code ui/logo_banner.png}：{@code top:80px; left:360px; 560×140}，并复刻
 *       {@code logo-bob} 上下浮动 8px；</li>
 *   <li>菜单列：{@code top:270px; left:500px; width:280px; gap:15px}，按钮 {@code 280×52}、
 *       字号 18；</li>
 *   <li>页脚 {@code bottom:15px; left:20px}，右侧提示 {@code bottom:15px; right:20px}。</li>
 * </ul>
 */
public final class TitleScreen {

    /** 五个菜单项文案（与原型 {@code index.html} 的按钮顺序一致）。 */
    public static final String[] ITEMS = {
            "[ 新 的 冒 险 ]",
            "[ 继 续 游 戏 ]",
            "[ 存 档 管 理 ]",
            "[ 系 统 设 置 ]",
            "[ 关 于 游 戏 ]"
    };

    private static final double LOGO_X = 360;
    private static final double LOGO_Y = 80;
    private static final double LOGO_W = 560;
    private static final double LOGO_H = 140;

    private static final double MENU_X = 500;
    private static final double MENU_Y = 270;
    private static final double MENU_W = 280;
    private static final double BUTTON_H = 52;
    private static final double BUTTON_GAP = 15;

    private TitleScreen() {
    }

    /**
     * @param selected   当前选中项（键盘 ↑↓ 或鼠标悬停都会更新它）
     * @param animTime   动画时间（秒），仅用于 LOGO 浮动
     * @param onActivate 激活第 i 项的回调
     */
    public static void render(GraphicsContext gc, int selected, double animTime, IntConsumer onActivate) {
        Image background = Assets.background("bg_title.png");
        if (background != null) {
            gc.drawImage(background, 0, 0, Assets.DESIGN_WIDTH, Assets.DESIGN_HEIGHT);
        } else {
            UiKit.fill(gc, Color.web("#0A0C0E"), 0, 0, Assets.DESIGN_WIDTH, Assets.DESIGN_HEIGHT);
        }

        // LOGO：logo-bob 动画（0 → -8px，来回 3s）；偏移取整以保持像素硬边
        Image logo = Assets.ui("logo_banner.png");
        if (logo != null) {
            double bob = Math.round(-8 * (0.5 - 0.5 * Math.cos(animTime * Math.PI / 3.0)));
            gc.drawImage(logo, 0, 0, logo.getWidth(), logo.getHeight(),
                    LOGO_X, LOGO_Y + bob, LOGO_W, LOGO_H);
        }

        // 菜单
        for (int i = 0; i < ITEMS.length; i++) {
            double y = MENU_Y + i * (BUTTON_H + BUTTON_GAP);
            final int index = i;
            boolean hovered = UiKit.hot(MENU_X, y, MENU_W, BUTTON_H,
                    () -> onActivate.accept(index));
            boolean active = hovered || selected == i;
            UiKit.button(gc, MENU_X, y, MENU_W, BUTTON_H, ITEMS[i], active, index == 0);

            if (selected == i) {
                // 键盘选中指示：左侧金色箭头（像素三角）
                gc.setFill(Color.web("#FFD264"));
                gc.fillRect(MENU_X - 22, y + BUTTON_H / 2 - 2, 6, 4);
                gc.fillRect(MENU_X - 18, y + BUTTON_H / 2 - 5, 4, 10);
                gc.fillRect(MENU_X - 14, y + BUTTON_H / 2 - 8, 4, 16);
            }
        }

        UiKit.text(gc, "v2.0.0-M1 | 引擎：Java 21 + JavaFX 21 | 视觉规范：design/ui-prototype",
                20, Assets.DESIGN_HEIGHT - 15, Color.web("#8FA0AD"), 13, Palette.TEXT_SHADOW, true);
        UiKit.textCenter(gc, "[↑↓ / W S] 选择    [Enter / 空格] 确认    [鼠标] 直接点击",
                Assets.DESIGN_WIDTH - 220, Assets.DESIGN_HEIGHT - 15, Color.web("#D6A848"), 13);
    }
}
