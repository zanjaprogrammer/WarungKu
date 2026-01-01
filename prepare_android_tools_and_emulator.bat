@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Android Development Tools Setup Script
echo ========================================
echo.

REM Prompt user for installation path
set /p INSTALL_PATH="Enter the path where you want to install Android tools (e.g., C:\Android): "

REM Validate path input
if "%INSTALL_PATH%"=="" (
    echo Error: No path provided. Exiting.
    pause
    exit /b 1
)

REM Remove trailing backslash if present
if "%INSTALL_PATH:~-1%"=="\" set INSTALL_PATH=%INSTALL_PATH:~0,-1%

echo.
echo Installation path: %INSTALL_PATH%
echo.

REM Create installation directory
if not exist "%INSTALL_PATH%" (
    echo Creating installation directory...
    mkdir "%INSTALL_PATH%"
    if errorlevel 1 (
        echo Error: Failed to create directory %INSTALL_PATH%p
        pause
        exit /b 1
    )
)

cd /d "%INSTALL_PATH%"

echo ========================================
echo Step 1: Downloading Android Command Line Tools
echo ========================================

REM Download Android Command Line Tools
set CMDTOOLS_URL=https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip
set CMDTOOLS_ZIP=commandlinetools-win-latest.zip

echo Downloading Android Command Line Tools...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%CMDTOOLS_URL%' -OutFile '%CMDTOOLS_ZIP%'}"

if not exist "%CMDTOOLS_ZIP%" (
    echo Error: Failed to download Command Line Tools
    pause
    exit /b 1
)

echo Extracting Command Line Tools...
powershell -Command "Expand-Archive -Path '%CMDTOOLS_ZIP%' -DestinationPath '.' -Force"

REM Move cmdline-tools to proper structure
if exist "cmdline-tools" (
    if not exist "cmdline-tools\latest" (
        mkdir "cmdline-tools\latest"
        move "cmdline-tools\bin" "cmdline-tools\latest\"
        move "cmdline-tools\lib" "cmdline-tools\latest\"
        move "cmdline-tools\NOTICE.txt" "cmdline-tools\latest\"
        move "cmdline-tools\source.properties" "cmdline-tools\latest\"
    )
)

REM Clean up zip file
del "%CMDTOOLS_ZIP%"

echo ========================================
echo Step 2: Setting up Environment Variables
echo ========================================

set ANDROID_HOME=%INSTALL_PATH%
set ANDROID_SDK_ROOT=%INSTALL_PATH%
set PATH=%ANDROID_HOME%\cmdline-tools\latest\bin;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\emulator;%PATH%

echo ANDROID_HOME set to: %ANDROID_HOME%
echo PATH updated with Android tools

echo ========================================
echo Step 3: Installing SDK Components
echo ========================================

echo Installing Android SDK Platform Tools...
call "%ANDROID_HOME%\cmdline-tools\latest\bin\sdkmanager.bat" "platform-tools"

echo Installing Android SDK Build Tools...
call "%ANDROID_HOME%\cmdline-tools\latest\bin\sdkmanager.bat" "build-tools;34.0.0"

echo Installing Android Platform (API 34)...
call "%ANDROID_HOME%\cmdline-tools\latest\bin\sdkmanager.bat" "platforms;android-34"

echo Installing Android Emulator...
call "%ANDROID_HOME%\cmdline-tools\latest\bin\sdkmanager.bat" "emulator"

echo Installing System Images for AVD...
call "%ANDROID_HOME%\cmdline-tools\latest\bin\sdkmanager.bat" "system-images;android-34;default;x86_64"

echo Accepting all licenses...
echo y | call "%ANDROID_HOME%\cmdline-tools\latest\bin\sdkmanager.bat" --licenses

echo ========================================
echo Step 4: Setting up AVD Storage Location
echo ========================================

REM Set AVD path to user-specified location to avoid C: drive space issues
set ANDROID_AVD_HOME=%INSTALL_PATH%\avd
echo Setting AVD storage to: %ANDROID_AVD_HOME%

REM Create AVD directory
if not exist "%ANDROID_AVD_HOME%" (
    mkdir "%ANDROID_AVD_HOME%"
)

echo ========================================
echo Step 5: Creating Android Virtual Device (AVD)
echo ========================================

set AVD_NAME=Clean_Android_API_34
echo Creating AVD: %AVD_NAME% (WITHOUT Google Play Services)
echo AVD will be stored at: %ANDROID_AVD_HOME%

REM Create AVD with clean Android (no Google apps) and smaller partitions
echo no | call "%ANDROID_HOME%\cmdline-tools\latest\bin\avdmanager.bat" create avd -n %AVD_NAME% -k "system-images;android-34;default;x86_64" -d "pixel_7" -c 2048M

if errorlevel 1 (
    echo Warning: AVD creation may have encountered issues, but continuing...
)

echo ========================================
echo Step 6: Creating Helper Scripts
echo ========================================

REM Create a script to set environment variables for future sessions
echo Creating android_env.bat for future use...
(
echo @echo off
echo REM Android Development Environment Setup
echo set ANDROID_HOME=%INSTALL_PATH%
echo set ANDROID_SDK_ROOT=%INSTALL_PATH%
echo set ANDROID_AVD_HOME=%INSTALL_PATH%\avd
echo set PATH=%INSTALL_PATH%\cmdline-tools\latest\bin;%INSTALL_PATH%\platform-tools;%INSTALL_PATH%\emulator;%%PATH%%
echo echo Android environment variables set!
echo echo ANDROID_HOME: %%ANDROID_HOME%%
echo echo.
echo echo Available commands:
echo echo   adb - Android Debug Bridge
echo echo   emulator - Android Emulator
echo echo   sdkmanager - SDK Manager
echo echo   avdmanager - AVD Manager
echo echo.
echo echo To start the emulator: emulator -avd %AVD_NAME%
) > "%INSTALL_PATH%\android_env.bat"

REM Get the current directory where this script is running
set CURRENT_DIR=%~dp0
if "%CURRENT_DIR:~-1%"=="\" set CURRENT_DIR=%CURRENT_DIR:~0,-1%

echo Creating start_emulator.bat in current directory...
(
echo @echo off
echo echo ========================================
echo echo Starting Android Emulator
echo echo ========================================
echo echo.
echo.
echo echo Setting up Android environment...
echo set ANDROID_HOME=%INSTALL_PATH%
echo set ANDROID_SDK_ROOT=%INSTALL_PATH%
echo set ANDROID_AVD_HOME=%INSTALL_PATH%\avd
echo set PATH=%INSTALL_PATH%\cmdline-tools\latest\bin;%INSTALL_PATH%\platform-tools;%INSTALL_PATH%\emulator;%%PATH%%
echo.
echo echo Checking if emulator is available...
echo if not exist "%INSTALL_PATH%\emulator\emulator.exe" ^(
echo     echo Error: Android emulator not found at %INSTALL_PATH%\emulator\emulator.exe
echo     echo Please run prepare_android_tools_and_emulator.bat first
echo     pause
echo     exit /b 1
echo ^)
echo.
echo echo.
echo echo Available AVDs:
echo call "%INSTALL_PATH%\cmdline-tools\latest\bin\avdmanager.bat" list avd
echo.
echo echo.
echo echo ========================================
echo echo Starting emulator: %AVD_NAME%
echo echo ========================================
echo echo.
echo echo Enhanced settings enabled:
echo echo - GPU: Software rendering ^(stable^)
echo echo - Memory: 4GB
echo echo - CPU cores: 4
echo echo - Network: Full speed
echo echo - Hardware acceleration: Auto
echo echo.
echo.
echo echo Starting emulator... Please wait.
echo echo.
echo.
echo REM Start emulator with enhanced settings and custom AVD path
echo set ANDROID_AVD_HOME=%INSTALL_PATH%\avd
echo "%INSTALL_PATH%\emulator\emulator.exe" -avd %AVD_NAME% -gpu swiftshader_indirect -accel auto -memory 4096 -cores 4 -netdelay none -netspeed full -verbose
echo.
echo echo.
echo echo ========================================
echo echo Emulator has closed or failed to start
echo echo ========================================
echo echo.
echo echo If the emulator didn't start properly:
echo echo 1. Check if Hyper-V is enabled in Windows
echo echo 2. Make sure no other emulator is running
echo echo 3. Try running as administrator
echo echo 4. Check Windows Event Viewer for errors
echo echo.
echo echo Troubleshooting tips:
echo echo - Button issues: Try longer press and hold
echo echo - Audio issues: Check Windows sound settings
echo echo - Performance: Close other applications
echo echo - Extended Controls: Ctrl+Shift+E in emulator
echo echo.
echo pause
) > "%CURRENT_DIR%\start_emulator.bat"

echo ========================================
echo Step 7: Testing Installation
echo ========================================

echo Testing ADB...
call "%ANDROID_HOME%\platform-tools\adb.exe" version

echo.
echo Testing AVD Manager...
call "%ANDROID_HOME%\cmdline-tools\latest\bin\avdmanager.bat" list avd

echo ========================================
echo Installation Complete!
echo ========================================
echo.
echo Installation Summary:
echo - Android tools installed to: %INSTALL_PATH%
echo - AVD created: %AVD_NAME% (CLEAN - No Google Apps!)
echo - Environment script: %INSTALL_PATH%\android_env.bat
echo - Emulator launcher: %CURRENT_DIR%\start_emulator.bat
echo.
echo Next Steps:
echo 1. Simply run "start_emulator.bat" to launch the CLEAN Android emulator
echo 2. NO Google Play Services = NO UPDATE PROMPTS!
echo 3. Install APKs manually if you need specific apps
echo.
echo Quick Start: Double-click start_emulator.bat for a Google-free experience!
echo.

pause