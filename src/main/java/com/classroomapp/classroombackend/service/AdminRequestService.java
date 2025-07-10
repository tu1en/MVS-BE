package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.requestmanagement.RequestDTO;

public interface AdminRequestService {
    List<RequestDTO> getAllRequests();
    RequestDTO approveRequest(Long id);
    RequestDTO rejectRequest(Long id, String reason);
    List<RequestDTO> getPendingRequests();
} 