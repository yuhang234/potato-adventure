package com.potatoadventure.core;

/**
 * 游戏状态枚举。
 *
 * <p>状态机由 {@link GameEngine} 持有并驱动：
 * {@code MENU → CHAR_SELECT → PLAYING ⇄ {PAUSED, BATTLE} → {VICTORY, GAME_OVER}}。
 * 启动进入 {@code MENU}（标题画面），选角后进入 {@code PLAYING}；战斗中玩家死亡切入
 * {@code GAME_OVER}，三名敌人全灭切入 {@code VICTORY}。
 */
public enum GameState {
    /** 标题画面。启动即进入，任意键/点击进入职业选择。 */
    MENU,
    /** 职业选择：三职业卡片，选中后进入大地图。 */
    CHAR_SELECT,
    /** 大地图探索：可移动、可触发战斗、可存档读档。 */
    PLAYING,
    /** 暂停：逻辑冻结，画面覆盖遮罩。 */
    PAUSED,
    /** 回合制战斗。 */
    BATTLE,
    /** 胜利结算：三敌全灭后展示。 */
    VICTORY,
    /** 玩家死亡。 */
    GAME_OVER
}
