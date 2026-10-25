package com.school.attendance.attendance.dto;

import com.school.attendance.attendance.entity.AttendanceStatus;
import com.school.attendance.attendance.entity.VerificationStatus;
import com.school.attendance.fraud.model.VerificationResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RegisterAttendanceResponse(
    UUID recordId,
    boolean accepted,
    AttendanceStatus attendanceStatus,
    VerificationStatus verificationStatus,
    String message,
    String riskLevel,
    Instant recordedAt,
    List<VerificationResult.MethodAudit> methodAudits
) {}
