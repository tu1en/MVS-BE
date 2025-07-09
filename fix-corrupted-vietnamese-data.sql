-- =====================================================
-- Fix Corrupted Vietnamese Data Script
-- =====================================================
-- This script attempts to fix existing corrupted Vietnamese text data
-- by replacing common corrupted patterns with correct Vietnamese characters

USE SchoolManagementDB;
GO

PRINT '🔧 Starting Vietnamese data corruption fix...';

-- =====================================================
-- 1. Create mapping table for common corrupted characters
-- =====================================================
IF OBJECT_ID('tempdb..#vietnamese_char_mapping') IS NOT NULL
    DROP TABLE #vietnamese_char_mapping;

CREATE TABLE #vietnamese_char_mapping (
    corrupted_char NVARCHAR(10),
    correct_char NVARCHAR(10),
    description NVARCHAR(100)
);

-- Insert common Vietnamese character corruption mappings
INSERT INTO #vietnamese_char_mapping (corrupted_char, correct_char, description) VALUES
-- Vowels with diacritics
('?', 'ă', 'a with breve'),
('?', 'â', 'a with circumflex'),
('?', 'á', 'a with acute'),
('?', 'à', 'a with grave'),
('?', 'ả', 'a with hook above'),
('?', 'ã', 'a with tilde'),
('?', 'ạ', 'a with dot below'),
('?', 'ắ', 'a breve with acute'),
('?', 'ằ', 'a breve with grave'),
('?', 'ẳ', 'a breve with hook above'),
('?', 'ẵ', 'a breve with tilde'),
('?', 'ặ', 'a breve with dot below'),
('?', 'ấ', 'a circumflex with acute'),
('?', 'ầ', 'a circumflex with grave'),
('?', 'ẩ', 'a circumflex with hook above'),
('?', 'ẫ', 'a circumflex with tilde'),
('?', 'ậ', 'a circumflex with dot below'),
-- E vowels
('?', 'é', 'e with acute'),
('?', 'è', 'e with grave'),
('?', 'ẻ', 'e with hook above'),
('?', 'ẽ', 'e with tilde'),
('?', 'ẹ', 'e with dot below'),
('?', 'ê', 'e with circumflex'),
('?', 'ế', 'e circumflex with acute'),
('?', 'ề', 'e circumflex with grave'),
('?', 'ể', 'e circumflex with hook above'),
('?', 'ễ', 'e circumflex with tilde'),
('?', 'ệ', 'e circumflex with dot below'),
-- I vowels
('?', 'í', 'i with acute'),
('?', 'ì', 'i with grave'),
('?', 'ỉ', 'i with hook above'),
('?', 'ĩ', 'i with tilde'),
('?', 'ị', 'i with dot below'),
-- O vowels
('?', 'ó', 'o with acute'),
('?', 'ò', 'o with grave'),
('?', 'ỏ', 'o with hook above'),
('?', 'õ', 'o with tilde'),
('?', 'ọ', 'o with dot below'),
('?', 'ô', 'o with circumflex'),
('?', 'ố', 'o circumflex with acute'),
('?', 'ồ', 'o circumflex with grave'),
('?', 'ổ', 'o circumflex with hook above'),
('?', 'ỗ', 'o circumflex with tilde'),
('?', 'ộ', 'o circumflex with dot below'),
('?', 'ơ', 'o with horn'),
('?', 'ớ', 'o horn with acute'),
('?', 'ờ', 'o horn with grave'),
('?', 'ở', 'o horn with hook above'),
('?', 'ỡ', 'o horn with tilde'),
('?', 'ợ', 'o horn with dot below'),
-- U vowels
('?', 'ú', 'u with acute'),
('?', 'ù', 'u with grave'),
('?', 'ủ', 'u with hook above'),
('?', 'ũ', 'u with tilde'),
('?', 'ụ', 'u with dot below'),
('?', 'ư', 'u with horn'),
('?', 'ứ', 'u horn with acute'),
('?', 'ừ', 'u horn with grave'),
('?', 'ử', 'u horn with hook above'),
('?', 'ữ', 'u horn with tilde'),
('?', 'ự', 'u horn with dot below'),
-- Y vowels
('?', 'ý', 'y with acute'),
('?', 'ỳ', 'y with grave'),
('?', 'ỷ', 'y with hook above'),
('?', 'ỹ', 'y with tilde'),
('?', 'ỵ', 'y with dot below'),
-- Consonants
('?', 'đ', 'd with stroke'),
('?', 'Đ', 'D with stroke');

PRINT '✅ Vietnamese character mapping table created';

-- =====================================================
-- 2. Function to fix Vietnamese text
-- =====================================================
-- Note: SQL Server doesn't support user-defined functions in temp tables easily
-- So we'll use a different approach with REPLACE statements

-- =====================================================
-- 3. Fix Submissions Table
-- =====================================================
PRINT '🔧 Fixing Vietnamese text in submissions table...';

-- Backup original data first
IF OBJECT_ID('submissions_backup_vietnamese_fix') IS NOT NULL
    DROP TABLE submissions_backup_vietnamese_fix;

SELECT * INTO submissions_backup_vietnamese_fix FROM submissions;
PRINT '✅ Submissions backup created';

-- Fix comment field
UPDATE submissions 
SET comment = 
    REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
    REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
    REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
    comment,
    '?ng', 'ứng'),
    '?c', 'ức'),
    '?i', 'ới'),
    '?n', 'ần'),
    '?t', 'ất'),
    '?m', 'ầm'),
    '?p', 'ập'),
    '?nh', 'ành'),
    '?ch', 'ách'),
    '?y', 'ấy'),
    '?u', 'ầu'),
    'h?', 'hệ'),
    'th?', 'thể'),
    'qu?', 'quả'),
    'tr?', 'trước'),
    'gi?', 'giữa'),
    'l?', 'lớp'),
    'h?c', 'học'),
    'sinh', 'sinh'),
    'vi?n', 'viên'),
    'thi?u', 'thiếu')
WHERE comment IS NOT NULL AND comment LIKE '%?%';

-- Fix feedback field with common Vietnamese words
UPDATE submissions 
SET feedback = 
    REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
    REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
    REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
    feedback,
    'L?m r?t t?t', 'Làm rất tốt'),
    'C?n c?i thi?n', 'Cần cải thiện'),
    'B?i l?m t?t', 'Bài làm tốt'),
    'K?t qu? t?t', 'Kết quả tốt'),
    'Thi?u chi ti?t', 'Thiếu chi tiết'),
    'C?n b? sung', 'Cần bổ sung'),
    'R?t t?t', 'Rất tốt'),
    'T?t l?m', 'Tốt lắm'),
    'Xu?t s?c', 'Xuất sắc'),
    'H?y ti?p t?c', 'Hãy tiếp tục'),
    'C? g?ng h?n', 'Cố gắng hơn'),
    'L?m t?t h?n', 'Làm tốt hơn'),
    'K?t qu?', 'Kết quả'),
    'ph?n tích', 'phân tích'),
    'k?t lu?n', 'kết luận'),
    'gi?i thi?u', 'giới thiệu'),
    'n?i dung', 'nội dung'),
    'ch?t l??ng', 'chất lượng'),
    'hoàn thi?n', 'hoàn thiện'),
    'c?p nh?t', 'cập nhật'),
    'th?c hi?n', 'thực hiện')
WHERE feedback IS NOT NULL AND feedback LIKE '%?%';

PRINT '✅ Submissions table Vietnamese text fixed';

-- =====================================================
-- 4. Fix Assignments Table
-- =====================================================
PRINT '🔧 Fixing Vietnamese text in assignments table...';

-- Backup original data
IF OBJECT_ID('assignments_backup_vietnamese_fix') IS NOT NULL
    DROP TABLE assignments_backup_vietnamese_fix;

SELECT * INTO assignments_backup_vietnamese_fix FROM assignments;
PRINT '✅ Assignments backup created';

-- Fix title field
UPDATE assignments 
SET title = 
    REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
    REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
    title,
    'B?i t?p', 'Bài tập'),
    'Ki?m tra', 'Kiểm tra'),
    'Thi gi?a k?', 'Thi giữa kỳ'),
    'Thi cu?i k?', 'Thi cuối kỳ'),
    'D? án', 'Dự án'),
    'Báo cáo', 'Báo cáo'),
    'Th?c hành', 'Thực hành'),
    'L? thuy?t', 'Lý thuyết'),
    'Nghiên c?u', 'Nghiên cứu'),
    'Phân tích', 'Phân tích'),
    'Thi?t k?', 'Thiết kế'),
    'C?i thi?n', 'Cải thiện'),
    'Hoàn thi?n', 'Hoàn thiện'),
    'Th?c hi?n', 'Thực hiện')
WHERE title IS NOT NULL AND title LIKE '%?%';

-- Fix description field
UPDATE assignments 
SET description = 
    REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
    REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
    REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
    description,
    'H??ng d?n', 'Hướng dẫn'),
    'Yêu c?u', 'Yêu cầu'),
    'M?c tiêu', 'Mục tiêu'),
    'N?i dung', 'Nội dung'),
    'Th?i gian', 'Thời gian'),
    'H?n n?p', 'Hạn nộp'),
    'Tiêu chí', 'Tiêu chí'),
    'Đánh giá', 'Đánh giá'),
    'Ch?m ?i?m', 'Chấm điểm'),
    'K?t qu?', 'Kết quả'),
    'Hoàn thành', 'Hoàn thành'),
    'N?p bài', 'Nộp bài'),
    'Làm bài', 'Làm bài'),
    'Gi?i thi?u', 'Giới thiệu'),
    'Th?c hi?n', 'Thực hiện'),
    'C?n thi?t', 'Cần thiết'),
    'Quan tr?ng', 'Quan trọng'),
    'Ch? ý', 'Chú ý'),
    'L?u ý', 'Lưu ý'),
    'Ghi chú', 'Ghi chú'),
    'Tham kh?o', 'Tham khảo')
WHERE description IS NOT NULL AND description LIKE '%?%';

PRINT '✅ Assignments table Vietnamese text fixed';

-- =====================================================
-- 5. Fix Announcements Table
-- =====================================================
PRINT '🔧 Fixing Vietnamese text in announcements table...';

-- Backup original data
IF OBJECT_ID('announcements_backup_vietnamese_fix') IS NOT NULL
    DROP TABLE announcements_backup_vietnamese_fix;

SELECT * INTO announcements_backup_vietnamese_fix FROM announcements;
PRINT '✅ Announcements backup created';

-- Fix title field
UPDATE announcements 
SET title = 
    REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
    REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
    title,
    'Thông báo', 'Thông báo'),
    'C?p nh?t', 'Cập nhật'),
    'Th?y ??i', 'Thay đổi'),
    'H?y b?', 'Hủy bỏ'),
    'D?i l?ch', 'Dời lịch'),
    'L?ch thi', 'Lịch thi'),
    'L?ch h?c', 'Lịch học'),
    'B?o trì', 'Bảo trì'),
    'N?ng c?p', 'Nâng cấp'),
    'S?a ch?a', 'Sửa chữa'),
    'Hoàn thành', 'Hoàn thành'),
    'B?t ??u', 'Bắt đầu'),
    'K?t thúc', 'Kết thúc'),
    'Quan tr?ng', 'Quan trọng')
WHERE title IS NOT NULL AND title LIKE '%?%';

-- Fix content field with common announcement phrases
UPDATE announcements 
SET content = 
    REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
    REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
    REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
    content,
    'Chúng tôi vui m?ng thông báo', 'Chúng tôi vui mừng thông báo'),
    'ra m?t h? th?ng qu?n lý l?p h?c m?i', 'ra mắt hệ thống quản lý lớp học mới'),
    'H? th?ng cung c?p nhi?u tính nang h?u ích', 'Hệ thống cung cấp nhiều tính năng hữu ích'),
    'cho c? giáo viên và h?c sinh', 'cho cả giáo viên và học sinh'),
    'L?ch thi gi?a k? môn h?c', 'Lịch thi giữa kỳ môn học'),
    's? di?n ra vào tu?n t?i', 'sẽ diễn ra vào tuần tới'),
    'Chi ti?t v? th?i gian và d?a di?m', 'Chi tiết về thời gian và địa điểm'),
    's? du?c c?p nh?t s?m', 'sẽ được cập nhật sớm'),
    'H? th?ng s? du?c b?o trì', 'Hệ thống sẽ được bảo trì'),
    'vào lúc 2 gi? sáng Ch? Nh?t', 'vào lúc 2 giờ sáng Chủ Nhật'),
    'tu?n này', 'tuần này'),
    'Vui lòng luu l?i công vi?c', 'Vui lòng lưu lại công việc'),
    'c?a b?n tru?c th?i gian này', 'của bạn trước thời gian này'),
    'Xin chân thành c?m ?n', 'Xin chân thành cảm ơn'),
    'M?i th?c m?c xin liên h?', 'Mọi thắc mắc xin liên hệ'),
    'Tr?n tr?ng thông báo', 'Trân trọng thông báo'),
    'K?nh g?i', 'Kính gửi'),
    'Th?y cô và các em h?c sinh', 'Thầy cô và các em học sinh'),
    'Ban Giám hi?u', 'Ban Giám hiệu'),
    'Ph?ng Đào t?o', 'Phòng Đào tạo'),
    'Chúc m?i ng??i', 'Chúc mọi người')
WHERE content IS NOT NULL AND content LIKE '%?%';

PRINT '✅ Announcements table Vietnamese text fixed';

-- =====================================================
-- 6. Verification and Results
-- =====================================================
PRINT '🔍 Verifying Vietnamese text fixes...';

-- Check remaining corruption in submissions
SELECT 
    'submissions.comment' as table_column,
    COUNT(*) as total_records,
    SUM(CASE WHEN comment LIKE '%?%' THEN 1 ELSE 0 END) as records_still_corrupted,
    CAST(SUM(CASE WHEN comment LIKE '%?%' THEN 1 ELSE 0 END) * 100.0 / COUNT(*) AS DECIMAL(5,2)) as corruption_percentage
FROM submissions 
WHERE comment IS NOT NULL;

SELECT 
    'submissions.feedback' as table_column,
    COUNT(*) as total_records,
    SUM(CASE WHEN feedback LIKE '%?%' THEN 1 ELSE 0 END) as records_still_corrupted,
    CAST(SUM(CASE WHEN feedback LIKE '%?%' THEN 1 ELSE 0 END) * 100.0 / COUNT(*) AS DECIMAL(5,2)) as corruption_percentage
FROM submissions 
WHERE feedback IS NOT NULL;

-- Check assignments
SELECT 
    'assignments.title' as table_column,
    COUNT(*) as total_records,
    SUM(CASE WHEN title LIKE '%?%' THEN 1 ELSE 0 END) as records_still_corrupted,
    CAST(SUM(CASE WHEN title LIKE '%?%' THEN 1 ELSE 0 END) * 100.0 / COUNT(*) AS DECIMAL(5,2)) as corruption_percentage
FROM assignments 
WHERE title IS NOT NULL;

-- Check announcements
SELECT 
    'announcements.content' as table_column,
    COUNT(*) as total_records,
    SUM(CASE WHEN content LIKE '%?%' THEN 1 ELSE 0 END) as records_still_corrupted,
    CAST(SUM(CASE WHEN content LIKE '%?%' THEN 1 ELSE 0 END) * 100.0 / COUNT(*) AS DECIMAL(5,2)) as corruption_percentage
FROM announcements 
WHERE content IS NOT NULL;

-- Clean up temporary table
DROP TABLE #vietnamese_char_mapping;

PRINT '✅ Vietnamese data corruption fix completed!';
PRINT '📊 Check the verification results above to see the improvement';
PRINT '💾 Backup tables created: submissions_backup_vietnamese_fix, assignments_backup_vietnamese_fix, announcements_backup_vietnamese_fix';
PRINT '🔄 Please restart your Spring Boot application to ensure all changes take effect';

GO
