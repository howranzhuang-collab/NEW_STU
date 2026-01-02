<template>
  <div class="dashboard-container">
    <h2 class="dashboard-title">仪表盘</h2>
    
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :md="8" :lg="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon students">
              <el-icon :size="40"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">在读学生数</div>
              <div class="stat-value">{{ stats.totalStudents }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="8" :lg="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon applications">
              <el-icon :size="40"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">待审核申请数</div>
              <div class="stat-value">{{ stats.pendingApplications }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="8" :lg="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon visas">
              <el-icon :size="40"><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">即将到期签证数</div>
              <div class="stat-value">{{ stats.expiringVisas }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="welcome-card">
      <h3>欢迎使用来华留学生服务系统</h3>
      <p>您可以通过左侧菜单管理学生、招生、收费、住宿、签证和校友信息。</p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAdminDashboardStats } from '../../api/dashboard'
import { User, Document, Tickets } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const stats = ref({
  totalStudents: 0,
  pendingApplications: 0,
  expiringVisas: 0
})

const loading = ref(false)

const loadStats = async () => {
  loading.value = true
  try {
    const data = await getAdminDashboardStats()
    stats.value = data
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  .dashboard-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: bold;
    color: #303133;
  }

  .stats-cards {
    margin-bottom: 20px;

    .stat-card {
      margin-bottom: 20px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }

      .stat-content {
        display: flex;
        align-items: center;
        padding: 10px 0;

        .stat-icon {
          width: 80px;
          height: 80px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 20px;

          &.students {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
          }

          &.applications {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            color: white;
          }

          &.visas {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
            color: white;
          }
        }

        .stat-info {
          flex: 1;

          .stat-label {
            font-size: 14px;
            color: #909399;
            margin-bottom: 10px;
          }

          .stat-value {
            font-size: 32px;
            font-weight: bold;
            color: #303133;
          }
        }
      }
    }
  }

  .welcome-card {
    margin-top: 20px;

    h3 {
      margin-bottom: 10px;
      color: #303133;
    }

    p {
      color: #606266;
      line-height: 1.8;
    }
  }
}
</style>

