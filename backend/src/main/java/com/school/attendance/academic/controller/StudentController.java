package com.school.attendance.academic.controller;

import com.school.attendance.academic.entity.Student;
import com.school.attendance.academic.repository.StudentRepository;
import com.school.attendance.academic.service.ExcelImportService;
import com.school.attendance.common.dto.ApiResponse;
import com.school.attendance.common.exception.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students")
@Tag(name = "Student Management", description = "Student records, Excel bulk import, and student profiles")
public class StudentController {

    private final StudentRepository studentRepository;
    private final ExcelImportService excelImportService;

    public StudentController(
            StudentRepository studentRepository,
            ExcelImportService excelImportService) {
        this.studentRepository = studentRepository;
        this.excelImportService = excelImportService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<Student>>> getAllStudents() {
        return ResponseEntity.ok(ApiResponse.ok(studentRepository.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Student>> getStudentById(@PathVariable UUID id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Student not found"));
        return ResponseEntity.ok(ApiResponse.ok(student));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Student>> createStudent(@RequestBody Map<String, String> body) {
        String studentNumber = body.get("studentNumber");
        if (studentRepository.existsByStudentNumber(studentNumber)) {
            throw ApiException.conflict("Student with number " + studentNumber + " already exists");
        }
        Student student = new Student(
                null,
                studentNumber,
                body.get("fullName"),
                body.get("contactEmail"),
                body.get("contactPhone")
        );
        return ResponseEntity.ok(ApiResponse.ok(studentRepository.save(student), "Student created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Student>> updateStudent(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Student not found"));

        if (body.containsKey("fullName")) student.setFullName(body.get("fullName"));
        if (body.containsKey("contactEmail")) student.setContactEmail(body.get("contactEmail"));
        if (body.containsKey("contactPhone")) student.setContactPhone(body.get("contactPhone"));
        if (body.containsKey("status")) student.setStatus(body.get("status"));

        return ResponseEntity.ok(ApiResponse.ok(studentRepository.save(student), "Student updated"));
    }

    @PostMapping(value = "/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Validate and preview student Excel sheet before committing")
    public ResponseEntity<ApiResponse<Map<String, Object>>> previewImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("academicYearId") UUID academicYearId) {
        Map<String, Object> preview = excelImportService.previewExcel(file, academicYearId);
        return ResponseEntity.ok(ApiResponse.ok(preview));
    }

    @PostMapping("/import/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Commit validated Excel records into database")
    public ResponseEntity<ApiResponse<Map<String, Object>>> confirmImport(
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> records = (List<Map<String, String>>) body.get("records");
        UUID academicYearId = UUID.fromString((String) body.get("academicYearId"));

        Map<String, Object> result = excelImportService.confirmImport(records, academicYearId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Students imported successfully"));
    }

    @GetMapping("/import/template")
    @Operation(summary = "Download standard sample Excel import template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] excelBytes = excelImportService.generateTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student_import_template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }
}
