-- Add comprehensive sample users (password: 123456 - encrypted with BCrypt)
-- Admin users
INSERT INTO users (username, password, full_name, email, role_id, department, status) VALUES
('admin', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Quản trị viên hệ thống', 'admin@mvs.edu.vn', 3, 'IT', 'active');

-- Teacher users
INSERT INTO users (username, password, full_name, email, role_id, department, hire_date, status) VALUES
('gv.nguyenvana', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Nguyễn Văn An', 'nguyenvana@mvs.edu.vn', 2, 'Công nghệ thông tin', '2020-09-01', 'active'),
('gv.tranthib', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Trần Thị Bình', 'tranthib@mvs.edu.vn', 2, 'Công nghệ thông tin', '2019-02-15', 'active'),
('gv.lethic', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Lê Thị Cẩm', 'lethic@mvs.edu.vn', 2, 'Toán học', '2021-08-20', 'active'),
('gv.phamvand', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Phạm Văn Dũng', 'phamvand@mvs.edu.vn', 2, 'Tiếng Anh', '2018-03-10', 'active');

-- Student users
INSERT INTO users (username, password, full_name, email, role_id, enrollment_date, status) VALUES
('sv.levanc', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Lê Văn Cường', 'levanc@student.mvs.edu.vn', 1, '2023-09-01', 'active'),
('sv.phamthid', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Phạm Thị Diệu', 'phamthid@student.mvs.edu.vn', 1, '2023-09-01', 'active'),
('sv.hoangvane', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Hoàng Văn Em', 'hoangvane@student.mvs.edu.vn', 1, '2023-09-01', 'active'),
('sv.nguyenthif', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Nguyễn Thị Phương', 'nguyenthif@student.mvs.edu.vn', 1, '2023-09-01', 'active'),
('sv.tranhg', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Trần Hồng Giang', 'tranhg@student.mvs.edu.vn', 1, '2024-09-01', 'active'),
('sv.vuvanhi', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Vũ Văn Hưng', 'vuvanhi@student.mvs.edu.vn', 1, '2024-09-01', 'active'),
('sv.dothij', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Đỗ Thị Lan', 'dothij@student.mvs.edu.vn', 1, '2024-09-01', 'active'),
('sv.buivank', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Bùi Văn Khoa', 'buivank@student.mvs.edu.vn', 1, '2023-09-01', 'active');

-- Add sample classrooms with real coordinates in Vietnam
INSERT INTO classrooms (name, description, section, subject, teacher_id, location_lat, location_lon, allowed_radius) VALUES
('Lập trình Java Spring Boot', 'Khóa học lập trình Java Spring Boot nâng cao cho sinh viên năm 3', 'SE490-01', 'Công nghệ phần mềm', 2, 21.028511, 105.804817, 150.0),
('Phát triển ứng dụng React', 'Khóa học phát triển giao diện người dùng với ReactJS', 'SE490-02', 'Công nghệ phần mềm', 3, 21.007529, 105.783834, 150.0),
('Toán rời rạc', 'Môn học cơ sở toán học cho ngành CNTT', 'MA101-01', 'Toán học', 4, 21.038129, 105.782089, 100.0),
('Tiếng Anh chuyên ngành IT', 'Tiếng Anh chuyên ngành công nghệ thông tin', 'EN201-01', 'Tiếng Anh', 5, 21.005734, 105.845942, 100.0),
('Cấu trúc dữ liệu và giải thuật', 'Khóa học về cấu trúc dữ liệu và giải thuật', 'CS201-01', 'Khoa học máy tính', 2, 21.013715, 105.798100, 120.0);

-- Add students to classes (Many-to-many relationship)
INSERT INTO classroom_enrollments (classroom_id, user_id) VALUES
-- Java Spring Boot class
(1, 3), (1, 4), (1, 5), (1, 6), (1, 9), (1, 10),
-- React class  
(2, 4), (2, 5), (2, 7), (2, 8), (2, 10),
-- Toán rời rạc
(3, 3), (3, 6), (3, 7), (3, 8), (3, 9),
-- Tiếng Anh chuyên ngành
(4, 5), (4, 6), (4, 8), (4, 9), (4, 10),
-- Cấu trúc dữ liệu
(5, 3), (5, 4), (5, 7), (5, 8);

-- Add sample attendance sessions for different time periods
INSERT INTO attendance_sessions (title, classroom_id, teacher_id, start_time, end_time, session_type, status, auto_mark, auto_mark_teacher_attendance) VALUES
-- Java Spring Boot sessions
('Buổi 1: Giới thiệu Spring Boot', 1, 2, '2024-12-01 08:00:00', '2024-12-01 10:30:00', 'OFFLINE', 'COMPLETED', true, true),
('Buổi 2: Spring MVC và REST API', 1, 2, '2024-12-03 08:00:00', '2024-12-03 10:30:00', 'OFFLINE', 'COMPLETED', true, true),
('Buổi 3: Spring Data JPA', 1, 2, '2024-12-05 08:00:00', '2024-12-05 10:30:00', 'OFFLINE', 'ACTIVE', true, true),
('Buổi 4: Spring Security', 1, 2, '2024-12-08 08:00:00', '2024-12-08 10:30:00', 'ONLINE', 'SCHEDULED', true, true),

-- React sessions
('Buổi 1: JSX và Components', 2, 3, '2024-12-02 14:00:00', '2024-12-02 16:30:00', 'OFFLINE', 'COMPLETED', true, true),
('Buổi 2: State và Props', 2, 3, '2024-12-04 14:00:00', '2024-12-04 16:30:00', 'ONLINE', 'COMPLETED', true, true),
('Buổi 3: React Hooks', 2, 3, '2024-12-06 14:00:00', '2024-12-06 16:30:00', 'OFFLINE', 'ACTIVE', true, true),

-- Toán rời rạc sessions
('Chương 1: Logic và tập hợp', 3, 4, '2024-12-01 10:00:00', '2024-12-01 11:30:00', 'OFFLINE', 'COMPLETED', true, true),
('Chương 2: Quan hệ và hàm', 3, 4, '2024-12-03 10:00:00', '2024-12-03 11:30:00', 'OFFLINE', 'ACTIVE', true, true),

-- Tiếng Anh sessions
('Unit 1: Computer Hardware', 4, 5, '2024-12-02 16:00:00', '2024-12-02 17:30:00', 'OFFLINE', 'COMPLETED', true, true),
('Unit 2: Software Development', 4, 5, '2024-12-04 16:00:00', '2024-12-04 17:30:00', 'OFFLINE', 'ACTIVE', true, true),

-- Cấu trúc dữ liệu sessions
('Bài 1: Mảng và danh sách liên kết', 5, 2, '2024-12-01 13:00:00', '2024-12-01 15:30:00', 'OFFLINE', 'COMPLETED', true, true),
('Bài 2: Stack và Queue', 5, 2, '2024-12-03 13:00:00', '2024-12-03 15:30:00', 'OFFLINE', 'ACTIVE', true, true);

-- Add allowed IPs for different locations
INSERT INTO allowed_ips (ip_address, description) VALUES
('127.0.0.1', 'Localhost - Development'),
('192.168.1.0/24', 'Campus Network - Building A'),
('192.168.2.0/24', 'Campus Network - Building B'), 
('10.0.0.0/8', 'VPN Network'),
('172.16.0.0/16', 'Lab Network'),
('203.162.4.0/24', 'University Public IP Range');

-- Add sample announcements
INSERT INTO announcements (title, content, created_by, classroom_id, target_audience, priority, status, is_pinned, created_at, updated_at) VALUES
('Thông báo về lịch học môn Java Spring Boot', 'Lịch học môn Java Spring Boot tuần tới sẽ thay đổi do giáo viên có công tác. Các bạn vui lòng theo dõi thông báo tiếp theo.', 2, 1, 'STUDENTS', 'HIGH', 'ACTIVE', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Bài tập lớn môn React', 'Các bạn sinh viên cần hoàn thành bài tập lớn về React trước ngày 15/12. Nộp bài qua email của giáo viên.', 3, 2, 'STUDENTS', 'NORMAL', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Thông báo nghỉ học', 'Do thời tiết xấu, các lớp học hôm nay sẽ tạm nghỉ. Thời gian học bù sẽ được thông báo sau.', 2, NULL, 'ALL', 'URGENT', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Cuộc thi lập trình', 'Khoa CNTT tổ chức cuộc thi lập trình dành cho sinh viên. Thời gian đăng ký từ ngày 1/12 đến 10/12.', 4, NULL, 'STUDENTS', 'NORMAL', 'ACTIVE', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Họp phụ huynh', 'Thông báo tới phụ huynh sinh viên về buổi họp định kỳ vào cuối tháng 12.', 2, 1, 'ALL', 'NORMAL', 'ACTIVE', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
