package com.game.potato.util;

/**
 * 二维向量类
 * 用于表示位置、速度、方向等
 */
public class Vector2 {
    
    public double x;
    public double y;
    
    public Vector2() {
        this(0, 0);
    }
    
    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public Vector2(Vector2 other) {
        this.x = other.x;
        this.y = other.y;
    }
    
    /**
     * 向量加法
     */
    public Vector2 add(Vector2 other) {
        return new Vector2(x + other.x, y + other.y);
    }
    
    /**
     * 向量减法
     */
    public Vector2 subtract(Vector2 other) {
        return new Vector2(x - other.x, y - other.y);
    }
    
    /**
     * 标量乘法
     */
    public Vector2 multiply(double scalar) {
        return new Vector2(x * scalar, y * scalar);
    }
    
    /**
     * 标量除法
     */
    public Vector2 divide(double scalar) {
        if (scalar == 0) {
            throw new IllegalArgumentException("不能除以0");
        }
        return new Vector2(x / scalar, y / scalar);
    }
    
    /**
     * 计算向量长度
     */
    public double length() {
        return Math.sqrt(x * x + y * y);
    }
    
    /**
     * 计算向量长度的平方（避免开方运算）
     */
    public double lengthSquared() {
        return x * x + y * y;
    }
    
    /**
     * 归一化向量
     */
    public Vector2 normalize() {
        double len = length();
        if (len == 0) {
            return new Vector2(0, 0);
        }
        return new Vector2(x / len, y / len);
    }
    
    /**
     * 点积
     */
    public double dot(Vector2 other) {
        return x * other.x + y * other.y;
    }
    
    /**
     * 计算两点之间的距离
     */
    public double distance(Vector2 other) {
        return subtract(other).length();
    }
    
    /**
     * 计算两点之间距离的平方
     */
    public double distanceSquared(Vector2 other) {
        return subtract(other).lengthSquared();
    }
    
    /**
     * 设置向量值
     */
    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * 设置为另一个向量的值
     */
    public void set(Vector2 other) {
        this.x = other.x;
        this.y = other.y;
    }
    
    /**
     * 向量是否为零向量
     */
    public boolean isZero() {
        return x == 0 && y == 0;
    }
    
    @Override
    public String toString() {
        return String.format("Vector2(%.2f, %.2f)", x, y);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector2 other = (Vector2) obj;
        return Double.compare(other.x, x) == 0 && Double.compare(other.y, y) == 0;
    }
    
    @Override
    public int hashCode() {
        long xBits = Double.doubleToLongBits(x);
        long yBits = Double.doubleToLongBits(y);
        return (int) (xBits ^ (xBits >>> 32) ^ yBits ^ (yBits >>> 32));
    }
    
    // 静态方法
    public static Vector2 zero() {
        return new Vector2(0, 0);
    }
    
    public static Vector2 one() {
        return new Vector2(1, 1);
    }
    
    public static Vector2 up() {
        return new Vector2(0, -1);
    }
    
    public static Vector2 down() {
        return new Vector2(0, 1);
    }
    
    public static Vector2 left() {
        return new Vector2(-1, 0);
    }
    
    public static Vector2 right() {
        return new Vector2(1, 0);
    }
}
