<template>
  <div class="project-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">招生项目管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增项目</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入项目标题搜索"
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
        :data="paginatedProjects"
        style="width: 100%; margin-top: 20px"
        stripe
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="项目标题" min-width="200" />
        <el-table-column prop="description" label="项目描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="quota" label="招生名额" width="120" align="center" />
        <el-table-column prop="appliedCount" label="已报名" width="120" align="center" />
        <el-table-column prop="deadline" label="截止日期" width="120" align="center">
          <template #default="{ row }">
            {{ formatDate(row.deadline) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredProjects.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="项目标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入项目标题" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入项目描述"
          />
        </el-form-item>
        <el-form-item label="招生名额" prop="quota">
          <el-input-number
            v-model="formData.quota"
            :min="1"
            :max="9999"
            placeholder="请输入招生名额"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker
            v-model="formData.deadline"
            type="date"
            placeholder="请选择截止日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="开放" value="OPEN" />
            <el-option label="关闭" value="CLOSED" />
            <el-option label="已结束" value="ENDED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getAllProjects, createProject, updateProject, deleteProject } from '../../../api/admission'
import type { AdmissionProject } from '../../../types/entity'

const loading = ref(false)
const submitLoading = ref(false)
const projects = ref<AdmissionProject[]>([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const dialogTitle = ref('新增项目')
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const formData = ref<Partial<AdmissionProject>>({
  title: '',
  description: '',
  quota: 1,
  deadline: '',
  status: 'OPEN'
})

const formRules: FormRules = {
  title: [
    { required: true, message: '请输入项目标题', trigger: 'blur' }
  ],
  quota: [
    { required: true, message: '请输入招生名额', trigger: 'blur' },
    { type: 'number', min: 1, message: '招生名额必须大于0', trigger: 'blur' }
  ],
  deadline: [
    { required: true, message: '请选择截止日期', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 过滤后的项目列表
const filteredProjects = computed(() => {
  if (!searchKeyword.value) {
    return projects.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return projects.value.filter(project => 
    project.title?.toLowerCase().includes(keyword) ||
    project.description?.toLowerCase().includes(keyword)
  )
})

// 分页后的项目列表
const paginatedProjects = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredProjects.value.slice(start, end)
})

// 获取状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'OPEN': '开放',
    'CLOSED': '关闭',
    'ENDED': '已结束'
  }
  return statusMap[status] || status
}

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'OPEN': 'success',
    'CLOSED': 'warning',
    'ENDED': 'info'
  }
  return typeMap[status] || ''
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return date
}

// 加载项目列表
const loadProjects = async () => {
  loading.value = true
  try {
    projects.value = await getAllProjects()
  } catch (error) {
    console.error('加载项目列表失败:', error)
    ElMessage.error('加载项目列表失败')
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

// 新增
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增项目'
  formData.value = {
    title: '',
    description: '',
    quota: 1,
    deadline: '',
    status: 'OPEN'
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: AdmissionProject) => {
  isEdit.value = true
  dialogTitle.value = '编辑项目'
  formData.value = {
    id: row.id,
    title: row.title,
    description: row.description,
    quota: row.quota,
    deadline: row.deadline,
    status: row.status
  }
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: AdmissionProject) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除项目"${row.title}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteProject(row.id)
    ElMessage.success('删除成功')
    await loadProjects()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除项目失败:', error)
      ElMessage.error('删除项目失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    if (isEdit.value) {
      await updateProject(formData.value.id!, formData.value as AdmissionProject)
      ElMessage.success('更新成功')
    } else {
      await createProject(formData.value as AdmissionProject)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    await loadProjects()
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
    }
  } finally {
    submitLoading.value = false
  }
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped lang="scss">
.project-list-container {
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

