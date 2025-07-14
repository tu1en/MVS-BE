-- Comprehensive Data Consistency Check Script
-- Mục tiêu: Tìm tất cả các vấn đề data consistency tương tự như submissions trùng lặp

-- ============================================================================
-- 1. SUBMISSIONS CONSISTENCY CHECK
-- ============================================================================
SELECT '=== SUBMISSIONS CONSISTENCY CHECK ===' as check_section;

-- 1.1. Submissions từ non-enrolled students
SELECT 'SUBMISSIONS FROM NON-ENROLLED STUDENTS' as issue_type;
SELECT s.id as submission_id, s.assignment_id, s.student_id, 
       u.full_name as student_name, u.email as student_email,
       a.classroom_id, c.name as classroom_name
FROM submissions s
JOIN assignments a ON s.assignment_id = a.id
JOIN users u ON s.student_id = u.id
JOIN classrooms c ON a.classroom_id = c.id
LEFT JOIN classroom_enrollments ce ON ce.classroom_id = a.classroom_id AND ce.user_id = s.student_id
WHERE ce.user_id IS NULL
ORDER BY s.assignment_id, s.student_id;

-- 1.2. Duplicate submissions (same student, same assignment)
SELECT 'DUPLICATE SUBMISSIONS' as issue_type;
SELECT s.assignment_id, s.student_id, COUNT(*) as submission_count,
       u.full_name as student_name, a.title as assignment_title
FROM submissions s
JOIN users u ON s.student_id = u.id
JOIN assignments a ON s.assignment_id = a.id
GROUP BY s.assignment_id, s.student_id, u.full_name, a.title
HAVING COUNT(*) > 1
ORDER BY submission_count DESC;

-- ============================================================================
-- 2. ATTENDANCE CONSISTENCY CHECK
-- ============================================================================
SELECT '=== ATTENDANCE CONSISTENCY CHECK ===' as check_section;

-- 2.1. Attendance từ non-enrolled students
SELECT 'ATTENDANCE FROM NON-ENROLLED STUDENTS' as issue_type;
SELECT att.id as attendance_id, att.student_id, att.session_id,
       u.full_name as student_name, u.email as student_email,
       ats.classroom_id, c.name as classroom_name
FROM attendance att
JOIN attendance_sessions ats ON att.session_id = ats.id
JOIN users u ON att.student_id = u.id
JOIN classrooms c ON ats.classroom_id = c.id
LEFT JOIN classroom_enrollments ce ON ce.classroom_id = ats.classroom_id AND ce.user_id = att.student_id
WHERE ce.user_id IS NULL
ORDER BY ats.classroom_id, att.student_id;

-- 2.2. Duplicate attendance records (same student, same session)
SELECT 'DUPLICATE ATTENDANCE RECORDS' as issue_type;
SELECT att.session_id, att.student_id, COUNT(*) as attendance_count,
       u.full_name as student_name, ats.session_date
FROM attendance att
JOIN users u ON att.student_id = u.id
JOIN attendance_sessions ats ON att.session_id = ats.id
GROUP BY att.session_id, att.student_id, u.full_name, ats.session_date
HAVING COUNT(*) > 1
ORDER BY attendance_count DESC;

-- ============================================================================
-- 3. ENROLLMENT CONSISTENCY CHECK
-- ============================================================================
SELECT '=== ENROLLMENT CONSISTENCY CHECK ===' as check_section;

-- 3.1. Duplicate enrollments (same student, same classroom)
SELECT 'DUPLICATE ENROLLMENTS' as issue_type;
SELECT ce.classroom_id, ce.user_id, COUNT(*) as enrollment_count,
       u.full_name as student_name, c.name as classroom_name
FROM classroom_enrollments ce
JOIN users u ON ce.user_id = u.id
JOIN classrooms c ON ce.classroom_id = c.id
GROUP BY ce.classroom_id, ce.user_id, u.full_name, c.name
HAVING COUNT(*) > 1
ORDER BY enrollment_count DESC;

-- 3.2. Enrollments với invalid user_id hoặc classroom_id
SELECT 'ORPHANED ENROLLMENTS' as issue_type;
SELECT ce.classroom_id, ce.user_id, 
       CASE WHEN u.id IS NULL THEN 'INVALID_USER' ELSE 'VALID_USER' END as user_status,
       CASE WHEN c.id IS NULL THEN 'INVALID_CLASSROOM' ELSE 'VALID_CLASSROOM' END as classroom_status
FROM classroom_enrollments ce
LEFT JOIN users u ON ce.user_id = u.id
LEFT JOIN classrooms c ON ce.classroom_id = c.id
WHERE u.id IS NULL OR c.id IS NULL;

-- ============================================================================
-- 4. USER ROLE CONSISTENCY CHECK
-- ============================================================================
SELECT '=== USER ROLE CONSISTENCY CHECK ===' as check_section;

-- 4.1. Users với invalid role_id
SELECT 'USERS WITH INVALID ROLES' as issue_type;
SELECT u.id, u.username, u.email, u.full_name, u.role_id,
       CASE WHEN r.id IS NULL THEN 'INVALID_ROLE' ELSE 'VALID_ROLE' END as role_status
FROM users u
LEFT JOIN roles r ON u.role_id = r.id
WHERE r.id IS NULL;

-- 4.2. Duplicate user emails
SELECT 'DUPLICATE USER EMAILS' as issue_type;
SELECT u.email, COUNT(*) as user_count,
       GROUP_CONCAT(u.id) as user_ids,
       GROUP_CONCAT(u.full_name) as user_names
FROM users u
WHERE u.email IS NOT NULL AND u.email != ''
GROUP BY u.email
HAVING COUNT(*) > 1
ORDER BY user_count DESC;

-- ============================================================================
-- 5. ASSIGNMENT CONSISTENCY CHECK
-- ============================================================================
SELECT '=== ASSIGNMENT CONSISTENCY CHECK ===' as check_section;

-- 5.1. Assignments với invalid classroom_id
SELECT 'ASSIGNMENTS WITH INVALID CLASSROOM' as issue_type;
SELECT a.id, a.title, a.classroom_id,
       CASE WHEN c.id IS NULL THEN 'INVALID_CLASSROOM' ELSE 'VALID_CLASSROOM' END as classroom_status
FROM assignments a
LEFT JOIN classrooms c ON a.classroom_id = c.id
WHERE c.id IS NULL;

-- 5.2. Assignments với invalid teacher_id
SELECT 'ASSIGNMENTS WITH INVALID TEACHER' as issue_type;
SELECT a.id, a.title, a.teacher_id,
       CASE WHEN u.id IS NULL THEN 'INVALID_TEACHER' ELSE 'VALID_TEACHER' END as teacher_status
FROM assignments a
LEFT JOIN users u ON a.teacher_id = u.id
WHERE a.teacher_id IS NOT NULL AND u.id IS NULL;

-- ============================================================================
-- 6. LECTURE CONSISTENCY CHECK
-- ============================================================================
SELECT '=== LECTURE CONSISTENCY CHECK ===' as check_section;

-- 6.1. Lectures với invalid classroom_id
SELECT 'LECTURES WITH INVALID CLASSROOM' as issue_type;
SELECT l.id, l.title, l.classroom_id,
       CASE WHEN c.id IS NULL THEN 'INVALID_CLASSROOM' ELSE 'VALID_CLASSROOM' END as classroom_status
FROM lectures l
LEFT JOIN classrooms c ON l.classroom_id = c.id
WHERE c.id IS NULL;

-- 6.2. Lectures missing dates
SELECT 'LECTURES WITHOUT DATES' as issue_type;
SELECT l.id, l.title, l.classroom_id, c.name as classroom_name
FROM lectures l
JOIN classrooms c ON l.classroom_id = c.id
WHERE l.lecture_date IS NULL;

-- ============================================================================
-- 7. SUMMARY STATISTICS
-- ============================================================================
SELECT '=== SUMMARY STATISTICS ===' as check_section;

SELECT 'TOTAL COUNTS' as summary_type;
SELECT 
    (SELECT COUNT(*) FROM users) as total_users,
    (SELECT COUNT(*) FROM classrooms) as total_classrooms,
    (SELECT COUNT(*) FROM classroom_enrollments) as total_enrollments,
    (SELECT COUNT(*) FROM assignments) as total_assignments,
    (SELECT COUNT(*) FROM submissions) as total_submissions,
    (SELECT COUNT(*) FROM attendance) as total_attendance_records,
    (SELECT COUNT(*) FROM lectures) as total_lectures;

-- Tỷ lệ submissions vs enrollments
SELECT 'SUBMISSION TO ENROLLMENT RATIO' as summary_type;
SELECT 
    c.id as classroom_id,
    c.name as classroom_name,
    COUNT(DISTINCT ce.user_id) as enrolled_students,
    COUNT(DISTINCT s.student_id) as students_with_submissions,
    COUNT(s.id) as total_submissions,
    ROUND(COUNT(s.id) * 1.0 / NULLIF(COUNT(DISTINCT ce.user_id), 0), 2) as submissions_per_student
FROM classrooms c
LEFT JOIN classroom_enrollments ce ON c.id = ce.classroom_id
LEFT JOIN assignments a ON c.id = a.classroom_id
LEFT JOIN submissions s ON a.id = s.assignment_id
GROUP BY c.id, c.name
HAVING enrolled_students > 0 OR students_with_submissions > 0
ORDER BY c.id;
