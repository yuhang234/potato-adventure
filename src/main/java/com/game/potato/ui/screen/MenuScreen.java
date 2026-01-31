package com.game.potato.ui.screen;

import com.game.potato.input.InputHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * 主菜单屏幕
 */
public class MenuScreen extends Screen {
    
    private static final String TITLE = "土豆冒险";
    private static final String[] MENU_ITEMS = {
        "开始游戏",
        "继续游戏",
        "读取存档",
        "设置",
        "帮助",
        "退出游戏"
    };
    
    private int selectedIndex = 0;
    private Font titleFont;
    private Font menuFont;
    
    @Override
    public void onEnter() {
        titleFont = new Font("Microsoft YaHei", 48);
        menuFont = new Font("Microsoft YaHei", 24);
    }
    
    @Override
    public void onExit() {
        // 清理资源
    }
    
    @Override
    public void update(double deltaTime, InputHandler input) {
        // 上下键选择菜单项
        if (input.isKeyJustPressed(KeyCode.UP) || input.isKeyJustPressed(KeyCode.W)) {
            selectedIndex = (selectedIndex - 1 + MENU_ITEMS.length) % MENU_ITEMS.length;
        }
        
        if (input.isKeyJustPressed(KeyCode.DOWN) || input.isKeyJustPressed(KeyCode.S)) {
            selectedIndex = (selectedIndex + 1) % MENU_ITEMS.length;
        }
        
        // 回车键确认选择
        if (input.isKeyJustPressed(KeyCode.ENTER) || input.isKeyJustPressed(KeyCode.SPACE)) {
            handleMenuSelection();
        }
    }
    
    @Override
    public void render() {
        double width = gc.getCanvas().getWidth();
        double height = gc.getCanvas().getHeight();
        
        // 绘制背景
        gc.setFill(Color.rgb(20, 30, 40));
        gc.fillRect(0, 0, width, height);
        
        // 绘制标题
        gc.setFont(titleFont);
        gc.setFill(Color.GOLD);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(TITLE, width / 2, height / 4);
        
        // 绘制菜单项
        gc.setFont(menuFont);
        double startY = height / 2;
        double spacing = 50;
        
        for (int i = 0; i < MENU_ITEMS.length; i++) {
            if (i == selectedIndex) {
                // 选中项高亮
                gc.setFill(Color.YELLOW);
                gc.fillText("> " + MENU_ITEMS[i] + " <", width / 2, startY + i * spacing);
            } else {
                gc.setFill(Color.WHITE);
                gc.fillText(MENU_ITEMS[i], width / 2, startY + i * spacing);
            }
        }
        
        // 绘制版本信息
        gc.setFont(new Font("Arial", 14));
        gc.setFill(Color.GRAY);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Version 1.0.0", 10, height - 10);
    }
    
    /**
     * 处理菜单选择
     */
    private void handleMenuSelection() {
        switch (selectedIndex) {
            case 0: // 开始游戏
                // TODO: 切换到游戏屏幕
                System.out.println("开始游戏");
                break;
                
            case 1: // 继续游戏
                System.out.println("继续游戏");
                break;
                
            case 2: // 读取存档
                System.out.println("读取存档");
                break;
                
            case 3: // 设置
                System.out.println("设置");
                break;
                
            case 4: // 帮助
                System.out.println("帮助");
                break;
                
            case 5: // 退出游戏
                System.exit(0);
                break;
        }
    }
}
