package com.game.potato.quest;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务系统
 */
public class Quest {
    
    /**
     * 任务状态
     */
    public enum QuestStatus {
        NOT_STARTED,    // 未开始
        IN_PROGRESS,    // 进行中
        COMPLETED,      // 已完成
        FAILED          // 已失败
    }
    
    /**
     * 任务类型
     */
    public enum QuestType {
        MAIN,           // 主线任务
        SIDE,           // 支线任务
        DAILY,          // 日常任务
        REPEATABLE      // 可重复任务
    }
    
    private final String id;
    private final String title;
    private final String description;
    private final QuestType type;
    private QuestStatus status;
    
    private final List<QuestObjective> objectives;
    private final List<QuestReward> rewards;
    
    private final int requiredLevel;
    private final String requiredQuest; // 前置任务ID
    
    public Quest(String id, String title, String description, 
                 QuestType type, int requiredLevel) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.requiredLevel = requiredLevel;
        this.requiredQuest = null;
        this.status = QuestStatus.NOT_STARTED;
        this.objectives = new ArrayList<>();
        this.rewards = new ArrayList<>();
    }
    
    /**
     * 添加任务目标
     */
    public void addObjective(QuestObjective objective) {
        objectives.add(objective);
    }
    
    /**
     * 添加奖励
     */
    public void addReward(QuestReward reward) {
        rewards.add(reward);
    }
    
    /**
     * 开始任务
     */
    public void start() {
        if (status == QuestStatus.NOT_STARTED) {
            status = QuestStatus.IN_PROGRESS;
        }
    }
    
    /**
     * 更新任务进度
     */
    public void update(String objectiveId, int progress) {
        for (QuestObjective objective : objectives) {
            if (objective.getId().equals(objectiveId)) {
                objective.updateProgress(progress);
                break;
            }
        }
        
        // 检查是否所有目标都完成
        if (areAllObjectivesComplete()) {
            complete();
        }
    }
    
    /**
     * 检查所有目标是否完成
     */
    private boolean areAllObjectivesComplete() {
        for (QuestObjective objective : objectives) {
            if (!objective.isComplete()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 完成任务
     */
    private void complete() {
        if (status == QuestStatus.IN_PROGRESS) {
            status = QuestStatus.COMPLETED;
        }
    }
    
    /**
     * 失败任务
     */
    public void fail() {
        if (status == QuestStatus.IN_PROGRESS) {
            status = QuestStatus.FAILED;
        }
    }
    
    /**
     * 检查是否可以接受任务
     */
    public boolean canAccept(int playerLevel, List<String> completedQuests) {
        if (status != QuestStatus.NOT_STARTED) {
            return false;
        }
        
        if (playerLevel < requiredLevel) {
            return false;
        }
        
        if (requiredQuest != null && !completedQuests.contains(requiredQuest)) {
            return false;
        }
        
        return true;
    }
    
    // Getters
    
    public String getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public QuestType getType() {
        return type;
    }
    
    public QuestStatus getStatus() {
        return status;
    }
    
    public List<QuestObjective> getObjectives() {
        return objectives;
    }
    
    public List<QuestReward> getRewards() {
        return rewards;
    }
    
    public int getRequiredLevel() {
        return requiredLevel;
    }
}

/**
 * 任务目标
 */
class QuestObjective {
    
    private final String id;
    private final String description;
    private final int targetAmount;
    private int currentAmount;
    
    public QuestObjective(String id, String description, int targetAmount) {
        this.id = id;
        this.description = description;
        this.targetAmount = targetAmount;
        this.currentAmount = 0;
    }
    
    public void updateProgress(int amount) {
        currentAmount = Math.min(currentAmount + amount, targetAmount);
    }
    
    public boolean isComplete() {
        return currentAmount >= targetAmount;
    }
    
    public String getId() {
        return id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getProgress() {
        return currentAmount;
    }
    
    public int getTargetAmount() {
        return targetAmount;
    }
}

/**
 * 任务奖励
 */
class QuestReward {
    
    public enum RewardType {
        EXPERIENCE,
        GOLD,
        ITEM
    }
    
    private final RewardType type;
    private final int amount;
    private final String itemId;
    
    public QuestReward(RewardType type, int amount) {
        this(type, amount, null);
    }
    
    public QuestReward(RewardType type, int amount, String itemId) {
        this.type = type;
        this.amount = amount;
        this.itemId = itemId;
    }
    
    public RewardType getType() {
        return type;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public String getItemId() {
        return itemId;
    }
}
