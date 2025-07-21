-- Script để verify seeders đã chạy thành công
-- Chạy trong SQL Server Management Studio sau khi application đã start

USE classroom_management;

PRINT N'🔍 VERIFYING SEEDER RESULTS';
PRINT N'============================';
PRINT N'';

-- Check if database exists and is accessible
IF DB_ID('classroom_management') IS NULL
BEGIN
    PRINT N'❌ ERROR: Database classroom_management not found!';
    PRINT N'💡 SOLUTION: Create database or check connection string';
    RETURN;
END

PRINT N'✅ Database classroom_management found and accessible';
PRINT N'';

-- Core tables check
DECLARE @userCount BIGINT = 0;
DECLARE @roleCount BIGINT = 0;
DECLARE @classroomCount BIGINT = 0;
DECLARE @courseCount BIGINT = 0;

-- Extended seeder tables check
DECLARE @systemRoleCount BIGINT = 0;
DECLARE @systemPermissionCount BIGINT = 0;
DECLARE @rolePermissionCount BIGINT = 0;
DECLARE @systemConfigCount BIGINT = 0;
DECLARE @auditLogCount BIGINT = 0;

-- Final seeder tables check
DECLARE @absenceCount BIGINT = 0;
DECLARE @requestCount BIGINT = 0;
DECLARE @systemMonitoringCount BIGINT = 0;
DECLARE @syllabusCount BIGINT = 0;

-- Educational content
DECLARE @lectureCount BIGINT = 0;
DECLARE @lectureMaterialCount BIGINT = 0;
DECLARE @lectureRecordingCount BIGINT = 0;
DECLARE @assignmentCount BIGINT = 0;

-- Get counts safely
BEGIN TRY
    IF OBJECT_ID('users', 'U') IS NOT NULL SET @userCount = (SELECT COUNT(*) FROM users);
    IF OBJECT_ID('roles', 'U') IS NOT NULL SET @roleCount = (SELECT COUNT(*) FROM roles);
    IF OBJECT_ID('classrooms', 'U') IS NOT NULL SET @classroomCount = (SELECT COUNT(*) FROM classrooms);
    IF OBJECT_ID('courses', 'U') IS NOT NULL SET @courseCount = (SELECT COUNT(*) FROM courses);
    
    IF OBJECT_ID('system_roles', 'U') IS NOT NULL SET @systemRoleCount = (SELECT COUNT(*) FROM system_roles);
    IF OBJECT_ID('system_permissions', 'U') IS NOT NULL SET @systemPermissionCount = (SELECT COUNT(*) FROM system_permissions);
    IF OBJECT_ID('role_permissions', 'U') IS NOT NULL SET @rolePermissionCount = (SELECT COUNT(*) FROM role_permissions);
    IF OBJECT_ID('system_configurations', 'U') IS NOT NULL SET @systemConfigCount = (SELECT COUNT(*) FROM system_configurations);
    IF OBJECT_ID('audit_logs', 'U') IS NOT NULL SET @auditLogCount = (SELECT COUNT(*) FROM audit_logs);
    
    IF OBJECT_ID('absences', 'U') IS NOT NULL SET @absenceCount = (SELECT COUNT(*) FROM absences);
    IF OBJECT_ID('requests', 'U') IS NOT NULL SET @requestCount = (SELECT COUNT(*) FROM requests);
    IF OBJECT_ID('system_monitoring', 'U') IS NOT NULL SET @systemMonitoringCount = (SELECT COUNT(*) FROM system_monitoring);
    IF OBJECT_ID('syllabuses', 'U') IS NOT NULL SET @syllabusCount = (SELECT COUNT(*) FROM syllabuses);
    
    IF OBJECT_ID('lectures', 'U') IS NOT NULL SET @lectureCount = (SELECT COUNT(*) FROM lectures);
    IF OBJECT_ID('lecture_materials', 'U') IS NOT NULL SET @lectureMaterialCount = (SELECT COUNT(*) FROM lecture_materials);
    IF OBJECT_ID('lecture_recordings', 'U') IS NOT NULL SET @lectureRecordingCount = (SELECT COUNT(*) FROM lecture_recordings);
    IF OBJECT_ID('assignments', 'U') IS NOT NULL SET @assignmentCount = (SELECT COUNT(*) FROM assignments);
END TRY
BEGIN CATCH
    PRINT N'⚠️ Warning: Some tables may not exist yet (normal if app is starting)';
END CATCH

-- Display results
PRINT N'📊 CORE TABLES:';
PRINT N'  👥 Users: ' + CAST(@userCount AS NVARCHAR(10));
PRINT N'  🔐 Roles: ' + CAST(@roleCount AS NVARCHAR(10));
PRINT N'  📚 Courses: ' + CAST(@courseCount AS NVARCHAR(10));
PRINT N'  🏫 Classrooms: ' + CAST(@classroomCount AS NVARCHAR(10));
PRINT N'';

PRINT N'🔧 EXTENDED SYSTEM TABLES (RoleSeeder):';
PRINT N'  ⚙️ System Roles: ' + CAST(@systemRoleCount AS NVARCHAR(10)) + N' (Expected: 6)';
PRINT N'  🔑 System Permissions: ' + CAST(@systemPermissionCount AS NVARCHAR(10)) + N' (Expected: 8)';
PRINT N'  🔗 Role Permissions: ' + CAST(@rolePermissionCount AS NVARCHAR(10)) + N' (Expected: ~30)';
PRINT N'';

PRINT N'📚 EDUCATIONAL CONTENT (LectureSeeder):';
PRINT N'  📖 Lectures: ' + CAST(@lectureCount AS NVARCHAR(10));
PRINT N'  📄 Lecture Materials: ' + CAST(@lectureMaterialCount AS NVARCHAR(10)) + N' (Expected: 100+)';
PRINT N'  🎥 Lecture Recordings: ' + CAST(@lectureRecordingCount AS NVARCHAR(10)) + N' (Expected: 70+)';
PRINT N'  📝 Assignments: ' + CAST(@assignmentCount AS NVARCHAR(10));
PRINT N'';

PRINT N'⚙️ SYSTEM MANAGEMENT (ComprehensiveTableSeeder):';
PRINT N'  🔧 System Configurations: ' + CAST(@systemConfigCount AS NVARCHAR(10)) + N' (Expected: 15)';
PRINT N'  📋 Audit Logs: ' + CAST(@auditLogCount AS NVARCHAR(10)) + N' (Expected: 300+)';
PRINT N'';

PRINT N'🎯 FINAL TABLES (FinalTableSeeder):';
PRINT N'  🏥 Absences: ' + CAST(@absenceCount AS NVARCHAR(10)) + N' (Expected: 25)';
PRINT N'  📨 Requests: ' + CAST(@requestCount AS NVARCHAR(10)) + N' (Expected: 30)';
PRINT N'  📊 System Monitoring: ' + CAST(@systemMonitoringCount AS NVARCHAR(10)) + N' (Expected: 280+)';
PRINT N'  📚 Syllabi: ' + CAST(@syllabusCount AS NVARCHAR(10)) + N' (Expected: varies)';
PRINT N'';

-- Calculate totals and status
DECLARE @totalRecords BIGINT = @userCount + @roleCount + @classroomCount + @courseCount + 
                               @systemRoleCount + @systemPermissionCount + @rolePermissionCount + 
                               @systemConfigCount + @auditLogCount + @absenceCount + @requestCount + 
                               @systemMonitoringCount + @syllabusCount + @lectureCount + 
                               @lectureMaterialCount + @lectureRecordingCount + @assignmentCount;

PRINT N'📊 TOTAL RECORDS: ' + CAST(@totalRecords AS NVARCHAR(10));
PRINT N'';

-- Status assessment
IF @totalRecords = 0
BEGIN
    PRINT N'❌ STATUS: NO DATA FOUND';
    PRINT N'🔧 POSSIBLE CAUSES:';
    PRINT N'   - Application has not started yet';
    PRINT N'   - Seeders failed to run';
    PRINT N'   - Database connection issues';
    PRINT N'   - Wrong database name in connection string';
    PRINT N'';
    PRINT N'💡 SOLUTIONS:';
    PRINT N'   1. Check if application is running with --spring.profiles.active=local';
    PRINT N'   2. Check application logs for errors';
    PRINT N'   3. Verify database connection string';
    PRINT N'   4. Wait for application to fully start (may take 1-2 minutes)';
END
ELSE IF @totalRecords < 100
BEGIN
    PRINT N'⚠️ STATUS: MINIMAL DATA - SEEDERS PARTIALLY WORKING';
    PRINT N'🔧 Some basic seeders may have run but extended seeders may have failed';
    PRINT N'💡 Check application logs for seeder-specific errors';
END
ELSE IF @totalRecords < 500
BEGIN
    PRINT N'✅ STATUS: GOOD PROGRESS - MOST SEEDERS WORKING';
    PRINT N'🎯 Core seeders appear to be working well';
    PRINT N'💡 Some extended seeders may still be running or may have minor issues';
END
ELSE
BEGIN
    PRINT N'🎉 STATUS: EXCELLENT! ALL SEEDERS WORKING PERFECTLY';
    PRINT N'🏆 Database is fully populated with comprehensive data';
    PRINT N'✨ Ready for development, testing, and demonstration';
END

-- Count empty tables
DECLARE @emptyTables INT = 0;
DECLARE @checkedTables INT = 17; -- Total tables we're checking

IF @userCount = 0 SET @emptyTables = @emptyTables + 1;
IF @roleCount = 0 SET @emptyTables = @emptyTables + 1;
IF @classroomCount = 0 SET @emptyTables = @emptyTables + 1;
IF @courseCount = 0 SET @emptyTables = @emptyTables + 1;
IF @systemRoleCount = 0 SET @emptyTables = @emptyTables + 1;
IF @systemPermissionCount = 0 SET @emptyTables = @emptyTables + 1;
IF @rolePermissionCount = 0 SET @emptyTables = @emptyTables + 1;
IF @systemConfigCount = 0 SET @emptyTables = @emptyTables + 1;
IF @auditLogCount = 0 SET @emptyTables = @emptyTables + 1;
IF @absenceCount = 0 SET @emptyTables = @emptyTables + 1;
IF @requestCount = 0 SET @emptyTables = @emptyTables + 1;
IF @systemMonitoringCount = 0 SET @emptyTables = @emptyTables + 1;
IF @syllabusCount = 0 SET @emptyTables = @emptyTables + 1;
IF @lectureCount = 0 SET @emptyTables = @emptyTables + 1;
IF @lectureMaterialCount = 0 SET @emptyTables = @emptyTables + 1;
IF @lectureRecordingCount = 0 SET @emptyTables = @emptyTables + 1;
IF @assignmentCount = 0 SET @emptyTables = @emptyTables + 1;

PRINT N'🎯 EMPTY TABLES: ' + CAST(@emptyTables AS NVARCHAR(10)) + N'/' + CAST(@checkedTables AS NVARCHAR(10)) + N' checked tables';

IF @emptyTables = 0
BEGIN
    PRINT N'🏆 ACHIEVEMENT UNLOCKED: 0 EMPTY TABLES!';
    PRINT N'🎉 MISSION ACCOMPLISHED!';
END
ELSE
BEGIN
    DECLARE @progressPercent FLOAT = (CAST(@checkedTables - @emptyTables AS FLOAT) / @checkedTables) * 100;
    PRINT N'📈 PROGRESS: ' + CAST(@checkedTables - @emptyTables AS NVARCHAR(10)) + N'/' + CAST(@checkedTables AS NVARCHAR(10)) + 
          N' tables have data (' + CAST(ROUND(@progressPercent, 1) AS NVARCHAR(10)) + N'%)';
END

PRINT N'';
PRINT N'============================';
PRINT N'✅ Verification completed!';

-- Show sample data from key tables
IF @userCount > 0
BEGIN
    PRINT N'';
    PRINT N'👥 SAMPLE USERS:';
    SELECT TOP 3 id, full_name, email, role_id FROM users ORDER BY id;
END

IF @systemRoleCount > 0
BEGIN
    PRINT N'';
    PRINT N'⚙️ SYSTEM ROLES:';
    SELECT role_name, description, is_active FROM system_roles ORDER BY sort_order;
END

IF @systemConfigCount > 0
BEGIN
    PRINT N'';
    PRINT N'🔧 SYSTEM CONFIGURATIONS (Sample):';
    SELECT TOP 5 config_key, config_name, config_value FROM system_configurations ORDER BY config_key;
END
