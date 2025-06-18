-- Create schedules table
CREATE TABLE schedules (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    teacher_id BIGINT NOT NULL,
    classroom_id BIGINT NOT NULL,
    day_of_week INT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room NVARCHAR(50),
    subject NVARCHAR(100) NOT NULL,
    materials_url NVARCHAR(255),
    meet_url NVARCHAR(255),
    FOREIGN KEY (teacher_id) REFERENCES users(id),
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id)
);

-- Add indexes for faster queries
CREATE INDEX idx_schedules_teacher_id ON schedules(teacher_id);
CREATE INDEX idx_schedules_classroom_id ON schedules(classroom_id);
CREATE INDEX idx_schedules_day_of_week ON schedules(day_of_week); 