# 土豆冒险游戏 (Potato Adventure)

## 项目简介

《土豆冒险》是一款基于Java 8和JavaFX 8开发的2D冒险游戏，实现了核心玩法包括：角色移动、地图探索、碰撞检测、战斗系统和存档功能。游戏采用纯代码生成图形资源，无需外部资源文件，开箱即用。

### 核心特性

✅ **基础游戏框架** - 完整的游戏循环和渲染系统  
✅ **角色系统** - 玩家角色移动、属性管理、升级系统  
✅ **地图系统** - 随机生成地图、碰撞检测  
✅ **战斗系统** - 实时战斗、普通攻击、技能系统  
✅ **敌人AI** - 3种不同类型的敌人（史莱姆、哥布林、野狼）  
✅ **存档系统** - 基于SQLite的完整存档/读档功能  
✅ **纯代码生成** - 所有图形资源通过JavaFX代码生成，无外部依赖  

---

## 环境要求

### 必需环境

1. **Java开发工具包 (JDK)**
   - **版本**: JDK 8 或更高版本（推荐 JDK 8u401）
   - **下载地址**: 
     - Oracle JDK 8: https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html
     - OpenJDK 8: https://adoptium.net/temurin/releases/?version=8

2. **Apache Maven**
   - **版本**: Maven 3.6.3 或更高版本
   - **下载地址**: https://maven.apache.org/download.cgi

3. **JavaFX 8**
   - **重要**: Oracle JDK 8 内置 JavaFX 8
   - **注意**: 如果使用 OpenJDK 8，需要单独安装 JavaFX

### 可选工具

- **IDE**: IntelliJ IDEA / Eclipse / NetBeans
- **Git**: 用于版本控制

---

## 环境搭建指南

### Windows 系统

#### 1. 安装 JDK 8

1. 下载 JDK 8 安装包（推荐使用 Oracle JDK 8，已内置 JavaFX）
2. 运行安装程序，默认安装路径：`C:\Program Files\Java\jdk1.8.0_xxx`
3. 配置环境变量：
   - 右键"此电脑" → "属性" → "高级系统设置" → "环境变量"
   - 新建系统变量：
     - 变量名：`JAVA_HOME`
     - 变量值：`C:\Program Files\Java\jdk1.8.0_xxx`（实际安装路径）
   - 编辑系统变量 `Path`，添加：
     - `%JAVA_HOME%\bin`
     - `%JAVA_HOME%\jre\bin`

4. 验证安装：
```powershell
java -version
javac -version
```

应该显示类似：
```
java version "1.8.0_401"
Java(TM) SE Runtime Environment (build 1.8.0_401-b10)
Java HotSpot(TM) 64-Bit Server VM (build 25.401-b10, mixed mode)
```

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
Java version: 1.8.0_401, vendor: Oracle Corporation
```

#### 3. 验证 JavaFX

运行以下命令验证 JavaFX 是否可用：

```powershell
java -cp "%JAVA_HOME%\jre\lib\ext\jfxrt.jar" javafx.application.Application
```

如果没有报错，说明 JavaFX 已正确安装。

### macOS / Linux 系统

#### 1. 安装 JDK 8

**macOS (使用 Homebrew)**:
```bash
brew tap AdoptOpenJDK/openjdk
brew install --cask adoptopenjdk8
```

**Linux (Ubuntu/Debian)**:
```bash
sudo apt update
sudo apt install openjdk-8-jdk openjfx
```

配置环境变量（添加到 `~/.bashrc` 或 `~/.zshrc`）：
```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
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

### 方法一：使用 Maven 运行（推荐）

1. **下载项目**
```bash
# 如果使用 Git
git clone <repository-url>
cd potatoadven

# 或直接解压项目压缩包
```

2. **编译项目**
```bash
mvn clean compile
```

3. **运行游戏**
```bash
mvn exec:java -Dexec.mainClass="com.potatoadventure.Main"
```

### 方法二：打包为可执行 JAR

1. **打包项目**
```bash
mvn clean package
```

2. **运行 JAR 文件**
```bash
java -jar target/potato-adventure-1.0.0-jar-with-dependencies.jar
```

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
| **W / ↑** | 向上移动 |
| **S / ↓** | 向下移动 |
| **A / ←** | 向左移动 |
| **D / →** | 向右移动 |
| **空格** | 普通攻击（需在敌人旁边） |
| **P** | 暂停/继续游戏 |
| **F5** | 快速保存游戏 |
| **F9** | 快速加载存档 |

### 战斗操作（触碰敌人进入战斗）

| 按键 | 功能 | 消耗 |
|------|------|------|
| **1** | 普通攻击 | 无 |
| **2** | 火球术（1.5倍伤害） | 10 MP |
| **3** | 治疗术（恢复30%HP） | 15 MP |
| **ESC** | 尝试逃跑（50%成功率） | 无 |

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

- **地图大小**: 40×30 格子（每格32×32像素）
- **地形类型**:
  - 🟩 草地：可通行
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
  - 敌人状态（位置、生命值）
  - 游戏时间戳

---

## 项目结构

```
potatoadven/
├── pom.xml                                 # Maven配置文件
├── README.md                               # 本文档
├── .gitignore                              # Git忽略文件
├── Docs/
│   └── 需求文档.md                         # 完整需求文档
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── potatoadventure/
│                   ├── Main.java           # 主程序入口
│                   ├── core/
│                   │   └── GameEngine.java # 游戏引擎（核心循环）
│                   ├── model/
│                   │   ├── Player.java     # 玩家模型
│                   │   ├── Enemy.java      # 敌人模型
│                   │   └── GameMap.java    # 地图模型
│                   ├── service/
│                   │   ├── BattleService.java      # 战斗服务
│                   │   └── SaveLoadService.java    # 存档服务
│                   └── util/
│                       └── InputHandler.java       # 输入处理
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
mvn exec:java -Dexec.mainClass="com.potatoadventure.Main"
```

### 问题 2: JavaFX 相关错误（如 "Error: JavaFX runtime components are missing"）

**原因**: 使用的 JDK 版本不包含 JavaFX

**解决方案**:
1. 确认使用的是 Oracle JDK 8（内置 JavaFX）
2. 或者切换到包含 JavaFX 的 JDK 发行版（如 Liberica JDK、Azul Zulu FX）

### 问题 3: SQLite 数据库错误

**解决方案**:
```bash
# 删除旧的存档文件
rm -rf saves/save.db  # Linux/macOS
del saves\save.db     # Windows

# 重新运行游戏，会自动创建新的数据库
```

### 问题 4: Maven 下载依赖失败

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

- **编程语言**: Java SE 8
- **UI框架**: JavaFX 8
- **数据库**: SQLite 3.45.0
- **构建工具**: Apache Maven 3.6.3
- **JDBC驱动**: xerial sqlite-jdbc 3.45.0.0

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
