package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * REST controller for data export functionality.
 * Provides endpoints to export tasks and analytics in various formats.
 */
@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "*")
@SuppressWarnings({"PMD.AvoidCatchingGenericException", "REC_CATCH_EXCEPTION"}) // Intentional generic exception handling for REST controller
public class ExportController extends BaseController {

    @Autowired
    private ExportService exportService;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Export tasks to CSV format.
     *
     * @param filters optional filters to apply
     * @return CSV file download
     */
    @GetMapping(value = "/tasks/csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportTasksToCsv(@RequestParam(required = false) String filters) {
        try {
            byte[] csvData = exportService.exportTasksToCsv(filters);
            
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String filename = "tasks_export_" + timestamp + ".csv";
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8")
                .body(csvData);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .build();
        }
    }

    /**
     * Export tasks to JSON format.
     *
     * @param filters optional filters to apply
     * @return JSON file download
     */
    @GetMapping(value = "/tasks/json", produces = "application/json")
    public ResponseEntity<byte[]> exportTasksToJson(@RequestParam(required = false) String filters) {
        try {
            byte[] jsonData = exportService.exportTasksToJson(filters);
            
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String filename = "tasks_export_" + timestamp + ".json";
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .body(jsonData);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .build();
        }
    }

    /**
     * Export analytics to JSON format.
     *
     * @return JSON analytics file download
     */
    @GetMapping(value = "/analytics/json", produces = "application/json")
    public ResponseEntity<byte[]> exportAnalyticsToJson() {
        try {
            byte[] jsonData = exportService.exportAnalyticsToJson();
            
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String filename = "analytics_export_" + timestamp + ".json";
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .body(jsonData);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .build();
        }
    }

    /**
     * Get available export formats.
     *
     * @return list of available export formats wrapped in ApiResponse
     */
    @GetMapping("/formats")
    public ResponseEntity<ApiResponse<Map<String, String[]>>> getAvailableFormats() {
        Map<String, String[]> formats = new java.util.HashMap<>();
        formats.put("taskFormats", new String[]{"csv", "json"});
        formats.put("analyticsFormats", new String[]{"json"});
        formats.put("supportedFilters", new String[]{"status", "priority", "dateRange"});
        return handleSuccess(formats);
    }

    /**
     * Export tasks based on format.
     *
     * @param format the export format (csv or json)
     * @return file download
     */
    @GetMapping("/tasks")
    public ResponseEntity<byte[]> exportTasks(@RequestParam(defaultValue = "json") String format) {
        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String filename = "tasks_export_" + timestamp;
            
            if ("csv".equalsIgnoreCase(format)) {
                byte[] csvData = exportService.exportTasksToCsv(null);
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + ".csv\"")
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8")
                    .body(csvData);
            } else if ("json".equalsIgnoreCase(format)) {
                byte[] jsonData = exportService.exportTasksToJson(null);
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + ".json\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                    .body(jsonData);
            } else {
                String errorMessage = "Unsupported format: " + format + ". Supported formats: csv, json";
                return ResponseEntity.badRequest()
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=utf-8")
                    .body(errorMessage.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .build();
        }
    }

    /**
     * Export analytics data.
     *
     * @return JSON file download
     */
    @GetMapping("/analytics")
    public ResponseEntity<byte[]> exportAnalytics() {
        try {
            byte[] jsonData = exportService.exportAnalyticsToJson();
            
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String filename = "analytics_export_" + timestamp + ".json";
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                .body(jsonData);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .build();
        }
    }
}