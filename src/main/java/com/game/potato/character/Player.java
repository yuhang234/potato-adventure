package com.game.potato.character;

import com.game.potato.graphics.PlayerGraphics;
import com.game.potato.inventory.Inventory;
import javafx.scene.canvas.GraphicsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 玩家角色
 */
public class Player extends Character {
    
    private static final Logger logger = LoggerFactory.getLogger(Player.class);
    
    private Inventory inventory;
    private int gold;
    
    public Player(String name, double x, double y) {
        super(name, x, y, 1);
        this.inventory = new Inventory(30); // 30格背包
        this.gold = 0;
        logger.info("创建玩家: {}, 位置: ({}, {})", name, x, y);
    }
    
    @Override
    public void update(double deltaTime) {
        // 玩家的更新逻辑由输入控制，这里可以添加自动恢复等逻辑
        
        // 示例：每秒恢复1点MP
        if (stats.getCurrentMp() < stats.getMaxMp()) {
            // TODO: 添加MP恢复逻辑
        }
    }
    
    @Override
    public void render(GraphicsContext gc) {
        PlayerGraphics.drawPlayer(gc, position.getX(), position.getY(), 
                                  direction, moving);
    }
    
    @Override
    protected void onDeath() {
        logger.warn("玩家死亡: {}", name);
        // TODO: 触发游戏结束或复活逻辑
    }
    
    /**
     * 拾取物品
     */
    public boolean pickupItem(com.game.potato.inventory.Item item) {
        boolean success = inventory.addItem(item);
        if (success) {
            logger.info("拾取物品: {}", item.getName());
        } else {
            logger.warn("背包已满，无法拾取: {}", item.getName());
        }
        return success;
    }
    
    /**
     * 增加金币
     */
    public void addGold(int amount) {
        gold += amount;
        logger.info("获得金币: {}, 当前金币: {}", amount, gold);
    }
    
    /**
     * 消费金币
     * @return 是否成功消费
     */
    public boolean spendGold(int amount) {
        if (gold >= amount) {
            gold -= amount;
            logger.info("消费金币: {}, 剩余金币: {}", amount, gold);
            return true;
        }
        logger.warn("金币不足，需要: {}, 当前: {}", amount, gold);
        return false;
    }
    
    // Getters
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public int getGold() {
        return gold;
    }
}
