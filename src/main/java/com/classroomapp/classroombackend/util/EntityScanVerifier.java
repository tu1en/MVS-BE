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
        log.info("🔍 Verifying Entity Scanning for Hibernate + SQL Server...");
        
        var metamodel = entityManager.getMetamodel();
        var entities = metamodel.getEntities();
        
        log.info("✅ Found {} entities:", entities.size());
        
        for (EntityType<?> entity : entities) {
            String entityName = entity.getName();
            String javaType = entity.getJavaType().getSimpleName();
            String packageName = entity.getJavaType().getPackageName();
            
            log.info("📋 Entity: {} (Class: {}) in package: {}", 
                entityName, javaType, packageName);
        }
        
        log.info("✅ Entity scanning verification completed!");
        log.info("🗄️ Hibernate will now create/update tables in SQL Server");
    }
}
