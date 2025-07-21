-- ================================================================================================
-- FINAL VIETNAMESE ENCODING MIGRATION SCRIPT
-- ================================================================================================
-- Mục đích: Migration toàn diện tất cả các cột VARCHAR/TEXT sang NVARCHAR/NTEXT
-- Ngày tạo: 2025-07-17
-- Người tạo: Augment Agent
-- ================================================================================================

USE [classroom_management_db]
GO

PRINT N'🚀 Bắt đầu Final Vietnamese Encoding Migration...'
PRINT N'📅 Ngày thực hiện: ' + CONVERT(NVARCHAR, GETDATE(), 120)
PRINT N''

-- ================================================================================================
-- 1. BACKUP RECOMMENDATIONS
-- ================================================================================================
PRINT N'⚠️  KHUYẾN NGHỊ: Hãy backup database trước khi chạy script này!'
PRINT N'   BACKUP DATABASE [classroom_management_db] TO DISK = ''C:\Backup\classroom_db_backup.bak'''
PRINT N''

-- ================================================================================================
-- 2. CLASSROOM SCHEDULES TABLE
-- ================================================================================================
PRINT N'📋 Cập nhật bảng classroom_schedules...'

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'classroom_schedules')
BEGIN
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classroom_schedules') AND name = 'location')
    BEGIN
        ALTER TABLE classroom_schedules ALTER COLUMN location NVARCHAR(255) COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã cập nhật cột location trong classroom_schedules'
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classroom_schedules') AND name = 'notes')
    BEGIN
        ALTER TABLE classroom_schedules ALTER COLUMN notes NVARCHAR(500) COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã cập nhật cột notes trong classroom_schedules'
    END
END

-- ================================================================================================
-- 3. SYLLABUSES TABLE
-- ================================================================================================
PRINT N'📋 Cập nhật bảng syllabuses...'

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'syllabuses')
BEGIN
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('syllabuses') AND name = 'title')
    BEGIN
        ALTER TABLE syllabuses ALTER COLUMN title NVARCHAR(255) COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã cập nhật cột title trong syllabuses'
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('syllabuses') AND name = 'content')
    BEGIN
        ALTER TABLE syllabuses ALTER COLUMN content NTEXT COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã cập nhật cột content trong syllabuses'
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('syllabuses') AND name = 'learning_objectives')
    BEGIN
        ALTER TABLE syllabuses ALTER COLUMN learning_objectives NVARCHAR(2000) COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã cập nhật cột learning_objectives trong syllabuses'
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('syllabuses') AND name = 'required_materials')
    BEGIN
        ALTER TABLE syllabuses ALTER COLUMN required_materials NVARCHAR(1000) COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã cập nhật cột required_materials trong syllabuses'
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('syllabuses') AND name = 'grading_criteria')
    BEGIN
        ALTER TABLE syllabuses ALTER COLUMN grading_criteria NVARCHAR(1000) COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã cập nhật cột grading_criteria trong syllabuses'
    END
END

-- ================================================================================================
-- 4. EXAM SUBMISSIONS TABLE
-- ================================================================================================
PRINT N'📋 Cập nhật bảng exam_submissions...'

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'exam_submissions')
BEGIN
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('exam_submissions') AND name = 'content')
    BEGIN
        ALTER TABLE exam_submissions ALTER COLUMN content NVARCHAR(2000) COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã cập nhật cột content trong exam_submissions'
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('exam_submissions') AND name = 'feedback')
    BEGIN
        ALTER TABLE exam_submissions ALTER COLUMN feedback NTEXT COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã cập nhật cột feedback trong exam_submissions'
    END
END

-- ================================================================================================
-- 5. LECTURES TABLE
-- ================================================================================================
PRINT N'📋 Cập nhật bảng lectures...'

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'lectures')
BEGIN
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('lectures') AND name = 'content')
    BEGIN
        ALTER TABLE lectures ALTER COLUMN content NTEXT COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã cập nhật cột content trong lectures'
    END
END

-- ================================================================================================
-- 6. COURSE MATERIALS TABLE
-- ================================================================================================
PRINT N'📋 Cập nhật bảng course_materials...'

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'course_materials')
BEGIN
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('course_materials') AND name = 'file_path')
    BEGIN
        ALTER TABLE course_materials ALTER COLUMN file_path NVARCHAR(500) COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã cập nhật cột file_path trong course_materials'
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('course_materials') AND name = 'file_name')
    BEGIN
        ALTER TABLE course_materials ALTER COLUMN file_name NVARCHAR(255) COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã cập nhật cột file_name trong course_materials'
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('course_materials') AND name = 'file_type')
    BEGIN
        ALTER TABLE course_materials ALTER COLUMN file_type NVARCHAR(100) COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã cập nhật cột file_type trong course_materials'
    END
END

-- ================================================================================================
-- 7. STUDENT MESSAGES TABLE
-- ================================================================================================
PRINT N'📋 Cập nhật bảng student_messages...'

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'student_messages')
BEGIN
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('student_messages') AND name = 'message_type')
    BEGIN
        ALTER TABLE student_messages ALTER COLUMN message_type NVARCHAR(50) COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã cập nhật cột message_type trong student_messages'
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('student_messages') AND name = 'priority')
    BEGIN
        ALTER TABLE student_messages ALTER COLUMN priority NVARCHAR(50) COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã cập nhật cột priority trong student_messages'
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('student_messages') AND name = 'status')
    BEGIN
        ALTER TABLE student_messages ALTER COLUMN status NVARCHAR(50) COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã cập nhật cột status trong student_messages'
    END
END

-- ================================================================================================
-- 8. VERIFICATION QUERIES
-- ================================================================================================
PRINT N''
PRINT N'🔍 Kiểm tra kết quả migration...'

-- Kiểm tra collation của các cột quan trọng
SELECT 
    t.name AS TableName,
    c.name AS ColumnName,
    c.collation_name AS Collation,
    ty.name AS DataType,
    c.max_length AS MaxLength
FROM sys.tables t
JOIN sys.columns c ON t.object_id = c.object_id
JOIN sys.types ty ON c.user_type_id = ty.user_type_id
WHERE c.name IN ('location', 'notes', 'title', 'content', 'learning_objectives', 'required_materials', 'grading_criteria', 'file_path', 'file_name', 'file_type', 'message_type', 'priority', 'status', 'feedback')
AND t.name IN ('classroom_schedules', 'syllabuses', 'exam_submissions', 'lectures', 'course_materials', 'student_messages')
ORDER BY t.name, c.name

-- ================================================================================================
-- 9. FINAL VERIFICATION
-- ================================================================================================
PRINT N''
PRINT N'📊 Thống kê các cột đã được migration:'

SELECT 
    'Total NVARCHAR columns' as StatType,
    COUNT(*) as Count
FROM sys.tables t
JOIN sys.columns c ON t.object_id = c.object_id
JOIN sys.types ty ON c.user_type_id = ty.user_type_id
WHERE ty.name = 'nvarchar'
AND c.collation_name = 'Vietnamese_CI_AS'

UNION ALL

SELECT 
    'Total NTEXT columns' as StatType,
    COUNT(*) as Count
FROM sys.tables t
JOIN sys.columns c ON t.object_id = c.object_id
JOIN sys.types ty ON c.user_type_id = ty.user_type_id
WHERE ty.name = 'ntext'
AND c.collation_name = 'Vietnamese_CI_AS'

-- ================================================================================================
-- 10. COMPLETION MESSAGE
-- ================================================================================================
PRINT N''
PRINT N'🎉 Final Vietnamese Encoding Migration hoàn thành!'
PRINT N'📝 Tóm tắt:'
PRINT N'   ✅ classroom_schedules: location, notes'
PRINT N'   ✅ syllabuses: title, content, learning_objectives, required_materials, grading_criteria'
PRINT N'   ✅ exam_submissions: content, feedback'
PRINT N'   ✅ lectures: content'
PRINT N'   ✅ course_materials: file_path, file_name, file_type'
PRINT N'   ✅ student_messages: message_type, priority, status'
PRINT N''
PRINT N'🔧 Khuyến nghị tiếp theo:'
PRINT N'   1. Restart ứng dụng Spring Boot'
PRINT N'   2. Kiểm tra dữ liệu tiếng Việt hiển thị chính xác'
PRINT N'   3. Chạy unit tests để verify functionality'
PRINT N'   4. Monitor performance sau khi migration'
PRINT N''
PRINT N'✅ Migration script completed successfully!'

GO
