-- Migration script for Course Enrollment System
-- Add public enrollment features to existing course templates and create enrollment requests table

-- 1. Add columns to course_templates table for public enrollment
ALTER TABLE course_templates 
ADD is_public BIT DEFAULT 0,
    enrollment_fee DECIMAL(10,2) DEFAULT 0.00,
    max_students_per_template INT NULL;

-- 2. Create enrollment_requests table
CREATE TABLE enrollment_requests (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    course_template_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status NVARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    message NVARCHAR(MAX) NULL, -- Student's message
    rejection_reason NVARCHAR(MAX) NULL, -- Manager's rejection reason
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    processed_by BIGINT NULL, -- Manager who processed the request
    processed_at DATETIME2 NULL,
    
    FOREIGN KEY (course_template_id) REFERENCES course_templates(id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (processed_by) REFERENCES users(id),
    
    -- Ensure one request per student per course template
    CONSTRAINT unique_student_course_template UNIQUE (student_id, course_template_id)
);

-- 3. Create indexes for better performance
CREATE INDEX idx_enrollment_requests_status ON enrollment_requests(status);
CREATE INDEX idx_enrollment_requests_created_at ON enrollment_requests(created_at DESC);
CREATE INDEX idx_enrollment_requests_course_template_id ON enrollment_requests(course_template_id);
CREATE INDEX idx_enrollment_requests_student_id ON enrollment_requests(student_id);

-- Compound index for common queries
CREATE INDEX idx_enrollment_requests_status_created ON enrollment_requests(status, created_at DESC);

-- 4. Create trigger to update updated_at timestamp
CREATE TRIGGER tr_enrollment_requests_updated_at
ON enrollment_requests
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE enrollment_requests 
    SET updated_at = GETDATE()
    FROM enrollment_requests er
    INNER JOIN inserted i ON er.id = i.id;
END;

-- 5. Add sample data to make some course templates public (optional)
-- UPDATE course_templates 
-- SET is_public = 1, enrollment_fee = 500000.00 
-- WHERE id IN (1, 2, 3); -- Adjust IDs as needed

PRINT 'Enrollment system migration completed successfully!';