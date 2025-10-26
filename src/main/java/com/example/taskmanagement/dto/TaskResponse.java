package com.example.taskmanagement.dto;

import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for task response data.
 * Contains all task information including audit fields.
 */
@Schema(description = "Response object containing complete task information")
public class TaskResponse {

    @Schema(description = "Unique task identifier", example = "1")
    private Long id;

    @Schema(description = "Task title", example = "Complete project documentation")
    private String title;

    @Schema(description = "Detailed description of the task", example = "Write comprehensive API documentation with examples")
    private String description;

    @Schema(description = "Task priority level", example = "HIGH")
    private Priority priority;

    @Schema(description = "Current task status", example = "TODO")
    private Status status;

    @Schema(description = "Task due date", example = "2025-10-31")
    private LocalDate dueDate;

    @Schema(description = "Completion timestamp", example = "2025-10-25T14:30:00")
    private LocalDateTime completedAt;

    @Schema(description = "Additional notes or comments", example = "Remember to include screenshots")
    private String notes;

    @Schema(description = "Creation timestamp", example = "2025-10-19T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-10-20T15:45:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Indicates if task is past due date", example = "false")
    private boolean overdue;

    // Constructors
    /**
     * Default constructor for TaskResponse.
     */
    @SuppressWarnings("PMD.UncommentedEmptyConstructor")
    public TaskResponse() {
    }

    public TaskResponse(Long id, String title, String description, Priority priority, 
                       Status status, LocalDate dueDate, LocalDateTime completedAt, 
                       String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.dueDate = dueDate;
        this.completedAt = completedAt;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.overdue = calculateOverdue();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    /**
     * Calculates if the task is overdue.
     * A task is overdue if its due date is in the past and its status is not COMPLETED.
     *
     * @return true if the task is overdue, false otherwise
     */
    private boolean calculateOverdue() {
        return dueDate != null
                && dueDate.isBefore(LocalDate.now())
                && status != Status.COMPLETED;
    }

    @Override
    public String toString() {
        return "TaskResponse{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", dueDate=" + dueDate +
                ", overdue=" + overdue +
                '}';
    }
}
