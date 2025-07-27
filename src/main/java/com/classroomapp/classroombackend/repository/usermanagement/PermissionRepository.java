package com.classroomapp.classroombackend.repository.usermanagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.usermanagement.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
}