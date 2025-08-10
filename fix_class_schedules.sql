-- Fix Class Schedule Data
-- This script adds schedule data to classes that don't have any

-- Check current schedule data
SELECT 
    id, 
    class_name, 
    schedule_json,
    CASE 
        WHEN schedule_json IS NULL OR schedule_json = '' THEN 'NO SCHEDULE' 
        ELSE 'HAS SCHEDULE' 
    END as schedule_status
FROM classes 
ORDER BY id;

-- Update classes without schedule data with sample schedules
UPDATE classes 
SET schedule_json = 
    CASE 
        WHEN id % 6 = 1 THEN '{"days": ["monday", "wednesday", "friday"], "startTime": "08:00", "endTime": "10:00", "duration": 120}'
        WHEN id % 6 = 2 THEN '{"days": ["tuesday", "thursday"], "startTime": "14:00", "endTime": "16:00", "duration": 120}'
        WHEN id % 6 = 3 THEN '{"days": ["monday", "wednesday"], "startTime": "10:00", "endTime": "12:00", "duration": 120}'
        WHEN id % 6 = 4 THEN '{"days": ["tuesday", "friday"], "startTime": "16:00", "endTime": "18:00", "duration": 120}'
        WHEN id % 6 = 5 THEN '{"days": ["wednesday", "friday"], "startTime": "09:00", "endTime": "11:00", "duration": 120}'
        ELSE '{"days": ["monday", "thursday"], "startTime": "13:00", "endTime": "15:00", "duration": 120}'
    END
WHERE schedule_json IS NULL OR schedule_json = '';

-- Verify the update
SELECT 
    id, 
    class_name, 
    schedule_json,
    CASE 
        WHEN schedule_json IS NULL OR schedule_json = '' THEN 'NO SCHEDULE' 
        ELSE 'HAS SCHEDULE' 
    END as schedule_status
FROM classes 
ORDER BY id;

-- Count classes with and without schedules
SELECT 
    SUM(CASE WHEN schedule_json IS NULL OR schedule_json = '' THEN 1 ELSE 0 END) as classes_without_schedule,
    SUM(CASE WHEN schedule_json IS NOT NULL AND schedule_json != '' THEN 1 ELSE 0 END) as classes_with_schedule,
    COUNT(*) as total_classes
FROM classes;

PRINT 'Class schedule data has been updated successfully!';