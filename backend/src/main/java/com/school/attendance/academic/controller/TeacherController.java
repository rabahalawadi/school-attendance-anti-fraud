package com.school.attendance.academic.controller;

import com.school.attendance.academic.entity.Teacher;
import com.school.attendance.academic.entity.TeacherAssignment;
import com.school.attendance.academic.repository.TeacherAssignmentRepository;
import com.school.attendance.academic.repository.TeacherRepository;
import com.school.attendance.common.dto.ApiResponse;
import com.school.attendance.common.exception.ApiException;
import com.school.attendance.security.UserPrincipal;
import com.school.attendance.user.entity.Role;
import com.school.attendance.user.entity.User;
import com.school.attendance.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Teacher Management", description = "Teacher accounts and teaching schedules")
public class TeacherController {

    private final TeacherRepository teacherRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherController(
            TeacherRepository teacherRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Teacher>>> getAllTeachers() {
        return ResponseEntity.ok(ApiResponse.ok(teacherRepository.findAll()));
    }

    @GetMapping("/teachers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Teacher>> getTeacherById(@PathVariable UUID id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Teacher not found"));
        return ResponseEntity.ok(ApiResponse.ok(teacher));
    }

    @PostMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create teacher account and user credentials")
    public ResponseEntity<ApiResponse<Teacher>> createTeacher(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String employeeNumber = body.get("employeeNumber");

        if (userRepository.existsByUsername(username)) {
            throw ApiException.conflict("Username already taken");
        }
        if (teacherRepository.existsByEmployeeNumber(employeeNumber)) {
            throw ApiException.conflict("Employee number already registered");
        }

        String password = body.getOrDefault("password", "Password@123456");
        User user = new User(username, passwordEncoder.encode(password), Role.TEACHER);
        user = userRepository.save(user);

        Teacher teacher = new Teacher(user, employeeNumber, body.get("fullName"), body.get("contactEmail"));
        return ResponseEntity.ok(ApiResponse.ok(teacherRepository.save(teacher), "Teacher account created"));
    }

    @GetMapping("/teacher-assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<TeacherAssignment>>> getAssignments(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN) {
            return ResponseEntity.ok(ApiResponse.ok(teacherAssignmentRepository.findAll()));
        } else {
            Teacher teacher = teacherRepository.findByUserId(principal.getId())
                    .orElseThrow(() -> ApiException.notFound("Teacher record not found"));
            return ResponseEntity.ok(ApiResponse.ok(teacherAssignmentRepository.findByTeacherId(teacher.getId())));
        }
    }
}
