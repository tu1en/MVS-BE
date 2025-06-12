package com.classroomapp.classroombackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.LectureMaterial;

@Repository
public interface LectureMaterialRepository extends JpaRepository<LectureMaterial, Long> {
    
    List<LectureMaterial> findByLecture(Lecture lecture);
    
    List<LectureMaterial> findByLectureIdOrderByCreatedAtDesc(Long lectureId);
    
    List<LectureMaterial> findByFileNameContainingIgnoreCase(String fileName);
}
