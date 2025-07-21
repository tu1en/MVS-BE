package com.classroomapp.classroombackend.dto;

/**
 * Lớp mẫu chung cho tất cả các phản hồi API với generic type
 * @param <T> Kiểu dữ liệu của payload
 */
public class ApiResponse<T> {
    private boolean success; // Trạng thái thành công hay thất bại
    private String message;  // Thông điệp phản hồi
    private T data;     // Dữ liệu bổ sung (nếu có)

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
    public ApiResponse(boolean success, String message, T data) {
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
    public T getData() {
        return data;
    }

    /**
     * Thiết lập dữ liệu bổ sung
     * 
     * @param data dữ liệu bổ sung mới
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * Static factory method để tạo response thành công
     * 
     * @param data dữ liệu trả về
     * @param <T> kiểu dữ liệu
     * @return ApiResponse chứa data và thông điệp thành công
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Thành công", data);
    }

    /**
     * Static factory method để tạo response thành công với thông điệp tùy chỉnh
     * 
     * @param data dữ liệu trả về
     * @param message thông điệp tùy chỉnh
     * @param <T> kiểu dữ liệu
     * @return ApiResponse chứa data và thông điệp tùy chỉnh
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Static factory method để tạo response lỗi
     * 
     * @param message thông điệp lỗi
     * @param <T> kiểu dữ liệu
     * @return ApiResponse chứa thông điệp lỗi
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    /**
     * Static factory method để tạo response lỗi với dữ liệu tùy chỉnh
     * 
     * @param message thông điệp lỗi
     * @param data dữ liệu trả về tùy chỉnh (có thể là lỗi chi tiết)
     * @param <T> kiểu dữ liệu
     * @return ApiResponse chứa thông điệp lỗi và dữ liệu tùy chỉnh
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
} 