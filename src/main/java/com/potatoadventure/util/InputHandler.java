package com.potatoadventure.util;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 输入处理器 —— 事件驱动的按键缓冲。
 *
 * <p><b>缺陷 6（快速连续按键被吞掉）的成因与修法：</b>原实现把“刚按下”记录在
 * {@code Map<KeyCode, Boolean> justPressed} 里，而 <em>释放</em> 回调会立刻写入
 * {@code false}。于是在两次渲染帧之间完成的“按下→释放”短按会被后到的释放事件抹掉，
 * 逻辑层永远看不到它。本实现改为<b>有序缓冲</b>：
 *
 * <ul>
 *   <li>{@link #onKeyPressed(KeyCode)} 在按键从未按下变为按下时，把该键追加进待处理队列；
 *       队列里的按键在渲染帧被 {@link #beginFrame()} 取走之前不会被任何事件删除，
 *       因此短按再也不可能丢失。</li>
 *   <li>{@link #beginFrame()} 把待处理队列整体搬进本帧事件表（保持按下的先后顺序），
 *       并重建本帧的 {@code justPressed} 集合。</li>
 *   <li>{@link #isJustPressed(KeyCode)} 查询本帧事件，<b>不消费</b>——同一帧内多处查询都看得到，
 *       避免“被无关的检查提前吃掉”。</li>
 *   <li>{@link #deferToNextFrame(KeyCode)} 用于战斗指令等只允许一帧一次的场合：把一个尚未
 *       处理完的按键退回队列，下一帧继续可见。</li>
 *   <li>自动重复（长按系统连发）通过 {@code held} 集合过滤：同一个键只有“抬起后再按下”
 *       才算一次新按下。</li>
 * </ul>
 *
 * <p>本类不依赖 JavaFX 运行时环境（仅 {@link #attach(Scene)} 需要），可直接单元测试。
 */
public class InputHandler {

    /** 待处理队列上限：仅作为防御性保护，真实输入远达不到；溢出时丢弃最旧的按键。 */
    public static final int MAX_BUFFERED_PRESSES = 32;

    private final Set<KeyCode> held = new HashSet<>();
    private final Deque<KeyCode> pending = new ArrayDeque<>();
    private final Set<KeyCode> justPressed = EnumSet.noneOf(KeyCode.class);
    private List<KeyCode> frameEvents = Collections.emptyList();

    /** JavaFX 事件接线（可重复调用，会替换上一次的处理器）。 */
    public void attach(Scene scene) {
        scene.setOnKeyPressed(event -> onKeyPressed(event.getCode()));
        scene.setOnKeyReleased(event -> onKeyReleased(event.getCode()));
    }

    /**
     * 按键按下。自动重复（键已在按下集合中）会被忽略。
     */
    public void onKeyPressed(KeyCode code) {
        if (code == null || !held.add(code)) {
            return;
        }
        if (pending.size() >= MAX_BUFFERED_PRESSES) {
            pending.pollFirst();
        }
        pending.addLast(code);
    }

    /**
     * 按键释放。
     */
    public void onKeyReleased(KeyCode code) {
        if (code == null) {
            return;
        }
        held.remove(code);
    }

    /**
     * 开始新的一帧：结算上一帧的事件，把缓冲中的按下事件搬入本帧事件表。
     */
    public void beginFrame() {
        justPressed.clear();
        if (pending.isEmpty()) {
            frameEvents = Collections.emptyList();
            return;
        }
        frameEvents = new ArrayList<>(pending);
        pending.clear();
        justPressed.addAll(frameEvents);
    }

    /** 把按键退回缓冲，下一帧重新可见；顺序追加在队尾。 */
    public void deferToNextFrame(KeyCode code) {
        if (code == null) {
            return;
        }
        if (pending.size() >= MAX_BUFFERED_PRESSES) {
            pending.pollFirst();
        }
        pending.addLast(code);
    }

    /** 按键当前是否处于按下状态（长按/持续移动）。 */
    public boolean isKeyPressed(KeyCode code) {
        return held.contains(code);
    }

    /** 本帧是否发生了该键的按下事件（不消费）。 */
    public boolean isKeyJustPressed(KeyCode code) {
        return justPressed.contains(code);
    }

    /** 本帧按下事件，按发生顺序排列。 */
    public List<KeyCode> getFrameEvents() {
        return Collections.unmodifiableList(frameEvents);
    }

    /** 缓冲中尚未被取走的按下事件数量（调试用）。 */
    public int getBufferedCount() {
        return pending.size();
    }

    /** 清空缓冲与按下状态（状态切换时调用，避免旧输入串场）。 */
    public void reset() {
        held.clear();
        pending.clear();
        justPressed.clear();
        frameEvents = Collections.emptyList();
    }
}
