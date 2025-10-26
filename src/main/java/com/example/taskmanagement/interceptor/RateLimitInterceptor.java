package com.example.taskmanagement.interceptor;

import com.example.taskmanagement.config.RateLimitConfig;
import com.example.taskmanagement.exception.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interceptor for API rate limiting.
 * Checks request rate limits before processing.
 * 
 * @author Task Management Team
 * @version 1.0
 * @since 2.0
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    @Autowired
    private RateLimitConfig rateLimitConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        // Skip rate limiting for actuator endpoints
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/actuator")) {
            return true;
        }

        String key = getClientIdentifier(request);
        Bucket bucket = rateLimitConfig.resolveBucket(key);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            
            logger.warn("Rate limit exceeded for client: {}", key);
            throw new RateLimitExceededException("Too many requests. Please try again in " + waitForRefill + " seconds.");
        }
    }

    /**
     * Gets a unique identifier for the client (IP address).
     *
     * @param request the HTTP request
     * @return the client identifier
     */
    private String getClientIdentifier(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}




