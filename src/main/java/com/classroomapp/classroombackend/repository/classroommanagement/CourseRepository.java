package com.classroomapp.classroombackend.repository.classroommanagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.classroommanagement.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
} 