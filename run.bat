@echo off
setlocal
REM 土豆冒险游戏 - Windows 启动脚本（M1 起）
REM
REM 要求：JDK 21 或更高版本（必须是完整 JDK，带 javac.exe）+ Apache Maven 3.6+
REM JavaFX 21 由 Maven 自动下载（pom.xml 中的 openjfx 依赖），无需单独安装 JavaFX SDK。

set MIN_JAVA_MAJOR=21

echo ========================================
echo 土豆冒险游戏 - Potato Adventure
echo ========================================
echo.

REM ---------- 检查 Java ----------
echo [1/3] 检查 Java 环境（需要 JDK 21+，必须带 javac）...
if not "%JAVA_HOME%"=="" (
    if exist "%JAVA_HOME%\bin\javac.exe" goto java_ok
    echo [警告] JAVA_HOME 指向的目录没有 javac.exe（可能只装了 JRE）: %JAVA_HOME%
)

where javac >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 JDK 21+ 的 javac.exe！
    echo.
    echo 安装建议：
    echo   https://adoptium.net/temurin/releases/?version=21
    echo   或   winget install EclipseAdoptium.Temurin.21.JDK
    echo.
    echo 提示：如果已经安装，请设置系统环境变量 JAVA_HOME 指向 JDK 目录，例如
    echo   set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.x
    pause
    exit /b 1
)

REM 从 javac 的路径反推 JDK 根目录（javac.exe 位于 %JAVA_HOME%\bin\ 下）
for /f "delims=" %%i in ('where javac') do (
    set "JAVAC_PATH=%%i"
    goto java_found
)
:java_found
for %%i in ("%JAVAC_PATH%") do set "JAVAC_DIR=%%~dpi"
for %%i in ("%JAVAC_DIR:~0,-1%") do set "JAVA_HOME=%%~dpi"
set "JAVA_HOME=%JAVA_HOME:~0,-1%"

:java_ok
"%JAVA_HOME%\bin\java" -version 2>&1 | findstr /r /c:"version \"2[1-9]" /c:"version \"[3-9][0-9]" >nul
if errorlevel 1 (
    echo [警告] JAVA_HOME 中的 Java 主版本可能低于 21，本项目要求 JDK 21+。
    echo         当前版本：
    "%JAVA_HOME%\bin\java" -version
    echo.
    pause
    exit /b 1
)
echo [√] Java 环境检测成功
echo.

REM ---------- 检查 Maven ----------
echo [2/3] 检查 Maven 环境...
where mvn >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Maven！
    echo 下载地址: https://maven.apache.org/download.cgi
    echo 或:       winget install Apache.Maven
    pause
    exit /b 1
)
echo [√] Maven 环境检测成功
echo.

REM ---------- 编译并运行 ----------
echo [3/3] 编译并启动游戏（首次运行需要联网下载 JavaFX/SQLite 依赖）...
echo.
cd /d "%~dp0"
call mvn -q clean compile javafx:run

if errorlevel 1 (
    echo.
    echo [错误] 游戏启动失败！
    echo 请检查上面的错误信息，并参考 README.md 的“故障排除”章节
    pause
    exit /b 1
)

endlocal
