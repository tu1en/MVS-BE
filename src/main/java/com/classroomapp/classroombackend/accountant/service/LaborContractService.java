package com.classroomapp.classroombackend.accountant.service;

import com.classroomapp.classroombackend.accountant.model.LaborContract;
import com.classroomapp.classroombackend.accountant.repository.LaborContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.classroomapp.classroombackend.accountant.model.ContractStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LaborContractService {
    @Autowired
    private LaborContractRepository laborContractRepository;

    public List<LaborContract> findAll() {
        return laborContractRepository.findAll();
    }

    public Optional<LaborContract> findById(Long id) {
        return laborContractRepository.findById(id);
    }

    public LaborContract save(LaborContract contract) {
        return laborContractRepository.save(contract);
    }

    public void deleteById(Long id) {
        laborContractRepository.deleteById(id);
    }

    public LaborContract terminateContract(Long id) {
        LaborContract contract = laborContractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        contract.setStatus(ContractStatus.TERMINATED);
        return laborContractRepository.save(contract);
    }
}
