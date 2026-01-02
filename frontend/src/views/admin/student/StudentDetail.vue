<template>
  <div class="student-detail-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div>
            <el-button type="text" :icon="ArrowLeft" @click="$router.back()">返回</el-button>
            <span class="card-title">考勤与成绩管理</span>
            <span v-if="studentRecord" class="student-info">
              - {{ studentRecord.name }}（{{ studentRecord.studentNumber }}）
            </span>
          </div>
        </div>
      </template>

      <div v-if="loading" style="text-align: center; padding: 40px;">
        <el-icon class="is-loading" style="font-size: 32px;"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="!studentRecord" style="text-align: center; padding: 40px;">
        <el-empty description="学生信息不存在" />
      </div>

      <div v-else>
        <!-- 考勤管理 -->
        <el-card class="section-card" style="margin-bottom: 20px;">
          <template #header>
            <div class="section-header">
              <span>考勤记录</span>
              <el-button type="primary" :icon="Plus" @click="handleAddAttendance">新增考勤</el-button>
            </div>
          </template>

          <el-table :data="attendances" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="courseName" label="课程名称" min-width="150" />
            <el-table-column prop="attendanceDate" label="考勤日期" width="120" align="center">
              <template #default="{ row }">
                {{ formatDate(row.attendanceDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="getAttendanceStatusType(row.status)">
                  {{ getAttendanceStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remarks" label="备注" min-width="150" show-overflow-tooltip />
            <el-table-column label="操作" width="150" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditAttendance(row)">
                  编辑
                </el-button>
                <el-button type="danger" link size="small" @click="handleDeleteAttendance(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 成绩管理 -->
        <el-card class="section-card">
          <template #header>
            <div class="section-header">
              <span>成绩记录</span>
              <el-button type="primary" :icon="Plus" @click="handleAddExam">新增成绩</el-button>
            </div>
          </template>

          <el-table :data="exams" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="courseName" label="课程名称" min-width="150" />
            <el-table-column prop="examType" label="类型" width="120" align="center">
              <template #default="{ row }">
                {{ getExamTypeText(row.examType) }}
              </template>
            </el-table-column>
            <el-table-column prop="examDate" label="考试日期" width="120" align="center">
              <template #default="{ row }">
                {{ formatDate(row.examDate) }}
              </template>
            </el-table-column>
            <el-table-column label="成绩" width="150" align="center">
              <template #default="{ row }">
                {{ row.score }} / {{ row.fullScore }}
                <el-tag :type="getScoreType(row.score, row.fullScore)" size="small" style="margin-left: 8px;">
                  {{ getScorePercent(row.score, row.fullScore) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remarks" label="备注" min-width="150" show-overflow-tooltip />
            <el-table-column label="操作" width="150" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditExam(row)">
                  编辑
                </el-button>
                <el-button type="danger" link size="small" @click="handleDeleteExam(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </el-card>

    <!-- 考勤编辑对话框 -->
    <el-dialog
      v-model="attendanceDialogVisible"
      :title="attendanceDialogTitle"
      width="600px"
      @close="handleAttendanceDialogClose"
    >
      <el-form
        ref="attendanceFormRef"
        :model="attendanceForm"
        :rules="attendanceRules"
        label-width="100px"
      >
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="attendanceForm.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="考勤日期" prop="attendanceDate">
          <el-date-picker
            v-model="attendanceForm.attendanceDate"
            type="date"
            placeholder="请选择考勤日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="考勤状态" prop="status">
          <el-select v-model="attendanceForm.status" placeholder="请选择考勤状态" style="width: 100%">
            <el-option label="出席" value="PRESENT" />
            <el-option label="缺席" value="ABSENT" />
            <el-option label="迟到" value="LATE" />
            <el-option label="请假" value="LEAVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="attendanceForm.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入备注（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="attendanceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="attendanceSubmitLoading" @click="handleAttendanceSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 成绩编辑对话框 -->
    <el-dialog
      v-model="examDialogVisible"
      :title="examDialogTitle"
      width="600px"
      @close="handleExamDialogClose"
    >
      <el-form
        ref="examFormRef"
        :model="examForm"
        :rules="examRules"
        label-width="100px"
      >
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="examForm.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="考试类型" prop="examType">
          <el-select v-model="examForm.examType" placeholder="请选择考试类型" style="width: 100%">
            <el-option label="期中" value="MIDTERM" />
            <el-option label="期末" value="FINAL" />
            <el-option label="测验" value="QUIZ" />
            <el-option label="作业" value="ASSIGNMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试日期" prop="examDate">
          <el-date-picker
            v-model="examForm.examDate"
            type="date"
            placeholder="请选择考试日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="成绩" prop="score">
          <el-input-number
            v-model="examForm.score"
            :min="0"
            :precision="2"
            placeholder="请输入成绩"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="满分" prop="fullScore">
          <el-input-number
            v-model="examForm.fullScore"
            :min="0"
            :precision="2"
            placeholder="请输入满分"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="examForm.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入备注（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="examDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="examSubmitLoading" @click="handleExamSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft, Plus, Loading } from '@element-plus/icons-vue'
import {
  getStudentRecordById,
  getAttendancesByStudentId,
  createAttendance,
  updateAttendance,
  deleteAttendance,
  getExamsByStudentId,
  createExam,
  updateExam,
  deleteExam
} from '../../../api/student'
import type { StudentRecord, Attendance, Exam } from '../../../types/entity'

const route = useRoute()
const loading = ref(false)
const studentRecord = ref<StudentRecord | null>(null)
const attendances = ref<Attendance[]>([])
const exams = ref<Exam[]>([])

const attendanceDialogVisible = ref(false)
const attendanceDialogTitle = ref('新增考勤')
const attendanceSubmitLoading = ref(false)
const attendanceFormRef = ref<FormInstance>()
const attendanceForm = ref<Partial<Attendance>>({
  courseName: '',
  attendanceDate: '',
  status: 'PRESENT',
  remarks: ''
})

const examDialogVisible = ref(false)
const examDialogTitle = ref('新增成绩')
const examSubmitLoading = ref(false)
const examFormRef = ref<FormInstance>()
const examForm = ref<Partial<Exam>>({
  courseName: '',
  examType: 'FINAL',
  examDate: '',
  score: 0,
  fullScore: 100,
  remarks: ''
})

const attendanceRules: FormRules = {
  courseName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' }
  ],
  attendanceDate: [
    { required: true, message: '请选择考勤日期', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择考勤状态', trigger: 'change' }
  ]
}

const examRules: FormRules = {
  courseName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' }
  ],
  examDate: [
    { required: true, message: '请选择考试日期', trigger: 'change' }
  ],
  score: [
    { required: true, message: '请输入成绩', trigger: 'blur' },
    { type: 'number', min: 0, message: '成绩不能小于0', trigger: 'blur' }
  ],
  fullScore: [
    { required: true, message: '请输入满分', trigger: 'blur' },
    { type: 'number', min: 0, message: '满分不能小于0', trigger: 'blur' }
  ]
}

// 获取考勤状态文本
const getAttendanceStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'PRESENT': '出席',
    'ABSENT': '缺席',
    'LATE': '迟到',
    'LEAVE': '请假'
  }
  return statusMap[status] || status
}

// 获取考勤状态类型
const getAttendanceStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'PRESENT': 'success',
    'ABSENT': 'danger',
    'LATE': 'warning',
    'LEAVE': 'info'
  }
  return typeMap[status] || ''
}

// 获取考试类型文本
const getExamTypeText = (type?: string) => {
  if (!type) return '-'
  const typeMap: Record<string, string> = {
    'MIDTERM': '期中',
    'FINAL': '期末',
    'QUIZ': '测验',
    'ASSIGNMENT': '作业'
  }
  return typeMap[type] || type
}

// 获取成绩百分比
const getScorePercent = (score: number, fullScore: number) => {
  if (fullScore === 0) return '0%'
  return `${((score / fullScore) * 100).toFixed(1)}%`
}

// 获取成绩类型（用于标签颜色）
const getScoreType = (score: number, fullScore: number) => {
  if (fullScore === 0) return 'info'
  const percent = (score / fullScore) * 100
  if (percent >= 90) return 'success'
  if (percent >= 80) return 'success'
  if (percent >= 60) return 'warning'
  return 'danger'
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return date
}

// 加载学生信息
const loadStudentInfo = async () => {
  const recordId = route.query.recordId as string
  if (!recordId) {
    ElMessage.error('缺少学籍ID参数')
    return
  }

  loading.value = true
  try {
    studentRecord.value = await getStudentRecordById(Number(recordId))
    if (studentRecord.value) {
      await loadData()
    }
  } catch (error) {
    console.error('加载学生信息失败:', error)
    ElMessage.error('加载学生信息失败')
  } finally {
    loading.value = false
  }
}

// 加载考勤和成绩数据
const loadData = async () => {
  if (!studentRecord.value) return

  try {
    const [attendanceList, examList] = await Promise.all([
      getAttendancesByStudentId(studentRecord.value.studentId),
      getExamsByStudentId(studentRecord.value.studentId)
    ])
    attendances.value = attendanceList
    exams.value = examList
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

// 新增考勤
const handleAddAttendance = () => {
  attendanceDialogTitle.value = '新增考勤'
  attendanceForm.value = {
    courseName: '',
    attendanceDate: '',
    status: 'PRESENT',
    remarks: ''
  }
  attendanceDialogVisible.value = true
}

// 编辑考勤
const handleEditAttendance = (row: Attendance) => {
  attendanceDialogTitle.value = '编辑考勤'
  attendanceForm.value = {
    id: row.id,
    courseName: row.courseName,
    attendanceDate: row.attendanceDate,
    status: row.status,
    remarks: row.remarks
  }
  attendanceDialogVisible.value = true
}

// 删除考勤
const handleDeleteAttendance = async (row: Attendance) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除这条考勤记录吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteAttendance(row.id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除考勤失败:', error)
      ElMessage.error('删除考勤失败')
    }
  }
}

// 提交考勤
const handleAttendanceSubmit = async () => {
  if (!attendanceFormRef.value || !studentRecord.value) return

  try {
    await attendanceFormRef.value.validate()
    attendanceSubmitLoading.value = true

    const attendanceData: Attendance = {
      ...attendanceForm.value,
      studentId: studentRecord.value.studentId
    } as Attendance

    if (attendanceForm.value.id) {
      await updateAttendance(attendanceForm.value.id, attendanceData)
      ElMessage.success('更新成功')
    } else {
      await createAttendance(attendanceData)
      ElMessage.success('创建成功')
    }

    attendanceDialogVisible.value = false
    await loadData()
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
    }
  } finally {
    attendanceSubmitLoading.value = false
  }
}

// 考勤对话框关闭
const handleAttendanceDialogClose = () => {
  attendanceFormRef.value?.resetFields()
}

// 新增成绩
const handleAddExam = () => {
  examDialogTitle.value = '新增成绩'
  examForm.value = {
    courseName: '',
    examType: 'FINAL',
    examDate: '',
    score: 0,
    fullScore: 100,
    remarks: ''
  }
  examDialogVisible.value = true
}

// 编辑成绩
const handleEditExam = (row: Exam) => {
  examDialogTitle.value = '编辑成绩'
  examForm.value = {
    id: row.id,
    courseName: row.courseName,
    examType: row.examType,
    examDate: row.examDate,
    score: row.score,
    fullScore: row.fullScore,
    remarks: row.remarks
  }
  examDialogVisible.value = true
}

// 删除成绩
const handleDeleteExam = async (row: Exam) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除这条成绩记录吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteExam(row.id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除成绩失败:', error)
      ElMessage.error('删除成绩失败')
    }
  }
}

// 提交成绩
const handleExamSubmit = async () => {
  if (!examFormRef.value || !studentRecord.value) return

  try {
    await examFormRef.value.validate()
    examSubmitLoading.value = true

    const examData: Exam = {
      ...examForm.value,
      studentId: studentRecord.value.studentId
    } as Exam

    if (examForm.value.id) {
      await updateExam(examForm.value.id, examData)
      ElMessage.success('更新成功')
    } else {
      await createExam(examData)
      ElMessage.success('创建成功')
    }

    examDialogVisible.value = false
    await loadData()
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
    }
  } finally {
    examSubmitLoading.value = false
  }
}

// 成绩对话框关闭
const handleExamDialogClose = () => {
  examFormRef.value?.resetFields()
}

onMounted(() => {
  loadStudentInfo()
})
</script>

<style scoped lang="scss">
.student-detail-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      font-size: 18px;
      font-weight: bold;
      color: #303133;
      margin-left: 10px;
    }

    .student-info {
      color: #909399;
      font-weight: normal;
      margin-left: 10px;
    }
  }

  .section-card {
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: bold;
    }
  }
}
</style>
