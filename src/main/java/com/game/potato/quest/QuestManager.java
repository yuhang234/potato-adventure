package com.game.potato.quest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务管理器
 * 管理所有任务的状态和进度
 */
public class QuestManager {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestManager.class);
    
    private final Map<String, Quest> allQuests;
    private final List<Quest> activeQuests;
    private final List<String> completedQuestIds;
    
    public QuestManager() {
        this.allQuests = new HashMap<>();
        this.activeQuests = new ArrayList<>();
        this.completedQuestIds = new ArrayList<>();
        
        initializeQuests();
    }
    
    /**
     * 初始化游戏中的所有任务
     */
    private void initializeQuests() {
        // 主线任务1：新的开始
        Quest mainQuest1 = new Quest(
            "main_001",
            "新的开始",
            "探索这个陌生的世界，与村长对话",
            Quest.QuestType.MAIN,
            1
        );
        mainQuest1.addObjective(new QuestObjective("talk_to_mayor", "与村长对话", 1));
        mainQuest1.addReward(new QuestReward(QuestReward.RewardType.EXPERIENCE, 100));
        mainQuest1.addReward(new QuestReward(QuestReward.RewardType.GOLD, 50));
        allQuests.put(mainQuest1.getId(), mainQuest1);
        
        // 支线任务1：清理史莱姆
        Quest sideQuest1 = new Quest(
            "side_001",
            "史莱姆威胁",
            "村外的史莱姆越来越多，击败10只史莱姆",
            Quest.QuestType.SIDE,
            1
        );
        sideQuest1.addObjective(new QuestObjective("kill_slimes", "击败史莱姆", 10));
        sideQuest1.addReward(new QuestReward(QuestReward.RewardType.EXPERIENCE, 150));
        sideQuest1.addReward(new QuestReward(QuestReward.RewardType.GOLD, 100));
        allQuests.put(sideQuest1.getId(), sideQuest1);
        
        logger.info("已加载 {} 个任务", allQuests.size());
    }
    
    /**
     * 接受任务
     */
    public boolean acceptQuest(String questId, int playerLevel) {
        Quest quest = allQuests.get(questId);
        if (quest == null) {
            logger.warn("任务不存在: {}", questId);
            return false;
        }
        
        if (!quest.canAccept(playerLevel, completedQuestIds)) {
            logger.warn("无法接受任务: {} (等级不足或前置任务未完成)", questId);
            return false;
        }
        
        quest.start();
        activeQuests.add(quest);
        logger.info("接受任务: {}", quest.getTitle());
        return true;
    }
    
    /**
     * 更新任务进度
     */
    public void updateQuestProgress(String objectiveId, int progress) {
        for (Quest quest : activeQuests) {
            quest.update(objectiveId, progress);
            
            if (quest.getStatus() == Quest.QuestStatus.COMPLETED) {
                completeQuest(quest);
            }
        }
    }
    
    /**
     * 完成任务
     */
    private void completeQuest(Quest quest) {
        activeQuests.remove(quest);
        completedQuestIds.add(quest.getId());
        logger.info("完成任务: {}", quest.getTitle());
        
        // 给予奖励
        for (QuestReward reward : quest.getRewards()) {
            logger.info("获得奖励: {} x{}", reward.getType(), reward.getAmount());
        }
    }
    
    /**
     * 放弃任务
     */
    public void abandonQuest(String questId) {
        Quest quest = findActiveQuest(questId);
        if (quest != null) {
            activeQuests.remove(quest);
            quest.fail();
            logger.info("放弃任务: {}", quest.getTitle());
        }
    }
    
    /**
     * 查找活跃任务
     */
    private Quest findActiveQuest(String questId) {
        for (Quest quest : activeQuests) {
            if (quest.getId().equals(questId)) {
                return quest;
            }
        }
        return null;
    }
    
    /**
     * 获取可接受的任务列表
     */
    public List<Quest> getAvailableQuests(int playerLevel) {
        List<Quest> available = new ArrayList<>();
        for (Quest quest : allQuests.values()) {
            if (quest.canAccept(playerLevel, completedQuestIds)) {
                available.add(quest);
            }
        }
        return available;
    }
    
    // Getters
    
    public List<Quest> getActiveQuests() {
        return activeQuests;
    }
    
    public List<String> getCompletedQuestIds() {
        return completedQuestIds;
    }
}
