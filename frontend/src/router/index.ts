import { createRouter, createWebHistory, type RouteLocationNormalized } from 'vue-router'
import { useUserStore } from '../stores/user'
import Login from '../views/Login.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import StudentLayout from '../layouts/StudentLayout.vue'
import AdminHome from '../views/AdminHome.vue'
import StudentHome from '../views/StudentHome.vue'
import NotFound from '../views/NotFound.vue'
import ProjectList from '../views/admin/admission/ProjectList.vue'
import ApplicationList from '../views/admin/admission/ApplicationList.vue'
import StudentList from '../views/admin/student/StudentList.vue'
import StudentDetail from '../views/admin/student/StudentDetail.vue'
import FeeList from '../views/admin/fee/FeeList.vue'
import DormList from '../views/admin/dorm/DormList.vue'
import VisaList from '../views/admin/visa/VisaList.vue'
import AlumniList from '../views/admin/alumni/AlumniList.vue'
import Apply from '../views/public/Apply.vue'
import Profile from '../views/student/Profile.vue'
import Record from '../views/student/Record.vue'
import Fee from '../views/student/Fee.vue'
import Dorm from '../views/student/Dorm.vue'
import Visa from '../views/student/Visa.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: Login,
      meta: { requiresAuth: false }
    },
    {
      path: '/apply',
      name: 'Apply',
      component: Apply,
      meta: { requiresAuth: false }
    },
    // 管理员路由
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, role: 'ADMIN' },
      children: [
        {
          path: 'home',
          name: 'AdminHome',
          component: AdminHome
        },
        {
          path: 'admission/projects',
          name: 'ProjectList',
          component: ProjectList
        },
        {
          path: 'admission/applications',
          name: 'ApplicationList',
          component: ApplicationList
        },
        {
          path: 'students',
          name: 'StudentList',
          component: StudentList
        },
        {
          path: 'students/detail',
          name: 'StudentDetail',
          component: StudentDetail
        },
        {
          path: 'fees',
          name: 'FeeList',
          component: FeeList
        },
        {
          path: 'dorm',
          name: 'DormList',
          component: DormList
        },
        {
          path: 'visa',
          name: 'VisaList',
          component: VisaList
        },
        {
          path: 'alumni',
          name: 'AlumniList',
          component: AlumniList
        }
      ]
    },
    // 学生路由
    {
      path: '/student',
      component: StudentLayout,
      meta: { requiresAuth: true, role: 'STUDENT' },
      children: [
        {
          path: 'home',
          name: 'StudentHome',
          component: StudentHome
        },
        {
          path: 'profile',
          name: 'StudentProfile',
          component: Profile
        },
        {
          path: 'record',
          name: 'StudentRecord',
          component: Record
        },
        {
          path: 'fees',
          name: 'StudentFee',
          component: Fee
        },
        {
          path: 'dorm',
          name: 'StudentDorm',
          component: Dorm
        },
        {
          path: 'visa',
          name: 'StudentVisa',
          component: Visa
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: NotFound
    }
  ]
})

// 路由守卫
router.beforeEach((to: RouteLocationNormalized, _from, next) => {
  const userStore = useUserStore()
  const requiresAuth = to.meta.requiresAuth !== false
  const requiredRole = to.meta.role as string | undefined

  // 如果不需要认证（如登录页），直接通过
  if (!requiresAuth) {
    // 如果已登录，重定向到对应首页
    if (userStore.token && userStore.userInfo) {
      if (userStore.userInfo.role === 'ADMIN') {
        next('/admin/home')
      } else if (userStore.userInfo.role === 'STUDENT') {
        next('/student/home')
      } else {
        next()
      }
    } else {
      next()
    }
    return
  }

  // 需要认证的路由
  if (!userStore.token || !userStore.userInfo) {
    // 未登录，跳转到登录页
    next('/login')
    return
  }

  // 检查角色权限
  if (requiredRole && userStore.userInfo.role !== requiredRole) {
    // 角色不匹配，跳转到对应角色的首页
    if (userStore.userInfo.role === 'ADMIN') {
      next('/admin/home')
    } else if (userStore.userInfo.role === 'STUDENT') {
      next('/student/home')
    } else {
      next('/login')
    }
    return
  }

  next()
})

export default router
