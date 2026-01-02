/**
 * 后端统一响应格式
 */
export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
}

/**
 * 登录响应数据
 */
export interface LoginResponse {
  token: string
  username: string
  role: string
  userId?: number
}

