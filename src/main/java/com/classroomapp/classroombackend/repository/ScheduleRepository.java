package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByTeacherId(Long teacherId);
    List<Schedule> findByStudentIdsContaining(Long studentId);
    List<Schedule> findByClassId(String classId);
}
