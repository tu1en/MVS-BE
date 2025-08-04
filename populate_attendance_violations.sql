-- SQL Script to Populate Attendance Violations/Explanations for Accountant
-- This creates test data for attendance violations and their explanations
-- Run this script against your SchoolManagementDB database

USE [SchoolManagementDB];
GO

-- Table for attendance violations (if not exists)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'attendance_violations')
BEGIN
    CREATE TABLE [dbo].[attendance_violations] (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        staff_name NVARCHAR(255),
        staff_email NVARCHAR(255),
        violation_date DATE,
        violation_type NVARCHAR(50),
        severity NVARCHAR(20),
        expected_time NVARCHAR(20),
        actual_time NVARCHAR(20),
        deviation_minutes INT,
        system_description NVARCHAR(MAX),
        violation_reason NVARCHAR(MAX),
        created_at DATETIME2 DEFAULT GETDATE()
    );
END

-- Table for attendance explanations  
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'attendance_explanations')
BEGIN
    CREATE TABLE [dbo].[attendance_explanations] (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        staff_id BIGINT,
        staff_name NVARCHAR(255),
        department NVARCHAR(255),
        violation_date DATE,
        explanation_text NVARCHAR(MAX),
        evidence_files NVARCHAR(MAX),
        status VARCHAR(20) DEFAULT 'PENDING',
        approver_name NVARCHAR(255),
        submitted_at DATETIME2 DEFAULT GETDATE(),
        processed_at DATATETIME2
    );
END
GO

-- Clear existing data
DELETE FROM [dbo].[attendance_explanations];
DELETE FROM [dbo].[attendance_violations];
GO

-- Ensure we have an accountant user
-- (Should be created by DataLoader, but let's ensure one exists)
IF NOT EXISTS (SELECT 1 FROM [dbo].[users] WHERE role_id = 5 AND email = 'accountant@test.com')
BEGIN
    INSERT INTO [dbo].[users] (
        full_name, email, password, phone, address, role_id, 
        department, is_active, created_at, updated_at
    )
    VALUES 
    (N'Nguyễn Thị Kế Toán', 'accountant@test.com', 
     '$2a$12$7J6fXkzVjXNVLqYj6VjZ3e6P4F5G8H2J3K4L5M6N7P8Q9R0S1T2U3V4', 
     '0987654321', N'Sài Gòn', 5, N'Phòng Kế Toán', 1, GETDATE(), GETDATE());
    
    PRINT 'Created test accountant user';
END
GO

-- Insert sample attendance violations for accountant
INSERT INTO [dbo].[attendance_violations] (
    staff_name, staff_email, violation_date, violation_type, 
    severity, expected_time, actual_time, deviation_minutes, 
    system_description, violation_reason
)
VALUES
(N'Nguyễn Thị Kế Toán', 'accountant@test.com', '2024-01-15', 'LATE_ARRIVAL', 'MODERATE', '08:00', '08:25', 25, N'Đi muộn 25 phút vào ngày 15/01/2024', N'Đi muộn do kẹt xe nghiêm trọng trên đường Võ Văn Kiệt'),
(N'Nguyễn Thị Kế Toán', 'accountant@test.com', '2024-01-18', 'EARLY_DEPARTURE', 'MINOR', '17:00', '16:30', 30, N'Về sớm 30 phút ngày 18/01/2024', N'Phải về sớm đón con đi khám bệnh'),
(N'Nguyễn Thị Kế Toán', 'accountant@test.com', '2024-01-22', 'MISSING_CHECK_IN', 'MAJOR', '08:00', NULL, 480, N'Không chấm công vào ngày 22/01/2024', N'Quên chấm công do có việc gấp'),
(N'Nguyễn Thị Kế Toán', 'accountant@test.com', '2024-01-25', 'LATE_ARRIVAL', 'MINOR', '08:00', '08:15', 15, N'Đi muộn 15 phút ngày 25/01/2024', N'Tắc đường khu vực Nguyễn Trãi'),
(N'Nguyễn Thị Kế Toán', 'accountant@test.com', '2024-02-01', 'ABSENT_WITHOUT_LEAVE', 'CRITICAL', '08:00', NULL, 1440, N'Vắng không phép ngày 01/02/2024', N'Ốm cảm cúm nặng không thể đi làm'),
(N'Nguyễn Thị Kế Toán', 'accountant@test.com', '2024-02-05', 'MISSING_CHECK_OUT', 'MODERATE', '17:00', NULL, 480, N'Thiếu chấm công ra ngày 05/02/2024', N'Bận họp khẩn cấp với ban giám đốc'),
(N'Nguyễn Thị Kế Toán', 'accountant@test.com', '2024-02-08', 'LATE_ARRIVAL', 'MINOR', '08:00', '08:10', 10, N'Đi muộn 10 phút ngày 08/02/2024', N'Kẹt xe tại ngã tư Nguyễn Văn Linh'),
(N'Nguyễn Thị Kế Toán', 'accountant@test.com', '2024-02-12', 'EARLY_DEPARTURE', 'MINOR', '17:00', '16:45', 15, N'Về sớm 15 phút ngày 12/02/2024', N'Có việc gia đình phải về sớm'),
(N'Nguyễn Thị Kế Toán', 'accountant@test.com', '2024-02-15', 'LATE_ARRIVAL', 'MODERATE', '08:00', '08:20', 20, N'Đi muộn 20 phút ngày 15/02/2024', N'Thời tiết xấu do mưa lớn'),
(N'Nguyễn Thị Kế Toán', 'accountant@test.com', '2024-02-19', 'MISSING_CHECK_IN', 'MAJOR', '08:00', NULL, 480, N'Thiếu chấm công vào ngày 19/02/2024', N'Máy chấm công hỏng nên không thể quẹt thẻ')
GO

-- Insert sample attendance explanations (responses) for these violations
INSERT INTO [dbo].[attendance_explanations] (
    staff_id, staff_name, department, violation_date, explanation_text, status, approver_name
)
VALUES 
(1, N'Nguyễn Thị Kế Toán', N'Phòng Kế Toán', '2024-01-15', N'Tôi xin giải trình: Sáng 15/01 tôi bị kẹt xe nghiêm trọng do có tai nạn trên đường Võ Văn Kiệt. Tôi đã cố gắng đến sớm nhất có thể nhưng vẫn bị muộn 25 phút. Đây là sự cố bất khả kháng và không lặp lại. Tôi cam kết thức dậy sớm hơn vào hôm sau để tránh tình trạng tương tự.', 'APPROVED', N'Quản lý trực tiếp'),

(1, N'Nguyễn Thị Kế Toán', N'Phòng Kế Toán', '2024-01-22', N'Xin phép quản lý: Ngày 22/01 tôi hoàn toàn quên mất việc quẹt thẻ khi đi làm. Tuy nhiên, tôi vẫn cố gắng làm việc hết công suất và sẽ cố gắng ghi nhớ quẹt thẻ đầy đủ trong những ngày tiếp theo.', 'REJECTED', N'Quản lý trực tiếp'),

(1, N'Nguyễn Thị Kế Toán', N'Phòng Kế Toán', '2024-02-01', N'Tôi xin lỗi, tôi bị cảm cúm nặng và không thể thông báo nghỉ kịp thời được. Tôi có giấy khám bệnh của bệnh viện đính kèm. Hy vọng quản lý thông cảm và cho phép tôi được xin nghỉ không phép lần này.', 'APPROVED', N'HR Manager'),

(1, N'Nguyễn Thị Kế Toán', N'Phòng Kế Toán', '2024-02-19', N'Xin phépp quản lý về việc không chấm công vào 19/02: Tôi đã đến đúng giờ nhưng hệ thống máy chấm công gặp lỗi nên không thể quẹt thẻ. Tôi đã thông báo cho bộ phận IT và được họ xác nhận. Tôi hy vọng có thể nhận được sự thông cảm từ quản lý.', 'PENDING', NULL)
GO

-- Verify the data
SELECT 
    v.id,
    v.staff_name,
    CONVERT(VARCHAR(10), v.violation_date, 103) as violation_date_vi,
    v.violation_type,
    CASE 
        WHEN v.violation_type = 'LATE_ARRIVAL' THEN N'Đi muộn'
        WHEN v.violation_type = 'EARLY_DEPARTURE' THEN N'Về sớm'
        WHEN v.violation_type = 'MISSING_CHECK_IN' THEN N'Thiếu chấm công vào'
        WHEN v.violation_type = 'MISSING_CHECK_OUT' THEN N'Thiếu chấm công ra'
        WHEN v.violation_type = 'ABSENT_WITHOUT_LEAVE' THEN N'Vắng không phép'
        ELSE v.violation_type
    END as violation_vi,
    v.deviation_minutes,
    v.violation_reason,
    CONVERT(VARCHAR(10), v.created_at, 103) as created_date_vi
FROM [dbo].[attendance_violations] v
WHERE v.staff_email = 'accountant@test.com'
ORDER BY v.violation_date DESC;

-- Summary of explanations
SELECT 
    e.staff_name,
    COUNT(*) as total_violations,
    COUNT(CASE WHEN e.status = 'PENDING' THEN 1 END) as pending_explanations,
    COUNT(CASE WHEN e.status = 'APPROVED' THEN 1 END) as approved_explanations,
    COUNT(CASE WHEN e.status = 'REJECTED' THEN 1 END) as rejected_explanations
FROM [dbo].[attendance_explanations] e
GROUP BY e.staff_name;

PRINT CONCAT('Total attendance violations inserted: ', @@ROWCOUNT);
GO