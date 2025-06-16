package com.classroomapp.classroombackend.model.classroommanagement;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomEnrollmentId implements Serializable {
    private Long classroom;
    private Long user;
} 