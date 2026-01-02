package com.university.foreignstudent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.foreignstudent.entity.Fees;
import com.university.foreignstudent.mapper.FeesMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收费管理服务类
 * 提供缴费相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeesService {
    
    private final FeesMapper feesMapper;
    
    /**
     * 创建费用记录
     * 
     * @param fees 费用对象
     * @return 创建的费用记录
     */
    @Transactional
    public Fees createFees(Fees fees) {
        log.info("创建费用记录，学生ID: {}, 费用类型: {}", fees.getStudentId(), fees.getFeeType());
        
        // 检查必填字段
        if (fees.getStudentId() == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        if (fees.getFeeType() == null || fees.getFeeType().trim().isEmpty()) {
            throw new IllegalArgumentException("费用类型不能为空");
        }
        if (fees.getAmount() == null || fees.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("费用金额必须大于0");
        }
        if (fees.getDueDate() == null) {
            throw new IllegalArgumentException("应缴日期不能为空");
        }
        
        // 设置默认值
        if (fees.getPaidAmount() == null) {
            fees.setPaidAmount(BigDecimal.ZERO);
        }
        if (fees.getStatus() == null || fees.getStatus().isEmpty()) {
            fees.setStatus("UNPAID");
        }
        
        // 更新状态（根据应缴日期和实缴金额）
        updateFeesStatus(fees);
        
        fees.setCreateTime(LocalDateTime.now());
        fees.setUpdateTime(LocalDateTime.now());
        fees.setDeleted(0);
        
        feesMapper.insert(fees);
        log.info("费用记录创建成功，费用ID: {}", fees.getId());
        return fees;
    }
    
    /**
     * 根据ID获取费用记录
     * 
     * @param id 费用ID
     * @return 费用记录
     */
    public Fees getFeesById(Long id) {
        LambdaQueryWrapper<Fees> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Fees::getId, id);
        queryWrapper.eq(Fees::getDeleted, 0);
        return feesMapper.selectOne(queryWrapper);
    }
    
    /**
     * 根据学生ID获取费用记录列表
     * 
     * @param studentId 学生ID
     * @return 费用记录列表
     */
    public List<Fees> getFeesByStudentId(Long studentId) {
        LambdaQueryWrapper<Fees> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Fees::getStudentId, studentId);
        queryWrapper.eq(Fees::getDeleted, 0);
        queryWrapper.orderByDesc(Fees::getDueDate);
        return feesMapper.selectList(queryWrapper);
    }
    
    /**
     * 获取所有费用记录列表
     * 
     * @return 费用记录列表
     */
    public List<Fees> getAllFees() {
        LambdaQueryWrapper<Fees> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Fees::getDeleted, 0);
        queryWrapper.orderByDesc(Fees::getCreateTime);
        return feesMapper.selectList(queryWrapper);
    }
    
    /**
     * 更新费用记录
     * 
     * @param fees 费用对象
     * @return 更新后的费用记录
     */
    @Transactional
    public Fees updateFees(Fees fees) {
        log.info("更新费用记录，费用ID: {}", fees.getId());
        
        Fees existingFees = getFeesById(fees.getId());
        if (existingFees == null) {
            throw new IllegalArgumentException("费用记录不存在");
        }
        
        // 更新状态
        updateFeesStatus(fees);
        
        fees.setUpdateTime(LocalDateTime.now());
        feesMapper.updateById(fees);
        log.info("费用记录更新成功，费用ID: {}", fees.getId());
        return fees;
    }
    
    /**
     * 计算学生的欠费总额
     * 
     * @param studentId 学生ID
     * @return 欠费信息（包含欠费总额和明细）
     */
    public Map<String, Object> calculateUnpaidFees(Long studentId) {
        log.info("计算学生欠费，学生ID: {}", studentId);
        
        List<Fees> feesList = getFeesByStudentId(studentId);
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalPaidAmount = BigDecimal.ZERO;
        BigDecimal totalUnpaidAmount = BigDecimal.ZERO;
        int unpaidCount = 0;
        int overdueCount = 0;
        
        LocalDate today = LocalDate.now();
        
        for (Fees fees : feesList) {
            totalAmount = totalAmount.add(fees.getAmount());
            totalPaidAmount = totalPaidAmount.add(fees.getPaidAmount() != null ? fees.getPaidAmount() : BigDecimal.ZERO);
            
            BigDecimal unpaid = fees.getAmount().subtract(fees.getPaidAmount() != null ? fees.getPaidAmount() : BigDecimal.ZERO);
            if (unpaid.compareTo(BigDecimal.ZERO) > 0) {
                totalUnpaidAmount = totalUnpaidAmount.add(unpaid);
                unpaidCount++;
                
                // 检查是否逾期
                if (fees.getDueDate() != null && fees.getDueDate().isBefore(today) && 
                    !"PAID".equals(fees.getStatus())) {
                    overdueCount++;
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("studentId", studentId);
        result.put("totalAmount", totalAmount);
        result.put("totalPaidAmount", totalPaidAmount);
        result.put("totalUnpaidAmount", totalUnpaidAmount);
        result.put("unpaidCount", unpaidCount);
        result.put("overdueCount", overdueCount);
        result.put("feesList", feesList);
        
        log.info("欠费计算完成，学生ID: {}, 欠费总额: {}", studentId, totalUnpaidAmount);
        return result;
    }
    
    /**
     * 标记费用为已缴（幂等操作）
     * 如果已经标记为已缴，重复调用不会重复更新
     * 
     * @param feesId 费用ID
     * @param paidAmount 实缴金额
     * @param paidDate 实缴日期（可选，默认为今天）
     * @return 更新后的费用记录
     */
    @Transactional
    public Fees markAsPaid(Long feesId, BigDecimal paidAmount, LocalDate paidDate) {
        log.info("标记费用为已缴，费用ID: {}, 实缴金额: {}", feesId, paidAmount);
        
        Fees fees = getFeesById(feesId);
        if (fees == null) {
            throw new IllegalArgumentException("费用记录不存在");
        }
        
        // 幂等性检查：如果已经是已缴状态且实缴金额等于费用金额，直接返回
        if ("PAID".equals(fees.getStatus()) && 
            fees.getPaidAmount() != null && 
            fees.getPaidAmount().compareTo(fees.getAmount()) >= 0) {
            log.info("费用已标记为已缴，跳过更新，费用ID: {}", feesId);
            return fees;
        }
        
        // 检查实缴金额
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("实缴金额必须大于0");
        }
        if (paidAmount.compareTo(fees.getAmount()) > 0) {
            throw new IllegalArgumentException("实缴金额不能超过费用金额");
        }
        
        // 更新实缴金额和日期
        BigDecimal currentPaidAmount = fees.getPaidAmount() != null ? fees.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal newPaidAmount = currentPaidAmount.add(paidAmount);
        
        // 如果实缴金额超过费用金额，只记录费用金额
        if (newPaidAmount.compareTo(fees.getAmount()) > 0) {
            newPaidAmount = fees.getAmount();
        }
        
        fees.setPaidAmount(newPaidAmount);
        fees.setPaidDate(paidDate != null ? paidDate : LocalDate.now());
        
        // 更新状态
        updateFeesStatus(fees);
        
        fees.setUpdateTime(LocalDateTime.now());
        feesMapper.updateById(fees);
        
        log.info("费用标记为已缴成功，费用ID: {}, 实缴金额: {}", feesId, newPaidAmount);
        return fees;
    }
    
    /**
     * 完全标记费用为已缴（一次性缴清，幂等操作）
     * 
     * @param feesId 费用ID
     * @param paidDate 实缴日期（可选，默认为今天）
     * @return 更新后的费用记录
     */
    @Transactional
    public Fees markAsFullyPaid(Long feesId, LocalDate paidDate) {
        log.info("标记费用为完全已缴，费用ID: {}", feesId);
        
        Fees fees = getFeesById(feesId);
        if (fees == null) {
            throw new IllegalArgumentException("费用记录不存在");
        }
        
        // 幂等性检查：如果已经是已缴状态，直接返回
        if ("PAID".equals(fees.getStatus()) && 
            fees.getPaidAmount() != null && 
            fees.getPaidAmount().compareTo(fees.getAmount()) >= 0) {
            log.info("费用已完全缴清，跳过更新，费用ID: {}", feesId);
            return fees;
        }
        
        // 设置为完全已缴
        fees.setPaidAmount(fees.getAmount());
        fees.setPaidDate(paidDate != null ? paidDate : LocalDate.now());
        fees.setStatus("PAID");
        
        fees.setUpdateTime(LocalDateTime.now());
        feesMapper.updateById(fees);
        
        log.info("费用标记为完全已缴成功，费用ID: {}", feesId);
        return fees;
    }
    
    /**
     * 更新费用状态（根据应缴日期和实缴金额）
     * 
     * @param fees 费用对象
     */
    private void updateFeesStatus(Fees fees) {
        if (fees.getAmount() == null || fees.getDueDate() == null) {
            return;
        }
        
        BigDecimal paidAmount = fees.getPaidAmount() != null ? fees.getPaidAmount() : BigDecimal.ZERO;
        LocalDate today = LocalDate.now();
        
        // 判断缴费状态
        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            // 未缴费
            if (fees.getDueDate().isBefore(today)) {
                fees.setStatus("OVERDUE");
            } else {
                fees.setStatus("UNPAID");
            }
        } else if (paidAmount.compareTo(fees.getAmount()) >= 0) {
            // 已完全缴费
            fees.setStatus("PAID");
        } else {
            // 部分缴费
            if (fees.getDueDate().isBefore(today)) {
                fees.setStatus("OVERDUE");
            } else {
                fees.setStatus("PARTIAL");
            }
        }
    }
    
    /**
     * 删除费用记录（逻辑删除）
     * 
     * @param id 费用ID
     */
    @Transactional
    public void deleteFees(Long id) {
        log.info("删除费用记录，费用ID: {}", id);
        
        Fees fees = getFeesById(id);
        if (fees == null) {
            throw new IllegalArgumentException("费用记录不存在");
        }
        
        fees.setDeleted(1);
        fees.setUpdateTime(LocalDateTime.now());
        feesMapper.updateById(fees);
        log.info("费用记录删除成功，费用ID: {}", id);
    }
}

