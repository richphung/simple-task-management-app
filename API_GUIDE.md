# 📖 Task Management API - User Guide

## Table of Contents
1. [Introduction](#introduction)
2. [Quick Start](#quick-start)
3. [API Overview](#api-overview)
4. [Core Operations](#core-operations)
5. [Advanced Features](#advanced-features)
6. [Error Handling](#error-handling)
7. [Best Practices](#best-practices)

---

## Introduction

The Task Management API is a RESTful web service that provides comprehensive task management capabilities including CRUD operations, bulk operations, analytics, smart suggestions, and audit trails.

### Key Features
- ✅ Complete CRUD operations for tasks
- ⚡ Bulk operations for efficiency
- 📊 Real-time analytics and reporting
- 🔍 Advanced search and filtering
- 💡 AI-powered smart suggestions
- 📝 Complete audit trail
- 📤 Data export (CSV, JSON)
- 🔄 Task duplication with smart defaults

### Base URL
```
http://localhost:8080/api
```

### Response Format
All endpoints return responses wrapped in a standardized `ApiResponse` object:

```json
{
  "success": true,
  "data": { ... },
  "message": null,
  "timestamp": "2025-10-19T20:30:00.000",
  "status": 200
}
```

---

## Quick Start

### 1. Create a Task

**Request:**
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete project documentation",
    "description": "Write comprehensive API documentation with examples",
    "priority": "HIGH",
    "status": "TODO",
    "dueDate": "2025-10-31",
    "notes": "Include Swagger UI screenshots"
  }'
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Complete project documentation",
    "description": "Write comprehensive API documentation with examples",
    "priority": "HIGH",
    "status": "TODO",
    "dueDate": "2025-10-31",
    "completedAt": null,
    "notes": "Include Swagger UI screenshots",
    "createdAt": "2025-10-19T20:30:00",
    "updatedAt": "2025-10-19T20:30:00",
    "overdue": false
  },
  "message": null,
  "timestamp": "2025-10-19T20:30:00.000",
  "status": 201
}
```

### 2. Get All Tasks

**Request:**
```bash
curl -X GET "http://localhost:8080/api/tasks?page=0&size=10&sort=createdAt,desc"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "pageable": { ... },
    "totalElements": 25,
    "totalPages": 3,
    "last": false,
    "first": true
  },
  "message": null,
  "timestamp": "2025-10-19T20:30:00.000",
  "status": 200
}
```

### 3. Update a Task

**Request:**
```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete project documentation",
    "description": "Write comprehensive API documentation with examples and screenshots",
    "priority": "HIGH",
    "status": "IN_PROGRESS",
    "dueDate": "2025-10-31"
  }'
```

### 4. Complete a Task

**Request:**
```bash
curl -X PUT http://localhost:8080/api/tasks/1/complete
```

### 5. Delete a Task

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/tasks/1
```

**Response:** 204 No Content

---

## API Overview

### 1. Task Management (`/api/tasks`)
Core CRUD operations for task management.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/tasks` | Create a new task |
| GET | `/tasks` | Get all tasks (paginated) |
| GET | `/tasks/{id}` | Get task by ID |
| PUT | `/tasks/{id}` | Update a task |
| DELETE | `/tasks/{id}` | Delete a task |
| PUT | `/tasks/{id}/complete` | Mark task as completed |
| POST | `/tasks/search` | Search tasks with criteria |
| GET | `/tasks/search/quick?q={keyword}` | Quick keyword search |
| GET | `/tasks/status/{status}` | Get tasks by status |
| GET | `/tasks/overdue` | Get overdue tasks |

### 2. Bulk Operations (`/api/tasks/bulk`)
Efficient batch operations for multiple tasks.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/bulk/create` | Create multiple tasks |
| PUT | `/bulk/status` | Update status of multiple tasks |
| PUT | `/bulk/complete` | Complete multiple tasks |
| POST | `/bulk/delete` | Delete multiple tasks |

### 3. Analytics & Reporting (`/api/analytics`)
Task statistics and productivity insights.

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/analytics/dashboard` | Get comprehensive statistics |
| GET | `/analytics/status-counts` | Get task count by status |
| GET | `/analytics/priority-counts` | Get task count by priority |

### 4. Data Export (`/api/export`)
Export task data in various formats.

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/export/tasks/csv` | Export all tasks to CSV |
| GET | `/export/tasks/json` | Export all tasks to JSON |
| GET | `/export/analytics/csv` | Export analytics to CSV |

### 5. Smart Suggestions (`/api/suggestions`)
AI-powered task property suggestions.

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/suggestions/task-properties?title={title}` | Get property suggestions |
| GET | `/suggestions/priority?title={title}` | Get priority suggestion |
| GET | `/suggestions/title-completions?prefix={prefix}` | Get title completions |

### 6. Task Duplication (`/api/tasks`)
Duplicate existing tasks with smart defaults.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/tasks/{id}/duplicate` | Duplicate a task |
| POST | `/tasks/{id}/duplicate-with-changes` | Duplicate with custom changes |

### 7. Audit Trail (`/api/audit`)
Task change history and audit logs.

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/audit` | Get all audit records |
| GET | `/audit/task/{taskId}` | Get audit history for a task |
| GET | `/audit/action/{action}` | Get audits by action type |
| GET | `/audit/date-range` | Get audits by date range |
| GET | `/audit/task/{taskId}/statistics` | Get audit statistics for a task |

---

## Core Operations

### Creating Tasks

**Endpoint:** `POST /api/tasks`

**Required Fields:**
- `title` (string, max 255 chars)
- `priority` (HIGH, MEDIUM, LOW)
- `status` (TODO, IN_PROGRESS, COMPLETED, CANCELLED)

**Optional Fields:**
- `description` (string, max 2000 chars)
- `dueDate` (date, format: YYYY-MM-DD)
- `notes` (string, max 1000 chars)

**Example:**
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Review pull request",
    "description": "Review PR #123 for authentication feature",
    "priority": "HIGH",
    "status": "TODO",
    "dueDate": "2025-10-25"
  }'
```

### Searching Tasks

**Endpoint:** `POST /api/tasks/search`

**Search Criteria:**
- `searchTerm`: Keyword search in title and description
- `status`: Filter by status (TODO, IN_PROGRESS, COMPLETED, CANCELLED)
- `priority`: Filter by priority (HIGH, MEDIUM, LOW)
- `dueDateFrom` / `dueDateTo`: Filter by due date range
- `createdFrom` / `createdTo`: Filter by creation date range
- `overdue`: Filter overdue tasks (true/false)
- `page`, `size`: Pagination parameters
- `sortBy`, `sortDirection`: Sorting parameters

**Example:**
```bash
curl -X POST http://localhost:8080/api/tasks/search \
  -H "Content-Type: application/json" \
  -d '{
    "searchTerm": "documentation",
    "status": "TODO",
    "priority": "HIGH",
    "dueDateFrom": "2025-10-01",
    "dueDateTo": "2025-10-31",
    "page": 0,
    "size": 20,
    "sortBy": "priority",
    "sortDirection": "asc"
  }'
```

### Quick Search

**Endpoint:** `GET /api/tasks/search/quick?q={keyword}`

**Example:**
```bash
curl -X GET "http://localhost:8080/api/tasks/search/quick?q=documentation&page=0&size=10"
```

---

## Advanced Features

### Bulk Operations

#### Bulk Create Tasks
**Endpoint:** `POST /api/tasks/bulk/create`

**Example:**
```bash
curl -X POST http://localhost:8080/api/tasks/bulk/create \
  -H "Content-Type: application/json" \
  -d '[
    {
      "title": "Task 1",
      "priority": "HIGH",
      "status": "TODO"
    },
    {
      "title": "Task 2",
      "priority": "MEDIUM",
      "status": "TODO"
    }
  ]'
```

#### Bulk Update Status
**Endpoint:** `PUT /api/tasks/bulk/status`

**Example:**
```bash
curl -X PUT http://localhost:8080/api/tasks/bulk/status \
  -H "Content-Type: application/json" \
  -d '{
    "taskIds": [1, 2, 3],
    "status": "IN_PROGRESS"
  }'
```

#### Bulk Complete Tasks
**Endpoint:** `PUT /api/tasks/bulk/complete`

**Example:**
```bash
curl -X PUT http://localhost:8080/api/tasks/bulk/complete \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3]'
```

#### Bulk Delete Tasks
**Endpoint:** `POST /api/tasks/bulk/delete`

**Example:**
```bash
curl -X POST http://localhost:8080/api/tasks/bulk/delete \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3]'
```

**Response:** 204 No Content

### Analytics Dashboard

**Endpoint:** `GET /api/analytics/dashboard`

**Example:**
```bash
curl -X GET http://localhost:8080/api/analytics/dashboard
```

**Response:**
```json
{
  "success": true,
  "data": {
    "totalTasks": 100,
    "completedTasks": 75,
    "pendingTasks": 25,
    "overdueTasks": 5,
    "completionRate": 75.0,
    "statusCounts": {
      "TODO": 15,
      "IN_PROGRESS": 10,
      "COMPLETED": 75,
      "CANCELLED": 0
    },
    "priorityCounts": {
      "HIGH": 30,
      "MEDIUM": 50,
      "LOW": 20
    }
  },
  "message": null,
  "timestamp": "2025-10-19T20:30:00.000",
  "status": 200
}
```

### Smart Suggestions

**Endpoint:** `GET /api/suggestions/task-properties?title={title}`

**Example:**
```bash
curl -X GET "http://localhost:8080/api/suggestions/task-properties?title=Fix%20critical%20bug"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "suggestedPriority": "HIGH",
    "suggestedStatus": "TODO",
    "confidence": 0.85,
    "similarTasks": 5
  },
  "message": null,
  "timestamp": "2025-10-19T20:30:00.000",
  "status": 200
}
```

### Data Export

#### Export to CSV
**Endpoint:** `GET /api/export/tasks/csv`

**Example:**
```bash
curl -X GET http://localhost:8080/api/export/tasks/csv -o tasks_export.csv
```

#### Export to JSON
**Endpoint:** `GET /api/export/tasks/json`

**Example:**
```bash
curl -X GET http://localhost:8080/api/export/tasks/json -o tasks_export.json
```

### Task Duplication

**Endpoint:** `POST /api/tasks/{id}/duplicate`

**Example:**
```bash
curl -X POST http://localhost:8080/api/tasks/1/duplicate
```

---

## Error Handling

### Error Response Format

```json
{
  "error": "BAD_REQUEST",
  "message": "Validation failed",
  "details": {
    "title": "Title is required",
    "priority": "Priority must be HIGH, MEDIUM, or LOW"
  },
  "status": 400,
  "timestamp": "2025-10-19T20:30:00"
}
```

### Common HTTP Status Codes

| Code | Description | Common Causes |
|------|-------------|---------------|
| 200 | OK | Request succeeded |
| 201 | Created | Resource created successfully |
| 204 | No Content | Delete operation succeeded |
| 400 | Bad Request | Invalid input, validation failed |
| 404 | Not Found | Task not found |
| 500 | Internal Server Error | Server error |

### Common Validation Errors

1. **Missing Required Field**
   ```json
   {
     "error": "BAD_REQUEST",
     "message": "Title is required",
     "status": 400
   }
   ```

2. **Invalid Enum Value**
   ```json
   {
     "error": "BAD_REQUEST",
     "message": "Priority must be HIGH, MEDIUM, or LOW",
     "status": 400
   }
   ```

3. **Task Not Found**
   ```json
   {
     "error": "NOT_FOUND",
     "message": "Task not found with ID: 999",
     "status": 404
   }
   ```

---

## Best Practices

### 1. Pagination
Always use pagination for list endpoints to improve performance:
```bash
GET /api/tasks?page=0&size=20&sort=createdAt,desc
```

### 2. Bulk Operations
Use bulk operations for efficiency when working with multiple tasks:
```bash
# Instead of deleting tasks one by one
POST /api/tasks/bulk/delete
```

### 3. Search Optimization
- Use `quick search` for simple keyword searches
- Use `advanced search` for complex filtering
- Combine multiple criteria for precise results

### 4. Caching
- Analytics endpoints are cached (10-minute TTL)
- Refresh data by clearing cache or waiting for expiration

### 5. Error Handling
Always check the `success` field in the response:
```javascript
const response = await fetch('/api/tasks');
const data = await response.json();

if (data.success) {
  // Handle success
  console.log(data.data);
} else {
  // Handle error
  console.error(data.message);
}
```

### 6. Date Formats
- Use ISO 8601 format for dates: `YYYY-MM-DD`
- Use ISO 8601 format for timestamps: `YYYY-MM-DDTHH:mm:ss`

### 7. Performance Tips
- Use bulk operations for batches of 10+ tasks
- Enable pagination (default: 10 items per page)
- Use analytics cache for frequently accessed metrics
- Export data regularly for backups

---

## Interactive API Documentation

Explore the API interactively using Swagger UI:

**Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

**OpenAPI Spec:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

The Swagger UI provides:
- ✅ Interactive endpoint testing
- 📝 Complete request/response examples
- 🔍 Schema definitions
- 📊 Response format documentation
- 🧪 "Try it out" functionality

---

## Support & Resources

- **GitHub Repository:** [https://github.com/yourusername/task-management-api](https://github.com/yourusername/task-management-api)
- **Health Check:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- **H2 Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

---

**Last Updated:** October 19, 2025  
**API Version:** 1.0.0




