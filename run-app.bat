@echo off
echo ==========================================
echo      Spring Boot Application Launcher
echo ==========================================
echo.
echo Starting Spring Boot application...
echo Server will be available at: http://localhost:8088
echo.

cd /d "%~dp0"

echo Checking for compiled classes...
if not exist "target\classes" (
    echo Error: Project not compiled yet!
    echo Please compile the project first using VS Code or IDE
    echo Or install Maven to use: mvn clean compile
    pause
    exit /b 1
)

echo Running Spring Boot application...
java -cp "target\classes;target\dependency\*" com.classroomapp.classroombackend.ClassroomBackendApplication

pause
