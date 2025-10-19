package com.example.taskmanagement.util;

import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralized utility for calculating task statistics.
 * Eliminates duplicate statistics calculation logic across services.
 */
@Component
public class TaskStatisticsCalculator {
    
    /**
     * Calculates completion rate percentage
     */
    public double calculateCompletionRate(long totalTasks, long completedTasks) {
        if (totalTasks == 0) {
            return 0.0;
        }
        return Math.round((double) completedTasks / totalTasks * 100.0 * 100.0) / 100.0;
    }
    
    /**
     * Calculates overdue rate percentage
     */
    public double calculateOverdueRate(long totalTasks, long overdueTasks) {
        if (totalTasks == 0) {
            return 0.0;
        }
        return Math.round((double) overdueTasks / totalTasks * 100.0 * 100.0) / 100.0;
    }
    
    /**
     * Calculates status counts from task list
     */
    public Map<Status, Long> calculateStatusCounts(List<Task> tasks) {
        return tasks.stream()
                .collect(Collectors.groupingBy(
                    Task::getStatus,
                    Collectors.counting()
                ));
    }
    
    /**
     * Calculates priority counts from task list
     */
    public Map<Priority, Long> calculatePriorityCounts(List<Task> tasks) {
        return tasks.stream()
                .collect(Collectors.groupingBy(
                    Task::getPriority,
                    Collectors.counting()
                ));
    }
    
    /**
     * Calculates comprehensive task statistics
     */
    public Map<String, Object> calculateComprehensiveStats(List<Task> tasks) {
        Map<String, Object> stats = new HashMap<>();
        
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .mapToLong(task -> task.getStatus() == Status.COMPLETED ? 1 : 0)
                .sum();
        long overdueTasks = tasks.stream()
                .mapToLong(task -> task.isOverdue() ? 1 : 0)
                .sum();
        
        stats.put("totalTasks", totalTasks);
        stats.put("completedTasks", completedTasks);
        stats.put("overdueTasks", overdueTasks);
        stats.put("completionRate", calculateCompletionRate(totalTasks, completedTasks));
        stats.put("overdueRate", calculateOverdueRate(totalTasks, overdueTasks));
        stats.put("statusCounts", calculateStatusCounts(tasks));
        stats.put("priorityCounts", calculatePriorityCounts(tasks));
        
        return stats;
    }
}
