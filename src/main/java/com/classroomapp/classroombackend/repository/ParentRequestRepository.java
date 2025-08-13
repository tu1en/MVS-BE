package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.ParentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ParentRequestRepository extends JpaRepository<ParentRequest, Long> {
    
    /**
     * Find requests by classroom ID
     */
    List<ParentRequest> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);
    
    /**
     * Find requests by student ID
     */
    List<ParentRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    
    /**
     * Find pending requests for a classroom
     */
    @Query("SELECT pr FROM ParentRequest pr WHERE pr.classroomId = :classroomId AND pr.status = 'PENDING' ORDER BY pr.createdAt DESC")
    List<ParentRequest> findPendingRequestsByClassroom(@Param("classroomId") Long classroomId);
    
    /**
     * Find requests by status
     */
    List<ParentRequest> findByStatusOrderByCreatedAtDesc(ParentRequest.RequestStatus status);
    
    /**
     * Find requests for today by classroom
     */
    @Query("SELECT pr FROM ParentRequest pr WHERE pr.classroomId = :classroomId AND pr.requestDate = :date ORDER BY pr.createdAt DESC")
    List<ParentRequest> findByClassroomAndDate(@Param("classroomId") Long classroomId, @Param("date") LocalDate date);
    
    /**
     * Find requests that need teacher notification
     */
    @Query("SELECT pr FROM ParentRequest pr WHERE pr.classroomId = :classroomId AND pr.teacherNotified = false AND pr.status = 'PENDING' ORDER BY pr.createdAt DESC")
    List<ParentRequest> findUnnotifiedRequestsForTeacher(@Param("classroomId") Long classroomId);
    
    /**
     * Find requests that need assistant notification
     */
    @Query("SELECT pr FROM ParentRequest pr WHERE pr.classroomId = :classroomId AND pr.assistantNotified = false AND pr.status = 'PENDING' ORDER BY pr.createdAt DESC")
    List<ParentRequest> findUnnotifiedRequestsForAssistant(@Param("classroomId") Long classroomId);
    
    /**
     * Find requests by date range
     */
    @Query("SELECT pr FROM ParentRequest pr WHERE pr.classroomId = :classroomId AND pr.requestDate BETWEEN :startDate AND :endDate ORDER BY pr.requestDate DESC, pr.createdAt DESC")
    List<ParentRequest> findByClassroomAndDateRange(@Param("classroomId") Long classroomId, 
                                                   @Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);
    
    /**
     * Count pending requests for a classroom
     */
    @Query("SELECT COUNT(pr) FROM ParentRequest pr WHERE pr.classroomId = :classroomId AND pr.status = 'PENDING'")
    Long countPendingRequestsByClassroom(@Param("classroomId") Long classroomId);
    
    /**
     * Find expired requests that need to be updated
     */
    @Query("SELECT pr FROM ParentRequest pr WHERE pr.status = 'PENDING' AND pr.requestDate < :currentDate")
    List<ParentRequest> findExpiredRequests(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Find requests created in the last N hours
     */
    @Query("SELECT pr FROM ParentRequest pr WHERE pr.classroomId = :classroomId AND pr.createdAt >= :since ORDER BY pr.createdAt DESC")
    List<ParentRequest> findRecentRequestsByClassroom(@Param("classroomId") Long classroomId, @Param("since") LocalDateTime since);
}