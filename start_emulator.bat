@echo off
echo ========================================
echo Starting Android Emulator
echo ========================================
echo.

echo Setting up Android environment...
set ANDROID_HOME=C:\Android
set ANDROID_SDK_ROOT=C:\Android
set ANDROID_AVD_HOME=C:\Android\avd
set PATH=C:\Android\cmdline-tools\latest\bin;C:\Android\platform-tools;C:\Android\emulator;%PATH%

echo Checking if emulator is available...
if not exist "C:\Android\emulator\emulator.exe" (
    echo Error: Android emulator not found at C:\Android\emulator\emulator.exe
    echo Please run prepare_android_tools_and_emulator.bat first
    pause
    exit /b 1
)

echo.
echo Available AVDs:
call "C:\Android\cmdline-tools\latest\bin\avdmanager.bat" list avd

echo.
echo ========================================
echo Starting emulator: Clean_Android_API_34
echo ========================================
echo.
echo Enhanced settings enabled:
echo - GPU: Software rendering (stable)
echo - Memory: 4GB
echo - CPU cores: 4
echo - Network: Full speed
echo - Hardware acceleration: Auto
echo.

echo Starting emulator... Please wait.
echo.

REM Start emulator with enhanced settings and custom AVD path
set ANDROID_AVD_HOME=C:\Android\avd
"C:\Android\emulator\emulator.exe" -avd Clean_Android_API_34 -gpu swiftshader_indirect -accel auto -memory 4096 -cores 4 -netdelay none -netspeed full -verbose

echo.
echo ========================================
echo Emulator has closed or failed to start
echo ========================================
echo.
echo If the emulator didn't start properly:
echo 1. Check if Hyper-V is enabled in Windows
echo 2. Make sure no other emulator is running
echo 3. Try running as administrator
echo 4. Check Windows Event Viewer for errors
echo.
echo Troubleshooting tips:
echo - Button issues: Try longer press and hold
echo - Audio issues: Check Windows sound settings
echo - Performance: Close other applications
echo - Extended Controls: Ctrl+Shift+E in emulator
echo.
pause
