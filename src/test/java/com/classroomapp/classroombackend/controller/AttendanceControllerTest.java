package com.classroomapp.classroombackend.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.classroomapp.classroombackend.dto.ApiResponse;
import com.classroomapp.classroombackend.dto.LocationDataDto;
import com.classroomapp.classroombackend.repository.AttendanceRepository;
import com.classroomapp.classroombackend.repository.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.UserRepository;
import com.classroomapp.classroombackend.service.AttendanceService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Lớp kiểm thử cho AttendanceController
 */
@ExtendWith(MockitoExtension.class)
public class AttendanceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttendanceService attendanceService;
    
    @Mock
    private AttendanceRepository attendanceRepository;
    
    @Mock
    private AttendanceSessionRepository sessionRepository;
    
    @Mock
    private UserRepository userRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Thiết lập trước mỗi test case
     */
    @BeforeEach
    void setUp() {
        AttendanceController attendanceController = new AttendanceController(
            attendanceService,
            attendanceRepository,
            sessionRepository,
            userRepository
        );
        mockMvc = MockMvcBuilders.standaloneSetup(attendanceController).build();
    }

    /**
     * Kiểm tra trường hợp điểm danh thành công
     */
    @Test
    void TestSuccessfulCheckIn() throws Exception {
        // Chuẩn bị dữ liệu kiểm thử
        LocationDataDto locationData = new LocationDataDto(21.028511, 105.804817, 50.0);
        ApiResponse expectedResponse = new ApiResponse(true, "Điểm danh thành công!");

        // Thiết lập mock
        when(attendanceService.PerformCheckInLogic(eq("teacher_demo_user"), any(LocationDataDto.class), eq("127.0.0.1")))
                .thenReturn(expectedResponse);

        // Thực thi request và kiểm tra
        mockMvc.perform(post("/api/attendance/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Điểm danh thành công!"));
    }

    /**
     * Kiểm tra trường hợp dữ liệu vị trí không hợp lệ
     */
    @Test
    void TestInvalidLocationData() throws Exception {
        // Chuẩn bị dữ liệu không hợp lệ (độ chính xác âm)
        LocationDataDto invalidLocationData = new LocationDataDto(21.028511, 105.804817, -10.0);

        // Thực thi request và kiểm tra
        mockMvc.perform(post("/api/attendance/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLocationData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Dữ liệu vị trí không hợp lệ."));
    }

    /**
     * Kiểm tra trường hợp điểm danh thất bại
     */
    @Test
    void TestFailedCheckIn() throws Exception {
        // Chuẩn bị dữ liệu kiểm thử
        LocationDataDto locationData = new LocationDataDto(21.028511, 105.804817, 50.0);
        ApiResponse failureResponse = new ApiResponse(false, "Vị trí nằm ngoài khu vực cho phép.");

        // Thiết lập mock
        when(attendanceService.PerformCheckInLogic(eq("teacher_demo_user"), any(LocationDataDto.class), eq("127.0.0.1")))
                .thenReturn(failureResponse);

        // Thực thi request và kiểm tra
        mockMvc.perform(post("/api/attendance/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Vị trí nằm ngoài khu vực cho phép."));
    }

    /**
     * Kiểm tra trường hợp service ném ngoại lệ
     */
    @Test
    void TestServiceThrowsException() throws Exception {
        // Chuẩn bị dữ liệu kiểm thử
        LocationDataDto locationData = new LocationDataDto(21.028511, 105.804817, 50.0);

        // Thiết lập mock để ném ngoại lệ
        when(attendanceService.PerformCheckInLogic(eq("teacher_demo_user"), any(LocationDataDto.class), eq("127.0.0.1")))
                .thenThrow(new RuntimeException("Lỗi bất ngờ"));

        // Thực thi request và kiểm tra
        mockMvc.perform(post("/api/attendance/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationData)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Đã xảy ra lỗi máy chủ nội bộ. Vui lòng thử lại sau."));
    }
} 