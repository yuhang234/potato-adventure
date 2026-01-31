package com.game.potato.core;

import com.game.potato.input.InputHandler;
import com.game.potato.ui.ScreenManager;
import com.game.potato.ui.screen.MenuScreen;
import com.game.potato.world.MapManager;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 游戏引擎核心类
 * 负责游戏循环、更新和渲染
 */
public class GameEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(GameEngine.class);
    
    // 目标帧率
    private static final long TARGET_FPS = 60;
    private static final long FRAME_TIME = 1000000000 / TARGET_FPS; // 纳秒
    
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final InputHandler inputHandler;
    private final ScreenManager screenManager;
    private final MapManager mapManager;
    
    private GameState gameState;
    private AnimationTimer gameLoop;
    
    // 帧率统计
    private long lastFrameTime;
    private int fps;
    private int frameCount;
    private long fpsTimer;
    
    // Delta time (秒)
    private double deltaTime;
    
    private boolean running;
    
    public GameEngine(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.inputHandler = new InputHandler();
        this.screenManager = new ScreenManager(gc);
        this.mapManager = new MapManager();
        this.gameState = GameState.MENU;
        this.running = false;
        
        initializeGameLoop();
        
        logger.info("游戏引擎初始化完成");
    }
    
    /**
     * 初始化游戏循环
     */
    private void initializeGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                if (!running) {
                    return;
                }
                
                // 计算delta time
                if (lastFrameTime == 0) {
                    lastFrameTime = currentNanoTime;
                }
                long elapsedTime = currentNanoTime - lastFrameTime;
                deltaTime = elapsedTime / 1000000000.0; // 转换为秒
                lastFrameTime = currentNanoTime;
                
                // 更新FPS
                updateFPS(currentNanoTime);
                
                // 更新游戏逻辑
                update(deltaTime);
                
                // 渲染画面
                render();
            }
        };
    }
    
    /**
     * 启动游戏引擎
     */
    public void start() {
        if (running) {
            logger.warn("游戏引擎已经在运行");
            return;
        }
        
        logger.info("启动游戏引擎...");
        running = true;
        lastFrameTime = 0;
        
        // 切换到主菜单
        screenManager.switchScreen(new MenuScreen());
        
        gameLoop.start();
        logger.info("游戏引擎已启动");
    }
    
    /**
     * 停止游戏引擎
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        logger.info("停止游戏引擎...");
        running = false;
        gameLoop.stop();
        logger.info("游戏引擎已停止");
    }
    
    /**
     * 更新游戏逻辑
     */
    private void update(double deltaTime) {
        try {
            // 更新当前屏幕
            screenManager.update(deltaTime, inputHandler);
            
            // 根据游戏状态更新相应系统
            switch (gameState) {
                case RUNNING:
                    mapManager.update(deltaTime);
                    break;
                    
                case BATTLE:
                    // 战斗系统更新
                    break;
                    
                case DIALOGUE:
                    // 对话系统更新
                    break;
                    
                default:
                    break;
            }
            
            // 清理输入状态
            inputHandler.update();
            
        } catch (Exception e) {
            logger.error("更新游戏逻辑时发生错误", e);
        }
    }
    
    /**
     * 渲染画面
     */
    private void render() {
        try {
            // 清空画布
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            
            // 渲染当前屏幕
            screenManager.render();
            
            // 渲染FPS（调试模式）
            renderDebugInfo();
            
        } catch (Exception e) {
            logger.error("渲染画面时发生错误", e);
        }
    }
    
    /**
     * 更新FPS统计
     */
    private void updateFPS(long currentNanoTime) {
        frameCount++;
        if (currentNanoTime - fpsTimer >= 1000000000) {
            fps = frameCount;
            frameCount = 0;
            fpsTimer = currentNanoTime;
        }
    }
    
    /**
     * 渲染调试信息
     */
    private void renderDebugInfo() {
        gc.setFill(Color.YELLOW);
        gc.fillText("FPS: " + fps, 10, 20);
        gc.fillText("State: " + gameState, 10, 40);
    }
    
    // Getters
    public GameState getGameState() {
        return gameState;
    }
    
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        logger.info("游戏状态切换至: " + gameState);
    }
    
    public InputHandler getInputHandler() {
        return inputHandler;
    }
    
    public ScreenManager getScreenManager() {
        return screenManager;
    }
    
    public MapManager getMapManager() {
        return mapManager;
    }
    
    public double getDeltaTime() {
        return deltaTime;
    }
    
    public int getFPS() {
        return fps;
    }
}
