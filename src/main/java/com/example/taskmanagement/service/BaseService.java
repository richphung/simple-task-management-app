package com.example.taskmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base service providing common functionality for all service classes.
 * Eliminates duplicate patterns across services.
 */
public abstract class BaseService {
    
    protected static final Logger logger = LoggerFactory.getLogger(BaseService.class);
    
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
    
    /**
     * Logs error information consistently
     */
    protected void logError(String message, Throwable throwable) {
        if (logger.isErrorEnabled()) {
            logger.error(message, throwable);
        }
    }
}

