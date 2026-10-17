#!/bin/bash
# 土豆冒险 - Linux/macOS 启动脚本（M1 起）
#
# 要求：JDK 21 或更高版本（必须是完整 JDK，带 javac）+ Apache Maven 3.6+
# JavaFX 21 由 Maven 自动下载（pom.xml 中的 openjfx 依赖），不需要单独安装 SDK。
#
# 用法：./run.sh   （或 bash run.sh）
# 作用：定位一个可用的 JDK -> export JAVA_HOME -> mvn clean compile javafx:run

set -u

MIN_JAVA_MAJOR=21
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "========================================"
echo "土豆冒险游戏 - Potato Adventure"
echo "========================================"
echo ""

# ---------------------------------------------------------------- 工具函数

# 打印某个 java 可执行文件的主版本号（失败则输出空）
java_major() {
    local java_bin="$1"
    [ -x "$java_bin" ] || return 1
    local raw
    raw="$("$java_bin" -version 2>&1 | head -1)"
    # 形如：openjdk version "21.0.12" 2025-01-21 / java version "1.8.0_402"
    local version
    version="$(echo "$raw" | sed -n 's/.*version "\([^"]*\)".*/\1/p')"
    [ -n "$version" ] || return 1
    case "$version" in
        1.*) echo "$version" | cut -d. -f2 ;;   # 1.8.0_402 -> 8
        *)   echo "$version" | cut -d. -f1 ;;   # 21.0.12   -> 21
    esac
}

# 判断目录是否是一个 >= MIN_JAVA_MAJOR 的完整 JDK
usable_jdk() {
    local home="$1"
    [ -n "$home" ] || return 1
    [ -x "$home/bin/javac" ] || return 1   # 只带 JRE 的 Java 无法编译，直接跳过
    local major
    major="$(java_major "$home/bin/java")" || return 1
    [ -n "$major" ] || return 1
    [ "$major" -ge "$MIN_JAVA_MAJOR" ] 2>/dev/null
}

# 依次尝试若干候选目录，第一个可用者即为 JAVA_HOME
find_jdk() {
    local candidates=()
    local dir

    # 1) 已显式导出的 JAVA_HOME
    [ -n "${JAVA_HOME:-}" ] && candidates+=("$JAVA_HOME")

    # 2) PATH 上的 java（顺藤摸瓜找到 JDK 根目录）
    if command -v java >/dev/null 2>&1; then
        local resolved
        resolved="$(readlink -f "$(command -v java)" 2>/dev/null)"
        [ -n "$resolved" ] && candidates+=("$(dirname "$(dirname "$resolved")")")
    fi

    # 3) SDKMAN
    if [ -d "$HOME/.sdkman/candidates/java" ]; then
        while IFS= read -r dir; do candidates+=("$dir"); done \
            < <(ls -d "$HOME/.sdkman/candidates/java"/*/ 2>/dev/null | sed 's:/$::')
    fi

    # 4) 发行版常见位置
    while IFS= read -r dir; do candidates+=("$dir"); done \
        < <(ls -d /usr/lib/jvm/*/ 2>/dev/null | sed 's:/$::')
    while IFS= read -r dir; do candidates+=("$dir"); done \
        < <(ls -d /Library/Java/JavaVirtualMachines/*/Contents/Home/ 2>/dev/null | sed 's:/$::')
    while IFS= read -r dir; do candidates+=("$dir"); done \
        < <(ls -d /opt/*jdk*/ /opt/java/*/ 2>/dev/null | sed 's:/$::')
    while IFS= read -r dir; do candidates+=("$dir"); done \
        < <(ls -d "$HOME"/jdk*/ "$HOME"/.jdks/*/ 2>/dev/null | sed 's:/$::')

    for dir in "${candidates[@]}"; do
        if usable_jdk "$dir"; then
            echo "$dir"
            return 0
        fi
    done
    return 1
}

# ---------------------------------------------------------------- 环境检查

echo "[1/3] 检查 Java 环境（需要 JDK ${MIN_JAVA_MAJOR}+，必须带 javac）..."
JDK_HOME="$(find_jdk)"
if [ -z "${JDK_HOME:-}" ]; then
    echo "[错误] 没有找到可用的 JDK ${MIN_JAVA_MAJOR}+（只带运行时的 JRE 不能编译本项目）"
    echo ""
    echo "安装建议："
    echo "  Ubuntu/Debian: sudo apt install openjdk-21-jdk"
    echo "  macOS:         brew install openjdk@21"
    echo "  任意平台:      https://adoptium.net/temurin/releases/?version=21"
    echo ""
    echo "提示：已安装但仍报错时，请显式指定 JAVA_HOME，例如"
    echo "  JAVA_HOME=/path/to/jdk-21 ./run.sh"
    exit 1
fi
export JAVA_HOME="$JDK_HOME"
echo "[√] 使用 JDK $(java_major "$JAVA_HOME/bin/java") : $JAVA_HOME"
echo ""

echo "[2/3] 检查 Maven 环境..."
if ! command -v mvn >/dev/null 2>&1; then
    echo "[错误] 未检测到 Maven！"
    echo "  Ubuntu/Debian: sudo apt install maven"
    echo "  macOS:         brew install maven"
    echo "  其他:          https://maven.apache.org/download.cgi"
    exit 1
fi
echo "[√] Maven 环境检测成功：$(mvn -v 2>/dev/null | head -1)"
echo ""

# ---------------------------------------------------------------- 编译并运行

echo "[3/3] 编译并启动游戏（首次运行需要联网下载 JavaFX/SQLite 依赖）..."
echo ""
cd "$PROJECT_DIR" || exit 1
mvn -q clean compile javafx:run

status=$?
if [ $status -ne 0 ]; then
    echo ""
    echo "[错误] 游戏启动失败（退出码 $status）！"
    echo "请检查上面的错误信息，并参考 README.md 的“故障排除”章节"
    exit $status
fi
