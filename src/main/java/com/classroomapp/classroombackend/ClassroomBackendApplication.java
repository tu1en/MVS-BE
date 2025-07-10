package com.classroomapp.classroombackend;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.classroomapp.classroombackend.config.seed.ClassroomEnrollmentSeeder;
import com.classroomapp.classroombackend.config.seed.ClassroomSeeder;
import com.classroomapp.classroombackend.config.seed.LectureSeeder;
import com.classroomapp.classroombackend.config.seed.RoleSeeder;
import com.classroomapp.classroombackend.config.seed.UserSeeder;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;

@SpringBootApplication
@EnableCaching
@Configuration
@ComponentScan(basePackages = "com.classroomapp.classroombackend")
public class ClassroomBackendApplication implements WebMvcConfigurer {
    
    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        SpringApplication.run(ClassroomBackendApplication.class, args);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    @Bean
    public CommandLineRunner commandLineRunner(RoleSeeder roleSeeder, UserSeeder userSeeder, ClassroomSeeder classroomSeeder, ClassroomEnrollmentSeeder enrollmentSeeder, LectureSeeder lectureSeeder) {
        return args -> {
            System.out.println("--- Seeding Database ---");
            roleSeeder.seed();
            userSeeder.seed();
            List<Classroom> seededClassrooms = classroomSeeder.seed();

            // Enroll students. This seeder will find users and classrooms on its own.
            enrollmentSeeder.seed();

            // Now create lectures for those classrooms
            lectureSeeder.seed(seededClassrooms);
            System.out.println("--- Seeding Complete ---");
        };
    }
}