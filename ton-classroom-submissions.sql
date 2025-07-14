-- Script để tạo submissions cho các bài tập trong classroom "Tôn"
-- Chạy script này sau khi đã chạy ton-classroom-sample-data.sql

USE SchoolManagementDB;
GO

SET QUOTED_IDENTIFIER ON;
GO

-- Bắt đầu transaction
BEGIN TRANSACTION;

PRINT N'🔄 Bắt đầu tạo submissions cho các bài tập...';

-- Lấy ID của các học sinh trong classroom Tôn
DECLARE @student1_id INT = (SELECT id FROM users WHERE email = 'nguyenvanan.ton@student.edu.vn');
DECLARE @student2_id INT = (SELECT id FROM users WHERE email = 'tranthibinh.ton@student.edu.vn');
DECLARE @student3_id INT = (SELECT id FROM users WHERE email = 'lehoangcuong.ton@student.edu.vn');
DECLARE @student4_id INT = (SELECT id FROM users WHERE email = 'phamthidung.ton@student.edu.vn');
DECLARE @student5_id INT = (SELECT id FROM users WHERE email = 'hoangvanem.ton@student.edu.vn');

-- Lấy ID của các bài tập cần chấm điểm (5 bài tập đầu tiên được tạo)
DECLARE @assignment1_id INT = (SELECT TOP 1 id FROM assignments WHERE title LIKE N'%Đạo hàm và Tích phân - Cần chấm điểm%' ORDER BY id DESC);
DECLARE @assignment2_id INT = (SELECT TOP 1 id FROM assignments WHERE title LIKE N'%Giải phương trình vi phân - Cần chấm điểm%' ORDER BY id DESC);
DECLARE @assignment3_id INT = (SELECT TOP 1 id FROM assignments WHERE title LIKE N'%Ma trận và Định thức nâng cao - Cần chấm điểm%' ORDER BY id DESC);
DECLARE @assignment4_id INT = (SELECT TOP 1 id FROM assignments WHERE title LIKE N'%Ứng dụng Toán học trong Kinh tế - Cần chấm điểm%' ORDER BY id DESC);
DECLARE @assignment5_id INT = (SELECT TOP 1 id FROM assignments WHERE title LIKE N'%Bài tập tổng hợp Giải tích - Cần chấm điểm%' ORDER BY id DESC);

-- Lấy ID của các bài tập đã hết hạn
DECLARE @overdue1_id INT = (SELECT TOP 1 id FROM assignments WHERE title LIKE N'%Hàm số một biến - Đã hết hạn%' ORDER BY id DESC);
DECLARE @overdue2_id INT = (SELECT TOP 1 id FROM assignments WHERE title LIKE N'%Tính đạo hàm cấp cao - Đã hết hạn%' ORDER BY id DESC);
DECLARE @overdue3_id INT = (SELECT TOP 1 id FROM assignments WHERE title LIKE N'%Tích phân bội - Đã hết hạn%' ORDER BY id DESC);

PRINT N'📝 Tạo submissions cho bài tập cần chấm điểm (chưa có grade)...';

-- Submissions cho bài tập 1 - Đạo hàm và Tích phân (4/5 học sinh nộp bài)
INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@assignment1_id, @student1_id, N'Bài làm của Nguyễn Văn An cho bài tập Đạo hàm và Tích phân. Em đã hoàn thành tất cả các câu hỏi.', DATEADD(hour, -2, GETDATE()));

INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@assignment1_id, @student2_id, N'Bài làm của Trần Thị Bình cho bài tập Đạo hàm và Tích phân. Em gặp một chút khó khăn ở câu 3.', DATEADD(hour, -5, GETDATE()));

INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@assignment1_id, @student3_id, N'Bài làm của Lê Hoàng Cường cho bài tập Đạo hàm và Tích phân. Em đã kiểm tra lại kết quả.', DATEADD(hour, -1, GETDATE()));

INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@assignment1_id, @student4_id, N'Bài làm của Phạm Thị Dung cho bài tập Đạo hàm và Tích phân. Em cần thêm thời gian để hoàn thiện.', DATEADD(hour, -3, GETDATE()));

-- Submissions cho bài tập 2 - Giải phương trình vi phân (3/5 học sinh nộp bài)
INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@assignment2_id, @student1_id, N'Bài làm của Nguyễn Văn An cho bài tập Giải phương trình vi phân. Bài này khá thú vị.', DATEADD(hour, -4, GETDATE()));

INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@assignment2_id, @student3_id, N'Bài làm của Lê Hoàng Cường cho bài tập Giải phương trình vi phân. Em đã áp dụng nhiều phương pháp khác nhau.', DATEADD(hour, -6, GETDATE()));

INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@assignment2_id, @student5_id, N'Bài làm của Hoàng Văn Em cho bài tập Giải phương trình vi phân. Em cần hỏi thêm về phương pháp giải.', DATEADD(hour, -2, GETDATE()));

-- Submissions cho bài tập 3 - Ma trận và Định thức (5/5 học sinh nộp bài)
INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@assignment3_id, @student1_id, N'Bài làm của Nguyễn Văn An cho bài tập Ma trận và Định thức nâng cao.', DATEADD(hour, -8, GETDATE()));

INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@assignment3_id, @student2_id, N'Bài làm của Trần Thị Bình cho bài tập Ma trận và Định thức nâng cao.', DATEADD(hour, -7, GETDATE()));

INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@assignment3_id, @student3_id, N'Bài làm của Lê Hoàng Cường cho bài tập Ma trận và Định thức nâng cao.', DATEADD(hour, -5, GETDATE()));

INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@assignment3_id, @student4_id, N'Bài làm của Phạm Thị Dung cho bài tập Ma trận và Định thức nâng cao.', DATEADD(hour, -4, GETDATE()));

INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@assignment3_id, @student5_id, N'Bài làm của Hoàng Văn Em cho bài tập Ma trận và Định thức nâng cao.', DATEADD(hour, -3, GETDATE()));

-- Submissions cho bài tập 4 - Ứng dụng Toán học trong Kinh tế (2/5 học sinh nộp bài)
INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@assignment4_id, @student2_id, N'Bài làm của Trần Thị Bình cho bài tập Ứng dụng Toán học trong Kinh tế. Em thấy bài này rất thực tế.', DATEADD(hour, -10, GETDATE()));

INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@assignment4_id, @student4_id, N'Bài làm của Phạm Thị Dung cho bài tập Ứng dụng Toán học trong Kinh tế. Em đã tham khảo thêm tài liệu.', DATEADD(hour, -6, GETDATE()));

-- Submissions cho bài tập 5 - Bài tập tổng hợp Giải tích (3/5 học sinh nộp bài)
INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@assignment5_id, @student1_id, N'Bài làm của Nguyễn Văn An cho bài tập tổng hợp Giải tích. Bài tổng hợp khá khó.', DATEADD(hour, -12, GETDATE()));

INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@assignment5_id, @student3_id, N'Bài làm của Lê Hoàng Cường cho bài tập tổng hợp Giải tích. Em đã ôn tập kỹ trước khi làm.', DATEADD(hour, -9, GETDATE()));

INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@assignment5_id, @student5_id, N'Bài làm của Hoàng Văn Em cho bài tập tổng hợp Giải tích. Em cần thêm thời gian để hoàn thiện.', DATEADD(hour, -7, GETDATE()));

PRINT N'✅ Đã tạo submissions cho 5 bài tập cần chấm điểm (tổng cộng 17 submissions chưa chấm)';

PRINT N'📝 Tạo submissions cho bài tập đã hết hạn (một số có grade, một số chưa)...';

-- Submissions cho bài tập đã hết hạn 1 - Hàm số một biến (3/5 học sinh nộp, 2 đã chấm)
INSERT INTO submissions (assignment_id, student_id, comment, submittedAt, score, feedback, gradedAt)
VALUES (@overdue1_id, @student1_id, N'Bài làm của Nguyễn Văn An cho bài tập Hàm số một biến.', DATEADD(day, -2, GETDATE()), 85, N'Bài làm tốt, cần chú ý thêm về tính liên tục.', DATEADD(day, -1, GETDATE()));

INSERT INTO submissions (assignment_id, student_id, comment, submittedAt, score, feedback, gradedAt)
VALUES (@overdue1_id, @student2_id, N'Bài làm của Trần Thị Bình cho bài tập Hàm số một biến.', DATEADD(day, -2, GETDATE()), 78, N'Cần cải thiện cách trình bày lời giải.', DATEADD(day, -1, GETDATE()));

INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@overdue1_id, @student3_id, N'Bài làm của Lê Hoàng Cường cho bài tập Hàm số một biến. Em nộp muộn 1 ngày.', DATEADD(hour, -18, GETDATE()));

-- Submissions cho bài tập đã hết hạn 2 - Tính đạo hàm cấp cao (4/5 học sinh nộp, 1 đã chấm)
INSERT INTO submissions (assignment_id, student_id, comment, submittedAt, score, feedback, gradedAt)
VALUES (@overdue2_id, @student1_id, N'Bài làm của Nguyễn Văn An cho bài tập Tính đạo hàm cấp cao.', DATEADD(day, -3, GETDATE()), 92, N'Xuất sắc! Phương pháp giải rất hay.', DATEADD(day, -2, GETDATE()));

INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@overdue2_id, @student2_id, N'Bài làm của Trần Thị Bình cho bài tập Tính đạo hàm cấp cao.', DATEADD(day, -3, GETDATE()));

INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@overdue2_id, @student4_id, N'Bài làm của Phạm Thị Dung cho bài tập Tính đạo hàm cấp cao.', DATEADD(day, -3, GETDATE()));

INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@overdue2_id, @student5_id, N'Bài làm của Hoàng Văn Em cho bài tập Tính đạo hàm cấp cao.', DATEADD(day, -2, GETDATE()));

-- Submissions cho bài tập đã hết hạn 3 - Tích phân bội (2/5 học sinh nộp, chưa chấm)
INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@overdue3_id, @student3_id, N'Bài làm của Lê Hoàng Cường cho bài kiểm tra Tích phân bội.', DATEADD(day, -4, GETDATE()));

INSERT INTO submissions (assignment_id, student_id, comment, submittedAt)
VALUES (@overdue3_id, @student5_id, N'Bài làm của Hoàng Văn Em cho bài kiểm tra Tích phân bội.', DATEADD(day, -4, GETDATE()));

PRINT N'✅ Đã tạo submissions cho bài tập đã hết hạn (tổng cộng 9 submissions, 3 đã chấm, 6 chưa chấm)';

-- Commit transaction
COMMIT TRANSACTION;

PRINT N'🎉 Hoàn thành tạo submissions!';
PRINT N'📊 Tổng kết submissions:';
PRINT N'   - Bài tập cần chấm điểm: 17 submissions (tất cả chưa chấm)';
PRINT N'   - Bài tập đã hết hạn: 9 submissions (3 đã chấm, 6 chưa chấm)';
PRINT N'   - Tổng cộng: 26 submissions';

GO
