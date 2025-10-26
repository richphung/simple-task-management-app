package com.example.taskmanagement.exception;

/**
 * Exception thrown when a client exceeds the rate limit.
 * 
 * @author Task Management Team
 * @version 1.0
 * @since 2.0
 */
public class RateLimitExceededException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new RateLimitExceededException with a default message.
     */
    public RateLimitExceededException() {
        super("Rate limit exceeded. Please try again later.");
    }

    /**
     * Constructs a new RateLimitExceededException with the specified message.
     *
     * @param message the detail message
     */
    public RateLimitExceededException(String message) {
        super(message);
    }

    /**
     * Constructs a new RateLimitExceededException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}




