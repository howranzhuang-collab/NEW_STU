<template>
  <div class="fee-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">费用管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">添加费用</el-button>
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
          <el-option label="未缴" value="UNPAID" />
          <el-option label="部分已缴" value="PARTIAL" />
          <el-option label="已缴清" value="PAID" />
        </el-select>
        <el-input
          v-model="searchKeyword"
          placeholder="请输入费用类型搜索"
          clearable
          style="width: 250px; margin-right: 10px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="loadFees">刷新</el-button>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="paginatedFees"
        style="width: 100%; margin-top: 20px"
        stripe
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="学生信息" width="200">
          <template #default="{ row }">
            <div>
              <div><strong>{{ getStudentName(row.studentId) }}</strong></div>
              <div style="font-size: 12px; color: #909399;">{{ getStudentNumber(row.studentId) }}</div>
            </div>
          </template>
        </el-table-column>
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
            <span :style="{ color: getUnpaidAmount(row) > 0 ? '#f56c6c' : '#67c23a' }">
              ¥{{ getUnpaidAmount(row).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="缴费截止日期" width="150" align="center">
          <template #default="{ row }">
            {{ formatDate(row.dueDate) }}
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
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link size="small" @click="handleMarkPaid(row)">
              标记已缴
            </el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
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
          :total="filteredFees.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑费用对话框 -->
    <el-dialog
      v-model="feeDialogVisible"
      :title="feeDialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="feeFormRef"
        :model="feeForm"
        :rules="feeFormRules"
        label-width="120px"
      >
        <el-form-item label="学生" prop="studentId">
          <el-select
            v-model="feeForm.studentId"
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
        <el-form-item v-if="!feeForm.studentId" label="" style="margin-top: -20px;">
          <span style="color: #f56c6c; font-size: 12px;">请选择学生</span>
        </el-form-item>
        <el-form-item label="费用类型" prop="feeType">
          <el-input v-model="feeForm.feeType" placeholder="请输入费用类型，如：学费、住宿费等" />
        </el-form-item>
        <el-form-item label="费用金额" prop="amount">
          <el-input-number
            v-model="feeForm.amount"
            :min="0"
            :precision="2"
            :step="100"
            style="width: 100%"
            placeholder="请输入费用金额"
          />
        </el-form-item>
        <el-form-item label="缴费截止日期" prop="dueDate">
          <el-date-picker
            v-model="feeForm.dueDate"
            type="date"
            placeholder="请选择缴费截止日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="feeForm.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 标记已缴对话框 -->
    <el-dialog
      v-model="payDialogVisible"
      title="标记已缴"
      width="500px"
      @close="handlePayDialogClose"
    >
      <el-form
        ref="payFormRef"
        :model="payForm"
        :rules="payFormRules"
        label-width="120px"
      >
        <el-form-item label="学生">
          <el-input :value="currentFee?.studentId ? getStudentName(currentFee.studentId) : ''" disabled />
        </el-form-item>
        <el-form-item label="费用类型">
          <el-input :value="currentFee?.feeType" disabled />
        </el-form-item>
        <el-form-item label="费用总额">
          <el-input :value="`¥${currentFee?.amount?.toFixed(2) || '0.00'}`" disabled />
        </el-form-item>
        <el-form-item label="已缴金额">
          <el-input :value="`¥${currentFee?.paidAmount?.toFixed(2) || '0.00'}`" disabled />
        </el-form-item>
        <el-form-item label="欠费金额">
          <el-input :value="`¥${getUnpaidAmount(currentFee).toFixed(2)}`" disabled />
        </el-form-item>
        <el-form-item label="实缴金额" prop="paidAmount">
          <el-input-number
            v-model="payForm.paidAmount"
            :min="0"
            :max="getUnpaidAmount(currentFee)"
            :precision="2"
            :step="100"
            style="width: 100%"
            placeholder="请输入实缴金额"
          />
        </el-form-item>
        <el-form-item label="缴费日期" prop="paidDate">
          <el-date-picker
            v-model="payForm.paidDate"
            type="date"
            placeholder="请选择缴费日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="payLoading" @click="handlePayFull">全额缴清</el-button>
        <el-button type="primary" :loading="payLoading" @click="handlePay">确认缴费</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { getAllFees, createFees, updateFees, deleteFees, markAsPaid, markAsFullyPaid } from '../../../api/fees'
import { getAllStudentRecords } from '../../../api/student'
import type { Fees, StudentRecord } from '../../../types/entity'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false)
const fees = ref<Fees[]>([])
const students = ref<StudentRecord[]>([])
const searchKeyword = ref('')
const filterStudentId = ref<number | null>(null)
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

// 费用对话框
const feeDialogVisible = ref(false)
const feeDialogTitle = ref('添加费用')
const feeFormRef = ref<FormInstance>()
const saveLoading = ref(false)
const currentFeeId = ref<number | null>(null)
const feeForm = ref({
  studentId: undefined as number | undefined,
  feeType: '',
  amount: 0,
  dueDate: '',
  remarks: ''
})

const feeFormRules: FormRules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  feeType: [{ required: true, message: '请输入费用类型', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入费用金额', trigger: 'blur' }],
  dueDate: [{ required: true, message: '请选择缴费截止日期', trigger: 'change' }]
}

// 缴费对话框
const payDialogVisible = ref(false)
const payFormRef = ref<FormInstance>()
const payLoading = ref(false)
const currentFee = ref<Fees | null>(null)
const payForm = ref({
  paidAmount: 0,
  paidDate: ''
})

const payFormRules: FormRules = {
  paidAmount: [{ required: true, message: '请输入实缴金额', trigger: 'blur' }],
  paidDate: [{ required: true, message: '请选择缴费日期', trigger: 'change' }]
}

// 过滤后的费用列表
const filteredFees = computed(() => {
  let result = fees.value

  // 按学生筛选
  if (filterStudentId.value !== null) {
    result = result.filter(fee => fee.studentId === filterStudentId.value)
  }

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

// 分页后的费用列表
const paginatedFees = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredFees.value.slice(start, end)
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

// 计算欠费金额
const getUnpaidAmount = (fee: Fees | null) => {
  if (!fee) return 0
  return (fee.amount || 0) - (fee.paidAmount || 0)
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
  loading.value = true
  try {
    fees.value = await getAllFees()
  } catch (error) {
    console.error('加载费用列表失败:', error)
    ElMessage.error('加载费用列表失败')
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

// 添加费用
const handleAdd = () => {
  feeDialogTitle.value = '添加费用'
  currentFeeId.value = null
  feeForm.value = {
    studentId: undefined,
    feeType: '',
    amount: 0,
    dueDate: '',
    remarks: ''
  }
  feeDialogVisible.value = true
}

// 编辑费用
const handleEdit = (row: Fees) => {
  feeDialogTitle.value = '编辑费用'
  currentFeeId.value = row.id
  feeForm.value = {
    studentId: row.studentId,
    feeType: row.feeType,
    amount: row.amount,
    dueDate: row.dueDate,
    remarks: row.remarks || ''
  }
  feeDialogVisible.value = true
}

// 保存费用
const handleSave = async () => {
  if (!feeFormRef.value) return

  await feeFormRef.value.validate(async (valid) => {
    if (!valid) return

    saveLoading.value = true
    try {
      if (currentFeeId.value === null) {
        // 添加费用
        await createFees(feeForm.value)
        ElMessage.success('添加费用成功')
      } else {
        // 编辑费用
        if (currentFeeId.value) {
          await updateFees(currentFeeId.value, feeForm.value)
          ElMessage.success('更新费用成功')
        }
      }
      feeDialogVisible.value = false
      await loadFees()
    } catch (error) {
      console.error('保存费用失败:', error)
      // 错误消息已在拦截器中显示
    } finally {
      saveLoading.value = false
    }
  })
}

// 对话框关闭
const handleDialogClose = () => {
  feeFormRef.value?.resetFields()
  currentFeeId.value = null
}

// 标记已缴
const handleMarkPaid = (row: Fees) => {
  currentFee.value = row
  const unpaidAmount = getUnpaidAmount(row)
  const today = new Date().toISOString().split('T')[0]
  payForm.value = {
    paidAmount: unpaidAmount,
    paidDate: today || ''
  }
  payDialogVisible.value = true
}

// 确认缴费
const handlePay = async () => {
  if (!payFormRef.value || !currentFee.value) return

  await payFormRef.value.validate(async (valid) => {
    if (!valid) return

    payLoading.value = true
    try {
      await markAsPaid(
        currentFee.value!.id,
        payForm.value.paidAmount,
        payForm.value.paidDate
      )
      ElMessage.success('标记已缴成功')
      payDialogVisible.value = false
      await loadFees()
    } catch (error) {
      console.error('标记已缴失败:', error)
      // 错误消息已在拦截器中显示
    } finally {
      payLoading.value = false
    }
  })
}

// 全额缴清
const handlePayFull = async () => {
  if (!currentFee.value) return

  payLoading.value = true
  try {
    await markAsFullyPaid(
      currentFee.value.id,
      payForm.value.paidDate || new Date().toISOString().split('T')[0]
    )
    ElMessage.success('费用已完全缴清')
    payDialogVisible.value = false
    await loadFees()
  } catch (error) {
    console.error('标记已缴失败:', error)
    // 错误消息已在拦截器中显示
  } finally {
    payLoading.value = false
  }
}

// 缴费对话框关闭
const handlePayDialogClose = () => {
  payFormRef.value?.resetFields()
  currentFee.value = null
}

// 删除费用
const handleDelete = async (row: Fees) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除费用记录"${row.feeType}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteFees(row.id)
    ElMessage.success('删除成功')
    await loadFees()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除费用失败:', error)
      // 错误消息已在拦截器中显示
    }
  }
}

onMounted(() => {
  loadFees()
  loadStudents()
})
</script>

<style scoped lang="scss">
.fee-list-container {
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

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>

