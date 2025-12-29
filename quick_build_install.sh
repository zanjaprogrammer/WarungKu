#!/bin/bash

# Quick Build & Install Script
# Use this when emulator is already running

set -e

echo "⚡ Quick Build & Install for WarungKu"
echo "===================================="

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
ADB_PATH="$HOME/Library/Android/sdk/platform-tools/adb"
JAVA_HOME="/opt/homebrew/opt/openjdk@17"

# Check if emulator is running
if ! "$ADB_PATH" devices | grep -q "emulator"; then
    echo -e "${RED}❌ No emulator detected${NC}"
    echo -e "${YELLOW}💡 Start emulator first or use: ./run_app_with_emulator.sh${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Emulator detected${NC}"

# Build the app
echo -e "${BLUE}🔨 Building the app...${NC}"
export JAVA_HOME="$JAVA_HOME"
if ! ./gradlew assembleDebug; then
    echo -e "${RED}❌ Build failed${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Build successful!${NC}"

# Install the app
echo -e "${BLUE}📦 Installing app on emulator...${NC}"
if ! "$ADB_PATH" install -r app/build/outputs/apk/debug/app-debug.apk; then
    echo -e "${RED}❌ Installation failed${NC}"
    exit 1
fi

echo -e "${GREEN}✅ App installed successfully!${NC}"

# Launch the app
echo -e "${BLUE}🚀 Launching WarungKu app...${NC}"
if ! "$ADB_PATH" shell am start -n com.zanjaprogrammer.warungku/.MainActivity; then
    echo -e "${RED}❌ Failed to launch app${NC}"
    exit 1
fi

echo -e "${GREEN}🎉 WarungKu app is now running!${NC}"
echo -e "${BLUE}💡 Test the new product image features!${NC}"