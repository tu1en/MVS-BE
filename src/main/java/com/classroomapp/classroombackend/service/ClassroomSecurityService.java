package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.model.usermanagement.User;

public interface ClassroomSecurityService {

    boolean isMember(Long classroomId);

    boolean isTeacher(Long classroomId);

    boolean isMember(Long classroomId, Object principal);

    boolean isTeacherOfClassroom(Object principal, Long classroomId);

    boolean isTeacherOfClassroom(User user, Long classroomId);
} 