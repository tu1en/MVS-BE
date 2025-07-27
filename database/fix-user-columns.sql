-- Fix User table columns - Add missing columns with proper DEFAULT values
-- File: fix-user-columns.sql
-- Description: Add account_locked and is_deleted columns to users table

USE [classroom_management];

-- Add account_locked column with default value FALSE
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[users]') AND name = 'account_locked')
BEGIN
    ALTER TABLE [dbo].[users] 
    ADD [account_locked] BIT NOT NULL DEFAULT 0;
    PRINT 'Added account_locked column with default value FALSE';
END
ELSE
BEGIN
    PRINT 'Column account_locked already exists';
END

-- Add is_deleted column with default value FALSE  
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[users]') AND name = 'is_deleted')
BEGIN
    ALTER TABLE [dbo].[users] 
    ADD [is_deleted] BIT NOT NULL DEFAULT 0;
    PRINT 'Added is_deleted column with default value FALSE';
END
ELSE
BEGIN
    PRINT 'Column is_deleted already exists';
END

-- Verify the columns were added
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'users' 
AND COLUMN_NAME IN ('account_locked', 'is_deleted')
ORDER BY COLUMN_NAME;

PRINT 'Migration completed successfully!';