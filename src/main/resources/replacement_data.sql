-- =====================================================
-- REPLACEMENT DATA SCRIPT FOR CLEANED INVALID RECORDS
-- =====================================================
-- This script creates valid replacement data to ensure
-- the system functions properly after cleanup operations
-- =====================================================

-- First, let's check current data to understand the structure
-- Run this to see current assignments and enrollments:
-- SELECT a.id, a.title, a.classroom_id, c.name as classroom_name, c.teacher_id 
-- FROM assignments a JOIN classrooms c ON a.classroom_id = c.id;

-- SELECT ce.classroom_id, ce.user_id, u.full_name, u.role_id 
-- FROM classroom_enrollments ce JOIN users u ON ce.user_id = u.id 
-- WHERE u.role_id = 1 ORDER BY ce.classroom_id;

-- =====================================================
-- VALID SUBMISSIONS FROM ENROLLED STUDENTS
-- =====================================================
-- Create submissions for Assignment 1 (Bài tập Java cơ bản) - Classroom 1
INSERT INTO submissions (assignment_id, student_id, comment, submitted_at)
SELECT 1, ce.user_id, 
       'Bài tập Java cơ bản đã hoàn thành - ' + u.full_name + ' - Classroom: Lập trình Java',
       DATEADD(day, -2, GETDATE())
FROM classroom_enrollments ce 
JOIN users u ON ce.user_id = u.id
WHERE ce.classroom_id = 1 AND u.role_id = 1 
AND NOT EXISTS (SELECT 1 FROM submissions s WHERE s.assignment_id = 1 AND s.student_id = ce.user_id);

-- Create submissions for Assignment 2 (Bài tập Python nâng cao) - Classroom 2  
INSERT INTO submissions (assignment_id, student_id, comment, submitted_at)
SELECT 2, ce.user_id,
       'Bài tập Python nâng cao đã hoàn thành - ' + u.full_name + ' - Classroom: Lập trình Python',
       DATEADD(day, -1, GETDATE())
FROM classroom_enrollments ce
JOIN users u ON ce.user_id = u.id  
WHERE ce.classroom_id = 2 AND u.role_id = 1
AND NOT EXISTS (SELECT 1 FROM submissions s WHERE s.assignment_id = 2 AND s.student_id = ce.user_id);

-- Create submissions for Assignment 3 (Bài tập Web Development) - Classroom 3
INSERT INTO submissions (assignment_id, student_id, comment, submitted_at)
SELECT 3, ce.user_id,
       'Bài tập Web Development đã hoàn thành - ' + u.full_name + ' - Classroom: Phát triển Web',
       DATEADD(hour, -12, GETDATE())
FROM classroom_enrollments ce
JOIN users u ON ce.user_id = u.id
WHERE ce.classroom_id = 3 AND u.role_id = 1  
AND NOT EXISTS (SELECT 1 FROM submissions s WHERE s.assignment_id = 3 AND s.student_id = ce.user_id);

-- Create submissions for Assignment 4 (Bài tập Database) - Classroom 4
INSERT INTO submissions (assignment_id, student_id, comment, submitted_at)
SELECT 4, ce.user_id,
       'Bài tập Database đã hoàn thành - ' + u.full_name + ' - Classroom: Cơ sở dữ liệu',
       DATEADD(hour, -6, GETDATE())
FROM classroom_enrollments ce
JOIN users u ON ce.user_id = u.id
WHERE ce.classroom_id = 4 AND u.role_id = 1
AND NOT EXISTS (SELECT 1 FROM submissions s WHERE s.assignment_id = 4 AND s.student_id = ce.user_id);

-- =====================================================
-- VALID MESSAGES BETWEEN USERS WITH CLASSROOM CONTEXT
-- =====================================================
-- Messages from students to their teachers in the same classroom

-- Java classroom messages (Classroom 1)
INSERT INTO student_messages (sender_id, recipient_id, subject, content, sent_at)
SELECT ce_student.user_id, c.teacher_id,
       'Câu hỏi về bài tập Java',
       'Thầy/Cô ơi, em có thắc mắc về bài tập Java cơ bản. Em không hiểu rõ về phần inheritance. Mong thầy/cô giải thích thêm ạ.',
       DATEADD(hour, -8, GETDATE())
FROM classroom_enrollments ce_student
JOIN classrooms c ON ce_student.classroom_id = c.id
JOIN users u ON ce_student.user_id = u.id
WHERE ce_student.classroom_id = 1 AND u.role_id = 1
AND NOT EXISTS (
    SELECT 1 FROM student_messages sm 
    WHERE sm.sender_id = ce_student.user_id AND sm.recipient_id = c.teacher_id
    AND sm.subject = 'Câu hỏi về bài tập Java'
)
AND ROWNUM <= 2; -- Limit to 2 messages

-- Python classroom messages (Classroom 2)  
INSERT INTO student_messages (sender_id, recipient_id, subject, content, sent_at)
SELECT ce_student.user_id, c.teacher_id,
       'Xin phép nộp bài muộn',
       'Thầy/Cô ơi, em xin phép được nộp bài tập Python muộn 1 ngày do có việc gia đình đột xuất. Em cam kết sẽ hoàn thành đúng chất lượng.',
       DATEADD(hour, -4, GETDATE())
FROM classroom_enrollments ce_student
JOIN classrooms c ON ce_student.classroom_id = c.id  
JOIN users u ON ce_student.user_id = u.id
WHERE ce_student.classroom_id = 2 AND u.role_id = 1
AND NOT EXISTS (
    SELECT 1 FROM student_messages sm
    WHERE sm.sender_id = ce_student.user_id AND sm.recipient_id = c.teacher_id
    AND sm.subject = 'Xin phép nộp bài muộn'
)
AND ROWNUM <= 2; -- Limit to 2 messages

-- Web Development classroom messages (Classroom 3)
INSERT INTO student_messages (sender_id, recipient_id, subject, content, sent_at)
SELECT ce_student.user_id, c.teacher_id,
       'Tư vấn về project cuối kỳ',
       'Thầy/Cô ơi, em muốn tham khảo ý kiến về chủ đề project cuối kỳ. Em đang nghĩ làm một website e-commerce đơn giản. Thầy/Cô có góp ý gì không ạ?',
       DATEADD(hour, -2, GETDATE())
FROM classroom_enrollments ce_student
JOIN classrooms c ON ce_student.classroom_id = c.id
JOIN users u ON ce_student.user_id = u.id  
WHERE ce_student.classroom_id = 3 AND u.role_id = 1
AND NOT EXISTS (
    SELECT 1 FROM student_messages sm
    WHERE sm.sender_id = ce_student.user_id AND sm.recipient_id = c.teacher_id
    AND sm.subject = 'Tư vấn về project cuối kỳ'
)
AND ROWNUM <= 2; -- Limit to 2 messages

-- Database classroom messages (Classroom 4)
INSERT INTO student_messages (sender_id, recipient_id, subject, content, sent_at)
SELECT ce_student.user_id, c.teacher_id,
       'Khó khăn với SQL queries',
       'Thầy/Cô ơi, em gặp khó khăn với việc viết các câu lệnh SQL phức tạp, đặc biệt là JOIN nhiều bảng. Thầy/Cô có thể hướng dẫn thêm không ạ?',
       DATEADD(hour, -1, GETDATE())
FROM classroom_enrollments ce_student  
JOIN classrooms c ON ce_student.classroom_id = c.id
JOIN users u ON ce_student.user_id = u.id
WHERE ce_student.classroom_id = 4 AND u.role_id = 1
AND NOT EXISTS (
    SELECT 1 FROM student_messages sm
    WHERE sm.sender_id = ce_student.user_id AND sm.recipient_id = c.teacher_id  
    AND sm.subject = 'Khó khăn với SQL queries'
)
AND ROWNUM <= 2; -- Limit to 2 messages

-- =====================================================
-- ADDITIONAL ATTENDANCE RECORDS (IF NEEDED)
-- =====================================================
-- Note: Attendance records were already healthy, but we can add more if needed
-- This section is commented out as attendance was not affected by cleanup

/*
-- Add attendance for recent sessions from enrolled students only
INSERT INTO attendance_records (session_id, student_id, status, recorded_at)
SELECT s.id, ce.user_id, 'PRESENT', GETDATE()
FROM attendance_sessions s
JOIN classroom_enrollments ce ON ce.classroom_id = s.classroom_id
JOIN users u ON ce.user_id = u.id
WHERE u.role_id = 1 
AND s.session_date >= DATEADD(day, -7, GETDATE()) -- Last 7 days
AND NOT EXISTS (
    SELECT 1 FROM attendance_records ar 
    WHERE ar.session_id = s.id AND ar.student_id = ce.user_id
);
*/

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================
-- Run these queries after executing the script to verify the data

-- Check new submissions count by classroom
-- SELECT c.name as classroom_name, COUNT(s.id) as submission_count
-- FROM classrooms c
-- LEFT JOIN assignments a ON a.classroom_id = c.id  
-- LEFT JOIN submissions s ON s.assignment_id = a.id
-- GROUP BY c.id, c.name
-- ORDER BY submission_count DESC;

-- Check new messages count by classroom context
-- SELECT c.name as classroom_name, COUNT(sm.id) as message_count
-- FROM classrooms c
-- LEFT JOIN student_messages sm ON sm.recipient_id = c.teacher_id
-- LEFT JOIN classroom_enrollments ce ON ce.user_id = sm.sender_id AND ce.classroom_id = c.id
-- WHERE ce.user_id IS NOT NULL
-- GROUP BY c.id, c.name
-- ORDER BY message_count DESC;

-- Check data integrity after insertion
-- SELECT 'Submissions' as data_type, COUNT(*) as total_count FROM submissions
-- UNION ALL
-- SELECT 'Messages' as data_type, COUNT(*) as total_count FROM student_messages  
-- UNION ALL
-- SELECT 'Attendance' as data_type, COUNT(*) as total_count FROM attendance_records
-- UNION ALL
-- SELECT 'Enrollments' as data_type, COUNT(*) as total_count FROM classroom_enrollments;

PRINT 'Replacement data script completed successfully!';
PRINT 'Valid submissions and messages have been created for enrolled students only.';
PRINT 'All new data follows business rules and maintains data integrity.';
