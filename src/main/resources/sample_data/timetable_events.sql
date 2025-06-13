-- Sample timetable events for June 2025
INSERT INTO timetable_events (
    title, description, start_datetime, end_datetime, event_type, 
    classroom_id, created_by, location, is_all_day, color
) VALUES 
('Math Class', 'Regular math class for first-year students', 
 '2025-06-12 09:00:00', '2025-06-12 10:30:00', 'CLASS', 1, 1, 'Room 101', FALSE, '#007bff'),
 
('History Exam', 'Mid-term history exam', 
 '2025-06-14 13:00:00', '2025-06-14 15:00:00', 'EXAM', 1, 1, 'Exam Hall A', FALSE, '#dc3545'),
 
('Science Project Meeting', 'Meeting to discuss science project progress', 
 '2025-06-16 14:00:00', '2025-06-16 15:30:00', 'MEETING', 2, 2, 'Room 202', FALSE, '#28a745'),
 
('Literature Assignment Due', 'Submit literature essay', 
 '2025-06-18 23:59:00', '2025-06-18 23:59:00', 'ASSIGNMENT_DUE', 3, 3, NULL, TRUE, '#ffc107'),
 
('School Holiday', 'National holiday - no classes', 
 '2025-06-20 00:00:00', '2025-06-20 23:59:00', 'HOLIDAY', NULL, 1, NULL, TRUE, '#6c757d'); 