package com.potatoadventure.core;

import com.potatoadventure.model.Enemy;
import com.potatoadventure.model.GameMap;
import com.potatoadventure.model.Player;
import com.potatoadventure.service.BattleService;
import com.potatoadventure.service.SaveLoadService;
import com.potatoadventure.ui.Palette;
import com.potatoadventure.util.InputHandler;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏引擎 —— 主循环、状态分发、渲染与输入调度。
 *
 * <p>M1 重构要点：
 *
 * <ul>
 *   <li><b>摄像机跟随 + 视口裁剪</b>：大地图为 60×45 = 1920×1440 像素，窗口 1280×720，
 *       由 {@link Camera} 平滑跟随玩家并夹在地图边界内（不再出现“地图下半部分永远看不到”
 *       或走到边缘露黑边）。渲染只绘制可见地块窗口。</li>
 *   <li><b>按真实时间推进移动</b>：{@code dt} 来自 {@link AnimationTimer}，移动按秒推进，
 *       因此帧率波动不会改变移动速度，也不再需要“固定 16.67ms 一帧否则整帧丢弃”的写法
 *       （旧写法在 60Hz 屏上会周期性地丢帧，看起来就是抖动）。</li>
 *   <li><b>输入按帧缓冲</b>：见 {@link InputHandler}，短按不会丢。</li>
 *   <li><b>战斗回合交接</b>：见 {@link BattleService}，敌人不再每帧攻击。</li>
 *   <li><b>击败奖励只发一次</b>：由 {@link BattleService#grantDefeatReward} 统一结算，
 *       引擎不再直接调用 {@code gainExp}。</li>
 *   <li><b>存档含地图</b>：F5/F9 走 {@link SaveLoadService} 的地图表，读档后地图不再重新随机。</li>
 * </ul>
 *
 * <p>{@link GameState#MENU} 目前不会被进入（M1 范围不含菜单界面，M4 接入），
 * 状态分发的 {@code MENU} 分支已显式留出接入点。
 */
public class GameEngine {

    /**
     * 主循环节拍：约 60 FPS。
     *
     * <p>取 16.0ms（≈62.5 FPS）而不是 16.666ms：显示器垂直同步在 60Hz 附近都有毫秒级抖动，
     * 用 16.666ms 做“未到时间就整帧跳过”会把抖动放大成可见丢帧。逻辑与渲染都按真实 dt 推进，
     * 所以放宽到 16.0ms 只是把渲染上限固定在 60 FPS 附近，不会改变游戏速度。
     */
    private static final long FRAME_INTERVAL_NANOS = 16_000_000L;

    /** 单帧 dt 上限：窗口拖动/断点后不要一次补上巨量时间。 */
    private static final double MAX_FRAME_SECONDS = 0.25;

    /** 顶部提示条显示时长（秒）。 */
    private static final double BANNER_SECONDS = 3.0;

    private static final KeyCode[] MOVE_UP = {KeyCode.W, KeyCode.UP};
    private static final KeyCode[] MOVE_DOWN = {KeyCode.S, KeyCode.DOWN};
    private static final KeyCode[] MOVE_LEFT = {KeyCode.A, KeyCode.LEFT};
    private static final KeyCode[] MOVE_RIGHT = {KeyCode.D, KeyCode.RIGHT};

    private final Stage stage;
    /**
     * 画布（= 窗口内容区）尺寸。初值是构造时的期望值（1280×720），
     * 窗口管理器给出不同尺寸时由 {@link #applyViewport()} 更新。
     */
    private int width;
    private int height;

    private Canvas canvas;
    private GraphicsContext gc;
    private Scene scene;
    private AnimationTimer gameLoop;

    // 游戏对象
    private Player player;
    private GameMap gameMap;
    private List<Enemy> enemies;
    private Camera camera;

    // 服务
    private InputHandler inputHandler;
    private SaveLoadService saveLoadService;
    private BattleService battleService;

    // 状态
    private GameState gameState;
    private long lastUpdateTime;

    /** 触发战斗前玩家所在的格子，逃跑时优先退回这里。 */
    private int lastOverworldTileX;
    private int lastOverworldTileY;

    /** 粘性移动方向：本帧没有新的方向键按下时继续沿用。 */
    private int moveDirX;
    private int moveDirY;

    private boolean debugOverlay;
    private String banner = "";
    private double bannerTimer;
    private double fpsAccumulator;
    private int fpsFrames;
    private double fps;

    public GameEngine(Stage stage, int width, int height) {
        this.stage = stage;
        this.width = width;
        this.height = height;
        initializeComponents();
    }

    private void initializeComponents() {
        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.setResizable(false);

        inputHandler = new InputHandler();
        inputHandler.attach(scene);

        // 窗口内容区未必正好是 1280×720（窗口管理器可能给一个小一些的窗口）。
        // 画布与摄像机视口都跟随真实尺寸，否则画布会被裁切、镜头会按错误视口夹取。
        scene.widthProperty().addListener((obs, oldV, newV) -> applyViewport());
        scene.heightProperty().addListener((obs, oldV, newV) -> applyViewport());

        saveLoadService = new SaveLoadService();
        battleService = new BattleService();

        newGame();

        gameState = GameState.PLAYING;
        lastUpdateTime = System.nanoTime();
    }

    /**
     * 让画布与摄像机视口跟随真实窗口内容区尺寸。
     *
     * <p>场景尺寸在窗口显示后由窗口管理器最终确定，因此在 width/height 变化时重新对齐：
     * 画布尺寸直接决定 {@link #gc} 的坐标系，摄像机视口则决定镜头能滚到多远
     * （视口比地图小才不会出现黑边）。相机尚未创建时只更新画布。
     */
    private void applyViewport() {
        int newWidth = (int) Math.round(scene.getWidth());
        int newHeight = (int) Math.round(scene.getHeight());
        if (newWidth <= 0 || newHeight <= 0 || (newWidth == width && newHeight == height)) {
            return;
        }
        width = newWidth;
        height = newHeight;
        canvas.setWidth(newWidth);
        canvas.setHeight(newHeight);
        if (camera != null) {
            camera.setViewport(newWidth, newHeight);
        }
    }

    /**
     * 开一局新游戏：新地图、新玩家、按地图点位放置敌人。
     */
    private void newGame() {
        gameMap = new GameMap(GameMap.DEFAULT_WIDTH, GameMap.DEFAULT_HEIGHT, System.nanoTime());
        player = new Player(GameMap.SPAWN_TILE_X, GameMap.SPAWN_TILE_Y);

        String[] names = {"史莱姆", "哥布林", "野狼"};
        enemies = new ArrayList<>();
        List<int[]> spawns = gameMap.getEnemySpawns();
        for (int i = 0; i < spawns.size(); i++) {
            int[] spawn = spawns.get(i);
            enemies.add(new Enemy(spawn[0], spawn[1], names[i % names.length]));
        }

        camera = new Camera(width, height, gameMap.getPixelWidth(), gameMap.getPixelHeight());
        camera.snapTo(player.getCenterPixelX(), player.getCenterPixelY());

        lastOverworldTileX = player.getX();
        lastOverworldTileY = player.getY();

        System.out.printf("新地图: %dx%d seed=0x%X hash=0x%X 玩家出生=(%d,%d) 敌人=%s%n",
                gameMap.getWidth(), gameMap.getHeight(), gameMap.getSeed(), gameMap.tileHash(),
                player.getX(), player.getY(), describeEnemies());
    }

    private String describeEnemies() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(e.getName()).append('(').append(e.getX()).append(',').append(e.getY()).append(')');
        }
        return sb.append(']').toString();
    }

    /**
     * 启动主循环。
     */
    public void start() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdateTime < FRAME_INTERVAL_NANOS) {
                    return;
                }
                double dt = (now - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = now;
                if (dt > MAX_FRAME_SECONDS) {
                    dt = MAX_FRAME_SECONDS;
                }

                inputHandler.beginFrame();
                update(dt);
                render();

                fpsAccumulator += dt;
                fpsFrames++;
                if (fpsAccumulator >= 0.5) {
                    fps = fpsFrames / fpsAccumulator;
                    fpsAccumulator = 0;
                    fpsFrames = 0;
                }
            }
        };
        gameLoop.start();
    }

    /**
     * 停止主循环。
     */
    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (saveLoadService != null) {
            saveLoadService.close();
        }
    }

    // ------------------------------------------------------------------ 更新

    private void update(double dt) {
        switch (gameState) {
            case PLAYING:
                updatePlaying(dt);
                break;
            case BATTLE:
                updateBattle(dt);
                break;
            case PAUSED:
                break;
            case GAME_OVER:
                break;
            case MENU:
                break;
            default:
                break;
        }

        if (bannerTimer > 0) {
            bannerTimer -= dt;
            if (bannerTimer <= 0) {
                banner = "";
            }
        }

        handleCommonInput();
    }

    private void updatePlaying(double dt) {
        updateMoveDirection();

        int beforeX = player.getX();
        int beforeY = player.getY();
        boolean startedStep = player.updateMovement(dt, gameMap, moveDirX, moveDirY);
        if (startedStep) {
            lastOverworldTileX = beforeX;
            lastOverworldTileY = beforeY;
        }

        camera.follow(player.getCenterPixelX(), player.getCenterPixelY());
        camera.update(dt);

        // 大地图近身攻击（空格）：站在相邻格即可出手，与旧版行为一致
        if (inputHandler.isKeyJustPressed(KeyCode.SPACE)) {
            for (Enemy enemy : enemies) {
                if (enemy.isAlive() && isInAttackRange(player, enemy)) {
                    battleService.overworldAttack(player, enemy);
                }
            }
        }

        // 走到敌人格 → 进入战斗。只在一步走完之后判定，避免视觉上还在滑行就切场景。
        if (player.isStepping()) {
            return;
        }
        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && player.getX() == enemy.getX() && player.getY() == enemy.getY()) {
                startBattle(enemy);
                return;
            }
        }
    }

    private void updateBattle(double dt) {
        Enemy enemy = battleService.getCurrentEnemy();
        if (enemy == null) {
            gameState = GameState.PLAYING;
            return;
        }

        if (battleService.isPlayerTurn()) {
            if (inputHandler.isKeyJustPressed(KeyCode.DIGIT1)) {
                battleService.playerAttack(player, enemy);
            } else if (inputHandler.isKeyJustPressed(KeyCode.DIGIT2)) {
                battleService.playerSkill(player, enemy, BattleService.SKILL_FIREBALL);
            } else if (inputHandler.isKeyJustPressed(KeyCode.DIGIT3)) {
                battleService.playerSkill(player, enemy, BattleService.SKILL_HEAL);
            } else if (inputHandler.isKeyJustPressed(KeyCode.DIGIT4)) {
                battleService.playerSkill(player, enemy, BattleService.SKILL_FREEZE);
            } else if (inputHandler.isKeyJustPressed(KeyCode.ESCAPE)) {
                if (battleService.tryEscape() == BattleService.EscapeResult.SUCCESS) {
                    retreatAfterEscape(enemy);
                    return;
                }
            }
        }

        // 敌人只在行动权交给它时才出手：每帧调用也只会打一次。
        if (!battleService.isPlayerTurn()) {
            battleService.enemyAttack(enemy, player);
        }

        if (!enemy.isAlive()) {
            battleService.grantDefeatReward(player, enemy);
            endBattleAndResume();
            return;
        }

        if (!player.isAlive()) {
            battleService.endBattle();
            gameState = GameState.GAME_OVER;
            System.out.println("玩家阵亡，游戏结束");
        }
    }

    private void handleCommonInput() {
        if (inputHandler.isKeyJustPressed(KeyCode.F5)) {
            if (saveLoadService.saveGame(player, gameMap, enemies)) {
                notify("已保存  map hash=0x" + Long.toHexString(gameMap.tileHash()).toUpperCase());
            } else {
                notify("保存失败，详见控制台日志");
            }
        }

        if (inputHandler.isKeyJustPressed(KeyCode.F9)) {
            GameMap loaded = saveLoadService.loadGame(player, enemies);
            if (loaded != null) {
                gameMap = loaded;
                camera = new Camera(width, height, gameMap.getPixelWidth(), gameMap.getPixelHeight());
                camera.snapTo(player.getCenterPixelX(), player.getCenterPixelY());
                battleService.endBattle();
                gameState = GameState.PLAYING;
                lastOverworldTileX = player.getX();
                lastOverworldTileY = player.getY();
                notify("已加载  map hash=0x" + Long.toHexString(gameMap.tileHash()).toUpperCase());
            } else {
                notify("没有可用存档（或存档缺少地图数据）");
            }
        }

        if (inputHandler.isKeyJustPressed(KeyCode.P)) {
            if (gameState == GameState.PLAYING) {
                gameState = GameState.PAUSED;
            } else if (gameState == GameState.PAUSED) {
                gameState = GameState.PLAYING;
            }
        }

        if (inputHandler.isKeyJustPressed(KeyCode.F3)) {
            debugOverlay = !debugOverlay;
        }
    }

    /**
     * 结算本帧的移动方向（X / Y 两个轴独立判定，因此支持斜向移动，与旧版行为一致）。
     *
     * <p>每个轴的优先级：本帧新按下的方向键（同一轴取最后按下的那个）＞ 仍然按住的粘性方向
     * ＞ 该轴上任一按住的方向键。这样短按一定生效，长按持续移动，两个方向键同时按住时也不会
     * 互相打架。
     */
    private void updateMoveDirection() {
        List<KeyCode> events = inputHandler.getFrameEvents();
        int pressedX = 0;
        int pressedY = 0;
        boolean hasPressedX = false;
        boolean hasPressedY = false;
        for (KeyCode code : events) {
            int[] dir = directionOf(code);
            if (dir == null) {
                continue;
            }
            if (dir[0] != 0) {
                pressedX = dir[0];
                hasPressedX = true;
            } else {
                pressedY = dir[1];
                hasPressedY = true;
            }
        }

        if (hasPressedX) {
            moveDirX = pressedX;
        } else if (moveDirX != 0 && !isAxisHeld(moveDirX, true)) {
            moveDirX = 0;
        }
        if (hasPressedY) {
            moveDirY = pressedY;
        } else if (moveDirY != 0 && !isAxisHeld(moveDirY, false)) {
            moveDirY = 0;
        }

        if (!hasPressedX && moveDirX == 0) {
            if (anyHeld(MOVE_LEFT)) {
                moveDirX = -1;
            } else if (anyHeld(MOVE_RIGHT)) {
                moveDirX = 1;
            }
        }
        if (!hasPressedY && moveDirY == 0) {
            if (anyHeld(MOVE_UP)) {
                moveDirY = -1;
            } else if (anyHeld(MOVE_DOWN)) {
                moveDirY = 1;
            }
        }
    }

    /** 某个轴上的方向键是否仍被按住。 */
    private boolean isAxisHeld(int direction, boolean horizontal) {
        if (horizontal) {
            return anyHeld(direction < 0 ? MOVE_LEFT : MOVE_RIGHT);
        }
        return anyHeld(direction < 0 ? MOVE_UP : MOVE_DOWN);
    }

    private boolean anyHeld(KeyCode[] keys) {
        for (KeyCode key : keys) {
            if (inputHandler.isKeyPressed(key)) {
                return true;
            }
        }
        return false;
    }

    private static int[] directionOf(KeyCode code) {
        if (code == KeyCode.W || code == KeyCode.UP) {
            return new int[]{0, -1};
        }
        if (code == KeyCode.S || code == KeyCode.DOWN) {
            return new int[]{0, 1};
        }
        if (code == KeyCode.A || code == KeyCode.LEFT) {
            return new int[]{-1, 0};
        }
        if (code == KeyCode.D || code == KeyCode.RIGHT) {
            return new int[]{1, 0};
        }
        return null;
    }

    private void startBattle(Enemy enemy) {
        gameState = GameState.BATTLE;
        battleService.startBattle(player, enemy);
    }

    /**
     * 逃跑成功后：结束战斗并把玩家挪到安全格，否则下一帧立刻重新触发同一场战斗。
     */
    private void retreatAfterEscape(Enemy enemy) {
        int[] destination = RetreatPlanner.escapeDestination(
                gameMap, player.getX(), player.getY(),
                lastOverworldTileX, lastOverworldTileY,
                enemy.getX(), enemy.getY());

        battleService.endBattle();
        player.setPosition(destination[0], destination[1]);
        gameState = GameState.PLAYING;
        lastOverworldTileX = destination[0];
        lastOverworldTileY = destination[1];
        notify("逃跑成功，退回 (" + destination[0] + ", " + destination[1] + ")");
        System.out.println("逃跑成功，玩家退回到 (" + destination[0] + ", " + destination[1] + ")");
    }

    private void endBattleAndResume() {
        battleService.endBattle();
        gameState = GameState.PLAYING;
    }

    private boolean isInAttackRange(Player player, Enemy enemy) {
        int dx = Math.abs(player.getX() - enemy.getX());
        int dy = Math.abs(player.getY() - enemy.getY());
        return dx <= 1 && dy <= 1;
    }

    private void notify(String message) {
        this.banner = message;
        this.bannerTimer = BANNER_SECONDS;
    }

    // ------------------------------------------------------------------ 渲染

    private void render() {
        gc.setFill(Palette.PANEL_BASE);
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
            case MENU:
                // M1 不进入主菜单（见 GameState 注释），M4 在此接入菜单 screen。
                break;
            default:
                break;
        }
    }

    private void renderPlaying() {
        int camX = camera.getRenderX();
        int camY = camera.getRenderY();

        gameMap.render(gc, camX, camY);

        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.render(gc, camX, camY);
            }
        }
        player.render(gc, camX, camY);

        renderHud();
        if (debugOverlay) {
            renderDebugOverlay();
        }
    }

    /**
     * 顶部状态条（HP / MP / EXP）+ 底部操作提示 + 临时提示条。
     */
    private void renderHud() {
        gc.setFont(Font.font(14));

        // 面板底
        gc.setFill(Color.rgb(30, 39, 44, 0.82));
        gc.fillRect(8, 8, 236, 96);
        gc.setStroke(Palette.FRAME_GOLD);
        gc.setLineWidth(2);
        gc.strokeRect(8, 8, 236, 96);
        gc.setLineWidth(1);

        gc.setFill(Palette.FRAME_GOLD);
        gc.fillText(player.getName() + "  Lv." + player.getLevel(), 16, 28);
        gc.setFill(Palette.TEXT_PRIMARY);
        gc.fillText("(" + player.getX() + ", " + player.getY() + ")", 184, 28);

        drawBar(16, 36, 220, 16, player.getHp(), player.getMaxHp(), Palette.HP_RED, "HP");
        drawBar(16, 58, 220, 16, player.getMp(), player.getMaxMp(), Palette.MP_BLUE, "MP");
        drawBar(16, 80, 220, 16, player.getExp(), player.getExpToNextLevel(), Palette.EXP_GOLD, "EXP");

        gc.setFill(Palette.TEXT_PRIMARY);
        gc.fillText("WASD/方向键-移动 | 空格-攻击 | P-暂停 | F5-保存 | F9-加载 | F3-调试",
                10, height - 10);

        if (!banner.isEmpty()) {
            gc.setFont(Font.font(18));
            gc.setFill(Palette.PANEL_BASE);
            gc.fillRect(width / 2 - 260, 14, 520, 34);
            gc.setStroke(Palette.FRAME_GOLD);
            gc.setLineWidth(2);
            gc.strokeRect(width / 2 - 260, 14, 520, 34);
            gc.setLineWidth(1);
            gc.setFill(Palette.FRAME_GOLD);
            gc.fillText(banner, width / 2 - 246, 38);
            gc.setFont(Font.font(14));
        }
    }

    private void drawBar(double x, double y, double w, double h, int value, int max, Color color, String label) {
        gc.setFill(Palette.SLOT_INNER);
        gc.fillRect(x, y, w, h);
        double ratio = max <= 0 ? 0 : Math.min(1.0, (double) value / max);
        gc.setFill(color);
        gc.fillRect(x, y, w * ratio, h);
        gc.setStroke(Palette.FRAME_SHADOW);
        gc.setLineWidth(1);
        gc.strokeRect(x, y, w, h);
        gc.setFill(Palette.TEXT_PRIMARY);
        gc.fillText(label + " " + value + "/" + max, x + 6, y + h - 4);
    }

    /**
     * F3 调试覆盖层：把“摄像机偏移 / 玩家格 / 帧率 / 输入缓冲深度 / 地图哈希”直接画进画面，
     * 使截图本身可自证（无需另附日志）。
     */
    private void renderDebugOverlay() {
        int camX = camera.getRenderX();
        int camY = camera.getRenderY();
        gc.setFont(Font.font("Monospaced", 14));

        String[] lines = {
                String.format("F3 DEBUG   fps=%.1f", fps),
                String.format("tile=(%d,%d) cam=(%d,%d) step=%.2f", player.getX(), player.getY(), camX, camY,
                        player.getStepProgress()),
                String.format("events=%d buffered=%d state=%s", inputHandler.getFrameEvents().size(),
                        inputHandler.getBufferedCount(), gameState),
                String.format("map=%dx%d world=%dx%d seed=0x%X", gameMap.getWidth(), gameMap.getHeight(),
                        gameMap.getPixelWidth(), gameMap.getPixelHeight(), gameMap.getSeed()),
                String.format("hash=0x%016X enemies=%d", gameMap.tileHash(), liveEnemyCount())
        };

        double boxW = 470;
        double boxH = lines.length * 18 + 16;
        double boxX = width - boxW - 10;
        double boxY = 10;
        gc.setFill(Color.rgb(16, 21, 24, 0.86));
        gc.fillRect(boxX, boxY, boxW, boxH);
        gc.setStroke(Palette.FRAME_GOLD);
        gc.setLineWidth(1);
        gc.strokeRect(boxX, boxY, boxW, boxH);
        gc.setFill(Palette.TEXT_PRIMARY);
        for (int i = 0; i < lines.length; i++) {
            gc.fillText(lines[i], boxX + 8, boxY + 22 + i * 18);
        }
        gc.setFont(Font.font(14));
    }

    private int liveEnemyCount() {
        int count = 0;
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 战斗界面（M1 版本：保持原本的信息结构，配色改为已批准调色板，并补上回合指示与冰冻术按键）。
     * M2 会替换为带演出与跳字的完整战斗界面。
     */
    private void renderBattle() {
        Enemy enemy = battleService.getCurrentEnemy();

        gc.setFill(Palette.PANEL_BASE);
        gc.fillRect(0, 0, width, height);

        // 顶部：回合指示
        gc.setFont(Font.font(20));
        gc.setFill(Palette.FRAME_GOLD);
        String turnText = battleService.isPlayerTurn() ? "▶ 当前行动方：玩家" : "▶ 当前行动方：敌人";
        gc.fillText(turnText, 40, 44);

        // 战斗舞台
        double stageX = 40;
        double stageY = 70;
        double stageW = width - 80;
        double stageH = 300;
        gc.setFill(Palette.SLOT_INNER);
        gc.fillRect(stageX, stageY, stageW, stageH);
        gc.setStroke(Palette.FRAME_GOLD);
        gc.setLineWidth(2);
        gc.strokeRect(stageX, stageY, stageW, stageH);

        drawPlayerSprite(stageX + 150, stageY + stageH / 2);
        if (enemy != null) {
            drawEnemySprite(enemy, stageX + stageW - 150, stageY + stageH / 2);
        }

        // 左下：玩家状态
        gc.setFont(Font.font(16));
        gc.setFill(Palette.TEXT_PRIMARY);
        double panelY = stageY + stageH + 24;
        gc.fillText(player.getName() + "  Lv." + player.getLevel(), 40, panelY + 18);
        drawBar(40, panelY + 28, 300, 18, player.getHp(), player.getMaxHp(), Palette.HP_RED, "HP");
        drawBar(40, panelY + 52, 300, 18, player.getMp(), player.getMaxMp(), Palette.MP_BLUE, "MP");

        // 右下：敌人状态
        if (enemy != null) {
            gc.setFill(Palette.TEXT_PRIMARY);
            gc.fillText(enemy.getName() + "  Lv." + enemy.getLevel(), 760, panelY + 18);
            drawBar(760, panelY + 28, 300, 18, enemy.getHp(), enemy.getMaxHp(), Palette.HP_RED, "HP");
        }

        // 指令卡
        double cardY = panelY + 82;
        drawCommandCard(40, cardY, "1  普通攻击");
        drawCommandCard(270, cardY, "2  火球术 10MP");
        drawCommandCard(500, cardY, "3  治疗术 15MP");
        drawCommandCard(730, cardY, "4  冰冻术 12MP");
        drawCommandCard(960, cardY, "ESC  逃跑 50%");

        gc.setFont(Font.font(14));
        gc.setFill(Palette.TEXT_PRIMARY);
        gc.fillText("每回合只能行动一次：行动后行动权交给敌人，敌人还击一次；逃跑失败也会被还击一次。",
                40, height - 14);
    }

    private void drawCommandCard(double x, double y, String text) {
        gc.setFill(Palette.SLOT_INNER);
        gc.fillRect(x, y, 220, 56);
        gc.setStroke(Palette.FRAME_GOLD);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, 220, 56);
        gc.setLineWidth(1);
        gc.setFill(Palette.TEXT_PRIMARY);
        gc.setFont(Font.font(15));
        gc.fillText(text, x + 12, y + 34);
    }

    /** 战斗舞台上的玩家立绘：32×32 精灵的 4 倍放大绘制。 */
    private void drawPlayerSprite(double centerX, double centerY) {
        double scale = 4;
        double size = GameMap.TILE_SIZE * scale;
        double x = centerX - size / 2;
        double y = centerY - size / 2;

        gc.setFill(Color.rgb(0, 0, 0, 0.3));
        gc.fillOval(x + 3 * scale, y + 25 * scale, 26 * scale, 6 * scale);

        gc.setFill(Palette.POTATO_SKIN);
        gc.fillOval(x + 3 * scale, y + 3 * scale, 26 * scale, 26 * scale);
        gc.setFill(Palette.POTATO_SHADE);
        gc.fillOval(x + 6 * scale, y + 17 * scale, 20 * scale, 11 * scale);
        gc.setStroke(Palette.FRAME_SHADOW);
        gc.setLineWidth(3);
        gc.strokeOval(x + 3 * scale, y + 3 * scale, 26 * scale, 26 * scale);
        gc.setLineWidth(1);

        gc.setFill(Palette.PANEL_BASE);
        gc.fillOval(x + 9 * scale, y + 11 * scale, 4 * scale, 4 * scale);
        gc.fillOval(x + 18 * scale, y + 11 * scale, 4 * scale, 4 * scale);
        gc.setFill(Palette.TEXT_PRIMARY);
        gc.fillOval(x + 10.5 * scale, y + 12 * scale, 1.6 * scale, 1.6 * scale);
        gc.fillOval(x + 19.5 * scale, y + 12 * scale, 1.6 * scale, 1.6 * scale);
    }

    /** 战斗舞台上的敌人立绘：按类型放大绘制。 */
    private void drawEnemySprite(Enemy enemy, double centerX, double centerY) {
        double scale = 4;
        double size = GameMap.TILE_SIZE * scale;
        double x = centerX - size / 2;
        double y = centerY - size / 2;
        Color color = enemy.getType().getColor();

        gc.setFill(Color.rgb(0, 0, 0, 0.3));
        gc.fillOval(x + 5 * scale, y + 25 * scale, 22 * scale, 6 * scale);

        switch (enemy.getType()) {
            case SLIME:
                gc.setFill(color);
                gc.fillOval(x + 4 * scale, y + 8 * scale, 24 * scale, 20 * scale);
                gc.setFill(Palette.BUSH_LIGHT);
                gc.fillOval(x + 8 * scale, y + 10 * scale, 8 * scale, 6 * scale);
                gc.setFill(Color.WHITE);
                gc.fillOval(x + 10 * scale, y + 14 * scale, 4 * scale, 4 * scale);
                gc.fillOval(x + 18 * scale, y + 14 * scale, 4 * scale, 4 * scale);
                gc.setFill(Palette.PANEL_BASE);
                gc.fillOval(x + 11 * scale, y + 15 * scale, 2 * scale, 2 * scale);
                gc.fillOval(x + 19 * scale, y + 15 * scale, 2 * scale, 2 * scale);
                break;

            case GOBLIN:
                gc.setFill(color);
                gc.fillPolygon(
                        new double[]{x + 16 * scale, x + 6 * scale, x + 26 * scale},
                        new double[]{y + 6 * scale, y + 28 * scale, y + 28 * scale},
                        3
                );
                gc.fillOval(x + 9 * scale, y + 4 * scale, 14 * scale, 14 * scale);
                gc.setFill(Palette.HP_RED);
                gc.fillOval(x + 12 * scale, y + 9 * scale, 3 * scale, 3 * scale);
                gc.fillOval(x + 17 * scale, y + 9 * scale, 3 * scale, 3 * scale);
                break;

            case WOLF:
                gc.setFill(color);
                gc.fillRect(x + 5 * scale, y + 12 * scale, 22 * scale, 13 * scale);
                gc.fillOval(x + 18 * scale, y + 7 * scale, 12 * scale, 12 * scale);
                gc.fillPolygon(
                        new double[]{x + 20 * scale, x + 18 * scale, x + 22 * scale},
                        new double[]{y + 8 * scale, y + 3 * scale, y + 8 * scale},
                        3
                );
                gc.fillPolygon(
                        new double[]{x + 27 * scale, x + 25 * scale, x + 29 * scale},
                        new double[]{y + 8 * scale, y + 3 * scale, y + 8 * scale},
                        3
                );
                gc.setFill(Palette.EXP_GOLD);
                gc.fillOval(x + 20 * scale, y + 10 * scale, 3 * scale, 3 * scale);
                gc.fillOval(x + 25 * scale, y + 10 * scale, 3 * scale, 3 * scale);
                break;
        }
    }

    private void renderPaused() {
        gc.setFill(Color.rgb(0, 0, 0, 0.55));
        gc.fillRect(0, 0, width, height);

        gc.setFill(Palette.PANEL_BASE);
        gc.fillRect(width / 2 - 160, height / 2 - 70, 320, 120);
        gc.setStroke(Palette.FRAME_GOLD);
        gc.setLineWidth(2);
        gc.strokeRect(width / 2 - 160, height / 2 - 70, 320, 120);
        gc.setLineWidth(1);

        gc.setFont(Font.font(24));
        gc.setFill(Palette.FRAME_GOLD);
        gc.fillText("游戏暂停", width / 2 - 56, height / 2 - 20);
        gc.setFont(Font.font(16));
        gc.setFill(Palette.TEXT_PRIMARY);
        gc.fillText("按 P 继续", width / 2 - 40, height / 2 + 20);
        gc.setFont(Font.font(14));
    }

    private void renderGameOver() {
        gc.setFill(Color.web("#231010"));
        gc.fillRect(0, 0, width, height);

        gc.setFill(Palette.PANEL_BASE);
        gc.fillRect(width / 2 - 220, height / 2 - 90, 440, 160);
        gc.setStroke(Palette.HP_RED);
        gc.setLineWidth(3);
        gc.strokeRect(width / 2 - 220, height / 2 - 90, 440, 160);
        gc.setLineWidth(1);

        gc.setFont(Font.font(30));
        gc.setFill(Palette.HP_RED);
        gc.fillText("游戏结束", width / 2 - 70, height / 2 - 30);
        gc.setFont(Font.font(16));
        gc.setFill(Palette.TEXT_PRIMARY);
        gc.fillText("按 F9 载入存档重新开始", width / 2 - 96, height / 2 + 20);
        gc.fillText("本局等级 Lv." + player.getLevel() + "  经验 " + player.getExp(), width / 2 - 96, height / 2 + 46);
        gc.setFont(Font.font(14));
    }
}
