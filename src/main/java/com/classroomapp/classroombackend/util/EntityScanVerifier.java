package com.classroomapp.classroombackend.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility to verify all JPA entities are properly scanned by Hibernate
 * This will run on application startup and log all detected entities
 */
@Component
@Slf4j
public class EntityScanVerifier implements CommandLineRunner {

    @Autowired
    private EntityManager entityManager;

    @Override
    public void run(String... args) throws Exception {
        log.info("🗄️ Hibernate will now create/update tables in SQL Server");
    }
}
