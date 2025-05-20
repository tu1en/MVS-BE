# Coding Conventions and Implementation Approach

## Coding Standards

### Naming Conventions

1. **Class Names:**
   - PascalCase (e.g., `UserController`, `AuthService`)
   - Names should be nouns and clearly describe the purpose of the class
   - Entity classes should be singular nouns

2. **Method Names:**
   - Follow the `DoSomething` pattern (e.g., `FindAllUsers`, `CreateUser`)
   - Should start with a verb indicating the action
   - Should clearly describe the purpose of the method

3. **Variable Names:**
   - camelCase (e.g., `userName`, `isAdmin`)
   - Should be clear and descriptive
   - Avoid abbreviations unless widely understood
   - Boolean variables should start with "is", "has", or similar

4. **Constants:**
   - ALL_CAPS with underscores (e.g., `MAX_USERS`, `DEFAULT_PAGE_SIZE`)

5. **Package Names:**
   - All lowercase (e.g., `com.classroomapp.classroombackend.controller`)
   - Use reversed domain name pattern

### Code Organization

1. **Package Structure:**
   - Follow standard Spring layered architecture
   - Group related functionality into packages
   - Separate interfaces and implementations

2. **Class Organization:**
   - Order fields, constructors, methods logically
   - Group related methods together
   - Keep methods short and focused on a single responsibility

3. **Layer Responsibilities:**
   - Controllers: Handle HTTP requests/responses, validate inputs
   - Services: Implement business logic
   - Repositories: Handle data access
   - DTOs: Transfer data between layers
   - Entities: Represent database tables

## Documentation Standards

1. **Class Documentation:**
   - Brief description of the class purpose
   - Note any special considerations or relationships

2. **Method Documentation:**
   - Describe purpose of the method
   - Document parameters and return values
   - Note exceptions that may be thrown

3. **Code Comments:**
   - Comment complex business logic
   - Always comment regex patterns
   - Comments for conditional logic that may not be obvious
   - Comments for loops with complex conditions

## Implementation Practices

1. **Error Handling:**
   - Use specific exception types 
   - Handle exceptions at appropriate levels
   - Provide meaningful error messages
   - Use global exception handler for consistent responses

2. **Security Practices:**
   - Never store plain-text passwords
   - Validate input on both client and server sides
   - Use DTOs to prevent over-posting attacks
   - Apply principle of least privilege for authorization

3. **Testing Approach:**
   - Write unit tests for services
   - Write integration tests for repositories
   - Use MockMvc for controller tests
   - Test both positive and negative scenarios

4. **Design Patterns:**
   - DTO pattern for data transfer
   - Repository pattern for data access
   - Dependency Injection through Spring
   - Factory pattern where appropriate

## Database Design

1. **Naming:**
   - Table names: plural nouns in snake_case
   - Column names: snake_case
   - Primary keys: `id` or `table_name_id`
   - Foreign keys: `related_table_singular_id`

2. **Relationships:**
   - Use appropriate joins (One-to-Many, Many-to-Many)
   - Define proper cascades
   - Use indexes for frequently queried columns

## REST API Design

1. **Endpoints:**
   - Use nouns, not verbs for resources
   - Use HTTP methods appropriately (GET, POST, PUT, DELETE)
   - Versioning through URL path (`/api/v1/users`)

2. **Status Codes:**
   - 200: Success
   - 201: Created
   - 204: No Content (successful deletion)
   - 400: Bad Request (validation errors)
   - 401: Unauthorized
   - 403: Forbidden
   - 404: Not Found
   - 500: Internal Server Error

3. **Response Format:**
   - Consistent structure for all responses
   - Use envelopes for metadata (pagination, etc.)
   - Clear error messages and codes 