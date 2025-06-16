package com.classroomapp.classroombackend.model;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.usermanagement.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "announcement_reads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "announcement_id", nullable = false)
    private Long announcementId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "read_at")
    private LocalDateTime readAt = LocalDateTime.now();

    // JPA relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id", insertable = false, updatable = false)
    private Announcement announcement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
