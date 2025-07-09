package com.classroomapp.classroombackend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "announcement_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "announcement_id", nullable = false)
    private Long announcementId;

    @NotBlank
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @NotBlank
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt = LocalDateTime.now();

    // JPA relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id", insertable = false, updatable = false)
    private Announcement announcement;
}
