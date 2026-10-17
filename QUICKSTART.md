# 土豆冒险游戏 - 快速开始指南

## 📋 前置要求

在开始之前，请确保您的系统已安装：

- ✅ **JDK 21 或更高版本**（必须是带 `javac` 的完整 JDK；JavaFX 由 Maven 自动下载，无需单独安装）
- ✅ **Apache Maven 3.6.3+**

如未安装，请参考 [README.md](README.md) 中的"环境搭建指南"章节。

---

## 🚀 三步快速启动

### Windows 用户

1. **双击运行启动脚本**
   ```
   run.bat
   ```
   
2. 脚本会自动：
   - ✓ 定位可用的 JDK 21（自动跳过只有 JRE 的目录）
   - ✓ 检查 Maven 环境
   - ✓ 编译项目并启动游戏（`mvn clean compile javafx:run`）

3. 开始游戏！

### macOS / Linux 用户

1. **添加执行权限并运行**
   ```bash
   chmod +x run.sh
   ./run.sh
   ```

2. 脚本会自动：
   - ✓ 定位可用的 JDK 21（自动跳过只有 JRE 的目录）
   - ✓ 检查 Maven 环境
   - ✓ 编译项目并启动游戏（`mvn clean compile javafx:run`）

3. 开始游戏！

---

## 🎮 游戏操作速查

### 移动控制
- **W / ↑** - 向上
- **S / ↓** - 向下
- **A / ←** - 向左
- **D / →** - 向右

### 战斗操作
- **空格** - 普通攻击（需靠近敌人）
- **1** - 普通攻击（战斗中）
- **2** - 火球术（消耗10MP）
- **3** - 治疗术（消耗15MP）
- **4** - 冰冻术（消耗12MP）
- **ESC** - 逃跑（50%成功率）

### 系统功能
- **F3** - 调试信息（FPS / 镜头 / 地图哈希）
- **F5** - 快速保存
- **F9** - 快速加载
- **P** - 暂停/继续

---

## 🎯 游戏目标

1. **探索地图** - 在 60×45 的大地图中自由移动（镜头平滑跟随）
2. **战斗敌人** - 击败史莱姆、哥布林、野狼
3. **升级角色** - 获得经验值提升等级
4. **保存进度** - 随时保存游戏进度（F5/F9，地图与位置完全一致）

---

## 📦 打包为独立程序

如果您想创建一个可独立运行的 JAR 文件：

### Windows
```cmd
build.bat
```

### macOS / Linux
```bash
mvn clean package
```

生成的文件位于：
```
target/potato-adventure-2.0.0-M1-jar-with-dependencies.jar
```

运行方法：
```bash
java -jar target/potato-adventure-2.0.0-M1-jar-with-dependencies.jar
```

---

## ❓ 常见问题

### Q1: 提示"找不到 Java"怎么办？
**A**: 安装 JDK 21（带 `javac` 的完整 JDK）并配置环境变量。详见 [README.md](README.md) 的"环境搭建指南"。

### Q2: 提示"找不到 Maven"怎么办？
**A**: 安装 Apache Maven 并配置环境变量。详见 [README.md](README.md) 的"环境搭建指南"。

### Q3: 出现 JavaFX 相关错误？
**A**: 请使用 `mvn javafx:run` 启动（`run.sh` / `run.bat` 已内置）。JavaFX 21 由 Maven 依赖提供，不再需要安装 JavaFX SDK 或使用特定 JDK 发行版。

### Q4: 如何删除存档重新开始？
**A**: 删除 `saves/save.db` 文件，重新启动游戏即可。

### Q5: 读档提示"M1 之前的旧存档不可读"？
**A**: M1 起存档包含完整地图数据；旧格式存档无法保证地图一致，因此被整体拒绝。按 F5 重新保存一次即可。

---

## 📚 更多信息

- 完整文档：[README.md](README.md)
- 需求文档：[Docs/需求文档.md](Docs/需求文档.md)
- 故障排除：[README.md#故障排除](README.md#故障排除)

---

**现在就开始您的土豆冒险之旅吧！🥔✨**
