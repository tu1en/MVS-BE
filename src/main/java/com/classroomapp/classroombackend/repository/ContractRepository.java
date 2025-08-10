package com.classroomapp.classroombackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.Contract;

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

    // Phục vụ cho PayrollGenerationServiceImpl (không cần sắp xếp)
    List<Contract> findByContractTypeAndStatus(String contractType, String status);
    
    // Đếm số hợp đồng theo loại
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.contractType = :contractType")
    Long countByContractType(@Param("contractType") String contractType);
    
    // Đếm số hợp đồng theo trạng thái
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.status = :status")
    Long countByStatus(@Param("status") String status);
    
    // Tìm hợp đồng theo email
    Optional<Contract> findByEmail(String email);

    // Tìm hợp đồng ACTIVE cho giáo viên theo userId hoặc email (ưu tiên bản cập nhật mới nhất)
    @Query("SELECT c FROM Contract c WHERE (c.userId = :userId OR LOWER(c.email) = LOWER(:email)) " +
           "AND UPPER(c.contractType) = UPPER(:type) AND UPPER(c.status) = UPPER(:status) " +
           "ORDER BY c.updatedAt DESC")
    List<Contract> findActiveTeacherContracts(@Param("userId") Long userId,
                                              @Param("email") String email,
                                              @Param("type") String type,
                                              @Param("status") String status);

    // Tìm hợp đồng ACTIVE theo userId (method được SalaryController sử dụng)
    @Query("SELECT c FROM Contract c WHERE c.userId = :userId AND UPPER(c.status) = 'ACTIVE' ORDER BY c.updatedAt DESC")
    Optional<Contract> findActiveContractByUserId(@Param("userId") Long userId);
}
