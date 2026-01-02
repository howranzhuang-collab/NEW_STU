package com.university.foreignstudent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.foreignstudent.entity.Visas;
import com.university.foreignstudent.mapper.VisasMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 签证管理服务类
 * 提供签证相关的业务逻辑，包括到期计算
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VisasService {
    
    private final VisasMapper visasMapper;
    
    /**
     * 提前提醒天数（默认30天）
     */
    @Value("${visa.expiring-soon-days:30}")
    private Integer expiringSoonDays;
    
    /**
     * 创建签证记录
     * 
     * @param visas 签证对象
     * @return 创建的签证记录
     */
    @Transactional
    public Visas createVisas(Visas visas) {
        log.info("创建签证记录，学生ID: {}, 签证号: {}", visas.getStudentId(), visas.getVisaNumber());
        
        // 检查必填字段
        if (visas.getStudentId() == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        if (visas.getVisaNumber() == null || visas.getVisaNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("签证号码不能为空");
        }
        if (visas.getExpiryDate() == null) {
            throw new IllegalArgumentException("到期日期不能为空");
        }
        
        // 检查签证号是否已存在
        LambdaQueryWrapper<Visas> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Visas::getVisaNumber, visas.getVisaNumber());
        queryWrapper.eq(Visas::getDeleted, 0);
        Visas existingVisas = visasMapper.selectOne(queryWrapper);
        if (existingVisas != null) {
            throw new IllegalArgumentException("签证号码已存在");
        }
        
        // 计算并设置状态
        calculateAndSetStatus(visas);
        
        visas.setCreateTime(LocalDateTime.now());
        visas.setUpdateTime(LocalDateTime.now());
        visas.setDeleted(0);
        
        visasMapper.insert(visas);
        log.info("签证记录创建成功，签证ID: {}", visas.getId());
        return visas;
    }
    
    /**
     * 根据ID获取签证记录
     * 
     * @param id 签证ID
     * @return 签证记录
     */
    public Visas getVisasById(Long id) {
        LambdaQueryWrapper<Visas> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Visas::getId, id);
        queryWrapper.eq(Visas::getDeleted, 0);
        Visas visas = visasMapper.selectOne(queryWrapper);
        if (visas != null) {
            // 重新计算状态（可能已过期）
            calculateAndSetStatus(visas);
        }
        return visas;
    }
    
    /**
     * 根据学生ID获取签证记录列表
     * 
     * @param studentId 学生ID
     * @return 签证记录列表
     */
    public List<Visas> getVisasByStudentId(Long studentId) {
        LambdaQueryWrapper<Visas> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Visas::getStudentId, studentId);
        queryWrapper.eq(Visas::getDeleted, 0);
        queryWrapper.orderByDesc(Visas::getExpiryDate);
        List<Visas> visasList = visasMapper.selectList(queryWrapper);
        
        // 重新计算每个签证的状态
        visasList.forEach(this::calculateAndSetStatus);
        
        return visasList;
    }
    
    /**
     * 获取所有签证记录列表
     * 
     * @return 签证记录列表
     */
    public List<Visas> getAllVisas() {
        LambdaQueryWrapper<Visas> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Visas::getDeleted, 0);
        queryWrapper.orderByDesc(Visas::getCreateTime);
        List<Visas> visasList = visasMapper.selectList(queryWrapper);
        
        // 重新计算每个签证的状态
        visasList.forEach(this::calculateAndSetStatus);
        
        return visasList;
    }
    
    /**
     * 更新签证记录
     * 
     * @param visas 签证对象
     * @return 更新后的签证记录
     */
    @Transactional
    public Visas updateVisas(Visas visas) {
        log.info("更新签证记录，签证ID: {}", visas.getId());
        
        Visas existingVisas = getVisasById(visas.getId());
        if (existingVisas == null) {
            throw new IllegalArgumentException("签证记录不存在");
        }
        
        // 如果修改了签证号，检查新签证号是否已被使用
        if (visas.getVisaNumber() != null && 
            !visas.getVisaNumber().equals(existingVisas.getVisaNumber())) {
            LambdaQueryWrapper<Visas> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Visas::getVisaNumber, visas.getVisaNumber());
            queryWrapper.eq(Visas::getDeleted, 0);
            queryWrapper.ne(Visas::getId, visas.getId());
            Visas duplicateVisas = visasMapper.selectOne(queryWrapper);
            if (duplicateVisas != null) {
                throw new IllegalArgumentException("签证号码已被使用");
            }
        }
        
        // 重新计算状态
        calculateAndSetStatus(visas);
        
        visas.setUpdateTime(LocalDateTime.now());
        visasMapper.updateById(visas);
        log.info("签证记录更新成功，签证ID: {}", visas.getId());
        return visas;
    }
    
    /**
     * 删除签证记录（逻辑删除）
     * 
     * @param id 签证ID
     */
    @Transactional
    public void deleteVisas(Long id) {
        log.info("删除签证记录，签证ID: {}", id);
        
        Visas visas = getVisasById(id);
        if (visas == null) {
            throw new IllegalArgumentException("签证记录不存在");
        }
        
        visas.setDeleted(1);
        visas.setUpdateTime(LocalDateTime.now());
        visasMapper.updateById(visas);
        log.info("签证记录删除成功，签证ID: {}", id);
    }
    
    /**
     * 获取即将过期的签证列表（用于提醒）
     * 
     * @param days 提前提醒天数（可选，默认使用配置值）
     * @return 即将过期的签证列表
     */
    public List<Visas> getExpiringSoonVisas(Integer days) {
        LocalDate today = LocalDate.now();
        int reminderDays = days != null ? days : expiringSoonDays;
        LocalDate reminderDate = today.plusDays(reminderDays);
        
        LambdaQueryWrapper<Visas> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Visas::getDeleted, 0);
        queryWrapper.ge(Visas::getExpiryDate, today);  // 未过期
        queryWrapper.le(Visas::getExpiryDate, reminderDate);  // 在提醒日期内
        queryWrapper.ne(Visas::getStatus, "CANCELLED");  // 未取消
        queryWrapper.orderByAsc(Visas::getExpiryDate);
        
        List<Visas> visasList = visasMapper.selectList(queryWrapper);
        
        // 重新计算状态并更新
        visasList.forEach(visas -> {
            calculateAndSetStatus(visas);
            if (!visas.getStatus().equals(visasMapper.selectById(visas.getId()).getStatus())) {
                visas.setUpdateTime(LocalDateTime.now());
                visasMapper.updateById(visas);
            }
        });
        
        log.info("查询到 {} 条即将过期的签证记录（{}天内）", visasList.size(), reminderDays);
        return visasList;
    }
    
    /**
     * 获取已过期的签证列表
     * 
     * @return 已过期的签证列表
     */
    public List<Visas> getExpiredVisas() {
        LocalDate today = LocalDate.now();
        
        LambdaQueryWrapper<Visas> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Visas::getDeleted, 0);
        queryWrapper.lt(Visas::getExpiryDate, today);  // 已过期
        queryWrapper.ne(Visas::getStatus, "CANCELLED");  // 未取消
        queryWrapper.orderByAsc(Visas::getExpiryDate);
        
        List<Visas> visasList = visasMapper.selectList(queryWrapper);
        
        // 重新计算状态并更新
        visasList.forEach(visas -> {
            calculateAndSetStatus(visas);
            if (!visas.getStatus().equals(visasMapper.selectById(visas.getId()).getStatus())) {
                visas.setUpdateTime(LocalDateTime.now());
                visasMapper.updateById(visas);
            }
        });
        
        log.info("查询到 {} 条已过期的签证记录", visasList.size());
        return visasList;
    }
    
    /**
     * 计算并设置签证状态（根据到期日期）
     * 
     * @param visas 签证对象
     */
    private void calculateAndSetStatus(Visas visas) {
        if (visas.getExpiryDate() == null) {
            return;
        }
        
        // 如果状态是已取消，不重新计算
        if ("CANCELLED".equals(visas.getStatus())) {
            return;
        }
        
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = visas.getExpiryDate();
        
        // 计算距离到期的天数
        long daysUntilExpiry = ChronoUnit.DAYS.between(today, expiryDate);
        
        if (daysUntilExpiry < 0) {
            // 已过期
            visas.setStatus("EXPIRED");
        } else if (daysUntilExpiry <= expiringSoonDays) {
            // 即将过期（在提醒天数内）
            visas.setStatus("EXPIRING_SOON");
        } else {
            // 有效
            visas.setStatus("VALID");
        }
    }
}

