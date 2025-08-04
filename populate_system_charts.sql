-- SQL Script to Populate system_charts Table with Sample Data
-- Run this script against your SchoolManagementDB database

USE [SchoolManagementDB];
GO

-- Delete existing data
DELETE FROM [dbo].[system_charts];
GO

-- Reset identity column
DBCC CHECKIDENT ('[dbo].[system_charts]', RESEED, 1);
GO

-- Insert sample system charts
INSERT INTO [dbo].[system_charts] (title, description, chart_type, chart_data, chart_config, is_public, created_by, updated_by, created_at, updated_at)
VALUES
-- 1. Student enrollment by month - Bar Chart
(N'Thống kê số lượng học sinh theo tháng', 
 N'Số lượng học sinh mới đăng ký theo từng tháng trong năm học',
 'BAR_CHART',
 '{
  "labels": ["Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"],
  "datasets": [{
    "label": "Số học sinh",
    "data": [45, 89, 120, 156, 78, 34, 12, 235, 312, 289, 178, 98],
    "backgroundColor": "rgba(54, 162, 235, 0.8)",
    "borderColor": "rgba(54, 162, 235, 1)",
    "borderWidth": 1
  }]
}',
 '{
  "responsive": true,
  "plugins": {
    "legend": { "position": "top" },
    "title": { "display": true, "text": "Học sinh mới đăng ký theo tháng" }
  },
  "scales": {
    "y": { "beginAtZero": true }
  }
}',
 1, -- is_public
 'admin',
 'admin',
 GETDATE(),
 GETDATE()),

-- 2. Student attendance rate - Line Chart
(N'Tỷ lệ điểm danh học sinh', 
 N'Tỷ lệ điểm danh học sinh qua các tháng',
 'LINE_CHART',
 '{
  "labels": ["Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6"],
  "datasets": [{
    "label": "Tỷ lệ điểm danh (%)",
    "data": [85, 87, 92, 88, 94, 96],
    "fill": false,
    "borderColor": "rgba(75, 192, 192, 1)",
    "tension": 0.1
  }]
}',
 '{
  "responsive": true,
  "plugins": {
    "legend": { "position": "top" },
    "title": { "display": true, "text": "Tỷ lệ điểm danh học sinh" }
  },
  "scales": {
    "y": { 
      "beginAtZero": true,
      "max": 100
    }
  }
}',
 1,
 'admin',
 'admin',
 GETDATE(),
 GETDATE()),

-- 3. Course distribution - Pie Chart
(N'Phân bố khóa học', 
 N'Distribution of courses by category',
 'PIE_CHART',
 '{
  "labels": ["Công nghệ thông tin", "Kinh doanh", "Thiết kế", "Ngoại ngữ", "Marketing"],
  "datasets": [{
    "label": "Số khóa học",
    "data": [45, 30, 25, 20, 15],
    "backgroundColor": [
      "rgba(255, 99, 132, 0.8)",
      "rgba(54, 162, 235, 0.8)",
      "rgba(255, 206, 86, 0.8)",
      "rgba(75, 192, 192, 0.8)",
      "rgba(153, 102, 255, 0.8)"
    ],
    "borderWidth": 1
  }]
}',
 '{
  "responsive": true,
  "plugins": {
    "legend": { "position": "right" },
    "title": { "display": true, "text": "Phân bố khóa học theo lĩnh vực" }
  }
}',
 1,
 'admin',
 'admin',
 GETDATE(),
 GETDATE()),

-- 4. Revenue by quarter - Area Chart
(N'Doanh thu theo quý',
 N'Doanh thu hệ thống theo quý',
 'AREA_CHART',
 '{
  "labels": ["Q1/2023", "Q2/2023", "Q3/2023", "Q4/2023", "Q1/2024", "Q2/2024"],
  "datasets": [{
    "label": "Doanh thu (triệu VNĐ)",
    "data": [450, 520, 680, 890, 720, 1050],
    "fill": true,
    "backgroundColor": "rgba(255, 159, 64, 0.2)",
    "borderColor": "rgba(255, 159, 64, 1)",
    "tension": 0.4
  }]
}',
 '{
  "responsive": true,
  "plugins": {
    "legend": { "position": "top" },
    "title": { "display": true, "text": "Doanh thu theo quý" }
  },
  "scales": {
    "y": { beginAtZero: true }
  }
}',
 1,
 'admin',
 'admin',
 GETDATE(),
 GETDATE()),

-- 5. Teacher workload distribution - Doughnut Chart
(N'Khối lượng công việc giáo viên',
 N'Phân bố khối lượng công việc của giáo viên',
 'DOUGHNUT_CHART',
 '{
  "labels": ["Giảng dạy", "Chấm bài", "Tư vấn", "Phát triển khóa học", "Họp và báo cáo"],
  "datasets": [{
    "label": "Giờ làm việc",
    "data": [60, 25, 15, 20, 10],
    "backgroundColor": [
      "rgba(54, 162, 235, 0.8)",
      "rgba(255, 99, 132, 0.8)",
      "rgba(255, 205, 86, 0.8)",
      "rgba(75, 192, 192, 0.8)",
      "rgba(153, 102, 255, 0.8)"
    ]
  }]
}',
 '{
  "responsive": true,
  "cutout": "50%",
  "plugins": {
    "legend": { "position": "bottom" },
    "title": { "display": true, "text": "Phân bố thời gian làm việc giáo viên" }
  }
}',
 1,
 'admin',
 'admin',
 GETDATE(),
 GETDATE()),

-- 6. Assignment submission rate by month - Bar Chart
(N'Tỷ lệ nộp bài tập theo tháng',
 N'Phần trăm học sinh nộp bài tập đúng hạn',
 'BAR_CHART',
 '{
  "labels": ["Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6"],
  "datasets": [{
    "label": "Tỷ lệ (%)",
    "data": [78, 82, 85, 90, 87, 93],
    "backgroundColor": "rgba(153, 102, 255, 0.8)",
    "borderColor": "rgba(153, 102, 255, 1)",
    "borderWidth": 1
  }]
}',
 '{
  "responsive": true,
  "plugins": {
    "legend": { "position": "top" },
    "title": { "display": true, "text": "Tỷ lệ nộp bài tập đúng hạn" }
  },
  "scales": {
    "y": { beginAtZero: true, max: 100 }
  }
}',
 1,
 'admin',
 'admin',
 GETDATE(),
 GETDATE()),

-- 7. System login activity - Line Chart
(N'Hoạt động đăng nhập hệ thống',
 N'Số lượng đăng nhập vào hệ thống theo tuần',
 'LINE_CHART',
 '{
  "labels": ["Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4", "Tuần 5", "Tuần 6"],
  "datasets": [{
    "label": "Số lần đăng nhập",
    "data": [1250, 1380, 1520, 1490, 1680, 1820],
    "fill": false,
    "backgroundColor": "rgba(255, 99, 132, 0.2)",
    "borderColor": "rgba(255, 99, 132, 1)",
    "tension": 0.1
  }]
}',
 '{
  "responsive": true,
  "plugins": {
    "legend": { "position": "top" },
    "title": { "display": true, "text": "Lượt đăng nhập hệ thống" }
  },
  "scales": {
    "y": { beginAtZero: true }
  }
}',
 1,
 'admin',
 'admin',
 GETDATE(),
 GETDATE()),

-- 8. Grade distribution - Pie Chart
(N'Phân bố điểm số học sinh',
 N'Phân loại điểm học sinh',
 'PIE_CHART',
 '{
  "labels": ["Xuất sắc (9-10)", "Giỏi (8-8.9)", "Khá (6.5-7.9)", "Trung bình (5-6.4)", "Yếu (<5)"],
  "datasets": [{
    "label": "Số học sinh",
    "data": [45, 89, 156, 78, 23],
    "backgroundColor": [
      "rgba(46, 204, 113, 0.8)",
      "rgba(52, 152, 219, 0.8)",
      "rgba(155, 89, 182, 0.8)",
      "rgba(230, 126, 34, 0.8)",
      "rgba(231, 76, 60, 0.8)"
    ]
  }]
}',
 '{
  "responsive": true,
  "plugins": {
    "legend": { "position": "right" },
    "title": { "display": true, "text": "Phân loại học lực học sinh" }
  }
}',
 1,
 'admin',
 'admin',
 GETDATE(),
 GETDATE()),

-- 9. Revenue by course category - Bar Chart
(N'Doanh thu theo loại khóa học',
 N'Doanh thu từ các loại khóa học khác nhau',
 'BAR_CHART',
 '{
  "labels": ["Web Development", "Mobile Development", "Data Science", "AI/ML", "DevOps"],
  "datasets": [{
    "label": "Doanh thu (triệu VNĐ)",
    "data": [320, 450, 680, 920, 275],
    "backgroundColor": "rgba(255, 159, 64, 0.8)",
    "borderColor": "rgba(255, 159, 64, 1)",
    "borderWidth": 1
  }]
}',
 '{
  "responsive": true,
  "plugins": {
    "legend": { "position": "top" },
    "title": { "display": true, "text": "Doanh thu theo loại khóa học" }
  },
  "scales": {
    "y": { beginAtZero: true }
  }
}',
 1,
 'admin',
 'admin',
 GETDATE(),
 GETDATE()),

-- 10. Private dashboard - not visible to users
(N'Thống kê nội bộ',
 N'Dashboard thống kê nội bộ không công khai',
 'BAR_CHART',
 '{
  "labels": ["Server 1", "Server 2", "Server 3", "Cloud"],
  "datasets": [{
    "label": "Server Usage",
    "data": [65, 78, 43, 92],
    "backgroundColor": "rgba(128, 128, 128, 0.8)"
  }]
}',
 '{
  "responsive": true,
  "plugins": {
    "legend": { "position": "top" },
    "title": { "display": true, "text": "Server Usage" }
  }
}',
 0, -- not public
 'admin',
 'admin',
 GETDATE(),
 GETDATE());

SELECT * FROM [dbo].[system_charts];

-- Check the results
PRINT CONCAT('Total charts inserted: ', @@ROWCOUNT);
GO