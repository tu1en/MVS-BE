package com.classroomapp.classroombackend.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
// Enable CORS filter
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
// Disabled to avoid conflicts with SecurityConfig CORS configuration
// @Component
// @Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomCorsFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(CustomCorsFilter.class);
    
    // List of allowed origins
    private final List<String> allowedOrigins = Arrays.asList(
        "http://localhost:3000",
        "http://localhost:3001",  // Add port 3001 for frontend
        "http://localhost:5173",
        "http://localhost:8088"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        
        logger.debug("CustomCorsFilter processing request: {} {}", request.getMethod(), request.getRequestURI());

        // Get origin from request
        String origin = request.getHeader("Origin");
        
        // Set CORS headers
        if (origin != null && allowedOrigins.contains(origin)) {
            // Only allow specified origins
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
        } 
        
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Content-Length, X-Requested-With");
        
        // Handle preflight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            logger.debug("Handling OPTIONS preflight request");
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            // Pass request down the chain
            chain.doFilter(req, res);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        logger.info("CustomCorsFilter initialized");
    }

    @Override
    public void destroy() {
        logger.info("CustomCorsFilter destroyed");
    }
} 