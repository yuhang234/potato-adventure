package com.potatoadventure.core;

/**
 * 游戏状态枚举。
 *
 * <p>状态机由 {@link GameEngine} 持有并驱动：{@code PLAYING} ⇄ {@code PAUSED}，进入敌人格切换
 * 到 {@code BATTLE}，战斗中玩家死亡切入 {@code GAME_OVER}。
 *
 * <p><b>已知缺口（M1 遗留，M4 交付）：</b>{@code MENU} 目前在 M1 阶段<b>不会被进入</b>——
 * 游戏启动后直接进入 {@code PLAYING}。这里保留该枚举常量并把 {@link GameEngine} 的分发
 * switch 显式写成空实现分支，是为了让 M4 的“主菜单 / 职业选择 / 关于 / 三槽存档 UI / 设置”
 * 有唯一的接入点：M4 只需要让 {@code GameEngine} 在启动时置为 {@code MENU}，并在 switch 的
 * {@code MENU} 分支里接上菜单 screen 即可，无需再改动本枚举或分发结构。
 * M1 范围内不实现任何菜单界面。
 */
public enum GameState {
    /** 主菜单。M1 不进入（见类注释的缺口说明），M4 接入。 */
    MENU,
    /** 大地图探索：可移动、可触发战斗、可存档读档。 */
    PLAYING,
    /** 暂停：逻辑冻结，画面覆盖遮罩。 */
    PAUSED,
    /** 回合制战斗。 */
    BATTLE,
    /** 玩家死亡。 */
    GAME_OVER
}
