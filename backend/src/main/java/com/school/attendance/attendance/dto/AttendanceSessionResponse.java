package com.school.attendance.attendance.dto;

import com.school.attendance.attendance.entity.AttendanceSession;
import com.school.attendance.attendance.entity.SessionStatus;

import java.time.Instant;
import java.util.UUID;

public record AttendanceSessionResponse(
        UUID id,
        AcademicYearInfo academicYear,
        SectionInfo section,
        SubjectInfo subject,
        TeacherInfo teacher,
        Instant scheduledStartAt,
        Instant scheduledEndAt,
        Instant openedAt,
        Instant closedAt,
        SessionStatus status,
        String policySnapshot
) {
    public record AcademicYearInfo(UUID id, String name) {}
    public record GradeInfo(UUID id, String name) {}
    public record SectionInfo(UUID id, String name, GradeInfo grade) {}
    public record SubjectInfo(UUID id, String name, String code) {}
    public record TeacherInfo(UUID id, String fullName) {}

    public static AttendanceSessionResponse fromEntity(AttendanceSession s) {
        if (s == null) return null;
        return new AttendanceSessionResponse(
                s.getId(),
                s.getAcademicYear() != null ? new AcademicYearInfo(s.getAcademicYear().getId(), s.getAcademicYear().getName()) : null,
                s.getSection() != null ? new SectionInfo(
                        s.getSection().getId(),
                        s.getSection().getName(),
                        s.getSection().getGrade() != null ? new GradeInfo(s.getSection().getGrade().getId(), s.getSection().getGrade().getName()) : null
                ) : null,
                s.getSubject() != null ? new SubjectInfo(s.getSubject().getId(), s.getSubject().getName(), s.getSubject().getCode()) : null,
                s.getTeacher() != null ? new TeacherInfo(s.getTeacher().getId(), s.getTeacher().getFullName()) : null,
                s.getScheduledStartAt(),
                s.getScheduledEndAt(),
                s.getOpenedAt(),
                s.getClosedAt(),
                s.getStatus(),
                s.getPolicySnapshot()
        );
    }
}
