package com.school.attendance.device.controller;

import com.school.attendance.common.dto.ApiResponse;
import com.school.attendance.common.exception.ApiException;
import com.school.attendance.device.entity.RegisteredDevice;
import com.school.attendance.device.repository.RegisteredDeviceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/devices")
@Tag(name = "Registered Classroom Devices", description = "Authorized school PCs and kiosk terminals (Method A)")
public class DeviceController {

    private final RegisteredDeviceRepository deviceRepository;

    public DeviceController(RegisteredDeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get list of registered classroom devices")
    public ResponseEntity<ApiResponse<List<RegisteredDevice>>> getAllDevices() {
        return ResponseEntity.ok(ApiResponse.ok(deviceRepository.findAll()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register a school-owned classroom PC or terminal")
    public ResponseEntity<ApiResponse<RegisteredDevice>> registerDevice(@RequestBody Map<String, String> body) {
        String name = body.get("deviceName");
        String type = body.getOrDefault("deviceType", "CLASSROOM_PC");
        String classroom = body.get("classroom");
        String credRef = body.getOrDefault("credentialReference", "KIOSK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        RegisteredDevice device = new RegisteredDevice(name, type, classroom, credRef);
        return ResponseEntity.ok(ApiResponse.ok(deviceRepository.save(device), "Device registered"));
    }

    @PostMapping("/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Revoke device authorization immediately")
    public ResponseEntity<ApiResponse<RegisteredDevice>> revokeDevice(@PathVariable UUID id) {
        RegisteredDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Device not found"));

        device.setStatus("REVOKED");
        device.setRevokedAt(Instant.now());
        return ResponseEntity.ok(ApiResponse.ok(deviceRepository.save(device), "Device revoked"));
    }
}
