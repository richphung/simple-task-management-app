package com.example.taskmanagement.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing audit trail for task changes.
 * Tracks all modifications made to tasks for compliance and history purposes.
 */
@Entity
@Table(name = "task_audit")
public class TaskAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "task_title", nullable = false)
    private String taskTitle;

    @Column(name = "action", nullable = false)
    private String action; // CREATED, UPDATED, COMPLETED, DELETED

    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;

    @Column(name = "changed_by")
    private String changedBy; // Could be user ID in a real system

    @Column(name = "change_timestamp", nullable = false)
    private LocalDateTime changeTimestamp;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    /**
     * Default constructor.
     */
    public TaskAudit() {
        this.changeTimestamp = LocalDateTime.now();
    }

    /**
     * Constructor with required fields.
     *
     * @param taskId the ID of the task being audited
     * @param taskTitle the title of the task
     * @param action the action performed on the task
     */
    public TaskAudit(Long taskId, String taskTitle, String action) {
        this();
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.action = action;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOldValues() {
        return oldValues;
    }

    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }

    public String getNewValues() {
        return newValues;
    }

    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangeTimestamp() {
        return changeTimestamp;
    }

    public void setChangeTimestamp(LocalDateTime changeTimestamp) {
        this.changeTimestamp = changeTimestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    // Additional methods for compatibility with tests
    public LocalDateTime getTimestamp() {
        return changeTimestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.changeTimestamp = timestamp;
    }

    public String getEntityType() {
        return "Task";
    }

    public void setEntityType(String entityType) {
        // This is a computed field, so we don't store it
    }

    public String getOldValue() {
        return oldValues;
    }

    public void setOldValue(String oldValue) {
        this.oldValues = oldValue;
    }

    public String getNewValue() {
        return newValues;
    }

    public void setNewValue(String newValue) {
        this.newValues = newValue;
    }

    @Override
    public String toString() {
        return "TaskAudit{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", taskTitle='" + taskTitle + '\'' +
                ", action='" + action + '\'' +
                ", changeTimestamp=" + changeTimestamp +
                ", changedBy='" + changedBy + '\'' +
                '}';
    }
}
