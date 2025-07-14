-- Script để làm sạch dữ liệu Tôn một cách đúng đắn với cascade delete

USE SchoolManagementDB;
GO

SET QUOTED_IDENTIFIER ON;
GO

PRINT N'🧹 LÀM SẠCH DỮ LIỆU TÔN THEO ĐÚNG THỨ TỰ...';
PRINT N'';

-- 1. Xóa submission_attachments trước
PRINT N'🗑️ 1. Xóa submission attachments...';
DELETE FROM submission_attachments 
WHERE submission_id IN (
    SELECT s.id FROM submissions s
    JOIN assignments a ON s.assignment_id = a.id
    WHERE a.classroom_id = 1
);
PRINT N'   ✅ Đã xóa submission attachments';

-- 2. Xóa submissions
PRINT N'🗑️ 2. Xóa submissions...';
DELETE FROM submissions 
WHERE assignment_id IN (SELECT id FROM assignments WHERE classroom_id = 1);
PRINT N'   ✅ Đã xóa submissions';

-- 3. Xóa assignment_attachments
PRINT N'🗑️ 3. Xóa assignment attachments...';
DELETE FROM assignment_attachments 
WHERE assignment_id IN (SELECT id FROM assignments WHERE classroom_id = 1);
PRINT N'   ✅ Đã xóa assignment attachments';

-- 4. Xóa assignments (trừ assignment gốc nếu có)
PRINT N'🗑️ 4. Xóa assignments...';
DELETE FROM assignments 
WHERE classroom_id = 1 AND id > 1;
PRINT N'   ✅ Đã xóa assignments';

-- 5. Xóa attendance_records của học sinh test
PRINT N'🗑️ 5. Xóa attendance records...';
DELETE FROM attendance_records 
WHERE student_id IN (SELECT id FROM users WHERE email LIKE '%ton@student.edu.vn');
PRINT N'   ✅ Đã xóa attendance records';

-- 6. Bỏ qua grades (bảng không tồn tại)
PRINT N'🗑️ 6. Bỏ qua grades (bảng không tồn tại)...';
PRINT N'   ✅ Bỏ qua grades';

-- 7. Xóa classroom_enrollments
PRINT N'🗑️ 7. Xóa classroom enrollments...';
DELETE FROM classroom_enrollments 
WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%ton@student.edu.vn');
PRINT N'   ✅ Đã xóa classroom enrollments';

-- 8. Xóa các bản ghi liên quan khác nếu có
PRINT N'🗑️ 8. Xóa các bản ghi liên quan khác...';

-- Xóa student_messages (recipient_id hoặc sender_id)
DELETE FROM student_messages
WHERE recipient_id IN (SELECT id FROM users WHERE email LIKE '%ton@student.edu.vn')
   OR sender_id IN (SELECT id FROM users WHERE email LIKE '%ton@student.edu.vn');

-- Xóa student_questions
DELETE FROM student_questions
WHERE student_id IN (SELECT id FROM users WHERE email LIKE '%ton@student.edu.vn');

-- Xóa student_progress
DELETE FROM student_progress
WHERE student_id IN (SELECT id FROM users WHERE email LIKE '%ton@student.edu.vn');

PRINT N'   ✅ Đã xóa các bản ghi liên quan khác';

-- 9. Cuối cùng xóa users
PRINT N'🗑️ 9. Xóa users...';
DELETE FROM users 
WHERE email LIKE '%ton@student.edu.vn';
PRINT N'   ✅ Đã xóa học sinh test';

PRINT N'';
PRINT N'✅ HOÀN THÀNH LÀM SẠCH DỮ LIỆU!';

GO
