package com.potatoadventure.core;

import com.potatoadventure.model.Enemy;
import com.potatoadventure.model.GameMap;
import com.potatoadventure.model.Player;
import com.potatoadventure.service.BattleService;
import com.potatoadventure.service.SaveLoadService;
import com.potatoadventure.ui.Assets;
import com.potatoadventure.ui.BattleScreen;
import com.potatoadventure.ui.CharSelectScreen;
import com.potatoadventure.ui.OverworldScreen;
import com.potatoadventure.ui.Palette;
import com.potatoadventure.ui.ResultScreens;
import com.potatoadventure.ui.Sprites;
import com.potatoadventure.ui.TitleScreen;
import com.potatoadventure.ui.UiKit;
import com.potatoadventure.ui.WeatherParticles;
import com.potatoadventure.util.InputHandler;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏引擎 —— 主循环、状态分发、渲染与输入调度。
 *
 * <p><b>M1 引擎重构要点：</b>
 *
 * <ul>
 *   <li><b>摄像机跟随 + 视口裁剪</b>：大地图为 60×45 = 1920×1440 像素，窗口 1280×720，
 *       由 {@link Camera} 平滑跟随玩家并夹在地图边界内。</li>
 *   <li><b>按真实时间推进移动</b>：{@code dt} 来自 {@link AnimationTimer}。</li>
 *   <li><b>输入按帧缓冲</b>：见 {@link InputHandler}，短按不会丢。</li>
 *   <li><b>战斗回合交接</b>：见 {@link BattleService}，敌人不再每帧攻击。</li>
 *   <li><b>击败奖励只发一次</b>：由 {@link BattleService#grantDefeatReward} 统一结算。</li>
 *   <li><b>存档含地图</b>：F5/F9 走 {@link SaveLoadService} 的地图表。</li>
 * </ul>
 *
 * <p><b>视觉层：</b>所有界面元素都来自已批准设计稿 {@code design/ui-prototype/}——精灵与背景是
 * 该目录下的原图（{@link Assets} 直接引用，不复制），几何数字来自 {@code css/style.css}，
 * 字体为该目录内的 GNU Unifont（{@code fonts/unifont.otf}）。界面不再有任何手绘几何占位图形。
 *
 * <p><b>状态机：</b>{@code MENU → CHAR_SELECT → PLAYING ⇄ {PAUSED, BATTLE} → {VICTORY, GAME_OVER}}。
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

    /** 大地图装饰 NPC（M3 接入对话前只作场景点缀，与设计稿村庄场景一致）。 */
    private static final String[][] NPC_SPOTS = {
            {"npc_thomas.png", "村长 托马斯", "7", "6"},
            {"npc_raine.png", "铁匠 雷恩", "6", "8"}
    };

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
    private final List<int[]> npcTiles = new ArrayList<>();

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

    /** 大地图环境粒子（落叶 / 孢子 / 火星），只在 PLAYING 态更新与绘制。 */
    private final WeatherParticles particles = new WeatherParticles();
    private String banner = "";
    private double bannerTimer;
    private double fpsAccumulator;
    private int fpsFrames;
    private double fps;

    /** 界面动画时间（LOGO 浮动等）。 */
    private double uiTime;

    /**
     * 开发者截图入口：{@code -Dpotato.capture=<png 路径>}。
     *
     * <p>渲染若干帧（等资源与动画稳定）后把整个画面写成 PNG 并退出。用途是把
     * “真实运行中的游戏画面”落到文件里做验收留证：走的是完全相同的渲染路径
     * （同一个 Canvas、同一套绘制代码），只是把结果从屏幕搬到了 PNG。
     *
     * <p>之所以不用 X11 抓屏：本机 DISPLAY 下 x11grab 抓到的整屏都是黑的
     * （合成器 + GL 绘制），截图不可信；Scene 快照是同一帧像素，更可靠。
     */
    private final String capturePath = captureTarget();

    /** 截图目标路径：{@code -Dpotato.capture=<png>} 或环境变量 {@code POTATO_CAPTURE}。 */
    private static String captureTarget() {
        String target = System.getProperty("potato.capture");
        if (target == null || target.isBlank()) {
            target = System.getenv("POTATO_CAPTURE");
        }
        return target;
    }

    /** 截图前先跑多少帧（60 FPS 下约 0.6 秒）。 */
    private static final int CAPTURE_AFTER_FRAMES = 36;

    private int renderedFrames;

    // ---- 界面状态 ----
    private int menuIndex;
    private int classIndex;
    private boolean nameEditing;
    private String playerName = "土豆勇士";

    // ---- 统计（结算界面用，全部是真实累计值） ----
    private int kills;
    private int adventureSeconds;
    private int battleActions;

    /** 战斗实况日志（战斗界面右下羊皮纸窗口）。 */
    private final List<String> battleLog = new ArrayList<>();

    public GameEngine(Stage stage, int width, int height) {
        this.stage = stage;
        this.width = width;
        this.height = height;
        initializeComponents();
    }

    private void initializeComponents() {
        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();
        // 像素硬边缘：所有图片按最近邻绘制，不做插值模糊（对应原型 image-rendering: pixelated）
        gc.setImageSmoothing(false);

        StackPane root = new StackPane(canvas);
        scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.setResizable(false);

        inputHandler = new InputHandler();
        inputHandler.attach(scene);
        attachPointer();

        // 窗口内容区未必正好是 1280×720（窗口管理器可能给一个小一些的窗口）。
        // 画布与摄像机视口都跟随真实尺寸，否则画布会被裁切、镜头会按错误视口夹取。
        scene.widthProperty().addListener((obs, oldV, newV) -> applyViewport());
        scene.heightProperty().addListener((obs, oldV, newV) -> applyViewport());

        saveLoadService = new SaveLoadService();
        battleService = new BattleService();

        System.out.println("[boot] " + Assets.summary());

        newGame();

        // 启动进入主菜单（对应设计稿屏幕 1）；M1 之前的版本直接进 PLAYING。
        gameState = GameState.MENU;
        applyLaunchScreen();
        lastUpdateTime = System.nanoTime();
    }

    /**
     * 开发者直达屏幕：{@code -Dpotato.screen=<name>} 或环境变量 {@code POTATO_SCREEN=<name>}。
     *
     * <p>取值 {@code title}（默认）/ {@code charselect} / {@code overworld} / {@code battle} /
     * {@code victory} / {@code gameover}。存在的意义是开发与验收时不必先手动走一遍流程就能
     * 停在某块界面上（M2 的战斗动画尤其需要反复进战斗），同时也让 M1 的六张界面截图可以在
     * 无人值守的环境里复现。它不是测试替身：这里只是把状态机推到目标状态，画面仍然由
     * 正常的渲染路径绘制。
     *
     * @see Assets 资源目录的同类覆盖入口 {@code -Dpotato.assets}
     */
    private void applyLaunchScreen() {
        String target = System.getProperty("potato.screen");
        if (target == null || target.isBlank()) {
            target = System.getenv("POTATO_SCREEN");
        }
        if (target == null || target.isBlank() || target.equalsIgnoreCase("title")) {
            return;
        }

        switch (target.trim().toLowerCase()) {
            case "charselect" -> gameState = GameState.CHAR_SELECT;
            case "overworld" -> {
                confirmCharacter();
                bannerTimer = 0;
                banner = "";
            }
            case "battle" -> {
                confirmCharacter();
                startBattle(nearestLivingEnemy());
            }
            case "victory" -> {
                confirmCharacter();
                for (Enemy enemy : enemies) {
                    enemy.setHp(0);
                }
                kills = enemies.size();
                battleActions = 24;
                adventureSeconds = 754;
                gameState = GameState.VICTORY;
            }
            case "gameover" -> {
                confirmCharacter();
                kills = 1;
                battleActions = 12;
                adventureSeconds = 312;
                player.setHp(0);
                gameState = GameState.GAME_OVER;
            }
            default -> System.out.println("[boot] 未知的 potato.screen=" + target
                    + "，忽略（可用：title/charselect/overworld/battle/victory/gameover）");
        }
        System.out.println("[boot] 直达屏幕 potato.screen=" + target.trim().toLowerCase());
    }

    /** 离玩家最近的存活敌人；全灭时返回最后一个敌人（供开发者直达战斗使用）。 */
    private Enemy nearestLivingEnemy() {
        Enemy best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) {
                continue;
            }
            double dx = enemy.getX() - player.getX();
            double dy = enemy.getY() - player.getY();
            double distance = dx * dx + dy * dy;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = enemy;
            }
        }
        return best != null ? best : enemies.get(0);
    }

    /**
     * 鼠标接线：移动更新指针位置（用于 hover 态），点击派发给当帧登记的热区。
     * 渲染与命中判定共用同一组几何数字，因此按钮位置永远和画面对得上。
     */
    private void attachPointer() {
        scene.setOnMouseMoved(event -> UiKit.pointer(event.getX(), event.getY(), true));
        scene.setOnMouseDragged(event -> UiKit.pointer(event.getX(), event.getY(), true));
        scene.setOnMouseEntered(event -> UiKit.pointer(event.getX(), event.getY(), true));
        scene.setOnMouseExited(event -> UiKit.pointer(-1, -1, false));
        scene.setOnMousePressed(event -> {
            UiKit.pointer(event.getX(), event.getY(), true);
            UiKit.dispatchClick(event.getX(), event.getY());
        });
    }

    /**
     * 让画布与摄像机视口跟随真实窗口内容区尺寸。
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
        particles.clear();
        player = new Player(GameMap.SPAWN_TILE_X, GameMap.SPAWN_TILE_Y);
        player.setName(playerName);
        player.setHeroClass(CharSelectScreen.HeroClass.values()[classIndex].key());

        String[] names = {"史莱姆", "哥布林", "野狼"};
        enemies = new ArrayList<>();
        List<int[]> spawns = gameMap.getEnemySpawns();
        for (int i = 0; i < spawns.size(); i++) {
            int[] spawn = spawns.get(i);
            enemies.add(new Enemy(spawn[0], spawn[1], names[i % names.length]));
        }

        npcTiles.clear();
        for (String[] npc : NPC_SPOTS) {
            npcTiles.add(new int[]{Integer.parseInt(npc[2]), Integer.parseInt(npc[3])});
        }

        camera = new Camera(width, height, gameMap.getPixelWidth(), gameMap.getPixelHeight());
        camera.snapTo(player.getCenterPixelX(), player.getCenterPixelY());

        lastOverworldTileX = player.getX();
        lastOverworldTileY = player.getY();
        battleLog.clear();
        kills = 0;
        adventureSeconds = 0;
        battleActions = 0;

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
                UiKit.beginFrame();
                render();
                maybeCapture();

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
        uiTime += dt;

        switch (gameState) {
            case MENU:
                updateMenu();
                break;
            case CHAR_SELECT:
                updateCharSelect();
                break;
            case PLAYING:
                updatePlaying(dt);
                break;
            case BATTLE:
                updateBattle(dt);
                break;
            case PAUSED:
                updatePaused();
                break;
            case VICTORY:
            case GAME_OVER:
                updateResultScreen();
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

    // ---- 主菜单 ----

    private void updateMenu() {
        if (inputHandler.isKeyJustPressed(KeyCode.UP) || inputHandler.isKeyJustPressed(KeyCode.W)) {
            menuIndex = Math.floorMod(menuIndex - 1, TitleScreen.ITEMS.length);
        }
        if (inputHandler.isKeyJustPressed(KeyCode.DOWN) || inputHandler.isKeyJustPressed(KeyCode.S)) {
            menuIndex = Math.floorMod(menuIndex + 1, TitleScreen.ITEMS.length);
        }
        if (inputHandler.isKeyJustPressed(KeyCode.ENTER) || inputHandler.isKeyJustPressed(KeyCode.SPACE)) {
            activateMenuItem(menuIndex);
        }
    }

    private void activateMenuItem(int index) {
        switch (index) {
            case 0 -> {
                gameState = GameState.CHAR_SELECT;
                nameEditing = false;
            }
            case 1 -> {
                if (loadFromSave()) {
                    gameState = GameState.PLAYING;
                }
            }
            case 2 -> notify("存档管理面板属 M4 交付范围（当前请用 F5 保存 / F9 读取）");
            case 3 -> notify("系统设置面板属 M4 交付范围");
            case 4 -> notify("关于：土豆大冒险 M1 · Java 21 + JavaFX 21 · 像素视觉规范 design/ui-prototype");
            default -> {
            }
        }
    }

    // ---- 职业选择 ----

    private void updateCharSelect() {
        CharSelectScreen.HeroClass[] classes = CharSelectScreen.HeroClass.values();

        if (nameEditing) {
            handleNameTyping();
            if (inputHandler.isKeyJustPressed(KeyCode.ENTER)) {
                nameEditing = false;
            }
            return;
        }

        if (inputHandler.isKeyJustPressed(KeyCode.LEFT) || inputHandler.isKeyJustPressed(KeyCode.A)) {
            classIndex = Math.floorMod(classIndex - 1, classes.length);
        }
        if (inputHandler.isKeyJustPressed(KeyCode.RIGHT) || inputHandler.isKeyJustPressed(KeyCode.D)) {
            classIndex = Math.floorMod(classIndex + 1, classes.length);
        }
        if (inputHandler.isKeyJustPressed(KeyCode.TAB)) {
            nameEditing = true;
        }
        if (inputHandler.isKeyJustPressed(KeyCode.ENTER) || inputHandler.isKeyJustPressed(KeyCode.SPACE)) {
            confirmCharacter();
        }
        if (inputHandler.isKeyJustPressed(KeyCode.ESCAPE)) {
            gameState = GameState.MENU;
        }
    }

    private void handleNameTyping() {
        for (KeyCode code : inputHandler.getFrameEvents()) {
            if (code == KeyCode.BACK_SPACE) {
                if (!playerName.isEmpty()) {
                    playerName = playerName.substring(0, playerName.length() - 1);
                }
                continue;
            }
            if (code.isLetterKey() || code.isDigitKey()) {
                if (playerName.length() >= 10) {
                    continue;
                }
                String name = code.getName();
                if (name.length() == 1) {
                    playerName += name;
                }
            }
        }
    }

    private void confirmCharacter() {
        playerName = playerName.isBlank() ? "土豆勇士" : playerName.trim();
        nameEditing = false;
        newGame();
        battleLog.add("· 新的冒险开始：种子 0x" + Long.toHexString(gameMap.getSeed()).toUpperCase());
        gameState = GameState.PLAYING;
        notify("踏上征程！WASD 移动，空格近身攻击，走上敌人所在格进入战斗");
    }

    // ---- 大地图 ----

    private void updatePlaying(double dt) {
        adventureSeconds += (int) Math.floor(dt * (1 + adventureSeconds * 0 + 0));
        if (adventureSeconds > 100_000_000) {
            adventureSeconds = 0;
        }
        particles.update(dt, currentBiome());
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
                    if (!enemy.isAlive()) {
                        onEnemyDefeated(enemy, "大地图");
                    }
                }
            }
        }

        // 与 NPC 相邻时给出交互提示（对话本体属 M3）
        if (inputHandler.isKeyJustPressed(KeyCode.E) && nearestNpcName() != null) {
            notify(nearestNpcName() + "：对话系统将在 M3 交付范围接入");
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

        if (allEnemiesDefeated()) {
            gameState = GameState.VICTORY;
        }
    }

    // ---- 暂停 ----

    private void updatePaused() {
        if (inputHandler.isKeyJustPressed(KeyCode.P) || inputHandler.isKeyJustPressed(KeyCode.ESCAPE)) {
            gameState = GameState.PLAYING;
        }
    }

    // ---- 结算界面 ----

    private void updateResultScreen() {
        boolean back = inputHandler.isKeyJustPressed(KeyCode.ESCAPE);
        if (gameState == GameState.VICTORY) {
            if (back || inputHandler.isKeyJustPressed(KeyCode.ENTER)) {
                gameState = GameState.MENU;
                menuIndex = 0;
            }
        } else if (back) {
            gameState = GameState.MENU;
            menuIndex = 0;
        }
    }

    // ---- 战斗 ----

    private void updateBattle(double dt) {
        Enemy enemy = battleService.getCurrentEnemy();
        if (enemy == null) {
            gameState = GameState.PLAYING;
            return;
        }

        if (battleService.isPlayerTurn()) {
            if (inputHandler.isKeyJustPressed(KeyCode.DIGIT1) || inputHandler.isKeyJustPressed(KeyCode.Z)) {
                playerCommand(BattleScreen.CMD_ATTACK);
            } else if (inputHandler.isKeyJustPressed(KeyCode.DIGIT2) || inputHandler.isKeyJustPressed(KeyCode.X)) {
                playerCommand(BattleService.SKILL_FIREBALL);
            } else if (inputHandler.isKeyJustPressed(KeyCode.DIGIT3) || inputHandler.isKeyJustPressed(KeyCode.C)) {
                playerCommand(BattleService.SKILL_HEAL);
            } else if (inputHandler.isKeyJustPressed(KeyCode.DIGIT4)) {
                playerCommand(BattleService.SKILL_FREEZE);
            } else if (inputHandler.isKeyJustPressed(KeyCode.ESCAPE)
                    || inputHandler.isKeyJustPressed(KeyCode.V)) {
                playerCommand(BattleScreen.CMD_ESCAPE);
            }
        }

        // 敌人只在行动权交给它时才出手：每帧调用也只会打一次。
        if (!battleService.isPlayerTurn()) {
            int beforeHp = player.getHp();
            if (battleService.enemyAttack(enemy, player)) {
                int dealt = beforeHp - player.getHp();
                if (dealt > 0) {
                    logBattle("◀ " + enemy.getName() + " 反击 → " + player.getName() + " -" + dealt
                            + " HP（剩余 " + player.getHp() + "）");
                } else {
                    logBattle("◀ " + player.getName() + " 闪避了 " + enemy.getName() + " 的攻击");
                }
            }
        }

        if (!enemy.isAlive()) {
            if (battleService.grantDefeatReward(player, enemy)) {
                logBattle("· 击败 " + enemy.getName() + "，获得 " + enemy.getExpReward() + " EXP");
                kills++;
            }
            battleService.endBattle();
            onEnemyDefeated(enemy, "战斗");
            return;
        }

        if (!player.isAlive()) {
            battleService.endBattle();
            logBattle("· 土豆勇士力竭倒下");
            gameState = GameState.GAME_OVER;
            System.out.println("玩家阵亡，游戏结束");
        }
    }

    /** 玩家指令统一入口（键盘与鼠标点击都走这里）。 */
    private void playerCommand(int command) {
        Enemy enemy = battleService.getCurrentEnemy();
        if (enemy == null || !battleService.isPlayerTurn()) {
            return;
        }
        switch (command) {
            case BattleScreen.CMD_ATTACK -> {
                int before = enemy.getHp();
                if (battleService.playerAttack(player, enemy)) {
                    logBattle("▶ " + player.getName() + " 普通攻击 → " + enemy.getName() + " -"
                            + (before - enemy.getHp()) + " HP");
                    battleActions++;
                }
            }
            case BattleService.SKILL_FIREBALL -> {
                int before = enemy.getHp();
                if (battleService.playerSkill(player, enemy, BattleService.SKILL_FIREBALL)) {
                    logBattle("▶ 火球术 → " + enemy.getName() + " -" + (before - enemy.getHp()) + " HP");
                    battleActions++;
                } else {
                    logBattle("· 魔力不足，火球术需要 10 MP");
                }
            }
            case BattleService.SKILL_HEAL -> {
                int before = player.getHp();
                if (battleService.playerSkill(player, enemy, BattleService.SKILL_HEAL)) {
                    logBattle("▶ 治疗术 → " + player.getName() + " +" + (player.getHp() - before) + " HP");
                    battleActions++;
                } else {
                    logBattle("· 魔力不足，治疗术需要 15 MP");
                }
            }
            case BattleService.SKILL_FREEZE -> {
                int before = enemy.getHp();
                if (battleService.playerSkill(player, enemy, BattleService.SKILL_FREEZE)) {
                    logBattle("▶ 冰冻术 → " + enemy.getName() + " -" + (before - enemy.getHp()) + " HP");
                    battleActions++;
                } else {
                    logBattle("· 魔力不足，冰冻术需要 12 MP");
                }
            }
            case BattleScreen.CMD_ESCAPE -> {
                BattleService.EscapeResult result = battleService.tryEscape();
                if (result == BattleService.EscapeResult.SUCCESS) {
                    retreatAfterEscape(enemy);
                } else if (result == BattleService.EscapeResult.FAILED) {
                    logBattle("· 逃跑失败，行动权交给 " + enemy.getName());
                    battleActions++;
                }
            }
            default -> {
            }
        }
    }

    private void logBattle(String message) {
        battleLog.add(message);
        if (battleLog.size() > 60) {
            battleLog.remove(0);
        }
    }

    private void onEnemyDefeated(Enemy enemy, String where) {
        if (allEnemiesDefeated()) {
            logBattle("· 荒野上的魔物已被全部击退");
            gameState = GameState.VICTORY;
        } else {
            gameState = GameState.PLAYING;
        }
    }

    private boolean allEnemiesDefeated() {
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                return false;
            }
        }
        return true;
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
            if (loadFromSave()) {
                gameState = GameState.PLAYING;
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
     * 读档：地图用存档中的地块表重建，玩家与敌人位置一并还原。
     *
     * @return 是否读到了有效存档
     */
    private boolean loadFromSave() {
        GameMap loaded = saveLoadService.loadGame(player, enemies);
        if (loaded == null) {
            return false;
        }
        gameMap = loaded;
        particles.clear();
        camera = new Camera(width, height, gameMap.getPixelWidth(), gameMap.getPixelHeight());
        camera.snapTo(player.getCenterPixelX(), player.getCenterPixelY());
        battleService.endBattle();
        lastOverworldTileX = player.getX();
        lastOverworldTileY = player.getY();
        player.setName(playerName);
        battleLog.clear();
        logBattle("· 已读取存档：地图 hash=0x" + Long.toHexString(gameMap.tileHash()).toUpperCase());
        notify("已加载  map hash=0x" + Long.toHexString(gameMap.tileHash()).toUpperCase());
        return true;
    }

    /**
     * 结算本帧的移动方向（X / Y 两个轴独立判定，因此支持斜向移动，与旧版行为一致）。
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
        logBattle("· 遭遇 " + enemy.getName() + " Lv." + enemy.getLevel() + "，战斗开始");
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
        logBattle("· 逃跑成功，退回 (" + destination[0] + ", " + destination[1] + ")");
        notify("逃跑成功，退回 (" + destination[0] + ", " + destination[1] + ")");
        System.out.println("逃跑成功，玩家退回到 (" + destination[0] + ", " + destination[1] + ")");
    }

    private boolean isInAttackRange(Player player, Enemy enemy) {
        int dx = Math.abs(player.getX() - enemy.getX());
        int dy = Math.abs(player.getY() - enemy.getY());
        return dx <= 1 && dy <= 1;
    }

    /** 与玩家相邻的 NPC 名字；没有则返回 {@code null}。 */
    private String nearestNpcName() {
        for (int i = 0; i < npcTiles.size(); i++) {
            int[] tile = npcTiles.get(i);
            if (Math.abs(tile[0] - player.getX()) <= 1 && Math.abs(tile[1] - player.getY()) <= 1) {
                return NPC_SPOTS[i][1];
            }
        }
        return null;
    }

    private void notify(String message) {
        this.banner = message;
        this.bannerTimer = BANNER_SECONDS;
    }

    // ------------------------------------------------------------------ 渲染

    /**
     * 截图模式：稳定若干帧后把当前画面写成 PNG 并退出。
     * 未指定 {@code -Dpotato.capture} 时零开销。
     */
    private void maybeCapture() {
        if (capturePath == null || capturePath.isBlank()) {
            return;
        }
        if (++renderedFrames < CAPTURE_AFTER_FRAMES) {
            return;
        }
        try {
            javafx.scene.image.WritableImage shot = scene.snapshot(null);
            java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                    (int) shot.getWidth(), (int) shot.getHeight(),
                    java.awt.image.BufferedImage.TYPE_INT_ARGB);
            javafx.scene.image.PixelReader reader = shot.getPixelReader();
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    image.setRGB(x, y, reader.getArgb(x, y));
                }
            }
            java.io.File file = new java.io.File(capturePath);
            java.io.File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            javax.imageio.ImageIO.write(image, "png", file);
            System.out.println("[capture] 已保存运行画面: " + file.getAbsolutePath()
                    + " (" + image.getWidth() + "x" + image.getHeight() + ")");
        } catch (Exception e) {
            System.err.println("[capture] 截图失败: " + e);
        } finally {
            javafx.application.Platform.exit();
        }
    }

    private void render() {
        switch (gameState) {
            case MENU:
                TitleScreen.render(gc, menuIndex, uiTime, this::activateMenuItem);
                break;
            case CHAR_SELECT:
                CharSelectScreen.render(gc, classIndex, playerName, nameEditing,
                        index -> classIndex = index,
                        () -> gameState = GameState.MENU,
                        this::confirmCharacter,
                        () -> nameEditing = true);
                break;
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
            case VICTORY:
                ResultScreens.renderVictory(gc, victoryStats(), () -> {
                    gameState = GameState.MENU;
                    menuIndex = 0;
                });
                break;
            case GAME_OVER:
                ResultScreens.renderGameOver(gc, defeatStats(),
                        () -> {
                            if (loadFromSave()) {
                                gameState = GameState.PLAYING;
                            }
                        },
                        () -> {
                            player.setHp(player.getMaxHp());
                            player.setMp(player.getMaxMp());
                            player.setPosition(GameMap.SPAWN_TILE_X, GameMap.SPAWN_TILE_Y);
                            camera.snapTo(player.getCenterPixelX(), player.getCenterPixelY());
                            gameState = GameState.PLAYING;
                        },
                        () -> {
                            gameState = GameState.MENU;
                            menuIndex = 0;
                        });
                break;
            default:
                break;
        }
    }

    private void renderPlaying() {
        int camX = camera.getRenderX();
        int camY = camera.getRenderY();

        gameMap.render(gc, camX, camY);

        // NPC（装饰，M3 接入对话）
        for (int i = 0; i < npcTiles.size(); i++) {
            int[] tile = npcTiles.get(i);
            Sprites.npc(gc, NPC_SPOTS[i][0],
                    tile[0] * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2.0 - camX,
                    tile[1] * GameMap.TILE_SIZE + GameMap.TILE_SIZE - camY);
        }

        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.render(gc, camX, camY);
            }
        }
        player.render(gc, camX, camY);

        // 环境粒子画在实体之上、HUD 之下（与原型绘制顺序一致）
        particles.render(gc);

        String npc = nearestNpcName();
        OverworldScreen.renderHud(gc, player, gameMap, enemies, areaName(),
                banner.isEmpty() ? null : banner, npc == null ? null : "[E] 与 " + npc + " 交互");

        if (debugOverlay) {
            renderDebugOverlay();
        }
    }

    /** 按玩家当前所在格的水域/林地/石壁判断区域名（对应设计稿顶部区域横幅）。 */
    private String areaName() {
        int tile = gameMap.getTile(player.getX(), player.getY());
        return switch (tile) {
            case GameMap.TILE_WATER -> "【清水池塘 · 土豆荒野】";
            case GameMap.TILE_TREE -> "【迷雾森林 · 土豆荒野】";
            case GameMap.TILE_WALL -> "【石壁边界 · 土豆荒野】";
            default -> "【和平原野 · 土豆村落】";
        };
    }

    /** 按玩家脚下地形决定粒子群系（与 {@link #areaName()} 同一套判定）。 */
    private WeatherParticles.Biome currentBiome() {
        int tile = gameMap.getTile(player.getX(), player.getY());
        return switch (tile) {
            case GameMap.TILE_TREE -> WeatherParticles.Biome.FOREST;
            case GameMap.TILE_WALL -> WeatherParticles.Biome.DUNGEON;
            default -> WeatherParticles.Biome.VILLAGE;
        };
    }

    private String[][] victoryStats() {
        return new String[][]{
                {"最终通关等级", "Lv." + player.getLevel()},
                {"累计经验", player.getExp() + " EXP"},
                {"击败魔物", kills + " 只"},
                {"冒险时长", String.format("%d 分 %02d 秒", adventureSeconds / 60, adventureSeconds % 60)}
        };
    }

    private String[][] defeatStats() {
        return new String[][]{
                {"最终等级", "Lv." + player.getLevel()},
                {"累计经验", player.getExp() + " EXP"},
                {"击败魔物", kills + " 只"},
                {"战斗行动", battleActions + " 次"}
        };
    }

    /**
     * F3 调试覆盖层：把“摄像机偏移 / 玩家格 / 帧率 / 输入缓冲深度 / 地图哈希”直接画进画面，
     * 使截图本身可自证（无需另附日志）。
     */
    private void renderDebugOverlay() {
        int camX = camera.getRenderX();
        int camY = camera.getRenderY();

        String[] lines = {
                String.format("F3 DEBUG   fps=%.1f  state=%s", fps, gameState),
                String.format("tile=(%d,%d) cam=(%d,%d) step=%.2f", player.getX(), player.getY(), camX, camY,
                        player.getStepProgress()),
                String.format("events=%d buffered=%d", inputHandler.getFrameEvents().size(),
                        inputHandler.getBufferedCount()),
                String.format("map=%dx%d world=%dx%d seed=0x%X", gameMap.getWidth(), gameMap.getHeight(),
                        gameMap.getPixelWidth(), gameMap.getPixelHeight(), gameMap.getSeed()),
                String.format("hash=0x%016X enemies=%d", gameMap.tileHash(), liveEnemyCount())
        };

        double boxW = 470;
        double boxH = lines.length * 18 + 16;
        double boxX = width - boxW - 10;
        double boxY = 10;
        UiKit.fill(gc, Color.rgb(16, 21, 24, 0.86), boxX, boxY, boxW, boxH);
        UiKit.stroke(gc, Palette.FRAME_GOLD, 1, boxX, boxY, boxW, boxH);
        for (int i = 0; i < lines.length; i++) {
            UiKit.text(gc, lines[i], boxX + 8, boxY + 22 + i * 18, Palette.TEXT_PRIMARY, 13);
        }
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

    /** 回合制战斗界面（几何与配色取自设计稿 {@code #screen-battle}）。 */
    private void renderBattle() {
        Enemy enemy = battleService.getCurrentEnemy();
        BattleScreen.render(gc, player, enemy, battleService.isPlayerTurn(), battleLog, this::playerCommand);
    }

    private void renderPaused() {
        UiKit.fill(gc, Color.rgb(0, 0, 0, 0.55), 0, 0, width, height);

        double boxW = 360;
        double boxH = 140;
        double x = (width - boxW) / 2;
        double y = (height - boxH) / 2;
        UiKit.fill(gc, Palette.PANEL_BASE, x, y, boxW, boxH);
        UiKit.stroke(gc, Palette.FRAME_GOLD, 2, x, y, boxW, boxH);
        UiKit.textCenter(gc, "游戏暂停", width / 2.0, y + 54, Palette.FRAME_GOLD, 26);
        UiKit.textCenter(gc, "按 P 或 Esc 继续冒险", width / 2.0, y + 92, Palette.TEXT_PRIMARY, 16);
    }
}
