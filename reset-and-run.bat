@echo off
echo ========================================
echo Cashflow Dashboard - Reset and Run
echo ========================================

echo.
echo Step 1: Removing old database files...
if exist cashflow.db del cashflow.db
if exist cashflow.db-shm del cashflow.db-shm
if exist cashflow.db-wal del cashflow.db-wal

echo.
echo Step 2: Creating new database with correct schema...
sqlite3 cashflow.db < reset-database.sql

echo.
echo Step 3: Compiling and running the application...
mvn clean compile javafx:run

echo.
echo Application started successfully!
pause 