package com.classroomapp.classroombackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "live_streams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveStream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "lecture_id", nullable = false)
    private Long lectureId;

    @NotBlank
    @Column(name = "stream_key", unique = true, nullable = false)
    private String streamKey;

    @Column(name = "stream_url", length = 500)
    private String streamUrl;

    @Column(name = "chat_enabled")
    private Boolean chatEnabled = true;

    @Column(name = "max_viewers")
    private Integer maxViewers = 100;

    @Column(name = "current_viewers")
    private Integer currentViewers = 0;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private StreamStatus status = StreamStatus.SCHEDULED;

    @Column(name = "recording_enabled")
    private Boolean recordingEnabled = true;

    // JPA relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", insertable = false, updatable = false)
    private Lecture lecture;

    public enum StreamStatus {
        SCHEDULED, LIVE, ENDED, CANCELLED
    }
}
