package com.classroomapp.classroombackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    
    List<Lecture> findByClassroomOrderByCreatedAtDesc(Classroom classroom);
    
    List<Lecture> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);
    
    List<Lecture> findByTitleContainingIgnoreCase(String title);

    List<Lecture> findByClassroomId(Long classroomId);
    boolean existsByClassroomId(Long classroomId);
    Optional<Lecture> findById(Long id);
}
