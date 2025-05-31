package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByEmail(String email);
    List<Request> findByStatus(String status);
    List<Request> findByRequestedRole(String role);
    boolean existsByEmailAndStatusAndRequestedRole(String email, String status, String role);
} 