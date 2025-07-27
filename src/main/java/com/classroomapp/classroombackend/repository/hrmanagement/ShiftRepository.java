package com.classroomapp.classroombackend.repository.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.Shift;
import com.classroomapp.classroombackend.model.usermanagement.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findByIsActiveTrueOrderByStartTimeAsc();
    
    List<Shift> findByNameContainingIgnoreCaseOrderByStartTime(String name);
    
    @Query("SELECT s FROM Shift s WHERE s.startTime < s.endTime AND s.isActive = true")
    List<Shift> findValidActiveShifts();
    
    boolean existsByNameAndIsActiveTrue(String name);
}