package com.game.potato.inventory;

/**
 * 游戏物品
 */
public class Item {
    
    /**
     * 物品类型
     */
    public enum ItemType {
        CONSUMABLE,  // 消耗品
        EQUIPMENT,   // 装备
        QUEST,       // 任务物品
        MATERIAL,    // 材料
        KEY          // 钥匙
    }
    
    /**
     * 物品稀有度
     */
    public enum Rarity {
        COMMON,      // 普通（白色）
        UNCOMMON,    // 非凡（绿色）
        RARE,        // 稀有（蓝色）
        EPIC,        // 史诗（紫色）
        LEGENDARY    // 传说（橙色）
    }
    
    private final String id;
    private final String name;
    private final String description;
    private final ItemType type;
    private final Rarity rarity;
    private final int maxStackSize;
    private final int value;  // 价值（金币）
    
    public Item(String id, String name, String description, ItemType type, 
                Rarity rarity, int maxStackSize, int value) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.rarity = rarity;
        this.maxStackSize = maxStackSize;
        this.value = value;
    }
    
    /**
     * 使用物品
     * @param target 使用目标（如角色）
     * @return 是否成功使用
     */
    public boolean use(Object target) {
        // 默认实现，子类应该重写此方法
        return false;
    }
    
    /**
     * 是否可堆叠
     */
    public boolean isStackable() {
        return maxStackSize > 1;
    }
    
    // Getters
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public ItemType getType() {
        return type;
    }
    
    public Rarity getRarity() {
        return rarity;
    }
    
    public int getMaxStackSize() {
        return maxStackSize;
    }
    
    public int getValue() {
        return value;
    }
}
