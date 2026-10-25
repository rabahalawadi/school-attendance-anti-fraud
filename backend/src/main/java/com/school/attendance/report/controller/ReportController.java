package com.school.attendance.report.controller;

import com.school.attendance.common.dto.ApiResponse;
import com.school.attendance.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports/attendance")
@Tag(name = "Reports & Analytics", description = "Attendance metrics, student percentages, and Excel/CSV exports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get school-wide attendance metrics, counts, and percentages")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getAdminSummary()));
    }

    @GetMapping("/students/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get student specific attendance analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudentAnalytics(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getStudentAnalytics(id)));
    }

    @GetMapping(value = "/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Export attendance records to Excel (.xlsx) or CSV")
    public ResponseEntity<?> exportAttendance(@RequestParam(defaultValue = "xlsx") String format) {
        if ("csv".equalsIgnoreCase(format)) {
            String csvContent = reportService.exportAttendanceCsv();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_export.csv")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(csvContent);
        } else {
            byte[] excelBytes = reportService.exportAttendanceExcel();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_export.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelBytes);
        }
    }
}
