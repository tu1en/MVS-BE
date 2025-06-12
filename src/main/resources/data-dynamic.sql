-- Dynamic Homework Test Data for Classroom Management System
-- This file contains dynamically generated test data based on existing classroom context
-- Designed to test "Student Do Assigned Homework" functionality comprehensively

-- Clean up tables for H2 referential integrity
DELETE FROM submissions;
DELETE FROM assignments;
DELETE FROM attendances;
DELETE FROM attendance_sessions;
DELETE FROM classroom_enrollments;
DELETE FROM classrooms;
DELETE FROM users;
DELETE FROM allowed_ips;

-- Add sample users (password: 123456 - encrypted)
INSERT INTO users (username, password, full_name, email, role_id, department, status, enrollment_date) VALUES
('teacher1', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Dr. Nguyễn Văn Minh', 'teacher1@university.edu', 2, 'Computer Science', 'active', '2023-09-01'),
('teacher2', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Th.S Trần Thị Linh', 'teacher2@university.edu', 2, 'Software Engineering', 'active', '2023-09-01'),
('teacher3', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Dr. Lê Quang Đức', 'teacher3@university.edu', 2, 'Database Systems', 'active', '2023-09-01');

-- Add diverse student profiles for comprehensive testing
INSERT INTO users (username, password, full_name, email, role_id, department, status, enrollment_date) VALUES
('student1', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Hoàng Minh Tuấn', 'student1@student.edu', 1, 'Computer Science', 'active', '2024-01-15'),
('student2', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Nguyễn Thị Mai', 'student2@student.edu', 1, 'Computer Science', 'active', '2024-01-15'),
('student3', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Phạm Đức Nam', 'student3@student.edu', 1, 'Software Engineering', 'active', '2024-01-15'),
('student4', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Vũ Thị Hương', 'student4@student.edu', 1, 'Computer Science', 'active', '2024-01-15'),
('student5', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Đỗ Văn Hải', 'student5@student.edu', 1, 'Software Engineering', 'active', '2024-01-15'),
('student6', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Bùi Thị Lan', 'student6@student.edu', 1, 'Database Systems', 'active', '2024-01-15'),
('student7', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Trần Quang Vinh', 'student7@student.edu', 1, 'Computer Science', 'active', '2024-01-15'),
('student8', '$2a$10$rDkPvvAFV6GgJddt4gq5O.YG1AWXzmqHSMtqkN7BT3J8JGKGa4U3W', 'Lê Thị Oanh', 'student8@student.edu', 1, 'Software Engineering', 'active', '2024-01-15');

-- Add diverse classrooms with realistic subjects and descriptions
INSERT INTO classrooms (name, description, section, subject, teacher_id, location_lat, location_lon, allowed_radius) VALUES
('Advanced Java Programming', 'Advanced Java concepts including Spring Boot, microservices, and enterprise patterns. Focus on building scalable web applications.', 'CS401', 'Java Programming', 1, 21.028511, 105.804817, 100.0),
('Modern Web Development', 'Full-stack web development using React, Node.js, and modern JavaScript frameworks. Emphasis on responsive design and user experience.', 'SE302', 'Web Development', 2, 21.007529, 105.783834, 100.0),
('Database Systems Design', 'Database design principles, SQL optimization, NoSQL databases, and data modeling for enterprise applications.', 'DB201', 'Database Systems', 3, 21.015123, 105.798456, 100.0),
('Software Engineering Project', 'Capstone project course focusing on software development lifecycle, team collaboration, and project management.', 'SE499', 'Software Engineering', 2, 21.025789, 105.791234, 100.0);

-- Enroll students in multiple classes for realistic cross-enrollment
INSERT INTO classroom_enrollments (classroom_id, user_id) VALUES
-- Advanced Java Programming class
(1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
-- Modern Web Development class  
(2, 4), (2, 5), (2, 9), (2, 10), (2, 11),
-- Database Systems Design class
(3, 6), (3, 7), (3, 9), (3, 11),
-- Software Engineering Project class
(4, 5), (4, 8), (4, 10), (4, 11);

-- Add sample attendance sessions
INSERT INTO attendance_sessions (title, classroom_id, teacher_id, start_time, end_time, session_type, status, auto_mark, auto_mark_teacher_attendance) VALUES
('Spring Boot Architecture - Session 1', 1, 1, DATEADD(DAY, -3, CURRENT_TIMESTAMP), DATEADD(HOUR, 2, DATEADD(DAY, -3, CURRENT_TIMESTAMP)), 'OFFLINE', 'COMPLETED', true, true),
('React Hooks Workshop', 2, 2, DATEADD(DAY, -2, CURRENT_TIMESTAMP), DATEADD(HOUR, 3, DATEADD(DAY, -2, CURRENT_TIMESTAMP)), 'ONLINE', 'COMPLETED', true, true),
('Database Design Principles', 3, 3, DATEADD(DAY, -1, CURRENT_TIMESTAMP), DATEADD(HOUR, 2, DATEADD(DAY, -1, CURRENT_TIMESTAMP)), 'OFFLINE', 'COMPLETED', true, true);

-- Add allowed IPs
INSERT INTO allowed_ips (ip_address, description) VALUES
('127.0.0.1', 'Development Environment'),
('192.168.1.0/24', 'University Network'),
('10.0.0.0/8', 'Campus WiFi Network');

-- =============================================
-- DYNAMIC HOMEWORK ASSIGNMENTS
-- =============================================

-- Advanced Java Programming Assignments (Classroom ID: 1)
-- Assignment 1: Beginner level - due soon
INSERT INTO assignments (title, description, due_date, points, classroom_id) VALUES
('Spring Boot API Development',
'Create a RESTful API using Spring Boot with CRUD operations for a Student management system. This beginner-level task focuses on basic concepts and straightforward implementation. Include proper HTTP status codes, request/response DTOs, and basic validation. Expected completion time: 3-7 days.',
DATEADD(DAY, 4, CURRENT_TIMESTAMP),
75,
1);

-- Assignment 2: Intermediate level - current
INSERT INTO assignments (title, description, due_date, points, classroom_id) VALUES
('JPA Entity Relationships',
'Implement complex entity relationships using JPA annotations. This intermediate-level task focuses on moderate complexity with multiple components. Create entities for Student, Course, Enrollment with proper OneToMany, ManyToMany relationships. Expected completion time: 7-14 days.',
DATEADD(DAY, 10, CURRENT_TIMESTAMP),
100,
1);

-- Assignment 3: Advanced level - long term
INSERT INTO assignments (title, description, due_date, points, classroom_id) VALUES
('Microservices Architecture Implementation',
'Design and implement a microservices architecture using Spring Cloud. This advanced-level task focuses on complex implementation requiring advanced skills. Include service discovery, API gateway, and distributed configuration. Expected completion time: 14-21 days.',
DATEADD(DAY, 18, CURRENT_TIMESTAMP),
130,
1);

-- Assignment 4: Overdue assignment for testing
INSERT INTO assignments (title, description, due_date, points, classroom_id) VALUES
('Unit Testing with JUnit',
'Implement comprehensive unit tests for your Spring Boot application. This intermediate-level task focuses on moderate complexity with multiple components. Include test coverage reports and integration tests. Expected completion time: 7-14 days.',
DATEADD(DAY, -2, CURRENT_TIMESTAMP),
90,
1);

-- Modern Web Development Assignments (Classroom ID: 2)
-- Assignment 1: Beginner level
INSERT INTO assignments (title, description, due_date, points, classroom_id) VALUES
('React Component Architecture',
'Build a component library with reusable React components. This beginner-level task focuses on basic concepts and straightforward implementation. Create at least 8 functional components with proper prop handling. Expected completion time: 3-7 days.',
DATEADD(DAY, 6, CURRENT_TIMESTAMP),
80,
2);

-- Assignment 2: Intermediate level
INSERT INTO assignments (title, description, due_date, points, classroom_id) VALUES
('State Management with Redux',
'Implement global state management using Redux Toolkit. This intermediate-level task focuses on moderate complexity with multiple components. Include actions, reducers, and middleware for async operations. Expected completion time: 7-14 days.',
DATEADD(DAY, 12, CURRENT_TIMESTAMP),
110,
2);

-- Assignment 3: Advanced level
INSERT INTO assignments (title, description, due_date, points, classroom_id) VALUES
('Authentication Implementation',
'Create a complete authentication system with JWT tokens. This advanced-level task focuses on complex implementation requiring advanced skills. Include login, registration, password reset, and protected routes. Expected completion time: 14-21 days.',
DATEADD(DAY, 20, CURRENT_TIMESTAMP),
140,
2);

-- Assignment 4: Overdue for variety
INSERT INTO assignments (title, description, due_date, points, classroom_id) VALUES
('Responsive UI Design',
'Create a fully responsive web application using CSS Grid and Flexbox. This intermediate-level task focuses on moderate complexity with multiple components. Support mobile, tablet, and desktop viewports. Expected completion time: 7-14 days.',
DATEADD(DAY, -5, CURRENT_TIMESTAMP),
95,
2);

-- Database Systems Design Assignments (Classroom ID: 3)
INSERT INTO assignments (title, description, due_date, points, classroom_id) VALUES
('Database Schema Design',
'Design a normalized database schema for an e-commerce platform. This intermediate-level task focuses on moderate complexity with multiple components. Include ER diagrams, normalization analysis, and SQL DDL scripts. Expected completion time: 7-14 days.',
DATEADD(DAY, 8, CURRENT_TIMESTAMP),
105,
3);

INSERT INTO assignments (title, description, due_date, points, classroom_id) VALUES
('Query Optimization',
'Optimize database queries for performance. This advanced-level task focuses on complex implementation requiring advanced skills. Include execution plans, indexing strategies, and performance benchmarks. Expected completion time: 14-21 days.',
DATEADD(DAY, 16, CURRENT_TIMESTAMP),
125,
3);

-- Software Engineering Project Assignments (Classroom ID: 4)
INSERT INTO assignments (title, description, due_date, points, classroom_id) VALUES
('Requirements Analysis',
'Conduct comprehensive requirements analysis for your capstone project. This intermediate-level task focuses on moderate complexity with multiple components. Include functional/non-functional requirements and use case diagrams. Expected completion time: 7-14 days.',
DATEADD(DAY, 14, CURRENT_TIMESTAMP),
115,
4);

INSERT INTO assignments (title, description, due_date, points, classroom_id) VALUES
('System Architecture Design',
'Design the system architecture for your project. This advanced-level task focuses on complex implementation requiring advanced skills. Include component diagrams, deployment architecture, and technology stack decisions. Expected completion time: 14-21 days.',
DATEADD(DAY, 21, CURRENT_TIMESTAMP),
135,
4);

-- =============================================
-- DYNAMIC STUDENT SUBMISSIONS 
-- =============================================

-- Excellent Student (student1 - Hoàng Minh Tuấn) - 90% submit rate, 95% on time, avg 85
INSERT INTO submissions (assignment_id, student_id, content, submitted_at, status, grade, feedback) VALUES
(1, 4, 'Implemented REST API with CRUD operations. GitHub repository includes full documentation, proper error handling, and comprehensive testing. Added Swagger integration for API documentation.', 
 DATEADD(DAY, -1, CURRENT_TIMESTAMP), 'GRADED', 92, 'Excellent work! Demonstrates strong understanding of Spring Boot concepts and best practices.'),
(2, 4, 'Created Spring Boot application with JPA integration and comprehensive entity relationships. Included proper validation, custom queries, and database migrations.', 
 DATEADD(DAY, -2, CURRENT_TIMESTAMP), 'GRADED', 88, 'Outstanding implementation! Great attention to detail in entity relationships.'),
(4, 4, 'Comprehensive unit testing suite with 95% code coverage. Included integration tests, mock services, and automated CI/CD pipeline setup.', 
 DATEADD(DAY, -3, CURRENT_TIMESTAMP), 'GRADED', 95, 'Exceptional testing strategy! This is exactly what industry expects.'),
(5, 4, 'Built reusable component library with TypeScript and comprehensive testing. Included Storybook documentation and accessibility features.', 
 DATEADD(DAY, -1, CURRENT_TIMESTAMP), 'GRADED', 90, 'Excellent component design and documentation! TypeScript usage is impressive.');

-- Good Student (student2 - Nguyễn Thị Mai) - 80% submit rate, 80% on time, avg 75  
INSERT INTO submissions (assignment_id, student_id, content, submitted_at, status, grade, feedback) VALUES
(1, 5, 'Created Spring Boot application with JPA integration and proper error handling. Added basic validation and documented API endpoints.', 
 DATEADD(DAY, -1, CURRENT_TIMESTAMP), 'SUBMITTED', NULL, NULL),
(4, 5, 'Unit testing implementation with JUnit 5. Covered main service classes and repository layers. Working on improving test coverage.', 
 DATEADD(HOUR, -6, CURRENT_TIMESTAMP), 'GRADED', 78, 'Good implementation! Consider adding more edge case testing for better coverage.'),
(5, 5, 'Developed React components with proper state management and props handling. Included basic responsive design features.', 
 DATEADD(DAY, -2, CURRENT_TIMESTAMP), 'GRADED', 82, 'Good component architecture! Minor improvements needed in prop validation.'),
(9, 5, 'Database schema design completed with ER diagrams and normalization analysis. Included sample data and basic queries.', 
 DATEADD(DAY, -1, CURRENT_TIMESTAMP), 'SUBMITTED', NULL, NULL);

-- Average Student (student3 - Phạm Đức Nam) - 70% submit rate, 60% on time, avg 65
INSERT INTO submissions (assignment_id, student_id, content, submitted_at, status, grade, feedback) VALUES
(2, 6, 'JPA entity relationships implemented with basic annotations. Still working on complex queries and performance optimization.', 
 DATEADD(DAY, 1, CURRENT_TIMESTAMP), 'SUBMITTED', NULL, NULL), -- Late submission
(4, 6, 'Basic unit tests for main controllers. Need to add service layer testing and improve coverage metrics.', 
 DATEADD(DAY, -1, CURRENT_TIMESTAMP), 'GRADED', 65, 'Satisfactory work. Please focus on testing service layer and edge cases.'),
(8, 6, 'Responsive design implementation using CSS Grid. Mobile layout completed, working on tablet optimization.', 
 DATEADD(DAY, -3, CURRENT_TIMESTAMP), 'GRADED', 58, 'Late submission affected the grade. Basic requirements met but needs refinement.'),
(11, 6, 'Requirements analysis document with basic use cases. Need to expand functional requirements and add more detail.', 
 DATEADD(DAY, -2, CURRENT_TIMESTAMP), 'SUBMITTED', NULL, NULL);

-- Struggling Student (student4 - Vũ Thị Hương) - 50% submit rate, 40% on time, avg 50
INSERT INTO submissions (assignment_id, student_id, content, submitted_at, status, grade, feedback) VALUES
(4, 7, 'Basic unit tests implemented. Having difficulty with mocking and integration testing concepts.', 
 DATEADD(DAY, -5, CURRENT_TIMESTAMP), 'GRADED', 45, 'Late submission. Please schedule office hours to discuss testing concepts and best practices.'),
(8, 7, 'Started responsive design implementation. CSS Grid basics completed but having issues with complex layouts.', 
 DATEADD(DAY, -7, CURRENT_TIMESTAMP), 'GRADED', 42, 'Very late submission. Basic concepts understood but execution needs improvement. Please review course materials.'),
(5, 7, 'Created basic React components. Still learning about state management and component lifecycle.', 
 DATEADD(DAY, -1, CURRENT_TIMESTAMP), 'SUBMITTED', NULL, NULL);

-- Inconsistent Student (student5 - Đỗ Văn Hải) - 60% submit rate, 70% on time, avg 70
INSERT INTO submissions (assignment_id, student_id, content, submitted_at, status, grade, feedback) VALUES
(1, 8, 'REST API implementation with Spring Boot. Good structure but missing some advanced features like pagination.', 
 DATEADD(DAY, -2, CURRENT_TIMESTAMP), 'GRADED', 75, 'Good foundation! Consider adding pagination and advanced query features.'),
(3, 8, 'Microservices architecture design completed. Implemented service discovery but still working on API gateway configuration.', 
 DATEADD(DAY, -1, CURRENT_TIMESTAMP), 'SUBMITTED', NULL, NULL),
(6, 8, 'Redux implementation for state management. Actions and reducers completed, working on middleware integration.', 
 DATEADD(DAY, -3, CURRENT_TIMESTAMP), 'GRADED', 72, 'Good understanding of Redux concepts! Middleware implementation shows promise.'),
(12, 8, 'System architecture design with component diagrams. Technology stack decisions documented with justifications.', 
 DATEADD(DAY, -1, CURRENT_TIMESTAMP), 'SUBMITTED', NULL, NULL);

-- Improving Student (student6 - Bùi Thị Lan) - 75% submit rate, 65% on time, avg 68
INSERT INTO submissions (assignment_id, student_id, content, submitted_at, status, grade, feedback) VALUES
(2, 9, 'JPA entity relationships with proper annotations and cascading. Added custom repository methods for complex queries.', 
 DATEADD(DAY, -1, CURRENT_TIMESTAMP), 'GRADED', 80, 'Significant improvement! Custom repository methods show good understanding.'),
(5, 9, 'React component library with improved prop validation and documentation. Added unit tests for critical components.', 
 DATEADD(DAY, -2, CURRENT_TIMESTAMP), 'GRADED', 76, 'Great progress! Testing addition shows commitment to quality.'),
(9, 9, 'Database schema with normalization and indexing strategy. Included performance considerations and sample queries.', 
 DATEADD(DAY, -1, CURRENT_TIMESTAMP), 'SUBMITTED', NULL, NULL),
(11, 9, 'Comprehensive requirements analysis with functional and non-functional requirements. Added user stories and acceptance criteria.', 
 DATEADD(DAY, -1, CURRENT_TIMESTAMP), 'SUBMITTED', NULL, NULL);

-- Additional submissions for variety
INSERT INTO submissions (assignment_id, student_id, content, submitted_at, status, grade, feedback) VALUES
(6, 10, 'Redux Toolkit implementation with modern patterns. Included RTK Query for API state management.', 
 DATEADD(DAY, -2, CURRENT_TIMESTAMP), 'GRADED', 85, 'Excellent use of modern Redux patterns! RTK Query integration is impressive.'),
(7, 10, 'Authentication system with JWT, refresh tokens, and role-based access control. Included password strength validation.', 
 DATEADD(DAY, -1, CURRENT_TIMESTAMP), 'SUBMITTED', NULL, NULL),
(10, 11, 'Query optimization with execution plans and indexing strategies. Included before/after performance benchmarks.', 
 DATEADD(DAY, -1, CURRENT_TIMESTAMP), 'SUBMITTED', NULL, NULL),
(12, 11, 'Complete system architecture with microservices design, deployment strategies, and scalability considerations.', 
 DATEADD(DAY, -2, CURRENT_TIMESTAMP), 'GRADED', 88, 'Comprehensive architecture design! Scalability considerations are well thought out.');

-- Add some non-submitted assignments to show variety in submission patterns
-- (Some students haven't submitted certain assignments, which is realistic)
