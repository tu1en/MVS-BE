package com.classroomapp.classroombackend.config.seed;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ComprehensiveTableSeeder {

    @Transactional
    public void seed() {
        System.out.println("📊 [ComprehensiveTableSeeder] Skipped seeding (placeholder only).");
    }
}
