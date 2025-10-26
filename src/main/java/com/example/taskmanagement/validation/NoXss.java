package com.example.taskmanagement.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation to prevent XSS (Cross-Site Scripting) attacks.
 * Validates that input doesn't contain HTML/JavaScript injection patterns.
 * 
 * @author Task Management Team
 * @version 1.0
 * @since 2.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoXssValidator.class)
@Documented
public @interface NoXss {
    
    String message() default "Input contains potentially malicious HTML/JavaScript";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}




