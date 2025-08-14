package com.classroomapp.classroombackend.service.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.security.CustomUserDetails;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.service.ClassroomSecurityService;
import com.classroomapp.classroombackend.service.SubmissionSecurityService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("submissionSecurityService")
@RequiredArgsConstructor
@Slf4j
public class SubmissionSecurityServiceImpl implements SubmissionSecurityService {
    
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;
    private final ClassroomSecurityService classroomSecurityService;
    
    @Override
    public boolean isCurrentStudent(Long studentId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
                return false;
            }
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getId().equals(studentId);
        } catch (Exception e) {
            log.error("Error checking if current user is student {}: {}", studentId, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean canAccessAssignmentSubmissions(Long assignmentId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
                return false;
            }
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User currentUser = userRepository.findById(userDetails.getId()).orElse(null);
            if (currentUser == null) {
                return false;
            }
            
            // Admin can access all
            if ("ADMIN".equals(currentUser.getRole())) {
                return true;
            }
            
            Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
            if (assignment == null) {
                return false;
            }
            
            // Teacher can access if they are the teacher of the classroom
            if ("TEACHER".equals(currentUser.getRole())) {
                return classroomSecurityService.isTeacherOfClassroom(currentUser, assignment.getClassroom().getId());
            }
            
            // Students can only access their own submissions (handled by separate methods)
            return false;
        } catch (Exception e) {
            log.error("Error checking access to assignment {} submissions: {}", assignmentId, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean canGradeAssignmentSubmissions(Long assignmentId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
                return false;
            }
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User currentUser = userRepository.findById(userDetails.getId()).orElse(null);
            if (currentUser == null) {
                return false;
            }
            
            // Only admins and teachers can grade
            if (!"ADMIN".equals(currentUser.getRole()) && !"TEACHER".equals(currentUser.getRole())) {
                return false;
            }
            
            // Admin can grade all
            if ("ADMIN".equals(currentUser.getRole())) {
                return true;
            }
            
            Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
            if (assignment == null) {
                return false;
            }
            
            // Teacher can grade if they are the teacher of the classroom
            return classroomSecurityService.isTeacherOfClassroom(currentUser, assignment.getClassroom().getId());
        } catch (Exception e) {
            log.error("Error checking grading permission for assignment {}: {}", assignmentId, e.getMessage());
            return false;
        }
    }
}