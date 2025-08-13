-- =====================================================
-- PARENT ROLE SYSTEM - DATABASE MIGRATION SCRIPT
-- =====================================================
-- Based on PARENT_ROLE_SPEC.md requirements
-- This script creates all necessary tables for parent functionality

-- 1. Add PARENT role to roles table
INSERT INTO roles (id, name) VALUES (7, 'PARENT')
ON DUPLICATE KEY UPDATE name = 'PARENT';

-- 2. Create parents table
CREATE TABLE IF NOT EXISTS parents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name NVARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    status ENUM('ACTIVE', 'INACTIVE', 'BLOCKED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_parent_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Indexes
    INDEX idx_parent_user_id (user_id),
    INDEX idx_parent_email (email),
    INDEX idx_parent_phone (phone),
    INDEX idx_parent_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Create student_parent relationship table
CREATE TABLE IF NOT EXISTS student_parent (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    parent_id BIGINT NOT NULL,
    relation_type ENUM('MOTHER', 'FATHER', 'GUARDIAN', 'OTHER') NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    legal_guardian BOOLEAN DEFAULT FALSE,
    start_at DATE DEFAULT (CURRENT_DATE),
    end_at DATE NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_student_parent_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_parent_parent FOREIGN KEY (parent_id) REFERENCES parents(id) ON DELETE CASCADE,
    
    -- Unique constraints
    UNIQUE KEY uk_student_parent_relation (student_id, parent_id),
    
    -- Indexes
    INDEX idx_sp_student_id (student_id),
    INDEX idx_sp_parent_id (parent_id),
    INDEX idx_sp_relation_type (relation_type),
    INDEX idx_sp_is_primary (is_primary),
    INDEX idx_sp_active_period (start_at, end_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Create parent_leave_notice table (core feature from spec)
CREATE TABLE IF NOT EXISTS parent_leave_notice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    type ENUM('FULL_DAY', 'LATE', 'EARLY') NOT NULL,
    date DATE NOT NULL,
    arrive_at TIME NULL, -- For LATE type
    leave_at TIME NULL,  -- For EARLY type
    reason_code ENUM('SICK', 'FAMILY', 'APPOINTMENT', 'EMERGENCY', 'OTHER') NOT NULL,
    note TEXT,
    attachments JSON, -- Store file paths/URLs as JSON array
    status ENUM('SENT', 'DELIVERED', 'ACKNOWLEDGED') DEFAULT 'SENT',
    ack_at TIMESTAMP NULL,
    ack_by_user_id BIGINT NULL, -- Teacher/staff who acknowledged
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_leave_notice_parent FOREIGN KEY (parent_id) REFERENCES parents(id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_notice_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_notice_ack_user FOREIGN KEY (ack_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    
    -- Business logic constraints
    CONSTRAINT chk_leave_notice_arrive_time CHECK (
        (type != 'LATE') OR (type = 'LATE' AND arrive_at IS NOT NULL)
    ),
    CONSTRAINT chk_leave_notice_leave_time CHECK (
        (type != 'EARLY') OR (type = 'EARLY' AND leave_at IS NOT NULL)
    ),
    CONSTRAINT chk_leave_notice_full_day CHECK (
        (type != 'FULL_DAY') OR (type = 'FULL_DAY' AND arrive_at IS NULL AND leave_at IS NULL)
    ),
    
    -- Prevent duplicate notices for same student-date-time period
    UNIQUE KEY uk_leave_notice_unique (student_id, date, type, arrive_at, leave_at),
    
    -- Indexes
    INDEX idx_leave_notice_parent_id (parent_id),
    INDEX idx_leave_notice_student_id (student_id),
    INDEX idx_leave_notice_date (date),
    INDEX idx_leave_notice_status (status),
    INDEX idx_leave_notice_type (type),
    INDEX idx_leave_notice_reason (reason_code),
    INDEX idx_leave_notice_ack (ack_by_user_id, ack_at),
    INDEX idx_leave_notice_student_date (student_id, date) -- For attendance integration
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Create parent_notification_prefs table
CREATE TABLE IF NOT EXISTS parent_notification_prefs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NOT NULL,
    channels JSON NOT NULL DEFAULT '{"inapp": true, "email": false, "sms": false}', -- Available channels
    quiet_hours JSON DEFAULT '{"from": "22:00", "to": "07:00"}', -- Quiet hours setting
    event_toggles JSON NOT NULL DEFAULT '{"leave_notice_ack": true, "new_grade": true, "assignment_due": true, "invoice_issued": false, "attendance_flagged": true}', -- Per-event toggles
    digest_frequency ENUM('NONE', 'DAILY', 'WEEKLY') DEFAULT 'DAILY',
    language_preference VARCHAR(10) DEFAULT 'vi', -- Language preference
    timezone VARCHAR(50) DEFAULT 'Asia/Ho_Chi_Minh',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_notification_prefs_parent FOREIGN KEY (parent_id) REFERENCES parents(id) ON DELETE CASCADE,
    
    -- Unique constraint
    UNIQUE KEY uk_notification_prefs_parent (parent_id),
    
    -- Indexes
    INDEX idx_notification_prefs_digest (digest_frequency),
    INDEX idx_notification_prefs_language (language_preference)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Create parent_messages table (for 1-1 communication with teachers)
CREATE TABLE IF NOT EXISTS parent_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL, -- Context student
    sender_type ENUM('PARENT', 'TEACHER') NOT NULL,
    subject NVARCHAR(255),
    message_content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    reply_to_id BIGINT NULL, -- For threading messages
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_parent_msg_parent FOREIGN KEY (parent_id) REFERENCES parents(id) ON DELETE CASCADE,
    CONSTRAINT fk_parent_msg_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_parent_msg_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_parent_msg_reply FOREIGN KEY (reply_to_id) REFERENCES parent_messages(id) ON DELETE SET NULL,
    
    -- Indexes
    INDEX idx_parent_msg_parent_id (parent_id),
    INDEX idx_parent_msg_teacher_id (teacher_id),
    INDEX idx_parent_msg_student_id (student_id),
    INDEX idx_parent_msg_conversation (parent_id, teacher_id, student_id),
    INDEX idx_parent_msg_read_status (is_read, read_at),
    INDEX idx_parent_msg_created (created_at),
    INDEX idx_parent_msg_reply_to (reply_to_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Create parent_billing_access table (optional for phase 2)
CREATE TABLE IF NOT EXISTS parent_billing_access (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    can_view_invoices BOOLEAN DEFAULT TRUE,
    can_view_payments BOOLEAN DEFAULT TRUE,
    can_make_payments BOOLEAN DEFAULT FALSE, -- For future online payment integration
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_billing_access_parent FOREIGN KEY (parent_id) REFERENCES parents(id) ON DELETE CASCADE,
    CONSTRAINT fk_billing_access_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Unique constraint
    UNIQUE KEY uk_billing_access_parent_student (parent_id, student_id),
    
    -- Indexes
    INDEX idx_billing_access_parent (parent_id),
    INDEX idx_billing_access_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Add sample data for testing (optional)
-- Insert sample parent user
INSERT IGNORE INTO users (id, username, email, password, full_name, phone, created_at) 
VALUES (1000, 'parent001', 'parent001@example.com', '$2a$10$sample.hash.here', 'Nguyễn Thị Hoa', '0912345678', NOW());

-- Insert parent record
INSERT IGNORE INTO parents (id, user_id, name, phone, email, status) 
VALUES (1, 1000, 'Nguyễn Thị Hoa', '0912345678', 'parent001@example.com', 'ACTIVE');

-- Link parent to user roles
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (1000, 7); -- PARENT role

-- Create parent-student relationship (assuming student with id=100 exists)
-- INSERT IGNORE INTO student_parent (student_id, parent_id, relation_type, is_primary, legal_guardian) 
-- VALUES (100, 1, 'MOTHER', TRUE, TRUE);

-- Insert default notification preferences
INSERT IGNORE INTO parent_notification_prefs (parent_id) VALUES (1);

-- =====================================================
-- MIGRATION COMPLETION LOG
-- =====================================================
-- This script creates:
-- 1. PARENT role in roles table
-- 2. parents table for parent profiles
-- 3. student_parent relationship table
-- 4. parent_leave_notice table (core feature)
-- 5. parent_notification_prefs table
-- 6. parent_messages table for communication
-- 7. parent_billing_access table (optional)
-- 8. Sample data for testing

-- Next steps after running this migration:
-- 1. Create corresponding JPA entities
-- 2. Create repositories and services
-- 3. Create API controllers
-- 4. Update JWT token service to include childIds
-- 5. Create frontend components

SELECT 'Parent role database migration completed successfully' as Status;