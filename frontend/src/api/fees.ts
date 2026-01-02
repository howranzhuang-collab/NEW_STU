import http from './http'
import type { Fees } from '../types/entity'

/**
 * 获取所有费用记录
 */
export function getAllFees(): Promise<Fees[]> {
  return http.get('/fees')
}

/**
 * 根据ID获取费用记录
 */
export function getFeesById(id: number): Promise<Fees> {
  return http.get(`/fees/${id}`)
}

/**
 * 根据学生ID获取所有费用记录
 */
export function getFeesByStudentId(studentId: number): Promise<Fees[]> {
  return http.get(`/fees/student/${studentId}`)
}

/**
 * 根据学生ID获取未缴费费用
 */
export function getUnpaidFeesByStudentId(studentId: number): Promise<{ totalUnpaid: number; fees: Fees[] }> {
  return http.get(`/fees/student/${studentId}/unpaid`)
}

/**
 * 创建费用记录
 */
export function createFees(fees: Partial<Fees>): Promise<Fees> {
  return http.post('/fees', fees)
}

/**
 * 更新费用记录
 */
export function updateFees(id: number, fees: Partial<Fees>): Promise<Fees> {
  return http.put(`/fees/${id}`, fees)
}

/**
 * 删除费用记录
 */
export function deleteFees(id: number): Promise<void> {
  return http.delete(`/fees/${id}`)
}

/**
 * 标记费用为已缴（支持部分缴费）
 */
export function markAsPaid(feesId: number, paidAmount: number, paidDate?: string): Promise<Fees> {
  return http.post(`/fees/${feesId}/pay`, {
    paidAmount,
    paidDate
  })
}

/**
 * 完全标记费用为已缴（一次性缴清）
 */
export function markAsFullyPaid(feesId: number, paidDate?: string): Promise<Fees> {
  return http.post(`/fees/${feesId}/pay-full`, {
    paidDate
  })
}

