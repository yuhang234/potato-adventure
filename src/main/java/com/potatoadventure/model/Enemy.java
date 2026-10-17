package com.potatoadventure.model;

import com.potatoadventure.ui.Palette;
import com.potatoadventure.ui.Sprites;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 敌人类。
 *
 * <p>M1 修正：<b>缺陷 3（单次击杀重复结算双倍经验）</b>。旧实现在
 * {@code GameEngine.updatePlaying}（大地图攻击）与 {@code updateBattle}（战斗内击杀）两处
 * 各自调用 {@code player.gainExp(enemy.getExpReward())}，同一个敌人只要在两处都满足
 * {@code !isAlive()} 就会被结算两次。现在奖励发放收敛到 {@link #claimDefeatReward()}：
 * 每个敌人实例只可能成功领取一次，重复调用返回 {@code false}。
 */
public class Enemy {

    private int x;
    private int y;

    private String name;
    private int level;
    private int hp;
    private int maxHp;
    private int attack;
    private int defense;
    private int expReward;

    private EnemyType type;

    /** 击败奖励是否已经发放过（幂等护栏）。 */
    private boolean rewardClaimed;

    public enum EnemyType {
        SLIME("史莱姆", Palette.SLIME_GREEN),
        GOBLIN("哥布林", Palette.TRUNK),
        WOLF("野狼", Palette.WOLF_FUR);

        private final String displayName;
        private final Color color;

        EnemyType(String displayName, Color color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Color getColor() {
            return color;
        }
    }

    public Enemy(int x, int y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;

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
     * 扣除生命值。
     *
     * <p>与 {@link Player#takeDamage(int)} 一样，参数是<b>已经算好</b>的最终伤害：
     * 防御减免由 {@link com.potatoadventure.service.BattleService} 统一计算一次。
     */
    public void takeDamage(int rawDamage) {
        if (rawDamage <= 0) {
            return;
        }
        hp = Math.max(0, hp - rawDamage);
        System.out.println(name + " 受到 " + rawDamage + " 点伤害");

        if (!isAlive()) {
            System.out.println(name + " 被击败了！");
        }
    }

    /**
     * 领取击败奖励。本实例第一次调用返回 {@code true}，之后恒为 {@code false}，
     * 因此同一个敌人不可能被重复结算经验。
     */
    public boolean claimDefeatReward() {
        if (rewardClaimed) {
            return false;
        }
        rewardClaimed = true;
        return true;
    }

    public boolean isRewardClaimed() {
        return rewardClaimed;
    }

    /**
     * 是否存活。
     */
    public boolean isAlive() {
        return hp > 0;
    }

    /**
     * 渲染敌人 —— 直接贴已批准的敌怪精灵
     * （{@code sprites/enemy_slime.png} / {@code enemy_goblin.png} / {@code enemy_wolf.png}），
     * 头顶保留一条 3px 血条。{@code camX}/{@code camY} 是像素对齐的摄像机偏移。
     */
    public void render(GraphicsContext gc, int camX, int camY) {
        int screenX = x * GameMap.TILE_SIZE - camX;
        int screenY = y * GameMap.TILE_SIZE - camY;

        Sprites.enemy(gc, type, screenX + GameMap.TILE_SIZE / 2.0, screenY + GameMap.TILE_SIZE);

        // 血条（深色底槽 + 红色血条）
        gc.setFill(Palette.PANEL_BASE);
        gc.fillRect(screenX, screenY - 6, GameMap.TILE_SIZE, 4);
        gc.setFill(Palette.HP_RED);
        double hpPercent = (double) hp / maxHp;
        gc.fillRect(screenX + 1, screenY - 5, (GameMap.TILE_SIZE - 2) * hpPercent, 2);
    }

    // Getters and Setters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getExpReward() {
        return expReward;
    }

    public void setExpReward(int expReward) {
        this.expReward = expReward;
    }

    public EnemyType getType() {
        return type;
    }

    public void setType(EnemyType type) {
        this.type = type;
    }
}
