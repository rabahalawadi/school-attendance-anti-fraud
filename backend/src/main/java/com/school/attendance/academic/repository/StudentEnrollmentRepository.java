package com.school.attendance.academic.repository;

import com.school.attendance.academic.entity.StudentEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, UUID> {
    Optional<StudentEnrollment> findByStudentIdAndAcademicYearId(UUID studentId, UUID academicYearId);
    List<StudentEnrollment> findBySectionIdAndStatus(UUID sectionId, String status);
    List<StudentEnrollment> findByStudentId(UUID studentId);
}
