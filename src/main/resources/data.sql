-- Clear existing data
DELETE FROM user_roles;
DELETE FROM users;

-- Insert users with different roles
-- Password for all users is: password123 (BCrypt encoded)
INSERT INTO users (id, username, email, password, full_name, status, created_at, updated_at)
VALUES 
(1, 'admin', 'admin@mvs.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Admin User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'teacher', 'teacher@mvs.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Teacher User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'student', 'student@mvs.com', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Student User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert user roles
INSERT INTO user_roles (user_id, role)
VALUES 
(1, 'ROLE_MANAGER'),
(2, 'ROLE_TEACHER'),
(3, 'ROLE_STUDENT');

-- Create some basic classroom data
INSERT INTO classrooms (id, name, description, created_by_id, created_at, updated_at)
VALUES 
(1, 'Math Class 101', 'Introduction to Mathematics', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Physics Class 101', 'Introduction to Physics', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Enroll student in classes
INSERT INTO classroom_students (classroom_id, student_id)
VALUES 
(1, 3),
(2, 3);

-- Insert mock schedules
INSERT INTO schedules (id, class_id, class_name, subject, day, start_time, end_time, teacher_id, teacher_name, materials_url)
VALUES
(1, '1', 'Math Class 101', 'Mathematics', 1, '08:00:00', '09:30:00', 2, 'Teacher User', 'https://example.com/math-materials'),
(2, '1', 'Math Class 101', 'Mathematics', 3, '10:00:00', '11:30:00', 2, 'Teacher User', 'https://example.com/math-materials'),
(3, '2', 'Physics Class 101', 'Physics', 2, '13:00:00', '14:30:00', 2, 'Teacher User', 'https://example.com/physics-materials'),
(4, '2', 'Physics Class 101', 'Physics', 4, '15:00:00', '16:30:00', 2, 'Teacher User', 'https://example.com/physics-materials');

-- Link schedules with student
INSERT INTO schedule_student_ids (schedule_id, student_id)
VALUES
(1, 3),
(2, 3),
(3, 3),
(4, 3);
