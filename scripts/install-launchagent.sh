#!/bin/bash

# MyFocusme LaunchAgent 安装脚本
# 功能：安装系统级保活服务，实现开机自启动和进程守护

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PLIST_NAME="com.tsymq.myfocusme.plist"
PLIST_SOURCE="$SCRIPT_DIR/launchagent/$PLIST_NAME"
PLIST_DEST="$HOME/Library/LaunchAgents/$PLIST_NAME"
APP_PATH="/Applications/MyFocusme.app"

echo "🔧 MyFocusme LaunchAgent 安装程序"
echo "=================================="

# 检查应用是否已安装
if [ ! -d "$APP_PATH" ]; then
    echo "❌ 错误：MyFocusme.app 未安装到 /Applications/"
    echo "   请先将应用拖入 Applications 文件夹"
    exit 1
fi

# 检查 plist 源文件是否存在
if [ ! -f "$PLIST_SOURCE" ]; then
    echo "❌ 错误：找不到配置文件 $PLIST_SOURCE"
    exit 1
fi

# 创建 LaunchAgents 目录（如果不存在）
mkdir -p "$HOME/Library/LaunchAgents"

# 创建日志目录
mkdir -p "$HOME/.config/myfocusme"

# 如果已存在，先卸载旧的
if [ -f "$PLIST_DEST" ]; then
    echo "📦 检测到已安装的服务，正在卸载..."
    launchctl unload "$PLIST_DEST" 2>/dev/null || true
    rm -f "$PLIST_DEST"
fi

# 复制 plist 文件并替换用户目录占位符
echo "📋 复制配置文件..."
sed "s|__USER_HOME__|$HOME|g" "$PLIST_SOURCE" > "$PLIST_DEST"

# 设置正确的权限
chmod 644 "$PLIST_DEST"

# 加载服务
echo "🚀 加载 LaunchAgent 服务..."
launchctl load "$PLIST_DEST"

# 验证服务是否加载成功
if launchctl list | grep -q "com.tsymq.myfocusme"; then
    echo ""
    echo "✅ LaunchAgent 安装成功！"
    echo ""
    echo "📌 已启用的功能："
    echo "   • 开机/登录后自动启动"
    echo "   • 被关闭后 5 秒内自动重启"
    echo "   • 崩溃后自动恢复"
    echo ""
    echo "📍 配置文件：$PLIST_DEST"
    echo "📝 日志文件：$HOME/.config/myfocusme/launchagent.log"
    echo ""
    echo "💡 提示：使用 ./uninstall-launchagent.sh 可卸载此服务"
else
    echo "❌ 服务加载失败，请检查日志"
    exit 1
fi
