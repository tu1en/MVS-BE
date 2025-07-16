package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.AttendanceExplanation;
import com.classroomapp.classroombackend.model.ExplanationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface AttendanceExplanationRepository extends JpaRepository<AttendanceExplanation, Long> {

    @Query("SELECT ae FROM AttendanceExplanation ae WHERE " +
           "(:startDate IS NULL OR ae.absenceDate >= :startDate) AND " +
           "(:endDate IS NULL OR ae.absenceDate <= :endDate) AND " +
           "(:status IS NULL OR ae.status = :status) AND " +
           "(:department IS NULL OR ae.department = :department)")
    Page<AttendanceExplanation> findByFilters(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") ExplanationStatus status,
            @Param("department") String department,
            Pageable pageable);

    @Query("SELECT ae.reason, COUNT(ae) FROM AttendanceExplanation ae WHERE " +
           "(:startDate IS NULL OR ae.absenceDate >= :startDate) AND " +
           "(:endDate IS NULL OR ae.absenceDate <= :endDate) " +
           "GROUP BY ae.reason")
    Object[][] countByReason(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT ae.status, COUNT(ae) FROM AttendanceExplanation ae WHERE " +
           "(:startDate IS NULL OR ae.absenceDate >= :startDate) AND " +
           "(:endDate IS NULL OR ae.absenceDate <= :endDate) " +
           "GROUP BY ae.status")
    Object[][] countByStatus(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
