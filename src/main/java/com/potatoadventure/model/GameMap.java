package com.potatoadventure.model;

import com.potatoadventure.ui.Palette;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 游戏地图 —— 构造期一次性生成，此后<b>只读</b>。
 *
 * <p>M1 相对旧实现的三处结构性修正：
 *
 * <ol>
 *   <li><b>去随机化渲染路径（缺陷 5 草地闪烁）</b>：旧实现在 {@code renderTile} 里调用
 *       {@code random.nextInt(...)} 决定草叶位置，每帧重新抽签，于是草地整片闪烁。现在所有
 *       装饰细节都在构造期由种子确定地烘焙进 {@link #detail}，渲染只读取既定数值，
 *       运行期不再有任何随机数调用。</li>
 *   <li><b>可确定性复现</b>：同一 {@code seed} 必然生成完全相同的地图（{@link #tileHash()} 相同），
 *       存档只记录种子与地块表即可无损还原。</li>
 *   <li><b>视口裁剪</b>：{@link #render} 只绘制 {@link #visibleRange} 给出的可见地块窗口，
 *       而不是每帧遍历整张 60×45 地图。</li>
 * </ol>
 *
 * <p>地块数组与细节位图对外只暴露读取接口，没有 {@code setTile}，从 API 层面保证“生成一次、
 * 游玩期间只读”。
 */
public class GameMap {

    /** 单个地块边长（像素），同时是玩家一步的位移距离。 */
    public static final int TILE_SIZE = 32;

    public static final int TILE_GRASS = 0;
    public static final int TILE_WALL = 1;
    public static final int TILE_TREE = 2;
    public static final int TILE_WATER = 3;

    /** M1 大地图尺寸：60×45 格 = 1920×1440 像素（旧版 40×30 = 1280×960）。 */
    public static final int DEFAULT_WIDTH = 60;
    public static final int DEFAULT_HEIGHT = 45;

    /** 新游戏时玩家的固定出生格。 */
    public static final int SPAWN_TILE_X = 5;
    public static final int SPAWN_TILE_Y = 5;

    /** 敌人点位按地图比例取，保证不同尺寸下分布一致。 */
    private static final double[][] ENEMY_SPAWN_FRACTIONS = {
            {0.30, 0.22}, {0.57, 0.40}, {0.73, 0.67}
    };

    private final int width;
    private final int height;
    private final long seed;

    /** 地块类型，索引顺序 [x][y]。构造后不再写入。 */
    private final int[][] tiles;

    /** 每个地块的确定性装饰位（草叶/裂纹等），索引顺序 [x][y]。 */
    private final int[][] detail;

    /** 敌人点位，仅新生成的地图有意义；读档得到的地图由存档决定敌人位置。 */
    private final List<int[]> enemySpawns;

    /**
     * 新地图：使用给定种子生成 60×45 大地图。
     */
    public GameMap(int width, int height, long seed) {
        this(width, height, seed, null, null);
        generate();
    }

    /**
     * 默认 60×45 地图，种子取自 {@link System#nanoTime()}。
     */
    public GameMap() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, System.nanoTime());
    }

    /**
     * 读档用工厂：用存档中的尺寸、种子与地块表重建地图。装饰位从种子重算，
     * 因此外观与存档前完全一致。
     *
     * @param tiles 形如 {@code tiles[x][y]} 的地块表，尺寸必须与 width/height 匹配
     */
    public static GameMap fromTiles(int width, int height, long seed, int[][] tiles) {
        if (tiles == null || tiles.length != width || (width > 0 && tiles[0].length != height)) {
            throw new IllegalArgumentException("地块表尺寸与地图尺寸不匹配");
        }
        return new GameMap(width, height, seed, tiles, Collections.<int[]>emptyList());
    }

    private GameMap(int width, int height, long seed, int[][] sourceTiles, List<int[]> enemySpawns) {
        if (width <= 2 || height <= 2) {
            throw new IllegalArgumentException("地图尺寸过小: " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
        this.seed = seed;

        if (sourceTiles == null) {
            this.tiles = new int[width][height];
        } else {
            this.tiles = new int[width][height];
            for (int x = 0; x < width; x++) {
                System.arraycopy(sourceTiles[x], 0, this.tiles[x], 0, height);
            }
        }

        this.detail = new int[width][height];
        Random detailRng = new Random(seed ^ 0x9E3779B97F4A7C15L);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                this.detail[x][y] = detailRng.nextInt();
            }
        }

        this.enemySpawns = enemySpawns == null
                ? new ArrayList<int[]>(ENEMY_SPAWN_FRACTIONS.length)
                : new ArrayList<>(enemySpawns);
    }

    // ------------------------------------------------------------------ 生成

    /**
     * 确定性地图生成：同一 seed 必然得到同一张地图。
     */
    private void generate() {
        Random rng = new Random(seed);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = TILE_GRASS;
            }
        }

        // 边界石墙
        for (int x = 0; x < width; x++) {
            tiles[x][0] = TILE_WALL;
            tiles[x][height - 1] = TILE_WALL;
        }
        for (int y = 0; y < height; y++) {
            tiles[0][y] = TILE_WALL;
            tiles[width - 1][y] = TILE_WALL;
        }

        // 零散树木
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (rng.nextDouble() < 0.05) {
                    tiles[x][y] = TILE_TREE;
                }
            }
        }

        // 树丛（成簇，比纯随机点更像林地）
        int groves = Math.max(6, width * height / 220);
        for (int i = 0; i < groves; i++) {
            int cx = 2 + rng.nextInt(Math.max(1, width - 4));
            int cy = 2 + rng.nextInt(Math.max(1, height - 4));
            int radius = 1 + rng.nextInt(3);
            for (int x = Math.max(1, cx - radius); x <= Math.min(width - 2, cx + radius); x++) {
                for (int y = Math.max(1, cy - radius); y <= Math.min(height - 2, cy + radius); y++) {
                    if (rng.nextDouble() < 0.72) {
                        tiles[x][y] = TILE_TREE;
                    }
                }
            }
        }

        // 池塘
        int ponds = Math.max(3, width * height / 420);
        for (int i = 0; i < ponds; i++) {
            int cx = 3 + rng.nextInt(Math.max(1, width - 6));
            int cy = 3 + rng.nextInt(Math.max(1, height - 6));
            int rx = 2 + rng.nextInt(2);
            int ry = 1 + rng.nextInt(2);
            for (int x = Math.max(1, cx - rx); x <= Math.min(width - 2, cx + rx); x++) {
                for (int y = Math.max(1, cy - ry); y <= Math.min(height - 2, cy + ry); y++) {
                    if (rng.nextDouble() < 0.85) {
                        tiles[x][y] = TILE_WATER;
                    }
                }
            }
        }

        // 出生地周围清空
        clearRect(SPAWN_TILE_X - 3, SPAWN_TILE_Y - 3, SPAWN_TILE_X + 3, SPAWN_TILE_Y + 3);

        // 敌人点位：先按比例取点，障碍上就螺旋找最近空地，再保证可达
        for (double[] fraction : ENEMY_SPAWN_FRACTIONS) {
            int cx = (int) Math.round(fraction[0] * width);
            int cy = (int) Math.round(fraction[1] * height);
            int[] spot = nearestWalkable(cx, cy);
            tiles[spot[0]][spot[1]] = TILE_GRASS;
            enemySpawns.add(spot);
        }

        ensureReachable(enemySpawns);
    }

    private void clearRect(int x0, int y0, int x1, int y1) {
        for (int x = Math.max(1, x0); x <= Math.min(width - 2, x1); x++) {
            for (int y = Math.max(1, y0); y <= Math.min(height - 2, y1); y++) {
                tiles[x][y] = TILE_GRASS;
            }
        }
    }

    /**
     * 从 (cx, cy) 出发做螺旋搜索，返回最近的空地（草地），保证敌人不会卡在障碍里。
     */
    private int[] nearestWalkable(int cx, int cy) {
        cx = Math.min(width - 2, Math.max(1, cx));
        cy = Math.min(height - 2, Math.max(1, cy));
        for (int radius = 0; radius < Math.max(width, height); radius++) {
            for (int x = cx - radius; x <= cx + radius; x++) {
                for (int y = cy - radius; y <= cy + radius; y++) {
                    if (x < 1 || x > width - 2 || y < 1 || y > height - 2) {
                        continue;
                    }
                    if (Math.abs(x - cx) != radius && Math.abs(y - cy) != radius) {
                        continue;
                    }
                    if (tiles[x][y] == TILE_GRASS && !(x == SPAWN_TILE_X && y == SPAWN_TILE_Y)) {
                        return new int[]{x, y};
                    }
                }
            }
        }
        return new int[]{cx, cy};
    }

    /**
     * 保证所有目标点都能从出生格走到。不可达时沿 L 形开凿一条草地走廊（生成期一次性完成，
     * 运行期地图仍然只读）。
     */
    private void ensureReachable(List<int[]> targets) {
        boolean[][] reachable = floodFillFromSpawn();
        for (int[] target : targets) {
            if (reachable[target[0]][target[1]]) {
                continue;
            }
            int x = target[0];
            int y = target[1];
            while (x != SPAWN_TILE_X) {
                x += Integer.signum(SPAWN_TILE_X - x);
                if (isInterior(x, y)) {
                    tiles[x][y] = TILE_GRASS;
                }
            }
            while (y != SPAWN_TILE_Y) {
                y += Integer.signum(SPAWN_TILE_Y - y);
                if (isInterior(x, y)) {
                    tiles[x][y] = TILE_GRASS;
                }
            }
            reachable = floodFillFromSpawn();
        }
    }

    private boolean isInterior(int x, int y) {
        return x > 0 && x < width - 1 && y > 0 && y < height - 1;
    }

    /**
     * 以出生格为起点在地图上做广度优先搜索，返回可达标记表。可通行条件与
     * {@link #isCollision} 一致。
     */
    private boolean[][] floodFillFromSpawn() {
        boolean[][] reached = new boolean[width][height];
        int[] queueX = new int[width * height];
        int[] queueY = new int[width * height];
        int head = 0;
        int tail = 0;
        queueX[tail] = SPAWN_TILE_X;
        queueY[tail] = SPAWN_TILE_Y;
        tail++;
        reached[SPAWN_TILE_X][SPAWN_TILE_Y] = true;

        int[][] offsets = {{0, -1}, {-1, 0}, {1, 0}, {0, 1}};
        while (head < tail) {
            int cx = queueX[head];
            int cy = queueY[head];
            head++;
            for (int[] off : offsets) {
                int nx = cx + off[0];
                int ny = cy + off[1];
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
                    continue;
                }
                if (reached[nx][ny] || tiles[nx][ny] != TILE_GRASS) {
                    continue;
                }
                reached[nx][ny] = true;
                queueX[tail] = nx;
                queueY[tail] = ny;
                tail++;
            }
        }
        return reached;
    }

    // ------------------------------------------------------------------ 查询

    /**
     * 该格是否阻挡通行。越界按阻挡处理。
     */
    public boolean isCollision(int x, int y) {
        return getTile(x, y) != TILE_GRASS;
    }

    public int getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return TILE_WALL;
        }
        return tiles[x][y];
    }

    /** 装饰位，渲染期只读。 */
    public int getDetail(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return 0;
        }
        return detail[x][y];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getSeed() {
        return seed;
    }

    public int getPixelWidth() {
        return width * TILE_SIZE;
    }

    public int getPixelHeight() {
        return height * TILE_SIZE;
    }

    /** 敌人点位的只读副本。 */
    public List<int[]> getEnemySpawns() {
        List<int[]> copy = new ArrayList<>(enemySpawns.size());
        for (int[] spawn : enemySpawns) {
            copy.add(new int[]{spawn[0], spawn[1]});
        }
        return Collections.unmodifiableList(copy);
    }

    /** 地块表的防御性副本，供存档写入。 */
    public int[][] copyTiles() {
        int[][] copy = new int[width][height];
        for (int x = 0; x < width; x++) {
            System.arraycopy(tiles[x], 0, copy[x], 0, height);
        }
        return copy;
    }

    /** 出生格当前是否可通行（测试与自检用）。 */
    public boolean isSpawnWalkable() {
        return !isCollision(SPAWN_TILE_X, SPAWN_TILE_Y);
    }

    /**
     * 地图身份哈希：FNV-1a 64 位，覆盖尺寸、种子与全部地块。
     * 存档写入与读取时各打印一次，用于机器可校验的“读档后地图未被重新随机”证明。
     */
    public long tileHash() {
        final long prime = 0x100000001B3L;
        long hash = 0xCBF29CE484222325L;
        hash = mix(hash, prime, width);
        hash = mix(hash, prime, height);
        hash = mix(hash, prime, (int) (seed ^ (seed >>> 32)));
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                hash ^= ((long) x << 32) | (y & 0xFFFFFFFFL);
                hash *= prime;
                hash ^= tiles[x][y];
                hash *= prime;
            }
        }
        return hash;
    }

    private static long mix(long hash, long prime, int value) {
        hash ^= value;
        hash *= prime;
        return hash;
    }

    // ------------------------------------------------------------------ 视口裁剪

    /**
     * 计算需要绘制的可见地块窗口（闭区间），纯函数，不依赖画布。
     *
     * @param camX      视口左上角在世界中的像素 X（通常为 {@code camera.getRenderX()}）
     * @param camY      视口左上角在世界中的像素 Y
     * @param viewW     视口像素宽
     * @param viewH     视口像素高
     * @return {@code {x0, y0, x1, y1}}；当视口无像素时返回 {@code {0, 0, -1, -1}} 表示空
     */
    public static int[] visibleRange(int camX, int camY, int viewW, int viewH,
                                     int mapWidth, int mapHeight) {
        if (viewW <= 0 || viewH <= 0 || mapWidth <= 0 || mapHeight <= 0) {
            return new int[]{0, 0, -1, -1};
        }
        int x0 = clampTile(Math.floorDiv(camX, TILE_SIZE), mapWidth);
        int y0 = clampTile(Math.floorDiv(camY, TILE_SIZE), mapHeight);
        int x1 = clampTile(Math.floorDiv(camX + viewW - 1, TILE_SIZE), mapWidth);
        int y1 = clampTile(Math.floorDiv(camY + viewH - 1, TILE_SIZE), mapHeight);
        return new int[]{x0, y0, x1, y1};
    }

    private static int clampTile(int value, int limit) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, limit - 1);
    }

    // ------------------------------------------------------------------ 渲染

    /**
     * 绘制地图的可见部分。{@code camX}/{@code camY} 必须是像素对齐的整数偏移，
     * 否则像素画会被半像素采样拉糊。
     */
    public void render(GraphicsContext gc, int camX, int camY) {
        int viewW = (int) gc.getCanvas().getWidth();
        int viewH = (int) gc.getCanvas().getHeight();
        int[] range = visibleRange(camX, camY, viewW, viewH, width, height);
        renderRange(gc, camX, camY, range[0], range[1], range[2], range[3]);
    }

    /**
     * 渲染指定地块窗口。拆出来是为了让裁剪范围可被直接测试/复用。
     *
     * <p>绘制顺序：草地明暗底 → 草地纹理细节 → 障碍/水面覆盖。所有随机感都来自构造期烘焙的
     * {@link #detail}，渲染期没有任何 {@code Random}，逐帧完全一致（缺陷 5 的修正）。
     */
    public void renderRange(GraphicsContext gc, int camX, int camY, int x0, int y0, int x1, int y1) {
        if (x1 < x0 || y1 < y0) {
            return;
        }

        // 第 1 遍：草地明暗底（每格一色，由 detail 稳定推出）
        for (int shade = 0; shade < 3; shade++) {
            gc.setFill(grassShade(shade));
            for (int x = x0; x <= x1; x++) {
                for (int y = y0; y <= y1; y++) {
                    if (shadeOf(detail[x][y]) != shade) {
                        continue;
                    }
                    gc.fillRect(x * TILE_SIZE - camX, y * TILE_SIZE - camY, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // 第 2 遍：草地纹理（暗色土斑 + 亮色草叶），位置由构造期烘焙的 detail 决定
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                if (tiles[x][y] != TILE_GRASS) {
                    continue;
                }
                int px = x * TILE_SIZE - camX;
                int py = y * TILE_SIZE - camY;
                int d = detail[x][y];

                gc.setFill(Palette.GRASS_DARK);
                for (int i = 0; i < 2; i++) {
                    int bits = (d >>> (1 + i * 7)) & 0x7F;
                    gc.fillRect(px + 2 + bits % 28, py + 3 + (bits / 28) % 27, 2, 1);
                }

                gc.setFill(Palette.GRASS_BLADE);
                for (int i = 0; i < 3; i++) {
                    int bits = (d >>> (2 + i * 10)) & 0x3FF;
                    int bx = 3 + (bits % 26);
                    int by = 3 + ((bits / 26) % 24);
                    gc.fillRect(px + bx, py + by, 1, 3);
                }
            }
        }

        // 第 3 遍：障碍与水面覆盖
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                int tile = tiles[x][y];
                if (tile == TILE_GRASS) {
                    continue;
                }
                renderOverlayTile(gc, tile, x * TILE_SIZE - camX, y * TILE_SIZE - camY, detail[x][y]);
            }
        }
    }

    private static Color grassShade(int shade) {
        switch (shade) {
            case 0:
                return Palette.GRASS_DARK;
            case 2:
                return Palette.GRASS_LIGHT;
            default:
                return Palette.GRASS_BASE;
        }
    }

    /** 地块的草地明暗阶（0 暗 / 1 中 / 2 亮），由装饰位稳定推出。 */
    static int shadeOf(int detailBits) {
        return Math.floorMod(detailBits, 3);
    }

    /**
     * 覆盖地块的像素画：全部用整数矩形堆叠，不使用椭圆/圆弧，保证硬边缘（不糊边）。
     * 裂纹、砖缝错位、水波相位同样只读取构造期的 detail。
     */
    private void renderOverlayTile(GraphicsContext gc, int tile, int px, int py, int detailBits) {
        switch (tile) {
            case TILE_WALL: {
                // 石砖墙：底槽 + 交错砖缝 + 左上受光倒角 + 右下暗缝
                gc.setFill(Palette.STONE);
                gc.fillRect(px, py, TILE_SIZE, TILE_SIZE);

                gc.setFill(Palette.STONE_DARK);
                gc.fillRect(px, py + 10, TILE_SIZE, 2);
                gc.fillRect(px, py + 21, TILE_SIZE, 2);
                int offsetA = (detailBits >>> 3) & 7;
                gc.fillRect(px + 8 + offsetA, py, 2, 10);
                gc.fillRect(px + (offsetA + 14) % 26, py + 12, 2, 9);
                gc.fillRect(px + 8 + offsetA, py + 23, 2, 9);

                gc.setFill(Palette.STONE_LIGHT);
                gc.fillRect(px, py, TILE_SIZE, 2);
                gc.fillRect(px, py, 2, TILE_SIZE);
                gc.setFill(Palette.STONE_DARK);
                gc.fillRect(px, py + TILE_SIZE - 2, TILE_SIZE, 2);
                gc.fillRect(px + TILE_SIZE - 2, py, 2, TILE_SIZE);
                break;
            }

            case TILE_TREE: {
                // 松树：树干 + 三层塔状树冠（受光面在左上，底部压暗）
                gc.setFill(Palette.TRUNK);
                gc.fillRect(px + 13, py + 17, 7, 14);
                gc.setFill(Palette.TRUNK_DARK);
                gc.fillRect(px + 18, py + 17, 2, 14);

                gc.setFill(Palette.BUSH);
                gc.fillRect(px + 8, py + 2, 16, 6);
                gc.fillRect(px + 4, py + 7, 24, 7);
                gc.fillRect(px + 7, py + 13, 18, 6);

                gc.setFill(Palette.BUSH_LIGHT);
                gc.fillRect(px + 10, py + 3, 7, 3);
                gc.fillRect(px + 6, py + 8, 8, 3);
                gc.fillRect(px + 9, py + 14, 5, 2);

                Color bushDark = Palette.BUSH.interpolate(Palette.STONE_DARK, 0.45);
                gc.setFill(bushDark);
                gc.fillRect(px + 4, py + 14, 24, 1);
                gc.fillRect(px + 21, py + 8, 7, 2);
                gc.fillRect(px + 7, py + 18, 18, 2);
                break;
            }

            case TILE_WATER: {
                // 池塘：水体 + 梯级亮边 + 相位稳定的波光
                gc.setFill(Palette.WATER);
                gc.fillRect(px, py, TILE_SIZE, TILE_SIZE);

                Color deep = Palette.WATER.interpolate(Palette.STONE_DARK, 0.35);
                Color ripple = Palette.WATER.interpolate(Color.WHITE, 0.35);
                int phase = (detailBits >>> 4) & 7;

                gc.setFill(deep);
                gc.fillRect(px, py + TILE_SIZE - 3, TILE_SIZE, 3);

                gc.setFill(ripple);
                gc.fillRect(px + 3, py + 5 + phase, 11, 1);
                gc.fillRect(px + 18, py + 13 + (phase / 2), 8, 1);
                gc.fillRect(px + 6, py + 22 - (phase / 2), 13, 1);
                gc.fillRect(px + 24, py + 3 + phase, 4, 1);

                gc.setFill(Palette.WATER_LIGHT);
                gc.fillRect(px + 5, py + 5 + phase, 3, 1);
                gc.fillRect(px + 20, py + 13 + (phase / 2), 2, 1);
                break;
            }

            default:
                gc.setFill(Color.BLACK);
                gc.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                break;
        }
    }
}
