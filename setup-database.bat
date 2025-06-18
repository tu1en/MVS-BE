@echo off
echo Setting up SQL Server database user for Classroom Application...
echo.

REM Check if SQLCMD is available
where sqlcmd >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: sqlcmd is not found in PATH.
    echo Please make sure SQL Server Command Line Tools are installed.
    echo You can download them from: https://docs.microsoft.com/en-us/sql/tools/sqlcmd-utility
    pause
    exit /b 1
)

echo Please enter your SQL Server SA password:
set /p SA_PASSWORD=SA Password: 

echo.
echo Running database setup script...
sqlcmd -S localhost -U sa -P "%SA_PASSWORD%" -i setup-database-user.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Database setup completed successfully!
    echo You can now run your Spring Boot application.
) else (
    echo.
    echo ❌ Database setup failed. Please check the error messages above.
    echo Make sure:
    echo 1. SQL Server is running
    echo 2. SA password is correct
    echo 3. You have administrative privileges
)

echo.
pause
