package com.game.potato.ui.screen;

import com.game.potato.input.InputHandler;
import javafx.scene.canvas.GraphicsContext;

/**
 * 屏幕基类
 * 所有游戏界面都继承此类
 */
public abstract class Screen {
    
    protected GraphicsContext gc;
    
    /**
     * 设置图形上下文
     */
    public void setGraphicsContext(GraphicsContext gc) {
        this.gc = gc;
    }
    
    /**
     * 屏幕进入时调用
     */
    public abstract void onEnter();
    
    /**
     * 屏幕退出时调用
     */
    public abstract void onExit();
    
    /**
     * 屏幕暂停时调用（被压入栈）
     */
    public void onPause() {
        // 默认实现为空
    }
    
    /**
     * 屏幕恢复时调用（从栈中弹出）
     */
    public void onResume() {
        // 默认实现为空
    }
    
    /**
     * 更新屏幕逻辑
     */
    public abstract void update(double deltaTime, InputHandler input);
    
    /**
     * 渲染屏幕
     */
    public abstract void render();
}
