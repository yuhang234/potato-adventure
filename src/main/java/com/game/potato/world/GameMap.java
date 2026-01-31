package com.game.potato.world;

import javafx.scene.canvas.GraphicsContext;
import com.game.potato.graphics.TileGraphics;
import com.game.potato.util.Vector2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏地图
 * 使用瓦片地图系统
 */
public class GameMap {
    
    private static final Logger logger = LoggerFactory.getLogger(GameMap.class);
    
    public static final int TILE_SIZE = 32; // 每个瓦片32x32像素
    
    private final String mapName;
    private final int width;  // 地图宽度（瓦片数）
    private final int height; // 地图高度（瓦片数）
    private final Tile[][] tiles;
    private final List<Entity> entities;
    
    public GameMap(String mapName, int width, int height) {
        this.mapName = mapName;
        this.width = width;
        this.height = height;
        this.tiles = new Tile[height][width];
        this.entities = new ArrayList<>();
        
        initializeMap();
        logger.info("创建地图: {} ({}x{})", mapName, width, height);
    }
    
    /**
     * 初始化地图（填充默认瓦片）
     */
    private void initializeMap() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 默认填充草地
                tiles[y][x] = new Tile(Tile.TileType.GRASS);
            }
        }
    }
    
    /**
     * 设置指定位置的瓦片
     */
    public void setTile(int x, int y, Tile tile) {
        if (isValidPosition(x, y)) {
            tiles[y][x] = tile;
        }
    }
    
    /**
     * 获取指定位置的瓦片
     */
    public Tile getTile(int x, int y) {
        if (isValidPosition(x, y)) {
            return tiles[y][x];
        }
        return null;
    }
    
    /**
     * 检查位置是否在地图范围内
     */
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    /**
     * 检查位置是否可行走
     */
    public boolean isWalkable(int x, int y) {
        if (!isValidPosition(x, y)) {
            return false;
        }
        
        Tile tile = getTile(x, y);
        if (tile != null && !tile.isWalkable()) {
            return false;
        }
        
        // 检查是否有阻挡的实体
        for (Entity entity : entities) {
            if (entity.isActive()) {
                Vector2 entityPos = entity.getPosition();
                int entityTileX = (int)(entityPos.getX() / TILE_SIZE);
                int entityTileY = (int)(entityPos.getY() / TILE_SIZE);
                
                if (entityTileX == x && entityTileY == y) {
                    // 可以在这里判断实体类型是否阻挡
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * 添加实体到地图
     */
    public void addEntity(Entity entity) {
        entities.add(entity);
    }
    
    /**
     * 移除实体
     */
    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }
    
    /**
     * 更新地图（更新所有实体）
     */
    public void update(double deltaTime) {
        // 更新所有实体
        for (int i = entities.size() - 1; i >= 0; i--) {
            Entity entity = entities.get(i);
            if (entity.isActive()) {
                entity.update(deltaTime);
            } else {
                // 移除非活跃实体
                entities.remove(i);
            }
        }
    }
    
    /**
     * 渲染地图
     */
    public void render(GraphicsContext gc, double cameraX, double cameraY) {
        // 计算可见区域
        int startX = Math.max(0, (int)(cameraX / TILE_SIZE));
        int startY = Math.max(0, (int)(cameraY / TILE_SIZE));
        int endX = Math.min(width, startX + (int)(gc.getCanvas().getWidth() / TILE_SIZE) + 2);
        int endY = Math.min(height, startY + (int)(gc.getCanvas().getHeight() / TILE_SIZE) + 2);
        
        // 渲染瓦片
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                Tile tile = getTile(x, y);
                if (tile != null) {
                    double screenX = x * TILE_SIZE - cameraX;
                    double screenY = y * TILE_SIZE - cameraY;
                    TileGraphics.drawTile(gc, tile.getType(), screenX, screenY, TILE_SIZE);
                }
            }
        }
        
        // 渲染实体
        for (Entity entity : entities) {
            if (entity.isActive()) {
                gc.save();
                gc.translate(-cameraX, -cameraY);
                entity.render(gc);
                gc.restore();
            }
        }
    }
    
    // Getters
    
    public String getMapName() {
        return mapName;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public List<Entity> getEntities() {
        return entities;
    }
}
