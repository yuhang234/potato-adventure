package com.potatoadventure.core;

import com.potatoadventure.model.Enemy;
import com.potatoadventure.model.GameMap;
import com.potatoadventure.model.Player;
import com.potatoadventure.service.BattleService;
import com.potatoadventure.service.SaveLoadService;
import com.potatoadventure.util.InputHandler;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 引擎级回归测试 —— 驱动真实的 {@link GameEngine}（与 {@code mvn javafx:run} 启动的是同一个类、
 * 同一套 Player/GameMap/BattleService/SaveLoadService/InputHandler，不引入任何 mock 或替身），
 * 在真实 JavaFX 21 运行时（Xvfb）下按真实帧节拍推进 {@code update(dt)}，验证本次评审修复的四件事：
 *
 * <ol>
 *   <li>穿怪：按住方向键连续走步时，踏上敌人格必须立即进入战斗（修复前 {@code isStepping()} 早退
 *       导致按住方向键会径直穿过敌人）；</li>
 *   <li>击杀计数：大地图空格近身攻击击败敌人也必须计入 {@code kills}（修复前只有战斗内路径 +1）；</li>
 *   <li>冒险计时：{@code adventureSeconds} 按真实时间累积，不再恒为 0；</li>
 *   <li>读档名字：读档后保留存档里的自定义名字，不被 char-select 的默认名字覆盖。</li>
 * </ol>
 *
 * <p>与游戏主循环的唯一区别是把 {@code AnimationTimer} 换成测试线程手动调用 {@code beginFrame()}
 * + {@code update(dt)}（这正是 {@code GameEngine.start()} 的 AnimationTimer 每一帧做的事），
 * 渲染路径不影响这四处纯逻辑判定。
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameEngineRegressionTest {

    private static final double FRAME = 1.0 / 60.0;

    private GameEngine engine;

    @BeforeAll
    static void startJavaFx() throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(ready::countDown);
        assertTrue(ready.await(30, TimeUnit.SECONDS), "JavaFX 工具集应在 Xvfb 下启动");
    }

    @AfterEach
    void tearDown() {
        if (engine != null) {
            try {
                Object svc = field(engine, "saveLoadService");
                if (svc instanceof SaveLoadService service) {
                    service.close();
                }
            } catch (Exception ignored) {
                // 测试已自行关闭或替换了服务
            }
        }
    }

    // ------------------------------------------------------------------ 构造

    private GameEngine newEngine() throws Exception {
        return onFx(() -> {
            Stage stage = new Stage();
            engine = new GameEngine(stage, 1280, 720);
            return engine;
        });
    }

    private <T> T onFx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(30, TimeUnit.SECONDS);
    }

    private void confirmCharacter() throws Exception {
        invoke(engine, "confirmCharacter", new Class<?>[0]);
    }

    /** 模拟游戏主循环的一帧：先结算输入缓冲，再按 60 FPS 的真实 dt 推进一帧。 */
    private void frame() throws Exception {
        InputHandler input = (InputHandler) field(engine, "inputHandler");
        input.beginFrame();
        invoke(engine, "update", new Class<?>[]{double.class}, FRAME);
    }

    private void press(KeyCode code) throws Exception {
        ((InputHandler) field(engine, "inputHandler")).onKeyPressed(code);
    }

    // ------------------------------------------------------------------ 反射工具

    private static Field fld(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> c = type;
        while (c != null) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static Object field(Object target, String name) throws Exception {
        Field f = fld(target.getClass(), name);
        f.setAccessible(true);
        return f.get(target);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = fld(target.getClass(), name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Object invoke(Object target, String name, Class<?>[] types, Object... args) throws Exception {
        Method m = null;
        Class<?> c = target.getClass();
        while (c != null) {
            try {
                m = c.getDeclaredMethod(name, types);
                break;
            } catch (NoSuchMethodException e) {
                c = c.getSuperclass();
            }
        }
        if (m == null) {
            throw new NoSuchMethodException(name);
        }
        m.setAccessible(true);
        return m.invoke(target, args);
    }

    // ------------------------------------------------------------------ 场景

    /**
     * 缺陷「穿怪」：按住方向键时一步接着一步，逻辑格永远处于 stepping，修复前
     * {@code updatePlaying} 在敌人判定前被 {@code isStepping()} 早退，玩家会径直穿过敌人。
     * 本测试持续按住 D，让玩家连续走步踏上史莱姆所在格，必须触发战斗。
     */
    @Test
    void holdingMoveKeyOntoEnemyTriggersBattle() throws Exception {
        engine = newEngine();
        confirmCharacter();

        Player player = (Player) field(engine, "player");
        @SuppressWarnings("unchecked")
        List<Enemy> enemies = (List<Enemy>) field(engine, "enemies");

        // 玩家出生在 (5,5)，出生区已清空为草地；把史莱姆放到正右方 (8,5)，路径畅通。
        Enemy target = enemies.get(0);
        target.setPosition(8, 5);
        enemies.get(1).setPosition(40, 20);
        enemies.get(2).setPosition(41, 20);

        assertEquals(5, player.getX());
        assertEquals(5, player.getY());

        press(KeyCode.D);
        boolean battle = false;
        for (int i = 0; i < 60 && !battle; i++) {
            frame();
            battle = ((GameState) field(engine, "gameState")) == GameState.BATTLE;
        }

        assertTrue(battle, "按住 D 连续走步踏上敌人格必须触发战斗（不得穿怪）");
        assertEquals(target, ((BattleService) field(engine, "battleService")).getCurrentEnemy(),
                "战斗对手应是踩中的那只敌人");
    }

    /**
     * 缺陷「大地图击杀不计入 kills」：三只敌人各 1 血，站在相邻格，一次空格近身攻击全灭，
     * 修复前 kills 停在 0；修复后应为 3，并进入胜利结算、界面显示「击败魔物 3 只」。
     */
    @Test
    void overworldKillsAreCountedAndReachVictory() throws Exception {
        engine = newEngine();
        confirmCharacter();

        Player player = (Player) field(engine, "player");
        @SuppressWarnings("unchecked")
        List<Enemy> enemies = (List<Enemy>) field(engine, "enemies");

        int[][] spots = {{6, 5}, {5, 4}, {6, 6}};
        for (int i = 0; i < 3; i++) {
            enemies.get(i).setPosition(spots[i][0], spots[i][1]);
            enemies.get(i).setHp(1);
        }

        press(KeyCode.SPACE);
        frame();

        assertEquals(3, ((Integer) field(engine, "kills")).intValue(), "大地图击杀必须计入 kills");
        assertEquals(GameState.VICTORY, field(engine, "gameState"), "三只全灭应进入胜利结算");
        // 经验只发一次：20+35+50=105，105 经验恰好升到 2 级（消耗 100）并余 5
        assertEquals(2, player.getLevel(), "105 经验应恰好升到 2 级");
        assertEquals(5, player.getExp(), "升级后应余 5 点经验（105 - 100），不得翻倍");

        String[][] stats = (String[][]) invoke(engine, "victoryStats", new Class<?>[0]);
        assertEquals("3 只", stats[2][1], "胜利界面『击败魔物』应显示 3 只");
    }

    /**
     * 战斗内击杀的计数收敛到 {@code onEnemyDefeated} 后不得重复累加：一次战斗击杀 kills 恰好 +1。
     */
    @Test
    void battleKillCountsExactlyOnce() throws Exception {
        engine = newEngine();
        confirmCharacter();

        Player player = (Player) field(engine, "player");
        @SuppressWarnings("unchecked")
        List<Enemy> enemies = (List<Enemy>) field(engine, "enemies");

        Enemy target = enemies.get(0);
        target.setHp(1);
        invoke(engine, "startBattle", new Class<?>[]{Enemy.class}, target);
        assertEquals(GameState.BATTLE, field(engine, "gameState"));

        press(KeyCode.DIGIT1);
        frame();

        assertFalse(target.isAlive(), "一次攻击应击杀 1 血敌人");
        assertEquals(1, ((Integer) field(engine, "kills")).intValue(), "战斗击杀必须恰好计入一次");
        assertEquals(target.getExpReward(), player.getExp(), "经验只发一次，不得翻倍");
        assertEquals(GameState.PLAYING, field(engine, "gameState"), "还剩两只敌人，不应进入胜利结算");
    }

    /**
     * 缺陷「冒险时长恒为 0」：模拟 5 秒 PLAYING 后 {@code adventureSeconds} 应约等于 5，
     * 且胜利界面「冒险时长」格式正确。
     */
    @Test
    void adventureTimeAccumulatesInPlaying() throws Exception {
        engine = newEngine();
        confirmCharacter();
        assertEquals(0, ((Integer) field(engine, "adventureSeconds")).intValue());

        for (int i = 0; i < 300; i++) { // 5 秒
            frame();
        }

        int seconds = ((Integer) field(engine, "adventureSeconds")).intValue();
        assertTrue(seconds >= 4 && seconds <= 6, "模拟 5 秒后冒险时长应约 5 秒，实际 " + seconds);

        String[][] stats = (String[][]) invoke(engine, "victoryStats", new Class<?>[0]);
        String expected = String.format("%d 分 %02d 秒", seconds / 60, seconds % 60);
        assertEquals(expected, stats[3][1], "胜利界面『冒险时长』应与计数器一致");
        assertNotEquals("0 分 00 秒", stats[3][1], "冒险时长不得恒为 0 分 00 秒");
    }

    /**
     * 缺陷「读档名字被覆盖」：以自定义名字保存后读档，玩家名必须保留，且 char-select 的
     * playerName 字段同步为读到的值（修复前被默认『土豆勇士』覆盖）。
     */
    @Test
    void loadedSaveKeepsCustomName() throws Exception {
        engine = newEngine();

        // 用临时数据库隔离，绝不触碰真实存档位 saves/save.db
        Path db = Files.createTempFile("potato-regression", ".db");
        Files.deleteIfExists(db);
        SaveLoadService isolated = new SaveLoadService(db.toString());
        ((SaveLoadService) field(engine, "saveLoadService")).close();
        setField(engine, "saveLoadService", isolated);

        confirmCharacter();
        Player player = (Player) field(engine, "player");
        @SuppressWarnings("unchecked")
        List<Enemy> enemies = (List<Enemy>) field(engine, "enemies");

        player.setName("张三");
        assertTrue(isolated.saveGame(player, (GameMap) field(engine, "gameMap"), enemies), "保存应成功");

        // 模拟读档入口：玩家名与 char-select 字段都回到默认值
        player.setName("土豆勇士");
        setField(engine, "playerName", "土豆勇士");

        Boolean ok = (Boolean) invoke(engine, "loadFromSave", new Class<?>[0]);
        assertTrue(ok, "读档应成功");
        assertEquals("张三", player.getName(), "读档后玩家名不得被 char-select 字段覆盖");
        assertEquals("张三", field(engine, "playerName"), "playerName 字段应与读到的名字同步");

        isolated.close();
        Files.deleteIfExists(db);
    }
}
