package com.school.attendance.academic.service;

import com.school.attendance.academic.entity.*;
import com.school.attendance.academic.repository.*;
import com.school.attendance.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class AcademicService {

    private final AcademicYearRepository academicYearRepository;
    private final GradeRepository gradeRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository enrollmentRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;

    public AcademicService(
            AcademicYearRepository academicYearRepository,
            GradeRepository gradeRepository,
            SectionRepository sectionRepository,
            SubjectRepository subjectRepository,
            StudentRepository studentRepository,
            StudentEnrollmentRepository enrollmentRepository,
            TeacherAssignmentRepository teacherAssignmentRepository) {
        this.academicYearRepository = academicYearRepository;
        this.gradeRepository = gradeRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
    }

    // =========================================================================
    // Academic Years
    // =========================================================================
    public List<AcademicYear> getAllAcademicYears() {
        return academicYearRepository.findAll();
    }

    public AcademicYear createAcademicYear(String name, LocalDate start, LocalDate end) {
        return academicYearRepository.save(new AcademicYear(name, start, end));
    }

    // =========================================================================
    // Grades
    // =========================================================================
    public List<Grade> getGradesByYear(UUID academicYearId) {
        return gradeRepository.findByAcademicYearIdOrderByLevelAsc(academicYearId);
    }

    public Grade createGrade(UUID academicYearId, String name, int level) {
        AcademicYear year = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> ApiException.notFound("Academic year not found"));
        return gradeRepository.save(new Grade(year, name, level));
    }

    // =========================================================================
    // Sections
    // =========================================================================
    public List<Section> getSectionsByGrade(UUID gradeId) {
        return sectionRepository.findByGradeId(gradeId);
    }

    public Section createSection(UUID gradeId, String name, int capacity) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> ApiException.notFound("Grade not found"));
        return sectionRepository.save(new Section(grade, name, capacity));
    }

    // =========================================================================
    // Subjects
    // =========================================================================
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Subject createSubject(String code, String name) {
        return subjectRepository.save(new Subject(code, name));
    }

    // =========================================================================
    // Manual Promotion Workflow (Section 4)
    // =========================================================================
    @Transactional(readOnly = true)
    public Map<String, Object> previewPromotion(List<UUID> studentIds, UUID destinationGradeId, UUID destinationSectionId) {
        Grade destGrade = gradeRepository.findById(destinationGradeId)
                .orElseThrow(() -> ApiException.notFound("Destination grade not found"));
        Section destSection = sectionRepository.findById(destinationSectionId)
                .orElseThrow(() -> ApiException.notFound("Destination section not found"));

        List<Map<String, Object>> previewList = new ArrayList<>();
        for (UUID sId : studentIds) {
            Optional<Student> studentOpt = studentRepository.findById(sId);
            if (studentOpt.isPresent()) {
                Student s = studentOpt.get();
                Map<String, Object> item = new HashMap<>();
                item.put("studentId", s.getId());
                item.put("studentNumber", s.getStudentNumber());
                item.put("fullName", s.getFullName());
                item.put("targetGrade", destGrade.getName());
                item.put("targetSection", destSection.getName());
                item.put("eligible", true);
                previewList.add(item);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalSelected", studentIds.size());
        result.put("destinationGrade", destGrade.getName());
        result.put("destinationSection", destSection.getName());
        result.put("students", previewList);
        return result;
    }

    @Transactional
    public Map<String, Object> confirmPromotion(
            List<UUID> studentIds,
            UUID targetAcademicYearId,
            UUID targetGradeId,
            UUID targetSectionId) {

        AcademicYear year = academicYearRepository.findById(targetAcademicYearId)
                .orElseThrow(() -> ApiException.notFound("Target academic year not found"));
        Grade grade = gradeRepository.findById(targetGradeId)
                .orElseThrow(() -> ApiException.notFound("Target grade not found"));
        Section section = sectionRepository.findById(targetSectionId)
                .orElseThrow(() -> ApiException.notFound("Target section not found"));

        int promotedCount = 0;
        for (UUID sId : studentIds) {
            Student student = studentRepository.findById(sId).orElse(null);
            if (student != null) {
                // Check if already enrolled in target year
                Optional<StudentEnrollment> existing = enrollmentRepository
                        .findByStudentIdAndAcademicYearId(student.getId(), year.getId());

                if (existing.isEmpty()) {
                    StudentEnrollment enrollment = new StudentEnrollment(
                            student, year, grade, section, LocalDate.now());
                    enrollment.setStatus("PROMOTED");
                    enrollmentRepository.save(enrollment);
                    promotedCount++;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("promotedCount", promotedCount);
        result.put("status", "SUCCESS");
        return result;
    }
}
