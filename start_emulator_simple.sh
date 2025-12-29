#!/bin/bash

# Simple Emulator Starter (No Camera Complications)
# Use this if the main script has camera issues

echo "📱 Starting Android Emulator (Simple Mode)"
echo "=========================================="

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
EMULATOR_NAME="Medium_Phone_API_36.0"
ANDROID_SDK_PATH="$HOME/Library/Android/sdk"
EMULATOR_PATH="$ANDROID_SDK_PATH/emulator/emulator"

# Check if emulator exists
if [ ! -f "$EMULATOR_PATH" ]; then
    echo -e "${RED}❌ Emulator not found at: $EMULATOR_PATH${NC}"
    exit 1
fi

# Check if AVD exists
if ! "$EMULATOR_PATH" -list-avds | grep -q "$EMULATOR_NAME"; then
    echo -e "${RED}❌ AVD '$EMULATOR_NAME' not found${NC}"
    echo "Available AVDs:"
    "$EMULATOR_PATH" -list-avds
    exit 1
fi

echo -e "${BLUE}🚀 Starting emulator: $EMULATOR_NAME${NC}"
echo -e "${YELLOW}📱 Simple mode - no camera complications${NC}"

# Start emulator with minimal options
"$EMULATOR_PATH" -avd "$EMULATOR_NAME" \
    -no-snapshot-save \
    -wipe-data &

EMULATOR_PID=$!
echo -e "${GREEN}✅ Emulator started with PID: $EMULATOR_PID${NC}"
echo -e "${BLUE}💡 Emulator is starting in the background${NC}"
echo -e "${BLUE}💡 Use 'adb devices' to check when it's ready${NC}"
echo -e "${BLUE}💡 Then run: ./quick_build_install.sh${NC}"
echo ""
echo -e "${YELLOW}📝 Note: This emulator won't have camera access${NC}"
echo -e "${YELLOW}📝 Barcode scanning will use virtual camera only${NC}"