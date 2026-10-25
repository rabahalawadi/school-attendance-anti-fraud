package com.school.attendance.academic.repository;

import com.school.attendance.academic.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    Optional<Student> findByUserId(UUID userId);
    Optional<Student> findByStudentNumber(String studentNumber);
    boolean existsByStudentNumber(String studentNumber);
}
