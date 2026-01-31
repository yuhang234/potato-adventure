package com.game.potato.character;

import com.game.potato.graphics.EnemyGraphics;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 敌人角色
 */
public class Enemy extends Character {
    
    private static final Logger logger = LoggerFactory.getLogger(Enemy.class);
    
    /**
     * 敌人类型
     */
    public enum EnemyType {
        SLIME,      // 史莱姆
        SKELETON,   // 骷髅
        GOBLIN,     // 哥布林
        DRAGON      // 龙
    }
    
    private final EnemyType type;
    private final int expReward;
    private final int goldReward;
    
    public Enemy(String name, EnemyType type, double x, double y, int level) {
        super(name, x, y, level);
        this.type = type;
        this.expReward = level * 10;
        this.goldReward = level * 5;
        
        // 根据类型调整属性
        adjustStatsByType();
    }
    
    /**
     * 根据敌人类型调整属性
     */
    private void adjustStatsByType() {
        switch (type) {
            case SLIME:
                // 史莱姆：低攻高防
                stats.setAttack((int)(stats.getAttack() * 0.8));
                stats.setDefense((int)(stats.getDefense() * 1.3));
                moveSpeed = 60;
                break;
                
            case SKELETON:
                // 骷髅：均衡
                moveSpeed = 80;
                break;
                
            case GOBLIN:
                // 哥布林：高攻低防
                stats.setAttack((int)(stats.getAttack() * 1.4));
                stats.setDefense((int)(stats.getDefense() * 0.7));
                moveSpeed = 100;
                break;
                
            case DRAGON:
                // 龙：全属性强化
                stats.setAttack((int)(stats.getAttack() * 2.0));
                stats.setDefense((int)(stats.getDefense() * 1.5));
                stats.setMagic((int)(stats.getMagic() * 1.8));
                moveSpeed = 70;
                break;
        }
    }
    
    @Override
    public void update(double deltaTime) {
        // 简单的AI：随机移动
        if (Math.random() < 0.02) {
            Direction randomDir = Direction.values()[(int)(Math.random() * 4)];
            direction = randomDir;
            moving = true;
        } else {
            moving = false;
        }
    }
    
    @Override
    public void render(GraphicsContext gc) {
        // 根据类型渲染
        switch (type) {
            case SLIME:
                EnemyGraphics.drawSlime(gc, position.getX(), position.getY(), Color.GREEN);
                break;
            case SKELETON:
                EnemyGraphics.drawSkeleton(gc, position.getX(), position.getY());
                break;
            case GOBLIN:
                EnemyGraphics.drawGoblin(gc, position.getX(), position.getY());
                break;
            case DRAGON:
                EnemyGraphics.drawDragon(gc, position.getX(), position.getY(), 1.0);
                break;
        }
        
        // 渲染生命条
        if (stats.getCurrentHp() < stats.getMaxHp()) {
            EnemyGraphics.drawEnemyHealthBar(gc, position.getX(), position.getY(), 
                stats.getCurrentHp(), stats.getMaxHp());
        }
    }
    
    @Override
    protected void onDeath() {
        logger.info("敌人死亡: {}", name);
        setActive(false);
        // 掉落经验和金币会在战斗系统中处理
    }
    
    // Getters
    
    public EnemyType getType() {
        return type;
    }
    
    public int getExpReward() {
        return expReward;
    }
    
    public int getGoldReward() {
        return goldReward;
    }
}
