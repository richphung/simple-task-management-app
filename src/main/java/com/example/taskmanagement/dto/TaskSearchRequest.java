package com.example.taskmanagement.dto;

import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDate;

/**
 * DTO for task search and filtering requests.
 * Contains pagination and filtering parameters.
 */
@Schema(description = "Request object for searching and filtering tasks with pagination")
public class TaskSearchRequest {

    @Schema(description = "Search keyword for title and description", example = "documentation")
    private String searchTerm;

    @Schema(description = "Filter by task status", example = "TODO")
    private Status status;

    @Schema(description = "Filter by task priority", example = "HIGH")
    private Priority priority;

    @Schema(description = "Filter tasks with due date from this date", example = "2025-10-01")
    private LocalDate dueDateFrom;

    @Schema(description = "Filter tasks with due date until this date", example = "2025-10-31")
    private LocalDate dueDateTo;

    @Schema(description = "Filter tasks created from this date", example = "2025-10-01")
    private LocalDate createdFrom;

    @Schema(description = "Filter tasks created until this date", example = "2025-10-31")
    private LocalDate createdTo;

    @Schema(description = "Filter overdue tasks", example = "false")
    private Boolean overdue;

    @Schema(description = "Page number (0-based)", example = "0", minimum = "0")
    @Min(value = 0, message = "Page number must be 0 or greater")
    private int page = 0;

    @Schema(description = "Page size", example = "10", minimum = "1", maximum = "100")
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    private int size = 10;

    @Schema(description = "Sort field", example = "createdAt")
    private String sortBy = "createdAt";

    @Schema(description = "Sort direction", example = "desc", allowableValues = {"asc", "desc"})
    private String sortDirection = "desc";

    // Constructors - removed unnecessary default constructor

    // Getters and Setters
    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDateFrom() {
        return dueDateFrom;
    }

    public void setDueDateFrom(LocalDate dueDateFrom) {
        this.dueDateFrom = dueDateFrom;
    }

    public LocalDate getDueDateTo() {
        return dueDateTo;
    }

    public void setDueDateTo(LocalDate dueDateTo) {
        this.dueDateTo = dueDateTo;
    }

    public LocalDate getCreatedFrom() {
        return createdFrom;
    }

    public void setCreatedFrom(LocalDate createdFrom) {
        this.createdFrom = createdFrom;
    }

    public LocalDate getCreatedTo() {
        return createdTo;
    }

    public void setCreatedTo(LocalDate createdTo) {
        this.createdTo = createdTo;
    }

    public Boolean getOverdue() {
        return overdue;
    }

    public void setOverdue(Boolean overdue) {
        this.overdue = overdue;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    @Override
    public String toString() {
        return "TaskSearchRequest{" +
                "searchTerm='" + searchTerm + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}
