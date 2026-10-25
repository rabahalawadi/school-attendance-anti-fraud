package com.school.attendance.academic.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "student_subject_enrollments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_enrollment_id", "subject_id"})
})
public class StudentSubjectEnrollment {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_enrollment_id", nullable = false)
    private StudentEnrollment studentEnrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "enrollment_status", nullable = false, length = 32)
    private String enrollmentStatus = "ENROLLED";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public StudentSubjectEnrollment() {
        this.id = UUID.randomUUID();
    }

    public StudentSubjectEnrollment(StudentEnrollment studentEnrollment, Subject subject) {
        this.id = UUID.randomUUID();
        this.studentEnrollment = studentEnrollment;
        this.subject = subject;
        this.enrollmentStatus = "ENROLLED";
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public StudentEnrollment getStudentEnrollment() {
        return studentEnrollment;
    }

    public void setStudentEnrollment(StudentEnrollment studentEnrollment) {
        this.studentEnrollment = studentEnrollment;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(String enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
