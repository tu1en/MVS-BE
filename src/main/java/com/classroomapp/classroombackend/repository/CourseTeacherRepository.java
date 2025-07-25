package com.classroomapp.classroombackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.entity.CourseTeacher;
import com.classroomapp.classroombackend.entity.enumeration.CourseTeacherStatus;

@Repository
public interface CourseTeacherRepository extends JpaRepository<CourseTeacher, Long> {

    // ✅ Check if teacher is assigned to a course and active
    boolean existsByCourseIdAndTeacherIdAndIsActiveTrue(Long courseId, Long teacherId);

    // ✅ Find by courseId and teacherId
    Optional<CourseTeacher> findByCourseIdAndTeacherId(Long courseId, Long teacherId);

    // ✅ Find all active assignments by course
    List<CourseTeacher> findByCourseIdAndIsActiveTrue(Long courseId);

    // ✅ Find all active assignments by teacher
    List<CourseTeacher> findByTeacherIdAndIsActiveTrue(Long teacherId);

    // ✅ Find teacher assignments by status
    List<CourseTeacher> findByTeacherIdAndStatusOrderByAssignedAtDesc(Long teacherId, CourseTeacherStatus status);

    // ✅ Find pending assignments for teacher
    List<CourseTeacher> findByTeacherIdAndStatusAndIsActiveTrue(Long teacherId, CourseTeacherStatus status);

    // ✅ Count active courses for a teacher (status = ACCEPTED)
    @Query("SELECT COUNT(ct) FROM CourseTeacher ct WHERE ct.teacher.id = :teacherId AND ct.isActive = true AND ct.status = 'ACCEPTED'")
    long countActiveCoursesByTeacher(@Param("teacherId") Long teacherId);

    // ✅ Find main instructor for course
    @Query("SELECT ct FROM CourseTeacher ct WHERE ct.course.id = :courseId AND ct.role = 'MAIN_INSTRUCTOR' AND ct.isActive = true AND ct.status = 'ACCEPTED'")
    Optional<CourseTeacher> findMainInstructorForCourse(@Param("courseId") Long courseId);

    // ✅ Count courses grouped by status for teacher
    @Query("SELECT ct.status, COUNT(ct) FROM CourseTeacher ct WHERE ct.teacher.id = :teacherId AND ct.isActive = true GROUP BY ct.status")
    List<Object[]> countByTeacherAndStatusGrouped(@Param("teacherId") Long teacherId);

    // ✅ Find accepted courses with pagination
    @Query("SELECT ct FROM CourseTeacher ct JOIN FETCH ct.course JOIN FETCH ct.course.syllabus " +
           "WHERE ct.teacher.id = :teacherId AND ct.isActive = true AND ct.status = 'ACCEPTED' ORDER BY ct.assignedAt DESC")
    Page<CourseTeacher> findAcceptedCoursesForTeacher(@Param("teacherId") Long teacherId, Pageable pageable);

    // ✅ Find pending assignments for a list of courses (Manager view)
    @Query("SELECT ct FROM CourseTeacher ct JOIN FETCH ct.course JOIN FETCH ct.teacher " +
           "WHERE ct.course.id IN :courseIds AND ct.status = 'PENDING' AND ct.isActive = true ORDER BY ct.assignedAt DESC")
    List<CourseTeacher> findPendingAssignmentsForCourses(@Param("courseIds") List<Long> courseIds);

    // ✅ Soft delete assignment
    @Modifying
    @Query("UPDATE CourseTeacher ct SET ct.isActive = false, ct.removedAt = CURRENT_TIMESTAMP WHERE ct.id = :id")
    void softDeleteAssignment(@Param("id") Long id);
}
