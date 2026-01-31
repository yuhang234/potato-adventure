package com.game.potato.graphics;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 玩家图形渲染器
 */
public class PlayerGraphics {
    
    private double x;
    private double y;
    private Color color;
    
    public PlayerGraphics(double x, double y) {
        this.x = x;
        this.y = y;
        this.color = Color.BLUE;
    }
    
    /**
     * 渲染玩家
     */
    public void render(GraphicsContext gc, double width, double height) {
        gc.setFill(color);
        gc.fillOval(x, y, width, height);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(x, y, width, height);
    }
    
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
}
