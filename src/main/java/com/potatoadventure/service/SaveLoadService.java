package com.potatoadventure.service;

import com.potatoadventure.model.Player;
import com.potatoadventure.model.GameMap;
import com.potatoadventure.model.Enemy;

import java.sql.*;
import java.util.List;

/**
 * 存档加载服务 - 使用SQLite存储游戏数据
 */
public class SaveLoadService {
    
    private static final String DB_URL = "jdbc:sqlite:saves/save.db";
    private Connection connection;
    
    public SaveLoadService() {
        initializeDatabase();
    }
    
    /**
     * 初始化数据库
     */
    private void initializeDatabase() {
        try {
            // 创建saves目录
            java.io.File saveDir = new java.io.File("saves");
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            
            // 连接数据库
            connection = DriverManager.getConnection(DB_URL);
            
            // 创建玩家表
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
            
            Statement stmt = connection.createStatement();
            stmt.execute(createPlayerTable);
            
            // 创建敌人表
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
            
            stmt.execute(createEnemyTable);
            stmt.close();
            
            System.out.println("数据库初始化成功");
            
        } catch (SQLException e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 保存游戏
     */
    public boolean saveGame(Player player, GameMap map, List<Enemy> enemies) {
        try {
            // 开始事务
            connection.setAutoCommit(false);
            
            // 删除旧存档
            String deletePlayers = "DELETE FROM player";
            String deleteEnemies = "DELETE FROM enemy";
            Statement stmt = connection.createStatement();
            stmt.execute(deletePlayers);
            stmt.execute(deleteEnemies);
            stmt.close();
            
            // 保存玩家数据
            String insertPlayer = "INSERT INTO player (name, x, y, level, exp, hp, max_hp, mp, max_mp, attack, defense, speed) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = connection.prepareStatement(insertPlayer, Statement.RETURN_GENERATED_KEYS);
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
            
            // 获取保存的玩家ID
            ResultSet rs = pstmt.getGeneratedKeys();
            int saveId = 1;
            if (rs.next()) {
                saveId = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
            
            // 保存敌人数据
            String insertEnemy = "INSERT INTO enemy (save_id, name, x, y, level, hp, max_hp, attack, defense, exp_reward, is_alive) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            pstmt = connection.prepareStatement(insertEnemy);
            for (Enemy enemy : enemies) {
                pstmt.setInt(1, saveId);
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
            
            // 提交事务
            connection.commit();
            connection.setAutoCommit(true);
            
            System.out.println("游戏保存成功！");
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
     * 加载游戏
     */
    public boolean loadGame(Player player, GameMap map, List<Enemy> enemies) {
        try {
            // 加载玩家数据
            String selectPlayer = "SELECT * FROM player ORDER BY id DESC LIMIT 1";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(selectPlayer);
            
            if (rs.next()) {
                player.setName(rs.getString("name"));
                player.setPosition(rs.getInt("x"), rs.getInt("y"));
                player.setLevel(rs.getInt("level"));
                player.setExp(rs.getInt("exp"));
                player.setHp(rs.getInt("hp"));
                player.setMaxHp(rs.getInt("max_hp"));
                player.setMp(rs.getInt("mp"));
                player.setMaxMp(rs.getInt("max_mp"));
                player.setAttack(rs.getInt("attack"));
                player.setDefense(rs.getInt("defense"));
                player.setSpeed(rs.getInt("speed"));
                
                int saveId = rs.getInt("id");
                rs.close();
                stmt.close();
                
                // 加载敌人数据
                String selectEnemies = "SELECT * FROM enemy WHERE save_id = ?";
                PreparedStatement pstmt = connection.prepareStatement(selectEnemies);
                pstmt.setInt(1, saveId);
                rs = pstmt.executeQuery();
                
                enemies.clear();
                while (rs.next()) {
                    Enemy enemy = new Enemy(rs.getInt("x"), rs.getInt("y"), rs.getString("name"));
                    enemy.setLevel(rs.getInt("level"));
                    enemy.setHp(rs.getInt("hp"));
                    enemy.setMaxHp(rs.getInt("max_hp"));
                    enemy.setAttack(rs.getInt("attack"));
                    enemy.setDefense(rs.getInt("defense"));
                    enemy.setExpReward(rs.getInt("exp_reward"));
                    
                    // 如果敌人已死亡，设置HP为0
                    if (rs.getInt("is_alive") == 0) {
                        enemy.setHp(0);
                    }
                    
                    enemies.add(enemy);
                }
                
                rs.close();
                pstmt.close();
                
                System.out.println("游戏加载成功！");
                return true;
                
            } else {
                rs.close();
                stmt.close();
                System.out.println("没有找到存档");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("游戏加载失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 关闭数据库连接
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
