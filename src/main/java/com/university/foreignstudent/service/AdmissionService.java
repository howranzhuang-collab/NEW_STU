package com.university.foreignstudent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.foreignstudent.entity.AdmissionProject;
import com.university.foreignstudent.entity.Application;
import com.university.foreignstudent.mapper.AdmissionProjectMapper;
import com.university.foreignstudent.mapper.ApplicationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 招生管理服务类
 * 提供招生项目和申请的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdmissionService {
    
    private final AdmissionProjectMapper projectMapper;
    private final ApplicationMapper applicationMapper;
    
    /**
     * 创建招生项目
     * 
     * @param project 招生项目对象
     * @return 创建的项目
     */
    @Transactional
    public AdmissionProject createProject(AdmissionProject project) {
        log.info("创建招生项目: {}", project.getTitle());
        
        if (project.getAppliedCount() == null) {
            project.setAppliedCount(0);
        }
        if (project.getStatus() == null || project.getStatus().isEmpty()) {
            project.setStatus("OPEN");
        }
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());
        project.setDeleted(0);
        
        projectMapper.insert(project);
        log.info("招生项目创建成功，ID: {}", project.getId());
        return project;
    }
    
    /**
     * 根据ID获取招生项目
     * 
     * @param id 项目ID
     * @return 招生项目
     */
    public AdmissionProject getProjectById(Long id) {
        LambdaQueryWrapper<AdmissionProject> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdmissionProject::getId, id);
        queryWrapper.eq(AdmissionProject::getDeleted, 0);
        return projectMapper.selectOne(queryWrapper);
    }
    
    /**
     * 获取所有招生项目列表
     * 
     * @return 项目列表
     */
    public List<AdmissionProject> getAllProjects() {
        LambdaQueryWrapper<AdmissionProject> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdmissionProject::getDeleted, 0);
        queryWrapper.orderByDesc(AdmissionProject::getCreateTime);
        return projectMapper.selectList(queryWrapper);
    }
    
    /**
     * 更新招生项目
     * 
     * @param project 招生项目对象
     * @return 更新后的项目
     */
    @Transactional
    public AdmissionProject updateProject(AdmissionProject project) {
        log.info("更新招生项目，ID: {}", project.getId());
        
        AdmissionProject existingProject = getProjectById(project.getId());
        if (existingProject == null) {
            throw new RuntimeException("招生项目不存在");
        }
        
        project.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(project);
        log.info("招生项目更新成功，ID: {}", project.getId());
        return project;
    }
    
    /**
     * 删除招生项目（逻辑删除）
     * 
     * @param id 项目ID
     */
    @Transactional
    public void deleteProject(Long id) {
        log.info("删除招生项目，ID: {}", id);
        
        AdmissionProject project = getProjectById(id);
        if (project == null) {
            throw new RuntimeException("招生项目不存在");
        }
        
        project.setDeleted(1);
        project.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(project);
        log.info("招生项目删除成功，ID: {}", id);
    }
    
    /**
     * 提交申请
     * 
     * @param application 申请对象
     * @param filePath 上传文件路径
     * @return 创建的申请
     */
    @Transactional
    public Application submitApplication(Application application, String filePath) {
        log.info("学生提交申请，项目ID: {}, 学生ID: {}", application.getProjectId(), application.getStudentId());
        
        // 检查项目是否存在且未过期
        AdmissionProject project = getProjectById(application.getProjectId());
        if (project == null) {
            throw new RuntimeException("招生项目不存在");
        }
        
        if (!"OPEN".equals(project.getStatus())) {
            throw new RuntimeException("招生项目已关闭，无法申请");
        }
        
        LocalDate today = LocalDate.now();
        if (project.getDeadline() != null && project.getDeadline().isBefore(today)) {
            throw new RuntimeException("申请已截止");
        }
        
        // 检查是否已申请
        LambdaQueryWrapper<Application> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Application::getProjectId, application.getProjectId());
        queryWrapper.eq(Application::getStudentId, application.getStudentId());
        queryWrapper.eq(Application::getDeleted, 0);
        Application existingApplication = applicationMapper.selectOne(queryWrapper);
        
        if (existingApplication != null) {
            throw new RuntimeException("您已提交过申请，请勿重复提交");
        }
        
        // 检查名额是否已满
        if (project.getQuota() != null && project.getAppliedCount() >= project.getQuota()) {
            throw new RuntimeException("招生名额已满");
        }
        
        // 创建申请
        application.setStatus("PENDING");
        application.setApplicationDate(LocalDate.now());
        application.setFilePath(filePath);
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        application.setDeleted(0);
        
        applicationMapper.insert(application);
        
        // 更新项目的已报名人数
        project.setAppliedCount(project.getAppliedCount() + 1);
        project.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(project);
        
        log.info("申请提交成功，申请ID: {}", application.getId());
        return application;
    }
    
    /**
     * 根据学生ID获取申请列表
     * 
     * @param studentId 学生ID
     * @return 申请列表
     */
    public List<Application> getApplicationsByStudentId(Long studentId) {
        LambdaQueryWrapper<Application> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Application::getStudentId, studentId);
        queryWrapper.eq(Application::getDeleted, 0);
        queryWrapper.orderByDesc(Application::getCreateTime);
        return applicationMapper.selectList(queryWrapper);
    }
    
    /**
     * 根据项目ID获取申请列表
     * 
     * @param projectId 项目ID
     * @return 申请列表
     */
    public List<Application> getApplicationsByProjectId(Long projectId) {
        LambdaQueryWrapper<Application> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Application::getProjectId, projectId);
        queryWrapper.eq(Application::getDeleted, 0);
        queryWrapper.orderByDesc(Application::getCreateTime);
        return applicationMapper.selectList(queryWrapper);
    }
    
    /**
     * 更新申请状态
     * 
     * @param applicationId 申请ID
     * @param status 新状态
     * @return 更新后的申请
     */
    @Transactional
    public Application updateApplicationStatus(Long applicationId, String status) {
        log.info("更新申请状态，申请ID: {}, 新状态: {}", applicationId, status);
        
        Application application = applicationMapper.selectById(applicationId);
        if (application == null || application.getDeleted() == 1) {
            throw new RuntimeException("申请不存在");
        }
        
        application.setStatus(status);
        application.setUpdateTime(LocalDateTime.now());
        applicationMapper.updateById(application);
        
        log.info("申请状态更新成功，申请ID: {}", applicationId);
        return application;
    }
}

