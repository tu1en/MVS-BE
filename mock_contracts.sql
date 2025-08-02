-- Mock Contract Data for Testing
-- Tạo 5 hợp đồng ảo với các trạng thái khác nhau để test hệ thống

-- Hôm nay: 02/08/2025

-- 1. Hợp đồng ACTIVE (còn lâu mới hết hạn)
INSERT INTO contracts (
    user_id, full_name, email, phone_number, contract_type, position, department, 
    salary, working_hours, start_date, end_date, status, created_at, updated_at
) VALUES (
    1001, 'Nguyễn Văn An', 'nguyen.van.an@example.com', '0987654321', 'TEACHER', 
    'Giáo viên Toán', 'Phòng Giáo vụ', 15000000, 'ca sáng (7:30-9:30)', 
    '2024-01-15', '2026-01-15', 'ACTIVE', NOW(), NOW()
);

-- 2. Hợp đồng ACTIVE (sắp gần hết hạn - còn 20 ngày)
INSERT INTO contracts (
    user_id, full_name, email, phone_number, contract_type, position, department, 
    salary, working_hours, start_date, end_date, status, created_at, updated_at
) VALUES (
    1002, 'Trần Thị Bình', 'tran.thi.binh@example.com', '0976543210', 'STAFF', 
    'Nhân viên Kế toán', 'Phòng Tài chính', 12000000, 'ca chiều (14:30-16:30)', 
    '2024-02-01', '2025-08-22', 'ACTIVE', NOW(), NOW()
);

-- 3. Hợp đồng NEAR_EXPIRY (gần hết hạn - còn 10 ngày)
INSERT INTO contracts (
    user_id, full_name, email, phone_number, contract_type, position, department, 
    salary, working_hours, start_date, end_date, status, created_at, updated_at
) VALUES (
    1003, 'Lê Minh Cường', 'le.minh.cuong@example.com', '0965432109', 'TEACHER', 
    'Giáo viên Lý', 'Phòng Giáo vụ', 16000000, 'ca tối (19:20-21:20)', 
    '2024-03-01', '2025-08-12', 'NEAR_EXPIRY', NOW(), NOW()
);

-- 4. Hợp đồng NEAR_EXPIRY (gần hết hạn - còn 5 ngày)
INSERT INTO contracts (
    user_id, full_name, email, phone_number, contract_type, position, department, 
    salary, working_hours, start_date, end_date, status, created_at, updated_at
) VALUES (
    1004, 'Phạm Thị Dung', 'pham.thi.dung@example.com', '0954321098', 'STAFF', 
    'Nhân viên Hành chính', 'Phòng Hành chính', 11000000, 'ca sáng (7:30-9:30)', 
    '2024-04-01', '2025-08-07', 'NEAR_EXPIRY', NOW(), NOW()
);

-- 5. Hợp đồng EXPIRED (đã hết hạn - hết hạn 3 ngày trước)
INSERT INTO contracts (
    user_id, full_name, email, phone_number, contract_type, position, department, 
    salary, working_hours, start_date, end_date, status, created_at, updated_at
) VALUES (
    1005, 'Hoàng Văn Em', 'hoang.van.em@example.com', '0943210987', 'TEACHER', 
    'Giáo viên Hóa', 'Phòng Giáo vụ', 17000000, 'ca chiều (14:30-16:30)', 
    '2024-05-01', '2025-07-30', 'EXPIRED', NOW(), NOW()
);

-- Kiểm tra dữ liệu đã insert
SELECT 
    id, full_name, contract_type, position, 
    start_date, end_date, status,
    DATEDIFF(end_date, CURDATE()) as days_remaining
FROM contracts 
ORDER BY end_date;
