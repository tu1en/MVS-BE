-- Script để làm sạch và tái tạo dữ liệu mẫu cho classroom "Tôn"

USE SchoolManagementDB;
GO

SET QUOTED_IDENTIFIER ON;
GO

PRINT N'🧹 BẮT ĐẦU LÀM SẠCH VÀ TÁI TẠO DỮ LIỆU MẪU...';
PRINT N'';

-- 1. Xóa dữ liệu cũ
PRINT N'🗑️ 1. Xóa dữ liệu cũ...';

-- Xóa submissions của classroom Tôn
DELETE FROM submissions 
WHERE assignment_id IN (SELECT id FROM assignments WHERE classroom_id = 1);
PRINT N'   ✅ Đã xóa submissions cũ';

-- Xóa assignments của classroom Tôn (trừ assignment gốc)
DELETE FROM assignments 
WHERE classroom_id = 1 AND id > 1;
PRINT N'   ✅ Đã xóa assignments cũ';

-- Xóa enrollments của học sinh test
DELETE FROM classroom_enrollments 
WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%ton@student.edu.vn');
PRINT N'   ✅ Đã xóa enrollments cũ';

-- Xóa học sinh test
DELETE FROM users 
WHERE email LIKE '%ton@student.edu.vn';
PRINT N'   ✅ Đã xóa học sinh test cũ';

PRINT N'';

-- 2. Tạo lại 5 học sinh mới
PRINT N'👥 2. Tạo lại 5 học sinh mới...';

INSERT INTO users (username, email, full_name, password, role_id, status, created_at, updated_at)
VALUES 
('student_ton_1', 'nguyenvanan.ton@student.edu.vn', N'Nguyễn Văn An', 
 '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIh0Ca5gZb8YS6', 1, 'active', GETDATE(), GETDATE()),
('student_ton_2', 'tranthibinh.ton@student.edu.vn', N'Trần Thị Bình', 
 '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIh0Ca5gZb8YS6', 1, 'active', GETDATE(), GETDATE()),
('student_ton_3', 'lehoangcuong.ton@student.edu.vn', N'Lê Hoàng Cường', 
 '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIh0Ca5gZb8YS6', 1, 'active', GETDATE(), GETDATE()),
('student_ton_4', 'phamthidung.ton@student.edu.vn', N'Phạm Thị Dung', 
 '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIh0Ca5gZb8YS6', 1, 'active', GETDATE(), GETDATE()),
('student_ton_5', 'hoangvanem.ton@student.edu.vn', N'Hoàng Văn Em', 
 '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIh0Ca5gZb8YS6', 1, 'active', GETDATE(), GETDATE());

PRINT N'   ✅ Đã tạo 5 học sinh mới';

-- 3. Đăng ký học sinh vào classroom
PRINT N'📝 3. Đăng ký học sinh vào classroom...';

INSERT INTO classroom_enrollments (classroom_id, user_id)
SELECT 1, id FROM users WHERE email LIKE '%ton@student.edu.vn';

PRINT N'   ✅ Đã đăng ký 5 học sinh vào classroom Tôn';

-- 4. Tạo 5 bài tập cần chấm điểm
PRINT N'📋 4. Tạo 5 bài tập cần chấm điểm...';

DECLARE @assignment1_id BIGINT, @assignment2_id BIGINT, @assignment3_id BIGINT, @assignment4_id BIGINT, @assignment5_id BIGINT;

INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Bài tập Đạo hàm và Tích phân - Cần chấm điểm', 
        N'Bài tập này đã được học sinh nộp bài nhưng chưa được giáo viên chấm điểm. Cần giáo viên xem xét và đánh giá.',
        DATEADD(day, 10, GETDATE()), 100, 1);
SET @assignment1_id = SCOPE_IDENTITY();

INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Thực hành Giải phương trình vi phân - Cần chấm điểm', 
        N'Bài tập này đã được học sinh nộp bài nhưng chưa được giáo viên chấm điểm. Cần giáo viên xem xét và đánh giá.',
        DATEADD(day, 11, GETDATE()), 100, 1);
SET @assignment2_id = SCOPE_IDENTITY();

INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Bài tập Ma trận và Định thức nâng cao - Cần chấm điểm', 
        N'Bài tập này đã được học sinh nộp bài nhưng chưa được giáo viên chấm điểm. Cần giáo viên xem xét và đánh giá.',
        DATEADD(day, 12, GETDATE()), 100, 1);
SET @assignment3_id = SCOPE_IDENTITY();

INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Ứng dụng Toán học trong Kinh tế - Cần chấm điểm', 
        N'Bài tập này đã được học sinh nộp bài nhưng chưa được giáo viên chấm điểm. Cần giáo viên xem xét và đánh giá.',
        DATEADD(day, 13, GETDATE()), 100, 1);
SET @assignment4_id = SCOPE_IDENTITY();

INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Bài tập tổng hợp Giải tích - Cần chấm điểm', 
        N'Bài tập này đã được học sinh nộp bài nhưng chưa được giáo viên chấm điểm. Cần giáo viên xem xét và đánh giá.',
        DATEADD(day, 14, GETDATE()), 100, 1);
SET @assignment5_id = SCOPE_IDENTITY();

PRINT N'   ✅ Đã tạo 5 bài tập cần chấm điểm';

-- 5. Tạo 5 bài tập sắp hết hạn
PRINT N'⏰ 5. Tạo 5 bài tập sắp hết hạn...';

INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES 
(N'Bài kiểm tra Giới hạn và Liên tục - Sắp hết hạn', 
 N'Bài tập này sắp hết hạn nộp. Học sinh cần hoàn thành và nộp bài trong 1 ngày tới để không bị trễ hạn.',
 DATEADD(day, 1, GETDATE()), 80, 1),
(N'Thực hành Tính tích phân bằng phương pháp thế - Sắp hết hạn', 
 N'Bài tập này sắp hết hạn nộp. Học sinh cần hoàn thành và nộp bài trong 2 ngày tới để không bị trễ hạn.',
 DATEADD(day, 2, GETDATE()), 85, 1),
(N'Bài tập Chuỗi số và Chuỗi hàm - Sắp hết hạn', 
 N'Bài tập này sắp hết hạn nộp. Học sinh cần hoàn thành và nộp bài trong 3 ngày tới để không bị trễ hạn.',
 DATEADD(day, 3, GETDATE()), 90, 1),
(N'Ứng dụng Đạo hàm trong Hình học - Sắp hết hạn', 
 N'Bài tập này sắp hết hạn nộp. Học sinh cần hoàn thành và nộp bài trong 4 ngày tới để không bị trễ hạn.',
 DATEADD(day, 4, GETDATE()), 95, 1),
(N'Bài tập Phương trình tham số - Sắp hết hạn', 
 N'Bài tập này sắp hết hạn nộp. Học sinh cần hoàn thành và nộp bài trong 5 ngày tới để không bị trễ hạn.',
 DATEADD(day, 5, GETDATE()), 100, 1);

PRINT N'   ✅ Đã tạo 5 bài tập sắp hết hạn';

-- 6. Tạo 5 bài tập đã hết hạn
PRINT N'📅 6. Tạo 5 bài tập đã hết hạn...';

DECLARE @overdue1_id BIGINT, @overdue2_id BIGINT, @overdue3_id BIGINT;

INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Bài tập Hàm số một biến - Đã hết hạn', 
        N'Bài tập này đã hết hạn nộp từ 1 ngày trước. Một số học sinh có thể đã nộp bài, một số có thể chưa nộp.',
        DATEADD(day, -1, GETDATE()), 90, 1);
SET @overdue1_id = SCOPE_IDENTITY();

INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Thực hành Tính đạo hàm cấp cao - Đã hết hạn', 
        N'Bài tập này đã hết hạn nộp từ 2 ngày trước. Một số học sinh có thể đã nộp bài, một số có thể chưa nộp.',
        DATEADD(day, -2, GETDATE()), 92, 1);
SET @overdue2_id = SCOPE_IDENTITY();

INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Bài kiểm tra Tích phân bội - Đã hết hạn', 
        N'Bài tập này đã hết hạn nộp từ 3 ngày trước. Một số học sinh có thể đã nộp bài, một số có thể chưa nộp.',
        DATEADD(day, -3, GETDATE()), 94, 1);
SET @overdue3_id = SCOPE_IDENTITY();

INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES 
(N'Ứng dụng Toán học trong Vật lý - Đã hết hạn', 
 N'Bài tập này đã hết hạn nộp từ 4 ngày trước. Một số học sinh có thể đã nộp bài, một số có thể chưa nộp.',
 DATEADD(day, -4, GETDATE()), 96, 1),
(N'Bài tập tổng hợp Đại số tuyến tính - Đã hết hạn', 
 N'Bài tập này đã hết hạn nộp từ 5 ngày trước. Một số học sinh có thể đã nộp bài, một số có thể chưa nộp.',
 DATEADD(day, -5, GETDATE()), 98, 1);

PRINT N'   ✅ Đã tạo 5 bài tập đã hết hạn';

PRINT N'';
PRINT N'✅ HOÀN THÀNH LÀM SẠCH VÀ TÁI TẠO DỮ LIỆU MẪU!';
PRINT N'';
PRINT N'📋 Tóm tắt:';
PRINT N'   - 5 học sinh mới đã được tạo và đăng ký vào classroom';
PRINT N'   - 5 bài tập cần chấm điểm (sẽ có submissions ở script tiếp theo)';
PRINT N'   - 5 bài tập sắp hết hạn (due_date trong 1-5 ngày tới)';
PRINT N'   - 5 bài tập đã hết hạn (due_date đã qua 1-5 ngày)';

GO
