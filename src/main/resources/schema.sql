-- Create tables
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    role_id INT,
    enrollment_date DATE,
    hire_date DATE,
    department VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(10) DEFAULT 'active'
);

CREATE TABLE classrooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    section VARCHAR(50),
    subject VARCHAR(255),
    description VARCHAR(1024),
    teacher_id BIGINT,
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

CREATE TABLE classroom_enrollments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT,
    user_id BIGINT,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1024),
    due_date TIMESTAMP,
    points INT,
    classroom_id BIGINT,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id)
);

CREATE TABLE submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment VARCHAR(1024),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assignment_id BIGINT,
    student_id BIGINT,
    FOREIGN KEY (assignment_id) REFERENCES assignments(id),
    FOREIGN KEY (student_id) REFERENCES users(id)
);

CREATE TABLE accomplishments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_title VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    teacher_name VARCHAR(255),
    grade DOUBLE,
    completion_date DATE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
