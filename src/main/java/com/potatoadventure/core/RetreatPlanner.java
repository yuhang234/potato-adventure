package com.potatoadventure.core;

import com.potatoadventure.model.GameMap;

import java.util.ArrayList;
import java.util.List;

/**
 * 逃跑落点规划（纯函数，便于单元测试）。
 *
 * <p>缺陷 2「战斗中无法逃跑」的成因之一：即使逃跑成功，玩家仍然停在敌人所在格，下一帧
 * 立即重新触发战斗，表现为“逃不掉”。因此逃跑成功必须把玩家挪走。本类负责算出落到哪一格：
 *
 * <ol>
 *   <li>优先退回触发战斗前的格子（{@code fromTile}），只要它不是敌人格且可通行；</li>
 *   <li>否则在当前位置周围的 8 邻域里找最近的空格（可通行、不是敌人格）；</li>
 *   <li>再退一步在整个地图里找距离最近的空格（保证一定有结果）。</li>
 * </ol>
 */
public final class RetreatPlanner {

    private RetreatPlanner() {
    }

    /**
     * @param map       地图（只读）
     * @param curTileX  玩家当前所在格（也就是敌人格）
     * @param curTileY  玩家当前所在格
     * @param fromTileX 触发战斗前玩家所在的格子；没有记录时传与 cur 相同的值
     * @param fromTileY 触发战斗前玩家所在的格子
     * @param enemyX    敌人所在格
     * @param enemyY    敌人所在格
     * @return 长度 2 的数组 {x, y}，保证可通行且不是敌人格
     */
    public static int[] escapeDestination(GameMap map, int curTileX, int curTileY,
                                          int fromTileX, int fromTileY,
                                          int enemyX, int enemyY) {
        if (isFree(map, fromTileX, fromTileY, enemyX, enemyY)) {
            return new int[]{fromTileX, fromTileY};
        }

        // 8 邻域，按“正交优先、距离升序”的固定顺序扫描，结果稳定可复现。
        int[][] offsets = {
                {0, -1}, {-1, 0}, {1, 0}, {0, 1},
                {-1, -1}, {1, -1}, {-1, 1}, {1, 1}
        };
        for (int radius = 1; radius <= 6; radius++) {
            for (int[] off : offsets) {
                int nx = curTileX + off[0] * radius;
                int ny = curTileY + off[1] * radius;
                if (isFree(map, nx, ny, enemyX, enemyY)) {
                    return new int[]{nx, ny};
                }
            }
        }

        // 兜底：全图扫描取曼哈顿距离最近的空格（地图尺寸很小，代价可忽略）。
        int[] best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (int x = 1; x < map.getWidth() - 1; x++) {
            for (int y = 1; y < map.getHeight() - 1; y++) {
                if (!isFree(map, x, y, enemyX, enemyY)) {
                    continue;
                }
                int distance = Math.abs(x - curTileX) + Math.abs(y - curTileY);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = new int[]{x, y};
                }
            }
        }
        if (best != null) {
            return best;
        }
        // 极端情况（地图全是障碍）：原地不动，至少不会把玩家送出地图。
        return new int[]{curTileX, curTileY};
    }

    /**
     * 相邻的四个方向中第一个可通行的格子；没有则返回 {@code null}。
     */
    public static int[] firstFreeNeighbour(GameMap map, int x, int y) {
        int[][] offsets = {{0, -1}, {-1, 0}, {1, 0}, {0, 1}};
        List<int[]> free = new ArrayList<>(4);
        for (int[] off : offsets) {
            int nx = x + off[0];
            int ny = y + off[1];
            if (!map.isCollision(nx, ny)) {
                free.add(new int[]{nx, ny});
            }
        }
        return free.isEmpty() ? null : free.get(0);
    }

    private static boolean isFree(GameMap map, int x, int y, int enemyX, int enemyY) {
        if (x == enemyX && y == enemyY) {
            return false;
        }
        return !map.isCollision(x, y);
    }
}
