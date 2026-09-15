import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 导入页面组件
import MainLayout from '@/layout/MainLayout.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'

/**
 * 路由配置
 * 定义智能房产交易平台的所有页面路由，支持角色权限控制
 */
const routes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: {
      title: '登录 - 智能房产交易平台',
      requiresAuth: false
    }
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView,
    meta: {
      title: '注册 - 智能房产交易平台',
      requiresAuth: false
    }
  },
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('@/views/HomeView.vue'),
        meta: {
          title: '首页 - 智能房产交易平台',
          requiresAuth: true,
          roles: [0, 1, 2]
        }
      },
      // 普通用户页面
      {
        path: 'property',
        name: 'property',
        component: () => import('@/views/user/PropertyListView.vue'),
        meta: {
          title: '房源浏览 - 智能房产交易平台',
          requiresAuth: true,
          roles: [0, 1, 2] // 允许所有角色访问房源列表
        }
      },
      {
        path: 'property-detail/:id',
        name: 'propertyDetail',
        component: () => import('@/views/user/PropertyDetailView.vue'),
        meta: {
          title: '房源详情 - 智能房产交易平台',
          requiresAuth: true,
          roles: [0, 1, 2] // 允许所有角色访问房源详情
        }
      },
      {
        path: 'favorites',
        name: 'favorites',
        component: () => import('@/views/user/FavoritesView.vue'),
        meta: {
          title: '我的收藏 - 智能房产交易平台',
          requiresAuth: true,
          roles: [0]
        }
      },
      {
        path: 'appointments',
        name: 'appointments',
        component: () => import('@/views/user/AppointmentsView.vue'),
        meta: {
          title: '预约记录 - 智能房产交易平台',
          requiresAuth: true,
          roles: [0]
        }
      },
      {
        path: 'transaction-process/:id',
        name: 'transactionProcess',
        component: () => import('@/views/TransactionProcess.vue'),
        meta: {
          title: '交易流程 - 智能房产交易平台',
          requiresAuth: true,
          roles: [0, 1] // 用户和房东都可以查看
        }
      },
      {
        path: 'transaction-process-enhanced/:id',
        name: 'transactionProcessEnhanced',
        component: () => import('@/views/TransactionProcessEnhanced.vue'),
        meta: {
          title: '交易流程管理（增强版） - 智能房产交易平台',
          requiresAuth: true,
          roles: [0, 1] // 用户和房东都可以查看
        }
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/ProfileView.vue'),
        meta: {
          title: '个人中心 - 智能房产交易平台',
          requiresAuth: true,
          roles: [0, 1]
        }
      },
      // AI功能页面
      {
        path: 'ai-qa',
        name: 'aiQA',
        component: () => import('@/views/AIQAView.vue'),
        meta: {
          title: 'AI智能问答 - 智能房产交易平台',
          requiresAuth: true,
          roles: [0, 1] // 用户和房东可用
        }
      },
      // 房东页面
      {
        path: 'landlord/publish',
        name: 'landlordPublish',
        component: () => import('@/views/landlord/PublishPropertyView.vue'),
        meta: {
          title: '发布房源 - 智能房产交易平台',
          requiresAuth: true,
          roles: [1]
        }
      },
      {
        path: 'landlord/my-properties',
        name: 'landlordMyProperties',
        component: () => import('@/views/landlord/MyPropertiesView.vue'),
        meta: {
          title: '我的房源 - 智能房产交易平台',
          requiresAuth: true,
          roles: [1]
        }
      },
      {
        path: 'landlord/edit/:id',
        name: 'landlordEdit',
        component: () => import('@/views/landlord/EditPropertyView.vue'),
        meta: {
          title: '编辑房源 - 智能房产交易平台',
          requiresAuth: true,
          roles: [1]
        }
      },
      {
        path: 'landlord/appointments',
        name: 'landlordAppointments',
        component: () => import('@/views/landlord/LandlordAppointmentsView.vue'),
        meta: {
          title: '预约管理 - 智能房产交易平台',
          requiresAuth: true,
          roles: [1]
        }
      },
      // 管理员页面
      {
        path: 'admin/approval',
        name: 'adminApproval',
        component: () => import('@/views/admin/PropertyApprovalView.vue'),
        meta: {
          title: '房源审核 - 智能房产交易平台',
          requiresAuth: true,
          roles: [2]
        }
      },
      {
        path: 'admin/property',
        name: 'adminProperty',
        component: () => import('@/views/admin/PropertyManageView.vue'),
        meta: {
          title: '房源管理 - 智能房产交易平台',
          requiresAuth: true,
          roles: [2]
        }
      },
      {
        path: 'admin/property/edit/:id',
        name: 'adminPropertyEdit',
        component: () => import('@/views/landlord/EditPropertyView.vue'),
        meta: {
          title: '编辑房源 - 智能房产交易平台',
          requiresAuth: true,
          roles: [2]
        }
      },
      {
        path: 'admin/users',
        name: 'adminUsers',
        component: () => import('@/views/admin/UserManageView.vue'),
        meta: {
          title: '用户管理 - 智能房产交易平台',
          requiresAuth: true,
          roles: [2]
        }
      },
      {
        path: 'admin/appointments',
        name: 'adminAppointments',
        component: () => import('@/views/admin/AppointmentManageView.vue'),
        meta: {
          title: '预约管理 - 智能房产交易平台',
          requiresAuth: true,
          roles: [2]
        }
      },
      {
        path: 'admin/messages',
        name: 'adminMessages',
        component: () => import('@/views/admin/MessageManageView.vue'),
        meta: {
          title: '留言管理 - 智能房产交易平台',
          requiresAuth: true,
          roles: [2]
        }
      },
      {
        path: 'admin/favorites',
        name: 'adminFavorites',
        component: () => import('@/views/admin/FavoriteManageView.vue'),
        meta: {
          title: '收藏管理 - 智能房产交易平台',
          requiresAuth: true,
          roles: [2]
        }
      },
      {
        path: 'admin/setting',
        name: 'adminSetting',
        component: () => import('@/views/admin/SettingView.vue'),
        meta: {
          title: '系统管理 - 智能房产交易平台',
          requiresAuth: true,
          roles: [2]
        }
      },
      // 用户留言页面
      {
        path: 'messages',
        name: 'messages',
        component: () => import('@/views/user/MessagesView.vue'),
        meta: {
          title: '我的消息 - 智能房产交易平台',
          requiresAuth: true,
          roles: [0]
        }
      },
      // 对话详情页面
      {
        path: 'messages/conversation/:propertyId',
        name: 'messageConversation',
        component: () => import('@/views/user/MessageConversationView.vue'),
        meta: {
          title: '对话详情 - 智能房产交易平台',
          requiresAuth: true,
          roles: [0]
        }
      },
      // 房东留言页面
      {
        path: 'landlord/messages',
        name: 'landlordMessages',
        component: () => import('@/views/landlord/LandlordMessagesView.vue'),
        meta: {
          title: '我的消息 - 智能房产交易平台',
          requiresAuth: true,
          roles: [1]
        }
      },
      // 房东对话详情页面
      {
        path: 'landlord/messages/conversation/:propertyId/:userId',
        name: 'landlordConversation',
        component: () => import('@/views/landlord/LandlordConversationView.vue'),
        meta: {
          title: '对话详情 - 智能房产交易平台',
          requiresAuth: true,
          roles: [1]
        }
      }
    ]
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 获取登录状态（直接从localStorage读取，避免Pinia状态不同步问题）
const getLoginStatus = () => {
  const token = localStorage.getItem('token')
  const userInfoStr = localStorage.getItem('userInfo')
  
  if (!token || !userInfoStr) {
    return { isLoggedIn: false, userRole: 0 }
  }
  
  try {
    const userInfo = JSON.parse(userInfoStr)
    return {
      isLoggedIn: true,
      userRole: userInfo.role || 0
    }
  } catch {
    return { isLoggedIn: false, userRole: 0 }
  }
}

// 路由守卫：权限控制
router.beforeEach((to, from, next) => {
  const { isLoggedIn, userRole } = getLoginStatus()

  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title as string
  } else {
    document.title = '智能房产交易平台'
  }

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    // 检查是否已登录
    if (!isLoggedIn) {
      next('/login')
      return
    }

    // 检查角色权限
    const requiredRoles = to.meta.roles as number[]
    if (requiredRoles && requiredRoles.length > 0) {
      if (!requiredRoles.includes(userRole)) {
        // 角色不匹配，跳转到首页
        next('/')
        return
      }
    }
  }

  // 如果已登录但访问登录/注册页，跳转到首页
  if ((to.path === '/login' || to.path === '/register') && isLoggedIn) {
    next('/')
    return
  }

  next()
})

export default router
