package com.school.attendance.academic.repository;

import com.school.attendance.academic.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {
    List<Section> findByGradeId(UUID gradeId);
}
