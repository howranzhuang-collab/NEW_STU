package com.university.foreignstudent.controller;

import com.university.foreignstudent.dto.Result;
import com.university.foreignstudent.entity.Alumni;
import com.university.foreignstudent.service.AlumniService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 校友管理控制器
 * 提供校友相关的 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/alumni")
@RequiredArgsConstructor
public class AlumniController {
    
    private final AlumniService alumniService;
    
    /**
     * 获取所有校友记录列表
     * 
     * @return 校友记录列表
     */
    @GetMapping
    public Result<List<Alumni>> getAllAlumni() {
        try {
            List<Alumni> alumniList = alumniService.getAllAlumni();
            return Result.success(alumniList);
        } catch (Exception e) {
            log.error("获取校友记录列表失败", e);
            return Result.error("获取校友记录列表失败");
        }
    }
    
    /**
     * 根据ID获取校友记录
     * 
     * @param id 校友ID
     * @return 校友记录
     */
    @GetMapping("/{id}")
    public Result<Alumni> getAlumniById(@PathVariable Long id) {
        try {
            Alumni alumni = alumniService.getAlumniById(id);
            if (alumni == null) {
                return Result.error(404, "校友记录不存在");
            }
            return Result.success(alumni);
        } catch (Exception e) {
            log.error("获取校友记录失败", e);
            return Result.error("获取校友记录失败");
        }
    }
    
    /**
     * 根据学生ID获取校友记录
     * 
     * @param studentId 学生ID
     * @return 校友记录
     */
    @GetMapping("/student/{studentId}")
    public Result<Alumni> getAlumniByStudentId(@PathVariable Long studentId) {
        try {
            Alumni alumni = alumniService.getAlumniByStudentId(studentId);
            if (alumni == null) {
                return Result.error(404, "校友记录不存在");
            }
            return Result.success(alumni);
        } catch (Exception e) {
            log.error("获取校友记录失败", e);
            return Result.error("获取校友记录失败");
        }
    }
    
    /**
     * 创建校友记录
     * 
     * @param alumni 校友对象
     * @return 创建的校友记录
     */
    @PostMapping
    public Result<Alumni> createAlumni(@RequestBody Alumni alumni) {
        try {
            Alumni createdAlumni = alumniService.createAlumni(alumni);
            return Result.success("校友记录创建成功", createdAlumni);
        } catch (IllegalArgumentException e) {
            log.error("创建校友记录失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建校友记录异常", e);
            return Result.error("创建校友记录失败，请稍后重试");
        }
    }
    
    /**
     * 从学籍记录同步创建校友记录（毕业同步）
     * 
     * @param request 包含 studentId、graduationDate、degree、status 等的请求体
     * @return 创建的校友记录
     */
    @PostMapping("/sync")
    public Result<Alumni> syncFromStudentRecord(@RequestBody Map<String, Object> request) {
        try {
            Long studentId = Long.valueOf(request.get("studentId").toString());
            
            Object graduationDateObj = request.get("graduationDate");
            LocalDate graduationDate = graduationDateObj instanceof String 
                ? LocalDate.parse((String) graduationDateObj) 
                : null;
            
            if (graduationDate == null) {
                return Result.error("毕业日期不能为空");
            }
            
            String degree = (String) request.get("degree");
            String status = (String) request.get("status");
            String organization = (String) request.get("organization");
            String position = (String) request.get("position");
            String contact = (String) request.get("contact");
            
            Alumni alumni = alumniService.syncFromStudentRecord(
                studentId, graduationDate, degree, status, 
                organization, position, contact
            );
            return Result.success("校友记录同步创建成功", alumni);
        } catch (IllegalArgumentException e) {
            log.error("同步校友记录失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("同步校友记录异常", e);
            return Result.error("同步校友记录失败，请稍后重试");
        }
    }
    
    /**
     * 更新校友记录
     * 
     * @param id 校友ID
     * @param alumni 校友对象
     * @return 更新后的校友记录
     */
    @PutMapping("/{id}")
    public Result<Alumni> updateAlumni(@PathVariable Long id, @RequestBody Alumni alumni) {
        try {
            alumni.setId(id);
            Alumni updatedAlumni = alumniService.updateAlumni(alumni);
            return Result.success("校友记录更新成功", updatedAlumni);
        } catch (IllegalArgumentException e) {
            log.error("更新校友记录失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新校友记录异常", e);
            return Result.error("更新校友记录失败，请稍后重试");
        }
    }
    
    /**
     * 删除校友记录
     * 
     * @param id 校友ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAlumni(@PathVariable Long id) {
        try {
            alumniService.deleteAlumni(id);
            return Result.success("校友记录删除成功", null);
        } catch (IllegalArgumentException e) {
            log.error("删除校友记录失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除校友记录异常", e);
            return Result.error("删除校友记录失败，请稍后重试");
        }
    }
}

