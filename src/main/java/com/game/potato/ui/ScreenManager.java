package com.game.potato.ui;

import com.game.potato.input.InputHandler;
import com.game.potato.ui.screen.Screen;
import javafx.scene.canvas.GraphicsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;

/**
 * 屏幕管理器
 * 管理游戏中的各种界面切换
 */
public class ScreenManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ScreenManager.class);
    
    private final GraphicsContext gc;
    private Screen currentScreen;
    private final Stack<Screen> screenStack;
    
    public ScreenManager(GraphicsContext gc) {
        this.gc = gc;
        this.screenStack = new Stack<>();
    }
    
    /**
     * 切换到新屏幕
     */
    public void switchScreen(Screen newScreen) {
        if (currentScreen != null) {
            currentScreen.onExit();
        }
        
        currentScreen = newScreen;
        
        if (currentScreen != null) {
            currentScreen.setGraphicsContext(gc);
            currentScreen.onEnter();
            logger.info("切换到屏幕: " + newScreen.getClass().getSimpleName());
        }
    }
    
    /**
     * 压入新屏幕（保留当前屏幕）
     */
    public void pushScreen(Screen newScreen) {
        if (currentScreen != null) {
            screenStack.push(currentScreen);
            currentScreen.onPause();
        }
        
        currentScreen = newScreen;
        currentScreen.setGraphicsContext(gc);
        currentScreen.onEnter();
        
        logger.info("压入屏幕: " + newScreen.getClass().getSimpleName());
    }
    
    /**
     * 弹出当前屏幕，返回上一个
     */
    public void popScreen() {
        if (currentScreen != null) {
            currentScreen.onExit();
        }
        
        if (!screenStack.isEmpty()) {
            currentScreen = screenStack.pop();
            currentScreen.onResume();
            logger.info("返回到屏幕: " + currentScreen.getClass().getSimpleName());
        } else {
            currentScreen = null;
            logger.warn("屏幕栈为空");
        }
    }
    
    /**
     * 更新当前屏幕
     */
    public void update(double deltaTime, InputHandler input) {
        if (currentScreen != null) {
            currentScreen.update(deltaTime, input);
        }
    }
    
    /**
     * 渲染当前屏幕
     */
    public void render() {
        if (currentScreen != null) {
            currentScreen.render();
        }
    }
    
    /**
     * 获取当前屏幕
     */
    public Screen getCurrentScreen() {
        return currentScreen;
    }
}
