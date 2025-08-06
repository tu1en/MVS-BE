package com.classroomapp.classroombackend.repository.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.AttendanceVerificationLog;
import com.classroomapp.classroombackend.model.usermanagement.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceVerificationLogRepository extends JpaRepository<AttendanceVerificationLog, Long> {
    
    List<AttendanceVerificationLog> findByUserAndCreatedAtBetween(
        User user, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(avl) FROM AttendanceVerificationLog avl WHERE avl.user = :user " +
           "AND DATE(avl.createdAt) = :date AND avl.verificationStatus = 'FAILED'")
    Long countFailedAttemptsToday(@Param("user") User user, @Param("date") LocalDate date);
}