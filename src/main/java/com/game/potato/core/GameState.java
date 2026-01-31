package com.game.potato.core;

/**
 * 游戏状态枚举
 */
public enum GameState {
    /**
     * 主菜单
     */
    MENU,
    
    /**
     * 游戏运行中
     */
    RUNNING,
    
    /**
     * 暂停
     */
    PAUSED,
    
    /**
     * 战斗中
     */
    BATTLE,
    
    /**
     * 对话中
     */
    DIALOGUE,
    
    /**
     * 游戏结束
     */
    GAME_OVER,
    
    /**
     * 加载中
     */
    LOADING
}
