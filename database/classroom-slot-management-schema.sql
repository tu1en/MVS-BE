-- =====================================================
-- CLASSROOM & SLOT MANAGEMENT DATABASE SCHEMA
-- SQL Server Compatible Schema for SEP490 HR System
-- Created: 2025-07-20
-- =====================================================

USE SchoolManagementDB;
GO

-- =====================================================
-- 1. EXTEND EXISTING CLASSROOM TABLE
-- =====================================================

-- Add new columns to existing classrooms table for status workflow
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'code')
BEGIN
    ALTER TABLE classrooms ADD code NVARCHAR(50) NULL;
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'start_date')
BEGIN
    ALTER TABLE classrooms ADD start_date DATE NULL;
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'end_date')
BEGIN
    ALTER TABLE classrooms ADD end_date DATE NULL;
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'status')
BEGIN
    ALTER TABLE classrooms ADD status NVARCHAR(20) NOT NULL DEFAULT 'DRAFT';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'created_by')
BEGIN
    ALTER TABLE classrooms ADD created_by BIGINT NULL;
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'created_at')
BEGIN
    ALTER TABLE classrooms ADD created_at DATETIME2 DEFAULT GETDATE();
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'updated_at')
BEGIN
    ALTER TABLE classrooms ADD updated_at DATETIME2 DEFAULT GETDATE();
END

-- Add constraints for classroom (only if columns exist)
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'status')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK_Classroom_Status')
    BEGIN
        ALTER TABLE classrooms ADD CONSTRAINT CK_Classroom_Status
        CHECK (status IN ('DRAFT', 'ACTIVE', 'COMPLETED', 'ARCHIVED'));
        PRINT '✅ Added status constraint to classrooms table';
    END
END

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'start_date')
   AND EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'end_date')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK_Classroom_DateRange')
    BEGIN
        ALTER TABLE classrooms ADD CONSTRAINT CK_Classroom_DateRange
        CHECK (start_date IS NULL OR end_date IS NULL OR start_date <= end_date);
        PRINT '✅ Added date range constraint to classrooms table';
    END
END

-- Add unique constraint for classroom code (only if column exists)
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'code')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'UQ_Classroom_Code')
    BEGIN
        CREATE UNIQUE INDEX UQ_Classroom_Code ON classrooms(code) WHERE code IS NOT NULL;
        PRINT '✅ Added unique index for classroom code';
    END
END

-- Add foreign key for created_by (only if column exists)
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('classrooms') AND name = 'created_by')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_Classroom_CreatedBy')
    BEGIN
        ALTER TABLE classrooms ADD CONSTRAINT FK_Classroom_CreatedBy
        FOREIGN KEY (created_by) REFERENCES users(id);
        PRINT '✅ Added foreign key constraint for created_by';
    END
END

-- =====================================================
-- 2. CREATE SESSION TABLE
-- =====================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'sessions')
BEGIN
    CREATE TABLE sessions (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        classroom_id BIGINT NOT NULL,
        session_date DATE NOT NULL,
        description NVARCHAR(MAX),
        status NVARCHAR(20) NOT NULL DEFAULT 'UPCOMING',
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        
        -- Foreign key constraint
        CONSTRAINT FK_Session_Classroom FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
        
        -- Check constraint for status
        CONSTRAINT CK_Session_Status CHECK (status IN ('UPCOMING', 'IN_PROGRESS', 'COMPLETED')),
        
        -- Unique constraint for classroom_id + session_date
        CONSTRAINT UQ_Session_ClassroomDate UNIQUE(classroom_id, session_date)
    );
    
    -- Create indexes for performance
    CREATE INDEX IX_Session_ClassroomId ON sessions(classroom_id);
    CREATE INDEX IX_Session_Date ON sessions(session_date);
    CREATE INDEX IX_Session_Status ON sessions(status);
    
    PRINT '✅ Created sessions table with constraints and indexes';
END
ELSE
BEGIN
    PRINT '⚠️ Sessions table already exists';
END

-- =====================================================
-- 3. CREATE SLOT TABLE
-- =====================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'slots')
BEGIN
    CREATE TABLE slots (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        session_id BIGINT NOT NULL,
        start_time TIME NOT NULL,
        end_time TIME NOT NULL,
        description NVARCHAR(MAX),
        status NVARCHAR(20) NOT NULL DEFAULT 'PLANNED',
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        
        -- Foreign key constraint
        CONSTRAINT FK_Slot_Session FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE,
        
        -- Check constraints
        CONSTRAINT CK_Slot_TimeRange CHECK (start_time < end_time),
        CONSTRAINT CK_Slot_Status CHECK (status IN ('PLANNED', 'ACTIVE', 'DONE'))
    );
    
    -- Create indexes for performance
    CREATE INDEX IX_Slot_SessionId ON slots(session_id);
    CREATE INDEX IX_Slot_StartTime ON slots(start_time);
    CREATE INDEX IX_Slot_Status ON slots(status);
    
    PRINT '✅ Created slots table with constraints and indexes';
END
ELSE
BEGIN
    PRINT '⚠️ Slots table already exists';
END

-- =====================================================
-- 4. CREATE ATTACHMENT TABLE
-- =====================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'attachments')
BEGIN
    CREATE TABLE attachments (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        slot_id BIGINT NOT NULL,
        original_file_name NVARCHAR(255) NOT NULL,
        stored_file_name NVARCHAR(255) NOT NULL,
        file_path NVARCHAR(500) NOT NULL,
        file_size BIGINT NOT NULL,
        mime_type NVARCHAR(100) NOT NULL,
        uploaded_by BIGINT NOT NULL,
        uploaded_at DATETIME2 DEFAULT GETDATE(),
        
        -- Foreign key constraints
        CONSTRAINT FK_Attachment_Slot FOREIGN KEY (slot_id) REFERENCES slots(id) ON DELETE CASCADE,
        CONSTRAINT FK_Attachment_UploadedBy FOREIGN KEY (uploaded_by) REFERENCES users(id),
        
        -- Check constraints
        CONSTRAINT CK_Attachment_FileSize CHECK (file_size > 0 AND file_size <= 10485760), -- Max 10MB
        CONSTRAINT CK_Attachment_MimeType CHECK (
            mime_type IN (
                'application/pdf',
                'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
                'application/vnd.openxmlformats-officedocument.presentationml.presentation'
            )
        )
    );
    
    -- Create indexes for performance
    CREATE INDEX IX_Attachment_SlotId ON attachments(slot_id);
    CREATE INDEX IX_Attachment_UploadedBy ON attachments(uploaded_by);
    CREATE INDEX IX_Attachment_UploadedAt ON attachments(uploaded_at);
    
    PRINT '✅ Created attachments table with constraints and indexes';
END
ELSE
BEGIN
    PRINT '⚠️ Attachments table already exists';
END

-- =====================================================
-- 5. CREATE AUDIT LOG TABLE FOR TRACKING CHANGES
-- =====================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'classroom_slot_audit_log')
BEGIN
    CREATE TABLE classroom_slot_audit_log (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        table_name NVARCHAR(50) NOT NULL,
        record_id BIGINT NOT NULL,
        action NVARCHAR(20) NOT NULL, -- INSERT, UPDATE, DELETE
        old_values NVARCHAR(MAX), -- JSON format
        new_values NVARCHAR(MAX), -- JSON format
        changed_by BIGINT NOT NULL,
        changed_at DATETIME2 DEFAULT GETDATE(),
        
        -- Foreign key constraint
        CONSTRAINT FK_AuditLog_ChangedBy FOREIGN KEY (changed_by) REFERENCES users(id),
        
        -- Check constraint
        CONSTRAINT CK_AuditLog_Action CHECK (action IN ('INSERT', 'UPDATE', 'DELETE'))
    );
    
    -- Create indexes for performance
    CREATE INDEX IX_AuditLog_TableRecord ON classroom_slot_audit_log(table_name, record_id);
    CREATE INDEX IX_AuditLog_ChangedBy ON classroom_slot_audit_log(changed_by);
    CREATE INDEX IX_AuditLog_ChangedAt ON classroom_slot_audit_log(changed_at);
    
    PRINT '✅ Created classroom_slot_audit_log table';
END
ELSE
BEGIN
    PRINT '⚠️ Audit log table already exists';
END

-- =====================================================
-- 6. CREATE STORED PROCEDURES FOR BUSINESS LOGIC
-- =====================================================

-- Procedure to check time overlap in slots
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_CheckSlotTimeOverlap')
    DROP PROCEDURE sp_CheckSlotTimeOverlap;
GO

CREATE PROCEDURE sp_CheckSlotTimeOverlap
    @SessionId BIGINT,
    @StartTime TIME,
    @EndTime TIME,
    @ExcludeSlotId BIGINT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @OverlapCount INT;
    
    SELECT @OverlapCount = COUNT(*)
    FROM slots
    WHERE session_id = @SessionId
        AND (@ExcludeSlotId IS NULL OR id != @ExcludeSlotId)
        AND (
            (@StartTime >= start_time AND @StartTime < end_time) OR
            (@EndTime > start_time AND @EndTime <= end_time) OR
            (@StartTime <= start_time AND @EndTime >= end_time)
        );
    
    SELECT @OverlapCount as OverlapCount;
END
GO

PRINT '✅ Created stored procedure sp_CheckSlotTimeOverlap';

-- =====================================================
-- 7. CREATE TRIGGERS FOR AUDIT LOGGING
-- =====================================================

-- Trigger for classrooms table
IF EXISTS (SELECT * FROM sys.triggers WHERE name = 'tr_Classroom_Audit')
    DROP TRIGGER tr_Classroom_Audit;
GO

CREATE TRIGGER tr_Classroom_Audit
ON classrooms
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @Action NVARCHAR(20);
    DECLARE @UserId BIGINT = 1; -- Default system user, should be replaced with actual user context
    
    IF EXISTS(SELECT * FROM inserted) AND EXISTS(SELECT * FROM deleted)
        SET @Action = 'UPDATE';
    ELSE IF EXISTS(SELECT * FROM inserted)
        SET @Action = 'INSERT';
    ELSE
        SET @Action = 'DELETE';
    
    -- Insert audit record (simplified version)
    INSERT INTO classroom_slot_audit_log (table_name, record_id, action, changed_by)
    SELECT 'classrooms', 
           COALESCE(i.id, d.id), 
           @Action, 
           @UserId
    FROM inserted i
    FULL OUTER JOIN deleted d ON i.id = d.id;
END
GO

PRINT '✅ Created audit trigger for classrooms table';

-- =====================================================
-- 8. INSERT SAMPLE DATA FOR TESTING
-- =====================================================

PRINT '📊 Database schema creation completed successfully!';
PRINT '🎯 Next steps:';
PRINT '   1. Run JPA entity generation';
PRINT '   2. Create service layer with business logic';
PRINT '   3. Implement REST controllers';
PRINT '   4. Create frontend components';

GO
