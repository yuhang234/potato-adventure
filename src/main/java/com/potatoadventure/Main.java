package com.potatoadventure;

import javafx.application.Application;
import javafx.stage.Stage;
import com.potatoadventure.core.GameEngine;

/**
 * 土豆冒险游戏 - 主程序入口
 * 
 * @version 1.0.0
 * @author Potato Adventure Team
 */
public class Main extends Application {
    
    private static final String GAME_TITLE = "土豆冒险 - Potato Adventure";
    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 720;
    
    private GameEngine gameEngine;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // 初始化游戏引擎
            gameEngine = new GameEngine(primaryStage, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            
            // 设置窗口属性
            primaryStage.setTitle(GAME_TITLE);
            primaryStage.setResizable(false);
            
            // 启动游戏
            gameEngine.start();
            
            // 显示窗口
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("游戏启动失败: " + e.getMessage());
        }
    }
    
    @Override
    public void stop() {
        // 游戏退出时的清理工作
        if (gameEngine != null) {
            gameEngine.stop();
        }
    }
    
    /**
     * 程序入口
     */
    public static void main(String[] args) {
        launch(args);
    }
}
