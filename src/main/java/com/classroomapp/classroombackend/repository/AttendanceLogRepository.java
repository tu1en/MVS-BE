package com.classroomapp.classroombackend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.AttendanceLog;

@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {
    List<AttendanceLog> findByDate(LocalDate date);
    List<AttendanceLog> findByUserId(Long userId);
    List<AttendanceLog> findByRoleAndDate(String role, LocalDate date);
    List<AttendanceLog> findByDateAndShift(LocalDate date, String shift);
    List<AttendanceLog> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
List<AttendanceLog> findByRole(String role);

}
