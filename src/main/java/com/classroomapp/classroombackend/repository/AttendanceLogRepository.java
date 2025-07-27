package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {
    List<AttendanceLog> findByAttendanceDate(LocalDate date);
    List<AttendanceLog> findByStaffId(Long userId);
    List<AttendanceLog> findByStaffRoleIdAndAttendanceDate(Integer roleId, LocalDate date);
    List<AttendanceLog> findByAttendanceDateAndShiftName(LocalDate date, String shift);
    List<AttendanceLog> findByStaffIdAndAttendanceDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
