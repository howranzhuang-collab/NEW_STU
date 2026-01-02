<template>
  <div class="application-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">申请审核管理</span>
        </div>
      </template>

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
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已拒绝" value="REJECTED" />
        </el-select>
        <el-select
          v-model="filterProjectId"
          placeholder="请选择项目"
          clearable
          style="width: 250px; margin-right: 10px"
          @change="handleFilter"
        >
          <el-option label="全部项目" :value="null" />
          <el-option
            v-for="project in projects"
            :key="project.id"
            :label="project.title"
            :value="project.id"
          />
        </el-select>
        <el-button type="primary" :icon="Refresh" @click="loadApplications">刷新</el-button>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="paginatedApplications"
        style="width: 100%; margin-top: 20px"
        stripe
      >
        <el-table-column prop="id" label="申请ID" width="80" />
        <el-table-column prop="projectId" label="项目名称" min-width="200">
          <template #default="{ row }">
            {{ getProjectTitle(row.projectId) }}
          </template>
        </el-table-column>
        <el-table-column prop="studentId" label="学生ID" width="120" align="center" />
        <el-table-column prop="applicationDate" label="申请日期" width="120" align="center">
          <template #default="{ row }">
            {{ formatDate(row.applicationDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="filePath" label="附件" width="100" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.filePath"
              type="primary"
              link
              size="small"
              @click="handleDownloadFile(row.filePath)"
            >
              下载
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="remarks" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="success"
              link
              size="small"
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="danger"
              link
              size="small"
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredApplications.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="申请详情"
      width="600px"
    >
      <el-descriptions :column="2" border v-if="currentApplication">
        <el-descriptions-item label="申请ID">{{ currentApplication.id }}</el-descriptions-item>
        <el-descriptions-item label="项目名称">
          {{ getProjectTitle(currentApplication.projectId) }}
        </el-descriptions-item>
        <el-descriptions-item label="学生ID">{{ currentApplication.studentId }}</el-descriptions-item>
        <el-descriptions-item label="申请日期">
          {{ formatDate(currentApplication.applicationDate) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentApplication.status)">
            {{ getStatusText(currentApplication.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="附件">
          <el-button
            v-if="currentApplication.filePath"
            type="primary"
            link
            size="small"
            @click="handleDownloadFile(currentApplication.filePath)"
          >
            下载文件
          </el-button>
          <span v-else>无</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ currentApplication.remarks || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ formatDateTime(currentApplication.createTime) }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getAllProjects, getApplicationsByProjectId, updateApplicationStatus } from '../../../api/admission'
import type { AdmissionProject, Application } from '../../../types/entity'

const loading = ref(false)
const projects = ref<AdmissionProject[]>([])
const allApplications = ref<Application[]>([])
const filterStatus = ref('PENDING') // 默认显示待审核
const filterProjectId = ref<number | null>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const detailDialogVisible = ref(false)
const currentApplication = ref<Application | null>(null)

// 项目映射（用于快速查找项目名称）
const projectMap = computed(() => {
  const map: Record<number, string> = {}
  projects.value.forEach(project => {
    map[project.id] = project.title
  })
  return map
})

// 获取项目标题
const getProjectTitle = (projectId: number) => {
  return projectMap.value[projectId] || `项目ID: ${projectId}`
}

// 过滤后的申请列表
const filteredApplications = computed(() => {
  let result = allApplications.value

  // 按状态过滤
  if (filterStatus.value) {
    result = result.filter(app => app.status === filterStatus.value)
  }

  // 按项目过滤
  if (filterProjectId.value !== null) {
    result = result.filter(app => app.projectId === filterProjectId.value)
  }

  // 按申请日期倒序排序
  result = [...result].sort((a, b) => {
    const dateA = new Date(a.applicationDate).getTime()
    const dateB = new Date(b.applicationDate).getTime()
    return dateB - dateA
  })

  return result
})

// 分页后的申请列表
const paginatedApplications = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredApplications.value.slice(start, end)
})

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status
}

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return typeMap[status] || ''
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return date
}

// 格式化日期时间
const formatDateTime = (datetime: string) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ').substring(0, 19)
}

// 加载项目列表
const loadProjects = async () => {
  try {
    projects.value = await getAllProjects()
  } catch (error) {
    console.error('加载项目列表失败:', error)
    ElMessage.error('加载项目列表失败')
  }
}

// 加载所有申请
const loadApplications = async () => {
  loading.value = true
  try {
    // 先加载项目列表
    await loadProjects()

    // 并行加载所有项目的申请列表
    const applicationPromises = projects.value.map(project =>
      getApplicationsByProjectId(project.id).catch(() => [])
    )

    const applicationsArrays = await Promise.all(applicationPromises)
    // 合并所有申请
    allApplications.value = applicationsArrays.flat()
  } catch (error) {
    console.error('加载申请列表失败:', error)
    ElMessage.error('加载申请列表失败')
  } finally {
    loading.value = false
  }
}

// 筛选
const handleFilter = () => {
  currentPage.value = 1
}

// 分页大小改变
const handleSizeChange = () => {
  currentPage.value = 1
}

// 当前页改变
const handleCurrentChange = () => {
  // 分页改变时不需要额外操作
}

// 查看详情
const handleViewDetail = (row: Application) => {
  currentApplication.value = row
  detailDialogVisible.value = true
}

// 下载文件
const handleDownloadFile = (filePath: string) => {
  if (!filePath) {
    ElMessage.warning('文件路径为空')
    return
  }
  // 构建下载URL（根据后端FileUploadConfig配置，文件通过/uploads/**访问）
  const downloadUrl = `http://localhost:8080/uploads/${filePath}`
  window.open(downloadUrl, '_blank')
}

// 通过申请
const handleApprove = async (row: Application) => {
  try {
    await ElMessageBox.confirm(
      `确定要通过申请ID ${row.id} 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await updateApplicationStatus(row.id, 'APPROVED')
    ElMessage.success('申请已通过')
    await loadApplications()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('通过申请失败:', error)
      ElMessage.error('通过申请失败')
    }
  }
}

// 拒绝申请
const handleReject = async (row: Application) => {
  try {
    await ElMessageBox.confirm(
      `确定要拒绝申请ID ${row.id} 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await updateApplicationStatus(row.id, 'REJECTED')
    ElMessage.success('申请已拒绝')
    await loadApplications()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝申请失败:', error)
      ElMessage.error('拒绝申请失败')
    }
  }
}

onMounted(() => {
  loadApplications()
})
</script>

<style scoped lang="scss">
.application-list-container {
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
    align-items: center;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
