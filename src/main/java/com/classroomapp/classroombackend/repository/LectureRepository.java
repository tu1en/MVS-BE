package com.classroomapp.classroombackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.Classroom;
import com.classroomapp.classroombackend.model.Lecture;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    
    List<Lecture> findByClassroomOrderByCreatedAtDesc(Classroom classroom);
    
    List<Lecture> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);
    
    List<Lecture> findByTitleContainingIgnoreCase(String title);
}
