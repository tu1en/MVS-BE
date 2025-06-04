package com.classroomapp.classroombackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import com.classroomapp.classroombackend.model.usermanagement.User;

@Entity
@Table(name = "accomplishments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Accomplishment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "course_title")
    private String courseTitle;
    
    private String subject;
    
    @Column(name = "teacher_name")
    private String teacherName;
    
    private Double grade;
    
    @Column(name = "completion_date")
    private LocalDate completionDate;
}