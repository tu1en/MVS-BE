package com.classroomapp.classroombackend.repository.classroommanagement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    List<Classroom> findByTeacher(User teacher);
    List<Classroom> findByStudentsContaining(User student);
    List<Classroom> findByNameContainingIgnoreCase(String name);
    List<Classroom> findBySubject(String subject);
}
