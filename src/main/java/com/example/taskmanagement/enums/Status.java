package com.example.taskmanagement.enums;

/**
 * Enumeration representing task status states.
 * 
 * <p>This enum defines the different status states that a task can have
 * throughout its lifecycle, from creation to completion or cancellation.</p>
 * 
 * @author Task Management Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public enum Status {
    
    /**
     * Task has been created but not yet started.
     */
    TODO("To Do", "Task is pending"),
    
    /**
     * Task is currently in progress.
     */
    IN_PROGRESS("In Progress", "Task is being worked on"),
    
    /**
     * Task has been completed successfully.
     */
    COMPLETED("Completed", "Task has been finished"),
    
    /**
     * Task has been cancelled or abandoned.
     */
    CANCELLED("Cancelled", "Task has been cancelled"),
    
    /**
     * Task is on hold or paused.
     */
    ON_HOLD("On Hold", "Task is temporarily paused");
    
    private final String displayName;
    private final String description;
    
    /**
     * Constructs a Status enum with the specified display name and description.
     * 
     * @param displayName the human-readable display name
     * @param description the description of this status
     */
    Status(final String displayName, final String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Returns the display name of this status.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Returns the description of this status.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Returns a string representation of this status.
     * 
     * @return the display name
     */
    @Override
    public String toString() {
        return displayName;
    }
    
    /**
     * Returns the enum name for JSON serialization.
     * 
     * @return the enum name
     */
    @com.fasterxml.jackson.annotation.JsonValue
    public String toJsonValue() {
        return name();
    }
}
