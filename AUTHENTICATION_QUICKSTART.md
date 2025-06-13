# Authentication Flow Quick-Start Guide

## Overview

This document provides a quick reference for the authentication flow implemented in the Vietnamese Educational System application. The system uses Spring Security with JWT tokens for authentication and authorization.

## Available Endpoints

| Endpoint | Method | Description | Request Body | Response |
|----------|--------|-------------|-------------|----------|
| `/api/auth/register` | POST | Register a new user | RegisterDto | UserDto |
| `/api/auth/login` | POST | User login | LoginRequestDto | LoginResponseDto |
| `/api/auth/google-login` | POST | Login with Google | GoogleAuthRequestDto | LoginResponseDto |
| `/api/auth/forgot-password` | POST | Request password reset | PasswordResetRequestDto | Success message |
| `/api/auth/reset-password` | POST | Reset password | PasswordConfirmationDto | Success message |

## Authentication Process

### 1. User Registration

To register a new user, send a POST request to `/api/auth/register` with the following JSON:

```json
{
  "username": "newuser",
  "password": "password123",
  "email": "user@example.com",
  "fullName": "New User",
  "roleId": 1
}
```

The roleId values are:
- 1: STUDENT
- 2: TEACHER
- 3: MANAGER
- 4: ADMIN

### 2. User Login

To login, send a POST request to `/api/auth/login` with:

```json
{
  "username": "newuser",
  "password": "password123"
}
```

The response will include a JWT token to be used for authentication:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "role": "STUDENT",
  "roleId": 1,
  "username": "newuser",
  "email": "user@example.com",
  "userId": 1
}
```

### 3. Google Login

For Google authentication, send a POST request to `/api/auth/google-login` with:

```json
{
  "idToken": "google-id-token-from-client"
}
```

The response format is the same as regular login.

### 4. Using JWT Token for Authentication

For protected endpoints, include the JWT token in the Authorization header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 5. Password Reset

To initiate a password reset, send a POST request to `/api/auth/forgot-password` with:

```json
{
  "email": "user@example.com"
}
```

The user will receive an email with a reset link. To complete the reset, send a POST request to `/api/auth/reset-password` with:

```json
{
  "token": "reset-token-from-email",
  "newPassword": "newPassword123"
}
```

## Error Handling

All API responses follow a consistent error format:

```json
{
  "timestamp": "2023-06-03T10:15:30.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Username already taken",
  "path": "/api/auth/register"
}
```

For validation errors, additional field-specific errors are included:

```json
{
  "timestamp": "2023-06-03T10:15:30.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/auth/register",
  "errors": {
    "username": "Username is required",
    "password": "Password must be at least 6 characters"
  }
}
```

## Common HTTP Status Codes

- 200 OK: Request successful
- 201 Created: Resource created successfully
- 400 Bad Request: Invalid request data
- 401 Unauthorized: Authentication required or failed
- 403 Forbidden: Authenticated but not authorized
- 404 Not Found: Resource not found
- 500 Internal Server Error: Server-side error
