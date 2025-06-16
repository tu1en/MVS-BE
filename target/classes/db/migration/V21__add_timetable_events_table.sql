-- Create timetable_events table
CREATE TABLE IF NOT EXISTS timetable_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_datetime TIMESTAMP NOT NULL,
    end_datetime TIMESTAMP NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    classroom_id BIGINT,
    created_by BIGINT NOT NULL,
    location VARCHAR(255),
    is_all_day BOOLEAN DEFAULT FALSE,
    reminder_minutes INT DEFAULT 15,
    color VARCHAR(7) DEFAULT '#007bff',
    recurring_rule VARCHAR(255),
    parent_event_id BIGINT,
    is_cancelled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_timetable_classroom FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE SET NULL,
    CONSTRAINT fk_timetable_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_timetable_parent FOREIGN KEY (parent_event_id) REFERENCES timetable_events(id) ON DELETE CASCADE
);

-- Create index for faster querying
CREATE INDEX idx_timetable_start_datetime ON timetable_events (start_datetime);
CREATE INDEX idx_timetable_end_datetime ON timetable_events (end_datetime);
CREATE INDEX idx_timetable_classroom ON timetable_events (classroom_id);
CREATE INDEX idx_timetable_creator ON timetable_events (created_by);

-- Insert some sample data for testing
INSERT INTO timetable_events (
    title, description, start_datetime, end_datetime, event_type, 
    classroom_id, created_by, location, is_all_day, color
) VALUES 
('Math Class', 'Regular math class for first-year students', 
 '2024-06-10 09:00:00', '2024-06-10 10:30:00', 'CLASS', 1, 1, 'Room 101', FALSE, '#007bff'),
 
('History Exam', 'Mid-term history exam', 
 '2024-06-12 13:00:00', '2024-06-12 15:00:00', 'EXAM', 1, 1, 'Exam Hall A', FALSE, '#dc3545'),
 
('Science Project Meeting', 'Meeting to discuss science project progress', 
 '2024-06-14 14:00:00', '2024-06-14 15:30:00', 'MEETING', 2, 2, 'Room 202', FALSE, '#28a745'),
 
('Literature Assignment Due', 'Submit literature essay', 
 '2024-06-15 23:59:00', '2024-06-15 23:59:00', 'ASSIGNMENT_DUE', 3, 3, NULL, TRUE, '#ffc107'),
 
('School Holiday', 'National holiday - no classes', 
 '2024-06-17 00:00:00', '2024-06-17 23:59:00', 'HOLIDAY', NULL, 1, NULL, TRUE, '#6c757d'); 