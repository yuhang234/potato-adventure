package com.game.potato.inventory;

/**
 * 物品堆栈
 * 表示背包中的一格物品及其数量
 */
public class ItemStack {
    
    private final Item item;
    private int quantity;
    
    public ItemStack(Item item, int quantity) {
        this.item = item;
        this.quantity = Math.min(quantity, item.getMaxStackSize());
    }
    
    /**
     * 增加数量
     * @return 剩余未添加的数量
     */
    public int addQuantity(int amount) {
        int newQuantity = quantity + amount;
        int maxSize = item.getMaxStackSize();
        
        if (newQuantity <= maxSize) {
            quantity = newQuantity;
            return 0;
        } else {
            quantity = maxSize;
            return newQuantity - maxSize;
        }
    }
    
    /**
     * 减少数量
     * @return 是否成功减少
     */
    public boolean removeQuantity(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * 是否已满
     */
    public boolean isFull() {
        return quantity >= item.getMaxStackSize();
    }
    
    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return quantity <= 0;
    }
    
    /**
     * 获取总价值
     */
    public int getTotalValue() {
        return item.getValue() * quantity;
    }
    
    // Getters and Setters
    
    public Item getItem() {
        return item;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = Math.min(quantity, item.getMaxStackSize());
    }
}
