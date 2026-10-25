package com.school.attendance.fraud.repository;

import com.school.attendance.fraud.entity.VerificationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VerificationAttemptRepository extends JpaRepository<VerificationAttempt, UUID> {
    List<VerificationAttempt> findByAttendanceSessionId(UUID sessionId);
    List<VerificationAttempt> findByStudentId(UUID studentId);
}
