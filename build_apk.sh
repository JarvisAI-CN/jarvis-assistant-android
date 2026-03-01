#!/bin/bash

# 贾维斯助手APK本地构建脚本
# 这个脚本会帮助您在本地构建APK文件

set -e

echo "🚀 开始构建贾维斯助手APK..."
echo "========================================"

# 检查必要的环境
echo "🔍 检查构建环境..."

# 检查Java
if ! command -v java &> /dev/null; then
    echo "❌ Java未安装，请先安装JDK 11或更高版本"
    exit 1
fi

echo "✅ Java版本: $(java -version 2>&1 | head -n 1)"

# 检查Android SDK
if [ -z "$ANDROID_HOME" ]; then
    echo "⚠️  ANDROID_HOME环境变量未设置"
    echo "正在尝试自动设置..."
    export ANDROID_HOME=$HOME/Android/Sdk
fi

if [ ! -d "$ANDROID_HOME" ]; then
    echo "❌ Android SDK未找到"
    echo "请安装Android SDK并设置ANDROID_HOME环境变量"
    exit 1
fi

echo "✅ Android SDK: $ANDROID_HOME"

# 检查Gradle
if [ -f "./gradlew" ]; then
    echo "✅ Gradle wrapper已找到"
    chmod +x ./gradlew
else
    echo "❌ Gradle wrapper未找到"
    exit 1
fi

# 开始构建
echo ""
echo "🔨 开始构建Debug APK..."
echo "========================================"

# 清理旧的构建文件
echo "🧹 清理旧构建文件..."
./gradlew clean

# 构建Debug APK
echo "📱 构建APK文件..."
./gradlew :app:assembleDebug --stacktrace

# 检查构建结果
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo ""
    echo "🎉 构建成功！"
    echo "========================================"
    echo "📦 APK文件位置:"
    echo "   $(pwd)/app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "📱 安装方法:"
    echo "   1. 将APK文件传输到您的Android设备"
    echo "   2. 在设备上启用'未知来源应用'安装"
    echo "   3. 点击APK文件进行安装"
    echo ""
    echo "🔧 安装命令(通过ADB):"
    echo "   adb install app/build/outputs/apk/debug/app-debug.apk"
    echo ""

    # 显示APK文件信息
    APK_SIZE=$(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)
    echo "📊 APK文件大小: $APK_SIZE"

    # 获取当前时间戳
    TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    APK_BACKUP="jarvis-assistant_${TIMESTAMP}.apk"

    # 创建备份副本
    cp app/build/outputs/apk/debug/app-debug.apk "$APK_BACKUP"
    echo "💾 备份副本: $APK_BACKUP"

    echo ""
    echo "🚀 构建完成！您现在可以安装APK了。"
else
    echo ""
    echo "❌ 构建失败，请检查错误信息"
    exit 1
fi
