package com.classroomapp.classroombackend.model;

import java.time.LocalDate;

import com.classroomapp.classroombackend.model.usermanagement.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accomplishments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Accomplishment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User student;
    
    // Legacy fields - can be deprecated later
    @Column(name = "course_title", columnDefinition = "NVARCHAR(255)")
    private String courseTitle;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String subject;

    @Column(name = "teacher_name", columnDefinition = "NVARCHAR(255)")
    private String teacherName;
    
    private Double grade;
    
    @Column(name = "completion_date")
    private LocalDate completionDate;
}