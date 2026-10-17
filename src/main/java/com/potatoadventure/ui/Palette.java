package com.potatoadventure.ui;

import javafx.scene.paint.Color;

/**
 * 已批准视觉规范的调色板常量（来源：设计报告 Part C §3 色彩调色板 + 原型说明）。
 *
 * <p>命名规则：{@code 用途_色阶}。标 {@code [派生]} 的颜色是为了在 32×32 地块上做出体积感而从
 * 规范色按固定比例插值得到的明暗阶，不引入新色相，保证整体画面仍落在已批准配色体系内。
 */
public final class Palette {

    private Palette() {
    }

    // ---- UI 界面基底 ----
    /** 深灰石板底色 (Panel Base)。 */
    public static final Color PANEL_BASE = Color.web("#1E272C");
    /** 深海青次底色 (Slot / Card Inner)。 */
    public static final Color SLOT_INNER = Color.web("#2C3A47");
    /** 复古金属铜金 (Border Frame Highlight)。 */
    public static final Color FRAME_GOLD = Color.web("#D6A848");
    /** 阴影古铜 (Border Shadow)。 */
    public static final Color FRAME_SHADOW = Color.web("#7A5828");

    // ---- 状态与数值体系 ----
    /** HP 条 / 狂暴红。 */
    public static final Color HP_RED = Color.web("#E74C3C");
    /** MP 条 / 法师蓝。 */
    public static final Color MP_BLUE = Color.web("#2980B9");
    /** EXP 条 / 金币金。 */
    public static final Color EXP_GOLD = Color.web("#F39C12");
    /** 增益 / 史莱姆翠绿。 */
    public static final Color SLIME_GREEN = Color.web("#27AE60");
    /** Boss / 减益 / 史诗紫。 */
    public static final Color EPIC_PURPLE = Color.web("#8E44AD");

    // ---- 文本 ----
    /** 普通文本（凡品银白）。 */
    public static final Color TEXT_PRIMARY = Color.web("#BDC3C7");
    /** 文本投影/暗描边。 */
    public static final Color TEXT_SHADOW = Color.web("#101518");

    // ---- 环境与自然地貌 ----
    /** 青苔草地基底。 */
    public static final Color GRASS_BASE = Color.web("#557A46");
    /** 草地暗阶 [派生]。 */
    public static final Color GRASS_DARK = Color.web("#4A6B3D");
    /** 草地亮阶 [派生]。 */
    public static final Color GRASS_LIGHT = Color.web("#628A52");
    /** 草叶高光 [派生]。 */
    public static final Color GRASS_BLADE = Color.web("#7A9E60");
    /** 幽暗灌木绿（树冠）。 */
    public static final Color BUSH = Color.web("#385E38");
    /** 树冠受光面 [派生]。 */
    public static final Color BUSH_LIGHT = Color.web("#47714A");
    /** 湿润泥土 / 树干。 */
    public static final Color TRUNK = Color.web("#80583A");
    /** 树干暗部 [派生]。 */
    public static final Color TRUNK_DARK = Color.web("#5E3F2A");
    /** 地牢石壁暗灰（墙体基色）。 */
    public static final Color STONE = Color.web("#34495E");
    /** 石壁暗槽 / 交界阴影。 */
    public static final Color STONE_DARK = Color.web("#1E232D");
    /** 石壁受光倒角（原型 ui/stone_slate.png 高光色）。 */
    public static final Color STONE_LIGHT = Color.web("#788796");
    /** 水面基色（与 MP 海蓝同源）。 */
    public static final Color WATER = Color.web("#2980B9");
    /** 水面波光（原型 mana_crystal 高光色）。 */
    public static final Color WATER_LIGHT = Color.web("#8CFCFF");

    // ---- 角色 ----
    /** 土豆表皮本色。 */
    public static final Color POTATO_SKIN = Color.web("#D2B48C");
    /** 土豆表皮暗部 [派生]。 */
    public static final Color POTATO_SHADE = Color.web("#A98A63");
    /** 野狼银灰杂毛（原型 enemy_wolf 配方）。 */
    public static final Color WOLF_FUR = Color.web("#788796");
}
