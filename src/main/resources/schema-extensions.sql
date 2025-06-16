-- Extended Database Schema for Complete Vietnamese Classroom Management System
-- This extends the existing schema.sql with all missing tables

-- Course Materials Table
CREATE TABLE course_materials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_path VARCHAR(500),
    file_name VARCHAR(255),
    file_size BIGINT,
    file_type VARCHAR(100),
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    classroom_id BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL,
    is_public BOOLEAN DEFAULT TRUE,
    download_count INT DEFAULT 0,
    version_number INT DEFAULT 1,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Course Schedule Table
CREATE TABLE course_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    location VARCHAR(255),
    schedule_type VARCHAR(50) DEFAULT 'LECTURE', -- LECTURE, LAB, EXAM, MEETING
    recurring_type VARCHAR(50), -- NONE, DAILY, WEEKLY, MONTHLY
    recurring_end_date DATE,
    is_online BOOLEAN DEFAULT FALSE,
    meeting_link VARCHAR(500),
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Student Progress Table
CREATE TABLE student_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    classroom_id BIGINT NOT NULL,
    assignment_id BIGINT,
    progress_type VARCHAR(50) NOT NULL, -- ASSIGNMENT, LECTURE, QUIZ, OVERALL
    progress_percentage DECIMAL(5,2) DEFAULT 0.00,
    points_earned DECIMAL(10,2) DEFAULT 0.00,
    max_points DECIMAL(10,2) DEFAULT 0.00,
    completion_date TIMESTAMP,
    last_accessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    time_spent_minutes INT DEFAULT 0,
    notes TEXT,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    UNIQUE KEY unique_student_assignment (student_id, assignment_id)
);

-- Grading Rubrics Table
CREATE TABLE grading_rubrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    criteria_name VARCHAR(255) NOT NULL,
    description TEXT,
    max_points DECIMAL(10,2) NOT NULL,
    weight_percentage DECIMAL(5,2) DEFAULT 100.00,
    display_order INT DEFAULT 0,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Assignment Submissions Extended
ALTER TABLE submissions ADD COLUMN grade DECIMAL(10,2);
ALTER TABLE submissions ADD COLUMN feedback TEXT;
ALTER TABLE submissions ADD COLUMN graded_by BIGINT;
ALTER TABLE submissions ADD COLUMN graded_at TIMESTAMP;
ALTER TABLE submissions ADD COLUMN submission_type VARCHAR(50) DEFAULT 'FILE'; -- FILE, TEXT, URL
ALTER TABLE submissions ADD COLUMN file_path VARCHAR(500);
ALTER TABLE submissions ADD COLUMN file_name VARCHAR(255);
ALTER TABLE submissions ADD COLUMN status VARCHAR(50) DEFAULT 'SUBMITTED'; -- SUBMITTED, GRADED, RETURNED

-- Grading Details Table
CREATE TABLE grading_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    rubric_id BIGINT NOT NULL,
    points_awarded DECIMAL(10,2) NOT NULL,
    comments TEXT,
    graded_by BIGINT NOT NULL,
    graded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES submissions(id) ON DELETE CASCADE,
    FOREIGN KEY (rubric_id) REFERENCES grading_rubrics(id) ON DELETE CASCADE,
    FOREIGN KEY (graded_by) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_submission_rubric (submission_id, rubric_id)
);

-- Lecture Recordings Table
CREATE TABLE lecture_recordings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lecture_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    file_path VARCHAR(500),
    file_name VARCHAR(255),
    file_size BIGINT,
    duration_minutes INT,
    recording_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_public BOOLEAN DEFAULT TRUE,
    view_count INT DEFAULT 0,
    thumbnail_path VARCHAR(500),
    FOREIGN KEY (lecture_id) REFERENCES lectures(id) ON DELETE CASCADE
);

-- Live Streams Table
CREATE TABLE live_streams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lecture_id BIGINT NOT NULL,
    stream_key VARCHAR(255) UNIQUE NOT NULL,
    stream_url VARCHAR(500),
    chat_enabled BOOLEAN DEFAULT TRUE,
    max_viewers INT DEFAULT 100,
    current_viewers INT DEFAULT 0,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(50) DEFAULT 'SCHEDULED', -- SCHEDULED, LIVE, ENDED, CANCELLED
    recording_enabled BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (lecture_id) REFERENCES lectures(id) ON DELETE CASCADE
);

-- Announcements Table (replacing existing notification system)
CREATE TABLE announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    classroom_id BIGINT,
    created_by BIGINT NOT NULL,
    target_audience VARCHAR(50) DEFAULT 'ALL', -- ALL, STUDENTS, TEACHERS
    priority VARCHAR(50) DEFAULT 'NORMAL', -- LOW, NORMAL, HIGH, URGENT
    scheduled_date TIMESTAMP,
    expiry_date TIMESTAMP,
    is_pinned BOOLEAN DEFAULT FALSE,
    attachments_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'ACTIVE', -- ACTIVE, ARCHIVED, DELETED
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Announcement Attachments Table
CREATE TABLE announcement_attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    announcement_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(100),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (announcement_id) REFERENCES announcements(id) ON DELETE CASCADE
);

-- Announcement Reads Table (tracking who read what)
CREATE TABLE announcement_reads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    announcement_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (announcement_id) REFERENCES announcements(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_announcement_user (announcement_id, user_id)
);

-- Timetable Events Table
CREATE TABLE timetable_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_datetime TIMESTAMP NOT NULL,
    end_datetime TIMESTAMP NOT NULL,
    event_type VARCHAR(50) NOT NULL, -- CLASS, EXAM, MEETING, ASSIGNMENT_DUE, HOLIDAY
    classroom_id BIGINT,
    created_by BIGINT NOT NULL,
    location VARCHAR(255),
    is_all_day BOOLEAN DEFAULT FALSE,
    reminder_minutes INT DEFAULT 15,
    color VARCHAR(7) DEFAULT '#007bff',
    recurring_rule VARCHAR(255), -- RRULE format for recurring events
    parent_event_id BIGINT, -- For recurring event instances
    is_cancelled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_event_id) REFERENCES timetable_events(id) ON DELETE CASCADE
);

-- Event Attendees Table
CREATE TABLE event_attendees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    attendance_status VARCHAR(50) DEFAULT 'INVITED', -- INVITED, ACCEPTED, DECLINED, TENTATIVE
    response_date TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES timetable_events(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_event_user (event_id, user_id)
);

-- Assignment Extensions Table
ALTER TABLE assignments ADD COLUMN max_attempts INT DEFAULT 1;
ALTER TABLE assignments ADD COLUMN time_limit_minutes INT;
ALTER TABLE assignments ADD COLUMN auto_grade BOOLEAN DEFAULT FALSE;
ALTER TABLE assignments ADD COLUMN show_correct_answers BOOLEAN DEFAULT FALSE;
ALTER TABLE assignments ADD COLUMN randomize_questions BOOLEAN DEFAULT FALSE;
ALTER TABLE assignments ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE assignments ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE assignments ADD COLUMN assignment_type VARCHAR(50) DEFAULT 'HOMEWORK'; -- HOMEWORK, QUIZ, EXAM, PROJECT

-- Quiz Questions Table
CREATE TABLE quiz_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL, -- MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER, ESSAY
    correct_answer TEXT,
    points DECIMAL(10,2) DEFAULT 1.00,
    display_order INT DEFAULT 0,
    explanation TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE
);

-- Quiz Question Options Table (for multiple choice)
CREATE TABLE quiz_question_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    FOREIGN KEY (question_id) REFERENCES quiz_questions(id) ON DELETE CASCADE
);

-- Student Quiz Answers Table
CREATE TABLE student_quiz_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    selected_options JSON, -- For multiple choice answers
    text_answer TEXT, -- For text-based answers
    points_earned DECIMAL(10,2) DEFAULT 0.00,
    is_correct BOOLEAN,
    answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES submissions(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES quiz_questions(id) ON DELETE CASCADE,
    UNIQUE KEY unique_submission_question (submission_id, question_id)
);

-- Class Sessions Table (for attendance tracking)
CREATE TABLE class_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT NOT NULL,
    session_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    topic VARCHAR(255),
    description TEXT,
    session_type VARCHAR(50) DEFAULT 'REGULAR', -- REGULAR, MAKEUP, REVIEW, EXAM
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Update Attendance table to link to class sessions
ALTER TABLE attendance ADD COLUMN session_id BIGINT;
ALTER TABLE attendance ADD FOREIGN KEY (session_id) REFERENCES class_sessions(id) ON DELETE CASCADE;

-- Student Groups Table
CREATE TABLE student_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT NOT NULL,
    group_name VARCHAR(255) NOT NULL,
    description TEXT,
    max_members INT DEFAULT 10,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Group Members Table
CREATE TABLE group_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    role VARCHAR(50) DEFAULT 'MEMBER', -- LEADER, MEMBER
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES student_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_group_student (group_id, student_id)
);

-- Communication Channels Table
CREATE TABLE communication_channels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT NOT NULL,
    channel_name VARCHAR(255) NOT NULL,
    description TEXT,
    channel_type VARCHAR(50) DEFAULT 'GENERAL', -- GENERAL, ANNOUNCEMENTS, Q_AND_A, ASSIGNMENTS
    is_private BOOLEAN DEFAULT FALSE,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Channel Messages Table
CREATE TABLE channel_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    channel_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message_text TEXT NOT NULL,
    message_type VARCHAR(50) DEFAULT 'TEXT', -- TEXT, FILE, IMAGE, LINK
    file_path VARCHAR(500),
    file_name VARCHAR(255),
    reply_to_id BIGINT, -- For threaded conversations
    edited_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (channel_id) REFERENCES communication_channels(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reply_to_id) REFERENCES channel_messages(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_course_materials_classroom ON course_materials(classroom_id);
CREATE INDEX idx_course_schedule_classroom_time ON course_schedule(classroom_id, start_time);
CREATE INDEX idx_student_progress_student_classroom ON student_progress(student_id, classroom_id);
CREATE INDEX idx_announcements_classroom_date ON announcements(classroom_id, created_at);
CREATE INDEX idx_timetable_events_datetime ON timetable_events(start_datetime, end_datetime);
CREATE INDEX idx_submissions_assignment_student ON submissions(assignment_id, student_id);
CREATE INDEX idx_attendance_session_user ON attendance(session_id, user_id);
