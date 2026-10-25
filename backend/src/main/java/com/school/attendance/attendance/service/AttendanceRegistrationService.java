package com.school.attendance.attendance.service;

import com.school.attendance.academic.entity.Student;
import com.school.attendance.academic.entity.StudentEnrollment;
import com.school.attendance.academic.repository.StudentEnrollmentRepository;
import com.school.attendance.academic.repository.StudentRepository;
import com.school.attendance.attendance.dto.AttendanceRecordResponse;
import com.school.attendance.attendance.dto.RegisterAttendanceRequest;
import com.school.attendance.attendance.dto.RegisterAttendanceResponse;
import com.school.attendance.attendance.entity.*;
import com.school.attendance.attendance.repository.AttendanceRecordRepository;
import com.school.attendance.attendance.repository.AttendanceSessionRepository;
import com.school.attendance.common.exception.ApiException;
import com.school.attendance.fraud.engine.AntiFraudEngine;
import com.school.attendance.fraud.model.VerificationResult;
import com.school.attendance.user.entity.User;
import com.school.attendance.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AttendanceRegistrationService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final AntiFraudEngine antiFraudEngine;

    public AttendanceRegistrationService(
            AttendanceSessionRepository sessionRepository,
            AttendanceRecordRepository recordRepository,
            StudentRepository studentRepository,
            StudentEnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            AntiFraudEngine antiFraudEngine) {
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.antiFraudEngine = antiFraudEngine;
    }

    @Transactional
    public RegisterAttendanceResponse register(UUID studentUserId, RegisterAttendanceRequest request) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> ApiException.forbidden("Authenticated user is not registered as a student"));

        AttendanceSession session = sessionRepository.findById(request.sessionId())
                .orElseThrow(() -> ApiException.notFound("Attendance session not found"));

        if (session.getStatus() != SessionStatus.OPEN) {
            throw ApiException.badRequest("Session is closed or not accepting registrations (status: " + session.getStatus() + ")");
        }

        // Verify student enrollment in this session's section
        Optional<StudentEnrollment> enrollment = enrollmentRepository
                .findByStudentIdAndAcademicYearId(student.getId(), session.getAcademicYear().getId());

        if (enrollment.isEmpty() || !enrollment.get().getSection().getId().equals(session.getSection().getId())) {
            throw ApiException.forbidden("You are not enrolled in this class or section");
        }

        // Prevent duplicate attendance records
        Optional<AttendanceRecord> existingRecord = recordRepository
                .findByAttendanceSessionIdAndStudentId(session.getId(), student.getId());

        if (existingRecord.isPresent()) {
            AttendanceRecord existing = existingRecord.get();
            return new RegisterAttendanceResponse(
                    existing.getId(),
                    true,
                    existing.getStatus(),
                    existing.getVerificationStatus(),
                    "Attendance is already registered for this session",
                    "LOW",
                    existing.getRecordedAt(),
                    List.of()
            );
        }

        // Evaluate anti-fraud checks
        Instant now = Instant.now();
        VerificationResult result = antiFraudEngine.evaluate(
                session,
                student,
                request.qrToken(),
                request.sixDigitCode(),
                request.deviceCredential(),
                request.latitude(),
                request.longitude(),
                request.accuracyMeters(),
                now
        );

        if (!result.accepted()) {
            return new RegisterAttendanceResponse(
                    null,
                    false,
                    result.attendanceStatus(),
                    result.verificationStatus(),
                    result.failureReason() != null ? result.failureReason() : "Verification criteria not satisfied",
                    result.riskLevel(),
                    now,
                    result.methodAudits()
            );
        }

        // Check late threshold (e.g. 15 minutes after session opened)
        AttendanceStatus calculatedStatus = result.attendanceStatus();
        if (session.getOpenedAt() != null && now.isAfter(session.getOpenedAt().plusSeconds(900))) {
            if (calculatedStatus == AttendanceStatus.PRESENT) {
                calculatedStatus = AttendanceStatus.LATE;
            }
        }

        AttendanceRecord record = new AttendanceRecord(
                session,
                student,
                calculatedStatus,
                result.verificationStatus(),
                now,
                request.idempotencyKey()
        );

        AttendanceRecord saved = recordRepository.save(record);

        String successMessage = result.verificationStatus() == VerificationStatus.PENDING_MANUAL
                ? "Attendance submitted and pending teacher confirmation"
                : "Attendance successfully recorded and verified";

        return new RegisterAttendanceResponse(
                saved.getId(),
                true,
                saved.getStatus(),
                saved.getVerificationStatus(),
                successMessage,
                result.riskLevel(),
                saved.getRecordedAt(),
                result.methodAudits()
        );
    }

    @Transactional
    public AttendanceRecordResponse confirmAttendance(UUID recordId, UUID teacherUserId) {
        AttendanceRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> ApiException.notFound("Attendance record not found"));

        User teacherUser = userRepository.findById(teacherUserId)
                .orElseThrow(() -> ApiException.unauthorized("User not found"));

        record.setVerificationStatus(VerificationStatus.MANUALLY_CONFIRMED);
        if (record.getStatus() == AttendanceStatus.PENDING_CONFIRMATION) {
            record.setStatus(AttendanceStatus.PRESENT);
        }
        record.setConfirmedBy(teacherUser);
        record.setConfirmedAt(Instant.now());

        AttendanceRecord saved = recordRepository.save(record);
        return mapToResponse(saved);
    }

    @Transactional
    public AttendanceRecordResponse rejectAttendance(UUID recordId, UUID teacherUserId, String reason) {
        AttendanceRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> ApiException.notFound("Attendance record not found"));

        User teacherUser = userRepository.findById(teacherUserId)
                .orElseThrow(() -> ApiException.unauthorized("User not found"));

        record.setStatus(AttendanceStatus.REJECTED);
        record.setVerificationStatus(VerificationStatus.FLAGGED);
        record.setCorrectionReason(reason);
        record.setConfirmedBy(teacherUser);
        record.setConfirmedAt(Instant.now());

        AttendanceRecord saved = recordRepository.save(record);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getSessionRecords(UUID sessionId) {
        return recordRepository.findByAttendanceSessionId(sessionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getStudentRecords(UUID studentUserId) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> ApiException.notFound("Student not found"));

        return recordRepository.findByStudentIdOrderByRecordedAtDesc(student.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private AttendanceRecordResponse mapToResponse(AttendanceRecord record) {
        AttendanceSession session = record.getAttendanceSession();
        Student student = record.getStudent();

        String teacherName = session.getTeacher() != null ? session.getTeacher().getFullName() : "N/A";
        String confirmedByName = record.getConfirmedBy() != null ? record.getConfirmedBy().getUsername() : null;

        return new AttendanceRecordResponse(
                record.getId(),
                session.getId(),
                session.getSubject().getName(),
                session.getSection().getGrade().getName() + " - " + session.getSection().getName(),
                teacherName,
                student.getId(),
                student.getStudentNumber(),
                student.getFullName(),
                record.getStatus(),
                record.getVerificationStatus(),
                record.getRecordedAt(),
                record.getServerReceivedAt(),
                confirmedByName,
                record.getConfirmedAt(),
                record.getCorrectionReason()
        );
    }
}
