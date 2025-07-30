package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.RecruitmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruitmentPlanRepository extends JpaRepository<RecruitmentPlan, Long> {
}