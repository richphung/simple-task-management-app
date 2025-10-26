package com.example.taskmanagement.dto;

import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import com.example.taskmanagement.validation.NoSqlInjection;
import com.example.taskmanagement.validation.NoXss;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO for task creation and update requests.
 * Contains validation annotations for input validation.
 */
@Schema(description = "Request object for creating or updating a task")
public class TaskRequest {

    @Schema(description = "Task title", example = "Complete project documentation", required = true, maxLength = 255)
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @NoXss(message = "Title contains potentially malicious content")
    @NoSqlInjection(message = "Title contains potentially malicious SQL patterns")
    private String title;

    @Schema(description = "Detailed description of the task", example = "Write comprehensive API documentation with examples", maxLength = 2000)
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @NoXss(message = "Description contains potentially malicious content")
    @NoSqlInjection(message = "Description contains potentially malicious SQL patterns")
    private String description;

    @Schema(description = "Task priority level", example = "HIGH", required = true, allowableValues = {"HIGH", "MEDIUM", "LOW"})
    @NotNull(message = "Priority is required")
    private Priority priority;

    @Schema(description = "Current task status", example = "TODO", required = true, allowableValues = {"TODO", "IN_PROGRESS", "COMPLETED", "CANCELLED"})
    @NotNull(message = "Status is required")
    private Status status;

    @Schema(description = "Task due date", example = "2025-10-31", type = "string", format = "date")
    private LocalDate dueDate;

    @Schema(description = "Additional notes or comments", example = "Remember to include screenshots", maxLength = 1000)
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @NoXss(message = "Notes contain potentially malicious content")
    @NoSqlInjection(message = "Notes contain potentially malicious SQL patterns")
    private String notes;

    // Constructors
    /**
     * Default constructor for TaskRequest.
     */
    @SuppressWarnings("PMD.UncommentedEmptyConstructor")
    public TaskRequest() {
    }

    public TaskRequest(String title, Priority priority, Status status) {
        this.title = title;
        this.priority = priority;
        this.status = status;
    }

    // Getters and Setters
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "TaskRequest{" +
                "title='" + title + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", dueDate=" + dueDate +
                '}';
    }
}
