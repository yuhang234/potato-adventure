# 土豆冒险 (Potato Adventure)

一款纯Java实现的桌面冒险游戏，采用JavaFX图形界面，所有图形资源通过代码生成。

## 项目简介

**土豆冒险**是一个轻量级、可扩展的Java游戏学习项目，适合Java程序设计学习者。游戏采用纯代码生成所有图形资源，无需外部美术素材。

## 技术栈

- **语言**: Java SE 17
- **UI框架**: JavaFX 21.0.2
- **构建工具**: Maven 3.9.6
- **数据库**: SQLite 3.45.0
- **序列化**: Jackson 2.16.1

## 核心功能

- ✅ 多场景地图探索系统
- ✅ 回合制/实时战斗系统
- ✅ 丰富的物品装备系统
- ✅ 任务系统（主线、支线、日常）
- ✅ NPC交互与对话系统
- ✅ 技能树与角色成长
- ✅ 存档加载系统
- ✅ 纯代码生成图形资源

## 快速开始

### 环境要求

- JDK 17 或更高版本
- Maven 3.6+

### 编译运行

```bash
# 克隆项目
git clone https://github.com/yourusername/potato-adventure.git

# 进入项目目录
cd potato-adventure

# 编译项目
mvn clean compile

# 运行游戏
mvn javafx:run

# 打包
mvn clean package
```

### 开发模式

```bash
# 运行测试
mvn test

# 代码检查
mvn checkstyle:check

# 生成文档
mvn javadoc:javadoc
```

## 项目结构

```
potato-adventure/
├── src/main/java/com/game/potato/
│   ├── core/           # 核心引擎
│   ├── world/          # 世界系统
│   ├── entity/         # 实体系统
│   ├── character/      # 角色系统
│   ├── combat/         # 战斗系统
│   ├── inventory/      # 物品系统
│   ├── quest/          # 任务系统
│   ├── graphics/       # 图形生成
│   ├── ui/             # 用户界面
│   └── util/           # 工具类
├── docs/               # 项目文档
└── pom.xml
```

## 游戏玩法

- **WASD/方向键**: 移动角色
- **空格**: 互动/确认
- **E**: 打开背包
- **C**: 角色面板
- **Q**: 任务日志
- **M**: 地图
- **ESC**: 菜单

## 开发文档

详细的开发文档请查看：
- [需求文档](Docs/需求文档.md)
- [实现指南](Docs/实现指南-开发人员版.md)

## 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: 添加某个功能'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 联系方式

项目链接: [https://github.com/yourusername/potato-adventure](https://github.com/yourusername/potato-adventure)

---

Made with ❤️ and 🥔
