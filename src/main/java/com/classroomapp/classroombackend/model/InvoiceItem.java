package com.classroomapp.classroombackend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;

/**
 * Invoice item entity for detailed billing information
 */
@Entity
@Table(name = "invoice_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"invoice"})
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Nationalized
    @Column(name = "description", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String description;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", insertable = false, updatable = false)
    @JsonBackReference
    private Invoice invoice;

    // Constructors
    public InvoiceItem(Long invoiceId, String description, Integer quantity, BigDecimal unitPrice) {
        this.invoiceId = invoiceId;
        this.description = description;
        this.quantity = quantity != null ? quantity : 1;
        this.unitPrice = unitPrice;
        this.amount = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }

    // Business logic methods
    
    /**
     * Recalculate amount based on quantity and unit price
     */
    public void calculateAmount() {
        if (quantity != null && unitPrice != null) {
            this.amount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    // Lifecycle callbacks
    @PrePersist
    @PreUpdate
    protected void onSave() {
        calculateAmount();
    }
}