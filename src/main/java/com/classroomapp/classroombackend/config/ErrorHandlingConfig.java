package com.classroomapp.classroombackend.config;

import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình riêng cho error handling để đảm bảo 404 errors được throw exception
 * thay vì trả về trang 404 mặc định
 */
@Configuration
public class ErrorHandlingConfig {

    /**
     * Cấu hình DispatcherServlet để throw exception khi không tìm thấy handler
     * Điều này đảm bảo GlobalExceptionHandler có thể catch và xử lý 404 errors
     */
    // Note: setThrowExceptionIfNoHandlerFound is deprecated
    // This configuration is now handled in application.properties:
    // spring.web.throw-exception-if-no-handler-found=true
    // spring.web.resources.add-mappings=false
    
}
