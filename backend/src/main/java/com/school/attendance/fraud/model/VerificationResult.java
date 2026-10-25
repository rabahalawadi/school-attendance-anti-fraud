package com.school.attendance.fraud.model;

import com.school.attendance.attendance.entity.AttendanceStatus;
import com.school.attendance.attendance.entity.VerificationStatus;

import java.util.List;

public record VerificationResult(
    boolean accepted,
    AttendanceStatus attendanceStatus,
    VerificationStatus verificationStatus,
    String failureReason,
    String riskLevel, // LOW, MEDIUM, HIGH, CRITICAL
    List<MethodAudit> methodAudits
) {
    public record MethodAudit(
        String method,
        String result, // PASSED, FAILED, BYPASS, OPTIONAL_SKIPPED
        String reasonCode,
        String riskLevel,
        String detail
    ) {}
}
