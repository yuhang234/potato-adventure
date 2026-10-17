# 土豆冒险游戏 - 完整项目结构

> **M1 更新（2026-10-17）**：项目已升级到 Java 21 + OpenJFX 21，并新增 `core/Camera.java`、
> `core/GameState.java`、`core/RetreatPlanner.java`、`ui/Palette.java`、`src/test/**` 等文件；
> 最新目录树请见 [README.md](README.md) 的“项目结构”一节。

```
potatoadven/
│
├── 📄 pom.xml                          # Maven项目配置文件
│   ├── 项目信息（groupId, artifactId, version）
│   ├── Java 21 + OpenJFX 21 编译配置
│   ├── 依赖管理（JavaFX 21 / SQLite JDBC / SLF4J / JUnit 5）
│   └── 打包插件配置（Assembly Plugin）
│
├── 📄 .gitignore                       # Git版本控制忽略配置
│   ├── 忽略编译输出（target/）
│   ├── 忽略IDE配置（.idea/, *.iml）
│   └── 忽略存档文件（saves/, *.db）
│
├── 📄 README.md                        # 完整使用文档（450+行）
│   ├── 项目简介和核心特性
│   ├── 环境要求和搭建指南
│   ├── 快速开始教程
│   ├── 游戏操作说明
│   ├── 游戏系统详解
│   ├── 项目结构说明
│   ├── 故障排除指南
│   └── 更新日志
│
├── 📄 QUICKSTART.md                    # 快速开始指南（120行）
│   ├── 前置要求检查
│   ├── 三步快速启动
│   ├── 游戏操作速查表
│   └── 常见问题解答
│
├── 📄 PROJECT_SUMMARY.md               # 项目总览（280行）
│   ├── 已完成功能清单
│   ├── 完整文件结构
│   ├── 核心玩法实现
│   ├── 技术实现细节
│   └── 项目统计数据
│
├── 📄 DELIVERY.md                      # 完整交付文档（600+行）
│   ├── 核心功能实现清单
│   ├── 完整文件清单
│   ├── 完整游戏体验流程
│   ├── 技术实现细节
│   ├── 验收清单
│   └── 学习建议
│
├── 📄 run.bat                          # Windows启动脚本
│   ├── 自动检查Java环境
│   ├── 自动检查Maven环境
│   └── 编译并运行游戏
│
├── 📄 run.sh                           # Linux/macOS启动脚本
│   ├── Bash脚本语法
│   ├── 环境检查
│   └── 一键启动
│
├── 📄 build.bat                        # Windows打包脚本
│   ├── 清理旧文件（mvn clean）
│   └── 打包为JAR（mvn package）
│
├── 📁 Docs/                            # 文档目录
│   └── 📄 需求文档.md                  # 完整需求文档（570+行）
│       ├── 核心功能模块
│       ├── 系统功能
│       ├── 技术实现要求
│       ├── 美术与音效要求
│       └── 可选扩展功能
│
├── 📁 src/                             # 源代码目录
│   └── 📁 main/
│       └── 📁 java/
│           └── 📁 com/
│               └── 📁 potatoadventure/
│                   │
│                   ├── 📄 Main.java      # 主程序入口（57行）
│                   │
│                   ├── 📁 core/          # 核心引擎包
│                   │   ├── 📄 Camera.java         # 平滑滚屏摄像机（158行）
│                   │   ├── 📄 GameEngine.java     # 游戏引擎（1161行）
│                   │   ├── 📄 GameState.java      # 游戏状态枚举（26行）
│                   │   └── 📄 RetreatPlanner.java # 逃跑落点规划（101行）
│                   │
│                   ├── 📁 model/         # 数据模型包
│                   │   ├── 📄 Enemy.java           # 敌人模型（232行）
│                   │   ├── 📄 GameMap.java         # 地图模型：60×45格（614行）
│                   │   └── 📄 Player.java          # 玩家模型（393行）
│                   │
│                   ├── 📁 service/       # 业务服务包
│                   │   ├── 📄 BattleService.java   # 战斗服务（294行）
│                   │   └── 📄 SaveLoadService.java # 存档/读档服务（404行）
│                   │
│                   ├── 📁 ui/            # 界面包
│                   │   ├── 📄 Assets.java           # 美术资产加载入口（236行）
│                   │   ├── 📄 BattleScreen.java     # 战斗界面（287行）
│                   │   ├── 📄 CharSelectScreen.java # 角色创建界面（317行）
│                   │   ├── 📄 OverworldScreen.java  # 大地图探索 HUD（359行）
│                   │   ├── 📄 Palette.java          # 调色板常量（81行）
│                   │   ├── 📄 ResultScreens.java    # 结算/战败界面（159行）
│                   │   ├── 📄 Sprites.java          # 实体精灵绘制（106行）
│                   │   ├── 📄 TitleScreen.java      # 主菜单（92行）
│                   │   ├── 📄 UiKit.java            # 像素 UI 工具箱（286行）
│                   │   └── 📄 WeatherParticles.java # 环境粒子层（144行）
│                   │
│                   └── 📁 util/          # 工具类包
│                       └── 📄 InputHandler.java      # 输入处理/按键缓冲（130行）
│
├── 📁 target/                          # Maven编译输出（自动生成）
│   ├── classes/                        # 编译后的.class文件
│   ├── maven-archiver/                 # Maven打包信息
│   └── potato-adventure-2.0.0-M1-jar-with-dependencies.jar  # 可执行JAR
│
└── 📁 saves/                           # 存档目录（自动生成）
    └── save.db                         # SQLite存档数据库
        ├── player表（玩家数据）
        ├── enemy表（敌人数据）
        ├── map_meta表（地图元信息）
        └── map_tile表（全部地块）
```

---

## 📊 项目统计概览

### 代码统计

| 文件 | 路径 | 行数 | 类型 | 说明 |
|------|------|------|------|------|
| Main.java | com.potatoadventure | 57 | 入口 | 程序主入口 |
| Camera.java | core | 158 | 核心 | 平滑滚屏摄像机 |
| GameEngine.java | core | 1161 | 核心 | 游戏引擎 |
| GameState.java | core | 26 | 核心 | 游戏状态枚举 |
| RetreatPlanner.java | core | 101 | 核心 | 逃跑落点规划 |
| Enemy.java | model | 232 | 模型 | 敌人系统 |
| GameMap.java | model | 614 | 模型 | 地图系统（60×45） |
| Player.java | model | 393 | 模型 | 玩家角色 |
| BattleService.java | service | 294 | 服务 | 战斗逻辑 |
| SaveLoadService.java | service | 404 | 服务 | 存档系统 |
| Assets.java | ui | 236 | 界面 | 美术资产加载入口 |
| BattleScreen.java | ui | 287 | 界面 | 战斗界面 |
| CharSelectScreen.java | ui | 317 | 界面 | 角色创建界面 |
| OverworldScreen.java | ui | 359 | 界面 | 大地图探索 HUD |
| Palette.java | ui | 81 | 界面 | 调色板常量 |
| ResultScreens.java | ui | 159 | 界面 | 结算/战败界面 |
| Sprites.java | ui | 106 | 界面 | 实体精灵绘制 |
| TitleScreen.java | ui | 92 | 界面 | 主菜单 |
| UiKit.java | ui | 286 | 界面 | 像素 UI 工具箱 |
| WeatherParticles.java | ui | 144 | 界面 | 环境粒子层 |
| InputHandler.java | util | 130 | 工具 | 输入处理 |
| **总计** | - | **5,637** | - | - |

### 文档统计

| 文件 | 行数 | 说明 |
|------|------|------|
| README.md | 450+ | 完整使用文档 |
| QUICKSTART.md | 120 | 快速开始指南 |
| PROJECT_SUMMARY.md | 280 | 项目总览 |
| DELIVERY.md | 600+ | 交付文档 |
| 需求文档.md | 570+ | 需求规格 |
| **总计** | **2,020+** | - |

### 功能统计

| 功能模块 | 数量 | 说明 |
|----------|------|------|
| 游戏状态 | 7种 | 菜单/选角/游戏中/暂停/战斗/胜利/结束 |
| 敌人类型 | 3种 | 史莱姆/哥布林/野狼 |
| 战斗技能 | 3个 | 火球术/治疗术/冰冻术 |
| 地形类型 | 4种 | 草地/墙壁/树木/水域 |
| 操作按键 | 12个 | WASD/空格/数字/功能键 |
| 数据库表 | 4个 | player/enemy/map_meta/map_tile |

---

## 🎯 核心类关系图

```
┌─────────────────────────────────────────────────────────┐
│                       Main.java                          │
│                    (JavaFX Entry)                        │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ 创建并启动
                     ▼
┌─────────────────────────────────────────────────────────┐
│                   GameEngine.java                        │
│              (Core Game Loop & Rendering)                │
├─────────────────────────────────────────────────────────┤
│ • AnimationTimer (60 FPS)                               │
│ • update() - 逻辑更新                                   │
│ • render() - 图形渲染                                   │
│ • handleInput() - 输入处理                              │
└──┬───────┬──────────┬──────────┬────────────┬──────────┘
   │       │          │          │            │
   │       │          │          │            │
   ▼       ▼          ▼          ▼            ▼
┌─────┐ ┌─────┐  ┌────────┐ ┌─────────┐ ┌──────────┐
│Player│ │Enemy│  │GameMap │ │Battle   │ │SaveLoad  │
│.java │ │.java│  │.java   │ │Service  │ │Service   │
└─────┘ └─────┘  └────────┘ └─────────┘ └──────────┘
   │       │          │          │            │
   └───────┴──────────┴──────────┴────────────┘
              数据模型和服务层

┌─────────────────────────────────────────────────────────┐
│                  InputHandler.java                       │
│                  (Input Processing)                      │
└─────────────────────────────────────────────────────────┘
```

---

## 📦 Maven依赖树

```
potato-adventure (com.potatoadventure:potato-adventure:jar:2.0.0-M1)
├── org.openjfx:javafx-controls:jar:21.0.2
├── org.openjfx:javafx-graphics:jar:21.0.2
├── org.openjfx:javafx-media:jar:21.0.2
├── org.xerial:sqlite-jdbc:jar:3.45.1.0
├── org.slf4j:slf4j-api:jar:2.0.12
├── org.slf4j:slf4j-simple:jar:2.0.12 (runtime)
└── org.junit.jupiter:junit-jupiter:jar:5.10.2 (test)
```

---

## 🎮 游戏数据流

```
用户输入
   │
   ▼
InputHandler ──────► 按键状态检测
   │
   ▼
GameEngine.update()
   │
   ├──► 移动处理 ──────► GameMap.isCollision() ──► Player.move()
   │
   ├──► 战斗检测 ──────► 玩家与敌人位置重叠
   │                        │
   │                        ▼
   │                   进入战斗模式
   │                        │
   │                        ▼
   │                   BattleService
   │                        │
   │                        ├──► playerAttack()
   │                        ├──► playerSkill()
   │                        └──► enemyAttack()
   │
   ├──► 存档操作 ──────► SaveLoadService
   │                        │
   │                        ├──► saveGame() ──► SQLite写入
   │                        └──► loadGame() ──► SQLite读取
   │
   └──► 渲染更新
         │
         ▼
GameEngine.render()
   │
   ├──► 渲染地图 (GameMap.render())
   ├──► 渲染敌人 (Enemy.render())
   ├──► 渲染玩家 (Player.render())
   └──► 渲染UI (HUD/战斗界面)
         │
         ▼
      JavaFX Canvas
         │
         ▼
     显示到屏幕
```

---

## 💾 数据库设计

### player表结构

```sql
CREATE TABLE player (
    id INTEGER PRIMARY KEY AUTOINCREMENT,  -- 主键
    name TEXT NOT NULL,                    -- 角色名称
    x INTEGER NOT NULL,                    -- X坐标
    y INTEGER NOT NULL,                    -- Y坐标
    level INTEGER NOT NULL,                -- 等级
    exp INTEGER NOT NULL,                  -- 经验值
    hp INTEGER NOT NULL,                   -- 当前HP
    max_hp INTEGER NOT NULL,               -- 最大HP
    mp INTEGER NOT NULL,                   -- 当前MP
    max_mp INTEGER NOT NULL,               -- 最大MP
    attack INTEGER NOT NULL,               -- 攻击力
    defense INTEGER NOT NULL,              -- 防御力
    speed INTEGER NOT NULL,                -- 速度
    save_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 保存时间
);
```

### enemy表结构

```sql
CREATE TABLE enemy (
    id INTEGER PRIMARY KEY AUTOINCREMENT,  -- 主键
    save_id INTEGER NOT NULL,              -- 关联的存档ID
    name TEXT NOT NULL,                    -- 敌人名称
    x INTEGER NOT NULL,                    -- X坐标
    y INTEGER NOT NULL,                    -- Y坐标
    level INTEGER NOT NULL,                -- 等级
    hp INTEGER NOT NULL,                   -- 当前HP
    max_hp INTEGER NOT NULL,               -- 最大HP
    attack INTEGER NOT NULL,               -- 攻击力
    defense INTEGER NOT NULL,              -- 防御力
    exp_reward INTEGER NOT NULL,           -- 经验奖励
    is_alive INTEGER NOT NULL              -- 是否存活（0/1）
);
```

### map_meta表结构

```sql
CREATE TABLE map_meta (
    save_id INTEGER PRIMARY KEY,           -- 关联的存档ID
    width INTEGER NOT NULL,                -- 地图宽度（格）
    height INTEGER NOT NULL,               -- 地图高度（格）
    seed INTEGER NOT NULL,                 -- 地图随机种子
    tile_count INTEGER NOT NULL            -- 地块总数
);
```

### map_tile表结构

```sql
CREATE TABLE map_tile (
    save_id INTEGER NOT NULL,              -- 关联的存档ID
    x INTEGER NOT NULL,                    -- 地块X坐标
    y INTEGER NOT NULL,                    -- 地块Y坐标
    tile INTEGER NOT NULL,                 -- 地块类型
    PRIMARY KEY (save_id, x, y)            -- 复合主键
);
```

---

## 🏗️ 设计模式应用

| 设计模式 | 应用位置 | 说明 |
|----------|----------|------|
| 单例模式 | GameEngine | 游戏引擎唯一实例 |
| 工厂模式 | Enemy.java | 创建不同类型敌人 |
| 状态模式 | GameEngine.GameState | 游戏状态切换 |
| 策略模式 | BattleService | 不同技能实现 |
| MVC模式 | 整体架构 | Model-View-Controller分离 |

---

**本文档展示了《土豆冒险游戏》的完整项目结构！** 🥔✨
