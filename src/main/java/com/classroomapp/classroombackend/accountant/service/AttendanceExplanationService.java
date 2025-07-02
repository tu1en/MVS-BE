package com.classroomapp.classroombackend.accountant.service;

import com.classroomapp.classroombackend.accountant.model.AttendanceExplanation;
import com.classroomapp.classroombackend.accountant.repository.AttendanceExplanationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AttendanceExplanationService {
    @Autowired
    private AttendanceExplanationRepository repository;

    public AttendanceExplanation submitExplanation(AttendanceExplanation explanation) {
        explanation.setStatus("PENDING");
        return repository.save(explanation);
    }

    public List<AttendanceExplanation> getAllExplanations() {
        return repository.findAll();
    }

    public Optional<AttendanceExplanation> getExplanationById(Long id) {
        return repository.findById(id);
    }

    public AttendanceExplanation approveExplanation(Long id) {
        Optional<AttendanceExplanation> opt = repository.findById(id);
        if (opt.isPresent()) {
            AttendanceExplanation e = opt.get();
            e.setStatus("APPROVED");
            return repository.save(e);
        }
        throw new RuntimeException("Explanation not found");
    }

    public AttendanceExplanation rejectExplanation(Long id) {
        Optional<AttendanceExplanation> opt = repository.findById(id);
        if (opt.isPresent()) {
            AttendanceExplanation e = opt.get();
            e.setStatus("REJECTED");
            return repository.save(e);
        }
        throw new RuntimeException("Explanation not found");
    }
}
