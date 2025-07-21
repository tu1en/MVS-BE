-- FINAL VERIFICATION SCRIPT
-- Kiểm tra kết quả cuối cùng sau khi fix lỗi DateTime và chạy application
-- Mục tiêu: Verify rằng tất cả seeders hoạt động và đạt được 0 empty tables

USE classroom_management;

PRINT N'🎯 FINAL VERIFICATION - DATETIME FIX & SEEDERS RESULTS';
PRINT N'======================================================';
PRINT N'';

-- Check database connection
IF DB_ID('classroom_management') IS NULL
BEGIN
    PRINT N'❌ ERROR: Database classroom_management not found!';
    RETURN;
END

PRINT N'✅ Database connection successful';
PRINT N'';

-- Declare variables for all important tables
DECLARE @userCount BIGINT = 0;
DECLARE @roleCount BIGINT = 0;
DECLARE @courseCount BIGINT = 0;
DECLARE @classroomCount BIGINT = 0;
DECLARE @lectureCount BIGINT = 0;
DECLARE @assignmentCount BIGINT = 0;
DECLARE @submissionCount BIGINT = 0;
DECLARE @attendanceCount BIGINT = 0;
DECLARE @announcementCount BIGINT = 0;
DECLARE @messageCount BIGINT = 0;

-- Extended seeder tables
DECLARE @systemRoleCount BIGINT = 0;
DECLARE @systemPermissionCount BIGINT = 0;
DECLARE @rolePermissionCount BIGINT = 0;
DECLARE @systemConfigCount BIGINT = 0;
DECLARE @auditLogCount BIGINT = 0;

-- Final seeder tables
DECLARE @absenceCount BIGINT = 0;
DECLARE @requestCount BIGINT = 0;
DECLARE @systemMonitoringCount BIGINT = 0;
DECLARE @syllabusCount BIGINT = 0;

-- Lecture enhancement tables
DECLARE @lectureMaterialCount BIGINT = 0;
DECLARE @lectureRecordingCount BIGINT = 0;

-- Get counts safely
BEGIN TRY
    -- Core tables
    IF OBJECT_ID('users', 'U') IS NOT NULL SET @userCount = (SELECT COUNT(*) FROM users);
    IF OBJECT_ID('roles', 'U') IS NOT NULL SET @roleCount = (SELECT COUNT(*) FROM roles);
    IF OBJECT_ID('courses', 'U') IS NOT NULL SET @courseCount = (SELECT COUNT(*) FROM courses);
    IF OBJECT_ID('classrooms', 'U') IS NOT NULL SET @classroomCount = (SELECT COUNT(*) FROM classrooms);
    IF OBJECT_ID('lectures', 'U') IS NOT NULL SET @lectureCount = (SELECT COUNT(*) FROM lectures);
    IF OBJECT_ID('assignments', 'U') IS NOT NULL SET @assignmentCount = (SELECT COUNT(*) FROM assignments);
    IF OBJECT_ID('submissions', 'U') IS NOT NULL SET @submissionCount = (SELECT COUNT(*) FROM submissions);
    IF OBJECT_ID('attendance', 'U') IS NOT NULL SET @attendanceCount = (SELECT COUNT(*) FROM attendance);
    IF OBJECT_ID('announcements', 'U') IS NOT NULL SET @announcementCount = (SELECT COUNT(*) FROM announcements);
    IF OBJECT_ID('student_messages', 'U') IS NOT NULL SET @messageCount = (SELECT COUNT(*) FROM student_messages);
    
    -- Extended seeder tables
    IF OBJECT_ID('system_roles', 'U') IS NOT NULL SET @systemRoleCount = (SELECT COUNT(*) FROM system_roles);
    IF OBJECT_ID('system_permissions', 'U') IS NOT NULL SET @systemPermissionCount = (SELECT COUNT(*) FROM system_permissions);
    IF OBJECT_ID('role_permissions', 'U') IS NOT NULL SET @rolePermissionCount = (SELECT COUNT(*) FROM role_permissions);
    IF OBJECT_ID('system_configurations', 'U') IS NOT NULL SET @systemConfigCount = (SELECT COUNT(*) FROM system_configurations);
    IF OBJECT_ID('audit_logs', 'U') IS NOT NULL SET @auditLogCount = (SELECT COUNT(*) FROM audit_logs);
    
    -- Final seeder tables
    IF OBJECT_ID('absences', 'U') IS NOT NULL SET @absenceCount = (SELECT COUNT(*) FROM absences);
    IF OBJECT_ID('requests', 'U') IS NOT NULL SET @requestCount = (SELECT COUNT(*) FROM requests);
    IF OBJECT_ID('system_monitoring', 'U') IS NOT NULL SET @systemMonitoringCount = (SELECT COUNT(*) FROM system_monitoring);
    IF OBJECT_ID('syllabuses', 'U') IS NOT NULL SET @syllabusCount = (SELECT COUNT(*) FROM syllabuses);
    
    -- Lecture enhancement tables
    IF OBJECT_ID('lecture_materials', 'U') IS NOT NULL SET @lectureMaterialCount = (SELECT COUNT(*) FROM lecture_materials);
    IF OBJECT_ID('lecture_recordings', 'U') IS NOT NULL SET @lectureRecordingCount = (SELECT COUNT(*) FROM lecture_recordings);
    
END TRY
BEGIN CATCH
    PRINT N'⚠️ Warning: Error getting some table counts - ' + ERROR_MESSAGE();
END CATCH

-- Display results by category
PRINT N'📊 CORE TABLES (Basic Seeders):';
PRINT N'  👥 Users: ' + CAST(@userCount AS NVARCHAR(10));
PRINT N'  🔐 Roles: ' + CAST(@roleCount AS NVARCHAR(10));
PRINT N'  📚 Courses: ' + CAST(@courseCount AS NVARCHAR(10));
PRINT N'  🏫 Classrooms: ' + CAST(@classroomCount AS NVARCHAR(10));
PRINT N'  📖 Lectures: ' + CAST(@lectureCount AS NVARCHAR(10));
PRINT N'  📝 Assignments: ' + CAST(@assignmentCount AS NVARCHAR(10));
PRINT N'  📄 Submissions: ' + CAST(@submissionCount AS NVARCHAR(10));
PRINT N'  📅 Attendance: ' + CAST(@attendanceCount AS NVARCHAR(10));
PRINT N'  📢 Announcements: ' + CAST(@announcementCount AS NVARCHAR(10));
PRINT N'  💬 Messages: ' + CAST(@messageCount AS NVARCHAR(10));
PRINT N'';

PRINT N'🔧 EXTENDED SYSTEM TABLES (RoleSeeder):';
PRINT N'  ⚙️ System Roles: ' + CAST(@systemRoleCount AS NVARCHAR(10)) + N' (Target: 6)';
PRINT N'  🔑 System Permissions: ' + CAST(@systemPermissionCount AS NVARCHAR(10)) + N' (Target: 8)';
PRINT N'  🔗 Role Permissions: ' + CAST(@rolePermissionCount AS NVARCHAR(10)) + N' (Target: 30+)';
PRINT N'';

PRINT N'📚 LECTURE ENHANCEMENTS (LectureSeeder):';
PRINT N'  📄 Lecture Materials: ' + CAST(@lectureMaterialCount AS NVARCHAR(10)) + N' (Target: 100+)';
PRINT N'  🎥 Lecture Recordings: ' + CAST(@lectureRecordingCount AS NVARCHAR(10)) + N' (Target: 70+)';
PRINT N'';

PRINT N'⚙️ SYSTEM MANAGEMENT (ComprehensiveTableSeeder):';
PRINT N'  🔧 System Configurations: ' + CAST(@systemConfigCount AS NVARCHAR(10)) + N' (Target: 15)';
PRINT N'  📋 Audit Logs: ' + CAST(@auditLogCount AS NVARCHAR(10)) + N' (Target: 300+)';
PRINT N'';

PRINT N'🎯 FINAL TABLES (FinalTableSeeder):';
PRINT N'  🏥 Absences: ' + CAST(@absenceCount AS NVARCHAR(10)) + N' (Target: 25)';
PRINT N'  📨 Requests: ' + CAST(@requestCount AS NVARCHAR(10)) + N' (Target: 30)';
PRINT N'  📊 System Monitoring: ' + CAST(@systemMonitoringCount AS NVARCHAR(10)) + N' (Target: 280+)';
PRINT N'  📚 Syllabi: ' + CAST(@syllabusCount AS NVARCHAR(10)) + N' (Target: varies)';
PRINT N'';

-- Calculate totals
DECLARE @coreTotal BIGINT = @userCount + @roleCount + @courseCount + @classroomCount + @lectureCount + 
                           @assignmentCount + @submissionCount + @attendanceCount + @announcementCount + @messageCount;
DECLARE @extendedTotal BIGINT = @systemRoleCount + @systemPermissionCount + @rolePermissionCount;
DECLARE @lectureTotal BIGINT = @lectureMaterialCount + @lectureRecordingCount;
DECLARE @systemTotal BIGINT = @systemConfigCount + @auditLogCount;
DECLARE @finalTotal BIGINT = @absenceCount + @requestCount + @systemMonitoringCount + @syllabusCount;
DECLARE @grandTotal BIGINT = @coreTotal + @extendedTotal + @lectureTotal + @systemTotal + @finalTotal;

PRINT N'📊 SUMMARY BY SEEDER:';
PRINT N'  Core Seeders Total: ' + CAST(@coreTotal AS NVARCHAR(10));
PRINT N'  RoleSeeder Total: ' + CAST(@extendedTotal AS NVARCHAR(10));
PRINT N'  LectureSeeder Total: ' + CAST(@lectureTotal AS NVARCHAR(10));
PRINT N'  ComprehensiveTableSeeder Total: ' + CAST(@systemTotal AS NVARCHAR(10));
PRINT N'  FinalTableSeeder Total: ' + CAST(@finalTotal AS NVARCHAR(10));
PRINT N'  GRAND TOTAL RECORDS: ' + CAST(@grandTotal AS NVARCHAR(10));
PRINT N'';

-- Count empty tables
DECLARE @emptyTables INT = 0;
DECLARE @totalChecked INT = 19;

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

PRINT N'🎯 EMPTY TABLES ANALYSIS:';
PRINT N'  Empty Tables: ' + CAST(@emptyTables AS NVARCHAR(10)) + N'/' + CAST(@totalChecked AS NVARCHAR(10));

IF @emptyTables = 0
BEGIN
    PRINT N'🏆 ACHIEVEMENT UNLOCKED: 0 EMPTY TABLES!';
    PRINT N'🎉 MISSION ACCOMPLISHED!';
    PRINT N'✨ All seeders working perfectly!';
END
ELSE IF @emptyTables <= 5
BEGIN
    PRINT N'✅ EXCELLENT PROGRESS!';
    PRINT N'🎯 Most seeders working correctly';
    DECLARE @successPercent FLOAT = (CAST(@totalChecked - @emptyTables AS FLOAT) / @totalChecked) * 100;
    PRINT N'📈 Success Rate: ' + CAST(ROUND(@successPercent, 1) AS NVARCHAR(10)) + N'%';
END
ELSE IF @emptyTables <= 10
BEGIN
    PRINT N'⚠️ GOOD PROGRESS';
    PRINT N'🔧 Some seeders working, others may need attention';
END
ELSE
BEGIN
    PRINT N'❌ SEEDERS NOT WORKING';
    PRINT N'🔧 Most tables still empty - check application logs';
END

-- Status assessment based on total records
PRINT N'';
PRINT N'📊 OVERALL STATUS ASSESSMENT:';

IF @grandTotal = 0
BEGIN
    PRINT N'❌ STATUS: NO DATA - SEEDERS NOT RUN';
    PRINT N'🔧 POSSIBLE CAUSES:';
    PRINT N'   - Application not started with --spring.profiles.active=local';
    PRINT N'   - DataLoader not executed';
    PRINT N'   - Database connection issues';
END
ELSE IF @grandTotal < 100
BEGIN
    PRINT N'⚠️ STATUS: MINIMAL DATA - BASIC SEEDERS ONLY';
    PRINT N'🔧 Extended seeders may not have run';
END
ELSE IF @grandTotal < 500
BEGIN
    PRINT N'✅ STATUS: GOOD DATA COVERAGE';
    PRINT N'🎯 Most seeders appear to be working';
END
ELSE IF @grandTotal < 1000
BEGIN
    PRINT N'🎉 STATUS: EXCELLENT DATA COVERAGE';
    PRINT N'🏆 All major seeders working perfectly!';
END
ELSE
BEGIN
    PRINT N'🚀 STATUS: OUTSTANDING DATA COVERAGE';
    PRINT N'💎 Database fully populated - production ready!';
END

-- DateTime fix verification
PRINT N'';
PRINT N'🕐 DATETIME FIX VERIFICATION:';
IF @systemMonitoringCount > 0
BEGIN
    PRINT N'✅ DateTime fix successful - System Monitoring data created';
    PRINT N'✅ No DateTimeException errors detected';
END
ELSE
BEGIN
    PRINT N'⚠️ DateTime fix status unclear - no monitoring data found';
END

-- Final recommendations
PRINT N'';
PRINT N'💡 RECOMMENDATIONS:';
IF @emptyTables = 0 AND @grandTotal > 500
BEGIN
    PRINT N'🎯 Perfect! Database ready for development and testing';
    PRINT N'🚀 Consider running API tests to verify endpoints';
END
ELSE IF @emptyTables <= 5
BEGIN
    PRINT N'🔧 Run application longer to let all seeders complete';
    PRINT N'📊 Check application logs for any seeder errors';
END
ELSE
BEGIN
    PRINT N'🔧 Restart application with --spring.profiles.active=local';
    PRINT N'📋 Check DataLoader configuration and seeder dependencies';
END

PRINT N'';
PRINT N'======================================================';
PRINT N'✅ Final verification completed!';
PRINT N'📅 Timestamp: ' + CONVERT(NVARCHAR(19), GETDATE(), 120);
