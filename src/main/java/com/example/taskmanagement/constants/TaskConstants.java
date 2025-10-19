package com.example.taskmanagement.constants;

/**
 * Centralized constants for the task management application.
 * This class eliminates magic numbers and strings throughout the codebase.
 */
public final class TaskConstants {
    
    // Pagination Constants
    public static final String DEFAULT_SORT_FIELD = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    
    // Cache Names
    public static final String CACHE_TASKS = "tasks";
    public static final String CACHE_TASK_BY_ID = "taskById";
    public static final String CACHE_TASK_STATS = "taskStats";
    public static final String CACHE_TASK_ANALYTICS = "taskAnalytics";
    public static final String CACHE_SUGGESTIONS = "suggestions";
    
    // Date Formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";
    
    // Validation Limits
    public static final int MAX_TITLE_LENGTH = 255;
    public static final int MAX_DESCRIPTION_LENGTH = 2000;
    public static final int MAX_NOTES_LENGTH = 1000;
    
    // System Values
    public static final String SYSTEM_USER = "SYSTEM";
    public static final String AUDIT_ACTION_CREATED = "CREATED";
    public static final String AUDIT_ACTION_UPDATED = "UPDATED";
    public static final String AUDIT_ACTION_COMPLETED = "COMPLETED";
    public static final String AUDIT_ACTION_DELETED = "DELETED";
    
    // Application Information
    public static final String APP_NAME = "Task Management API";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_DESCRIPTION = "Advanced Task Management System with Smart Features";
    
    // System Messages
    public static final String TASK_NOT_FOUND = "Task not found with ID: ";
    public static final String INVALID_STATUS_TRANSITION = "Invalid status transition from %s to %s";
    public static final String BULK_OPERATION_SUCCESS = "Bulk operation completed successfully.";
    public static final String EXPORT_ERROR = "Error during data export.";
    
    // Private constructor to prevent instantiation
    private TaskConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
