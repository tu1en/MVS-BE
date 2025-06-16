package com.classroomapp.classroombackend.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/classes")
public class ClassController {

    @GetMapping
    public List<Map<String, Object>> getAllClasses() {
        List<Map<String, Object>> classes = new ArrayList<>();
        
        Map<String, Object> class1 = new HashMap<>();
        class1.put("id", 1);
        class1.put("name", "Lớp Java Spring Boot");
        class1.put("teacherId", 1);
        class1.put("students", Arrays.asList(2));
        
        classes.add(class1);
        return classes;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getClassById(@PathVariable int id) {
        Map<String, Object> classInfo = new HashMap<>();
        classInfo.put("id", id);
        classInfo.put("name", "Lớp " + id);
        classInfo.put("teacherId", 1);
        classInfo.put("students", Arrays.asList(2));
        return classInfo;
    }
} 