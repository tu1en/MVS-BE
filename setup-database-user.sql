-- SQL Script to create app_user for the classroom application
-- Run this script as SA or with administrative privileges

-- 1. Create the database if it doesn't exist
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'SchoolManagementDB')
BEGIN
    CREATE DATABASE [SchoolManagementDB];
    PRINT 'Database SchoolManagementDB created successfully';
END
ELSE
    PRINT 'Database SchoolManagementDB already exists';

-- 2. Use the database
USE [SchoolManagementDB];

-- 3. Create the login at server level (if it doesn't exist)
IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'app_user')
BEGIN
    CREATE LOGIN [app_user] WITH PASSWORD = '123456';
    PRINT 'Login app_user created successfully';
END
ELSE
    PRINT 'Login app_user already exists';

-- 4. Create the database user (if it doesn't exist)
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'app_user')
BEGIN
    CREATE USER [app_user] FOR LOGIN [app_user];
    PRINT 'Database user app_user created successfully';
END
ELSE
    PRINT 'Database user app_user already exists';

-- 5. Grant necessary permissions
ALTER ROLE [db_owner] ADD MEMBER [app_user];
PRINT 'Permissions granted to app_user';

-- 6. Test the connection
SELECT 'Database setup completed successfully for app_user' AS Status;
