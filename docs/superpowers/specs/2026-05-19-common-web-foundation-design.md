# Common Web Foundation Design

## Goal

Add initial shared web infrastructure for a Spring Boot 3 API: unified response payloads, business exceptions, centralized exception handling, and global CORS.

## Architecture

The project remains a single Spring Boot module under `com.example.smartconsult`. Shared API primitives live in focused packages:

- `common`: reusable response wrapper and status codes
- `exception`: business exception type and global exception handler
- `config`: cross-cutting Spring MVC configuration

Controllers return `Result<T>` explicitly. This keeps the first version simple and avoids automatic response wrapping edge cases with Swagger, `String` responses, and framework endpoints.

## Components

- `Result<T>` exposes `code`, `message`, and `data`, with static factories for success and failure responses.
- `ResultCode` centralizes common status codes and default messages.
- `BusinessException` carries a business code and message for predictable client errors.
- `GlobalExceptionHandler` maps business exceptions, request binding errors, and fallback exceptions into `Result<Void>` responses with appropriate HTTP status codes.
- `CorsConfig` allows cross-origin requests globally for API development.

## Testing

Use Spring Boot MockMvc tests to verify:

- `/health` returns the unified success payload.
- thrown `BusinessException` returns the expected business error body.
- missing request parameters return a parameter validation failure body.
- unexpected exceptions return the common system error body.
- CORS preflight requests receive permissive CORS headers.
