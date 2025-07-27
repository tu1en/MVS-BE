package com.classroomapp.classroombackend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.security.CustomUserDetails;
import com.classroomapp.classroombackend.service.ClassroomSecurityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClassroomSecurityServiceImpl implements ClassroomSecurityService {
    
    private static final Logger log = LoggerFactory.getLogger(ClassroomSecurityServiceImpl.class);
    
    private final ClassroomRepository classroomRepository;
    private final ClassroomEnrollmentRepository classroomEnrollmentRepository;
    private final UserRepository userRepository;

    @Override
    public boolean isMember(Long classroomId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.debug("No authenticated user found");
                return false;
            }

            User currentUser = getCurrentUser(authentication);
            if (currentUser == null) {
                log.debug("Could not retrieve current user");
                return false;
            }

            return isMember(classroomId, currentUser);
        } catch (Exception e) {
            log.error("Error checking if user is member of classroom {}: {}", classroomId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isTeacher(Long classroomId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.debug("No authenticated user found");
                return false;
            }

            User currentUser = getCurrentUser(authentication);
            if (currentUser == null) {
                log.debug("Could not retrieve current user");
                return false;
            }

            return isTeacherOfClassroom(currentUser, classroomId);
        } catch (Exception e) {
            log.error("Error checking if user is teacher of classroom {}: {}", classroomId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isMember(Long classroomId, Object principal) {
        try {
            User user = null;
            
            if (principal instanceof User) {
                user = (User) principal;
            } else if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                user = userRepository.findById(userDetails.getId()).orElse(null);
            } else if (principal instanceof Authentication) {
                user = getCurrentUser((Authentication) principal);
            } else {
                log.warn("Unknown principal type: {}", principal.getClass().getSimpleName());
                return false;
            }

            if (user == null) {
                log.debug("Could not resolve user from principal");
                return false;
            }

            // Check if user is enrolled in the classroom
            boolean isEnrolled = classroomEnrollmentRepository.existsByClassroomIdAndUserId(classroomId, user.getId());
            
            // Also check if user is the teacher of the classroom
            boolean isTeacher = isTeacherOfClassroom(user, classroomId);
            
            log.debug("User {} membership check for classroom {}: enrolled={}, teacher={}", 
                     user.getId(), classroomId, isEnrolled, isTeacher);
            
            return isEnrolled || isTeacher;
        } catch (Exception e) {
            log.error("Error checking membership for classroom {}: {}", classroomId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isTeacherOfClassroom(Object principal, Long classroomId) {
        try {
            User user = null;
            
            if (principal instanceof User) {
                user = (User) principal;
            } else if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                user = userRepository.findById(userDetails.getId()).orElse(null);
            } else if (principal instanceof Authentication) {
                user = getCurrentUser((Authentication) principal);
            } else {
                log.warn("Unknown principal type: {}", principal.getClass().getSimpleName());
                return false;
            }

            if (user == null) {
                log.debug("Could not resolve user from principal");
                return false;
            }

            return isTeacherOfClassroom(user, classroomId);
        } catch (Exception e) {
            log.error("Error checking if user is teacher of classroom {}: {}", classroomId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isTeacherOfClassroom(User user, Long classroomId) {
        try {
            if (user == null || classroomId == null) {
                log.debug("User or classroom ID is null");
                return false;
            }

            // Find the classroom and check if the user is the teacher
            Classroom classroom = classroomRepository.findById(classroomId).orElse(null);
            if (classroom == null) {
                log.debug("Classroom with ID {} not found", classroomId);
                return false;
            }

            // Check if the user is the teacher of this classroom
            boolean isTeacher = classroom.getTeacher() != null && 
                               classroom.getTeacher().getId().equals(user.getId());
            
            log.debug("Teacher check for user {} in classroom {}: {}", 
                     user.getId(), classroomId, isTeacher);
            
            return isTeacher;
        } catch (Exception e) {
            log.error("Error checking if user {} is teacher of classroom {}: {}", 
                     user.getId(), classroomId, e.getMessage());
            return false;
        }
    }

    /**
     * Helper method to get current user from Authentication
     */
    private User getCurrentUser(Authentication authentication) {
        try {
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                return userRepository.findById(userDetails.getId()).orElse(null);
            } else if (authentication.getName() != null) {
                // Try to find user by email (which is often the authentication name)
                return userRepository.findByEmail(authentication.getName()).orElse(null);
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting current user from authentication: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if user has any role that allows classroom access
     */
    public boolean hasClassroomAccess(User user, Long classroomId) {
        return isMember(classroomId, user) || isTeacherOfClassroom(user, classroomId);
    }

    /**
     * Check if user can modify classroom (teacher only)
     */
    public boolean canModifyClassroom(User user, Long classroomId) {
        return isTeacherOfClassroom(user, classroomId);
    }

    /**
     * Check if user can view classroom (member or teacher)
     */
    public boolean canViewClassroom(User user, Long classroomId) {
        return hasClassroomAccess(user, classroomId);
    }

    /**
     * Get current user from security context
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return getCurrentUser(authentication);
    }
}