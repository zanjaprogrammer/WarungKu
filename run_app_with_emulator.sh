#!/bin/bash

# WarungKu App Runner Script
# This script will start the emulator, build the app, and install it

set -e  # Exit on any error

echo "🚀 Starting WarungKu App with Emulator..."
echo "========================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
EMULATOR_NAME="Medium_Phone_API_36.0"
ANDROID_SDK_PATH="$HOME/Library/Android/sdk"
EMULATOR_PATH="$ANDROID_SDK_PATH/emulator/emulator"
ADB_PATH="$ANDROID_SDK_PATH/platform-tools/adb"
JAVA_HOME="/opt/homebrew/opt/openjdk@17"

# Function to check camera availability
check_camera() {
    echo -e "${BLUE}📷 Checking camera availability...${NC}"
    if system_profiler SPCameraDataType | grep -q "Camera"; then
        echo -e "${GREEN}✅ Camera detected${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️  No camera detected, barcode scanning may not work${NC}"
        return 1
    fi
}

# Function to check if emulator is running
check_emulator() {
    "$ADB_PATH" devices | grep -q "emulator"
}

# Function to wait for emulator to boot
wait_for_emulator() {
    echo -e "${YELLOW}⏳ Waiting for emulator to boot completely...${NC}"
    echo -e "${BLUE}💡 This may take 2-5 minutes depending on your system${NC}"
    echo -e "${BLUE}💡 Press Ctrl+C if you want to continue manually${NC}"
    
    local timeout=180  # 3 minutes timeout (reduced from 5)
    local counter=0
    local dots=0
    
    while [ $counter -lt $timeout ]; do
        if "$ADB_PATH" shell getprop sys.boot_completed 2>/dev/null | grep -q "1"; then
            echo ""
            echo -e "${GREEN}✅ Emulator is ready!${NC}"
            return 0
        fi
        
        # Show progress with rotating indicator
        case $((dots % 4)) in
            0) echo -ne "\r⏳ Booting emulator |   " ;;
            1) echo -ne "\r⏳ Booting emulator /   " ;;
            2) echo -ne "\r⏳ Booting emulator -   " ;;
            3) echo -ne "\r⏳ Booting emulator \\   " ;;
        esac
        
        sleep 3
        counter=$((counter + 3))
        dots=$((dots + 1))
        
        # Show time elapsed every 30 seconds
        if [ $((counter % 30)) -eq 0 ]; then
            echo -ne "\r⏳ Booting emulator... ${counter}s elapsed   "
        fi
    done
    
    echo ""
    echo -e "${YELLOW}⚠️  Emulator boot timeout (3 minutes)${NC}"
    echo -e "${BLUE}💡 The emulator might still be starting. You can:${NC}"
    echo -e "${BLUE}   1. Wait a bit more and run the script again${NC}"
    echo -e "${BLUE}   2. Check emulator manually and continue${NC}"
    return 1
}

# Check if Android SDK exists
if [ ! -f "$EMULATOR_PATH" ]; then
    echo -e "${RED}❌ Android SDK emulator not found at: $EMULATOR_PATH${NC}"
    echo "Please install Android Studio and SDK first."
    exit 1
fi

# Check if AVD exists
echo -e "${BLUE}📱 Checking available AVDs...${NC}"
AVDS=$("$EMULATOR_PATH" -list-avds)
if [[ ! "$AVDS" == *"$EMULATOR_NAME"* ]]; then
    echo -e "${RED}❌ AVD '$EMULATOR_NAME' not found${NC}"
    echo "Available AVDs:"
    echo "$AVDS"
    echo ""
    echo "Please create an AVD named '$EMULATOR_NAME' or update the script with your AVD name."
    exit 1
fi

# Check camera availability
check_camera

# Start emulator if not running
if ! check_emulator; then
    echo -e "${BLUE}🚀 Starting emulator: $EMULATOR_NAME${NC}"
    
    # Try starting emulator with camera first
    echo -e "${YELLOW}📷 Attempting to start with camera support...${NC}"
    "$EMULATOR_PATH" -avd "$EMULATOR_NAME" \
        -camera-back webcam0 \
        -camera-front webcam0 \
        -no-snapshot-save \
        -wipe-data > /tmp/emulator.log 2>&1 &
    EMULATOR_PID=$!
    
    # Wait a bit and check if it's still running
    sleep 10
    if ! kill -0 $EMULATOR_PID 2>/dev/null; then
        echo -e "${YELLOW}⚠️  Camera-enabled emulator failed, trying without camera...${NC}"
        
        # Try without camera
        "$EMULATOR_PATH" -avd "$EMULATOR_NAME" \
            -camera-back none \
            -camera-front none \
            -no-snapshot-save \
            -wipe-data > /tmp/emulator_no_camera.log 2>&1 &
        EMULATOR_PID=$!
        
        sleep 10
        if ! kill -0 $EMULATOR_PID 2>/dev/null; then
            echo -e "${RED}❌ Emulator failed to start even without camera${NC}"
            echo -e "${BLUE}📝 Check logs:${NC}"
            echo -e "${BLUE}   With camera: /tmp/emulator.log${NC}"
            echo -e "${BLUE}   Without camera: /tmp/emulator_no_camera.log${NC}"
            exit 1
        else
            echo -e "${GREEN}✅ Emulator started without camera${NC}"
            echo -e "${YELLOW}💡 Barcode scanning will use virtual camera (limited functionality)${NC}"
        fi
    else
        echo -e "${GREEN}✅ Emulator started with camera support${NC}"
    fi
    
    echo "Emulator PID: $EMULATOR_PID"
    
    # Wait a bit more for emulator to initialize
    echo -e "${YELLOW}⏳ Waiting for emulator to initialize...${NC}"
    sleep 15
    
    # Wait for emulator to boot completely (with timeout and better UX)
    if ! wait_for_emulator; then
        echo -e "${YELLOW}⚠️  Continuing anyway - emulator might still be starting${NC}"
        echo -e "${BLUE}💡 You can check emulator status with: adb devices${NC}"
        echo -e "${BLUE}💡 If needed, run this script again in a few minutes${NC}"
        
        # Ask user if they want to continue
        echo -e "${YELLOW}Do you want to continue building the app? (y/n)${NC}"
        read -r response
        if [[ ! "$response" =~ ^[Yy]$ ]]; then
            echo -e "${BLUE}Exiting. Run the script again when emulator is ready.${NC}"
            exit 0
        fi
    fi
else
    echo -e "${GREEN}✅ Emulator is already running${NC}"
    echo -e "${YELLOW}💡 Note: If camera doesn't work, restart emulator with this script${NC}"
fi

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

echo -e "${GREEN}🎉 WarungKu app is now running on the emulator!${NC}"
echo ""
echo -e "${YELLOW}📝 What's new in this version:${NC}"
echo "• ✨ Product image support (gallery + API)"
echo "• 🎨 Improved chip styling (better contrast)"
echo "• 📊 Enhanced dummy data generator"
echo "• 🔄 Database migration for image URLs"
echo "• 📷 Camera-enabled emulator for barcode scanning"
echo ""
echo -e "${BLUE}💡 Tips:${NC}"
echo "• Go to 'Tambah Barang' to test the new image feature"
echo "• Scan a barcode using your laptop's camera"
echo "• Use 'Generate Simple Test Chart' in Summary menu for testing"
echo "• Camera permission will be requested when scanning"
echo ""
echo -e "${BLUE}🔧 Troubleshooting:${NC}"
echo "• If camera doesn't work: Grant camera permission in emulator settings"
echo "• If barcode scanning fails: Ensure good lighting and clear barcode"
echo "• If app crashes: Check logcat with 'adb logcat | grep WarungKu'"
echo ""
echo -e "${GREEN}✅ Setup complete! Enjoy testing the app!${NC}"