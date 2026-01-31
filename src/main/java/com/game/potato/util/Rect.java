package com.game.potato.util;

/**
 * 矩形类
 * 用于碰撞检测和区域表示
 */
public class Rect {
    
    public double x;
    public double y;
    public double width;
    public double height;
    
    public Rect() {
        this(0, 0, 0, 0);
    }
    
    public Rect(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    /**
     * 获取左边界
     */
    public double getLeft() {
        return x;
    }
    
    /**
     * 获取右边界
     */
    public double getRight() {
        return x + width;
    }
    
    /**
     * 获取上边界
     */
    public double getTop() {
        return y;
    }
    
    /**
     * 获取下边界
     */
    public double getBottom() {
        return y + height;
    }
    
    /**
     * 获取中心点
     */
    public Vector2 getCenter() {
        return new Vector2(x + width / 2, y + height / 2);
    }
    
    /**
     * 检查是否与另一个矩形相交（AABB碰撞检测）
     */
    public boolean intersects(Rect other) {
        return !(getRight() <= other.getLeft() ||
                 getLeft() >= other.getRight() ||
                 getBottom() <= other.getTop() ||
                 getTop() >= other.getBottom());
    }
    
    /**
     * 检查点是否在矩形内
     */
    public boolean contains(double px, double py) {
        return px >= getLeft() && px <= getRight() &&
               py >= getTop() && py <= getBottom();
    }
    
    /**
     * 检查点是否在矩形内
     */
    public boolean contains(Vector2 point) {
        return contains(point.x, point.y);
    }
    
    /**
     * 设置矩形位置
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * 设置矩形大小
     */
    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }
    
    @Override
    public String toString() {
        return String.format("Rect(%.2f, %.2f, %.2f, %.2f)", x, y, width, height);
    }
}
