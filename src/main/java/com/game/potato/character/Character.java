package com.game.potato.character;

import com.game.potato.world.Entity;
import javafx.scene.canvas.GraphicsContext;

/**
 * 游戏角色基类
 * 玩家、敌人、NPC都继承此类
 */
public abstract class Character extends Entity {
    
    protected String name;
    protected CharacterStats stats;
    protected Direction direction;
    protected boolean moving;
    protected double moveSpeed;
    
    /**
     * 移动方向枚举
     */
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
    
    public Character(String name, double x, double y, int level) {
        super(x, y, 32, 32); // 默认32x32大小
        this.name = name;
        this.stats = new CharacterStats(level);
        this.direction = Direction.DOWN;
        this.moving = false;
        this.moveSpeed = 100.0; // 像素/秒
    }
    
    /**
     * 移动角色
     */
    public void move(Direction dir, double deltaTime, com.game.potato.world.GameMap map) {
        this.direction = dir;
        this.moving = true;
        
        double dx = 0, dy = 0;
        double moveDistance = moveSpeed * deltaTime;
        
        switch (dir) {
            case UP:
                dy = -moveDistance;
                break;
            case DOWN:
                dy = moveDistance;
                break;
            case LEFT:
                dx = -moveDistance;
                break;
            case RIGHT:
                dx = moveDistance;
                break;
        }
        
        // 计算新位置
        double newX = position.getX() + dx;
        double newY = position.getY() + dy;
        
        // 检查碰撞
        if (canMoveTo(newX, newY, map)) {
            position.set(newX, newY);
        }
    }
    
    /**
     * 检查是否可以移动到指定位置
     */
    protected boolean canMoveTo(double x, double y, com.game.potato.world.GameMap map) {
        // 转换为瓦片坐标
        int tileX = (int)(x / com.game.potato.world.GameMap.TILE_SIZE);
        int tileY = (int)(y / com.game.potato.world.GameMap.TILE_SIZE);
        
        // 检查四个角
        int tileX2 = (int)((x + size.getX()) / com.game.potato.world.GameMap.TILE_SIZE);
        int tileY2 = (int)((y + size.getY()) / com.game.potato.world.GameMap.TILE_SIZE);
        
        return map.isWalkable(tileX, tileY) &&
               map.isWalkable(tileX2, tileY) &&
               map.isWalkable(tileX, tileY2) &&
               map.isWalkable(tileX2, tileY2);
    }
    
    /**
     * 受到攻击
     */
    public void takeDamage(int damage) {
        boolean died = stats.takeDamage(damage);
        if (died) {
            onDeath();
        }
    }
    
    /**
     * 死亡处理
     */
    protected abstract void onDeath();
    
    /**
     * 获取攻击伤害
     */
    public int getAttackDamage() {
        return stats.getAttack();
    }
    
    // Getters and Setters
    
    public String getName() {
        return name;
    }
    
    public CharacterStats getStats() {
        return stats;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public boolean isMoving() {
        return moving;
    }
    
    public void setMoving(boolean moving) {
        this.moving = moving;
    }
    
    public double getMoveSpeed() {
        return moveSpeed;
    }
    
    public void setMoveSpeed(double moveSpeed) {
        this.moveSpeed = moveSpeed;
    }
}
