package com.example.taskmanagement.dto;

import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for task response data.
 * Contains all task information including audit fields.
 */
public class TaskResponse {

    private Long id;

    private String title;

    private String description;

    private Priority priority;

    private Status status;

    private LocalDate dueDate;

    private LocalDateTime completedAt;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

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
