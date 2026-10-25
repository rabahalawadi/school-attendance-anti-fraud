package com.school.attendance.fraud.entity;

import com.school.attendance.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verification_policies")
public class VerificationPolicy {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "scope_type", nullable = false, length = 64)
    private String scopeType = "GLOBAL"; // GLOBAL, GRADE, SECTION, SUBJECT

    @Column(name = "scope_id")
    private UUID scopeId;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "policy_configuration", nullable = false, columnDefinition = "TEXT")
    private String policyConfiguration;

    @Column(nullable = false)
    private int version = 1;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom = Instant.now();

    @Column(name = "effective_until")
    private Instant effectiveUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public VerificationPolicy() {
        this.id = UUID.randomUUID();
    }

    public VerificationPolicy(String name, String scopeType, UUID scopeId, String policyConfiguration) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.scopeType = scopeType != null ? scopeType : "GLOBAL";
        this.scopeId = scopeId;
        this.policyConfiguration = policyConfiguration;
        this.enabled = true;
        this.version = 1;
        this.effectiveFrom = Instant.now();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScopeType() {
        return scopeType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    public UUID getScopeId() {
        return scopeId;
    }

    public void setScopeId(UUID scopeId) {
        this.scopeId = scopeId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPolicyConfiguration() {
        return policyConfiguration;
    }

    public void setPolicyConfiguration(String policyConfiguration) {
        this.policyConfiguration = policyConfiguration;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Instant getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Instant effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public Instant getEffectiveUntil() {
        return effectiveUntil;
    }

    public void setEffectiveUntil(Instant effectiveUntil) {
        this.effectiveUntil = effectiveUntil;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
