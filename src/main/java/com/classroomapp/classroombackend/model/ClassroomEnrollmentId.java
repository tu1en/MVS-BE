package com.classroomapp.classroombackend.model;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomEnrollmentId implements Serializable {
    private Long classroom;
    private Long user;
} 