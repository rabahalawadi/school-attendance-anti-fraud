package com.school.attendance.academic.repository;

import com.school.attendance.academic.entity.TeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, UUID> {
    List<TeacherAssignment> findByTeacherIdAndAcademicYearId(UUID teacherId, UUID academicYearId);
    List<TeacherAssignment> findByTeacherId(UUID teacherId);
    Optional<TeacherAssignment> findByTeacherIdAndSectionIdAndSubjectId(UUID teacherId, UUID sectionId, UUID subjectId);
}
