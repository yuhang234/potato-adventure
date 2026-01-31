package com.potatoadventure.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 玩家角色类
 */
public class Player {
    
    // 位置
    private int x;
    private int y;
    
    // 基础属性
    private String name;
    private int level;
    private int exp;
    private int hp;
    private int maxHp;
    private int mp;
    private int maxMp;
    private int attack;
    private int defense;
    private int speed;
    
    // 渲染相关
    private static final int TILE_SIZE = 32;
    
    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.name = "土豆勇士";
        this.level = 1;
        this.exp = 0;
        this.maxHp = 100;
        this.hp = maxHp;
        this.maxMp = 50;
        this.mp = maxMp;
        this.attack = 15;
        this.defense = 10;
        this.speed = 5;
    }
    
    /**
     * 移动角色
     */
    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }
    
    /**
     * 受到伤害
     */
    public void takeDamage(int damage) {
        int actualDamage = Math.max(1, damage - defense / 2);
        hp = Math.max(0, hp - actualDamage);
        System.out.println(name + " 受到 " + actualDamage + " 点伤害");
    }
    
    /**
     * 治疗
     */
    public void heal(int amount) {
        hp = Math.min(maxHp, hp + amount);
        System.out.println(name + " 恢复了 " + amount + " 点生命值");
    }
    
    /**
     * 消耗魔法
     */
    public boolean consumeMp(int amount) {
        if (mp >= amount) {
            mp -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * 恢复魔法
     */
    public void restoreMp(int amount) {
        mp = Math.min(maxMp, mp + amount);
    }
    
    /**
     * 获得经验值
     */
    public void gainExp(int amount) {
        exp += amount;
        System.out.println("获得 " + amount + " 点经验值");
        
        // 检查升级
        while (exp >= getExpToNextLevel()) {
            levelUp();
        }
    }
    
    /**
     * 升级
     */
    private void levelUp() {
        exp -= getExpToNextLevel();
        level++;
        
        // 属性提升
        maxHp += 20;
        hp = maxHp;
        maxMp += 10;
        mp = maxMp;
        attack += 3;
        defense += 2;
        speed += 1;
        
        System.out.println("恭喜！等级提升到 " + level + " 级！");
    }
    
    /**
     * 获取升级所需经验值
     */
    public int getExpToNextLevel() {
        return level * 100;
    }
    
    /**
     * 是否存活
     */
    public boolean isAlive() {
        return hp > 0;
    }
    
    /**
     * 渲染角色
     */
    public void render(GraphicsContext gc) {
        int screenX = x * TILE_SIZE;
        int screenY = y * TILE_SIZE;
        
        // 绘制玩家（土豆形状）
        gc.setFill(Color.rgb(210, 180, 140)); // 土豆色
        gc.fillOval(screenX + 4, screenY + 4, TILE_SIZE - 8, TILE_SIZE - 8);
        
        // 绘制眼睛
        gc.setFill(Color.BLACK);
        gc.fillOval(screenX + 10, screenY + 12, 4, 4);
        gc.fillOval(screenX + 18, screenY + 12, 4, 4);
        
        // 绘制嘴巴
        gc.strokeArc(screenX + 10, screenY + 14, 12, 8, 180, 180, javafx.scene.shape.ArcType.OPEN);
        
        // 绘制武器（剑）
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(2);
        gc.strokeLine(screenX + 26, screenY + 10, screenX + 30, screenY + 6);
    }
    
    // Getters and Setters
    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }
    
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    
    public int getMp() { return mp; }
    public void setMp(int mp) { this.mp = mp; }
    
    public int getMaxMp() { return maxMp; }
    public void setMaxMp(int maxMp) { this.maxMp = maxMp; }
    
    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }
    
    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }
    
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
}
