package com.classroomapp.classroombackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Table(name = "schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String classId;

    @Column(nullable = false)
    private String className;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private Integer day; // 0 for Monday, 1 for Tuesday, etc.

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Long teacherId;

    @Column(nullable = false)
    private String teacherName;

    @ElementCollection
    @CollectionTable(name = "schedule_student_ids", 
        joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "student_id")
    private Set<Long> studentIds;

    private String materialsUrl;

    private String meetUrl;

    @Column(nullable = false)
    private String room;
}
