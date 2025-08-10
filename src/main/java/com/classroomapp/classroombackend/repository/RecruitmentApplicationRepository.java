package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.RecruitmentApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruitmentApplicationRepository extends JpaRepository<RecruitmentApplication, Long> {
    @Modifying
    @Query("DELETE FROM RecruitmentApplication ra WHERE ra.jobPosition.id = :jobPositionId")
    void deleteByJobPositionId(@Param("jobPositionId") Long jobPositionId);
    
    @Modifying
    @Query("DELETE FROM RecruitmentApplication ra WHERE ra.jobPosition.id IN (SELECT jp.id FROM JobPosition jp WHERE jp.recruitmentPlan.id = :recruitmentPlanId)")
    void deleteByJobPosition_RecruitmentPlanId(@Param("recruitmentPlanId") Long recruitmentPlanId);
    
    // Kiểm tra email đã tồn tại trong hệ thống
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
} 