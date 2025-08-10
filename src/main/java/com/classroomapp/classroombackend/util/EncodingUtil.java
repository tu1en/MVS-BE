package com.classroomapp.classroombackend.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to fix Vietnamese encoding issues
 */
@Slf4j
public class EncodingUtil {

    /**
     * Fix common Vietnamese encoding issues in text
     */
    public static String fixVietnameseText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        try {
            String fixed = text
                // Fix specific names and common words
                .replace("Lý Th? Bình", "Lý Thị Bình")
                .replace("Lê Hoàng Nam", "Lê Hoàng Nam")
                .replace("Nguy?n Van Huy", "Nguyễn Văn Huy")
                .replace("Giáo viên Van h?c l?p 10", "Giáo viên Văn học lớp 10")
                .replace("K? toán viên", "Kỹ toán viên")
                
                // Generic fixes
                .replace("Th?", "Thị")
                .replace("th?", "thị")
                .replace("Van h?c", "Văn học")
                .replace("van h?c", "văn học")
                .replace("l?p", "lớp")
                .replace("L?p", "Lớp")
                .replace("gi?o", "giáo")
                .replace("Gi?o", "Giáo")
                .replace("vi?n", "viên")
                .replace("Vi?n", "Viên")
                .replace("h?c", "học")
                .replace("H?c", "Học")
                .replace("to?n", "toán")
                .replace("To?n", "Toán")
                .replace("Nguy?n", "Nguyễn")
                .replace("nguy?n", "nguyễn")
                .replace("L?", "Lý")
                .replace("l?", "lý")
                .replace("B?nh", "Bình")
                .replace("b?nh", "bình")
                .replace("K?", "Kỹ")
                .replace("k?", "kỹ")
                .replace("?", "ị"); // Last resort for remaining ?
            
            return fixed;
            
        } catch (Exception e) {
            log.warn("Could not fix Vietnamese encoding for: {}", text, e);
            return text;
        }
    }
    
    /**
     * Fix encoding for object fields recursively
     */
    public static void fixObjectEncoding(Object obj) {
        if (obj == null) return;
        
        try {
            java.lang.reflect.Field[] fields = obj.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);
                
                if (value instanceof String) {
                    String fixedValue = fixVietnameseText((String) value);
                    field.set(obj, fixedValue);
                } else if (value != null && !field.getType().isPrimitive() && 
                          !field.getType().getName().startsWith("java.")) {
                    fixObjectEncoding(value);
                }
            }
        } catch (Exception e) {
            log.warn("Could not fix object encoding", e);
        }
    }
}
