-- 来华留学生服务系统数据库初始化脚本
-- 数据库：foreign_student_db

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS foreign_student_db 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE foreign_student_db;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT '角色：ADMIN、STAFF、STUDENT',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_username (username),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 招生项目表
CREATE TABLE IF NOT EXISTS admission_projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '项目ID',
    title VARCHAR(200) NOT NULL COMMENT '项目标题',
    description TEXT COMMENT '项目描述',
    quota INT NOT NULL COMMENT '招生名额',
    applied_count INT NOT NULL DEFAULT 0 COMMENT '已报名人数',
    deadline DATE NOT NULL COMMENT '申请截止日期',
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT '项目状态：OPEN、CLOSED、ENDED',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_status (status),
    INDEX idx_deadline (deadline),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='招生项目表';

-- 申请表
CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请ID',
    project_id BIGINT NOT NULL COMMENT '招生项目ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '申请状态：PENDING、APPROVED、REJECTED',
    application_date DATE NOT NULL COMMENT '申请日期',
    file_path VARCHAR(500) COMMENT '上传文件路径',
    remarks TEXT COMMENT '备注信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_project_id (project_id),
    INDEX idx_student_id (student_id),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted),
    UNIQUE KEY uk_project_student (project_id, student_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='申请表';

-- 学籍记录表
CREATE TABLE IF NOT EXISTS student_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '学籍ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    student_number VARCHAR(50) NOT NULL UNIQUE COMMENT '学号',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    gender VARCHAR(10) COMMENT '性别：MALE、FEMALE',
    birth_date DATE COMMENT '出生日期',
    nationality VARCHAR(50) COMMENT '国籍',
    major VARCHAR(100) COMMENT '专业',
    grade VARCHAR(20) COMMENT '年级',
    enrollment_date DATE NOT NULL COMMENT '入学日期',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '学籍状态：ACTIVE、GRADUATED、SUSPENDED、DROPPED',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '电子邮箱',
    address VARCHAR(500) COMMENT '地址',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_student_id (student_id),
    INDEX idx_student_number (student_number),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted),
    UNIQUE KEY uk_student_id (student_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学籍记录表';

-- 考勤表
CREATE TABLE IF NOT EXISTS attendances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '考勤ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_name VARCHAR(200) NOT NULL COMMENT '课程名称',
    attendance_date DATE NOT NULL COMMENT '考勤日期',
    status VARCHAR(20) NOT NULL COMMENT '考勤状态：PRESENT、ABSENT、LATE、LEAVE',
    remarks TEXT COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_student_id (student_id),
    INDEX idx_attendance_date (attendance_date),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤表';

-- 考试表
CREATE TABLE IF NOT EXISTS exams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '考试ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_name VARCHAR(200) NOT NULL COMMENT '课程名称',
    exam_type VARCHAR(20) COMMENT '考试类型：MIDTERM、FINAL、QUIZ、ASSIGNMENT',
    exam_date DATE NOT NULL COMMENT '考试日期',
    score DECIMAL(5,2) NOT NULL COMMENT '成绩',
    full_score DECIMAL(5,2) NOT NULL COMMENT '满分',
    remarks TEXT COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_student_id (student_id),
    INDEX idx_exam_date (exam_date),
    INDEX idx_course_name (course_name),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试表';

-- 收费表
CREATE TABLE IF NOT EXISTS fees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '费用ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    fee_type VARCHAR(50) NOT NULL COMMENT '费用类型：TUITION、ACCOMMODATION、OTHER',
    amount DECIMAL(10,2) NOT NULL COMMENT '费用金额',
    due_date DATE NOT NULL COMMENT '应缴日期',
    paid_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '实缴金额',
    paid_date DATE COMMENT '实缴日期',
    status VARCHAR(20) NOT NULL DEFAULT 'UNPAID' COMMENT '缴费状态：UNPAID、PAID、PARTIAL、OVERDUE',
    remarks TEXT COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_student_id (student_id),
    INDEX idx_due_date (due_date),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收费表';

-- 宿舍楼表
CREATE TABLE IF NOT EXISTS dorm_buildings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '宿舍楼ID',
    name VARCHAR(100) NOT NULL COMMENT '宿舍楼名称',
    address VARCHAR(500) COMMENT '宿舍楼地址',
    total_floors INT COMMENT '总楼层数',
    total_rooms INT NOT NULL DEFAULT 0 COMMENT '总房间数',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE、MAINTENANCE、CLOSED',
    remarks TEXT COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宿舍楼表';

-- 宿舍房间表
CREATE TABLE IF NOT EXISTS dorm_rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '房间ID',
    building_id BIGINT NOT NULL COMMENT '宿舍楼ID',
    room_number VARCHAR(50) NOT NULL COMMENT '房间号',
    floor INT COMMENT '楼层',
    capacity INT NOT NULL COMMENT '房间容量（可住人数）',
    current_occupancy INT NOT NULL DEFAULT 0 COMMENT '当前入住人数',
    room_type VARCHAR(20) COMMENT '房间类型：SINGLE、DOUBLE、QUAD',
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态：AVAILABLE、FULL、MAINTENANCE',
    remarks TEXT COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_building_id (building_id),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted),
    UNIQUE KEY uk_building_room (building_id, room_number, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宿舍房间表';

-- 宿舍分配表
CREATE TABLE IF NOT EXISTS dorm_allocations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分配ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    room_id BIGINT NOT NULL COMMENT '房间ID',
    check_in_date DATE NOT NULL COMMENT '入住日期',
    check_out_date DATE COMMENT '退房日期',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '分配状态：ACTIVE、COMPLETED、CANCELLED',
    remarks TEXT COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_student_id (student_id),
    INDEX idx_room_id (room_id),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宿舍分配表';

-- 签证表
CREATE TABLE IF NOT EXISTS visas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '签证ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    visa_type VARCHAR(20) COMMENT '签证类型：X1、X2、F、L等',
    visa_number VARCHAR(100) NOT NULL UNIQUE COMMENT '签证号码',
    issue_date DATE COMMENT '签发日期',
    expiry_date DATE NOT NULL COMMENT '到期日期',
    entry_date DATE COMMENT '入境日期',
    status VARCHAR(20) NOT NULL DEFAULT 'VALID' COMMENT '签证状态：VALID、EXPIRED、EXPIRING_SOON、CANCELLED',
    issuing_authority VARCHAR(200) COMMENT '签发机构',
    remarks TEXT COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_student_id (student_id),
    INDEX idx_expiry_date (expiry_date),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签证表';

-- 校友表
CREATE TABLE IF NOT EXISTS alumni (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '校友ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    student_number VARCHAR(50) COMMENT '学号',
    major VARCHAR(100) COMMENT '专业',
    graduation_date DATE NOT NULL COMMENT '毕业日期',
    degree VARCHAR(20) COMMENT '学位：BACHELOR、MASTER、DOCTOR',
    status VARCHAR(20) COMMENT '毕业去向：EMPLOYED、STUDYING、OTHER',
    organization VARCHAR(200) COMMENT '工作单位/继续深造学校',
    position VARCHAR(200) COMMENT '职位/专业',
    contact VARCHAR(200) COMMENT '联系方式（邮箱/电话）',
    remarks TEXT COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_student_id (student_id),
    INDEX idx_graduation_date (graduation_date),
    INDEX idx_deleted (deleted),
    UNIQUE KEY uk_student_id (student_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校友表';

