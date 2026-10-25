package com.school.attendance.offline.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "offline_sync_batches")
public class OfflineSyncBatch {

    @Id
    private UUID id;

    @Column(name = "source_device_id", nullable = false, length = 100)
    private String sourceDeviceId;

    @Column(name = "batch_identifier", nullable = false, unique = true, length = 100)
    private String batchIdentifier;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt = Instant.now();

    @Column(name = "synchronization_status", nullable = false, length = 32)
    private String synchronizationStatus; // PROCESSED, PARTIALLY_PROCESSED, FAILED

    @Column(name = "result_summary", columnDefinition = "TEXT")
    private String resultSummary;

    public OfflineSyncBatch() {
        this.id = UUID.randomUUID();
    }

    public OfflineSyncBatch(String sourceDeviceId, String batchIdentifier, Instant createdAt, String synchronizationStatus, String resultSummary) {
        this.id = UUID.randomUUID();
        this.sourceDeviceId = sourceDeviceId;
        this.batchIdentifier = batchIdentifier;
        this.createdAt = createdAt;
        this.receivedAt = Instant.now();
        this.synchronizationStatus = synchronizationStatus;
        this.resultSummary = resultSummary;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSourceDeviceId() {
        return sourceDeviceId;
    }

    public void setSourceDeviceId(String sourceDeviceId) {
        this.sourceDeviceId = sourceDeviceId;
    }

    public String getBatchIdentifier() {
        return batchIdentifier;
    }

    public void setBatchIdentifier(String batchIdentifier) {
        this.batchIdentifier = batchIdentifier;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public String getSynchronizationStatus() {
        return synchronizationStatus;
    }

    public void setSynchronizationStatus(String synchronizationStatus) {
        this.synchronizationStatus = synchronizationStatus;
    }

    public String getResultSummary() {
        return resultSummary;
    }

    public void setResultSummary(String resultSummary) {
        this.resultSummary = resultSummary;
    }
}
