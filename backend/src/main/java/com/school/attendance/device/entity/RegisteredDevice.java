package com.school.attendance.device.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "registered_devices")
public class RegisteredDevice {

    @Id
    private UUID id;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    @Column(name = "device_type", nullable = false, length = 64)
    private String deviceType; // CLASSROOM_PC, KIOSK_TERMINAL, TABLET

    @Column(nullable = false, length = 100)
    private String classroom;

    @Column(name = "credential_reference", nullable = false, unique = true)
    private String credentialReference;

    @Column(nullable = false, length = 32)
    private String status = "AUTHORIZED"; // AUTHORIZED, REVOKED, PENDING

    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt = Instant.now();

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public RegisteredDevice() {
        this.id = UUID.randomUUID();
    }

    public RegisteredDevice(String deviceName, String deviceType, String classroom, String credentialReference) {
        this.id = UUID.randomUUID();
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.classroom = classroom;
        this.credentialReference = credentialReference;
        this.status = "AUTHORIZED";
        this.registeredAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public String getCredentialReference() {
        return credentialReference;
    }

    public void setCredentialReference(String credentialReference) {
        this.credentialReference = credentialReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }
}
