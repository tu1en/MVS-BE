@echo off
echo =====================================
echo CLASSROOM MANAGEMENT SYSTEM - BACKEND
echo =====================================
echo.

cd /d "c:\Users\darky\Downloads\SEP490\backend\doproject"
echo Current directory: %CD%
echo.

echo Checking if pom.xml exists...
if exist pom.xml (
    echo ✅ pom.xml found
    echo Checking Maven configuration...
    type pom.xml | findstr "modelVersion" | head -1
) else (
    echo ❌ pom.xml NOT found
    exit /b 1
)
echo.

echo Checking Java version...
java -version
echo.

echo Checking Maven version...
mvn --version
echo.

echo Starting Spring Boot application...
echo Backend will be available at: http://localhost:8088
echo H2 Console will be available at: http://localhost:8088/h2-console
echo.

mvn spring-boot:run

pause
