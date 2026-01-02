<template>
  <el-container class="layout-container">
    <!-- 顶部导航栏 -->
    <el-header class="layout-header">
      <div class="header-left">
        <el-icon class="collapse-icon mobile-only" @click="mobileMenuVisible = !mobileMenuVisible">
          <Menu />
        </el-icon>
        <el-icon class="collapse-icon desktop-only" @click="isCollapse = !isCollapse">
          <Expand v-if="isCollapse" />
          <Fold v-else />
        </el-icon>
        <span class="header-title">来华留学生服务系统 - 学生端</span>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-icon><User /></el-icon>
            <span class="username-text">{{ userStore.userInfo?.username }}</span>
            <el-icon class="el-icon--right"><CaretBottom /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container>
      <!-- 侧边菜单 - 桌面端 -->
      <el-aside :width="isCollapse ? '64px' : '200px'" class="layout-aside desktop-only">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          class="layout-menu"
        >
          <el-menu-item index="/student/home">
            <el-icon><House /></el-icon>
            <template #title>首页</template>
          </el-menu-item>

          <el-menu-item index="/student/profile">
            <el-icon><User /></el-icon>
            <template #title>个人信息</template>
          </el-menu-item>

          <el-menu-item index="/student/record">
            <el-icon><Document /></el-icon>
            <template #title>学籍查看</template>
          </el-menu-item>

          <el-menu-item index="/student/fees">
            <el-icon><Wallet /></el-icon>
            <template #title>缴费查询</template>
          </el-menu-item>

          <el-menu-item index="/student/dorm">
            <el-icon><HomeFilled /></el-icon>
            <template #title>住宿信息</template>
          </el-menu-item>

          <el-menu-item index="/student/visa">
            <el-icon><Tickets /></el-icon>
            <template #title>签证信息</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 移动端抽屉菜单 -->
      <el-drawer
        v-model="mobileMenuVisible"
        title="菜单"
        :with-header="true"
        direction="ltr"
        size="250px"
        class="mobile-drawer"
      >
        <el-menu
          :default-active="activeMenu"
          router
          class="mobile-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/student/home">
            <el-icon><House /></el-icon>
            <template #title>首页</template>
          </el-menu-item>

          <el-menu-item index="/student/profile">
            <el-icon><User /></el-icon>
            <template #title>个人信息</template>
          </el-menu-item>

          <el-menu-item index="/student/record">
            <el-icon><Document /></el-icon>
            <template #title>学籍查看</template>
          </el-menu-item>

          <el-menu-item index="/student/fees">
            <el-icon><Wallet /></el-icon>
            <template #title>缴费查询</template>
          </el-menu-item>

          <el-menu-item index="/student/dorm">
            <el-icon><HomeFilled /></el-icon>
            <template #title>住宿信息</template>
          </el-menu-item>

          <el-menu-item index="/student/visa">
            <el-icon><Tickets /></el-icon>
            <template #title>签证信息</template>
          </el-menu-item>
        </el-menu>
      </el-drawer>

      <!-- 主内容区 -->
      <el-main class="layout-main">
        <!-- 面包屑 -->
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/student/home' }">
            <el-icon><House /></el-icon>
            <span style="margin-left: 4px;">首页</span>
          </el-breadcrumb-item>
          <el-breadcrumb-item
            v-for="(item, index) in breadcrumbItems"
            :key="index"
            :to="index < breadcrumbItems.length - 1 ? getBreadcrumbPath(index) : undefined"
          >
            {{ item }}
          </el-breadcrumb-item>
        </el-breadcrumb>

        <!-- 路由视图 -->
        <div class="content-wrapper">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import {
  Expand,
  Fold,
  User,
  CaretBottom,
  House,
  Document,
  Wallet,
  HomeFilled,
  Tickets,
  Menu
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const mobileMenuVisible = ref(false)

// 菜单选择处理（移动端）
const handleMenuSelect = () => {
  mobileMenuVisible.value = false
}

// 获取面包屑路径
const getBreadcrumbPath = (index: number) => {
  const segments = route.path.split('/').filter(Boolean)
  return '/' + segments.slice(0, index + 2).join('/')
}

// 当前激活的菜单项
const activeMenu = computed(() => route.path)

// 页面标题映射
const pageTitleMap: Record<string, string> = {
  'home': '首页',
  'profile': '个人信息',
  'record': '学籍查看',
  'fees': '缴费查询',
  'dorm': '住宿信息',
  'visa': '签证信息'
}

// 面包屑
const breadcrumbItems = computed(() => {
  const path = route.path
  if (path === '/student/home') return []
  const segments = path.split('/').filter(Boolean)
  return segments.slice(1).map(segment => {
    return pageTitleMap[segment] || segment
  })
})

// 处理用户下拉菜单命令
const handleCommand = async (command: string) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      userStore.logout()
      router.push('/login')
    } catch {
      // 用户取消
    }
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .header-left {
    display: flex;
    align-items: center;
    gap: 15px;

    .collapse-icon {
      cursor: pointer;
      font-size: 20px;
      transition: transform 0.3s;
      padding: 5px;

      &:hover {
        transform: scale(1.1);
        background-color: rgba(255, 255, 255, 0.1);
        border-radius: 4px;
      }
    }

    .header-title {
      font-size: 18px;
      font-weight: bold;
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      color: white;
      padding: 5px 10px;
      border-radius: 4px;
      transition: background-color 0.3s;
      
      &:hover {
        background-color: rgba(255, 255, 255, 0.1);
      }

      .username-text {
        @media (max-width: 768px) {
          display: none;
        }
      }
    }
  }
}

// 响应式显示/隐藏
.desktop-only {
  @media (max-width: 768px) {
    display: none !important;
  }
}

.mobile-only {
  display: none;
  @media (max-width: 768px) {
    display: block;
  }
}

.layout-aside {
  background-color: #304156;
  transition: width 0.3s;

  @media (max-width: 768px) {
    display: none;
  }

  .layout-menu {
    border-right: none;
    background-color: #304156;
    color: #bfcbd9;
    height: 100%;

    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      color: #bfcbd9;
      transition: all 0.3s;

      &:hover {
        background-color: #263445;
      }
    }

    :deep(.el-menu-item.is-active) {
      background-color: #409eff;
      color: white;
    }
  }
}

.mobile-drawer {
  :deep(.el-drawer__header) {
    margin-bottom: 0;
    padding: 20px;
    border-bottom: 1px solid #e4e7ed;
  }

  .mobile-menu {
    border-right: none;
  }
}

.layout-main {
  background-color: #f0f2f5;
  padding: 15px;
  overflow-x: hidden;

  @media (max-width: 768px) {
    padding: 10px;
  }

  .breadcrumb {
    margin-bottom: 15px;
    padding: 12px;
    background-color: white;
    border-radius: 4px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

    @media (max-width: 768px) {
      padding: 8px;
      margin-bottom: 10px;
    }
  }

  .content-wrapper {
    background-color: white;
    border-radius: 4px;
    padding: 20px;
    min-height: calc(100vh - 140px);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

    @media (max-width: 768px) {
      padding: 15px;
      min-height: calc(100vh - 120px);
    }
  }
}
</style>

