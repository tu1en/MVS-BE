package com.classroomapp.classroombackend.repository.classroommanagement;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomSchedule;

@Repository
public interface ClassroomScheduleRepository extends JpaRepository<ClassroomSchedule, Long> {
    
    List<ClassroomSchedule> findByClassroomId(Long classroomId);
    List<ClassroomSchedule> findByClassroom(Classroom classroom);
    
    /**
     * Find schedules by classroom ID ordered by day and start time
     */
    List<ClassroomSchedule> findByClassroomIdOrderByDayOfWeekAscStartTimeAsc(Long classroomId);
    
    /**
     * Find schedules by day of week ordered by start time
     */
    List<ClassroomSchedule> findByDayOfWeekOrderByStartTimeAsc(DayOfWeek dayOfWeek);
    
    /**
     * Find schedules by location containing keyword (case insensitive)
     */
    List<ClassroomSchedule> findByLocationContainingIgnoreCaseOrderByDayOfWeekAscStartTimeAsc(String location);
    
    /**
     * Find conflicting schedules for a classroom on a specific day and time range
     */
    @Query("SELECT s FROM ClassroomSchedule s WHERE s.classroom.id = :classroomId " +
           "AND s.dayOfWeek = :dayOfWeek " +
           "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<ClassroomSchedule> findConflictingSchedules(
            @Param("classroomId") Long classroomId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
    
    /**
     * Find conflicting schedules excluding a specific schedule ID
     */
    @Query("SELECT s FROM ClassroomSchedule s WHERE s.classroom.id = :classroomId " +
           "AND s.dayOfWeek = :dayOfWeek " +
           "AND s.id != :excludeId " +
           "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<ClassroomSchedule> findConflictingSchedulesExcluding(
            @Param("classroomId") Long classroomId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);
}
