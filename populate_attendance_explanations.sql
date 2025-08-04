-- SQL Script to Populate Accountant Absence/Leave Requests
-- Run this script against your SchoolManagementDB database

USE [SchoolManagementDB];
GO

-- First, let's ensure we have accountant users
-- Check if accountant users exist
IF NOT EXISTS (SELECT 1 FROM [dbo].[users] WHERE [role] = 'ACCOUNTANT')
BEGIN
    -- Add test accountant users if they don't exist
    INSERT INTO [dbo].[users] (full_name, email, password, phone, address, role, department, is_active, created_at, updated_at)
    VALUES 
    (N'Trần Văn A - Kế Toán', 'accountant1@test.com', '$2a$12$7J6fXkzVjXNVLqYj6VjZ3e6P4F5G8H2J3K4L5M6N7P8Q9R0S1T2U3V4', '0123456789', N'Sài Gòn', 'ACCOUNTANT', N'Phòng Kế Toán', 1, GETDATE(), GETDATE()),
    (N'Lê Thị B - Kế Toán', 'accountant2@test.com', '$2a$12$7J6fXkzVjXNVLqYj6VjZ3e6P4F5G8H2J3K4L5M6N7P8Q9R0S1T2U3V4', '0987654321', N'Hà Nội', 'ACCOUNTANT', N'Phòng Kế Toán', 1, GETDATE(), GETDATE()),
    (N'Nguyễn Văn C - Kế Toán Trưởng', 'accountant3@test.com', '$2a$12$7J6fXkzVjXNVLqYj6VjZ3e6P4F5G8H2J3K4L5M6N7P8Q9R0S1T2U3V4', '0112233445', N'Đà Nẵng', 'ACCOUNTANT', N'Phòng Kế Toán', 1, GETDATE(), GETDATE());
END
GO

-- Clear existing absence data for accountants
DELETE FROM [dbo].[absences] WHERE user_email LIKE '%@test.com';
GO

-- Insert sample absence requests for accountants
INSERT INTO [dbo].[absences] (
    user_id, user_email, user_full_name, start_date, end_date, 
    number_of_days, description, status, result_status, reject_reason, 
    created_at, processed_at, processed_by, is_over_limit
) VALUES
-- Đơn nghỉ phép đang chờ duyệt
(101, 'accountant1@test.com', N'Trần Văn A - Kế Toán', '2024-01-15', '2024-01-17', 3, 
 N'Xin nghỉ 3 ngày để lo giấy tờ bảo hiểm y tế cho gia đình', 
 'PENDING', NULL, NULL, '2024-01-10 09:30:00', NULL, NULL, 0),

(102, 'accountant2@test.com', N'Lê Thị B - Kế Toán', '2024-01-22', '2024-01-26', 5,
 N'Xin nghỉ 5 ngày để đi công tác họp tác xã với bên ngân hàng', 
 'PENDING', NULL, NULL, '2024-01-18 14:20:00', NULL, NULL, 0),

-- Đơn nghỉ phép đã được duyệt
(101, 'accountant1@test.com', N'Trần Văn A - Kế Toán', '2024-02-05', '2024-02-09', 5,
 N'Xin nghỉ 5 ngày để lo giấy tờ thừa kế và sắp xếp gia đình sau khi gặp biến cố', 
 'APPROVED', 'APPROVED', NULL, '2024-01-28 10:15:00', '2024-01-29 08:30:00', 1, 0),

(103, 'accountant3@test.com', N'Nguyễn Văn C - Kế Toán Trưởng', '2024-01-08', '2024-01-10', 2,
 N'Nghỉ 2 ngày do con gặp tai nạn nhẹ phải chăm sóc', 
 'APPROVED', 'APPROVED', NULL, '2024-01-05 16:45:00', '2024-01-06 09:00:00', 1, 0),

-- Đơn nghỉ phép bị từ chối
(102, 'accountant2@test.com', N'Lê Thị B - Kế Toán', '2024-01-12', '2024-01-12', 1,
 N'Xin nghỉ 1 ngày để đi xét nghiệm y tế', 
 'REJECTED', 'REJECTED', N'Vì đây là ngày cuối tháng, phải hoàn thành báo cáo', 
 '2024-01-10 11:30:00', '2024-01-11 10:00:00', 1, 1),

-- Đơn vượt quá giới hạn ngày nghỉ phép
(101, 'accountant1@test.com', N'Trần Văn A - Kế Toán', '2024-03-01', '2024-03-15', 15,
 N'Xin nghỉ phép 15 ngày để đi chữa bệnh hiếm muộn cùng vợ - đây là thời gian quan trọng', 
 'PENDING', NULL, NULL, '2024-02-25 08:00:00', NULL, NULL, 1),

-- Đơn nghỉ cuối tuần
(102, 'accountant2@test.com', N'Lê Thị B - Kế Toán', '2024-01-27', '2024-01-28', 2, -- Weekend so still 2 days
 N'Nghỉ cuối tuần để đưa gia đình đi picnic ở Vũng Tàu', 
 'PENDING', NULL, NULL, '2024-01-25 15:30:00', NULL, NULL, 0),

-- Đơn khác
(103, 'accountant3@test.com', N'Nguyễn Văn C - Kế Toán Trưởng', '2024-02-19', '2024-02-21', 3,
 N'Xin nghỉ phép 3 ngày để thăm cha mẹ đẻ đang ốm ở quê', 
 'PENDING', NULL, NULL, '2024-02-15 13:45:00', NULL, NULL, 0),

-- Đơn nghỉ với lý do học tập
(101, 'accountant1@test.com', N'Trần Văn A - Kế Toán', '2024-03-18', '2024-03-22', 5,
 N'Nghỉ phép 5 ngày để tham dự khóa học chuyên môn ngắn hạn của Hiệp hội kế toán tại Hà Nội', 
 'PENDING', NULL, NULL, '2024-03-12 09:00:00', NULL, NULL, 0),

-- Đơn bị từ chối khác
(103, 'accountant3@test.com', N'Nguyễn Văn C - Kế Toán Trưởng', '2024-01-29', '2024-01-30', 2,
 N'Xin nghỉ 2 ngày để lo giấy tờ liên quan đến vụ việc gia đình', 
 'REJECTED', 'REJECTED', N'Vì là đầu tháng, cần họp phòng kế hoạch, xin đợi sang tháng sau', 
 '2024-01-26 16:00:00', '2024-01-27 11:00:00', 1, 1);

-- Verify the data
SELECT 
    a.id,
    a.user_full_name,
    CONVERT(VARCHAR(10), a.start_date, 103) as start_date,
    CONVERT(VARCHAR(10), a.end_date, 103) as end_date,
    a.number_of_days,
    a.description,
    CASE 
        WHEN a.status = 'PENDING' THEN N'Chờ duyệt'
        WHEN a.status = 'APPROVED' THEN N'Đã duyệt'
        WHEN a.status = 'REJECTED' THEN N'Đã từ chối'
        ELSE a.status
    END as status_vi,
    a.is_over_limit,
    CONVERT(VARCHAR(16), a.created_at, 120) as created_at
FROM [dbo].[absences] a
JOIN [dbo].[users] u ON a.user_id = u.id
WHERE u.role = 'ACCOUNTANT'
ORDER BY a.created_at DESC;

-- Summary statistics
SELECT 
    COUNT(*) as total_requests,
    COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_requests,
    COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) as approved_requests,
    COUNT(CASE WHEN status = 'REJECTED' THEN 1 END) as rejected_requests
FROM [dbo].[absences] a
JOIN [dbo].[users] u ON a.user_id = u.id 
WHERE u.role = 'ACCOUNTANT';

PRINT CONCAT('Total accountant absence requests inserted: ', @@ROWCOUNT);
GO