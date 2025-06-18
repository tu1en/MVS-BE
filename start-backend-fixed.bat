@echo off
echo ========================================
echo Starting Classroom Backend Application
echo ========================================
echo.
echo This will create all tables in the database.
echo After the first successful run, change ddl-auto back to 'update' in application.properties
echo.

cd /d "%~dp0"
mvn spring-boot:run

echo.
echo Application stopped.
pause 