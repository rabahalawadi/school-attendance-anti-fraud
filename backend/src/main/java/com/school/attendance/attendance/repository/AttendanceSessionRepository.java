package com.school.attendance.attendance.repository;

import com.school.attendance.attendance.entity.AttendanceSession;
import com.school.attendance.attendance.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, UUID> {
    List<AttendanceSession> findByTeacherIdOrderByScheduledStartAtDesc(UUID teacherId);
    List<AttendanceSession> findBySectionIdOrderByScheduledStartAtDesc(UUID sectionId);
    List<AttendanceSession> findByStatus(SessionStatus status);
    Optional<AttendanceSession> findByIdAndTeacherId(UUID id, UUID teacherId);
    List<AttendanceSession> findByScheduledStartAtBetween(Instant start, Instant end);
}
