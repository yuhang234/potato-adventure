package com.potatoadventure.core;

/**
 * 平滑滚屏摄像机。
 *
 * <p>职责：把“跟随目标的世界像素坐标”转成“视口左上角的世界像素偏移”，并把偏移限制在地图
 * 范围内（{@link #clamp()}），因此镜头贴住地图边缘时不会露出画布黑边。渲染只使用
 * {@link #getRenderX()} / {@link #getRenderY()} 的<b>整数</b>偏移，保证像素画不会被半像素
 * 采样拉糊。
 *
 * <p>平滑采用指数趋近（帧率无关）：{@code x += (target - x) * (1 - exp(-dt / TAU))}。
 * 该式对任意 dt 都收敛，且不会在低帧率下过冲。
 */
public final class Camera {

    /** 指数趋近平滑时间常数（秒）。越小越“跟手”，越大越“飘”。 */
    public static final double TAU = 0.12;

    private final int worldWidth;
    private final int worldHeight;

    /** 视口尺寸（像素）。会随真实窗口尺寸变化，见 {@link #setViewport}。 */
    private int viewWidth;
    private int viewHeight;

    /** 视口左上角在世界坐标系中的位置（浮点，避免累积取整误差）。 */
    private double x;
    private double y;

    /** 本帧期望的视口左上角位置。 */
    private double targetX;
    private double targetY;

    public Camera(int viewWidth, int viewHeight, int worldWidth, int worldHeight) {
        if (viewWidth <= 0 || viewHeight <= 0) {
            throw new IllegalArgumentException("视口尺寸必须为正: " + viewWidth + "x" + viewHeight);
        }
        if (worldWidth <= 0 || worldHeight <= 0) {
            throw new IllegalArgumentException("世界尺寸必须为正: " + worldWidth + "x" + worldHeight);
        }
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    /**
     * 让镜头对准目标（通常是玩家）中心点。目标为世界像素坐标。
     */
    public void follow(double targetCenterX, double targetCenterY) {
        this.targetX = clampX(targetCenterX - viewWidth / 2.0);
        this.targetY = clampY(targetCenterY - viewHeight / 2.0);
    }

    /**
     * 立刻把镜头贴到目标上，不做平滑。用于读档/传送等需要硬切的场合。
     */
    public void snapTo(double targetCenterX, double targetCenterY) {
        follow(targetCenterX, targetCenterY);
        this.x = targetX;
        this.y = targetY;
    }

    /**
     * 推进平滑。{@code dt} 为秒。
     */
    public void update(double dt) {
        if (dt <= 0) {
            return;
        }
        double k = 1.0 - Math.exp(-dt / TAU);
        x += (targetX - x) * k;
        y += (targetY - y) * k;
        x = clampX(x);
        y = clampY(y);
    }

    /**
     * 是否已经贴到目标位置（用于测试与调试）。
     */
    public boolean isSettled() {
        return Math.abs(x - targetX) < 0.5 && Math.abs(y - targetY) < 0.5;
    }

    /** 供渲染使用的像素对齐（取整）横向偏移。 */
    public int getRenderX() {
        return (int) Math.round(x);
    }

    /** 供渲染使用的像素对齐（取整）纵向偏移。 */
    public int getRenderY() {
        return (int) Math.round(y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getViewWidth() {
        return viewWidth;
    }

    public int getViewHeight() {
        return viewHeight;
    }

    public int getWorldWidth() {
        return worldWidth;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    // ------------------------------------------------------------------ 视口

    /**
     * 更新视口尺寸（真实窗口变化时调用，例如窗口被窗口管理器缩到比 1280×720 更小）。
     *
     * <p>视口变了，可滚动的范围就变了，因此这里必须重新夹取当前与目标偏移，否则镜头会停在
     * 一个“已经越界”的位置上，画出黑边。地图比视口大时范围是 {@code [0, world-view]}，
     * 地图小于视口时固定 0。
     */
    public void setViewport(int newViewWidth, int newViewHeight) {
        if (newViewWidth <= 0 || newViewHeight <= 0) {
            throw new IllegalArgumentException("视口尺寸必须为正: " + newViewWidth + "x" + newViewHeight);
        }
        if (newViewWidth == viewWidth && newViewHeight == viewHeight) {
            return;
        }
        this.viewWidth = newViewWidth;
        this.viewHeight = newViewHeight;
        clamp();
    }

    /** 世界比视口大时限制在 [0, world-view]，否则固定 0（不会出现黑边以外的抖动）。 */
    private double clampX(double v) {
        double max = Math.max(0, worldWidth - viewWidth);
        return Math.min(max, Math.max(0, v));
    }

    private double clampY(double v) {
        double max = Math.max(0, worldHeight - viewHeight);
        return Math.min(max, Math.max(0, v));
    }

    /** 把当前偏移夹回合法范围（构造函数传入的世界尺寸变化后调用）。 */
    public void clamp() {
        x = clampX(x);
        y = clampY(y);
        targetX = clampX(targetX);
        targetY = clampY(targetY);
    }
}
