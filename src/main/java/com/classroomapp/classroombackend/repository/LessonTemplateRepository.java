package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.entity.LessonTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonTemplateRepository extends JpaRepository<LessonTemplate, Long> {
    
    List<LessonTemplate> findByCourseTemplateIdOrderBySortOrderAsc(Long courseTemplateId);
    
    List<LessonTemplate> findByCourseTemplateIdOrderByWeekNumberAscSortOrderAsc(Long courseTemplateId);
    
    List<LessonTemplate> findByCourseTemplateIdAndWeekNumberOrderBySortOrderAsc(Long courseTemplateId, Integer weekNumber);
    
    @Query("SELECT COALESCE(MAX(lt.weekNumber), 0) FROM LessonTemplate lt WHERE lt.courseTemplate.id = :courseTemplateId")
    Integer findMaxWeekNumberByCourseTemplateId(@Param("courseTemplateId") Long courseTemplateId);
    
    long countByCourseTemplateId(Long courseTemplateId);
    
    List<LessonTemplate> findByCourseTemplateId(Long courseTemplateId);
    
    @Query("SELECT lt FROM LessonTemplate lt WHERE lt.courseTemplate.id = :courseTemplateId AND lt.weekNumber BETWEEN :startWeek AND :endWeek")
    List<LessonTemplate> findByCourseTemplateIdAndWeekRange(
        @Param("courseTemplateId") Long courseTemplateId,
        @Param("startWeek") Integer startWeek,
        @Param("endWeek") Integer endWeek
    );
    
    void deleteByCourseTemplateId(Long courseTemplateId);
    
    boolean existsByCourseTemplateId(Long courseTemplateId);
}