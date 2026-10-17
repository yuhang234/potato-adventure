# 土豆冒险游戏 (Potato Adventure)

## 项目简介

《土豆冒险》是一款基于 **Java 21 + OpenJFX 21** 开发的 2D 冒险游戏，实现了核心玩法包括：角色移动、地图探索、碰撞检测、战斗系统和存档功能。游戏采用纯代码生成图形资源，无需外部资源文件，开箱即用。

> **v2.0.0-M1（本版本）** 完成“基础底座与引擎重构”：升级到 Java 21 + OpenJFX 21（JavaFX 由 Maven 依赖提供，任意标准 JDK 21 均可运行）、加入平滑滚屏摄像机与视口裁剪、地图扩展到 60×45、修复 6 个已复现缺陷。详见 [`Docs/M1_引擎重构说明.md`](Docs/M1_引擎重构说明.md)。

### 核心特性

✅ **基础游戏框架** - 完整的游戏循环和渲染系统  
✅ **角色系统** - 玩家角色移动、属性管理、升级系统  
✅ **地图系统** - 随机生成地图、碰撞检测  
✅ **战斗系统** - 回合制战斗、普通攻击、4 个技能（火球术/治疗术/冰冻术/逃跑）
✅ **敌人AI** - 3种不同类型的敌人（史莱姆、哥布林、野狼）  
✅ **存档系统** - 基于SQLite的完整存档/读档功能  
✅ **纯代码生成** - 所有图形资源通过JavaFX代码生成，无外部依赖  

---

## 环境要求

### 必需环境

1. **Java开发工具包 (JDK)**
   - **版本**: **JDK 21 或更高版本**（必须是完整 JDK，带 `javac`；只装 JRE 无法编译）
   - **下载地址**:
     - Eclipse Temurin 21: https://adoptium.net/temurin/releases/?version=21
     - 任意发行版均可（Temurin / Liberica / Zulu / Oracle / 各 Linux 发行版仓库）

2. **Apache Maven**
   - **版本**: Maven 3.6.3 或更高版本
   - **下载地址**: https://maven.apache.org/download.cgi

3. **JavaFX 21 —— 无需单独安装**
   - JavaFX（`org.openjfx:javafx-controls/graphics/media:21.0.2`）已在 `pom.xml` 声明为普通 Maven 依赖
   - 首次构建时 Maven 会自动下载本机平台对应的原生库，不依赖 Oracle JDK 或任何“带 JavaFX”的特殊发行版
   - 因此旧文档中“安装 JavaFX SDK / 设置 jfxrt.jar”的步骤全部不再需要

### 可选工具

- **IDE**: IntelliJ IDEA / Eclipse / NetBeans
- **Git**: 用于版本控制

---

## 环境搭建指南

### Windows 系统

#### 1. 安装 JDK 21

1. 下载 JDK 21 安装包（推荐 Eclipse Temurin 21，任意发行版均可）
2. 运行安装程序，默认安装路径：`C:\Program Files\Eclipse Adoptium\jdk-21.0.x`
3. 配置环境变量：
   - 右键"此电脑" → "属性" → "高级系统设置" → "环境变量"
   - 新建系统变量：
     - 变量名：`JAVA_HOME`
     - 变量值：`C:\Program Files\Eclipse Adoptium\jdk-21.0.x`（实际安装路径）
   - 编辑系统变量 `Path`，添加：
     - `%JAVA_HOME%\bin`

4. 验证安装：
```powershell
java -version
javac -version
```

应该显示类似：
```
openjdk version "21.0.2" 2024-01-16 LTS
OpenJDK Runtime Environment Temurin-21.0.2+13 (build 21.0.2+13-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.2+13 (build 21.0.2+13-LTS, mixed mode, sharing)
```

> 注意：`javac -version` 必须能输出版本号。如果提示找不到 `javac`，说明装的是 JRE 而不是 JDK。

#### 2. 安装 Maven

1. 下载 Maven 3.6.3 或更高版本（apache-maven-3.6.3-bin.zip）
2. 解压到任意目录，例如：`C:\Program Files\Apache\maven`
3. 配置环境变量：
   - 新建系统变量：
     - 变量名：`MAVEN_HOME`
     - 变量值：`C:\Program Files\Apache\maven`
   - 编辑系统变量 `Path`，添加：
     - `%MAVEN_HOME%\bin`

4. 验证安装：
```powershell
mvn -version
```

应该显示类似：
```
Apache Maven 3.6.3
Maven home: C:\Program Files\Apache\maven
Java version: 21.0.2, vendor: Eclipse Adoptium
```

#### 3. JavaFX：不需要额外步骤

JavaFX 21 由 Maven 从中央仓库自动下载（含 Windows 原生库）。只要 `mvn -version` 输出的 Java 版本是 21+，即可直接进入下面的“快速开始”运行游戏，无需配置任何 JavaFX SDK。

### macOS / Linux 系统

#### 1. 安装 JDK 21

**macOS (使用 Homebrew)**:
```bash
brew install openjdk@21
# 按 Homebrew 提示把 openjdk@21 加入 PATH，或手动设置：
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

**Linux (Ubuntu/Debian)**:
```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

配置环境变量（添加到 `~/.bashrc` 或 `~/.zshrc`）：
```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

应用配置：
```bash
source ~/.bashrc  # 或 source ~/.zshrc
```

#### 2. 安装 Maven

**macOS**:
```bash
brew install maven
```

**Linux (Ubuntu/Debian)**:
```bash
sudo apt install maven
```

验证安装：
```bash
java -version
mvn -version
```

---

## 快速开始

> 前置条件：`java -version` 与 `javac -version` 都是 21+，`mvn -version` 可用。

### 方法一：一条命令启动（推荐）

```bash
# Linux / macOS
./run.sh

# Windows
run.bat
```

脚本会自动定位可用的 JDK 21（跳过只有 JRE 的目录）、导出 `JAVA_HOME`，然后执行
`mvn -q clean compile javafx:run` 启动游戏。也可以手动执行等价命令：

```bash
mvn clean compile javafx:run
```

首次运行需要联网下载 JavaFX 21 与 SQLite 驱动（约 30–60 秒），之后启动在数秒内完成。

### 方法二：打包为可执行 JAR

1. **打包项目**
```bash
mvn clean package
```

2. **运行 JAR 文件**
```bash
java -jar target/potato-adventure-2.0.0-M1-jar-with-dependencies.jar
```

> 说明：JavaFX 桌面应用以 `java -jar` 运行时，JavaFX 类由 JAR 内的依赖提供；若你使用的是未内置 JavaFX 的 JDK，
> 推荐仍然使用 `mvn javafx:run`（它会自动配置模块路径，最稳妥）。

### 方法三：使用 IDE 运行

#### IntelliJ IDEA

1. 打开 IntelliJ IDEA
2. 选择 `File` → `Open`，打开项目目录
3. 等待 Maven 自动导入依赖
4. 右键 `src/main/java/com/potatoadventure/Main.java`
5. 选择 `Run 'Main.main()'`

#### Eclipse

1. 打开 Eclipse
2. 选择 `File` → `Import` → `Existing Maven Projects`
3. 选择项目目录，点击 `Finish`
4. 右键 `Main.java` → `Run As` → `Java Application`

---

## 游戏操作说明

### 基本操作

| 按键 | 功能 |
|------|------|
| **WASD / ↑↓←→** | 移动（支持按住连续走、斜向移动；按键有缓冲，快速连按不丢失） |
| **空格** | 普通攻击（需在敌人旁边） |
| **F3** | 切换调试信息（FPS / 当前格 / 镜头偏移 / 地图哈希） |
| **P** | 暂停/继续游戏 |
| **F5** | 快速保存游戏 |
| **F9** | 快速加载存档 |

### 战斗操作（触碰敌人进入战斗）

| 按键 | 功能 | 消耗 |
|------|------|------|
| **1** | 普通攻击 | 无 |
| **2** | 火球术（1.5倍伤害） | 10 MP |
| **3** | 治疗术（恢复30%HP） | 15 MP |
| **4** | 冰冻术 | 12 MP |
| **ESC** | 尝试逃跑（50%成功率，成功后退回上一格） | 无 |

> M1 修复：敌人不再每帧攻击（严格的回合制），逃跑成功后确实会退出战斗，击败敌人只结算一次经验。

---

## 游戏系统详解

### 角色系统

- **初始属性**:
  - HP: 100
  - MP: 50
  - 攻击: 15
  - 防御: 10
  - 速度: 5

- **升级系统**:
  - 击败敌人获得经验值
  - 升级所需经验 = 等级 × 100
  - 每升一级：HP+20, MP+10, 攻击+3, 防御+2, 速度+1

### 敌人类型

| 敌人 | 等级 | HP | 攻击 | 防御 | 经验奖励 |
|------|------|----|----|------|----------|
| **史莱姆** | 1 | 40 | 8 | 5 | 20 |
| **哥布林** | 2 | 60 | 12 | 8 | 35 |
| **野狼** | 3 | 80 | 15 | 10 | 50 |

### 地图系统

- **地图大小**: 60×45 格子 = 1920×1440 像素（每格 32×32 像素），窗口 1280×720
- **摄像机**: 平滑跟随玩家（指数趋近，时间常数 0.12 秒），贴边时自动夹取，不会露出黑边；渲染使用整数偏移保证像素不糊
- **视口裁剪**: 每帧只绘制可见范围内的地块，地图生成一次后即为只读
- **地形类型**:
  - 🟩 草地：可通行（草叶细节在地图生成时一次性确定，不会闪烁）
  - ⬛ 墙壁：不可通行
  - 🌳 树木：不可通行
  - 💧 水域：不可通行

### 战斗系统

- **伤害计算**: 实际伤害 = 攻击力 - 防御力/2（最低1点）
- **暴击系统**: 20%概率造成双倍伤害
- **闪避系统**: 10%概率完全闪避攻击

### 存档系统

- **自动保存**: 按 F5 快速保存
- **读取存档**: 按 F9 快速加载
- **存储位置**: `saves/save.db`（SQLite数据库）
- **存储内容**:
  - 玩家位置、等级、属性
  - 敌人状态（位置、生命值，含已被击败的敌人，保证读档后世界完全一致）
  - **地图本身**（`map_meta` + `map_tile` 两张表：尺寸、种子、逐格地形）
  - 游戏时间戳
- **一致性保证**: 读档后地图与存档时逐格一致（F3 调试面板中的 `hash` 在保存前后相同）
- **旧存档**: M1 之前的存档没有地图表，加载时会整体拒绝并提示（不会用随机新地图冒充旧地图）

---

## 项目结构

```
potatoadven/
├── pom.xml                                 # Maven配置文件
├── README.md                               # 本文档
├── QUICKSTART.md                           # 三分钟上手指南
├── run.sh / run.bat                        # 一键启动脚本（自动定位 JDK 21）
├── .gitignore                              # Git忽略文件
├── Docs/
│   ├── 需求文档.md                         # 完整需求文档
│   └── M1_引擎重构说明.md                  # M1 改动、缺陷根因与验证记录
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── potatoadventure/
│   │               ├── Main.java           # 主程序入口
│   │               ├── core/
│   │               │   ├── GameEngine.java    # 游戏引擎（核心循环、状态机、HUD）
│   │               │   ├── GameState.java     # 游戏状态枚举（含 MENU 占位说明）
│   │               │   ├── Camera.java        # 平滑滚屏摄像机
│   │               │   └── RetreatPlanner.java# 逃跑落点计算
│   │               ├── model/
│   │               │   ├── Player.java     # 玩家模型（逻辑格 + 视觉插值）
│   │               │   ├── Enemy.java      # 敌人模型
│   │               │   └── GameMap.java    # 地图模型（确定性生成、只读、视口裁剪）
│   │               ├── service/
│   │               │   ├── BattleService.java      # 战斗服务（回合制）
│   │               │   └── SaveLoadService.java    # 存档服务（含地图表）
│   │               ├── ui/
│   │               │   └── Palette.java    # 获批配色常量（单一来源）
│   │               └── util/
│   │                   └── InputHandler.java       # 输入处理（有序缓冲）
│   └── test/
│       └── java/com/potatoadventure/       # JUnit 5 单元测试（67 个用例）
│           ├── core/     CameraTest / RetreatPlannerTest
│           ├── model/    GameMapTest / PlayerMovementTest
│           ├── service/  BattleServiceTest / SaveLoadServiceTest
│           └── util/     InputHandlerTest
└── saves/                                  # 存档目录（自动生成）
    └── save.db                             # SQLite存档文件
```

---

## 故障排除

### 问题 1: 运行时提示 "找不到或无法加载主类"

**解决方案**:
```bash
# 清理并重新编译
mvn clean compile
mvn javafx:run
```

### 问题 2: JavaFX 相关错误（如 "Error: JavaFX runtime components are missing"）

**原因**: 用错了启动方式（例如直接用 `java` 运行未带模块路径的类），或本地仓库里的 JavaFX 依赖下载不完整。

**解决方案**:
1. 使用 `mvn javafx:run` 启动（脚本 `run.sh` / `run.bat` 已经这样做），JavaFX 21 由 Maven 依赖提供，不需要特殊 JDK
2. 若仍失败，清掉 JavaFX 缓存重新下载：
```bash
rm -rf ~/.m2/repository/org/openjfx
mvn clean compile javafx:run
```

### 问题 3: 提示 "JAVA_HOME 指向的目录没有 javac" / "No compiler is provided in this environment"

**原因**: 装的是 JRE（或只装了 `openjdk-21-jre`），不是完整 JDK。

**解决方案**:
```bash
# Ubuntu/Debian
sudo apt install openjdk-21-jdk
# 让脚本自动挑选：直接再次运行
./run.sh
```
`run.sh` 会遍历 `JAVA_HOME`、PATH、SDKMAN、`/usr/lib/jvm` 等位置，自动跳过只有 JRE 的目录。

### 问题 4: SQLite 数据库错误

**解决方案**:
```bash
# 删除旧的存档文件
rm -rf saves/save.db  # Linux/macOS
del saves\save.db     # Windows

# 重新运行游戏，会自动创建新的数据库
```

### 问题 5: 读档提示"存档缺少地图数据（M1 之前的旧存档不可读）"

**原因**: M1 之前的存档只记录玩家与敌人，没有地图数据，无法保证读档后地图一致，因此被整体拒绝（而不是悄悄换一张随机地图）。

**解决方案**: 按 F5 重新保存一次即可生成 M1 格式的新存档。

### 问题 6: Maven 下载依赖失败

**解决方案**:
```bash
# 清理本地仓库并重新下载
mvn clean
mvn dependency:purge-local-repository
mvn install
```

或者配置 Maven 使用国内镜像（编辑 `~/.m2/settings.xml`）:
```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <url>https://maven.aliyun.com/repository/central</url>
  </mirror>
</mirrors>
```

---

## 技术栈

- **编程语言**: Java SE 21（`maven.compiler.release=21`）
- **UI框架**: OpenJFX 21（`javafx-controls` / `javafx-graphics` / `javafx-media` 21.0.2，Maven Central）
- **数据库**: SQLite 3.45.1（xerial `sqlite-jdbc`）
- **日志**: SLF4J 2.0.12（api + simple）
- **测试**: JUnit 5（Jupiter）5.10.2，共 67 个用例
- **构建工具**: Apache Maven (compiler 3.13.0 / surefire 3.2.5 / javafx-maven-plugin 0.0.8)

---

## 开发计划

### 已实现功能 ✅

- [x] 基础游戏框架（游戏循环、渲染）
- [x] 角色移动和碰撞检测
- [x] 地图生成和渲染
- [x] 战斗系统（普通攻击、技能）
- [x] 敌人AI（3种类型）
- [x] 存档系统（SQLite）
- [x] 纯代码生成图形

### 待扩展功能 🚀

- [ ] 音效系统（使用Java Sound API）
- [ ] 装备系统（武器、防具）
- [ ] 物品系统（药水、材料）
- [ ] 任务系统（主线、支线）
- [ ] 更多敌人类型和BOSS
- [ ] 多个地图场景
- [ ] 游戏菜单界面
- [ ] 成就系统

---

## 许可证

本项目仅供学习和研究使用。

---

## 联系方式

如有问题或建议，欢迎反馈：
- 项目地址: [GitHub Repository]
- 文档: 查看 `Docs/需求文档.md` 了解完整需求

---

## 更新日志

### v2.0.0-M1 (2026-10-17)

**里程碑 M1：基础底座与引擎重构**

- ⬆️ 升级到 Java 21 + OpenJFX 21，JavaFX 改为 Maven 依赖（任意标准 JDK 21 即可运行）
- 🎥 新增平滑滚屏摄像机：跟随玩家、贴边夹取（不再有黑边）、整数像素对齐
- ✂️ 新增视口裁剪：每帧只绘制可见地块；地图生成一次后只读
- 🗺️ 地图扩展到 60×45（1920×1440 像素），确定性生成（同一存档读档后逐格一致）
- 🐞 修复 6 个缺陷：敌人每帧攻击、无法逃离战斗、单次击杀双倍经验、存档不保存地图、草地纹理逐帧闪烁、快速按键丢失
- 💾 存档新增地图表（`map_meta` / `map_tile`），读档保证地图与位置完全一致；旧格式存档明确拒绝
- ⌨️ 输入改为有序缓冲，支持按住连走、斜向移动，按键零丢失
- 🧪 新增 67 个 JUnit 5 单元测试覆盖上述改动
- 📄 新增 `Docs/M1_引擎重构说明.md`；`run.sh` / `run.bat` 自动定位 JDK 21

### v1.0.0 (2026-01-31)

**核心功能**:
- ✨ 实现基础游戏框架（游戏循环、渲染系统）
- ✨ 实现角色移动和地图探索系统
- ✨ 实现完整的战斗系统（普通攻击、技能、战斗AI）
- ✨ 实现SQLite存档系统
- ✨ 纯代码生成所有游戏图形
- 🎮 支持键盘操作（WASD/方向键移动）
- 💾 支持快速保存/加载（F5/F9）

**技术特性**:
- 基于Java 8和JavaFX 8
- 无外部资源依赖
- 支持60 FPS游戏循环
- 完整的碰撞检测系统

---

**祝您游戏愉快！🥔✨**
