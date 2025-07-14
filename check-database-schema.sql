-- Script kiểm tra schema database và encoding issues

USE SchoolManagementDB;
GO

PRINT N'🔍 KIỂM TRA SCHEMA DATABASE VÀ ENCODING...';
PRINT N'';

-- 1. Kiểm tra collation của database
PRINT N'📊 1. Database Collation:';
SELECT name, collation_name 
FROM sys.databases 
WHERE name = 'SchoolManagementDB';
PRINT N'';

-- 2. Tìm tất cả các cột NTEXT trong database
PRINT N'📝 2. Các cột NTEXT trong database:';
SELECT 
    t.TABLE_SCHEMA,
    t.TABLE_NAME,
    c.COLUMN_NAME,
    c.DATA_TYPE,
    c.CHARACTER_MAXIMUM_LENGTH,
    c.COLLATION_NAME
FROM INFORMATION_SCHEMA.TABLES t
JOIN INFORMATION_SCHEMA.COLUMNS c ON t.TABLE_NAME = c.TABLE_NAME
WHERE c.DATA_TYPE = 'ntext'
ORDER BY t.TABLE_NAME, c.COLUMN_NAME;
PRINT N'';

-- 3. Kiểm tra các cột text/varchar có thể cần chuyển sang nvarchar
PRINT N'📝 3. Các cột TEXT/VARCHAR có thể cần Unicode:';
SELECT 
    t.TABLE_SCHEMA,
    t.TABLE_NAME,
    c.COLUMN_NAME,
    c.DATA_TYPE,
    c.CHARACTER_MAXIMUM_LENGTH,
    c.COLLATION_NAME
FROM INFORMATION_SCHEMA.TABLES t
JOIN INFORMATION_SCHEMA.COLUMNS c ON t.TABLE_NAME = c.TABLE_NAME
WHERE c.DATA_TYPE IN ('text', 'varchar', 'char')
  AND t.TABLE_TYPE = 'BASE TABLE'
ORDER BY t.TABLE_NAME, c.COLUMN_NAME;
PRINT N'';

-- 4. Kiểm tra collation của các cột quan trọng
PRINT N'🌐 4. Collation của các cột quan trọng:';
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    COLLATION_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME IN ('users', 'classrooms', 'assignments', 'submissions')
  AND DATA_TYPE IN ('nvarchar', 'varchar', 'ntext', 'text', 'nchar', 'char')
ORDER BY TABLE_NAME, COLUMN_NAME;
PRINT N'';

-- 5. Kiểm tra dữ liệu tiếng Việt hiện tại có bị lỗi encoding không
PRINT N'🔤 5. Kiểm tra dữ liệu tiếng Việt trong các bảng chính:';

PRINT N'   Users:';
SELECT id, full_name, email 
FROM users 
WHERE full_name LIKE N'%ê%' OR full_name LIKE N'%ô%' OR full_name LIKE N'%ă%'
   OR full_name LIKE N'%â%' OR full_name LIKE N'%ư%' OR full_name LIKE N'%đ%'
ORDER BY id;

PRINT N'   Classrooms:';
SELECT id, name, subject, description 
FROM classrooms 
WHERE name LIKE N'%ô%' OR description LIKE N'%ê%' OR description LIKE N'%ă%'
   OR name LIKE N'%â%' OR description LIKE N'%ư%' OR description LIKE N'%đ%'
ORDER BY id;

PRINT N'   Assignments:';
SELECT id, title, description 
FROM assignments 
WHERE title LIKE N'%ê%' OR title LIKE N'%ô%' OR title LIKE N'%ă%'
   OR title LIKE N'%â%' OR title LIKE N'%ư%' OR title LIKE N'%đ%'
   OR description LIKE N'%ê%' OR description LIKE N'%ô%' OR description LIKE N'%ă%'
ORDER BY id;

PRINT N'   Submissions:';
SELECT id, comment, feedback 
FROM submissions 
WHERE comment LIKE N'%ê%' OR comment LIKE N'%ô%' OR comment LIKE N'%ă%'
   OR comment LIKE N'%â%' OR comment LIKE N'%ư%' OR comment LIKE N'%đ%'
   OR feedback LIKE N'%ê%' OR feedback LIKE N'%ô%' OR feedback LIKE N'%ă%'
ORDER BY id;

PRINT N'';
PRINT N'✅ Hoàn thành kiểm tra schema database!';

GO
