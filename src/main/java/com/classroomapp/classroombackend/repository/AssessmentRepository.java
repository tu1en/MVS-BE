package com.classroomapp.classroombackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.Assessment;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    List<Assessment> findByLectureId(Long lectureId);
} 