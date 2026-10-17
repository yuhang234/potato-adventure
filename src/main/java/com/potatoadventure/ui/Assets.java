package com.potatoadventure.ui;

import javafx.scene.image.Image;
import javafx.scene.text.Font;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 已批准像素美术资产的统一加载入口（视觉规范：{@code design/ui-prototype/}）。
 *
 * <p>这里的资产目录就是设计原型目录本身，<b>只读引用，不复制不重命名</b>：原型 {@code index.html}
 * 用 {@code <img src="assets/...">} 引用的同一批 PNG，本类用同一批路径加载，因此游戏画面与
 * 已批准设计稿用的是逐字节相同的素材。
 *
 * <p>目录解析顺序：
 *
 * <ol>
 *   <li>系统属性 {@code -Dpotato.assets=/path/to/design/ui-prototype/assets}；</li>
 *   <li>从当前工作目录（{@code mvn javafx:run} 下即工程根目录）逐级向上查找
 *       {@code design/ui-prototype/assets}；</li>
 *   <li>从本类所在 JAR/classes 目录逐级向上查找。</li>
 * </ol>
 *
 * <p>找不到目录时不抛异常：{@link #img(String)} 返回 {@code null}，调用方跳过该元素即可，
 * 保证“资产缺失也能启动”不被构建环境差异拖垮（README 中说明了资产目录必须随仓库一起存在）。
 */
public final class Assets {

    /** 设计稿基准分辨率（原型 {@code #game-viewport} 固定 1280×720）。 */
    public static final int DESIGN_WIDTH = 1280;
    public static final int DESIGN_HEIGHT = 720;

    /** 资产根相对路径（相对仓库根）。 */
    public static final String RELATIVE_ROOT = "design/ui-prototype/assets";

    private static final Map<String, Image> IMAGE_CACHE = new HashMap<>();
    private static final Map<Integer, Font> FONT_CACHE = new HashMap<>();

    private static Path root;
    private static boolean rootResolved;

    private static String fontFamily;
    private static boolean fontResolved;

    private Assets() {
    }

    // ------------------------------------------------------------------ 路径

    /**
     * 资产根目录；解析失败返回 {@code null}。
     */
    public static synchronized Path root() {
        if (rootResolved) {
            return root;
        }
        rootResolved = true;

        String override = System.getProperty("potato.assets");
        if (override != null && !override.isBlank()) {
            Path candidate = Paths.get(override).toAbsolutePath().normalize();
            if (Files.isDirectory(candidate)) {
                root = candidate;
                System.out.println("[assets] 使用 -Dpotato.assets 指定的资产目录: " + root);
                return root;
            }
            System.err.println("[assets] -Dpotato.assets 指向的目录不存在: " + candidate);
        }

        Path found = searchUpwards(Paths.get(System.getProperty("user.dir", ".")));
        if (found == null) {
            found = searchUpwards(classesDirectory());
        }
        if (found != null) {
            root = found;
            System.out.println("[assets] 资产目录: " + root);
        } else {
            System.err.println("[assets] 未找到 " + RELATIVE_ROOT
                    + "（界面会退化为纯色占位；请从仓库根目录运行，或用 -Dpotato.assets 指定）");
        }
        return root;
    }

    private static Path searchUpwards(Path start) {
        if (start == null) {
            return null;
        }
        Path dir = start.toAbsolutePath().normalize();
        for (int i = 0; i < 8 && dir != null; i++) {
            Path candidate = dir.resolve(RELATIVE_ROOT);
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        return null;
    }

    private static Path classesDirectory() {
        try {
            Path location = Paths.get(Assets.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            return Files.isDirectory(location) ? location : location.getParent();
        } catch (Exception e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ 图片

    /**
     * 按相对路径加载资产，例如 {@code sprites/hero_berserker.png}、{@code ui/slot_epic.png}。
     *
     * @return 图片；路径无效或解码失败时返回 {@code null}
     */
    public static Image img(String relativePath) {
        if (relativePath == null) {
            return null;
        }
        Image cached = IMAGE_CACHE.get(relativePath);
        if (cached != null) {
            return cached;
        }
        Path base = root();
        if (base == null) {
            return null;
        }
        File file = base.resolve(relativePath).toFile();
        if (!file.isFile()) {
            System.err.println("[assets] 缺少资产: " + relativePath);
            return null;
        }
        Image image = new Image(file.toURI().toString());
        if (image.isError()) {
            System.err.println("[assets] 解码失败: " + relativePath + " -> " + image.getException());
            return null;
        }
        IMAGE_CACHE.put(relativePath, image);
        return image;
    }

    /** 背景图（1280×720）。 */
    public static Image background(String name) {
        return img("backgrounds/" + name);
    }

    /** 角色 / 敌人 / NPC 精灵图。 */
    public static Image sprite(String name) {
        return img("sprites/" + name);
    }

    /** 物品图标（64×64）。 */
    public static Image item(String name) {
        return img("items/" + name);
    }

    /** UI 材质 / 图标 / 边框。 */
    public static Image ui(String name) {
        return img("ui/" + name);
    }

    /**
     * 按品质取物品框：{@code slot_common/uncommon/rare/epic/legendary.png}。
     */
    public static Image slot(String rarity) {
        return ui("slot_" + rarity + ".png");
    }

    // ------------------------------------------------------------------ 字体

    /**
     * GNU Unifont 点阵字体（原型 {@code assets/fonts/unifont.otf}，覆盖全部汉字）。
     *
     * <p>字号必须是整数：Unifont 字形按 16×16 点阵设计，整数号才不会出现半像素模糊。
     * 字体加载失败时退化为等宽字体，界面结构不变。
     *
     * @param size 像素字号（整数）
     */
    public static Font font(int size) {
        int clamped = Math.max(8, Math.min(96, size));
        Font cached = FONT_CACHE.get(clamped);
        if (cached != null) {
            return cached;
        }
        Font font = javafx.scene.text.Font.font(resolveFamily(), clamped);
        FONT_CACHE.put(clamped, font);
        return font;
    }

    private static synchronized String resolveFamily() {
        if (fontResolved) {
            return fontFamily;
        }
        fontResolved = true;
        Path base = root();
        if (base == null) {
            return fontFamily = "Monospaced";
        }
        File file = base.resolve("fonts/unifont.otf").toFile();
        if (!file.isFile()) {
            System.err.println("[assets] 缺少字体 fonts/unifont.otf");
            return fontFamily = "Monospaced";
        }
        try (InputStream in = Files.newInputStream(file.toPath())) {
            Font loaded = Font.loadFont(in, 16);
            if (loaded != null) {
                fontFamily = loaded.getFamily();
                System.out.println("[assets] 已加载像素字体: " + fontFamily + " (" + file + ")");
            } else {
                System.err.println("[assets] 字体解析失败，退回等宽字体");
                fontFamily = "Monospaced";
            }
        } catch (Exception e) {
            System.err.println("[assets] 字体读取异常: " + e);
            fontFamily = "Monospaced";
        }
        return fontFamily;
    }

    /** 资产自检摘要，启动时打印一行，便于从日志判断素材是否就位。 */
    public static String summary() {
        Path base = root();
        if (base == null) {
            return "assets=MISSING";
        }
        return "assets=" + base + " font=" + resolveFamily()
                + " hero=" + (sprite("hero_berserker.png") != null)
                + " bg_title=" + (background("bg_title.png") != null);
    }
}
