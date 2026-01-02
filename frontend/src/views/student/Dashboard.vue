<template>
  <div class="dashboard-container">
    <h2 class="dashboard-title">我的首页</h2>

    <!-- 数据安全提示 -->
    <el-alert
      v-if="!studentRecord"
      title="数据安全提示"
      type="warning"
      :closable="false"
      show-icon
      style="margin-bottom: 20px;"
    >
      <template #default>
        <p style="margin: 5px 0;">为了确保数据安全，学生仪表盘需要后端支持才能正常显示。</p>
        <p style="margin: 5px 0;">请确保后端满足以下条件之一：</p>
        <ul style="margin: 10px 0; padding-left: 20px;">
          <li>登录响应中包含 userId</li>
          <li>提供 /api/auth/me 接口返回当前用户信息（包含 userId）</li>
          <li>提供 /api/students/records/current 接口返回当前学生的学籍记录</li>
        </ul>
        <p style="margin: 5px 0;">这样可以确保学生只能看到自己的信息，不会泄露其他学生的数据。</p>
      </template>
    </el-alert>

    <!-- 个人信息卡片 -->
    <el-card class="info-card" v-if="studentRecord">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学号">{{ studentRecord.studentNumber }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ studentRecord.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ studentRecord.gender === 'MALE' ? '男' : '女' }}</el-descriptions-item>
        <el-descriptions-item label="国籍">{{ studentRecord.nationality }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ studentRecord.major }}</el-descriptions-item>
        <el-descriptions-item label="年级">{{ studentRecord.grade }}</el-descriptions-item>
        <el-descriptions-item label="学籍状态">
          <el-tag :type="getStatusType(studentRecord.status)">
            {{ getStatusText(studentRecord.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="入学日期">{{ studentRecord.enrollmentDate }}</el-descriptions-item>
        <el-descriptions-item label="联系电话" :span="2">{{ studentRecord.phone || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="电子邮箱" :span="2">{{ studentRecord.email || '未填写' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-row :gutter="20" class="info-cards">
      <!-- 欠费提醒 -->
      <el-col :xs="24" :sm="12" :md="12">
        <el-card class="reminder-card" v-if="unpaidInfo">
          <template #header>
            <div class="card-header">
              <el-icon><Warning /></el-icon>
              <span>欠费提醒</span>
            </div>
          </template>
          <div class="reminder-content">
            <div class="reminder-amount">
              <span class="amount-label">欠费总额：</span>
              <span class="amount-value">¥{{ unpaidInfo.totalUnpaid?.toFixed(2) || '0.00' }}</span>
            </div>
            <el-button 
              v-if="unpaidInfo.totalUnpaid > 0" 
              type="primary" 
              @click="$router.push('/student/fees')"
              style="margin-top: 10px;"
            >
              查看详情
            </el-button>
            <el-empty 
              v-else 
              description="无欠费" 
              :image-size="80"
              style="padding: 20px 0;"
            />
          </div>
        </el-card>
      </el-col>

      <!-- 住宿信息 -->
      <el-col :xs="24" :sm="12" :md="12">
        <el-card class="reminder-card" v-if="dormAllocation">
          <template #header>
            <div class="card-header">
              <el-icon><HomeFilled /></el-icon>
              <span>住宿信息</span>
            </div>
          </template>
          <div class="reminder-content">
            <div v-if="dormAllocation && dormAllocation.length > 0">
              <p><strong>入住日期：</strong>{{ dormAllocation[0]?.checkInDate }}</p>
              <p v-if="dormAllocation[0]?.checkOutDate">
                <strong>退房日期：</strong>{{ dormAllocation[0].checkOutDate }}
              </p>
              <el-tag :type="dormAllocation[0]?.status === 'ACTIVE' ? 'success' : 'info'">
                {{ dormAllocation[0]?.status === 'ACTIVE' ? '在住' : '已退房' }}
              </el-tag>
            </div>
            <el-empty 
              v-else 
              description="暂无住宿信息" 
              :image-size="80"
              style="padding: 20px 0;"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 签证到期提醒 -->
    <el-card class="reminder-card" style="margin-top: 20px;" v-if="visaInfo">
      <template #header>
        <div class="card-header">
          <el-icon><Tickets /></el-icon>
          <span>签证信息</span>
        </div>
      </template>
      <div class="reminder-content" v-if="visaInfo.length > 0">
        <el-table :data="visaInfo" style="width: 100%">
          <el-table-column prop="visaType" label="签证类型" width="150" />
          <el-table-column prop="visaNumber" label="签证号" width="150" />
          <el-table-column prop="issueDate" label="签发日期" width="120" />
          <el-table-column prop="expiryDate" label="到期日期" width="120">
            <template #default="{ row }">
              <span :class="{ 'expiring-soon': isExpiringSoon(row.expiryDate) }">
                {{ row.expiryDate }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getVisaStatusType(row.expiryDate)">
                {{ getVisaStatusText(row.expiryDate) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-empty 
        v-else 
        description="暂无签证信息" 
        :image-size="80"
        style="padding: 20px 0;"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { getStudentRecordByStudentId } from '../../api/student'
import { getUnpaidFeesByStudentId } from '../../api/fees'
import { getAllocationsByStudentId } from '../../api/dorm'
import { getVisasByStudentId } from '../../api/visa'
import type { StudentRecord, DormAllocation, Visas } from '../../types/entity'
import { Warning, HomeFilled, Tickets } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const studentRecord = ref<StudentRecord | null>(null)
const unpaidInfo = ref<{ totalUnpaid: number; fees: any[] } | null>(null)
const dormAllocation = ref<DormAllocation[] | null>(null)
const visaInfo = ref<Visas[] | null>(null)

// 注意：这里需要知道学生的ID，由于登录响应中没有userId
// 我们需要通过其他方式获取，这里先使用一个临时方案
// 实际项目中应该在后端提供一个根据当前登录用户获取信息的接口
const studentId = ref<number | null>(null)

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'ACTIVE': '在读',
    'GRADUATED': '已毕业',
    'SUSPENDED': '休学',
    'DROPPED': '退学'
  }
  return statusMap[status] || status
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'ACTIVE': 'success',
    'GRADUATED': 'info',
    'SUSPENDED': 'warning',
    'DROPPED': 'danger'
  }
  return typeMap[status] || ''
}

// 检查签证是否即将到期（30天内）
const isExpiringSoon = (expiryDate: string) => {
  const expiry = new Date(expiryDate)
  const now = new Date()
  const diffTime = expiry.getTime() - now.getTime()
  const diffDays = diffTime / (1000 * 60 * 60 * 24)
  return diffDays > 0 && diffDays <= 30
}

const getVisaStatusType = (expiryDate: string) => {
  const expiry = new Date(expiryDate)
  const now = new Date()
  if (expiry < now) return 'danger'
  if (isExpiringSoon(expiryDate)) return 'warning'
  return 'success'
}

const getVisaStatusText = (expiryDate: string) => {
  const expiry = new Date(expiryDate)
  const now = new Date()
  if (expiry < now) return '已过期'
  if (isExpiringSoon(expiryDate)) return '即将到期'
  return '有效'
}

// 加载学生信息
// 注意：这里需要一个方法获取当前学生的ID
// 由于后端没有提供根据username获取用户信息的接口，这里先使用一个占位逻辑
// 实际应该：1. 后端在登录时返回userId，2. 或者提供一个获取当前用户信息的接口
// 此函数保留供将来使用，一旦后端支持获取userId，即可调用此函数加载数据
// @ts-ignore - 保留供将来使用
const loadStudentInfo = async () => {
  if (!studentId.value) {
    ElMessage.warning('无法获取学生ID，请重新登录')
    return
  }

  try {
    // 并行加载所有数据
    const [record, unpaid, dorm, visas] = await Promise.all([
      getStudentRecordByStudentId(studentId.value),
      getUnpaidFeesByStudentId(studentId.value).catch(() => ({ totalUnpaid: 0, fees: [] })),
      getAllocationsByStudentId(studentId.value).catch(() => []),
      getVisasByStudentId(studentId.value).catch(() => [])
    ])

    studentRecord.value = record
    unpaidInfo.value = unpaid
    dormAllocation.value = dorm
    visaInfo.value = visas
  } catch (error) {
    console.error('加载学生信息失败:', error)
    ElMessage.error('加载学生信息失败')
  }
}

onMounted(async () => {
  // 数据安全说明：
  // 当前实现存在数据泄露风险，因为无法安全地获取当前学生的ID
  // 为了确保"无其他学生数据泄露"，我们需要：
  // 1. 后端在登录响应中包含 userId（推荐）
  // 2. 或者后端提供 /api/auth/me 接口，根据JWT token返回当前用户信息（包含userId）
  // 3. 或者后端提供 /api/students/records/current 接口，根据当前登录用户返回学籍记录
  
  // 当前临时方案：尝试通过学号或其他唯一标识匹配
  // 注意：这需要确保学生只能访问自己的数据
  // 如果无法安全获取studentId，则不加载数据，避免数据泄露
  
  if (!userStore.userInfo?.username) {
    ElMessage.error('用户信息不完整，请重新登录')
    return
  }

  // 由于无法安全地获取当前学生的ID，这里显示提示信息
  // 实际部署时，应该要求后端提供以下接口之一：
  // 1. 登录响应包含 userId
  // 2. /api/auth/me 接口返回当前用户信息
  // 3. /api/students/records/current 接口返回当前学生的学籍记录
  
  ElMessage.warning({
    message: '学生仪表盘需要获取学生ID。为了数据安全，请确保后端在登录响应中包含userId，或提供获取当前用户信息的接口。',
    duration: 5000
  })
  
  // 临时方案：如果后端有根据username获取用户信息的接口，可以在这里调用
  // 但当前后端没有这样的接口，所以不执行数据加载，避免数据泄露
  // TODO: 需要后端支持才能安全地加载学生数据
  
  // 注意：loadStudentInfo 函数已定义但当前未调用，这是为了数据安全
  // 一旦后端支持获取userId，可以取消下面的注释来调用：
  // if (studentId.value) {
  //   await loadStudentInfo()
  // }
})
</script>

<style scoped lang="scss">
.dashboard-container {
  .dashboard-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: bold;
    color: #303133;
  }

  .info-card {
    margin-bottom: 20px;
  }

  .info-cards {
    margin-bottom: 20px;
  }

  .reminder-card {
    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: bold;
    }

    .reminder-content {
      .reminder-amount {
        font-size: 18px;
        padding: 20px 0;

        .amount-label {
          color: #909399;
        }

        .amount-value {
          font-size: 24px;
          font-weight: bold;
          color: #f56c6c;
          margin-left: 10px;
        }
      }
    }
  }

  .expiring-soon {
    color: #e6a23c;
    font-weight: bold;
  }
}
</style>

