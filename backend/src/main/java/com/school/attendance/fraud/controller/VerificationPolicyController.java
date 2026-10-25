package com.school.attendance.fraud.controller;

import com.school.attendance.common.dto.ApiResponse;
import com.school.attendance.common.exception.ApiException;
import com.school.attendance.fraud.entity.VerificationPolicy;
import com.school.attendance.fraud.repository.VerificationPolicyRepository;
import com.school.attendance.security.UserPrincipal;
import com.school.attendance.user.entity.User;
import com.school.attendance.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/verification/policies")
@Tag(name = "Anti-Fraud Policies", description = "Administrator anti-fraud configuration and verification engine controls")
public class VerificationPolicyController {

    private final VerificationPolicyRepository policyRepository;
    private final UserRepository userRepository;

    public VerificationPolicyController(
            VerificationPolicyRepository policyRepository,
            UserRepository userRepository) {
        this.policyRepository = policyRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all anti-fraud policies")
    public ResponseEntity<ApiResponse<List<VerificationPolicy>>> getAllPolicies() {
        return ResponseEntity.ok(ApiResponse.ok(policyRepository.findAllByOrderByUpdatedAtDesc()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create or activate a new anti-fraud policy configuration")
    public ResponseEntity<ApiResponse<VerificationPolicy>> createPolicy(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {

        String name = (String) body.get("name");
        String scopeType = (String) body.getOrDefault("scopeType", "GLOBAL");
        String configJson = (String) body.get("policyConfiguration");

        User user = userRepository.findById(principal.getId()).orElse(null);

        VerificationPolicy policy = new VerificationPolicy(name, scopeType, null, configJson);
        policy.setUpdatedBy(user);
        VerificationPolicy saved = policyRepository.save(policy);

        return ResponseEntity.ok(ApiResponse.ok(saved, "Anti-fraud policy activated"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing anti-fraud policy")
    public ResponseEntity<ApiResponse<VerificationPolicy>> updatePolicy(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {

        VerificationPolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Policy not found"));

        if (body.containsKey("name")) policy.setName((String) body.get("name"));
        if (body.containsKey("enabled")) policy.setEnabled((Boolean) body.get("enabled"));
        if (body.containsKey("policyConfiguration")) policy.setPolicyConfiguration((String) body.get("policyConfiguration"));

        policy.setVersion(policy.getVersion() + 1);
        policy.setUpdatedAt(Instant.now());
        User user = userRepository.findById(principal.getId()).orElse(null);
        policy.setUpdatedBy(user);

        VerificationPolicy saved = policyRepository.save(policy);
        return ResponseEntity.ok(ApiResponse.ok(saved, "Anti-fraud policy updated"));
    }
}
