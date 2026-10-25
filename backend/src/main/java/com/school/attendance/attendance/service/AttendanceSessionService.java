package com.school.attendance.attendance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.attendance.academic.entity.*;
import com.school.attendance.academic.repository.*;
import com.school.attendance.attendance.dto.AttendanceSessionResponse;
import com.school.attendance.attendance.entity.*;
import com.school.attendance.attendance.repository.*;
import com.school.attendance.common.exception.ApiException;
import com.school.attendance.fraud.entity.VerificationPolicy;
import com.school.attendance.fraud.model.AntiFraudPolicyModel;
import com.school.attendance.fraud.repository.VerificationPolicyRepository;
import com.school.attendance.security.UserPrincipal;
import com.school.attendance.user.entity.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceSessionService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceChallengeRepository challengeRepository;
    private final AttendanceRecordRepository recordRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicYearRepository academicYearRepository;
    private final StudentEnrollmentRepository enrollmentRepository;
    private final VerificationPolicyRepository policyRepository;

    private final SecureRandom secureRandom = new SecureRandom();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AttendanceSessionService(
            AttendanceSessionRepository sessionRepository,
            AttendanceChallengeRepository challengeRepository,
            AttendanceRecordRepository recordRepository,
            TeacherRepository teacherRepository,
            StudentRepository studentRepository,
            SectionRepository sectionRepository,
            SubjectRepository subjectRepository,
            AcademicYearRepository academicYearRepository,
            StudentEnrollmentRepository enrollmentRepository,
            VerificationPolicyRepository policyRepository) {
        this.sessionRepository = sessionRepository;
        this.challengeRepository = challengeRepository;
        this.recordRepository = recordRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.academicYearRepository = academicYearRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.policyRepository = policyRepository;
    }

    @Transactional
    public AttendanceSession createSession(
            UUID teacherUserId,
            UUID sectionId,
            UUID subjectId,
            Instant startAt,
            Instant endAt) {

        Teacher teacher = teacherRepository.findByUserId(teacherUserId)
                .orElseThrow(() -> ApiException.forbidden("Authenticated user is not registered as a teacher"));

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> ApiException.notFound("Section not found"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> ApiException.notFound("Subject not found"));

        AcademicYear academicYear = section.getGrade().getAcademicYear();

        // Snapshot applicable verification policy (scoped or global)
        String policySnapshot = resolvePolicySnapshot(sectionId);

        AttendanceSession session = new AttendanceSession(
                academicYear, section, subject, teacher, startAt, endAt, policySnapshot);

        return sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public List<AttendanceSessionResponse> getSessions(UserPrincipal principal, SessionStatus status) {
        List<AttendanceSession> sessions;
        if (principal == null) {
            sessions = status != null ? sessionRepository.findByStatus(status) : sessionRepository.findAll();
        } else if (principal.getRole() == Role.TEACHER) {
            Optional<Teacher> teacher = teacherRepository.findByUserId(principal.getId());
            if (teacher.isPresent()) {
                sessions = sessionRepository.findByTeacherIdOrderByScheduledStartAtDesc(teacher.get().getId());
                if (status != null) {
                    sessions = sessions.stream().filter(s -> s.getStatus() == status).toList();
                }
            } else {
                sessions = status != null ? sessionRepository.findByStatus(status) : sessionRepository.findAll();
            }
        } else if (principal.getRole() == Role.STUDENT) {
            Optional<Student> student = studentRepository.findByUserId(principal.getId());
            if (student.isPresent()) {
                List<StudentEnrollment> enrollments = enrollmentRepository.findByStudentId(student.get().getId());
                Set<UUID> sectionIds = enrollments.stream().map(e -> e.getSection().getId()).collect(Collectors.toSet());
                sessions = sessionRepository.findAll().stream()
                        .filter(s -> sectionIds.contains(s.getSection().getId()))
                        .filter(s -> status == null || s.getStatus() == status)
                        .toList();
            } else {
                sessions = status != null ? sessionRepository.findByStatus(status) : sessionRepository.findAll();
            }
        } else {
            sessions = status != null ? sessionRepository.findByStatus(status) : sessionRepository.findAll();
        }

        return sessions.stream().map(AttendanceSessionResponse::fromEntity).toList();
    }

    @Transactional
    public Map<String, Object> launchDemoSession() {
        UUID demoSessionId = UUID.fromString("c0000000-0000-0000-0000-000000000001");
        Optional<AttendanceSession> opt = sessionRepository.findById(demoSessionId);

        AttendanceSession session;
        if (opt.isPresent()) {
            session = opt.get();
            if (session.getStatus() != SessionStatus.OPEN) {
                session.setStatus(SessionStatus.OPEN);
                session.setOpenedAt(Instant.now());
                session = sessionRepository.save(session);
            }
        } else {
            AcademicYear year = academicYearRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> ApiException.badRequest("No academic year found"));
            Section section = sectionRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> ApiException.badRequest("No section found"));
            Subject subject = subjectRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> ApiException.badRequest("No subject found"));
            Teacher teacher = teacherRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> ApiException.badRequest("No teacher found"));

            session = new AttendanceSession(year, section, subject, teacher,
                    Instant.now().minusSeconds(600), Instant.now().plusSeconds(3000), resolvePolicySnapshot(section.getId()));
            session.setId(demoSessionId);
            session.setStatus(SessionStatus.OPEN);
            session.setOpenedAt(Instant.now());
            session = sessionRepository.save(session);
        }

        rotateChallenges(session);
        Map<String, Object> challenge = getCurrentChallenge(session.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("session", AttendanceSessionResponse.fromEntity(session));
        result.put("challenge", challenge);
        return result;
    }

    @Transactional
    public Map<String, Object> getActiveDemoSession() {
        return launchDemoSession();
    }

    @Transactional
    public AttendanceSession openSession(UUID sessionId, UUID teacherUserId) {
        AttendanceSession session = getAuthorizedSession(sessionId, teacherUserId);

        if (session.getStatus() == SessionStatus.OPEN) {
            return session;
        }

        if (session.getStatus() == SessionStatus.CLOSED || session.getStatus() == SessionStatus.CANCELLED) {
            throw ApiException.badRequest("Cannot open a session that is already " + session.getStatus());
        }

        session.setStatus(SessionStatus.OPEN);
        session.setOpenedAt(Instant.now());
        sessionRepository.save(session);

        // Generate initial challenges
        rotateChallenges(session);

        return session;
    }

    @Transactional
    public AttendanceSession closeSession(UUID sessionId, UUID teacherUserId) {
        AttendanceSession session = getAuthorizedSession(sessionId, teacherUserId);

        if (session.getStatus() == SessionStatus.CLOSED) {
            return session;
        }

        session.setStatus(SessionStatus.CLOSED);
        session.setClosedAt(Instant.now());
        sessionRepository.save(session);

        // Expire all active challenges
        List<AttendanceChallenge> activeChallenges = challengeRepository
                .findByAttendanceSessionIdAndStatus(sessionId, "ACTIVE");
        for (AttendanceChallenge ch : activeChallenges) {
            ch.setStatus("EXPIRED");
            challengeRepository.save(ch);
        }

        // Finalize unrecorded enrolled students as ABSENT
        List<StudentEnrollment> enrollments = enrollmentRepository
                .findBySectionIdAndStatus(session.getSection().getId(), "ENROLLED");

        for (StudentEnrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            Optional<AttendanceRecord> existing = recordRepository
                    .findByAttendanceSessionIdAndStudentId(sessionId, student.getId());

            if (existing.isEmpty()) {
                AttendanceRecord absentRecord = new AttendanceRecord(
                        session,
                        student,
                        AttendanceStatus.ABSENT,
                        VerificationStatus.SERVER_VERIFIED,
                        Instant.now(),
                        UUID.randomUUID().toString()
                );
                recordRepository.save(absentRecord);
            }
        }

        return session;
    }

    @Transactional
    public AttendanceSession cancelSession(UUID sessionId, UUID teacherUserId) {
        AttendanceSession session = getAuthorizedSession(sessionId, teacherUserId);
        session.setStatus(SessionStatus.CANCELLED);
        return sessionRepository.save(session);
    }

    @Transactional
    public Map<String, Object> getCurrentChallenge(UUID sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> ApiException.notFound("Session not found"));

        if (session.getStatus() != SessionStatus.OPEN) {
            throw ApiException.badRequest("Session is not open (status: " + session.getStatus() + ")");
        }

        Instant now = Instant.now();
        Optional<AttendanceChallenge> qrChallenge = challengeRepository
                .findFirstByAttendanceSessionIdAndChallengeTypeAndStatusAndExpiresAtAfterOrderByIssuedAtDesc(
                        sessionId, ChallengeType.QR_TOKEN, "ACTIVE", now);

        Optional<AttendanceChallenge> codeChallenge = challengeRepository
                .findFirstByAttendanceSessionIdAndChallengeTypeAndStatusAndExpiresAtAfterOrderByIssuedAtDesc(
                        sessionId, ChallengeType.SIX_DIGIT_CODE, "ACTIVE", now);

        // If either expired, rotate!
        if (qrChallenge.isEmpty() || codeChallenge.isEmpty()) {
            rotateChallenges(session);
            now = Instant.now();
            qrChallenge = challengeRepository
                    .findFirstByAttendanceSessionIdAndChallengeTypeAndStatusAndExpiresAtAfterOrderByIssuedAtDesc(
                            sessionId, ChallengeType.QR_TOKEN, "ACTIVE", now);
            codeChallenge = challengeRepository
                    .findFirstByAttendanceSessionIdAndChallengeTypeAndStatusAndExpiresAtAfterOrderByIssuedAtDesc(
                            sessionId, ChallengeType.SIX_DIGIT_CODE, "ACTIVE", now);
        }

        String qrToken = qrChallenge.map(AttendanceChallenge::getTokenHash).orElse("");
        String sixDigitCode = codeChallenge.map(AttendanceChallenge::getTokenHash).orElse("");
        long secondsRemaining = qrChallenge.map(ch -> Math.max(0, ch.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond())).orElse(0L);

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("qrToken", qrToken);
        response.put("sixDigitCode", sixDigitCode);
        response.put("expiresAt", qrChallenge.map(AttendanceChallenge::getExpiresAt).orElse(now));
        response.put("secondsRemaining", secondsRemaining);
        response.put("status", session.getStatus().name());

        return response;
    }

    @Transactional
    public void rotateChallenges(AttendanceSession session) {
        Instant now = Instant.now();
        AntiFraudPolicyModel policy = parsePolicy(session.getPolicySnapshot());

        int qrDuration = policy.getQrCheck().getRotationSeconds();
        if (qrDuration < 5) qrDuration = 15;

        int codeDuration = policy.getSixDigitCheck().getExpirationSeconds();
        if (codeDuration < 10) codeDuration = 30;

        // Generate cryptographically secure QR token
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        String qrToken = HexFormat.of().formatHex(randomBytes);

        // Generate 6-digit challenge code
        int codeInt = 100000 + secureRandom.nextInt(900000);
        String sixDigitCode = String.valueOf(codeInt);

        AttendanceChallenge qrCh = new AttendanceChallenge(
                session, ChallengeType.QR_TOKEN, qrToken, now, now.plusSeconds(qrDuration));
        AttendanceChallenge codeCh = new AttendanceChallenge(
                session, ChallengeType.SIX_DIGIT_CODE, sixDigitCode, now, now.plusSeconds(codeDuration));

        challengeRepository.save(qrCh);
        challengeRepository.save(codeCh);
    }

    private AttendanceSession getAuthorizedSession(UUID sessionId, UUID teacherUserId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> ApiException.notFound("Session not found"));

        Teacher teacher = teacherRepository.findByUserId(teacherUserId).orElse(null);
        if (teacher != null && !session.getTeacher().getId().equals(teacher.getId())) {
            throw ApiException.forbidden("You are not authorized to manage this session");
        }

        return session;
    }

    private String resolvePolicySnapshot(UUID sectionId) {
        Optional<VerificationPolicy> sectionPolicy = policyRepository
                .findFirstByScopeTypeAndScopeIdAndEnabledTrueOrderByVersionDesc("SECTION", sectionId);

        if (sectionPolicy.isPresent()) {
            return sectionPolicy.get().getPolicyConfiguration();
        }

        Optional<VerificationPolicy> globalPolicy = policyRepository
                .findFirstByScopeTypeAndEnabledTrueOrderByVersionDesc("GLOBAL");

        if (globalPolicy.isPresent()) {
            return globalPolicy.get().getPolicyConfiguration();
        }

        try {
            return objectMapper.writeValueAsString(new AntiFraudPolicyModel());
        } catch (Exception e) {
            return "{}";
        }
    }

    private AntiFraudPolicyModel parsePolicy(String json) {
        try {
            return objectMapper.readValue(json, AntiFraudPolicyModel.class);
        } catch (Exception e) {
            return new AntiFraudPolicyModel();
        }
    }
}
