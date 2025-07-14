-- Script để kiểm tra tính nhất quán dữ liệu enrollment
-- Mục tiêu: Tìm ra sự khác biệt giữa các cách đếm học viên

-- 1. Kiểm tra tổng quan về classrooms và enrollments
SELECT 'OVERVIEW' as check_type;
SELECT 
    c.id as classroom_id,
    c.name as classroom_name,
    c.teacher_id,
    COUNT(ce.user_id) as enrollment_count
FROM classrooms c
LEFT JOIN classroom_enrollments ce ON c.id = ce.classroom_id
GROUP BY c.id, c.name, c.teacher_id
ORDER BY c.id;

-- 2. Kiểm tra chi tiết enrollments cho từng classroom
SELECT 'ENROLLMENT_DETAILS' as check_type;
SELECT 
    ce.classroom_id,
    ce.user_id,
    u.full_name,
    u.email,
    u.role_id,
    CASE 
        WHEN u.role_id = 1 THEN 'STUDENT'
        WHEN u.role_id = 2 THEN 'TEACHER'
        WHEN u.role_id = 3 THEN 'MANAGER'
        WHEN u.role_id = 4 THEN 'ADMIN'
        ELSE 'UNKNOWN'
    END as role_name
FROM classroom_enrollments ce
JOIN users u ON ce.user_id = u.id
ORDER BY ce.classroom_id, u.role_id, u.full_name;

-- 3. Kiểm tra xem có teachers được enroll như students không
SELECT 'TEACHER_ENROLLED_AS_STUDENT' as check_type;
SELECT 
    c.id as classroom_id,
    c.name as classroom_name,
    c.teacher_id,
    teacher.full_name as teacher_name,
    ce.user_id as enrolled_user_id,
    enrolled_user.full_name as enrolled_user_name
FROM classrooms c
JOIN users teacher ON c.teacher_id = teacher.id
JOIN classroom_enrollments ce ON c.id = ce.classroom_id
JOIN users enrolled_user ON ce.user_id = enrolled_user.id
WHERE c.teacher_id = ce.user_id;

-- 4. Đếm students thực sự (role_id = 1) trong mỗi classroom
SELECT 'ACTUAL_STUDENT_COUNT' as check_type;
SELECT 
    c.id as classroom_id,
    c.name as classroom_name,
    COUNT(CASE WHEN u.role_id = 1 THEN 1 END) as actual_student_count,
    COUNT(ce.user_id) as total_enrollment_count
FROM classrooms c
LEFT JOIN classroom_enrollments ce ON c.id = ce.classroom_id
LEFT JOIN users u ON ce.user_id = u.id
GROUP BY c.id, c.name
ORDER BY c.id;

-- 5. Kiểm tra assignments và submissions cho classroom có vấn đề
SELECT 'ASSIGNMENT_SUBMISSION_ANALYSIS' as check_type;
SELECT 
    c.id as classroom_id,
    c.name as classroom_name,
    COUNT(DISTINCT a.id) as assignment_count,
    COUNT(DISTINCT s.id) as submission_count,
    COUNT(DISTINCT s.student_id) as unique_submitters,
    COUNT(DISTINCT ce.user_id) as enrolled_users,
    COUNT(DISTINCT CASE WHEN u.role_id = 1 THEN ce.user_id END) as enrolled_students
FROM classrooms c
LEFT JOIN assignments a ON c.id = a.classroom_id
LEFT JOIN submissions s ON a.id = s.assignment_id
LEFT JOIN classroom_enrollments ce ON c.id = ce.classroom_id
LEFT JOIN users u ON ce.user_id = u.id
GROUP BY c.id, c.name
ORDER BY c.id;

-- 6. Tìm submissions từ non-enrolled students
SELECT 'INVALID_SUBMISSIONS' as check_type;
SELECT 
    s.id as submission_id,
    s.assignment_id,
    a.classroom_id,
    s.student_id,
    student.full_name as student_name,
    student.email as student_email,
    CASE 
        WHEN ce.user_id IS NULL THEN 'NOT_ENROLLED'
        ELSE 'ENROLLED'
    END as enrollment_status
FROM submissions s
JOIN assignments a ON s.assignment_id = a.id
JOIN users student ON s.student_id = student.id
LEFT JOIN classroom_enrollments ce ON a.classroom_id = ce.classroom_id AND s.student_id = ce.user_id
WHERE ce.user_id IS NULL
ORDER BY a.classroom_id, s.id;

-- 7. Kiểm tra lazy loading issue - so sánh với query trực tiếp
SELECT 'LAZY_LOADING_COMPARISON' as check_type;
-- Mô phỏng query từ findStudentIdsByClassroomId
SELECT 
    ce.classroom_id,
    'JPQL_QUERY' as method,
    COUNT(DISTINCT ce.user_id) as student_count
FROM classroom_enrollments ce
GROUP BY ce.classroom_id

UNION ALL

-- Mô phỏng query từ findById_ClassroomId
SELECT 
    ce.classroom_id,
    'ENROLLMENT_ENTITY' as method,
    COUNT(*) as student_count
FROM classroom_enrollments ce
GROUP BY ce.classroom_id

ORDER BY classroom_id, method;

-- 8. Kiểm tra status của users (active/disabled)
SELECT 'USER_STATUS_CHECK' as check_type;
SELECT 
    ce.classroom_id,
    u.status,
    COUNT(*) as count
FROM classroom_enrollments ce
JOIN users u ON ce.user_id = u.id
GROUP BY ce.classroom_id, u.status
ORDER BY ce.classroom_id, u.status;

-- 9. Tìm potential data inconsistencies
SELECT 'POTENTIAL_ISSUES' as check_type;
SELECT 
    'Classrooms with 0 enrollments but have assignments' as issue_type,
    COUNT(*) as count
FROM classrooms c
LEFT JOIN classroom_enrollments ce ON c.id = ce.classroom_id
JOIN assignments a ON c.id = a.classroom_id
WHERE ce.classroom_id IS NULL

UNION ALL

SELECT 
    'Assignments with submissions but classroom has 0 enrolled students' as issue_type,
    COUNT(DISTINCT a.classroom_id) as count
FROM assignments a
JOIN submissions s ON a.id = s.assignment_id
LEFT JOIN classroom_enrollments ce ON a.classroom_id = ce.classroom_id
LEFT JOIN users u ON ce.user_id = u.id AND u.role_id = 1
WHERE u.id IS NULL;
