package com.example.taskmanagement.controller;

import com.example.taskmanagement.service.TaskAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for task analytics and reporting.
 * Provides endpoints for productivity metrics, statistics, and insights.
 */
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

    private final TaskAnalyticsService taskAnalyticsService;

    public AnalyticsController(TaskAnalyticsService taskAnalyticsService) {
        this.taskAnalyticsService = taskAnalyticsService;
    }

    /**
     * Gets task counts by status.
     *
     * @return a map of status to count
     */
    @GetMapping("/status-counts")
    public ResponseEntity<Map<com.example.taskmanagement.enums.Status, Long>> getTaskCountsByStatus() {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting task counts by status");
        }
        
        Map<String, Object> stats = taskAnalyticsService.getTaskStatistics();
        @SuppressWarnings("unchecked")
        Map<String, Long> statusCounts = (Map<String, Long>) stats.get("statusCounts");
        Map<com.example.taskmanagement.enums.Status, Long> counts = new java.util.HashMap<>();
        for (com.example.taskmanagement.enums.Status status : com.example.taskmanagement.enums.Status.values()) {
            counts.put(status, statusCounts.getOrDefault(status.getDisplayName(), 0L));
        }
        return ResponseEntity.ok(counts);
    }

    /**
     * Gets task counts by priority.
     *
     * @return a map of priority to count
     */
    @GetMapping("/priority-counts")
    public ResponseEntity<Map<com.example.taskmanagement.enums.Priority, Long>> getTaskCountsByPriority() {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting task counts by priority");
        }
        
        Map<String, Object> stats = taskAnalyticsService.getTaskStatistics();
        @SuppressWarnings("unchecked")
        Map<String, Long> priorityCounts = (Map<String, Long>) stats.get("priorityCounts");
        Map<com.example.taskmanagement.enums.Priority, Long> counts = new java.util.HashMap<>();
        for (com.example.taskmanagement.enums.Priority priority : com.example.taskmanagement.enums.Priority.values()) {
            counts.put(priority, priorityCounts.getOrDefault(priority.getDisplayName(), 0L));
        }
        return ResponseEntity.ok(counts);
    }

    /**
     * Gets comprehensive analytics dashboard data.
     *
     * @return analytics data including status and priority counts
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardAnalytics() {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting dashboard analytics");
        }
        
        Map<String, Object> stats = taskAnalyticsService.getTaskStatistics();
        @SuppressWarnings("unchecked")
        Map<String, Long> statusCountsMap = (Map<String, Long>) stats.get("statusCounts");
        @SuppressWarnings("unchecked")
        Map<String, Long> priorityCountsMap = (Map<String, Long>) stats.get("priorityCounts");
        
        Map<com.example.taskmanagement.enums.Status, Long> statusCounts = new java.util.HashMap<>();
        for (com.example.taskmanagement.enums.Status status : com.example.taskmanagement.enums.Status.values()) {
            statusCounts.put(status, statusCountsMap.getOrDefault(status.getDisplayName(), 0L));
        }
        
        Map<com.example.taskmanagement.enums.Priority, Long> priorityCounts = new java.util.HashMap<>();
        for (com.example.taskmanagement.enums.Priority priority : com.example.taskmanagement.enums.Priority.values()) {
            priorityCounts.put(priority, priorityCountsMap.getOrDefault(priority.getDisplayName(), 0L));
        }
        
        Map<String, Object> dashboard = new java.util.HashMap<>();
        dashboard.put("statusCounts", statusCounts);
        dashboard.put("priorityCounts", priorityCounts);
        dashboard.put("totalTasks", statusCounts.values().stream().mapToLong(Long::longValue).sum());
        dashboard.put("completedTasks", statusCounts.getOrDefault(com.example.taskmanagement.enums.Status.COMPLETED, 0L));
        dashboard.put("completionRate", calculateCompletionRate(statusCounts));
        
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Calculates the completion rate percentage.
     *
     * @param statusCounts the status counts map
     * @return the completion rate as a percentage
     */
    private double calculateCompletionRate(Map<com.example.taskmanagement.enums.Status, Long> statusCounts) {
        long totalTasks = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        if (totalTasks == 0) {
            return 0.0;
        }
        
        long completedTasks = statusCounts.getOrDefault(com.example.taskmanagement.enums.Status.COMPLETED, 0L);
        return (double) completedTasks / totalTasks * 100.0;
    }
}
