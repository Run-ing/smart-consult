# Common Web Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add shared Spring Boot API infrastructure for response wrapping, business exceptions, global exception handling, and CORS.

**Architecture:** Add small focused classes under `common`, `exception`, and `config`. Controllers return `Result<T>` explicitly, while exception handlers convert failures into the same payload shape.

**Tech Stack:** Java 17, Spring Boot 3.5.12, Spring MVC, JUnit 5, MockMvc.

---

### Task 1: Failing Tests

**Files:**
- Create: `src/test/java/com/example/smartconsult/CommonWebFoundationTest.java`

- [ ] **Step 1: Write tests for response wrapping, exceptions, and CORS**

Create MockMvc tests that import the application, expose a test-only controller for exception scenarios, and assert JSON fields `code`, `message`, and `data`.

- [ ] **Step 2: Run tests and verify RED**

Run: `mvn test`

Expected: compilation failure because `Result`, `ResultCode`, and `BusinessException` do not exist yet.

### Task 2: Response Wrapper and Exceptions

**Files:**
- Create: `src/main/java/com/example/smartconsult/common/Result.java`
- Create: `src/main/java/com/example/smartconsult/common/ResultCode.java`
- Create: `src/main/java/com/example/smartconsult/exception/BusinessException.java`
- Create: `src/main/java/com/example/smartconsult/exception/GlobalExceptionHandler.java`
- Modify: `src/main/java/com/example/smartconsult/controller/HealthController.java`

- [ ] **Step 1: Implement minimal classes**

Add status codes, response factories, business exception fields, and handler methods for business, parameter, and fallback exceptions.

- [ ] **Step 2: Run tests and verify GREEN for web behavior**

Run: `mvn test`

Expected: all tests pass.

### Task 3: CORS

**Files:**
- Create: `src/main/java/com/example/smartconsult/config/CorsConfig.java`

- [ ] **Step 1: Implement global CORS**

Register `/**` CORS mapping with common methods, headers, credentials disabled, and all origin patterns.

- [ ] **Step 2: Run tests and verify full suite**

Run: `mvn test`

Expected: all tests pass.
