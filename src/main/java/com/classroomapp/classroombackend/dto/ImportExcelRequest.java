package com.classroomapp.classroombackend.dto;

import java.util.List;
import java.util.Map;

public class ImportExcelRequest {
    private List<Map<String, String>> data;
    
    public ImportExcelRequest() {}
    
    public ImportExcelRequest(List<Map<String, String>> data) {
        this.data = data;
    }
    
    public List<Map<String, String>> getData() {
        return data;
    }
    
    public void setData(List<Map<String, String>> data) {
        this.data = data;
    }
}