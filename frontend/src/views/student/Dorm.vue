<template>
  <div class="dorm-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">我的住宿信息</span>
          <el-button :icon="Refresh" @click="loadDormInfo">刷新</el-button>
        </div>
      </template>

      <div v-if="loading" style="text-align: center; padding: 40px;">
        <el-icon class="is-loading" style="font-size: 32px;"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="!allocation" style="text-align: center; padding: 40px;">
        <el-empty description="暂无住宿信息" :image-size="120" />
      </div>

      <div v-else>
        <!-- 住宿信息卡片 -->
        <el-card class="info-card" style="margin-bottom: 20px;">
          <template #header>
            <div class="section-header">
              <el-icon><HomeFilled /></el-icon>
              <span>住宿信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="宿舍楼">{{ buildingInfo?.name || '-' }}</el-descriptions-item>
            <el-descriptions-item label="房间号">{{ roomInfo?.roomNumber || '-' }}</el-descriptions-item>
            <el-descriptions-item label="楼层">{{ roomInfo?.floor || '-' }}</el-descriptions-item>
              <el-descriptions-item label="房间类型">
              {{ getRoomTypeText(roomInfo?.roomType || '') }}
            </el-descriptions-item>
            <el-descriptions-item label="房间容量">
              {{ roomInfo?.capacity || '-' }} 人
            </el-descriptions-item>
            <el-descriptions-item label="当前入住">
              {{ roomInfo?.currentOccupancy || 0 }} / {{ roomInfo?.capacity || 0 }} 人
            </el-descriptions-item>
            <el-descriptions-item label="入住日期">
              {{ allocation.checkInDate ? formatDate(allocation.checkInDate) : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="退房日期">
              {{ allocation.checkOutDate ? formatDate(allocation.checkOutDate) : '未退房' }}
            </el-descriptions-item>
            <el-descriptions-item label="住宿状态">
              <el-tag :type="allocation.status === 'ACTIVE' ? 'success' : 'info'">
                {{ getStatusText(allocation.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">
              {{ allocation.remarks || '无' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 室友信息卡片 -->
        <el-card class="info-card">
          <template #header>
            <div class="section-header">
              <el-icon><User /></el-icon>
              <span>室友信息</span>
            </div>
          </template>
          <el-table :data="roommates" stripe style="width: 100%">
            <el-table-column prop="studentNumber" label="学号" width="150" />
            <el-table-column prop="name" label="姓名" width="120" />
            <el-table-column prop="gender" label="性别" width="80" align="center">
              <template #default="{ row }">
                {{ row.gender === 'MALE' ? '男' : '女' }}
              </template>
            </el-table-column>
            <el-table-column prop="nationality" label="国籍" width="120" />
            <el-table-column prop="major" label="专业" min-width="150" />
            <el-table-column prop="checkInDate" label="入住日期" width="120" align="center">
              <template #default="{ row }">
                {{ formatDate(row.checkInDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                  {{ row.status === 'ACTIVE' ? '在住' : '已退房' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty
            v-if="roommates.length === 0"
            description="暂无室友"
            :image-size="80"
            style="padding: 20px 0;"
          />
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, HomeFilled, User, Loading } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { getAllocationsByStudentId, getAllocationsByRoomId, getRoomById, getBuildingById } from '../../api/dorm'
import { getAllStudentRecords } from '../../api/student'
import type { DormAllocation, DormRoom, DormBuilding, StudentRecord } from '../../types/entity'

const userStore = useUserStore()
const loading = ref(false)
const allocation = ref<DormAllocation | null>(null)
const roomInfo = ref<DormRoom | null>(null)
const buildingInfo = ref<DormBuilding | null>(null)
const roommates = ref<any[]>([])
const students = ref<StudentRecord[]>([])

// 注意：这里需要知道学生的ID，由于登录响应中没有userId
// 我们需要通过其他方式获取，这里先使用一个临时方案
// 实际项目中应该在后端提供一个根据当前登录用户获取信息的接口
const studentId = ref<number | null>(null)

// 获取房间类型文本
const getRoomTypeText = (roomType?: string) => {
  const typeMap: Record<string, string> = {
    'SINGLE': '单人间',
    'DOUBLE': '双人间',
    'QUAD': '四人间'
  }
  return roomType ? typeMap[roomType] || roomType : '-'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'ACTIVE': '在住',
    'COMPLETED': '已退房',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return date
}

// 加载住宿信息
const loadDormInfo = async () => {
  // 数据安全说明：
  // 当前实现存在数据泄露风险，因为无法安全地获取当前学生的ID
  // 为了确保"无其他学生数据泄露"，我们需要：
  // 1. 后端在登录响应中包含 userId（推荐）
  // 2. 或者后端提供 /api/auth/me 接口，根据JWT token返回当前用户信息（包含userId）
  // 3. 或者后端提供 /api/dorms/allocations/current 接口，根据当前登录用户返回住宿分配记录

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
    // 获取学生的住宿分配记录
    const allocations = await getAllocationsByStudentId(studentId.value)
    if (allocations && allocations.length > 0) {
      // 获取最新的在住记录
      allocation.value = allocations.find(a => a.status === 'ACTIVE') || allocations[0] || null

      if (allocation.value) {
        // 获取房间信息
        roomInfo.value = await getRoomById(allocation.value.roomId)

        if (roomInfo.value) {
          // 获取宿舍楼信息
          buildingInfo.value = await getBuildingById(roomInfo.value.buildingId)

          // 获取室友信息
          await loadRoommates()
        }
      }
    }
  } catch (error) {
    console.error('加载住宿信息失败:', error)
    ElMessage.error('加载住宿信息失败')
  } finally {
    loading.value = false
  }
}

// 加载室友信息
const loadRoommates = async () => {
  if (!allocation.value) return

  try {
    // 获取同一房间的所有分配记录
    const allocations = await getAllocationsByRoomId(allocation.value.roomId)
    
    // 过滤掉自己，并获取学生信息
    const roommateAllocations = allocations.filter(a => a.studentId !== studentId.value)
    const roommateList = roommateAllocations.map(alloc => {
      const student = students.value.find(s => s.studentId === alloc.studentId)
      return {
        ...alloc,
        studentNumber: student?.studentNumber || '',
        name: student?.name || `学生ID: ${alloc.studentId}`,
        gender: student?.gender || '',
        nationality: student?.nationality || '',
        major: student?.major || ''
      }
    })
    roommates.value = roommateList
  } catch (error) {
    console.error('加载室友信息失败:', error)
    ElMessage.error('加载室友信息失败')
  }
}

// 加载学生列表（用于查找室友信息）
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

onMounted(() => {
  loadStudents()
  // 注意：由于无法安全获取studentId，loadDormInfo可能不会执行
  // 这是为了数据安全，避免泄露其他学生的数据
  // 一旦后端支持获取当前用户信息，可以取消注释来调用：
  // loadDormInfo()
})
</script>

<style scoped lang="scss">
.dorm-container {
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

  .info-card {
    .section-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: bold;
    }
  }
}
</style>

