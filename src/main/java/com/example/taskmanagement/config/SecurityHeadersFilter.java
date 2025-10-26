package com.example.taskmanagement.config;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter to add security headers to all HTTP responses.
 * Implements OWASP security best practices.
 * 
 * @author Task Management Team
 * @version 1.0
 * @since 2.0
 */
@Component
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Content Security Policy
        httpResponse.setHeader("Content-Security-Policy", 
            "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:;");

        // X-Frame-Options (Clickjacking protection)
        httpResponse.setHeader("X-Frame-Options", "DENY");

        // X-Content-Type-Options (MIME sniffing protection)
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");

        // X-XSS-Protection
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        // Strict-Transport-Security (HSTS)
        httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Referrer Policy
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Permissions Policy
        httpResponse.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}




