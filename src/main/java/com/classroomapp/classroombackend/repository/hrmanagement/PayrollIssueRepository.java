package com.classroomapp.classroombackend.repository.hrmanagement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.classroomapp.classroombackend.model.hrmanagement.PayrollIssue;

public interface PayrollIssueRepository extends JpaRepository<PayrollIssue, Long> {
    List<PayrollIssue> findByUserIdOrderByCreatedAtDesc(Long userId);
}


