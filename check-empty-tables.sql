-- Script để kiểm tra số lượng empty tables và tạo báo cáo chi tiết
-- Mục tiêu: Verify kết quả seeding và đạt được 0 empty tables

USE classroom_management;

-- Tạo bảng tạm để lưu kết quả
CREATE TABLE #TableCounts (
    table_name NVARCHAR(128),
    record_count BIGINT
);

-- Insert tất cả table counts
INSERT INTO #TableCounts (table_name, record_count)
SELECT 'roles', COUNT(*) FROM roles
UNION ALL SELECT 'users', COUNT(*) FROM users
UNION ALL SELECT 'courses', COUNT(*) FROM courses
UNION ALL SELECT 'classrooms', COUNT(*) FROM classrooms
UNION ALL SELECT 'classroom_enrollments', COUNT(*) FROM classroom_enrollments
UNION ALL SELECT 'lectures', COUNT(*) FROM lectures
UNION ALL SELECT 'lecture_materials', COUNT(*) FROM lecture_materials
UNION ALL SELECT 'lecture_recordings', COUNT(*) FROM lecture_recordings
UNION ALL SELECT 'assignments', COUNT(*) FROM assignments
UNION ALL SELECT 'submissions', COUNT(*) FROM submissions
UNION ALL SELECT 'attendance', COUNT(*) FROM attendance
UNION ALL SELECT 'attendance_logs', COUNT(*) FROM attendance_logs
UNION ALL SELECT 'staff_attendance_logs', COUNT(*) FROM staff_attendance_logs
UNION ALL SELECT 'system_roles', COUNT(*) FROM system_roles
UNION ALL SELECT 'system_permissions', COUNT(*) FROM system_permissions
UNION ALL SELECT 'role_permissions', COUNT(*) FROM role_permissions
UNION ALL SELECT 'system_configurations', COUNT(*) FROM system_configurations
UNION ALL SELECT 'audit_logs', COUNT(*) FROM audit_logs
UNION ALL SELECT 'announcements', COUNT(*) FROM announcements
UNION ALL SELECT 'blogs', COUNT(*) FROM blogs
UNION ALL SELECT 'messages', COUNT(*) FROM messages
UNION ALL SELECT 'schedules', COUNT(*) FROM schedules
UNION ALL SELECT 'exams', COUNT(*) FROM exams
UNION ALL SELECT 'student_progress', COUNT(*) FROM student_progress
UNION ALL SELECT 'accomplishments', COUNT(*) FROM accomplishments
UNION ALL SELECT 'absences', COUNT(*) FROM absences
UNION ALL SELECT 'requests', COUNT(*) FROM requests
UNION ALL SELECT 'timetable_events', COUNT(*) FROM timetable_events
UNION ALL SELECT 'system_monitoring', COUNT(*) FROM system_monitoring
UNION ALL SELECT 'syllabi', COUNT(*) FROM syllabi;

-- Báo cáo tổng quan
PRINT N'📊 BÁO CÁO TỔNG QUAN SEEDING RESULTS';
PRINT N'=====================================';

DECLARE @totalTables INT = (SELECT COUNT(*) FROM #TableCounts);
DECLARE @emptyTables INT = (SELECT COUNT(*) FROM #TableCounts WHERE record_count = 0);
DECLARE @nonEmptyTables INT = @totalTables - @emptyTables;
DECLARE @totalRecords BIGINT = (SELECT SUM(record_count) FROM #TableCounts);

PRINT N'📋 Tổng số tables: ' + CAST(@totalTables AS NVARCHAR(10));
PRINT N'✅ Tables có data: ' + CAST(@nonEmptyTables AS NVARCHAR(10));
PRINT N'❌ Tables empty: ' + CAST(@emptyTables AS NVARCHAR(10));
PRINT N'📊 Tổng số records: ' + CAST(@totalRecords AS NVARCHAR(20));
PRINT N'🎯 Tỷ lệ thành công: ' + CAST(ROUND((CAST(@nonEmptyTables AS FLOAT) / @totalTables) * 100, 2) AS NVARCHAR(10)) + N'%';
PRINT N'';

-- Hiển thị chi tiết các tables có data
PRINT N'✅ TABLES CÓ DATA:';
PRINT N'==================';
SELECT 
    table_name AS [Table Name],
    record_count AS [Records],
    CASE 
        WHEN record_count > 1000 THEN N'🔥 Rất nhiều'
        WHEN record_count > 100 THEN N'📈 Nhiều'
        WHEN record_count > 10 THEN N'📊 Vừa phải'
        ELSE N'📝 Ít'
    END AS [Status]
FROM #TableCounts 
WHERE record_count > 0
ORDER BY record_count DESC;

PRINT N'';

-- Hiển thị các tables còn empty
IF @emptyTables > 0
BEGIN
    PRINT N'❌ TABLES CÒN EMPTY:';
    PRINT N'====================';
    SELECT 
        table_name AS [Empty Table],
        N'Cần tạo seeder' AS [Action Required]
    FROM #TableCounts 
    WHERE record_count = 0
    ORDER BY table_name;
    
    PRINT N'';
    PRINT N'🔧 HƯỚNG DẪN TIẾP THEO:';
    PRINT N'======================';
    PRINT N'1. Tạo seeders cho các tables còn empty';
    PRINT N'2. Thêm vào DataLoader.java';
    PRINT N'3. Chạy lại application';
    PRINT N'4. Verify lại bằng script này';
END
ELSE
BEGIN
    PRINT N'🎉 CHÚC MỪNG! ĐÃ ĐẠT ĐƯỢC 0 EMPTY TABLES!';
    PRINT N'==========================================';
    PRINT N'✅ Tất cả tables đều có data';
    PRINT N'✅ Mục tiêu seeding đã hoàn thành';
    PRINT N'✅ Database đã sẵn sàng cho production';
END

PRINT N'';

-- Thống kê theo nhóm chức năng
PRINT N'📈 THỐNG KÊ THEO NHÓM CHỨC NĂNG:';
PRINT N'================================';

-- Core System
DECLARE @coreCount INT = (
    SELECT COUNT(*) FROM #TableCounts 
    WHERE table_name IN ('roles', 'users', 'system_roles', 'system_permissions', 'role_permissions', 'system_configurations', 'audit_logs')
    AND record_count > 0
);
PRINT N'🔧 Core System: ' + CAST(@coreCount AS NVARCHAR(10)) + N'/7 tables';

-- Education Management
DECLARE @eduCount INT = (
    SELECT COUNT(*) FROM #TableCounts 
    WHERE table_name IN ('courses', 'classrooms', 'classroom_enrollments', 'lectures', 'lecture_materials', 'lecture_recordings', 'syllabi')
    AND record_count > 0
);
PRINT N'🎓 Education: ' + CAST(@eduCount AS NVARCHAR(10)) + N'/7 tables';

-- Assessment & Assignments
DECLARE @assessCount INT = (
    SELECT COUNT(*) FROM #TableCounts 
    WHERE table_name IN ('assignments', 'submissions', 'exams', 'student_progress')
    AND record_count > 0
);
PRINT N'📝 Assessment: ' + CAST(@assessCount AS NVARCHAR(10)) + N'/4 tables';

-- Attendance & HR
DECLARE @attendanceCount INT = (
    SELECT COUNT(*) FROM #TableCounts 
    WHERE table_name IN ('attendance', 'attendance_logs', 'staff_attendance_logs', 'absences')
    AND record_count > 0
);
PRINT N'📅 Attendance: ' + CAST(@attendanceCount AS NVARCHAR(10)) + N'/4 tables';

-- Communication
DECLARE @commCount INT = (
    SELECT COUNT(*) FROM #TableCounts 
    WHERE table_name IN ('announcements', 'blogs', 'messages')
    AND record_count > 0
);
PRINT N'💬 Communication: ' + CAST(@commCount AS NVARCHAR(10)) + N'/3 tables';

-- Other
DECLARE @otherCount INT = (
    SELECT COUNT(*) FROM #TableCounts 
    WHERE table_name IN ('schedules', 'accomplishments', 'requests', 'timetable_events', 'system_monitoring')
    AND record_count > 0
);
PRINT N'🔄 Other: ' + CAST(@otherCount AS NVARCHAR(10)) + N'/5 tables';

PRINT N'';

-- Kết luận
IF @emptyTables = 0
BEGIN
    PRINT N'🏆 KẾT QUẢ CUỐI CÙNG: THÀNH CÔNG HOÀN TOÀN!';
    PRINT N'🎯 Đã đạt được mục tiêu 0 empty tables';
    PRINT N'📊 Database đã được seed đầy đủ với ' + CAST(@totalRecords AS NVARCHAR(20)) + N' records';
END
ELSE
BEGIN
    PRINT N'⚠️ KẾT QUẢ: CẦN TIẾP TỤC SEEDING';
    PRINT N'📊 Còn ' + CAST(@emptyTables AS NVARCHAR(10)) + N' tables cần được seed';
    PRINT N'🎯 Tiến độ: ' + CAST(@nonEmptyTables AS NVARCHAR(10)) + N'/' + CAST(@totalTables AS NVARCHAR(10)) + N' tables hoàn thành';
END

-- Cleanup
DROP TABLE #TableCounts;

PRINT N'';
PRINT N'✅ Báo cáo hoàn thành!';
