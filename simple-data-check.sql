-- Script đơn giản để kiểm tra dữ liệu hiện tại

-- 1. Kiểm tra tất cả classrooms
SELECT id, name, subject, description, teacher_id FROM classrooms;

-- 2. Kiểm tra tất cả users
SELECT id, username, email, full_name, role_id FROM users ORDER BY role_id, id;

-- 3. Kiểm tra classroom enrollments
SELECT classroom_id, user_id FROM classroom_enrollments;

-- 4. Kiểm tra assignments
SELECT id, title, due_date, points, classroom_id FROM assignments;

-- 5. Kiểm tra submissions
SELECT id, assignment_id, student_id, score, feedback FROM submissions;
