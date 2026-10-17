package com.potatoadventure.core;

import com.potatoadventure.model.GameMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 逃跑落点测试 —— 覆盖缺陷 2「逃跑后原地重触发、表现为逃不掉」。
 */
class RetreatPlannerTest {

    private static final int W = 12;
    private static final int H = 12;

    private static GameMap openMap() {
        int[][] tiles = new int[W][H];
        return GameMap.fromTiles(W, H, 7L, tiles); // 全 0 = 全草地
    }

    @Test
    void prefersTheTileThePlayerCameFrom() {
        GameMap map = openMap();
        int[] dest = RetreatPlanner.escapeDestination(map, 5, 5, 5, 4, 5, 5);
        assertArrayEquals(new int[]{5, 4}, dest);
    }

    @Test
    void skipsThePreviousTileWhenItIsBlocked() {
        GameMap map = openMap();
        int[] dest = RetreatPlanner.escapeDestination(map, 5, 5, 5, 4, 5, 5);
        assertArrayEquals(new int[]{5, 4}, dest);

        // 把 (5,4) 变成树之后必须换一个空格
        int[][] tiles = new int[W][H];
        tiles[5][4] = GameMap.TILE_TREE;
        GameMap blocked = GameMap.fromTiles(W, H, 7L, tiles);
        int[] fallback = RetreatPlanner.escapeDestination(blocked, 5, 5, 5, 4, 5, 5);
        assertFalse(fallback[0] == 5 && fallback[1] == 4, "不得回到被阻挡的格子");
        assertFalse(blocked.isCollision(fallback[0], fallback[1]));
        assertFalse(fallback[0] == 5 && fallback[1] == 5, "落点也不能是敌人格");
    }

    @Test
    void neverReturnsTheEnemyTile() {
        GameMap map = openMap();
        // 没有历史位置时 from 会被传成敌人格
        int[] dest = RetreatPlanner.escapeDestination(map, 5, 5, 5, 5, 5, 5);
        assertFalse(dest[0] == 5 && dest[1] == 5, "落点不能是敌人格，否则立刻重新触发战斗");
        assertFalse(map.isCollision(dest[0], dest[1]));
    }

    @Test
    void destinationIsAlwaysWalkable() {
        int[][] tiles = new int[W][H];
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                tiles[x][y] = GameMap.TILE_WATER;
            }
        }
        tiles[5][5] = GameMap.TILE_GRASS;
        tiles[5][4] = GameMap.TILE_GRASS;
        tiles[4][5] = GameMap.TILE_GRASS;
        GameMap almostBlocked = GameMap.fromTiles(W, H, 3L, tiles);

        int[] dest = RetreatPlanner.escapeDestination(almostBlocked, 5, 5, 5, 5, 5, 5);
        assertFalse(almostBlocked.isCollision(dest[0], dest[1]));
    }

    @Test
    void firstFreeNeighbourFindsWalkableTile() {
        int[][] tiles = new int[W][H];
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                tiles[x][y] = GameMap.TILE_WALL;
            }
        }
        tiles[4][4] = GameMap.TILE_GRASS;
        tiles[3][3] = GameMap.TILE_WALL;
        tiles[3][4] = GameMap.TILE_GRASS;
        GameMap map = GameMap.fromTiles(W, H, 3L, tiles);
        int[] neighbour = RetreatPlanner.firstFreeNeighbour(map, 3, 3);
        assertTrue(neighbour != null && !map.isCollision(neighbour[0], neighbour[1]));
    }

    @Test
    void enclosedPlayerFallsBackToOwnTile() {
        int[][] tiles = new int[W][H];
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                tiles[x][y] = GameMap.TILE_WALL;
            }
        }
        GameMap crowded = GameMap.fromTiles(W, H, 3L, tiles);
        int[] dest = RetreatPlanner.escapeDestination(crowded, 5, 5, 5, 5, 5, 5);
        assertArrayEquals(new int[]{5, 5}, dest, "极端情况下原地不动，但不得抛异常");
    }
}
