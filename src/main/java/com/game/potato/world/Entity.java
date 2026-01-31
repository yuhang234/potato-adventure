package com.game.potato.world;

import com.game.potato.util.Vector2;
import javafx.scene.canvas.GraphicsContext;

/**
 * 游戏实体基类
 * 所有游戏对象（玩家、敌人、NPC、道具等）都继承此类
 */
public abstract class Entity {
    
    protected Vector2 position;
    protected Vector2 size;
    protected boolean active;
    protected String id;
    
    public Entity(double x, double y, double width, double height) {
        this.position = new Vector2(x, y);
        this.size = new Vector2(width, height);
        this.active = true;
        this.id = generateId();
    }
    
    /**
     * 生成唯一ID
     */
    private String generateId() {
        return this.getClass().getSimpleName() + "_" + System.nanoTime();
    }
    
    /**
     * 更新实体逻辑
     */
    public abstract void update(double deltaTime);
    
    /**
     * 渲染实体
     */
    public abstract void render(GraphicsContext gc);
    
    /**
     * 获取实体边界矩形（用于碰撞检测）
     */
    public com.game.potato.util.Rect getBounds() {
        return new com.game.potato.util.Rect(
            position.getX(),
            position.getY(),
            size.getX(),
            size.getY()
        );
    }
    
    /**
     * 检测与另一个实体的碰撞
     */
    public boolean collidesWith(Entity other) {
        return this.getBounds().intersects(other.getBounds());
    }
    
    // Getters and Setters
    
    public Vector2 getPosition() {
        return position;
    }
    
    public void setPosition(double x, double y) {
        this.position.set(x, y);
    }
    
    public void setPosition(Vector2 pos) {
        this.position.set(pos.getX(), pos.getY());
    }
    
    public Vector2 getSize() {
        return size;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public String getId() {
        return id;
    }
    
    /**
     * 获取实体中心点
     */
    public Vector2 getCenter() {
        return new Vector2(
            position.getX() + size.getX() / 2,
            position.getY() + size.getY() / 2
        );
    }
}
