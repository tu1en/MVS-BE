package com.classroomapp.classroombackend.repository.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.AllowedNetwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AllowedNetworkRepository extends JpaRepository<AllowedNetwork, Long> {
    List<AllowedNetwork> findByActiveTrue();
}