package com.school.attendance.device.entity;

import com.school.attendance.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "device_authorizations")
public class DeviceAuthorization {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private RegisteredDevice device;

    @Column(name = "authorized_scope", nullable = false, length = 64)
    private String authorizedScope; // SECTION, GRADE, SCHOOL_WIDE

    @Column(name = "scope_id")
    private UUID scopeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorized_by")
    private User authorizedBy;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    @Column(nullable = false, length = 32)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public DeviceAuthorization() {
        this.id = UUID.randomUUID();
    }

    public DeviceAuthorization(RegisteredDevice device, String authorizedScope, UUID scopeId, User authorizedBy, Instant validFrom, Instant validUntil) {
        this.id = UUID.randomUUID();
        this.device = device;
        this.authorizedScope = authorizedScope;
        this.scopeId = scopeId;
        this.authorizedBy = authorizedBy;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public RegisteredDevice getDevice() {
        return device;
    }

    public void setDevice(RegisteredDevice device) {
        this.device = device;
    }

    public String getAuthorizedScope() {
        return authorizedScope;
    }

    public void setAuthorizedScope(String authorizedScope) {
        this.authorizedScope = authorizedScope;
    }

    public UUID getScopeId() {
        return scopeId;
    }

    public void setScopeId(UUID scopeId) {
        this.scopeId = scopeId;
    }

    public User getAuthorizedBy() {
        return authorizedBy;
    }

    public void setAuthorizedBy(User authorizedBy) {
        this.authorizedBy = authorizedBy;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
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
