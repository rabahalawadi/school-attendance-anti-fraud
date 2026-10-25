package com.school.attendance.fraud.entity;

import com.school.attendance.academic.entity.Student;
import com.school.attendance.attendance.entity.AttendanceSession;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verification_attempts")
public class VerificationAttempt {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_session_id", nullable = false)
    private AttendanceSession attendanceSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, length = 32)
    private String method; // REGISTERED_DEVICE, QR_CODE, SIX_DIGIT_CODE, GPS_GEOFENCE

    @Column(nullable = false, length = 32)
    private String result; // PASSED, FAILED, BYPASS, OPTIONAL_SKIPPED

    @Column(name = "reason_code", length = 64)
    private String reasonCode;

    @Column(name = "risk_level", nullable = false, length = 32)
    private String riskLevel = "LOW"; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt = Instant.now();

    @Column(name = "device_reference")
    private String deviceReference;

    public VerificationAttempt() {
        this.id = UUID.randomUUID();
    }

    public VerificationAttempt(
            AttendanceSession attendanceSession,
            Student student,
            String method,
            String result,
            String reasonCode,
            String riskLevel,
            String deviceReference) {
        this.id = UUID.randomUUID();
        this.attendanceSession = attendanceSession;
        this.student = student;
        this.method = method;
        this.result = result;
        this.reasonCode = reasonCode;
        this.riskLevel = riskLevel != null ? riskLevel : "LOW";
        this.attemptedAt = Instant.now();
        this.deviceReference = deviceReference;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AttendanceSession getAttendanceSession() {
        return attendanceSession;
    }

    public void setAttendanceSession(AttendanceSession attendanceSession) {
        this.attendanceSession = attendanceSession;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Instant getAttemptedAt() {
        return attemptedAt;
    }

    public String getDeviceReference() {
        return deviceReference;
    }

    public void setDeviceReference(String deviceReference) {
        this.deviceReference = deviceReference;
    }
}
