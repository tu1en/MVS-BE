-- ========================================================
-- Migration script for Attendance Verification System
-- Run this after existing tables
-- ========================================================

-- 1. Company Locations
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='company_locations' AND xtype='U')
CREATE TABLE company_locations (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(200) NOT NULL,
    address NVARCHAR(500),
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    allowed_radius INT DEFAULT 100,
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE()
);

-- 2. Allowed Networks
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='allowed_networks' AND xtype='U')
CREATE TABLE allowed_networks (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(200) NOT NULL,
    ip_range VARCHAR(50) NOT NULL,
    network_type VARCHAR(50),
    location_id BIGINT,
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (location_id) REFERENCES company_locations(id)
);

-- 3. Attendance Verification Logs
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='attendance_verification_logs' AND xtype='U')
CREATE TABLE attendance_verification_logs (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id BIGINT NOT NULL,
    attendance_log_id BIGINT,
    verification_type VARCHAR(50),
    gps_latitude DECIMAL(10, 8),
    gps_longitude DECIMAL(11, 8),
    gps_accuracy INT,
    location_verified BIT DEFAULT 0,
    location_distance INT,
    public_ip VARCHAR(45),
    network_verified BIT DEFAULT 0,
    network_name NVARCHAR(200),
    user_agent NVARCHAR(500),
    device_fingerprint VARCHAR(100),
    verification_status VARCHAR(50),
    failure_reason NVARCHAR(500),
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (attendance_log_id) REFERENCES staff_attendance_logs(id)
);

-- 4. Add indexes for performance
CREATE INDEX IX_company_locations_active ON company_locations(is_active);
CREATE INDEX IX_allowed_networks_active ON allowed_networks(is_active);
CREATE INDEX IX_verification_logs_user ON attendance_verification_logs(user_id);
CREATE INDEX IX_verification_logs_date ON attendance_verification_logs(created_at);

-- 5. Insert sample data
INSERT INTO company_locations (name, address, latitude, longitude, allowed_radius) VALUES
(N'Văn phòng chính', N'123 Nguyễn Huệ, Q1, TP.HCM', 10.776889, 106.700897, 100),
(N'Chi nhánh Q2', N'456 Trần Não, Q2, TP.HCM', 10.786702, 106.748565, 80),
(N'Chi nhánh Q7', N'789 Đường Nguyễn Văn Linh, Q7, TP.HCM', 10.732776, 106.722289, 120);

INSERT INTO allowed_networks (name, ip_range, network_type, location_id) VALUES
('Office Network 1', '192.168.1.0/24', 'OFFICE', 1),
('Office Network 2', '192.168.2.0/24', 'OFFICE', 1),
('Company VPN', '10.0.0.0/16', 'VPN', NULL),
('Public IP Range 1', '116.118.0.0/16', 'OFFICE', 1),
('Branch Q2 Network', '192.168.10.0/24', 'OFFICE', 2),
('Branch Q7 Network', '192.168.20.0/24', 'OFFICE', 3),
('Remote Work VPN', '172.16.0.0/12', 'REMOTE', NULL);

-- 6. Add new methods to AttendanceLogRepository (comment for reference)
-- Need to add these methods to AttendanceLogRepository.java:
-- List<AttendanceLog> findByDateBetween(LocalDate startDate, LocalDate endDate);
-- List<AttendanceLog> findByDepartmentAndDateBetween(String department, LocalDate startDate, LocalDate endDate);

PRINT 'Attendance Verification System migration completed successfully!';