package com.school.attendance.attendance.controller;

import com.school.attendance.attendance.dto.AttendanceRecordResponse;
import com.school.attendance.attendance.dto.RegisterAttendanceRequest;
import com.school.attendance.attendance.dto.RegisterAttendanceResponse;
import com.school.attendance.attendance.entity.AttendanceSession;
import com.school.attendance.attendance.service.AttendanceRegistrationService;
import com.school.attendance.attendance.service.AttendanceSessionService;
import com.school.attendance.common.dto.ApiResponse;
import com.school.attendance.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "Attendance Management", description = "Attendance sessions, dynamic challenge rotation, student check-in, and verification")
public class AttendanceController {

    private final AttendanceSessionService sessionService;
    private final AttendanceRegistrationService registrationService;

    public AttendanceController(
            AttendanceSessionService sessionService,
            AttendanceRegistrationService registrationService) {
        this.sessionService = sessionService;
        this.registrationService = registrationService;
    }

    @PostMapping("/sessions")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Schedule or initialize a new attendance session")
    public ResponseEntity<ApiResponse<AttendanceSession>> createSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        UUID sectionId = UUID.fromString((String) body.get("sectionId"));
        UUID subjectId = UUID.fromString((String) body.get("subjectId"));
        Instant startAt = body.get("scheduledStartAt") != null ? Instant.parse((String) body.get("scheduledStartAt")) : Instant.now();
        Instant endAt = body.get("scheduledEndAt") != null ? Instant.parse((String) body.get("scheduledEndAt")) : startAt.plusSeconds(3600);

        AttendanceSession session = sessionService.createSession(principal.getId(), sectionId, subjectId, startAt, endAt);
        return ResponseEntity.ok(ApiResponse.ok(session, "Attendance session created"));
    }

    @PostMapping("/sessions/{id}/open")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Open an attendance session and start challenge rotation")
    public ResponseEntity<ApiResponse<AttendanceSession>> openSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        AttendanceSession session = sessionService.openSession(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(session, "Session opened and challenges activated"));
    }

    @PostMapping("/sessions/{id}/close")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Close session, finalize attendance, and mark unrecorded students absent")
    public ResponseEntity<ApiResponse<AttendanceSession>> closeSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        AttendanceSession session = sessionService.closeSession(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(session, "Session closed successfully"));
    }

    @PostMapping("/sessions/{id}/cancel")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Cancel an attendance session")
    public ResponseEntity<ApiResponse<AttendanceSession>> cancelSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        AttendanceSession session = sessionService.cancelSession(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(session, "Session cancelled"));
    }

    @GetMapping("/sessions/{id}/challenge")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Get the active rotating QR code and six-digit challenge code")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChallenge(@PathVariable UUID id) {
        Map<String, Object> challenge = sessionService.getCurrentChallenge(id);
        return ResponseEntity.ok(ApiResponse.ok(challenge));
    }

    @GetMapping("/sessions/{id}/records")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Get attendance records for a session")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getSessionRecords(@PathVariable UUID id) {
        List<AttendanceRecordResponse> records = registrationService.getSessionRecords(id);
        return ResponseEntity.ok(ApiResponse.ok(records));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Student registers attendance using active verification factors")
    public ResponseEntity<ApiResponse<RegisterAttendanceResponse>> register(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RegisterAttendanceRequest request) {
        RegisterAttendanceResponse response = registrationService.register(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response, response.message()));
    }

    @GetMapping("/my-records")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get student's own attendance history")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getMyRecords(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<AttendanceRecordResponse> records = registrationService.getStudentRecords(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(records));
    }

    @PostMapping("/records/{id}/confirm")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Teacher manually confirms attendance (e.g. during 2-week window)")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> confirmAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        AttendanceRecordResponse record = registrationService.confirmAttendance(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(record, "Attendance manually confirmed"));
    }

    @PostMapping("/records/{id}/reject")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @Operation(summary = "Teacher manually rejects attendance")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> rejectAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "Teacher rejected attendance") : "Teacher rejected attendance";
        AttendanceRecordResponse record = registrationService.rejectAttendance(id, principal.getId(), reason);
        return ResponseEntity.ok(ApiResponse.ok(record, "Attendance rejected"));
    }
}
