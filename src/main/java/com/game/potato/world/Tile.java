package com.game.potato.world;

/**
 * 地图瓦片
 * 表示地图上的一个单元格
 */
public class Tile {
    
    /**
     * 瓦片类型枚举
     */
    public enum TileType {
        GRASS,      // 草地
        DIRT,       // 泥土
        STONE,      // 石头
        WATER,      // 水
        SAND,       // 沙地
        WOOD_FLOOR, // 木地板
        WALL,       // 墙壁
        DOOR,       // 门
        CHEST,      // 宝箱
        STAIRS_UP,  // 上楼梯
        STAIRS_DOWN // 下楼梯
    }
    
    private final TileType type;
    private boolean walkable;
    private boolean transparent;  // 是否透明（影响视野）
    private int terrainCost;      // 移动消耗
    
    public Tile(TileType type) {
        this.type = type;
        initializeProperties();
    }
    
    /**
     * 根据类型初始化属性
     */
    private void initializeProperties() {
        switch (type) {
            case GRASS:
            case DIRT:
            case SAND:
            case WOOD_FLOOR:
                walkable = true;
                transparent = true;
                terrainCost = 1;
                break;
                
            case STONE:
                walkable = true;
                transparent = true;
                terrainCost = 1;
                break;
                
            case WATER:
                walkable = false;
                transparent = true;
                terrainCost = 3;
                break;
                
            case WALL:
                walkable = false;
                transparent = false;
                terrainCost = Integer.MAX_VALUE;
                break;
                
            case DOOR:
                walkable = true;
                transparent = false;
                terrainCost = 1;
                break;
                
            case CHEST:
            case STAIRS_UP:
            case STAIRS_DOWN:
                walkable = true;
                transparent = true;
                terrainCost = 1;
                break;
                
            default:
                walkable = true;
                transparent = true;
                terrainCost = 1;
        }
    }
    
    // Getters
    
    public TileType getType() {
        return type;
    }
    
    public boolean isWalkable() {
        return walkable;
    }
    
    public void setWalkable(boolean walkable) {
        this.walkable = walkable;
    }
    
    public boolean isTransparent() {
        return transparent;
    }
    
    public int getTerrainCost() {
        return terrainCost;
    }
}
