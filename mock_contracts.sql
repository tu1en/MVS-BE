-- =========================================================
-- REAL CONTRACT DATA INITIALIZATION SCRIPT
-- This script initializes the contracts table with proper data structure
-- =========================================================

-- First, ensure the contracts table exists with proper structure
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='contracts' AND xtype='U')
BEGIN
    CREATE TABLE contracts (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        contract_number NVARCHAR(50) UNIQUE,
        full_name NVARCHAR(255) NOT NULL,
        email NVARCHAR(255),
        phone_number NVARCHAR(20),
        contract_type NVARCHAR(50) NOT NULL, -- 'TEACHER', 'STAFF', 'MANAGER'
        position NVARCHAR(255),
        department NVARCHAR(255),
        salary DECIMAL(15,2),
        working_hours NVARCHAR(255),
        start_date DATE NOT NULL,
        end_date DATE NOT NULL,
        status NVARCHAR(50) DEFAULT 'ACTIVE', -- 'ACTIVE', 'NEAR_EXPIRY', 'EXPIRED', 'TERMINATED'
        terms_and_conditions NVARCHAR(MAX),
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        created_by BIGINT,
        
        -- Add indexes for better performance
        INDEX IX_contracts_user_id (user_id),
        INDEX IX_contracts_status (status),
        INDEX IX_contracts_end_date (end_date),
        INDEX IX_contracts_type (contract_type),
        
        -- Add foreign key constraint if users table exists
        -- FOREIGN KEY (user_id) REFERENCES Users(id),
        -- FOREIGN KEY (created_by) REFERENCES Users(id)
    );
END;

-- Add contract auto-numbering if not exists
IF NOT EXISTS (SELECT * FROM sys.sequences WHERE name = 'ContractNumberSequence')
BEGIN
    CREATE SEQUENCE ContractNumberSequence
        START WITH 1
        INCREMENT BY 1
        MINVALUE 1
        MAXVALUE 999999
        CACHE 10;
END;

-- Create trigger for auto-generating contract numbers
IF NOT EXISTS (SELECT * FROM sys.triggers WHERE name = 'trg_GenerateContractNumber')
BEGIN
    EXEC('
    CREATE TRIGGER trg_GenerateContractNumber
    ON contracts
    AFTER INSERT
    AS
    BEGIN
        SET NOCOUNT ON;
        
        UPDATE contracts 
        SET contract_number = ''HD'' + RIGHT(''000000'' + CAST(NEXT VALUE FOR ContractNumberSequence AS NVARCHAR(6)), 6)
        WHERE id IN (SELECT id FROM inserted) AND contract_number IS NULL;
    END
    ');
END;

-- Create trigger for automatic status updates based on end_date
IF NOT EXISTS (SELECT * FROM sys.triggers WHERE name = 'trg_UpdateContractStatus')
BEGIN
    EXEC('
    CREATE TRIGGER trg_UpdateContractStatus
    ON contracts
    AFTER UPDATE
    AS
    BEGIN
        SET NOCOUNT ON;
        
        -- Update status based on end date
        UPDATE contracts 
        SET 
            status = CASE 
                WHEN end_date < GETDATE() THEN ''EXPIRED''
                WHEN end_date <= DATEADD(day, 30, GETDATE()) THEN ''NEAR_EXPIRY''
                ELSE ''ACTIVE''
            END,
            updated_at = GETDATE()
        WHERE id IN (SELECT id FROM inserted);
    END
    ');
END;

-- Create stored procedure for contract management
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_ManageContractStatus')
    DROP PROCEDURE sp_ManageContractStatus;

EXEC('
CREATE PROCEDURE sp_ManageContractStatus
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Update all contract statuses based on current date
    UPDATE contracts 
    SET 
        status = CASE 
            WHEN end_date < GETDATE() THEN ''EXPIRED''
            WHEN end_date <= DATEADD(day, 30, GETDATE()) THEN ''NEAR_EXPIRY''
            ELSE ''ACTIVE''
        END,
        updated_at = GETDATE()
    WHERE status IN (''ACTIVE'', ''NEAR_EXPIRY'');
    
    -- Return summary of status changes
    SELECT 
        status,
        COUNT(*) as contract_count,
        CASE 
            WHEN status = ''EXPIRED'' THEN ''Hợp đồng đã hết hạn''
            WHEN status = ''NEAR_EXPIRY'' THEN ''Hợp đồng sắp hết hạn''
            WHEN status = ''ACTIVE'' THEN ''Hợp đồng đang hiệu lực''
            ELSE ''Trạng thái khác''
        END as status_description
    FROM contracts 
    GROUP BY status
    ORDER BY 
        CASE status 
            WHEN ''EXPIRED'' THEN 1
            WHEN ''NEAR_EXPIRY'' THEN 2
            WHEN ''ACTIVE'' THEN 3
            ELSE 4
        END;
END
');

-- Execute the procedure to initialize statuses
EXEC sp_ManageContractStatus;
