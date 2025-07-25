-- =============================================
-- Phase 1 Database Migration - User Foundation
-- UTF-8 Vietnamese Support & Enhanced Schema
-- =============================================

-- Ensure database uses UTF-8
ALTER DATABASE learning_management_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Step 1: Backup existing users
CREATE TABLE IF NOT EXISTS users_backup AS SELECT * FROM users;

-- Step 2: Add new columns to users table
ALTER TABLE users 
    ADD COLUMN IF NOT EXISTS role_enum ENUM('MANAGER', 'TEACHER', 'STUDENT', 'ADMIN') AFTER role_id,
    ADD COLUMN IF NOT EXISTS last_login TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS failed_login_attempts INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS account_locked BOOLEAN NOT NULL DEFAULT FALSE;

-- Step 3: Convert existing fields to UTF-8
ALTER TABLE users CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE users 
    MODIFY COLUMN full_name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    MODIFY COLUMN username VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    MODIFY COLUMN email VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    MODIFY COLUMN phone_number VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    MODIFY COLUMN department VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Step 4: Migrate role mapping
UPDATE users SET role_enum = 
CASE role_id
    WHEN 1 THEN 'ADMIN'
    WHEN 2 THEN 'MANAGER' 
    WHEN 3 THEN 'TEACHER'
    WHEN 4 THEN 'STUDENT'
    WHEN 5 THEN 'ACCOUNTANT'  -- Keep for backward compatibility
    ELSE 'STUDENT'
END;

-- Step 5: Add performance indexes
CREATE INDEX IF NOT EXISTS idx_users_role_enum ON users(role_enum);
CREATE INDEX IF NOT EXISTS idx_users_is_deleted ON users(is_deleted);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_email_unique ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username_unique ON users(username);

-- Step 6: Create user_profiles table (for future expansion)
CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    avatar_url VARCHAR(500),
    bio TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    preferences JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_profiles_user_id (user_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Step 7: Sync existing user profile data
INSERT IGNORE INTO user_profiles (user_id, bio)
SELECT id, CONCAT('Thành viên ', full_name) 
FROM users 
WHERE full_name IS NOT NULL AND full_name != '';

-- Step 8: Verify the migration
SELECT COUNT(*) as total_users, 
       COUNT(role_enum) as role_mapped_users,
       (SELECT COUNT(*) FROM user_profiles) as profile_users
FROM users;