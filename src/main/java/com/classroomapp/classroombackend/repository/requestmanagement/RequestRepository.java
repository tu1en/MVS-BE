package com.classroomapp.classroombackend.repository.requestmanagement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.Request;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByEmail(String email);
    List<Request> findByStatus(String status);
    List<Request> findByRequestedRole(String role);
    boolean existsByEmailAndStatusAndRequestedRole(String email, String status, String role);
    boolean existsByEmailAndStatusIn(String email, List<String> statuses);
}
