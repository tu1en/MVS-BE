package com.classroomapp.classroombackend.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller tối thiểu cho role PARENT.
 * Hiện tại chỉ cung cấp endpoint đọc danh sách con được liên kết với phụ huynh.
 * Chưa có API quản lý khác theo yêu cầu.
 */
@RestController
@RequestMapping("/api/parent")
public class ParentController {

    @GetMapping("/children")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<Object>> getChildren() {
        // Chưa tích hợp quan hệ Parent-Student → tạm trả về danh sách rỗng
        return ResponseEntity.ok(Collections.emptyList());
    }
}


