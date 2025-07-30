package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.JobPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {
    List<JobPosition> findByRecruitmentPlanId(Long recruitmentPlanId);
} 