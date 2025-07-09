package com.classroomapp.classroombackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.Accomplishment;
import com.classroomapp.classroombackend.model.usermanagement.User;

@Repository
public interface AccomplishmentRepository extends JpaRepository<Accomplishment, Long> {

    List<Accomplishment> findByStudentOrderByCompletionDateDesc(User student);

}