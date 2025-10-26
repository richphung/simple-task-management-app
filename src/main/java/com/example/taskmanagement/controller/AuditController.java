package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;

import com.example.taskmanagement.entity.TaskAudit;
import com.example.taskmanagement.repository.TaskAuditRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for audit trail functionality.
 * Provides endpoints to view task change history and audit information.
 * 
 * @author Task Management Team
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Audit Trail", description = "Task change history and audit logs. " +
        "Track all modifications, view audit statistics, and filter audit records by date, action, or task.")
@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditController extends BaseController {

    @Autowired
    private TaskAuditRepository taskAuditRepository;

    /**
     * Get audit history for a specific task.
     *
     * @param taskId the ID of the task
     * @param page page number (0-based)
     * @param size page size
     * @return page of audit records for the task
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<ApiResponse<Page<TaskAudit>>> getTaskAuditHistory(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logDebug("Fetching audit history for task ID: {}, page: {}, size: {}", taskId, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("changeTimestamp").descending());
        Page<TaskAudit> auditPage = taskAuditRepository.findByTaskIdOrderByChangeTimestampDesc(taskId, pageable);
        
        return handleSuccess(auditPage);
    }

    /**
     * Get audit records by action type.
     *
     * @param action the action type (CREATED, UPDATED, COMPLETED, DELETED)
     * @param page page number (0-based)
     * @param size page size
     * @return page of audit records for the action
     */
    @GetMapping("/action/{action}")
    public ResponseEntity<ApiResponse<Page<TaskAudit>>> getAuditByAction(
            @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logDebug("Fetching audit records by action: {}, page: {}, size: {}", action, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("changeTimestamp").descending());
        Page<TaskAudit> auditPage = taskAuditRepository.findByActionOrderByChangeTimestampDesc(action, pageable);
        
        return handleSuccess(auditPage);
    }

    /**
     * Get audit records within a date range.
     *
     * @param startDate start date (ISO format)
     * @param endDate end date (ISO format)
     * @param page page number (0-based)
     * @param size page size
     * @return page of audit records within the date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<Page<TaskAudit>>> getAuditByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logDebug("Fetching audit records by date range: {} to {}", startDate, endDate);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("changeTimestamp").descending());
        Page<TaskAudit> auditPage = taskAuditRepository.findByChangeTimestampBetweenOrderByChangeTimestampDesc(
                startDate, endDate, pageable);
        
        return handleSuccess(auditPage);
    }

    /**
     * Get recent audit records for a specific task.
     *
     * @param taskId the ID of the task
     * @param limit maximum number of records to return
     * @return list of recent audit records
     */
    @GetMapping("/task/{taskId}/recent")
    public ResponseEntity<ApiResponse<List<TaskAudit>>> getRecentTaskAudit(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "5") int limit) {
        
        logDebug("Fetching recent audit records for task ID: {}, limit: {}", taskId, limit);
        
        Pageable pageable = PageRequest.of(0, limit, Sort.by("changeTimestamp").descending());
        List<TaskAudit> recentAudits = taskAuditRepository.findRecentByTaskId(taskId, pageable);
        
        return handleSuccess(recentAudits);
    }

    /**
     * Get audit statistics for a specific task.
     *
     * @param taskId the ID of the task
     * @return audit statistics
     */
    @GetMapping("/task/{taskId}/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTaskAuditStatistics(@PathVariable Long taskId) {
        logDebug("Fetching audit statistics for task ID: {}", taskId);
        
        Object[] stats = taskAuditRepository.getAuditStatisticsForTask(taskId);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalChanges", stats[0]);
        statistics.put("lastChangeTimestamp", stats[1]);
        
        return handleSuccess(statistics);
    }

    /**
     * Get audit records by user.
     *
     * @param changedBy the user who made the changes
     * @param page page number (0-based)
     * @param size page size
     * @return page of audit records for the user
     */
    @GetMapping("/user/{changedBy}")
    public ResponseEntity<ApiResponse<Page<TaskAudit>>> getAuditByUser(
            @PathVariable String changedBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logDebug("Fetching audit records by user: {}, page: {}, size: {}", changedBy, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("changeTimestamp").descending());
        Page<TaskAudit> auditPage = taskAuditRepository.findByChangedByOrderByChangeTimestampDesc(changedBy, pageable);
        
        return handleSuccess(auditPage);
    }

    /**
     * Get audit summary statistics.
     *
     * @return audit summary
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuditSummary() {
        logDebug("Fetching audit summary statistics");
        
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("totalCreated", taskAuditRepository.countByAction("CREATED"));
        summary.put("totalUpdated", taskAuditRepository.countByAction("UPDATED"));
        summary.put("totalCompleted", taskAuditRepository.countByAction("COMPLETED"));
        summary.put("totalDeleted", taskAuditRepository.countByAction("DELETED"));
        
        return handleSuccess(summary);
    }

    /**
     * Retrieves all audit records for a specific task.
     *
     * @param taskId the ID of the task to retrieve audit records for
     * @return a list of {@link TaskAudit} records
     */
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<List<TaskAudit>>> getAuditHistoryForTask(@PathVariable Long taskId) {
        logDebug("Fetching audit history for task ID: {}", taskId);
        
        List<TaskAudit> auditRecords = taskAuditRepository.findByTaskIdOrderByChangeTimestampDesc(taskId);
        
        if (auditRecords.isEmpty()) {
            logInfo("No audit records found for task ID: {}", taskId);
        }
        
        return handleSuccess(auditRecords);
    }

    /**
     * Retrieves all audit records in the system.
     *
     * @return a list of all {@link TaskAudit} records
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskAudit>>> getAllAuditRecords() {
        logDebug("Fetching all audit records");
        
        List<TaskAudit> auditRecords = taskAuditRepository.findAll();
        
        return handleSuccess(auditRecords);
    }
}
