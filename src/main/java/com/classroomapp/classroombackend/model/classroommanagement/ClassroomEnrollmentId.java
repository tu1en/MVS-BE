package com.classroomapp.classroombackend.model.classroommanagement;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ClassroomEnrollmentId implements Serializable {

    @Column(name = "classroom_id")
    private Long classroomId;

    @Column(name = "user_id")
    private Long userId;

    public ClassroomEnrollmentId() {
    }

    public ClassroomEnrollmentId(Long classroomId, Long userId) {
        this.classroomId = classroomId;
        this.userId = userId;
    }

    public Long getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(Long classroomId) {
        this.classroomId = classroomId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassroomEnrollmentId that = (ClassroomEnrollmentId) o;
        return Objects.equals(classroomId, that.classroomId) &&
               Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classroomId, userId);
    }
} 