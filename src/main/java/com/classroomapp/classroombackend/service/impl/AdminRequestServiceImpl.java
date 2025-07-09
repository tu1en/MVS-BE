package com.classroomapp.classroombackend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.requestmanagement.RequestDTO;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.Request;
import com.classroomapp.classroombackend.repository.requestmanagement.RequestRepository;
import com.classroomapp.classroombackend.service.AdminRequestService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminRequestServiceImpl implements AdminRequestService {

    private final RequestRepository requestRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<RequestDTO> getAllRequests() {
        return requestRepository.findAll().stream()
                .map(request -> modelMapper.map(request, RequestDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public RequestDTO approveRequest(Long id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
        // Logic to approve request and create user
        request.setStatus("APPROVED");
        Request savedRequest = requestRepository.save(request);
        return modelMapper.map(savedRequest, RequestDTO.class);
    }

    @Override
    public RequestDTO rejectRequest(Long id, String reason) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
        request.setStatus("REJECTED");
        request.setRejectReason(reason);
        Request savedRequest = requestRepository.save(request);
        return modelMapper.map(savedRequest, RequestDTO.class);
    }
} 