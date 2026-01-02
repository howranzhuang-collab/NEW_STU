package com.university.foreignstudent.controller;

import com.university.foreignstudent.dto.Result;
import com.university.foreignstudent.entity.Attendance;
import com.university.foreignstudent.entity.Exam;
import com.university.foreignstudent.entity.StudentRecord;
import com.university.foreignstudent.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学籍管理控制器
 * 提供学籍、考勤、考试的 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
    
    private final StudentService studentService;
    
    // ==================== 学籍记录相关接口 ====================
    
    /**
     * 获取所有学籍记录列表
     * 
     * @return 学籍记录列表
     */
    @GetMapping("/records")
    public Result<List<StudentRecord>> getAllStudentRecords() {
        try {
            List<StudentRecord> records = studentService.getAllStudentRecords();
            return Result.success(records);
        } catch (Exception e) {
            log.error("获取学籍记录列表失败", e);
            return Result.error("获取学籍记录列表失败");
        }
    }
    
    /**
     * 根据ID获取学籍记录
     * 
     * @param id 学籍ID
     * @return 学籍记录
     */
    @GetMapping("/records/{id}")
    public Result<StudentRecord> getStudentRecordById(@PathVariable Long id) {
        try {
            StudentRecord record = studentService.getStudentRecordById(id);
            if (record == null) {
                return Result.error(404, "学籍记录不存在");
            }
            return Result.success(record);
        } catch (Exception e) {
            log.error("获取学籍记录失败", e);
            return Result.error("获取学籍记录失败");
        }
    }
    
    /**
     * 根据学生ID获取学籍记录
     * 
     * @param studentId 学生ID
     * @return 学籍记录
     */
    @GetMapping("/records/student/{studentId}")
    public Result<StudentRecord> getStudentRecordByStudentId(@PathVariable Long studentId) {
        try {
            StudentRecord record = studentService.getStudentRecordByStudentId(studentId);
            if (record == null) {
                return Result.error(404, "学籍记录不存在");
            }
            return Result.success(record);
        } catch (Exception e) {
            log.error("获取学籍记录失败", e);
            return Result.error("获取学籍记录失败");
        }
    }
    
    /**
     * 录入学籍信息
     * 
     * @param record 学籍记录
     * @return 创建的学籍记录
     */
    @PostMapping("/records")
    public Result<StudentRecord> createStudentRecord(@RequestBody StudentRecord record) {
        try {
            StudentRecord createdRecord = studentService.createStudentRecord(record);
            return Result.success("学籍信息录入成功", createdRecord);
        } catch (IllegalArgumentException e) {
            log.error("录入学籍信息失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("录入学籍信息异常", e);
            return Result.error("录入学籍信息失败，请稍后重试");
        }
    }
    
    /**
     * 更新学籍信息
     * 
     * @param id 学籍ID
     * @param record 学籍记录
     * @return 更新后的学籍记录
     */
    @PutMapping("/records/{id}")
    public Result<StudentRecord> updateStudentRecord(@PathVariable Long id, @RequestBody StudentRecord record) {
        try {
            record.setId(id);
            StudentRecord updatedRecord = studentService.updateStudentRecord(record);
            return Result.success("学籍信息更新成功", updatedRecord);
        } catch (IllegalArgumentException e) {
            log.error("更新学籍信息失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新学籍信息异常", e);
            return Result.error("更新学籍信息失败，请稍后重试");
        }
    }
    
    /**
     * 删除学籍记录
     * 
     * @param id 学籍ID
     * @return 删除结果
     */
    @DeleteMapping("/records/{id}")
    public Result<Void> deleteStudentRecord(@PathVariable Long id) {
        try {
            studentService.deleteStudentRecord(id);
            return Result.success("学籍记录删除成功", null);
        } catch (IllegalArgumentException e) {
            log.error("删除学籍记录失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除学籍记录异常", e);
            return Result.error("删除学籍记录失败，请稍后重试");
        }
    }
    
    // ==================== 考勤相关接口 ====================
    
    /**
     * 添加考勤记录
     * 
     * @param attendance 考勤记录
     * @return 创建的考勤记录
     */
    @PostMapping("/attendances")
    public Result<Attendance> createAttendance(@RequestBody Attendance attendance) {
        try {
            Attendance createdAttendance = studentService.createAttendance(attendance);
            return Result.success("考勤记录添加成功", createdAttendance);
        } catch (IllegalArgumentException e) {
            log.error("添加考勤记录失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("添加考勤记录异常", e);
            return Result.error("添加考勤记录失败，请稍后重试");
        }
    }
    
    /**
     * 根据学生ID获取考勤记录列表
     * 
     * @param studentId 学生ID
     * @return 考勤记录列表
     */
    @GetMapping("/attendances/student/{studentId}")
    public Result<List<Attendance>> getAttendancesByStudentId(@PathVariable Long studentId) {
        try {
            List<Attendance> attendances = studentService.getAttendancesByStudentId(studentId);
            return Result.success(attendances);
        } catch (Exception e) {
            log.error("获取考勤记录列表失败", e);
            return Result.error("获取考勤记录列表失败");
        }
    }
    
    /**
     * 更新考勤记录
     * 
     * @param id 考勤ID
     * @param attendance 考勤记录
     * @return 更新后的考勤记录
     */
    @PutMapping("/attendances/{id}")
    public Result<Attendance> updateAttendance(@PathVariable Long id, @RequestBody Attendance attendance) {
        try {
            attendance.setId(id);
            Attendance updatedAttendance = studentService.updateAttendance(attendance);
            return Result.success("考勤记录更新成功", updatedAttendance);
        } catch (IllegalArgumentException e) {
            log.error("更新考勤记录失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新考勤记录异常", e);
            return Result.error("更新考勤记录失败，请稍后重试");
        }
    }
    
    /**
     * 删除考勤记录
     * 
     * @param id 考勤ID
     * @return 删除结果
     */
    @DeleteMapping("/attendances/{id}")
    public Result<Void> deleteAttendance(@PathVariable Long id) {
        try {
            studentService.deleteAttendance(id);
            return Result.success("考勤记录删除成功", null);
        } catch (IllegalArgumentException e) {
            log.error("删除考勤记录失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除考勤记录异常", e);
            return Result.error("删除考勤记录失败，请稍后重试");
        }
    }
    
    // ==================== 考试相关接口 ====================
    
    /**
     * 添加考试成绩
     * 
     * @param exam 考试记录
     * @return 创建的考试记录
     */
    @PostMapping("/exams")
    public Result<Exam> createExam(@RequestBody Exam exam) {
        try {
            Exam createdExam = studentService.createExam(exam);
            return Result.success("考试成绩添加成功", createdExam);
        } catch (IllegalArgumentException e) {
            log.error("添加考试成绩失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("添加考试成绩异常", e);
            return Result.error("添加考试成绩失败，请稍后重试");
        }
    }
    
    /**
     * 根据学生ID获取考试记录列表
     * 
     * @param studentId 学生ID
     * @return 考试记录列表
     */
    @GetMapping("/exams/student/{studentId}")
    public Result<List<Exam>> getExamsByStudentId(@PathVariable Long studentId) {
        try {
            List<Exam> exams = studentService.getExamsByStudentId(studentId);
            return Result.success(exams);
        } catch (Exception e) {
            log.error("获取考试记录列表失败", e);
            return Result.error("获取考试记录列表失败");
        }
    }
    
    /**
     * 更新考试记录
     * 
     * @param id 考试ID
     * @param exam 考试记录
     * @return 更新后的考试记录
     */
    @PutMapping("/exams/{id}")
    public Result<Exam> updateExam(@PathVariable Long id, @RequestBody Exam exam) {
        try {
            exam.setId(id);
            Exam updatedExam = studentService.updateExam(exam);
            return Result.success("考试记录更新成功", updatedExam);
        } catch (IllegalArgumentException e) {
            log.error("更新考试记录失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新考试记录异常", e);
            return Result.error("更新考试记录失败，请稍后重试");
        }
    }
    
    /**
     * 删除考试记录
     * 
     * @param id 考试ID
     * @return 删除结果
     */
    @DeleteMapping("/exams/{id}")
    public Result<Void> deleteExam(@PathVariable Long id) {
        try {
            studentService.deleteExam(id);
            return Result.success("考试记录删除成功", null);
        } catch (IllegalArgumentException e) {
            log.error("删除考试记录失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除考试记录异常", e);
            return Result.error("删除考试记录失败，请稍后重试");
        }
    }
}

