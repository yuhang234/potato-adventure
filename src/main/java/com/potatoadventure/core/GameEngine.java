package com.potatoadventure.core;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import com.potatoadventure.model.Player;
import com.potatoadventure.model.GameMap;
import com.potatoadventure.model.Enemy;
import com.potatoadventure.util.InputHandler;
import com.potatoadventure.service.SaveLoadService;
import com.potatoadventure.service.BattleService;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏引擎 - 核心游戏循环、渲染和逻辑处理
 */
public class GameEngine {
    
    private final Stage stage;
    private final int width;
    private final int height;
    
    private Canvas canvas;
    private GraphicsContext gc;
    private Scene scene;
    private AnimationTimer gameLoop;
    
    // 游戏对象
    private Player player;
    private GameMap gameMap;
    private List<Enemy> enemies;
    
    // 服务
    private InputHandler inputHandler;
    private SaveLoadService saveLoadService;
    private BattleService battleService;
    
    // 游戏状态
    private GameState gameState;
    private long lastUpdateTime;
    private static final long UPDATE_INTERVAL = 16_666_667; // ~60 FPS (纳秒)
    
    /**
     * 游戏状态枚举
     */
    public enum GameState {
        MENU,      // 主菜单
        PLAYING,   // 游戏中
        PAUSED,    // 暂停
        BATTLE,    // 战斗中
        GAME_OVER  // 游戏结束
    }
    
    public GameEngine(Stage stage, int width, int height) {
        this.stage = stage;
        this.width = width;
        this.height = height;
        
        initializeComponents();
    }
    
    /**
     * 初始化游戏组件
     */
    private void initializeComponents() {
        // 创建画布
        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();
        
        // 创建场景
        StackPane root = new StackPane(canvas);
        scene = new Scene(root, width, height);
        stage.setScene(scene);
        
        // 初始化输入处理器
        inputHandler = new InputHandler(scene);
        
        // 初始化服务
        saveLoadService = new SaveLoadService();
        battleService = new BattleService();
        
        // 初始化游戏对象
        initializeGameObjects();
        
        // 设置游戏状态
        gameState = GameState.PLAYING;
        lastUpdateTime = System.nanoTime();
    }
    
    /**
     * 初始化游戏对象
     */
    private void initializeGameObjects() {
        // 创建地图
        gameMap = new GameMap(40, 30); // 40x30 格子
        
        // 创建玩家
        player = new Player(5, 5); // 初始位置 (5, 5)
        
        // 创建敌人列表
        enemies = new ArrayList<>();
        // 添加几个初始敌人
        enemies.add(new Enemy(15, 10, "史莱姆"));
        enemies.add(new Enemy(25, 15, "哥布林"));
        enemies.add(new Enemy(30, 20, "野狼"));
    }
    
    /**
     * 启动游戏循环
     */
    public void start() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdateTime >= UPDATE_INTERVAL) {
                    update((now - lastUpdateTime) / 1_000_000_000.0);
                    render();
                    lastUpdateTime = now;
                }
            }
        };
        gameLoop.start();
    }
    
    /**
     * 停止游戏循环
     */
    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
    
    /**
     * 更新游戏逻辑
     */
    private void update(double deltaTime) {
        switch (gameState) {
            case PLAYING:
                updatePlaying(deltaTime);
                break;
            case BATTLE:
                updateBattle(deltaTime);
                break;
            case PAUSED:
                // 暂停状态不更新
                break;
            case GAME_OVER:
                // 游戏结束状态
                break;
            default:
                break;
        }
        
        // 处理通用输入
        handleCommonInput();
    }
    
    /**
     * 更新游戏进行状态
     */
    private void updatePlaying(double deltaTime) {
        // 保存玩家旧位置
        int oldX = player.getX();
        int oldY = player.getY();
        
        // 处理玩家移动
        if (inputHandler.isKeyPressed(KeyCode.W) || inputHandler.isKeyPressed(KeyCode.UP)) {
            player.move(0, -1);
        }
        if (inputHandler.isKeyPressed(KeyCode.S) || inputHandler.isKeyPressed(KeyCode.DOWN)) {
            player.move(0, 1);
        }
        if (inputHandler.isKeyPressed(KeyCode.A) || inputHandler.isKeyPressed(KeyCode.LEFT)) {
            player.move(-1, 0);
        }
        if (inputHandler.isKeyPressed(KeyCode.D) || inputHandler.isKeyPressed(KeyCode.RIGHT)) {
            player.move(1, 0);
        }
        
        // 碰撞检测
        if (gameMap.isCollision(player.getX(), player.getY())) {
            player.setPosition(oldX, oldY);
        }
        
        // 检测与敌人的碰撞（触发战斗）
        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && player.getX() == enemy.getX() && player.getY() == enemy.getY()) {
                // 进入战斗状态
                gameState = GameState.BATTLE;
                battleService.startBattle(player, enemy);
                break;
            }
        }
        
        // 处理攻击输入
        if (inputHandler.isKeyJustPressed(KeyCode.SPACE)) {
            // 检测攻击范围内的敌人
            for (Enemy enemy : enemies) {
                if (enemy.isAlive() && isInAttackRange(player, enemy)) {
                    battleService.playerAttack(player, enemy);
                    if (!enemy.isAlive()) {
                        player.gainExp(enemy.getExpReward());
                    }
                }
            }
        }
    }
    
    /**
     * 更新战斗状态
     */
    private void updateBattle(double deltaTime) {
        Enemy currentEnemy = battleService.getCurrentEnemy();
        
        // 玩家回合
        if (inputHandler.isKeyJustPressed(KeyCode.DIGIT1)) {
            // 普通攻击
            battleService.playerAttack(player, currentEnemy);
        } else if (inputHandler.isKeyJustPressed(KeyCode.DIGIT2)) {
            // 使用技能1
            if (player.getMp() >= 10) {
                battleService.playerSkill(player, currentEnemy, 1);
            }
        } else if (inputHandler.isKeyJustPressed(KeyCode.DIGIT3)) {
            // 使用技能2
            if (player.getMp() >= 15) {
                battleService.playerSkill(player, currentEnemy, 2);
            }
        } else if (inputHandler.isKeyJustPressed(KeyCode.ESCAPE)) {
            // 逃跑
            if (Math.random() < 0.5) {
                gameState = GameState.PLAYING;
                battleService.endBattle();
            }
        }
        
        // 敌人回合
        if (currentEnemy != null && currentEnemy.isAlive()) {
            battleService.enemyAttack(currentEnemy, player);
        }
        
        // 检查战斗结束
        if (currentEnemy != null && !currentEnemy.isAlive()) {
            player.gainExp(currentEnemy.getExpReward());
            gameState = GameState.PLAYING;
            battleService.endBattle();
        }
        
        // 检查玩家死亡
        if (!player.isAlive()) {
            gameState = GameState.GAME_OVER;
        }
    }
    
    /**
     * 处理通用输入
     */
    private void handleCommonInput() {
        // F5 快速保存
        if (inputHandler.isKeyJustPressed(KeyCode.F5)) {
            saveLoadService.saveGame(player, gameMap, enemies);
            System.out.println("游戏已保存");
        }
        
        // F9 快速加载
        if (inputHandler.isKeyJustPressed(KeyCode.F9)) {
            if (saveLoadService.loadGame(player, gameMap, enemies)) {
                System.out.println("游戏已加载");
            }
        }
        
        // P 暂停/继续
        if (inputHandler.isKeyJustPressed(KeyCode.P)) {
            if (gameState == GameState.PLAYING) {
                gameState = GameState.PAUSED;
            } else if (gameState == GameState.PAUSED) {
                gameState = GameState.PLAYING;
            }
        }
    }
    
    /**
     * 渲染游戏画面
     */
    private void render() {
        // 清空画布
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);
        
        switch (gameState) {
            case PLAYING:
                renderPlaying();
                break;
            case BATTLE:
                renderBattle();
                break;
            case PAUSED:
                renderPlaying();
                renderPaused();
                break;
            case GAME_OVER:
                renderGameOver();
                break;
            default:
                break;
        }
    }
    
    /**
     * 渲染游戏进行画面
     */
    private void renderPlaying() {
        // 渲染地图
        gameMap.render(gc);
        
        // 渲染敌人
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.render(gc);
            }
        }
        
        // 渲染玩家
        player.render(gc);
        
        // 渲染UI
        renderUI();
    }
    
    /**
     * 渲染战斗画面
     */
    private void renderBattle() {
        // 绘制战斗背景
        gc.setFill(Color.rgb(50, 50, 100));
        gc.fillRect(0, 0, width, height);
        
        // 渲染玩家信息
        gc.setFill(Color.WHITE);
        gc.fillText("玩家: " + player.getName(), 50, 50);
        gc.fillText("HP: " + player.getHp() + "/" + player.getMaxHp(), 50, 70);
        gc.fillText("MP: " + player.getMp() + "/" + player.getMaxMp(), 50, 90);
        gc.fillText("等级: " + player.getLevel(), 50, 110);
        
        // 渲染敌人信息
        Enemy currentEnemy = battleService.getCurrentEnemy();
        if (currentEnemy != null) {
            gc.fillText("敌人: " + currentEnemy.getName(), 500, 50);
            gc.fillText("HP: " + currentEnemy.getHp() + "/" + currentEnemy.getMaxHp(), 500, 70);
        }
        
        // 渲染战斗指令
        gc.fillText("战斗指令:", 50, 500);
        gc.fillText("1 - 普通攻击", 50, 520);
        gc.fillText("2 - 火球术 (消耗10MP)", 50, 540);
        gc.fillText("3 - 治疗术 (消耗15MP)", 50, 560);
        gc.fillText("ESC - 逃跑 (50%成功率)", 50, 580);
    }
    
    /**
     * 渲染暂停画面
     */
    private void renderPaused() {
        // 半透明黑色遮罩
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRect(0, 0, width, height);
        
        // 暂停文字
        gc.setFill(Color.WHITE);
        gc.fillText("游戏暂停", width / 2 - 50, height / 2);
        gc.fillText("按P继续", width / 2 - 50, height / 2 + 30);
    }
    
    /**
     * 渲染游戏结束画面
     */
    private void renderGameOver() {
        gc.setFill(Color.rgb(100, 0, 0));
        gc.fillRect(0, 0, width, height);
        
        gc.setFill(Color.WHITE);
        gc.fillText("游戏结束", width / 2 - 50, height / 2);
        gc.fillText("按F9加载存档", width / 2 - 70, height / 2 + 30);
    }
    
    /**
     * 渲染UI
     */
    private void renderUI() {
        // 玩家信息
        gc.setFill(Color.WHITE);
        gc.fillText("玩家: " + player.getName() + " Lv." + player.getLevel(), 10, 20);
        
        // 生命值条
        gc.setFill(Color.RED);
        gc.fillRect(10, 30, 200, 20);
        gc.setFill(Color.GREEN);
        double hpPercent = (double) player.getHp() / player.getMaxHp();
        gc.fillRect(10, 30, 200 * hpPercent, 20);
        gc.setFill(Color.WHITE);
        gc.fillText("HP: " + player.getHp() + "/" + player.getMaxHp(), 15, 45);
        
        // 魔法值条
        gc.setFill(Color.DARKBLUE);
        gc.fillRect(10, 55, 200, 20);
        gc.setFill(Color.BLUE);
        double mpPercent = (double) player.getMp() / player.getMaxMp();
        gc.fillRect(10, 55, 200 * mpPercent, 20);
        gc.setFill(Color.WHITE);
        gc.fillText("MP: " + player.getMp() + "/" + player.getMaxMp(), 15, 70);
        
        // 经验值条
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(10, 80, 200, 15);
        gc.setFill(Color.YELLOW);
        double expPercent = (double) player.getExp() / player.getExpToNextLevel();
        gc.fillRect(10, 80, 200 * expPercent, 15);
        gc.setFill(Color.WHITE);
        gc.fillText("EXP: " + player.getExp() + "/" + player.getExpToNextLevel(), 15, 92);
        
        // 操作提示
        gc.fillText("WASD/方向键-移动 | 空格-攻击 | P-暂停 | F5-保存 | F9-加载", 10, height - 10);
    }
    
    /**
     * 判断是否在攻击范围内
     */
    private boolean isInAttackRange(Player player, Enemy enemy) {
        int dx = Math.abs(player.getX() - enemy.getX());
        int dy = Math.abs(player.getY() - enemy.getY());
        return dx <= 1 && dy <= 1;
    }
}
