package com.example.taskmanagement.controller;

import com.example.taskmanagement.entity.TaskAudit;
import com.example.taskmanagement.repository.TaskAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 */
@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditController {

    private static final Logger logger = LoggerFactory.getLogger(AuditController.class);

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
    public ResponseEntity<Page<TaskAudit>> getTaskAuditHistory(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("changeTimestamp").descending());
        Page<TaskAudit> auditPage = taskAuditRepository.findByTaskIdOrderByChangeTimestampDesc(taskId, pageable);
        
        return ResponseEntity.ok(auditPage);
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
    public ResponseEntity<Page<TaskAudit>> getAuditByAction(
            @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("changeTimestamp").descending());
        Page<TaskAudit> auditPage = taskAuditRepository.findByActionOrderByChangeTimestampDesc(action, pageable);
        
        return ResponseEntity.ok(auditPage);
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
    public ResponseEntity<Page<TaskAudit>> getAuditByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("changeTimestamp").descending());
        Page<TaskAudit> auditPage = taskAuditRepository.findByChangeTimestampBetweenOrderByChangeTimestampDesc(
                startDate, endDate, pageable);
        
        return ResponseEntity.ok(auditPage);
    }

    /**
     * Get recent audit records for a specific task.
     *
     * @param taskId the ID of the task
     * @param limit maximum number of records to return
     * @return list of recent audit records
     */
    @GetMapping("/task/{taskId}/recent")
    public ResponseEntity<List<TaskAudit>> getRecentTaskAudit(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "5") int limit) {
        
        Pageable pageable = PageRequest.of(0, limit, Sort.by("changeTimestamp").descending());
        List<TaskAudit> recentAudits = taskAuditRepository.findRecentByTaskId(taskId, pageable);
        
        return ResponseEntity.ok(recentAudits);
    }

    /**
     * Get audit statistics for a specific task.
     *
     * @param taskId the ID of the task
     * @return audit statistics
     */
    @GetMapping("/task/{taskId}/statistics")
    public ResponseEntity<Map<String, Object>> getTaskAuditStatistics(@PathVariable Long taskId) {
        Object[] stats = taskAuditRepository.getAuditStatisticsForTask(taskId);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalChanges", stats[0]);
        statistics.put("lastChangeTimestamp", stats[1]);
        
        return ResponseEntity.ok(statistics);
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
    public ResponseEntity<Page<TaskAudit>> getAuditByUser(
            @PathVariable String changedBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("changeTimestamp").descending());
        Page<TaskAudit> auditPage = taskAuditRepository.findByChangedByOrderByChangeTimestampDesc(changedBy, pageable);
        
        return ResponseEntity.ok(auditPage);
    }

    /**
     * Get audit summary statistics.
     *
     * @return audit summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getAuditSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("totalCreated", taskAuditRepository.countByAction("CREATED"));
        summary.put("totalUpdated", taskAuditRepository.countByAction("UPDATED"));
        summary.put("totalCompleted", taskAuditRepository.countByAction("COMPLETED"));
        summary.put("totalDeleted", taskAuditRepository.countByAction("DELETED"));
        
        return ResponseEntity.ok(summary);
    }

    /**
     * Retrieves all audit records for a specific task.
     *
     * @param taskId the ID of the task to retrieve audit records for
     * @return a list of {@link TaskAudit} records
     */
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<List<TaskAudit>> getAuditHistoryForTask(@PathVariable Long taskId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching audit history for task ID: {}", taskId);
        }
        List<TaskAudit> auditRecords = taskAuditRepository.findByTaskIdOrderByChangeTimestampDesc(taskId);
        if (auditRecords.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("No audit records found for task ID: {}", taskId);
            }
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(auditRecords);
    }

    /**
     * Retrieves all audit records in the system.
     *
     * @return a list of all {@link TaskAudit} records
     */
    @GetMapping
    public ResponseEntity<List<TaskAudit>> getAllAuditRecords() {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching all audit records");
        }
        List<TaskAudit> auditRecords = taskAuditRepository.findAll();
        return ResponseEntity.ok(auditRecords);
    }
}
