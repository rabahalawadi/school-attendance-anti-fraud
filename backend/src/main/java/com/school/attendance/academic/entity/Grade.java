package com.school.attendance.academic.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "grades", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"academic_year_id", "name"})
})
public class Grade {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false, length = 32)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Grade() {
        this.id = UUID.randomUUID();
    }

    public Grade(AcademicYear academicYear, String name, int level) {
        this.id = UUID.randomUUID();
        this.academicYear = academicYear;
        this.name = name;
        this.level = level;
        this.status = "ACTIVE";
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
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
