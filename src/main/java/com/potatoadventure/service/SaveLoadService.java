package com.potatoadventure.service;

import com.potatoadventure.model.Enemy;
import com.potatoadventure.model.GameMap;
import com.potatoadventure.model.Player;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 存档 / 读档服务（SQLite，单存档位）。
 *
 * <p><b>缺陷 4（存档不写地图表，读档后地图被重新随机）的成因与修法：</b>旧实现只把玩家与敌人
 * 写进数据库，地图完全没落盘；{@code loadGame} 也就只能还原玩家与敌人，地图对象仍是内存里
 * 那张（重开游戏则重新随机）。现在新增两张表：
 *
 * <ul>
 *   <li>{@code map_meta(save_id, width, height, seed, tile_count)} —— 地图元信息；</li>
 *   <li>{@code map_tile(save_id, x, y, tile)} —— 全部地块，主键 (save_id,x,y)。</li>
 * </ul>
 *
 * <p>一致性策略：
 * <ul>
 *   <li>存档在一次事务里删除四张表的旧数据后整体写入，失败则回滚；</li>
 *   <li>读档先把玩家 / 敌人 / 地图全部读进临时变量并校验（地图行数必须等于 width×height），
 *       校验通过之后才写回传入的对象——<b>缺少地图行的旧存档会被整体拒绝，不会出现半加载</b>
 *       （M1 之前的旧存档因此不可用，见 {@code Docs/M1_引擎重构说明.md}）；</li>
 *   <li>存档与读档各打印一次 {@link GameMap#tileHash()}，读档后哈希一致即证明地图未经重新随机。</li>
 * </ul>
 */
public class SaveLoadService {

    /** 单存档位固定主键。 */
    public static final int SAVE_SLOT_ID = 1;

    /** M1 之前的旧存档没有地图表，会被整体拒绝。 */
    private static final String DEFAULT_DB_PATH = "saves/save.db";

    private final String databasePath;
    private Connection connection;

    public SaveLoadService() {
        this(DEFAULT_DB_PATH);
    }

    /**
     * @param databasePath SQLite 文件路径（相对或绝对）；单元测试用临时文件注入。
     */
    public SaveLoadService(String databasePath) {
        this.databasePath = databasePath;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            File dbFile = new File(databasePath);
            File parent = dbFile.getAbsoluteFile().getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                System.err.println("无法创建存档目录: " + parent);
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);

            String createPlayerTable = "CREATE TABLE IF NOT EXISTS player (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "x INTEGER NOT NULL," +
                    "y INTEGER NOT NULL," +
                    "level INTEGER NOT NULL," +
                    "exp INTEGER NOT NULL," +
                    "hp INTEGER NOT NULL," +
                    "max_hp INTEGER NOT NULL," +
                    "mp INTEGER NOT NULL," +
                    "max_mp INTEGER NOT NULL," +
                    "attack INTEGER NOT NULL," +
                    "defense INTEGER NOT NULL," +
                    "speed INTEGER NOT NULL," +
                    "save_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";

            String createEnemyTable = "CREATE TABLE IF NOT EXISTS enemy (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "save_id INTEGER NOT NULL," +
                    "name TEXT NOT NULL," +
                    "x INTEGER NOT NULL," +
                    "y INTEGER NOT NULL," +
                    "level INTEGER NOT NULL," +
                    "hp INTEGER NOT NULL," +
                    "max_hp INTEGER NOT NULL," +
                    "attack INTEGER NOT NULL," +
                    "defense INTEGER NOT NULL," +
                    "exp_reward INTEGER NOT NULL," +
                    "is_alive INTEGER NOT NULL" +
                    ")";

            String createMapMetaTable = "CREATE TABLE IF NOT EXISTS map_meta (" +
                    "save_id INTEGER PRIMARY KEY," +
                    "width INTEGER NOT NULL," +
                    "height INTEGER NOT NULL," +
                    "seed INTEGER NOT NULL," +
                    "tile_count INTEGER NOT NULL" +
                    ")";

            String createMapTileTable = "CREATE TABLE IF NOT EXISTS map_tile (" +
                    "save_id INTEGER NOT NULL," +
                    "x INTEGER NOT NULL," +
                    "y INTEGER NOT NULL," +
                    "tile INTEGER NOT NULL," +
                    "PRIMARY KEY (save_id, x, y)" +
                    ")";

            Statement stmt = connection.createStatement();
            stmt.execute(createPlayerTable);
            stmt.execute(createEnemyTable);
            stmt.execute(createMapMetaTable);
            stmt.execute(createMapTileTable);
            stmt.close();

            System.out.println("数据库初始化成功");

        } catch (SQLException e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 保存游戏：玩家 + 敌人 + 地图（元信息与全部地块），单事务。
     */
    public boolean saveGame(Player player, GameMap map, List<Enemy> enemies) {
        if (connection == null) {
            System.err.println("游戏保存失败: 数据库未就绪");
            return false;
        }
        if (player == null || map == null || enemies == null) {
            System.err.println("游戏保存失败: 参数不完整");
            return false;
        }

        try {
            connection.setAutoCommit(false);

            Statement clear = connection.createStatement();
            clear.execute("DELETE FROM player");
            clear.execute("DELETE FROM enemy");
            clear.execute("DELETE FROM map_meta");
            clear.execute("DELETE FROM map_tile");
            clear.close();

            String insertPlayer = "INSERT INTO player (id, name, x, y, level, exp, hp, max_hp, mp, max_mp, attack, defense, speed) " +
                    "VALUES (" + SAVE_SLOT_ID + ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(insertPlayer);
            pstmt.setString(1, player.getName());
            pstmt.setInt(2, player.getX());
            pstmt.setInt(3, player.getY());
            pstmt.setInt(4, player.getLevel());
            pstmt.setInt(5, player.getExp());
            pstmt.setInt(6, player.getHp());
            pstmt.setInt(7, player.getMaxHp());
            pstmt.setInt(8, player.getMp());
            pstmt.setInt(9, player.getMaxMp());
            pstmt.setInt(10, player.getAttack());
            pstmt.setInt(11, player.getDefense());
            pstmt.setInt(12, player.getSpeed());
            pstmt.executeUpdate();
            pstmt.close();

            String insertEnemy = "INSERT INTO enemy (save_id, name, x, y, level, hp, max_hp, attack, defense, exp_reward, is_alive) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = connection.prepareStatement(insertEnemy);
            for (Enemy enemy : enemies) {
                pstmt.setInt(1, SAVE_SLOT_ID);
                pstmt.setString(2, enemy.getName());
                pstmt.setInt(3, enemy.getX());
                pstmt.setInt(4, enemy.getY());
                pstmt.setInt(5, enemy.getLevel());
                pstmt.setInt(6, enemy.getHp());
                pstmt.setInt(7, enemy.getMaxHp());
                pstmt.setInt(8, enemy.getAttack());
                pstmt.setInt(9, enemy.getDefense());
                pstmt.setInt(10, enemy.getExpReward());
                pstmt.setInt(11, enemy.isAlive() ? 1 : 0);
                pstmt.executeUpdate();
            }
            pstmt.close();

            String insertMeta = "INSERT INTO map_meta (save_id, width, height, seed, tile_count) VALUES (?, ?, ?, ?, ?)";
            pstmt = connection.prepareStatement(insertMeta);
            pstmt.setInt(1, SAVE_SLOT_ID);
            pstmt.setInt(2, map.getWidth());
            pstmt.setInt(3, map.getHeight());
            pstmt.setLong(4, map.getSeed());
            pstmt.setInt(5, map.getWidth() * map.getHeight());
            pstmt.executeUpdate();
            pstmt.close();

            String insertTile = "INSERT INTO map_tile (save_id, x, y, tile) VALUES (?, ?, ?, ?)";
            pstmt = connection.prepareStatement(insertTile);
            for (int x = 0; x < map.getWidth(); x++) {
                for (int y = 0; y < map.getHeight(); y++) {
                    pstmt.setInt(1, SAVE_SLOT_ID);
                    pstmt.setInt(2, x);
                    pstmt.setInt(3, y);
                    pstmt.setInt(4, map.getTile(x, y));
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
            pstmt.close();

            connection.commit();
            connection.setAutoCommit(true);

            System.out.println("游戏保存成功！");
            System.out.printf("地图已保存: %dx%d seed=0x%X tiles=%d hash=0x%X%n",
                    map.getWidth(), map.getHeight(), map.getSeed(),
                    map.getWidth() * map.getHeight(), map.tileHash());
            return true;

        } catch (SQLException e) {
            System.err.println("游戏保存失败: " + e.getMessage());
            e.printStackTrace();
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    /**
     * 加载游戏。
     *
     * <p>全部数据先读进临时变量并校验，通过后才写回 {@code player} / {@code enemies}。
     * 缺少地图表数据（M1 之前的旧存档）或地图行数不匹配时整体拒绝，返回 {@code null}，
     * 且不修改传入对象。
     *
     * @return 还原出的 {@link GameMap}；没有可用存档时返回 {@code null}
     */
    public GameMap loadGame(Player player, List<Enemy> enemies) {
        if (connection == null) {
            System.err.println("游戏加载失败: 数据库未就绪");
            return null;
        }

        try {
            // ---- 1. 读玩家 ----
            PreparedStatement pstmt = connection.prepareStatement(
                    "SELECT * FROM player WHERE id = ?");
            pstmt.setInt(1, SAVE_SLOT_ID);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                pstmt.close();
                System.out.println("没有找到存档");
                return null;
            }
            final String name = rs.getString("name");
            final int px = rs.getInt("x");
            final int py = rs.getInt("y");
            final int level = rs.getInt("level");
            final int exp = rs.getInt("exp");
            final int hp = rs.getInt("hp");
            final int maxHp = rs.getInt("max_hp");
            final int mp = rs.getInt("mp");
            final int maxMp = rs.getInt("max_mp");
            final int attack = rs.getInt("attack");
            final int defense = rs.getInt("defense");
            final int speed = rs.getInt("speed");
            rs.close();
            pstmt.close();

            // ---- 2. 读地图元信息 ----
            pstmt = connection.prepareStatement(
                    "SELECT width, height, seed, tile_count FROM map_meta WHERE save_id = ?");
            pstmt.setInt(1, SAVE_SLOT_ID);
            rs = pstmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                pstmt.close();
                System.err.println("存档缺少地图数据（M1 之前的旧存档不可读），已整体取消加载");
                return null;
            }
            final int mapWidth = rs.getInt("width");
            final int mapHeight = rs.getInt("height");
            final long seed = rs.getLong("seed");
            final int expectedTiles = rs.getInt("tile_count");
            rs.close();
            pstmt.close();

            if (mapWidth < 3 || mapHeight < 3 || expectedTiles != mapWidth * mapHeight) {
                System.err.println("存档地图元信息非法: " + mapWidth + "x" + mapHeight
                        + " tile_count=" + expectedTiles + "，已整体取消加载");
                return null;
            }

            // ---- 3. 读地块 ----
            int[][] tiles = new int[mapWidth][mapHeight];
            int loadedTiles = 0;
            pstmt = connection.prepareStatement(
                    "SELECT x, y, tile FROM map_tile WHERE save_id = ?");
            pstmt.setInt(1, SAVE_SLOT_ID);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int tile = rs.getInt("tile");
                if (x < 0 || x >= mapWidth || y < 0 || y >= mapHeight
                        || tile < GameMap.TILE_GRASS || tile > GameMap.TILE_WATER) {
                    rs.close();
                    pstmt.close();
                    System.err.println("存档地块非法: (" + x + "," + y + ")=" + tile + "，已整体取消加载");
                    return null;
                }
                tiles[x][y] = tile;
                loadedTiles++;
            }
            rs.close();
            pstmt.close();

            if (loadedTiles != expectedTiles) {
                System.err.println("存档地图不完整: 期望 " + expectedTiles + " 格，实际 " + loadedTiles
                        + " 格，已整体取消加载");
                return null;
            }

            // ---- 4. 读敌人 ----
            final int[] enemyCount = {0};
            List<Enemy> loadedEnemies = new ArrayList<>();
            pstmt = connection.prepareStatement("SELECT * FROM enemy WHERE save_id = ? ORDER BY id");
            pstmt.setInt(1, SAVE_SLOT_ID);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Enemy enemy = new Enemy(rs.getInt("x"), rs.getInt("y"), rs.getString("name"));
                enemy.setLevel(rs.getInt("level"));
                enemy.setMaxHp(rs.getInt("max_hp"));
                enemy.setHp(rs.getInt("is_alive") == 0 ? 0 : rs.getInt("hp"));
                enemy.setAttack(rs.getInt("attack"));
                enemy.setDefense(rs.getInt("defense"));
                enemy.setExpReward(rs.getInt("exp_reward"));
                loadedEnemies.add(enemy);
                enemyCount[0]++;
            }
            rs.close();
            pstmt.close();

            // ---- 5. 校验通过，落盘到传入对象 ----
            GameMap map = GameMap.fromTiles(mapWidth, mapHeight, seed, tiles);

            player.setName(name);
            player.setLevel(level);
            player.setExp(exp);
            player.setHp(hp);
            player.setMaxHp(maxHp);
            player.setMp(mp);
            player.setMaxMp(maxMp);
            player.setAttack(attack);
            player.setDefense(defense);
            player.setSpeed(speed);
            player.setPosition(px, py);

            enemies.clear();
            enemies.addAll(loadedEnemies);

            System.out.println("游戏加载成功！");
            System.out.printf("地图已加载: %dx%d seed=0x%X tiles=%d hash=0x%X 敌人=%d 玩家=(%d,%d)%n",
                    mapWidth, mapHeight, seed, loadedTiles, map.tileHash(), enemyCount[0], px, py);
            return map;

        } catch (SQLException e) {
            System.err.println("游戏加载失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String getDatabasePath() {
        return databasePath;
    }

    /**
     * 关闭数据库连接。
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("数据库连接已关闭");
            }
        } catch (SQLException e) {
            System.err.println("关闭数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
