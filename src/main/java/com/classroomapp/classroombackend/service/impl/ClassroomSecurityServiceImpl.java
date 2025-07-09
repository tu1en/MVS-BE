package com.classroomapp.classroombackend.service.impl;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollmentId;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ClassroomSecurityService;

import lombok.RequiredArgsConstructor;

@Service("classroomSecurityService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassroomSecurityServiceImpl implements ClassroomSecurityService {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final ClassroomEnrollmentRepository enrollmentRepository;

    @Override
    public boolean isMember(Long classroomId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElse(null);
        if (user == null) {
            return false;
        }

        Classroom classroom = classroomRepository.findById(classroomId).orElse(null);
        if (classroom == null) {
            return false;
        }

        return classroom.getStudents().contains(user) || classroom.getTeacher().equals(user);
    }

    @Override
    public boolean isTeacher(Long classroomId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElse(null);
        if (user == null) {
            return false;
        }
        Classroom classroom = classroomRepository.findById(classroomId).orElse(null);
        if (classroom == null) {
            return false;
        }
        return classroom.getTeacher().equals(user);
    }

    @Override
    public boolean isMember(Long classroomId, Object principal) {
        if (!(principal instanceof UserDetails)) {
            return false;
        }
        UserDetails userDetails = (UserDetails) principal;
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return false;
        }

        // Check if the user is the teacher of the class
        boolean isTeacher = classroomRepository.findById(classroomId)
                .map(c -> c.getTeacher() != null && c.getTeacher().getId().equals(user.getId()))
                .orElse(false);

        if (isTeacher) {
            return true;
        }

        // Check if the user is an enrolled student
        ClassroomEnrollmentId enrollmentId = new ClassroomEnrollmentId(classroomId, user.getId());
        return enrollmentRepository.existsById(enrollmentId);
    }

    @Override
    public boolean isTeacherOfClassroom(Object principal, Long classroomId) {
        if (!(principal instanceof UserDetails)) {
            return false;
        }
        UserDetails userDetails = (UserDetails) principal;
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return false;
        }
        return isTeacherOfClassroom(user, classroomId);
    }

    @Override
    public boolean isTeacherOfClassroom(User user, Long classroomId) {
        if (user == null) {
            return false;
        }
        return classroomRepository.findById(classroomId)
                .map(classroom -> classroom.getTeacher() != null && classroom.getTeacher().getId().equals(user.getId()))
                .orElse(false);
    }
} 