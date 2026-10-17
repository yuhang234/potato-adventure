package com.potatoadventure.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 摄像机测试：边界夹取、像素对齐、平滑收敛。
 */
class CameraTest {

    private static final int VIEW_W = 1280;
    private static final int VIEW_H = 720;
    private static final int WORLD_W = 1920;
    private static final int WORLD_H = 1440;

    private Camera camera() {
        return new Camera(VIEW_W, VIEW_H, WORLD_W, WORLD_H);
    }

    @Test
    void clampsAtTopLeft() {
        Camera camera = camera();
        camera.snapTo(16, 16);
        assertEquals(0, camera.getRenderX());
        assertEquals(0, camera.getRenderY());
    }

    @Test
    void clampsAtBottomRight() {
        Camera camera = camera();
        camera.snapTo(WORLD_W - 16, WORLD_H - 16);
        assertEquals(WORLD_W - VIEW_W, camera.getRenderX());
        assertEquals(WORLD_H - VIEW_H, camera.getRenderY());
    }

    @Test
    void neverLeavesTheWorldEvenAfterManyFollows() {
        Camera camera = camera();
        double[][] targets = {
                {-9999, -9999}, {9999, 9999}, {0, 0}, {WORLD_W, WORLD_H},
                {640, 360}, {-1, 20000}
        };
        for (double[] target : targets) {
            camera.follow(target[0], target[1]);
            for (int i = 0; i < 120; i++) {
                camera.update(1.0 / 60.0);
                assertTrue(camera.getX() >= 0 && camera.getX() <= WORLD_W - VIEW_W,
                        "镜头 X 越界: " + camera.getX());
                assertTrue(camera.getY() >= 0 && camera.getY() <= WORLD_H - VIEW_H,
                        "镜头 Y 越界: " + camera.getY());
            }
        }
    }

    @Test
    void smoothingConvergesAndStaysFrameRateIndependent() {
        Camera fast = camera();
        Camera slow = camera();
        fast.snapTo(0, 0);
        slow.snapTo(0, 0);
        for (int i = 0; i < 60; i++) {
            fast.follow(WORLD_W - 100, WORLD_H - 100);
            slow.follow(WORLD_W - 100, WORLD_H - 100);
        }
        // 1 秒后两者都必须已经贴到目标，且路径长度不同（大 dt 步数少）
        for (int i = 0; i < 60; i++) {
            fast.update(1.0 / 60.0);
        }
        for (int i = 0; i < 20; i++) {
            slow.update(1.0 / 20.0);
        }
        assertTrue(fast.isSettled(), "60 FPS 下 0.5 秒内应收敛");
        assertTrue(slow.isSettled(), "20 FPS 下同样时间应收敛");
        assertEquals(fast.getX(), slow.getX(), 1.0, "不同帧率收敛到同一位置");
    }

    @Test
    void viewportFollowsTheWindowAndReclamps() {
        Camera camera = camera();
        camera.snapTo(WORLD_W - 100, WORLD_H - 100);
        assertEquals(WORLD_W - VIEW_W, camera.getX(), 1e-9);
        assertEquals(WORLD_H - VIEW_H, camera.getY(), 1e-9);

        // 窗口变大 → 可滚动范围变小，当前偏移与目标都必须重新夹取（否则会露出黑边）
        camera.setViewport(1600, 1000);
        assertEquals(WORLD_W - 1600, camera.getX(), 1e-9, "视口变化后镜头必须重新夹取 X");
        assertEquals(WORLD_H - 1000, camera.getY(), 1e-9, "视口变化后镜头必须重新夹取 Y");
        assertEquals(320, camera.getRenderX());
        assertEquals(440, camera.getRenderY());

        // 之后再跟随仍不能越界
        camera.follow(WORLD_W, WORLD_H);
        for (int i = 0; i < 120; i++) {
            camera.update(1.0 / 60.0);
        }
        assertTrue(camera.getX() <= WORLD_W - 1600 && camera.getX() >= 0, "越界: " + camera.getX());
        assertTrue(camera.getY() <= WORLD_H - 1000 && camera.getY() >= 0, "越界: " + camera.getY());

        // 相同尺寸是空操作；非法尺寸报错
        double before = camera.getX();
        camera.setViewport(1600, 1000);
        assertEquals(before, camera.getX(), 1e-9);
        assertThrows(IllegalArgumentException.class, () -> camera.setViewport(0, 1000));
        assertThrows(IllegalArgumentException.class, () -> camera.setViewport(1600, -1));
    }

    @Test
    void renderOffsetsArePixelAligned() {
        Camera camera = camera();
        camera.snapTo(100.7, 200.3);
        camera.update(0.016);
        assertEquals((int) Math.round(camera.getX()), camera.getRenderX());
        assertEquals((int) Math.round(camera.getY()), camera.getRenderY());
    }

    @Test
    void zeroDeltaTimeDoesNotMoveTheCamera() {
        Camera camera = camera();
        camera.snapTo(500, 500);
        double x = camera.getX();
        camera.follow(0, 0);
        camera.update(0);
        assertEquals(x, camera.getX(), 1e-9);
    }

    @Test
    void worldSmallerThanViewportPinsToZero() {
        Camera camera = new Camera(VIEW_W, VIEW_H, 640, 360);
        camera.snapTo(100, 100);
        assertEquals(0, camera.getRenderX());
        assertEquals(0, camera.getRenderY());
        assertFalse(camera.getX() > 0);
    }
}
