package com.classroomapp.classroombackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lecture_recordings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LectureRecording {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "lecture_id", nullable = false)
    private Long lectureId;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "recording_date")
    private LocalDateTime recordingDate = LocalDateTime.now();

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "thumbnail_path", length = 500)
    private String thumbnailPath;

    // JPA relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", insertable = false, updatable = false)
    private Lecture lecture;
}
