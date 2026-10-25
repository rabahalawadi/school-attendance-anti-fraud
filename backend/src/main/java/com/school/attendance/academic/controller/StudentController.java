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
    @Operation(summary = "Download standard sample Excel or CSV import template")
    public ResponseEntity<byte[]> downloadTemplate(@RequestParam(defaultValue = "xlsx") String format) {
        if ("csv".equalsIgnoreCase(format)) {
            String csvContent = "Student Number,Full Name,Grade,Section,Email,Phone\n" +
                    "STU-2001,Yousef Al-Kahlout,Grade 10,Section A,yousef@student.school.edu,+962791234567\n" +
                    "STU-2002,Dana Al-Masri,Grade 10,Section A,dana@student.school.edu,+962792345678\n" +
                    "STU-2003,Zaid Qasim,Grade 10,Section B,zaid@student.school.edu,+962793456789\n";
            byte[] csvBytes = csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student_import_template.csv")
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(csvBytes);
        }

        byte[] excelBytes = excelImportService.generateTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student_import_template.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelBytes);
    }
}
