package com.school.attendance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.attendance.academic.entity.*;
import com.school.attendance.academic.repository.*;
import com.school.attendance.attendance.dto.RegisterAttendanceRequest;
import com.school.attendance.attendance.entity.*;
import com.school.attendance.attendance.repository.AttendanceRecordRepository;
import com.school.attendance.attendance.service.AttendanceRegistrationService;
import com.school.attendance.attendance.service.AttendanceSessionService;
import com.school.attendance.user.entity.Role;
import com.school.attendance.user.entity.User;
import com.school.attendance.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class AttendanceLifecycleIntegrationTest {

    @Autowired
    private AttendanceSessionService sessionService;

    @Autowired
    private AttendanceRegistrationService registrationService;

    @Autowired
    private AttendanceRecordRepository recordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Test
    @DisplayName("Complete end-to-end lifecycle: Session Start -> Challenge Rotation -> Student Registration -> Duplicate Prevention -> Session Close")
    void testCompleteAttendanceLifecycle() {
        // Fetch seed data
        User teacherUser = userRepository.findByUsername("teacher.ahmad")
                .orElseThrow(() -> new AssertionError("Seed teacher not found"));
        Teacher teacher = teacherRepository.findByUserId(teacherUser.getId())
                .orElseThrow(() -> new AssertionError("Teacher record not found"));

        User studentUser = userRepository.findByUsername("student.tariq")
                .orElseThrow(() -> new AssertionError("Seed student not found"));
        Student student = studentRepository.findByUserId(studentUser.getId())
                .orElseThrow(() -> new AssertionError("Student record not found"));

        Section section = sectionRepository.findAll().getFirst();
        Subject subject = subjectRepository.findAll().getFirst();

        // 1. Teacher schedules and opens session
        AttendanceSession session = sessionService.createSession(
                teacherUser.getId(),
                section.getId(),
                subject.getId(),
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );
        assertNotNull(session.getId());
        assertEquals(SessionStatus.SCHEDULED, session.getStatus());

        session = sessionService.openSession(session.getId(), teacherUser.getId());
        assertEquals(SessionStatus.OPEN, session.getStatus());
        assertNotNull(session.getOpenedAt());

        // 2. Fetch active rotating challenge tokens
        Map<String, Object> challenge = sessionService.getCurrentChallenge(session.getId());
        assertNotNull(challenge);
        String qrToken = (String) challenge.get("qrToken");
        String sixDigitCode = (String) challenge.get("sixDigitCode");

        assertNotNull(qrToken);
        assertFalse(qrToken.isBlank());
        assertEquals(6, sixDigitCode.length());

        // 3. Student registers attendance using the dynamic challenge tokens
        RegisterAttendanceRequest request = new RegisterAttendanceRequest(
                session.getId(),
                qrToken,
                sixDigitCode,
                null,
                32.0575,
                35.8890,
                15.0,
                UUID.randomUUID().toString()
        );

        var response = registrationService.register(studentUser.getId(), request);
        assertTrue(response.accepted(), "Attendance should be accepted with valid QR and code");
        assertNotNull(response.recordId());

        // 4. Duplicate registration attempt by the same student returns idempotent existing record
        var duplicateResponse = registrationService.register(studentUser.getId(), request);
        assertTrue(duplicateResponse.accepted());
        assertEquals(response.recordId(), duplicateResponse.recordId(), "Must return existing record for duplicate check-in");

        // 5. Teacher confirms attendance during manual review window
        var confirmed = registrationService.confirmAttendance(response.recordId(), teacherUser.getId());
        assertEquals(VerificationStatus.MANUALLY_CONFIRMED, confirmed.verificationStatus());
        assertEquals(AttendanceStatus.PRESENT, confirmed.status());

        // 6. Teacher closes session
        session = sessionService.closeSession(session.getId(), teacherUser.getId());
        assertEquals(SessionStatus.CLOSED, session.getStatus());

        // 7. Verify unrecorded enrolled students (e.g. Layla) are automatically finalized as ABSENT
        User laylaUser = userRepository.findByUsername("student.layla")
                .orElseThrow(() -> new AssertionError("Seed student Layla not found"));
        Student layla = studentRepository.findByUserId(laylaUser.getId())
                .orElseThrow(() -> new AssertionError("Student Layla not found"));

        var laylaRecord = recordRepository.findByAttendanceSessionIdAndStudentId(session.getId(), layla.getId());
        assertTrue(laylaRecord.isPresent(), "Unrecorded student must have finalized record");
        assertEquals(AttendanceStatus.ABSENT, laylaRecord.get().getStatus(), "Unrecorded student must be marked ABSENT");
    }
}
