package com.classroomapp.classroombackend.dto;

/**
 * Lớp DTO lưu trữ thông tin vị trí GPS gửi từ client
 */
public class LocationDataDto {
    private double latitude;    // Vĩ độ
    private double longitude;   // Kinh độ
    private double accuracy;    // Độ chính xác (tính bằng mét)

    /**
     * Constructor mặc định
     */
    public LocationDataDto() {
    }

    /**
     * Constructor với tham số
     * 
     * @param latitude Vĩ độ
     * @param longitude Kinh độ
     * @param accuracy Độ chính xác
     */
    public LocationDataDto(double latitude, double longitude, double accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }

    /**
     * Lấy vĩ độ
     * 
     * @return giá trị vĩ độ
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Thiết lập vĩ độ
     * 
     * @param latitude giá trị vĩ độ mới
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Lấy kinh độ
     * 
     * @return giá trị kinh độ
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Thiết lập kinh độ
     * 
     * @param longitude giá trị kinh độ mới
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Lấy độ chính xác
     * 
     * @return giá trị độ chính xác
     */
    public double getAccuracy() {
        return accuracy;
    }

    /**
     * Thiết lập độ chính xác
     * 
     * @param accuracy giá trị độ chính xác mới
     */
    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    public String toString() {
        return "LocationDataDto{" +
               "latitude=" + latitude +
               ", longitude=" + longitude +
               ", accuracy=" + accuracy +
               '}';
    }
} 