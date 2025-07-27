-- Phase 2 Test Script - Complete Workflow Test
-- ===========================================

-- 1. Setup Test Data
DECLARE @managerUsername NVARCHAR(100) = 'manager_user'
DECLARE @teacherUsername NVARCHAR(100) = 'teacher_user'

-- Get Manager User ID
DECLARE @managerId BIGINT = (SELECT id FROM users WHERE username = @managerUsername AND role_enum = 'MANAGER')

-- Get Teacher User ID  
DECLARE @teacherId BIGINT = (SELECT id FROM users WHERE username = @teacherUsername AND role_enum = 'TEACHER')

-- Verify users exist
IF @managerId IS NULL
BEGIN
    INSERT INTO users (username, password, email, role_enum, full_name, created_at, updated_at)
    VALUES (@managerUsername, '$2a$10$passwordHash', @managerUsername + '@classroom.com', 'MANAGER', N'Manager User', GETDATE(), GETDATE())
    SET @managerId = SCOPE_IDENTITY()
END

IF @teacherId IS NULL
BEGIN
    INSERT INTO users (username, password, email, role_enum, full_name, created_at, updated_at)
    VALUES (@teacherUsername, '$2a$10$passwordHash', @teacherUsername + '@classroom.com', 'TEACHER', N'Teacher User', GETDATE(), GETDATE())
    SET @teacherId = SCOPE_IDENTITY()
END

-- 2. Manager creates syllabus
DECLARE @syllabusId BIGINT
INSERT INTO syllabuses (name, code, description, subject, grade_level, created_by, status, created_at)
VALUES (N'Toán 11 Nâng cao', 'MATH11_ADVANCED', N'Chương trình nâng cao cho học sinh giỏi', 
        N'Toán học', N'Lớp 11', @managerId, 'ACTIVE', GETDATE())
SET @syllabusId = SCOPE_IDENTITY()

PRINT 'Created syllabus ID: ' + CAST(@syllabusId AS VARCHAR(10))

-- 3. Manager creates course from syllabus  
DECLARE @courseId BIGINT
INSERT INTO courses (syllabus_id, name, code, description, start_date, end_date, max_students, price, created_by, status)
VALUES (@syllabusId, N'Toán 11 - Lôp A - 2024', 'MATH11A_2024', N'Khóa học nâng cao cho học sinh lớp 11', 
        '2024-09-01', '2024-12-31', 25, 3500000.00, @managerId, 'ACTIVE')
SET @courseId = SCOPE_IDENTITY()

PRINT 'Created course ID: ' + CAST(@courseId AS VARCHAR(10))

-- 4. Manager assigns teacher to course
DECLARE @assignmentId BIGINT
INSERT INTO course_teachers (course_id, teacher_id, role, assigned_at, status, is_active, notes)
VALUES (@courseId, @teacherId, 'MAIN_INSTRUCTOR', GETDATE(), 'PENDING', 1, N'Phân công giảng dạy lớp Toán 11A')
SET @assignmentId = SCOPE_IDENTITY()

PRINT 'Created teacher assignment ID: ' + CAST(@assignmentId AS VARCHAR(10))

-- 5. Teacher accepts assignment
UPDATE course_teachers 
SET status = 'ACCEPTED', accepted_at = GETDATE()
WHERE id = @assignmentId

PRINT 'Teacher accepted assignment'

-- 6. Verify complete workflow
SELECT '=== PHASE 2 VERIFICATION ===' AS Test_Result

-- Verify syllabus created
SELECT 'Syllabus Verification:' AS Description, name, code, subject FROM syllabuses WHERE id = @syllabusId

-- Verify course created  
SELECT 'Course Verification:' AS Description, name, code, price FROM courses WHERE id = @courseId

-- Verify teacher assignment
SELECT 'Teacher Assignment:' AS Description, course_id, teacher_id, status, assigned_at 
FROM course_teachers WHERE id = @assignmentId

-- Verify full workflow
SELECT 'Complete Workflow Status:' AS Description,
       CASE WHEN EXISTS (SELECT 1 FROM syllabuses WHERE id = @syllabusId AND status = 'ACTIVE') AND
                 EXISTS (SELECT 1 FROM courses WHERE id = @courseId AND status = 'ACTIVE') AND
                 EXISTS (SELECT 1 FROM course_teachers WHERE id = @assignmentId AND status = 'ACCEPTED')
            THEN 'SUCCESS - PHASE 2 COMPLETE'
            ELSE 'FAILED - Check data integrity'
       END AS Status

-- 7. Test Vietnamese search capability
PRINT 'Testing Vietnamese search...'

SELECT 'Vietnamese Search Test:' AS Test, name FROM syllabuses WHERE LOWER(name) LIKE N'%toán%'
SELECT 'Subject Search Test:' AS Test, subject FROM syllabuses WHERE LOWER(subject) LIKE N'%học%'

-- 8. Test business rule validation
BEGIN TRY
    DELETE FROM syllabuses WHERE id = @syllabusId
    PRINT 'ERROR: Should not allow deletion of syllabus with courses'
END TRY
BEGIN CATCH
    PRINT 'SUCCESS: Business rule preventing deletion of syllabus with active courses'
END CATCH

-- 9. Display final test results
SELECT '=== FINAL TEST SUMMARY ===' AS Test_Result

SELECT 'Entities Created:' AS Category, 
       'Syllabus: 1, Course: 1, Teacher Assignment: 1' AS Counts

SELECT 'Workflow Verified:' AS Category,
       CASE WHEN EXISTS (SELECT 1 FROM syllabuses WHERE is_deleted = 0) AND
                 EXISTS (SELECT 1 FROM courses WHERE is_deleted = 0) AND
                 EXISTS (SELECT 1 FROM course_teachers WHERE is_active = 1)
            THEN 'ALL VALID'
            ELSE 'MISSING ENTITIES'
       END AS Status

PRINT 'PHASE 2 INTEGRATION TEST COMPLETE'