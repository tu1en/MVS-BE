-- =====================================================
-- SQL Server Vietnamese Character Encoding Fix Script
-- =====================================================
-- This script fixes Vietnamese character encoding issues
-- by updating column collations and data types

USE SchoolManagementDB;
GO

-- Check current database collation
SELECT DATABASEPROPERTYEX('SchoolManagementDB', 'Collation') AS DatabaseCollation;
GO

-- =====================================================
-- 1. Fix Submissions Table
-- =====================================================
PRINT 'Fixing Submissions table encoding...';

-- Check current column definitions
SELECT 
    c.COLUMN_NAME,
    c.DATA_TYPE,
    c.CHARACTER_MAXIMUM_LENGTH,
    c.COLLATION_NAME
FROM INFORMATION_SCHEMA.COLUMNS c
WHERE c.TABLE_NAME = 'submissions' 
AND c.COLUMN_NAME IN ('comment', 'feedback');

-- Fix comment column
IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
           WHERE TABLE_NAME = 'submissions' AND COLUMN_NAME = 'comment')
BEGIN
    ALTER TABLE submissions 
    ALTER COLUMN comment NVARCHAR(2000) COLLATE SQL_Latin1_General_CP1_CI_AS;
    PRINT 'Fixed submissions.comment column';
END

-- Fix feedback column
IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
           WHERE TABLE_NAME = 'submissions' AND COLUMN_NAME = 'feedback')
BEGIN
    ALTER TABLE submissions 
    ALTER COLUMN feedback NVARCHAR(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS;
    PRINT 'Fixed submissions.feedback column';
END

-- =====================================================
-- 2. Fix Assignments Table
-- =====================================================
PRINT 'Fixing Assignments table encoding...';

-- Fix title column
IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
           WHERE TABLE_NAME = 'assignments' AND COLUMN_NAME = 'title')
BEGIN
    ALTER TABLE assignments 
    ALTER COLUMN title NVARCHAR(255) COLLATE SQL_Latin1_General_CP1_CI_AS;
    PRINT 'Fixed assignments.title column';
END

-- Fix description column
IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
           WHERE TABLE_NAME = 'assignments' AND COLUMN_NAME = 'description')
BEGIN
    ALTER TABLE assignments 
    ALTER COLUMN description NVARCHAR(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS;
    PRINT 'Fixed assignments.description column';
END

-- =====================================================
-- 3. Fix Announcements Table
-- =====================================================
PRINT 'Fixing Announcements table encoding...';

-- Fix title column
IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
           WHERE TABLE_NAME = 'announcements' AND COLUMN_NAME = 'title')
BEGIN
    ALTER TABLE announcements 
    ALTER COLUMN title NVARCHAR(255) COLLATE SQL_Latin1_General_CP1_CI_AS;
    PRINT 'Fixed announcements.title column';
END

-- Fix content column
IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
           WHERE TABLE_NAME = 'announcements' AND COLUMN_NAME = 'content')
BEGIN
    ALTER TABLE announcements 
    ALTER COLUMN content NVARCHAR(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS;
    PRINT 'Fixed announcements.content column';
END

-- =====================================================
-- 4. Fix Blogs Table
-- =====================================================
PRINT 'Fixing Blogs table encoding...';

-- Fix title column
IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
           WHERE TABLE_NAME = 'blogs' AND COLUMN_NAME = 'title')
BEGIN
    ALTER TABLE blogs 
    ALTER COLUMN title NVARCHAR(255) COLLATE SQL_Latin1_General_CP1_CI_AS;
    PRINT 'Fixed blogs.title column';
END

-- Fix content column
IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
           WHERE TABLE_NAME = 'blogs' AND COLUMN_NAME = 'content')
BEGIN
    ALTER TABLE blogs 
    ALTER COLUMN content NVARCHAR(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS;
    PRINT 'Fixed blogs.content column';
END

-- Fix tags column
IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
           WHERE TABLE_NAME = 'blogs' AND COLUMN_NAME = 'tags')
BEGIN
    ALTER TABLE blogs 
    ALTER COLUMN tags NVARCHAR(255) COLLATE SQL_Latin1_General_CP1_CI_AS;
    PRINT 'Fixed blogs.tags column';
END

-- =====================================================
-- 5. Fix Users Table
-- =====================================================
PRINT 'Fixing Users table encoding...';

-- Fix full_name column
IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
           WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'full_name')
BEGIN
    ALTER TABLE users 
    ALTER COLUMN full_name NVARCHAR(255) COLLATE SQL_Latin1_General_CP1_CI_AS;
    PRINT 'Fixed users.full_name column';
END

-- =====================================================
-- 6. Fix Classrooms Table
-- =====================================================
PRINT 'Fixing Classrooms table encoding...';

-- Fix name column
IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
           WHERE TABLE_NAME = 'classrooms' AND COLUMN_NAME = 'name')
BEGIN
    ALTER TABLE classrooms 
    ALTER COLUMN name NVARCHAR(255) COLLATE SQL_Latin1_General_CP1_CI_AS;
    PRINT 'Fixed classrooms.name column';
END

-- Fix description column
IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
           WHERE TABLE_NAME = 'classrooms' AND COLUMN_NAME = 'description')
BEGIN
    ALTER TABLE classrooms 
    ALTER COLUMN description NVARCHAR(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS;
    PRINT 'Fixed classrooms.description column';
END

-- =====================================================
-- 7. Test Vietnamese Characters
-- =====================================================
PRINT 'Testing Vietnamese character support...';

-- Create a test table to verify encoding
IF OBJECT_ID('tempdb..#vietnamese_test') IS NOT NULL
    DROP TABLE #vietnamese_test;

CREATE TABLE #vietnamese_test (
    id INT IDENTITY(1,1),
    test_text NVARCHAR(500) COLLATE SQL_Latin1_General_CP1_CI_AS
);

-- Insert test Vietnamese text
INSERT INTO #vietnamese_test (test_text) VALUES 
(N'Chúng tôi vui mừng thông báo ra mắt hệ thống quản lý lớp học mới.'),
(N'Lịch thi giữa kỳ môn học sẽ diễn ra vào tuần tới.'),
(N'Hệ thống sẽ được bảo trì vào lúc 2 giờ sáng Chủ Nhật tuần này.'),
(N'Làm rất tốt! Bài làm chi tiết và đầy đủ.'),
(N'Cần cải thiện thêm về phần phân tích và kết luận.');

-- Display test results
SELECT 
    id,
    test_text,
    LEN(test_text) as text_length,
    CASE 
        WHEN test_text LIKE '%?%' THEN 'ENCODING_ISSUE'
        ELSE 'OK'
    END as encoding_status
FROM #vietnamese_test;

-- Clean up test table
DROP TABLE #vietnamese_test;

-- =====================================================
-- 8. Verification Queries
-- =====================================================
PRINT 'Running verification queries...';

-- Check all text columns that might contain Vietnamese
SELECT 
    t.TABLE_NAME,
    c.COLUMN_NAME,
    c.DATA_TYPE,
    c.CHARACTER_MAXIMUM_LENGTH,
    c.COLLATION_NAME
FROM INFORMATION_SCHEMA.TABLES t
JOIN INFORMATION_SCHEMA.COLUMNS c ON t.TABLE_NAME = c.TABLE_NAME
WHERE c.DATA_TYPE IN ('varchar', 'nvarchar', 'text', 'ntext')
AND t.TABLE_TYPE = 'BASE TABLE'
AND t.TABLE_NAME IN ('submissions', 'assignments', 'announcements', 'blogs', 'users', 'classrooms')
ORDER BY t.TABLE_NAME, c.COLUMN_NAME;

-- Check for any remaining encoding issues in actual data
PRINT 'Checking for encoding issues in existing data...';

-- Check submissions feedback
SELECT 
    'submissions.feedback' as table_column,
    COUNT(*) as total_records,
    SUM(CASE WHEN feedback LIKE '%?%' THEN 1 ELSE 0 END) as records_with_issues
FROM submissions 
WHERE feedback IS NOT NULL;

-- Check submissions comment
SELECT 
    'submissions.comment' as table_column,
    COUNT(*) as total_records,
    SUM(CASE WHEN comment LIKE '%?%' THEN 1 ELSE 0 END) as records_with_issues
FROM submissions 
WHERE comment IS NOT NULL;

-- Check assignments title
SELECT 
    'assignments.title' as table_column,
    COUNT(*) as total_records,
    SUM(CASE WHEN title LIKE '%?%' THEN 1 ELSE 0 END) as records_with_issues
FROM assignments 
WHERE title IS NOT NULL;

PRINT 'Vietnamese encoding fix script completed successfully!';
PRINT 'Please restart your Spring Boot application to apply the changes.';
GO
