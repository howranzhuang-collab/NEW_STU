package com.university.foreignstudent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.foreignstudent.entity.Alumni;
import com.university.foreignstudent.entity.StudentRecord;
import com.university.foreignstudent.mapper.AlumniMapper;
import com.university.foreignstudent.mapper.StudentRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 校友管理服务类
 * 提供校友相关的业务逻辑，包括毕业同步
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlumniService {
    
    private final AlumniMapper alumniMapper;
    private final StudentRecordMapper studentRecordMapper;
    
    /**
     * 创建校友记录
     * 
     * @param alumni 校友对象
     * @return 创建的校友记录
     */
    @Transactional
    public Alumni createAlumni(Alumni alumni) {
        log.info("创建校友记录，学生ID: {}", alumni.getStudentId());
        
        if (alumni.getStudentId() == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        if (alumni.getGraduationDate() == null) {
            throw new IllegalArgumentException("毕业日期不能为空");
        }
        
        // 检查是否已存在
        LambdaQueryWrapper<Alumni> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Alumni::getStudentId, alumni.getStudentId());
        queryWrapper.eq(Alumni::getDeleted, 0);
        Alumni existingAlumni = alumniMapper.selectOne(queryWrapper);
        if (existingAlumni != null) {
            throw new IllegalArgumentException("该学生已有校友记录");
        }
        
        alumni.setCreateTime(LocalDateTime.now());
        alumni.setUpdateTime(LocalDateTime.now());
        alumni.setDeleted(0);
        
        alumniMapper.insert(alumni);
        log.info("校友记录创建成功，校友ID: {}", alumni.getId());
        return alumni;
    }
    
    /**
     * 从学籍记录同步创建校友记录（毕业同步）
     * 
     * @param studentId 学生ID
     * @param graduationDate 毕业日期
     * @param degree 学位
     * @param status 毕业去向
     * @param organization 工作单位/继续深造学校
     * @param position 职位/专业
     * @param contact 联系方式
     * @return 创建的校友记录
     */
    @Transactional
    public Alumni syncFromStudentRecord(Long studentId, LocalDate graduationDate, 
                                       String degree, String status, 
                                       String organization, String position, String contact) {
        log.info("从学籍记录同步创建校友记录，学生ID: {}", studentId);
        
        // 检查学籍记录是否存在
        LambdaQueryWrapper<StudentRecord> recordQuery = new LambdaQueryWrapper<>();
        recordQuery.eq(StudentRecord::getStudentId, studentId);
        recordQuery.eq(StudentRecord::getDeleted, 0);
        StudentRecord studentRecord = studentRecordMapper.selectOne(recordQuery);
        
        if (studentRecord == null) {
            throw new IllegalArgumentException("学籍记录不存在，无法同步");
        }
        
        // 检查是否已存在校友记录
        LambdaQueryWrapper<Alumni> alumniQuery = new LambdaQueryWrapper<>();
        alumniQuery.eq(Alumni::getStudentId, studentId);
        alumniQuery.eq(Alumni::getDeleted, 0);
        Alumni existingAlumni = alumniMapper.selectOne(alumniQuery);
        
        if (existingAlumni != null) {
            log.info("校友记录已存在，更新记录，学生ID: {}", studentId);
            // 更新现有记录
            existingAlumni.setName(studentRecord.getName());
            existingAlumni.setStudentNumber(studentRecord.getStudentNumber());
            existingAlumni.setMajor(studentRecord.getMajor());
            existingAlumni.setGraduationDate(graduationDate);
            existingAlumni.setDegree(degree);
            existingAlumni.setStatus(status);
            existingAlumni.setOrganization(organization);
            existingAlumni.setPosition(position);
            existingAlumni.setContact(contact != null ? contact : studentRecord.getEmail());
            existingAlumni.setUpdateTime(LocalDateTime.now());
            alumniMapper.updateById(existingAlumni);
            return existingAlumni;
        }
        
        // 创建新校友记录（从学籍记录同步数据）
        Alumni alumni = new Alumni();
        alumni.setStudentId(studentId);
        alumni.setName(studentRecord.getName());
        alumni.setStudentNumber(studentRecord.getStudentNumber());
        alumni.setMajor(studentRecord.getMajor());
        alumni.setGraduationDate(graduationDate);
        alumni.setDegree(degree);
        alumni.setStatus(status);
        alumni.setOrganization(organization);
        alumni.setPosition(position);
        alumni.setContact(contact != null ? contact : studentRecord.getEmail());
        alumni.setCreateTime(LocalDateTime.now());
        alumni.setUpdateTime(LocalDateTime.now());
        alumni.setDeleted(0);
        
        alumniMapper.insert(alumni);
        
        // 更新学籍状态为已毕业
        studentRecord.setStatus("GRADUATED");
        studentRecord.setUpdateTime(LocalDateTime.now());
        studentRecordMapper.updateById(studentRecord);
        
        log.info("校友记录同步创建成功，校友ID: {}, 学籍状态已更新为已毕业", alumni.getId());
        return alumni;
    }
    
    /**
     * 获取所有校友记录列表
     * 
     * @return 校友记录列表
     */
    public List<Alumni> getAllAlumni() {
        LambdaQueryWrapper<Alumni> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Alumni::getDeleted, 0);
        queryWrapper.orderByDesc(Alumni::getGraduationDate);
        return alumniMapper.selectList(queryWrapper);
    }
    
    /**
     * 根据ID获取校友记录
     * 
     * @param id 校友ID
     * @return 校友记录
     */
    public Alumni getAlumniById(Long id) {
        LambdaQueryWrapper<Alumni> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Alumni::getId, id);
        queryWrapper.eq(Alumni::getDeleted, 0);
        return alumniMapper.selectOne(queryWrapper);
    }
    
    /**
     * 根据学生ID获取校友记录
     * 
     * @param studentId 学生ID
     * @return 校友记录
     */
    public Alumni getAlumniByStudentId(Long studentId) {
        LambdaQueryWrapper<Alumni> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Alumni::getStudentId, studentId);
        queryWrapper.eq(Alumni::getDeleted, 0);
        return alumniMapper.selectOne(queryWrapper);
    }
    
    /**
     * 更新校友记录
     * 
     * @param alumni 校友对象
     * @return 更新后的校友记录
     */
    @Transactional
    public Alumni updateAlumni(Alumni alumni) {
        log.info("更新校友记录，校友ID: {}", alumni.getId());
        
        Alumni existingAlumni = getAlumniById(alumni.getId());
        if (existingAlumni == null) {
            throw new IllegalArgumentException("校友记录不存在");
        }
        
        alumni.setUpdateTime(LocalDateTime.now());
        alumniMapper.updateById(alumni);
        log.info("校友记录更新成功，校友ID: {}", alumni.getId());
        return alumni;
    }
    
    /**
     * 删除校友记录（逻辑删除）
     * 
     * @param id 校友ID
     */
    @Transactional
    public void deleteAlumni(Long id) {
        log.info("删除校友记录，校友ID: {}", id);
        
        Alumni alumni = getAlumniById(id);
        if (alumni == null) {
            throw new IllegalArgumentException("校友记录不存在");
        }
        
        alumni.setDeleted(1);
        alumni.setUpdateTime(LocalDateTime.now());
        alumniMapper.updateById(alumni);
        log.info("校友记录删除成功，校友ID: {}", id);
    }
}

