-- ================================================================================================
-- COMPREHENSIVE VIETNAMESE ENCODING FIX SCRIPT
-- Sửa toàn bộ vấn đề encoding tiếng Việt từ gốc rễ
-- ================================================================================================

-- 1. SET DATABASE COLLATION TO SUPPORT VIETNAMESE
-- ================================================================================================
PRINT N'🔧 Bước 1: Kiểm tra và cấu hình Database Collation'

-- Kiểm tra collation hiện tại
SELECT 
    DB_NAME() AS DatabaseName,
    DATABASEPROPERTYEX(DB_NAME(), 'Collation') AS CurrentCollation

-- Backup database trước khi thay đổi (optional)
-- BACKUP DATABASE SchoolManagementDB TO DISK = 'C:\Backup\SchoolManagementDB_BeforeEncodingFix.bak'

-- 2. FIX ALL TABLE COLUMNS TO USE NVARCHAR/NTEXT
-- ================================================================================================
PRINT N'🔧 Bước 2: Sửa cấu trúc bảng để hỗ trợ Unicode'

-- Users table
ALTER TABLE users ALTER COLUMN full_name NVARCHAR(255) COLLATE Vietnamese_CI_AS;
ALTER TABLE users ALTER COLUMN email NVARCHAR(255) COLLATE Vietnamese_CI_AS;
ALTER TABLE users ALTER COLUMN password NVARCHAR(255) COLLATE Vietnamese_CI_AS;
ALTER TABLE users ALTER COLUMN department NVARCHAR(100) COLLATE Vietnamese_CI_AS;
ALTER TABLE users ALTER COLUMN status NVARCHAR(10) COLLATE Vietnamese_CI_AS;

-- Classrooms table
ALTER TABLE classrooms ALTER COLUMN name NVARCHAR(255) COLLATE Vietnamese_CI_AS;
ALTER TABLE classrooms ALTER COLUMN description NTEXT COLLATE Vietnamese_CI_AS;
ALTER TABLE classrooms ALTER COLUMN section NVARCHAR(50) COLLATE Vietnamese_CI_AS;
ALTER TABLE classrooms ALTER COLUMN subject NVARCHAR(100) COLLATE Vietnamese_CI_AS;

-- Courses table
ALTER TABLE courses ALTER COLUMN name NVARCHAR(255) COLLATE Vietnamese_CI_AS;
ALTER TABLE courses ALTER COLUMN description NVARCHAR(MAX) COLLATE Vietnamese_CI_AS;

-- Assignments table
ALTER TABLE assignments ALTER COLUMN title NVARCHAR(255) COLLATE Vietnamese_CI_AS;
ALTER TABLE assignments ALTER COLUMN description NTEXT COLLATE Vietnamese_CI_AS;

-- Submissions table
ALTER TABLE submissions ALTER COLUMN comment NVARCHAR(2000) COLLATE Vietnamese_CI_AS;
ALTER TABLE submissions ALTER COLUMN feedback NVARCHAR(MAX) COLLATE Vietnamese_CI_AS;

-- Announcements table
ALTER TABLE announcements ALTER COLUMN title NVARCHAR(255) COLLATE Vietnamese_CI_AS;
ALTER TABLE announcements ALTER COLUMN content NTEXT COLLATE Vietnamese_CI_AS;

-- Blogs table
ALTER TABLE blogs ALTER COLUMN title NVARCHAR(200) COLLATE Vietnamese_CI_AS;
ALTER TABLE blogs ALTER COLUMN description NTEXT COLLATE Vietnamese_CI_AS;
ALTER TABLE blogs ALTER COLUMN content NTEXT COLLATE Vietnamese_CI_AS;
ALTER TABLE blogs ALTER COLUMN status NVARCHAR(50) COLLATE Vietnamese_CI_AS;
ALTER TABLE blogs ALTER COLUMN tags NVARCHAR(255) COLLATE Vietnamese_CI_AS;

-- Student messages table
ALTER TABLE student_messages ALTER COLUMN subject NVARCHAR(255) COLLATE Vietnamese_CI_AS;
ALTER TABLE student_messages ALTER COLUMN content NTEXT COLLATE Vietnamese_CI_AS;
ALTER TABLE student_messages ALTER COLUMN reply NTEXT COLLATE Vietnamese_CI_AS;

-- Student questions table
ALTER TABLE student_questions ALTER COLUMN subject NVARCHAR(255) COLLATE Vietnamese_CI_AS;
ALTER TABLE student_questions ALTER COLUMN content NTEXT COLLATE Vietnamese_CI_AS;
ALTER TABLE student_questions ALTER COLUMN answer NTEXT COLLATE Vietnamese_CI_AS;

-- Notifications table
ALTER TABLE notifications ALTER COLUMN message NTEXT COLLATE Vietnamese_CI_AS;

-- Lectures table
ALTER TABLE lectures ALTER COLUMN title NVARCHAR(255) COLLATE Vietnamese_CI_AS;
ALTER TABLE lectures ALTER COLUMN content NTEXT COLLATE Vietnamese_CI_AS;

-- Course materials table
ALTER TABLE course_materials ALTER COLUMN title NVARCHAR(255) COLLATE Vietnamese_CI_AS;
ALTER TABLE course_materials ALTER COLUMN description NTEXT COLLATE Vietnamese_CI_AS;

-- Exams table
ALTER TABLE exams ALTER COLUMN title NVARCHAR(255) COLLATE Vietnamese_CI_AS;

-- Quiz questions table
ALTER TABLE quiz_questions ALTER COLUMN question_text NTEXT COLLATE Vietnamese_CI_AS;
ALTER TABLE quiz_questions ALTER COLUMN correct_answer NTEXT COLLATE Vietnamese_CI_AS;
ALTER TABLE quiz_questions ALTER COLUMN explanation NTEXT COLLATE Vietnamese_CI_AS;

-- Quiz question options table
ALTER TABLE quiz_question_options ALTER COLUMN option_text NTEXT COLLATE Vietnamese_CI_AS;

-- Student quiz answers table
ALTER TABLE student_quiz_answers ALTER COLUMN selected_options NVARCHAR(MAX) COLLATE Vietnamese_CI_AS;
ALTER TABLE student_quiz_answers ALTER COLUMN text_answer NVARCHAR(MAX) COLLATE Vietnamese_CI_AS;

-- Grading rubric table
ALTER TABLE grading_rubric ALTER COLUMN criteria_name NVARCHAR(255) COLLATE Vietnamese_CI_AS;
ALTER TABLE grading_rubric ALTER COLUMN description NTEXT COLLATE Vietnamese_CI_AS;

-- Timetable events table
ALTER TABLE timetable_events ALTER COLUMN title NVARCHAR(255) COLLATE Vietnamese_CI_AS;
ALTER TABLE timetable_events ALTER COLUMN description NTEXT COLLATE Vietnamese_CI_AS;

-- Student progress table
ALTER TABLE student_progress ALTER COLUMN notes NTEXT COLLATE Vietnamese_CI_AS;

-- Assessments table
ALTER TABLE assessments ALTER COLUMN title NVARCHAR(255) COLLATE Vietnamese_CI_AS;
ALTER TABLE assessments ALTER COLUMN description NTEXT COLLATE Vietnamese_CI_AS;

-- Lecture recordings table
ALTER TABLE lecture_recordings ALTER COLUMN title NVARCHAR(255) COLLATE Vietnamese_CI_AS;

-- 3. FIX CORRUPTED DATA
-- ================================================================================================
PRINT N'🔧 Bước 3: Sửa dữ liệu bị lỗi encoding'

-- Fix corrupted classroom names
UPDATE classrooms SET name = N'Toán cao cấp A1' WHERE name LIKE '%Toán cao c?p A1%';
UPDATE classrooms SET name = N'Văn học Việt Nam' WHERE name LIKE '%Van h?c Vi?t Nam%';
UPDATE classrooms SET name = N'Tiếng Anh giao tiếp' WHERE name LIKE '%Ti?ng Anh giao ti?p%';
UPDATE classrooms SET name = N'Công nghệ thông tin cơ bản' WHERE name LIKE '%Công ngh? thông tin co b?n%';
UPDATE classrooms SET name = N'Lập trình Java cơ bản' WHERE name LIKE '%L?p trình Java co b?n%';

-- Fix corrupted user names
UPDATE users SET full_name = N'Nguyễn Văn Toán' WHERE full_name LIKE '%Nguy?n Van Toán%';
UPDATE users SET full_name = N'Trần Thị Vân' WHERE full_name LIKE '%Tr?n Th? Van%';
UPDATE users SET full_name = N'Lê Anh' WHERE full_name LIKE '%Lê Anh%';
UPDATE users SET full_name = N'Phạm Văn Nam' WHERE full_name LIKE '%Ph?m Van Nam%';

-- Fix corrupted assignment titles
UPDATE assignments SET title = N'Bài tập về Ma trận và Định thức' WHERE title LIKE '%Bài t?p v? Ma tr?n và D?nh th?c%';
UPDATE assignments SET title = N'So sánh các tác phẩm thơ của Hồ Xuân Hương' WHERE title LIKE '%So sánh các tác ph?m tho c?a H? Xuân Huong%';
UPDATE assignments SET title = N'Bài tập test từ script' WHERE title LIKE '%Bài t?p test t? script%';

-- Fix corrupted assignment descriptions
UPDATE assignments SET description = N'Làm các bài tập từ 1-10 trang 45 sách giáo khoa.' WHERE description LIKE '%Làm các bài t?p t? 1-10 trang 45 sách giáo khoa.%';
UPDATE assignments SET description = N'Viết một đoạn văn khoảng 200 từ tả cảnh mùa xuân ở quê hương em.' WHERE description LIKE '%Vi?t m?t do?n van kho?ng 200 t? t? c?nh mùa xuân ? quê huong em.%';

-- Fix corrupted submission comments and feedback
UPDATE submissions SET comment = N'Em đã hoàn thành bài tập. Có một số bài em chưa chắc chắn.' WHERE comment LIKE '%Em dã hoàn thành bài t?p. Có m?t s? bài em chua ch?c ch?n.%';
UPDATE submissions SET comment = N'Em đã làm xong bài tập.' WHERE comment LIKE '%Em dã làm xong bài t?p.%';
UPDATE submissions SET comment = N'Em đã nộp bài, mong thầy/cô góp ý.' WHERE comment LIKE '%Em dã n?p bài, mong th?y/cô góp ý.%';

UPDATE submissions SET feedback = N'Bài làm tốt, cần cải thiện phần giải phương trình vô nghiệm.' WHERE feedback LIKE '%Bài làm t?t, c?n c?i thi?n ph?n gi?i phuong trình vô nghi?m.%';
UPDATE submissions SET feedback = N'Bài làm rất tốt, đầy đủ và chính xác.' WHERE feedback LIKE '%Bài làm r?t t?t, d?y d? và chính xác.%';

-- Fix corrupted exam titles
UPDATE exams SET title = N'Kiểm tra giữa kỳ Toán' WHERE title LIKE '%Ki?m tra gi?a k? Toán%';
UPDATE exams SET title = N'Kiểm tra cuối kỳ Toán' WHERE title LIKE '%Ki?m tra cu?i k? Toán%';
UPDATE exams SET title = N'Bài thi hết môn Văn' WHERE title LIKE '%Bài thi h?t môn V?n%';
UPDATE exams SET title = N'Thi thực hành Java' WHERE title LIKE '%Thi th?c hành Java%';

-- Fix corrupted course names
UPDATE courses SET name = N'Toán cao cấp A1' WHERE name LIKE '%Toán cao c?p A1%';
UPDATE courses SET name = N'Văn học Việt Nam' WHERE name LIKE '%Van h?c Vi?t Nam%';
UPDATE courses SET name = N'Tiếng Anh giao tiếp' WHERE name LIKE '%Ti?ng Anh giao ti?p%';
UPDATE courses SET name = N'Công nghệ thông tin cơ bản' WHERE name LIKE '%Công ngh? thông tin co b?n%';
UPDATE courses SET name = N'Lập trình Java cơ bản' WHERE name LIKE '%L?p trình Java co b?n%';

-- Fix corrupted lecture titles
UPDATE lectures SET title = N'Bài giảng về tích phân' WHERE title LIKE '%Bài gi?ng v? tích phân%';
UPDATE lectures SET title = N'Phân tích tác phẩm' WHERE title LIKE '%Phân tích tác ph?m%';
UPDATE lectures SET title = N'Ngữ pháp tiếng Anh' WHERE title LIKE '%Ng? pháp ti?ng Anh%';

-- 4. VERIFY FIXES
-- ================================================================================================
PRINT N'🔧 Bước 4: Kiểm tra kết quả sau khi sửa'

-- Check classroom names
SELECT 'Classroom Names' as CheckType, id, name FROM classrooms WHERE name LIKE N'%ế%' OR name LIKE N'%ă%' OR name LIKE N'%ố%';

-- Check user names
SELECT 'User Names' as CheckType, id, full_name FROM users WHERE full_name LIKE N'%ế%' OR full_name LIKE N'%ă%' OR full_name LIKE N'%ố%';

-- Check assignment titles
SELECT 'Assignment Titles' as CheckType, id, title FROM assignments WHERE title LIKE N'%ế%' OR title LIKE N'%ă%' OR title LIKE N'%ố%';

-- Check for any remaining corrupted data (question marks in Vietnamese text)
SELECT 'Corrupted Classrooms' as CheckType, id, name FROM classrooms WHERE name LIKE '%?%';
SELECT 'Corrupted Users' as CheckType, id, full_name FROM users WHERE full_name LIKE '%?%';
SELECT 'Corrupted Assignments' as CheckType, id, title FROM assignments WHERE title LIKE '%?%';
SELECT 'Corrupted Submissions' as CheckType, id, comment, feedback FROM submissions WHERE comment LIKE '%?%' OR feedback LIKE '%?%';

-- 5. VERIFY COLLATION SETTINGS
-- ================================================================================================
PRINT N'🔧 Bước 5: Kiểm tra collation settings'

SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    COLLATION_NAME
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME IN ('users', 'classrooms', 'assignments', 'submissions', 'announcements', 'blogs', 'courses', 'exams')
    AND DATA_TYPE IN ('varchar', 'nvarchar', 'text', 'ntext')
ORDER BY TABLE_NAME, COLUMN_NAME;

-- 6. FINAL VERIFICATION QUERIES
-- ================================================================================================
PRINT N'🔧 Bước 6: Kiểm tra cuối cùng'

-- Test Vietnamese text search
SELECT 'Search Test' as TestType, id, name FROM classrooms WHERE name LIKE N'%Toán%';
SELECT 'Search Test' as TestType, id, full_name FROM users WHERE full_name LIKE N'%Nguyễn%';
SELECT 'Search Test' as TestType, id, title FROM assignments WHERE title LIKE N'%Bài%';

-- Count records with Vietnamese characters
SELECT 
    'Vietnamese Characters Count' as TestType,
    COUNT(*) as ClassroomsWithVietnamese
FROM classrooms 
WHERE name LIKE N'%ế%' OR name LIKE N'%ă%' OR name LIKE N'%ố%' OR name LIKE N'%ư%' OR name LIKE N'%ê%';

PRINT N'✅ Hoàn thành việc sửa lỗi encoding tiếng Việt!'
PRINT N'📋 Hãy kiểm tra kết quả và khởi động lại ứng dụng Spring Boot' 