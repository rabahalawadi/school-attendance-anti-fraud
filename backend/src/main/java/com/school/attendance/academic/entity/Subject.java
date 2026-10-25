package com.school.attendance.academic.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 32)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Subject() {
        this.id = UUID.randomUUID();
    }

    public Subject(String code, String name) {
        this.id = UUID.randomUUID();
        this.code = code;
        this.name = name;
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
