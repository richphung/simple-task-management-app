package com.example.taskmanagement.service;

import com.example.taskmanagement.constants.TaskConstants;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.util.TaskStatisticsCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for task analytics and statistics.
 * Provides insights into task management patterns.
 */
@Service
@Transactional(readOnly = true)
public class TaskAnalyticsService extends BaseService {

    private final TaskRepository taskRepository;
    private final TaskStatisticsCalculator statisticsCalculator;

    @Autowired
    public TaskAnalyticsService(TaskRepository taskRepository, TaskStatisticsCalculator statisticsCalculator) {
        this.taskRepository = taskRepository;
        this.statisticsCalculator = statisticsCalculator;
    }

    /**
     * Gets comprehensive task statistics using optimized repository methods.
     *
     * @return a map containing various task statistics
     */
    @Cacheable(value = TaskConstants.CACHE_TASK_ANALYTICS, key = "'all'")
    public Map<String, Object> getTaskStatistics() {
        logDebug("Generating task statistics");

        // Get all tasks and calculate statistics
        List<Task> allTasks = taskRepository.findAll();
        Map<String, Object> stats = statisticsCalculator.calculateComprehensiveStats(allTasks);
        
            // Convert enum-based maps to enum name maps for API responses
            // Initialize all statuses with 0 count
            Map<String, Long> statusCounts = new HashMap<>();
            for (Status status : Status.values()) {
                statusCounts.put(status.name(), 0L);
            }
            
            // Override with actual counts
            @SuppressWarnings("unchecked")
            Map<Status, Long> statusCountsEnum = (Map<Status, Long>) stats.get("statusCounts");
            for (Map.Entry<Status, Long> entry : statusCountsEnum.entrySet()) {
                statusCounts.put(entry.getKey().name(), entry.getValue());
            }
            
            // Initialize all priorities with 0 count
            Map<String, Long> priorityCounts = new HashMap<>();
            for (Priority priority : Priority.values()) {
                priorityCounts.put(priority.name(), 0L);
            }
            
            // Override with actual counts
            @SuppressWarnings("unchecked")
            Map<Priority, Long> priorityCountsEnum = (Map<Priority, Long>) stats.get("priorityCounts");
            for (Map.Entry<Priority, Long> entry : priorityCountsEnum.entrySet()) {
                priorityCounts.put(entry.getKey().name(), entry.getValue());
            }
            
            // Update stats with enum name maps
            stats.put("statusCounts", statusCounts);
            stats.put("priorityCounts", priorityCounts);

        logInfo("Task statistics generated: Total={}, Completed={}, Overdue={}", 
                stats.get("totalTasks"), stats.get("completedTasks"), stats.get("overdueTasks"));

        return stats;
    }

    /**
     * Gets task statistics for a specific date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return a map containing task statistics for the date range
     */
    @Cacheable(value = "taskStats", key = "#startDate + '-' + #endDate")
    public Map<String, Object> getTaskStatisticsForDateRange(LocalDate startDate, LocalDate endDate) {
        logDebug("Generating task statistics for date range: {} to {}", startDate, endDate);

        Map<String, Object> stats = new HashMap<>();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        // Tasks created in date range
        long tasksCreated = taskRepository.findTasksCreatedBetween(startDateTime, endDateTime, 
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();
        stats.put("tasksCreated", tasksCreated);

        // Get tasks created in date range for statistics
        List<Task> tasksInRange = taskRepository.findTasksCreatedBetween(startDateTime, endDateTime, 
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)).getContent();

        // Use statistics calculator for consistent calculations
        Map<String, Object> rangeStats = statisticsCalculator.calculateComprehensiveStats(tasksInRange);
        
        // Convert enum maps to display name maps for API response
        Map<String, Long> statusCounts = new HashMap<>();
        @SuppressWarnings("unchecked")
        Map<Status, Long> statusCountsEnum = (Map<Status, Long>) rangeStats.get("statusCounts");
        for (Map.Entry<Status, Long> entry : statusCountsEnum.entrySet()) {
            statusCounts.put(entry.getKey().getDisplayName(), entry.getValue());
        }
        stats.put("statusCounts", statusCounts);

        logInfo("Task statistics for date range generated: Created={}", tasksCreated);

        return stats;
    }

    /**
     * Gets productivity metrics.
     *
     * @return a map containing productivity metrics
     */
    @Cacheable(value = "productivityMetrics", key = "'current'")
    public Map<String, Object> getProductivityMetrics() {
        logDebug("Generating productivity metrics");

        // Get all tasks for comprehensive metrics
        List<Task> allTasks = taskRepository.findAll();
        
        // Use the statistics calculator for consistent calculations
        Map<String, Object> metrics = statisticsCalculator.calculateComprehensiveStats(allTasks);

        // Add additional metrics
        long inProgressTasks = taskRepository.countByStatus(Status.IN_PROGRESS);
        metrics.put("inProgressTasks", inProgressTasks);

        logInfo("Productivity metrics generated: Completion Rate={}%, Overdue Rate={}%", 
                metrics.get("completionRate"), metrics.get("overdueRate"));

        return metrics;
    }
}
