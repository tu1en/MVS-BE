package com.classroomapp.classroombackend.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CourseDetailsDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CourseImportRequest;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseImportService {

    private final CourseService courseService;
    private final UserRepository userRepository;

    public CourseDetailsDto importCourseFromExcel(CourseImportRequest request) throws IOException {
        MultipartFile file = request.getFile();
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        // Skip header
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        List<String> studentEmails = new ArrayList<>();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Cell emailCell = row.getCell(0);
            if (emailCell != null && emailCell.getCellType() == CellType.STRING) {
                studentEmails.add(emailCell.getStringCellValue().trim());
            }
        }

        workbook.close();

        CourseDetailsDto courseDto = new CourseDetailsDto();
        courseDto.setName(request.getCourseName());
        courseDto.setDescription(request.getDescription());
        courseDto.setSection(request.getSection());
        courseDto.setSubject(request.getSubject());

        User teacher = userRepository.findById(request.getTeacherId())
            .orElseThrow(() -> new RuntimeException("Teacher not found"));
        courseDto.setTeacher(new UserDto(teacher));

        List<User> students = userRepository.findByEmailIn(studentEmails);
        courseDto.setStudents(students.stream().map(UserDto::new).toList());
        courseDto.setTotalStudents(students.size());

        return courseService.createCourseWithStudents(
            courseDto,
            students.stream().map(User::getId).toList()
        );
    }
}
