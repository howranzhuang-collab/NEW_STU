<template>
  <div class="profile-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">个人信息</span>
          <el-button
            v-if="!isEditing"
            type="primary"
            :icon="Edit"
            @click="handleEdit"
          >
            编辑
          </el-button>
          <div v-else>
            <el-button type="success" :icon="Check" :loading="saveLoading" @click="handleSave">
              保存
            </el-button>
            <el-button :icon="Close" @click="handleCancel">取消</el-button>
          </div>
        </div>
      </template>

      <div v-if="loading" style="text-align: center; padding: 40px;">
        <el-icon class="is-loading" style="font-size: 32px;"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="!studentRecord" style="text-align: center; padding: 40px;">
        <el-empty description="暂无个人信息" />
      </div>

      <el-form
        v-else
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        :disabled="!isEditing"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学号">
              <el-input :value="studentRecord.studentNumber" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input :value="studentRecord.name" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-input
                :value="studentRecord.gender === 'MALE' ? '男' : '女'"
                disabled
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="国籍">
              <el-input :value="studentRecord.nationality" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="专业">
              <el-input :value="studentRecord.major" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年级">
              <el-input :value="studentRecord.grade" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-input :value="formatDate(studentRecord.birthDate)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入学日期">
              <el-input :value="formatDate(studentRecord.enrollmentDate)" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学籍状态">
              <el-tag :type="getStatusType(studentRecord.status)">
                {{ getStatusText(studentRecord.status) }}
              </el-tag>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider />

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电子邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入电子邮箱" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="地址" prop="address">
          <el-input
            v-model="formData.address"
            type="textarea"
            :rows="3"
            placeholder="请输入地址"
          />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Edit, Check, Close, Loading } from '@element-plus/icons-vue'
import { updateStudentRecord } from '../../api/student'
import type { StudentRecord } from '../../types/entity'
const loading = ref(false)
const isEditing = ref(false)
const saveLoading = ref(false)
const studentRecord = ref<StudentRecord | null>(null)
const formRef = ref<FormInstance>()

const formData = ref({
  phone: '',
  email: '',
  address: ''
})

const formRules: FormRules = {
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  address: [
    { max: 200, message: '地址长度不能超过 200 个字符', trigger: 'blur' }
  ]
}

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

// 加载学生信息
const loadStudentInfo = async () => {
  // 注意：这里需要获取当前学生的ID
  // 由于后端登录响应中没有返回userId，这里需要根据实际情况调整
  // 实际项目中应该：1. 后端在登录时返回userId，2. 或者提供一个获取当前用户信息的接口
  
  // 临时方案：假设可以通过其他方式获取studentId
  // 这里先显示提示，实际使用时需要后端支持
  ElMessage.warning({
    message: '获取学生信息需要studentId。请确保后端在登录响应中包含userId，或提供获取当前用户信息的接口。',
    duration: 5000
  })
  
  // TODO: 一旦后端支持获取userId，可以取消下面的注释
  // if (!studentId) {
  //   ElMessage.error('无法获取学生ID，请重新登录')
  //   return
  // }
  
  // loading.value = true
  // try {
  //   const record = await getStudentRecordByStudentId(studentId)
  //   studentRecord.value = record
  //   formData.value = {
  //     phone: record.phone || '',
  //     email: record.email || '',
  //     address: record.address || ''
  //   }
  // } catch (error) {
  //   console.error('加载学生信息失败:', error)
  //   ElMessage.error('加载学生信息失败')
  // } finally {
  //   loading.value = false
  // }
}

// 编辑
const handleEdit = () => {
  if (!studentRecord.value) {
    ElMessage.warning('请先加载学生信息')
    return
  }
  isEditing.value = true
}

// 保存
const handleSave = async () => {
  if (!formRef.value || !studentRecord.value) return

  try {
    await formRef.value.validate()
    saveLoading.value = true

    await updateStudentRecord(studentRecord.value.id, {
      phone: formData.value.phone,
      email: formData.value.email,
      address: formData.value.address
    })

    ElMessage.success('保存成功')
    
    // 重新加载数据
    await loadStudentInfo()
    isEditing.value = false
  } catch (error) {
    if (error !== false) {
      console.error('保存失败:', error)
    }
  } finally {
    saveLoading.value = false
  }
}

// 取消
const handleCancel = () => {
  if (!studentRecord.value) return
  
  // 恢复原始数据
  formData.value = {
    phone: studentRecord.value.phone || '',
    email: studentRecord.value.email || '',
    address: studentRecord.value.address || ''
  }
  isEditing.value = false
}

onMounted(() => {
  loadStudentInfo()
})
</script>

<style scoped lang="scss">
.profile-container {
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
}
</style>
