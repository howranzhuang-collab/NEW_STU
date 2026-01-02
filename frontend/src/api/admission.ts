import http from './http'
import type { AdmissionProject, Application } from '../types/entity'

/**
 * 获取所有招生项目
 */
export function getAllProjects(): Promise<AdmissionProject[]> {
  return http.get('/admission/projects')
}

/**
 * 根据ID获取招生项目
 */
export function getProjectById(id: number): Promise<AdmissionProject> {
  return http.get(`/admission/projects/${id}`)
}

/**
 * 创建招生项目
 */
export function createProject(project: AdmissionProject): Promise<AdmissionProject> {
  return http.post('/admission/projects', project)
}

/**
 * 更新招生项目
 */
export function updateProject(id: number, project: AdmissionProject): Promise<AdmissionProject> {
  return http.put(`/admission/projects/${id}`, project)
}

/**
 * 删除招生项目
 */
export function deleteProject(id: number): Promise<void> {
  return http.delete(`/admission/projects/${id}`)
}

/**
 * 根据项目ID获取申请列表
 */
export function getApplicationsByProjectId(projectId: number): Promise<Application[]> {
  return http.get(`/admission/applications/project/${projectId}`)
}

/**
 * 根据学生ID获取申请列表
 */
export function getApplicationsByStudentId(studentId: number): Promise<Application[]> {
  return http.get(`/admission/applications/student/${studentId}`)
}

/**
 * 更新申请状态
 */
export function updateApplicationStatus(applicationId: number, status: string): Promise<Application> {
  return http.put(`/admission/applications/${applicationId}/status`, { status })
}

/**
 * 提交申请（支持文件上传）
 */
export function submitApplication(
  projectId: number,
  studentId: number,
  file?: File,
  remarks?: string
): Promise<Application> {
  const formData = new FormData()
  formData.append('projectId', projectId.toString())
  formData.append('studentId', studentId.toString())
  if (file) {
    formData.append('file', file)
  }
  if (remarks) {
    formData.append('remarks', remarks)
  }
  
  // 使用 axios 直接发送，因为需要设置 Content-Type
  return http.post('/admission/applications', formData)
}

