package com.school.attendance.offline.repository;

import com.school.attendance.offline.entity.OfflineSyncBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OfflineSyncBatchRepository extends JpaRepository<OfflineSyncBatch, UUID> {
    Optional<OfflineSyncBatch> findByBatchIdentifier(String batchIdentifier);
}
