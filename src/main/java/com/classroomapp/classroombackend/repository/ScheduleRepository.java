package com.classroomapp.classroombackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // ==================== NATIVE QUERIES (MOST RELIABLE) ====================
    
    /**
     * Native SQL query - bypasses entity relations completely
     */
    @Query(value = "SELECT * FROM schedules s " +
                   "WHERE s.teacher_id = :teacherId " +
                   "AND s.start_datetime >= :startDateTime " +
                   "AND s.start_datetime < :endDateTime " +
                   "ORDER BY s.start_datetime ASC", 
           nativeQuery = true)
    List<Schedule> findByTeacherIdAndDateRangeNative(
        @Param("teacherId") Long teacherId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query(value = "SELECT * FROM schedules WHERE teacher_id = :teacherId ORDER BY start_datetime ASC", 
           nativeQuery = true)
    List<Schedule> findByTeacherIdNative(@Param("teacherId") Long teacherId);

    @Query(value = "SELECT * FROM schedules s " +
                   "WHERE s.teacher_id = :teacherId " +
                   "AND s.start_datetime >= :startDateTime " +
                   "AND s.start_datetime < :endDateTime " +
                   "ORDER BY s.start_datetime ASC", 
           nativeQuery = true)
    List<Schedule> findTodaySchedulesByTeacherIdNative(
        @Param("teacherId") Long teacherId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query(value = "SELECT * FROM schedules s " +
                   "WHERE s.teacher_id = :teacherId " +
                   "AND s.start_datetime > :now " +
                   "ORDER BY s.start_datetime ASC", 
           nativeQuery = true)
    List<Schedule> findUpcomingSchedulesByTeacherIdNative(
        @Param("teacherId") Long teacherId,
        @Param("now") LocalDateTime now
    );

    @Query(value = "SELECT COUNT(*) FROM schedules " +
                   "WHERE teacher_id = :teacherId " +
                   "AND start_datetime >= :startDateTime " +
                   "AND start_datetime < :endDateTime", 
           nativeQuery = true)
    long countByTeacherIdAndDateRangeNative(
        @Param("teacherId") Long teacherId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime
    );

    // ==================== JPQL WITH FETCH JOIN (BACKUP) ====================

    /**
     * JPQL with FETCH JOIN to avoid lazy loading issues
     */
    @Query("SELECT s FROM Schedule s " +
           "LEFT JOIN FETCH s.teacher t " +
           "LEFT JOIN FETCH s.classroom c " +
           "WHERE s.teacher.id = :teacherId " +
           "AND s.startDatetime >= :startDateTime " +
           "AND s.startDatetime < :endDateTime " +
           "ORDER BY s.startDatetime ASC")
    List<Schedule> findByTeacherIdAndDateRangeWithFetch(
        @Param("teacherId") Long teacherId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("SELECT s FROM Schedule s LEFT JOIN FETCH s.teacher LEFT JOIN FETCH s.classroom WHERE s.teacher.id = :teacherId ORDER BY s.startDatetime ASC")
    List<Schedule> findByTeacherIdWithFetch(@Param("teacherId") Long teacherId);

    // ==================== CLASSROOM QUERIES ====================

    @Query(value = "SELECT * FROM schedules s " +
                   "WHERE s.classroom_id = :classroomId " +
                   "AND s.start_datetime >= :startDateTime " +
                   "AND s.start_datetime < :endDateTime " +
                   "ORDER BY s.start_datetime ASC", 
           nativeQuery = true)
    List<Schedule> findByClassroomIdAndDateRangeNative(
        @Param("classroomId") Long classroomId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query(value = "SELECT * FROM schedules s " +
                   "WHERE s.classroom_id IN (:classroomIds) " +
                   "AND s.start_datetime >= :startDateTime " +
                   "AND s.start_datetime < :endDateTime " +
                   "ORDER BY s.start_datetime ASC", 
           nativeQuery = true)
    List<Schedule> findByClassroomIdsAndDateRangeNative(
        @Param("classroomIds") List<Long> classroomIds,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime
    );

    // ==================== DEBUG & UTILITY QUERIES ====================

    @Query("SELECT s FROM Schedule s LEFT JOIN FETCH s.teacher LEFT JOIN FETCH s.classroom WHERE s.id = :id")
    Optional<Schedule> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT s FROM Schedule s LEFT JOIN FETCH s.teacher LEFT JOIN FETCH s.classroom ORDER BY s.startDatetime ASC")
    List<Schedule> findAllWithDetails();

    // ==================== LEGACY JPQL QUERIES (KEEP FOR COMPATIBILITY) ====================

    @Query("SELECT s FROM Schedule s " +
           "WHERE s.teacher.id = :teacherId " +
           "AND s.startDatetime >= :startDateTime " +
           "AND s.startDatetime < :endDateTime " +
           "ORDER BY s.startDatetime ASC")
    List<Schedule> findByTeacherIdAndDateRange(
        @Param("teacherId") Long teacherId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("SELECT s FROM Schedule s WHERE s.teacher.id = :teacherId ORDER BY s.startDatetime ASC")
    List<Schedule> findByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT s FROM Schedule s " +
           "WHERE s.classroom.id = :classroomId " +
           "AND s.startDatetime >= :startDateTime " +
           "AND s.startDatetime < :endDateTime " +
           "ORDER BY s.startDatetime ASC")
    List<Schedule> findByClassroomIdAndDateRange(
        @Param("classroomId") Long classroomId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("SELECT s FROM Schedule s " +
           "WHERE s.classroom.id IN :classroomIds " +
           "AND s.startDatetime >= :startDateTime " +
           "AND s.startDatetime < :endDateTime " +
           "ORDER BY s.startDatetime ASC")
    List<Schedule> findByClassroomIdsAndDateRange(
        @Param("classroomIds") List<Long> classroomIds,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("SELECT s FROM Schedule s " +
           "WHERE s.teacher.id = :teacherId " +
           "AND s.startDatetime >= :startDateTime " +
           "AND s.startDatetime < :endDateTime " +
           "ORDER BY s.startDatetime ASC")
    List<Schedule> findTodaySchedulesByTeacherId(
        @Param("teacherId") Long teacherId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("SELECT s FROM Schedule s " +
           "WHERE s.teacher.id = :teacherId " +
           "AND s.startDatetime > :now " +
           "ORDER BY s.startDatetime ASC")
    List<Schedule> findUpcomingSchedulesByTeacherId(
        @Param("teacherId") Long teacherId,
        @Param("now") LocalDateTime now
    );

    @Query("SELECT COUNT(s) FROM Schedule s " +
           "WHERE s.teacher.id = :teacherId " +
           "AND s.startDatetime >= :startDateTime " +
           "AND s.startDatetime < :endDateTime")
    long countByTeacherIdAndDateRange(
        @Param("teacherId") Long teacherId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("SELECT s FROM Schedule s WHERE s.classroom.id = :classroomId ORDER BY s.startDatetime ASC")
    List<Schedule> findByClassroomId(@Param("classroomId") Long classroomId);
}