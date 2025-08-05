package com.classroomapp.classroombackend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.entity.ScheduleConflict;
import com.classroomapp.classroombackend.service.hrmanagement.shift.ShiftConflictDetectionService.ConflictType;

@Repository
public interface ScheduleConflictRepository extends JpaRepository<ScheduleConflict, Long> {
    
    List<ScheduleConflict> findByResolvedFalseOrderByDetectedAtDesc();
    
    List<ScheduleConflict> findByResolvedTrueOrderByDetectedAtDesc();
    
    List<ScheduleConflict> findByConflictTypeOrderByDetectedAtDesc(ConflictType conflictType);
    
    @Query("SELECT sc FROM ScheduleConflict sc WHERE sc.classEntity1.id = :classId OR sc.classEntity2.id = :classId")
    List<ScheduleConflict> findByInvolvedClass(@Param("classId") Long classId);
    
    @Query("SELECT sc FROM ScheduleConflict sc WHERE (sc.classEntity1.id = :classId1 AND sc.classEntity2.id = :classId2) OR (sc.classEntity1.id = :classId2 AND sc.classEntity2.id = :classId1)")
    List<ScheduleConflict> findByPairOfClasses(@Param("classId1") Long classId1, @Param("classId2") Long classId2);
    
    @Query("SELECT sc FROM ScheduleConflict sc WHERE sc.detectedAt >= :startDate AND sc.detectedAt <= :endDate")
    List<ScheduleConflict> findByDetectedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find unresolved conflicts involving a specific class
    @Query("SELECT sc FROM ScheduleConflict sc WHERE sc.resolved = false AND (sc.classEntity1.id = :classId OR sc.classEntity2.id = :classId)")
    List<ScheduleConflict> findUnresolvedConflictsForClass(@Param("classId") Long classId);
    
    // Count new conflicts not resolved
    long countByResolvedFalse();
    
    // Count new conflicts by type
    long countByConflictTypeAndResolvedFalse(ConflictType conflictType);
    
    // Delete conflicts involving a specific class
    void deleteByClassEntity1IdOrClassEntity2Id(Long classEntity1Id, Long classEntity2Id);
    
    @Query("SELECT sc FROM ScheduleConflict sc WHERE sc.resolved = false AND (sc.classEntity1.room.id = :roomId OR sc.classEntity2.room.id = :roomId)")
    List<ScheduleConflict> findUnresolvedConflictsByRoom(@Param("roomId") Long roomId);
    
    @Query("SELECT sc FROM ScheduleConflict sc WHERE sc.resolved = false AND (sc.classEntity1.teacher.id = :teacherId OR sc.classEntity2.teacher.id = :teacherId)")
    List<ScheduleConflict> findUnresolvedConflictsByTeacher(@Param("teacherId") Long teacherId);
    
    void deleteByClassEntity1Id(Long classId);
    
    void deleteByClassEntity2Id(Long classId);
}