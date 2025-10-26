package com.example.taskmanagement.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NoSqlInjectionValidator.
 * 
 * @author Task Management Team
 * @version 1.0
 */
class NoSqlInjectionValidatorTest {

    private NoSqlInjectionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NoSqlInjectionValidator();
        validator.initialize(null);
    }

    @Test
    @DisplayName("Should accept null values")
    void shouldAcceptNullValues() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    @DisplayName("Should accept normal text")
    void shouldAcceptNormalText() {
        assertTrue(validator.isValid("Normal task title", null));
        assertTrue(validator.isValid("Buy groceries", null));
        assertTrue(validator.isValid("Complete project documentation", null));
    }

    @Test
    @DisplayName("Should reject SQL injection with OR statement")
    void shouldRejectOrStatement() {
        assertFalse(validator.isValid("' OR 1=1 --", null));
        assertFalse(validator.isValid("admin' OR '1'='1", null));
    }

    @Test
    @DisplayName("Should reject SQL injection with SELECT")
    void shouldRejectSelect() {
        assertFalse(validator.isValid("'; SELECT * FROM tasks --", null));
        // "test SELECT password" is now allowed - SELECT alone without injection pattern is not malicious
        assertTrue(validator.isValid("test SELECT password", null));
    }

    @Test
    @DisplayName("Should reject SQL injection with INSERT")
    void shouldRejectInsert() {
        assertFalse(validator.isValid("'; INSERT INTO users VALUES", null));
    }

    @Test
    @DisplayName("Should reject SQL injection with UPDATE")
    void shouldRejectUpdate() {
        assertFalse(validator.isValid("'; UPDATE tasks SET", null));
    }

    @Test
    @DisplayName("Should reject SQL injection with DELETE")
    void shouldRejectDelete() {
        assertFalse(validator.isValid("'; DELETE FROM tasks", null));
    }

    @Test
    @DisplayName("Should reject SQL injection with DROP")
    void shouldRejectDrop() {
        assertFalse(validator.isValid("'; DROP TABLE tasks --", null));
    }

    @Test
    @DisplayName("Should reject SQL injection with UNION")
    void shouldRejectUnion() {
        assertFalse(validator.isValid("' UNION SELECT NULL", null));
    }

    @Test
    @DisplayName("Should reject SQL injection with EXEC")
    void shouldRejectExec() {
        assertFalse(validator.isValid("'; EXEC xp_cmdshell", null));
    }

    @Test
    @DisplayName("Should reject SQL injection with semicolon")
    void shouldRejectSemicolon() {
        assertFalse(validator.isValid("test'; DROP TABLE tasks;", null));
    }

    @Test
    @DisplayName("Should reject SQL comments in injection patterns")
    void shouldRejectSqlComments() {
        // SQL comment after quote is suspicious (injection pattern)
        assertFalse(validator.isValid("test'; -- comment", null));
        // SQL block comments anywhere are suspicious
        assertFalse(validator.isValid("test /* comment */", null));
        // Plain dashes are allowed (not an injection pattern)
        assertTrue(validator.isValid("test -- not a SQL comment", null));
    }

    @Test
    @DisplayName("Should accept text with numbers")
    void shouldAcceptTextWithNumbers() {
        assertTrue(validator.isValid("Task 123", null));
        assertTrue(validator.isValid("2023 Project Goals", null));
    }

    @Test
    @DisplayName("Should accept text with special characters (non-SQL)")
    void shouldAcceptSpecialCharacters() {
        assertTrue(validator.isValid("Task: Complete @project #1", null));
        assertTrue(validator.isValid("Buy milk & eggs", null));
    }

    @Test
    @DisplayName("Should reject case variations of SQL keywords in injection patterns")
    void shouldRejectCaseVariations() {
        // SQL keywords alone are not malicious - must be part of injection pattern
        assertFalse(validator.isValid("test'; SELECT", null));
        assertFalse(validator.isValid("test'; SeLeCt", null));
        assertFalse(validator.isValid("test'; drop table", null));
        // Plain SQL keywords without injection pattern are allowed
        assertTrue(validator.isValid("test SELECT", null));
        assertTrue(validator.isValid("test drop table", null));
    }
}



