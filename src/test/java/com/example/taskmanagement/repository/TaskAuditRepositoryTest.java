package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.TaskAudit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link TaskAuditRepository}.
 */
@DataJpaTest
@ActiveProfiles("test")
class TaskAuditRepositoryTest {

    @Autowired
    private TaskAuditRepository taskAuditRepository;

    private TaskAudit testAudit1;
    private TaskAudit testAudit2;
    private TaskAudit testAudit3;

    @BeforeEach
    void setUp() {
        taskAuditRepository.deleteAll();

        testAudit1 = new TaskAudit();
        testAudit1.setTaskId(1L);
        testAudit1.setAction("CREATED");
        testAudit1.setTaskTitle("Test Task 1");
        testAudit1.setTimestamp(LocalDateTime.now().minusHours(2));
        testAudit1.setChangedBy("SYSTEM");
        testAudit1.setEntityType("Task");
        testAudit1.setNewValue("{\"id\":1,\"title\":\"Test Task 1\"}");

        testAudit2 = new TaskAudit();
        testAudit2.setTaskId(1L);
        testAudit2.setAction("UPDATED");
        testAudit2.setTaskTitle("Test Task 1");
        testAudit2.setTimestamp(LocalDateTime.now().minusHours(1));
        testAudit2.setChangedBy("USER");
        testAudit2.setEntityType("Task");
        testAudit2.setOldValue("{\"id\":1,\"title\":\"Test Task 1\"}");
        testAudit2.setNewValue("{\"id\":1,\"title\":\"Updated Task 1\"}");

        testAudit3 = new TaskAudit();
        testAudit3.setTaskId(2L);
        testAudit3.setAction("CREATED");
        testAudit3.setTaskTitle("Test Task 2");
        testAudit3.setTimestamp(LocalDateTime.now());
        testAudit3.setChangedBy("SYSTEM");
        testAudit3.setEntityType("Task");
        testAudit3.setNewValue("{\"id\":2,\"title\":\"Test Task 2\"}");

        taskAuditRepository.save(testAudit1);
        taskAuditRepository.save(testAudit2);
        taskAuditRepository.save(testAudit3);
    }

    @Test
    void testFindByTaskIdOrderByTimestampDesc() {
        // When
        List<TaskAudit> auditRecords = taskAuditRepository.findByTaskIdOrderByChangeTimestampDesc(1L);

        // Then
        assertNotNull(auditRecords);
        assertEquals(2, auditRecords.size());
        
        // Should be ordered by timestamp descending (newest first)
        assertTrue(auditRecords.get(0).getTimestamp().isAfter(auditRecords.get(1).getTimestamp()));
        assertEquals("UPDATED", auditRecords.get(0).getAction());
        assertEquals("CREATED", auditRecords.get(1).getAction());
    }

    @Test
    void testFindByTaskIdOrderByTimestampDescWithNonExistentTask() {
        // When
        List<TaskAudit> auditRecords = taskAuditRepository.findByTaskIdOrderByChangeTimestampDesc(999L);

        // Then
        assertNotNull(auditRecords);
        assertTrue(auditRecords.isEmpty());
    }

    @Test
    void testFindByTaskIdOrderByTimestampDescWithNullTaskId() {
        // When
        List<TaskAudit> auditRecords = taskAuditRepository.findByTaskIdOrderByChangeTimestampDesc(null);

        // Then
        assertNotNull(auditRecords);
        assertTrue(auditRecords.isEmpty());
    }

    @Test
    void testFindByTaskIdOrderByTimestampDescWithSingleRecord() {
        // When
        List<TaskAudit> auditRecords = taskAuditRepository.findByTaskIdOrderByChangeTimestampDesc(2L);

        // Then
        assertNotNull(auditRecords);
        assertEquals(1, auditRecords.size());
        assertEquals("CREATED", auditRecords.get(0).getAction());
        assertEquals("Test Task 2", auditRecords.get(0).getTaskTitle());
    }

    @Test
    void testSaveTaskAudit() {
        // Given
        TaskAudit newAudit = new TaskAudit();
        newAudit.setTaskId(3L);
        newAudit.setAction("DELETED");
        newAudit.setTaskTitle("Test Task 3");
        newAudit.setTimestamp(LocalDateTime.now());
        newAudit.setChangedBy("ADMIN");
        newAudit.setEntityType("Task");
        newAudit.setOldValue("{\"id\":3,\"title\":\"Test Task 3\"}");

        // When
        TaskAudit savedAudit = taskAuditRepository.save(newAudit);

        // Then
        assertNotNull(savedAudit);
        assertNotNull(savedAudit.getId());
        assertEquals(3L, savedAudit.getTaskId());
        assertEquals("DELETED", savedAudit.getAction());
        assertEquals("Test Task 3", savedAudit.getTaskTitle());
        assertEquals("ADMIN", savedAudit.getChangedBy());
        assertEquals("Task", savedAudit.getEntityType());
    }

    @Test
    void testFindAll() {
        // When
        List<TaskAudit> allAudits = taskAuditRepository.findAll();

        // Then
        assertNotNull(allAudits);
        assertEquals(3, allAudits.size());
    }

    @Test
    void testDeleteAll() {
        // When
        taskAuditRepository.deleteAll();

        // Then
        List<TaskAudit> allAudits = taskAuditRepository.findAll();
        assertTrue(allAudits.isEmpty());
    }

    @Test
    void testFindByTaskIdOrderByTimestampDescWithMultipleTasks() {
        // Given - Add more audit records for task 1
        TaskAudit additionalAudit = new TaskAudit();
        additionalAudit.setTaskId(1L);
        additionalAudit.setAction("COMPLETED");
        additionalAudit.setTaskTitle("Test Task 1");
        additionalAudit.setTimestamp(LocalDateTime.now().minusMinutes(30));
        additionalAudit.setChangedBy("USER");
        additionalAudit.setEntityType("Task");
        additionalAudit.setNewValue("{\"id\":1,\"title\":\"Test Task 1\",\"status\":\"COMPLETED\"}");
        taskAuditRepository.save(additionalAudit);

        // When
        List<TaskAudit> auditRecords = taskAuditRepository.findByTaskIdOrderByChangeTimestampDesc(1L);

        // Then
        assertNotNull(auditRecords);
        assertEquals(3, auditRecords.size());
        
        // Should be ordered by timestamp descending
        assertTrue(auditRecords.get(0).getTimestamp().isAfter(auditRecords.get(1).getTimestamp()));
        assertTrue(auditRecords.get(1).getTimestamp().isAfter(auditRecords.get(2).getTimestamp()));
        
        assertEquals("COMPLETED", auditRecords.get(0).getAction());
        assertEquals("UPDATED", auditRecords.get(1).getAction());
        assertEquals("CREATED", auditRecords.get(2).getAction());
    }

    @Test
    void testTaskAuditFields() {
        // Given
        TaskAudit audit = new TaskAudit();
        audit.setTaskId(4L);
        audit.setAction("UPDATED");
        audit.setTaskTitle("Test Task 4");
        audit.setTimestamp(LocalDateTime.now());
        audit.setChangedBy("SYSTEM");
        audit.setEntityType("Task");
        audit.setOldValue("{\"old\":\"value\"}");
        audit.setNewValue("{\"new\":\"value\"}");
        audit.setIpAddress("192.168.1.1");
        audit.setUserAgent("Mozilla/5.0");

        // When
        TaskAudit savedAudit = taskAuditRepository.save(audit);

        // Then
        assertNotNull(savedAudit.getId());
        assertEquals(4L, savedAudit.getTaskId());
        assertEquals("UPDATED", savedAudit.getAction());
        assertEquals("Test Task 4", savedAudit.getTaskTitle());
        assertEquals("SYSTEM", savedAudit.getChangedBy());
        assertEquals("Task", savedAudit.getEntityType());
        assertEquals("{\"old\":\"value\"}", savedAudit.getOldValue());
        assertEquals("{\"new\":\"value\"}", savedAudit.getNewValue());
        assertEquals("192.168.1.1", savedAudit.getIpAddress());
        assertEquals("Mozilla/5.0", savedAudit.getUserAgent());
    }
}
