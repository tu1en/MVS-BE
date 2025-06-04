-- Insert student users
INSERT INTO users (
    username,
    password, -- Using hashed password '123456'
    email,
    full_name,
    role_id,   -- 3 for student role
    department,
    status,
    enrollment_date,
    created_at,
    updated_at
)
VALUES 
    ('student001', '$2a$10$rS.FGQy9S8bH8AzHCbGrNOeNo8P.kCR.9.T8CKVj8vO/RNZKEXxhC', 'student001@mvs.edu.vn', 'Nguyễn Văn An', 3, 'Computer Science', 'active', '2023-09-01', GETDATE(), GETDATE()),
    ('student002', '$2a$10$rS.FGQy9S8bH8AzHCbGrNOeNo8P.kCR.9.T8CKVj8vO/RNZKEXxhC', 'student002@mvs.edu.vn', 'Trần Thị Bình', 3, 'Computer Science', 'active', '2023-09-01', GETDATE(), GETDATE()),
    ('student003', '$2a$10$rS.FGQy9S8bH8AzHCbGrNOeNo8P.kCR.9.T8CKVj8vO/RNZKEXxhC', 'student003@mvs.edu.vn', 'Lê Văn Cường', 3, 'Computer Science', 'active', '2023-09-01', GETDATE(), GETDATE()),
    ('student004', '$2a$10$rS.FGQy9S8bH8AzHCbGrNOeNo8P.kCR.9.T8CKVj8vO/RNZKEXxhC', 'student004@mvs.edu.vn', 'Phạm Thị Dung', 3, 'Computer Science', 'active', '2023-09-01', GETDATE(), GETDATE()),
    ('student005', '$2a$10$rS.FGQy9S8bH8AzHCbGrNOeNo8P.kCR.9.T8CKVj8vO/RNZKEXxhC', 'student005@mvs.edu.vn', 'Hoàng Văn Em', 3, 'Computer Science', 'active', '2023-09-01', GETDATE(), GETDATE());

-- Enroll students in classrooms
INSERT INTO classroom_enrollments (classroom_id, user_id)
SELECT 1, id FROM users WHERE username IN ('student001', 'student002', 'student003', 'student004', 'student005');

-- Add some sample submissions for these students
INSERT INTO submissions (comment, submitted_at, assignment_id, student_id)
SELECT 
    'Submission from ' + username,
    GETDATE(),
    1, -- assignment_id from the existing assignment
    id
FROM users 
WHERE username IN ('student001', 'student002', 'student003');
