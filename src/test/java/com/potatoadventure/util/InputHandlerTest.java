package com.potatoadventure.util;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 输入缓冲测试 —— 覆盖缺陷 6「快速连续按键被吞掉」。
 */
class InputHandlerTest {

    @Test
    void tapBetweenFramesIsNeverLost() {
        InputHandler input = new InputHandler();
        // 两次渲染帧之间完成的一次完整短按
        input.onKeyPressed(KeyCode.SPACE);
        input.onKeyReleased(KeyCode.SPACE);

        input.beginFrame();
        assertTrue(input.isKeyJustPressed(KeyCode.SPACE), "短按必须在下一帧被看到");
        assertFalse(input.isKeyPressed(KeyCode.SPACE), "已经抬起，不应算按住");

        input.beginFrame();
        assertFalse(input.isKeyJustPressed(KeyCode.SPACE), "事件只在一帧内有效");
    }

    /**
     * 旧实现的致命路径：事件先被看到，随后到来的释放回调把 justPressed 抹成 false。
     * 新实现里释放只影响“按住”状态，不影响本帧已结算的按下事件。
     */
    @Test
    void releaseAfterFrameDoesNotEraseTheEvent() {
        InputHandler input = new InputHandler();
        input.onKeyPressed(KeyCode.W);
        input.beginFrame();
        assertTrue(input.isKeyPressed(KeyCode.W));

        input.onKeyReleased(KeyCode.W);
        assertTrue(input.isKeyJustPressed(KeyCode.W), "释放不得抹掉本帧的按下事件");
        assertFalse(input.isKeyPressed(KeyCode.W));
    }

    @Test
    void autoRepeatIsFiltered() {
        InputHandler input = new InputHandler();
        for (int i = 0; i < 5; i++) {
            input.onKeyPressed(KeyCode.W);
        }
        input.beginFrame();
        assertEquals(1, input.getFrameEvents().size(), "长按自动重复只能算一次按下");
    }

    @Test
    void rapidTapsInOneFrameAreAllRecordedInOrder() {
        InputHandler input = new InputHandler();
        input.onKeyPressed(KeyCode.W);
        input.onKeyReleased(KeyCode.W);
        input.onKeyPressed(KeyCode.D);
        input.onKeyReleased(KeyCode.D);
        input.onKeyPressed(KeyCode.W);
        input.onKeyReleased(KeyCode.W);

        input.beginFrame();
        List<KeyCode> events = input.getFrameEvents();
        assertEquals(List.of(KeyCode.W, KeyCode.D, KeyCode.W), events, "同一帧的多次短按必须按顺序全部保留");
    }

    @Test
    void manualRepeatedTapsAcrossFramesAreAllDelivered() {
        InputHandler input = new InputHandler();
        int delivered = 0;
        for (int i = 0; i < 10; i++) {
            input.onKeyPressed(KeyCode.SPACE);
            input.onKeyReleased(KeyCode.SPACE);
            input.beginFrame();
            if (input.isKeyJustPressed(KeyCode.SPACE)) {
                delivered++;
            }
        }
        assertEquals(10, delivered, "10 次跨帧短按必须全部送达");
    }

    @Test
    void heldKeyStaysPressedAndFiresOnce() {
        InputHandler input = new InputHandler();
        input.onKeyPressed(KeyCode.A);
        input.beginFrame();
        assertTrue(input.isKeyPressed(KeyCode.A));
        assertTrue(input.isKeyJustPressed(KeyCode.A));

        input.beginFrame();
        assertTrue(input.isKeyPressed(KeyCode.A), "按住状态必须持续");
        assertFalse(input.isKeyJustPressed(KeyCode.A), "按下事件不应重复触发");
    }

    @Test
    void deferToNextFrameRebuffersEvent() {
        InputHandler input = new InputHandler();
        input.onKeyPressed(KeyCode.DIGIT1);
        input.beginFrame();
        assertTrue(input.isKeyJustPressed(KeyCode.DIGIT1));

        input.deferToNextFrame(KeyCode.DIGIT1);
        assertTrue(input.isKeyJustPressed(KeyCode.DIGIT1), "当帧仍然可见");

        input.beginFrame();
        assertTrue(input.isKeyJustPressed(KeyCode.DIGIT1), "退回的按键下一帧继续可见");
        input.beginFrame();
        assertFalse(input.isKeyJustPressed(KeyCode.DIGIT1));
    }

    @Test
    void bufferIsBounded() {
        InputHandler input = new InputHandler();
        for (int i = 0; i < 200; i++) {
            input.onKeyPressed(KeyCode.SPACE);
            input.onKeyReleased(KeyCode.SPACE);
        }
        assertTrue(input.getBufferedCount() <= InputHandler.MAX_BUFFERED_PRESSES,
                "缓冲必须有上限，不能无界增长");
        assertTrue(input.getBufferedCount() > 0);
    }

    @Test
    void resetClearsEverything() {
        InputHandler input = new InputHandler();
        input.onKeyPressed(KeyCode.W);
        input.beginFrame();
        input.reset();
        assertFalse(input.isKeyPressed(KeyCode.W));
        assertFalse(input.isKeyJustPressed(KeyCode.W));
        assertEquals(0, input.getBufferedCount());
    }
}
