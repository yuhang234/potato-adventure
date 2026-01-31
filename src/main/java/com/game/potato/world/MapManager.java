package com.game.potato.world;

import java.util.HashMap;
import java.util.Map;

/**
 * 地图管理器
 * 负责管理游戏中的所有地图
 */
public class MapManager {
    
    private Map<String, GameMap> maps;
    private GameMap currentMap;
    
    public MapManager() {
        this.maps = new HashMap<>();
    }
    
    /**
     * 添加地图
     */
    public void addMap(String name, GameMap map) {
        maps.put(name, map);
    }
    
    /**
     * 获取地图
     */
    public GameMap getMap(String name) {
        return maps.get(name);
    }
    
    /**
     * 设置当前地图
     */
    public void setCurrentMap(String name) {
        this.currentMap = maps.get(name);
    }
    
    /**
     * 获取当前地图
     */
    public GameMap getCurrentMap() {
        return currentMap;
    }
    
    /**
     * 移除地图
     */
    public void removeMap(String name) {
        maps.remove(name);
    }
    
    /**
     * 清空所有地图
     */
    public void clear() {
        maps.clear();
        currentMap = null;
    }
}
