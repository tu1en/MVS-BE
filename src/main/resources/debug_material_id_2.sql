-- Debug script để kiểm tra tài liệu ID 2
-- Kiểm tra xem tài liệu ID 2 có tồn tại không
SELECT 
    id,
    title,
    description,
    file_path,
    file_name,
    file_size,
    file_type,
    upload_date,
    classroom_id,
    uploaded_by,
    is_public,
    download_count
FROM course_materials 
WHERE id = 2;

-- Kiểm tra tất cả tài liệu có sẵn
SELECT 
    id,
    title,
    file_name,
    file_path,
    classroom_id
FROM course_materials 
ORDER BY id;

-- Nếu không có tài liệu ID 2, tạo một tài liệu test
INSERT INTO course_materials (
    id,
    title, 
    description, 
    file_path, 
    file_name, 
    file_size, 
    file_type, 
    upload_date, 
    classroom_id, 
    uploaded_by, 
    is_public, 
    download_count, 
    version_number
) 
SELECT 2, 
       'Tài liệu test ID 2', 
       'Tài liệu test để kiểm tra chức năng tải xuống', 
       '/uploads/materials/1/exercises.docx', 
       'exercises.docx', 
       1024, 
       'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 
       NOW(), 
       1, 
       2, 
       true, 
       0, 
       1
WHERE NOT EXISTS (SELECT 1 FROM course_materials WHERE id = 2);

-- Kiểm tra lại sau khi insert
SELECT 
    id,
    title,
    description,
    file_path,
    file_name,
    file_size,
    file_type,
    classroom_id,
    uploaded_by
FROM course_materials 
WHERE id = 2;
