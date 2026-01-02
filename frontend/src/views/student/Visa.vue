<template>
  <div class="visa-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">我的签证信息</span>
          <el-button :icon="Refresh" @click="loadVisas">刷新</el-button>
        </div>
      </template>

      <div v-if="loading" style="text-align: center; padding: 40px;">
        <el-icon class="is-loading" style="font-size: 32px;"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="visas.length === 0" style="text-align: center; padding: 40px;">
        <el-empty description="暂无签证信息" :image-size="120" />
      </div>

      <div v-else>
        <!-- 签证列表 -->
        <div v-for="visa in visas" :key="visa.id" class="visa-card">
          <el-card class="info-card" style="margin-bottom: 20px;">
            <template #header>
              <div class="section-header">
                <el-icon><Tickets /></el-icon>
                <span>签证信息</span>
                <el-tag
                  :type="getStatusType(visa.status)"
                  size="large"
                  style="margin-left: 10px;"
                >
                  {{ getStatusText(visa.status) }}
                </el-tag>
              </div>
            </template>

            <!-- 到期提醒 -->
            <el-alert
              v-if="getRemainingDays(visa.expiryDate) <= 30 && getRemainingDays(visa.expiryDate) > 0"
              title="签证即将到期"
              type="warning"
              :closable="false"
              show-icon
              style="margin-bottom: 20px;"
            >
              <template #default>
                <p style="margin: 5px 0;">
                  您的签证将在 <strong class="expiring-soon-text">{{ getRemainingDays(visa.expiryDate) }}</strong> 天后到期，请及时办理续签手续。
                </p>
              </template>
            </el-alert>

            <el-alert
              v-if="isExpired(visa.expiryDate)"
              title="签证已过期"
              type="error"
              :closable="false"
              show-icon
              style="margin-bottom: 20px;"
            >
              <template #default>
                <p style="margin: 5px 0;">您的签证已过期，请尽快办理续签手续。</p>
              </template>
            </el-alert>

            <!-- 签证详情 -->
            <el-descriptions :column="2" border>
              <el-descriptions-item label="签证类型">
                {{ getVisaTypeText(visa.visaType) }}
              </el-descriptions-item>
              <el-descriptions-item label="签证号码">
                {{ visa.visaNumber }}
              </el-descriptions-item>
              <el-descriptions-item label="签发日期">
                {{ formatDate(visa.issueDate) }}
              </el-descriptions-item>
              <el-descriptions-item label="到期日期">
                <span :class="{ 'expiring-soon': getRemainingDays(visa.expiryDate) <= 30 && getRemainingDays(visa.expiryDate) > 0, 'expired': isExpired(visa.expiryDate) }">
                  {{ formatDate(visa.expiryDate) }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="剩余天数">
                <span :class="{ 'expiring-soon': getRemainingDays(visa.expiryDate) <= 30 && getRemainingDays(visa.expiryDate) > 0, 'expired': isExpired(visa.expiryDate) }" class="remaining-days">
                  {{ getRemainingDaysText(visa.expiryDate) }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="入境日期">
                {{ visa.entryDate ? formatDate(visa.entryDate) : '未填写' }}
              </el-descriptions-item>
              <el-descriptions-item label="签发机构" :span="2">
                {{ visa.issuingAuthority || '未填写' }}
              </el-descriptions-item>
              <el-descriptions-item label="备注" :span="2">
                {{ visa.remarks || '无' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Tickets, Loading } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { getVisasByStudentId } from '../../api/visa'
import type { Visas } from '../../types/entity'

const userStore = useUserStore()
const loading = ref(false)
const visas = ref<Visas[]>([])

// 注意：这里需要知道学生的ID，由于登录响应中没有userId
// 我们需要通过其他方式获取，这里先使用一个临时方案
// 实际项目中应该在后端提供一个根据当前登录用户获取信息的接口
const studentId = ref<number | null>(null)

// 获取签证类型文本
const getVisaTypeText = (visaType: string) => {
  const typeMap: Record<string, string> = {
    'X1': 'X1 - 长期学习',
    'X2': 'X2 - 短期学习',
    'F': 'F - 访问',
    'L': 'L - 旅游'
  }
  return typeMap[visaType] || visaType
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'VALID': '有效',
    'EXPIRING_SOON': '即将过期',
    'EXPIRED': '已过期',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'VALID': 'success',
    'EXPIRING_SOON': 'warning',
    'EXPIRED': 'danger',
    'CANCELLED': 'info'
  }
  return typeMap[status] || ''
}

// 检查是否已过期
const isExpired = (expiryDate: string) => {
  if (!expiryDate) return false
  const expiry = new Date(expiryDate)
  const now = new Date()
  return expiry < now
}

// 获取剩余天数
const getRemainingDays = (expiryDate: string) => {
  if (!expiryDate) return 0
  const expiry = new Date(expiryDate)
  const now = new Date()
  const diffTime = expiry.getTime() - now.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays
}

// 获取剩余天数文本
const getRemainingDaysText = (expiryDate: string) => {
  const days = getRemainingDays(expiryDate)
  if (days < 0) return '已过期'
  if (days === 0) return '今天到期'
  return `${days} 天`
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return date
}

// 加载签证信息
const loadVisas = async () => {
  // 数据安全说明：
  // 当前实现存在数据泄露风险，因为无法安全地获取当前学生的ID
  // 为了确保"无其他学生数据泄露"，我们需要：
  // 1. 后端在登录响应中包含 userId（推荐）
  // 2. 或者后端提供 /api/auth/me 接口，根据JWT token返回当前用户信息（包含userId）
  // 3. 或者后端提供 /api/visas/current 接口，根据当前登录用户返回签证记录

  if (!studentId.value) {
    // 临时方案：尝试通过username匹配学生记录
    // 注意：这需要确保学生只能访问自己的数据
    if (!userStore.userInfo?.username) {
      ElMessage.error('用户信息不完整，请重新登录')
      return
    }

    // 尝试通过username查找对应的学生记录
    // 这需要后端支持，或者前端有一个安全的映射机制
    // 当前实现：如果无法获取studentId，则不加载数据，避免数据泄露
    ElMessage.warning({
      message: '无法获取学生ID。为了数据安全，请确保后端在登录响应中包含userId，或提供获取当前用户信息的接口。',
      duration: 5000
    })
    return
  }

  loading.value = true
  try {
    visas.value = await getVisasByStudentId(studentId.value)
  } catch (error) {
    console.error('加载签证信息失败:', error)
    ElMessage.error('加载签证信息失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 注意：由于无法安全获取studentId，loadVisas可能不会执行
  // 这是为了数据安全，避免泄露其他学生的数据
  // 一旦后端支持获取当前用户信息，可以取消注释来调用：
  // loadVisas()
})
</script>

<style scoped lang="scss">
.visa-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      font-size: 18px;
      font-weight: bold;
      color: #303133;
    }
  }

  .visa-card {
    .info-card {
      .section-header {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: bold;
      }
    }
  }

  .expiring-soon {
    color: #e6a23c;
    font-weight: bold;
  }

  .expired {
    color: #f56c6c;
    font-weight: bold;
  }

  .expiring-soon-text {
    color: #e6a23c;
    font-size: 18px;
    font-weight: bold;
  }

  .remaining-days {
    font-size: 16px;
    font-weight: bold;
  }
}
</style>

