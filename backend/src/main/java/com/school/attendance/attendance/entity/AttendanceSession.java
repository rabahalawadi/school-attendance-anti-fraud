package com.school.attendance.attendance.entity;

import com.school.attendance.academic.entity.AcademicYear;
import com.school.attendance.academic.entity.Section;
import com.school.attendance.academic.entity.Subject;
import com.school.attendance.academic.entity.Teacher;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attendance_sessions")
public class AttendanceSession {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(name = "scheduled_start_at", nullable = false)
    private Instant scheduledStartAt;

    @Column(name = "scheduled_end_at", nullable = false)
    private Instant scheduledEndAt;

    @Column(name = "opened_at")
    private Instant openedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Column(name = "policy_snapshot", nullable = false, columnDefinition = "TEXT")
    private String policySnapshot;

    @Column(name = "session_secret", nullable = false)
    private String sessionSecret;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AttendanceSession() {
        this.id = UUID.randomUUID();
        this.sessionSecret = UUID.randomUUID().toString().replace("-", "");
    }

    public AttendanceSession(
            AcademicYear academicYear,
            Section section,
            Subject subject,
            Teacher teacher,
            Instant scheduledStartAt,
            Instant scheduledEndAt,
            String policySnapshot) {
        this.id = UUID.randomUUID();
        this.academicYear = academicYear;
        this.section = section;
        this.subject = subject;
        this.teacher = teacher;
        this.scheduledStartAt = scheduledStartAt;
        this.scheduledEndAt = scheduledEndAt;
        this.policySnapshot = policySnapshot;
        this.sessionSecret = UUID.randomUUID().toString().replace("-", "");
        this.status = SessionStatus.SCHEDULED;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(AcademicYear academicYear) {
        this.academicYear = academicYear;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public Instant getScheduledStartAt() {
        return scheduledStartAt;
    }

    public void setScheduledStartAt(Instant scheduledStartAt) {
        this.scheduledStartAt = scheduledStartAt;
    }

    public Instant getScheduledEndAt() {
        return scheduledEndAt;
    }

    public void setScheduledEndAt(Instant scheduledEndAt) {
        this.scheduledEndAt = scheduledEndAt;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(Instant openedAt) {
        this.openedAt = openedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public String getPolicySnapshot() {
        return policySnapshot;
    }

    public void setPolicySnapshot(String policySnapshot) {
        this.policySnapshot = policySnapshot;
    }

    public String getSessionSecret() {
        return sessionSecret;
    }

    public void setSessionSecret(String sessionSecret) {
        this.sessionSecret = sessionSecret;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
