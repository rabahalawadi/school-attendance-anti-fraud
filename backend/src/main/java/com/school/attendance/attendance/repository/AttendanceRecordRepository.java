package com.school.attendance.attendance.repository;

import com.school.attendance.attendance.entity.AttendanceRecord;
import com.school.attendance.attendance.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {
    Optional<AttendanceRecord> findByAttendanceSessionIdAndStudentId(UUID sessionId, UUID studentId);
    Optional<AttendanceRecord> findByIdempotencyKey(String idempotencyKey);
    List<AttendanceRecord> findByAttendanceSessionId(UUID sessionId);
    List<AttendanceRecord> findByStudentIdOrderByRecordedAtDesc(UUID studentId);
    List<AttendanceRecord> findByStudentIdAndRecordedAtBetween(UUID studentId, Instant start, Instant end);
    long countByAttendanceSessionIdAndStatus(UUID sessionId, AttendanceStatus status);
    long countByStudentIdAndStatus(UUID studentId, AttendanceStatus status);
    long countByStudentId(UUID studentId);
}
