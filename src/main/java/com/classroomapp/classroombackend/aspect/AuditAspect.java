package com.classroomapp.classroombackend.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.classroomapp.classroombackend.model.administration.AuditLog;
import com.classroomapp.classroombackend.service.administration.SystemAdministrationService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AuditAspect {

    @Autowired
    private SystemAdministrationService adminService;

    // Audit cho Authentication Controller (login/logout)
    @AfterReturning(pointcut = "execution(* com.classroomapp.classroombackend.controller.auth.*.login*(..))", returning = "result")
    public void auditLogin(JoinPoint joinPoint, Object result) {
        try {
            String username = extractUsernameFromLoginArgs(joinPoint.getArgs());
            boolean success = isLoginSuccessful(result);
            
            createAuditLog(
                username,
                success ? AuditLog.AuditAction.LOGIN : AuditLog.AuditAction.LOGIN_FAILED,
                "User login attempt",
                success,
                success ? AuditLog.AuditSeverity.INFO : AuditLog.AuditSeverity.WARN,
                AuditLog.AuditCategory.AUTHENTICATION
            );
        } catch (Exception e) {
            log.error("Error creating login audit log", e);
        }
    }

    @AfterReturning(pointcut = "execution(* com.classroomapp.classroombackend.controller.auth.*.logout*(..))")
    public void auditLogout(JoinPoint joinPoint) {
        try {
            String username = getCurrentUsername();
            
            createAuditLog(
                username,
                AuditLog.AuditAction.LOGOUT,
                "User logout",
                true,
                AuditLog.AuditSeverity.INFO,
                AuditLog.AuditCategory.AUTHENTICATION
            );
        } catch (Exception e) {
            log.error("Error creating logout audit log", e);
        }
    }

    // Audit cho failed login
    @AfterThrowing(pointcut = "execution(* com.classroomapp.classroombackend.controller.auth.*.login*(..))", throwing = "exception")
    public void auditLoginFailure(JoinPoint joinPoint, Exception exception) {
        try {
            String username = extractUsernameFromLoginArgs(joinPoint.getArgs());
            
            createAuditLog(
                username,
                AuditLog.AuditAction.LOGIN_FAILED,
                "Login failed: " + exception.getMessage(),
                false,
                AuditLog.AuditSeverity.WARN,
                AuditLog.AuditCategory.AUTHENTICATION
            );
        } catch (Exception e) {
            log.error("Error creating login failure audit log", e);
        }
    }

    // Audit cho các controller admin
    @AfterReturning(pointcut = "execution(* com.classroomapp.classroombackend.controller.administration.*.*(..))")
    public void auditAdminActions(JoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String username = getCurrentUsername();
            
            AuditLog.AuditAction action = mapMethodToAction(methodName);
            
            createAuditLog(
                username,
                action,
                String.format("Admin action: %s.%s", className, methodName),
                true,
                AuditLog.AuditSeverity.INFO,
                AuditLog.AuditCategory.SYSTEM_CONFIG
            );
        } catch (Exception e) {
            log.error("Error creating admin action audit log", e);
        }
    }

    // Audit cho user management
    @AfterReturning(pointcut = "execution(* com.classroomapp.classroombackend.controller.usermanagement.*.*(..))")
    public void auditUserManagement(JoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String username = getCurrentUsername();
            
            AuditLog.AuditAction action = mapMethodToAction(methodName);
            
            createAuditLog(
                username,
                action,
                String.format("User management: %s.%s", className, methodName),
                true,
                AuditLog.AuditSeverity.INFO,
                AuditLog.AuditCategory.USER_MANAGEMENT
            );
        } catch (Exception e) {
            log.error("Error creating user management audit log", e);
        }
    }

    // Audit cho errors
    @AfterThrowing(pointcut = "execution(* com.classroomapp.classroombackend.controller.*.*(..))", throwing = "exception")
    public void auditErrors(JoinPoint joinPoint, Exception exception) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String username = getCurrentUsername();
            
            createAuditLog(
                username,
                AuditLog.AuditAction.ERROR,
                String.format("Error in %s.%s: %s", className, methodName, exception.getMessage()),
                false,
                AuditLog.AuditSeverity.ERROR,
                AuditLog.AuditCategory.ERROR
            );
        } catch (Exception e) {
            log.error("Error creating error audit log", e);
        }
    }

    private void createAuditLog(String username, AuditLog.AuditAction action, String description, 
                               boolean success, AuditLog.AuditSeverity severity, AuditLog.AuditCategory category) {
        try {
            HttpServletRequest request = getCurrentRequest();
            
            AuditLog auditLog = AuditLog.builder()
                .username(username)
                .action(action)
                .description(description)
                .success(success)
                .severity(severity)
                .category(category)
                .request(
                    request != null ? request.getRemoteAddr() : "unknown",
                    request != null ? request.getHeader("User-Agent") : "unknown",
                    request != null ? request.getRequestURL().toString() : "unknown",
                    request != null ? request.getMethod() : "unknown"
                )
                .build();
            
            // Async save to avoid performance impact
            try {
                adminService.createAuditLog(auditLog);
            } catch (Exception e) {
                log.error("Failed to save audit log", e);
            }
        } catch (Exception e) {
            log.error("Error in createAuditLog", e);
        }
    }

    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception e) {
            log.debug("Could not get current username", e);
        }
        return "anonymous";
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            log.debug("Could not get current request", e);
            return null;
        }
    }

    private String extractUsernameFromLoginArgs(Object[] args) {
        try {
            // Giả sử arg đầu tiên là login request có username
            if (args != null && args.length > 0) {
                Object loginRequest = args[0];
                // Sử dụng reflection để lấy username
                if (loginRequest != null) {
                    String str = loginRequest.toString();
                    // Simple extraction, có thể cần customize dựa trên LoginRequest class
                    return str.contains("username") ? "extracted_user" : "unknown";
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract username from login args", e);
        }
        return "unknown";
    }

    private boolean isLoginSuccessful(Object result) {
        // Kiểm tra result có chứa token hoặc success indicator
        if (result != null) {
            String resultStr = result.toString();
            return resultStr.contains("token") || resultStr.contains("success");
        }
        return false;
    }

    private AuditLog.AuditAction mapMethodToAction(String methodName) {
        if (methodName.startsWith("create") || methodName.startsWith("add")) {
            return AuditLog.AuditAction.CREATE;
        } else if (methodName.startsWith("update") || methodName.startsWith("edit")) {
            return AuditLog.AuditAction.UPDATE;
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return AuditLog.AuditAction.DELETE;
        } else if (methodName.startsWith("get") || methodName.startsWith("find") || methodName.startsWith("search")) {
            return AuditLog.AuditAction.READ;
        } else {
            return AuditLog.AuditAction.READ; // Default
        }
    }
}