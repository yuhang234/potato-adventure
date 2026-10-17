package com.potatoadventure.ui;

import com.potatoadventure.model.Enemy;
import com.potatoadventure.model.Player;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.function.IntConsumer;

/**
 * 屏幕 4：战术回合制对决（对应原型 {@code #screen-battle}）。
 *
 * <p>几何取自 {@code css/style.css}：
 *
 * <ul>
 *   <li>战斗舞台 {@code 1280×470}，背景 {@code bg_mist_forest.png} 铺满、贴底对齐；</li>
 *   <li>行动队列 {@code top:15; left:340; 600×46}，3px 金边，头像格 {@code 34×34}、
 *       图标 {@code 28×28}，行动中的一方金边高亮；</li>
 *   <li>我方 {@code left:220; bottom:80}、敌方 {@code right:220; bottom:80}，
 *       状态条 {@code 150} 宽、血条 {@code 10} 高，立绘 {@code 100×100}，脚下椭圆阴影
 *       {@code 80×16}；</li>
 *   <li>下部控制台 {@code 1280×250}（石板底、顶边 4px 深色、内边距 {@code 16 24}、间距 20）：
 *       左侧 {@code 340×215} 四宫格指令卡、中间 {@code 480×215} 技能子面板、
 *       右侧羊皮纸战斗日志（剩余宽度）。</li>
 * </ul>
 *
 * <p>指令编号沿用既有战斗键位：{@code 0=普通攻击, 1=火球术, 2=治疗术, 3=冰冻术, 4=逃跑}。
 */
public final class BattleScreen {

    private static final double ARENA_H = 470;
    private static final double DECK_H = 250;

    /** 指令常量（与 {@link com.potatoadventure.service.BattleService} 的技能号对齐）。 */
    public static final int CMD_ATTACK = 0;
    public static final int CMD_ESCAPE = 4;

    private record Command(int id, String name, String hotkey, String cost, String desc) {
    }

    private static final Command[] SKILLS = {
            new Command(1, "火球术", "[2]", "10 MP", "1.5× 攻击力火焰伤害"),
            new Command(2, "治疗术", "[3]", "15 MP", "恢复 30% 生命上限"),
            new Command(3, "冰冻术", "[4]", "12 MP", "普通伤害，寒气凝结"),
            new Command(CMD_ESCAPE, "逃跑", "[ESC]", "—", "50% 成功率，失败被还击")
    };

    private BattleScreen() {
    }

    public static void render(GraphicsContext gc, Player player, Enemy enemy, boolean playerTurn,
                              List<String> log, IntConsumer onCommand) {
        drawArena(gc, player, enemy, playerTurn);
        drawConsole(gc, player, playerTurn, log, onCommand);
    }

    // ---------------------------------------------------------------- 舞台

    private static void drawArena(GraphicsContext gc, Player player, Enemy enemy, boolean playerTurn) {
        Image background = Assets.background("bg_mist_forest.png");
        if (background != null) {
            gc.drawImage(background, 0, 0, background.getWidth(), background.getHeight(),
                    0, 0, Assets.DESIGN_WIDTH, ARENA_H);
        } else {
            UiKit.fill(gc, Color.web("#1E272C"), 0, 0, Assets.DESIGN_WIDTH, ARENA_H);
        }

        drawTurnGauge(gc, player, enemy, playerTurn);

        // 我方（left:220; bottom:80）
        double heroCx = 220 + 80;
        double heroBottom = ARENA_H - 80;
        drawFighterStatus(gc, heroCx, heroBottom - 100 - 60,
                player.getName() + " Lv." + player.getLevel(),
                player.getHp() + "/" + player.getMaxHp(),
                player.getMaxHp() <= 0 ? 0 : (double) player.getHp() / player.getMaxHp());
        Image hero = Assets.sprite("hero_" + player.getHeroClass() + ".png");
        Sprites.battleSprite(gc, hero, heroCx, heroBottom - 50 - 8, 100);
        shadowEllipse(gc, heroCx, heroBottom - 14, 80, 16);

        // 敌方（right:220; bottom:80）
        if (enemy != null) {
            double enemyCx = Assets.DESIGN_WIDTH - 220 - 90;
            double enemyBottom = ARENA_H - 80;
            drawFighterStatus(gc, enemyCx, enemyBottom - 100 - 60,
                    enemy.getName() + " Lv." + enemy.getLevel(),
                    enemy.getHp() + "/" + enemy.getMaxHp(),
                    enemy.getMaxHp() <= 0 ? 0 : (double) enemy.getHp() / enemy.getMaxHp());
            Image sprite = enemySprite(enemy);
            Sprites.battleSprite(gc, sprite, enemyCx, enemyBottom - 50 - 8, 100);
            shadowEllipse(gc, enemyCx, enemyBottom - 14, 80, 16);
        }
    }

    private static Image enemySprite(Enemy enemy) {
        return switch (enemy.getType()) {
            case GOBLIN -> Assets.sprite("enemy_goblin.png");
            case WOLF -> Assets.sprite("enemy_wolf.png");
            default -> Assets.sprite("enemy_slime.png");
        };
    }

    /** 脚下椭圆阴影（{@code .fighter-shadow}：80×16、rgba(0,0,0,0.5)）。 */
    private static void shadowEllipse(GraphicsContext gc, double centerX, double centerY,
                                      double w, double h) {
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillOval(centerX - w / 2, centerY - h / 2, w, h);
    }

    private static void drawTurnGauge(GraphicsContext gc, Player player, Enemy enemy, boolean playerTurn) {
        double x = 340;
        double y = 15;
        double w = 600;
        double h = 46;

        UiKit.fill(gc, Color.rgb(18, 24, 32, 0.9), x, y, w, h);
        UiKit.stroke(gc, Color.web("#D6A848"), 3, x, y, w, h);

        UiKit.text(gc, "行动队列 »", x + 12, UiKit.baseline(y, h, 12), Color.web("#FFD264"), 12);

        double avatarX = x + 100;
        drawAvatar(gc, "hero_" + player.getHeroClass() + ".png", avatarX, y + 6, playerTurn);
        drawAvatar(gc, enemy == null ? "enemy_slime.png" : switch (enemy.getType()) {
            case GOBLIN -> "enemy_goblin.png";
            case WOLF -> "enemy_wolf.png";
            default -> "enemy_slime.png";
        }, avatarX + 44, y + 6, !playerTurn);

        String current = playerTurn ? player.getName() : enemy == null ? "—" : enemy.getName();
        UiKit.text(gc, "当前行动：" + current, avatarX + 96, UiKit.baseline(y, h, 11),
                Color.web("#FFD264"), 11);
    }

    private static void drawAvatar(GraphicsContext gc, String sprite, double x, double y, boolean active) {
        UiKit.fill(gc, Color.web("#0C1014"), x, y, 34, 34);
        UiKit.stroke(gc, active ? Color.web("#FFD264") : Color.web("#788796"), 2, x, y, 34, 34);
        Image image = Assets.sprite(sprite);
        if (image != null) {
            gc.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), x + 3, y + 3, 28, 28);
        }
    }

    /** 战斗单位状态条（{@code .fighter-status-bar}，150 宽、血条 10 高）。 */
    private static void drawFighterStatus(GraphicsContext gc, double centerX, double y,
                                          String name, String hpText, double ratio) {
        double w = 150;
        double x = centerX - w / 2;
        UiKit.fill(gc, Color.rgb(15, 20, 26, 0.9), x, y, w, 44);
        UiKit.stroke(gc, Color.web("#3C4B57"), 2, x, y, w, 44);

        UiKit.text(gc, name, x + 8, y + 17, Color.web("#FFD264"), 13);
        double nameW = name.length() * 13.0;
        UiKit.text(gc, hpText, x + w - 8 - hpText.length() * 13.0, y + 17, Color.web("#FF6B5B"), 13);
        if (nameW > w - 60) {
            // 名字过长时只保留血条，避免与血值重叠
            UiKit.fill(gc, Color.rgb(15, 20, 26, 0.9), x + 8, y + 2, w - 16, 18);
            UiKit.text(gc, UiKit.ellipsize(name, 5), x + 8, y + 17, Color.web("#FFD264"), 13);
            UiKit.text(gc, hpText, x + w - 8 - hpText.length() * 13.0, y + 17,
                    Color.web("#FF6B5B"), 13);
        }

        UiKit.bar(gc, x + 8, y + 26, w - 16, 10, ratio,
                Color.web("#FF6B5B"), Color.web("#C0392B"), Color.web("#401010"));
    }

    // ---------------------------------------------------------------- 控制台

    private static void drawConsole(GraphicsContext gc, Player player, boolean playerTurn,
                                    List<String> log, IntConsumer onCommand) {
        double y = ARENA_H;
        Image slate = Assets.ui("stone_slate.png");
        if (slate != null) {
            UiKit.tiled(gc, slate, 0, y, Assets.DESIGN_WIDTH, DECK_H, slate.getWidth());
        } else {
            UiKit.fill(gc, Color.web("#2C3A47"), 0, y, Assets.DESIGN_WIDTH, DECK_H);
        }
        UiKit.fill(gc, Color.web("#141419"), 0, y, Assets.DESIGN_WIDTH, 4);
        UiKit.fill(gc, Color.web("#788796"), 0, y + 4, Assets.DESIGN_WIDTH, 2);

        drawCommandGrid(gc, 24, y + 16, playerTurn, onCommand);
        drawSubDeck(gc, 24 + 340 + 20, y + 16, player, playerTurn, onCommand);
        drawLog(gc, 24 + 340 + 20 + 480 + 20, y + 16, 372, log);
    }

    private static void drawCommandGrid(GraphicsContext gc, double x, double y, boolean playerTurn,
                                        IntConsumer onCommand) {
        String[][] cards = {
                {"⚔ 普通攻击", "[快捷键 Z / 1]", "0"},
                {"★ 战术技能", "[快捷键 X / 2]", "1"},
                {"◆ 背包道具", "[快捷键 C / 3] (M3)", "2"},
                {"▲ 防御 / 逃跑", "[快捷键 V / ESC]", "4"}
        };
        double w = (340 - 12) / 2.0;
        double h = (215 - 12) / 2.0;

        for (int i = 0; i < cards.length; i++) {
            final int command = Integer.parseInt(cards[i][2]);
            double cx = x + (i % 2) * (w + 12);
            double cy = y + (i / 2) * (h + 12);
            boolean enabled = playerTurn && command != 2;
            boolean hover = enabled && UiKit.hot(cx, cy, w, h, () -> onCommand.accept(command));
            drawCommandCard(gc, cx, cy, w, h, cards[i][0], cards[i][1], enabled, hover);
        }
    }

    private static void drawCommandCard(GraphicsContext gc, double x, double y, double w, double h,
                                        String title, String hint, boolean enabled, boolean hover) {
        Color top = enabled ? (hover ? Color.web("#B47D4B") : Color.web("#96643C")) : Color.web("#3A2E26");
        Color bottom = enabled ? (hover ? Color.web("#7D4B2D") : Color.web("#5A321A")) : Color.web("#241C18");
        UiKit.gradientColumn(gc, x, y, w, h, top, bottom);
        UiKit.stroke(gc, hover ? Color.web("#FFD264") : Color.web("#14100C"), 3, x, y, w, h);
        UiKit.fill(gc, enabled ? Color.web("#C89664") : Color.web("#4A3A2E"), x + 3, y + 3, w - 6, 2);

        Color titleColor = enabled ? Color.web("#F5F5F5") : Color.web("#8A8078");
        UiKit.textCenter(gc, title, x + w / 2, y + h / 2 + 2, titleColor, 16);
        UiKit.textCenter(gc, hint, x + w / 2, y + h / 2 + 24,
                enabled ? Color.web("#FFD264") : Color.web("#8A8078"), 11);
    }

    private static void drawSubDeck(GraphicsContext gc, double x, double y, Player player,
                                    boolean playerTurn, IntConsumer onCommand) {
        UiKit.fill(gc, Color.rgb(16, 22, 30, 0.95), x, y, 480, 215);
        UiKit.stroke(gc, Color.web("#3C4B57"), 3, x, y, 480, 215);

        UiKit.text(gc, "战术技能（点击或按数字键）", x + 12, y + 20, Color.web("#FFD264"), 14);
        UiKit.fill(gc, Color.web("#3C4B57"), x + 12, y + 26, 456, 1);

        double itemW = (480 - 24 - 10) / 2.0;
        double itemH = (215 - 40 - 10) / 2.0;
        for (int i = 0; i < SKILLS.length; i++) {
            Command cmd = SKILLS[i];
            double cx = x + 12 + (i % 2) * (itemW + 10);
            double cy = y + 34 + (i / 2) * (itemH + 10);

            boolean affordable = cmd.id() == CMD_ESCAPE
                    || (cmd.id() == 1 && player.getMp() >= 10)
                    || (cmd.id() == 2 && player.getMp() >= 15)
                    || (cmd.id() == 3 && player.getMp() >= 12);
            boolean enabled = playerTurn && affordable;
            boolean hover = enabled && UiKit.hot(cx, cy, itemW, itemH,
                    () -> onCommand.accept(cmd.id()));

            UiKit.fill(gc, enabled && hover ? Color.web("#2A3848") : Color.web("#202A35"),
                    cx, cy, itemW, itemH);
            UiKit.stroke(gc, hover ? Color.web("#FFD264") : Color.web("#506478"), 2, cx, cy, itemW, itemH);

            Color nameColor = enabled ? Color.WHITE : Color.web("#8A94A0");
            UiKit.text(gc, cmd.name() + " " + cmd.hotkey(), cx + 8, cy + 18, nameColor, 13);
            UiKit.text(gc, cmd.cost(), cx + 8, cy + 34,
                    affordable ? Color.web("#5DADE2") : Color.web("#A05252"), 11);
            UiKit.text(gc, cmd.desc(), cx + 8, cy + 50, Color.web("#A0B0C0"), 11);
        }
    }

    private static void drawLog(GraphicsContext gc, double x, double y, double w, List<String> log) {
        Image parchment = Assets.ui("parchment.png");
        if (parchment != null) {
            UiKit.tiled(gc, parchment, x, y, w, 215, parchment.getWidth());
        } else {
            UiKit.fill(gc, Color.web("#E6D2A5"), x, y, w, 215);
        }
        UiKit.stroke(gc, Color.web("#371E12"), 3, x, y, w, 215);
        UiKit.fill(gc, Color.web("#F5E6BE"), x + 3, y + 3, w - 6, 2);

        UiKit.text(gc, "战斗实况", x + 14, y + 22, Color.web("#7A5828"), 13);
        UiKit.fill(gc, Color.web("#C3AA78"), x + 14, y + 28, w - 28, 1);

        int maxLines = 10;
        int start = Math.max(0, log.size() - maxLines);
        double lineY = y + 48;
        for (int i = start; i < log.size(); i++) {
            String message = UiKit.ellipsize(log.get(i), (int) ((w - 28) / 13));
            Color color = Color.web("#2B180D");
            if (log.get(i).startsWith("▶")) {
                color = Color.web("#8C251C");
            } else if (log.get(i).startsWith("◀")) {
                color = Color.web("#1A4D2E");
            } else if (log.get(i).startsWith("·")) {
                color = Color.web("#7A5828");
            }
            UiKit.text(gc, message, x + 14, lineY, color, 13, Color.web("#F5E6BE"), false);
            lineY += 17;
        }
    }
}
