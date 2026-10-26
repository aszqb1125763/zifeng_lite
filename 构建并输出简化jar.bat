@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ==========================================
echo  Zifeng Lite - build and export
echo ==========================================
call gradlew.bat exportLiteJar --console=plain
if errorlevel 1 (
    echo.
    echo Build failed. See Gradle log above.
    pause
    exit /b 1
)

echo.
echo Done. Output folder:
echo   F:\IDEA\简化输出jar
pause
