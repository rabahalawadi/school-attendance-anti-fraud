package com.school.attendance.common.entity;

import com.school.attendance.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actor;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(nullable = false, updatable = false)
    private Instant timestamp = Instant.now();

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "sanitized_metadata", columnDefinition = "TEXT")
    private String sanitizedMetadata;

    public AuditLog() {
        this.id = UUID.randomUUID();
    }

    public AuditLog(User actor, String action, String entityType, String entityId, String sanitizedMetadata) {
        this.id = UUID.randomUUID();
        this.actor = actor;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.sanitizedMetadata = sanitizedMetadata;
        this.timestamp = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getActor() {
        return actor;
    }

    public void setActor(User actor) {
        this.actor = actor;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSanitizedMetadata() {
        return sanitizedMetadata;
    }

    public void setSanitizedMetadata(String sanitizedMetadata) {
        this.sanitizedMetadata = sanitizedMetadata;
    }
}
