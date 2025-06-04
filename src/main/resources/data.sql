-- Thêm users mẫu (password: 123456 - đã được mã hóa)
INSERT INTO users (username, password, full_name, email, role_id) VALUES
('teacher1', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Nguyễn Văn A', 'teacher1@example.com', 2),
('teacher2', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Trần Thị B', 'teacher2@example.com', 2),
('student1', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Lê Văn C', 'student1@example.com', 1),
('student2', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Phạm Thị D', 'student2@example.com', 1),
('student3', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Hoàng Văn E', 'student3@example.com', 1);

-- Thêm lớp học mẫu (tọa độ của một số địa điểm ở Hà Nội)
INSERT INTO classrooms (name, description, section, subject, teacher_id, location_lat, location_lon, allowed_radius) VALUES
('Lớp Java Spring Boot', 'Lớp học lập trình Java Spring Boot', 'SE1', 'Java Programming', 1, 21.028511, 105.804817, 100.0),
('Lớp React', 'Lớp học lập trình React', 'SE2', 'Web Development', 2, 21.007529, 105.783834, 100.0);

-- Thêm sinh viên vào lớp
INSERT INTO classroom_enrollments (classroom_id, user_id) VALUES
(1, 3), -- student1 trong lớp Java
(1, 4), -- student2 trong lớp Java
(2, 4), -- student2 trong lớp React
(2, 5); -- student3 trong lớp React

-- Thêm phiên điểm danh mẫu
INSERT INTO attendance_sessions (title, classroom_id, teacher_id, start_time, end_time, session_type, status, auto_mark, auto_mark_teacher_attendance) VALUES
('Buổi 1 - Java Spring Boot', 1, 1, '2024-01-15 08:00:00', '2024-01-15 10:00:00', 'OFFLINE', 'ACTIVE', true, true),
('Buổi 1 - React', 2, 2, '2024-01-15 14:00:00', '2024-01-15 16:00:00', 'ONLINE', 'ACTIVE', true, true);

-- Thêm IP được phép
INSERT INTO allowed_ips (ip_address, description) VALUES
('127.0.0.1', 'Localhost'),
('192.168.1.1', 'Local network'); 