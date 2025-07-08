package com.classroomapp.classroombackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.model.Accomplishment;
import com.classroomapp.classroombackend.repository.AccomplishmentRepository;

@Service("accomplishmentSecurityService")
public class AccomplishmentSecurityService {

    @Autowired
    private AccomplishmentRepository accomplishmentRepository;

    public boolean isOwner(Authentication authentication, Long accomplishmentId) {
        String currentUsername = authentication.getName();
        Accomplishment accomplishment = accomplishmentRepository.findById(accomplishmentId).orElse(null);
        if (accomplishment == null) {
            return false;
        }
        return accomplishment.getStudent().getEmail().equals(currentUsername);
    }
} 