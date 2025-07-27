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
    List<ClassroomSchedule> findByClassroomIdOrderByDayOfWeekAscStartTimeAsc(Long classroomId);
    
    @Query("SELECT cs FROM ClassroomSchedule cs WHERE cs.classroom.id = :classroomId AND cs.dayOfWeek = :dayOfWeek AND cs.startTime < :endTime AND cs.endTime > :startTime AND cs.id != :excludeId")
    List<ClassroomSchedule> findConflictingSchedulesExcluding(@Param("classroomId") Long classroomId, @Param("dayOfWeek") DayOfWeek dayOfWeek, @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime, @Param("excludeId") Long excludeId);
    
    @Query("SELECT cs FROM ClassroomSchedule cs WHERE cs.classroom.id = :classroomId AND cs.dayOfWeek = :dayOfWeek AND cs.startTime < :endTime AND cs.endTime > :startTime")
    List<ClassroomSchedule> findConflictingSchedules(@Param("classroomId") Long classroomId, @Param("dayOfWeek") DayOfWeek dayOfWeek, @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);
    
    List<ClassroomSchedule> findByDayOfWeekOrderByStartTimeAsc(DayOfWeek dayOfWeek);
    List<ClassroomSchedule> findByLocationContainingIgnoreCaseOrderByDayOfWeekAscStartTimeAsc(String location);
}
