package com.classroomapp.classroombackend.repository.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho ShiftSchedule entity
 * Cung cáº¥p cÃ¡c query methods cho quáº£n lÃ½ shift schedules
 */
@Repository
public interface ShiftScheduleRepository extends JpaRepository<ShiftSchedule, Long> {

    /**
     * TÃ¬m schedules theo tráº¡ng thÃ¡i
     */
    List<ShiftSchedule> findByStatusOrderByCreatedAtDesc(ShiftSchedule.ScheduleStatus status);

    /**
     * TÃ¬m schedules theo loáº¡i
     */
    List<ShiftSchedule> findByScheduleTypeOrderByStartDateDesc(ShiftSchedule.ScheduleType scheduleType);

    /**
     * TÃ¬m schedules Ä‘ang hoáº¡t Ä‘á»™ng (published vÃ  trong thá»i gian)
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "ss.status = 'PUBLISHED' AND " +
           "ss.startDate <= :currentDate AND ss.endDate >= :currentDate " +
           "ORDER BY ss.startDate ASC")
    List<ShiftSchedule> findActiveSchedules(@Param("currentDate") LocalDate currentDate);

    /**
     * TÃ¬m schedule Ä‘ang hoáº¡t Ä‘á»™ng cho ngÃ y cá»¥ thá»ƒ
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "ss.status = 'PUBLISHED' AND " +
           "ss.startDate <= :date AND ss.endDate >= :date " +
           "ORDER BY ss.createdAt DESC")
    Optional<ShiftSchedule> findActiveScheduleForDate(@Param("date") LocalDate date);

    /**
     * TÃ¬m schedules theo ngÆ°á»i táº¡o
     */
    List<ShiftSchedule> findByCreatedByIdOrderByCreatedAtDesc(Long createdById);

    /**
     * TÃ¬m schedules theo ngÆ°á»i xuáº¥t báº£n
     */
    List<ShiftSchedule> findByPublishedByIdOrderByPublishedAtDesc(Long publishedById);

    /**
     * TÃ¬m schedules trong khoáº£ng thá»i gian
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "ss.startDate <= :endDate AND ss.endDate >= :startDate " +
           "ORDER BY ss.startDate ASC")
    List<ShiftSchedule> findByDateRange(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    /**
     * TÃ¬m schedules cÃ³ overlap vá»›i khoáº£ng thá»i gian
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "ss.status IN ('DRAFT', 'PUBLISHED') AND " +
           "ss.startDate <= :endDate AND ss.endDate >= :startDate AND " +
           "(:excludeId IS NULL OR ss.id != :excludeId) " +
           "ORDER BY ss.startDate ASC")
    List<ShiftSchedule> findOverlappingSchedules(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate,
                                                @Param("excludeId") Long excludeId);

    /**
     * Search schedules vá»›i filters
     */
    @Query("SELECT ss FROM ShiftSchedule ss " +
           "LEFT JOIN ss.createdBy cb " +
           "LEFT JOIN ss.publishedBy pb WHERE " +
           "(:status IS NULL OR ss.status = :status) AND " +
           "(:scheduleType IS NULL OR ss.scheduleType = :scheduleType) AND " +
           "(:createdById IS NULL OR ss.createdBy.id = :createdById) AND " +
           "(:startDate IS NULL OR ss.startDate >= :startDate) AND " +
           "(:endDate IS NULL OR ss.endDate <= :endDate) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(ss.scheduleName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(ss.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(cb.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY ss.startDate DESC, ss.createdAt DESC")
    Page<ShiftSchedule> searchSchedules(@Param("status") ShiftSchedule.ScheduleStatus status,
                                       @Param("scheduleType") ShiftSchedule.ScheduleType scheduleType,
                                       @Param("createdById") Long createdById,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate,
                                       @Param("search") String search,
                                       Pageable pageable);

    /**
     * TÃ¬m schedules cáº§n archive (Ä‘Ã£ káº¿t thÃºc > 30 ngÃ y)
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "ss.status = 'PUBLISHED' AND " +
           "ss.endDate < :cutoffDate " +
           "ORDER BY ss.endDate ASC")
    List<ShiftSchedule> findSchedulesNeedingArchive(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * TÃ¬m schedules sáº¯p báº¯t Ä‘áº§u (Ä‘á»ƒ gá»­i notification)
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "ss.status = 'PUBLISHED' AND " +
           "ss.startDate BETWEEN :today AND :notificationDate " +
           "ORDER BY ss.startDate ASC")
    List<ShiftSchedule> findUpcomingSchedules(@Param("today") LocalDate today,
                                             @Param("notificationDate") LocalDate notificationDate);

    /**
     * Äáº¿m schedules theo tráº¡ng thÃ¡i
     */
    @Query("SELECT ss.status, COUNT(ss) FROM ShiftSchedule ss " +
           "GROUP BY ss.status")
    List<Object[]> countByStatus();

    /**
     * Äáº¿m schedules theo loáº¡i
     */
    @Query("SELECT ss.scheduleType, COUNT(ss) FROM ShiftSchedule ss " +
           "GROUP BY ss.scheduleType")
    List<Object[]> countByType();

    /**
     * TÃ­nh tá»•ng assignments trong schedules
     */
    @Query("SELECT " +
           "COUNT(ss) as totalSchedules, " +
           "SUM(ss.totalAssignments) as totalAssignments, " +
           "AVG(ss.totalAssignments) as avgAssignments " +
           "FROM ShiftSchedule ss WHERE " +
           "ss.status = 'PUBLISHED' AND " +
           "ss.startDate BETWEEN :startDate AND :endDate")
    Object[] getScheduleStatistics(@Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    /**
     * TÃ¬m schedules cÃ³ nhiá»u assignments nháº¥t
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "ss.status = 'PUBLISHED' " +
           "ORDER BY ss.totalAssignments DESC")
    List<ShiftSchedule> findSchedulesWithMostAssignments(Pageable pageable);

    /**
     * TÃ¬m schedules theo thÃ¡ng/nÄƒm
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "YEAR(ss.startDate) = :year AND " +
           "(:month IS NULL OR MONTH(ss.startDate) = :month) " +
           "ORDER BY ss.startDate ASC")
    List<ShiftSchedule> findByYearAndMonth(@Param("year") int year,
                                          @Param("month") Integer month);

    /**
     * Kiá»ƒm tra xem cÃ³ schedule nÃ o Ä‘ang hoáº¡t Ä‘á»™ng khÃ´ng
     */
    @Query("SELECT COUNT(ss) > 0 FROM ShiftSchedule ss WHERE " +
           "ss.status = 'PUBLISHED' AND " +
           "ss.startDate <= :currentDate AND ss.endDate >= :currentDate")
    boolean hasActiveSchedule(@Param("currentDate") LocalDate currentDate);

    /**
     * TÃ¬m schedule gáº§n nháº¥t sáº½ báº¯t Ä‘áº§u
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "ss.status = 'PUBLISHED' AND " +
           "ss.startDate > :currentDate " +
           "ORDER BY ss.startDate ASC")
    Optional<ShiftSchedule> findNextSchedule(@Param("currentDate") LocalDate currentDate);

    /**
     * Cáº­p nháº­t sá»‘ lÆ°á»£ng assignments
     */
    @Query("UPDATE ShiftSchedule ss SET ss.totalAssignments = " +
           "(SELECT COUNT(sa) FROM ShiftAssignment sa WHERE sa.schedule.id = ss.id), " +
           "ss.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE ss.id = :scheduleId")
    int updateAssignmentCount(@Param("scheduleId") Long scheduleId);

    /**
     * Cáº­p nháº­t tráº¡ng thÃ¡i schedule
     */
    @Query("UPDATE ShiftSchedule ss SET ss.status = :status, ss.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE ss.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") ShiftSchedule.ScheduleStatus status);

    /**
     * Archive schedules cÅ©
     */
    @Query("UPDATE ShiftSchedule ss SET ss.status = 'ARCHIVED', " +
           "ss.archivedAt = CURRENT_TIMESTAMP, ss.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE ss.status = 'PUBLISHED' AND ss.endDate < :cutoffDate")
    int archiveOldSchedules(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * TÃ¬m schedules draft cÅ© (chÆ°a publish > 7 ngÃ y)
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "ss.status = 'DRAFT' AND " +
           "ss.createdAt < :cutoffDate " +
           "ORDER BY ss.createdAt ASC")
    List<ShiftSchedule> findOldDraftSchedules(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * XÃ³a schedules draft cÅ©
     */
    @Query("DELETE FROM ShiftSchedule ss WHERE " +
           "ss.status = 'DRAFT' AND " +
           "ss.totalAssignments = 0 AND " +
           "ss.createdAt < :cutoffDate")
    int deleteOldEmptyDrafts(@Param("cutoffDate") LocalDateTime cutoffDate);
}
