@echo off
echo 🚀 Starting Seeder Test Application...
echo.

REM Set UTF-8 encoding
chcp 65001 > nul

REM Clean and compile
echo 📦 Building application...
call mvn clean package -DskipTests -q
if %ERRORLEVEL% neq 0 (
    echo ❌ Build failed!
    pause
    exit /b 1
)

echo ✅ Build successful!
echo.

REM Run application with local profile for 60 seconds then stop
echo 🔄 Running application with seeders (will auto-stop after 60 seconds)...
echo.

timeout /t 3 > nul

REM Start application in background and capture PID
start /b java -jar target/classroom-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=local --server.port=8080 > application.log 2>&1

REM Wait for application to start and seeders to run
echo ⏳ Waiting for application startup and seeding...
timeout /t 60 > nul

REM Kill Java processes (application)
echo 🛑 Stopping application...
taskkill /f /im java.exe > nul 2>&1

echo.
echo 📋 Application logs:
echo ==================
type application.log | findstr /i "seeder"
echo.
echo ==================

echo.
echo ✅ Seeder test completed!
echo 📊 Check the logs above for seeding results
echo.

REM Clean up
del application.log > nul 2>&1

pause
