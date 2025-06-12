package com.classroomapp.classroombackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.StudentQuestion;
import com.classroomapp.classroombackend.model.User;

@Repository
public interface StudentQuestionRepository extends JpaRepository<StudentQuestion, Long> {
    
    List<StudentQuestion> findByStudentOrderByCreatedAtDesc(User student);
    
    List<StudentQuestion> findByTeacherOrderByCreatedAtDesc(User teacher);
    
    List<StudentQuestion> findByStatusOrderByCreatedAtDesc(String status);
    
    List<StudentQuestion> findByPriorityOrderByCreatedAtDesc(String priority);
    
    List<StudentQuestion> findByStudentAndStatusOrderByCreatedAtDesc(User student, String status);
    
    List<StudentQuestion> findByTeacherAndStatusOrderByCreatedAtDesc(User teacher, String status);
    
    @Query("SELECT sq FROM StudentQuestion sq WHERE sq.subject LIKE %:keyword% OR sq.content LIKE %:keyword% ORDER BY sq.createdAt DESC")
    List<StudentQuestion> findByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT sq FROM StudentQuestion sq WHERE sq.subject LIKE %:keyword% OR sq.content LIKE %:keyword% ORDER BY sq.createdAt DESC")
    List<StudentQuestion> searchQuestions(@Param("keyword") String keyword);
    
    @Query("SELECT sq FROM StudentQuestion sq ORDER BY sq.createdAt DESC")
    List<StudentQuestion> findRecentQuestions();
    
    List<StudentQuestion> findByStudentAndPriorityOrderByCreatedAtDesc(User student, String priority);
    
    List<StudentQuestion> findByStudentAndTeacherOrderByCreatedAtDesc(User student, User teacher);
    
    @Query("SELECT COUNT(sq) FROM StudentQuestion sq WHERE sq.teacher = :teacher AND sq.status = 'PENDING'")
    Long countPendingQuestionsByTeacher(@Param("teacher") User teacher);
}
