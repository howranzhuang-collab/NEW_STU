<template>
  <div class="dorm-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">宿舍管理</span>
          <div>
            <el-button type="primary" :icon="Plus" @click="handleAddBuilding">添加宿舍楼</el-button>
            <el-button type="success" :icon="Plus" @click="handleAddRoom">添加房间</el-button>
          </div>
        </div>
      </template>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-select
          v-model="filterBuildingId"
          placeholder="请选择宿舍楼"
          clearable
          style="width: 250px; margin-right: 10px"
          @change="handleFilter"
        >
          <el-option label="全部" :value="null" />
          <el-option
            v-for="building in buildings"
            :key="building.id"
            :label="building.name"
            :value="building.id"
          />
        </el-select>
        <el-select
          v-model="filterRoomStatus"
          placeholder="请选择房间状态"
          clearable
          style="width: 150px; margin-right: 10px"
          @change="handleFilter"
        >
          <el-option label="全部" value="" />
          <el-option label="可用" value="AVAILABLE" />
          <el-option label="已满" value="FULL" />
          <el-option label="维护中" value="MAINTENANCE" />
        </el-select>
        <el-button :icon="Refresh" @click="loadBuildings">刷新</el-button>
      </div>

      <!-- 宿舍楼和房间列表 -->
      <div v-loading="loading" class="dorm-list">
        <el-collapse v-model="activeBuildings" accordion>
          <el-collapse-item
            v-for="building in filteredBuildings"
            :key="building.id"
            :name="building.id"
            @change="handleBuildingExpand(building.id)"
          >
            <template #title>
              <div class="building-header">
                <el-icon><HomeFilled /></el-icon>
                <span class="building-name">{{ building.name }}</span>
                <el-tag :type="getBuildingStatusType(building.status)" size="small" style="margin-left: 10px;">
                  {{ getBuildingStatusText(building.status) }}
                </el-tag>
                <span class="building-info">
                  {{ building.totalRooms }} 间房间
                  <span v-if="building.address" style="margin-left: 10px; color: #909399;">
                    {{ building.address }}
                  </span>
                </span>
              </div>
            </template>

            <!-- 房间列表 -->
            <div class="rooms-container">
              <el-table :data="buildingRooms[building.id] || []" stripe style="width: 100%">
                <el-table-column prop="roomNumber" label="房间号" width="120" />
                <el-table-column prop="floor" label="楼层" width="80" align="center" />
                <el-table-column prop="roomType" label="类型" width="100" align="center">
                  <template #default="{ row }">
                    {{ getRoomTypeText(row.roomType) }}
                  </template>
                </el-table-column>
                <el-table-column label="容量" width="120" align="center">
                  <template #default="{ row }">
                    <span :style="{ color: row.currentOccupancy >= row.capacity ? '#f56c6c' : '#67c23a' }">
                      {{ row.currentOccupancy }} / {{ row.capacity }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getRoomStatusType(row.status)" size="small">
                      {{ getRoomStatusText(row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="remarks" label="备注" min-width="150" show-overflow-tooltip />
                <el-table-column label="操作" width="200" align="center" fixed="right">
                  <template #default="{ row }">
                    <el-button type="primary" link size="small" @click="handleAllocate(building, row)">
                      分配
                    </el-button>
                    <el-button type="info" link size="small" @click="handleViewRoommates(building, row)">
                      查看室友
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-collapse-item>
        </el-collapse>

        <el-empty
          v-if="!loading && filteredBuildings.length === 0"
          description="暂无宿舍楼"
          :image-size="120"
          style="padding: 40px 0;"
        />
      </div>
    </el-card>

    <!-- 添加/编辑宿舍楼对话框 -->
    <el-dialog
      v-model="buildingDialogVisible"
      :title="buildingDialogTitle"
      width="600px"
      @close="handleBuildingDialogClose"
    >
      <el-form
        ref="buildingFormRef"
        :model="buildingForm"
        :rules="buildingFormRules"
        label-width="120px"
      >
        <el-form-item label="宿舍楼名称" prop="name">
          <el-input v-model="buildingForm.name" placeholder="请输入宿舍楼名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="buildingForm.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="总楼层数" prop="totalFloors">
          <el-input-number
            v-model="buildingForm.totalFloors"
            :min="1"
            style="width: 100%"
            placeholder="请输入总楼层数"
          />
        </el-form-item>
        <el-form-item label="总房间数" prop="totalRooms">
          <el-input-number
            v-model="buildingForm.totalRooms"
            :min="0"
            style="width: 100%"
            placeholder="请输入总房间数"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="buildingForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="使用中" value="ACTIVE" />
            <el-option label="维护中" value="MAINTENANCE" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="buildingForm.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buildingDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSaveBuilding">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加房间对话框 -->
    <el-dialog
      v-model="roomDialogVisible"
      title="添加房间"
      width="600px"
      @close="handleRoomDialogClose"
    >
      <el-form
        ref="roomFormRef"
        :model="roomForm"
        :rules="roomFormRules"
        label-width="120px"
      >
        <el-form-item label="宿舍楼" prop="buildingId">
          <el-select
            v-model="roomForm.buildingId"
            placeholder="请选择宿舍楼"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="building in buildings"
              :key="building.id"
              :label="building.name"
              :value="building.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="房间号" prop="roomNumber">
          <el-input v-model="roomForm.roomNumber" placeholder="请输入房间号" />
        </el-form-item>
        <el-form-item label="楼层" prop="floor">
          <el-input-number
            v-model="roomForm.floor"
            :min="1"
            style="width: 100%"
            placeholder="请输入楼层"
          />
        </el-form-item>
        <el-form-item label="房间容量" prop="capacity">
          <el-input-number
            v-model="roomForm.capacity"
            :min="1"
            :max="6"
            style="width: 100%"
            placeholder="请输入房间容量（最多6人）"
          />
        </el-form-item>
        <el-form-item label="房间类型" prop="roomType">
          <el-select v-model="roomForm.roomType" placeholder="请选择房间类型" style="width: 100%">
            <el-option label="单人间" value="SINGLE" />
            <el-option label="双人间" value="DOUBLE" />
            <el-option label="四人间" value="QUAD" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="roomForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="可用" value="AVAILABLE" />
            <el-option label="已满" value="FULL" />
            <el-option label="维护中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="roomForm.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roomDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSaveRoom">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配宿舍对话框 -->
    <el-dialog
      v-model="allocateDialogVisible"
      title="分配宿舍"
      width="600px"
      @close="handleAllocateDialogClose"
    >
      <el-form
        ref="allocateFormRef"
        :model="allocateForm"
        :rules="allocateFormRules"
        label-width="120px"
      >
        <el-form-item label="宿舍楼">
          <el-input :value="currentBuilding?.name" disabled />
        </el-form-item>
        <el-form-item label="房间号">
          <el-input :value="currentRoom?.roomNumber" disabled />
        </el-form-item>
        <el-form-item label="房间容量">
          <el-input :value="`${currentRoom?.currentOccupancy || 0} / ${currentRoom?.capacity || 0}`" disabled />
        </el-form-item>
        <el-form-item label="学生" prop="studentId">
          <el-select
            v-model="allocateForm.studentId"
            placeholder="请选择学生"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="student in students"
              :key="student.studentId"
              :label="`${student.studentNumber} - ${student.name}`"
              :value="student.studentId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入住日期" prop="checkInDate">
          <el-date-picker
            v-model="allocateForm.checkInDate"
            type="date"
            placeholder="请选择入住日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="allocateForm.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="allocateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="allocateLoading" @click="handleSaveAllocate">确认分配</el-button>
      </template>
    </el-dialog>

    <!-- 查看室友对话框 -->
    <el-dialog
      v-model="roommatesDialogVisible"
      title="室友信息"
      width="800px"
    >
      <el-table :data="roommatesList" stripe style="width: 100%">
        <el-table-column prop="studentNumber" label="学号" width="150" />
        <el-table-column prop="name" label="姓名" width="120" />
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
        <el-table-column prop="remarks" label="备注" min-width="150" show-overflow-tooltip />
      </el-table>
      <template #footer>
        <el-button @click="roommatesDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Refresh, HomeFilled } from '@element-plus/icons-vue'
import {
  getAllBuildings,
  createBuilding,
  getRoomsByBuildingId,
  createRoom,
  allocateDorm,
  getAllocationsByRoomId
} from '../../../api/dorm'
import { getAllStudentRecords } from '../../../api/student'
import type { DormBuilding, DormRoom, StudentRecord } from '../../../types/entity'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false)
const buildings = ref<DormBuilding[]>([])
const students = ref<StudentRecord[]>([])
const buildingRooms = ref<Record<number, DormRoom[]>>({})
const activeBuildings = ref<number[]>([])
const filterBuildingId = ref<number | null>(null)
const filterRoomStatus = ref('')

// 宿舍楼对话框
const buildingDialogVisible = ref(false)
const buildingDialogTitle = ref('添加宿舍楼')
const buildingFormRef = ref<FormInstance>()
const saveLoading = ref(false)
const buildingForm = ref({
  name: '',
  address: '',
  totalFloors: undefined as number | undefined,
  totalRooms: 0,
  status: 'ACTIVE',
  remarks: ''
})

const buildingFormRules: FormRules = {
  name: [{ required: true, message: '请输入宿舍楼名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 房间对话框
const roomDialogVisible = ref(false)
const roomFormRef = ref<FormInstance>()
const roomForm = ref({
  buildingId: undefined as number | undefined,
  roomNumber: '',
  floor: undefined as number | undefined,
  capacity: 4,
  roomType: 'QUAD',
  status: 'AVAILABLE',
  remarks: ''
})

const roomFormRules: FormRules = {
  buildingId: [{ required: true, message: '请选择宿舍楼', trigger: 'change' }],
  roomNumber: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入房间容量', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 分配对话框
const allocateDialogVisible = ref(false)
const allocateFormRef = ref<FormInstance>()
const allocateLoading = ref(false)
const currentBuilding = ref<DormBuilding | null>(null)
const currentRoom = ref<DormRoom | null>(null)
const allocateForm = ref({
  studentId: undefined as number | undefined,
  roomId: undefined as number | undefined,
  checkInDate: '',
  remarks: ''
})

const allocateFormRules: FormRules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  checkInDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }]
}

// 室友对话框
const roommatesDialogVisible = ref(false)
const roommatesList = ref<any[]>([])

// 过滤后的宿舍楼列表
const filteredBuildings = computed(() => {
  let result = buildings.value

  if (filterBuildingId.value !== null) {
    result = result.filter(building => building.id === filterBuildingId.value)
  }

  return result
})

// 获取宿舍楼状态文本
const getBuildingStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'ACTIVE': '使用中',
    'MAINTENANCE': '维护中',
    'CLOSED': '已关闭'
  }
  return statusMap[status] || status
}

// 获取宿舍楼状态类型
const getBuildingStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'ACTIVE': 'success',
    'MAINTENANCE': 'warning',
    'CLOSED': 'danger'
  }
  return typeMap[status] || ''
}

// 获取房间类型文本
const getRoomTypeText = (roomType?: string) => {
  const typeMap: Record<string, string> = {
    'SINGLE': '单人间',
    'DOUBLE': '双人间',
    'QUAD': '四人间'
  }
  return roomType ? typeMap[roomType] || roomType : '-'
}

// 获取房间状态文本
const getRoomStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'AVAILABLE': '可用',
    'FULL': '已满',
    'MAINTENANCE': '维护中'
  }
  return statusMap[status] || status
}

// 获取房间状态类型
const getRoomStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'AVAILABLE': 'success',
    'FULL': 'danger',
    'MAINTENANCE': 'warning'
  }
  return typeMap[status] || ''
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return date
}

// 加载宿舍楼列表
const loadBuildings = async () => {
  loading.value = true
  try {
    buildings.value = await getAllBuildings()
  } catch (error) {
    console.error('加载宿舍楼列表失败:', error)
    ElMessage.error('加载宿舍楼列表失败')
  } finally {
    loading.value = false
  }
}

// 加载学生列表
const loadStudents = async () => {
  try {
    students.value = await getAllStudentRecords()
  } catch (error) {
    console.error('加载学生列表失败:', error)
    ElMessage.error('加载学生列表失败')
  }
}

// 加载房间列表
const loadRooms = async (buildingId: number) => {
  try {
    const rooms = await getRoomsByBuildingId(buildingId)
    buildingRooms.value[buildingId] = rooms
  } catch (error) {
    console.error('加载房间列表失败:', error)
    ElMessage.error('加载房间列表失败')
  }
}

// 宿舍楼展开
const handleBuildingExpand = (buildingId: number) => {
  if (!buildingRooms.value[buildingId]) {
    loadRooms(buildingId)
  }
}

// 筛选
const handleFilter = () => {
  // 筛选时不需要额外操作
}

// 添加宿舍楼
const handleAddBuilding = () => {
  buildingDialogTitle.value = '添加宿舍楼'
  buildingForm.value = {
    name: '',
    address: '',
    totalFloors: undefined,
    totalRooms: 0,
    status: 'ACTIVE',
    remarks: ''
  }
  buildingDialogVisible.value = true
}

// 保存宿舍楼
const handleSaveBuilding = async () => {
  if (!buildingFormRef.value) return

  await buildingFormRef.value.validate(async (valid) => {
    if (!valid) return

    saveLoading.value = true
    try {
      await createBuilding(buildingForm.value)
      ElMessage.success('添加宿舍楼成功')
      buildingDialogVisible.value = false
      await loadBuildings()
    } catch (error) {
      console.error('保存宿舍楼失败:', error)
      // 错误消息已在拦截器中显示
    } finally {
      saveLoading.value = false
    }
  })
}

// 宿舍楼对话框关闭
const handleBuildingDialogClose = () => {
  buildingFormRef.value?.resetFields()
}

// 添加房间
const handleAddRoom = () => {
  roomForm.value = {
    buildingId: undefined,
    roomNumber: '',
    floor: undefined,
    capacity: 4,
    roomType: 'QUAD',
    status: 'AVAILABLE',
    remarks: ''
  }
  roomDialogVisible.value = true
}

// 保存房间
const handleSaveRoom = async () => {
  if (!roomFormRef.value) return

  await roomFormRef.value.validate(async (valid) => {
    if (!valid) return

    saveLoading.value = true
    try {
      await createRoom(roomForm.value)
      ElMessage.success('添加房间成功')
      roomDialogVisible.value = false
      // 如果该宿舍楼的房间列表已加载，则刷新
      if (roomForm.value.buildingId && buildingRooms.value[roomForm.value.buildingId]) {
        await loadRooms(roomForm.value.buildingId)
      }
    } catch (error) {
      console.error('保存房间失败:', error)
      // 错误消息已在拦截器中显示
    } finally {
      saveLoading.value = false
    }
  })
}

// 房间对话框关闭
const handleRoomDialogClose = () => {
  roomFormRef.value?.resetFields()
}

// 分配宿舍
const handleAllocate = (_building: DormBuilding, room: DormRoom) => {
  // 检查房间是否已满
  if (room.currentOccupancy >= room.capacity) {
    ElMessage.error('宿舍已满，无法分配')
    return
  }

  currentBuilding.value = _building
  currentRoom.value = room
  const today = new Date().toISOString().split('T')[0]
  allocateForm.value = {
    studentId: undefined,
    roomId: room.id,
    checkInDate: today || '',
    remarks: ''
  }
  allocateDialogVisible.value = true
}

// 保存分配
const handleSaveAllocate = async () => {
  if (!allocateFormRef.value) return

  await allocateFormRef.value.validate(async (valid) => {
    if (!valid) return

    allocateLoading.value = true
    try {
      await allocateDorm(allocateForm.value)
      ElMessage.success('分配宿舍成功')
      allocateDialogVisible.value = false
      // 刷新房间列表
      if (currentRoom.value?.buildingId) {
        await loadRooms(currentRoom.value.buildingId)
      }
    } catch (error: any) {
      console.error('分配宿舍失败:', error)
      // 检查是否是容量错误
      if (error?.message?.includes('已满') || error?.message?.includes('容量')) {
        ElMessage.error('宿舍已满，无法分配')
      }
      // 其他错误消息已在拦截器中显示
    } finally {
      allocateLoading.value = false
    }
  })
}

// 分配对话框关闭
const handleAllocateDialogClose = () => {
  allocateFormRef.value?.resetFields()
  currentBuilding.value = null
  currentRoom.value = null
}

// 查看室友
const handleViewRoommates = async (_building: DormBuilding, room: DormRoom) => {
  try {
    const allocations = await getAllocationsByRoomId(room.id)
    // 获取学生信息
    const roommates = allocations.map(allocation => {
      const student = students.value.find(s => s.studentId === allocation.studentId)
      return {
        ...allocation,
        studentNumber: student?.studentNumber || '',
        name: student?.name || `学生ID: ${allocation.studentId}`
      }
    })
    roommatesList.value = roommates
    roommatesDialogVisible.value = true
  } catch (error) {
    console.error('获取室友信息失败:', error)
    ElMessage.error('获取室友信息失败')
  }
}

onMounted(() => {
  loadBuildings()
  loadStudents()
})
</script>

<style scoped lang="scss">
.dorm-list-container {
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

  .filter-bar {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    flex-wrap: wrap;
  }

  .dorm-list {
    .building-header {
      display: flex;
      align-items: center;
      gap: 10px;
      flex: 1;

      .building-name {
        font-weight: bold;
        font-size: 16px;
      }

      .building-info {
        margin-left: auto;
        color: #909399;
        font-size: 14px;
      }
    }

    .rooms-container {
      padding: 20px;
      background-color: #f5f7fa;
      border-radius: 4px;
    }
  }
}
</style>

