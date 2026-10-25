package com.school.attendance.academic.repository;

import com.school.attendance.academic.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, UUID> {
    Optional<Teacher> findByUserId(UUID userId);
    Optional<Teacher> findByEmployeeNumber(String employeeNumber);
    boolean existsByEmployeeNumber(String employeeNumber);
}
