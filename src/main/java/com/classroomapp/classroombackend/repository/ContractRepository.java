package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    
    // Tìm hợp đồng theo loại (TEACHER hoặc STAFF)
    List<Contract> findByContractTypeOrderByCreatedAtDesc(String contractType);
    
    // Tìm hợp đồng theo user ID
    List<Contract> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Tìm hợp đồng theo user ID và loại hợp đồng
    Optional<Contract> findByUserIdAndContractType(Long userId, String contractType);
    
    // Tìm hợp đồng theo user ID, loại và trạng thái
    Optional<Contract> findByUserIdAndContractTypeAndStatus(Long userId, String contractType, String status);
    
    // Tìm hợp đồng theo trạng thái
    List<Contract> findByStatusOrderByCreatedAtDesc(String status);
    
    // Tìm hợp đồng theo loại và trạng thái
    List<Contract> findByContractTypeAndStatusOrderByCreatedAtDesc(String contractType, String status);
    
    // Đếm số hợp đồng theo loại
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.contractType = :contractType")
    Long countByContractType(@Param("contractType") String contractType);
    
    // Đếm số hợp đồng theo trạng thái
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.status = :status")
    Long countByStatus(@Param("status") String status);
    
    // Tìm hợp đồng theo email
    Optional<Contract> findByEmail(String email);
    
    // Đếm số hợp đồng được tạo trong ngày
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.createdAt >= :startOfDay AND c.createdAt < :endOfDay")
    Long countByCreatedAtBetween(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
    
    // Kiểm tra xem user đã có hợp đồng chưa
    boolean existsByUserId(Long userId);
}
