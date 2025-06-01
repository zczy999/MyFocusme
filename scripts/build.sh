#!/bin/bash

# MyFocusme 构建脚本

echo "开始构建 MyFocusme 项目..."

# 清理之前的构建
echo "清理之前的构建..."
mvn clean

# 编译项目
echo "编译项目..."
mvn compile

# 运行测试
echo "运行测试..."
mvn test

# 打包项目
echo "打包项目..."
mvn package

echo "构建完成！"
echo "可执行文件位置: target/MyFocusme-1.0-SNAPSHOT.jar" 