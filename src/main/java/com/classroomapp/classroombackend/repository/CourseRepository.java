package com.classroomapp.classroombackend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.entity.Course;
import com.classroomapp.classroombackend.model.classroommanagement.Syllabus;
import com.classroomapp.classroombackend.entity.enumeration.CourseStatus;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // Find active courses (not deleted)
    List<Course> findByIsDeletedFalseOrderByCreatedAtDesc();

    // Find by status
    List<Course> findByStatusAndIsDeletedFalse(CourseStatus status);

    // Find by syllabus
    List<Course> findBySyllabusIdAndIsDeletedFalse(Long syllabusId);

    // Find by creator
    List<Course> findByCreatedByIdAndIsDeletedFalseOrderByCreatedAtDesc(Long createdBy);

    // Find by subject and status (with Vietnamese support)
    @Query("SELECT c FROM Course c WHERE " +
           "c.syllabus.subject LIKE LOWER(CONCAT('%', :subject, '%')) AND " +
           "c.isDeleted = false AND " +
           "(:status IS NULL OR c.status = :status)")
    List<Course> findBySubjectAndStatus(@Param("subject") String subject,@Param("status") CourseStatus status);

    // Vietnamese search
    @Query("SELECT c FROM Course c WHERE " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.syllabus.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.syllabus.subject) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "c.isDeleted = false")
    List<Course> searchByKeyword(@Param("keyword") String keyword);

    // Vietnamese search with filters
    @Query("SELECT c FROM Course c WHERE " +
           "(:keyword IS NULL OR " +
           "   LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "   LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "   LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "   LOWER(c.syllabus.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "   LOWER(c.syllabus.subject) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:syllabusName IS NULL OR LOWER(c.syllabus.title) LIKE LOWER(CONCAT('%', :syllabusName, '%'))) AND " +
           "(:subject IS NULL OR LOWER(c.syllabus.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:startDate IS NULL OR c.startDate >= :startDate) AND " +
           "(:endDate IS NULL OR c.endDate <= :endDate) AND " +
           "c.isDeleted = false")
    Page<Course> searchCourses(
            @Param("keyword") String keyword,
            @Param("syllabusName") String syllabusName,
            @Param("subject") String subject,
            @Param("status") CourseStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    // Find courses by date range
    List<Course> findByStartDateBetweenAndIsDeletedFalse(LocalDate startDate, LocalDate endDate);

    // Find courses starting after date
    List<Course> findByStartDateAfterAndIsDeletedFalseOrderByStartDateAsc(LocalDate startDate);

    // Find courses without teachers
    @Query("SELECT c FROM Course c WHERE c.id NOT IN " +
           "(SELECT ct.course.id FROM CourseTeacher ct WHERE ct.isActive = true AND ct.status = 'ACCEPTED')" +
           "AND c.isDeleted = false AND c.status = 'ACTIVE'")
    Page<Course> findCoursesWithoutTeacher(Pageable pageable);

    // Find active courses with count
    @Query("SELECT COUNT(c) FROM Course c WHERE c.isDeleted = false AND c.status = 'ACTIVE'")
    long countActiveCourses();

    // Check if course exists by code
    boolean existsByCodeIgnoreCaseAndIsDeletedFalse(String code);

    // Find by code
    Optional<Course> findByCodeIgnoreCaseAndIsDeletedFalse(String code);

    // Find courses by status and syllabus
    List<Course> findBySyllabusAndStatusAndIsDeletedFalse(Syllabus syllabus, CourseStatus status);

    // Count by syllabus
    long countBySyllabusIdAndIsDeletedFalse(Long syllabusId);

    // Find recent courses
    List<Course> findTop10ByIsDeletedFalseOrderByCreatedAtDesc();
}