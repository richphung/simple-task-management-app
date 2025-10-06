package com.example.taskmanagement.enums;

/**
 * Enumeration representing task priority levels.
 * 
 * <p>This enum defines the different priority levels that can be assigned
 * to tasks, ranging from LOW to URGENT. Each priority has an associated
 * numeric value for sorting and comparison purposes.</p>
 * 
 * @author Task Management Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public enum Priority {
    
    /**
     * Low priority task - can be completed when time permits.
     */
    LOW(1, "Low"),
    
    /**
     * Medium priority task - normal priority level.
     */
    MEDIUM(2, "Medium"),
    
    /**
     * High priority task - should be completed soon.
     */
    HIGH(3, "High"),
    
    /**
     * Urgent priority task - requires immediate attention.
     */
    URGENT(4, "Urgent");
    
    private final int value;
    private final String displayName;
    
    /**
     * Constructs a Priority enum with the specified value and display name.
     * 
     * @param value the numeric value for sorting
     * @param displayName the human-readable display name
     */
    Priority(final int value, final String displayName) {
        this.value = value;
        this.displayName = displayName;
    }
    
    /**
     * Returns the numeric value of this priority.
     * 
     * @return the priority value
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Returns the display name of this priority.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Returns a string representation of this priority.
     * 
     * @return the display name
     */
    @Override
    public String toString() {
        return displayName;
    }
}
