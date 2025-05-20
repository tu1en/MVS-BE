# Classroom Management Application - Backend

This is the backend service for the Classroom Management Application, built with Spring Boot.

## Project Structure

The project follows a standard Spring Boot application structure:

- `config`: Configuration classes for the application
- `controller`: REST API endpoints
- `model`: Entity classes representing database tables
- `repository`: Data access interfaces
- `service`: Business logic implementation
- `dto`: Data Transfer Objects for API requests/responses
- `exception`: Custom exception classes
- `util`: Utility classes

## Setup & Run

### Prerequisites
- Java 17 or higher
- Maven

### Running the Application
1. Clone the repository
2. Navigate to the project root directory
3. Run the application using Maven:
   ```
   mvn spring-boot:run
   ```
4. The application will start on port 8088

### Accessing the H2 Database Console
When the application is running, you can access the H2 database console at:
- URL: http://localhost:8088/h2-console
- JDBC URL: jdbc:h2:mem:classroomdb
- Username: sa
- Password: (leave empty)

## API Documentation

API documentation will be implemented in a future update using Springdoc OpenAPI or Swagger. 