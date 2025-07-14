-- Comprehensive verification script for data consistency fixes
-- This script verifies that all enrollment data consistency issues have been resolved

-- 1. Verify no invalid submissions remain
SELECT 'INVALID_SUBMISSIONS_CHECK' as test_name;
SELECT 
    COUNT(*) as invalid_submission_count,
    CASE 
        WHEN COUNT(*) = 0 THEN 'PASS - No invalid submissions found'
        ELSE 'FAIL - Invalid submissions still exist'
    END as test_result
FROM (
    SELECT s.id as submission_id
    FROM submissions s
    JOIN assignments a ON s.assignment_id = a.id
    JOIN users student ON s.student_id = student.id
    LEFT JOIN classroom_enrollments ce ON a.classroom_id = ce.classroom_id AND s.student_id = ce.user_id
    WHERE ce.user_id IS NULL
) AS invalid_subs;

-- 2. Verify enrollment count consistency across all classrooms
SELECT 'ENROLLMENT_COUNT_CONSISTENCY' as test_name;
SELECT 
    c.id as classroom_id,
    c.name as classroom_name,
    COUNT(ce.user_id) as enrollment_count,
    COUNT(DISTINCT s.student_id) as unique_submitters,
    CASE 
        WHEN COUNT(DISTINCT s.student_id) <= COUNT(ce.user_id) THEN 'PASS'
        ELSE 'FAIL - More submitters than enrolled students'
    END as consistency_check
FROM classrooms c
LEFT JOIN classroom_enrollments ce ON c.id = ce.classroom_id
LEFT JOIN assignments a ON c.id = a.classroom_id
LEFT JOIN submissions s ON a.id = s.assignment_id
GROUP BY c.id, c.name
ORDER BY c.id;

-- 3. Verify submission statistics accuracy
SELECT 'SUBMISSION_STATISTICS_ACCURACY' as test_name;
SELECT 
    a.id as assignment_id,
    a.title as assignment_title,
    a.classroom_id,
    COUNT(DISTINCT ce.user_id) as enrolled_students,
    COUNT(DISTINCT s.student_id) as students_with_submissions,
    COUNT(s.id) as total_submissions,
    CASE 
        WHEN COUNT(DISTINCT s.student_id) <= COUNT(DISTINCT ce.user_id) THEN 'PASS'
        ELSE 'FAIL - More submitters than enrolled students'
    END as accuracy_check
FROM assignments a
LEFT JOIN classroom_enrollments ce ON a.classroom_id = ce.classroom_id
LEFT JOIN submissions s ON a.id = s.assignment_id
GROUP BY a.id, a.title, a.classroom_id
ORDER BY a.classroom_id, a.id;

-- 4. Verify no teacher enrollments as students
SELECT 'TEACHER_ENROLLMENT_CHECK' as test_name;
SELECT 
    COUNT(*) as teacher_enrolled_as_student_count,
    CASE 
        WHEN COUNT(*) = 0 THEN 'PASS - No teachers enrolled as students'
        ELSE 'FAIL - Teachers found enrolled as students'
    END as test_result
FROM classroom_enrollments ce
JOIN classrooms c ON ce.classroom_id = c.id
WHERE ce.user_id = c.teacher_id;

-- 5. Verify all submissions are from enrolled students
SELECT 'ALL_SUBMISSIONS_FROM_ENROLLED_STUDENTS' as test_name;
SELECT 
    COUNT(DISTINCT s.id) as total_submissions,
    COUNT(DISTINCT CASE WHEN ce.user_id IS NOT NULL THEN s.id END) as valid_submissions,
    CASE 
        WHEN COUNT(DISTINCT s.id) = COUNT(DISTINCT CASE WHEN ce.user_id IS NOT NULL THEN s.id END) 
        THEN 'PASS - All submissions are from enrolled students'
        ELSE 'FAIL - Some submissions are from non-enrolled students'
    END as test_result
FROM submissions s
JOIN assignments a ON s.assignment_id = a.id
LEFT JOIN classroom_enrollments ce ON a.classroom_id = ce.classroom_id AND s.student_id = ce.user_id;

-- 6. Summary report
SELECT 'SUMMARY_REPORT' as test_name;
SELECT 
    'Data Consistency Verification Complete' as status,
    COUNT(DISTINCT c.id) as total_classrooms,
    COUNT(DISTINCT ce.classroom_id) as classrooms_with_enrollments,
    COUNT(DISTINCT a.id) as total_assignments,
    COUNT(DISTINCT s.id) as total_submissions,
    COUNT(DISTINCT ce.user_id) as total_enrollments
FROM classrooms c
LEFT JOIN classroom_enrollments ce ON c.id = ce.classroom_id
LEFT JOIN assignments a ON c.id = a.classroom_id
LEFT JOIN submissions s ON a.id = s.assignment_id;

-- 7. Detailed classroom analysis
SELECT 'DETAILED_CLASSROOM_ANALYSIS' as test_name;
SELECT 
    c.id as classroom_id,
    c.name as classroom_name,
    t.full_name as teacher_name,
    COUNT(DISTINCT ce.user_id) as enrolled_students,
    COUNT(DISTINCT a.id) as assignments,
    COUNT(DISTINCT s.id) as submissions,
    COUNT(DISTINCT s.student_id) as unique_submitters,
    CASE 
        WHEN COUNT(DISTINCT s.student_id) <= COUNT(DISTINCT ce.user_id) OR COUNT(DISTINCT s.student_id) = 0
        THEN 'CONSISTENT'
        ELSE 'INCONSISTENT'
    END as data_consistency_status
FROM classrooms c
LEFT JOIN users t ON c.teacher_id = t.id
LEFT JOIN classroom_enrollments ce ON c.id = ce.classroom_id
LEFT JOIN assignments a ON c.id = a.classroom_id
LEFT JOIN submissions s ON a.id = s.assignment_id
GROUP BY c.id, c.name, t.full_name
ORDER BY c.id;
