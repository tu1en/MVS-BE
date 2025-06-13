-- Migration script to add Student Communication features to existing database
-- Run this script to add the three new tables for student communication features

-- 1. Create student_questions table for Student-Teacher Q&A feature
CREATE TABLE student_questions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    subject NVARCHAR(255) NOT NULL,
    content NTEXT NOT NULL,
    priority NVARCHAR(50) DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, URGENT
    status NVARCHAR(50) DEFAULT 'PENDING', -- PENDING, ANSWERED, CLOSED
    answer NTEXT,
    answered_at DATETIME2,
    answered_by BIGINT,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2,
    CONSTRAINT FK_student_questions_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT FK_student_questions_teacher FOREIGN KEY (teacher_id) REFERENCES users(id),
    CONSTRAINT FK_student_questions_answered_by FOREIGN KEY (answered_by) REFERENCES users(id)
);

-- 2. Create course_feedback table for Course Feedback feature
CREATE TABLE course_feedback (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id BIGINT NOT NULL,
    classroom_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    title NVARCHAR(255) NOT NULL,
    content NTEXT NOT NULL,
    overall_rating INT NOT NULL, -- 1-5 scale
    teaching_quality INT, -- 1-5 scale
    course_content INT, -- 1-5 scale
    organization INT, -- 1-5 scale
    communication INT, -- 1-5 scale
    feedback_category NVARCHAR(100), -- TEACHING, CONTENT, ORGANIZATION, COMMUNICATION, OTHER
    is_anonymous BIT DEFAULT 0,
    status NVARCHAR(50) DEFAULT 'SUBMITTED', -- SUBMITTED, REVIEWED, ACKNOWLEDGED
    teacher_response NTEXT,
    response_date DATETIME2,
    responded_by BIGINT,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2,
    CONSTRAINT FK_course_feedback_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT FK_course_feedback_classroom FOREIGN KEY (classroom_id) REFERENCES classrooms(id),
    CONSTRAINT FK_course_feedback_teacher FOREIGN KEY (teacher_id) REFERENCES users(id),
    CONSTRAINT FK_course_feedback_responded_by FOREIGN KEY (responded_by) REFERENCES users(id),
    CONSTRAINT CHK_overall_rating CHECK (overall_rating >= 1 AND overall_rating <= 5),
    CONSTRAINT CHK_teaching_quality CHECK (teaching_quality IS NULL OR (teaching_quality >= 1 AND teaching_quality <= 5)),
    CONSTRAINT CHK_course_content CHECK (course_content IS NULL OR (course_content >= 1 AND course_content <= 5)),
    CONSTRAINT CHK_organization CHECK (organization IS NULL OR (organization >= 1 AND organization <= 5)),
    CONSTRAINT CHK_communication CHECK (communication IS NULL OR (communication >= 1 AND communication <= 5))
);

-- 3. Create student_messages table for Student-Manager messaging feature
CREATE TABLE student_messages (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    subject NVARCHAR(255) NOT NULL,
    content NTEXT NOT NULL,
    message_type NVARCHAR(50) DEFAULT 'GENERAL', -- GENERAL, COMPLAINT, REQUEST, INQUIRY, URGENT
    priority NVARCHAR(50) DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, URGENT
    status NVARCHAR(50) DEFAULT 'SENT', -- SENT, READ, REPLIED, RESOLVED, ARCHIVED
    is_read BIT DEFAULT 0,
    read_at DATETIME2,
    parent_message_id BIGINT, -- For reply threading
    attachments NTEXT, -- JSON array of attachment info
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2,
    CONSTRAINT FK_student_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id),
    CONSTRAINT FK_student_messages_recipient FOREIGN KEY (recipient_id) REFERENCES users(id),
    CONSTRAINT FK_student_messages_parent FOREIGN KEY (parent_message_id) REFERENCES student_messages(id)
);

-- Create indexes for better performance
CREATE INDEX IX_student_questions_student_id ON student_questions(student_id);
CREATE INDEX IX_student_questions_teacher_id ON student_questions(teacher_id);
CREATE INDEX IX_student_questions_status ON student_questions(status);
CREATE INDEX IX_student_questions_priority ON student_questions(priority);
CREATE INDEX IX_student_questions_created_at ON student_questions(created_at);

CREATE INDEX IX_course_feedback_student_id ON course_feedback(student_id);
CREATE INDEX IX_course_feedback_classroom_id ON course_feedback(classroom_id);
CREATE INDEX IX_course_feedback_teacher_id ON course_feedback(teacher_id);
CREATE INDEX IX_course_feedback_status ON course_feedback(status);
CREATE INDEX IX_course_feedback_rating ON course_feedback(overall_rating);
CREATE INDEX IX_course_feedback_created_at ON course_feedback(created_at);

CREATE INDEX IX_student_messages_sender_id ON student_messages(sender_id);
CREATE INDEX IX_student_messages_recipient_id ON student_messages(recipient_id);
CREATE INDEX IX_student_messages_status ON student_messages(status);
CREATE INDEX IX_student_messages_priority ON student_messages(priority);
CREATE INDEX IX_student_messages_read ON student_messages(is_read);
CREATE INDEX IX_student_messages_created_at ON student_messages(created_at);
CREATE INDEX IX_student_messages_parent_id ON student_messages(parent_message_id);

-- Add comments to tables for documentation
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Table for storing student questions sent to teachers with Q&A functionality', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'student_questions';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Table for storing course feedback and ratings submitted by students', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'course_feedback';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Table for storing messages sent between students and managers/administrators', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'student_messages';

PRINT 'Student Communication features database migration completed successfully!';
PRINT 'Added tables: student_questions, course_feedback, student_messages';
PRINT 'Created indexes for optimal query performance';
