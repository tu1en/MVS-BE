package com.classroomapp.classroombackend.repository.classroommanagement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomSchedule;

@Repository
public interface ClassroomScheduleRepository extends JpaRepository<ClassroomSchedule, Long> {
    List<ClassroomSchedule> findByClassroomId(Long classroomId);
    List<ClassroomSchedule> findByClassroom(Classroom classroom);
}
