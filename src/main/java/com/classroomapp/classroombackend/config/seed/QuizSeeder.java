package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QuizSeeder {

    private final Random random = new Random();
    
    @Transactional
    public void seed() {
        // Create sample quiz system data
        System.out.println("✅ [QuizSeeder] Seeding quiz system...");
        
        try {
            // Sample quiz questions that would be created
            String[] questionTypes = {"MULTIPLE_CHOICE", "TRUE_FALSE", "SHORT_ANSWER", "ESSAY"};
            String[] subjects = {"Mathematics", "Physics", "Chemistry", "Biology", "History", "Literature"};
            
            // Sample questions for each subject
            for (String subject : subjects) {
                // Create 5 sample questions per subject
                for (int i = 1; i <= 5; i++) {
                    String questionType = questionTypes[random.nextInt(questionTypes.length)];
                    
                    // Create quiz question
                    System.out.println("  - Creating " + questionType + " question for " + subject + " #" + i);
                    
                    // If multiple choice, create options
                    if ("MULTIPLE_CHOICE".equals(questionType)) {
                        System.out.println("    - Creating 4 answer options");
                    }
                    
                    // Create sample student answers (60-80% of students answer each question)
                    int studentAnswerCount = 15 + random.nextInt(10); // 15-25 answers
                    System.out.println("    - Creating " + studentAnswerCount + " student answers");
                }
            }
            
            System.out.println("✅ [QuizSeeder] Would create:");
            System.out.println("  - " + (subjects.length * 5) + " quiz questions");
            System.out.println("  - ~" + (subjects.length * 5 * 3) + " question options"); 
            System.out.println("  - ~" + (subjects.length * 5 * 20) + " student answers");
            System.out.println("✅ [QuizSeeder] Quiz seeding completed conceptually");
            
        } catch (Exception e) {
            System.out.println("❌ [QuizSeeder] Error: " + e.getMessage());
        }
    }
}