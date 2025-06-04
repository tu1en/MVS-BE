package com.classroomapp.classroombackend.repository.assignmentmanagement;

import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
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
    
    // Find overdue assignments (due date is before now)
    List<Assignment> findByClassroomAndDueDateBeforeOrderByDueDateAsc(Classroom classroom, LocalDateTime now);
    
    // Find overdue assignments ordered by due date descending
    List<Assignment> findByClassroomAndDueDateBeforeOrderByDueDateDesc(Classroom classroom, LocalDateTime now);
    
    // Find assignments by title containing (global search)
    List<Assignment> findByTitleContainingIgnoreCase(String title);
    
    // Find assignments by title containing in specific classroom
    List<Assignment> findByClassroomAndTitleContainingIgnoreCase(Classroom classroom, String title);
}
