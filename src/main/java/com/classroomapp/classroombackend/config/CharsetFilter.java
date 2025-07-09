package com.classroomapp.classroombackend.config;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter to ensure all requests and responses use UTF-8 encoding
 * This helps prevent Vietnamese character encoding issues
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@WebFilter("/*")
public class CharsetFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to initialize
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        // Set UTF-8 encoding for request
        if (request instanceof HttpServletRequest) {
            request.setCharacterEncoding("UTF-8");
        }
        
        // Set UTF-8 encoding for response
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.setHeader("Content-Type", "application/json; charset=UTF-8");
        }
        
        // Continue with the filter chain
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Nothing to destroy
    }
}
