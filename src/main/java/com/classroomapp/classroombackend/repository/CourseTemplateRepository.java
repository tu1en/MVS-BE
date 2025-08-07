package com.classroomapp.classroombackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.classroommanagement.CourseTemplate;

@Repository
public interface CourseTemplateRepository extends JpaRepository<CourseTemplate, Long> {
    
    List<CourseTemplate> findByIsActiveTrueOrderByCreatedAtDesc();
    
    Optional<CourseTemplate> findByIdAndIsActiveTrue(Long id);
    
    List<CourseTemplate> findBySubjectContainingIgnoreCaseAndIsActiveTrue(String subject);
    
    List<CourseTemplate> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
    
    List<CourseTemplate> findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(Long createdBy);
    
    @Query("SELECT ct FROM CourseTemplate ct WHERE ct.name LIKE CONCAT('%', :keyword, '%') OR ct.subject LIKE CONCAT('%', :keyword, '%')")
    List<CourseTemplate> searchByNameOrSubject(@Param("keyword") String keyword);
    
    @Query("SELECT ct FROM CourseTemplate ct ORDER BY ct.createdAt DESC")
    List<CourseTemplate> findAllOrderByCreatedAtDesc();
    
    long countByIsActiveTrue();
    
    long countByCreatedByAndIsActiveTrue(Long createdBy);
    
    // Methods for teacher course templates
    
    // Methods for public course templates
    List<CourseTemplate> findByIsPublicTrueAndIsActiveTrueOrderByCreatedAtDesc();
    
    Optional<CourseTemplate> findByIdAndIsPublicTrueAndIsActiveTrue(Long id);
}