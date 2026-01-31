package com.game.potato.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 背包/库存系统
 */
public class Inventory {
    
    private static final Logger logger = LoggerFactory.getLogger(Inventory.class);
    
    private final int capacity;
    private final List<ItemStack> items;
    
    public Inventory(int capacity) {
        this.capacity = capacity;
        this.items = new ArrayList<>();
    }
    
    /**
     * 添加物品
     * @return 是否成功添加
     */
    public boolean addItem(Item item) {
        return addItem(item, 1);
    }
    
    /**
     * 添加指定数量的物品
     * @return 是否完全添加
     */
    public boolean addItem(Item item, int quantity) {
        if (quantity <= 0) {
            return true;
        }
        
        int remaining = quantity;
        
        // 如果物品可堆叠，先尝试添加到现有堆栈
        if (item.isStackable()) {
            for (ItemStack stack : items) {
                if (stack.getItem().getId().equals(item.getId()) && !stack.isFull()) {
                    int added = Math.min(remaining, item.getMaxStackSize() - stack.getQuantity());
                    stack.addQuantity(added);
                    remaining -= added;
                    
                    if (remaining == 0) {
                        logger.debug("物品添加到现有堆栈: {} x{}", item.getName(), quantity);
                        return true;
                    }
                }
            }
        }
        
        // 创建新堆栈
        while (remaining > 0 && items.size() < capacity) {
            int stackSize = Math.min(remaining, item.getMaxStackSize());
            items.add(new ItemStack(item, stackSize));
            remaining -= stackSize;
        }
        
        if (remaining == 0) {
            logger.debug("物品添加到新堆栈: {} x{}", item.getName(), quantity);
            return true;
        } else {
            logger.warn("背包空间不足，剩余 {} 个 {} 无法添加", remaining, item.getName());
            return false;
        }
    }
    
    /**
     * 移除物品
     * @return 是否成功移除
     */
    public boolean removeItem(Item item, int quantity) {
        int remaining = quantity;
        
        for (int i = items.size() - 1; i >= 0 && remaining > 0; i--) {
            ItemStack stack = items.get(i);
            if (stack.getItem().getId().equals(item.getId())) {
                int removed = Math.min(remaining, stack.getQuantity());
                stack.removeQuantity(removed);
                remaining -= removed;
                
                if (stack.isEmpty()) {
                    items.remove(i);
                }
            }
        }
        
        boolean success = remaining == 0;
        if (success) {
            logger.debug("移除物品: {} x{}", item.getName(), quantity);
        } else {
            logger.warn("物品数量不足: {}, 需要: {}, 剩余缺少: {}", item.getName(), quantity, remaining);
        }
        return success;
    }
    
    /**
     * 检查是否有足够的物品
     */
    public boolean hasItem(Item item, int quantity) {
        int count = 0;
        for (ItemStack stack : items) {
            if (stack.getItem().getId().equals(item.getId())) {
                count += stack.getQuantity();
            }
        }
        return count >= quantity;
    }
    
    /**
     * 获取物品数量
     */
    public int getItemCount(Item item) {
        int count = 0;
        for (ItemStack stack : items) {
            if (stack.getItem().getId().equals(item.getId())) {
                count += stack.getQuantity();
            }
        }
        return count;
    }
    
    /**
     * 清空背包
     */
    public void clear() {
        items.clear();
        logger.debug("背包已清空");
    }
    
    /**
     * 是否已满
     */
    public boolean isFull() {
        return items.size() >= capacity;
    }
    
    /**
     * 剩余空位
     */
    public int getFreeSlots() {
        return capacity - items.size();
    }
    
    // Getters
    
    public int getCapacity() {
        return capacity;
    }
    
    public List<ItemStack> getItems() {
        return items;
    }
    
    /**
     * 获取指定位置的物品堆栈
     */
    public ItemStack getItemAt(int index) {
        if (index >= 0 && index < items.size()) {
            return items.get(index);
        }
        return null;
    }
}
