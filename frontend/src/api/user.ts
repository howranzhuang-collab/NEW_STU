// import http from './http' // 暂时未使用

/**
 * 用户信息（临时方案，实际应该从后端获取）
 * 由于后端登录响应中没有返回userId，这里提供一个辅助函数
 * 实际项目中应该：1. 后端在登录时返回userId，2. 或者提供一个 /api/auth/me 接口
 */
export interface UserInfo {
  id: number
  username: string
  role: string
}

/**
 * 根据username获取用户ID的辅助函数
 * 注意：这是一个临时方案，实际应该由后端提供接口
 * TODO: 后端应该提供 /api/auth/me 接口获取当前用户信息（包含id）
 */
export async function getCurrentUserId(): Promise<number | null> {
  // 临时方案：从localStorage或其他地方获取
  // 或者通过其他接口获取
  // 这里返回null，表示无法获取，需要后端支持
  return null
}

