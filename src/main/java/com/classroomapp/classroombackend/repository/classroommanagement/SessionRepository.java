package com.classroomapp.classroombackend.repository.classroommanagement;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.classroommanagement.Session;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long>, JpaSpecificationExecutor<Session> {

    // Methods using classroom relationship
    @Query("SELECT s FROM Session s WHERE s.classroom.id = :classroomId")
    List<Session> findByClassroomId(@Param("classroomId") Long classroomId);

    @Query("SELECT s FROM Session s WHERE s.classroom.id = :classroomId")
    Page<Session> findByClassroomId(@Param("classroomId") Long classroomId, Pageable pageable);

    @Query("SELECT s FROM Session s WHERE s.classroom.id = :classroomId AND s.status = :status")
    List<Session> findByClassroomIdAndStatus(@Param("classroomId") Long classroomId, @Param("status") Session.SessionStatus status);

    List<Session> findBySessionDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT s FROM Session s WHERE s.classroom.id = :classroomId AND s.sessionDate BETWEEN :startDate AND :endDate")
    List<Session> findByClassroomIdAndSessionDateBetween(@Param("classroomId") Long classroomId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM Session s WHERE s.classroom.id = :classroomId AND s.sessionDate = :sessionDate")
    Optional<Session> findByClassroomIdAndSessionDate(@Param("classroomId") Long classroomId, @Param("sessionDate") LocalDate sessionDate);

    @Query("SELECT COUNT(s) > 0 FROM Session s WHERE s.classroom.id = :classroomId AND s.sessionDate = :sessionDate")
    boolean existsByClassroomIdAndSessionDate(@Param("classroomId") Long classroomId, @Param("sessionDate") LocalDate sessionDate);

    List<Session> findByStatus(Session.SessionStatus status);

    Page<Session> findByStatus(Session.SessionStatus status, Pageable pageable);

    @Query("SELECT s FROM Session s WHERE s.status = 'UPCOMING' AND s.sessionDate >= :today ORDER BY s.sessionDate ASC")
    List<Session> findUpcomingSessions(@Param("today") LocalDate today);

    @Query("SELECT s FROM Session s WHERE s.sessionDate < :today ORDER BY s.sessionDate DESC")
    List<Session> findPastSessions(@Param("today") LocalDate today);

    @Query("SELECT s FROM Session s WHERE s.sessionDate = :today ORDER BY s.createdAt ASC")
    List<Session> findTodaySessions(@Param("today") LocalDate today);

    @Query("SELECT s, COUNT(sl) as slotCount FROM Session s LEFT JOIN s.slots sl WHERE s.classroom.id = :classroomId GROUP BY s ORDER BY s.sessionDate ASC")
    List<Object[]> findSessionsWithSlotCount(@Param("classroomId") Long classroomId);

    @Query("SELECT DISTINCT s FROM Session s LEFT JOIN FETCH s.slots WHERE s.classroom.id = :classroomId ORDER BY s.sessionDate ASC")
    List<Session> findByClassroomIdWithSlots(@Param("classroomId") Long classroomId);

    @Query("SELECT s FROM Session s LEFT JOIN FETCH s.slots LEFT JOIN FETCH s.classroom WHERE s.id = :sessionId")
    Optional<Session> findByIdWithSlotsAndClassroom(@Param("sessionId") Long sessionId);

    long countByClassroom_Id(Long classroomId);

    long countByClassroom_IdAndStatus(Long classroomId, Session.SessionStatus status);

    @Query("SELECT s FROM Session s WHERE s.sessionDate < :today AND s.status IN ('UPCOMING', 'IN_PROGRESS')")
    List<Session> findSessionsNeedingStatusUpdate(@Param("today") LocalDate today);

    @Query("SELECT s FROM Session s WHERE s.classroom.teacher.id = :teacherId ORDER BY s.sessionDate DESC")
    List<Session> findByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT s FROM Session s WHERE s.classroom.teacher.id = :teacherId ORDER BY s.sessionDate DESC")
    Page<Session> findByTeacherId(@Param("teacherId") Long teacherId, Pageable pageable);

    @Query("SELECT s FROM Session s JOIN s.classroom.enrollments e WHERE e.user.id = :studentId ORDER BY s.sessionDate DESC")
    List<Session> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT s FROM Session s JOIN s.classroom.enrollments e WHERE e.user.id = :studentId ORDER BY s.sessionDate DESC")
    Page<Session> findByStudentId(@Param("studentId") Long studentId, Pageable pageable);

    @Query("SELECT s FROM Session s WHERE LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY s.sessionDate DESC")
    List<Session> searchByDescription(@Param("keyword") String keyword);

    @Query("SELECT s FROM Session s WHERE s.classroom.id IN :classroomIds ORDER BY s.sessionDate DESC")
    List<Session> findByClassroomIds(@Param("classroomIds") List<Long> classroomIds);

    @Query("SELECT " +
           "COUNT(s) as totalSessions, " +
           "COUNT(CASE WHEN s.status = 'COMPLETED' THEN 1 END) as completedSessions, " +
           "COUNT(CASE WHEN s.status = 'UPCOMING' THEN 1 END) as upcomingSessions, " +
           "COUNT(CASE WHEN s.status = 'IN_PROGRESS' THEN 1 END) as inProgressSessions " +
           "FROM Session s WHERE s.classroom.id = :classroomId")
    Object[] getSessionStatistics(@Param("classroomId") Long classroomId);

    void deleteByClassroom_Id(Long classroomId);

    @Query("SELECT s FROM Session s WHERE s.status = 'UPCOMING' AND s.slots IS EMPTY")
    List<Session> findDeletableSessions();

    @Query("SELECT s FROM Session s WHERE s.sessionDate BETWEEN :startDate AND :endDate AND s.status = :status ORDER BY s.sessionDate ASC")
    List<Session> findByDateRangeAndStatus(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          @Param("status") Session.SessionStatus status);

    @Query("SELECT s FROM Session s JOIN s.slots sl WHERE sl.status != 'DONE' GROUP BY s HAVING COUNT(sl) > 0")
    List<Session> findSessionsWithIncompleteSlots();

    @Query("SELECT s FROM Session s WHERE s.classroom.id = :classroomId ORDER BY s.sessionDate DESC")
    List<Session> findByClassroomIdOrderBySessionDateDesc(@Param("classroomId") Long classroomId);

    List<Session> findBySessionDateBetweenOrderBySessionDateAsc(LocalDate startDate, LocalDate endDate);

    @Query("SELECT s FROM Session s WHERE s.status = :status ORDER BY s.sessionDate DESC")
    List<Session> findByStatusOrderBySessionDateDesc(@Param("status") Session.SessionStatus status);

    @Query("SELECT COUNT(s) FROM Session s WHERE s.classroom.id = :classroomId")
    long countByClassroomId(@Param("classroomId") Long classroomId);

    @Query("SELECT COUNT(s) FROM Session s WHERE s.status = :status")
    long countByStatus(@Param("status") Session.SessionStatus status);
}
