<template>
  <div class="record-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">我的学籍</span>
        </div>
      </template>

      <div v-if="loading" style="text-align: center; padding: 40px;">
        <el-icon class="is-loading" style="font-size: 32px;"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="!studentRecord" style="text-align: center; padding: 40px;">
        <el-empty description="暂无学籍信息" />
      </div>

      <div v-else>
        <!-- 基本信息 -->
        <el-card class="info-card" style="margin-bottom: 20px;">
          <template #header>
            <div class="section-header">基本信息</div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="学号">{{ studentRecord.studentNumber }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ studentRecord.name }}</el-descriptions-item>
            <el-descriptions-item label="性别">
              {{ studentRecord.gender === 'MALE' ? '男' : '女' }}
            </el-descriptions-item>
            <el-descriptions-item label="国籍">{{ studentRecord.nationality }}</el-descriptions-item>
            <el-descriptions-item label="专业">{{ studentRecord.major }}</el-descriptions-item>
            <el-descriptions-item label="年级">{{ studentRecord.grade }}</el-descriptions-item>
            <el-descriptions-item label="学籍状态">
              <el-tag :type="getStatusType(studentRecord.status)">
                {{ getStatusText(studentRecord.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="入学日期">{{ formatDate(studentRecord.enrollmentDate) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 考勤统计 -->
        <el-card class="section-card" style="margin-bottom: 20px;">
          <template #header>
            <div class="section-header">考勤统计</div>
          </template>

          <div v-if="attendances.length === 0" style="text-align: center; padding: 40px;">
            <el-empty description="暂无考勤记录" />
          </div>

          <div v-else>
            <!-- 考勤统计汇总 -->
            <el-row :gutter="20" style="margin-bottom: 20px;">
              <el-col :span="6">
                <el-statistic title="总考勤次数" :value="attendances.length" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="出席次数" :value="attendanceStats.present" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="缺席次数" :value="attendanceStats.absent" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="出勤率" :value="attendanceRate" suffix="%" :precision="1" />
              </el-col>
            </el-row>

            <!-- 考勤表格 -->
            <el-table :data="attendances" stripe style="width: 100%">
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
            </el-table>
          </div>
        </el-card>

        <!-- 成绩单 -->
        <el-card class="section-card">
          <template #header>
            <div class="section-header">成绩单</div>
          </template>

          <div v-if="exams.length === 0" style="text-align: center; padding: 40px;">
            <el-empty description="暂无成绩记录" />
          </div>

          <div v-else>
            <!-- 成绩统计汇总 -->
            <el-row :gutter="20" style="margin-bottom: 20px;">
              <el-col :span="8">
                <el-statistic title="总考试次数" :value="exams.length" />
              </el-col>
              <el-col :span="8">
                <el-statistic title="平均分" :value="averageScore" :precision="2" />
              </el-col>
              <el-col :span="8">
                <el-statistic title="GPA" :value="gpa" :precision="2" />
              </el-col>
            </el-row>

            <!-- 成绩表格 -->
            <el-table :data="exams" stripe style="width: 100%">
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
            </el-table>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
// 这些函数暂时未使用（在注释代码中），保留供将来使用
// import {
//   getAttendancesByStudentId,
//   getExamsByStudentId
// } from '../../api/student'
import type { StudentRecord, Attendance, Exam } from '../../types/entity'
const loading = ref(false)
const studentRecord = ref<StudentRecord | null>(null)
const attendances = ref<Attendance[]>([])
const exams = ref<Exam[]>([])

// 考勤统计
const attendanceStats = computed(() => {
  const stats = {
    present: 0,
    absent: 0,
    late: 0,
    leave: 0
  }
  attendances.value.forEach(att => {
    if (att.status === 'PRESENT') stats.present++
    else if (att.status === 'ABSENT') stats.absent++
    else if (att.status === 'LATE') stats.late++
    else if (att.status === 'LEAVE') stats.leave++
  })
  return stats
})

// 出勤率
const attendanceRate = computed(() => {
  if (attendances.value.length === 0) return 0
  const presentCount = attendanceStats.value.present
  return (presentCount / attendances.value.length) * 100
})

// 平均分
const averageScore = computed(() => {
  if (exams.value.length === 0) return 0
  const totalScore = exams.value.reduce((sum, exam) => sum + exam.score, 0)
  return totalScore / exams.value.length
})

// GPA（简化计算：90-100=4.0, 80-89=3.0, 70-79=2.0, 60-69=1.0, <60=0）
const gpa = computed(() => {
  if (exams.value.length === 0) return 0
  let totalPoints = 0
  exams.value.forEach(exam => {
    const percent = (exam.score / exam.fullScore) * 100
    if (percent >= 90) totalPoints += 4.0
    else if (percent >= 80) totalPoints += 3.0
    else if (percent >= 70) totalPoints += 2.0
    else if (percent >= 60) totalPoints += 1.0
    else totalPoints += 0
  })
  return totalPoints / exams.value.length
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
  // 注意：这里需要获取当前学生的ID
  // 由于后端登录响应中没有返回userId，这里需要根据实际情况调整
  // 实际项目中应该：1. 后端在登录时返回userId，2. 或者提供一个获取当前用户信息的接口
  
  loading.value = true
  try {
    // TODO: 一旦后端支持获取userId，可以取消下面的注释
    // const studentId = userStore.userInfo?.id
    // if (!studentId) {
    //   ElMessage.error('无法获取学生ID，请重新登录')
    //   return
    // }
    
    // const [record, attendanceList, examList] = await Promise.all([
    //   getStudentRecordByStudentId(studentId),
    //   getAttendancesByStudentId(studentId),
    //   getExamsByStudentId(studentId)
    // ])
    
    // studentRecord.value = record
    // attendances.value = attendanceList
    // exams.value = examList

    ElMessage.warning({
      message: '获取学生信息需要studentId。请确保后端在登录响应中包含userId，或提供获取当前用户信息的接口。',
      duration: 5000
    })
  } catch (error) {
    console.error('加载学生信息失败:', error)
    ElMessage.error('加载学生信息失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStudentInfo()
})
</script>

<style scoped lang="scss">
.record-container {
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

  .info-card,
  .section-card {
    .section-header {
      font-weight: bold;
      font-size: 16px;
    }
  }
}
</style>
