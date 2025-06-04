-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    role_id INT,
    enrollment_date DATE,
    hire_date DATE,
    department VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
    status VARCHAR(10) DEFAULT 'active'
);

-- Create uploads directory for file storage
-- Note: This is a comment as SQL cannot create directories, 
-- you'll need to create this directory manually
