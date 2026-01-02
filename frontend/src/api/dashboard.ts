import http from './http'

/**
 * 管理员仪表盘统计数据
 */
export interface AdminDashboardStats {
  totalStudents: number
  pendingApplications: number
  expiringVisas: number
}

/**
 * 获取管理员仪表盘统计数据
 */
export async function getAdminDashboardStats(): Promise<AdminDashboardStats> {
  // 由于后端没有专门的统计接口，这里通过多个接口组合获取数据
  const results = await Promise.all([
    http.get('/students/records'),
    http.get('/admission/projects'),
    http.get('/visas/expiring-soon?days=30')
  ])
  const students = (results[0] as unknown) as any[]
  const projects = (results[1] as unknown) as any[]
  const visas = (results[2] as unknown) as any[]

  // 统计在读学生数（status = 'ACTIVE'）
  const totalStudents = students.filter((s: any) => s.status === 'ACTIVE').length

  // 统计待审核申请数
  let pendingApplications = 0
  for (const project of projects) {
    try {
      const applications = await http.get(`/admission/applications/project/${project.id}`) as any[]
      pendingApplications += applications.filter((app: any) => app.status === 'PENDING').length
    } catch (error) {
      console.error(`获取项目 ${project.id} 的申请列表失败:`, error)
    }
  }

  // 统计即将到期的签证数（30天内）
  const expiringVisas = visas.length

  return {
    totalStudents,
    pendingApplications,
    expiringVisas
  }
}

