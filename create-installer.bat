@echo off
title Create Cashflow Dashboard Installer
echo Creating Cashflow Dashboard Installer...
echo.

REM Kiểm tra Inno Setup
where iscc >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Inno Setup Compiler (iscc) not found
    echo Please install Inno Setup from: https://jrsoftware.org/isinfo.php
    echo Make sure to add Inno Setup to PATH during installation
    pause
    exit /b 1
)

REM Tạo thư mục installer nếu chưa có
if not exist "installer" mkdir installer

REM Compile installer
echo Compiling installer...
iscc setup.iss

if %errorlevel% equ 0 (
    echo.
    echo SUCCESS: Installer created successfully!
    echo Location: installer\CashflowDashboard-Setup.exe
    echo.
    echo You can now distribute this installer to users.
    echo.
) else (
    echo.
    echo ERROR: Failed to create installer
    echo Please check the setup.iss file and Inno Setup installation
    echo.
)

pause 