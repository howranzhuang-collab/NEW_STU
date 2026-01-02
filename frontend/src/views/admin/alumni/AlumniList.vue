<template>
  <div class="alumni-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">校友管理</span>
          <el-button :icon="Refresh" @click="loadAlumni">刷新</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入姓名、学号、专业搜索"
          clearable
          style="width: 300px; margin-right: 10px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="filterStatus"
          placeholder="请选择毕业去向"
          clearable
          style="width: 150px; margin-right: 10px"
          @change="handleFilter"
        >
          <el-option label="全部" value="" />
          <el-option label="就业" value="EMPLOYED" />
          <el-option label="继续深造" value="STUDYING" />
          <el-option label="其他" value="OTHER" />
        </el-select>
        <el-select
          v-model="filterDegree"
          placeholder="请选择学位"
          clearable
          style="width: 150px; margin-right: 10px"
          @change="handleFilter"
        >
          <el-option label="全部" value="" />
          <el-option label="学士" value="BACHELOR" />
          <el-option label="硕士" value="MASTER" />
          <el-option label="博士" value="DOCTOR" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="paginatedAlumni"
        style="width: 100%; margin-top: 20px"
        stripe
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="studentNumber" label="学号" width="150" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="major" label="专业" min-width="150" />
        <el-table-column prop="graduationDate" label="毕业日期" width="120" align="center">
          <template #default="{ row }">
            {{ formatDate(row.graduationDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="degree" label="学位" width="100" align="center">
          <template #default="{ row }">
            {{ getDegreeText(row.degree) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="毕业去向" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="organization" label="工作单位/学校" min-width="200" show-overflow-tooltip />
        <el-table-column prop="position" label="职位/专业" min-width="150" show-overflow-tooltip />
        <el-table-column prop="contact" label="联系方式" width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button type="success" link size="small" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredAlumni.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="校友详情"
      width="800px"
    >
      <el-descriptions v-if="currentAlumni" :column="2" border>
        <el-descriptions-item label="ID">{{ currentAlumni?.id }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentAlumni.studentNumber }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentAlumni.name }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ currentAlumni.major }}</el-descriptions-item>
        <el-descriptions-item label="毕业日期">{{ formatDate(currentAlumni.graduationDate) }}</el-descriptions-item>
        <el-descriptions-item label="学位">{{ getDegreeText(currentAlumni.degree) }}</el-descriptions-item>
        <el-descriptions-item label="毕业去向">
          <el-tag :type="getStatusType(currentAlumni.status)">
            {{ getStatusText(currentAlumni.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="工作单位/学校">{{ currentAlumni.organization || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="职位/专业">{{ currentAlumni.position || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ currentAlumni.contact || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentAlumni.remarks || '无' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentAlumni.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(currentAlumni.updateTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleEditFromDetail">编辑</el-button>
      </template>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑校友信息"
      width="600px"
      @close="handleEditDialogClose"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editFormRules"
        label-width="120px"
      >
        <el-form-item label="姓名">
          <el-input :value="currentAlumni?.name" disabled />
        </el-form-item>
        <el-form-item label="学号">
          <el-input :value="currentAlumni?.studentNumber" disabled />
        </el-form-item>
        <el-form-item label="专业">
          <el-input :value="currentAlumni?.major" disabled />
        </el-form-item>
        <el-form-item label="毕业日期">
          <el-input :value="formatDate(currentAlumni?.graduationDate || '')" disabled />
        </el-form-item>
        <el-form-item label="学位">
          <el-input :value="getDegreeText(currentAlumni?.degree || '')" disabled />
        </el-form-item>
        <el-form-item label="毕业去向" prop="status">
          <el-select v-model="editForm.status" placeholder="请选择毕业去向" style="width: 100%">
            <el-option label="就业" value="EMPLOYED" />
            <el-option label="继续深造" value="STUDYING" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作单位/学校" prop="organization">
          <el-input
            v-model="editForm.organization"
            placeholder="请输入工作单位或继续深造的学校"
          />
        </el-form-item>
        <el-form-item label="职位/专业" prop="position">
          <el-input
            v-model="editForm.position"
            placeholder="请输入职位或继续深造的专业"
          />
        </el-form-item>
        <el-form-item label="联系方式" prop="contact">
          <el-input
            v-model="editForm.contact"
            placeholder="请输入邮箱或电话"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="editForm.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import {
  getAllAlumni,
  getAlumniById,
  updateAlumni
} from '../../../api/alumni'
import type { Alumni } from '../../../types/entity'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false)
const alumni = ref<Alumni[]>([])
const searchKeyword = ref('')
const filterStatus = ref('')
const filterDegree = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

// 详情对话框
const detailDialogVisible = ref(false)
const currentAlumni = ref<Alumni | null>(null)

// 编辑对话框
const editDialogVisible = ref(false)
const editFormRef = ref<FormInstance>()
const saveLoading = ref(false)
const editForm = ref({
  status: '',
  organization: '',
  position: '',
  contact: '',
  remarks: ''
})

const editFormRules: FormRules = {
  status: [{ required: true, message: '请选择毕业去向', trigger: 'change' }]
}

// 过滤后的校友列表
const filteredAlumni = computed(() => {
  let result = alumni.value

  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(item =>
      item.name?.toLowerCase().includes(keyword) ||
      item.studentNumber?.toLowerCase().includes(keyword) ||
      item.major?.toLowerCase().includes(keyword)
    )
  }

  // 按毕业去向筛选
  if (filterStatus.value) {
    result = result.filter(item => item.status === filterStatus.value)
  }

  // 按学位筛选
  if (filterDegree.value) {
    result = result.filter(item => item.degree === filterDegree.value)
  }

  return result
})

// 分页后的校友列表
const paginatedAlumni = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredAlumni.value.slice(start, end)
})

// 获取学位文本
const getDegreeText = (degree: string) => {
  const degreeMap: Record<string, string> = {
    'BACHELOR': '学士',
    'MASTER': '硕士',
    'DOCTOR': '博士'
  }
  return degreeMap[degree] || degree
}

// 获取毕业去向文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'EMPLOYED': '就业',
    'STUDYING': '继续深造',
    'OTHER': '其他'
  }
  return statusMap[status] || status
}

// 获取毕业去向类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'EMPLOYED': 'success',
    'STUDYING': 'info',
    'OTHER': 'warning'
  }
  return typeMap[status] || ''
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return date
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return dateTime
}

// 加载校友列表
const loadAlumni = async () => {
  loading.value = true
  try {
    alumni.value = await getAllAlumni()
  } catch (error) {
    console.error('加载校友列表失败:', error)
    ElMessage.error('加载校友列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
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
const handleViewDetail = async (row: Alumni) => {
  try {
    currentAlumni.value = await getAlumniById(row.id)
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取校友详情失败:', error)
    ElMessage.error('获取校友详情失败')
  }
}

// 从详情页编辑
const handleEditFromDetail = () => {
  if (!currentAlumni.value) return
  detailDialogVisible.value = false
  handleEdit(currentAlumni.value)
}

// 编辑
const handleEdit = (row: Alumni) => {
  currentAlumni.value = row
  editForm.value = {
    status: row.status,
    organization: row.organization || '',
    position: row.position || '',
    contact: row.contact || '',
    remarks: row.remarks || ''
  }
  editDialogVisible.value = true
}

// 保存
const handleSave = async () => {
  if (!editFormRef.value || !currentAlumni.value) return

  await editFormRef.value.validate(async (valid) => {
    if (!valid) return

    saveLoading.value = true
    try {
      if (!currentAlumni.value) {
        ElMessage.error('校友信息不存在')
        return
      }
      await updateAlumni(currentAlumni.value.id, editForm.value)
      ElMessage.success('更新校友信息成功')
      editDialogVisible.value = false
      await loadAlumni()
    } catch (error) {
      console.error('更新校友信息失败:', error)
      // 错误消息已在拦截器中显示
    } finally {
      saveLoading.value = false
    }
  })
}

// 编辑对话框关闭
const handleEditDialogClose = () => {
  editFormRef.value?.resetFields()
  currentAlumni.value = null
}

onMounted(() => {
  loadAlumni()
})
</script>

<style scoped lang="scss">
.alumni-list-container {
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
    flex-wrap: wrap;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>

