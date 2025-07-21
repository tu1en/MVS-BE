-- Script để kiểm tra số lượng empty tables sau khi chạy seeders
-- Chạy script này để verify kết quả seeding

USE classroom_management;

-- Kiểm tra tất cả tables và đếm số records
SELECT 
    'roles' as table_name, 
    COUNT(*) as record_count 
FROM roles
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'courses', COUNT(*) FROM courses
UNION ALL
SELECT 'classrooms', COUNT(*) FROM classrooms
UNION ALL
SELECT 'classroom_enrollments', COUNT(*) FROM classroom_enrollments
UNION ALL
SELECT 'lectures', COUNT(*) FROM lectures
UNION ALL
SELECT 'lecture_materials', COUNT(*) FROM lecture_materials
UNION ALL
SELECT 'lecture_recordings', COUNT(*) FROM lecture_recordings
UNION ALL
SELECT 'assignments', COUNT(*) FROM assignments
UNION ALL
SELECT 'submissions', COUNT(*) FROM submissions
UNION ALL
SELECT 'attendance', COUNT(*) FROM attendance
UNION ALL
SELECT 'attendance_logs', COUNT(*) FROM attendance_logs
UNION ALL
SELECT 'staff_attendance_logs', COUNT(*) FROM staff_attendance_logs
UNION ALL
SELECT 'system_roles', COUNT(*) FROM system_roles
UNION ALL
SELECT 'system_permissions', COUNT(*) FROM system_permissions
UNION ALL
SELECT 'role_permissions', COUNT(*) FROM role_permissions
UNION ALL
SELECT 'system_configurations', COUNT(*) FROM system_configurations
UNION ALL
SELECT 'audit_logs', COUNT(*) FROM audit_logs
UNION ALL
SELECT 'announcements', COUNT(*) FROM announcements
UNION ALL
SELECT 'blogs', COUNT(*) FROM blogs
UNION ALL
SELECT 'messages', COUNT(*) FROM messages
UNION ALL
SELECT 'schedules', COUNT(*) FROM schedules
UNION ALL
SELECT 'exams', COUNT(*) FROM exams
UNION ALL
SELECT 'student_progress', COUNT(*) FROM student_progress
UNION ALL
SELECT 'accomplishments', COUNT(*) FROM accomplishments
UNION ALL
SELECT 'absences', COUNT(*) FROM absences
UNION ALL
SELECT 'requests', COUNT(*) FROM requests
UNION ALL
SELECT 'timetable_events', COUNT(*) FROM timetable_events
ORDER BY record_count ASC;

-- Đếm số empty tables
SELECT 
    'EMPTY_TABLES_COUNT' as metric,
    COUNT(*) as count
FROM (
    SELECT 'roles' as table_name, COUNT(*) as record_count FROM roles
    UNION ALL
    SELECT 'users', COUNT(*) FROM users
    UNION ALL
    SELECT 'courses', COUNT(*) FROM courses
    UNION ALL
    SELECT 'classrooms', COUNT(*) FROM classrooms
    UNION ALL
    SELECT 'classroom_enrollments', COUNT(*) FROM classroom_enrollments
    UNION ALL
    SELECT 'lectures', COUNT(*) FROM lectures
    UNION ALL
    SELECT 'lecture_materials', COUNT(*) FROM lecture_materials
    UNION ALL
    SELECT 'lecture_recordings', COUNT(*) FROM lecture_recordings
    UNION ALL
    SELECT 'assignments', COUNT(*) FROM assignments
    UNION ALL
    SELECT 'submissions', COUNT(*) FROM submissions
    UNION ALL
    SELECT 'attendance', COUNT(*) FROM attendance
    UNION ALL
    SELECT 'attendance_logs', COUNT(*) FROM attendance_logs
    UNION ALL
    SELECT 'staff_attendance_logs', COUNT(*) FROM staff_attendance_logs
    UNION ALL
    SELECT 'system_roles', COUNT(*) FROM system_roles
    UNION ALL
    SELECT 'system_permissions', COUNT(*) FROM system_permissions
    UNION ALL
    SELECT 'role_permissions', COUNT(*) FROM role_permissions
    UNION ALL
    SELECT 'system_configurations', COUNT(*) FROM system_configurations
    UNION ALL
    SELECT 'audit_logs', COUNT(*) FROM audit_logs
    UNION ALL
    SELECT 'announcements', COUNT(*) FROM announcements
    UNION ALL
    SELECT 'blogs', COUNT(*) FROM blogs
    UNION ALL
    SELECT 'messages', COUNT(*) FROM messages
    UNION ALL
    SELECT 'schedules', COUNT(*) FROM schedules
    UNION ALL
    SELECT 'exams', COUNT(*) FROM exams
    UNION ALL
    SELECT 'student_progress', COUNT(*) FROM student_progress
    UNION ALL
    SELECT 'accomplishments', COUNT(*) FROM accomplishments
    UNION ALL
    SELECT 'absences', COUNT(*) FROM absences
    UNION ALL
    SELECT 'requests', COUNT(*) FROM requests
    UNION ALL
    SELECT 'timetable_events', COUNT(*) FROM timetable_events
) t 
WHERE t.record_count = 0;

-- Hiển thị các tables còn empty
SELECT 
    table_name,
    record_count
FROM (
    SELECT 'roles' as table_name, COUNT(*) as record_count FROM roles
    UNION ALL
    SELECT 'users', COUNT(*) FROM users
    UNION ALL
    SELECT 'courses', COUNT(*) FROM courses
    UNION ALL
    SELECT 'classrooms', COUNT(*) FROM classrooms
    UNION ALL
    SELECT 'classroom_enrollments', COUNT(*) FROM classroom_enrollments
    UNION ALL
    SELECT 'lectures', COUNT(*) FROM lectures
    UNION ALL
    SELECT 'lecture_materials', COUNT(*) FROM lecture_materials
    UNION ALL
    SELECT 'lecture_recordings', COUNT(*) FROM lecture_recordings
    UNION ALL
    SELECT 'assignments', COUNT(*) FROM assignments
    UNION ALL
    SELECT 'submissions', COUNT(*) FROM submissions
    UNION ALL
    SELECT 'attendance', COUNT(*) FROM attendance
    UNION ALL
    SELECT 'attendance_logs', COUNT(*) FROM attendance_logs
    UNION ALL
    SELECT 'staff_attendance_logs', COUNT(*) FROM staff_attendance_logs
    UNION ALL
    SELECT 'system_roles', COUNT(*) FROM system_roles
    UNION ALL
    SELECT 'system_permissions', COUNT(*) FROM system_permissions
    UNION ALL
    SELECT 'role_permissions', COUNT(*) FROM role_permissions
    UNION ALL
    SELECT 'system_configurations', COUNT(*) FROM system_configurations
    UNION ALL
    SELECT 'audit_logs', COUNT(*) FROM audit_logs
    UNION ALL
    SELECT 'announcements', COUNT(*) FROM announcements
    UNION ALL
    SELECT 'blogs', COUNT(*) FROM blogs
    UNION ALL
    SELECT 'messages', COUNT(*) FROM messages
    UNION ALL
    SELECT 'schedules', COUNT(*) FROM schedules
    UNION ALL
    SELECT 'exams', COUNT(*) FROM exams
    UNION ALL
    SELECT 'student_progress', COUNT(*) FROM student_progress
    UNION ALL
    SELECT 'accomplishments', COUNT(*) FROM accomplishments
    UNION ALL
    SELECT 'absences', COUNT(*) FROM absences
    UNION ALL
    SELECT 'requests', COUNT(*) FROM requests
    UNION ALL
    SELECT 'timetable_events', COUNT(*) FROM timetable_events
) t 
WHERE t.record_count = 0
ORDER BY table_name;
