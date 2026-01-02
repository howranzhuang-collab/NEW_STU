<template>
  <div class="student-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">学籍管理</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入学号、姓名搜索"
          clearable
          style="width: 300px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="paginatedRecords"
        style="width: 100%; margin-top: 20px"
        stripe
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="studentNumber" label="学号" width="150" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="{ row }">
            {{ row.gender === 'MALE' ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="nationality" label="国籍" width="120" />
        <el-table-column prop="major" label="专业" min-width="150" />
        <el-table-column prop="grade" label="年级" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enrollmentDate" label="入学日期" width="120" align="center">
          <template #default="{ row }">
            {{ formatDate(row.enrollmentDate) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              详情
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
          :total="filteredRecords.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getAllStudentRecords } from '../../../api/student'
import type { StudentRecord } from '../../../types/entity'

const router = useRouter()
const loading = ref(false)
const records = ref<StudentRecord[]>([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

// 过滤后的记录列表
const filteredRecords = computed(() => {
  if (!searchKeyword.value) {
    return records.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return records.value.filter(record =>
    record.studentNumber?.toLowerCase().includes(keyword) ||
    record.name?.toLowerCase().includes(keyword)
  )
})

// 分页后的记录列表
const paginatedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredRecords.value.slice(start, end)
})

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

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'ACTIVE': 'success',
    'GRADUATED': 'info',
    'SUSPENDED': 'warning',
    'DROPPED': 'danger'
  }
  return typeMap[status] || ''
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return date
}

// 加载学籍记录列表
const loadRecords = async () => {
  loading.value = true
  try {
    records.value = await getAllStudentRecords()
  } catch (error) {
    console.error('加载学籍记录列表失败:', error)
    ElMessage.error('加载学籍记录列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
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
const handleViewDetail = (row: StudentRecord) => {
  // 跳转到考勤与成绩录入页，传递studentId
  router.push({
    path: '/admin/students/detail',
    query: { studentId: row.studentId, recordId: row.id }
  })
}

onMounted(() => {
  loadRecords()
})
</script>

<style scoped lang="scss">
.student-list-container {
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

  .search-bar {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
