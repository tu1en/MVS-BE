@echo off
echo =====================================================
echo CLASSROOM & SLOT MANAGEMENT SCHEMA DEPLOYMENT
echo =====================================================
echo.

echo 🔍 Checking SQL Server connection...
sqlcmd -S localhost -U sa -P 12345678 -Q "SELECT @@VERSION" >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ Cannot connect to SQL Server. Please check:
    echo    - SQL Server is running
    echo    - Username: sa
    echo    - Password: 12345678
    echo    - Server: localhost
    pause
    exit /b 1
)

echo ✅ SQL Server connection successful
echo.

echo 🗄️ Deploying Classroom & Slot Management schema...
sqlcmd -S localhost -U sa -P 12345678 -i classroom-slot-management-schema.sql

if %ERRORLEVEL% neq 0 (
    echo ❌ Schema deployment failed!
    pause
    exit /b 1
)

echo.
echo ✅ Schema deployment completed successfully!
echo.

echo 🔍 Verifying table creation...
sqlcmd -S localhost -U sa -P 12345678 -Q "USE SchoolManagementDB; SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('sessions', 'slots', 'attachments', 'classroom_slot_audit_log') ORDER BY TABLE_NAME;"

echo.
echo 📊 Checking existing classrooms table structure...
sqlcmd -S localhost -U sa -P 12345678 -Q "USE SchoolManagementDB; SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'classrooms' ORDER BY ORDINAL_POSITION;"

echo.
echo 🎯 Schema deployment completed!
echo 📋 Next steps:
echo    1. Create JPA entities
echo    2. Implement service layer
echo    3. Create REST controllers
echo    4. Build frontend components
echo.
pause
