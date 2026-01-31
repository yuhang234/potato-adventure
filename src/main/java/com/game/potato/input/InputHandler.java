package com.game.potato.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * 输入处理器
 * 管理键盘和鼠标输入
 */
public class InputHandler {
    
    private final Set<KeyCode> keysPressed;
    private final Set<KeyCode> keysJustPressed;
    private final Set<KeyCode> keysReleased;
    
    private double mouseX;
    private double mouseY;
    private boolean mouseLeftPressed;
    private boolean mouseRightPressed;
    private boolean mouseLeftClicked;
    private boolean mouseRightClicked;
    
    public InputHandler() {
        this.keysPressed = new HashSet<>();
        this.keysJustPressed = new HashSet<>();
        this.keysReleased = new HashSet<>();
    }
    
    /**
     * 将输入处理器附加到场景
     */
    public void attachToScene(Scene scene) {
        // 键盘事件
        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(this::handleKeyReleased);
        
        // 鼠标事件
        scene.setOnMousePressed(this::handleMousePressed);
        scene.setOnMouseReleased(this::handleMouseReleased);
        scene.setOnMouseMoved(this::handleMouseMoved);
        scene.setOnMouseDragged(this::handleMouseMoved);
    }
    
    /**
     * 处理按键按下事件
     */
    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if (!keysPressed.contains(code)) {
            keysJustPressed.add(code);
        }
        keysPressed.add(code);
    }
    
    /**
     * 处理按键释放事件
     */
    private void handleKeyReleased(KeyEvent event) {
        KeyCode code = event.getCode();
        keysPressed.remove(code);
        keysReleased.add(code);
    }
    
    /**
     * 处理鼠标按下事件
     */
    private void handleMousePressed(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            mouseLeftPressed = true;
            mouseLeftClicked = true;
        } else if (event.getButton() == MouseButton.SECONDARY) {
            mouseRightPressed = true;
            mouseRightClicked = true;
        }
        mouseX = event.getX();
        mouseY = event.getY();
    }
    
    /**
     * 处理鼠标释放事件
     */
    private void handleMouseReleased(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            mouseLeftPressed = false;
        } else if (event.getButton() == MouseButton.SECONDARY) {
            mouseRightPressed = false;
        }
    }
    
    /**
     * 处理鼠标移动事件
     */
    private void handleMouseMoved(MouseEvent event) {
        mouseX = event.getX();
        mouseY = event.getY();
    }
    
    /**
     * 每帧更新后调用，清理一次性状态
     */
    public void update() {
        keysJustPressed.clear();
        keysReleased.clear();
        mouseLeftClicked = false;
        mouseRightClicked = false;
    }
    
    // 键盘查询方法
    public boolean isKeyPressed(KeyCode key) {
        return keysPressed.contains(key);
    }
    
    public boolean isKeyJustPressed(KeyCode key) {
        return keysJustPressed.contains(key);
    }
    
    public boolean isKeyReleased(KeyCode key) {
        return keysReleased.contains(key);
    }
    
    // 方向键检测
    public boolean isUpPressed() {
        return isKeyPressed(KeyCode.W) || isKeyPressed(KeyCode.UP);
    }
    
    public boolean isDownPressed() {
        return isKeyPressed(KeyCode.S) || isKeyPressed(KeyCode.DOWN);
    }
    
    public boolean isLeftPressed() {
        return isKeyPressed(KeyCode.A) || isKeyPressed(KeyCode.LEFT);
    }
    
    public boolean isRightPressed() {
        return isKeyPressed(KeyCode.D) || isKeyPressed(KeyCode.RIGHT);
    }
    
    // 鼠标查询方法
    public double getMouseX() {
        return mouseX;
    }
    
    public double getMouseY() {
        return mouseY;
    }
    
    public boolean isMouseLeftPressed() {
        return mouseLeftPressed;
    }
    
    public boolean isMouseRightPressed() {
        return mouseRightPressed;
    }
    
    public boolean isMouseLeftClicked() {
        return mouseLeftClicked;
    }
    
    public boolean isMouseRightClicked() {
        return mouseRightClicked;
    }
}
