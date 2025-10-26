package com.example.taskmanagement.config;

import com.example.taskmanagement.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Test-specific web security configuration that relaxes CORS restrictions.
 * Only active in test profile.
 * 
 * @author Task Management Team
 * @version 1.0
 */
@Configuration
@Profile("test")
public class TestWebSecurityConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Keep rate limiter but with high test limits (configured in application-test.properties)
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/actuator/**", "/swagger-ui/**", "/api-docs/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow all origins for testing without credentials requirement
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("X-Rate-Limit-Remaining", "X-Rate-Limit-Retry-After-Seconds", "X-Correlation-ID")
                .allowCredentials(false)  // Set to false to allow wildcard origins
                .maxAge(3600);
    }
}

