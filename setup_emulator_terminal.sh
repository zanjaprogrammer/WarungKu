#!/bin/bash

# Setup script untuk emulator via terminal
# This script helps setup everything needed to run emulator from terminal

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🔧 Android Emulator Terminal Setup${NC}"
echo ""

# Find Android SDK
if [ -d "$HOME/Library/Android/sdk" ]; then
    ANDROID_SDK="$HOME/Library/Android/sdk"
elif [ -d "$HOME/Android/Sdk" ]; then
    ANDROID_SDK="$HOME/Android/Sdk"
else
    echo -e "${RED}❌ Android SDK tidak ditemukan${NC}"
    echo ""
    echo "💡 Install Android SDK:"
    echo "   1. Download Android Command Line Tools:"
    echo "      https://developer.android.com/studio#command-tools"
    echo "   2. Extract dan setup SDK:"
    echo "      export ANDROID_HOME=\$HOME/Library/Android/sdk"
    echo "      export PATH=\$PATH:\$ANDROID_HOME/tools:\$ANDROID_HOME/platform-tools:\$ANDROID_HOME/emulator"
    exit 1
fi

echo -e "${GREEN}✅ Android SDK: $ANDROID_SDK${NC}"

# Check emulator
EMULATOR="$ANDROID_SDK/emulator/emulator"
if [ ! -f "$EMULATOR" ]; then
    echo -e "${RED}❌ Emulator tidak ditemukan${NC}"
    echo ""
    echo "💡 Install emulator menggunakan sdkmanager:"
    echo "   \$ANDROID_SDK/cmdline-tools/latest/bin/sdkmanager 'emulator'"
    exit 1
fi

echo -e "${GREEN}✅ Emulator: $EMULATOR${NC}"

# List AVDs
echo ""
echo -e "${BLUE}📋 AVD yang tersedia:${NC}"
AVD_LIST=$("$EMULATOR" -list-avds 2>/dev/null)

if [ -z "$AVD_LIST" ]; then
    echo -e "${YELLOW}⚠️  Tidak ada AVD yang ditemukan${NC}"
    echo ""
    echo "💡 Buat AVD menggunakan command line:"
    echo "   \$ANDROID_SDK/cmdline-tools/latest/bin/avdmanager create avd -n <NAME> -k <SYSTEM_IMAGE>"
    echo ""
    echo "   Atau download Android Studio untuk membuat AVD via GUI"
    exit 1
else
    echo "$AVD_LIST" | while read -r avd; do
        echo "   - $avd"
    done
fi

# Check adb
ADB="$ANDROID_SDK/platform-tools/adb"
if [ ! -f "$ADB" ]; then
    echo -e "${YELLOW}⚠️  ADB tidak ditemukan${NC}"
else
    echo -e "${GREEN}✅ ADB: $ADB${NC}"
fi

echo ""
echo -e "${GREEN}✅ Setup selesai!${NC}"
echo ""
echo "🚀 Untuk launch emulator dengan webcam:"
echo "   ./launch_emulator_with_webcam.sh [AVD_NAME]"
echo ""
echo "📱 Untuk install aplikasi:"
echo "   $ADB install app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "🔍 Untuk melihat devices:"
echo "   $ADB devices"
