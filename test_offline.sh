#!/bin/bash
# Script untuk test offline mode di emulator

echo "=== Testing Offline Mode ==="
echo ""
echo "1. Mengaktifkan Airplane Mode..."
adb shell settings put global airplane_mode_on 1
adb shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true
echo "✅ Airplane Mode AKTIF"
echo ""
echo "Sekarang aplikasi dalam mode offline."
echo "Silakan test fitur-fitur aplikasi."
echo ""
read -p "Tekan Enter untuk menonaktifkan Airplane Mode..."
echo ""
echo "2. Menonaktifkan Airplane Mode..."
adb shell settings put global airplane_mode_on 0
adb shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false
echo "✅ Airplane Mode NONAKTIF"
echo ""
echo "Aplikasi kembali online."
