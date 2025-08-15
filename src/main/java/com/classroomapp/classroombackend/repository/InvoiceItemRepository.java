package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    /**
     * Find all items for a specific invoice
     */
    List<InvoiceItem> findByInvoiceIdOrderById(Long invoiceId);

    /**
     * Delete all items for a specific invoice
     */
    void deleteByInvoiceId(Long invoiceId);
}