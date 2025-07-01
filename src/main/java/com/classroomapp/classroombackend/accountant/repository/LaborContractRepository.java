package com.classroomapp.classroombackend.accountant.repository;

import com.classroomapp.classroombackend.accountant.model.LaborContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LaborContractRepository extends JpaRepository<LaborContract, Long> {
}
