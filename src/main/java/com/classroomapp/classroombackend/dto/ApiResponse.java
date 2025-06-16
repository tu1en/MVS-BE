package com.classroomapp.classroombackend.dto;

/**
 * Lớp mẫu chung cho tất cả các phản hồi API
 */
public class ApiResponse {
    private boolean success; // Trạng thái thành công hay thất bại
    private String message;  // Thông điệp phản hồi
    private Object data;     // Dữ liệu bổ sung (nếu có)

    /**
     * Constructor với trạng thái và thông điệp
     * 
     * @param success trạng thái thành công
     * @param message thông điệp phản hồi
     */
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null; // Mặc định không có dữ liệu bổ sung
    }

    /**
     * Constructor với trạng thái, thông điệp và dữ liệu
     * 
     * @param success trạng thái thành công
     * @param message thông điệp phản hồi
     * @param data dữ liệu bổ sung
     */
    public ApiResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * Kiểm tra trạng thái thành công
     * 
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Thiết lập trạng thái thành công
     * 
     * @param success trạng thái mới
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Lấy thông điệp phản hồi
     * 
     * @return thông điệp phản hồi
     */
    public String getMessage() {
        return message;
    }

    /**
     * Thiết lập thông điệp phản hồi
     * 
     * @param message thông điệp phản hồi mới
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Lấy dữ liệu bổ sung
     * 
     * @return dữ liệu bổ sung
     */
    public Object getData() {
        return data;
    }

    /**
     * Thiết lập dữ liệu bổ sung
     * 
     * @param data dữ liệu bổ sung mới
     */
    public void setData(Object data) {
        this.data = data;
    }
} 