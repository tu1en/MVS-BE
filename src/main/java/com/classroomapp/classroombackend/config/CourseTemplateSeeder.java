package com.classroomapp.classroombackend.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.entity.ClassEntity;
import com.classroomapp.classroombackend.entity.ClassLesson;
import com.classroomapp.classroombackend.entity.LessonTemplate;
import com.classroomapp.classroombackend.entity.Room;
import com.classroomapp.classroombackend.model.classroommanagement.CourseTemplate;
import com.classroomapp.classroombackend.model.classroommanagement.TemplateStatus;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ClassLessonRepository;
import com.classroomapp.classroombackend.repository.ClassRepository;
import com.classroomapp.classroombackend.repository.CourseTemplateRepository;
import com.classroomapp.classroombackend.repository.LessonTemplateRepository;
import com.classroomapp.classroombackend.repository.RoomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
@Order(2) // Run after main DataLoader
public class CourseTemplateSeeder implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(CourseTemplateSeeder.class);
    
    @Autowired
    private CourseTemplateRepository courseTemplateRepository;
    
    @Autowired
    private LessonTemplateRepository lessonTemplateRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private ClassLessonRepository classLessonRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    private Random random = new Random();
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("🎓 Starting Course Template Seeder...");
        
        try {
            // Check if course templates already exist
            if (courseTemplateRepository.count() > 0) {
                log.info("📚 Course templates already exist. Skipping seeding.");
                return;
            }
            
            // Get an admin/teacher user for creation
            Long createdBy = userRepository.findAll().stream()
                .findFirst()
                .map(user -> user.getId())
                .orElse(1L);
            
            // Seed data
            seedRooms(); // Create rooms first
            List<CourseTemplate> courseTemplates = seedCourseTemplates(createdBy);
            seedClasses(courseTemplates, createdBy);
            
            log.info("✅ Course Template Seeder completed successfully!");
            
        } catch (Exception e) {
            log.error("❌ Error in Course Template Seeder: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void seedRooms() {
        log.info("🏢 Creating sample rooms...");
        
        if (roomRepository.count() > 0) {
            log.info("Rooms already exist. Skipping room creation.");
            return;
        }
        
        List<RoomData> roomsData = Arrays.asList(
            new RoomData("A101", "Phòng học A101", "Tầng 1 - Tòa A", 30, true),
            new RoomData("A102", "Phòng học A102", "Tầng 1 - Tòa A", 25, true),
            new RoomData("A201", "Phòng học A201", "Tầng 2 - Tòa A", 35, true),
            new RoomData("A202", "Phòng học A202", "Tầng 2 - Tòa A", 30, true),
            new RoomData("B101", "Phòng máy tính B101", "Tầng 1 - Tòa B", 20, true),
            new RoomData("B102", "Phòng máy tính B102", "Tầng 1 - Tòa B", 20, true),
            new RoomData("B201", "Phòng thực hành B201", "Tầng 2 - Tòa B", 25, true),
            new RoomData("C101", "Phòng hội thảo C101", "Tầng 1 - Tòa C", 50, true),
            new RoomData("C102", "Phòng học C102", "Tầng 1 - Tòa C", 40, true),
            new RoomData("LAB1", "Phòng thí nghiệm LAB1", "Tầng hầm", 15, true)
        );
        
        for (RoomData roomData : roomsData) {
            Room room = new Room();
            room.setRoomCode(roomData.roomCode);
            room.setRoomName(roomData.roomName);
            room.setLocation(roomData.location);
            room.setCapacity(roomData.capacity);
            room.setIsActive(roomData.isActive);
            room.setCreatedAt(LocalDateTime.now());
            roomRepository.save(room);
        }
        
        log.info("✅ Created {} rooms", roomsData.size());
    }
    
    private List<CourseTemplate> seedCourseTemplates(Long createdBy) {
        log.info("🌱 Creating course templates with lesson schedules...");
        
        // Create Java Programming Course
        CourseTemplate javaCourse = createJavaCourse(createdBy);
        createJavaLessons(javaCourse);
        
        // Create Web Development Course  
        CourseTemplate webCourse = createWebDevelopmentCourse(createdBy);
        createWebDevelopmentLessons(webCourse);
        
        // Create Database Design Course
        CourseTemplate dbCourse = createDatabaseCourse(createdBy);
        createDatabaseLessons(dbCourse);
        
        // Create Mobile App Development Course
        CourseTemplate mobileCourse = createMobileCourse(createdBy);
        createMobileLessons(mobileCourse);
        
        // Create Data Structures & Algorithms Course
        CourseTemplate dsaCourse = createDataStructuresCourse(createdBy);
        createDataStructuresLessons(dsaCourse);
        
        List<CourseTemplate> courseTemplates = Arrays.asList(javaCourse, webCourse, dbCourse, mobileCourse, dsaCourse);
        log.info("📊 Created {} course templates with lesson schedules", courseTemplates.size());
        
        return courseTemplates;
    }
    
    private CourseTemplate createJavaCourse(Long createdBy) {
        CourseTemplate course = new CourseTemplate();
        course.setName("Java Programming Fundamentals");
        course.setDescription("Comprehensive introduction to Java programming language covering object-oriented programming concepts, data structures, and application development.");
        course.setSubject("Computer Science");
        course.setTotalWeeks(16);
        course.setCreatedBy(createdBy);
        course.setStatus(TemplateStatus.ACTIVE);
        course.setIsActive(true);
        course.setIsPublic(true);
        course.setEnrollmentFee(new BigDecimal("2500000")); // 2.5M VND
        course.setMaxStudentsPerTemplate(25);
        
        return courseTemplateRepository.save(course);
    }
    
    private CourseTemplate createWebDevelopmentCourse(Long createdBy) {
        CourseTemplate course = new CourseTemplate();
        course.setName("Full-Stack Web Development");
        course.setDescription("Complete web development course covering HTML, CSS, JavaScript, React, Node.js, and database integration for modern web applications.");
        course.setSubject("Web Development");
        course.setTotalWeeks(20);
        course.setCreatedBy(createdBy);
        course.setStatus(TemplateStatus.ACTIVE);
        course.setIsActive(true);
        course.setIsPublic(true);
        course.setEnrollmentFee(new BigDecimal("3500000")); // 3.5M VND
        course.setMaxStudentsPerTemplate(20);
        
        return courseTemplateRepository.save(course);
    }
    
    private CourseTemplate createDatabaseCourse(Long createdBy) {
        CourseTemplate course = new CourseTemplate();
        course.setName("Database Design & SQL");
        course.setDescription("Learn database design principles, SQL programming, and database management systems including MySQL, PostgreSQL, and MongoDB.");
        course.setSubject("Database Technology");
        course.setTotalWeeks(12);
        course.setCreatedBy(createdBy);
        course.setStatus(TemplateStatus.ACTIVE);
        course.setIsActive(true);
        course.setIsPublic(true);
        course.setEnrollmentFee(new BigDecimal("2000000")); // 2M VND
        course.setMaxStudentsPerTemplate(30);
        
        return courseTemplateRepository.save(course);
    }
    
    private CourseTemplate createMobileCourse(Long createdBy) {
        CourseTemplate course = new CourseTemplate();
        course.setName("Mobile App Development");
        course.setDescription("Build native and cross-platform mobile applications using React Native, Flutter, and native Android/iOS development techniques.");
        course.setSubject("Mobile Development");
        course.setTotalWeeks(18);
        course.setCreatedBy(createdBy);
        course.setStatus(TemplateStatus.ACTIVE);
        course.setIsActive(true);
        course.setIsPublic(true);
        course.setEnrollmentFee(new BigDecimal("4000000")); // 4M VND
        course.setMaxStudentsPerTemplate(15);
        
        return courseTemplateRepository.save(course);
    }
    
    private CourseTemplate createDataStructuresCourse(Long createdBy) {
        CourseTemplate course = new CourseTemplate();
        course.setName("Data Structures & Algorithms");
        course.setDescription("Master fundamental data structures and algorithms including arrays, linked lists, trees, graphs, sorting, and searching algorithms.");
        course.setSubject("Computer Science");
        course.setTotalWeeks(14);
        course.setCreatedBy(createdBy);
        course.setStatus(TemplateStatus.ACTIVE);
        course.setIsActive(true);
        course.setIsPublic(true);
        course.setEnrollmentFee(new BigDecimal("3000000")); // 3M VND
        course.setMaxStudentsPerTemplate(25);
        
        return courseTemplateRepository.save(course);
    }
    
    private void createJavaLessons(CourseTemplate course) {
        List<LessonData> lessons = Arrays.asList(
            new LessonData(1, "Java Introduction & Setup", "Theory", "Introduction to Java programming, JDK installation, IDE setup", "Computer with internet", "Install JDK and IDE", 120),
            new LessonData(2, "Variables & Data Types", "Practice", "Learn Java variables, primitive types, and basic operations", "Java environment", "Review data type concepts", 120),
            new LessonData(3, "Control Structures", "Practice", "If-else statements, loops, switch statements", "Basic Java knowledge", "Practice control flow problems", 120),
            new LessonData(4, "Methods & Functions", "Practice", "Method declaration, parameters, return types, overloading", "Control structures", "Create utility methods", 120),
            new LessonData(5, "Object-Oriented Programming Basics", "Theory", "Classes, objects, constructors, and instance variables", "Methods knowledge", "Design simple classes", 120),
            new LessonData(6, "Inheritance & Polymorphism", "Theory", "Inheritance, method overriding, polymorphism concepts", "OOP basics", "Implement inheritance examples", 120),
            new LessonData(7, "Arrays & Collections", "Practice", "Arrays, ArrayList, HashMap, and collection operations", "OOP knowledge", "Work with collection exercises", 120),
            new LessonData(8, "Exception Handling", "Practice", "Try-catch, throws, custom exceptions", "Collections", "Handle exceptions properly", 120),
            new LessonData(9, "File I/O Operations", "Practice", "Reading/writing files, streams, serialization", "Exception handling", "Create file processing programs", 120),
            new LessonData(10, "Java GUI with Swing", "Practice", "Creating desktop applications with Swing components", "File I/O", "Build GUI applications", 120),
            new LessonData(11, "Database Connectivity (JDBC)", "Practice", "Connecting Java to databases, executing SQL queries", "GUI basics", "Connect to database", 120),
            new LessonData(12, "Multithreading Basics", "Theory", "Threads, synchronization, concurrent programming", "JDBC", "Implement basic threading", 120),
            new LessonData(13, "Java 8+ Features", "Practice", "Lambda expressions, streams, optional", "Multithreading", "Use modern Java features", 120),
            new LessonData(14, "Design Patterns", "Theory", "Common design patterns: Singleton, Factory, Observer", "Java 8 features", "Apply design patterns", 120),
            new LessonData(15, "Testing with JUnit", "Practice", "Unit testing, test-driven development", "Design patterns", "Write comprehensive tests", 120),
            new LessonData(16, "Final Project", "Project", "Complete Java application development project", "All previous lessons", "Present final project", 180)
        );
        
        createLessonsForCourse(course, lessons);
    }
    
    private void createWebDevelopmentLessons(CourseTemplate course) {
        List<LessonData> lessons = Arrays.asList(
            new LessonData(1, "Web Development Introduction", "Theory", "Web technologies overview, client-server architecture", "Basic computer skills", "Set up development environment", 120),
            new LessonData(2, "HTML Fundamentals", "Practice", "HTML structure, tags, forms, semantic elements", "Web basics", "Create HTML pages", 120),
            new LessonData(3, "CSS Styling", "Practice", "CSS selectors, properties, layout techniques", "HTML", "Style web pages", 120),
            new LessonData(4, "Responsive Design", "Practice", "Media queries, flexbox, grid layout", "CSS basics", "Create responsive layouts", 120),
            new LessonData(5, "JavaScript Fundamentals", "Practice", "Variables, functions, DOM manipulation", "HTML/CSS", "Interactive web pages", 120),
            new LessonData(6, "Advanced JavaScript", "Practice", "ES6+, async programming, APIs", "JS basics", "Work with modern JavaScript", 120),
            new LessonData(7, "React Introduction", "Theory", "Component-based architecture, JSX, props", "Advanced JS", "Build React components", 120),
            new LessonData(8, "React State & Lifecycle", "Practice", "State management, lifecycle methods, hooks", "React basics", "Manage component state", 120),
            new LessonData(9, "React Router & Navigation", "Practice", "Single-page applications, routing", "React state", "Implement navigation", 120),
            new LessonData(10, "Node.js Backend", "Practice", "Server-side JavaScript, Express.js", "Frontend skills", "Create backend APIs", 120),
            new LessonData(11, "Database Integration", "Practice", "MongoDB, Mongoose, database operations", "Node.js", "Connect to database", 120),
            new LessonData(12, "Authentication & Security", "Practice", "JWT, password hashing, security practices", "Database", "Implement authentication", 120),
            new LessonData(13, "API Development", "Practice", "RESTful APIs, testing with Postman", "Authentication", "Create robust APIs", 120),
            new LessonData(14, "Frontend-Backend Integration", "Practice", "Connecting React with Node.js APIs", "API development", "Full-stack integration", 120),
            new LessonData(15, "Deployment & DevOps", "Practice", "Hosting, CI/CD, environment management", "Full integration", "Deploy applications", 120),
            new LessonData(16, "Advanced Topics", "Theory", "Performance optimization, SEO, PWAs", "Deployment", "Optimize applications", 120),
            new LessonData(17, "Project Planning", "Project", "Plan and design capstone project", "All skills", "Create project plan", 120),
            new LessonData(18, "Project Development 1", "Project", "Backend development and API creation", "Project plan", "Develop backend", 180),
            new LessonData(19, "Project Development 2", "Project", "Frontend development and integration", "Backend ready", "Complete frontend", 180),
            new LessonData(20, "Project Presentation", "Project", "Final project demonstration and review", "Complete project", "Present final project", 120)
        );
        
        createLessonsForCourse(course, lessons);
    }
    
    private void createDatabaseLessons(CourseTemplate course) {
        List<LessonData> lessons = Arrays.asList(
            new LessonData(1, "Database Concepts", "Theory", "DBMS introduction, relational model, ACID properties", "Basic computer skills", "Understand database fundamentals", 120),
            new LessonData(2, "Relational Database Design", "Theory", "ER diagrams, normalization, table relationships", "DB concepts", "Design database schema", 120),
            new LessonData(3, "SQL Basics", "Practice", "SELECT, INSERT, UPDATE, DELETE operations", "DB design", "Write basic SQL queries", 120),
            new LessonData(4, "Advanced SQL Queries", "Practice", "JOINs, subqueries, aggregate functions", "SQL basics", "Complex data retrieval", 120),
            new LessonData(5, "Database Functions & Procedures", "Practice", "Stored procedures, triggers, user-defined functions", "Advanced SQL", "Implement business logic in DB", 120),
            new LessonData(6, "Database Indexing", "Theory", "Index types, query optimization, performance tuning", "Functions/procedures", "Optimize database performance", 120),
            new LessonData(7, "Transaction Management", "Theory", "Transactions, concurrency control, locking", "Indexing", "Manage data consistency", 120),
            new LessonData(8, "NoSQL Databases", "Theory", "MongoDB, document databases, JSON operations", "Transactions", "Work with NoSQL", 120),
            new LessonData(9, "Database Security", "Practice", "User management, permissions, data encryption", "NoSQL basics", "Secure database systems", 120),
            new LessonData(10, "Backup & Recovery", "Practice", "Backup strategies, disaster recovery, migration", "Security", "Implement backup solutions", 120),
            new LessonData(11, "Database Integration", "Practice", "Connecting applications to databases", "Backup/recovery", "Build database applications", 120),
            new LessonData(12, "Final Database Project", "Project", "Complete database design and implementation", "All topics", "Present database solution", 180)
        );
        
        createLessonsForCourse(course, lessons);
    }
    
    private void createMobileLessons(CourseTemplate course) {
        List<LessonData> lessons = Arrays.asList(
            new LessonData(1, "Mobile Development Overview", "Theory", "Mobile platforms, native vs cross-platform", "Programming basics", "Choose development approach", 120),
            new LessonData(2, "React Native Setup", "Practice", "Environment setup, first React Native app", "Web development", "Create mobile app structure", 120),
            new LessonData(3, "React Native Components", "Practice", "Views, Text, Image, ScrollView, FlatList", "React Native basics", "Build UI components", 120),
            new LessonData(4, "Navigation & Routing", "Practice", "Stack, tab, drawer navigation", "Components", "Implement app navigation", 120),
            new LessonData(5, "State Management", "Practice", "Local state, Context API, Redux", "Navigation", "Manage application state", 120),
            new LessonData(6, "API Integration", "Practice", "Fetch data, REST APIs, async operations", "State management", "Connect to backend services", 120),
            new LessonData(7, "Local Storage", "Practice", "AsyncStorage, SQLite, data persistence", "API integration", "Store data locally", 120),
            new LessonData(8, "Device Features", "Practice", "Camera, GPS, contacts, notifications", "Local storage", "Access device capabilities", 120),
            new LessonData(9, "Styling & Animations", "Practice", "Flexbox, animations, custom styling", "Device features", "Create engaging UI", 120),
            new LessonData(10, "Native Modules", "Theory", "Bridge to native code, third-party libraries", "Styling", "Extend app functionality", 120),
            new LessonData(11, "Testing Mobile Apps", "Practice", "Unit testing, integration testing, debugging", "Native modules", "Ensure app quality", 120),
            new LessonData(12, "App Store Deployment", "Practice", "Build process, app store submission", "Testing", "Publish mobile apps", 120),
            new LessonData(13, "Flutter Introduction", "Theory", "Dart language, Flutter widgets", "React Native experience", "Learn alternative framework", 120),
            new LessonData(14, "Flutter Development", "Practice", "Build Flutter app, compare with RN", "Flutter intro", "Develop with Flutter", 120),
            new LessonData(15, "Performance Optimization", "Practice", "App performance, memory management", "Multiple frameworks", "Optimize mobile apps", 120),
            new LessonData(16, "Project Development 1", "Project", "Plan and start mobile app project", "All skills", "Begin final project", 180),
            new LessonData(17, "Project Development 2", "Project", "Continue mobile app development", "Project started", "Complete core features", 180),
            new LessonData(18, "Project Finalization", "Project", "Testing, deployment, presentation", "Nearly complete project", "Finalize and present", 120)
        );
        
        createLessonsForCourse(course, lessons);
    }
    
    private void createDataStructuresLessons(CourseTemplate course) {
        List<LessonData> lessons = Arrays.asList(
            new LessonData(1, "Algorithm Analysis", "Theory", "Big O notation, time/space complexity", "Programming basics", "Analyze algorithm efficiency", 120),
            new LessonData(2, "Arrays & Strings", "Practice", "Array operations, string manipulation algorithms", "Algorithm analysis", "Implement array algorithms", 120),
            new LessonData(3, "Linked Lists", "Practice", "Singly, doubly linked lists, operations", "Arrays", "Build linked list structures", 120),
            new LessonData(4, "Stacks & Queues", "Practice", "LIFO/FIFO operations, applications", "Linked lists", "Implement stack/queue ADTs", 120),
            new LessonData(5, "Recursion", "Theory", "Recursive algorithms, base cases, optimization", "Stacks/queues", "Write recursive solutions", 120),
            new LessonData(6, "Trees - Binary Trees", "Theory", "Binary tree structure, traversals", "Recursion", "Implement tree operations", 120),
            new LessonData(7, "Binary Search Trees", "Practice", "BST operations, search, insert, delete", "Binary trees", "Build BST applications", 120),
            new LessonData(8, "Heaps & Priority Queues", "Practice", "Min/max heaps, heap sort, priority queues", "BST", "Implement heap structures", 120),
            new LessonData(9, "Hash Tables", "Practice", "Hashing functions, collision resolution", "Heaps", "Create hash table implementations", 120),
            new LessonData(10, "Graphs - Representation", "Theory", "Graph types, adjacency matrix/list", "Hash tables", "Represent graph structures", 120),
            new LessonData(11, "Graph Traversal", "Practice", "BFS, DFS algorithms, applications", "Graph representation", "Traverse graph structures", 120),
            new LessonData(12, "Sorting Algorithms", "Practice", "Bubble, merge, quick, heap sort", "Graph traversal", "Implement sorting methods", 120),
            new LessonData(13, "Advanced Graph Algorithms", "Practice", "Shortest path, MST, topological sort", "Sorting", "Solve graph problems", 120),
            new LessonData(14, "Dynamic Programming", "Theory", "DP principles, memoization, optimization", "Advanced graphs", "Apply DP techniques", 120)
        );
        
        createLessonsForCourse(course, lessons);
    }
    
    private void createLessonsForCourse(CourseTemplate course, List<LessonData> lessonsData) {
        int sortOrder = 0;
        for (LessonData lessonData : lessonsData) {
            LessonTemplate lesson = new LessonTemplate();
            lesson.setCourseTemplate(course);
            lesson.setWeekNumber(lessonData.weekNumber);
            lesson.setTopicName(lessonData.topicName);
            lesson.setLessonType(lessonData.lessonType);
            lesson.setObjectives(lessonData.objectives);
            lesson.setRequirements(lessonData.requirements);
            lesson.setPreparations(lessonData.preparations);
            lesson.setDurationMinutes(lessonData.durationMinutes);
            lesson.setSortOrder(sortOrder++);
            
            lessonTemplateRepository.save(lesson);
        }
        log.info("✅ Created {} lessons for course: {}", lessonsData.size(), course.getName());
    }
    
    private void seedClasses(List<CourseTemplate> courseTemplates, Long createdBy) {
        log.info("👥 Creating sample classes and schedules...");
        
        // Get available rooms
        List<Room> rooms = roomRepository.findAll();
        List<User> teachers = userRepository.findAll().stream()
            .limit(5) // Use first 5 users as teachers
            .toList();
        
        if (rooms.isEmpty() || teachers.isEmpty()) {
            log.warn("No rooms or teachers found. Skipping class creation.");
            return;
        }
        
        // Create classes for each course template
        for (CourseTemplate courseTemplate : courseTemplates) {
            createClassesForCourseTemplate(courseTemplate, rooms, teachers, createdBy);
        }
        
        log.info("✅ Created classes and schedules for {} course templates", courseTemplates.size());
    }
    
    private void createClassesForCourseTemplate(CourseTemplate courseTemplate, List<Room> rooms, List<User> teachers, Long createdBy) {
        // Create 2-3 classes per course template
        int classCount = random.nextInt(2) + 2; // 2 or 3 classes
        
        for (int i = 1; i <= classCount; i++) {
            ClassEntity classEntity = createClassEntity(courseTemplate, rooms, teachers, createdBy, i);
            createClassSchedule(classEntity);
        }
    }
    
    private ClassEntity createClassEntity(CourseTemplate courseTemplate, List<Room> rooms, List<User> teachers, Long createdBy, int classNumber) {
        ClassEntity classEntity = new ClassEntity();
        
        // Set class basic info
        String className = courseTemplate.getName() + " - Lớp " + String.format("%02d", classNumber);
        classEntity.setClassName(className);
        classEntity.setDescription("Lớp học " + courseTemplate.getName() + " kỳ " + getCurrentSemester());
        classEntity.setCourseTemplate(courseTemplate);
        
        // Assign random teacher and room
        classEntity.setTeacher(teachers.get(random.nextInt(teachers.size())));
        classEntity.setRoom(rooms.get(random.nextInt(rooms.size())));
        
        // Set dates
        LocalDate startDate = getRandomStartDate();
        classEntity.setStartDate(startDate);
        classEntity.setEndDate(startDate.plusWeeks(courseTemplate.getTotalWeeks()));
        
        // Set other properties
        classEntity.setMaxStudents(25 + random.nextInt(15)); // 25-40 students
        classEntity.setCurrentStudents(15 + random.nextInt(20)); // 15-35 current students
        classEntity.setStatus(getRandomClassStatus());
        classEntity.setCreatedBy(createdBy);
        
        return classRepository.save(classEntity);
    }
    
    private void createClassSchedule(ClassEntity classEntity) {
        log.info("📅 Creating schedule for class: {}", classEntity.getClassName());
        
        // Get lesson templates for this course
        List<LessonTemplate> lessonTemplates = lessonTemplateRepository
            .findByCourseTemplateIdOrderByWeekNumberAscSortOrderAsc(classEntity.getCourseTemplate().getId());
        
        if (lessonTemplates.isEmpty()) {
            log.warn("No lesson templates found for course: {}", classEntity.getCourseTemplate().getName());
            return;
        }
        
        // Create class lessons based on templates
        LocalDate currentDate = classEntity.getStartDate();
        
        for (LessonTemplate lessonTemplate : lessonTemplates) {
            ClassLesson classLesson = new ClassLesson();
            classLesson.setClassEntity(classEntity);
            classLesson.setLessonTemplate(lessonTemplate);
            
            // Set lesson date (advance by week number)
            LocalDate lessonDate = classEntity.getStartDate().plusWeeks(lessonTemplate.getWeekNumber() - 1);
            classLesson.setActualDate(lessonDate);
            
            // Set random time slots
            TimeSlot timeSlot = getRandomTimeSlot();
            classLesson.setActualStartTime(timeSlot.startTime);
            classLesson.setActualEndTime(timeSlot.endTime);
            
            // Set status based on date
            classLesson.setStatus(getLessonStatusByDate(lessonDate));
            
            // Set attendance count for completed lessons
            if (classLesson.getStatus() == ClassLesson.LessonStatus.COMPLETED) {
                classLesson.setAttendanceCount(15 + random.nextInt(15)); // 15-30 attendees
            }
            
            classLessonRepository.save(classLesson);
        }
        
        log.info("✅ Created {} lessons for class: {}", lessonTemplates.size(), classEntity.getClassName());
    }
    
    // Helper methods
    private String getCurrentSemester() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        
        if (month >= 1 && month <= 5) {
            return "Spring " + year;
        } else if (month >= 6 && month <= 8) {
            return "Summer " + year;
        } else {
            return "Fall " + year;
        }
    }
    
    private LocalDate getRandomStartDate() {
        LocalDate now = LocalDate.now();
        // Random start date between 1 month ago and 2 months from now
        int daysOffset = random.nextInt(90) - 30; // -30 to +60 days
        return now.plusDays(daysOffset);
    }
    
    private ClassEntity.ClassStatus getRandomClassStatus() {
        ClassEntity.ClassStatus[] statuses = ClassEntity.ClassStatus.values();
        // Weight towards ACTIVE classes
        if (random.nextDouble() < 0.6) {
            return ClassEntity.ClassStatus.ACTIVE;
        } else if (random.nextDouble() < 0.8) {
            return ClassEntity.ClassStatus.PLANNING;
        } else if (random.nextDouble() < 0.95) {
            return ClassEntity.ClassStatus.COMPLETED;
        } else {
            return ClassEntity.ClassStatus.CANCELLED;
        }
    }
    
    private TimeSlot getRandomTimeSlot() {
        TimeSlot[] timeSlots = {
            new TimeSlot(LocalTime.of(8, 0), LocalTime.of(10, 0)),   // 8:00-10:00
            new TimeSlot(LocalTime.of(10, 15), LocalTime.of(12, 15)), // 10:15-12:15
            new TimeSlot(LocalTime.of(13, 30), LocalTime.of(15, 30)), // 13:30-15:30
            new TimeSlot(LocalTime.of(15, 45), LocalTime.of(17, 45)), // 15:45-17:45
            new TimeSlot(LocalTime.of(18, 0), LocalTime.of(20, 0)),   // 18:00-20:00
        };
        return timeSlots[random.nextInt(timeSlots.length)];
    }
    
    private ClassLesson.LessonStatus getLessonStatusByDate(LocalDate lessonDate) {
        LocalDate today = LocalDate.now();
        
        if (lessonDate.isBefore(today.minusDays(1))) {
            return ClassLesson.LessonStatus.COMPLETED;
        } else if (lessonDate.isEqual(today)) {
            return random.nextBoolean() ? ClassLesson.LessonStatus.IN_PROGRESS : ClassLesson.LessonStatus.SCHEDULED;
        } else {
            return ClassLesson.LessonStatus.SCHEDULED;
        }
    }
    
    // Helper classes for data structures
    private static class TimeSlot {
        final LocalTime startTime;
        final LocalTime endTime;
        
        public TimeSlot(LocalTime startTime, LocalTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
    
    private static class RoomData {
        final String roomCode;
        final String roomName;
        final String location;
        final Integer capacity;
        final Boolean isActive;
        
        public RoomData(String roomCode, String roomName, String location, Integer capacity, Boolean isActive) {
            this.roomCode = roomCode;
            this.roomName = roomName;
            this.location = location;
            this.capacity = capacity;
            this.isActive = isActive;
        }
    }
    
    // Helper class for lesson data
    private static class LessonData {
        final int weekNumber;
        final String topicName;
        final String lessonType;
        final String objectives;
        final String requirements;
        final String preparations;
        final int durationMinutes;
        
        public LessonData(int weekNumber, String topicName, String lessonType, 
                         String objectives, String requirements, String preparations, int durationMinutes) {
            this.weekNumber = weekNumber;
            this.topicName = topicName;
            this.lessonType = lessonType;
            this.objectives = objectives;
            this.requirements = requirements;
            this.preparations = preparations;
            this.durationMinutes = durationMinutes;
        }
    }
}