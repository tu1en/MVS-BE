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
            List<CourseTemplate> courseTemplates = seedCourseTemplatesForHighSchool(createdBy);
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
    
    private List<CourseTemplate> seedCourseTemplatesForHighSchool(Long createdBy) {
        log.info("🌱 Creating high-school course templates (Toán/Lý/Hóa/Văn/Anh/Sinh)...");
        CourseTemplate toan = createSimpleCourse("Toán Nâng cao 10-12", "Toán", 16, createdBy);
        CourseTemplate ly = createSimpleCourse("Vật lý Chuyên đề", "Vật lý", 14, createdBy);
        CourseTemplate hoa = createSimpleCourse("Hóa học Trọng tâm", "Hóa học", 14, createdBy);
        CourseTemplate van = createSimpleCourse("Ngữ văn - Đọc hiểu & Nghị luận", "Ngữ văn", 12, createdBy);
        CourseTemplate anh = createSimpleCourse("Tiếng Anh - Grammar & Reading", "Tiếng Anh", 12, createdBy);
        CourseTemplate sinh = createSimpleCourse("Sinh học - Di truyền & Sinh thái", "Sinh học", 12, createdBy);

        // Tạo bài học 120' mỗi tuần
        for (CourseTemplate ct : Arrays.asList(toan, ly, hoa, van, anh, sinh)) {
            createWeeklyLessons(ct, ct.getTotalWeeks());
        }
        return Arrays.asList(toan, ly, hoa, van, anh, sinh);
    }

    private CourseTemplate createSimpleCourse(String name, String subject, int weeks, Long createdBy) {
        CourseTemplate course = new CourseTemplate();
        course.setName(name);
        course.setDescription(name + " dành cho học sinh cấp 3, bám sát chương trình và luyện đề.");
        course.setSubject(subject);
        course.setTotalWeeks(weeks);
        course.setCreatedBy(createdBy);
        course.setStatus(TemplateStatus.ACTIVE);
        course.setIsActive(true);
        course.setIsPublic(true);
        course.setEnrollmentFee(new BigDecimal("0"));
        course.setMaxStudentsPerTemplate(35);
        return courseTemplateRepository.save(course);
    }

    private void createWeeklyLessons(CourseTemplate course, int weeks) {
        for (int w = 1; w <= weeks; w++) {
            LessonTemplate lesson = new LessonTemplate();
            lesson.setCourseTemplate(course);
            lesson.setWeekNumber(w);
            lesson.setTopicName("Tuần " + w + " - Bài học chủ đề");
            lesson.setLessonType("Lý thuyết");
            lesson.setObjectives("Củng cố kiến thức trọng tâm tuần " + w);
            lesson.setRequirements("Hoàn thành bài tập tuần " + w);
            lesson.setPreparations("Ôn lại bài tuần trước");
            lesson.setDurationMinutes(120);
            lesson.setSortOrder(w - 1);
            lessonTemplateRepository.save(lesson);
        }
    }
    
    // Legacy methods kept for reference (not used with high-school seeding)
    @SuppressWarnings("unused")
    private CourseTemplate createJavaCourse(Long createdBy) {
        CourseTemplate course = new CourseTemplate();
        course.setName("Java Programming Fundamentals");
        course.setDescription(createJavaDescription());
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
    
    @SuppressWarnings("unused")
    private CourseTemplate createWebDevelopmentCourse(Long createdBy) {
        CourseTemplate course = new CourseTemplate();
        course.setName("Full-Stack Web Development");
        course.setDescription(createWebDevelopmentDescription());
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
    
    @SuppressWarnings("unused")
    private CourseTemplate createDatabaseCourse(Long createdBy) {
        CourseTemplate course = new CourseTemplate();
        course.setName("Database Design & SQL");
        course.setDescription(createDatabaseDescription());
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
    
    @SuppressWarnings("unused")
    private CourseTemplate createMobileCourse(Long createdBy) {
        CourseTemplate course = new CourseTemplate();
        course.setName("Mobile App Development");
        course.setDescription(createMobileDescription());
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
    
    @SuppressWarnings("unused")
    private CourseTemplate createDataStructuresCourse(Long createdBy) {
        CourseTemplate course = new CourseTemplate();
        course.setName("Data Structures & Algorithms");
        course.setDescription(createDataStructuresDescription());
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
    
    @SuppressWarnings("unused")
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
    
    @SuppressWarnings("unused")
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
    
    @SuppressWarnings("unused")
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
    
    @SuppressWarnings("unused")
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
    
    @SuppressWarnings("unused")
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
    
    private int statusRoundRobinIndex = 0;

    private void createClassesForCourseTemplate(CourseTemplate courseTemplate, List<Room> rooms, List<User> teachers, Long createdBy) {
        // Tạo 4 lớp mỗi template để phân bổ trạng thái đồng đều
        int classCount = 4;
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
        
        // Trạng thái phân bổ đồng đều theo vòng lặp
        ClassEntity.ClassStatus status = getEvenlyDistributedStatus();

        // Set dates phù hợp với trạng thái để job auto không đảo ngược sai
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        if (status == ClassEntity.ClassStatus.PLANNING) {
            startDate = today.plusDays(1 + random.nextInt(20)); // tương lai gần
        } else if (status == ClassEntity.ClassStatus.ACTIVE) {
            startDate = today.minusDays(7 + random.nextInt(7)); // đã bắt đầu trong quá khứ gần
        } else if (status == ClassEntity.ClassStatus.COMPLETED) {
            startDate = today.minusWeeks(6 + random.nextInt(4)); // quá khứ xa
        } else { // CANCELLED
            startDate = today.minusDays(random.nextInt(15));
        }
        classEntity.setStartDate(startDate);
        classEntity.setEndDate(startDate.plusWeeks(Math.max(1, courseTemplate.getTotalWeeks())));

        // Set other properties
        classEntity.setMaxStudents(25 + random.nextInt(15)); // 25-40 students
        classEntity.setCurrentStudents(15 + random.nextInt(20)); // 15-35 current students
        classEntity.setStatus(status);
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
        // LocalDate currentDate = classEntity.getStartDate(); // not used
        
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
    
    // (removed unused getRandomStartDate helper)
    
    private ClassEntity.ClassStatus getEvenlyDistributedStatus() {
        ClassEntity.ClassStatus[] order = new ClassEntity.ClassStatus[] {
            ClassEntity.ClassStatus.PLANNING,
            ClassEntity.ClassStatus.ACTIVE,
            ClassEntity.ClassStatus.COMPLETED,
            ClassEntity.ClassStatus.CANCELLED
        };
        ClassEntity.ClassStatus status = order[statusRoundRobinIndex % order.length];
        statusRoundRobinIndex++;
        return status;
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
    
    // Course Description Creators - Detailed and Attractive
    private String createJavaDescription() {
        return """
            🎯 **KHÓA HỌC JAVA PROGRAMMING FUNDAMENTALS - NỀN TẢNG VỮNG CHẮC CHO DEVELOPER**
            
            ⭐ **TẠI SAO CHỌN JAVA?**
            • #1 ngôn ngữ lập trình được sử dụng nhiều nhất thế giới
            • Nền tảng cho hàng triệu ứng dụng enterprise và startup
            • Mức lương trung bình: 15-30 triệu VND/tháng cho fresher
            • Cơ hội việc làm rộng mở tại các công ty công nghệ hàng đầu
            
            🎬 **VIDEO HỌC THỬ MIỄN PHÍ**: https://www.youtube.com/watch?v=EFfrnPKJuPY&ab_channel=TiCungBimDSA
            
            📋 **CHƯƠNG TRÌNH HỌC CHI TIẾT (16 TUẦN)**
            
            **🏁 GIAI ĐOẠN 1: FOUNDATION (Tuần 1-4)**
            ✅ Tuần 1: Java Setup & Syntax - Viết chương trình đầu tiên
            ✅ Tuần 2: Variables & Data Types - Quản lý dữ liệu hiệu quả  
            ✅ Tuần 3: Control Flow - Logic và điều khiển chương trình
            ✅ Tuần 4: Methods & Functions - Tổ chức code chuyên nghiệp
            
            **🚀 GIAI ĐOẠN 2: OOP MASTERY (Tuần 5-8)**
            ✅ Tuần 5-6: Classes & Objects - Lập trình hướng đối tượng
            ✅ Tuần 7: Inheritance & Polymorphism - Tái sử dụng code
            ✅ Tuần 8: Collections Framework - Xử lý dữ liệu nâng cao
            
            **💪 GIAI ĐOẠN 3: ADVANCED (Tuần 9-12)**
            ✅ Tuần 9: Exception Handling - Xử lý lỗi chuyên nghiệp
            ✅ Tuần 10: File I/O - Đọc/ghi file và database
            ✅ Tuần 11: GUI với Swing - Tạo ứng dụng desktop
            ✅ Tuần 12: JDBC - Kết nối database thực tế
            
            **🔥 GIAI ĐOẠN 4: MODERN JAVA (Tuần 13-16)**
            ✅ Tuần 13: Multithreading - Lập trình đa luồng
            ✅ Tuần 14: Java 8+ Features - Lambda, Stream API
            ✅ Tuần 15: Design Patterns - Code như Senior Dev
            ✅ Tuần 16: **CAPSTONE PROJECT** - Ứng dụng quản lý thư viện hoàn chỉnh
            
            💼 **DỰ ÁN THỰC TẾ**
            • **Library Management System** - Ứng dụng quản lý thư viện với GUI
            • **Banking Application** - Hệ thống ngân hàng mini
            • **Student Portal** - Website quản lý học sinh
            • **E-commerce Backend** - API cho website bán hàng
            
            🎯 **KẾT QUẢ HỌC TẬP**
            Sau khóa học bạn sẽ:
            ✅ Thành thạo Java Core và OOP
            ✅ Xây dựng được ứng dụng desktop và web
            ✅ Hiểu sâu về Design Patterns và Clean Code
            ✅ Sẵn sàng cho vị trí Java Developer Junior
            ✅ Portfolio 4+ dự án thực tế đầy ấn tượng
            
            📈 **LỘ TRÌNH NGHỀ NGHIỆP**
            → Junior Java Developer (15-20tr/tháng)
            → Java Developer (20-30tr/tháng) 
            → Senior Java Developer (30-50tr/tháng)
            → Technical Lead / Architect (50tr+/tháng)
            
            👨‍🎓 **PHẢN HỒI HỌC VIÊN**
            
            ⭐⭐⭐⭐⭐ **Nguyễn Văn A - Đã có việc tại FPT Software**
            "Khóa học rất thực tế, project cuối khóa giúp mình tự tin phỏng vấn và pass được vị trí Java Developer với lương 18tr/tháng!"
            
            ⭐⭐⭐⭐⭐ **Trần Thị B - Chuyển nghề thành công** 
            "Từ kế toán chuyển sang IT, sau 4 tháng học đã có offer tại Viettel với package 22tr/tháng. Cảm ơn thầy rất nhiều!"
            
            🎁 **ƯU ĐÃI ĐẶC BIỆT**
            • 🆓 Tài liệu học tập độc quyền 500+ trang
            • 💻 Trở lại mã nguồn tất cả dự án
            • 🤝 Hỗ trợ tìm việc và review CV
            • 📚 Truy cập vĩnh viễn khóa học
            • 🏆 Chứng chỉ hoàn thành được công nhận
            
            ⏰ **SỐ LƯỢNG CÓ HẠN: CHỈ 25 HỌC VIÊN/LỚP**
            """;
    }
    
    private String createWebDevelopmentDescription() {
        return """
            🌐 **FULL-STACK WEB DEVELOPMENT - TRỞ THÀNH DEVELOPER TOÀN NĂNG**
            
            🚀 **TẠI SAO CHỌN FULL-STACK?**
            • Kỹ năng hot nhất 2024 - Frontend + Backend + Database
            • Mức lương cao nhất ngành IT: 20-40 triệu VND/tháng
            • Làm việc remote 100% tại các công ty quốc tế
            • Tự do freelance với mức giá 500-2000$/project
            
            🎬 **DEMO TRỰC TIẾP**: https://www.youtube.com/watch?v=H3cYspcoSaA&ab_channel=TiCungBimDSA
            📱 **APP MẪU**: https://demo.webdev-course.com
            
            📋 **ROADMAP HỌC TẬP 20 TUẦN**
            
            **🎨 MODULE 1: FRONTEND FUNDAMENTALS (Tuần 1-5)**
            ✅ HTML5 Semantic - Cấu trúc web hiện đại
            ✅ CSS3 Advanced - Animation, Flexbox, Grid
            ✅ Responsive Design - Mobile-first approach  
            ✅ JavaScript ES6+ - DOM, Events, API calls
            ✅ **Mini Project**: Landing page responsive hoàn chỉnh
            
            **⚛️ MODULE 2: REACT ECOSYSTEM (Tuần 6-10)**
            ✅ React Hooks & Components - Tư duy component
            ✅ State Management - Redux Toolkit, Context API
            ✅ React Router - SPA navigation
            ✅ API Integration - Axios, async/await
            ✅ **Project**: Todo App với React + API thực tế
            
            **🔧 MODULE 3: BACKEND MASTERY (Tuần 11-15)**
            ✅ Node.js & Express.js - Server-side JavaScript
            ✅ MongoDB & Mongoose - NoSQL database
            ✅ Authentication - JWT, bcrypt, session
            ✅ RESTful API Design - Best practices
            ✅ **Project**: Blog API với authentication hoàn chỉnh
            
            **🎯 MODULE 4: DEPLOYMENT & DEVOPS (Tuần 16-17)**
            ✅ Git/GitHub workflow - Version control chuyên nghiệp
            ✅ Docker containerization - Deploy anywhere
            ✅ AWS/Heroku deployment - Production ready
            ✅ CI/CD Pipeline - Tự động hóa deployment
            
            **💼 MODULE 5: CAPSTONE PROJECT (Tuần 18-20)**
            🎯 **E-Commerce Platform hoàn chỉnh**:
            • Frontend: React + Redux + Material-UI
            • Backend: Node.js + Express + MongoDB  
            • Features: Cart, Payment, Admin Dashboard
            • Deploy: AWS với domain thực tế
            
            🛠️ **CÔNG NGHỆ SỬ DỤNG**
            **Frontend**: React, Redux, JavaScript ES6+, HTML5, CSS3, Bootstrap
            **Backend**: Node.js, Express.js, MongoDB, Mongoose
            **Tools**: Git, VS Code, Postman, Docker, AWS
            **Payment**: Stripe API, PayPal integration
            
            💰 **CƠ HỘI NGHỀ NGHIỆP**
            
            🎯 **Frontend Developer**: 18-35 triệu/tháng
            • React Developer tại Shopee, Grab, Tiki
            • UI/UX Developer tại các startup
            • Remote work cho client nước ngoài
            
            🎯 **Backend Developer**: 20-40 triệu/tháng  
            • Node.js Developer tại VNG, FPT
            • API Developer cho fintech companies
            • Microservices architect
            
            🎯 **Full-Stack Developer**: 25-50 triệu/tháng
            • Tech Lead tại các công ty product
            • CTO tại startup (equity + salary)
            • Freelance $30-80/giờ
            
            🏆 **SUCCESS STORIES**
            
            ⭐ **Lê Minh C - Full-Stack Developer tại Tiki**
            "Sau khóa học 5 tháng đã nhận offer 28tr/tháng tại Tiki. Project cuối khóa chính là portfolio giúp mình pass interview!"
            
            ⭐ **Phạm Thị D - Remote Developer** 
            "Hiện tại đang remote cho client Mỹ với mức 2500$/tháng. Khóa học cho mình foundation vững để tự tin take on các project lớn."
            
            ⭐ **Hoàng Văn E - Startup CTO**
            "Từ kiến thức học được, mình đã build MVP cho startup riêng và gọi vốn thành công 50,000$ seed funding!"
            
            🎁 **BONUS ĐẶC BIỆT**
            • 💻 Source code 10+ projects thực tế
            • 📚 E-book "Web Developer Roadmap 2024"
            • 🤝 Mentoring 1-1 với Senior Developer
            • 💼 Hỗ trợ viết CV và chuẩn bị interview
            • 🌐 Deploy free domain cho portfolio
            • 🏅 Certificate được công nhận bởi các công ty IT
            
            ⚡ **EARLY BIRD: GIẢM 30% CHO 10 HỌC VIÊN ĐẦU TIÊN**
            🔥 **CHỈ CÒN 20 SUẤT - ĐĂNG KÝ NGAY!**
            """;
    }
    
    private String createDatabaseDescription() {
        return """
            🗄️ **DATABASE DESIGN & SQL MASTERY - TRỞ THÀNH CHUYÊN GIA DỮ LIỆU**
            
            📊 **TẠI SAO HỌC DATABASE?**
            • 90% ứng dụng cần database - Kỹ năng bắt buộc mọi developer
            • Database Administrator: 25-45 triệu VND/tháng  
            • Data Analyst/Scientist: 20-60 triệu VND/tháng
            • Backend Developer cần SQL: 18-35 triệu VND/tháng
            
            🎬 **LIVE DEMO**: https://www.youtube.com/watch?v=EFfrnPKJuPY&ab_channel=TiCungBimDSA
            📊 **INTERACTIVE QUERIES**: https://sql-playground.edu.vn
            
            📚 **CURRICULUM THỰC CHIẾN 12 TUẦN**
            
            **📐 FUNDAMENTALS (Tuần 1-3)**
            ✅ Database Concepts - RDBMS vs NoSQL
            ✅ ER Diagram Design - Thiết kế database chuyên nghiệp  
            ✅ Normalization - Tối ưu hóa cấu trúc dữ liệu
            🎯 **Project**: Thiết kế database cho hệ thống quản lý bán hàng
            
            **💻 SQL PROGRAMMING (Tuần 4-6)**
            ✅ Basic Queries - SELECT, INSERT, UPDATE, DELETE
            ✅ Advanced Queries - JOINs, Subqueries, CTEs
            ✅ Aggregate Functions - GROUP BY, HAVING, Window Functions
            🎯 **Project**: Phân tích dữ liệu bán hàng với SQL phức tạp
            
            **⚡ PERFORMANCE & OPTIMIZATION (Tuần 7-9)**
            ✅ Indexing Strategies - Tăng tốc truy vấn 100x
            ✅ Query Optimization - Execution plans, Statistics
            ✅ Stored Procedures & Functions - Business logic trong DB
            🎯 **Project**: Tối ưu database cho 1 triệu records
            
            **🔒 ADVANCED TOPICS (Tuần 10-12)**  
            ✅ Transaction Management - ACID properties, Concurrency
            ✅ Security & Backup - Bảo mật và phục hồi dữ liệu
            ✅ NoSQL với MongoDB - Document-based database
            🎯 **Final Project**: Hệ thống ERP hoàn chỉnh với SQL Server
            
            🛠️ **CÔNG CỤ & CÔNG NGHỆ**
            **Relational**: SQL Server, MySQL, PostgreSQL, Oracle
            **NoSQL**: MongoDB, Redis, Cassandra
            **Tools**: SSMS, MySQL Workbench, MongoDB Compass
            **Cloud**: Azure SQL, AWS RDS, Google Cloud SQL
            
            💼 **REAL-WORLD PROJECTS**
            
            🏪 **E-Commerce Database**
            • Users, Products, Orders, Inventory management
            • Complex queries cho reports và analytics
            • Performance tuning cho millions records
            
            🏥 **Hospital Management System**  
            • Patients, Doctors, Appointments, Medical records
            • HIPAA compliance và data security
            • Backup & disaster recovery procedures
            
            📈 **Business Intelligence Dashboard**
            • Data warehouse design (Star schema)
            • ETL processes với SQL Scripts  
            • Reporting với Power BI integration
            
            🎯 **CAREER PATHS**
            
            **Database Developer** (20-35tr/tháng)
            • Thiết kế và optimize database schema
            • Viết stored procedures phức tạp
            • Performance tuning và troubleshooting
            
            **Data Analyst** (18-40tr/tháng)
            • SQL queries cho business insights  
            • Data visualization với Power BI/Tableau
            • A/B testing và statistical analysis
            
            **Database Administrator** (25-50tr/tháng)
            • Quản lý production database systems
            • Backup, security, và disaster recovery
            • High availability và scalability planning
            
            **Data Engineer** (30-60tr/tháng)
            • Build ETL pipelines với SQL
            • Data warehouse và data lake design
            • Big data technologies (Spark, Hadoop)
            
            🌟 **SUCCESS STORIES**
            
            ⭐ **Nguyễn Anh F - DBA tại Vietcombank**
            "Khóa học giúp mình từ zero thành DBA với lương 32tr/tháng. Phần performance tuning rất thực tế!"
            
            ⭐ **Trần Văn G - Data Analyst tại Vingroup**  
            "SQL skills từ khóa học giúp mình analyze dữ liệu khách hàng hiệu quả, được promote lên Senior sau 8 tháng."
            
            ⭐ **Lê Thị H - Freelance Database Consultant**
            "Hiện tại mình nhận consulting cho các SME với giá 800-1500$/project. ROI khóa học chỉ sau 2 tháng!"
            
            🎁 **EXCLUSIVE BONUSES**
            • 📊 SQL Practice Database với 10GB real data
            • 📖 "SQL Interview Questions" - 200+ câu hỏi thường gặp  
            • 🔧 Performance tuning toolkit và scripts
            • 💰 Hướng dẫn freelance database projects
            • 🏆 Microsoft SQL Server certification prep
            • 🤝 Job referral program với các đối tác doanh nghiệp
            
            ⭐ **SATISFACTION GUARANTEE: 100% hoàn tiền nếu không hài lòng**
            🔥 **LIMITED: CHỈ 30 HỌC VIÊN/KỲ ĐỂ ĐẢM BẢO CHẤT LƯỢNG**
            """;
    }
    
    private String createMobileDescription() {
        return """
            📱 **MOBILE APP DEVELOPMENT - XÂY DỰNG ỨNG DỤNG TRIỆU DOWNLOADS**
            
            🚀 **TẠI SAO CHỌN MOBILE DEVELOPMENT?**
            • 6.8 tỷ smartphone users worldwide - Thị trường khổng lồ
            • Mobile Developer: 22-45 triệu VND/tháng  
            • Freelance app: $5,000-$50,000/project
            • Passive income từ app trên Store: $500-$10,000/tháng
            
            🎬 **APP DEMO**: https://www.youtube.com/watch?v=EFfrnPKJuPY&ab_channel=TiCungBimDSA
            📱 **DOWNLOAD APK**: https://drive.google.com/mobile-demo-app
            
            🛣️ **LEARNING JOURNEY 18 TUẦN**
            
            **🎯 MOBILE FOUNDATIONS (Tuần 1-3)**
            ✅ Mobile Development Landscape - Native vs Cross-platform
            ✅ React Native Setup - Expo vs CLI, emulator setup
            ✅ Component Architecture - Props, State, Lifecycle
            🚀 **Build**: Hello World app trên cả iOS và Android
            
            **📱 UI/UX MASTERY (Tuần 4-6)**
            ✅ Navigation Systems - Stack, Tab, Drawer navigation
            ✅ Responsive Design - Multiple screen sizes và orientations  
            ✅ Styling & Animations - StyleSheet, Animated API, Lottie
            🚀 **Build**: Instagram-like UI với smooth animations
            
            **🔗 DATA & API INTEGRATION (Tuần 7-9)**
            ✅ State Management - Redux Toolkit, Context API
            ✅ API Integration - Fetch, Axios, error handling
            ✅ Local Storage - AsyncStorage, SQLite, Realm
            🚀 **Build**: Weather app với real-time API data
            
            **📲 DEVICE FEATURES (Tuần 10-12)**
            ✅ Camera & Image Processing - Photo capture, filters
            ✅ Location & Maps - GPS, Google Maps integration
            ✅ Push Notifications - Firebase Cloud Messaging
            ✅ Biometric Authentication - TouchID, FaceID
            🚀 **Build**: Social media app với camera và location
            
            **🎮 ADVANCED FEATURES (Tuần 13-15)**
            ✅ Native Modules - Bridge to iOS/Android native code
            ✅ Performance Optimization - Memory, rendering, bundle size
            ✅ Testing & Debugging - Jest, Detox, Flipper
            ✅ App Store Optimization - Keywords, screenshots, reviews
            🚀 **Build**: Gaming app với native performance
            
            **🏗️ DEPLOYMENT & MONETIZATION (Tuần 16-18)**
            ✅ App Store Submission - iOS App Store, Google Play  
            ✅ Code Push & OTA Updates - Instant updates without store
            ✅ Analytics & Crash Reporting - Firebase, Sentry
            ✅ Monetization - In-app purchases, ads, subscriptions
            🚀 **Final Project**: Complete marketplace app deployment
            
            💰 **MONETIZATION STRATEGIES**
            
            **📊 Freemium Model**
            • Free app với premium features
            • In-app purchases: $0.99-$99.99
            • Subscription: $4.99-$29.99/tháng
            
            **📢 Ad-Supported**  
            • Banner ads: $1-5 CPM
            • Video ads: $10-25 CPM
            • Sponsored content integration
            
            **💎 Premium Apps**
            • Paid download: $0.99-$9.99
            • Enterprise licensing: $100-1000s
            • White-label solutions
            
            🎯 **CAREER OPPORTUNITIES**
            
            **📱 Mobile Developer** (22-40tr/tháng)
            • React Native Developer tại Grab, Shopee
            • iOS/Android Developer tại banks, fintechs  
            • Cross-platform specialist tại startups
            
            **🚀 App Entrepreneur** (Unlimited income)
            • Build và monetize own apps
            • App agency với recurring revenue
            • SaaS mobile solutions
            
            **💼 Freelance Mobile Developer** ($25-100/giờ)
            • Remote work cho US/EU clients
            • $5K-50K per project contracts
            • Long-term maintenance deals
            
            📈 **SUCCESS STORIES**
            
            ⭐ **Phạm Minh I - React Native tại Grab**
            "Từ web developer chuyển sang mobile sau khóa học. Hiện tại lương 35tr/tháng plus stock options tại Grab!"
            
            ⭐ **Lê Thị K - Solo App Developer**
            "App fitness của mình hiện có 50K downloads, thu nhập $2,000/tháng từ premium subscriptions. Best investment ever!"
            
            ⭐ **Nguyễn Văn L - Mobile Agency Owner**
            "Đã build agency 8 người, nhận projects $10K-50K. Started từ knowledge học được trong khóa này."
            
            🎁 **PREMIUM PACKAGE INCLUDES**
            • 📱 Complete source code của 8 production-ready apps
            • 🎨 UI Kit library với 100+ components  
            • 🔧 Development tools và debugging utilities
            • 📊 App analytics và monetization playbook
            • 🏪 App Store Optimization guide và templates
            • 🤝 1-on-1 mentoring sessions với Senior Mobile Dev
            • 💼 Job placement assistance và interview prep
            • 🌐 Access to exclusive mobile developer community
            
            🔥 **EARLY BIRD SPECIAL**
            ✅ **Đăng ký trong 48h**: Giảm 40% + Free MacBook Air rental  
            ✅ **Đăng ký trong tuần**: Giảm 25% + Free iOS Developer license
            ✅ **Guarantee**: Hoàn tiền 100% nếu không publish được app lên store
            
            ⚡ **VIP CLASS: CHỈ 15 HỌC VIÊN - MENTORING CÁ NHÂN HOÁ**
            """;
    }
    
    private String createDataStructuresDescription() {
        return """
            🧠 **DATA STRUCTURES & ALGORITHMS - MASTER THE FUNDAMENTALS OF PROGRAMMING**
            
            💡 **TẠI SAO CẦN DS&A?**
            • Nền tảng của mọi Senior Developer - Không thể thiếu  
            • FAANG Interview: 90% câu hỏi về algorithms
            • Problem-solving skills - Tư duy lập trình chuyên nghiệp
            • System Design foundation - Scalable architecture
            
            🎬 **ALGORITHM VISUALIZER**: https://www.youtube.com/watch?v=EFfrnPKJuPY&ab_channel=TiCungBimDSA
            🧮 **INTERACTIVE PLAYGROUND**: https://algorithm-playground.dev
            
            🎯 **INTENSIVE BOOTCAMP 14 TUẦN**
            
            **📊 COMPLEXITY ANALYSIS (Tuần 1)**
            ✅ Big O Notation - Time và Space complexity
            ✅ Best, Average, Worst case analysis  
            ✅ Mathematical foundations - Logarithms, recursion
            💻 **Practice**: Analyze complexity của popular algorithms
            
            **🔢 LINEAR DATA STRUCTURES (Tuần 2-4)**
            ✅ Arrays & Dynamic Arrays - Memory layout, operations
            ✅ Linked Lists - Singly, Doubly, Circular implementations  
            ✅ Stacks & Queues - LIFO/FIFO principles và applications
            ✅ Strings & String Matching - KMP, Rabin-Karp algorithms
            💻 **Build**: Text editor với undo/redo functionality
            
            **🌳 TREE STRUCTURES (Tuần 5-7)**
            ✅ Binary Trees - Traversals, properties, applications
            ✅ Binary Search Trees - Search, insert, delete operations
            ✅ Balanced Trees - AVL, Red-Black trees
            ✅ Heaps & Priority Queues - Min/Max heap, heap sort
            ✅ Tries - Prefix trees cho string operations
            💻 **Build**: Autocomplete system như Google Search
            
            **🕸️ GRAPH ALGORITHMS (Tuần 8-9)**
            ✅ Graph Representations - Adjacency matrix/list
            ✅ Graph Traversal - BFS, DFS và applications  
            ✅ Shortest Paths - Dijkstra, Bellman-Ford, Floyd-Warshall
            ✅ Minimum Spanning Tree - Kruskal, Prim algorithms
            ✅ Topological Sort - Dependency resolution
            💻 **Build**: GPS Navigation system với shortest path
            
            **⚡ SORTING & SEARCHING (Tuần 10-11)**
            ✅ Comparison Sorts - Bubble, Selection, Insertion, Merge, Quick
            ✅ Non-comparison Sorts - Counting, Radix, Bucket sort
            ✅ Binary Search - và variations (lower_bound, upper_bound)
            ✅ Hash Tables - Collision resolution, load balancing
            💻 **Build**: Database indexing system simulation
            
            **🧩 DYNAMIC PROGRAMMING (Tuần 12-13)**
            ✅ DP Fundamentals - Overlapping subproblems, optimal substructure
            ✅ Memoization vs Tabulation - Top-down vs Bottom-up
            ✅ Classic Problems - Fibonacci, LCS, LIS, Knapsack
            ✅ Advanced DP - Tree DP, Digit DP, Bitmask DP
            💻 **Build**: Stock trading optimization algorithm
            
            **🏆 COMPETITIVE PROGRAMMING (Tuần 14)**
            ✅ Problem-solving strategies - Pattern recognition
            ✅ Contest environment - Time management, debugging
            ✅ Advanced algorithms - Network flow, Game theory
            ✅ Mock interviews - FAANG-style coding challenges
            💻 **Final Challenge**: Solve 50 LeetCode problems live
            
            🏢 **INTERVIEW PREPARATION**
            
            **🎯 FAANG Companies** (Google, Apple, Meta, Amazon, Netflix)
            • Algorithm complexity analysis
            • System design fundamentals  
            • Coding under pressure
            • Communication và explanation skills
            
            **💼 Common Interview Topics**
            ✅ Array manipulation & Two pointers
            ✅ Tree traversal & Binary search
            ✅ Dynamic programming patterns
            ✅ Graph algorithms applications  
            ✅ Hash table design & usage
            ✅ String processing algorithms
            
            📈 **SALARY IMPACT**
            
            **Without DS&A Knowledge**
            • Junior Developer: 12-18 triệu VND/tháng
            • Limited to simple CRUD applications  
            • Difficulty passing technical interviews
            
            **With Strong DS&A Foundation**  
            • Senior Developer: 25-45 triệu VND/tháng
            • Tech Lead positions: 40-70 triệu VND/tháng
            • FAANG offers: $150K-300K USD/năm
            • Competitive programming prizes: $1K-50K USD
            
            🏆 **GRADUATE SUCCESS**
            
            ⭐ **Hoàng Anh M - Software Engineer tại Google**
            "DS&A foundation từ khóa học giúp mình pass 5 vòng interview Google. Offer package $180K USD/năm!"
            
            ⭐ **Phạm Thị N - Algorithm Engineer tại Shopee**
            "Từ junior dev lên Senior trong 1 năm nhờ algorithmic thinking. Hiện tại lead team recommendation system."
            
            ⭐ **Lê Văn O - Competitive Programmer**  
            "Won ACM ICPC Regional với team, nhận scholarship $50K để học Master tại Stanford University."
            
            🎁 **EXCLUSIVE RESOURCES**
            • 📚 Algorithm Bible - 800+ pages comprehensive guide
            • 💻 Custom judge system với 1000+ problems
            • 📊 Progress tracking với skill assessment  
            • 🎬 Video solutions cho mỗi algorithm
            • 🤝 Study group với top competitive programmers
            • 💼 Mock interview sessions với FAANG engineers
            • 🏅 Certificate recognized by top tech companies
            • 📱 Mobile app để practice anywhere, anytime
            
            ⚡ **INTENSIVE FORMAT**
            • 🕘 **3 buổi/tuần** - Thứ 2, 4, 6 (19:00-21:00)
            • 🏃‍♂️ **Fast-paced learning** - Cover maximum concepts
            • 👥 **Small groups** - Max 25 students cho personal attention
            • 💪 **Bootcamp style** - Intensive practice sessions
            • 🎯 **Goal-oriented** - Target specific skill levels
            
            🔥 **CHALLENGE ACCEPTED?**
            ✅ **Guarantee**: Solve 200+ LeetCode problems hoặc full refund  
            ✅ **Placement**: 90% học viên nhận job offer trong 6 tháng
            ✅ **Community**: Lifetime access to alumni network
            
            ⭐ **DIFFICULTY: CHALLENGING - CHỈ DÀNH CHO NHỮNG AI NGHIÊM TÚC**
            """;
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