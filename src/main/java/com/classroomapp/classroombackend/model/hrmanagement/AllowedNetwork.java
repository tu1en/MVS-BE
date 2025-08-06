package com.classroomapp.classroombackend.model.hrmanagement;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "allowed_networks")
@Data
public class AllowedNetwork {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "ip_range", nullable = false)
    private String ipRange;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "network_type")
    private NetworkType networkType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private CompanyLocation location;
    
    @Column(name = "is_active")
    private boolean active = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum NetworkType {
        OFFICE, VPN, REMOTE
    }
}