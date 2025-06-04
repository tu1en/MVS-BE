package com.classroomapp.classroombackend.dto;

public class CourseDetailResponse {
    private Long id;
    private String name;
    private String code;
    private String teacher;
    private String schedule;
    private String syllabus;

    public CourseDetailResponse(Long id, String name, String code, String teacher, String schedule, String syllabus) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.teacher = teacher;
        this.schedule = schedule;
        this.syllabus = syllabus;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getTeacher() { return teacher; }
    public String getSchedule() { return schedule; }
    public String getSyllabus() { return syllabus; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCode(String code) { this.code = code; }
    public void setTeacher(String teacher) { this.teacher = teacher; }
    public void setSchedule(String schedule) { this.schedule = schedule; }
    public void setSyllabus(String syllabus) { this.syllabus = syllabus; }
}
