import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface UserInfo {
  username: string
  role: string
  userId?: number
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | undefined>(undefined)
  const userInfo = ref<UserInfo | null>(null)

  function setToken(newToken: string) {
    token.value = newToken
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
  }

  function logout() {
    token.value = undefined
    userInfo.value = null
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    logout
  }
})
