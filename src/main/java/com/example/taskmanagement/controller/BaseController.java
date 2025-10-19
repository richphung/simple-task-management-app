package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.ApiResponse;
import com.example.taskmanagement.dto.TaskSearchRequest;
import com.example.taskmanagement.enums.Status;
import com.example.taskmanagement.constants.TaskConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Base controller providing common functionality for all REST controllers.
 * Eliminates duplicate patterns across controllers.
 */
public abstract class BaseController {
    
    protected static final Logger logger = LoggerFactory.getLogger(BaseController.class);
    
    /**
     * Handles successful response with data wrapped in ApiResponse
     */
    protected <T> ResponseEntity<ApiResponse<T>> handleSuccess(T data) {
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    
    /**
     * Handles successful response with data and custom message
     */
    protected <T> ResponseEntity<ApiResponse<T>> handleSuccess(T data, String message) {
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }
    
    /**
     * Handles successful response with created data wrapped in ApiResponse
     */
    protected <T> ResponseEntity<ApiResponse<T>> handleCreated(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(data));
    }
    
    /**
     * Handles successful response with created data and custom message
     */
    protected <T> ResponseEntity<ApiResponse<T>> handleCreated(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(data, message));
    }
    
    /**
     * Handles successful response with no content
     */
    protected ResponseEntity<ApiResponse<Void>> handleNoContent() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
    
    /**
     * Builds TaskSearchRequest from common parameters
     */
    protected TaskSearchRequest buildSearchRequest(String searchTerm, Pageable pageable) {
        TaskSearchRequest searchRequest = new TaskSearchRequest();
        searchRequest.setSearchTerm(searchTerm);
        searchRequest.setPage(pageable.getPageNumber());
        searchRequest.setSize(pageable.getPageSize());
        
        if (pageable.getSort().isSorted()) {
            Sort.Order order = pageable.getSort().iterator().next();
            searchRequest.setSortBy(order.getProperty());
            searchRequest.setSortDirection(order.getDirection().name());
        } else {
            searchRequest.setSortBy(TaskConstants.DEFAULT_SORT_FIELD);
            searchRequest.setSortDirection(TaskConstants.DEFAULT_SORT_DIRECTION);
        }
        
        return searchRequest;
    }
    
    /**
     * Builds TaskSearchRequest with status filter
     */
    protected TaskSearchRequest buildSearchRequestWithStatus(Status status, Pageable pageable) {
        TaskSearchRequest searchRequest = buildSearchRequest(null, pageable);
        searchRequest.setStatus(status);
        return searchRequest;
    }
    
    /**
     * Logs debug information consistently
     */
    protected void logDebug(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(message, args);
        }
    }
    
    /**
     * Logs info information consistently
     */
    protected void logInfo(String message, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(message, args);
        }
    }
}
