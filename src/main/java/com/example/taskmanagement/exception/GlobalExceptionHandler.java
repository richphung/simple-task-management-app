package com.example.taskmanagement.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Handles various exceptions and returns appropriate HTTP responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation errors from @Valid annotations.
     *
     * @param ex the validation exception
     * @param request the web request
     * @return a response entity with validation error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "Validation failed",
            errors,
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value()
        );

        if (logger.isWarnEnabled()) {
            logger.warn("Validation error: {}", errors);
        }

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles task not found exceptions.
     *
     * @param ex the task not found exception
     * @param request the web request
     * @return a 404 response with error details
     */
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFound(
            TaskNotFoundException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            "TASK_NOT_FOUND",
            ex.getMessage(),
            null,
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value()
        );

        if (logger.isWarnEnabled()) {
            logger.warn("Task not found: {}", ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles validation exceptions.
     *
     * @param ex the validation exception
     * @param request the web request
     * @return a 400 response with error details
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            ex.getMessage(),
            null,
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value()
        );

        if (logger.isWarnEnabled()) {
            logger.warn("Validation error: {}", ex.getMessage());
        }

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles illegal argument exceptions.
     *
     * @param ex the illegal argument exception
     * @param request the web request
     * @return a 400 response with error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_ARGUMENT",
            ex.getMessage(),
            null,
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value()
        );

        if (logger.isWarnEnabled()) {
            logger.warn("Illegal argument: {}", ex.getMessage());
        }

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles Jackson deserialization errors.
     *
     * @param ex the invalid format exception
     * @param request the web request
     * @return a 400 response with error details
     */
    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFormatException(
            InvalidFormatException ex, WebRequest request) {
        
        String fieldName = ex.getPath().isEmpty() ? "unknown" : ex.getPath().get(0).getFieldName();
        String errorMessage = "Invalid value for field '" + fieldName + "': " + ex.getValue() + 
                            ". Expected one of: " + String.join(", ", Arrays.toString(ex.getTargetType().getEnumConstants()));
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_FORMAT",
            errorMessage,
            null,
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value()
        );
        
        if (logger.isWarnEnabled()) {
            logger.warn("Invalid format error: {}", errorMessage);
        }
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles HTTP message not readable errors (including Jackson deserialization errors).
     *
     * @param ex the HTTP message not readable exception
     * @param request the web request
     * @return a 400 response with error details
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Handling HttpMessageNotReadableException: {}", ex.getMessage());
        }
        
        String errorMessage = "Invalid request body format";
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException invalidFormatEx = (InvalidFormatException) cause;
            String fieldName = invalidFormatEx.getPath().isEmpty() ? "unknown" : invalidFormatEx.getPath().get(0).getFieldName();
            errorMessage = "Invalid value for field '" + fieldName + "': " + invalidFormatEx.getValue() + 
                          ". Expected one of: " + String.join(", ", Arrays.toString(invalidFormatEx.getTargetType().getEnumConstants()));
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_REQUEST",
            errorMessage,
            null,
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value()
        );
        
        if (logger.isWarnEnabled()) {
            logger.warn("HTTP message not readable error: {}", errorMessage);
        }
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all other exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return a 500 response with error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred",
            null,
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        if (logger.isErrorEnabled()) {
            logger.error("Unexpected error occurred", ex);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Error response class for consistent error formatting.
     */
    public static class ErrorResponse {
        @JsonProperty("error")
        private String error;

        @JsonProperty("message")
        private String message;

        @JsonProperty("details")
        private Map<String, String> details;

        @JsonProperty("status")
        private int status;

        @JsonProperty("timestamp")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        private LocalDateTime timestamp;

        public ErrorResponse(String error, String message, Map<String, String> details, LocalDateTime timestamp) {
            this.error = error;
            this.message = message;
            this.details = details != null ? new HashMap<>(details) : null;
            this.timestamp = timestamp;
            this.status = 0; // Will be set by the exception handler
        }
        
        public ErrorResponse(String error, String message, Map<String, String> details, LocalDateTime timestamp, int status) {
            this.error = error;
            this.message = message;
            this.details = details != null ? new HashMap<>(details) : null;
            this.timestamp = timestamp;
            this.status = status;
        }

        // Getters and setters
        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, String> getDetails() {
            return details != null ? new HashMap<>(details) : null;
        }

        public void setDetails(Map<String, String> details) {
            this.details = details != null ? new HashMap<>(details) : null;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
