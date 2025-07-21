-- =====================================================
-- ADD COLUMNS TO EXISTING CLASSROOM TABLE
-- =====================================================

USE SchoolManagementDB;
GO

PRINT 'Adding columns to classrooms table...';

-- Add code column
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'code')
BEGIN
    ALTER TABLE classrooms ADD code NVARCHAR(50) NULL;
    PRINT '✅ Added code column';
END
ELSE
BEGIN
    PRINT '⚠️ Code column already exists';
END

-- Add start_date column
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'start_date')
BEGIN
    ALTER TABLE classrooms ADD start_date DATE NULL;
    PRINT '✅ Added start_date column';
END
ELSE
BEGIN
    PRINT '⚠️ start_date column already exists';
END

-- Add end_date column
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'end_date')
BEGIN
    ALTER TABLE classrooms ADD end_date DATE NULL;
    PRINT '✅ Added end_date column';
END
ELSE
BEGIN
    PRINT '⚠️ end_date column already exists';
END

-- Add status column
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'status')
BEGIN
    ALTER TABLE classrooms ADD status NVARCHAR(20) NOT NULL DEFAULT 'DRAFT';
    PRINT '✅ Added status column';
END
ELSE
BEGIN
    PRINT '⚠️ status column already exists';
END

-- Add created_by column
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'created_by')
BEGIN
    ALTER TABLE classrooms ADD created_by BIGINT NULL;
    PRINT '✅ Added created_by column';
END
ELSE
BEGIN
    PRINT '⚠️ created_by column already exists';
END

-- Add created_at column
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'created_at')
BEGIN
    ALTER TABLE classrooms ADD created_at DATETIME2 DEFAULT GETDATE();
    PRINT '✅ Added created_at column';
END
ELSE
BEGIN
    PRINT '⚠️ created_at column already exists';
END

-- Add updated_at column
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'updated_at')
BEGIN
    ALTER TABLE classrooms ADD updated_at DATETIME2 DEFAULT GETDATE();
    PRINT '✅ Added updated_at column';
END
ELSE
BEGIN
    PRINT '⚠️ updated_at column already exists';
END

PRINT 'Columns addition completed!';
GO
