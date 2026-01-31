package com.game.potato.resources;

import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import java.util.HashMap;
import java.util.Map;

/**
 * 资源管理器
 * 负责加载和管理游戏资源（图片、音频等）
 */
public class ResourceManager {
    
    private Map<String, Image> images;
    private Map<String, AudioClip> sounds;
    
    public ResourceManager() {
        this.images = new HashMap<>();
        this.sounds = new HashMap<>();
    }
    
    /**
     * 加载图片资源
     */
    public void loadImage(String name, String path) {
        try {
            Image image = new Image(path);
            images.put(name, image);
        } catch (Exception e) {
            System.err.println("无法加载图片: " + path);
            e.printStackTrace();
        }
    }
    
    /**
     * 获取图片资源
     */
    public Image getImage(String name) {
        return images.get(name);
    }
    
    /**
     * 加载音频资源
     */
    public void loadSound(String name, String path) {
        try {
            AudioClip sound = new AudioClip(path);
            sounds.put(name, sound);
        } catch (Exception e) {
            System.err.println("无法加载音频: " + path);
            e.printStackTrace();
        }
    }
    
    /**
     * 获取音频资源
     */
    public AudioClip getSound(String name) {
        return sounds.get(name);
    }
    
    /**
     * 清空所有资源
     */
    public void clear() {
        images.clear();
        sounds.clear();
    }
}
