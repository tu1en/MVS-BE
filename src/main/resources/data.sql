-- Clean up tables for H2 referential integrity
DELETE FROM submissions;
DELETE FROM assignments;
DELETE FROM attendances;
DELETE FROM attendance_sessions;
DELETE FROM classroom_enrollments;
DELETE FROM classrooms;
DELETE FROM users;
DELETE FROM allowed_ips;

-- Add sample users (password: 123456 - encrypted)
INSERT INTO users (username, password, full_name, email, role_id) VALUES
('teacher1', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Nguyễn Văn A', 'teacher1@example.com', 2),
('teacher2', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Trần Thị B', 'teacher2@example.com', 2),
('student1', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Lê Văn C', 'student1@example.com', 1),
('student2', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Phạm Thị D', 'student2@example.com', 1),
('student3', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Hoàng Văn E', 'student3@example.com', 1);

-- Add sample classrooms (coordinates of some locations in Hanoi)
INSERT INTO classrooms (name, description, section, subject, teacher_id, location_lat, location_lon, allowed_radius) VALUES
('Java Spring Boot Class', 'Java Spring Boot Programming Class', 'SE1', 'Java Programming', 1, 21.028511, 105.804817, 100.0),
('React Class', 'React Programming Class', 'SE2', 'Web Development', 2, 21.007529, 105.783834, 100.0);

-- Add students to classes
INSERT INTO classroom_enrollments (classroom_id, user_id) VALUES
(1, 3), -- student1 in Java class
(1, 4), -- student2 in Java class
(2, 4), -- student2 in React class
(2, 5); -- student3 in React class

-- Add sample attendance sessions
INSERT INTO attendance_sessions (title, classroom_id, teacher_id, start_time, end_time, session_type, status, auto_mark, auto_mark_teacher_attendance) VALUES
('Session 1 - Java Spring Boot', 1, 1, '2024-01-15 08:00:00', '2024-01-15 10:00:00', 'OFFLINE', 'ACTIVE', true, true),
('Session 1 - React', 2, 2, '2024-01-15 14:00:00', '2024-01-15 16:00:00', 'ONLINE', 'ACTIVE', true, true);

-- Add allowed IPs
INSERT INTO allowed_ips (ip_address, description) VALUES
('127.0.0.1', 'Localhost'),
('192.168.1.1', 'Local network');
