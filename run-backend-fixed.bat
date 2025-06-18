@echo off
echo ==========================================
echo      Spring Boot Backend Launcher
echo ==========================================
echo.

REM Thiết lập JAVA_HOME
set JAVA_HOME=C:\Users\darky\AppData\Roaming\Code\User\globalStorage\pleiades.java-extension-pack-jdk\java\21
set PATH=%JAVA_HOME%\bin;%PATH%

echo JAVA_HOME: %JAVA_HOME%
echo.

REM Chuyển đến thư mục project
cd /d "%~dp0"
echo Current directory: %CD%
echo.

REM Kiểm tra Maven
echo Testing Maven...
mvn --version > nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven not working properly!
    pause
    exit /b 1
)

echo Starting Spring Boot application...
echo Server will be available at: http://localhost:8088
echo.
echo Press Ctrl+C to stop the server
echo.

mvn spring-boot:run

echo.
echo Server stopped.
pause
