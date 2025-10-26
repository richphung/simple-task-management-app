package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.service.TaskAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for task analytics and reporting.
 * Provides endpoints for productivity metrics, statistics, and insights.
 * 
 * @author Task Management Team
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Analytics & Reporting", description = "Task analytics, statistics, and insights. " +
        "Get real-time metrics on task completion rates, status distributions, and productivity trends.")
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController extends BaseController {

    private final TaskAnalyticsService taskAnalyticsService;

    public AnalyticsController(TaskAnalyticsService taskAnalyticsService) {
        this.taskAnalyticsService = taskAnalyticsService;
    }

    /**
     * Gets task counts by status.
     *
     * @return a map of status to count wrapped in ApiResponse
     */
    @Operation(summary = "Get status counts", description = "Retrieves task count distribution by status (TODO, IN_PROGRESS, COMPLETED, CANCELLED).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status counts retrieved successfully")
    })
    @GetMapping("/status-counts")
    public ResponseEntity<ApiResponse<Map<com.example.taskmanagement.enums.Status, Long>>> getTaskCountsByStatus() {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting task counts by status");
        }
        
        Map<String, Object> stats = taskAnalyticsService.getTaskStatistics();
        @SuppressWarnings("unchecked")
        Map<String, Long> statusCounts = (Map<String, Long>) stats.get("statusCounts");
        Map<com.example.taskmanagement.enums.Status, Long> counts = new java.util.HashMap<>();
        if (statusCounts != null) {
            for (com.example.taskmanagement.enums.Status status : com.example.taskmanagement.enums.Status.values()) {
                counts.put(status, statusCounts.getOrDefault(status.name(), 0L));
            }
        }
        return handleSuccess(counts);
    }

    /**
     * Gets task counts by priority.
     *
     * @return a map of priority to count wrapped in ApiResponse
     */
    @Operation(summary = "Get priority counts", description = "Retrieves task count distribution by priority (HIGH, MEDIUM, LOW).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Priority counts retrieved successfully")
    })
    @GetMapping("/priority-counts")
    public ResponseEntity<ApiResponse<Map<com.example.taskmanagement.enums.Priority, Long>>> getTaskCountsByPriority() {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting task counts by priority");
        }
        
        Map<String, Object> stats = taskAnalyticsService.getTaskStatistics();
        @SuppressWarnings("unchecked")
        Map<String, Long> priorityCounts = (Map<String, Long>) stats.get("priorityCounts");
        Map<com.example.taskmanagement.enums.Priority, Long> counts = new java.util.HashMap<>();
        if (priorityCounts != null) {
            for (com.example.taskmanagement.enums.Priority priority : com.example.taskmanagement.enums.Priority.values()) {
                counts.put(priority, priorityCounts.getOrDefault(priority.name(), 0L));
            }
        }
        return handleSuccess(counts);
    }

    /**
     * Gets comprehensive analytics dashboard data.
     *
     * @return analytics data including status and priority counts wrapped in ApiResponse
     */
    @Operation(summary = "Get dashboard analytics", description = "Retrieves comprehensive task statistics including counts, completion rates, and distributions.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Analytics data retrieved successfully")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardAnalytics() {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting dashboard analytics");
        }
        
        Map<String, Object> stats = taskAnalyticsService.getTaskStatistics();
        
        // Return the statistics wrapped in ApiResponse
        return handleSuccess(stats);
    }
}
