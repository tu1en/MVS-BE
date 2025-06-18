@echo off
echo ==========================================
echo    Java & Maven Environment Setup
echo ==========================================
echo.

REM Tìm Java installation
for /f "tokens=*" %%i in ('where java 2^>nul') do (
    set JAVA_EXE=%%i
    goto :found_java
)

echo Java not found in PATH!
pause
exit /b 1

:found_java
echo Found Java at: %JAVA_EXE%

REM Lấy JAVA_HOME từ đường dẫn java.exe
for %%i in ("%JAVA_EXE%") do set JAVA_BIN=%%~dpi
for %%i in ("%JAVA_BIN%\..\") do set JAVA_HOME=%%~fi

echo Setting JAVA_HOME to: %JAVA_HOME%
set JAVA_HOME=%JAVA_HOME%

REM Kiểm tra Maven
echo.
echo Checking Maven...
mvn --version
if %ERRORLEVEL% NEQ 0 (
    echo Maven check failed!
    pause
    exit /b 1
)

echo.
echo ==========================================
echo    Starting Spring Boot Application
echo ==========================================
echo.

cd /d "%~dp0"
echo Current directory: %CD%
echo.

echo Running: mvn spring-boot:run
mvn spring-boot:run

pause
