import http from './http'
import type { DormBuilding, DormRoom, DormAllocation } from '../types/entity'

// ==================== 宿舍楼相关接口 ====================

/**
 * 获取所有宿舍楼列表
 */
export function getAllBuildings(): Promise<DormBuilding[]> {
  return http.get('/dorms/buildings')
}

/**
 * 根据ID获取宿舍楼
 */
export function getBuildingById(id: number): Promise<DormBuilding> {
  return http.get(`/dorms/buildings/${id}`)
}

/**
 * 创建宿舍楼
 */
export function createBuilding(building: Partial<DormBuilding>): Promise<DormBuilding> {
  return http.post('/dorms/buildings', building)
}

// ==================== 房间相关接口 ====================

/**
 * 根据宿舍楼ID获取房间列表
 */
export function getRoomsByBuildingId(buildingId: number): Promise<DormRoom[]> {
  return http.get(`/dorms/buildings/${buildingId}/rooms`)
}

/**
 * 获取可用房间列表
 */
export function getAvailableRooms(buildingId?: number): Promise<DormRoom[]> {
  const url = buildingId 
    ? `/dorms/rooms/available?buildingId=${buildingId}`
    : '/dorms/rooms/available'
  return http.get(url)
}

/**
 * 根据ID获取房间
 */
export function getRoomById(id: number): Promise<DormRoom> {
  return http.get(`/dorms/rooms/${id}`)
}

/**
 * 创建房间
 */
export function createRoom(room: Partial<DormRoom>): Promise<DormRoom> {
  return http.post('/dorms/rooms', room)
}

// ==================== 分配相关接口 ====================

/**
 * 分配宿舍（检查容量，使用事务）
 */
export function allocateDorm(allocation: Partial<DormAllocation>): Promise<DormAllocation> {
  return http.post('/dorms/allocations', allocation)
}

/**
 * 退房
 */
export function checkOut(allocationId: number, checkOutDate?: string): Promise<DormAllocation> {
  return http.post(`/dorms/allocations/${allocationId}/checkout`, {
    checkOutDate
  })
}

/**
 * 根据学生ID获取分配记录列表
 */
export function getAllocationsByStudentId(studentId: number): Promise<DormAllocation[]> {
  return http.get(`/dorms/allocations/student/${studentId}`)
}

/**
 * 根据房间ID获取分配记录列表
 */
export function getAllocationsByRoomId(roomId: number): Promise<DormAllocation[]> {
  return http.get(`/dorms/allocations/room/${roomId}`)
}

/**
 * 根据ID获取分配记录
 */
export function getAllocationById(id: number): Promise<DormAllocation> {
  return http.get(`/dorms/allocations/${id}`)
}

