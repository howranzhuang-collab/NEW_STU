import axios from 'axios'
import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResult } from '../types/api'
import { useUserStore } from '../stores/user'

const http: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor
http.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    // 如果是 FormData，不设置 Content-Type，让浏览器自动设置（包含 boundary）
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
http.interceptors.response.use(
  (response: AxiosResponse<ApiResult>) => {
    const result = response.data
    
    // 如果 code 不是 200，说明业务逻辑错误
    if (result.code !== 200) {
      ElMessage.error(result.message || '请求失败')
      return Promise.reject(new Error(result.message || '请求失败'))
    }
    
    // 成功时直接返回 data 字段
    return result.data
  },
  (error) => {
    // 网络错误或其他异常
    let errorMessage = '请求失败，请稍后重试'
    
    if (error.response) {
      const status = error.response.status
      
      // 统一处理 401 未授权，自动退出登录
      if (status === 401) {
        const userStore = useUserStore()
        ElMessage.error('登录已过期，请重新登录')
        userStore.logout()
        // 延迟跳转，确保消息显示
        setTimeout(() => {
          window.location.href = '/login'
        }, 1000)
        return Promise.reject(error)
      }
      
      // 服务器返回了错误状态码
      const result = error.response.data as ApiResult
      if (result && result.message) {
        errorMessage = result.message
      } else {
        errorMessage = `请求失败：${status} ${error.response.statusText}`
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      errorMessage = '网络连接失败，请检查网络'
    } else {
      // 其他错误
      errorMessage = error.message || errorMessage
    }
    
    ElMessage.error(errorMessage)
    return Promise.reject(error)
  }
)

export default http
