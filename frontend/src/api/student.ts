import http from './http'
import type { StudentRecord, Attendance, Exam } from '../types/entity'

/**
 * 获取所有学籍记录
 */
export function getAllStudentRecords(): Promise<StudentRecord[]> {
  return http.get('/students/records')
}

/**
 * 根据ID获取学籍记录
 */
export function getStudentRecordById(id: number): Promise<StudentRecord> {
  return http.get(`/students/records/${id}`)
}

/**
 * 根据学生ID获取学籍记录
 */
export function getStudentRecordByStudentId(studentId: number): Promise<StudentRecord> {
  return http.get(`/students/records/student/${studentId}`)
}

/**
 * 更新学籍信息
 */
export function updateStudentRecord(id: number, record: Partial<StudentRecord>): Promise<StudentRecord> {
  return http.put(`/students/records/${id}`, record)
}

// ==================== 考勤相关接口 ====================

/**
 * 添加考勤记录
 */
export function createAttendance(attendance: Attendance): Promise<Attendance> {
  return http.post('/students/attendances', attendance)
}

/**
 * 根据学生ID获取考勤记录列表
 */
export function getAttendancesByStudentId(studentId: number): Promise<Attendance[]> {
  return http.get(`/students/attendances/student/${studentId}`)
}

/**
 * 更新考勤记录
 */
export function updateAttendance(id: number, attendance: Partial<Attendance>): Promise<Attendance> {
  return http.put(`/students/attendances/${id}`, attendance)
}

/**
 * 删除考勤记录
 */
export function deleteAttendance(id: number): Promise<void> {
  return http.delete(`/students/attendances/${id}`)
}

// ==================== 考试相关接口 ====================

/**
 * 添加考试记录
 */
export function createExam(exam: Exam): Promise<Exam> {
  return http.post('/students/exams', exam)
}

/**
 * 根据学生ID获取考试记录列表
 */
export function getExamsByStudentId(studentId: number): Promise<Exam[]> {
  return http.get(`/students/exams/student/${studentId}`)
}

/**
 * 更新考试记录
 */
export function updateExam(id: number, exam: Partial<Exam>): Promise<Exam> {
  return http.put(`/students/exams/${id}`, exam)
}

/**
 * 删除考试记录
 */
export function deleteExam(id: number): Promise<void> {
  return http.delete(`/students/exams/${id}`)
}

