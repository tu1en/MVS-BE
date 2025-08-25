package com.classroomapp.classroombackend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.entity.EnrollmentRequest;
import com.classroomapp.classroombackend.entity.EnrollmentRequest.EnrollmentStatus;
import com.classroomapp.classroombackend.model.classroommanagement.CourseTemplate;
import com.classroomapp.classroombackend.model.usermanagement.User;

@Repository
public interface EnrollmentRequestRepository extends JpaRepository<EnrollmentRequest, Long> {
    
    List<EnrollmentRequest> findByStatusOrderByCreatedAtDesc(EnrollmentStatus status);
    
    List<EnrollmentRequest> findByStudentOrderByCreatedAtDesc(User student);
    
    boolean existsByStudentAndCourseTemplate(User student, CourseTemplate courseTemplate);
    
    @Query("SELECT er FROM EnrollmentRequest er WHERE er.status = :status AND er.createdAt >= :since")
    List<EnrollmentRequest> findRecentRequests(@Param("status") EnrollmentStatus status, 
                                             @Param("since") LocalDateTime since);
    
    List<EnrollmentRequest> findByCourseTemplateAndStatus(CourseTemplate courseTemplate, EnrollmentStatus status);
    
    @Query("SELECT COUNT(er) FROM EnrollmentRequest er WHERE er.courseTemplate = :courseTemplate AND er.status = :status")
    Long countByCourseTemplateAndStatus(@Param("courseTemplate") CourseTemplate courseTemplate, 
                                       @Param("status") EnrollmentStatus status);
    
    @Query("SELECT er FROM EnrollmentRequest er WHERE er.student.id = :studentId")
    List<EnrollmentRequest> findByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT er FROM EnrollmentRequest er WHERE er.courseTemplate.id = :courseTemplateId")
    List<EnrollmentRequest> findByCourseTemplateId(@Param("courseTemplateId") Long courseTemplateId);

    @Query("SELECT er FROM EnrollmentRequest er WHERE er.courseTemplate.id = :courseTemplateId AND er.status = :status")
    List<EnrollmentRequest> findByCourseTemplateIdAndStatus(@Param("courseTemplateId") Long courseTemplateId,
                                                           @Param("status") EnrollmentStatus status);
}