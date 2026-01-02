package com.university.foreignstudent.controller;

import com.university.foreignstudent.dto.Result;
import com.university.foreignstudent.entity.AdmissionProject;
import com.university.foreignstudent.entity.Application;
import com.university.foreignstudent.service.AdmissionService;
import com.university.foreignstudent.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 招生管理控制器
 * 提供招生项目和申请的 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/admission")
@RequiredArgsConstructor
public class AdmissionController {
    
    private final AdmissionService admissionService;
    private final FileUploadService fileUploadService;
    
    /**
     * 获取所有招生项目列表
     * 
     * @return 项目列表
     */
    @GetMapping("/projects")
    public Result<List<AdmissionProject>> getAllProjects() {
        try {
            List<AdmissionProject> projects = admissionService.getAllProjects();
            return Result.success(projects);
        } catch (Exception e) {
            log.error("获取项目列表失败", e);
            return Result.error("获取项目列表失败");
        }
    }
    
    /**
     * 根据ID获取招生项目
     * 
     * @param id 项目ID
     * @return 项目信息
     */
    @GetMapping("/projects/{id}")
    public Result<AdmissionProject> getProjectById(@PathVariable Long id) {
        try {
            AdmissionProject project = admissionService.getProjectById(id);
            if (project == null) {
                return Result.error(404, "项目不存在");
            }
            return Result.success(project);
        } catch (Exception e) {
            log.error("获取项目失败", e);
            return Result.error("获取项目失败");
        }
    }
    
    /**
     * 创建招生项目（需要管理员权限）
     * 
     * @param project 项目信息
     * @return 创建的项目
     */
    @PostMapping("/projects")
    public Result<AdmissionProject> createProject(@RequestBody AdmissionProject project) {
        try {
            if (project.getTitle() == null || project.getTitle().trim().isEmpty()) {
                return Result.error("项目标题不能为空");
            }
            if (project.getQuota() == null || project.getQuota() <= 0) {
                return Result.error("招生名额必须大于0");
            }
            if (project.getDeadline() == null) {
                return Result.error("截止日期不能为空");
            }
            
            AdmissionProject createdProject = admissionService.createProject(project);
            return Result.success("项目创建成功", createdProject);
        } catch (RuntimeException e) {
            log.error("创建项目失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建项目异常", e);
            return Result.error("创建项目失败，请稍后重试");
        }
    }
    
    /**
     * 更新招生项目（需要管理员权限）
     * 
     * @param id 项目ID
     * @param project 项目信息
     * @return 更新后的项目
     */
    @PutMapping("/projects/{id}")
    public Result<AdmissionProject> updateProject(@PathVariable Long id, @RequestBody AdmissionProject project) {
        try {
            project.setId(id);
            AdmissionProject updatedProject = admissionService.updateProject(project);
            return Result.success("项目更新成功", updatedProject);
        } catch (RuntimeException e) {
            log.error("更新项目失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新项目异常", e);
            return Result.error("更新项目失败，请稍后重试");
        }
    }
    
    /**
     * 删除招生项目（需要管理员权限）
     * 
     * @param id 项目ID
     * @return 删除结果
     */
    @DeleteMapping("/projects/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        try {
            admissionService.deleteProject(id);
            return Result.success("项目删除成功", null);
        } catch (RuntimeException e) {
            log.error("删除项目失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除项目异常", e);
            return Result.error("删除项目失败，请稍后重试");
        }
    }
    
    /**
     * 提交申请（支持文件上传）
     * 
     * @param projectId 项目ID
     * @param studentId 学生ID
     * @param file 上传的文件（可选）
     * @param remarks 备注信息（可选）
     * @return 申请结果
     */
    @PostMapping("/applications")
    public Result<Application> submitApplication(
            @RequestParam Long projectId,
            @RequestParam Long studentId,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String remarks) {
        try {
            Application application = new Application();
            application.setProjectId(projectId);
            application.setStudentId(studentId);
            application.setRemarks(remarks);
            
            // 处理文件上传
            String filePath = null;
            if (file != null && !file.isEmpty()) {
                filePath = fileUploadService.uploadFile(file, "applications");
            }
            
            Application createdApplication = admissionService.submitApplication(application, filePath);
            return Result.success("申请提交成功", createdApplication);
        } catch (RuntimeException e) {
            log.error("提交申请失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("提交申请异常", e);
            return Result.error("提交申请失败，请稍后重试");
        }
    }
    
    /**
     * 根据学生ID获取申请列表
     * 
     * @param studentId 学生ID
     * @return 申请列表
     */
    @GetMapping("/applications/student/{studentId}")
    public Result<List<Application>> getApplicationsByStudentId(@PathVariable Long studentId) {
        try {
            List<Application> applications = admissionService.getApplicationsByStudentId(studentId);
            return Result.success(applications);
        } catch (Exception e) {
            log.error("获取申请列表失败", e);
            return Result.error("获取申请列表失败");
        }
    }
    
    /**
     * 根据项目ID获取申请列表（需要管理员权限）
     * 
     * @param projectId 项目ID
     * @return 申请列表
     */
    @GetMapping("/applications/project/{projectId}")
    public Result<List<Application>> getApplicationsByProjectId(@PathVariable Long projectId) {
        try {
            List<Application> applications = admissionService.getApplicationsByProjectId(projectId);
            return Result.success(applications);
        } catch (Exception e) {
            log.error("获取申请列表失败", e);
            return Result.error("获取申请列表失败");
        }
    }
    
    /**
     * 更新申请状态（需要管理员权限）
     * 
     * @param applicationId 申请ID
     * @param request 包含 status 字段的请求体
     * @return 更新后的申请
     */
    @PutMapping("/applications/{applicationId}/status")
    public Result<Application> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            if (status == null || status.trim().isEmpty()) {
                return Result.error("状态不能为空");
            }
            if (!status.equals("PENDING") && !status.equals("APPROVED") && !status.equals("REJECTED")) {
                return Result.error("无效的状态值");
            }
            
            Application application = admissionService.updateApplicationStatus(applicationId, status);
            return Result.success("申请状态更新成功", application);
        } catch (RuntimeException e) {
            log.error("更新申请状态失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新申请状态异常", e);
            return Result.error("更新申请状态失败，请稍后重试");
        }
    }
}

