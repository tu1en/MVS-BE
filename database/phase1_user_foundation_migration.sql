-- =====================================================
-- PHASE 1: USER FOUNDATION MIGRATION FOR SQL SERVER
-- Description: Fix existing User table and add Phase 1 requirements
-- Version: 1.0
-- Date: 2025-07-26
-- =====================================================

USE SchoolManagementDB;
GO

PRINT '🚀 Starting Phase 1: User Foundation Migration...';

-- =====================================================
-- 1. FIX EXISTING USER TABLE ISSUES
-- =====================================================

-- Check if users table exists
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'users')
BEGIN
    PRINT '✅ Users table found. Applying fixes...';
    
    -- Add role_enum column if it doesn't exist
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'role_enum')
    BEGIN
        ALTER TABLE users ADD role_enum NVARCHAR(20) NULL;
        PRINT '✅ Added role_enum column';
    END
    
    -- Add last_login column if it doesn't exist
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'last_login')
    BEGIN
        ALTER TABLE users ADD last_login DATETIME2 NULL;
        PRINT '✅ Added last_login column';
    END
    
    -- Add is_deleted column if it doesn't exist
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'is_deleted')
    BEGIN
        ALTER TABLE users ADD is_deleted BIT NOT NULL DEFAULT 0;
        PRINT '✅ Added is_deleted column';
    END
    
    -- Update role_enum based on existing role_id
    UPDATE users SET role_enum = 
        CASE role_id
            WHEN 1 THEN 'STUDENT'
            WHEN 2 THEN 'TEACHER'
            WHEN 3 THEN 'MANAGER'
            WHEN 4 THEN 'ADMIN'
            WHEN 5 THEN 'ACCOUNTANT'
            ELSE 'STUDENT'
        END
    WHERE role_enum IS NULL;
    PRINT '✅ Updated role_enum values based on role_id';
    
    -- Add check constraint for role_enum
    IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK_User_RoleEnum')
    BEGIN
        ALTER TABLE users ADD CONSTRAINT CK_User_RoleEnum 
        CHECK (role_enum IN ('STUDENT', 'TEACHER', 'MANAGER', 'ADMIN', 'ACCOUNTANT'));
        PRINT '✅ Added role_enum constraint';
    END
    
    -- Add check constraint for status
    IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK_User_Status')
    BEGIN
        ALTER TABLE users ADD CONSTRAINT CK_User_Status 
        CHECK (status IN ('active', 'inactive', 'suspended', 'pending'));
        PRINT '✅ Added status constraint';
    END
    
    -- Ensure UTF-8 support for text columns
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'full_name' AND collation_name != 'SQL_Latin1_General_CP1_CI_AS')
    BEGIN
        ALTER TABLE users ALTER COLUMN full_name NVARCHAR(255);
        PRINT '✅ Updated full_name to NVARCHAR for UTF-8 support';
    END
    
    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'department' AND collation_name != 'SQL_Latin1_General_CP1_CI_AS')
    BEGIN
        ALTER TABLE users ALTER COLUMN department NVARCHAR(100);
        PRINT '✅ Updated department to NVARCHAR for UTF-8 support';
    END
    
    -- Create indexes for performance
    IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_User_RoleEnum')
    BEGIN
        CREATE INDEX IX_User_RoleEnum ON users(role_enum);
        PRINT '✅ Created index on role_enum';
    END
    
    IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_User_IsDeleted')
    BEGIN
        CREATE INDEX IX_User_IsDeleted ON users(is_deleted);
        PRINT '✅ Created index on is_deleted';
    END
    
    IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_User_Status')
    BEGIN
        CREATE INDEX IX_User_Status ON users(status);
        PRINT '✅ Created index on status';
    END
    
    IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_User_LastLogin')
    BEGIN
        CREATE INDEX IX_User_LastLogin ON users(last_login);
        PRINT '✅ Created index on last_login';
    END
END
ELSE
BEGIN
    PRINT '❌ Users table not found. Creating new users table...';
    
    -- Create users table from scratch
    CREATE TABLE users (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(50) NOT NULL UNIQUE,
        password NVARCHAR(255) NOT NULL,
        email NVARCHAR(255) NOT NULL UNIQUE,
        full_name NVARCHAR(255),
        phone_number NVARCHAR(20),
        role_id INT,
        role_enum NVARCHAR(20) NOT NULL DEFAULT 'STUDENT',
        hire_date DATE,
        department NVARCHAR(100),
        status NVARCHAR(32) NOT NULL DEFAULT 'active',
        annual_leave_balance INT DEFAULT 12,
        leave_reset_date DATE,
        last_login DATETIME2 NULL,
        is_deleted BIT NOT NULL DEFAULT 0,
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        
        -- Constraints
        CONSTRAINT CK_User_RoleEnum CHECK (role_enum IN ('STUDENT', 'TEACHER', 'MANAGER', 'ADMIN', 'ACCOUNTANT')),
        CONSTRAINT CK_User_Status CHECK (status IN ('active', 'inactive', 'suspended', 'pending'))
    );
    
    -- Create indexes
    CREATE INDEX IX_User_RoleEnum ON users(role_enum);
    CREATE INDEX IX_User_IsDeleted ON users(is_deleted);
    CREATE INDEX IX_User_Status ON users(status);
    CREATE INDEX IX_User_LastLogin ON users(last_login);
    
    PRINT '✅ Created new users table with all Phase 1 requirements';
END

-- =====================================================
-- 2. CREATE USER_PROFILES TABLE (Enhanced profile data)
-- =====================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'user_profiles')
BEGIN
    CREATE TABLE user_profiles (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        avatar_url NVARCHAR(1000),
        bio NVARCHAR(MAX),
        date_of_birth DATE,
        address NVARCHAR(500),
        city NVARCHAR(100),
        country NVARCHAR(100) DEFAULT N'Vietnam',
        timezone NVARCHAR(50) DEFAULT 'Asia/Ho_Chi_Minh',
        language_preference NVARCHAR(10) DEFAULT 'vi',
        notification_preferences NVARCHAR(MAX), -- JSON format
        social_links NVARCHAR(MAX), -- JSON format  
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        
        -- Foreign key constraint
        CONSTRAINT FK_UserProfile_User FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
        
        -- Unique constraint
        CONSTRAINT UQ_UserProfile_UserId UNIQUE(user_id)
    );
    
    -- Create indexes
    CREATE INDEX IX_UserProfile_UserId ON user_profiles(user_id);
    CREATE INDEX IX_UserProfile_City ON user_profiles(city);
    
    PRINT '✅ Created user_profiles table';
END
ELSE
BEGIN
    PRINT '⚠️ User_profiles table already exists';
END

-- =====================================================
-- 3. CREATE USER_PERMISSIONS TABLE (Role-based permissions)
-- =====================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'user_permissions')
BEGIN
    CREATE TABLE user_permissions (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        permission_name NVARCHAR(100) NOT NULL,
        resource_type NVARCHAR(50), -- e.g., 'COURSE', 'ASSIGNMENT', 'USER'
        resource_id BIGINT, -- specific resource ID (optional)
        granted_by BIGINT NOT NULL,
        granted_at DATETIME2 DEFAULT GETDATE(),
        expires_at DATETIME2 NULL,
        is_active BIT NOT NULL DEFAULT 1,
        
        -- Foreign key constraints
        CONSTRAINT FK_UserPermission_User FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
        CONSTRAINT FK_UserPermission_GrantedBy FOREIGN KEY (granted_by) REFERENCES users(id)
    );
    
    -- Create indexes
    CREATE INDEX IX_UserPermission_UserId ON user_permissions(user_id);
    CREATE INDEX IX_UserPermission_PermissionName ON user_permissions(permission_name);
    CREATE INDEX IX_UserPermission_ResourceType ON user_permissions(resource_type);
    CREATE INDEX IX_UserPermission_IsActive ON user_permissions(is_active);
    
    PRINT '✅ Created user_permissions table';
END
ELSE
BEGIN
    PRINT '⚠️ User_permissions table already exists';
END

-- =====================================================
-- 4. CREATE USER_SESSIONS TABLE (Authentication tracking)
-- =====================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'user_sessions')
BEGIN
    CREATE TABLE user_sessions (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        session_token NVARCHAR(500) NOT NULL UNIQUE,
        refresh_token NVARCHAR(500),
        device_info NVARCHAR(500),
        ip_address NVARCHAR(45),
        user_agent NVARCHAR(1000),
        login_at DATETIME2 DEFAULT GETDATE(),
        last_activity DATETIME2 DEFAULT GETDATE(),
        expires_at DATETIME2 NOT NULL,
        is_active BIT NOT NULL DEFAULT 1,
        logout_at DATETIME2 NULL,
        logout_reason NVARCHAR(100), -- 'USER_LOGOUT', 'SESSION_EXPIRED', 'ADMIN_LOGOUT'
        
        -- Foreign key constraint
        CONSTRAINT FK_UserSession_User FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );
    
    -- Create indexes
    CREATE INDEX IX_UserSession_UserId ON user_sessions(user_id);
    CREATE INDEX IX_UserSession_SessionToken ON user_sessions(session_token);
    CREATE INDEX IX_UserSession_IsActive ON user_sessions(is_active);
    CREATE INDEX IX_UserSession_ExpiresAt ON user_sessions(expires_at);
    CREATE INDEX IX_UserSession_LastActivity ON user_sessions(last_activity);
    
    PRINT '✅ Created user_sessions table';
END
ELSE
BEGIN
    PRINT '⚠️ User_sessions table already exists';
END

-- =====================================================
-- 5. INSERT DEFAULT ADMIN USER (if not exists)
-- =====================================================

IF NOT EXISTS (SELECT * FROM users WHERE username = 'admin' OR email = 'admin@school.edu.vn')
BEGIN
    INSERT INTO users (
        username, 
        email, 
        password, 
        full_name, 
        role_id, 
        role_enum, 
        status,
        department,
        hire_date,
        annual_leave_balance
    ) VALUES (
        'admin',
        'admin@school.edu.vn',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', -- password: 'password'
        N'Quản trị viên hệ thống',
        4,
        'ADMIN',
        'active',
        N'Quản trị',
        GETDATE(),
        0
    );
    
    PRINT '✅ Created default admin user (username: admin, password: password)';
END
ELSE
BEGIN
    PRINT '⚠️ Admin user already exists';
END

-- =====================================================
-- 6. CREATE DEFAULT MANAGER USER (if not exists)
-- =====================================================

IF NOT EXISTS (SELECT * FROM users WHERE username = 'manager' OR email = 'manager@school.edu.vn')
BEGIN
    INSERT INTO users (
        username, 
        email, 
        password, 
        full_name, 
        role_id, 
        role_enum, 
        status,
        department,
        hire_date,
        annual_leave_balance
    ) VALUES (
        'manager',
        'manager@school.edu.vn',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', -- password: 'password'
        N'Trưởng phòng Giáo vụ',
        3,
        'MANAGER',
        'active',
        N'Giáo vụ',
        GETDATE(),
        12
    );
    
    PRINT '✅ Created default manager user (username: manager, password: password)';
END
ELSE
BEGIN
    PRINT '⚠️ Manager user already exists';
END

-- =====================================================
-- 7. CREATE SAMPLE TEACHER USER (if not exists)
-- =====================================================

IF NOT EXISTS (SELECT * FROM users WHERE username = 'teacher1' OR email = 'teacher1@school.edu.vn')
BEGIN
    INSERT INTO users (
        username, 
        email, 
        password, 
        full_name, 
        role_id, 
        role_enum, 
        status,
        department,
        hire_date,
        annual_leave_balance
    ) VALUES (
        'teacher1',
        'teacher1@school.edu.vn',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', -- password: 'password'
        N'Giáo viên Nguyễn Văn An',
        2,
        'TEACHER',
        'active',
        N'Toán học',
        GETDATE(),
        12
    );
    
    PRINT '✅ Created sample teacher user (username: teacher1, password: password)';
END
ELSE
BEGIN
    PRINT '⚠️ Teacher1 user already exists';
END

-- =====================================================
-- 8. CREATE SAMPLE STUDENT USER (if not exists)
-- =====================================================

IF NOT EXISTS (SELECT * FROM users WHERE username = 'student1' OR email = 'student1@school.edu.vn')
BEGIN
    INSERT INTO users (
        username, 
        email, 
        password, 
        full_name, 
        role_id, 
        role_enum, 
        status,
        department,
        hire_date,
        annual_leave_balance
    ) VALUES (
        'student1',
        'student1@school.edu.vn',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', -- password: 'password'
        N'Học sinh Trần Thị Bình',
        1,
        'STUDENT',
        'active',
        N'Học sinh',
        NULL,
        0
    );
    
    PRINT '✅ Created sample student user (username: student1, password: password)';
END
ELSE
BEGIN
    PRINT '⚠️ Student1 user already exists';
END

-- =====================================================
-- 9. VERIFICATION & CLEANUP
-- =====================================================

-- Update all existing users to have role_enum if null
UPDATE users SET role_enum = 
    CASE role_id
        WHEN 1 THEN 'STUDENT'
        WHEN 2 THEN 'TEACHER'
        WHEN 3 THEN 'MANAGER'
        WHEN 4 THEN 'ADMIN'
        WHEN 5 THEN 'ACCOUNTANT'
        ELSE 'STUDENT'
    END
WHERE role_enum IS NULL;

-- Update timestamps for existing users without them
UPDATE users SET created_at = GETDATE() WHERE created_at IS NULL;
UPDATE users SET updated_at = GETDATE() WHERE updated_at IS NULL;

PRINT '✅ Updated existing users with missing role_enum and timestamps';

-- =====================================================
-- 10. FINAL VERIFICATION
-- =====================================================

DECLARE @userCount INT, @adminCount INT, @managerCount INT, @teacherCount INT, @studentCount INT;

SELECT @userCount = COUNT(*) FROM users WHERE is_deleted = 0;
SELECT @adminCount = COUNT(*) FROM users WHERE role_enum = 'ADMIN' AND is_deleted = 0;
SELECT @managerCount = COUNT(*) FROM users WHERE role_enum = 'MANAGER' AND is_deleted = 0;
SELECT @teacherCount = COUNT(*) FROM users WHERE role_enum = 'TEACHER' AND is_deleted = 0;
SELECT @studentCount = COUNT(*) FROM users WHERE role_enum = 'STUDENT' AND is_deleted = 0;

PRINT '📊 PHASE 1 MIGRATION SUMMARY:';
PRINT '   Total users: ' + CAST(@userCount AS NVARCHAR(10));
PRINT '   Admins: ' + CAST(@adminCount AS NVARCHAR(10));
PRINT '   Managers: ' + CAST(@managerCount AS NVARCHAR(10));
PRINT '   Teachers: ' + CAST(@teacherCount AS NVARCHAR(10));
PRINT '   Students: ' + CAST(@studentCount AS NVARCHAR(10));

PRINT '🎉 Phase 1: User Foundation Migration completed successfully!';
PRINT '';
PRINT '🔑 DEFAULT TEST ACCOUNTS:';
PRINT '   Admin: username=admin, password=password';
PRINT '   Manager: username=manager, password=password';
PRINT '   Teacher: username=teacher1, password=password';
PRINT '   Student: username=student1, password=password';

GO