package com.classroomapp.classroombackend.model.classroommanagement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "classrooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(columnDefinition = "NVARCHAR(255)")
    private String name;

    @Column(columnDefinition = "NVARCHAR(1000)")
    private String description;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String section;

    @Column(columnDefinition = "NVARCHAR(100)")
    private String subject;    // The teacher who created/owns this classroom
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User teacher;

    @Column(name = "course_id")
    private Long courseId;

    @OneToMany(mappedBy = "classroom", fetch = FetchType.LAZY)
    private Set<ClassroomEnrollment> enrollments = new HashSet<>();

    public Set<User> getStudents() {
        Set<User> students = new HashSet<>();
        for (ClassroomEnrollment enrollment : this.enrollments) {
            students.add(enrollment.getUser());
        }
        return students;
    }
    
    // Syllabus for this classroom - one classroom has one syllabus
    @OneToOne(mappedBy = "classroom", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "classroom"})
    private Syllabus syllabus;
    
    // Schedules for this classroom - one classroom has many schedule entries
    @OneToMany(mappedBy = "classroom", fetch = FetchType.LAZY)
    private List<Schedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "classroom", fetch = FetchType.LAZY)
    private List<Lecture> lectures = new ArrayList<>();
}
