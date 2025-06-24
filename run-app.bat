@echo off
title Cashflow Dashboard
echo Starting Cashflow Dashboard...
echo.

REM Ưu tiên dùng Java trong thư mục jdk nếu có
set JAVA_EXE=%~dp0jdk\bin\java.exe
if exist "%JAVA_EXE%" (
    echo Using bundled Java...
) else (
    set JAVA_EXE=java
    echo Using system Java...
)

REM Chạy ứng dụng
echo Running Cashflow Dashboard...
"%JAVA_EXE%" -jar "%~dp0target\cashflow-dashboard-1.0-SNAPSHOT.jar"

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Failed to start Cashflow Dashboard
    echo Please check if Java 17+ is installed
    pause
)