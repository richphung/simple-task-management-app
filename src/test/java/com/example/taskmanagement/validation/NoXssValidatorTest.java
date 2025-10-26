package com.example.taskmanagement.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NoXssValidator.
 * 
 * @author Task Management Team
 * @version 1.0
 */
class NoXssValidatorTest {

    private NoXssValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NoXssValidator();
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
    @DisplayName("Should reject script tags")
    void shouldRejectScriptTags() {
        assertFalse(validator.isValid("<script>alert('XSS')</script>", null));
        assertFalse(validator.isValid("<SCRIPT>alert('XSS')</SCRIPT>", null));
        assertFalse(validator.isValid("<script src='malicious.js'></script>", null));
    }

    @Test
    @DisplayName("Should reject javascript: protocol")
    void shouldRejectJavascriptProtocol() {
        assertFalse(validator.isValid("javascript:alert('XSS')", null));
        assertFalse(validator.isValid("JAVASCRIPT:alert(1)", null));
    }

    @Test
    @DisplayName("Should reject onerror attribute")
    void shouldRejectOnError() {
        assertFalse(validator.isValid("<img src=x onerror=alert('XSS')>", null));
        assertFalse(validator.isValid("onerror=alert(1)", null));
    }

    @Test
    @DisplayName("Should reject onload attribute")
    void shouldRejectOnLoad() {
        assertFalse(validator.isValid("<body onload=alert('XSS')>", null));
        assertFalse(validator.isValid("onload=malicious()", null));
    }

    @Test
    @DisplayName("Should reject onclick attribute")
    void shouldRejectOnClick() {
        assertFalse(validator.isValid("<div onclick=alert('XSS')>", null));
        assertFalse(validator.isValid("onclick=doSomething()", null));
    }

    @Test
    @DisplayName("Should reject iframe tags")
    void shouldRejectIframe() {
        assertFalse(validator.isValid("<iframe src='malicious.html'></iframe>", null));
        assertFalse(validator.isValid("<IFRAME src=x>", null));
    }

    @Test
    @DisplayName("Should reject object tags")
    void shouldRejectObject() {
        assertFalse(validator.isValid("<object data='malicious.swf'>", null));
    }

    @Test
    @DisplayName("Should reject embed tags")
    void shouldRejectEmbed() {
        assertFalse(validator.isValid("<embed src='malicious.swf'>", null));
    }

    @Test
    @DisplayName("Should reject eval calls")
    void shouldRejectEval() {
        assertFalse(validator.isValid("eval(maliciousCode)", null));
        assertFalse(validator.isValid("EVAL(alert(1))", null));
    }

    @Test
    @DisplayName("Should reject expression calls")
    void shouldRejectExpression() {
        assertFalse(validator.isValid("expression(alert('XSS'))", null));
    }

    @Test
    @DisplayName("Should accept HTML entities (encoded)")
    void shouldAcceptHtmlEntities() {
        assertTrue(validator.isValid("&lt;script&gt;", null));
        assertTrue(validator.isValid("&amp; &quot;", null));
    }

    @Test
    @DisplayName("Should accept normal HTML-like text without dangerous content")
    void shouldAcceptSafeHtmlText() {
        assertTrue(validator.isValid("Price < 100", null));
        assertTrue(validator.isValid("x > y", null));
    }

    @Test
    @DisplayName("Should accept text with special characters")
    void shouldAcceptSpecialCharacters() {
        assertTrue(validator.isValid("Task: Complete @project #1", null));
        assertTrue(validator.isValid("Buy milk & eggs", null));
        assertTrue(validator.isValid("Meeting at 3:00 PM", null));
    }

    @Test
    @DisplayName("Should reject case variations of XSS patterns")
    void shouldRejectCaseVariations() {
        assertFalse(validator.isValid("<ScRiPt>alert(1)</ScRiPt>", null));
        assertFalse(validator.isValid("JaVaScRiPt:alert(1)", null));
        assertFalse(validator.isValid("OnErRoR=alert(1)", null));
    }
}




