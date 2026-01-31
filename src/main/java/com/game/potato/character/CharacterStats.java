package com.game.potato.character;

/**
 * 角色属性
 * 包含HP、MP、攻击、防御等基础属性
 */
public class CharacterStats {
    
    // 基础属性
    private int level;           // 等级
    private int experience;      // 经验值
    private int experienceToNext; // 升级所需经验
    
    // 生命和魔法
    private int maxHp;
    private int currentHp;
    private int maxMp;
    private int currentMp;
    
    // 战斗属性
    private int attack;          // 攻击力
    private int defense;         // 防御力
    private int magic;           // 魔法力
    private int magicDefense;    // 魔法防御
    private int speed;           // 速度
    private int luck;            // 幸运
    
    public CharacterStats(int level) {
        this.level = level;
        this.experience = 0;
        this.experienceToNext = calculateExpForNextLevel(level);
        
        // 初始化基础属性
        initializeBaseStats();
    }
    
    /**
     * 初始化基础属性
     */
    private void initializeBaseStats() {
        // 基于等级计算属性
        this.maxHp = 50 + (level - 1) * 10;
        this.currentHp = maxHp;
        this.maxMp = 30 + (level - 1) * 5;
        this.currentMp = maxMp;
        
        this.attack = 10 + (level - 1) * 2;
        this.defense = 5 + (level - 1) * 1;
        this.magic = 8 + (level - 1) * 2;
        this.magicDefense = 5 + (level - 1) * 1;
        this.speed = 10 + (level - 1) * 1;
        this.luck = 5;
    }
    
    /**
     * 计算升级所需经验
     */
    private int calculateExpForNextLevel(int currentLevel) {
        return (int)(100 * Math.pow(1.2, currentLevel - 1));
    }
    
    /**
     * 增加经验值
     * @return 是否升级
     */
    public boolean addExperience(int exp) {
        experience += exp;
        
        if (experience >= experienceToNext) {
            levelUp();
            return true;
        }
        return false;
    }
    
    /**
     * 升级
     */
    private void levelUp() {
        level++;
        experience -= experienceToNext;
        experienceToNext = calculateExpForNextLevel(level);
        
        // 提升属性
        maxHp += 10;
        currentHp = maxHp;
        maxMp += 5;
        currentMp = maxMp;
        
        attack += 2;
        defense += 1;
        magic += 2;
        magicDefense += 1;
        speed += 1;
    }
    
    /**
     * 受到伤害
     * @return 是否死亡
     */
    public boolean takeDamage(int damage) {
        currentHp -= damage;
        if (currentHp < 0) {
            currentHp = 0;
        }
        return currentHp == 0;
    }
    
    /**
     * 恢复生命值
     */
    public void heal(int amount) {
        currentHp += amount;
        if (currentHp > maxHp) {
            currentHp = maxHp;
        }
    }
    
    /**
     * 消耗魔法值
     * @return 是否成功消耗
     */
    public boolean consumeMp(int amount) {
        if (currentMp >= amount) {
            currentMp -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * 恢复魔法值
     */
    public void restoreMp(int amount) {
        currentMp += amount;
        if (currentMp > maxMp) {
            currentMp = maxMp;
        }
    }
    
    /**
     * 检查是否存活
     */
    public boolean isAlive() {
        return currentHp > 0;
    }
    
    /**
     * 完全恢复
     */
    public void fullRecover() {
        currentHp = maxHp;
        currentMp = maxMp;
    }
    
    // Getters and Setters
    
    public int getLevel() {
        return level;
    }
    
    public int getExperience() {
        return experience;
    }
    
    public int getExperienceToNext() {
        return experienceToNext;
    }
    
    public int getMaxHp() {
        return maxHp;
    }
    
    public int getCurrentHp() {
        return currentHp;
    }
    
    public int getMaxMp() {
        return maxMp;
    }
    
    public int getCurrentMp() {
        return currentMp;
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
    
    public int getMagic() {
        return magic;
    }
    
    public void setMagic(int magic) {
        this.magic = magic;
    }
    
    public int getMagicDefense() {
        return magicDefense;
    }
    
    public void setMagicDefense(int magicDefense) {
        this.magicDefense = magicDefense;
    }
    
    public int getSpeed() {
        return speed;
    }
    
    public void setSpeed(int speed) {
        this.speed = speed;
    }
    
    public int getLuck() {
        return luck;
    }
    
    public void setLuck(int luck) {
        this.luck = luck;
    }
}
