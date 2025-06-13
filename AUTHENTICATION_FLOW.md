# Vietnamese Educational System - Login Flow Architecture

## Authentication Flow

```mermaid
sequenceDiagram
    participant User
    participant AuthController
    participant AuthService
    participant UserDetailsService
    participant SecurityContext
    participant UserRepository
    participant JwtUtil

    User->>AuthController: Login Request (username, password)
    AuthController->>AuthService: authenticateUser()
    AuthService->>UserDetailsService: loadUserByUsername()
    UserDetailsService->>UserRepository: findByUsername()
    UserRepository-->>UserDetailsService: User data
    UserDetailsService-->>AuthService: UserDetails object
    AuthService->>SecurityContext: Set Authentication
    AuthService->>JwtUtil: generateToken()
    JwtUtil-->>AuthService: JWT Token
    AuthService-->>AuthController: LoginResponseDto
    AuthController-->>User: Token + User Info
```

## Token Validation Flow

```mermaid
sequenceDiagram
    participant User
    participant JwtAuthenticationFilter
    participant JwtUtil
    participant SecurityContext
    participant ProtectedResource

    User->>JwtAuthenticationFilter: Request with Bearer Token
    JwtAuthenticationFilter->>JwtUtil: validateToken()
    JwtUtil-->>JwtAuthenticationFilter: Valid/Invalid
    
    alt Token valid
        JwtAuthenticationFilter->>JwtUtil: getUsernameFromToken()
        JwtUtil-->>JwtAuthenticationFilter: Username
        JwtAuthenticationFilter->>JwtUtil: getRoleFromToken()
        JwtUtil-->>JwtAuthenticationFilter: Role
        JwtAuthenticationFilter->>SecurityContext: Set Authentication
        JwtAuthenticationFilter->>ProtectedResource: Continue to resource
        ProtectedResource-->>User: Resource Data
    else Token invalid
        JwtAuthenticationFilter->>ProtectedResource: Continue without Authentication
        ProtectedResource-->>User: 401 Unauthorized
    end
```

## Registration Flow

```mermaid
sequenceDiagram
    participant User
    participant AuthController
    participant AuthService
    participant UserRepository
    participant PasswordEncoder

    User->>AuthController: Register Request (RegisterDto)
    AuthController->>AuthService: registerUser()
    AuthService->>UserRepository: Check if username exists
    UserRepository-->>AuthService: Username availability
    AuthService->>UserRepository: Check if email exists
    UserRepository-->>AuthService: Email availability
    
    alt Username/Email available
        AuthService->>PasswordEncoder: encode(password)
        PasswordEncoder-->>AuthService: Encoded password
        AuthService->>UserRepository: save(user)
        UserRepository-->>AuthService: Saved User
        AuthService-->>AuthController: UserDto
        AuthController-->>User: Registration Successful
    else Username/Email already exists
        AuthService-->>AuthController: Exception
        AuthController-->>User: Registration Failed (Error message)
    end
```

## Password Reset Flow

```mermaid
sequenceDiagram
    participant User
    participant AuthController
    participant AuthService
    participant UserRepository
    participant JwtUtil
    participant EmailService

    User->>AuthController: Forgot Password Request (email)
    AuthController->>AuthService: requestPasswordReset()
    AuthService->>UserRepository: findByEmail()
    UserRepository-->>AuthService: User data
    AuthService->>JwtUtil: generateToken()
    JwtUtil-->>AuthService: Reset Token
    AuthService->>EmailService: sendPasswordResetEmail()
    EmailService-->>User: Email with Reset Link
    
    User->>AuthController: Reset Password Request (token, newPassword)
    AuthController->>AuthService: resetPassword()
    AuthService->>JwtUtil: validateToken()
    JwtUtil-->>AuthService: Token validity
    AuthService->>JwtUtil: getUsernameFromToken()
    JwtUtil-->>AuthService: Username
    AuthService->>UserRepository: findByUsername()
    UserRepository-->>AuthService: User data
    AuthService->>UserRepository: update password
    AuthService-->>AuthController: Operation result
    AuthController-->>User: Password Reset Confirmation
```

## Google Authentication Flow

```mermaid
sequenceDiagram
    participant User
    participant AuthController
    participant AuthService
    participant FirebaseAuth
    participant UserRepository
    participant JwtUtil

    User->>AuthController: Google Login Request (idToken)
    AuthController->>AuthService: authenticateWithGoogle()
    AuthService->>FirebaseAuth: verifyIdToken()
    FirebaseAuth-->>AuthService: Decoded Token (email, name)
    AuthService->>UserRepository: findByEmail()
    
    alt User exists
        UserRepository-->>AuthService: User data
    else User doesn't exist
        AuthService->>UserRepository: create new user
        UserRepository-->>AuthService: New User data
    end
    
    AuthService->>JwtUtil: generateToken()
    JwtUtil-->>AuthService: JWT Token
    AuthService-->>AuthController: LoginResponseDto
    AuthController-->>User: Token + User Info
```

## Architecture Components

### Controllers
- **AuthController**: Handles authentication requests (login, registration, password reset)
- **UserController**: User management operations
- **AdminController**: Administrative functions

### Services
- **AuthService**: Authentication business logic
- **UserService**: User management business logic
- **EmailService**: Email sending functionality

### Security
- **JwtUtil**: JWT token generation, validation and manipulation
- **JwtAuthenticationFilter**: Filter for checking JWT tokens in requests
- **SecurityConfig**: Spring Security configuration
- **CustomUserDetailsService**: User details loading for Spring Security

### Data
- **User**: User entity
- **UserRepository**: Data access for users
- **UserDto**: User data transfer object
- **LoginRequestDto/LoginResponseDto**: Login data transfer objects

### Exceptions
- **GlobalExceptionHandler**: Centralizes exception handling
- **CustomExceptions**: Application-specific exceptions
