package com.potatoadventure.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 敌人类
 */
public class Enemy {
    
    // 位置
    private int x;
    private int y;
    
    // 基础属性
    private String name;
    private int level;
    private int hp;
    private int maxHp;
    private int attack;
    private int defense;
    private int expReward;
    
    // 敌人类型
    private EnemyType type;
    
    // 渲染相关
    private static final int TILE_SIZE = 32;
    
    public enum EnemyType {
        SLIME("史莱姆", Color.GREEN),
        GOBLIN("哥布林", Color.rgb(139, 69, 19)),
        WOLF("野狼", Color.GRAY);
        
        private final String displayName;
        private final Color color;
        
        EnemyType(String displayName, Color color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public Color getColor() { return color; }
    }
    
    public Enemy(int x, int y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
        
        // 根据名称确定类型
        if (name.contains("史莱姆")) {
            this.type = EnemyType.SLIME;
            this.level = 1;
            this.maxHp = 40;
            this.attack = 8;
            this.defense = 5;
            this.expReward = 20;
        } else if (name.contains("哥布林")) {
            this.type = EnemyType.GOBLIN;
            this.level = 2;
            this.maxHp = 60;
            this.attack = 12;
            this.defense = 8;
            this.expReward = 35;
        } else if (name.contains("野狼")) {
            this.type = EnemyType.WOLF;
            this.level = 3;
            this.maxHp = 80;
            this.attack = 15;
            this.defense = 10;
            this.expReward = 50;
        } else {
            // 默认
            this.type = EnemyType.SLIME;
            this.level = 1;
            this.maxHp = 50;
            this.attack = 10;
            this.defense = 5;
            this.expReward = 25;
        }
        
        this.hp = maxHp;
    }
    
    /**
     * 受到伤害
     */
    public void takeDamage(int damage) {
        int actualDamage = Math.max(1, damage - defense / 2);
        hp = Math.max(0, hp - actualDamage);
        System.out.println(name + " 受到 " + actualDamage + " 点伤害");
        
        if (!isAlive()) {
            System.out.println(name + " 被击败了！");
        }
    }
    
    /**
     * 是否存活
     */
    public boolean isAlive() {
        return hp > 0;
    }
    
    /**
     * 渲染敌人
     */
    public void render(GraphicsContext gc) {
        int screenX = x * TILE_SIZE;
        int screenY = y * TILE_SIZE;
        
        Color color = type.getColor();
        
        switch (type) {
            case SLIME:
                // 史莱姆（圆形）
                gc.setFill(color);
                gc.fillOval(screenX + 4, screenY + 8, TILE_SIZE - 8, TILE_SIZE - 12);
                // 眼睛
                gc.setFill(Color.WHITE);
                gc.fillOval(screenX + 10, screenY + 14, 4, 4);
                gc.fillOval(screenX + 18, screenY + 14, 4, 4);
                gc.setFill(Color.BLACK);
                gc.fillOval(screenX + 11, screenY + 15, 2, 2);
                gc.fillOval(screenX + 19, screenY + 15, 2, 2);
                break;
                
            case GOBLIN:
                // 哥布林（三角形身体）
                gc.setFill(color);
                gc.fillPolygon(
                    new double[]{screenX + 16, screenX + 8, screenX + 24},
                    new double[]{screenY + 8, screenY + 24, screenY + 24},
                    3
                );
                // 头部
                gc.fillOval(screenX + 10, screenY + 6, 12, 12);
                // 眼睛
                gc.setFill(Color.RED);
                gc.fillOval(screenX + 12, screenY + 10, 3, 3);
                gc.fillOval(screenX + 17, screenY + 10, 3, 3);
                break;
                
            case WOLF:
                // 野狼（四边形）
                gc.setFill(color);
                // 身体
                gc.fillRect(screenX + 6, screenY + 12, 20, 12);
                // 头部
                gc.fillOval(screenX + 18, screenY + 8, 10, 10);
                // 耳朵
                gc.fillPolygon(
                    new double[]{screenX + 20, screenX + 18, screenX + 22},
                    new double[]{screenY + 8, screenY + 4, screenY + 8},
                    3
                );
                gc.fillPolygon(
                    new double[]{screenX + 26, screenX + 24, screenX + 28},
                    new double[]{screenY + 8, screenY + 4, screenY + 8},
                    3
                );
                // 眼睛
                gc.setFill(Color.YELLOW);
                gc.fillOval(screenX + 20, screenY + 10, 3, 3);
                gc.fillOval(screenX + 24, screenY + 10, 3, 3);
                break;
        }
        
        // 绘制血条
        gc.setFill(Color.RED);
        gc.fillRect(screenX, screenY - 4, TILE_SIZE, 3);
        gc.setFill(Color.GREEN);
        double hpPercent = (double) hp / maxHp;
        gc.fillRect(screenX, screenY - 4, TILE_SIZE * hpPercent, 3);
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
    
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    
    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }
    
    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }
    
    public int getExpReward() { return expReward; }
    public void setExpReward(int expReward) { this.expReward = expReward; }
    
    public EnemyType getType() { return type; }
    public void setType(EnemyType type) { this.type = type; }
}
