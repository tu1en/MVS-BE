package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.InterviewSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterviewScheduleRepository extends JpaRepository<InterviewSchedule, Long> {
    List<InterviewSchedule> findByApplication_JobPosition_Id(Long jobPositionId);
    List<InterviewSchedule> findByApplication_Id(Long applicationId);
} 