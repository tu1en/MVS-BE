package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.Assignment;
import com.classroomapp.classroombackend.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    
    // Find assignments by classroom
    List<Assignment> findByClassroom(Classroom classroom);
    
    // Find assignments by classroom ordered by due date
    List<Assignment> findByClassroomOrderByDueDateAsc(Classroom classroom);
    
    // Find upcoming assignments (due date is after now)
    List<Assignment> findByClassroomAndDueDateAfterOrderByDueDateAsc(Classroom classroom, LocalDateTime now);
    
    // Find past assignments (due date is before now)
    List<Assignment> findByClassroomAndDueDateBeforeOrderByDueDateDesc(Classroom classroom, LocalDateTime now);
    
    // Find assignments by title containing search term
    List<Assignment> findByTitleContainingIgnoreCase(String title);
} 