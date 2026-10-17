package com.potatoadventure.service;

import com.potatoadventure.model.Enemy;
import com.potatoadventure.model.Player;

import java.util.Random;

/**
 * 战斗服务 —— 最小回合交接（M1 范围）。
 *
 * <p><b>缺陷 1（敌人每帧攻击，约 60 次/秒）的成因与修法：</b>旧实现
 * {@code GameEngine.updateBattle} 每帧无条件调用 {@code battleService.enemyAttack(...)}，
 * 战斗服务里也没有任何“轮到谁”的概念，于是敌人跟着渲染节奏每秒砍 60 次。现在战斗服务持有
 * {@link Turn} 行动权：玩家行动成功才把行动权交给敌人，{@link #enemyAttack} 只在
 * {@code turn == ENEMY} 时生效，敌人行动完再把行动权交还玩家。引擎仍然可以每帧无脑调用
 * {@code enemyAttack}，但它只会打一次——<b>这是机制上的修正，不是冷却补丁</b>。
 *
 * <p><b>缺陷 2（无法逃跑）</b>由 {@link #tryEscape()} 处理：成功返回
 * {@link EscapeResult#SUCCESS}，由引擎结束战斗并把玩家挪到安全格（见
 * {@link com.potatoadventure.core.RetreatPlanner}）；失败则把行动权交给敌人，敌人只打一次，
 * 而不是让玩家无限重试。
 *
 * <p><b>伤害只减一次防御</b>：{@link #calculateDamage} 内部完成 {@code 攻击 - 防御/2}，
 * {@link Player#takeDamage(int)} / {@link Enemy#takeDamage(int)} 只做纯粹扣血，避免旧版
 * “战斗服务先减一次防御、扣血时再减一次”的双重减免。
 *
 * <p>M2 会在此基础上扩展完整战斗状态机（演出、跳字、动画）；M1 只交付回合交接与上述缺陷修复。
 */
public class BattleService {

    /** 行动权归属。 */
    public enum Turn {
        PLAYER,
        ENEMY
    }

    /** 逃跑尝试结果。 */
    public enum EscapeResult {
        /** 当前不是玩家回合或不在战斗中，未尝试。 */
        BLOCKED,
        /** 逃跑成功。 */
        SUCCESS,
        /** 逃跑失败，行动权已交给敌人。 */
        FAILED
    }

    public static final double CRIT_CHANCE = 0.20;
    public static final double DODGE_CHANCE = 0.10;
    public static final double ESCAPE_CHANCE = 0.50;

    public static final int SKILL_FIREBALL = 1;
    public static final int SKILL_HEAL = 2;
    public static final int SKILL_FREEZE = 3;

    public static final int COST_FIREBALL = 10;
    public static final int COST_HEAL = 15;
    public static final int COST_FREEZE = 12;

    private final Random random;

    private Enemy currentEnemy;
    private Turn turn;
    private boolean inBattle;

    public BattleService() {
        this(new Random());
    }

    /**
     * 可注入随机源：单元测试用它构造确定性序列（暴击/闪避/逃跑判定都可复现）。
     */
    public BattleService(Random random) {
        this.random = random;
        this.turn = Turn.PLAYER;
        this.inBattle = false;
    }

    /**
     * 开始战斗。玩家先手。
     */
    public void startBattle(Player player, Enemy enemy) {
        this.currentEnemy = enemy;
        this.inBattle = true;
        this.turn = Turn.PLAYER;
        System.out.println("=== 战斗开始！===");
        System.out.println("遭遇了 " + enemy.getName() + "！");
        System.out.println("当前行动方: 玩家");
    }

    /**
     * 结束战斗。
     */
    public void endBattle() {
        if (!inBattle) {
            return;
        }
        this.currentEnemy = null;
        this.inBattle = false;
        System.out.println("=== 战斗结束 ===");
    }

    /**
     * 玩家普通攻击。
     *
     * @return 是否消耗了本次行动（消耗则行动权已经交给敌人）
     */
    public boolean playerAttack(Player player, Enemy enemy) {
        if (!canPlayerAct(enemy)) {
            return false;
        }
        int damage = rollDamage("普通攻击", player.getAttack(), enemy.getDefense());
        System.out.println(player.getName() + " 对 " + enemy.getName() + " 造成了 " + damage + " 点伤害");
        enemy.takeDamage(damage);
        handOverToEnemy();
        return true;
    }

    /**
     * 玩家使用技能。
     *
     * <p>魔法不足或技能号非法时返回 {@code false} 且<b>不</b>消耗行动权，玩家可以继续选别的指令。
     *
     * @param skillId 1=火球术(10MP, 1.5 倍伤害) 2=治疗术(15MP, 回复 30% 上限) 3=冰冻术(12MP, 普通伤害)
     */
    public boolean playerSkill(Player player, Enemy enemy, int skillId) {
        if (!canPlayerAct(enemy)) {
            return false;
        }

        switch (skillId) {
            case SKILL_FIREBALL: {
                if (!player.consumeMp(COST_FIREBALL)) {
                    System.out.println("魔法不足！火球术需要 " + COST_FIREBALL + " MP");
                    return false;
                }
                int damage = rollDamage("火球术", (int) (player.getAttack() * 1.5), enemy.getDefense());
                enemy.takeDamage(damage);
                System.out.println(player.getName() + " 使用火球术！造成 " + damage + " 点伤害");
                break;
            }

            case SKILL_HEAL: {
                if (!player.consumeMp(COST_HEAL)) {
                    System.out.println("魔法不足！治疗术需要 " + COST_HEAL + " MP");
                    return false;
                }
                int healAmount = (int) (player.getMaxHp() * 0.3);
                player.heal(healAmount);
                System.out.println(player.getName() + " 使用治疗术！恢复 " + healAmount + " 点生命值");
                break;
            }

            case SKILL_FREEZE: {
                if (!player.consumeMp(COST_FREEZE)) {
                    System.out.println("魔法不足！冰冻术需要 " + COST_FREEZE + " MP");
                    return false;
                }
                int damage = rollDamage("冰冻术", player.getAttack(), enemy.getDefense());
                enemy.takeDamage(damage);
                System.out.println(player.getName() + " 使用冰冻术！造成 " + damage + " 点伤害");
                break;
            }

            default:
                System.out.println("未知技能: " + skillId);
                return false;
        }

        handOverToEnemy();
        return true;
    }

    /**
     * 大地图上的近身攻击（不在回合制战斗内）。行为与旧版一致：伤害照常结算，击败后由
     * {@link #grantDefeatReward} 幂等地发放奖励。
     *
     * @return 是否真的出手
     */
    public boolean overworldAttack(Player player, Enemy enemy) {
        if (enemy == null || !enemy.isAlive()) {
            return false;
        }
        int damage = rollDamage("普通攻击", player.getAttack(), enemy.getDefense());
        System.out.println(player.getName() + " 对 " + enemy.getName() + " 造成了 " + damage + " 点伤害");
        enemy.takeDamage(damage);
        grantDefeatReward(player, enemy);
        return true;
    }

    /**
     * 结算击败奖励 —— 全工程唯一的经验发放点，并且因为
     * {@link Enemy#claimDefeatReward()} 是幂等的，同一敌人无论从大地图还是战斗内被击败，
     * 都只会发放一次（缺陷 3）。
     *
     * @return 本次是否真的发放了经验
     */
    public boolean grantDefeatReward(Player player, Enemy enemy) {
        if (enemy == null || enemy.isAlive() || player == null) {
            return false;
        }
        if (!enemy.claimDefeatReward()) {
            System.out.println("（" + enemy.getName() + " 的击败奖励已结算过，忽略重复结算）");
            return false;
        }
        player.gainExp(enemy.getExpReward());
        return true;
    }

    /**
     * 尝试逃跑。
     *
     * <p>成功时<b>不</b>结束战斗也不移动玩家——引擎拿到 {@link EscapeResult#SUCCESS} 后负责
     * {@link #endBattle()} 与落点（避免“逃跑成功但原地重触发”）。
     */
    public EscapeResult tryEscape() {
        if (!inBattle || turn != Turn.PLAYER) {
            return EscapeResult.BLOCKED;
        }
        if (random.nextDouble() < ESCAPE_CHANCE) {
            System.out.println("逃跑成功！");
            return EscapeResult.SUCCESS;
        }
        System.out.println("逃跑失败！");
        turn = Turn.ENEMY;
        return EscapeResult.FAILED;
    }

    /**
     * 敌人攻击。只有行动权在敌人手里时才真正生效——每帧调用也只会打一次。
     *
     * @return 本次调用是否真的进行了一次攻击结算
     */
    public boolean enemyAttack(Enemy enemy, Player player) {
        if (!inBattle || turn != Turn.ENEMY) {
            return false;
        }
        if (enemy == null || !enemy.isAlive() || !player.isAlive()) {
            turn = Turn.PLAYER;
            return false;
        }

        if (random.nextDouble() < DODGE_CHANCE) {
            System.out.println(player.getName() + " 闪避了攻击！");
            turn = Turn.PLAYER;
            return true;
        }

        int damage = calculateDamage(enemy.getAttack(), player.getDefense());
        System.out.println(enemy.getName() + " 对 " + player.getName() + " 造成了 " + damage + " 点伤害");
        player.takeDamage(damage);
        turn = Turn.PLAYER;
        return true;
    }

    private boolean canPlayerAct(Enemy enemy) {
        return inBattle && turn == Turn.PLAYER && enemy != null && enemy.isAlive();
    }

    private void handOverToEnemy() {
        turn = Turn.ENEMY;
    }

    private int rollDamage(String label, int attack, int defense) {
        int damage = calculateDamage(attack, defense);
        if (random.nextDouble() < CRIT_CHANCE) {
            damage *= 2;
            System.out.println("暴击！(" + label + ")");
        }
        return damage;
    }

    /**
     * 伤害公式：攻击力 − 防御力/2，最少 1 点。整个工程里只有这一处做减防。
     */
    public static int calculateDamage(int attack, int defense) {
        return Math.max(1, attack - defense / 2);
    }

    public Enemy getCurrentEnemy() {
        return currentEnemy;
    }

    public boolean isInBattle() {
        return inBattle;
    }

    public Turn getTurn() {
        return turn;
    }

    public boolean isPlayerTurn() {
        return inBattle && turn == Turn.PLAYER;
    }
}
