package com.school.attendance.academic.entity;

import com.school.attendance.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "students")
public class Student {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "student_number", nullable = false, unique = true, length = 64)
    private String studentNumber;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone", length = 64)
    private String contactPhone;

    @Column(nullable = false, length = 32)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Student() {
        this.id = UUID.randomUUID();
    }

    public Student(User user, String studentNumber, String fullName, String contactEmail, String contactPhone) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.studentNumber = studentNumber;
        this.fullName = fullName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
