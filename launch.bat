@echo off
title Digital Detox Tracker
echo ====================================
echo  Digital Detox Tracker
echo  Building application...
echo ====================================

cd /d "%~dp0"

call mvn -q clean package -DskipTests
if errorlevel 1 (
    echo.
    echo [ERROR] Build failed. Check output above.
    pause
    exit /b 1
)

echo.
echo  Starting Digital Detox Tracker...
echo ====================================
java -jar target\digital-detox-tracker-1.0.0.jar
