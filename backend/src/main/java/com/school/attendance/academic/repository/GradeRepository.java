package com.school.attendance.academic.repository;

import com.school.attendance.academic.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GradeRepository extends JpaRepository<Grade, UUID> {
    List<Grade> findByAcademicYearIdOrderByLevelAsc(UUID academicYearId);
}
