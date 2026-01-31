package com.game.potato;

import com.game.potato.core.GameEngine;
import com.game.potato.resources.ResourceManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 土豆冒险游戏主入口类
 * 
 * @author Potato Adventure Team
 * @version 1.0.0
 */
public class PotatoAdventure extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(PotatoAdventure.class);
    
    // 游戏窗口配置
    public static final String GAME_TITLE = "土豆冒险 - Potato Adventure";
    public static final int DEFAULT_WIDTH = 1280;
    public static final int DEFAULT_HEIGHT = 720;
    public static final int MIN_WIDTH = 800;
    public static final int MIN_HEIGHT = 600;
    
    private GameEngine gameEngine;
    private Canvas canvas;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("正在启动土豆冒险游戏...");
            
            // 初始化资源管理器
            ResourceManager.getInstance().initialize();
            
            // 创建游戏画布
            canvas = new Canvas(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            
            // 创建游戏引擎
            gameEngine = new GameEngine(canvas);
            
            // 创建场景
            StackPane root = new StackPane(canvas);
            Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            
            // 设置输入事件
            gameEngine.getInputHandler().attachToScene(scene);
            
            // 配置主窗口
            primaryStage.setTitle(GAME_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(MIN_WIDTH);
            primaryStage.setMinHeight(MIN_HEIGHT);
            primaryStage.setResizable(true);
            
            // 窗口关闭事件
            primaryStage.setOnCloseRequest(event -> {
                logger.info("游戏正在关闭...");
                shutdown();
            });
            
            // 显示窗口
            primaryStage.show();
            
            // 启动游戏引擎
            gameEngine.start();
            
            logger.info("土豆冒险游戏启动成功！");
            
        } catch (Exception e) {
            logger.error("游戏启动失败", e);
            showErrorDialog("游戏启动失败: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * 优雅关闭游戏
     */
    private void shutdown() {
        try {
            if (gameEngine != null) {
                gameEngine.stop();
            }
            
            ResourceManager.getInstance().dispose();
            
            logger.info("游戏已关闭");
        } catch (Exception e) {
            logger.error("关闭游戏时发生错误", e);
        }
    }
    
    /**
     * 显示错误对话框
     */
    private void showErrorDialog(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle("错误");
        alert.setHeaderText("游戏错误");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 程序入口点
     */
    public static void main(String[] args) {
        logger.info("======================================");
        logger.info("     土豆冒险 - Potato Adventure      ");
        logger.info("            Version 1.0.0            ");
        logger.info("======================================");
        launch(args);
    }
}
