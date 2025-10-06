package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.TaskResponse;
import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for exporting task data in various formats.
 * Supports CSV and JSON export formats.
 */
@Service
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Export tasks to CSV format.
     *
     * @return CSV data as byte array
     */
    public byte[] exportTasksToCsv() {
        return exportTasksToCsv(null);
    }

    /**
     * Export tasks to CSV format with filters.
     *
     * @param filters optional filters to apply
     * @return CSV data as byte array
     */
    public byte[] exportTasksToCsv(String filters) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Exporting tasks to CSV with filters: {}", filters);
            }

            List<Task> tasks = getAllTasksForExport(filters);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            
            // Write CSV header
            writer.write(String.format("ID,Title,Description,Status,Priority,Due Date,Created At,Updated At,Notes%n"));
            
            // Write task data
            for (Task task : tasks) {
                writer.write(String.format("%d,\"%s\",\"%s\",%s,%s,%s,%s,%s,\"%s\"%n",
                    task.getId(),
                    escapeCsvValue(task.getTitle()),
                    escapeCsvValue(task.getDescription()),
                    task.getStatus() != null ? task.getStatus().getDisplayName() : "",
                    task.getPriority() != null ? task.getPriority().getDisplayName() : "",
                    task.getDueDate() != null ? task.getDueDate().format(DATE_FORMATTER) : "",
                    task.getCreatedAt() != null ? task.getCreatedAt().format(DATETIME_FORMATTER) : "",
                    task.getUpdatedAt() != null ? task.getUpdatedAt().format(DATETIME_FORMATTER) : "",
                    escapeCsvValue(task.getNotes())
                ));
            }
            
            writer.flush();
            writer.close();
            
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            logger.error("Failed to export tasks to CSV", e);
            throw new RuntimeException("Failed to export tasks to CSV", e);
        }
    }

    /**
     * Export tasks to JSON format.
     *
     * @return JSON data as byte array
     */
    public byte[] exportTasksToJson() {
        return exportTasksToJson(null);
    }

    /**
     * Export tasks to JSON format with filters.
     *
     * @param filters optional filters to apply
     * @return JSON data as byte array
     */
    public byte[] exportTasksToJson(String filters) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Exporting tasks to JSON with filters: {}", filters);
            }

            List<Task> tasks = getAllTasksForExport(filters);
            List<TaskResponse> taskResponses = tasks.stream()
                .map(this::convertToTaskResponse)
                .collect(Collectors.toList());
            
            return objectMapper.writeValueAsBytes(taskResponses);
            
        } catch (Exception e) {
            logger.error("Failed to export tasks to JSON", e);
            throw new RuntimeException("Failed to export tasks to JSON", e);
        }
    }

    /**
     * Export task analytics to JSON format.
     *
     * @return analytics data as byte array
     */
    public byte[] exportAnalyticsToJson() {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Exporting analytics to JSON");
            }

            // Get basic statistics
            long totalTasks = taskRepository.count();
            long completedTasks = taskRepository.countByStatus(com.example.taskmanagement.enums.Status.COMPLETED);
            long inProgressTasks = taskRepository.countByStatus(com.example.taskmanagement.enums.Status.IN_PROGRESS);
            long todoTasks = taskRepository.countByStatus(com.example.taskmanagement.enums.Status.TODO);
            
            // Create analytics object
            AnalyticsData analytics = new AnalyticsData();
            analytics.totalTasks = totalTasks;
            analytics.completedTasks = completedTasks;
            analytics.inProgressTasks = inProgressTasks;
            analytics.todoTasks = todoTasks;
            analytics.completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0.0;
            analytics.exportTimestamp = java.time.LocalDateTime.now().format(DATETIME_FORMATTER);
            
            return objectMapper.writeValueAsBytes(analytics);
            
        } catch (Exception e) {
            logger.error("Failed to export analytics to JSON", e);
            throw new RuntimeException("Failed to export analytics to JSON", e);
        }
    }

    /**
     * Get all tasks for export, applying any filters.
     *
     * @param filters optional filters to apply
     * @return list of tasks
     */
    private List<Task> getAllTasksForExport(@SuppressWarnings("unused") String filters) {
        // For now, return all tasks. In a real implementation, 
        // you would parse the filters and apply them to the query
        return taskRepository.findAll();
    }

    /**
     * Convert Task entity to TaskResponse DTO.
     *
     * @param task the task entity
     * @return task response DTO
     */
    private TaskResponse convertToTaskResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDueDate(task.getDueDate());
        response.setNotes(task.getNotes());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        response.setOverdue(task.isOverdue());
        return response;
    }

    /**
     * Escape CSV values to handle commas, quotes, and newlines.
     *
     * @param value the value to escape
     * @return escaped value
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // Replace quotes with double quotes and wrap in quotes if contains comma, quote, or newline
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        
        return escaped;
    }

    /**
     * Inner class for analytics data structure.
     */
    private static class AnalyticsData {
        public long totalTasks;
        public long completedTasks;
        public long inProgressTasks;
        public long todoTasks;
        public double completionRate;
        public String exportTimestamp;
        
        // Getters for SpotBugs
        public long getTotalTasks() { return totalTasks; }
        public long getCompletedTasks() { return completedTasks; }
        public long getInProgressTasks() { return inProgressTasks; }
        public long getTodoTasks() { return todoTasks; }
        public double getCompletionRate() { return completionRate; }
        public String getExportTimestamp() { return exportTimestamp; }
    }
}
