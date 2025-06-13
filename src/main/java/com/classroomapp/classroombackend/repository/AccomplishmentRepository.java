package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.Accomplishment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AccomplishmentRepository extends JpaRepository<Accomplishment, Long> {
    List<Accomplishment> findByUserOrderByCompletionDateDesc(User user);
}