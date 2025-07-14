-- Script để thêm dữ liệu mẫu cho classroom "Tôn" (ID = 1)
-- Sử dụng SQL Server syntax

USE SchoolManagementDB;
GO

-- Bắt đầu transaction
BEGIN TRANSACTION;

PRINT N'🔄 Bắt đầu tạo dữ liệu mẫu cho classroom Tôn...';

-- 1. Tạo 5 học sinh mới
PRINT N'📚 Tạo 5 học sinh mới...';

-- Kiểm tra và tạo học sinh 1
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'nguyenvanan.ton@student.edu.vn')
BEGIN
    INSERT INTO users (username, email, full_name, password, role_id, status, created_at, updated_at)
    VALUES ('student_ton_1', 'nguyenvanan.ton@student.edu.vn', N'Nguyễn Văn An', 
            '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIh0Ca5gZb8YS6', 1, 'active', GETDATE(), GETDATE());
    PRINT N'✅ Tạo học sinh: Nguyễn Văn An';
END

-- Kiểm tra và tạo học sinh 2
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'tranthibinh.ton@student.edu.vn')
BEGIN
    INSERT INTO users (username, email, full_name, password, role_id, status, created_at, updated_at)
    VALUES ('student_ton_2', 'tranthibinh.ton@student.edu.vn', N'Trần Thị Bình', 
            '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIh0Ca5gZb8YS6', 1, 'active', GETDATE(), GETDATE());
    PRINT N'✅ Tạo học sinh: Trần Thị Bình';
END

-- Kiểm tra và tạo học sinh 3
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'lehoangcuong.ton@student.edu.vn')
BEGIN
    INSERT INTO users (username, email, full_name, password, role_id, status, created_at, updated_at)
    VALUES ('student_ton_3', 'lehoangcuong.ton@student.edu.vn', N'Lê Hoàng Cường', 
            '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIh0Ca5gZb8YS6', 1, 'active', GETDATE(), GETDATE());
    PRINT N'✅ Tạo học sinh: Lê Hoàng Cường';
END

-- Kiểm tra và tạo học sinh 4
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'phamthidung.ton@student.edu.vn')
BEGIN
    INSERT INTO users (username, email, full_name, password, role_id, status, created_at, updated_at)
    VALUES ('student_ton_4', 'phamthidung.ton@student.edu.vn', N'Phạm Thị Dung', 
            '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIh0Ca5gZb8YS6', 1, 'active', GETDATE(), GETDATE());
    PRINT N'✅ Tạo học sinh: Phạm Thị Dung';
END

-- Kiểm tra và tạo học sinh 5
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'hoangvanem.ton@student.edu.vn')
BEGIN
    INSERT INTO users (username, email, full_name, password, role_id, status, created_at, updated_at)
    VALUES ('student_ton_5', 'hoangvanem.ton@student.edu.vn', N'Hoàng Văn Em', 
            '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxIh0Ca5gZb8YS6', 1, 'active', GETDATE(), GETDATE());
    PRINT N'✅ Tạo học sinh: Hoàng Văn Em';
END

-- 2. Đăng ký học sinh vào classroom "Tôn" (ID = 1)
PRINT N'📝 Đăng ký học sinh vào classroom...';

-- Lấy ID của các học sinh vừa tạo
DECLARE @student1_id INT = (SELECT id FROM users WHERE email = 'nguyenvanan.ton@student.edu.vn');
DECLARE @student2_id INT = (SELECT id FROM users WHERE email = 'tranthibinh.ton@student.edu.vn');
DECLARE @student3_id INT = (SELECT id FROM users WHERE email = 'lehoangcuong.ton@student.edu.vn');
DECLARE @student4_id INT = (SELECT id FROM users WHERE email = 'phamthidung.ton@student.edu.vn');
DECLARE @student5_id INT = (SELECT id FROM users WHERE email = 'hoangvanem.ton@student.edu.vn');

-- Đăng ký từng học sinh vào classroom
IF NOT EXISTS (SELECT 1 FROM classroom_enrollments WHERE classroom_id = 1 AND user_id = @student1_id)
BEGIN
    INSERT INTO classroom_enrollments (classroom_id, user_id) VALUES (1, @student1_id);
    PRINT N'✅ Đăng ký Nguyễn Văn An vào classroom Tôn';
END

IF NOT EXISTS (SELECT 1 FROM classroom_enrollments WHERE classroom_id = 1 AND user_id = @student2_id)
BEGIN
    INSERT INTO classroom_enrollments (classroom_id, user_id) VALUES (1, @student2_id);
    PRINT N'✅ Đăng ký Trần Thị Bình vào classroom Tôn';
END

IF NOT EXISTS (SELECT 1 FROM classroom_enrollments WHERE classroom_id = 1 AND user_id = @student3_id)
BEGIN
    INSERT INTO classroom_enrollments (classroom_id, user_id) VALUES (1, @student3_id);
    PRINT N'✅ Đăng ký Lê Hoàng Cường vào classroom Tôn';
END

IF NOT EXISTS (SELECT 1 FROM classroom_enrollments WHERE classroom_id = 1 AND user_id = @student4_id)
BEGIN
    INSERT INTO classroom_enrollments (classroom_id, user_id) VALUES (1, @student4_id);
    PRINT N'✅ Đăng ký Phạm Thị Dung vào classroom Tôn';
END

IF NOT EXISTS (SELECT 1 FROM classroom_enrollments WHERE classroom_id = 1 AND user_id = @student5_id)
BEGIN
    INSERT INTO classroom_enrollments (classroom_id, user_id) VALUES (1, @student5_id);
    PRINT N'✅ Đăng ký Hoàng Văn Em vào classroom Tôn';
END

-- 3. Tạo 5 bài tập cần chấm điểm (có submission nhưng chưa chấm)
PRINT N'📋 Tạo 5 bài tập cần chấm điểm...';

-- Bài tập 1
INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Bài tập Đạo hàm và Tích phân - Cần chấm điểm', 
        N'Bài tập này đã được học sinh nộp bài nhưng chưa được giáo viên chấm điểm. Cần giáo viên xem xét và đánh giá.',
        DATEADD(day, 10, GETDATE()), 100, 1);

-- Bài tập 2
INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Thực hành Giải phương trình vi phân - Cần chấm điểm', 
        N'Bài tập này đã được học sinh nộp bài nhưng chưa được giáo viên chấm điểm. Cần giáo viên xem xét và đánh giá.',
        DATEADD(day, 11, GETDATE()), 100, 1);

-- Bài tập 3
INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Bài tập Ma trận và Định thức nâng cao - Cần chấm điểm', 
        N'Bài tập này đã được học sinh nộp bài nhưng chưa được giáo viên chấm điểm. Cần giáo viên xem xét và đánh giá.',
        DATEADD(day, 12, GETDATE()), 100, 1);

-- Bài tập 4
INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Ứng dụng Toán học trong Kinh tế - Cần chấm điểm', 
        N'Bài tập này đã được học sinh nộp bài nhưng chưa được giáo viên chấm điểm. Cần giáo viên xem xét và đánh giá.',
        DATEADD(day, 13, GETDATE()), 100, 1);

-- Bài tập 5
INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Bài tập tổng hợp Giải tích - Cần chấm điểm', 
        N'Bài tập này đã được học sinh nộp bài nhưng chưa được giáo viên chấm điểm. Cần giáo viên xem xét và đánh giá.',
        DATEADD(day, 14, GETDATE()), 100, 1);

PRINT N'✅ Đã tạo 5 bài tập cần chấm điểm';

-- 4. Tạo 5 bài tập sắp hết hạn (due_date trong 1-3 ngày tới)
PRINT N'⏰ Tạo 5 bài tập sắp hết hạn...';

-- Bài tập sắp hết hạn 1
INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Bài kiểm tra Giới hạn và Liên tục - Sắp hết hạn', 
        N'Bài tập này sắp hết hạn nộp. Học sinh cần hoàn thành và nộp bài trong 1 ngày tới để không bị trễ hạn.',
        DATEADD(day, 1, GETDATE()), 80, 1);

-- Bài tập sắp hết hạn 2
INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Thực hành Tính tích phân bằng phương pháp thế - Sắp hết hạn', 
        N'Bài tập này sắp hết hạn nộp. Học sinh cần hoàn thành và nộp bài trong 2 ngày tới để không bị trễ hạn.',
        DATEADD(day, 2, GETDATE()), 85, 1);

-- Bài tập sắp hết hạn 3
INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Bài tập Chuỗi số và Chuỗi hàm - Sắp hết hạn', 
        N'Bài tập này sắp hết hạn nộp. Học sinh cần hoàn thành và nộp bài trong 3 ngày tới để không bị trễ hạn.',
        DATEADD(day, 3, GETDATE()), 90, 1);

-- Bài tập sắp hết hạn 4
INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Ứng dụng Đạo hàm trong Hình học - Sắp hết hạn', 
        N'Bài tập này sắp hết hạn nộp. Học sinh cần hoàn thành và nộp bài trong 4 ngày tới để không bị trễ hạn.',
        DATEADD(day, 4, GETDATE()), 95, 1);

-- Bài tập sắp hết hạn 5
INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Bài tập Phương trình tham số - Sắp hết hạn', 
        N'Bài tập này sắp hết hạn nộp. Học sinh cần hoàn thành và nộp bài trong 5 ngày tới để không bị trễ hạn.',
        DATEADD(day, 5, GETDATE()), 100, 1);

PRINT N'✅ Đã tạo 5 bài tập sắp hết hạn';

-- 5. Tạo 5 bài tập đã hết hạn (due_date đã qua 1-7 ngày)
PRINT N'📅 Tạo 5 bài tập đã hết hạn...';

-- Bài tập đã hết hạn 1
INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Bài tập Hàm số một biến - Đã hết hạn', 
        N'Bài tập này đã hết hạn nộp từ 1 ngày trước. Một số học sinh có thể đã nộp bài, một số có thể chưa nộp.',
        DATEADD(day, -1, GETDATE()), 90, 1);

-- Bài tập đã hết hạn 2
INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Thực hành Tính đạo hàm cấp cao - Đã hết hạn', 
        N'Bài tập này đã hết hạn nộp từ 2 ngày trước. Một số học sinh có thể đã nộp bài, một số có thể chưa nộp.',
        DATEADD(day, -2, GETDATE()), 92, 1);

-- Bài tập đã hết hạn 3
INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Bài kiểm tra Tích phân bội - Đã hết hạn', 
        N'Bài tập này đã hết hạn nộp từ 3 ngày trước. Một số học sinh có thể đã nộp bài, một số có thể chưa nộp.',
        DATEADD(day, -3, GETDATE()), 94, 1);

-- Bài tập đã hết hạn 4
INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Ứng dụng Toán học trong Vật lý - Đã hết hạn', 
        N'Bài tập này đã hết hạn nộp từ 4 ngày trước. Một số học sinh có thể đã nộp bài, một số có thể chưa nộp.',
        DATEADD(day, -4, GETDATE()), 96, 1);

-- Bài tập đã hết hạn 5
INSERT INTO assignments (title, description, due_date, points, classroom_id)
VALUES (N'Bài tập tổng hợp Đại số tuyến tính - Đã hết hạn', 
        N'Bài tập này đã hết hạn nộp từ 5 ngày trước. Một số học sinh có thể đã nộp bài, một số có thể chưa nộp.',
        DATEADD(day, -5, GETDATE()), 98, 1);

PRINT N'✅ Đã tạo 5 bài tập đã hết hạn';

-- Commit transaction
COMMIT TRANSACTION;

PRINT N'🎉 Hoàn thành tạo dữ liệu mẫu cho classroom Tôn!';
PRINT N'📊 Tổng kết:';
PRINT N'   - 5 học sinh mới đã được tạo và đăng ký vào classroom';
PRINT N'   - 5 bài tập cần chấm điểm (có submission sẽ được tạo ở script tiếp theo)';
PRINT N'   - 5 bài tập sắp hết hạn (due_date trong 1-5 ngày tới)';
PRINT N'   - 5 bài tập đã hết hạn (due_date đã qua 1-5 ngày)';

GO
