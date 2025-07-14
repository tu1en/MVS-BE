-- ================================================================================================
-- COMPREHENSIVE VIETNAMESE ENCODING FIX SCRIPT V2
-- Xử lý constraints trước khi thay đổi cấu trúc cột
-- ================================================================================================

-- 1. BACKUP DATABASE TRƯỚC KHI THAY ĐỔI
-- ================================================================================================
PRINT N'🔧 Bước 1: Backup Database (Tùy chọn - Bỏ comment nếu muốn backup)'
-- BACKUP DATABASE SchoolManagementDB TO DISK = 'C:\Backup\SchoolManagementDB_BeforeEncodingFix_v2.bak'

-- 2. KIỂM TRA VÀ CẤU HÌNH DATABASE COLLATION
-- ================================================================================================
PRINT N'🔧 Bước 2: Kiểm tra Database Collation hiện tại'
SELECT 
    DB_NAME() AS DatabaseName,
    DATABASEPROPERTYEX(DB_NAME(), 'Collation') AS CurrentCollation

-- 3. TÌM VÀ XỬ LÝ CÁC CONSTRAINTS TRƯỚC KHI THAY ĐỔI CỘT
-- ================================================================================================
PRINT N'🔧 Bước 3: Tìm và xử lý constraints'

-- Tạo bảng tạm để lưu thông tin constraints
IF OBJECT_ID('tempdb..#ConstraintsToRestore') IS NOT NULL
    DROP TABLE #ConstraintsToRestore

CREATE TABLE #ConstraintsToRestore (
    TableName NVARCHAR(255),
    ConstraintName NVARCHAR(255),
    ConstraintType NVARCHAR(50),
    ColumnName NVARCHAR(255),
    ConstraintDefinition NVARCHAR(MAX)
)

-- Lấy thông tin các UNIQUE constraints
INSERT INTO #ConstraintsToRestore (TableName, ConstraintName, ConstraintType, ColumnName, ConstraintDefinition)
SELECT 
    t.name AS TableName,
    kc.name AS ConstraintName,
    'UNIQUE' AS ConstraintType,
    c.name AS ColumnName,
    'ALTER TABLE [' + t.name + '] ADD CONSTRAINT [' + kc.name + '] UNIQUE ([' + c.name + '])'
FROM sys.key_constraints kc
JOIN sys.tables t ON kc.parent_object_id = t.object_id
JOIN sys.index_columns ic ON kc.parent_object_id = ic.object_id AND kc.unique_index_id = ic.index_id
JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
WHERE kc.type = 'UQ'
AND c.name IN ('email', 'title', 'description', 'content', 'message', 'subject', 'comment', 'feedback', 'full_name', 'username')

-- Lấy thông tin các CHECK constraints
INSERT INTO #ConstraintsToRestore (TableName, ConstraintName, ConstraintType, ColumnName, ConstraintDefinition)
SELECT 
    t.name AS TableName,
    cc.name AS ConstraintName,
    'CHECK' AS ConstraintType,
    c.name AS ColumnName,
    'ALTER TABLE [' + t.name + '] ADD CONSTRAINT [' + cc.name + '] CHECK ' + cc.definition
FROM sys.check_constraints cc
JOIN sys.tables t ON cc.parent_object_id = t.object_id
JOIN sys.columns c ON cc.parent_object_id = c.object_id AND cc.parent_column_id = c.column_id
WHERE c.name IN ('email', 'title', 'description', 'content', 'message', 'subject', 'comment', 'feedback', 'full_name', 'username')

-- Hiển thị constraints sẽ bị xóa
PRINT N'📋 Constraints sẽ được xóa tạm thời:'
SELECT * FROM #ConstraintsToRestore

-- 4. XÓA CÁC CONSTRAINTS TRƯỚC KHI THAY ĐỔI CỘT
-- ================================================================================================
PRINT N'🔧 Bước 4: Xóa constraints tạm thời'

-- Xóa UNIQUE constraints
DECLARE @sql NVARCHAR(MAX) = ''
SELECT @sql = @sql + 'ALTER TABLE [' + TableName + '] DROP CONSTRAINT [' + ConstraintName + '];' + CHAR(13)
FROM #ConstraintsToRestore 
WHERE ConstraintType = 'UNIQUE'

IF LEN(@sql) > 0
BEGIN
    PRINT N'Xóa UNIQUE constraints:'
    PRINT @sql
    EXEC sp_executesql @sql
END

-- Xóa CHECK constraints
SET @sql = ''
SELECT @sql = @sql + 'ALTER TABLE [' + TableName + '] DROP CONSTRAINT [' + ConstraintName + '];' + CHAR(13)
FROM #ConstraintsToRestore 
WHERE ConstraintType = 'CHECK'

IF LEN(@sql) > 0
BEGIN
    PRINT N'Xóa CHECK constraints:'
    PRINT @sql
    EXEC sp_executesql @sql
END

-- 5. THAY ĐỔI CẤU TRÚC CỘT ĐỂ HỖ TRỢ UNICODE
-- ================================================================================================
PRINT N'🔧 Bước 5: Thay đổi cấu trúc cột để hỗ trợ Unicode'

-- Bảng Users
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Users')
BEGIN
    PRINT N'Sửa bảng Users...'
    
    -- Kiểm tra và sửa cột email
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Users') AND name = 'email')
    BEGIN
        ALTER TABLE Users ALTER COLUMN email NVARCHAR(255) COLLATE Vietnamese_CI_AS NOT NULL
        PRINT N'✅ Đã sửa cột email trong bảng Users'
    END
    
    -- Kiểm tra và sửa cột full_name
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Users') AND name = 'full_name')
    BEGIN
        ALTER TABLE Users ALTER COLUMN full_name NVARCHAR(255) COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã sửa cột full_name trong bảng Users'
    END
    
    -- Kiểm tra và sửa cột username
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Users') AND name = 'username')
    BEGIN
        ALTER TABLE Users ALTER COLUMN username NVARCHAR(255) COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã sửa cột username trong bảng Users'
    END
END

-- Bảng Assignments
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Assignments')
BEGIN
    PRINT N'Sửa bảng Assignments...'
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Assignments') AND name = 'title')
    BEGIN
        ALTER TABLE Assignments ALTER COLUMN title NVARCHAR(255) COLLATE Vietnamese_CI_AS NOT NULL
        PRINT N'✅ Đã sửa cột title trong bảng Assignments'
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Assignments') AND name = 'description')
    BEGIN
        ALTER TABLE Assignments ALTER COLUMN description NTEXT COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã sửa cột description trong bảng Assignments'
    END
END

-- Bảng Submissions
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Submissions')
BEGIN
    PRINT N'Sửa bảng Submissions...'
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Submissions') AND name = 'comment')
    BEGIN
        ALTER TABLE Submissions ALTER COLUMN comment NTEXT COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã sửa cột comment trong bảng Submissions'
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Submissions') AND name = 'feedback')
    BEGIN
        ALTER TABLE Submissions ALTER COLUMN feedback NTEXT COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã sửa cột feedback trong bảng Submissions'
    END
END

-- Bảng Classrooms
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Classrooms')
BEGIN
    PRINT N'Sửa bảng Classrooms...'
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Classrooms') AND name = 'name')
    BEGIN
        ALTER TABLE Classrooms ALTER COLUMN name NVARCHAR(255) COLLATE Vietnamese_CI_AS NOT NULL
        PRINT N'✅ Đã sửa cột name trong bảng Classrooms'
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Classrooms') AND name = 'description')
    BEGIN
        ALTER TABLE Classrooms ALTER COLUMN description NTEXT COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã sửa cột description trong bảng Classrooms'
    END
END

-- Bảng Lectures
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Lectures')
BEGIN
    PRINT N'Sửa bảng Lectures...'
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Lectures') AND name = 'title')
    BEGIN
        ALTER TABLE Lectures ALTER COLUMN title NVARCHAR(255) COLLATE Vietnamese_CI_AS NOT NULL
        PRINT N'✅ Đã sửa cột title trong bảng Lectures'
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Lectures') AND name = 'content')
    BEGIN
        ALTER TABLE Lectures ALTER COLUMN content NTEXT COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã sửa cột content trong bảng Lectures'
    END
END

-- Bảng Announcements
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Announcements')
BEGIN
    PRINT N'Sửa bảng Announcements...'
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Announcements') AND name = 'title')
    BEGIN
        ALTER TABLE Announcements ALTER COLUMN title NVARCHAR(255) COLLATE Vietnamese_CI_AS NOT NULL
        PRINT N'✅ Đã sửa cột title trong bảng Announcements'
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Announcements') AND name = 'content')
    BEGIN
        ALTER TABLE Announcements ALTER COLUMN content NTEXT COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã sửa cột content trong bảng Announcements'
    END
END

-- Bảng StudentMessages
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'StudentMessages')
BEGIN
    PRINT N'Sửa bảng StudentMessages...'
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('StudentMessages') AND name = 'subject')
    BEGIN
        ALTER TABLE StudentMessages ALTER COLUMN subject NVARCHAR(255) COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã sửa cột subject trong bảng StudentMessages'
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('StudentMessages') AND name = 'message')
    BEGIN
        ALTER TABLE StudentMessages ALTER COLUMN message NTEXT COLLATE Vietnamese_CI_AS
        PRINT N'✅ Đã sửa cột message trong bảng StudentMessages'
    END
END

-- 6. TẠO LẠI CÁC CONSTRAINTS
-- ================================================================================================
PRINT N'🔧 Bước 6: Tạo lại constraints'

-- Tạo lại UNIQUE constraints
DECLARE constraint_cursor CURSOR FOR
SELECT ConstraintDefinition FROM #ConstraintsToRestore WHERE ConstraintType = 'UNIQUE'

OPEN constraint_cursor
FETCH NEXT FROM constraint_cursor INTO @sql

WHILE @@FETCH_STATUS = 0
BEGIN
    BEGIN TRY
        EXEC sp_executesql @sql
        PRINT N'✅ Tạo lại constraint: ' + @sql
    END TRY
    BEGIN CATCH
        PRINT N'⚠️ Lỗi khi tạo lại constraint: ' + @sql
        PRINT N'Lỗi: ' + ERROR_MESSAGE()
    END CATCH
    
    FETCH NEXT FROM constraint_cursor INTO @sql
END

CLOSE constraint_cursor
DEALLOCATE constraint_cursor

-- Tạo lại CHECK constraints
DECLARE constraint_cursor CURSOR FOR
SELECT ConstraintDefinition FROM #ConstraintsToRestore WHERE ConstraintType = 'CHECK'

OPEN constraint_cursor
FETCH NEXT FROM constraint_cursor INTO @sql

WHILE @@FETCH_STATUS = 0
BEGIN
    BEGIN TRY
        EXEC sp_executesql @sql
        PRINT N'✅ Tạo lại constraint: ' + @sql
    END TRY
    BEGIN CATCH
        PRINT N'⚠️ Lỗi khi tạo lại constraint: ' + @sql
        PRINT N'Lỗi: ' + ERROR_MESSAGE()
    END CATCH
    
    FETCH NEXT FROM constraint_cursor INTO @sql
END

CLOSE constraint_cursor
DEALLOCATE constraint_cursor

-- 7. SỬA DỮ LIỆU BỊ CORRUPT
-- ================================================================================================
PRINT N'🔧 Bước 7: Sửa dữ liệu bị corrupt'

-- Sửa tên classroom bị lỗi encoding
UPDATE Classrooms 
SET name = N'Toán cao cấp A1'
WHERE name LIKE '%Toán cao c?p A1%' OR name LIKE '%ToA¡n cao cáº¥p A1%'

UPDATE Classrooms 
SET name = N'Văn học Việt Nam'
WHERE name LIKE '%Van h?c Vi?t Nam%' OR name LIKE '%VÄƒn há»?c Viá»╪t Nam%'

UPDATE Classrooms 
SET name = N'Tiếng Anh cơ bản'
WHERE name LIKE '%Ti?ng Anh co b?n%' OR name LIKE '%Tiáº¿ng Anh cÆ¡ báº£n%'

-- Sửa assignment titles bị lỗi encoding
UPDATE Assignments 
SET title = N'Bài tập về Ma trận và Định thức'
WHERE title LIKE '%Bài t?p v? Ma tr?n%' OR title LIKE '%BA i táº-p vá»? Ma tráº-n%'

UPDATE Assignments 
SET title = N'Ứng dụng Toán học trong Kinh tế'
WHERE title LIKE '%?ng d?ng Toán h?c%' OR title LIKE '%á»"ng dá»¥ng ToA¡n há»?c%'

-- Sửa user names bị lỗi encoding
UPDATE Users 
SET full_name = N'Phạm Văn Nam'
WHERE full_name LIKE '%Ph?m Van Nam%' OR full_name LIKE '%Pháº¡m VÄƒn Nam%'

UPDATE Users 
SET full_name = N'Trần Thị Bình'
WHERE full_name LIKE '%Tr?n Th? Binh%' OR full_name LIKE '%Tráº§n Thá»< BA¬nh%'

UPDATE Users 
SET full_name = N'Nguyễn Văn An'
WHERE full_name LIKE '%Nguyen Van An%' OR full_name LIKE '%Nguyá».n VÄƒn An%'

UPDATE Users 
SET full_name = N'Lê Hoàng Cường'
WHERE full_name LIKE '%Le Hoang Cuong%' OR full_name LIKE '%LAª HoA ng CÆ°á»?ng%'

UPDATE Users 
SET full_name = N'Phạm Thị Dung'
WHERE full_name LIKE '%Pham Thi Dung%' OR full_name LIKE '%Pháº¡m Thá»< Dung%'

UPDATE Users 
SET full_name = N'Hoàng Văn Em'
WHERE full_name LIKE '%Hoang Van Em%' OR full_name LIKE '%HoA ng VÄƒn Em%'

-- Sửa submission comments bị lỗi encoding
UPDATE Submissions 
SET comment = N'Bài làm của em cho bài tập. Em đã hoàn thành tất cả câu hỏi.'
WHERE comment LIKE '%BA i lA m cá»§a em%' OR comment LIKE '%Bài làm của em%'

UPDATE Submissions 
SET comment = N'Em đã tham khảo thêm tài liệu.'
WHERE comment LIKE '%Em Ä`A£ tham kháº£o thAªm tA i liá»╪u%'

UPDATE Submissions 
SET comment = N'Em cần thêm thời gian để hoàn thiện.'
WHERE comment LIKE '%Em cáº§n thAªm thá»?i gian%'

-- 8. KIỂM TRA KẾT QUẢ
-- ================================================================================================
PRINT N'🔧 Bước 8: Kiểm tra kết quả'

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
WHERE c.name IN ('email', 'full_name', 'username', 'title', 'description', 'content', 'message', 'subject', 'comment', 'feedback', 'name')
AND t.name IN ('Users', 'Assignments', 'Submissions', 'Classrooms', 'Lectures', 'Announcements', 'StudentMessages')
ORDER BY t.name, c.name

-- Kiểm tra dữ liệu đã được sửa
PRINT N'📋 Kiểm tra dữ liệu đã sửa:'

SELECT TOP 5 
    name AS ClassroomName,
    description AS ClassroomDescription
FROM Classrooms
WHERE name NOT LIKE '%?%' AND name NOT LIKE '%A¡%' AND name NOT LIKE '%á»%'

SELECT TOP 5 
    title AS AssignmentTitle,
    description AS AssignmentDescription
FROM Assignments
WHERE title NOT LIKE '%?%' AND title NOT LIKE '%A¡%' AND title NOT LIKE '%á»%'

SELECT TOP 5 
    full_name AS UserName,
    email AS UserEmail
FROM Users
WHERE full_name NOT LIKE '%?%' AND full_name NOT LIKE '%A¡%' AND full_name NOT LIKE '%á»%'

-- Xóa bảng tạm
DROP TABLE #ConstraintsToRestore

PRINT N'🎉 Hoàn thành việc sửa lỗi encoding tiếng Việt!'
PRINT N'📝 Lưu ý: Hãy kiểm tra application.properties và restart ứng dụng để áp dụng thay đổi.' 