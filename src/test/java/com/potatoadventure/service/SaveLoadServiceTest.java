package com.potatoadventure.service;

import com.potatoadventure.model.Enemy;
import com.potatoadventure.model.GameMap;
import com.potatoadventure.model.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 存档往返测试 —— 覆盖缺陷 4「存档不写地图表，读档后地图被重新随机」。
 */
class SaveLoadServiceTest {

    @TempDir
    Path tempDir;

    private String dbPath() {
        return tempDir.resolve("test-save.db").toString();
    }

    private List<Enemy> sampleEnemies() {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy(18, 10, "史莱姆"));
        enemies.add(new Enemy(34, 18, "哥布林"));
        enemies.add(new Enemy(44, 30, "野狼"));
        return enemies;
    }

    @Test
    void saveLoadRoundTripIsLossless() {
        String db = dbPath();
        SaveLoadService service = new SaveLoadService(db);
        try {
            GameMap map = new GameMap(GameMap.DEFAULT_WIDTH, GameMap.DEFAULT_HEIGHT, 42424242L);

            Player player = new Player(GameMap.SPAWN_TILE_X, GameMap.SPAWN_TILE_Y);
            player.setPosition(9, 12);
            player.setLevel(3);
            player.setExp(77);
            player.setHp(55);
            player.setMaxHp(140);
            player.setMp(21);
            player.setMaxMp(70);
            player.setAttack(21);
            player.setDefense(14);
            player.setSpeed(7);

            List<Enemy> enemies = sampleEnemies();
            enemies.get(1).setHp(33);

            assertTrue(service.saveGame(player, map, enemies));

            Player loadedPlayer = new Player(0, 0);
            List<Enemy> loadedEnemies = new ArrayList<>();
            GameMap loadedMap = service.loadGame(loadedPlayer, loadedEnemies);

            assertNotNull(loadedMap, "存档里必须有地图数据");
            assertEquals(map.tileHash(), loadedMap.tileHash(), "读档后地图必须与存档前完全一致");
            assertEquals(map.getWidth(), loadedMap.getWidth());
            assertEquals(map.getHeight(), loadedMap.getHeight());
            assertEquals(map.getSeed(), loadedMap.getSeed());
            for (int x = 0; x < map.getWidth(); x++) {
                for (int y = 0; y < map.getHeight(); y++) {
                    assertEquals(map.getTile(x, y), loadedMap.getTile(x, y), "地块不一致: (" + x + "," + y + ")");
                }
            }

            assertEquals(player.getName(), loadedPlayer.getName());
            assertEquals(9, loadedPlayer.getX());
            assertEquals(12, loadedPlayer.getY());
            assertEquals(3, loadedPlayer.getLevel());
            assertEquals(77, loadedPlayer.getExp());
            assertEquals(55, loadedPlayer.getHp());
            assertEquals(140, loadedPlayer.getMaxHp());
            assertEquals(21, loadedPlayer.getMp());
            assertEquals(70, loadedPlayer.getMaxMp());
            assertEquals(21, loadedPlayer.getAttack());
            assertEquals(14, loadedPlayer.getDefense());
            assertEquals(7, loadedPlayer.getSpeed());

            assertEquals(enemies.size(), loadedEnemies.size(), "敌人数量必须一致");
            for (int i = 0; i < enemies.size(); i++) {
                Enemy expected = enemies.get(i);
                Enemy actual = loadedEnemies.get(i);
                assertEquals(expected.getName(), actual.getName());
                assertEquals(expected.getX(), actual.getX(), "敌人位置必须一致");
                assertEquals(expected.getY(), actual.getY(), "敌人位置必须一致");
                assertEquals(expected.getHp(), actual.getHp());
                assertEquals(expected.getMaxHp(), actual.getMaxHp());
                assertEquals(expected.getAttack(), actual.getAttack());
                assertEquals(expected.getDefense(), actual.getDefense());
                assertEquals(expected.getExpReward(), actual.getExpReward());
            }
        } finally {
            service.close();
        }
    }

    @Test
    void loadingWithoutAnySaveReturnsNull() {
        SaveLoadService service = new SaveLoadService(dbPath());
        try {
            Player player = new Player(5, 5);
            assertNull(service.loadGame(player, new ArrayList<>()));
            assertEquals(5, player.getX(), "没有存档时不得修改玩家");
        } finally {
            service.close();
        }
    }

    /**
     * M1 之前的旧存档没有地图表：必须整体拒绝，且不能把玩家改坏（不许半加载）。
     */
    @Test
    void legacySaveWithoutMapTablesIsRejectedWholesale() throws Exception {
        String db = dbPath();
        SaveLoadService service = new SaveLoadService(db);
        try {
            GameMap map = new GameMap(GameMap.DEFAULT_WIDTH, GameMap.DEFAULT_HEIGHT, 99L);
            Player player = new Player(5, 5);
            player.setPosition(9, 12);
            player.setHp(55);
            assertTrue(service.saveGame(player, map, sampleEnemies()));

            // 人为删除地图数据，模拟旧版存档
            try (Connection raw = DriverManager.getConnection("jdbc:sqlite:" + db);
                 Statement stmt = raw.createStatement()) {
                stmt.executeUpdate("DELETE FROM map_meta");
                stmt.executeUpdate("DELETE FROM map_tile");
            }

            Player target = new Player(0, 0);
            target.setHp(100);
            List<Enemy> enemies = new ArrayList<>();
            assertNull(service.loadGame(target, enemies), "缺少地图数据的存档必须被整体拒绝");
            assertEquals(0, target.getX(), "拒绝加载时玩家位置不得被部分写入");
            assertEquals(100, target.getHp(), "拒绝加载时玩家属性不得被部分写入");
            assertTrue(enemies.isEmpty(), "拒绝加载时敌人列表不得被部分写入");
        } finally {
            service.close();
        }
    }

    @Test
    void incompleteMapIsRejected() throws Exception {
        String db = dbPath();
        SaveLoadService service = new SaveLoadService(db);
        try {
            GameMap map = new GameMap(GameMap.DEFAULT_WIDTH, GameMap.DEFAULT_HEIGHT, 100L);
            assertTrue(service.saveGame(new Player(5, 5), map, sampleEnemies()));

            try (Connection raw = DriverManager.getConnection("jdbc:sqlite:" + db);
                 Statement stmt = raw.createStatement()) {
                stmt.executeUpdate("DELETE FROM map_tile WHERE x = 0");
            }

            Player target = new Player(0, 0);
            assertNull(service.loadGame(target, new ArrayList<>()), "地块缺失必须被拒绝");
        } finally {
            service.close();
        }
    }

    @Test
    void deadEnemiesStayDeadAfterRoundTrip() {
        SaveLoadService service = new SaveLoadService(dbPath());
        try {
            GameMap map = new GameMap(GameMap.DEFAULT_WIDTH, GameMap.DEFAULT_HEIGHT, 555L);
            List<Enemy> enemies = sampleEnemies();
            enemies.get(0).setHp(0);

            assertTrue(service.saveGame(new Player(5, 5), map, enemies));

            List<Enemy> loaded = new ArrayList<>();
            assertNotNull(service.loadGame(new Player(0, 0), loaded));
            assertEquals(3, loaded.size(), "已击败的敌人仍然保留位置记录");
            assertFalse(loaded.get(0).isAlive(), "已击败的敌人读档后不得复活");
            assertTrue(loaded.get(1).isAlive());
        } finally {
            service.close();
        }
    }

    @Test
    void repeatedSavesKeepTheSingleSlot() {
        SaveLoadService service = new SaveLoadService(dbPath());
        try {
            GameMap first = new GameMap(GameMap.DEFAULT_WIDTH, GameMap.DEFAULT_HEIGHT, 111L);
            GameMap second = new GameMap(GameMap.DEFAULT_WIDTH, GameMap.DEFAULT_HEIGHT, 222L);
            assertTrue(service.saveGame(new Player(5, 5), first, sampleEnemies()));
            assertTrue(service.saveGame(new Player(5, 5), second, sampleEnemies()));

            List<Enemy> loaded = new ArrayList<>();
            GameMap loadedMap = service.loadGame(new Player(0, 0), loaded);
            assertNotNull(loadedMap);
            assertEquals(second.tileHash(), loadedMap.tileHash(), "单存档位：最后一次存档生效");
            assertEquals(3, loaded.size(), "敌人表不得残留上一次存档的行");
        } finally {
            service.close();
        }
    }
}
