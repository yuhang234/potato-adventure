package com.potatoadventure.service;

import com.potatoadventure.model.Player;
import com.potatoadventure.model.Enemy;

/**
 * 战斗服务 - 处理战斗逻辑
 */
public class BattleService {
    
    private Enemy currentEnemy;
    private boolean inBattle;
    
    public BattleService() {
        this.inBattle = false;
    }
    
    /**
     * 开始战斗
     */
    public void startBattle(Player player, Enemy enemy) {
        this.currentEnemy = enemy;
        this.inBattle = true;
        System.out.println("=== 战斗开始！===");
        System.out.println("遭遇了 " + enemy.getName() + "！");
    }
    
    /**
     * 结束战斗
     */
    public void endBattle() {
        this.currentEnemy = null;
        this.inBattle = false;
        System.out.println("=== 战斗结束 ===");
    }
    
    /**
     * 玩家普通攻击
     */
    public void playerAttack(Player player, Enemy enemy) {
        if (!enemy.isAlive()) {
            return;
        }
        
        // 计算伤害
        int baseDamage = player.getAttack();
        int damage = calculateDamage(baseDamage, enemy.getDefense());
        
        // 暴击判定（20%概率）
        if (Math.random() < 0.2) {
            damage *= 2;
            System.out.println("暴击！");
        }
        
        // 造成伤害
        enemy.takeDamage(damage);
        System.out.println(player.getName() + " 对 " + enemy.getName() + " 造成了 " + damage + " 点伤害");
    }
    
    /**
     * 玩家使用技能
     */
    public void playerSkill(Player player, Enemy enemy, int skillId) {
        if (!enemy.isAlive()) {
            return;
        }
        
        switch (skillId) {
            case 1:
                // 火球术（消耗10MP，1.5倍伤害）
                if (player.consumeMp(10)) {
                    int damage = calculateDamage((int)(player.getAttack() * 1.5), enemy.getDefense());
                    enemy.takeDamage(damage);
                    System.out.println(player.getName() + " 使用火球术！造成 " + damage + " 点伤害");
                } else {
                    System.out.println("魔法不足！");
                }
                break;
                
            case 2:
                // 治疗术（消耗15MP，恢复30%HP）
                if (player.consumeMp(15)) {
                    int healAmount = (int)(player.getMaxHp() * 0.3);
                    player.heal(healAmount);
                    System.out.println(player.getName() + " 使用治疗术！恢复 " + healAmount + " 点生命值");
                } else {
                    System.out.println("魔法不足！");
                }
                break;
                
            case 3:
                // 冰冻术（消耗12MP，普通伤害+减速）
                if (player.consumeMp(12)) {
                    int damage = calculateDamage(player.getAttack(), enemy.getDefense());
                    enemy.takeDamage(damage);
                    System.out.println(player.getName() + " 使用冰冻术！造成 " + damage + " 点伤害并冰冻敌人");
                } else {
                    System.out.println("魔法不足！");
                }
                break;
                
            default:
                System.out.println("未知技能！");
                break;
        }
    }
    
    /**
     * 敌人攻击
     */
    public void enemyAttack(Enemy enemy, Player player) {
        if (!enemy.isAlive() || !player.isAlive()) {
            return;
        }
        
        // 计算伤害
        int baseDamage = enemy.getAttack();
        int damage = calculateDamage(baseDamage, player.getDefense());
        
        // 闪避判定（10%概率）
        if (Math.random() < 0.1) {
            System.out.println(player.getName() + " 闪避了攻击！");
            return;
        }
        
        // 造成伤害
        player.takeDamage(damage);
        System.out.println(enemy.getName() + " 对 " + player.getName() + " 造成了 " + damage + " 点伤害");
    }
    
    /**
     * 计算实际伤害
     */
    private int calculateDamage(int attack, int defense) {
        // 伤害公式：攻击力 - 防御力/2，最少1点伤害
        int damage = attack - defense / 2;
        return Math.max(1, damage);
    }
    
    /**
     * 获取当前战斗的敌人
     */
    public Enemy getCurrentEnemy() {
        return currentEnemy;
    }
    
    /**
     * 是否在战斗中
     */
    public boolean isInBattle() {
        return inBattle;
    }
}
