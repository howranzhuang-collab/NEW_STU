import http from './http'
import type { Visas } from '../types/entity'

/**
 * 获取所有签证记录列表
 */
export function getAllVisas(): Promise<Visas[]> {
  return http.get('/visas')
}

/**
 * 根据ID获取签证记录
 */
export function getVisasById(id: number): Promise<Visas> {
  return http.get(`/visas/${id}`)
}

/**
 * 根据学生ID获取签证列表
 */
export function getVisasByStudentId(studentId: number): Promise<Visas[]> {
  return http.get(`/visas/student/${studentId}`)
}

/**
 * 创建签证记录
 */
export function createVisas(visas: Partial<Visas>): Promise<Visas> {
  return http.post('/visas', visas)
}

/**
 * 更新签证记录
 */
export function updateVisas(id: number, visas: Partial<Visas>): Promise<Visas> {
  return http.put(`/visas/${id}`, visas)
}

/**
 * 删除签证记录
 */
export function deleteVisas(id: number): Promise<void> {
  return http.delete(`/visas/${id}`)
}

/**
 * 获取即将到期的签证列表
 */
export function getExpiringSoonVisas(days?: number): Promise<Visas[]> {
  const url = days ? `/visas/expiring-soon?days=${days}` : '/visas/expiring-soon'
  return http.get(url)
}

/**
 * 获取已过期的签证列表
 */
export function getExpiredVisas(): Promise<Visas[]> {
  return http.get('/visas/expired')
}

