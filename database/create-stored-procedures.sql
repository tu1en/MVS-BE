-- =====================================================
-- CREATE STORED PROCEDURES FOR BUSINESS LOGIC
-- =====================================================

USE SchoolManagementDB;
GO

-- =====================================================
-- 1. PROCEDURE TO CHECK SLOT TIME OVERLAP
-- =====================================================

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
-- 2. PROCEDURE TO VALIDATE SESSION DATE RANGE
-- =====================================================

IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_ValidateSessionDateRange')
    DROP PROCEDURE sp_ValidateSessionDateRange;
GO

CREATE PROCEDURE sp_ValidateSessionDateRange
    @ClassroomId BIGINT,
    @SessionDate DATE
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @IsValid BIT = 0;
    DECLARE @StartDate DATE;
    DECLARE @EndDate DATE;
    
    SELECT @StartDate = start_date, @EndDate = end_date
    FROM classrooms
    WHERE id = @ClassroomId;
    
    IF (@StartDate IS NULL OR @EndDate IS NULL)
        SET @IsValid = 1; -- Allow if dates are not set
    ELSE IF (@SessionDate >= @StartDate AND @SessionDate <= @EndDate)
        SET @IsValid = 1;
    
    SELECT @IsValid as IsValid, @StartDate as ClassroomStartDate, @EndDate as ClassroomEndDate;
END
GO

PRINT '✅ Created stored procedure sp_ValidateSessionDateRange';

-- =====================================================
-- 3. PROCEDURE TO GET CLASSROOM STATISTICS
-- =====================================================

IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_GetClassroomStatistics')
    DROP PROCEDURE sp_GetClassroomStatistics;
GO

CREATE PROCEDURE sp_GetClassroomStatistics
    @ClassroomId BIGINT
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        c.id as ClassroomId,
        c.name as ClassroomName,
        c.status as ClassroomStatus,
        COUNT(DISTINCT s.id) as TotalSessions,
        COUNT(DISTINCT sl.id) as TotalSlots,
        COUNT(DISTINCT a.id) as TotalAttachments,
        COUNT(DISTINCT CASE WHEN s.status = 'COMPLETED' THEN s.id END) as CompletedSessions,
        COUNT(DISTINCT CASE WHEN sl.status = 'DONE' THEN sl.id END) as CompletedSlots
    FROM classrooms c
    LEFT JOIN sessions s ON c.id = s.classroom_id
    LEFT JOIN slots sl ON s.id = sl.session_id
    LEFT JOIN attachments a ON sl.id = a.slot_id
    WHERE c.id = @ClassroomId
    GROUP BY c.id, c.name, c.status;
END
GO

PRINT '✅ Created stored procedure sp_GetClassroomStatistics';

-- =====================================================
-- 4. PROCEDURE TO UPDATE CLASSROOM STATUS WITH VALIDATION
-- =====================================================

IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_UpdateClassroomStatus')
    DROP PROCEDURE sp_UpdateClassroomStatus;
GO

CREATE PROCEDURE sp_UpdateClassroomStatus
    @ClassroomId BIGINT,
    @NewStatus NVARCHAR(20),
    @UserId BIGINT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @CurrentStatus NVARCHAR(20);
    DECLARE @IsValidTransition BIT = 0;
    DECLARE @ErrorMessage NVARCHAR(500);
    
    -- Get current status
    SELECT @CurrentStatus = status FROM classrooms WHERE id = @ClassroomId;
    
    IF @CurrentStatus IS NULL
    BEGIN
        SELECT 0 as Success, 'Classroom not found' as ErrorMessage;
        RETURN;
    END
    
    -- Validate status transition (one-way flow)
    IF (@CurrentStatus = 'DRAFT' AND @NewStatus IN ('ACTIVE', 'ARCHIVED'))
        SET @IsValidTransition = 1;
    ELSE IF (@CurrentStatus = 'ACTIVE' AND @NewStatus IN ('COMPLETED', 'ARCHIVED'))
        SET @IsValidTransition = 1;
    ELSE IF (@CurrentStatus = 'COMPLETED' AND @NewStatus = 'ARCHIVED')
        SET @IsValidTransition = 1;
    ELSE IF (@CurrentStatus = @NewStatus)
        SET @IsValidTransition = 1; -- Allow same status
    
    IF @IsValidTransition = 0
    BEGIN
        SET @ErrorMessage = 'Invalid status transition from ' + @CurrentStatus + ' to ' + @NewStatus;
        SELECT 0 as Success, @ErrorMessage as ErrorMessage;
        RETURN;
    END
    
    -- Update status
    UPDATE classrooms 
    SET status = @NewStatus, updated_at = GETDATE()
    WHERE id = @ClassroomId;
    
    SELECT 1 as Success, 'Status updated successfully' as Message;
END
GO

PRINT '✅ Created stored procedure sp_UpdateClassroomStatus';

PRINT '🎯 All stored procedures created successfully!';
GO
