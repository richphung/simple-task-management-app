package com.example.taskmanagement.util;

import com.example.taskmanagement.dto.TaskRequest;
import com.example.taskmanagement.dto.TaskResponse;
import com.example.taskmanagement.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralized utility for converting between Task entities and DTOs.
 * Eliminates duplicate conversion logic across services.
 */
@Component
public class TaskConverter {
    
    /**
     * Converts Task entity to TaskResponse DTO
     */
    public TaskResponse convertToResponse(Task task) {
        if (task == null) {
            return null;
        }
        
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setPriority(task.getPriority());
        response.setStatus(task.getStatus());
        response.setDueDate(task.getDueDate());
        response.setCompletedAt(task.getCompletedAt());
        response.setNotes(task.getNotes());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        response.setOverdue(task.isOverdue());
        
        return response;
    }
    
    /**
     * Converts TaskRequest DTO to Task entity
     */
    public Task convertToEntity(TaskRequest request) {
        if (request == null) {
            return null;
        }
        
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());
        task.setNotes(request.getNotes());
        
        return task;
    }
    
    /**
     * Converts list of Task entities to TaskResponse DTOs
     */
    public List<TaskResponse> convertToResponseList(List<Task> tasks) {
        return tasks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Converts Page of Task entities to Page of TaskResponse DTOs
     */
    public Page<TaskResponse> convertToResponsePage(Page<Task> taskPage) {
        return taskPage.map(this::convertToResponse);
    }
}
