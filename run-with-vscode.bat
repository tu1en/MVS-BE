
@echo off
echo ==========================================
echo    Running Spring Boot with VS Code
echo ==========================================
echo.
echo This will use VS Code tasks to run the application
echo Press any key to start...
pause > nul

echo Opening VS Code and running backend task...
code .
echo.
echo Backend is starting via VS Code Task...
echo Check VS Code terminal for status
echo Server will be available at: http://localhost:8088
echo.
pause
