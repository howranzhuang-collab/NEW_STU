import http from './http'
import type { LoginResponse } from '../types/api'

/**
 * 用户注册
 */
export function register(username: string, password: string, role?: string): Promise<{ userId: number }> {
  return http.post('/auth/register', {
    username,
    password,
    role: role || 'STUDENT'
  })
}

/**
 * 登录接口
 */
export function login(username: string, password: string): Promise<LoginResponse> {
  return http.post('/auth/login', {
    username,
    password
  })
}

