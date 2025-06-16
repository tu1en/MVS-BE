@echo off
cd /d "c:\Users\darky\Downloads\SEP490\backend\doproject"
echo Current directory: %CD%
echo.
echo Checking if pom.xml exists...
if exist pom.xml (
    echo pom.xml found
    type pom.xml | findstr "modelVersion"
) else (
    echo pom.xml NOT found
)
echo.
echo Running mvn spring-boot:run...
mvn spring-boot:run
pause
