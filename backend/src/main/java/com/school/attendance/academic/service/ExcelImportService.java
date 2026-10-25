package com.school.attendance.academic.service;

import com.school.attendance.academic.entity.*;
import com.school.attendance.academic.repository.*;
import com.school.attendance.common.exception.ApiException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

@Service
public class ExcelImportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelImportService.class);

    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository enrollmentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final GradeRepository gradeRepository;
    private final SectionRepository sectionRepository;

    public ExcelImportService(
            StudentRepository studentRepository,
            StudentEnrollmentRepository enrollmentRepository,
            AcademicYearRepository academicYearRepository,
            GradeRepository gradeRepository,
            SectionRepository sectionRepository) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.academicYearRepository = academicYearRepository;
        this.gradeRepository = gradeRepository;
        this.sectionRepository = sectionRepository;
    }

    public Map<String, Object> previewExcel(MultipartFile file, UUID academicYearId) {
        validateFile(file);

        List<Map<String, Object>> validRecords = new ArrayList<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        Set<String> seenStudentNumbers = new HashSet<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                throw ApiException.badRequest("Uploaded spreadsheet is empty");
            }

            // Header Row
            Row headerRow = rowIterator.next();
            Map<String, Integer> columnMap = parseHeaders(headerRow);

            int rowIndex = 1;
            while (rowIterator.hasNext()) {
                rowIndex++;
                Row row = rowIterator.next();
                if (isRowEmpty(row)) continue;

                String studentNumber = sanitizeCell(getCellString(row, columnMap.get("student_number")));
                String fullName = sanitizeCell(getCellString(row, columnMap.get("full_name")));
                String gradeName = sanitizeCell(getCellString(row, columnMap.get("grade")));
                String sectionName = sanitizeCell(getCellString(row, columnMap.get("section")));
                String email = sanitizeCell(getCellString(row, columnMap.get("email")));
                String phone = sanitizeCell(getCellString(row, columnMap.get("phone")));

                if (studentNumber == null || studentNumber.isBlank() || fullName == null || fullName.isBlank()) {
                    errors.add(Map.of("row", rowIndex, "error", "Missing required fields (Student Number or Full Name)"));
                    continue;
                }

                if (seenStudentNumbers.contains(studentNumber)) {
                    errors.add(Map.of("row", rowIndex, "studentNumber", studentNumber, "error", "Duplicate Student Number in file"));
                    continue;
                }
                seenStudentNumbers.add(studentNumber);

                boolean existsInDb = studentRepository.existsByStudentNumber(studentNumber);

                Map<String, Object> record = new HashMap<>();
                record.put("row", rowIndex);
                record.put("studentNumber", studentNumber);
                record.put("fullName", fullName);
                record.put("grade", gradeName != null ? gradeName : "Default Grade");
                record.put("section", sectionName != null ? sectionName : "Section A");
                record.put("email", email);
                record.put("phone", phone);
                record.put("isExisting", existsInDb);

                validRecords.add(record);
            }
        } catch (Exception e) {
            log.error("Failed to parse Excel file", e);
            throw ApiException.badRequest("Failed to process Excel file: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalRows", validRecords.size() + errors.size());
        result.put("validCount", validRecords.size());
        result.put("errorCount", errors.size());
        result.put("validRecords", validRecords);
        result.put("errors", errors);

        return result;
    }

    @Transactional
    public Map<String, Object> confirmImport(List<Map<String, String>> records, UUID academicYearId) {
        AcademicYear year = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> ApiException.notFound("Academic year not found"));

        int createdCount = 0;
        int updatedCount = 0;

        for (Map<String, String> item : records) {
            String studentNumber = item.get("studentNumber");
            String fullName = item.get("fullName");
            String email = item.get("email");
            String phone = item.get("phone");
            String sectionIdStr = item.get("sectionId");

            Student student = studentRepository.findByStudentNumber(studentNumber)
                    .orElseGet(() -> new Student(null, studentNumber, fullName, email, phone));

            student.setFullName(fullName);
            student.setContactEmail(email);
            student.setContactPhone(phone);
            student = studentRepository.save(student);

            if (sectionIdStr != null && !sectionIdStr.isBlank()) {
                UUID sectionId = UUID.fromString(sectionIdStr);
                Optional<Section> secOpt = sectionRepository.findById(sectionId);
                if (secOpt.isPresent()) {
                    Section section = secOpt.get();
                    Grade grade = section.getGrade();

                    Optional<StudentEnrollment> enrollment = enrollmentRepository
                            .findByStudentIdAndAcademicYearId(student.getId(), year.getId());

                    if (enrollment.isEmpty()) {
                        StudentEnrollment newEnrollment = new StudentEnrollment(
                                student, year, grade, section, LocalDate.now());
                        enrollmentRepository.save(newEnrollment);
                    }
                }
            }
            createdCount++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("importedCount", createdCount);
        result.put("status", "SUCCESS");
        return result;
    }

    public byte[] generateTemplate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Students");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row header = sheet.createRow(0);
            String[] headers = {"Student Number", "Full Name", "Grade", "Section", "Email", "Phone"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }

            // Sample rows
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("STU-2001");
            row1.createCell(1).setCellValue("Yousef Al-Kahlout");
            row1.createCell(2).setCellValue("Grade 10");
            row1.createCell(3).setCellValue("Section A");
            row1.createCell(4).setCellValue("yousef@student.school.edu");
            row1.createCell(5).setCellValue("+962791234567");

            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate Excel template", e);
            throw new RuntimeException("Could not generate Excel template", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Please select a file to upload");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".csv"))) {
            throw ApiException.badRequest("Only .xlsx and .csv files are supported");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw ApiException.badRequest("File exceeds maximum allowed size (10MB)");
        }
    }

    private Map<String, Integer> parseHeaders(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : headerRow) {
            String value = cell.getStringCellValue().trim().toLowerCase().replace(" ", "_");
            if (value.contains("number") || value.contains("id")) {
                map.put("student_number", cell.getColumnIndex());
            } else if (value.contains("name")) {
                map.put("full_name", cell.getColumnIndex());
            } else if (value.contains("grade")) {
                map.put("grade", cell.getColumnIndex());
            } else if (value.contains("section")) {
                map.put("section", cell.getColumnIndex());
            } else if (value.contains("email")) {
                map.put("email", cell.getColumnIndex());
            } else if (value.contains("phone")) {
                map.put("phone", cell.getColumnIndex());
            }
        }
        return map;
    }

    private String getCellString(Row row, Integer columnIndex) {
        if (columnIndex == null || row == null) return null;
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private String sanitizeCell(String value) {
        if (value == null) return null;
        // Formula injection protection: strip leading =, +, -, @
        if (value.startsWith("=") || value.startsWith("+") || value.startsWith("-") || value.startsWith("@")) {
            return "'" + value;
        }
        return value;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
}
