# SeranaMCP Setup Guide

## Overview
SeranaMCP is a Model Communication Protocol (MCP) server for the classroom management system. It provides AI assistance capabilities through web-based communication protocols.

## Architecture

### Components
1. **SeranaMCPServer** - Core MCP server implementation with WebSocket support
2. **SeranaMCPConfig** - Configuration management with application properties
3. **SeranaMCPController** - REST API controller for MCP operations
4. **SeranaMCPClient** - Client implementation for MCP communication
5. **Serana*Tool** - Classroom management tools for AI assistance

### Tools Available
- Classroom Management
  - get_classroom_info
  - list_classrooms
  - create_classroom
  - update_classroom
  - delete_classroom
- Student Management
  - get_student_info
  - list_students
  - add_student
  - update_student
  - remove_student
- Assignment Management
  - get_assignment_info
  - list_assignments
  - create_assignment
  - update_assignment
  - delete_assignment
  - submit_assignment

## Configuration

### Application Properties
```properties
# Enable Serana MCP
serana.mcp.enabled=true
serana.mcp.name=Serana-Classroom-MCP
serana.mcp.port=8081
serana.mcp.auth-token=serana-classroom-mcp-2024

# Tools Configuration
serana.mcp.tools.enable-classroom-tools=true
serana.mcp.tools.enable-student-tools=true
serana.mcp.tools.enable-assignment-tools=true
```

## API Endpoints

### RESTful Endpoints
- **GET** `/api/mcp/serana/status` - Server status and configuration
- **GET** `/api/mcp/serana/capabilities` - Server capabilities
- **GET** `/api/mcp/serana/tools` - List available tools
- **POST** `/api/mcp/serana/tools/execute` - Execute a specific tool
- **POST** `/api/mcp/serana/mcp/request` - Process MCP request
- **GET** `/api/mcp/serana/health` - Health check

### WebSocket Endpoints
- **WebSocket** `/ws/serana-mcp` - Real-time MCP communication

## Usage

### Starting the MCP Server
1. Ensure Spring Boot application is running
2. Configure application.properties for SeranaMCP
3. Server will start on configured port

### Testing Connection
```bash
curl -X GET http://localhost:8088/api/mcp/serana/status
```

### Execute a Tool
```bash
curl -X POST http://localhost:8088/api/mcp/serana/tools/execute \
  -H "Content-Type: application/json" \
  -d '{
    "tool": "get_server_info",
    "arguments": {}
  }'
```

### WebSocket Testing
Connect to `ws://localhost:8088/ws/serana-mcp` and send MCP requests:
```json
{
  "method": "initialize",
  "params": {
    "protocolVersion": "2024-11-05"
  }
}
```

### MCP Client Usage
```java
// Initialize MCP client
SeranaMCPClient client = new SeranaMCPClient(config);
MCPResponse response = client.sendRequest(request);
```

## Testing

### Unit Tests
Run tests with:
```bash
mvn test
```

### Integration Tests
```bash
mvn test -Dtest=SeranaMCPIntegrationTest
```

### Manual Testing with Postman
Import the collection from `test/resources/serana-mcp-collection.json`

## Troubleshooting

### Common Issues
1. **Port Already in Use** - Change serana.mcp.port in application.properties
2. **Authentication Failed** - Check serana.mcp.auth-token
3. **Tools Not Loading** - Ensure tools are enabled in configuration
4. **WebSocket Connection Timeout** - Increase connection timeout value

### Debug Mode
Enable debug logging:
```properties
serana.mcp.logging.level=DEBUG
logging.level.com.classroomapp.classroombackend.mcp.serana=DEBUG
```

## Security Considerations
- All endpoints respect application security configuration
- WebSocket connections require appropriate authentication
- CORS is configured for cross-origin requests
- Token-based authentication is supported

## Future Enhancements
- OAuth2/JWT authentication
- Rate limiting
- Advanced logging and monitoring
- Plugin system for tools
- Multi-tenancy support

## Support
For issues or questions about SeranaMCP implementation, check:
1. Application logs at logs/serana-mcp.log
2. Spring Boot logging at DEBUG level
3. Integration test results
4. Configuration validation