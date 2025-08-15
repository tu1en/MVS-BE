package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find all payments for a specific student
     */
    List<Payment> findByStudentIdOrderByPaymentDateDesc(Long studentId);

    /**
     * Find payments for a student within date range
     */
    @Query("SELECT p FROM Payment p WHERE p.studentId = :studentId " +
           "AND p.paymentDate >= :startDate AND p.paymentDate <= :endDate " +
           "ORDER BY p.paymentDate DESC")
    List<Payment> findByStudentIdAndDateRange(@Param("studentId") Long studentId, 
                                            @Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * Find payments with pagination for a student
     */
    Page<Payment> findByStudentIdOrderByPaymentDateDesc(Long studentId, Pageable pageable);

    /**
     * Find all payments for a specific invoice
     */
    List<Payment> findByInvoiceIdOrderByPaymentDateDesc(Long invoiceId);

    /**
     * Find payments by reference number
     */
    List<Payment> findByReferenceNumber(String referenceNumber);

    /**
     * Get total payments for a student
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.studentId = :studentId AND p.status = 'COMPLETED'")
    Double getTotalPaymentsByStudentId(@Param("studentId") Long studentId);

    /**
     * Get total payments for an invoice
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.invoiceId = :invoiceId AND p.status = 'COMPLETED'")
    Double getTotalPaymentsByInvoiceId(@Param("invoiceId") Long invoiceId);
}