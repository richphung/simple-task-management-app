package com.example.taskmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA configuration class for the task management application.
 * 
 * <p>This configuration class enables JPA repositories, transaction
 * management, and JPA auditing for the application. It also configures 
 * the base package for repository scanning.</p>
 * 
 * @author Task Management Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.example.taskmanagement.repository")
@EnableTransactionManagement
@EnableJpaAuditing
public class JpaConfig {
    
    // JPA configuration is handled by Spring Boot auto-configuration
    // This class serves as a placeholder for any custom JPA configuration
    // that might be needed in the future
}
