package com.example.taskmanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Standardized API response wrapper for consistent response format.
 * Provides common response structure across all endpoints.
 */
@Schema(description = "Standardized API response wrapper for all endpoints")
public class ApiResponse<T> {
    
    @Schema(description = "Indicates if the request was successful", example = "true")
    @JsonProperty("success")
    private boolean success;
    
    @Schema(description = "Response data (type varies by endpoint)")
    @JsonProperty("data")
    private T data;
    
    @Schema(description = "Additional message or error description", example = "Task created successfully")
    @JsonProperty("message")
    private String message;
    
    @Schema(description = "Response timestamp", example = "2025-10-19T20:30:00.000")
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;
    
    @Schema(description = "HTTP status code", example = "200")
    @JsonProperty("status")
    private int status;
    
    // Constructors
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ApiResponse(boolean success, T data, String message, int status) {
        this();
        this.success = success;
        this.data = data;
        this.message = message;
        this.status = status;
    }
    
    // Static factory methods
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        response.setStatus(HttpStatus.OK.value());
        return response;
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        response.setMessage(message);
        response.setStatus(HttpStatus.OK.value());
        return response;
    }
    
    public static <T> ApiResponse<T> created(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        response.setStatus(HttpStatus.CREATED.value());
        return response;
    }
    
    public static <T> ApiResponse<T> created(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        response.setMessage(message);
        response.setStatus(HttpStatus.CREATED.value());
        return response;
    }
    
    public static <T> ApiResponse<T> error(String message, int status) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setStatus(status);
        return response;
    }
    
    // Getters and setters
    public boolean isSuccess() { 
        return success; 
    }
    
    public void setSuccess(boolean success) { 
        this.success = success; 
    }
    
    public T getData() { 
        return data; 
    }
    
    public void setData(T data) { 
        this.data = data; 
    }
    
    public String getMessage() { 
        return message; 
    }
    
    public void setMessage(String message) { 
        this.message = message; 
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

