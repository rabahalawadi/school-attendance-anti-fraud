package com.school.attendance.common.controller;

import com.school.attendance.common.dto.ApiResponse;
import com.school.attendance.common.entity.AuditLog;
import com.school.attendance.common.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "Audit Logs", description = "System-wide security and operational audit trail")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "View audit logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        return ResponseEntity.ok(ApiResponse.ok(auditLogRepository.findAllByOrderByTimestampDesc()));
    }
}
