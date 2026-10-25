package com.school.attendance.fraud.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.attendance.academic.entity.Student;
import com.school.attendance.attendance.entity.AttendanceChallenge;
import com.school.attendance.attendance.entity.AttendanceSession;
import com.school.attendance.attendance.entity.AttendanceStatus;
import com.school.attendance.attendance.entity.ChallengeType;
import com.school.attendance.attendance.entity.VerificationStatus;
import com.school.attendance.attendance.repository.AttendanceChallengeRepository;
import com.school.attendance.device.entity.RegisteredDevice;
import com.school.attendance.device.repository.RegisteredDeviceRepository;
import com.school.attendance.fraud.entity.VerificationAttempt;
import com.school.attendance.fraud.model.AntiFraudPolicyModel;
import com.school.attendance.fraud.model.VerificationResult;
import com.school.attendance.fraud.repository.VerificationAttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class AntiFraudEngine {

    private static final Logger log = LoggerFactory.getLogger(AntiFraudEngine.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AttendanceChallengeRepository challengeRepository;
    private final RegisteredDeviceRepository deviceRepository;
    private final VerificationAttemptRepository attemptRepository;

    public AntiFraudEngine(
            AttendanceChallengeRepository challengeRepository,
            RegisteredDeviceRepository deviceRepository,
            VerificationAttemptRepository attemptRepository) {
        this.challengeRepository = challengeRepository;
        this.deviceRepository = deviceRepository;
        this.attemptRepository = attemptRepository;
    }

    public VerificationResult evaluate(
            AttendanceSession session,
            Student student,
            String qrToken,
            String sixDigitCode,
            String deviceCredential,
            Double latitude,
            Double longitude,
            Double accuracyMeters,
            Instant submissionTime) {

        Instant now = submissionTime != null ? submissionTime : Instant.now();
        AntiFraudPolicyModel policy = parsePolicy(session.getPolicySnapshot());
        List<VerificationResult.MethodAudit> audits = new ArrayList<>();

        boolean requiredFailed = false;
        String primaryFailureReason = null;
        String aggregateRisk = "LOW";

        // =====================================================================
        // 1. Method A: Registered Classroom Device Verification
        // =====================================================================
        AntiFraudPolicyModel.MethodConfig deviceConfig = policy.getDeviceCheck();
        if (!deviceConfig.isEnabled()) {
            audits.add(new VerificationResult.MethodAudit("REGISTERED_DEVICE", "BYPASS", "DISABLED", "LOW", "Device check disabled in policy"));
        } else {
            boolean deviceValid = false;
            if (deviceCredential != null && !deviceCredential.isBlank()) {
                Optional<RegisteredDevice> dev = deviceRepository.findByCredentialReference(deviceCredential);
                if (dev.isPresent() && "AUTHORIZED".equalsIgnoreCase(dev.get().getStatus())) {
                    deviceValid = true;
                    RegisteredDevice d = dev.get();
                    d.setLastSeenAt(now);
                    deviceRepository.save(d);
                }
            }

            if (deviceValid) {
                audits.add(new VerificationResult.MethodAudit("REGISTERED_DEVICE", "PASSED", "DEVICE_AUTHORIZED", "LOW", "Trusted classroom hardware confirmed"));
            } else if (deviceConfig.isRequired()) {
                requiredFailed = true;
                primaryFailureReason = "Attendance requires an authorized classroom device";
                aggregateRisk = "HIGH";
                audits.add(new VerificationResult.MethodAudit("REGISTERED_DEVICE", "FAILED", "UNAUTHORIZED_DEVICE", "HIGH", "Required device check failed"));
            } else {
                if ("LOW".equals(aggregateRisk)) aggregateRisk = "MEDIUM";
                audits.add(new VerificationResult.MethodAudit("REGISTERED_DEVICE", "OPTIONAL_SKIPPED", "PERSONAL_DEVICE", "MEDIUM", "Personal device used (check is optional)"));
            }
        }

        // =====================================================================
        // 2. Method B: Dynamic Rotating QR Code Verification
        // =====================================================================
        AntiFraudPolicyModel.QrConfig qrConfig = policy.getQrCheck();
        if (!qrConfig.isEnabled()) {
            audits.add(new VerificationResult.MethodAudit("QR_CODE", "BYPASS", "DISABLED", "LOW", "QR check disabled in policy"));
        } else {
            boolean qrValid = false;
            if (qrToken != null && !qrToken.isBlank()) {
                Optional<AttendanceChallenge> challenge = challengeRepository
                        .findByAttendanceSessionIdAndChallengeTypeAndTokenHashAndStatus(
                                session.getId(), ChallengeType.QR_TOKEN, qrToken, "ACTIVE");

                if (challenge.isPresent() && challenge.get().getExpiresAt().isAfter(now)) {
                    qrValid = true;
                }
            }

            if (qrValid) {
                audits.add(new VerificationResult.MethodAudit("QR_CODE", "PASSED", "QR_VALID", "LOW", "Dynamic rotating QR token validated within timeframe"));
            } else if (qrConfig.isRequired()) {
                requiredFailed = true;
                if (primaryFailureReason == null) {
                    primaryFailureReason = "QR code has expired or is invalid. Please scan the current code on screen.";
                }
                aggregateRisk = "CRITICAL";
                audits.add(new VerificationResult.MethodAudit("QR_CODE", "FAILED", "QR_EXPIRED_OR_INVALID", "CRITICAL", "QR code mismatch or expired on server"));
            } else {
                audits.add(new VerificationResult.MethodAudit("QR_CODE", "OPTIONAL_SKIPPED", "QR_MISMATCH", "MEDIUM", "Optional QR check not satisfied"));
            }
        }

        // =====================================================================
        // 3. Method C: Ephemeral Six-Digit Code Verification
        // =====================================================================
        AntiFraudPolicyModel.SixDigitConfig codeConfig = policy.getSixDigitCheck();
        if (!codeConfig.isEnabled()) {
            audits.add(new VerificationResult.MethodAudit("SIX_DIGIT_CODE", "BYPASS", "DISABLED", "LOW", "6-digit code disabled in policy"));
        } else {
            boolean codeValid = false;
            if (sixDigitCode != null && !sixDigitCode.isBlank()) {
                Optional<AttendanceChallenge> challenge = challengeRepository
                        .findByAttendanceSessionIdAndChallengeTypeAndTokenHashAndStatus(
                                session.getId(), ChallengeType.SIX_DIGIT_CODE, sixDigitCode, "ACTIVE");

                if (challenge.isPresent() && challenge.get().getExpiresAt().isAfter(now)) {
                    codeValid = true;
                }
            }

            if (codeValid) {
                audits.add(new VerificationResult.MethodAudit("SIX_DIGIT_CODE", "PASSED", "CODE_VALID", "LOW", "Ephemeral 6-digit challenge validated"));
            } else if (codeConfig.isRequired()) {
                requiredFailed = true;
                if (primaryFailureReason == null) {
                    primaryFailureReason = "6-digit code expired or incorrect.";
                }
                aggregateRisk = "HIGH";
                audits.add(new VerificationResult.MethodAudit("SIX_DIGIT_CODE", "FAILED", "CODE_INVALID", "HIGH", "6-digit challenge code failed"));
            } else {
                audits.add(new VerificationResult.MethodAudit("SIX_DIGIT_CODE", "OPTIONAL_SKIPPED", "CODE_SKIPPED", "LOW", "Optional 6-digit code omitted or mismatched"));
            }
        }

        // =====================================================================
        // 4. Method D: GPS Geofencing Verification (Advisory / Optional)
        // =====================================================================
        AntiFraudPolicyModel.GpsConfig gpsConfig = policy.getGpsCheck();
        if (!gpsConfig.isEnabled()) {
            audits.add(new VerificationResult.MethodAudit("GPS_GEOFENCE", "BYPASS", "DISABLED", "LOW", "GPS geofence disabled in policy"));
        } else {
            if (latitude != null && longitude != null) {
                double distanceMeters = calculateHaversineDistance(
                        latitude, longitude, gpsConfig.getSchoolLatitude(), gpsConfig.getSchoolLongitude());

                boolean withinRadius = distanceMeters <= gpsConfig.getRadiusMeters();
                boolean accuracyAcceptable = accuracyMeters == null || accuracyMeters <= gpsConfig.getMaxAccuracyMeters();

                if (withinRadius && accuracyAcceptable) {
                    audits.add(new VerificationResult.MethodAudit("GPS_GEOFENCE", "PASSED", "WITHIN_GEOFENCE", "LOW",
                            String.format("Distance %.1fm within %.0fm limit", distanceMeters, gpsConfig.getRadiusMeters())));
                } else if (gpsConfig.isRequired()) {
                    requiredFailed = true;
                    if (primaryFailureReason == null) {
                        primaryFailureReason = "Location is outside the authorized school area";
                    }
                    aggregateRisk = "HIGH";
                    audits.add(new VerificationResult.MethodAudit("GPS_GEOFENCE", "FAILED", "OUTSIDE_RADIUS", "HIGH",
                            String.format("Distance %.1fm exceeds perimeter or low accuracy (%.1fm)", distanceMeters, accuracyMeters != null ? accuracyMeters : 0)));
                } else {
                    if ("LOW".equals(aggregateRisk)) aggregateRisk = "MEDIUM";
                    audits.add(new VerificationResult.MethodAudit("GPS_GEOFENCE", "OPTIONAL_SKIPPED", "OUTSIDE_ADVISORY", "MEDIUM",
                            String.format("Location discrepancy (%.1fm from target), check is advisory", distanceMeters)));
                }
            } else {
                if (gpsConfig.isRequired()) {
                    requiredFailed = true;
                    if (primaryFailureReason == null) {
                        primaryFailureReason = "Location permission is required for attendance";
                    }
                    audits.add(new VerificationResult.MethodAudit("GPS_GEOFENCE", "FAILED", "LOCATION_UNAVAILABLE", "MEDIUM", "GPS coordinates missing on required check"));
                } else {
                    audits.add(new VerificationResult.MethodAudit("GPS_GEOFENCE", "OPTIONAL_SKIPPED", "LOCATION_OMITTED", "LOW", "No GPS hardware or location permission omitted (advisory)"));
                }
            }
        }

        // =====================================================================
        // Persist Audit Verification Attempts
        // =====================================================================
        for (VerificationResult.MethodAudit audit : audits) {
            VerificationAttempt attempt = new VerificationAttempt(
                    session,
                    student,
                    audit.method(),
                    audit.result(),
                    audit.reasonCode(),
                    audit.riskLevel(),
                    deviceCredential
            );
            attemptRepository.save(attempt);
        }

        // =====================================================================
        // Aggregate Final Decision
        // =====================================================================
        if (requiredFailed) {
            return new VerificationResult(
                    false,
                    AttendanceStatus.REJECTED,
                    VerificationStatus.FLAGGED,
                    primaryFailureReason,
                    aggregateRisk,
                    audits
            );
        }

        // Determine if manual review is required (e.g., initial 2-week window or flagged risk)
        AntiFraudPolicyModel.ManualReviewConfig manualConfig = policy.getManualReview();
        boolean needsManualReview = false;

        if (manualConfig.isEnabled()) {
            if ("HIGH".equals(aggregateRisk) || "CRITICAL".equals(aggregateRisk)) {
                needsManualReview = true;
            } else if (session.getOpenedAt() != null) {
                // If session is within the initial configured period days of the policy start
                long daysSinceSession = (now.toEpochMilli() - session.getOpenedAt().toEpochMilli()) / (1000 * 60 * 60 * 24);
                if (daysSinceSession < manualConfig.getInitialPeriodDays()) {
                    needsManualReview = true;
                }
            }
        }

        VerificationStatus vStatus = needsManualReview ? VerificationStatus.PENDING_MANUAL : VerificationStatus.SERVER_VERIFIED;
        AttendanceStatus aStatus = needsManualReview ? AttendanceStatus.PENDING_CONFIRMATION : AttendanceStatus.PRESENT;

        return new VerificationResult(
                true,
                aStatus,
                vStatus,
                null,
                aggregateRisk,
                audits
        );
    }

    private AntiFraudPolicyModel parsePolicy(String json) {
        if (json == null || json.isBlank()) {
            return new AntiFraudPolicyModel();
        }
        try {
            return objectMapper.readValue(json, AntiFraudPolicyModel.class);
        } catch (Exception e) {
            log.error("Failed to parse policy JSON snapshot, using secure defaults", e);
            return new AntiFraudPolicyModel();
        }
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Radius of the Earth in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
