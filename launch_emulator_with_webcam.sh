#!/bin/bash

# WarungKu Emulator Launcher with Webcam Support
# Launches Android emulator with camera support for barcode scanning

echo "🚀 Launching WarungKu Emulator with Webcam Support..."
echo "=================================================="

# Check if Android SDK is available
if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ]; then
    echo "❌ Android SDK not found!"
    echo "💡 Please set ANDROID_HOME or ANDROID_SDK_ROOT environment variable"
    exit 1
fi

# Set SDK path
SDK_PATH=${ANDROID_HOME:-$ANDROID_SDK_ROOT}
EMULATOR_PATH="$SDK_PATH/emulator/emulator"

# Check if emulator exists
if [ ! -f "$EMULATOR_PATH" ]; then
    echo "❌ Android emulator not found at: $EMULATOR_PATH"
    echo "💡 Please install Android SDK and emulator"
    exit 1
fi

# List available AVDs
echo "📱 Available Android Virtual Devices:"
"$SDK_PATH/emulator/emulator" -list-avds

# Check if any AVDs exist
AVD_COUNT=$("$SDK_PATH/emulator/emulator" -list-avds | wc -l)
if [ $AVD_COUNT -eq 0 ]; then
    echo "❌ No AVDs found!"
    echo "💡 Please create an AVD using Android Studio or avdmanager"
    exit 1
fi

# Get the first available AVD or use default
DEFAULT_AVD=$("$SDK_PATH/emulator/emulator" -list-avds | head -n 1)
AVD_NAME=${1:-$DEFAULT_AVD}

echo "🎯 Using AVD: $AVD_NAME"

# Check if webcam is available
echo "📷 Checking webcam availability..."
if ls /dev/video* 1> /dev/null 2>&1; then
    echo "✅ Webcam detected"
    CAMERA_ARGS="-camera-back webcam0 -camera-front webcam0"
else
    echo "⚠️ No webcam detected, using emulated camera"
    CAMERA_ARGS="-camera-back emulated -camera-front emulated"
fi

# Launch emulator with camera support
echo "🚀 Starting emulator: $AVD_NAME"
echo "📷 Camera configuration: $CAMERA_ARGS"
echo "💡 This may take a few minutes to start..."

"$EMULATOR_PATH" -avd "$AVD_NAME" $CAMERA_ARGS -gpu host -skin 1080x1920 -memory 2048 -partition-size 2048 &

EMULATOR_PID=$!
echo "Emulator PID: $EMULATOR_PID"

# Wait for emulator to start
echo "⏳ Waiting for emulator to initialize..."
"$SDK_PATH/platform-tools/adb" wait-for-device

echo "✅ Emulator is ready!"
echo ""
echo "📝 Next steps:"
echo "• Install the app: adb install -r app/build/outputs/apk/debug/app-debug.apk"
echo "• Or use: ./run_app_with_emulator.sh"
echo "• Camera permission will be requested when scanning barcodes"
echo ""
echo "🔧 Troubleshooting:"
echo "• If camera doesn't work: Grant camera permission in emulator settings"
echo "• If barcode scanning fails: Ensure good lighting and clear barcode"
echo "• To stop emulator: kill $EMULATOR_PID"
echo ""
echo "✅ Emulator launched successfully!"