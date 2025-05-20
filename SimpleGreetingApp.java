import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Simple standalone HTTP server that mimics the Spring Boot greeting endpoint.
 * This can be run with just the JDK, no Maven required.
 */
public class SimpleGreetingApp {

    /**
     * Main method to start the HTTP server
     */
    public static void main(String[] args) throws IOException {
        // Changed port from 8089 to 8090 to avoid conflicts
        int port = 8090;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Create context for the greeting endpoint
        server.createContext("/api/v1/greetings/hello", new GreetingHandler());
        
        // Create context for a basic info page
        server.createContext("/", new InfoHandler());
        
        // Set executor
        server.setExecutor(Executors.newFixedThreadPool(10));
        
        // Start the server
        server.start();
        
        System.out.println("Server started on port " + port);
        System.out.println("Try accessing:");
        System.out.println("  - http://localhost:" + port + "/");
        System.out.println("  - http://localhost:" + port + "/api/v1/greetings/hello");
        System.out.println("  - http://localhost:" + port + "/api/v1/submissions-demo");
        
        // Add a demo endpoint for submissions
        server.createContext("/api/v1/submissions-demo", new SubmissionsDemoHandler());
    }

    /**
     * Handler for the greeting endpoint
     */
    static class GreetingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // The greeting message
            String response = "Xin chào! Backend Spring Boot cho ứng dụng quản lý lớp học đã sẵn sàng!";
            
            // Set response headers
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
            
            // Write response
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes("UTF-8"));
            }
        }
    }
    
    /**
     * Handler for the info page
     */
    static class InfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // HTML content with information and links
            String response = "<!DOCTYPE html>" +
                    "<html><head><title>Classroom App Test Server</title>" +
                    "<style>body{font-family:sans-serif;max-width:800px;margin:0 auto;padding:20px;line-height:1.6}</style>" +
                    "</head><body>" +
                    "<h1>Test Server for Classroom Management App</h1>" +
                    "<p>This is a simple test server that mimics the Spring Boot application.</p>" +
                    "<p>Available endpoints:</p>" +
                    "<ul>" +
                    "<li><a href=\"/api/v1/greetings/hello\">Greeting API</a></li>" +
                    "<li><a href=\"/api/v1/submissions-demo\">Submissions Demo API</a></li>" +
                    "</ul>" +
                    "<p>Note: This is a temporary solution until Maven dependency issues are resolved.</p>" +
                    "</body></html>";
            
            // Set response headers
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
            
            // Write response
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes("UTF-8"));
            }
        }
    }
    
    /**
     * Handler for the submissions demo endpoint
     */
    static class SubmissionsDemoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Demo JSON response
            String response = "{\n" +
                    "  \"submissions\": [\n" +
                    "    {\n" +
                    "      \"id\": 1,\n" +
                    "      \"assignmentTitle\": \"Introduction to Java\",\n" +
                    "      \"studentName\": \"John Doe\",\n" +
                    "      \"submittedAt\": \"2023-05-20T15:30:00\",\n" +
                    "      \"isGraded\": true,\n" +
                    "      \"score\": 95\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": 2,\n" +
                    "      \"assignmentTitle\": \"Spring Boot Basics\",\n" +
                    "      \"studentName\": \"Jane Smith\",\n" +
                    "      \"submittedAt\": \"2023-05-19T14:15:00\",\n" +
                    "      \"isGraded\": false\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
            
            // Set response headers
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            
            // Write response
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
} 