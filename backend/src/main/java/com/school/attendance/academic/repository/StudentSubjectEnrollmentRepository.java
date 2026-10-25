package com.school.attendance.academic.repository;

import com.school.attendance.academic.entity.StudentSubjectEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentSubjectEnrollmentRepository extends JpaRepository<StudentSubjectEnrollment, UUID> {
    List<StudentSubjectEnrollment> findByStudentEnrollmentId(UUID studentEnrollmentId);
}
