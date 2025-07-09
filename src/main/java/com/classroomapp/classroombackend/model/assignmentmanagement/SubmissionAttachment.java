package com.classroomapp.classroombackend.model.assignmentmanagement;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.dto.FileUploadResponse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "submission_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String fileName;

    @Column(nullable = true)  // Made nullable to avoid DDL error with existing data
    private String fileUrl;

    private String fileType;

    @Column(nullable = true)  // Made nullable to avoid DDL error with existing data
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public SubmissionAttachment(FileUploadResponse fileInfo, Submission submission) {
        this.fileName = fileInfo.getFileName();
        this.fileUrl = fileInfo.getFileUrl();
        this.fileType = fileInfo.getFileType();
        this.fileSize = fileInfo.getSize();
        this.submission = submission;
    }
} 