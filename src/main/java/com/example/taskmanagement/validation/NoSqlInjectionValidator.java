package com.example.taskmanagement.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validator implementation for NoSqlInjection annotation.
 * Checks input for common SQL injection patterns.
 * 
 * @author Task Management Team
 * @version 1.0
 * @since 2.0
 */
public class NoSqlInjectionValidator implements ConstraintValidator<NoSqlInjection, String> {
    
    // SQL injection patterns - focused on actual attack patterns, not just SQL keywords
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
        // SQL injection with quotes and keywords (actual injection patterns)
        Pattern.compile(".*[';]\\s*(or|and)\\s+['\"]?\\d+['\"]?\\s*=\\s*['\"]?\\d+.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*[';]\\s*(drop|delete|insert|update|exec|execute|union|select).*", Pattern.CASE_INSENSITIVE),
        // SQL comment injection patterns (with or without quotes)
        Pattern.compile(".*[';]\\s*--.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*/\\*.*\\*/.*", Pattern.CASE_INSENSITIVE),  // Match /* */ comments anywhere
        // SQL stored procedure calls
        Pattern.compile(".*(xp_|sp_)\\w+.*", Pattern.CASE_INSENSITIVE),
        // Multiple SQL statements (semicolon followed by SQL keyword)
        Pattern.compile(".*;\\s*(select|insert|update|delete|drop|create|alter|exec|execute|union).*", Pattern.CASE_INSENSITIVE)
    };
    
    @Override
    public void initialize(NoSqlInjection constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null values are valid (use @NotNull for null checking)
        if (value == null) {
            return true;
        }
        
        // Check against SQL injection patterns
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(value).matches()) {
                return false;
            }
        }
        
        return true;
    }
}



