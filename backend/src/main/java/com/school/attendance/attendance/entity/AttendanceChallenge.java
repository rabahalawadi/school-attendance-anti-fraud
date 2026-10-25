package com.school.attendance.attendance.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attendance_challenges")
public class AttendanceChallenge {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_session_id", nullable = false)
    private AttendanceSession attendanceSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "challenge_type", nullable = false, length = 32)
    private ChallengeType challengeType;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false, length = 32)
    private String status = "ACTIVE";

    public AttendanceChallenge() {
        this.id = UUID.randomUUID();
    }

    public AttendanceChallenge(AttendanceSession attendanceSession, ChallengeType challengeType, String tokenHash, Instant issuedAt, Instant expiresAt) {
        this.id = UUID.randomUUID();
        this.attendanceSession = attendanceSession;
        this.challengeType = challengeType;
        this.tokenHash = tokenHash;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.status = "ACTIVE";
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

    public ChallengeType getChallengeType() {
        return challengeType;
    }

    public void setChallengeType(ChallengeType challengeType) {
        this.challengeType = challengeType;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
