/**
 * 实体类型定义
 */

export interface StudentRecord {
  id: number
  studentId: number
  studentNumber: string
  name: string
  gender: string
  birthDate: string
  nationality: string
  major: string
  grade: string
  enrollmentDate: string
  status: string
  phone?: string
  email?: string
  address?: string
  createTime: string
  updateTime: string
}

export interface AdmissionProject {
  id: number
  title: string
  description?: string
  deadline: string
  quota: number
  appliedCount: number
  status: string
  createTime: string
  updateTime: string
}

export interface Application {
  id: number
  projectId: number
  studentId: number
  status: string
  applicationDate: string
  filePath?: string
  remarks?: string
  createTime: string
  updateTime: string
}

export interface Visas {
  id: number
  studentId: number
  visaType: string
  visaNumber: string
  issueDate: string
  expiryDate: string
  entryDate?: string
  issuingAuthority?: string
  status: string
  remarks?: string
  createTime: string
  updateTime: string
}

export interface Fees {
  id: number
  studentId: number
  feeType: string
  amount: number
  paidAmount: number
  dueDate: string
  paidDate?: string
  status: string
  remarks?: string
  createTime: string
  updateTime: string
}

export interface DormBuilding {
  id: number
  name: string
  address?: string
  totalFloors?: number
  totalRooms: number
  status: string
  remarks?: string
  createTime: string
  updateTime: string
}

export interface DormRoom {
  id: number
  buildingId: number
  roomNumber: string
  floor?: number
  capacity: number
  currentOccupancy: number
  roomType?: string
  status: string
  remarks?: string
  createTime: string
  updateTime: string
}

export interface DormAllocation {
  id: number
  studentId: number
  roomId: number
  checkInDate: string
  checkOutDate?: string
  status: string
  remarks?: string
  createTime: string
  updateTime: string
}

export interface Attendance {
  id: number
  studentId: number
  courseName: string
  attendanceDate: string
  status: string
  remarks?: string
  createTime: string
  updateTime: string
}

export interface Exam {
  id: number
  studentId: number
  courseName: string
  examType?: string
  examDate: string
  score: number
  fullScore: number
  remarks?: string
  createTime: string
  updateTime: string
}

export interface Alumni {
  id: number
  studentId: number
  name: string
  studentNumber: string
  major: string
  graduationDate: string
  degree: string
  status: string
  organization?: string
  position?: string
  contact?: string
  remarks?: string
  createTime: string
  updateTime: string
}

