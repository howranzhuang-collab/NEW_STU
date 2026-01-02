import http from './http'
import type { Alumni } from '../types/entity'

/**
 * 获取所有校友记录列表
 */
export function getAllAlumni(): Promise<Alumni[]> {
  return http.get('/alumni')
}

/**
 * 根据ID获取校友记录
 */
export function getAlumniById(id: number): Promise<Alumni> {
  return http.get(`/alumni/${id}`)
}

/**
 * 根据学生ID获取校友记录
 */
export function getAlumniByStudentId(studentId: number): Promise<Alumni> {
  return http.get(`/alumni/student/${studentId}`)
}

/**
 * 创建校友记录
 */
export function createAlumni(alumni: Partial<Alumni>): Promise<Alumni> {
  return http.post('/alumni', alumni)
}

/**
 * 从学籍记录同步创建校友记录（毕业同步）
 */
export function syncAlumniFromStudentRecord(data: {
  studentId: number
  graduationDate: string
  degree?: string
  status?: string
  organization?: string
  position?: string
  contact?: string
}): Promise<Alumni> {
  return http.post('/alumni/sync', data)
}

/**
 * 更新校友记录
 */
export function updateAlumni(id: number, alumni: Partial<Alumni>): Promise<Alumni> {
  return http.put(`/alumni/${id}`, alumni)
}

/**
 * 删除校友记录
 */
export function deleteAlumni(id: number): Promise<void> {
  return http.delete(`/alumni/${id}`)
}

