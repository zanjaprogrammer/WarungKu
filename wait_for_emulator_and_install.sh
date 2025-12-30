#!/bin/bash
echo "🔄 Waiting for emulator to be ready..."
echo ""

while true; do
  STATUS=$(adb devices 2>/dev/null | grep emulator | awk '{print $2}')
  
  if [ "$STATUS" = "device" ]; then
    echo "✅ Emulator ready and authorized!"
    echo ""
    echo "=== Installing app ==="
    cd /Users/ekowibowo/KahfiDev/WarungKu
    ./gradlew installDebug --no-daemon
    echo ""
    echo "=== Launching app ==="
    adb shell am start -n com.alkahfprogrammer.warungku/.LoginActivity
    echo ""
    echo "✅ App installed and launched!"
    echo "📱 Silakan test authentication flow (register & login)"
    exit 0
  elif [ "$STATUS" = "unauthorized" ]; then
    echo "⏳ Emulator detected but unauthorized. Waiting for authorization..."
    sleep 3
  else
    echo "⏳ Waiting for emulator connection..."
    sleep 3
  fi
done
