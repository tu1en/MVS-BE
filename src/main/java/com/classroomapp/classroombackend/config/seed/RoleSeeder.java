package com.classroomapp.classroombackend.config.seed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.usermanagement.Role;
import com.classroomapp.classroombackend.repository.usermanagement.RoleRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class RoleSeeder {

    @Autowired
    private RoleRepository roleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void seed() {
        if (roleRepository.count() == 0) {
            try {
                entityManager.createNativeQuery("SET IDENTITY_INSERT roles ON").executeUpdate();

                Role student = new Role("STUDENT");
                student.setId(1);
                roleRepository.save(student);

                Role teacher = new Role("TEACHER");
                teacher.setId(2);
                roleRepository.save(teacher);

                Role manager = new Role("MANAGER");
                manager.setId(3);
                roleRepository.save(manager);

                Role admin = new Role("ADMIN");
                admin.setId(4);
                roleRepository.save(admin);

                System.out.println("✅ [RoleSeeder] Created roles with explicit IDs.");

            } finally {
                entityManager.createNativeQuery("SET IDENTITY_INSERT roles OFF").executeUpdate();
            }
        } else {
            System.out.println("✅ [RoleSeeder] Roles already seeded.");
        }
    }
} 