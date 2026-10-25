package com.school.attendance.attendance.repository;

import com.school.attendance.attendance.entity.AttendanceChallenge;
import com.school.attendance.attendance.entity.ChallengeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceChallengeRepository extends JpaRepository<AttendanceChallenge, UUID> {
    Optional<AttendanceChallenge> findFirstByAttendanceSessionIdAndChallengeTypeAndStatusAndExpiresAtAfterOrderByIssuedAtDesc(
            UUID sessionId, ChallengeType challengeType, String status, Instant now);

    List<AttendanceChallenge> findByAttendanceSessionIdAndStatus(UUID sessionId, String status);

    Optional<AttendanceChallenge> findByAttendanceSessionIdAndChallengeTypeAndTokenHashAndStatus(
            UUID sessionId, ChallengeType challengeType, String tokenHash, String status);
}
