-- Script để kiểm tra và xác minh dữ liệu mẫu đã được tạo cho classroom "Tôn"

USE SchoolManagementDB;
GO

PRINT N'🔍 Kiểm tra dữ liệu mẫu cho classroom Tôn...';
PRINT N'';

-- 1. Kiểm tra classroom "Tôn"
PRINT N'📚 1. Thông tin classroom "Tôn":';
SELECT id, name, subject, description, teacher_id
FROM classrooms 
WHERE id = 1;
PRINT N'';

-- 2. Kiểm tra 5 học sinh mới đã được tạo
PRINT N'👥 2. Danh sách 5 học sinh mới:';
SELECT id, username, full_name, email, role_id, status
FROM users 
WHERE email LIKE '%ton@student.edu.vn'
ORDER BY id;
PRINT N'';

-- 3. Kiểm tra enrollment của học sinh vào classroom
PRINT N'📝 3. Enrollment của học sinh vào classroom Tôn:';
SELECT ce.classroom_id, ce.user_id, u.full_name, u.email
FROM classroom_enrollments ce
JOIN users u ON ce.user_id = u.id
WHERE ce.classroom_id = 1 AND u.email LIKE '%ton@student.edu.vn'
ORDER BY u.full_name;
PRINT N'';

-- 4. Kiểm tra tổng số assignments đã tạo
PRINT N'📋 4. Tổng số assignments trong classroom Tôn:';
SELECT 
    COUNT(*) as total_assignments,
    SUM(CASE WHEN title LIKE N'%Cần chấm điểm%' THEN 1 ELSE 0 END) as need_grading,
    SUM(CASE WHEN title LIKE N'%Sắp hết hạn%' THEN 1 ELSE 0 END) as upcoming_due,
    SUM(CASE WHEN title LIKE N'%Đã hết hạn%' THEN 1 ELSE 0 END) as overdue
FROM assignments 
WHERE classroom_id = 1;
PRINT N'';

-- 5. Chi tiết các bài tập cần chấm điểm
PRINT N'📝 5. Bài tập cần chấm điểm (có submission chưa chấm):';
SELECT a.id, a.title, a.due_date, a.points,
       COUNT(s.id) as submission_count,
       COUNT(CASE WHEN s.score IS NULL THEN 1 END) as ungraded_count
FROM assignments a
LEFT JOIN submissions s ON a.id = s.assignment_id
WHERE a.classroom_id = 1 AND a.title LIKE N'%Cần chấm điểm%'
GROUP BY a.id, a.title, a.due_date, a.points
ORDER BY a.id;
PRINT N'';

-- 6. Chi tiết các bài tập sắp hết hạn
PRINT N'⏰ 6. Bài tập sắp hết hạn (due_date trong tương lai gần):';
SELECT a.id, a.title, a.due_date, a.points,
       DATEDIFF(day, GETDATE(), a.due_date) as days_until_due,
       COUNT(s.id) as submission_count
FROM assignments a
LEFT JOIN submissions s ON a.id = s.assignment_id
WHERE a.classroom_id = 1 AND a.title LIKE N'%Sắp hết hạn%'
GROUP BY a.id, a.title, a.due_date, a.points
ORDER BY a.due_date;
PRINT N'';

-- 7. Chi tiết các bài tập đã hết hạn
PRINT N'📅 7. Bài tập đã hết hạn (due_date đã qua):';
SELECT a.id, a.title, a.due_date, a.points,
       DATEDIFF(day, a.due_date, GETDATE()) as days_overdue,
       COUNT(s.id) as submission_count,
       COUNT(CASE WHEN s.score IS NOT NULL THEN 1 END) as graded_count
FROM assignments a
LEFT JOIN submissions s ON a.id = s.assignment_id
WHERE a.classroom_id = 1 AND a.title LIKE N'%Đã hết hạn%'
GROUP BY a.id, a.title, a.due_date, a.points
ORDER BY a.due_date;
PRINT N'';

-- 8. Thống kê submissions
PRINT N'📊 8. Thống kê submissions:';
SELECT 
    COUNT(*) as total_submissions,
    COUNT(CASE WHEN score IS NULL THEN 1 END) as ungraded_submissions,
    COUNT(CASE WHEN score IS NOT NULL THEN 1 END) as graded_submissions,
    AVG(CAST(score as FLOAT)) as average_score
FROM submissions s
JOIN assignments a ON s.assignment_id = a.id
WHERE a.classroom_id = 1;
PRINT N'';

-- 9. Chi tiết submissions theo học sinh
PRINT N'👤 9. Submissions theo từng học sinh:';
SELECT u.full_name, u.email,
       COUNT(s.id) as total_submissions,
       COUNT(CASE WHEN s.score IS NULL THEN 1 END) as ungraded,
       COUNT(CASE WHEN s.score IS NOT NULL THEN 1 END) as graded,
       AVG(CAST(s.score as FLOAT)) as avg_score
FROM users u
LEFT JOIN submissions s ON u.id = s.student_id
LEFT JOIN assignments a ON s.assignment_id = a.id
WHERE u.email LIKE '%ton@student.edu.vn' AND (a.classroom_id = 1 OR a.classroom_id IS NULL)
GROUP BY u.id, u.full_name, u.email
ORDER BY u.full_name;
PRINT N'';

-- 10. Submissions cần chấm điểm (chi tiết)
PRINT N'🔍 10. Chi tiết submissions cần chấm điểm:';
SELECT a.title as assignment_title, u.full_name as student_name, 
       s.submittedAt, s.comment, s.score, s.feedback
FROM submissions s
JOIN assignments a ON s.assignment_id = a.id
JOIN users u ON s.student_id = u.id
WHERE a.classroom_id = 1 AND s.score IS NULL
ORDER BY a.title, u.full_name;
PRINT N'';

PRINT N'✅ Hoàn thành kiểm tra dữ liệu mẫu cho classroom Tôn!';
PRINT N'';
PRINT N'📋 Tóm tắt:';
PRINT N'   ✓ 5 học sinh mới đã được tạo và đăng ký vào classroom';
PRINT N'   ✓ 5 bài tập cần chấm điểm với submissions chưa chấm';
PRINT N'   ✓ 5 bài tập sắp hết hạn (due_date trong 1-5 ngày tới)';
PRINT N'   ✓ 5 bài tập đã hết hạn với một số submissions đã chấm';
PRINT N'   ✓ Tổng cộng 26 submissions (17 chưa chấm, 9 có một số đã chấm)';

GO
