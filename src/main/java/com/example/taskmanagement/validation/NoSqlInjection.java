package com.example.taskmanagement.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation to prevent SQL injection attacks.
 * Validates that input doesn't contain SQL injection patterns.
 * 
 * @author Task Management Team
 * @version 1.0
 * @since 2.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoSqlInjectionValidator.class)
@Documented
public @interface NoSqlInjection {
    
    String message() default "Input contains potentially malicious SQL patterns";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}




