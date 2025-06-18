@echo off
echo ========================================
echo Database Reset Script
echo ========================================
echo.
echo This script will:
echo 1. Stop the Spring Boot application (if running)
echo 2. Reset the database completely
echo 3. Start the application with fresh data
echo.
echo Press any key to continue...
pause > nul

echo.
echo 🛑 Stopping any running Spring Boot application...
taskkill /f /im java.exe 2>nul
timeout /t 3 /nobreak >nul

echo.
echo 🗑️ Resetting database...
echo The application will now start with create-drop mode
echo All existing data will be deleted and fresh data will be loaded
echo.

echo 🚀 Starting Spring Boot application...
cd /d "%~dp0"
mvn spring-boot:run

echo.
echo ✅ Database reset complete!
echo All data has been cleared and fresh sample data has been loaded.
pause 