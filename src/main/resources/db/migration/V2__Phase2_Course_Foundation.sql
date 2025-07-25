-- Phase 2: Course Foundation - Database Schema
-- =============================================

-- 1. SYLLABUSES TABLE
-- Khung môn học (template cho các khóa học)
CREATE TABLE syllabuses (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(200) NOT NULL COMMENT N'Tên syllabus: VD "Toán 11"',
    code NVARCHAR(50) NOT NULL UNIQUE COMMENT N'Mã: VD "MATH11"',
    description NVARCHAR(MAX) COMMENT N'Mô tả chi tiết',
    subject NVARCHAR(100) COMMENT N'Môn học: Toán, Lý, Hóa...',
    grade_level NVARCHAR(50) COMMENT N'Cấp độ: Lớp 11, THPT...',
    created_by BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED')),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    is_deleted BIT DEFAULT 0,
    
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE NO ACTION,
    INDEX idx_syllabuses_subject (subject),
    INDEX idx_syllabuses_grade_level (grade_level),
    INDEX idx_syllabuses_status (status),
    INDEX idx_syllabuses_created_by (created_by)
);

-- 2. COURSES TABLE  
-- Khóa học cụ thể tạo từ syllabus
CREATE TABLE courses (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    syllabus_id BIGINT NOT NULL COMMENT 'Link tới syllabus template',
    name NVARCHAR(200) NOT NULL COMMENT N'Tên khóa: VD "Toán 11 - Khóa tháng 8"',
    code NVARCHAR(50) NOT NULL UNIQUE COMMENT N'Mã khóa: VD "MATH11_AUG2024"',
    description NVARCHAR(MAX),
    start_date DATE COMMENT N'Ngày bắt đầu khóa học',
    end_date DATE COMMENT N'Ngày kết thúc',
    max_students INT DEFAULT 30 COMMENT N'Số học sinh tối đa',
    price DECIMAL(12,2) COMMENT N'Học phí (VNĐ)',
    created_by BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    is_deleted BIT DEFAULT 0,
    
    FOREIGN KEY (syllabus_id) REFERENCES syllabuses(id) ON DELETE NO ACTION,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE NO ACTION,
    INDEX idx_courses_syllabus_id (syllabus_id),
    INDEX idx_courses_start_date (start_date),
    INDEX idx_courses_status (status),
    INDEX idx_courses_created_by (created_by),
    INDEX idx_courses_price (price),
    
    -- Validate business rule
    CONSTRAINT chk_course_dates CHECK (start_date <= end_date),
    CONSTRAINT chk_max_students CHECK (max_students > 0)
);

-- 3. COURSE_TEACHERS TABLE
-- Phân công giảng viên cho khóa học
CREATE TABLE course_teachers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    course_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    role VARCHAR(20) DEFAULT 'MAIN_INSTRUCTOR' CHECK (role IN ('MAIN_INSTRUCTOR', 'ASSISTANT')),
    assigned_at DATETIME2 DEFAULT GETDATE(),
    accepted_at DATETIME2 NULL COMMENT N'Khi teacher accept assignment',
    removed_at DATETIME2 NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'REMOVED')),
    is_active BIT DEFAULT 1,
    notes NVARCHAR(MAX) COMMENT N'Ghi chú từ Manager',
    
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE NO ACTION,
    UNIQUE (course_id, teacher_id),
    INDEX idx_course_teachers_teacher_id (teacher_id),
    INDEX idx_course_teachers_status (status),
    INDEX idx_course_teachers_is_active (is_active),
    INDEX idx_course_teachers_assignment (course_id, teacher_id, status)
);

-- UPDATE EXISTING USERS TABLE - Ensure has MANAGER role
IF NOT EXISTS (SELECT 1 FROM syscolumns WHERE id = OBJECT_ID('users') AND name = 'role_enum')
    ALTER TABLE users ADD role_enum VARCHAR(20) DEFAULT 'STUDENT';

-- Insert default MANAGER users if not exists
IF NOT EXISTS (SELECT 1 FROM users WHERE role_enum = 'MANAGER')
BEGIN
    INSERT INTO users (username, password, email, role_enum, full_name, created_at, updated_at)
    VALUES 
    ('manager1', '$2a$10$8.UnVuG9HHgfsUDXF8o9hepKqv48sjWxzKHQK/NGNnc8ZLyO7vFES', 'manager1@classroom.com', 'MANAGER', N'Manager 1', GETDATE(), GETDATE()),
    ('manager2', '$2a$10$8.UnVuG9HHgfsUDXF8o9hepKqv48sjWxzKHQK/NGNnc8ZLyO7vFES', 'manager2@classroom.com', 'MANAGER', N'Manager 2', GETDATE(), GETDATE());
END

-- Insert default TEACHER users if not exists
IF NOT EXISTS (SELECT 1 FROM users WHERE role_enum = 'TEACHER')
BEGIN
    INSERT INTO users (username, password, email, role_enum, full_name, created_at, updated_at)
    VALUES 
    ('teacher1', '$2a$10$8.UnVuG9HHgfsUDXF8o9hepKqv48sjWxzKHQK/NGNnc8ZLyO7vFES', 'teacher1@classroom.com', 'TEACHER', N'Teacher 1', GETDATE(), GETDATE()),
    ('teacher2', '$2a$10$8.UnVuG9HHgfsUDXF8o9hepKqv48sjWxzKHQK/NGNnc8ZLyO7vFES', 'teacher2@classroom.com', 'TEACHER', N'Teacher 2', GETDATE(), GETDATE()),
    ('teacher3', '$2a$10$8.UnVuG9HHgfsUDXF8o9hepKqv48sjWxzKHQK/NGNnc8ZLyO7vFES', 'teacher3@classroom.com', 'TEACHER', N'Teacher 3', GETDATE(), GETDATE());
END

-- Sample data for testing Phase 2 workflow
-- Create sample syllabuses
IF NOT EXISTS (SELECT 1 FROM syllabuses WHERE code = 'MATH11')
BEGIN
    INSERT INTO syllabuses (name, code, description, subject, grade_level, created_by, status)
    VALUES 
    (N'Toán 11', 'MATH11', N'Chương trình Toán học lớp 11 cơ bản và nâng cao', N'Toán học', N'Lớp 11', 1, 'ACTIVE'),
    (N'Vật Lý 12', 'PHYS12', N'Chương trình Vật lý lớp 12 chuẩn và nâng cao', N'Vật lý', N'Lớp 12', 1, 'ACTIVE'),
    (N'Hóa học 11', 'CHEM11', N'Chương trình Hóa học lớp 11', N'Hóa học', N'Lớp 11', 1, 'DRAFT');
END

-- Create sample courses
IF NOT EXISTS (SELECT 1 FROM courses WHERE code = 'MATH11_AUG2024')
BEGIN
    INSERT INTO courses (syllabus_id, name, code, description, start_date, end_date, max_students, price, created_by, status)
    VALUES 
    (1, N'Toán 11 - Khóa tháng 8/2024', 'MATH11_AUG2024', N'Khóa học Toán 11 tháng 8 năm 2024', '2024-08-01', '2024-12-31', 30, 2500000.00, 1, 'ACTIVE'),
    (2, N'Vật Lý 12 - Khóa tháng 9/2024', 'PHYS12_SEPT2024', N'Khóa học Vật lý 12 tháng 9', '2024-09-01', '2024-12-31', 25, 3000000.00, 1, 'ACTIVE'),
    (1, N'Toán 11 - Khóa tháng 9/2024', 'MATH11_SEPT2024', N'Toán 11 tháng 9 cho học sinh nâng cao', '2024-09-15', '2025-01-15', 20, 3000000.00, 1, 'DRAFT');
END