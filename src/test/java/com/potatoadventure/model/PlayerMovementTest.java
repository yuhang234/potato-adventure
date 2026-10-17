package com.potatoadventure.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 移动测试：按步推进、短按完整走完一步、撞墙不移动、时间与帧率无关。
 */
class PlayerMovementTest {

    private static final double FRAME = 1.0 / 60.0;
    private static final int W = 20;
    private static final int H = 20;

    private static GameMap openMap() {
        return GameMap.fromTiles(W, H, 1L, new int[W][H]);
    }

    @Test
    void stepTakesConfiguredTimeAndLandsOnNextTile() {
        GameMap map = openMap();
        Player player = new Player(5, 5);

        assertTrue(player.updateMovement(FRAME, map, 1, 0), "有方向输入时应起步");
        assertTrue(player.isStepping());
        assertEquals(6, player.getX(), "逻辑格在起步时即更新");
        assertTrue(player.getVisualPixelX() < 6 * GameMap.TILE_SIZE, "视觉位置仍在旧格，说明在做插值");

        // 之后不再给方向输入：正在进行的这一步仍必须走完，而且不能自动接上第二步
        int frames = 1;
        while (player.isStepping() && frames < 60) {
            assertFalse(player.updateMovement(FRAME, map, 0, 0), "没有方向输入时不得自行起步");
            frames++;
        }
        assertFalse(player.isStepping(), "一步必须在有限帧内结束");
        assertEquals(6, player.getX());
        assertEquals(5, player.getY(), "不应出现横向以外的位移");
        assertEquals(6 * GameMap.TILE_SIZE, player.getVisualPixelX(), 0.001, "一步结束后视觉位置必须对齐格子");
        assertTrue(frames >= 6 && frames <= 8,
                "一步应耗时 ≈0.115 秒（60 FPS 下约 7 帧），实际 " + frames + " 帧");
    }

    @Test
    void heldDirectionStartsTheNextStepWithoutGap() {
        GameMap map = openMap();
        Player player = new Player(5, 5);

        // 持续按住方向键：一步走完必须立刻接下一步，中间不能停顿
        int previousX = player.getX();
        for (int i = 0; i < 60; i++) {
            player.updateMovement(FRAME, map, 1, 0);
            assertTrue(player.getX() >= previousX, "持续按住时逻辑格不得后退");
            previousX = player.getX();
        }
        assertTrue(player.getX() >= 12,
                "按住 1 秒应至少走 8 格（0.115 秒/格），实际到 " + player.getX());
    }

    @Test
    void shortTapStillCompletesTheStep() {
        GameMap map = openMap();
        Player player = new Player(5, 5);
        // 一次短按：只在这一帧给方向，之后方向为 0（相当于按下即放开）
        assertTrue(player.updateMovement(FRAME, map, 0, -1));
        double elapsed = FRAME;
        while (player.isStepping() && elapsed < 1.0) {
            player.updateMovement(FRAME, map, 0, 0);
            elapsed += FRAME;
        }
        assertEquals(4, player.getY(), "短按必须完整走完一格，不能半路停下");
        assertEquals(5, player.getX());
    }

    @Test
    void movementSpeedIsFrameRateIndependent() {
        GameMap map = openMap();
        Player fast = new Player(5, 5);
        Player slow = new Player(5, 5);

        for (int i = 0; i < 60; i++) {
            fast.updateMovement(FRAME, map, 1, 0);
        }
        for (int i = 0; i < 20; i++) {
            slow.updateMovement(1.0 / 20.0, map, 1, 0);
        }

        // 1 秒后两者的逻辑位置差不超过 1 格
        assertTrue(Math.abs(fast.getX() - slow.getX()) <= 1,
                "不同帧率下 1 秒内的位移必须基本一致: " + fast.getX() + " vs " + slow.getX());
        assertTrue(fast.getX() >= 12, "1 秒约走 8.7 格，起点 5 应至少到 12，实际 " + fast.getX());
    }

    @Test
    void blockedStepDoesNotMove() {
        int[][] tiles = new int[W][H];
        tiles[6][5] = GameMap.TILE_WALL;
        GameMap map = GameMap.fromTiles(W, H, 1L, tiles);
        Player player = new Player(5, 5);

        assertFalse(player.updateMovement(FRAME, map, 1, 0), "撞墙不得起步");
        assertFalse(player.isStepping());
        assertEquals(5, player.getX());
        assertEquals(5 * GameMap.TILE_SIZE, player.getVisualPixelX(), 0.001, "撞墙时视觉位置不得漂移");
    }

    @Test
    void diagonalStepMovesBothAxes() {
        GameMap map = openMap();
        Player player = new Player(5, 5);
        assertTrue(player.updateMovement(FRAME, map, 1, -1));
        assertEquals(6, player.getX());
        assertEquals(4, player.getY());
    }

    @Test
    void zeroDirectionDoesNothing() {
        GameMap map = openMap();
        Player player = new Player(5, 5);
        assertFalse(player.updateMovement(FRAME, map, 0, 0));
        assertFalse(player.isStepping());
    }

    @Test
    void setPositionSnapsVisual() {
        GameMap map = openMap();
        Player player = new Player(5, 5);
        player.updateMovement(FRAME, map, 1, 0);
        player.setPosition(9, 9);
        assertFalse(player.isStepping());
        assertEquals(9 * GameMap.TILE_SIZE, player.getVisualPixelX(), 0.001);
        assertEquals(9 * GameMap.TILE_SIZE, player.getVisualPixelY(), 0.001);
    }
}
