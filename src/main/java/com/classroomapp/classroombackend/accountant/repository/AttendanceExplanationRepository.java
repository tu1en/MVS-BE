package com.classroomapp.classroombackend.accountant.repository;

import com.classroomapp.classroombackend.accountant.model.AttendanceExplanation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceExplanationRepository extends JpaRepository<AttendanceExplanation, Long> {
    java.util.List<AttendanceExplanation> findByEmployeeIdInAndExplanationTypeAndStatus(
        java.util.List<Long> employeeIds, String explanationType, String status);

}
