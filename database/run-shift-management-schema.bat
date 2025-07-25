@echo off
echo ================================================================================================
echo SHIFT MANAGEMENT MODULE - DATABASE SCHEMA DEPLOYMENT
echo Module 2: Comprehensive Shift Management System for HR
echo ================================================================================================

set SERVER_NAME=localhost
set DATABASE_NAME=SEP490_ClassroomManagement
set USERNAME=sa
set /p PASSWORD=Enter SQL Server password: 

echo.
echo 🚀 Deploying Shift Management Schema...
echo Server: %SERVER_NAME%
echo Database: %DATABASE_NAME%
echo.

sqlcmd -S %SERVER_NAME% -d %DATABASE_NAME% -U %USERNAME% -P %PASSWORD% -i shift-management-schema.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Shift Management Schema deployed successfully!
    echo.
    echo 📊 Summary:
    echo    - shift_templates: Shift templates management
    echo    - shift_schedules: Weekly/Monthly schedules  
    echo    - shift_assignments: Employee shift assignments
    echo    - shift_swap_requests: Shift swap workflow
    echo    - shift_notifications: Notification system
    echo    - employee_availability: Employee availability
    echo    - shift_statistics: Reporting and analytics
    echo.
    echo 🎯 Next Steps:
    echo    1. Run the Spring Boot application
    echo    2. Test the shift management APIs
    echo    3. Verify data integrity
    echo.
) else (
    echo.
    echo ❌ Schema deployment failed!
    echo Please check the error messages above.
    echo.
)

pause
