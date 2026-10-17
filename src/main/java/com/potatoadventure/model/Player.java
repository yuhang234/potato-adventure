package com.potatoadventure.model;

import com.potatoadventure.ui.Sprites;
import javafx.scene.canvas.GraphicsContext;

/**
 * 玩家角色。
 *
 * <p>M1 把“逻辑位置”和“视觉位置”分开：
 *
 * <ul>
 *   <li>逻辑位置是整数格 {@link #getX()} / {@link #getY()}，碰撞与战斗触发只看它；</li>
 *   <li>视觉位置由 {@link #updateMovement} 按时间推进的一步插值给出
 *       （{@link #getVisualPixelX()}），一步耗时 {@link #MOVE_STEP_SECONDS}，
 *       因此移动是连续的而不是逐格瞬移。</li>
 *   <li>短按同样会完整走完一步（起步后即使松开按键也不取消），配合
 *       {@link com.potatoadventure.util.InputHandler} 的有序缓冲实现“按键零丢失”。</li>
 * </ul>
 */
public class Player {

    /** 走一格所需时间（秒）。像素速度 = TILE_SIZE / MOVE_STEP_SECONDS ≈ 278 px/s。 */
    public static final double MOVE_STEP_SECONDS = 0.115;

    /** 逻辑格坐标。 */
    private int x;
    private int y;

    /** 当前这一步的起点格与进度（视觉插值用）。 */
    private int fromX;
    private int fromY;
    private boolean stepping;
    private double stepProgress = 1.0;

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

    /** 职业外观标识，对应 {@code sprites/hero_<class>.png}；M2 起由职业选择界面写入。 */
    private String heroClass = "berserker";

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.fromX = x;
        this.fromY = y;
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

    // ------------------------------------------------------------------ 移动

    /**
     * 按真实时间推进移动。
     *
     * <p>算法：先把当前这一步的剩余时间扣掉；剩下的时间用于“起步”，只要时间够走完整格就
     * 一路推进下去（一帧内最多推进 {@code dt} 对应的格数），因此移动速度严格等于
     * {@code TILE_SIZE / MOVE_STEP_SECONDS} 像素/秒，与帧率无关。
     *
     * @param dt   帧间隔（秒）
     * @param map  只读地图，用于碰撞
     * @param dirX 期望方向 X（-1/0/1）；为 0 时若 {@code dirY} 也为 0 表示没有方向输入
     * @param dirY 期望方向 Y（-1/0/1）
     * @return 本次调用是否新起了一步
     */
    public boolean updateMovement(double dt, GameMap map, int dirX, int dirY) {
        if (dt <= 0) {
            return false;
        }
        boolean startedAny = false;
        // 单帧最多补 1 秒，避免窗口拖动/断点之后瞬移一大截
        double budget = Math.min(dt, 1.0);

        if (stepping) {
            double needed = (1.0 - stepProgress) * MOVE_STEP_SECONDS;
            if (budget < needed) {
                stepProgress += budget / MOVE_STEP_SECONDS;
                return false;
            }
            budget -= needed;
            finishStep();
        }

        if (dirX == 0 && dirY == 0) {
            return startedAny;
        }

        while (true) {
            if (!tryStartStep(map, dirX, dirY)) {
                break;
            }
            startedAny = true;
            if (budget >= MOVE_STEP_SECONDS) {
                finishStep();
                budget -= MOVE_STEP_SECONDS;
                continue;
            }
            // 把本帧剩余时间折算成新一步的起始进度，保证跨帧速度连续
            stepProgress = budget / MOVE_STEP_SECONDS;
            break;
        }
        return startedAny;
    }

    private boolean tryStartStep(GameMap map, int dx, int dy) {
        int nx = x + dx;
        int ny = y + dy;
        if (map.isCollision(nx, ny)) {
            return false;
        }
        fromX = x;
        fromY = y;
        x = nx;
        y = ny;
        stepping = true;
        stepProgress = 0;
        return true;
    }

    private void finishStep() {
        fromX = x;
        fromY = y;
        stepping = false;
        stepProgress = 1.0;
    }

    /** 是否正在走一格。 */
    public boolean isStepping() {
        return stepping;
    }

    /** 当前这一步的进度 0..1。 */
    public double getStepProgress() {
        return stepping ? stepProgress : 1.0;
    }

    /** 视觉像素 X（未减摄像机偏移）。 */
    public double getVisualPixelX() {
        double t = stepping ? stepProgress : 1.0;
        return (fromX + (x - fromX) * t) * GameMap.TILE_SIZE;
    }

    /** 视觉像素 Y（未减摄像机偏移）。 */
    public double getVisualPixelY() {
        double t = stepping ? stepProgress : 1.0;
        return (fromY + (y - fromY) * t) * GameMap.TILE_SIZE;
    }

    /** 摄像机跟随用的中心点像素 X。 */
    public double getCenterPixelX() {
        return getVisualPixelX() + GameMap.TILE_SIZE / 2.0;
    }

    /** 摄像机跟随用的中心点像素 Y。 */
    public double getCenterPixelY() {
        return getVisualPixelY() + GameMap.TILE_SIZE / 2.0;
    }

    /** 让视觉位置立刻对齐逻辑格（读档、逃跑落位、传送）。 */
    public void snapVisual() {
        finishStep();
    }

    // ------------------------------------------------------------------ 战斗

    /**
     * 扣除生命值。
     *
     * <p><b>缺陷“伤害被重复减免”的修正点：</b>本方法接收的是<b>已经由战斗规则算好</b>的
     * 最终伤害，内部不再做防御减免，否则 {@code BattleService} 先减一次防御、这里再减一次，
     * 防御属性会被计两次。
     */
    public void takeDamage(int rawDamage) {
        if (rawDamage <= 0) {
            return;
        }
        hp = Math.max(0, hp - rawDamage);
        System.out.println(name + " 受到 " + rawDamage + " 点伤害");
    }

    /**
     * 治疗。
     */
    public void heal(int amount) {
        if (amount <= 0) {
            return;
        }
        int healed = Math.min(maxHp - hp, amount);
        hp += healed;
        System.out.println(name + " 恢复了 " + healed + " 点生命值");
    }

    /**
     * 消耗魔法。
     */
    public boolean consumeMp(int amount) {
        if (amount < 0) {
            return false;
        }
        if (mp >= amount) {
            mp -= amount;
            return true;
        }
        return false;
    }

    /**
     * 恢复魔法。
     */
    public void restoreMp(int amount) {
        mp = Math.min(maxMp, mp + Math.max(0, amount));
    }

    /**
     * 获得经验值。
     */
    public void gainExp(int amount) {
        if (amount <= 0) {
            return;
        }
        exp += amount;
        System.out.println("获得 " + amount + " 点经验值");

        while (exp >= getExpToNextLevel()) {
            levelUp();
        }
    }

    private void levelUp() {
        exp -= getExpToNextLevel();
        level++;

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
     * 升级所需经验值。
     */
    public int getExpToNextLevel() {
        return level * 100;
    }

    /**
     * 是否存活。
     */
    public boolean isAlive() {
        return hp > 0;
    }

    // ------------------------------------------------------------------ 渲染

    /**
     * 绘制角色 —— 直接贴已批准的角色精灵
     * （{@code design/ui-prototype/assets/sprites/hero_<职业>.png}），
     * 不再用 {@code fillOval} 手绘土豆。
     *
     * <p>{@code camX}/{@code camY} 是像素对齐的摄像机偏移（整数，保证精灵不被半像素采样）。
     */
    public void render(GraphicsContext gc, int camX, int camY) {
        double centerX = getVisualPixelX() + GameMap.TILE_SIZE / 2.0 - camX;
        double bottomY = getVisualPixelY() + GameMap.TILE_SIZE - camY;
        Sprites.hero(gc, centerX, bottomY, heroClass);
    }

    // ------------------------------------------------------------------ 属性

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        snapVisual();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** 职业外观标识（berserker / mage / ranger）。 */
    public String getHeroClass() {
        return heroClass;
    }

    public void setHeroClass(String heroClass) {
        this.heroClass = heroClass == null || heroClass.isBlank() ? "berserker" : heroClass;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
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

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    public int getMaxMp() {
        return maxMp;
    }

    public void setMaxMp(int maxMp) {
        this.maxMp = maxMp;
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

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
