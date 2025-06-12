package com.classroomapp.classroombackend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.Classroom;
import com.classroomapp.classroombackend.model.CourseFeedback;
import com.classroomapp.classroombackend.model.User;

@Repository
public interface CourseFeedbackRepository extends JpaRepository<CourseFeedback, Long> {
    
    boolean existsByStudentAndClassroom(User student, Classroom classroom);
    
    List<CourseFeedback> findByStudentOrderByCreatedAtDesc(User student);
    
    List<CourseFeedback> findByClassroomOrderByCreatedAtDesc(Classroom classroom);
    
    List<CourseFeedback> findByTeacherOrderByCreatedAtDesc(User teacher);
    
    List<CourseFeedback> findByStatusOrderByCreatedAtDesc(String status);
    
    List<CourseFeedback> findByCategoryOrderByCreatedAtDesc(String category);
    
    List<CourseFeedback> findByOverallRatingBetweenOrderByCreatedAtDesc(Integer minRating, Integer maxRating);
    
    List<CourseFeedback> findByIsAnonymousTrueOrderByCreatedAtDesc();
    
    @Query("SELECT cf FROM CourseFeedback cf WHERE cf.title LIKE %:keyword% OR cf.content LIKE %:keyword% ORDER BY cf.createdAt DESC")
    List<CourseFeedback> searchFeedback(@Param("keyword") String keyword);
    
    @Query("SELECT cf FROM CourseFeedback cf WHERE cf.createdAt >= :fromDate ORDER BY cf.createdAt DESC")
    List<CourseFeedback> findRecentFeedback(@Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT AVG(cf.overallRating) FROM CourseFeedback cf WHERE cf.classroom = :classroom")
    Double getAverageRatingByClassroom(@Param("classroom") Classroom classroom);
    
    @Query("SELECT AVG(cf.teachingQualityRating) FROM CourseFeedback cf WHERE cf.teacher = :teacher")
    Double getAverageTeachingQualityByTeacher(@Param("teacher") User teacher);
    
    @Query("SELECT COUNT(cf) FROM CourseFeedback cf WHERE cf.teacher = :teacher AND cf.status = :status")
    Long countFeedbackByTeacherAndStatus(@Param("teacher") User teacher, @Param("status") String status);
}
