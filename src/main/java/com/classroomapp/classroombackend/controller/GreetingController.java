package com.classroomapp.classroombackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/greetings")
public class GreetingController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Xin chào! Backend Spring Boot cho ứng dụng quản lý lớp học đã sẵn sàng!";
    }
} 