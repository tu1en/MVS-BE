@echo off
chcp 65001 > nul
echo 🚀 Starting Spring Boot Application with Seeders
echo ================================================
echo.

echo 📦 Building application...
call mvn clean package -DskipTests -q
if %ERRORLEVEL% neq 0 (
    echo ❌ Build failed!
    pause
    exit /b 1
)
echo ✅ Build successful!
echo.

echo 🔄 Starting application with local profile...
echo 📊 Seeders will run automatically
echo ⏳ Please wait for startup (may take 1-2 minutes)...
echo.
echo 🛑 Press Ctrl+C to stop the application
echo.

java -jar target/classroom-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=local --server.port=8080

echo.
echo 🛑 Application stopped.
pause
