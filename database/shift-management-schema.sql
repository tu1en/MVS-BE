-- ================================================================================================
-- SHIFT MANAGEMENT MODULE - SQL SERVER SCHEMA
-- Module 2: Comprehensive Shift Management System for HR
-- Compatible with SQL Server 2019+
-- ================================================================================================

USE [SEP490_ClassroomManagement];
GO

-- ================================================================================================
-- 1. SHIFT TEMPLATES TABLE
-- Định nghĩa các mẫu ca làm việc (Morning, Afternoon, Evening, Full Day, Custom)
-- ================================================================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'shift_templates')
BEGIN
    CREATE TABLE shift_templates (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        template_name NVARCHAR(255) NOT NULL,
        template_code NVARCHAR(50) NOT NULL UNIQUE, -- MS, AS, ES, FD, etc.
        description NVARCHAR(MAX),
        start_time TIME NOT NULL,
        end_time TIME NOT NULL,
        break_start_time TIME NULL,
        break_end_time TIME NULL,
        break_duration_minutes INT DEFAULT 0,
        total_hours DECIMAL(4,2) NOT NULL,
        is_active BIT DEFAULT 1,
        is_overtime_eligible BIT DEFAULT 0,
        color_code NVARCHAR(7) DEFAULT '#1890ff', -- Hex color for UI
        sort_order INT DEFAULT 0,
        created_by BIGINT NOT NULL,
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        
        -- Constraints
        CONSTRAINT FK_shift_templates_created_by FOREIGN KEY (created_by) REFERENCES users(id),
        CONSTRAINT CK_shift_templates_time_valid CHECK (start_time < end_time),
        CONSTRAINT CK_shift_templates_break_valid CHECK (
            (break_start_time IS NULL AND break_end_time IS NULL) OR
            (break_start_time IS NOT NULL AND break_end_time IS NOT NULL AND 
             break_start_time >= start_time AND break_end_time <= end_time AND
             break_start_time < break_end_time)
        ),
        CONSTRAINT CK_shift_templates_hours_positive CHECK (total_hours > 0)
    );
    
    -- Indexes
    CREATE INDEX IX_shift_templates_active ON shift_templates(is_active);
    CREATE INDEX IX_shift_templates_code ON shift_templates(template_code);
    CREATE INDEX IX_shift_templates_sort ON shift_templates(sort_order);
    
    PRINT '✅ Created shift_templates table with constraints and indexes';
END
ELSE
BEGIN
    PRINT '⚠️ shift_templates table already exists';
END
GO

-- ================================================================================================
-- 2. SHIFT SCHEDULES TABLE  
-- Quản lý các lịch làm việc theo tuần/tháng
-- ================================================================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'shift_schedules')
BEGIN
    CREATE TABLE shift_schedules (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        schedule_name NVARCHAR(255) NOT NULL,
        description NVARCHAR(MAX),
        start_date DATE NOT NULL,
        end_date DATE NOT NULL,
        schedule_type NVARCHAR(50) DEFAULT 'WEEKLY', -- WEEKLY, MONTHLY, CUSTOM
        status NVARCHAR(50) DEFAULT 'DRAFT', -- DRAFT, PUBLISHED, ARCHIVED, CANCELLED
        total_assignments INT DEFAULT 0,
        created_by BIGINT NOT NULL,
        published_by BIGINT NULL,
        published_at DATETIME2 NULL,
        archived_at DATETIME2 NULL,
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        
        -- Constraints
        CONSTRAINT FK_shift_schedules_created_by FOREIGN KEY (created_by) REFERENCES users(id),
        CONSTRAINT FK_shift_schedules_published_by FOREIGN KEY (published_by) REFERENCES users(id),
        CONSTRAINT CK_shift_schedules_date_valid CHECK (start_date <= end_date),
        CONSTRAINT CK_shift_schedules_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED', 'CANCELLED')),
        CONSTRAINT CK_shift_schedules_type CHECK (schedule_type IN ('WEEKLY', 'MONTHLY', 'CUSTOM'))
    );
    
    -- Indexes
    CREATE INDEX IX_shift_schedules_dates ON shift_schedules(start_date, end_date);
    CREATE INDEX IX_shift_schedules_status ON shift_schedules(status);
    CREATE INDEX IX_shift_schedules_type ON shift_schedules(schedule_type);
    
    PRINT '✅ Created shift_schedules table with constraints and indexes';
END
ELSE
BEGIN
    PRINT '⚠️ shift_schedules table already exists';
END
GO

-- ================================================================================================
-- 3. SHIFT ASSIGNMENTS TABLE
-- Phân công ca làm việc cho nhân viên
-- ================================================================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'shift_assignments')
BEGIN
    CREATE TABLE shift_assignments (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        shift_template_id BIGINT NOT NULL,
        schedule_id BIGINT NULL, -- Reference to shift_schedules
        assignment_date DATE NOT NULL,
        planned_start_time TIME NOT NULL,
        planned_end_time TIME NOT NULL,
        actual_start_time TIME NULL,
        actual_end_time TIME NULL,
        break_start_time TIME NULL,
        break_end_time TIME NULL,
        status NVARCHAR(50) DEFAULT 'SCHEDULED', -- SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
        attendance_status NVARCHAR(50) DEFAULT 'PENDING', -- PENDING, PRESENT, ABSENT, LATE, EARLY_LEAVE
        planned_hours DECIMAL(4,2) NOT NULL,
        actual_hours DECIMAL(4,2) NULL,
        overtime_hours DECIMAL(4,2) DEFAULT 0,
        notes NVARCHAR(MAX),
        assigned_by BIGINT NOT NULL,
        check_in_time DATETIME2 NULL,
        check_out_time DATETIME2 NULL,
        location_check_in NVARCHAR(MAX) NULL, -- JSON for GPS coordinates
        location_check_out NVARCHAR(MAX) NULL, -- JSON for GPS coordinates
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        
        -- Constraints
        CONSTRAINT FK_shift_assignments_employee FOREIGN KEY (employee_id) REFERENCES users(id),
        CONSTRAINT FK_shift_assignments_template FOREIGN KEY (shift_template_id) REFERENCES shift_templates(id),
        CONSTRAINT FK_shift_assignments_schedule FOREIGN KEY (schedule_id) REFERENCES shift_schedules(id),
        CONSTRAINT FK_shift_assignments_assigned_by FOREIGN KEY (assigned_by) REFERENCES users(id),
        CONSTRAINT CK_shift_assignments_time_valid CHECK (planned_start_time < planned_end_time),
        CONSTRAINT CK_shift_assignments_actual_time_valid CHECK (
            actual_start_time IS NULL OR actual_end_time IS NULL OR actual_start_time <= actual_end_time
        ),
        CONSTRAINT CK_shift_assignments_status CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
        CONSTRAINT CK_shift_assignments_attendance CHECK (attendance_status IN ('PENDING', 'PRESENT', 'ABSENT', 'LATE', 'EARLY_LEAVE')),
        CONSTRAINT CK_shift_assignments_hours_positive CHECK (planned_hours > 0),
        CONSTRAINT CK_shift_assignments_overtime_positive CHECK (overtime_hours >= 0)
    );
    
    -- Indexes for performance
    CREATE INDEX IX_shift_assignments_employee_date ON shift_assignments(employee_id, assignment_date);
    CREATE INDEX IX_shift_assignments_date ON shift_assignments(assignment_date);
    CREATE INDEX IX_shift_assignments_status ON shift_assignments(status);
    CREATE INDEX IX_shift_assignments_attendance ON shift_assignments(attendance_status);
    CREATE INDEX IX_shift_assignments_schedule ON shift_assignments(schedule_id);
    CREATE UNIQUE INDEX IX_shift_assignments_unique ON shift_assignments(employee_id, assignment_date, planned_start_time);
    
    PRINT '✅ Created shift_assignments table with constraints and indexes';
END
ELSE
BEGIN
    PRINT '⚠️ shift_assignments table already exists';
END
GO

-- ================================================================================================
-- 4. SHIFT SWAP REQUESTS TABLE
-- Quản lý yêu cầu đổi ca giữa nhân viên
-- ================================================================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'shift_swap_requests')
BEGIN
    CREATE TABLE shift_swap_requests (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        requester_assignment_id BIGINT NOT NULL,
        target_assignment_id BIGINT NOT NULL,
        requester_id BIGINT NOT NULL,
        target_employee_id BIGINT NOT NULL,
        request_reason NVARCHAR(MAX),
        request_type NVARCHAR(50) DEFAULT 'SWAP', -- SWAP, COVER, TRADE
        status NVARCHAR(50) DEFAULT 'PENDING', -- PENDING, ACCEPTED_BY_TARGET, REJECTED_BY_TARGET, APPROVED, REJECTED, CANCELLED, EXPIRED
        target_response NVARCHAR(50) NULL, -- ACCEPTED, REJECTED
        target_response_reason NVARCHAR(MAX) NULL,
        target_responded_at DATETIME2 NULL,
        manager_response NVARCHAR(50) NULL, -- APPROVED, REJECTED
        manager_response_reason NVARCHAR(MAX) NULL,
        approved_by BIGINT NULL,
        approved_at DATETIME2 NULL,
        expires_at DATETIME2 NULL, -- Auto-expire after 48 hours
        priority NVARCHAR(50) DEFAULT 'NORMAL', -- LOW, NORMAL, HIGH, URGENT
        is_emergency BIT DEFAULT 0,
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        
        -- Constraints
        CONSTRAINT FK_shift_swap_requester_assignment FOREIGN KEY (requester_assignment_id) REFERENCES shift_assignments(id),
        CONSTRAINT FK_shift_swap_target_assignment FOREIGN KEY (target_assignment_id) REFERENCES shift_assignments(id),
        CONSTRAINT FK_shift_swap_requester FOREIGN KEY (requester_id) REFERENCES users(id),
        CONSTRAINT FK_shift_swap_target_employee FOREIGN KEY (target_employee_id) REFERENCES users(id),
        CONSTRAINT FK_shift_swap_approved_by FOREIGN KEY (approved_by) REFERENCES users(id),
        CONSTRAINT CK_shift_swap_different_assignments CHECK (requester_assignment_id != target_assignment_id),
        CONSTRAINT CK_shift_swap_different_employees CHECK (requester_id != target_employee_id),
        CONSTRAINT CK_shift_swap_status CHECK (status IN ('PENDING', 'ACCEPTED_BY_TARGET', 'REJECTED_BY_TARGET', 'APPROVED', 'REJECTED', 'CANCELLED', 'EXPIRED')),
        CONSTRAINT CK_shift_swap_type CHECK (request_type IN ('SWAP', 'COVER', 'TRADE')),
        CONSTRAINT CK_shift_swap_priority CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
        CONSTRAINT CK_shift_swap_target_response CHECK (target_response IN ('ACCEPTED', 'REJECTED') OR target_response IS NULL),
        CONSTRAINT CK_shift_swap_manager_response CHECK (manager_response IN ('APPROVED', 'REJECTED') OR manager_response IS NULL)
    );
    
    -- Indexes
    CREATE INDEX IX_shift_swap_requester ON shift_swap_requests(requester_id);
    CREATE INDEX IX_shift_swap_target ON shift_swap_requests(target_employee_id);
    CREATE INDEX IX_shift_swap_status ON shift_swap_requests(status);
    CREATE INDEX IX_shift_swap_expires ON shift_swap_requests(expires_at);
    CREATE INDEX IX_shift_swap_priority ON shift_swap_requests(priority, is_emergency);
    
    PRINT '✅ Created shift_swap_requests table with constraints and indexes';
END
ELSE
BEGIN
    PRINT '⚠️ shift_swap_requests table already exists';
END
GO

-- ================================================================================================
-- 5. SHIFT NOTIFICATIONS TABLE
-- Quản lý thông báo liên quan đến ca làm việc
-- ================================================================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'shift_notifications')
BEGIN
    CREATE TABLE shift_notifications (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        recipient_id BIGINT NOT NULL,
        sender_id BIGINT NULL,
        notification_type NVARCHAR(50) NOT NULL, -- ASSIGNMENT, SWAP_REQUEST, SWAP_APPROVED, SCHEDULE_PUBLISHED, REMINDER
        title NVARCHAR(255) NOT NULL,
        message NVARCHAR(MAX) NOT NULL,
        related_entity_type NVARCHAR(50) NULL, -- SHIFT_ASSIGNMENT, SWAP_REQUEST, SCHEDULE
        related_entity_id BIGINT NULL,
        is_read BIT DEFAULT 0,
        is_email_sent BIT DEFAULT 0,
        is_push_sent BIT DEFAULT 0,
        priority NVARCHAR(50) DEFAULT 'NORMAL', -- LOW, NORMAL, HIGH, URGENT
        scheduled_for DATETIME2 NULL, -- For scheduled notifications
        expires_at DATETIME2 NULL,
        metadata NVARCHAR(MAX) NULL, -- JSON for additional data
        created_at DATETIME2 DEFAULT GETDATE(),
        read_at DATETIME2 NULL,

        -- Constraints
        CONSTRAINT FK_shift_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users(id),
        CONSTRAINT FK_shift_notifications_sender FOREIGN KEY (sender_id) REFERENCES users(id),
        CONSTRAINT CK_shift_notifications_type CHECK (notification_type IN ('ASSIGNMENT', 'SWAP_REQUEST', 'SWAP_APPROVED', 'SWAP_REJECTED', 'SCHEDULE_PUBLISHED', 'REMINDER', 'CANCELLATION')),
        CONSTRAINT CK_shift_notifications_priority CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
        CONSTRAINT CK_shift_notifications_entity_type CHECK (related_entity_type IN ('SHIFT_ASSIGNMENT', 'SWAP_REQUEST', 'SCHEDULE') OR related_entity_type IS NULL)
    );

    -- Indexes
    CREATE INDEX IX_shift_notifications_recipient ON shift_notifications(recipient_id, is_read);
    CREATE INDEX IX_shift_notifications_type ON shift_notifications(notification_type);
    CREATE INDEX IX_shift_notifications_scheduled ON shift_notifications(scheduled_for);
    CREATE INDEX IX_shift_notifications_expires ON shift_notifications(expires_at);

    PRINT '✅ Created shift_notifications table with constraints and indexes';
END
ELSE
BEGIN
    PRINT '⚠️ shift_notifications table already exists';
END
GO

-- ================================================================================================
-- 6. EMPLOYEE AVAILABILITY TABLE
-- Quản lý thời gian có thể làm việc của nhân viên
-- ================================================================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'employee_availability')
BEGIN
    CREATE TABLE employee_availability (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        day_of_week INT NOT NULL, -- 1=Monday, 7=Sunday
        available_from TIME NOT NULL,
        available_to TIME NOT NULL,
        is_available BIT DEFAULT 1,
        max_hours_per_day DECIMAL(4,2) DEFAULT 8.0,
        preferred_shift_types NVARCHAR(MAX) NULL, -- JSON array of preferred shift template codes
        notes NVARCHAR(MAX),
        effective_from DATE DEFAULT CAST(GETDATE() AS DATE),
        effective_to DATE NULL,
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),

        -- Constraints
        CONSTRAINT FK_employee_availability_employee FOREIGN KEY (employee_id) REFERENCES users(id),
        CONSTRAINT CK_employee_availability_day CHECK (day_of_week BETWEEN 1 AND 7),
        CONSTRAINT CK_employee_availability_time CHECK (available_from < available_to),
        CONSTRAINT CK_employee_availability_hours CHECK (max_hours_per_day > 0 AND max_hours_per_day <= 24),
        CONSTRAINT CK_employee_availability_dates CHECK (effective_to IS NULL OR effective_from <= effective_to)
    );

    -- Indexes
    CREATE INDEX IX_employee_availability_employee ON employee_availability(employee_id);
    CREATE INDEX IX_employee_availability_day ON employee_availability(day_of_week);
    CREATE INDEX IX_employee_availability_effective ON employee_availability(effective_from, effective_to);
    CREATE UNIQUE INDEX IX_employee_availability_unique ON employee_availability(employee_id, day_of_week, effective_from);

    PRINT '✅ Created employee_availability table with constraints and indexes';
END
ELSE
BEGIN
    PRINT '⚠️ employee_availability table already exists';
END
GO

-- ================================================================================================
-- 7. SHIFT STATISTICS TABLE (For reporting and analytics)
-- Thống kê ca làm việc cho báo cáo
-- ================================================================================================

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'shift_statistics')
BEGIN
    CREATE TABLE shift_statistics (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        employee_id BIGINT NOT NULL,
        period_start DATE NOT NULL,
        period_end DATE NOT NULL,
        period_type NVARCHAR(50) NOT NULL, -- WEEKLY, MONTHLY, QUARTERLY, YEARLY
        total_scheduled_hours DECIMAL(6,2) DEFAULT 0,
        total_worked_hours DECIMAL(6,2) DEFAULT 0,
        total_overtime_hours DECIMAL(6,2) DEFAULT 0,
        total_shifts_assigned INT DEFAULT 0,
        total_shifts_completed INT DEFAULT 0,
        total_shifts_missed INT DEFAULT 0,
        total_late_arrivals INT DEFAULT 0,
        total_early_departures INT DEFAULT 0,
        attendance_rate DECIMAL(5,2) DEFAULT 0, -- Percentage
        punctuality_rate DECIMAL(5,2) DEFAULT 0, -- Percentage
        swap_requests_made INT DEFAULT 0,
        swap_requests_received INT DEFAULT 0,
        swap_requests_approved INT DEFAULT 0,
        calculated_at DATETIME2 DEFAULT GETDATE(),

        -- Constraints
        CONSTRAINT FK_shift_statistics_employee FOREIGN KEY (employee_id) REFERENCES users(id),
        CONSTRAINT CK_shift_statistics_period CHECK (period_start <= period_end),
        CONSTRAINT CK_shift_statistics_type CHECK (period_type IN ('WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY')),
        CONSTRAINT CK_shift_statistics_hours_positive CHECK (
            total_scheduled_hours >= 0 AND total_worked_hours >= 0 AND total_overtime_hours >= 0
        ),
        CONSTRAINT CK_shift_statistics_counts_positive CHECK (
            total_shifts_assigned >= 0 AND total_shifts_completed >= 0 AND total_shifts_missed >= 0
        ),
        CONSTRAINT CK_shift_statistics_rates CHECK (
            attendance_rate BETWEEN 0 AND 100 AND punctuality_rate BETWEEN 0 AND 100
        )
    );

    -- Indexes
    CREATE INDEX IX_shift_statistics_employee ON shift_statistics(employee_id);
    CREATE INDEX IX_shift_statistics_period ON shift_statistics(period_start, period_end);
    CREATE INDEX IX_shift_statistics_type ON shift_statistics(period_type);
    CREATE UNIQUE INDEX IX_shift_statistics_unique ON shift_statistics(employee_id, period_start, period_end, period_type);

    PRINT '✅ Created shift_statistics table with constraints and indexes';
END
ELSE
BEGIN
    PRINT '⚠️ shift_statistics table already exists';
END
GO

-- ================================================================================================
-- 8. INSERT SAMPLE SHIFT TEMPLATES
-- Tạo các mẫu ca làm việc cơ bản
-- ================================================================================================

-- Get admin user ID for created_by field
DECLARE @AdminUserId BIGINT = (SELECT TOP 1 id FROM users WHERE role = 'ADMIN' ORDER BY id);

IF @AdminUserId IS NOT NULL
BEGIN
    -- Morning Shift
    IF NOT EXISTS (SELECT 1 FROM shift_templates WHERE template_code = 'MS')
    BEGIN
        INSERT INTO shift_templates (template_name, template_code, description, start_time, end_time,
                                   break_start_time, break_end_time, break_duration_minutes, total_hours,
                                   color_code, sort_order, created_by)
        VALUES (N'Ca Sáng', 'MS', N'Ca làm việc buổi sáng từ 8:00 đến 12:00',
                '08:00:00', '12:00:00', '10:00:00', '10:15:00', 15, 3.75, '#52c41a', 1, @AdminUserId);
        PRINT '✅ Inserted Morning Shift template';
    END

    -- Afternoon Shift
    IF NOT EXISTS (SELECT 1 FROM shift_templates WHERE template_code = 'AS')
    BEGIN
        INSERT INTO shift_templates (template_name, template_code, description, start_time, end_time,
                                   break_start_time, break_end_time, break_duration_minutes, total_hours,
                                   color_code, sort_order, created_by)
        VALUES (N'Ca Chiều', 'AS', N'Ca làm việc buổi chiều từ 13:00 đến 17:00',
                '13:00:00', '17:00:00', '15:00:00', '15:15:00', 15, 3.75, '#1890ff', 2, @AdminUserId);
        PRINT '✅ Inserted Afternoon Shift template';
    END

    -- Evening Shift
    IF NOT EXISTS (SELECT 1 FROM shift_templates WHERE template_code = 'ES')
    BEGIN
        INSERT INTO shift_templates (template_name, template_code, description, start_time, end_time,
                                   break_start_time, break_end_time, break_duration_minutes, total_hours,
                                   color_code, sort_order, created_by)
        VALUES (N'Ca Tối', 'ES', N'Ca làm việc buổi tối từ 18:00 đến 22:00',
                '18:00:00', '22:00:00', '20:00:00', '20:15:00', 15, 3.75, '#722ed1', 3, @AdminUserId);
        PRINT '✅ Inserted Evening Shift template';
    END

    -- Full Day Shift
    IF NOT EXISTS (SELECT 1 FROM shift_templates WHERE template_code = 'FD')
    BEGIN
        INSERT INTO shift_templates (template_name, template_code, description, start_time, end_time,
                                   break_start_time, break_end_time, break_duration_minutes, total_hours,
                                   color_code, sort_order, created_by)
        VALUES (N'Ca Cả Ngày', 'FD', N'Ca làm việc cả ngày từ 8:00 đến 17:00 với nghỉ trưa',
                '08:00:00', '17:00:00', '12:00:00', '13:00:00', 60, 8.0, '#fa8c16', 4, @AdminUserId);
        PRINT '✅ Inserted Full Day Shift template';
    END

    -- Overtime Shift
    IF NOT EXISTS (SELECT 1 FROM shift_templates WHERE template_code = 'OT')
    BEGIN
        INSERT INTO shift_templates (template_name, template_code, description, start_time, end_time,
                                   total_hours, is_overtime_eligible, color_code, sort_order, created_by)
        VALUES (N'Ca Tăng Ca', 'OT', N'Ca tăng ca ngoài giờ',
                '17:00:00', '20:00:00', 3.0, 1, '#f5222d', 5, @AdminUserId);
        PRINT '✅ Inserted Overtime Shift template';
    END

    PRINT '🎉 All shift templates inserted successfully';
END
ELSE
BEGIN
    PRINT '⚠️ No admin user found, skipping shift template insertion';
END
GO

-- ================================================================================================
-- 9. STORED PROCEDURES FOR SHIFT MANAGEMENT
-- ================================================================================================

-- Procedure to check shift conflicts
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_CheckShiftConflicts')
    DROP PROCEDURE sp_CheckShiftConflicts;
GO

CREATE PROCEDURE sp_CheckShiftConflicts
    @EmployeeId BIGINT,
    @AssignmentDate DATE,
    @StartTime TIME,
    @EndTime TIME,
    @ExcludeAssignmentId BIGINT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        sa.id,
        sa.assignment_date,
        sa.planned_start_time,
        sa.planned_end_time,
        st.template_name,
        'OVERLAP' as conflict_type
    FROM shift_assignments sa
    INNER JOIN shift_templates st ON sa.shift_template_id = st.id
    WHERE sa.employee_id = @EmployeeId
        AND sa.assignment_date = @AssignmentDate
        AND sa.status NOT IN ('CANCELLED')
        AND (@ExcludeAssignmentId IS NULL OR sa.id != @ExcludeAssignmentId)
        AND (
            -- Check for time overlap
            (sa.planned_start_time < @EndTime AND sa.planned_end_time > @StartTime)
        )

    UNION ALL

    -- Check minimum rest time (8 hours between shifts)
    SELECT
        sa.id,
        sa.assignment_date,
        sa.planned_start_time,
        sa.planned_end_time,
        st.template_name,
        'INSUFFICIENT_REST' as conflict_type
    FROM shift_assignments sa
    INNER JOIN shift_templates st ON sa.shift_template_id = st.id
    WHERE sa.employee_id = @EmployeeId
        AND sa.status NOT IN ('CANCELLED')
        AND (@ExcludeAssignmentId IS NULL OR sa.id != @ExcludeAssignmentId)
        AND (
            -- Previous day shift ending too close to new shift
            (sa.assignment_date = DATEADD(day, -1, @AssignmentDate)
             AND DATEDIFF(HOUR, sa.planned_end_time, @StartTime) < 8)
            OR
            -- Next day shift starting too close to new shift
            (sa.assignment_date = DATEADD(day, 1, @AssignmentDate)
             AND DATEDIFF(HOUR, @EndTime, sa.planned_start_time) < 8)
        );
END
GO

PRINT '✅ Created sp_CheckShiftConflicts stored procedure';

-- ================================================================================================
-- 10. VIEWS FOR REPORTING
-- ================================================================================================

-- View for current week shift assignments
IF EXISTS (SELECT * FROM sys.views WHERE name = 'vw_CurrentWeekShifts')
    DROP VIEW vw_CurrentWeekShifts;
GO

CREATE VIEW vw_CurrentWeekShifts AS
SELECT
    sa.id,
    sa.employee_id,
    u.full_name as employee_name,
    u.email as employee_email,
    sa.assignment_date,
    DATENAME(WEEKDAY, sa.assignment_date) as day_name,
    st.template_name,
    st.template_code,
    sa.planned_start_time,
    sa.planned_end_time,
    sa.planned_hours,
    sa.status,
    sa.attendance_status,
    st.color_code
FROM shift_assignments sa
INNER JOIN users u ON sa.employee_id = u.id
INNER JOIN shift_templates st ON sa.shift_template_id = st.id
WHERE sa.assignment_date BETWEEN
    DATEADD(day, -(DATEPART(weekday, GETDATE()) - 2), CAST(GETDATE() AS DATE)) AND
    DATEADD(day, 7 - DATEPART(weekday, GETDATE()), CAST(GETDATE() AS DATE))
    AND sa.status NOT IN ('CANCELLED');
GO

PRINT '✅ Created vw_CurrentWeekShifts view';

PRINT '🎉 Shift Management Database Schema created successfully!';
PRINT '📊 Summary:';
PRINT '   - 7 main tables created with proper constraints';
PRINT '   - Sample shift templates inserted';
PRINT '   - Stored procedures for conflict detection';
PRINT '   - Views for reporting';
PRINT '   - All indexes optimized for performance';
GO
