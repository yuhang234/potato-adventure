package com.potatoadventure.util;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 输入处理器 - 处理键盘输入
 */
public class InputHandler {
    
    private final Set<KeyCode> currentlyPressed = new HashSet<>();
    private final Map<KeyCode, Boolean> justPressed = new HashMap<>();
    
    public InputHandler(Scene scene) {
        // 按键按下事件
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (!currentlyPressed.contains(code)) {
                justPressed.put(code, true);
            }
            currentlyPressed.add(code);
        });
        
        // 按键释放事件
        scene.setOnKeyReleased(event -> {
            currentlyPressed.remove(event.getCode());
            justPressed.put(event.getCode(), false);
        });
    }
    
    /**
     * 检查按键是否被按下
     */
    public boolean isKeyPressed(KeyCode code) {
        return currentlyPressed.contains(code);
    }
    
    /**
     * 检查按键是否刚被按下（单次触发）
     */
    public boolean isKeyJustPressed(KeyCode code) {
        Boolean pressed = justPressed.get(code);
        if (pressed != null && pressed) {
            justPressed.put(code, false);
            return true;
        }
        return false;
    }
}
