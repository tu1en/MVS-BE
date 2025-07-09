package com.classroomapp.classroombackend.repository.assignmentmanagement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    
    // Find assignments by classroom
    List<Assignment> findByClassroom(Classroom classroom);
    
    // Find assignments by classroom ordered by due date
    List<Assignment> findByClassroomOrderByDueDateAsc(Classroom classroom);
    
    // Optimized query with JOIN FETCH to avoid N+1 problem when accessing classroom data
    @Query("SELECT a FROM Assignment a JOIN FETCH a.classroom WHERE a.classroom = :classroom ORDER BY a.dueDate ASC")
    List<Assignment> findByClassroomWithClassroomOrderByDueDateAsc(@Param("classroom") Classroom classroom);
    
    // Optimized query for GetCourseDetails method
    @Query("SELECT a FROM Assignment a JOIN FETCH a.classroom WHERE a.classroom = :classroom")
    List<Assignment> findByClassroomWithClassroom(@Param("classroom") Classroom classroom);
    
    // Find upcoming assignments (due date is after now)
    List<Assignment> findByClassroomAndDueDateAfterOrderByDueDateAsc(Classroom classroom, LocalDateTime now);
    
    // Optimized upcoming assignments query with JOIN FETCH
    @Query("SELECT a FROM Assignment a JOIN FETCH a.classroom WHERE a.classroom = :classroom AND a.dueDate > :now ORDER BY a.dueDate ASC")
    List<Assignment> findByClassroomAndDueDateAfterOrderByDueDateAscWithClassroom(@Param("classroom") Classroom classroom, @Param("now") LocalDateTime now);
    
    // Find overdue assignments (due date is before now)
    List<Assignment> findByClassroomAndDueDateBeforeOrderByDueDateAsc(Classroom classroom, LocalDateTime now);
    
    // Find overdue assignments ordered by due date descending
    List<Assignment> findByClassroomAndDueDateBeforeOrderByDueDateDesc(Classroom classroom, LocalDateTime now);
    
    // Optimized past assignments query with JOIN FETCH
    @Query("SELECT a FROM Assignment a JOIN FETCH a.classroom WHERE a.classroom = :classroom AND a.dueDate < :now ORDER BY a.dueDate DESC")
    List<Assignment> findByClassroomAndDueDateBeforeOrderByDueDateDescWithClassroom(@Param("classroom") Classroom classroom, @Param("now") LocalDateTime now);
      // Find assignments by title containing (global search)
    List<Assignment> findByTitleContainingIgnoreCase(String title);
    
    // Find assignments by title containing in specific classroom
    List<Assignment> findByClassroomAndTitleContainingIgnoreCase(Classroom classroom, String title);
    
    // Bulk query to get assignments for multiple classrooms at once to avoid N+1
    @Query("SELECT a FROM Assignment a JOIN FETCH a.classroom WHERE a.classroom IN :classrooms ORDER BY a.dueDate ASC")
    List<Assignment> findByClassroomInWithClassroomOrderByDueDateAsc(@Param("classrooms") List<Classroom> classrooms);

    @Query("SELECT a FROM Assignment a WHERE a.classroom.teacher.id = :teacherId")
    List<Assignment> findByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT a FROM Assignment a WHERE a.classroom.teacher.username = :username")
    List<Assignment> findByTeacherUsername(@Param("username") String username);

    List<Assignment> findByClassroomId(Long classroomId);
    
    long countByClassroomIdIn(List<Long> classroomIds);

    List<Assignment> findByClassroomIdIn(List<Long> classroomIds);

    List<Assignment> findByClassroomInOrderByDueDateAsc(List<Classroom> classrooms);

    List<Assignment> findByClassroomAndDueDateAfter(Classroom classroom, LocalDateTime dateTime);
    List<Assignment> findByClassroomAndDueDateBefore(Classroom classroom, LocalDateTime dateTime);
}
