package com.school.attendance.academic.controller;

import com.school.attendance.academic.entity.AcademicYear;
import com.school.attendance.academic.entity.Grade;
import com.school.attendance.academic.entity.Section;
import com.school.attendance.academic.entity.Subject;
import com.school.attendance.academic.service.AcademicService;
import com.school.attendance.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Academic Structure", description = "Management of Academic Years, Grades, Sections, and Subjects")
public class AcademicController {

    private final AcademicService academicService;

    public AcademicController(AcademicService academicService) {
        this.academicService = academicService;
    }

    // --- Academic Years ---
    @GetMapping("/academic-years")
    public ResponseEntity<ApiResponse<List<AcademicYear>>> getAcademicYears() {
        return ResponseEntity.ok(ApiResponse.ok(academicService.getAllAcademicYears()));
    }

    @PostMapping("/academic-years")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AcademicYear>> createAcademicYear(@RequestBody Map<String, String> body) {
        AcademicYear year = academicService.createAcademicYear(
                body.get("name"),
                LocalDate.parse(body.get("startDate")),
                LocalDate.parse(body.get("endDate"))
        );
        return ResponseEntity.ok(ApiResponse.ok(year, "Academic year created"));
    }

    // --- Grades ---
    @GetMapping("/grades")
    public ResponseEntity<ApiResponse<List<Grade>>> getGrades(@RequestParam UUID academicYearId) {
        return ResponseEntity.ok(ApiResponse.ok(academicService.getGradesByYear(academicYearId)));
    }

    @PostMapping("/grades")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Grade>> createGrade(@RequestBody Map<String, Object> body) {
        UUID yearId = UUID.fromString((String) body.get("academicYearId"));
        String name = (String) body.get("name");
        int level = body.get("level") instanceof Number num ? num.intValue() : Integer.parseInt(body.get("level").toString());

        Grade grade = academicService.createGrade(yearId, name, level);
        return ResponseEntity.ok(ApiResponse.ok(grade, "Grade created"));
    }

    // --- Sections ---
    @GetMapping("/sections")
    public ResponseEntity<ApiResponse<List<Section>>> getSections(@RequestParam UUID gradeId) {
        return ResponseEntity.ok(ApiResponse.ok(academicService.getSectionsByGrade(gradeId)));
    }

    @PostMapping("/sections")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Section>> createSection(@RequestBody Map<String, Object> body) {
        UUID gradeId = UUID.fromString((String) body.get("gradeId"));
        String name = (String) body.get("name");
        int capacity = body.containsKey("capacity") ? ((Number) body.get("capacity")).intValue() : 30;

        Section section = academicService.createSection(gradeId, name, capacity);
        return ResponseEntity.ok(ApiResponse.ok(section, "Section created"));
    }

    // --- Subjects ---
    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<List<Subject>>> getSubjects() {
        return ResponseEntity.ok(ApiResponse.ok(academicService.getAllSubjects()));
    }

    @PostMapping("/subjects")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Subject>> createSubject(@RequestBody Map<String, String> body) {
        Subject subject = academicService.createSubject(body.get("code"), body.get("name"));
        return ResponseEntity.ok(ApiResponse.ok(subject, "Subject created"));
    }

    // --- Promotion Workflow ---
    @PostMapping("/enrollments/promotions/preview")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Preview students promotion to a destination grade and section")
    public ResponseEntity<ApiResponse<Map<String, Object>>> previewPromotion(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> studentIdStrs = (List<String>) body.get("studentIds");
        List<UUID> studentIds = studentIdStrs.stream().map(UUID::fromString).toList();
        UUID destGradeId = UUID.fromString((String) body.get("destinationGradeId"));
        UUID destSectionId = UUID.fromString((String) body.get("destinationSectionId"));

        Map<String, Object> preview = academicService.previewPromotion(studentIds, destGradeId, destSectionId);
        return ResponseEntity.ok(ApiResponse.ok(preview));
    }

    @PostMapping("/enrollments/promotions/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Confirm and commit manual grade promotion without modifying historical records")
    public ResponseEntity<ApiResponse<Map<String, Object>>> confirmPromotion(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> studentIdStrs = (List<String>) body.get("studentIds");
        List<UUID> studentIds = studentIdStrs.stream().map(UUID::fromString).toList();
        UUID targetYearId = UUID.fromString((String) body.get("targetAcademicYearId"));
        UUID targetGradeId = UUID.fromString((String) body.get("targetGradeId"));
        UUID targetSectionId = UUID.fromString((String) body.get("targetSectionId"));

        Map<String, Object> result = academicService.confirmPromotion(studentIds, targetYearId, targetGradeId, targetSectionId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Promotion committed successfully"));
    }
}
