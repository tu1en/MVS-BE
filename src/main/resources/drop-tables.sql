-- Drop tables if they exist (in reverse order to handle foreign keys)
IF OBJECT_ID('submissions', 'U') IS NOT NULL DROP TABLE submissions;
IF OBJECT_ID('assignments', 'U') IS NOT NULL DROP TABLE assignments;
IF OBJECT_ID('classroom_enrollments', 'U') IS NOT NULL DROP TABLE classroom_enrollments;
IF OBJECT_ID('classrooms', 'U') IS NOT NULL DROP TABLE classrooms;
IF OBJECT_ID('users', 'U') IS NOT NULL DROP TABLE users;
