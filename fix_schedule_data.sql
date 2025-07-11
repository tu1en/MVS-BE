-- Fix Schedule Data for Teacher ID Mismatch
-- This script addresses the issue where schedules were created for user ID 201 
-- but the actual teacher has ID 2

-- First, let's see what users we have
SELECT id, username, full_name, role_id FROM users ORDER BY id;

-- Check current schedules
SELECT COUNT(*) as total_schedules FROM schedules;
SELECT teacher_id, COUNT(*) as schedule_count FROM schedules GROUP BY teacher_id;

-- Find the teacher user
SELECT id, username, full_name, role_id FROM users WHERE username = 'teacher';

-- Delete any existing schedules for the teacher (to avoid duplicates)
DELETE FROM schedules WHERE teacher_id IN (SELECT id FROM users WHERE username = 'teacher');

-- Now let's create schedules for the actual teacher ID
-- We'll use a variable to store the teacher ID
DECLARE @teacher_id BIGINT;
SELECT @teacher_id = id FROM users WHERE username = 'teacher';

-- Get a classroom ID to use
DECLARE @classroom_id BIGINT;
SELECT TOP 1 @classroom_id = id FROM classrooms;

-- Insert comprehensive schedule data for the teacher
INSERT INTO schedules (teacher_id, classroom_id, day_of_week, start_time, end_time, room, subject, materials_url, meet_url)
VALUES 
-- Monday schedules
(@teacher_id, @classroom_id, 0, '08:00:00', '09:30:00', 'Room 101', 'Java Programming - Fundamentals', 'https://drive.google.com/folder/java-fundamentals', 'https://meet.google.com/java-fundamentals'),
(@teacher_id, @classroom_id, 0, '14:00:00', '15:30:00', 'Room 102', 'Database Design - Theory', 'https://drive.google.com/folder/database-theory', 'https://meet.google.com/database-theory'),

-- Tuesday schedules  
(@teacher_id, @classroom_id, 1, '09:00:00', '10:30:00', 'Lab 201', 'Java Programming - Practice', 'https://drive.google.com/folder/java-practice', 'https://meet.google.com/java-practice'),
(@teacher_id, @classroom_id, 1, '18:00:00', '19:30:00', 'Online', 'Evening Tutorial Session', 'https://drive.google.com/folder/evening-tutorial', 'https://meet.google.com/evening-tutorial'),

-- Wednesday schedules
(@teacher_id, @classroom_id, 2, '08:00:00', '09:30:00', 'Room 102', 'Database Design - Practice', 'https://drive.google.com/folder/database-practice', 'https://meet.google.com/database-practice'),
(@teacher_id, @classroom_id, 2, '15:00:00', '16:30:00', 'Room 101', 'Advanced Java Topics', 'https://drive.google.com/folder/advanced-java', 'https://meet.google.com/advanced-java'),

-- Thursday schedules
(@teacher_id, @classroom_id, 3, '10:00:00', '11:30:00', 'Room 103', 'Software Engineering', 'https://drive.google.com/folder/software-engineering', 'https://meet.google.com/software-engineering'),

-- Friday schedules
(@teacher_id, @classroom_id, 4, '08:00:00', '09:30:00', 'Room 101', 'Java Programming - Review', 'https://drive.google.com/folder/java-review', 'https://meet.google.com/java-review'),
(@teacher_id, @classroom_id, 4, '13:00:00', '14:30:00', 'Lab 202', 'Database Lab Session', 'https://drive.google.com/folder/database-lab', 'https://meet.google.com/database-lab'),

-- Saturday workshop
(@teacher_id, @classroom_id, 5, '10:00:00', '12:00:00', 'Workshop Hall', 'Weekend Java Workshop', 'https://drive.google.com/folder/weekend-workshop', 'https://meet.google.com/weekend-workshop');

-- Verify the data was inserted
SELECT COUNT(*) as schedules_created FROM schedules WHERE teacher_id = @teacher_id;

-- Show sample schedules
SELECT TOP 5 
    s.day_of_week,
    s.start_time,
    s.end_time,
    s.room,
    s.subject,
    u.full_name as teacher_name
FROM schedules s
JOIN users u ON s.teacher_id = u.id
WHERE s.teacher_id = @teacher_id
ORDER BY s.day_of_week, s.start_time;

PRINT 'Schedule data has been fixed for teacher ID: ' + CAST(@teacher_id AS VARCHAR);
