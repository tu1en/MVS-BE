package com.classroomapp.classroombackend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "classroom_enrollments")
@IdClass(ClassroomEnrollmentId.class)
public class ClassroomEnrollment {
    @Id
    @ManyToOne
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
} 