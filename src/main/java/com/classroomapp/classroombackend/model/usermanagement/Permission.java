package com.classroomapp.classroombackend.model.usermanagement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "system_permissions")
@Data
@NoArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // Constructor tiện dụng để tạo permission với tên
    public Permission(String name) {
        this.name = name;
    }
}
