# Database Fixes Applied

## Issues Fixed

### 1. JSON Data Type Issue
**Problem**: SQL Server doesn't support the `JSON` data type directly, causing the error:
```
Column, parameter, or variable #6: Cannot find data type json.
```

**Solution**: Changed the column definition in `StudentQuizAnswer.java` from:
```java
@Column(name = "selected_options", columnDefinition = "JSON")
```
to:
```java
@Column(name = "selected_options", columnDefinition = "NVARCHAR(MAX)")
```

Also updated the corresponding SQL schema in `schema-extensions.sql`:
```sql
-- Before
selected_options JSON, -- For multiple choice answers

-- After  
selected_options NVARCHAR(MAX), -- For multiple choice answers
```

### 2. Table Creation Order Issue
**Problem**: Hibernate was trying to create foreign key constraints before the `student_quiz_answers` table existed.

**Solution**: Temporarily changed the DDL auto setting to `create-drop` to ensure all tables are created properly.

## Files Modified

1. `src/main/java/com/classroomapp/classroombackend/model/StudentQuizAnswer.java`
   - Changed JSON column definition to NVARCHAR(MAX)

2. `src/main/resources/schema-extensions.sql`
   - Updated student_quiz_answers table definition

3. `src/main/resources/application.properties`
   - Temporarily set `spring.jpa.hibernate.ddl-auto=create-drop`
   - Added additional Hibernate configuration for better table creation handling

## How to Run

1. **First Run** (creates all tables):
   ```bash
   # Run the application - this will create all tables
   mvn spring-boot:run
   ```

2. **After Successful First Run**:
   - Change `spring.jpa.hibernate.ddl-auto` back to `update` in `application.properties`
   - This prevents data loss on subsequent runs

3. **Alternative**: Use the provided batch script:
   ```bash
   start-backend-fixed.bat
   ```

## Notes

- The `NVARCHAR(MAX)` data type can store JSON strings up to 2GB in size
- The application will still work with JSON data - it's just stored as a string
- You can use Jackson or Gson to serialize/deserialize the JSON strings in your application code

## Verification

After running the application successfully, you should see:
- No more "Cannot find data type json" errors
- No more "Cannot find the object student_quiz_answers" errors
- All tables created successfully in SQL Server 