package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.entity.ClassLesson;
import com.classroomapp.classroombackend.entity.ClassLesson.LessonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClassLessonRepository extends JpaRepository<ClassLesson, Long> {
    
    List<ClassLesson> findByClassEntityIdOrderByActualDateAsc(Long classId);
    
    List<ClassLesson> findByClassEntityIdAndStatusOrderByActualDateAsc(Long classId, LessonStatus status);
    
    List<ClassLesson> findByLessonTemplateId(Long lessonTemplateId);
    
    List<ClassLesson> findByClassEntityIdAndActualDateBetweenOrderByActualDateAsc(
        Long classId, 
        LocalDate startDate, 
        LocalDate endDate
    );
    
    List<ClassLesson> findByActualDateBetweenOrderByActualDateAsc(LocalDate startDate, LocalDate endDate);
    
    long countByClassEntityId(Long classId);
    
    long countByClassEntityIdAndStatus(Long classId, LessonStatus status);
    
    @Query("SELECT cl FROM ClassLesson cl WHERE cl.classEntity.id = :classId AND DATE(cl.actualDate) = :date ORDER BY cl.lessonTemplate.sortOrder ASC")
    List<ClassLesson> findByClassIdAndDateOrderByLessonOrder(
        @Param("classId") Long classId, 
        @Param("date") LocalDate date
    );
    
    @Query("SELECT cl FROM ClassLesson cl WHERE cl.classEntity.id IN (SELECT c.id FROM ClassEntity c WHERE c.teacher.id = :teacherId)")
    List<ClassLesson> findByTeacherId(@Param("teacherId") Long teacherId);
    
    @Query("SELECT cl FROM ClassLesson cl WHERE cl.classEntity.id IN (SELECT c.id FROM ClassEntity c WHERE c.teacher.id = :teacherId) AND cl.actualDate IS NOT NULL ORDER BY cl.actualDate ASC")
    List<ClassLesson> findByTeacherIdOrderByActualDateAsc(@Param("teacherId") Long teacherId);
    
    List<ClassLesson> findByStatusOrderByActualDateAsc(LessonStatus status);
    
    @Query("SELECT cl FROM ClassLesson cl WHERE cl.actualDate IS NULL AND cl.classEntity.status = 'ACTIVE'")
    List<ClassLesson> findUnscheduledLessonsInActiveClasses();
    
    @Query("SELECT cl FROM ClassLesson cl WHERE cl.actualDate >= :startDate AND cl.actualDate <= :endDate AND cl.status = 'SCHEDULED'")
    List<ClassLesson> findScheduledLessonsInDateRange(
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
    
    List<ClassLesson> findByClassEntity_IdIsNull();
    
    @Query(value = "SELECT * FROM class_lessons WHERE attendance_count > 0 ORDER BY actual_date DESC", nativeQuery = true)
    List<ClassLesson> findClassesWithAttendance();
    
    void deleteByClassEntityId(Long classId);
    
    void deleteByLessonTemplateId(Long lessonTemplateId);
    
    @Query("SELECT cl FROM ClassLesson cl JOIN cl.classEntity c WHERE c.status = 'ACTIVE' AND cl.actualDate IS NULL")
    List<ClassLesson> findUnscheduledLessonsForActiveClasses();
    
    long countByLessonTemplateIdIn(List<Long> lessonTemplateIds);
}