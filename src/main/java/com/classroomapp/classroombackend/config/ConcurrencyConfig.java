package com.classroomapp.classroombackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.Executor;

/**
 * Configuration for handling concurrency issues and preventing race conditions
 * Addresses the database deadlock issues identified in the logs
 */
@Configuration
public class ConcurrencyConfig {

    /**
     * Semaphore to limit concurrent class update operations
     * This prevents multiple PUT requests from causing database deadlocks
     */
    private final ConcurrentHashMap<Long, Semaphore> classUpdateSemaphores = new ConcurrentHashMap<>();
    
    /**
     * Get or create a semaphore for a specific class ID
     * This ensures only one update operation per class at a time
     */
    public Semaphore getClassUpdateSemaphore(Long classId) {
        return classUpdateSemaphores.computeIfAbsent(classId, k -> new Semaphore(1));
    }
    
    /**
     * Thread pool for async operations to prevent blocking main request threads
     */
    @Bean(name = "classAsyncExecutor")
    public Executor classAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("ClassAsync-");
        executor.setRejectedExecutionHandler((r, executor1) -> {
            // Log rejected tasks instead of throwing exception
            System.err.println("Task rejected: " + r.toString());
        });
        executor.initialize();
        return executor;
    }
    
    /**
     * Global semaphore for auto-sync operations
     * Prevents multiple sync operations from running simultaneously
     */
    @Bean
    public Semaphore autoSyncSemaphore() {
        return new Semaphore(1); // Only allow one sync operation at a time
    }
}
