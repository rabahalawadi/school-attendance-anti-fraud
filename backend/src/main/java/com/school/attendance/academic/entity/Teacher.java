package com.school.attendance.academic.entity;

import com.school.attendance.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "employee_number", nullable = false, unique = true, length = 64)
    private String employeeNumber;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(nullable = false, length = 32)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Teacher() {
        this.id = UUID.randomUUID();
    }

    public Teacher(User user, String employeeNumber, String fullName, String contactEmail) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.employeeNumber = employeeNumber;
        this.fullName = fullName;
        this.contactEmail = contactEmail;
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

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
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
