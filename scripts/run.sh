#!/bin/bash

# MyFocusme 运行脚本

echo "启动 MyFocusme 应用..."

# 检查 Java 版本
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "当前 Java 版本: $java_version"

# 检查是否已构建
if [ ! -f "target/MyFocusme-1.0-SNAPSHOT.jar" ]; then
    echo "未找到构建文件，正在构建项目..."
    ./scripts/build.sh
fi

# 运行应用
echo "启动应用..."
mvn javafx:run 