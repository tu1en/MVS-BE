@echo off
echo Running migration to add video_url column to lectures table...
echo.

REM Connect to SQL Server and run migration
sqlcmd -S localhost,1433 -d SchoolManagementDB -U sa -P "YourStrong@Passw0rd" -i migrations\add_video_url_to_lectures.sql

echo.
echo Migration completed!
pause
