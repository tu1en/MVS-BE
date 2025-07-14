package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {
    List<AttendanceLog> findByDate(LocalDate date);
    List<AttendanceLog> findByUserId(Long userId);
    List<AttendanceLog> findByRoleAndDate(String role, LocalDate date);
    List<AttendanceLog> findByDateAndShift(LocalDate date, String shift);
    List<AttendanceLog> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
