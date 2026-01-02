package com.university.foreignstudent.controller;

import com.university.foreignstudent.dto.Result;
import com.university.foreignstudent.entity.Visas;
import com.university.foreignstudent.service.VisasService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 签证管理控制器
 * 提供签证相关的 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/visas")
@RequiredArgsConstructor
public class VisasController {
    
    private final VisasService visasService;
    
    /**
     * 获取所有签证记录列表
     * 
     * @return 签证记录列表
     */
    @GetMapping
    public Result<List<Visas>> getAllVisas() {
        try {
            List<Visas> visasList = visasService.getAllVisas();
            return Result.success(visasList);
        } catch (Exception e) {
            log.error("获取签证记录列表失败", e);
            return Result.error("获取签证记录列表失败");
        }
    }
    
    /**
     * 根据ID获取签证记录
     * 
     * @param id 签证ID
     * @return 签证记录
     */
    @GetMapping("/{id}")
    public Result<Visas> getVisasById(@PathVariable Long id) {
        try {
            Visas visas = visasService.getVisasById(id);
            if (visas == null) {
                return Result.error(404, "签证记录不存在");
            }
            return Result.success(visas);
        } catch (Exception e) {
            log.error("获取签证记录失败", e);
            return Result.error("获取签证记录失败");
        }
    }
    
    /**
     * 根据学生ID获取签证记录列表
     * 
     * @param studentId 学生ID
     * @return 签证记录列表
     */
    @GetMapping("/student/{studentId}")
    public Result<List<Visas>> getVisasByStudentId(@PathVariable Long studentId) {
        try {
            List<Visas> visasList = visasService.getVisasByStudentId(studentId);
            return Result.success(visasList);
        } catch (Exception e) {
            log.error("获取签证记录列表失败", e);
            return Result.error("获取签证记录列表失败");
        }
    }
    
    /**
     * 创建签证记录
     * 
     * @param visas 签证对象
     * @return 创建的签证记录
     */
    @PostMapping
    public Result<Visas> createVisas(@RequestBody Visas visas) {
        try {
            Visas createdVisas = visasService.createVisas(visas);
            return Result.success("签证记录创建成功", createdVisas);
        } catch (IllegalArgumentException e) {
            log.error("创建签证记录失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建签证记录异常", e);
            return Result.error("创建签证记录失败，请稍后重试");
        }
    }
    
    /**
     * 更新签证记录
     * 
     * @param id 签证ID
     * @param visas 签证对象
     * @return 更新后的签证记录
     */
    @PutMapping("/{id}")
    public Result<Visas> updateVisas(@PathVariable Long id, @RequestBody Visas visas) {
        try {
            visas.setId(id);
            Visas updatedVisas = visasService.updateVisas(visas);
            return Result.success("签证记录更新成功", updatedVisas);
        } catch (IllegalArgumentException e) {
            log.error("更新签证记录失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新签证记录异常", e);
            return Result.error("更新签证记录失败，请稍后重试");
        }
    }
    
    /**
     * 删除签证记录
     * 
     * @param id 签证ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteVisas(@PathVariable Long id) {
        try {
            visasService.deleteVisas(id);
            return Result.success("签证记录删除成功", null);
        } catch (IllegalArgumentException e) {
            log.error("删除签证记录失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除签证记录异常", e);
            return Result.error("删除签证记录失败，请稍后重试");
        }
    }
    
    /**
     * 获取即将过期的签证列表（用于提醒）
     * 
     * @param days 提前提醒天数（可选查询参数，默认使用配置值）
     * @return 即将过期的签证列表
     */
    @GetMapping("/expiring-soon")
    public Result<List<Visas>> getExpiringSoonVisas(@RequestParam(required = false) Integer days) {
        try {
            List<Visas> visasList = visasService.getExpiringSoonVisas(days);
            return Result.success(visasList);
        } catch (Exception e) {
            log.error("获取即将过期的签证列表失败", e);
            return Result.error("获取即将过期的签证列表失败");
        }
    }
    
    /**
     * 获取已过期的签证列表
     * 
     * @return 已过期的签证列表
     */
    @GetMapping("/expired")
    public Result<List<Visas>> getExpiredVisas() {
        try {
            List<Visas> visasList = visasService.getExpiredVisas();
            return Result.success(visasList);
        } catch (Exception e) {
            log.error("获取已过期的签证列表失败", e);
            return Result.error("获取已过期的签证列表失败");
        }
    }
}

