package com.classroomapp.classroombackend.repository.classroommanagement;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.Syllabus;

@Repository
public interface SyllabusRepository extends JpaRepository<Syllabus, Long> {
    Optional<Syllabus> findByClassroomId(Long classroomId);
    Optional<Syllabus> findByClassroom(Classroom classroom);
}
