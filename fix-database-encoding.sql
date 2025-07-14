-- Script để sửa chữa encoding và schema database

USE SchoolManagementDB;
GO

SET QUOTED_IDENTIFIER ON;
GO

PRINT N'🔧 BẮT ĐẦU SỬA CHỮA DATABASE ENCODING...';
PRINT N'';

-- 1. Chuyển đổi các cột NTEXT sang NVARCHAR(MAX) với Vietnamese collation
PRINT N'📝 1. Chuyển đổi NTEXT sang NVARCHAR(MAX)...';

-- assignments.description
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'assignments' AND COLUMN_NAME = 'description' AND DATA_TYPE = 'ntext')
BEGIN
    ALTER TABLE assignments ALTER COLUMN description NVARCHAR(MAX) COLLATE Vietnamese_CI_AS;
    PRINT N'   ✅ assignments.description -> NVARCHAR(MAX)';
END

-- blogs.content
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'blogs' AND COLUMN_NAME = 'content' AND DATA_TYPE = 'ntext')
BEGIN
    ALTER TABLE blogs ALTER COLUMN content NVARCHAR(MAX) COLLATE Vietnamese_CI_AS;
    PRINT N'   ✅ blogs.content -> NVARCHAR(MAX)';
END

-- lectures.content
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'lectures' AND COLUMN_NAME = 'content' AND DATA_TYPE = 'ntext')
BEGIN
    ALTER TABLE lectures ALTER COLUMN content NVARCHAR(MAX) COLLATE Vietnamese_CI_AS;
    PRINT N'   ✅ lectures.content -> NVARCHAR(MAX)';
END

-- submissions.comment
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'submissions' AND COLUMN_NAME = 'comment' AND DATA_TYPE = 'ntext')
BEGIN
    ALTER TABLE submissions ALTER COLUMN comment NVARCHAR(MAX) COLLATE Vietnamese_CI_AS;
    PRINT N'   ✅ submissions.comment -> NVARCHAR(MAX)';
END

-- submissions.feedback
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'submissions' AND COLUMN_NAME = 'feedback' AND DATA_TYPE = 'ntext')
BEGIN
    ALTER TABLE submissions ALTER COLUMN feedback NVARCHAR(MAX) COLLATE Vietnamese_CI_AS;
    PRINT N'   ✅ submissions.feedback -> NVARCHAR(MAX)';
END

PRINT N'';

-- 2. Cập nhật collation cho các cột quan trọng khác
PRINT N'🌐 2. Cập nhật collation cho các cột quan trọng...';

-- assignments.title (nếu chưa có Vietnamese collation)
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'assignments' AND COLUMN_NAME = 'title' AND COLLATION_NAME != 'Vietnamese_CI_AS')
BEGIN
    ALTER TABLE assignments ALTER COLUMN title NVARCHAR(255) COLLATE Vietnamese_CI_AS;
    PRINT N'   ✅ assignments.title -> Vietnamese_CI_AS';
END

-- classrooms.subject
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'classrooms' AND COLUMN_NAME = 'subject' AND COLLATION_NAME != 'Vietnamese_CI_AS')
BEGIN
    ALTER TABLE classrooms ALTER COLUMN subject NVARCHAR(100) COLLATE Vietnamese_CI_AS;
    PRINT N'   ✅ classrooms.subject -> Vietnamese_CI_AS';
END

-- classrooms.section
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'classrooms' AND COLUMN_NAME = 'section' AND COLLATION_NAME != 'Vietnamese_CI_AS')
BEGIN
    ALTER TABLE classrooms ALTER COLUMN section NVARCHAR(50) COLLATE Vietnamese_CI_AS;
    PRINT N'   ✅ classrooms.section -> Vietnamese_CI_AS';
END

-- users.department
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'department' AND COLLATION_NAME != 'Vietnamese_CI_AS')
BEGIN
    ALTER TABLE users ALTER COLUMN department NVARCHAR(100) COLLATE Vietnamese_CI_AS;
    PRINT N'   ✅ users.department -> Vietnamese_CI_AS';
END

-- users.status
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'status' AND COLLATION_NAME != 'Vietnamese_CI_AS')
BEGIN
    ALTER TABLE users ALTER COLUMN status NVARCHAR(20) COLLATE Vietnamese_CI_AS;
    PRINT N'   ✅ users.status -> Vietnamese_CI_AS';
END

PRINT N'';

-- 3. Chuyển đổi một số cột TEXT/VARCHAR quan trọng sang NVARCHAR
PRINT N'📄 3. Chuyển đổi TEXT/VARCHAR sang NVARCHAR...';

-- syllabuses.content
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'syllabuses' AND COLUMN_NAME = 'content' AND DATA_TYPE = 'varchar')
BEGIN
    ALTER TABLE syllabuses ALTER COLUMN content NVARCHAR(MAX) COLLATE Vietnamese_CI_AS;
    PRINT N'   ✅ syllabuses.content -> NVARCHAR(MAX)';
END

-- syllabuses.title
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'syllabuses' AND COLUMN_NAME = 'title' AND DATA_TYPE = 'varchar')
BEGIN
    ALTER TABLE syllabuses ALTER COLUMN title NVARCHAR(255) COLLATE Vietnamese_CI_AS;
    PRINT N'   ✅ syllabuses.title -> NVARCHAR(255)';
END

-- syllabuses.learning_objectives
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'syllabuses' AND COLUMN_NAME = 'learning_objectives' AND DATA_TYPE = 'varchar')
BEGIN
    ALTER TABLE syllabuses ALTER COLUMN learning_objectives NVARCHAR(MAX) COLLATE Vietnamese_CI_AS;
    PRINT N'   ✅ syllabuses.learning_objectives -> NVARCHAR(MAX)';
END

-- syllabuses.grading_criteria
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'syllabuses' AND COLUMN_NAME = 'grading_criteria' AND DATA_TYPE = 'varchar')
BEGIN
    ALTER TABLE syllabuses ALTER COLUMN grading_criteria NVARCHAR(MAX) COLLATE Vietnamese_CI_AS;
    PRINT N'   ✅ syllabuses.grading_criteria -> NVARCHAR(MAX)';
END

-- syllabuses.required_materials
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'syllabuses' AND COLUMN_NAME = 'required_materials' AND DATA_TYPE = 'varchar')
BEGIN
    ALTER TABLE syllabuses ALTER COLUMN required_materials NVARCHAR(MAX) COLLATE Vietnamese_CI_AS;
    PRINT N'   ✅ syllabuses.required_materials -> NVARCHAR(MAX)';
END

PRINT N'';

-- 4. Cập nhật dữ liệu tiếng Việt bị lỗi encoding (nếu có)
PRINT N'🔤 4. Sửa chữa dữ liệu tiếng Việt bị lỗi encoding...';

-- Cập nhật lại dữ liệu classroom "Tôn" để đảm bảo encoding đúng
UPDATE classrooms 
SET name = N'Toán cao cấp A1',
    subject = N'Mathematics',
    description = N'Lớp toán cao cấp cho kỳ 1, bao gồm giải tích, đại số tuyến tính.'
WHERE id = 1;

PRINT N'   ✅ Đã cập nhật classroom "Tôn"';

-- Cập nhật lại tên học sinh để đảm bảo encoding đúng
UPDATE users SET full_name = N'Nguyễn Văn An' WHERE email = 'nguyenvanan.ton@student.edu.vn';
UPDATE users SET full_name = N'Trần Thị Bình' WHERE email = 'tranthibinh.ton@student.edu.vn';
UPDATE users SET full_name = N'Lê Hoàng Cường' WHERE email = 'lehoangcuong.ton@student.edu.vn';
UPDATE users SET full_name = N'Phạm Thị Dung' WHERE email = 'phamthidung.ton@student.edu.vn';
UPDATE users SET full_name = N'Hoàng Văn Em' WHERE email = 'hoangvanem.ton@student.edu.vn';

PRINT N'   ✅ Đã cập nhật tên học sinh';

-- Cập nhật lại title assignments để đảm bảo encoding đúng
UPDATE assignments 
SET title = N'Bài tập Đạo hàm và Tích phân - Cần chấm điểm',
    description = N'Bài tập này đã được học sinh nộp bài nhưng chưa được giáo viên chấm điểm. Cần giáo viên xem xét và đánh giá.'
WHERE title LIKE N'%Đạo hàm và Tích phân%';

UPDATE assignments 
SET title = N'Thực hành Giải phương trình vi phân - Cần chấm điểm',
    description = N'Bài tập này đã được học sinh nộp bài nhưng chưa được giáo viên chấm điểm. Cần giáo viên xem xét và đánh giá.'
WHERE title LIKE N'%Giải phương trình vi phân%';

UPDATE assignments 
SET title = N'Bài tập Ma trận và Định thức nâng cao - Cần chấm điểm',
    description = N'Bài tập này đã được học sinh nộp bài nhưng chưa được giáo viên chấm điểm. Cần giáo viên xem xét và đánh giá.'
WHERE title LIKE N'%Ma trận và Định thức%';

UPDATE assignments 
SET title = N'Ứng dụng Toán học trong Kinh tế - Cần chấm điểm',
    description = N'Bài tập này đã được học sinh nộp bài nhưng chưa được giáo viên chấm điểm. Cần giáo viên xem xét và đánh giá.'
WHERE title LIKE N'%Ứng dụng Toán học trong Kinh tế%';

UPDATE assignments 
SET title = N'Bài tập tổng hợp Giải tích - Cần chấm điểm',
    description = N'Bài tập này đã được học sinh nộp bài nhưng chưa được giáo viên chấm điểm. Cần giáo viên xem xét và đánh giá.'
WHERE title LIKE N'%tổng hợp Giải tích%';

PRINT N'   ✅ Đã cập nhật assignments cần chấm điểm';

PRINT N'';
PRINT N'✅ HOÀN THÀNH SỬA CHỮA DATABASE ENCODING!';
PRINT N'';
PRINT N'📋 Tóm tắt thay đổi:';
PRINT N'   - Chuyển đổi NTEXT -> NVARCHAR(MAX) với Vietnamese collation';
PRINT N'   - Cập nhật collation cho các cột quan trọng';
PRINT N'   - Chuyển đổi TEXT/VARCHAR -> NVARCHAR cho syllabuses';
PRINT N'   - Sửa chữa dữ liệu tiếng Việt bị lỗi encoding';

GO
