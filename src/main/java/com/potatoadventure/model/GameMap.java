package com.potatoadventure.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

/**
 * 游戏地图类
 */
public class GameMap {
    
    private final int width;  // 地图宽度（格子数）
    private final int height; // 地图高度（格子数）
    private final int[][] tiles; // 地图数据（0=草地, 1=墙壁, 2=树木, 3=水）
    
    private static final int TILE_SIZE = 32;
    private Random random = new Random();
    
    // 地形类型
    public static final int TILE_GRASS = 0;
    public static final int TILE_WALL = 1;
    public static final int TILE_TREE = 2;
    public static final int TILE_WATER = 3;
    
    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new int[width][height];
        
        generateMap();
    }
    
    /**
     * 生成地图
     */
    private void generateMap() {
        // 填充草地
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = TILE_GRASS;
            }
        }
        
        // 生成边界墙
        for (int x = 0; x < width; x++) {
            tiles[x][0] = TILE_WALL;
            tiles[x][height - 1] = TILE_WALL;
        }
        for (int y = 0; y < height; y++) {
            tiles[0][y] = TILE_WALL;
            tiles[width - 1][y] = TILE_WALL;
        }
        
        // 随机生成树木
        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(width - 2) + 1;
            int y = random.nextInt(height - 2) + 1;
            tiles[x][y] = TILE_TREE;
        }
        
        // 随机生成水域
        for (int i = 0; i < 20; i++) {
            int x = random.nextInt(width - 2) + 1;
            int y = random.nextInt(height - 2) + 1;
            tiles[x][y] = TILE_WATER;
        }
        
        // 确保起始位置周围可通行
        for (int x = 4; x <= 6; x++) {
            for (int y = 4; y <= 6; y++) {
                tiles[x][y] = TILE_GRASS;
            }
        }
    }
    
    /**
     * 检查碰撞
     */
    public boolean isCollision(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return true;
        }
        
        int tile = tiles[x][y];
        return tile == TILE_WALL || tile == TILE_TREE || tile == TILE_WATER;
    }
    
    /**
     * 渲染地图
     */
    public void render(GraphicsContext gc) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int screenX = x * TILE_SIZE;
                int screenY = y * TILE_SIZE;
                
                renderTile(gc, tiles[x][y], screenX, screenY);
            }
        }
    }
    
    /**
     * 渲染单个地形块
     */
    private void renderTile(GraphicsContext gc, int tileType, int x, int y) {
        switch (tileType) {
            case TILE_GRASS:
                // 草地（绿色）
                gc.setFill(Color.rgb(124, 252, 0));
                gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                // 草纹理
                gc.setFill(Color.rgb(100, 200, 0));
                for (int i = 0; i < 3; i++) {
                    int gx = x + random.nextInt(TILE_SIZE - 4);
                    int gy = y + random.nextInt(TILE_SIZE - 4);
                    gc.fillRect(gx, gy, 2, 4);
                }
                break;
                
            case TILE_WALL:
                // 墙壁（灰色砖块）
                gc.setFill(Color.rgb(128, 128, 128));
                gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                gc.setStroke(Color.rgb(80, 80, 80));
                gc.strokeRect(x, y, TILE_SIZE, TILE_SIZE);
                gc.strokeRect(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4);
                break;
                
            case TILE_TREE:
                // 树木
                gc.setFill(Color.rgb(124, 252, 0));
                gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                // 树干
                gc.setFill(Color.rgb(139, 69, 19));
                gc.fillRect(x + 12, y + 16, 8, 12);
                // 树冠
                gc.setFill(Color.rgb(34, 139, 34));
                gc.fillOval(x + 4, y + 4, 24, 20);
                break;
                
            case TILE_WATER:
                // 水域（蓝色）
                gc.setFill(Color.rgb(0, 191, 255));
                gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                // 波纹
                gc.setStroke(Color.rgb(100, 200, 255));
                gc.strokeOval(x + 8, y + 8, 16, 16);
                gc.strokeOval(x + 4, y + 4, 24, 24);
                break;
                
            default:
                gc.setFill(Color.BLACK);
                gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                break;
        }
    }
    
    // Getters
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getTile(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return tiles[x][y];
        }
        return TILE_WALL;
    }
    public void setTile(int x, int y, int type) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            tiles[x][y] = type;
        }
    }
}
