@echo off
REM 土豆冒险游戏 - Windows启动脚本
REM 运行前确保已安装 JDK 8 和 Maven

echo ========================================
echo 土豆冒险游戏 - Potato Adventure
echo ========================================
echo.

REM 检查 Java 环境
echo [1/3] 检查 Java 环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Java 环境！
    echo 请先安装 JDK 8 或更高版本
    echo 下载地址: https://www.oracle.com/java/technologies/javase-downloads.html
    pause
    exit /b 1
)
echo [√] Java 环境检测成功
echo.

REM 检查 Maven 环境
echo [2/3] 检查 Maven 环境...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Maven！
    echo 请先安装 Apache Maven
    echo 下载地址: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)
echo [√] Maven 环境检测成功
echo.

REM 编译并运行游戏
echo [3/3] 启动游戏...
echo.
mvn clean compile exec:java -Dexec.mainClass="com.potatoadventure.Main"

if errorlevel 1 (
    echo.
    echo [错误] 游戏启动失败！
    echo 请检查错误信息并参考 README.md 中的故障排除章节
    pause
    exit /b 1
)

pause
