#!/bin/bash

# Script untuk launch Android Emulator dengan webcam laptop via terminal
# Usage: ./launch_emulator_with_webcam.sh [AVD_NAME]

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 Launching Android Emulator with Webcam${NC}"
echo ""

# Find Android SDK
if [ -d "$HOME/Library/Android/sdk" ]; then
    ANDROID_SDK="$HOME/Library/Android/sdk"
elif [ -d "$HOME/Android/Sdk" ]; then
    ANDROID_SDK="$HOME/Android/Sdk"
else
    echo -e "${RED}❌ Error: Android SDK tidak ditemukan${NC}"
    echo "💡 Install Android SDK atau set ANDROID_HOME environment variable"
    exit 1
fi

EMULATOR="$ANDROID_SDK/emulator/emulator"
if [ ! -f "$EMULATOR" ]; then
    echo -e "${RED}❌ Error: Emulator tidak ditemukan di $EMULATOR${NC}"
    exit 1
fi

# Get AVD name
if [ -z "$1" ]; then
    echo "📋 Mencari AVD yang tersedia..."
    AVD_LIST=$("$EMULATOR" -list-avds 2>/dev/null)
    
    if [ -z "$AVD_LIST" ]; then
        echo -e "${RED}❌ Error: Tidak ada AVD yang ditemukan${NC}"
        echo "💡 Buat AVD terlebih dahulu menggunakan:"
        echo "   $EMULATOR -avd <AVD_NAME> -list-avds"
        echo "   atau install Android Studio untuk membuat AVD"
        exit 1
    fi
    
    # Use first AVD
    AVD_NAME=$(echo "$AVD_LIST" | head -n 1)
    echo -e "${YELLOW}⚠️  AVD tidak di-specify, menggunakan: $AVD_NAME${NC}"
else
    AVD_NAME="$1"
fi

echo ""
echo -e "${GREEN}✅ Using AVD: $AVD_NAME${NC}"
echo -e "${GREEN}📷 Camera: Webcam0 (laptop webcam)${NC}"
echo ""

# Check if emulator is already running
if pgrep -f "emulator.*$AVD_NAME" > /dev/null; then
    echo -e "${YELLOW}⚠️  Emulator dengan AVD '$AVD_NAME' sudah berjalan${NC}"
    echo "💡 Stop emulator terlebih dahulu atau gunakan AVD lain"
    exit 1
fi

# Launch emulator with webcam
echo "🚀 Starting emulator..."
echo ""

# macOS specific: Request camera permission if needed
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "💡 macOS akan meminta permission untuk camera saat pertama kali"
    echo "   Klik 'Allow' saat muncul dialog permission"
fi

# Launch in background
"$EMULATOR" -avd "$AVD_NAME" \
    -camera-back webcam0 \
    -camera-front webcam0 \
    -no-snapshot-load \
    -no-boot-anim \
    > /dev/null 2>&1 &

EMULATOR_PID=$!

echo -e "${GREEN}✅ Emulator sedang starting (PID: $EMULATOR_PID)${NC}"
echo ""
echo "⏳ Tunggu hingga emulator siap (biasanya 30-60 detik)..."
echo ""
echo "💡 Tips:"
echo "   - Pastikan webcam laptop tidak digunakan aplikasi lain"
echo "   - Jika muncul dialog permission camera, klik 'Allow'"
echo "   - Setelah emulator siap, install aplikasi dengan:"
echo "     adb install app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "🔍 Untuk melihat log emulator:"
echo "   tail -f ~/.android/avd/$AVD_NAME.avd/hardware-qemu.ini.log"
echo ""
echo "🛑 Untuk stop emulator:"
echo "   pkill -f 'emulator.*$AVD_NAME'"
