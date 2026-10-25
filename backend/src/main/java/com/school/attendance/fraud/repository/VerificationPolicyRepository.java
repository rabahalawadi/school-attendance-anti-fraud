package com.school.attendance.fraud.repository;

import com.school.attendance.fraud.entity.VerificationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationPolicyRepository extends JpaRepository<VerificationPolicy, UUID> {
    Optional<VerificationPolicy> findFirstByScopeTypeAndEnabledTrueOrderByVersionDesc(String scopeType);
    Optional<VerificationPolicy> findFirstByScopeTypeAndScopeIdAndEnabledTrueOrderByVersionDesc(String scopeType, UUID scopeId);
    List<VerificationPolicy> findAllByOrderByUpdatedAtDesc();
}
