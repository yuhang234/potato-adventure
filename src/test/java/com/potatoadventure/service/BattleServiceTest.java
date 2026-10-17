package com.potatoadventure.service;

import com.potatoadventure.model.Enemy;
import com.potatoadventure.model.Player;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 战斗测试：回合交接（缺陷 1）、逃跑（缺陷 2）、奖励只发一次（缺陷 3）、伤害只减一次防御。
 * 随机源全部注入，判定可复现。
 */
class BattleServiceTest {

    /** 让所有 random 判定返回同一个值：0.99 = 不暴击 / 不闪避 / 逃跑失败；0.0 = 全部命中。 */
    private static Random fixed(double value) {
        return new Random() {
            @Override
            public double nextDouble() {
                return value;
            }
        };
    }

    @Test
    void enemyDoesNotAttackWhilePlayerHoldsTheTurn() {
        Player player = new Player(5, 5);
        Enemy enemy = new Enemy(6, 5, "史莱姆");
        BattleService battle = new BattleService(fixed(0.99));
        battle.startBattle(player, enemy);

        int hp = player.getHp();
        for (int frame = 0; frame < 600; frame++) {
            // 旧实现每帧调用一次就是 600 次攻击；现在行动权在玩家手里，必须一次都不生效
            battle.enemyAttack(enemy, player);
        }
        assertEquals(hp, player.getHp(), "行动权在玩家手里时敌人不得出手（缺陷 1 回归测试）");
    }

    @Test
    void enemyAttacksExactlyOncePerPlayerAction() {
        Player player = new Player(5, 5);
        Enemy enemy = new Enemy(6, 5, "史莱姆");
        BattleService battle = new BattleService(fixed(0.99));
        battle.startBattle(player, enemy);

        int hp = player.getHp();
        int expectedDamage = BattleService.calculateDamage(enemy.getAttack(), player.getDefense());

        assertTrue(battle.playerAttack(player, enemy), "玩家回合的攻击应被接受");
        assertFalse(battle.isPlayerTurn(), "玩家行动后行动权交给敌人");

        assertTrue(battle.enemyAttack(enemy, player), "敌人回合应出手一次");
        assertEquals(hp - expectedDamage, player.getHp());
        assertTrue(battle.isPlayerTurn(), "敌人行动后行动权交还玩家");

        battle.enemyAttack(enemy, player);
        battle.enemyAttack(enemy, player);
        assertEquals(hp - expectedDamage, player.getHp(), "同一回合内敌人不能连续出手");
    }

    @Test
    void sufficientPlayerActionsDealExactDamageOnce() {
        Player player = new Player(5, 5);
        Enemy enemy = new Enemy(6, 5, "史莱姆");
        BattleService battle = new BattleService(fixed(0.99));
        battle.startBattle(player, enemy);

        int hp = enemy.getHp();
        battle.playerAttack(player, enemy);
        assertEquals(hp - BattleService.calculateDamage(player.getAttack(), enemy.getDefense()), enemy.getHp(),
                "防御只应被减免一次");
    }

    @Test
    void insufficientMpDoesNotConsumeTheTurn() {
        Player player = new Player(5, 5);
        player.setMp(3);
        Enemy enemy = new Enemy(6, 5, "史莱姆");
        BattleService battle = new BattleService(fixed(0.99));
        battle.startBattle(player, enemy);

        assertFalse(battle.playerSkill(player, enemy, BattleService.SKILL_FIREBALL));
        assertTrue(battle.isPlayerTurn(), "魔法不足不应消耗行动");
        assertEquals(3, player.getMp(), "魔法不足不应扣魔");
    }

    @Test
    void skill3FreezeIsUsableAndCosts12Mp() {
        Player player = new Player(5, 5);
        player.setMp(12);
        Enemy enemy = new Enemy(6, 5, "史莱姆");
        BattleService battle = new BattleService(fixed(0.99));
        battle.startBattle(player, enemy);

        assertTrue(battle.playerSkill(player, enemy, BattleService.SKILL_FREEZE), "冰冻术必须可用（旧版没有绑定按键）");
        assertEquals(0, player.getMp());
        assertFalse(battle.isPlayerTurn());
    }

    @Test
    void healSkillDoesNotDamageEnemyButHandsOverTurn() {
        Player player = new Player(5, 5);
        player.setHp(40);
        Enemy enemy = new Enemy(6, 5, "史莱姆");
        BattleService battle = new BattleService(fixed(0.99));
        battle.startBattle(player, enemy);

        int enemyHp = enemy.getHp();
        assertTrue(battle.playerSkill(player, enemy, BattleService.SKILL_HEAL));
        assertEquals(40 + (int) (player.getMaxHp() * 0.3), player.getHp());
        assertEquals(enemyHp, enemy.getHp());
        assertFalse(battle.isPlayerTurn());
    }

    @Test
    void successfulEscapeIsReportedWithoutEndingTheBattleItself() {
        Player player = new Player(5, 5);
        Enemy enemy = new Enemy(6, 5, "史莱姆");
        BattleService battle = new BattleService(fixed(0.0));
        battle.startBattle(player, enemy);

        assertEquals(BattleService.EscapeResult.SUCCESS, battle.tryEscape());
        assertTrue(battle.isInBattle(), "结束战斗与移动玩家由引擎负责，战斗服务只给出结果");
        battle.endBattle();
        assertFalse(battle.isInBattle());
    }

    @Test
    void failedEscapeHandsTheTurnToTheEnemySoItAttacksOnlyOnce() {
        Player player = new Player(5, 5);
        Enemy enemy = new Enemy(6, 5, "史莱姆");
        BattleService battle = new BattleService(fixed(0.99));
        battle.startBattle(player, enemy);

        int hp = player.getHp();
        assertEquals(BattleService.EscapeResult.FAILED, battle.tryEscape());
        assertFalse(battle.isPlayerTurn(), "逃跑失败要把行动权交给敌人");
        for (int frame = 0; frame < 120; frame++) {
            battle.enemyAttack(enemy, player);
        }
        assertEquals(hp - BattleService.calculateDamage(enemy.getAttack(), player.getDefense()), player.getHp(),
                "逃跑失败只被还击一次");
    }

    @Test
    void escapeIsBlockedWhenItIsNotThePlayersTurn() {
        Player player = new Player(5, 5);
        Enemy enemy = new Enemy(6, 5, "史莱姆");
        BattleService battle = new BattleService(fixed(0.0));
        battle.startBattle(player, enemy);
        battle.playerAttack(player, enemy);
        assertEquals(BattleService.EscapeResult.BLOCKED, battle.tryEscape());
    }

    @Test
    void defeatRewardIsGrantedExactlyOnce() {
        Player player = new Player(5, 5);
        Enemy enemy = new Enemy(6, 5, "史莱姆");
        BattleService battle = new BattleService(fixed(0.99));

        enemy.setHp(0);
        assertTrue(battle.grantDefeatReward(player, enemy), "第一次应发放奖励");
        int exp = player.getExp();
        assertEquals(enemy.getExpReward(), exp);

        assertFalse(battle.grantDefeatReward(player, enemy), "重复结算必须被拒绝（缺陷 3 回归测试）");
        assertEquals(exp, player.getExp(), "经验不得翻倍");
    }

    @Test
    void overworldKillAndBattleKillCannotBothGrantReward() {
        Player player = new Player(5, 5);
        Enemy enemy = new Enemy(6, 5, "史莱姆");
        enemy.setHp(5);
        BattleService battle = new BattleService(fixed(0.0)); // 必定暴击，一击必杀

        assertTrue(battle.overworldAttack(player, enemy));
        assertFalse(enemy.isAlive());
        int exp = player.getExp();
        // 引擎里的战斗结算路径同样会调用 grantDefeatReward
        assertFalse(battle.grantDefeatReward(player, enemy));
        assertEquals(exp, player.getExp());
    }

    @Test
    void healingDoesNotOverheal() {
        Player player = new Player(5, 5);
        player.setHp(player.getMaxHp());
        player.heal(30);
        assertEquals(player.getMaxHp(), player.getHp());
    }

    @Test
    void damageMitigationIsAppliedAtMostOnce() {
        Enemy enemy = new Enemy(6, 5, "史莱姆");
        int raw = BattleService.calculateDamage(15, 10); // 15 - 10/2 = 10
        int hp = enemy.getHp();
        enemy.takeDamage(raw);
        assertEquals(hp - raw, enemy.getHp(), "Enemy.takeDamage 只做纯扣血，不再二次减防");
    }

    @Test
    void minimumDamageIsOne() {
        assertEquals(1, BattleService.calculateDamage(3, 999));
    }
}
