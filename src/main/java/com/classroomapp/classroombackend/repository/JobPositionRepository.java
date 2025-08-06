package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.JobPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {
    List<JobPosition> findByRecruitmentPlanId(Long recruitmentPlanId);
    
    @Modifying
    @Query("DELETE FROM JobPosition jp WHERE jp.recruitmentPlan.id = :recruitmentPlanId")
    void deleteByRecruitmentPlanId(@Param("recruitmentPlanId") Long recruitmentPlanId);
} 