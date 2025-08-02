-- Insert 5 test contracts for demonstration
-- Run this SQL script in your database management tool

-- Clear existing contracts (optional)
-- DELETE FROM contracts;

-- Insert test contracts with various statuses
INSERT INTO contracts (
    user_id, full_name, email, phone_number, contract_type, position, department, 
    salary, working_hours, start_date, end_date, status, created_at, updated_at
) VALUES 
-- 1. Active contract (long term)
(1001, 'Nguyễn Văn An', 'nguyen.van.an@example.com', '0987654321', 'TEACHER', 
 'Giáo viên Toán', 'Phòng Giáo vụ', 15000000, 'ca sáng (7:30-9:30)', 
 '2024-01-15', '2026-01-15', 'ACTIVE', GETDATE(), GETDATE()),

-- 2. Active contract (expires in 20 days)
(1002, 'Trần Thị Bình', 'tran.thi.binh@example.com', '0976543210', 'STAFF', 
 'Nhân viên Kế toán', 'Phòng Tài chính', 12000000, 'ca chiều (14:30-16:30)', 
 '2024-02-01', DATEADD(day, 20, GETDATE()), 'ACTIVE', GETDATE(), GETDATE()),

-- 3. Near expiry contract (expires in 10 days)
(1003, 'Lê Minh Cường', 'le.minh.cuong@example.com', '0965432109', 'TEACHER', 
 'Giáo viên Lý', 'Phòng Giáo vụ', 16000000, 'ca tối (19:20-21:20)', 
 '2024-03-01', DATEADD(day, 10, GETDATE()), 'NEAR_EXPIRY', GETDATE(), GETDATE()),

-- 4. Near expiry contract (expires in 5 days)
(1004, 'Phạm Thị Dung', 'pham.thi.dung@example.com', '0954321098', 'STAFF', 
 'Nhân viên Hành chính', 'Phòng Hành chính', 11000000, 'ca sáng (7:30-9:30)', 
 '2024-04-01', DATEADD(day, 5, GETDATE()), 'NEAR_EXPIRY', GETDATE(), GETDATE()),

-- 5. Expired contract (expired 3 days ago)
(1005, 'Hoàng Văn Em', 'hoang.van.em@example.com', '0943210987', 'TEACHER', 
 'Giáo viên Hóa', 'Phòng Giáo vụ', 17000000, 'ca chiều (14:30-16:30)', 
 '2024-05-01', DATEADD(day, -3, GETDATE()), 'EXPIRED', GETDATE(), GETDATE());

-- Verify the inserted data
SELECT 
    id, full_name, contract_type, position, 
    start_date, end_date, status,
    DATEDIFF(day, GETDATE(), end_date) as days_remaining
FROM contracts 
ORDER BY end_date;
