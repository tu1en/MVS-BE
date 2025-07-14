-- Script để kiểm tra submissions trùng lặp cho assignment ID = 4
-- Mục tiêu: Tìm hiểu tại sao có 3 submissions cho 1 sinh viên 1 bài tập

-- 1. Kiểm tra tất cả submissions cho assignment ID = 4
SELECT 'ALL SUBMISSIONS FOR ASSIGNMENT 4' as check_type;
SELECT s.id, s.assignment_id, s.student_id, s.comment, 
       s.submitted_at, s.score, s.feedback, s.graded_at, s.graded_by_id
FROM submissions s
WHERE s.assignment_id = 4
ORDER BY s.id;

-- 2. Kiểm tra submissions với thông tin sinh viên
SELECT 'SUBMISSIONS WITH STUDENT INFO' as check_type;
SELECT s.id as submission_id, s.assignment_id, s.student_id, 
       s.comment, s.submitted_at, s.score, s.feedback,
       u.full_name as student_name, u.email as student_email, u.username as student_username
FROM submissions s
LEFT JOIN users u ON s.student_id = u.id
WHERE s.assignment_id = 4
ORDER BY s.id;

-- 3. Kiểm tra duplicate submissions (cùng assignment_id và student_id)
SELECT 'DUPLICATE SUBMISSIONS CHECK' as check_type;
SELECT s.assignment_id, s.student_id, COUNT(*) as submission_count,
       u.full_name as student_name, u.email as student_email
FROM submissions s
LEFT JOIN users u ON s.student_id = u.id
WHERE s.assignment_id = 4
GROUP BY s.assignment_id, s.student_id, u.full_name, u.email
HAVING COUNT(*) > 1;

-- 4. Kiểm tra thời gian nộp bài để xem có pattern nào không
SELECT 'SUBMISSION TIMING ANALYSIS' as check_type;
SELECT s.id, s.student_id, s.submitted_at,
       u.full_name as student_name,
       DATEDIFF(SECOND, LAG(s.submitted_at) OVER (PARTITION BY s.student_id ORDER BY s.submitted_at), s.submitted_at) as seconds_between_submissions
FROM submissions s
LEFT JOIN users u ON s.student_id = u.id
WHERE s.assignment_id = 4
ORDER BY s.student_id, s.submitted_at;

-- 5. Kiểm tra nội dung submissions có giống nhau không
SELECT 'SUBMISSION CONTENT COMPARISON' as check_type;
SELECT s.id, s.student_id, s.comment, 
       CASE 
           WHEN s.comment = LAG(s.comment) OVER (PARTITION BY s.student_id ORDER BY s.id) THEN 'DUPLICATE_CONTENT'
           ELSE 'DIFFERENT_CONTENT'
       END as content_status
FROM submissions s
WHERE s.assignment_id = 4
ORDER BY s.student_id, s.id;

-- 6. Tìm submissions có thể xóa (giữ lại submission mới nhất)
SELECT 'SUBMISSIONS TO DELETE' as check_type;
SELECT s.id, s.student_id, s.submitted_at, u.full_name,
       ROW_NUMBER() OVER (PARTITION BY s.student_id ORDER BY s.submitted_at DESC) as row_num
FROM submissions s
LEFT JOIN users u ON s.student_id = u.id
WHERE s.assignment_id = 4
ORDER BY s.student_id, s.submitted_at DESC;
