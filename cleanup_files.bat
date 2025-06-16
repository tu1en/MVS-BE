@echo off
echo Starting cleanup of unnecessary files...

REM Delete test and temporary files
if exist "CheckDatabase.java" del "CheckDatabase.java"
if exist "app_startup.txt" del "app_startup.txt"
if exist "final_run_test.txt" del "final_run_test.txt"
if exist "cd" del "cd"
if exist "cls" del "cls"
if exist "set" del "set"
if exist "classroom-frontend@0.1.0" del "classroom-frontend@0.1.0"
if exist "mvn" del "mvn"
if exist "npm" del "npm"
if exist "react-scripts" del "react-scripts"
if exist "test-login-credentials.sh" del "test-login-credentials.sh"
if exist "[Help" del "[Help"

echo Cleanup completed!
echo.
echo Files deleted (if they existed):
echo - CheckDatabase.java (standalone test utility)
echo - app_startup.txt (test output)
echo - final_run_test.txt (test output)
echo - cd, cls, set (command artifacts)
echo - classroom-frontend@0.1.0 (misplaced frontend artifact)
echo - mvn, npm, react-scripts (command artifacts)
echo - test-login-credentials.sh (test script with credentials)
echo - [Help (temp file)
echo.
echo Important files preserved:
echo - pom.xml (Maven configuration)
echo - src/ (source code)
echo - All configuration and documentation files
pause
