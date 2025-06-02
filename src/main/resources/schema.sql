-- Tạo bảng users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng classrooms
CREATE TABLE classrooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    section VARCHAR(50),
    subject VARCHAR(100),
    teacher_id BIGINT,
    location_lat DOUBLE,
    location_lon DOUBLE,
    allowed_radius DOUBLE DEFAULT 100.0, -- Bán kính cho phép điểm danh (mét)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

-- Tạo bảng classroom_enrollments (quan hệ nhiều-nhiều giữa classroom và users)
CREATE TABLE classroom_enrollments (
    classroom_id BIGINT,
    user_id BIGINT,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (classroom_id, user_id),
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Tạo bảng attendance_sessions
CREATE TABLE attendance_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    classroom_id BIGINT,
    teacher_id BIGINT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    session_type VARCHAR(20) NOT NULL, -- ONLINE/OFFLINE
    status VARCHAR(20) NOT NULL, -- SCHEDULED/ACTIVE/COMPLETED/CANCELLED
    auto_mark BOOLEAN DEFAULT false,
    auto_mark_teacher_attendance BOOLEAN DEFAULT true,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id),
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

-- Tạo bảng attendances
CREATE TABLE attendances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    classroom_id BIGINT,
    session_date TIMESTAMP NOT NULL,
    is_present BOOLEAN DEFAULT false,
    attendance_type VARCHAR(20), -- ONLINE/OFFLINE
    comment VARCHAR(500),
    latitude DOUBLE,
    longitude DOUBLE,
    ip_address VARCHAR(45),
    marked_by_id BIGINT,
    photo_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_teacher_record BOOLEAN DEFAULT false,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id),
    FOREIGN KEY (marked_by_id) REFERENCES users(id)
);

-- Tạo bảng allowed_ips (whitelist IP cho điểm danh)
CREATE TABLE allowed_ips (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_address VARCHAR(45) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng assignments
CREATE TABLE assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    due_date TIMESTAMP NOT NULL,
    points INTEGER,
    file_attachment_url VARCHAR(255),
    classroom_id BIGINT,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id)
);

-- Tạo bảng submissions
CREATE TABLE submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT,
    student_id BIGINT,
    comment VARCHAR(2000),
    file_submission_url VARCHAR(255),
    submitted_at TIMESTAMP,
    score INTEGER,
    feedback VARCHAR(500),
    graded_at TIMESTAMP,
    graded_by_id BIGINT,
    FOREIGN KEY (assignment_id) REFERENCES assignments(id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (graded_by_id) REFERENCES users(id)
); 