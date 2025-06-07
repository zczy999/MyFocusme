#!/bin/bash

# MyFocusme Mac应用打包脚本 - 最小内存版本

set -e

echo "🚀 开始打包 MyFocusme Mac 应用 (最小内存版本)..."

# 项目信息
APP_NAME="MyFocusme"
APP_VERSION="1.0.0"
MAIN_CLASS="com.tsymq.Launcher"
VENDOR="tsymq"
PACKAGE_ID="com.tsymq.myfocusme"

# 目录设置
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET_DIR="$PROJECT_DIR/target"
PACKAGE_DIR="$TARGET_DIR/package-minimal"
APP_DIR="$PACKAGE_DIR/$APP_NAME.app"

echo "📁 项目目录: $PROJECT_DIR"

# 清理之前的构建
echo "🧹 清理之前的构建..."
rm -rf "$PACKAGE_DIR"
mkdir -p "$PACKAGE_DIR"

# 构建项目
echo "🔨 构建项目..."
cd "$PROJECT_DIR"
mvn clean package -q

# 检查构建结果
if [ ! -f "$TARGET_DIR/$APP_NAME-1.0.0-fat.jar" ]; then
    echo "❌ 构建失败：找不到 fat jar 文件"
    exit 1
fi

echo "✅ 项目构建完成"

# 准备图标文件
ICON_FILE="$PROJECT_DIR/src/main/resources/myfocus.png"
if [ ! -f "$ICON_FILE" ]; then
    echo "⚠️  警告：找不到图标文件 $ICON_FILE"
    ICON_OPTION=""
else
    echo "🎨 使用图标文件: $ICON_FILE"
    ICON_OPTION="--icon $ICON_FILE"
fi

# 创建临时输入目录，只包含必要的文件
TEMP_INPUT_DIR="$TARGET_DIR/jpackage_input_minimal"
rm -rf "$TEMP_INPUT_DIR"
mkdir -p "$TEMP_INPUT_DIR"

# 复制fat jar到临时目录
cp "$TARGET_DIR/$APP_NAME-1.0.0-fat.jar" "$TEMP_INPUT_DIR/"

# 使用 jpackage 创建 Mac 应用 - 最小内存配置
echo "📦 使用 jpackage 创建 Mac 应用 (最小内存配置)..."

jpackage \
    --input "$TEMP_INPUT_DIR" \
    --name "$APP_NAME" \
    --main-jar "$APP_NAME-1.0.0-fat.jar" \
    --main-class "$MAIN_CLASS" \
    --type app-image \
    --dest "$PACKAGE_DIR" \
    --vendor "$VENDOR" \
    --app-version "$APP_VERSION" \
    --mac-package-identifier "$PACKAGE_ID" \
    --mac-package-name "$APP_NAME" \
    --java-options "-Dfile.encoding=UTF-8" \
    --java-options "-Djava.awt.headless=false" \
    --java-options "-Xms8m" \
    --java-options "-Xmx64m" \
    --java-options "-XX:MaxMetaspaceSize=48m" \
    --java-options "-XX:+UseSerialGC" \
    --java-options "-XX:MaxNewSize=16m" \
    --java-options "-XX:SurvivorRatio=8" \
    --java-options "-XX:+UseCompressedOops" \
    --java-options "-XX:+UseCompressedClassPointers" \
    --java-options "-XX:+DisableExplicitGC" \
    --java-options "-XX:ReservedCodeCacheSize=16m" \
    --java-options "-XX:InitialCodeCacheSize=8m" \
    --java-options "-XX:+TieredCompilation" \
    --java-options "-XX:TieredStopAtLevel=1" \
    --java-options "-XX:-UseParallelGC" \
    --java-options "-XX:MinHeapFreeRatio=10" \
    --java-options "-XX:MaxHeapFreeRatio=20" \
    --java-options "-XX:+UseStringDeduplication" \
    --java-options "-XX:StringTableSize=1009" \
    --java-options "-XX:+OptimizeStringConcat" \
    --java-options "-XX:+UseCodeCacheFlushing" \
    --java-options "-XX:CompileThreshold=1500" \
    --java-options "-Dswing.aatext=true" \
    --java-options "-Dswing.plaf.metal.controlFont=Dialog-12" \
    --java-options "-Dswing.plaf.metal.userFont=Dialog-12" \
    --java-options "-Djavafx.animation.pulse=60" \
    --java-options "-Dprism.dirtyopts=false" \
    --java-options "-Dprism.vsync=false" \
    --java-options "-Djavafx.animation.fullspeed=true" \
    --java-options "-Dprism.poolstats=false" \
    --java-options "-Dprism.verbose=false" \
    --java-options "-Dprism.trace=false" \
    --java-options "-Dprism.printallocs=false" \
    $ICON_OPTION

# 检查打包结果
if [ -d "$APP_DIR" ]; then
    echo "✅ Mac 应用打包成功！"
    echo "📍 应用位置: $APP_DIR"
    
    # 显示应用信息
    APP_SIZE=$(du -sh "$APP_DIR" | cut -f1)
    echo "📊 应用大小: $APP_SIZE"
    
    # 创建 DMG 文件（可选）
    echo "💿 创建 DMG 安装包..."
    DMG_NAME="$APP_NAME-$APP_VERSION-minimal.dmg"
    DMG_PATH="$PACKAGE_DIR/$DMG_NAME"
    
    # 删除已存在的 DMG
    rm -f "$DMG_PATH"
    
    # 创建临时 DMG 目录
    DMG_TEMP_DIR="$TARGET_DIR/dmg_temp_minimal"
    rm -rf "$DMG_TEMP_DIR"
    mkdir -p "$DMG_TEMP_DIR"
    
    # 复制应用到临时目录
    cp -R "$APP_DIR" "$DMG_TEMP_DIR/"
    
    # 创建 Applications 链接
    ln -s /Applications "$DMG_TEMP_DIR/Applications"
    
    # 创建 DMG
    hdiutil create -volname "$APP_NAME" -srcfolder "$DMG_TEMP_DIR" -ov -format UDZO "$DMG_PATH"
    
    if [ -f "$DMG_PATH" ]; then
        DMG_SIZE=$(du -sh "$DMG_PATH" | cut -f1)
        echo "✅ DMG 创建成功！"
        echo "📍 DMG 位置: $DMG_PATH"
        echo "📊 DMG 大小: $DMG_SIZE"
    else
        echo "⚠️  DMG 创建失败"
    fi
    
    # 清理临时目录
    rm -rf "$DMG_TEMP_DIR"
    rm -rf "$TEMP_INPUT_DIR"
    
    echo ""
    echo "🎉 打包完成！"
    echo "📱 可以在 Finder 中打开 $PACKAGE_DIR 查看结果"
    echo "🚀 双击 $APP_NAME.app 运行应用"
    echo ""
    echo "⚡ 内存优化说明："
    echo "   - 初始堆内存: 8MB"
    echo "   - 最大堆内存: 64MB"
    echo "   - 使用Serial GC (最小内存占用)"
    echo "   - 优化的JVM参数配置"
    echo ""
    echo "💡 如果应用运行缓慢，请使用标准版本的打包脚本"
    
    # 打开 Finder 显示结果
    open "$PACKAGE_DIR"
    
else
    echo "❌ Mac 应用打包失败"
    exit 1
fi 