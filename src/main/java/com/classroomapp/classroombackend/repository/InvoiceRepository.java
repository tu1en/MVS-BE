package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * Find all invoices for a specific student
     */
    List<Invoice> findByStudentIdOrderByIssueDateDesc(Long studentId);

    /**
     * Find invoices for a student within date range
     */
    @Query("SELECT i FROM Invoice i WHERE i.studentId = :studentId " +
           "AND i.issueDate >= :startDate AND i.issueDate <= :endDate " +
           "ORDER BY i.issueDate DESC")
    List<Invoice> findByStudentIdAndDateRange(@Param("studentId") Long studentId, 
                                            @Param("startDate") LocalDate startDate, 
                                            @Param("endDate") LocalDate endDate);

    /**
     * Find invoices with pagination for a student
     */
    Page<Invoice> findByStudentIdOrderByIssueDateDesc(Long studentId, Pageable pageable);

    /**
     * Find invoice by invoice number
     */
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    /**
     * Find unpaid invoices for a student
     */
    @Query("SELECT i FROM Invoice i WHERE i.studentId = :studentId " +
           "AND (i.status = 'PENDING' OR i.status = 'PARTIAL' OR i.status = 'OVERDUE') " +
           "ORDER BY i.dueDate ASC")
    List<Invoice> findUnpaidInvoicesByStudentId(@Param("studentId") Long studentId);

    /**
     * Find overdue invoices for a student
     */
    @Query("SELECT i FROM Invoice i WHERE i.studentId = :studentId " +
           "AND i.dueDate < CURRENT_DATE " +
           "AND (i.status = 'PENDING' OR i.status = 'PARTIAL' OR i.status = 'OVERDUE') " +
           "ORDER BY i.dueDate ASC")
    List<Invoice> findOverdueInvoicesByStudentId(@Param("studentId") Long studentId);

    /**
     * Get billing summary for a student
     */
    @Query("SELECT " +
           "COALESCE(SUM(i.totalAmount - i.paidAmount), 0) as totalDebt, " +
           "COALESCE(SUM(i.paidAmount), 0) as totalPaid, " +
           "COUNT(CASE WHEN i.status IN ('PENDING', 'PARTIAL', 'OVERDUE') THEN 1 END) as unpaidInvoices, " +
           "COALESCE(SUM(CASE WHEN i.dueDate < CURRENT_DATE AND i.status IN ('PENDING', 'PARTIAL', 'OVERDUE') THEN i.totalAmount - i.paidAmount ELSE 0 END), 0) as overdueAmount " +
           "FROM Invoice i WHERE i.studentId = :studentId")
    Object[] getBillingSummaryByStudentId(@Param("studentId") Long studentId);
}