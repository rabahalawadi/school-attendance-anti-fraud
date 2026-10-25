package com.school.attendance.academic.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sections", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"grade_id", "name"})
})
public class Section {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;

    @Column(nullable = false, length = 100)
    private String name;

    @Column
    private int capacity = 30;

    @Column(nullable = false, length = 32)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Section() {
        this.id = UUID.randomUUID();
    }

    public Section(Grade grade, String name, int capacity) {
        this.id = UUID.randomUUID();
        this.grade = grade;
        this.name = name;
        this.capacity = capacity;
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
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
