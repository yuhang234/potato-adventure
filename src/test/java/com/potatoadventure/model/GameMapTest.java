package com.potatoadventure.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 地图测试：确定性、只读、点位置信、以及视口裁剪范围。
 */
class GameMapTest {

    private static final int W = GameMap.DEFAULT_WIDTH;
    private static final int H = GameMap.DEFAULT_HEIGHT;
    private static final long SEED = 20261017L;

    @Test
    void defaultMapIs60x45() {
        GameMap map = new GameMap(W, H, SEED);
        assertEquals(60, map.getWidth());
        assertEquals(45, map.getHeight());
        assertEquals(1920, map.getPixelWidth());
        assertEquals(1440, map.getPixelHeight());
    }

    @Test
    void sameSeedProducesIdenticalMap() {
        GameMap a = new GameMap(W, H, SEED);
        GameMap b = new GameMap(W, H, SEED);
        assertEquals(a.tileHash(), b.tileHash(), "同种子必须生成同一张地图");
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                assertEquals(a.getTile(x, y), b.getTile(x, y), "地块不一致: (" + x + "," + y + ")");
            }
        }
    }

    @Test
    void differentSeedProducesDifferentMap() {
        assertNotEquals(new GameMap(W, H, SEED).tileHash(), new GameMap(W, H, SEED + 1).tileHash());
    }

    @Test
    void tileHashCoversSizeAndSeed() {
        assertNotEquals(new GameMap(W, H, SEED).tileHash(), new GameMap(W + 1, H, SEED).tileHash());
    }

    /**
     * 缺陷 5（草地每帧闪烁）的回归测试：装饰细节必须与渲染次数无关。
     * 旧实现在渲染里调 {@code Random.nextInt}，连读两次都会不一样。
     */
    @Test
    void decorationIsStableAcrossReads() {
        GameMap map = new GameMap(W, H, SEED);
        int[][] first = new int[W][H];
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                first[x][y] = map.getDetail(x, y);
            }
        }
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                assertEquals(first[x][y], map.getDetail(x, y), "装饰位在两次读取之间发生变化: (" + x + "," + y + ")");
            }
        }
    }

    @Test
    void borderIsWall() {
        GameMap map = new GameMap(W, H, SEED);
        for (int x = 0; x < W; x++) {
            assertEquals(GameMap.TILE_WALL, map.getTile(x, 0));
            assertEquals(GameMap.TILE_WALL, map.getTile(x, H - 1));
        }
        for (int y = 0; y < H; y++) {
            assertEquals(GameMap.TILE_WALL, map.getTile(0, y));
            assertEquals(GameMap.TILE_WALL, map.getTile(W - 1, y));
        }
    }

    @Test
    void outOfBoundsCountsAsBlocked() {
        GameMap map = new GameMap(W, H, SEED);
        assertEquals(GameMap.TILE_WALL, map.getTile(-1, 5));
        assertEquals(GameMap.TILE_WALL, map.getTile(5, H + 3));
        assertTrue(map.isCollision(-1, -1));
        assertEquals(0, map.getDetail(-4, -4));
    }

    @Test
    void spawnAreaIsClear() {
        GameMap map = new GameMap(W, H, SEED);
        assertTrue(map.isSpawnWalkable());
        for (int x = GameMap.SPAWN_TILE_X - 1; x <= GameMap.SPAWN_TILE_X + 1; x++) {
            for (int y = GameMap.SPAWN_TILE_Y - 1; y <= GameMap.SPAWN_TILE_Y + 1; y++) {
                assertFalse(map.isCollision(x, y), "出生地 3x3 必须可通行: (" + x + "," + y + ")");
            }
        }
    }

    @Test
    void enemySpawnsAreWalkableAndReachable() {
        for (long seed : new long[]{1L, 2L, SEED, 987654321L, -42L}) {
            GameMap map = new GameMap(W, H, seed);
            List<int[]> spawns = map.getEnemySpawns();
            assertEquals(3, spawns.size(), "seed=" + seed + " 应有 3 个敌人点位");
            boolean[][] reachable = floodFill(map);
            for (int[] spawn : spawns) {
                assertFalse(map.isCollision(spawn[0], spawn[1]),
                        "seed=" + seed + " 敌人点位被障碍占据: " + spawn[0] + "," + spawn[1]);
                assertTrue(reachable[spawn[0]][spawn[1]],
                        "seed=" + seed + " 敌人点位不可达: " + spawn[0] + "," + spawn[1]);
            }
        }
    }

    @Test
    void copyTilesIsDefensive() {
        GameMap map = new GameMap(W, H, SEED);
        int[][] copy = map.copyTiles();
        assertEquals(map.tileHash(), GameMap.fromTiles(W, H, SEED, copy).tileHash());
        copy[1][1] = GameMap.TILE_WALL;
        assertEquals(GameMap.TILE_GRASS, map.getTile(1, 1), "copyTiles 必须是副本，改它不能影响地图");
    }

    @Test
    void fromTilesRoundTripsHash() {
        GameMap original = new GameMap(W, H, SEED);
        GameMap restored = GameMap.fromTiles(W, H, original.getSeed(), original.copyTiles());
        assertEquals(original.tileHash(), restored.tileHash());
        assertEquals(original.getSeed(), restored.getSeed());
    }

    @Test
    void visibleRangeAtTopLeft() {
        assertArrayEquals(new int[]{0, 0, 39, 22},
                GameMap.visibleRange(0, 0, 1280, 720, 60, 45));
    }

    @Test
    void visibleRangeAtBottomRightEdge() {
        // 摄像机贴到右下角：偏移 (1920-1280, 1440-720) = (640, 720)
        assertArrayEquals(new int[]{20, 22, 59, 44},
                GameMap.visibleRange(640, 720, 1280, 720, 60, 45));
    }

    @Test
    void visibleRangeClampsNegativeOffsets() {
        // 摄像机正常不会给出负偏移（Camera 已夹取到 0），但纯函数必须健壮：
        // 偏移 -500 表示画布左/上各露出 500px 空白，此刻地图真正可见的像素只到
        // 世界坐标 779（= -500 + 1280 - 1）与 219（= -500 + 720 - 1），
        // 分别落在第 24 列与第 6 行，因此右/下边界是 24/6 而不是 39/22。
        assertArrayEquals(new int[]{0, 0, 24, 6},
                GameMap.visibleRange(-500, -500, 1280, 720, 60, 45));
    }

    @Test
    void visibleRangeBeyondMapClampsToLastTile() {
        assertArrayEquals(new int[]{59, 44, 59, 44},
                GameMap.visibleRange(99999, 99999, 1280, 720, 60, 45));
    }

    @Test
    void visibleRangeIsEmptyForEmptyViewport() {
        int[] range = GameMap.visibleRange(0, 0, 0, 720, 60, 45);
        assertTrue(range[2] < range[0], "空视口不应产生地块");
    }

    @Test
    void visibleRangeNeverExceedsMap() {
        for (int camX = -64; camX <= 1920; camX += 37) {
            for (int camY = -64; camY <= 1440; camY += 41) {
                int[] r = GameMap.visibleRange(camX, camY, 1280, 720, 60, 45);
                assertTrue(r[0] >= 0 && r[1] >= 0 && r[2] < 60 && r[3] < 45, "范围越界: " + camX + "," + camY);
            }
        }
    }

    private static boolean[][] floodFill(GameMap map) {
        boolean[][] reached = new boolean[map.getWidth()][map.getHeight()];
        int[] qx = new int[map.getWidth() * map.getHeight()];
        int[] qy = new int[map.getWidth() * map.getHeight()];
        int head = 0;
        int tail = 0;
        qx[tail] = GameMap.SPAWN_TILE_X;
        qy[tail] = GameMap.SPAWN_TILE_Y;
        tail++;
        reached[GameMap.SPAWN_TILE_X][GameMap.SPAWN_TILE_Y] = true;
        int[][] offsets = {{0, -1}, {-1, 0}, {1, 0}, {0, 1}};
        while (head < tail) {
            int cx = qx[head];
            int cy = qy[head];
            head++;
            for (int[] off : offsets) {
                int nx = cx + off[0];
                int ny = cy + off[1];
                if (nx < 0 || nx >= map.getWidth() || ny < 0 || ny >= map.getHeight()) {
                    continue;
                }
                if (reached[nx][ny] || map.isCollision(nx, ny)) {
                    continue;
                }
                reached[nx][ny] = true;
                qx[tail] = nx;
                qy[tail] = ny;
                tail++;
            }
        }
        return reached;
    }
}
