@echo off
REM 土豆冒险游戏 - 打包脚本
REM 将游戏打包为可执行 JAR 文件

echo ========================================
echo 土豆冒险游戏 - 打包工具
echo ========================================
echo.

echo [1/2] 清理旧文件...
mvn clean
if errorlevel 1 (
    echo [错误] 清理失败！
    pause
    exit /b 1
)
echo [√] 清理完成
echo.

echo [2/2] 打包项目...
mvn package
if errorlevel 1 (
    echo [错误] 打包失败！
    pause
    exit /b 1
)
echo [√] 打包完成
echo.

echo ========================================
echo 打包成功！
echo ========================================
echo.
echo 可执行文件位置：
echo target\potato-adventure-1.0.0-jar-with-dependencies.jar
echo.
echo 运行方法：
echo java -jar target\potato-adventure-1.0.0-jar-with-dependencies.jar
echo.

pause
