package com.example.taskmanagement.util;

import com.example.taskmanagement.constants.TaskConstants;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized utility for date formatting operations.
 * Eliminates duplicate date formatting logic across services.
 */
@Component
public class DateUtil {
    
    public static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern(TaskConstants.DATE_FORMAT);
    public static final DateTimeFormatter DATETIME_FORMATTER = 
        DateTimeFormatter.ofPattern(TaskConstants.DATETIME_FORMAT);
    public static final DateTimeFormatter TIMESTAMP_FORMATTER = 
        DateTimeFormatter.ofPattern(TaskConstants.TIMESTAMP_FORMAT);
    
    /**
     * Formats LocalDate to string
     */
    public String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }
    
    /**
     * Formats LocalDateTime to string
     */
    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : "";
    }
    
    /**
     * Formats LocalDateTime to timestamp string
     */
    public String formatTimestamp(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(TIMESTAMP_FORMATTER) : "";
    }
    
    /**
     * Gets current timestamp string
     */
    public String getCurrentTimestamp() {
        return formatTimestamp(LocalDateTime.now());
    }
}

