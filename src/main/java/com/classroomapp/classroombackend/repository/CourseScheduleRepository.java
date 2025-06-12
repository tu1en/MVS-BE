package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.CourseSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Long> {
    
    /**
     * Find all schedule entries for a classroom
     */
    List<CourseSchedule> findByClassroomIdOrderByStartTime(Long classroomId);
    
    /**
     * Find schedule entries between dates
     */
    @Query("SELECT cs FROM CourseSchedule cs WHERE cs.classroomId = :classroomId " +
           "AND cs.startTime >= :startDate AND cs.endTime <= :endDate " +
           "ORDER BY cs.startTime")
    List<CourseSchedule> findByClassroomIdAndDateRange(@Param("classroomId") Long classroomId,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find upcoming schedule entries
     */
    @Query("SELECT cs FROM CourseSchedule cs WHERE cs.classroomId = :classroomId " +
           "AND cs.startTime > :currentTime ORDER BY cs.startTime")
    List<CourseSchedule> findUpcomingByClassroom(@Param("classroomId") Long classroomId,
                                               @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find schedule by type
     */
    List<CourseSchedule> findByClassroomIdAndScheduleTypeOrderByStartTime(
            Long classroomId, CourseSchedule.ScheduleType scheduleType);
    
    /**
     * Find recurring schedules
     */
    List<CourseSchedule> findByClassroomIdAndRecurringTypeIsNotNullOrderByStartTime(Long classroomId);
    
    /**
     * Find online classes
     */
    List<CourseSchedule> findByClassroomIdAndIsOnlineTrueOrderByStartTime(Long classroomId);
}
