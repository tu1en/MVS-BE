-- SQL Script to Fix Database UTF-8 Encoding Issues
-- Run this script to fix Vietnamese character encoding problems

-- 1. Set database and table charset to UTF-8
ALTER DATABASE your_database_name CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. Fix users table charset
ALTER TABLE users CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3. Fix specific columns that contain Vietnamese text
ALTER TABLE users MODIFY COLUMN full_name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE users MODIFY COLUMN email VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 4. Fix contracts table charset and Vietnamese text columns
ALTER TABLE contracts CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE contracts MODIFY COLUMN full_name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE contracts MODIFY COLUMN email VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE contracts MODIFY COLUMN position VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE contracts MODIFY COLUMN address TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE contracts MODIFY COLUMN qualification VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE contracts MODIFY COLUMN subject VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE contracts MODIFY COLUMN contract_terms TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE contracts MODIFY COLUMN comments TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 5. Fix other tables if they exist (uncomment as needed)
-- ALTER TABLE employees CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- ALTER TABLE positions CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 5. Check current charset settings
SELECT 
    TABLE_SCHEMA,
    TABLE_NAME,
    COLUMN_NAME,
    CHARACTER_SET_NAME,
    COLLATION_NAME
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'your_database_name'
AND CHARACTER_SET_NAME IS NOT NULL
ORDER BY TABLE_NAME, COLUMN_NAME;

-- 6. Check for encoding issues in data
SELECT 
    user_id,
    full_name,
    email,
    CASE 
        WHEN full_name LIKE '%?%' OR full_name LIKE '%�%' THEN 'HAS_ENCODING_ISSUE'
        ELSE 'OK'
    END as encoding_status
FROM users 
WHERE full_name LIKE '%?%' OR full_name LIKE '%�%'
LIMIT 10;

-- 7. Sample data fix (manual approach)
-- UPDATE users SET full_name = 'Lý Thị Bình' WHERE full_name = 'Lý Th? Bình';
-- UPDATE users SET full_name = 'Giáo viên Văn học lớp 10' WHERE full_name LIKE '%Van h?c l?p 10%';

-- 8. Set connection charset for current session
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

-- 9. Show current connection charset
SHOW VARIABLES LIKE 'character_set%';
SHOW VARIABLES LIKE 'collation%';
