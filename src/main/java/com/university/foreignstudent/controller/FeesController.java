package com.university.foreignstudent.controller;

import com.university.foreignstudent.dto.Result;
import com.university.foreignstudent.entity.Fees;
import com.university.foreignstudent.service.FeesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 收费管理控制器
 * 提供缴费相关的 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
public class FeesController {
    
    private final FeesService feesService;
    
    /**
     * 获取所有费用记录列表
     * 
     * @return 费用记录列表
     */
    @GetMapping
    public Result<List<Fees>> getAllFees() {
        try {
            List<Fees> feesList = feesService.getAllFees();
            return Result.success(feesList);
        } catch (Exception e) {
            log.error("获取费用记录列表失败", e);
            return Result.error("获取费用记录列表失败");
        }
    }
    
    /**
     * 根据ID获取费用记录
     * 
     * @param id 费用ID
     * @return 费用记录
     */
    @GetMapping("/{id}")
    public Result<Fees> getFeesById(@PathVariable Long id) {
        try {
            Fees fees = feesService.getFeesById(id);
            if (fees == null) {
                return Result.error(404, "费用记录不存在");
            }
            return Result.success(fees);
        } catch (Exception e) {
            log.error("获取费用记录失败", e);
            return Result.error("获取费用记录失败");
        }
    }
    
    /**
     * 根据学生ID获取费用记录列表
     * 
     * @param studentId 学生ID
     * @return 费用记录列表
     */
    @GetMapping("/student/{studentId}")
    public Result<List<Fees>> getFeesByStudentId(@PathVariable Long studentId) {
        try {
            List<Fees> feesList = feesService.getFeesByStudentId(studentId);
            return Result.success(feesList);
        } catch (Exception e) {
            log.error("获取费用记录列表失败", e);
            return Result.error("获取费用记录列表失败");
        }
    }
    
    /**
     * 创建费用记录
     * 
     * @param fees 费用对象
     * @return 创建的费用记录
     */
    @PostMapping
    public Result<Fees> createFees(@RequestBody Fees fees) {
        try {
            Fees createdFees = feesService.createFees(fees);
            return Result.success("费用记录创建成功", createdFees);
        } catch (IllegalArgumentException e) {
            log.error("创建费用记录失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建费用记录异常", e);
            return Result.error("创建费用记录失败，请稍后重试");
        }
    }
    
    /**
     * 更新费用记录
     * 
     * @param id 费用ID
     * @param fees 费用对象
     * @return 更新后的费用记录
     */
    @PutMapping("/{id}")
    public Result<Fees> updateFees(@PathVariable Long id, @RequestBody Fees fees) {
        try {
            fees.setId(id);
            Fees updatedFees = feesService.updateFees(fees);
            return Result.success("费用记录更新成功", updatedFees);
        } catch (IllegalArgumentException e) {
            log.error("更新费用记录失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新费用记录异常", e);
            return Result.error("更新费用记录失败，请稍后重试");
        }
    }
    
    /**
     * 删除费用记录
     * 
     * @param id 费用ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFees(@PathVariable Long id) {
        try {
            feesService.deleteFees(id);
            return Result.success("费用记录删除成功", null);
        } catch (IllegalArgumentException e) {
            log.error("删除费用记录失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除费用记录异常", e);
            return Result.error("删除费用记录失败，请稍后重试");
        }
    }
    
    /**
     * 计算学生的欠费总额
     * 
     * @param studentId 学生ID
     * @return 欠费信息
     */
    @GetMapping("/student/{studentId}/unpaid")
    public Result<Map<String, Object>> calculateUnpaidFees(@PathVariable Long studentId) {
        try {
            Map<String, Object> result = feesService.calculateUnpaidFees(studentId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("计算欠费失败", e);
            return Result.error("计算欠费失败");
        }
    }
    
    /**
     * 标记费用为已缴（支持部分缴费，幂等操作）
     * 
     * @param feesId 费用ID
     * @param request 包含 paidAmount 和 paidDate 的请求体
     * @return 更新后的费用记录
     */
    @PostMapping("/{feesId}/pay")
    public Result<Fees> markAsPaid(
            @PathVariable Long feesId,
            @RequestBody Map<String, Object> request) {
        try {
            Object paidAmountObj = request.get("paidAmount");
            if (paidAmountObj == null) {
                return Result.error("实缴金额不能为空");
            }
            
            BigDecimal paidAmount;
            if (paidAmountObj instanceof Number) {
                paidAmount = BigDecimal.valueOf(((Number) paidAmountObj).doubleValue());
            } else if (paidAmountObj instanceof String) {
                paidAmount = new BigDecimal((String) paidAmountObj);
            } else {
                return Result.error("实缴金额格式错误");
            }
            
            LocalDate paidDate = null;
            Object paidDateObj = request.get("paidDate");
            if (paidDateObj != null) {
                if (paidDateObj instanceof String) {
                    paidDate = LocalDate.parse((String) paidDateObj);
                }
            }
            
            Fees fees = feesService.markAsPaid(feesId, paidAmount, paidDate);
            return Result.success("缴费成功", fees);
        } catch (IllegalArgumentException e) {
            log.error("标记费用为已缴失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("标记费用为已缴异常", e);
            return Result.error("标记费用为已缴失败，请稍后重试");
        }
    }
    
    /**
     * 完全标记费用为已缴（一次性缴清，幂等操作）
     * 
     * @param feesId 费用ID
     * @param request 包含 paidDate 的请求体（可选）
     * @return 更新后的费用记录
     */
    @PostMapping("/{feesId}/pay-full")
    public Result<Fees> markAsFullyPaid(
            @PathVariable Long feesId,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            LocalDate paidDate = null;
            if (request != null) {
                Object paidDateObj = request.get("paidDate");
                if (paidDateObj != null && paidDateObj instanceof String) {
                    paidDate = LocalDate.parse((String) paidDateObj);
                }
            }
            
            Fees fees = feesService.markAsFullyPaid(feesId, paidDate);
            return Result.success("费用已完全缴清", fees);
        } catch (IllegalArgumentException e) {
            log.error("标记费用为完全已缴失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("标记费用为完全已缴异常", e);
            return Result.error("标记费用为完全已缴失败，请稍后重试");
        }
    }
}

