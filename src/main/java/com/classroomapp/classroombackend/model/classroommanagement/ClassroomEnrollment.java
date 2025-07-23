package com.classroomapp.classroombackend.model.classroommanagement;

import com.classroomapp.classroombackend.model.usermanagement.User;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "classroom_enrollments")
public class ClassroomEnrollment {

    @EmbeddedId
    private ClassroomEnrollmentId id;

    @ManyToOne
    @MapsId("classroomId")
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    public User getUser() {
        return user;
    }
} 