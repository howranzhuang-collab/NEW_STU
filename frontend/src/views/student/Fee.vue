<template>
  <div class="fee-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">我的缴费清单</span>
          <el-button :icon="Refresh" @click="loadFees">刷新</el-button>
        </div>
      </template>

      <div v-if="loading" style="text-align: center; padding: 40px;">
        <el-icon class="is-loading" style="font-size: 32px;"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else>
        <!-- 欠费统计 -->
        <el-card class="summary-card" style="margin-bottom: 20px;">
          <template #header>
            <div class="section-header">
              <el-icon><Warning /></el-icon>
              <span>欠费统计</span>
            </div>
          </template>
          <div class="summary-content">
            <div class="summary-item">
              <span class="summary-label">欠费总额：</span>
              <span class="summary-value unpaid">¥{{ unpaidTotal.toFixed(2) }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">已缴总额：</span>
              <span class="summary-value paid">¥{{ paidTotal.toFixed(2) }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">费用总额：</span>
              <span class="summary-value">¥{{ totalAmount.toFixed(2) }}</span>
            </div>
          </div>
        </el-card>

        <!-- 筛选栏 -->
        <div class="filter-bar">
          <el-select
            v-model="filterStatus"
            placeholder="请选择状态"
            clearable
            style="width: 150px; margin-right: 10px"
            @change="handleFilter"
          >
            <el-option label="全部" value="" />
            <el-option label="未缴" value="UNPAID" />
            <el-option label="部分已缴" value="PARTIAL" />
            <el-option label="已缴清" value="PAID" />
          </el-select>
          <el-input
            v-model="searchKeyword"
            placeholder="请输入费用类型搜索"
            clearable
            style="width: 250px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <!-- 费用列表 -->
        <el-table
          :data="filteredFees"
          style="width: 100%; margin-top: 20px"
          stripe
        >
          <el-table-column prop="feeType" label="费用类型" width="150" />
          <el-table-column label="费用金额" width="150" align="right">
            <template #default="{ row }">
              <span>¥{{ row.amount?.toFixed(2) || '0.00' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="已缴金额" width="150" align="right">
            <template #default="{ row }">
              <span>¥{{ row.paidAmount?.toFixed(2) || '0.00' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="欠费金额" width="150" align="right">
            <template #default="{ row }">
              <span :style="{ color: getUnpaidAmount(row) > 0 ? '#f56c6c' : '#67c23a', fontWeight: 'bold' }">
                ¥{{ getUnpaidAmount(row).toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="dueDate" label="缴费截止日期" width="150" align="center">
            <template #default="{ row }">
              <span :class="{ 'overdue': isOverdue(row.dueDate) }">
                {{ formatDate(row.dueDate) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="paidDate" label="缴费日期" width="150" align="center">
            <template #default="{ row }">
              {{ formatDate(row.paidDate) || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remarks" label="备注" min-width="150" show-overflow-tooltip />
          <el-table-column prop="createTime" label="创建时间" width="180" align="center">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>

        <!-- 空状态 -->
        <el-empty
          v-if="filteredFees.length === 0"
          description="暂无费用记录"
          :image-size="120"
          style="padding: 40px 0;"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Search, Warning, Loading } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { getFeesByStudentId } from '../../api/fees'
import { getAllStudentRecords } from '../../api/student'
import type { Fees, StudentRecord } from '../../types/entity'

const userStore = useUserStore()
const loading = ref(false)
const fees = ref<Fees[]>([])
const students = ref<StudentRecord[]>([])
const searchKeyword = ref('')
const filterStatus = ref('')

// 注意：这里需要知道学生的ID，由于登录响应中没有userId
// 我们需要通过其他方式获取，这里先使用一个临时方案
// 实际项目中应该在后端提供一个根据当前登录用户获取信息的接口
const studentId = ref<number | null>(null)

// 计算统计数据
const unpaidTotal = computed(() => {
  return fees.value.reduce((sum, fee) => {
    return sum + (getUnpaidAmount(fee))
  }, 0)
})

const paidTotal = computed(() => {
  return fees.value.reduce((sum, fee) => {
    return sum + (fee.paidAmount || 0)
  }, 0)
})

const totalAmount = computed(() => {
  return fees.value.reduce((sum, fee) => {
    return sum + (fee.amount || 0)
  }, 0)
})

// 过滤后的费用列表
const filteredFees = computed(() => {
  let result = fees.value

  // 按状态筛选
  if (filterStatus.value) {
    result = result.filter(fee => fee.status === filterStatus.value)
  }

  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(fee =>
      fee.feeType?.toLowerCase().includes(keyword)
    )
  }

  return result
})

// 计算欠费金额
const getUnpaidAmount = (fee: Fees) => {
  return (fee.amount || 0) - (fee.paidAmount || 0)
}

// 检查是否逾期
const isOverdue = (dueDate: string) => {
  if (!dueDate) return false
  const due = new Date(dueDate)
  const now = new Date()
  return due < now
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'UNPAID': '未缴',
    'PARTIAL': '部分已缴',
    'PAID': '已缴清'
  }
  return statusMap[status] || status
}

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'UNPAID': 'danger',
    'PARTIAL': 'warning',
    'PAID': 'success'
  }
  return typeMap[status] || ''
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return date
}

// 加载费用列表
const loadFees = async () => {
  // 数据安全说明：
  // 当前实现存在数据泄露风险，因为无法安全地获取当前学生的ID
  // 为了确保"无其他学生数据泄露"，我们需要：
  // 1. 后端在登录响应中包含 userId（推荐）
  // 2. 或者后端提供 /api/auth/me 接口，根据JWT token返回当前用户信息（包含userId）
  // 3. 或者后端提供 /api/fees/current 接口，根据当前登录用户返回费用记录

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
    fees.value = await getFeesByStudentId(studentId.value)
  } catch (error) {
    console.error('加载费用列表失败:', error)
    ElMessage.error('加载费用列表失败')
  } finally {
    loading.value = false
  }
}

// 加载学生列表（用于查找当前学生）
const loadStudents = async () => {
  try {
    students.value = await getAllStudentRecords()
    
    // 临时方案：尝试通过username匹配学生记录
    // 注意：这需要确保学生只能访问自己的数据
    if (userStore.userInfo?.username) {
      // 这里需要根据实际情况匹配，比如username就是学号，或者有其他映射关系
      // 当前实现：如果无法安全匹配，则不设置studentId，避免数据泄露
      // TODO: 需要后端支持才能安全地获取studentId
    }
  } catch (error) {
    console.error('加载学生列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  // 搜索时不需要额外操作
}

// 筛选
const handleFilter = () => {
  // 筛选时不需要额外操作
}

onMounted(() => {
  loadStudents()
  // 注意：由于无法安全获取studentId，loadFees可能不会执行
  // 这是为了数据安全，避免泄露其他学生的数据
  // 一旦后端支持获取当前用户信息，可以取消注释：
  // loadFees()
})
</script>

<style scoped lang="scss">
.fee-container {
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

  .summary-card {
    .section-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: bold;
    }

    .summary-content {
      display: flex;
      gap: 40px;
      flex-wrap: wrap;

      .summary-item {
        display: flex;
        align-items: center;
        gap: 10px;

        .summary-label {
          font-size: 14px;
          color: #909399;
        }

        .summary-value {
          font-size: 24px;
          font-weight: bold;
          color: #303133;

          &.unpaid {
            color: #f56c6c;
          }

          &.paid {
            color: #67c23a;
          }
        }
      }
    }
  }

  .filter-bar {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
  }

  .overdue {
    color: #f56c6c;
    font-weight: bold;
  }
}
</style>

