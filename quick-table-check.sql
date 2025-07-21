-- Quick script để kiểm tra số lượng records trong các tables quan trọng
-- Chạy script này để verify seeders đã hoạt động chưa

USE classroom_management;

PRINT N'📊 QUICK TABLE CHECK - SEEDER VERIFICATION';
PRINT N'============================================';
PRINT N'';

-- Declare variables
DECLARE @userCount BIGINT = (SELECT COUNT(*) FROM users);
DECLARE @roleCount BIGINT = (SELECT COUNT(*) FROM roles);
DECLARE @classroomCount BIGINT = (SELECT COUNT(*) FROM classrooms);
DECLARE @courseCount BIGINT = (SELECT COUNT(*) FROM courses);
DECLARE @assignmentCount BIGINT = (SELECT COUNT(*) FROM assignments);
DECLARE @submissionCount BIGINT = (SELECT COUNT(*) FROM submissions);
DECLARE @lectureCount BIGINT = (SELECT COUNT(*) FROM lectures);
DECLARE @attendanceCount BIGINT = (SELECT COUNT(*) FROM attendance);
DECLARE @announcementCount BIGINT = (SELECT COUNT(*) FROM announcements);
DECLARE @messageCount BIGINT = (SELECT COUNT(*) FROM messages);

-- Core tables
PRINT N'🔧 CORE TABLES:';
PRINT N'  Users: ' + CAST(@userCount AS NVARCHAR(10));
PRINT N'  Roles: ' + CAST(@roleCount AS NVARCHAR(10));
PRINT N'  Courses: ' + CAST(@courseCount AS NVARCHAR(10));
PRINT N'  Classrooms: ' + CAST(@classroomCount AS NVARCHAR(10));
PRINT N'';

-- Educational content
PRINT N'📚 EDUCATIONAL CONTENT:';
PRINT N'  Lectures: ' + CAST(@lectureCount AS NVARCHAR(10));
PRINT N'  Assignments: ' + CAST(@assignmentCount AS NVARCHAR(10));
PRINT N'  Submissions: ' + CAST(@submissionCount AS NVARCHAR(10));
PRINT N'';

-- Communication
PRINT N'💬 COMMUNICATION:';
PRINT N'  Announcements: ' + CAST(@announcementCount AS NVARCHAR(10));
PRINT N'  Messages: ' + CAST(@messageCount AS NVARCHAR(10));
PRINT N'';

-- Attendance
PRINT N'📅 ATTENDANCE:';
PRINT N'  Attendance Records: ' + CAST(@attendanceCount AS NVARCHAR(10));
PRINT N'';

-- Extended tables (from our new seeders)
DECLARE @systemRoleCount BIGINT = 0;
DECLARE @systemPermissionCount BIGINT = 0;
DECLARE @rolePermissionCount BIGINT = 0;
DECLARE @systemConfigCount BIGINT = 0;
DECLARE @auditLogCount BIGINT = 0;
DECLARE @absenceCount BIGINT = 0;
DECLARE @requestCount BIGINT = 0;
DECLARE @systemMonitoringCount BIGINT = 0;
DECLARE @syllabusCount BIGINT = 0;

-- Check if extended tables exist and get counts
IF OBJECT_ID('system_roles', 'U') IS NOT NULL
    SET @systemRoleCount = (SELECT COUNT(*) FROM system_roles);

IF OBJECT_ID('system_permissions', 'U') IS NOT NULL
    SET @systemPermissionCount = (SELECT COUNT(*) FROM system_permissions);

IF OBJECT_ID('role_permissions', 'U') IS NOT NULL
    SET @rolePermissionCount = (SELECT COUNT(*) FROM role_permissions);

IF OBJECT_ID('system_configurations', 'U') IS NOT NULL
    SET @systemConfigCount = (SELECT COUNT(*) FROM system_configurations);

IF OBJECT_ID('audit_logs', 'U') IS NOT NULL
    SET @auditLogCount = (SELECT COUNT(*) FROM audit_logs);

IF OBJECT_ID('absences', 'U') IS NOT NULL
    SET @absenceCount = (SELECT COUNT(*) FROM absences);

IF OBJECT_ID('requests', 'U') IS NOT NULL
    SET @requestCount = (SELECT COUNT(*) FROM requests);

IF OBJECT_ID('system_monitoring', 'U') IS NOT NULL
    SET @systemMonitoringCount = (SELECT COUNT(*) FROM system_monitoring);

IF OBJECT_ID('syllabuses', 'U') IS NOT NULL
    SET @syllabusCount = (SELECT COUNT(*) FROM syllabuses);

PRINT N'🔧 EXTENDED SYSTEM TABLES:';
PRINT N'  System Roles: ' + CAST(@systemRoleCount AS NVARCHAR(10));
PRINT N'  System Permissions: ' + CAST(@systemPermissionCount AS NVARCHAR(10));
PRINT N'  Role Permissions: ' + CAST(@rolePermissionCount AS NVARCHAR(10));
PRINT N'  System Configurations: ' + CAST(@systemConfigCount AS NVARCHAR(10));
PRINT N'  Audit Logs: ' + CAST(@auditLogCount AS NVARCHAR(10));
PRINT N'';

PRINT N'📋 FINAL SEEDER TABLES:';
PRINT N'  Absences: ' + CAST(@absenceCount AS NVARCHAR(10));
PRINT N'  Requests: ' + CAST(@requestCount AS NVARCHAR(10));
PRINT N'  System Monitoring: ' + CAST(@systemMonitoringCount AS NVARCHAR(10));
PRINT N'  Syllabi: ' + CAST(@syllabusCount AS NVARCHAR(10));
PRINT N'';

-- Calculate totals
DECLARE @coreTotal BIGINT = @userCount + @roleCount + @courseCount + @classroomCount;
DECLARE @educationTotal BIGINT = @lectureCount + @assignmentCount + @submissionCount;
DECLARE @communicationTotal BIGINT = @announcementCount + @messageCount;
DECLARE @extendedTotal BIGINT = @systemRoleCount + @systemPermissionCount + @rolePermissionCount + @systemConfigCount + @auditLogCount;
DECLARE @finalTotal BIGINT = @absenceCount + @requestCount + @systemMonitoringCount + @syllabusCount;
DECLARE @grandTotal BIGINT = @coreTotal + @educationTotal + @communicationTotal + @attendanceCount + @extendedTotal + @finalTotal;

PRINT N'📊 SUMMARY:';
PRINT N'  Core Tables Total: ' + CAST(@coreTotal AS NVARCHAR(10));
PRINT N'  Education Total: ' + CAST(@educationTotal AS NVARCHAR(10));
PRINT N'  Communication Total: ' + CAST(@communicationTotal AS NVARCHAR(10));
PRINT N'  Attendance Total: ' + CAST(@attendanceCount AS NVARCHAR(10));
PRINT N'  Extended System Total: ' + CAST(@extendedTotal AS NVARCHAR(10));
PRINT N'  Final Seeder Total: ' + CAST(@finalTotal AS NVARCHAR(10));
PRINT N'  GRAND TOTAL RECORDS: ' + CAST(@grandTotal AS NVARCHAR(10));
PRINT N'';

-- Status assessment
IF @grandTotal = 0
BEGIN
    PRINT N'❌ STATUS: NO DATA FOUND';
    PRINT N'🔧 ACTION: Seeders have not run yet or failed';
    PRINT N'💡 SOLUTION: Run application with --spring.profiles.active=local';
END
ELSE IF @grandTotal < 100
BEGIN
    PRINT N'⚠️ STATUS: MINIMAL DATA';
    PRINT N'🔧 ACTION: Some seeders may have run but not all';
    PRINT N'💡 SOLUTION: Check application logs for seeder errors';
END
ELSE IF @grandTotal < 1000
BEGIN
    PRINT N'✅ STATUS: GOOD DATA COVERAGE';
    PRINT N'🎯 ACTION: Most seeders appear to be working';
    PRINT N'💡 NEXT: Run comprehensive verification';
END
ELSE
BEGIN
    PRINT N'🎉 STATUS: EXCELLENT DATA COVERAGE';
    PRINT N'🏆 ACTION: Seeders working perfectly!';
    PRINT N'✨ RESULT: Ready for development/testing';
END

PRINT N'';
PRINT N'============================================';
PRINT N'✅ Quick table check completed!';

-- Show empty tables count
DECLARE @emptyTables INT = 0;

IF @userCount = 0 SET @emptyTables = @emptyTables + 1;
IF @roleCount = 0 SET @emptyTables = @emptyTables + 1;
IF @courseCount = 0 SET @emptyTables = @emptyTables + 1;
IF @classroomCount = 0 SET @emptyTables = @emptyTables + 1;
IF @lectureCount = 0 SET @emptyTables = @emptyTables + 1;
IF @assignmentCount = 0 SET @emptyTables = @emptyTables + 1;
IF @submissionCount = 0 SET @emptyTables = @emptyTables + 1;
IF @attendanceCount = 0 SET @emptyTables = @emptyTables + 1;
IF @announcementCount = 0 SET @emptyTables = @emptyTables + 1;
IF @messageCount = 0 SET @emptyTables = @emptyTables + 1;
IF @systemRoleCount = 0 SET @emptyTables = @emptyTables + 1;
IF @systemPermissionCount = 0 SET @emptyTables = @emptyTables + 1;
IF @rolePermissionCount = 0 SET @emptyTables = @emptyTables + 1;
IF @systemConfigCount = 0 SET @emptyTables = @emptyTables + 1;
IF @auditLogCount = 0 SET @emptyTables = @emptyTables + 1;
IF @absenceCount = 0 SET @emptyTables = @emptyTables + 1;
IF @requestCount = 0 SET @emptyTables = @emptyTables + 1;
IF @systemMonitoringCount = 0 SET @emptyTables = @emptyTables + 1;
IF @syllabusCount = 0 SET @emptyTables = @emptyTables + 1;

PRINT N'🎯 EMPTY TABLES: ' + CAST(@emptyTables AS NVARCHAR(10)) + N'/19 checked tables';

IF @emptyTables = 0
    PRINT N'🏆 ACHIEVEMENT UNLOCKED: 0 EMPTY TABLES!';
ELSE
    PRINT N'📈 PROGRESS: ' + CAST((19 - @emptyTables) AS NVARCHAR(10)) + N'/19 tables have data (' + CAST(ROUND((CAST(19 - @emptyTables AS FLOAT) / 19) * 100, 1) AS NVARCHAR(10)) + N'%)';
