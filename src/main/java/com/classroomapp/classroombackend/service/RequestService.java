package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.RequestDTO;
import com.classroomapp.classroombackend.dto.RequestResponseDTO;
import com.classroomapp.classroombackend.dto.requestmanagement.CreateRequestDto;

public interface RequestService {
    void createRegistrationRequest(CreateRequestDto dto);
    RequestResponseDTO createRequest(RequestDTO requestDTO);
    RequestResponseDTO approveRequest(Long requestId);
    RequestResponseDTO rejectRequest(Long requestId, String reason);
    List<RequestResponseDTO> getPendingRequests();
    List<RequestResponseDTO> getRequestsByEmail(String email);
    boolean hasActiveRequest(String email, String role);
    RequestResponseDTO getRequestDetails(Long requestId);
    List<RequestResponseDTO> getAllRequests();
} 