package com.university.foreignstudent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.foreignstudent.entity.Attendance;
import com.university.foreignstudent.entity.Exam;
import com.university.foreignstudent.entity.StudentRecord;
import com.university.foreignstudent.mapper.AttendanceMapper;
import com.university.foreignstudent.mapper.ExamMapper;
import com.university.foreignstudent.mapper.StudentRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学籍管理服务类
 * 提供学籍、考勤、考试的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {
    
    private final StudentRecordMapper studentRecordMapper;
    private final AttendanceMapper attendanceMapper;
    private final ExamMapper examMapper;
    
    /**
     * 录入学籍信息
     * 
     * @param record 学籍记录
     * @return 创建的学籍记录
     */
    @Transactional
    public StudentRecord createStudentRecord(StudentRecord record) {
        log.info("录入学籍信息，学生ID: {}", record.getStudentId());
        
        // 卫语句：检查必填字段
        if (record.getStudentId() == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        if (record.getStudentNumber() == null || record.getStudentNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("学号不能为空");
        }
        if (record.getName() == null || record.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("姓名不能为空");
        }
        if (record.getEnrollmentDate() == null) {
            throw new IllegalArgumentException("入学日期不能为空");
        }
        
        // 卫语句：检查学号是否已存在
        LambdaQueryWrapper<StudentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentRecord::getStudentNumber, record.getStudentNumber());
        queryWrapper.eq(StudentRecord::getDeleted, 0);
        StudentRecord existingRecord = studentRecordMapper.selectOne(queryWrapper);
        if (existingRecord != null) {
            throw new IllegalArgumentException("学号已存在");
        }
        
        // 卫语句：检查学生是否已有学籍记录
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentRecord::getStudentId, record.getStudentId());
        queryWrapper.eq(StudentRecord::getDeleted, 0);
        existingRecord = studentRecordMapper.selectOne(queryWrapper);
        if (existingRecord != null) {
            throw new IllegalArgumentException("该学生已有学籍记录");
        }
        
        // 设置默认值
        if (record.getStatus() == null || record.getStatus().isEmpty()) {
            record.setStatus("ACTIVE");
        }
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setDeleted(0);
        
        studentRecordMapper.insert(record);
        log.info("学籍信息录入成功，学籍ID: {}", record.getId());
        return record;
    }
    
    /**
     * 根据ID获取学籍记录
     * 
     * @param id 学籍ID
     * @return 学籍记录
     */
    public StudentRecord getStudentRecordById(Long id) {
        LambdaQueryWrapper<StudentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentRecord::getId, id);
        queryWrapper.eq(StudentRecord::getDeleted, 0);
        return studentRecordMapper.selectOne(queryWrapper);
    }
    
    /**
     * 根据学生ID获取学籍记录
     * 
     * @param studentId 学生ID
     * @return 学籍记录
     */
    public StudentRecord getStudentRecordByStudentId(Long studentId) {
        LambdaQueryWrapper<StudentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentRecord::getStudentId, studentId);
        queryWrapper.eq(StudentRecord::getDeleted, 0);
        return studentRecordMapper.selectOne(queryWrapper);
    }
    
    /**
     * 获取所有学籍记录列表
     * 
     * @return 学籍记录列表
     */
    public List<StudentRecord> getAllStudentRecords() {
        LambdaQueryWrapper<StudentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentRecord::getDeleted, 0);
        queryWrapper.orderByDesc(StudentRecord::getCreateTime);
        return studentRecordMapper.selectList(queryWrapper);
    }
    
    /**
     * 更新学籍信息
     * 
     * @param record 学籍记录
     * @return 更新后的学籍记录
     */
    @Transactional
    public StudentRecord updateStudentRecord(StudentRecord record) {
        log.info("更新学籍信息，学籍ID: {}", record.getId());
        
        // 卫语句：检查记录是否存在
        StudentRecord existingRecord = getStudentRecordById(record.getId());
        if (existingRecord == null) {
            throw new IllegalArgumentException("学籍记录不存在");
        }
        
        // 卫语句：如果修改了学号，检查新学号是否已被使用
        if (record.getStudentNumber() != null && 
            !record.getStudentNumber().equals(existingRecord.getStudentNumber())) {
            LambdaQueryWrapper<StudentRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StudentRecord::getStudentNumber, record.getStudentNumber());
            queryWrapper.eq(StudentRecord::getDeleted, 0);
            queryWrapper.ne(StudentRecord::getId, record.getId());
            StudentRecord duplicateRecord = studentRecordMapper.selectOne(queryWrapper);
            if (duplicateRecord != null) {
                throw new IllegalArgumentException("学号已被使用");
            }
        }
        
        record.setUpdateTime(LocalDateTime.now());
        studentRecordMapper.updateById(record);
        log.info("学籍信息更新成功，学籍ID: {}", record.getId());
        return record;
    }
    
    /**
     * 删除学籍记录（逻辑删除）
     * 
     * @param id 学籍ID
     */
    @Transactional
    public void deleteStudentRecord(Long id) {
        log.info("删除学籍记录，学籍ID: {}", id);
        
        // 卫语句：检查记录是否存在
        StudentRecord record = getStudentRecordById(id);
        if (record == null) {
            throw new IllegalArgumentException("学籍记录不存在");
        }
        
        record.setDeleted(1);
        record.setUpdateTime(LocalDateTime.now());
        studentRecordMapper.updateById(record);
        log.info("学籍记录删除成功，学籍ID: {}", id);
    }
    
    /**
     * 添加考勤记录
     * 
     * @param attendance 考勤记录
     * @return 创建的考勤记录
     */
    @Transactional
    public Attendance createAttendance(Attendance attendance) {
        log.info("添加考勤记录，学生ID: {}", attendance.getStudentId());
        
        // 卫语句：检查必填字段
        if (attendance.getStudentId() == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        if (attendance.getCourseName() == null || attendance.getCourseName().trim().isEmpty()) {
            throw new IllegalArgumentException("课程名称不能为空");
        }
        if (attendance.getAttendanceDate() == null) {
            throw new IllegalArgumentException("考勤日期不能为空");
        }
        if (attendance.getStatus() == null || attendance.getStatus().isEmpty()) {
            throw new IllegalArgumentException("考勤状态不能为空");
        }
        
        // 卫语句：检查学生是否存在学籍记录
        StudentRecord record = getStudentRecordByStudentId(attendance.getStudentId());
        if (record == null) {
            throw new IllegalArgumentException("学生学籍记录不存在");
        }
        
        // 卫语句：检查考勤状态是否有效
        String status = attendance.getStatus();
        if (!status.equals("PRESENT") && !status.equals("ABSENT") && 
            !status.equals("LATE") && !status.equals("LEAVE")) {
            throw new IllegalArgumentException("无效的考勤状态");
        }
        
        attendance.setCreateTime(LocalDateTime.now());
        attendance.setUpdateTime(LocalDateTime.now());
        attendance.setDeleted(0);
        
        attendanceMapper.insert(attendance);
        log.info("考勤记录添加成功，考勤ID: {}", attendance.getId());
        return attendance;
    }
    
    /**
     * 根据学生ID获取考勤记录列表
     * 
     * @param studentId 学生ID
     * @return 考勤记录列表
     */
    public List<Attendance> getAttendancesByStudentId(Long studentId) {
        LambdaQueryWrapper<Attendance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Attendance::getStudentId, studentId);
        queryWrapper.eq(Attendance::getDeleted, 0);
        queryWrapper.orderByDesc(Attendance::getAttendanceDate);
        return attendanceMapper.selectList(queryWrapper);
    }
    
    /**
     * 更新考勤记录
     * 
     * @param attendance 考勤记录
     * @return 更新后的考勤记录
     */
    @Transactional
    public Attendance updateAttendance(Attendance attendance) {
        log.info("更新考勤记录，考勤ID: {}", attendance.getId());
        
        // 卫语句：检查记录是否存在
        Attendance existingAttendance = attendanceMapper.selectById(attendance.getId());
        if (existingAttendance == null || existingAttendance.getDeleted() == 1) {
            throw new IllegalArgumentException("考勤记录不存在");
        }
        
        attendance.setUpdateTime(LocalDateTime.now());
        attendanceMapper.updateById(attendance);
        log.info("考勤记录更新成功，考勤ID: {}", attendance.getId());
        return attendance;
    }
    
    /**
     * 添加考试成绩
     * 
     * @param exam 考试记录
     * @return 创建的考试记录
     */
    @Transactional
    public Exam createExam(Exam exam) {
        log.info("添加考试成绩，学生ID: {}", exam.getStudentId());
        
        // 卫语句：检查必填字段
        if (exam.getStudentId() == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        if (exam.getCourseName() == null || exam.getCourseName().trim().isEmpty()) {
            throw new IllegalArgumentException("课程名称不能为空");
        }
        if (exam.getExamDate() == null) {
            throw new IllegalArgumentException("考试日期不能为空");
        }
        if (exam.getScore() == null) {
            throw new IllegalArgumentException("成绩不能为空");
        }
        if (exam.getFullScore() == null) {
            throw new IllegalArgumentException("满分不能为空");
        }
        
        // 卫语句：检查学生是否存在学籍记录
        StudentRecord record = getStudentRecordByStudentId(exam.getStudentId());
        if (record == null) {
            throw new IllegalArgumentException("学生学籍记录不存在");
        }
        
        // 卫语句：检查成绩是否有效（不能为负数，不能超过满分）
        if (exam.getScore().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("成绩不能为负数");
        }
        if (exam.getScore().compareTo(exam.getFullScore()) > 0) {
            throw new IllegalArgumentException("成绩不能超过满分");
        }
        if (exam.getFullScore().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("满分必须大于0");
        }
        
        exam.setCreateTime(LocalDateTime.now());
        exam.setUpdateTime(LocalDateTime.now());
        exam.setDeleted(0);
        
        examMapper.insert(exam);
        log.info("考试成绩添加成功，考试ID: {}", exam.getId());
        return exam;
    }
    
    /**
     * 根据学生ID获取考试记录列表
     * 
     * @param studentId 学生ID
     * @return 考试记录列表
     */
    public List<Exam> getExamsByStudentId(Long studentId) {
        LambdaQueryWrapper<Exam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Exam::getStudentId, studentId);
        queryWrapper.eq(Exam::getDeleted, 0);
        queryWrapper.orderByDesc(Exam::getExamDate);
        return examMapper.selectList(queryWrapper);
    }
    
    /**
     * 更新考试记录
     * 
     * @param exam 考试记录
     * @return 更新后的考试记录
     */
    @Transactional
    public Exam updateExam(Exam exam) {
        log.info("更新考试记录，考试ID: {}", exam.getId());
        
        // 卫语句：检查记录是否存在
        Exam existingExam = examMapper.selectById(exam.getId());
        if (existingExam == null || existingExam.getDeleted() == 1) {
            throw new IllegalArgumentException("考试记录不存在");
        }
        
        // 卫语句：如果更新了成绩，检查成绩是否有效
        if (exam.getScore() != null && exam.getFullScore() != null) {
            if (exam.getScore().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("成绩不能为负数");
            }
            if (exam.getScore().compareTo(exam.getFullScore()) > 0) {
                throw new IllegalArgumentException("成绩不能超过满分");
            }
        }
        
        exam.setUpdateTime(LocalDateTime.now());
        examMapper.updateById(exam);
        log.info("考试记录更新成功，考试ID: {}", exam.getId());
        return exam;
    }
    
    /**
     * 删除考勤记录（逻辑删除）
     * 
     * @param id 考勤ID
     */
    @Transactional
    public void deleteAttendance(Long id) {
        log.info("删除考勤记录，考勤ID: {}", id);
        
        // 卫语句：检查记录是否存在
        Attendance attendance = attendanceMapper.selectById(id);
        if (attendance == null || attendance.getDeleted() == 1) {
            throw new IllegalArgumentException("考勤记录不存在");
        }
        
        attendance.setDeleted(1);
        attendance.setUpdateTime(LocalDateTime.now());
        attendanceMapper.updateById(attendance);
        log.info("考勤记录删除成功，考勤ID: {}", id);
    }
    
    /**
     * 删除考试记录（逻辑删除）
     * 
     * @param id 考试ID
     */
    @Transactional
    public void deleteExam(Long id) {
        log.info("删除考试记录，考试ID: {}", id);
        
        // 卫语句：检查记录是否存在
        Exam exam = examMapper.selectById(id);
        if (exam == null || exam.getDeleted() == 1) {
            throw new IllegalArgumentException("考试记录不存在");
        }
        
        exam.setDeleted(1);
        exam.setUpdateTime(LocalDateTime.now());
        examMapper.updateById(exam);
        log.info("考试记录删除成功，考试ID: {}", id);
    }
}

