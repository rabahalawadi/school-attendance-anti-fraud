package com.school.attendance.fraud;

import com.school.attendance.academic.entity.*;
import com.school.attendance.attendance.entity.*;
import com.school.attendance.attendance.repository.AttendanceChallengeRepository;
import com.school.attendance.device.entity.RegisteredDevice;
import com.school.attendance.device.repository.RegisteredDeviceRepository;
import com.school.attendance.fraud.engine.AntiFraudEngine;
import com.school.attendance.fraud.model.VerificationResult;
import com.school.attendance.fraud.repository.VerificationAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AntiFraudEngineTest {

    @Mock
    private AttendanceChallengeRepository challengeRepository;

    @Mock
    private RegisteredDeviceRepository deviceRepository;

    @Mock
    private VerificationAttemptRepository attemptRepository;

    private AntiFraudEngine engine;
    private AttendanceSession session;
    private Student student;

    @BeforeEach
    void setUp() {
        engine = new AntiFraudEngine(challengeRepository, deviceRepository, attemptRepository);

        AcademicYear year = new AcademicYear("2026/2027", LocalDate.now(), LocalDate.now().plusMonths(9));
        Grade grade = new Grade(year, "Grade 10", 10);
        Section section = new Section(grade, "Section A", 30);
        Subject subject = new Subject("CS101", "Computer Science");
        Teacher teacher = new Teacher(null, "EMP-001", "Dr. Ahmad", "ahmad@school.edu");

        student = new Student(null, "STU-1001", "Tariq", "tariq@school.edu", "+962791112233");

        String policyJson = """
        {
            "deviceCheck": { "enabled": true, "required": false },
            "qrCheck": { "enabled": true, "required": true, "rotationSeconds": 15 },
            "sixDigitCheck": { "enabled": true, "required": false, "expirationSeconds": 30 },
            "gpsCheck": { "enabled": true, "required": false, "radiusMeters": 150.0, "maxAccuracyMeters": 80.0, "schoolLatitude": 32.0575, "schoolLongitude": 35.8890 },
            "manualReview": { "enabled": true, "initialPeriodDays": 14, "requireReviewForFlagged": true }
        }
        """;

        session = new AttendanceSession(year, section, subject, teacher, Instant.now(), Instant.now().plusSeconds(3600), policyJson);
        session.setOpenedAt(Instant.now());
    }

    @Test
    @DisplayName("Valid QR code within validity window accepts attendance")
    void testValidQrCodeAccepted() {
        Instant now = Instant.now();
        String validQrToken = "valid-qr-hash-12345";

        AttendanceChallenge challenge = new AttendanceChallenge(
                session, ChallengeType.QR_TOKEN, validQrToken, now.minusSeconds(2), now.plusSeconds(13));

        when(challengeRepository.findByAttendanceSessionIdAndChallengeTypeAndTokenHashAndStatus(
                session.getId(), ChallengeType.QR_TOKEN, validQrToken, "ACTIVE"))
                .thenReturn(Optional.of(challenge));

        VerificationResult result = engine.evaluate(
                session, student, validQrToken, null, null, null, null, null, now);

        assertTrue(result.accepted());
        assertEquals("MEDIUM", result.riskLevel(), "Risk level is MEDIUM because optional device check was skipped (personal device used)");
        assertEquals(VerificationStatus.PENDING_MANUAL, result.verificationStatus()); // During 14-day window
    }

    @Test
    @DisplayName("Expired QR token causes rejection when QR method is required")
    void testExpiredQrTokenRejected() {
        Instant now = Instant.now();
        String expiredQrToken = "expired-qr-hash";

        // Expired 5 seconds ago
        AttendanceChallenge challenge = new AttendanceChallenge(
                session, ChallengeType.QR_TOKEN, expiredQrToken, now.minusSeconds(20), now.minusSeconds(5));

        when(challengeRepository.findByAttendanceSessionIdAndChallengeTypeAndTokenHashAndStatus(
                session.getId(), ChallengeType.QR_TOKEN, expiredQrToken, "ACTIVE"))
                .thenReturn(Optional.of(challenge));

        VerificationResult result = engine.evaluate(
                session, student, expiredQrToken, null, null, null, null, null, now);

        assertFalse(result.accepted());
        assertEquals(AttendanceStatus.REJECTED, result.attendanceStatus());
        assertEquals(VerificationStatus.FLAGGED, result.verificationStatus());
        assertEquals("CRITICAL", result.riskLevel());
    }

    @Test
    @DisplayName("Method disabled in policy does not block registration or penalize student")
    void testDisabledMethodBypassed() {
        // Policy where QR check is disabled
        String disabledQrPolicy = """
        {
            "deviceCheck": { "enabled": false, "required": false },
            "qrCheck": { "enabled": false, "required": false, "rotationSeconds": 15 },
            "sixDigitCheck": { "enabled": false, "required": false, "expirationSeconds": 30 },
            "gpsCheck": { "enabled": false, "required": false },
            "manualReview": { "enabled": false, "initialPeriodDays": 0 }
        }
        """;
        session.setPolicySnapshot(disabledQrPolicy);

        VerificationResult result = engine.evaluate(
                session, student, null, null, null, null, null, null, Instant.now());

        assertTrue(result.accepted());
        assertEquals(AttendanceStatus.PRESENT, result.attendanceStatus());
        assertEquals(VerificationStatus.SERVER_VERIFIED, result.verificationStatus());
    }

    @Test
    @DisplayName("Optional GPS geofencing discrepancy logs advisory warning without rejecting")
    void testOptionalGpsDiscrepancy() {
        Instant now = Instant.now();
        String validQrToken = "valid-token";

        AttendanceChallenge challenge = new AttendanceChallenge(
                session, ChallengeType.QR_TOKEN, validQrToken, now.minusSeconds(1), now.plusSeconds(14));

        when(challengeRepository.findByAttendanceSessionIdAndChallengeTypeAndTokenHashAndStatus(
                session.getId(), ChallengeType.QR_TOKEN, validQrToken, "ACTIVE"))
                .thenReturn(Optional.of(challenge));

        // Coordinates far away (e.g. 50km away)
        VerificationResult result = engine.evaluate(
                session, student, validQrToken, null, null, 31.5000, 35.0000, 10.0, now);

        assertTrue(result.accepted(), "Attendance must be accepted because GPS is optional");
        assertEquals("MEDIUM", result.riskLevel(), "Risk level elevated to MEDIUM for location discrepancy");
    }

    @Test
    @DisplayName("Authorized classroom PC confirms Method A trusted hardware")
    void testAuthorizedClassroomPc() {
        Instant now = Instant.now();
        String validQrToken = "valid-token";
        String deviceCred = "KIOSK-LAB1-PC01";

        RegisteredDevice dev = new RegisteredDevice("Lab 1 PC 01", "CLASSROOM_PC", "Lab 1", deviceCred);
        dev.setStatus("AUTHORIZED");

        when(deviceRepository.findByCredentialReference(deviceCred)).thenReturn(Optional.of(dev));

        AttendanceChallenge challenge = new AttendanceChallenge(
                session, ChallengeType.QR_TOKEN, validQrToken, now.minusSeconds(1), now.plusSeconds(14));

        when(challengeRepository.findByAttendanceSessionIdAndChallengeTypeAndTokenHashAndStatus(
                session.getId(), ChallengeType.QR_TOKEN, validQrToken, "ACTIVE"))
                .thenReturn(Optional.of(challenge));

        VerificationResult result = engine.evaluate(
                session, student, validQrToken, null, deviceCred, null, null, null, now);

        assertTrue(result.accepted());
        assertTrue(result.methodAudits().stream().anyMatch(a -> "REGISTERED_DEVICE".equals(a.method()) && "PASSED".equals(a.result())));
    }
}
