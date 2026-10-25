package com.school.attendance.report.service;

import com.school.attendance.academic.repository.*;
import com.school.attendance.attendance.entity.AttendanceStatus;
import com.school.attendance.attendance.entity.VerificationStatus;
import com.school.attendance.attendance.repository.AttendanceRecordRepository;
import com.school.attendance.attendance.repository.AttendanceSessionRepository;
import com.school.attendance.fraud.repository.VerificationAttemptRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ReportService {

    private final AttendanceRecordRepository recordRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final VerificationAttemptRepository attemptRepository;

    public ReportService(
            AttendanceRecordRepository recordRepository,
            AttendanceSessionRepository sessionRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            SectionRepository sectionRepository,
            SubjectRepository subjectRepository,
            VerificationAttemptRepository attemptRepository) {
        this.recordRepository = recordRepository;
        this.sessionRepository = sessionRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.sectionRepository = sectionRepository;
        this.subjectRepository = subjectRepository;
        this.attemptRepository = attemptRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminSummary() {
        long totalStudents = studentRepository.count();
        long totalTeachers = teacherRepository.count();
        long totalSections = sectionRepository.count();
        long totalSubjects = subjectRepository.count();

        Instant startOfToday = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Instant endOfToday = startOfToday.plus(1, ChronoUnit.DAYS);

        var todaySessions = sessionRepository.findByScheduledStartAtBetween(startOfToday, endOfToday);
        long sessionsCount = todaySessions.size();

        var allRecords = recordRepository.findAll();
        long presentCount = allRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long lateCount = allRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();
        long absentCount = allRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
        long excusedCount = allRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.EXCUSED).count();
        long pendingReviewCount = allRecords.stream().filter(r -> r.getVerificationStatus() == VerificationStatus.PENDING_MANUAL).count();
        long suspiciousCount = allRecords.stream().filter(r -> r.getVerificationStatus() == VerificationStatus.FLAGGED).count();

        long eligibleTotal = presentCount + lateCount + absentCount;
        double overallPercentage = eligibleTotal > 0 ? ((double) (presentCount + lateCount) / eligibleTotal) * 100.0 : 100.0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalStudents", totalStudents);
        summary.put("totalTeachers", totalTeachers);
        summary.put("totalSections", totalSections);
        summary.put("totalSubjects", totalSubjects);
        summary.put("todaySessionsCount", sessionsCount);
        summary.put("presentCount", presentCount);
        summary.put("lateCount", lateCount);
        summary.put("absentCount", absentCount);
        summary.put("excusedCount", excusedCount);
        summary.put("pendingReviewCount", pendingReviewCount);
        summary.put("suspiciousAttemptsCount", suspiciousCount);
        summary.put("overallAttendancePercentage", Math.round(overallPercentage * 10.0) / 10.0);

        return summary;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentAnalytics(UUID studentId) {
        long total = recordRepository.countByStudentId(studentId);
        long present = recordRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.PRESENT);
        long late = recordRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.LATE);
        long absent = recordRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.ABSENT);
        long excused = recordRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.EXCUSED);

        long eligible = present + late + absent;
        double percentage = eligible > 0 ? ((double) (present + late) / eligible) * 100.0 : 100.0;

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalRecorded", total);
        analytics.put("presentCount", present);
        analytics.put("lateCount", late);
        analytics.put("absentCount", absent);
        analytics.put("excusedCount", excused);
        analytics.put("attendancePercentage", Math.round(percentage * 10.0) / 10.0);

        return analytics;
    }

    @Transactional(readOnly = true)
    public byte[] exportAttendanceExcel() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Attendance Records");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            String[] headers = {"Date", "Class", "Subject", "Teacher", "Student ID", "Student Name", "Status", "Verification", "Recorded At"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }

            var records = recordRepository.findAll();
            int rowIndex = 1;
            for (var rec : records) {
                Row row = sheet.createRow(rowIndex++);
                var session = rec.getAttendanceSession();
                var student = rec.getStudent();

                row.createCell(0).setCellValue(rec.getRecordedAt().toString().substring(0, 10));
                row.createCell(1).setCellValue(session.getSection().getGrade().getName() + " - " + session.getSection().getName());
                row.createCell(2).setCellValue(session.getSubject().getName());
                row.createCell(3).setCellValue(session.getTeacher().getFullName());
                row.createCell(4).setCellValue(student.getStudentNumber());
                row.createCell(5).setCellValue(student.getFullName());
                row.createCell(6).setCellValue(rec.getStatus().name());
                row.createCell(7).setCellValue(rec.getVerificationStatus().name());
                row.createCell(8).setCellValue(rec.getRecordedAt().toString());
            }

            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export attendance Excel", e);
        }
    }

    @Transactional(readOnly = true)
    public String exportAttendanceCsv() {
        try (StringWriter sw = new StringWriter();
             CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder()
                     .setHeader("Date", "Class", "Subject", "Teacher", "StudentID", "StudentName", "Status", "Verification", "RecordedAt")
                     .build())) {

            var records = recordRepository.findAll();
            for (var rec : records) {
                var session = rec.getAttendanceSession();
                var student = rec.getStudent();

                printer.printRecord(
                        rec.getRecordedAt().toString().substring(0, 10),
                        session.getSection().getGrade().getName() + " - " + session.getSection().getName(),
                        session.getSubject().getName(),
                        session.getTeacher().getFullName(),
                        student.getStudentNumber(),
                        student.getFullName(),
                        rec.getStatus().name(),
                        rec.getVerificationStatus().name(),
                        rec.getRecordedAt().toString()
                );
            }
            printer.flush();
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export attendance CSV", e);
        }
    }
}
