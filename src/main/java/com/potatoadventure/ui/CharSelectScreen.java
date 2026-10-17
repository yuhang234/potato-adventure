package com.potatoadventure.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.function.IntConsumer;

/**
 * 屏幕 2：职业与角色创建（对应原型 {@code #screen-charselect}）。
 *
 * <p>几何与配色逐条对齐 {@code css/style.css} 与原型脚本 {@code js/app.js}（雷达图由
 * {@code App.renderRadarCharts()} 绘制）：
 *
 * <ul>
 *   <li>木纹底铺满；标题 {@code 26px} 居中，占位 {@code 30..56}；</li>
 *   <li>三张 {@code 340×500} 石板卡片、间隔 40、居中（起始 x = 90）、卡片顶 = 81，
 *       选中卡整体上移 10px；</li>
 *   <li>卡片内自上而下：角标 {@code top:-12px}（13px，内边距 2/14）、立绘 {@code 140×140}
 *       （margin 10）、职业名 20px（margin-bottom 4）、难度 13px（margin-bottom 12）、
 *       {@code 190×130} 六维雷达（margin-bottom 12）、技能预览框（内边距 8/10，
 *       line-height 1.4，高度由内容决定）；</li>
 *   <li>底部操作条 {@code bottom:25px} 居中：返回按钮 / {@code 260} 宽姓名输入框 /
 *       踏上征程按钮，间隔 30。</li>
 * </ul>
 *
 * <p>雷达图数值与配色直接取自原型：{@code berserker [90,30,85,75,50,45]}（红）、
 * {@code mage [50,95,90,40,60,70]}（紫）、{@code ranger [65,55,75,55,90,85]}（绿），
 * 半径 45、三层六边形网格 {@code rgba(214,168,72,0.25)}、轴线 {@code rgba(214,168,72,0.3)}。
 */
public final class CharSelectScreen {

    /** 三个职业的展示数据（与原型卡片一一对应）。 */
    public enum HeroClass {
        BERSERKER("berserker", "土豆狂战士", "近战强攻", "上手难度: ★☆☆ (推荐新人)",
                "初始技能【旋风斩】",
                "以自身为轴心发动猛烈回旋重击，对敌阵造成 140% 物理狂暴伤害。",
                new int[]{90, 30, 85, 75, 50, 45},
                Color.web("#E74C3C"), Color.color(231 / 255.0, 76 / 255.0, 60 / 255.0, 0.5),
                "战士阶"),
        MAGE("mage", "土豆大魔导", "元素爆破", "上手难度: ★★☆ (法术爆发)",
                "初始技能【爆裂火球】",
                "汇聚天地火元素形成爆鸣火球，造成 180% 烈焰伤害并附加持续灼烧。",
                new int[]{50, 95, 90, 40, 60, 70},
                Color.web("#8E44AD"), Color.color(142 / 255.0, 68 / 255.0, 173 / 255.0, 0.5),
                "法师阶"),
        RANGER("ranger", "土豆游侠", "游击穿透", "上手难度: ★★★ (敏捷暴击)",
                "初始技能【连环快箭】",
                "迅捷拉弓连射三支破空穿甲箭，每次命中提升 10% 自身闪避率。",
                new int[]{65, 55, 75, 55, 90, 85},
                Color.web("#2ECC71"), Color.color(46 / 255.0, 204 / 255.0, 113 / 255.0, 0.5),
                "游侠阶");

        private final String key;
        private final String displayName;
        private final String badge;
        private final String difficulty;
        private final String skillTitle;
        private final String skillText;
        private final int[] radar;
        private final Color radarStroke;
        private final Color radarFill;
        private final String rank;

        HeroClass(String key, String displayName, String badge, String difficulty,
                  String skillTitle, String skillText, int[] radar,
                  Color radarStroke, Color radarFill, String rank) {
            this.key = key;
            this.displayName = displayName;
            this.badge = badge;
            this.difficulty = difficulty;
            this.skillTitle = skillTitle;
            this.skillText = skillText;
            this.radar = radar;
            this.radarStroke = radarStroke;
            this.radarFill = radarFill;
            this.rank = rank;
        }

        public String key() {
            return key;
        }

        public String displayName() {
            return displayName;
        }

        /** 状态框右上角的阶位标签（与原型 {@code 战士阶} 一致）。 */
        public String rank() {
            return rank;
        }
    }

    /** 雷达轴标签顺序与原型 {@code App.renderRadarCharts()} 一致。 */
    private static final String[] RADAR_LABELS = {"HP", "MP", "ATK", "DEF", "SPD", "CRIT"};

    private static final double CARD_W = 340;
    private static final double CARD_H = 500;
    private static final double CARD_GAP = 40;
    /** 卡片顶部 y：padding-top 30 + 标题行 26 + margin 25 = 81。 */
    private static final double CARDS_Y = 81;

    private CharSelectScreen() {
    }

    /**
     * @param selected    当前选中职业下标
     * @param playerName  当前姓名
     * @param editingName 是否处于姓名编辑状态（编辑时输入框高亮并接收键入）
     */
    public static void render(GraphicsContext gc, int selected, String playerName, boolean editingName,
                              IntConsumer onSelect, Runnable onBack, Runnable onConfirm, Runnable onEditName) {
        Image wood = Assets.ui("wood_panel.png");
        if (wood != null) {
            UiKit.tiled(gc, wood, 0, 0, Assets.DESIGN_WIDTH, Assets.DESIGN_HEIGHT, wood.getWidth());
        } else {
            UiKit.fill(gc, Color.web("#7D5032"), 0, 0, Assets.DESIGN_WIDTH, Assets.DESIGN_HEIGHT);
        }

        // 标题：26px、居中，行盒 30..56
        UiKit.textCenter(gc, "❖ 选 择 你 的 土 豆 勇 士 ❖", Assets.DESIGN_WIDTH / 2.0, 51,
                Color.web("#FFD264"), 26, Color.web("#14100C"), true);

        HeroClass[] classes = HeroClass.values();
        double totalW = classes.length * CARD_W + (classes.length - 1) * CARD_GAP;
        double startX = (Assets.DESIGN_WIDTH - totalW) / 2;

        Image slate = Assets.ui("stone_slate.png");
        for (int i = 0; i < classes.length; i++) {
            HeroClass hero = classes[i];
            boolean active = selected == i;
            // 选中卡上移 10px（CSS .char-card.selected 的 translateY(-10px)）
            double x = startX + i * (CARD_W + CARD_GAP);
            double y = CARDS_Y - (active ? 10 : 0);
            final int index = i;
            boolean hovered = UiKit.hot(startX + i * (CARD_W + CARD_GAP), CARDS_Y, CARD_W, CARD_H,
                    () -> onSelect.accept(index));

            if (slate != null) {
                UiKit.tiled(gc, slate, x, y, CARD_W, CARD_H, slate.getWidth());
            } else {
                UiKit.fill(gc, Color.web("#2C3A47"), x, y, CARD_W, CARD_H);
            }
            Color border = active ? Color.web("#FFD264") : hovered ? Color.web("#D6A848")
                    : Color.web("#141419");
            UiKit.stroke(gc, border, 4, x, y, CARD_W, CARD_H);
            double inset = active ? 3 : 2;
            UiKit.fill(gc, active ? Color.web("#FFEB96") : Color.web("#788796"),
                    x + 4, y + 4, CARD_W - 8, inset);
            UiKit.fill(gc, active ? Color.web("#5F4114") : Color.web("#1E232D"),
                    x + 4, y + CARD_H - 4 - inset, CARD_W - 8, inset);

            // 角标：top:-12、13px、内边距 2/14，压在卡片上沿
            double badgeW = hero.badge.length() * 13 + 28;
            double badgeX = Math.round(x + (CARD_W - badgeW) / 2);
            UiKit.fill(gc, Color.web("#D6A848"), badgeX, y - 12, badgeW, 21);
            UiKit.stroke(gc, Color.web("#14100C"), 2, badgeX, y - 12, badgeW, 21);
            UiKit.textCenter(gc, hero.badge, x + CARD_W / 2, y - 12 + 2 + 14,
                    Color.web("#14100C"), 13);

            // 立绘 140×140：卡片内边距 16 + margin-top 10
            double contentX = x + 4 + 16;
            double portraitY = y + 4 + 16 + 10;
            Image portrait = Assets.sprite("portrait_" + hero.key + ".png");
            if (portrait != null) {
                gc.drawImage(portrait, 0, 0, portrait.getWidth(), portrait.getHeight(),
                        contentX, portraitY, 140, 140);
            }

            // 职业名 20px（基线 = 行盒顶 + 16），margin-bottom 4
            double nameTop = portraitY + 140 + 10;
            UiKit.textCenter(gc, hero.displayName, x + CARD_W / 2, nameTop + 16,
                    Color.web("#FFD264"), 20);
            // 难度 13px，margin-bottom 12
            double difficultyTop = nameTop + 20 + 4;
            UiKit.textCenter(gc, hero.difficulty, x + CARD_W / 2, difficultyTop + 10,
                    Color.web("#A0B0C0"), 13);

            double radarY = difficultyTop + 13 + 12;
            drawRadar(gc, x + (CARD_W - 190) / 2, radarY, hero);

            double previewY = radarY + 130 + 12;
            drawSkillPreview(gc, contentX, previewY, CARD_W - 8 - 32, hero);
        }

        // 底部操作条（bottom:25px，居中，间距 30）
        double barH = 44;
        double barY = Assets.DESIGN_HEIGHT - 25 - barH;
        double backW = 160;
        double confirmW = 200;
        double nameW = 260;
        double total = backW + nameW + confirmW + 2 * 30;
        double barX = (Assets.DESIGN_WIDTH - total) / 2;

        boolean backHover = UiKit.hot(barX, barY, backW, barH, onBack);
        UiKit.button(gc, barX, barY, backW, barH, "« 返 回", backHover, false, true);

        double nameX = barX + backW + 30;
        boolean nameHover = UiKit.hot(nameX, barY, nameW, barH, onEditName);
        UiKit.fill(gc, Color.web("#141820"), nameX, barY, nameW, barH);
        UiKit.stroke(gc, editingName ? Color.web("#FFD264") : nameHover ? Color.web("#A0B0C0")
                : Color.web("#788796"), 3, nameX, barY, nameW, barH);
        String shown = playerName + (editingName ? "_" : "");
        UiKit.text(gc, shown, nameX + 3 + 18, UiKit.baseline(barY, barH, 18), Color.web("#FFD264"), 18);

        double confirmX = nameX + nameW + 30;
        boolean confirmHover = UiKit.hot(confirmX, barY, confirmW, barH, onConfirm);
        UiKit.button(gc, confirmX, barY, confirmW, barH, "踏 上 征 程 »", confirmHover, true, true);

        UiKit.textCenter(gc, "[← → ] 切换职业    [Enter] 确认并开始    [Esc] 返回主菜单",
                Assets.DESIGN_WIDTH / 2.0, Assets.DESIGN_HEIGHT - 6, Color.web("#F5E6BE"), 13);
    }

    /**
     * 六边形属性雷达：几何与配色复刻原型 {@code App.renderRadarCharts()}
     * （半径 45、三层网格、六条轴线、10px 轴标签、职业专属填充与描边）。
     */
    private static void drawRadar(GraphicsContext gc, double x, double y, HeroClass hero) {
        double w = 190;
        double h = 130;
        UiKit.fill(gc, Color.color(15 / 255.0, 20 / 255.0, 26 / 255.0, 0.7), x, y, w, h);
        UiKit.stroke(gc, Color.web("#3C4B57"), 1, x, y, w, h);

        double cx = x + w / 2;
        double cy = y + h / 2;
        double radius = 45;
        int n = 6;
        int[] values = hero.radar;

        // 三层六边形网格 rgba(214,168,72,0.25)
        gc.setStroke(Color.color(214 / 255.0, 168 / 255.0, 72 / 255.0, 0.25));
        gc.setLineWidth(1);
        for (double ratio : new double[]{0.33, 0.66, 1.0}) {
            for (int i = 0; i < n; i++) {
                double a0 = angle(i, n);
                double a1 = angle(i + 1, n);
                gc.strokeLine(cx + Math.cos(a0) * radius * ratio, cy + Math.sin(a0) * radius * ratio,
                        cx + Math.cos(a1) * radius * ratio, cy + Math.sin(a1) * radius * ratio);
            }
        }

        // 轴线 rgba(214,168,72,0.3) + 轴标签 10px 居中
        gc.setStroke(Color.color(214 / 255.0, 168 / 255.0, 72 / 255.0, 0.3));
        for (int i = 0; i < n; i++) {
            double a = angle(i, n);
            gc.strokeLine(cx, cy, cx + Math.cos(a) * radius, cy + Math.sin(a) * radius);
            double lx = cx + Math.cos(a) * (radius + 12);
            double ly = cy + Math.sin(a) * (radius + 12);
            // canvas textBaseline='middle' 等价：基线 = 中心 + 0.3em
            UiKit.textCenter(gc, RADAR_LABELS[i], lx, ly + 3, Color.web("#A0B0C0"), 10,
                    Palette.TEXT_SHADOW, false);
        }

        // 职业数据多边形
        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) {
            double a = angle(i, n);
            double r = radius * values[i] / 100.0;
            xs[i] = cx + Math.cos(a) * r;
            ys[i] = cy + Math.sin(a) * r;
        }
        gc.setFill(hero.radarFill);
        gc.fillPolygon(xs, ys, n);
        gc.setStroke(hero.radarStroke);
        gc.setLineWidth(2);
        gc.strokePolygon(xs, ys, n);
        gc.setLineWidth(1);
    }

    private static double angle(int i, int n) {
        return i * 2 * Math.PI / n - Math.PI / 2;
    }

    /**
     * 技能预览框：{@code border 2 / padding 8 10 / line-height 1.4}，高度由内容决定
     * （原型里没有固定高度，文字两行 → 75px 高）。
     */
    private static void drawSkillPreview(GraphicsContext gc, double x, double y, double w, HeroClass hero) {
        double textW = w - 4 - 20;
        String[] lines = wrap(hero.skillText, textW, 13);
        double lineH = 13 * 1.4;
        double h = 4 + 16 + lineH * (1 + lines.length);

        UiKit.fill(gc, Color.color(20 / 255.0, 25 / 255.0, 35 / 255.0, 0.85), x, y, w, h);
        UiKit.stroke(gc, Color.web("#3C4B57"), 2, x, y, w, h);

        double textX = x + 2 + 10;
        double baseline = y + 2 + 8 + lineH - (lineH - 13) / 2 - 13 * 0.2;
        UiKit.text(gc, hero.skillTitle, textX, baseline, Color.web("#FFD264"), 13);
        for (int i = 0; i < lines.length; i++) {
            UiKit.text(gc, lines[i], textX, baseline + lineH * (i + 1), Color.web("#E0E8F0"), 13);
        }
    }

    /** 按像素宽度折行：Unifont 等宽，中文 1em、ASCII 0.5em。 */
    private static String[] wrap(String text, double maxWidth, int size) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        StringBuilder line = new StringBuilder();
        double width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            double advance = c < 0x2E80 ? size * 0.5 : size;
            if (width + advance > maxWidth && line.length() > 0) {
                lines.add(line.toString());
                line.setLength(0);
                width = 0;
            }
            line.append(c);
            width += advance;
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines.toArray(new String[0]);
    }
}
