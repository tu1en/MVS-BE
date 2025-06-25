package com.classroomapp.classroombackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.AllowedIp;

@Repository
public interface AllowedIpRepository extends JpaRepository<AllowedIp, Long> {

    /**
     * Tìm IP address cụ thể
     */
    Optional<AllowedIp> findByIpAddress(String ipAddress);

    /**
     * Kiểm tra IP có được phép không
     */
    @Query("SELECT COUNT(a) > 0 FROM AllowedIp a WHERE a.ipAddress = :ipAddress AND a.isActive = true")
    boolean isIpAllowed(@Param("ipAddress") String ipAddress);

    /**
     * Lấy danh sách IP đang active
     */
    List<AllowedIp> findByIsActiveTrue();

    /**
     * Tìm theo description
     */
    List<AllowedIp> findByDescriptionContainingIgnoreCase(String description);
}
