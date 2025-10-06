package com.example.taskmanagement.service;

import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import com.example.taskmanagement.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for task analytics and statistics.
 * Provides insights into task management patterns.
 */
@Service
@Transactional(readOnly = true)
public class TaskAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(TaskAnalyticsService.class);

    private final TaskRepository taskRepository;

    @Autowired
    public TaskAnalyticsService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Gets comprehensive task statistics.
     *
     * @return a map containing various task statistics
     */
    @Cacheable(value = "taskStats", key = "'all'")
    public Map<String, Object> getTaskStatistics() {
        logger.debug("Generating task statistics");

        Map<String, Object> stats = new HashMap<>();

        // Status counts
        Map<String, Long> statusCounts = new HashMap<>();
        for (Status status : Status.values()) {
            statusCounts.put(status.getDisplayName(), taskRepository.countByStatus(status));
        }
        stats.put("statusCounts", statusCounts);

        // Priority counts
        Map<String, Long> priorityCounts = new HashMap<>();
        for (Priority priority : Priority.values()) {
            priorityCounts.put(priority.getDisplayName(), taskRepository.countByPriority(priority));
        }
        stats.put("priorityCounts", priorityCounts);

        // Overdue tasks count
        long overdueCount = taskRepository.findOverdueTasks(LocalDate.now()).size();
        stats.put("overdueCount", overdueCount);

        // Total tasks
        long totalTasks = taskRepository.count();
        stats.put("totalTasks", totalTasks);

        // Completion rate
        long completedTasks = taskRepository.countByStatus(Status.COMPLETED);
        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
        stats.put("completionRate", Math.round(completionRate * 100.0) / 100.0);

        logger.info("Task statistics generated: Total={}, Completed={}, Overdue={}", 
                totalTasks, completedTasks, overdueCount);

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
        logger.debug("Generating task statistics for date range: {} to {}", startDate, endDate);

        Map<String, Object> stats = new HashMap<>();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        // Tasks created in date range
        long tasksCreated = taskRepository.findTasksCreatedBetween(startDateTime, endDateTime, 
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();
        stats.put("tasksCreated", tasksCreated);

        // Status counts for created tasks
        Map<String, Long> statusCounts = new HashMap<>();
        for (Status status : Status.values()) {
            long count = taskRepository.findTasksCreatedBetween(startDateTime, endDateTime, 
                    org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent().stream()
                    .mapToLong(task -> task.getStatus() == status ? 1 : 0)
                    .sum();
            statusCounts.put(status.getDisplayName(), count);
        }
        stats.put("statusCounts", statusCounts);

        logger.info("Task statistics for date range generated: Created={}", tasksCreated);

        return stats;
    }

    /**
     * Gets productivity metrics.
     *
     * @return a map containing productivity metrics
     */
    @Cacheable(value = "productivityMetrics", key = "'current'")
    public Map<String, Object> getProductivityMetrics() {
        logger.debug("Generating productivity metrics");

        Map<String, Object> metrics = new HashMap<>();

        // Total tasks
        long totalTasks = taskRepository.count();
        metrics.put("totalTasks", totalTasks);

        // Completed tasks
        long completedTasks = taskRepository.countByStatus(Status.COMPLETED);
        metrics.put("completedTasks", completedTasks);

        // In progress tasks
        long inProgressTasks = taskRepository.countByStatus(Status.IN_PROGRESS);
        metrics.put("inProgressTasks", inProgressTasks);

        // Overdue tasks
        long overdueTasks = taskRepository.findOverdueTasks(LocalDate.now()).size();
        metrics.put("overdueTasks", overdueTasks);

        // Completion rate
        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
        metrics.put("completionRate", Math.round(completionRate * 100.0) / 100.0);

        // Overdue rate
        double overdueRate = totalTasks > 0 ? (double) overdueTasks / totalTasks * 100 : 0;
        metrics.put("overdueRate", Math.round(overdueRate * 100.0) / 100.0);

        logger.info("Productivity metrics generated: Completion Rate={}%, Overdue Rate={}%", 
                completionRate, overdueRate);

        return metrics;
    }
}
