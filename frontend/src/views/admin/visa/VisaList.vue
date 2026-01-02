<template>
  <div class="visa-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">签证管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">添加签证</el-button>
        </div>
      </template>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-select
          v-model="filterStudentId"
          placeholder="请选择学生"
          clearable
          filterable
          style="width: 250px; margin-right: 10px"
          @change="handleFilter"
        >
          <el-option label="全部学生" :value="null" />
          <el-option
            v-for="student in students"
            :key="student.studentId"
            :label="`${student.studentNumber} - ${student.name}`"
            :value="student.studentId"
          />
        </el-select>
        <el-select
          v-model="filterStatus"
          placeholder="请选择状态"
          clearable
          style="width: 150px; margin-right: 10px"
          @change="handleFilter"
        >
          <el-option label="全部" value="" />
          <el-option label="有效" value="VALID" />
          <el-option label="即将过期" value="EXPIRING_SOON" />
          <el-option label="已过期" value="EXPIRED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-button type="warning" :icon="Warning" @click="handleViewExpiringSoon">查看即将到期</el-button>
        <el-button type="danger" :icon="Warning" @click="handleViewExpired">查看已过期</el-button>
        <el-button :icon="Refresh" @click="loadVisas">刷新</el-button>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="paginatedVisas"
        style="width: 100%; margin-top: 20px"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="学生信息" width="200">
          <template #default="{ row }">
            <div>
              <div><strong>{{ getStudentName(row.studentId) }}</strong></div>
              <div style="font-size: 12px; color: #909399;">{{ getStudentNumber(row.studentId) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="visaType" label="签证类型" width="120" />
        <el-table-column prop="visaNumber" label="签证号码" width="150" />
        <el-table-column prop="issueDate" label="签发日期" width="120" align="center">
          <template #default="{ row }">
            {{ formatDate(row.issueDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="expiryDate" label="到期日期" width="120" align="center">
          <template #default="{ row }">
            <span :class="{ 'expiring-soon': isExpiringSoon(row.expiryDate), 'expired': isExpired(row.expiryDate) }">
              {{ formatDate(row.expiryDate) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="剩余天数" width="120" align="center">
          <template #default="{ row }">
            <span :class="{ 'expiring-soon': getRemainingDays(row.expiryDate) <= 30 && getRemainingDays(row.expiryDate) > 0, 'expired': isExpired(row.expiryDate) }">
              {{ getRemainingDaysText(row.expiryDate) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="issuingAuthority" label="签发机构" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作栏 -->
      <div v-if="selectedVisas.length > 0" class="batch-actions">
        <el-alert
          :title="`已选择 ${selectedVisas.length} 条记录`"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <el-button type="primary" size="small" @click="handleBatchEdit">批量编辑到期日期</el-button>
          </template>
        </el-alert>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredVisas.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑签证对话框 -->
    <el-dialog
      v-model="visaDialogVisible"
      :title="visaDialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="visaFormRef"
        :model="visaForm"
        :rules="visaFormRules"
        label-width="120px"
      >
        <el-form-item label="学生" prop="studentId">
          <el-select
            v-model="visaForm.studentId"
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
        <el-form-item label="签证类型" prop="visaType">
          <el-select v-model="visaForm.visaType" placeholder="请选择签证类型" style="width: 100%">
            <el-option label="X1 - 长期学习" value="X1" />
            <el-option label="X2 - 短期学习" value="X2" />
            <el-option label="F - 访问" value="F" />
            <el-option label="L - 旅游" value="L" />
          </el-select>
        </el-form-item>
        <el-form-item label="签证号码" prop="visaNumber">
          <el-input v-model="visaForm.visaNumber" placeholder="请输入签证号码" />
        </el-form-item>
        <el-form-item label="签发日期" prop="issueDate">
          <el-date-picker
            v-model="visaForm.issueDate"
            type="date"
            placeholder="请选择签发日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="到期日期" prop="expiryDate">
          <el-date-picker
            v-model="visaForm.expiryDate"
            type="date"
            placeholder="请选择到期日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="入境日期" prop="entryDate">
          <el-date-picker
            v-model="visaForm.entryDate"
            type="date"
            placeholder="请选择入境日期（可选）"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="签发机构" prop="issuingAuthority">
          <el-input v-model="visaForm.issuingAuthority" placeholder="请输入签发机构" />
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="visaForm.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visaDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量编辑到期日期对话框 -->
    <el-dialog
      v-model="batchEditDialogVisible"
      title="批量编辑到期日期"
      width="500px"
      @close="handleBatchDialogClose"
    >
      <el-form
        ref="batchFormRef"
        :model="batchForm"
        :rules="batchFormRules"
        label-width="120px"
      >
        <el-form-item label="已选择记录">
          <el-input :value="`${selectedVisas.length} 条记录`" disabled />
        </el-form-item>
        <el-form-item label="新到期日期" prop="expiryDate">
          <el-date-picker
            v-model="batchForm.expiryDate"
            type="date"
            placeholder="请选择新的到期日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchEditDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchSaveLoading" @click="handleBatchSave">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Warning } from '@element-plus/icons-vue'
import {
  getAllVisas,
  createVisas,
  updateVisas,
  deleteVisas,
  getExpiringSoonVisas,
  getExpiredVisas
} from '../../../api/visa'
import { getAllStudentRecords } from '../../../api/student'
import type { Visas, StudentRecord } from '../../../types/entity'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false)
const visas = ref<Visas[]>([])
const students = ref<StudentRecord[]>([])
const filterStudentId = ref<number | null>(null)
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const selectedVisas = ref<Visas[]>([])

// 签证对话框
const visaDialogVisible = ref(false)
const visaDialogTitle = ref('添加签证')
const visaFormRef = ref<FormInstance>()
const saveLoading = ref(false)
const currentVisaId = ref<number | null>(null)
const visaForm = ref({
  studentId: undefined as number | undefined,
  visaType: '',
  visaNumber: '',
  issueDate: '',
  expiryDate: '',
  entryDate: '',
  issuingAuthority: '',
  remarks: ''
})

const visaFormRules: FormRules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  visaType: [{ required: true, message: '请选择签证类型', trigger: 'change' }],
  visaNumber: [{ required: true, message: '请输入签证号码', trigger: 'blur' }],
  issueDate: [{ required: true, message: '请选择签发日期', trigger: 'change' }],
  expiryDate: [{ required: true, message: '请选择到期日期', trigger: 'change' }]
}

// 批量编辑对话框
const batchEditDialogVisible = ref(false)
const batchFormRef = ref<FormInstance>()
const batchSaveLoading = ref(false)
const batchForm = ref({
  expiryDate: ''
})

const batchFormRules: FormRules = {
  expiryDate: [{ required: true, message: '请选择新的到期日期', trigger: 'change' }]
}

// 过滤后的签证列表
const filteredVisas = computed(() => {
  let result = visas.value

  // 按学生筛选
  if (filterStudentId.value !== null) {
    result = result.filter(visa => visa.studentId === filterStudentId.value)
  }

  // 按状态筛选
  if (filterStatus.value) {
    result = result.filter(visa => visa.status === filterStatus.value)
  }

  return result
})

// 分页后的签证列表
const paginatedVisas = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredVisas.value.slice(start, end)
})

// 获取学生姓名
const getStudentName = (studentId: number) => {
  const student = students.value.find(s => s.studentId === studentId)
  return student?.name || `学生ID: ${studentId}`
}

// 获取学号
const getStudentNumber = (studentId: number) => {
  const student = students.value.find(s => s.studentId === studentId)
  return student?.studentNumber || ''
}

// 检查是否即将到期（30天内）
const isExpiringSoon = (expiryDate: string) => {
  if (!expiryDate) return false
  const expiry = new Date(expiryDate)
  const now = new Date()
  const diffTime = expiry.getTime() - now.getTime()
  const diffDays = diffTime / (1000 * 60 * 60 * 24)
  return diffDays > 0 && diffDays <= 30
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

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return date
}

// 加载签证列表
const loadVisas = async () => {
  loading.value = true
  try {
    visas.value = await getAllVisas()
  } catch (error) {
    console.error('加载签证列表失败:', error)
    ElMessage.error('加载签证列表失败')
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

// 查看即将到期
const handleViewExpiringSoon = async () => {
  loading.value = true
  try {
    visas.value = await getExpiringSoonVisas(30)
    filterStatus.value = ''
    filterStudentId.value = null
    ElMessage.success(`找到 ${visas.value.length} 条即将到期的签证记录`)
  } catch (error) {
    console.error('加载即将到期签证失败:', error)
    ElMessage.error('加载即将到期签证失败')
  } finally {
    loading.value = false
  }
}

// 查看已过期
const handleViewExpired = async () => {
  loading.value = true
  try {
    visas.value = await getExpiredVisas()
    filterStatus.value = ''
    filterStudentId.value = null
    ElMessage.success(`找到 ${visas.value.length} 条已过期的签证记录`)
  } catch (error) {
    console.error('加载已过期签证失败:', error)
    ElMessage.error('加载已过期签证失败')
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

// 选择改变
const handleSelectionChange = (selection: Visas[]) => {
  selectedVisas.value = selection
}

// 添加签证
const handleAdd = () => {
  visaDialogTitle.value = '添加签证'
  currentVisaId.value = null
  visaForm.value = {
    studentId: undefined,
    visaType: '',
    visaNumber: '',
    issueDate: '',
    expiryDate: '',
    entryDate: '',
    issuingAuthority: '',
    remarks: ''
  }
  visaDialogVisible.value = true
}

// 编辑签证
const handleEdit = (row: Visas) => {
  visaDialogTitle.value = '编辑签证'
  currentVisaId.value = row.id
  visaForm.value = {
    studentId: row.studentId,
    visaType: row.visaType,
    visaNumber: row.visaNumber,
    issueDate: row.issueDate,
    expiryDate: row.expiryDate,
    entryDate: row.entryDate || '',
    issuingAuthority: row.issuingAuthority || '',
    remarks: row.remarks || ''
  }
  visaDialogVisible.value = true
}

// 保存签证
const handleSave = async () => {
  if (!visaFormRef.value) return

  await visaFormRef.value.validate(async (valid) => {
    if (!valid) return

    saveLoading.value = true
    try {
      if (currentVisaId.value === null) {
        // 添加签证
        await createVisas(visaForm.value)
        ElMessage.success('添加签证成功')
      } else {
        // 编辑签证
        await updateVisas(currentVisaId.value, visaForm.value)
        ElMessage.success('更新签证成功')
      }
      visaDialogVisible.value = false
      await loadVisas()
    } catch (error) {
      console.error('保存签证失败:', error)
      // 错误消息已在拦截器中显示
    } finally {
      saveLoading.value = false
    }
  })
}

// 对话框关闭
const handleDialogClose = () => {
  visaFormRef.value?.resetFields()
  currentVisaId.value = null
}

// 批量编辑
const handleBatchEdit = () => {
  if (selectedVisas.value.length === 0) {
    ElMessage.warning('请先选择要编辑的记录')
    return
  }
  batchForm.value = {
    expiryDate: ''
  }
  batchEditDialogVisible.value = true
}

// 批量保存
const handleBatchSave = async () => {
  if (!batchFormRef.value) return

  await batchFormRef.value.validate(async (valid) => {
    if (!valid) return

    batchSaveLoading.value = true
    try {
      // 批量更新到期日期
      const promises = selectedVisas.value.map(visa =>
        updateVisas(visa.id, { expiryDate: batchForm.value.expiryDate })
      )
      await Promise.all(promises)
      ElMessage.success(`成功更新 ${selectedVisas.value.length} 条记录的到期日期`)
      batchEditDialogVisible.value = false
      selectedVisas.value = []
      await loadVisas()
    } catch (error) {
      console.error('批量更新失败:', error)
      // 错误消息已在拦截器中显示
    } finally {
      batchSaveLoading.value = false
    }
  })
}

// 批量对话框关闭
const handleBatchDialogClose = () => {
  batchFormRef.value?.resetFields()
}

// 删除签证
const handleDelete = async (row: Visas) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除签证记录"${row.visaNumber}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteVisas(row.id)
    ElMessage.success('删除成功')
    await loadVisas()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除签证失败:', error)
      // 错误消息已在拦截器中显示
    }
  }
}

onMounted(() => {
  loadVisas()
  loadStudents()
})
</script>

<style scoped lang="scss">
.visa-list-container {
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

  .batch-actions {
    margin-top: 20px;
    margin-bottom: 20px;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .expiring-soon {
    color: #e6a23c;
    font-weight: bold;
  }

  .expired {
    color: #f56c6c;
    font-weight: bold;
  }
}
</style>

