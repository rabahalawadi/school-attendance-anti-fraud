package com.school.attendance.user.controller;

import com.school.attendance.common.dto.ApiResponse;
import com.school.attendance.user.dto.AuthRequest;
import com.school.attendance.user.dto.AuthResponse;
import com.school.attendance.user.dto.ChangePasswordRequest;
import com.school.attendance.user.dto.UserProfileResponse;
import com.school.attendance.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User login, profile, password change, and token refresh")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and receive JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.ok(response, "Token refreshed"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserProfileResponse profile = authService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for authenticated user")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Password changed successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Sign out user")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.ok(null, "Logged out successfully"));
    }
}
