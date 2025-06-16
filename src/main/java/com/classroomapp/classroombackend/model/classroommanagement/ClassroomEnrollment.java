package com.classroomapp.classroombackend.model.classroommanagement;

import com.classroomapp.classroombackend.model.usermanagement.User;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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