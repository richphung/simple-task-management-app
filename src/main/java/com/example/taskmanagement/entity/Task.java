package com.example.taskmanagement.entity;

import com.example.taskmanagement.enums.Priority;
import com.example.taskmanagement.enums.Status;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a task in the task management system.
 * 
 * <p>This entity represents a task with all its properties including title,
 * description, priority, status, due date, and audit information. It extends
 * BaseAuditEntity to inherit automatic timestamp management.</p>
 * 
 * @author Task Management Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_task_title", columnList = "title"),
    @Index(name = "idx_task_status", columnList = "status"),
    @Index(name = "idx_task_priority", columnList = "priority"),
    @Index(name = "idx_task_due_date", columnList = "due_date"),
    @Index(name = "idx_task_created_at", columnList = "created_at")
})
public class Task extends BaseAuditEntity {

    /**
     * Unique identifier for the task.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Title of the task.
     */
    @NotBlank(message = "Task title is required")
    @Size(max = 255, message = "Task title must not exceed 255 characters")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /**
     * Detailed description of the task.
     */
    @Size(max = 2000, message = "Task description must not exceed 2000 characters")
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * Priority level of the task.
     */
    @NotNull(message = "Task priority is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private Priority priority;

    /**
     * Current status of the task.
     */
    @NotNull(message = "Task status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    /**
     * Due date for the task completion.
     */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /**
     * Date and time when the task was completed.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Additional notes or comments for the task.
     */
    @Size(max = 1000, message = "Task notes must not exceed 1000 characters")
    @Column(name = "notes", length = 1000)
    private String notes;

    /**
     * Default constructor.
     */
    public Task() {
        // Default constructor for JPA
    }

    /**
     * Constructs a new Task with the specified title, priority, and status.
     * 
     * @param title the task title
     * @param priority the task priority
     * @param status the task status
     */
    public Task(final String title, final Priority priority, final Status status) {
        this.title = title;
        this.priority = priority;
        this.status = status;
    }

    /**
     * Returns the task ID.
     * 
     * @return the task ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the task ID.
     * 
     * @param id the task ID
     */
    public void setId(final Long id) {
        this.id = id;
    }

    /**
     * Returns the task title.
     * 
     * @return the task title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the task title.
     * 
     * @param title the task title
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Returns the task description.
     * 
     * @return the task description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the task description.
     * 
     * @param description the task description
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Returns the task priority.
     * 
     * @return the task priority
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * Sets the task priority.
     * 
     * @param priority the task priority
     */
    public void setPriority(final Priority priority) {
        this.priority = priority;
    }

    /**
     * Returns the task status.
     * 
     * @return the task status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the task status.
     * 
     * @param status the task status
     */
    public void setStatus(final Status status) {
        this.status = status;
    }

    /**
     * Returns the task due date.
     * 
     * @return the task due date
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Sets the task due date.
     * 
     * @param dueDate the task due date
     */
    public void setDueDate(final LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Returns the task completion date.
     * 
     * @return the task completion date
     */
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    /**
     * Sets the task completion date.
     * 
     * @param completedAt the task completion date
     */
    public void setCompletedAt(final LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    /**
     * Returns the task notes.
     * 
     * @return the task notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the task notes.
     * 
     * @param notes the task notes
     */
    public void setNotes(final String notes) {
        this.notes = notes;
    }


    /**
     * Marks the task as completed and sets the completion timestamp.
     */
    public void markAsCompleted() {
        this.status = Status.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Marks the task as in progress.
     */
    public void markAsInProgress() {
        this.status = Status.IN_PROGRESS;
        this.completedAt = null;
    }

    /**
     * Checks if the task is overdue.
     * 
     * @return true if the task is overdue, false otherwise
     */
    public boolean isOverdue() {
        return dueDate != null 
            && dueDate.isBefore(LocalDate.now()) 
            && status != Status.COMPLETED 
            && status != Status.CANCELLED;
    }

    /**
     * Returns a string representation of the task.
     * 
     * @return string representation of the task
     */
    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", dueDate=" + dueDate +
                '}';
    }
}
