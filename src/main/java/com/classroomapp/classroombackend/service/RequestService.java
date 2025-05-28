package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.RequestDTO;
import com.classroomapp.classroombackend.dto.RequestResponseDTO;

import java.util.List;

public interface RequestService {
    RequestResponseDTO createRequest(RequestDTO requestDTO);
    RequestResponseDTO approveRequest(Long requestId);
    RequestResponseDTO rejectRequest(Long requestId, String reason);
    List<RequestResponseDTO> getPendingRequests();
    List<RequestResponseDTO> getRequestsByEmail(String email);
    boolean hasActiveRequest(String email, String role);
    RequestResponseDTO getRequestDetails(Long requestId);
} 