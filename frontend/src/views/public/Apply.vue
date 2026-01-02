<template>
  <div class="apply-container">
    <el-card class="apply-card">
      <template #header>
        <div class="card-header">
          <h2>来华留学生申请</h2>
        </div>
      </template>

      <el-steps :active="currentStep" finish-status="success" align-center>
        <el-step title="用户注册" />
        <el-step title="选择项目" />
        <el-step title="填写申请" />
        <el-step title="提交完成" />
      </el-steps>

      <!-- 步骤1: 用户注册 -->
      <div v-if="currentStep === 0" class="step-content">
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-width="100px"
          style="max-width: 500px; margin: 40px auto;"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="registerForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="registerLoading" @click="handleRegister">
              注册并继续
            </el-button>
            <el-button @click="handleSkipRegister">已有账号，跳过注册</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤2: 选择项目 -->
      <div v-if="currentStep === 1" class="step-content">
        <el-form
          ref="projectFormRef"
          :model="projectForm"
          :rules="projectRules"
          label-width="100px"
          style="max-width: 600px; margin: 40px auto;"
        >
          <el-form-item label="选择项目" prop="projectId">
            <el-select
              v-model="projectForm.projectId"
              placeholder="请选择招生项目"
              style="width: 100%"
              filterable
            >
              <el-option
                v-for="project in availableProjects"
                :key="project.id"
                :label="`${project.title} (截止: ${formatDate(project.deadline)})`"
                :value="project.id"
              >
                <div>
                  <div style="font-weight: bold;">{{ project.title }}</div>
                  <div style="font-size: 12px; color: #909399;">
                    名额: {{ project.appliedCount }}/{{ project.quota }} | 
                    截止: {{ formatDate(project.deadline) }}
                  </div>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="projectLoading" @click="handleSelectProject">
              下一步
            </el-button>
            <el-button @click="currentStep = 0">上一步</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤3: 填写申请 -->
      <div v-if="currentStep === 2" class="step-content">
        <el-form
          ref="applicationFormRef"
          :model="applicationForm"
          :rules="applicationRules"
          label-width="120px"
          style="max-width: 600px; margin: 40px auto;"
        >
          <el-form-item label="项目名称">
            <el-input :value="selectedProject?.title" disabled />
          </el-form-item>
          <el-form-item label="上传文件" prop="file">
            <el-upload
              v-model:file-list="fileList"
              :auto-upload="false"
              :limit="1"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
            >
              <el-button type="primary">选择文件</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  支持 PDF、Word、图片格式，文件大小不超过 10MB
                </div>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item label="备注信息" prop="remarks">
            <el-input
              v-model="applicationForm.remarks"
              type="textarea"
              :rows="4"
              placeholder="请输入备注信息（可选）"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
              提交申请
            </el-button>
            <el-button @click="currentStep = 1">上一步</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤4: 提交完成 -->
      <div v-if="currentStep === 3" class="step-content">
        <el-result
          icon="success"
          title="申请提交成功"
          sub-title="您的申请已提交，请等待管理员审核。审核结果将通过邮件或系统通知您。"
        >
          <template #extra>
            <el-button type="primary" @click="handleReset">重新申请</el-button>
            <el-button @click="$router.push('/login')">前往登录</el-button>
          </template>
        </el-result>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules, type UploadFile } from 'element-plus'
import { register } from '../../api/auth'
import { getAllProjects, submitApplication } from '../../api/admission'
import type { AdmissionProject } from '../../types/entity'

const currentStep = ref(0)
const registerLoading = ref(false)
const projectLoading = ref(false)
const submitLoading = ref(false)
const studentId = ref<number | null>(null)
const availableProjects = ref<AdmissionProject[]>([])
const selectedProject = ref<AdmissionProject | null>(null)
const fileList = ref<UploadFile[]>([])

const registerFormRef = ref<FormInstance>()
const projectFormRef = ref<FormInstance>()
const applicationFormRef = ref<FormInstance>()

const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: ''
})

const projectForm = ref({
  projectId: null as number | null
})

const applicationForm = ref({
  file: null as File | null,
  remarks: ''
})

// 注册表单验证规则
const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== registerForm.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 项目选择表单验证规则
const projectRules: FormRules = {
  projectId: [
    { required: true, message: '请选择招生项目', trigger: 'change' }
  ]
}

// 申请表单验证规则
const applicationRules: FormRules = {
  remarks: [
    { max: 500, message: '备注信息不能超过 500 个字符', trigger: 'blur' }
  ]
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return date
}

// 加载可用项目
const loadProjects = async () => {
  try {
    const projects = await getAllProjects()
    // 只显示开放状态且未截止的项目
    const today = new Date().toISOString().split('T')[0] || ''
    availableProjects.value = projects.filter(
      project => project.status === 'OPEN' && project.deadline >= today
    )
  } catch (error) {
    console.error('加载项目列表失败:', error)
    ElMessage.error('加载项目列表失败')
  }
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
    registerLoading.value = true

    const result = await register(
      registerForm.value.username,
      registerForm.value.password,
      'STUDENT'
    )
    
    studentId.value = result.userId
    ElMessage.success('注册成功')
    currentStep.value = 1
  } catch (error) {
    console.error('注册失败:', error)
    // 错误消息已由拦截器显示
  } finally {
    registerLoading.value = false
  }
}

// 跳过注册（使用已有账号）
const handleSkipRegister = () => {
  ElMessage.info('请先登录后再提交申请')
  // 可以跳转到登录页
}

// 选择项目
const handleSelectProject = async () => {
  if (!projectFormRef.value) return

  try {
    await projectFormRef.value.validate()
    projectLoading.value = true

    const project = availableProjects.value.find(
      p => p.id === projectForm.value.projectId
    )
    if (!project) {
      ElMessage.error('项目不存在')
      return
    }

    selectedProject.value = project
    currentStep.value = 2
  } catch (error) {
    console.error('选择项目失败:', error)
  } finally {
    projectLoading.value = false
  }
}

// 文件变化
const handleFileChange = (file: UploadFile) => {
  if (file.raw) {
    // 检查文件大小（10MB）
    if (file.raw.size > 10 * 1024 * 1024) {
      ElMessage.error('文件大小不能超过 10MB')
      fileList.value = []
      return
    }
    applicationForm.value.file = file.raw
  }
}

// 移除文件
const handleFileRemove = () => {
  applicationForm.value.file = null
}

// 提交申请
const handleSubmit = async () => {
  if (!applicationFormRef.value) return

  try {
    await applicationFormRef.value.validate()

    if (!studentId.value) {
      ElMessage.error('请先完成用户注册')
      currentStep.value = 0
      return
    }

    if (!projectForm.value.projectId) {
      ElMessage.error('请选择招生项目')
      currentStep.value = 1
      return
    }

    submitLoading.value = true

    await submitApplication(
      projectForm.value.projectId,
      studentId.value,
      applicationForm.value.file || undefined,
      applicationForm.value.remarks || undefined
    )

    ElMessage.success('申请提交成功')
    currentStep.value = 3
  } catch (error) {
    console.error('提交申请失败:', error)
    // 错误消息已由拦截器显示
  } finally {
    submitLoading.value = false
  }
}

// 重置表单
const handleReset = () => {
  currentStep.value = 0
  studentId.value = null
  selectedProject.value = null
  fileList.value = []
  registerForm.value = {
    username: '',
    password: '',
    confirmPassword: ''
  }
  projectForm.value = {
    projectId: null
  }
  applicationForm.value = {
    file: null,
    remarks: ''
  }
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped lang="scss">
.apply-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
  display: flex;
  justify-content: center;
  align-items: center;

  .apply-card {
    width: 100%;
    max-width: 900px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);

    .card-header {
      text-align: center;

      h2 {
        margin: 0;
        color: #303133;
        font-size: 24px;
      }
    }

    .step-content {
      padding: 40px 20px;
      min-height: 400px;
    }
  }
}
</style>
