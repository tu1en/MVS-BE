package com.classroomapp.classroombackend.repository.hrmanagement;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.classroomapp.classroombackend.model.hrmanagement.PayrollViewConfirmation;

public interface PayrollViewConfirmationRepository extends JpaRepository<PayrollViewConfirmation, Long> {
    Optional<PayrollViewConfirmation> findByUserIdAndPeriod(Long userId, String period);
}


