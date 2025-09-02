@echo off
echo Building WorldCopyPaste Plugin for CraftBukkit...
echo.

echo Cleaning previous build...
call mvn clean

echo.
echo Building plugin...
call mvn package

echo.
if %ERRORLEVEL% EQU 0 (
    echo Build successful!
    echo Plugin JAR file created in target/ directory
    echo File: target/myplugin-1.0-SNAPSHOT.jar
) else (
    echo Build failed! Check the error messages above.
)

echo.
pause
