-- Script để kiểm tra classroom "Tôn" và dữ liệu liên quan
-- Sử dụng SQL Server syntax

-- 1. Kiểm tra tất cả classrooms
SELECT 'ALL_CLASSROOMS' as check_type;
SELECT id, name, subject, description, teacher_id
FROM classrooms
ORDER BY id;

-- 2. Tìm classroom có tên chứa "Tôn"
SELECT 'TON_CLASSROOMS' as check_type;
SELECT id, name, subject, description, teacher_id
FROM classrooms
WHERE name LIKE N'%Tôn%' OR subject LIKE N'%Tôn%'
ORDER BY id;

-- 3. Kiểm tra tất cả users với role STUDENT (role_id = 1)
SELECT 'ALL_STUDENTS' as check_type;
SELECT id, username, email, full_name, role_id
FROM users
WHERE role_id = 1
ORDER BY id;

-- 4. Kiểm tra tất cả users với role TEACHER (role_id = 2)
SELECT 'ALL_TEACHERS' as check_type;
SELECT id, username, email, full_name, role_id
FROM users
WHERE role_id = 2
ORDER BY id;

-- 5. Kiểm tra classroom enrollments
SELECT 'CLASSROOM_ENROLLMENTS' as check_type;
SELECT ce.classroom_id, ce.user_id, c.name as classroom_name, u.full_name as user_name, u.role_id
FROM classroom_enrollments ce
LEFT JOIN classrooms c ON ce.classroom_id = c.id
LEFT JOIN users u ON ce.user_id = u.id
ORDER BY ce.classroom_id, u.role_id;

-- 6. Kiểm tra assignments
SELECT 'ALL_ASSIGNMENTS' as check_type;
SELECT id, title, description, due_date, points, classroom_id
FROM assignments
ORDER BY classroom_id, id;

-- 7. Kiểm tra submissions
SELECT 'ALL_SUBMISSIONS' as check_type;
SELECT id, assignment_id, student_id, comment, submitted_at, score, feedback, graded_at
FROM submissions
ORDER BY assignment_id, id;

-- 8. Thống kê tổng quan
SELECT 'SUMMARY_STATISTICS' as check_type;
SELECT 
    (SELECT COUNT(*) FROM classrooms) as total_classrooms,
    (SELECT COUNT(*) FROM users WHERE role_id = 1) as total_students,
    (SELECT COUNT(*) FROM users WHERE role_id = 2) as total_teachers,
    (SELECT COUNT(*) FROM classroom_enrollments) as total_enrollments,
    (SELECT COUNT(*) FROM assignments) as total_assignments,
    (SELECT COUNT(*) FROM submissions) as total_submissions;
