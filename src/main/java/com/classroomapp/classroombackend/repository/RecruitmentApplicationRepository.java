package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.RecruitmentApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruitmentApplicationRepository extends JpaRepository<RecruitmentApplication, Long> {
} 