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
@Table(name = "roles")
@Data
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20, unique = true, columnDefinition = "NVARCHAR(20)")
    private String name;

    public Role(String name) {
        this.name = name;
    }
    
    // Explicit setters to resolve compilation issues
    public void setId(int id) { this.id = id; }
    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
}
