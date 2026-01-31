package com.game.potato.graphics;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 瓦片图形渲染器
 */
public class TileGraphics {
    
    private Color color;
    
    public TileGraphics(Color color) {
        this.color = color;
    }
    
    /**
     * 渲染瓦片
     */
    public void render(GraphicsContext gc, double x, double y, double width, double height) {
        gc.setFill(color);
        gc.fillRect(x, y, width, height);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y, width, height);
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
}
