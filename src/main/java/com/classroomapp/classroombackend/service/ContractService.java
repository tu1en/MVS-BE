package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.ContractDto;
import com.classroomapp.classroombackend.dto.ContractStatsDto;
import java.util.List;

public interface ContractService {
    
    // Lấy tất cả hợp đồng theo loại (TEACHER hoặc STAFF)
    List<ContractDto> getContractsByType(String contractType);
    
    // Lấy tất cả hợp đồng
    List<ContractDto> getAllContracts();
    
    // Lấy hợp đồng theo ID
    ContractDto getContractById(Long id);
    
    // Tạo hợp đồng mới
    ContractDto createContract(ContractDto contractDto);
    
    // Cập nhật hợp đồng
    ContractDto updateContract(Long id, ContractDto contractDto);
    
    // Xóa hợp đồng
    void deleteContract(Long id);
    
    // Lấy hợp đồng theo user ID
    List<ContractDto> getContractsByUserId(Long userId);
    
    // Lấy danh sách ứng viên đã đỗ phỏng vấn (chưa có hợp đồng)
    List<ContractDto> getCandidatesReadyForContract();
    
    // Lấy thông tin offer của ứng viên để tạo hợp đồng
    ContractDto getCandidateOfferData(Long candidateId);
    
    // Thống kê hợp đồng
    ContractStatsDto getContractStats();
    
    // Gia hạn hợp đồng (đặt lại ngày bắt đầu và kết thúc)
    ContractDto renewContract(Long contractId);

    // Ký hợp đồng (PENDING -> ACTIVE, đặt ngày bắt đầu = hôm nay, kết thúc +90 ngày)
    ContractDto signContract(Long contractId);

    // Tạo hợp đồng cho tất cả giáo viên active (không trùng), gán lương theo giờ trong khoảng chỉ định
    List<ContractDto> createContractsForActiveTeachers(Long minHourly, Long maxHourly, boolean dryRun);

    // Reseed contract statuses for demo purposes
    void reseedContractStatuses();
}
