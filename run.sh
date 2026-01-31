#!/bin/bash
# 土豆冒险游戏 - Linux/macOS启动脚本
# 运行前确保已安装 JDK 8 和 Maven

echo "========================================"
echo "土豆冒险游戏 - Potato Adventure"
echo "========================================"
echo ""

# 检查 Java 环境
echo "[1/3] 检查 Java 环境..."
if ! command -v java &> /dev/null; then
    echo "[错误] 未检测到 Java 环境！"
    echo "请先安装 JDK 8 或更高版本"
    echo "Ubuntu/Debian: sudo apt install openjdk-8-jdk"
    echo "macOS: brew install adoptopenjdk8"
    exit 1
fi
echo "[√] Java 环境检测成功"
echo ""

# 检查 Maven 环境
echo "[2/3] 检查 Maven 环境..."
if ! command -v mvn &> /dev/null; then
    echo "[错误] 未检测到 Maven！"
    echo "请先安装 Apache Maven"
    echo "Ubuntu/Debian: sudo apt install maven"
    echo "macOS: brew install maven"
    exit 1
fi
echo "[√] Maven 环境检测成功"
echo ""

# 编译并运行游戏
echo "[3/3] 启动游戏..."
echo ""
mvn clean compile exec:java -Dexec.mainClass="com.potatoadventure.Main"

if [ $? -ne 0 ]; then
    echo ""
    echo "[错误] 游戏启动失败！"
    echo "请检查错误信息并参考 README.md 中的故障排除章节"
    exit 1
fi
