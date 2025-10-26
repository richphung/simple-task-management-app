package com.example.taskmanagement.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validator implementation for NoXss annotation.
 * Checks input for common XSS (Cross-Site Scripting) patterns.
 * 
 * @author Task Management Team
 * @version 1.0
 * @since 2.0
 */
public class NoXssValidator implements ConstraintValidator<NoXss, String> {
    
    // Common XSS patterns
    private static final Pattern[] XSS_PATTERNS = {
        Pattern.compile(".*<script.*?>.*</script>.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile(".*javascript:.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*onerror\\s*=.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*onload\\s*=.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*onclick\\s*=.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*<iframe.*?>.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*<object.*?>.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*<embed.*?>.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*eval\\s*\\(.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*expression\\s*\\(.*", Pattern.CASE_INSENSITIVE)
    };
    
    @Override
    public void initialize(NoXss constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null values are valid (use @NotNull for null checking)
        if (value == null) {
            return true;
        }
        
        // Check against XSS patterns
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(value).matches()) {
                return false;
            }
        }
        
        return true;
    }
}




