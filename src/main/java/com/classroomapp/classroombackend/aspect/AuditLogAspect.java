package com.classroomapp.classroombackend.aspect;

import java.time.LocalDateTime;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.classroomapp.classroombackend.model.administration.AuditLog;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.service.administration.SystemAdministrationService;
import com.classroomapp.classroombackend.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AuditLogAspect {

    @Autowired
    private SystemAdministrationService systemAdministrationService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Pointcut("execution(* com.classroomapp.classroombackend.controller..*(..)) && " +
              "!execution(* com.classroomapp.classroombackend.controller.administration.AdminController.getAuditLogs*(..))")
    public void controllerMethods() {}

    @Pointcut("execution(* com.classroomapp.classroombackend.controller.AuthController.*(..))")
    public void authenticationMethods() {}

    @Pointcut("execution(* com.classroomapp.classroombackend.service..*Service.create*(..)) || " +
              "execution(* com.classroomapp.classroombackend.service..*Service.update*(..)) || " +
              "execution(* com.classroomapp.classroombackend.service..*Service.delete*(..)) || " +
              "execution(* com.classroomapp.classroombackend.service..*Service.save*(..))")
    public void crudOperations() {}

    @Around("controllerMethods()")
    public Object logControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        HttpServletRequest request = getCurrentRequest();
        String ipAddress = getClientIpAddress(request);
        String userAgent = request != null ? request.getHeader("User-Agent") : null;
        String requestUrl = request != null ? request.getRequestURL().toString() : null;
        String requestMethod = request != null ? request.getMethod() : null;

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            logApiCall(className, methodName, joinPoint.getArgs(), result,
                    ipAddress, userAgent, requestUrl, requestMethod, executionTime, true, null);

            return result;
        } catch (Exception ex) {
            long executionTime = System.currentTimeMillis() - startTime;

            logApiCall(className, methodName, joinPoint.getArgs(), null,
                    ipAddress, userAgent, requestUrl, requestMethod, executionTime, false, ex.getMessage());

            throw ex;
        }
    }

    @AfterReturning(pointcut = "authenticationMethods() && args(..)", returning = "result")
    public void logAuthenticationSuccess(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();

        AuditLog.AuditAction action = determineAuthAction(methodName, true);
        if (action != null) {
            createAuditLog(action, AuditLog.AuditCategory.AUTHENTICATION,
                    AuditLog.AuditSeverity.INFO,
                    String.format("Authentication operation: %s succeeded", methodName),
                    null, null, true);
        }
    }

    @AfterThrowing(pointcut = "authenticationMethods()", throwing = "exception")
    public void logAuthenticationFailure(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().getName();

        AuditLog.AuditAction action = determineAuthAction(methodName, false);
        if (action != null) {
            createAuditLog(action, AuditLog.AuditCategory.AUTHENTICATION,
                    AuditLog.AuditSeverity.WARN,
                    String.format("Authentication operation: %s failed", methodName),
                    null, exception.getMessage(), false);
        }
    }

    @AfterReturning(pointcut = "crudOperations() && args(..)", returning = "result")
    public void logCrudOperation(JoinPoint joinPoint, Object result) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String serviceName = joinPoint.getTarget().getClass().getSimpleName();

            AuditLog.AuditAction action = determineCrudAction(methodName);
            if (action != null) {
                String entityType = extractEntityType(serviceName);
                String entityId = extractEntityId(result);
                String entityName = extractEntityName(result);

                createAuditLog(action, AuditLog.AuditCategory.DATA_MODIFICATION,
                        AuditLog.AuditSeverity.INFO,
                        String.format("%s operation on %s completed successfully", action, entityType),
                        entityType, entityId, entityName, true);
            }
        } catch (Exception ex) {
            log.error("Error logging CRUD operation: {}", ex.getMessage(), ex);
        }
    }

    @AfterThrowing(pointcut = "crudOperations()", throwing = "exception")
    public void logCrudOperationFailure(JoinPoint joinPoint, Exception exception) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String serviceName = joinPoint.getTarget().getClass().getSimpleName();

            AuditLog.AuditAction action = determineCrudAction(methodName);
            if (action != null) {
                String entityType = extractEntityType(serviceName);

                createAuditLog(action, AuditLog.AuditCategory.DATA_MODIFICATION,
                        AuditLog.AuditSeverity.ERROR,
                        String.format("%s operation on %s failed", action, entityType),
                        entityType, null, null, exception.getMessage(), false);
            }
        } catch (Exception ex) {
            log.error("Error logging CRUD operation failure: {}", ex.getMessage(), ex);
        }
    }

    private void createAuditLog(AuditLog.AuditAction action, AuditLog.AuditCategory category,
                                 AuditLog.AuditSeverity severity, String description,
                                 String entityType, String errorMessage, boolean success) {
        createAuditLog(action, category, severity, description, entityType, null, null, errorMessage, success);
    }

    private void createAuditLog(AuditLog.AuditAction action, AuditLog.AuditCategory category,
                                 AuditLog.AuditSeverity severity, String description,
                                 String entityType, String entityId, String entityName, boolean success) {
        createAuditLog(action, category, severity, description, entityType, entityId, entityName, null, success);
    }

    private void createAuditLog(AuditLog.AuditAction action, AuditLog.AuditCategory category,
                                 AuditLog.AuditSeverity severity, String description,
                                 String entityType, String entityId, String entityName,
                                 String errorMessage, boolean success) {
        try {
            User currentUser = securityUtils.getCurrentUser();
            HttpServletRequest request = getCurrentRequest();

            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setCategory(category);
            auditLog.setSeverity(severity);
            auditLog.setDescription(description);
            auditLog.setSuccess(success);
            auditLog.setTimestamp(LocalDateTime.now());

            if (currentUser != null) {
                auditLog.setUser(currentUser);
                auditLog.setUsername(currentUser.getUsername());
            } else {
                auditLog.setUsername("anonymous");
            }

            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setRequestUrl(request.getRequestURL().toString());
                auditLog.setRequestMethod(request.getMethod());
                auditLog.setSessionId(request.getSession(false) != null ?
                        request.getSession(false).getId() : null);
            }

            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setEntityName(entityName);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setModule(inferModule(entityType, category));

            systemAdministrationService.createAuditLog(auditLog);
        } catch (Exception ex) {
            log.error("Failed to create audit log: {}", ex.getMessage(), ex);
        }
    }

    private void logApiCall(String className, String methodName, Object[] args, Object result,
                             String ipAddress, String userAgent, String requestUrl, String requestMethod,
                             long executionTime, boolean success, String errorMessage) {
        try {
            if (requestUrl != null && (requestUrl.contains("/health") || requestUrl.contains("/metrics"))) {
                return;
            }

            AuditLog auditLog = new AuditLog();
            auditLog.setAction(success ? AuditLog.AuditAction.READ : AuditLog.AuditAction.ERROR);
            auditLog.setCategory(AuditLog.AuditCategory.GENERAL);
            auditLog.setSeverity(success ? AuditLog.AuditSeverity.INFO : AuditLog.AuditSeverity.ERROR);
            auditLog.setDescription(String.format("API call: %s.%s", className, methodName));
            auditLog.setSuccess(success);
            auditLog.setTimestamp(LocalDateTime.now());

            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setRequestUrl(requestUrl);
            auditLog.setRequestMethod(requestMethod);
            auditLog.setExecutionTimeMs(executionTime);
            auditLog.setErrorMessage(errorMessage);

            User currentUser = securityUtils.getCurrentUser();
            if (currentUser != null) {
                auditLog.setUser(currentUser);
                auditLog.setUsername(currentUser.getUsername());
            }

            systemAdministrationService.createAuditLog(auditLog);
        } catch (Exception ex) {
            log.error("Failed to log API call: {}", ex.getMessage(), ex);
        }
    }

    private AuditLog.AuditAction determineAuthAction(String methodName, boolean success) {
        if (methodName.contains("login") || methodName.contains("authenticate")) {
            return success ? AuditLog.AuditAction.LOGIN : AuditLog.AuditAction.LOGIN_FAILED;
        } else if (methodName.contains("logout")) {
            return AuditLog.AuditAction.LOGOUT;
        } else if (methodName.contains("register") || methodName.contains("signup")) {
            return AuditLog.AuditAction.CREATE;
        }
        return null;
    }

    private AuditLog.AuditAction determineCrudAction(String methodName) {
        if (methodName.startsWith("create") || methodName.startsWith("save") || methodName.startsWith("add")) {
            return AuditLog.AuditAction.CREATE;
        } else if (methodName.startsWith("update") || methodName.startsWith("modify") || methodName.startsWith("edit")) {
            return AuditLog.AuditAction.UPDATE;
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return AuditLog.AuditAction.DELETE;
        }
        return null;
    }

    private String extractEntityType(String serviceName) {
        if (serviceName.endsWith("Service")) {
            return serviceName.substring(0, serviceName.length() - 7);
        }
        return serviceName;
    }

    private String extractEntityId(Object result) {
        if (result == null) return null;

        try {
            java.lang.reflect.Method getIdMethod = result.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(result);
            return id != null ? id.toString() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private String extractEntityName(Object result) {
        if (result == null) return null;

        try {
            String[] nameFields = {"getName", "getTitle", "getUsername", "getDisplayName"};
            for (String fieldMethod : nameFields) {
                try {
                    java.lang.reflect.Method method = result.getClass().getMethod(fieldMethod);
                    Object name = method.invoke(result);
                    if (name != null) {
                        return name.toString();
                    }
                } catch (Exception ex) {
                    // Ignore
                }
            }
        } catch (Exception ex) {
            // Ignore
        }

        return result.getClass().getSimpleName();
    }

    private String inferModule(String entityType, AuditLog.AuditCategory category) {
        if (entityType == null) return category.name();

        String lowerEntityType = entityType.toLowerCase();
        if (lowerEntityType.contains("user")) return "USER_MANAGEMENT";
        if (lowerEntityType.contains("classroom") || lowerEntityType.contains("course")) return "CLASSROOM_MANAGEMENT";
        if (lowerEntityType.contains("assignment")) return "ASSIGNMENT_MANAGEMENT";
        if (lowerEntityType.contains("grade")) return "GRADE_MANAGEMENT";
        if (lowerEntityType.contains("attendance")) return "ATTENDANCE_MANAGEMENT";
        if (lowerEntityType.contains("schedule")) return "SCHEDULE_MANAGEMENT";
        if (lowerEntityType.contains("material")) return "MATERIAL_MANAGEMENT";

        return category.name();
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception ex) {
            return null;
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) return null;

        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}
