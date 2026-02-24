import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const routes = [
  {
    path: '/',
    redirect: '/employee/products',
  },
  {
    path: '/employee/products',
    name: 'ProductManagement',
    component: () => import('@/components/Employee/NV_QLSP.vue'),
  },
  {
    path: '/employee/import',
    name: 'NhapKho',
    component: () => import('@/components/Employee/NV_NhapKho.vue'),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();

  console.log('📍 Route change:', to.path);
  console.log('🔐 Auth state:', authStore.isAuthenticated);
  
  // Cho phép truy cập công khai các trang này
  const publicPaths = [
    '/',
    '/customer/index',
    '/customer/detail-product/:id?',
    '/auth/login'
  ];
  
  const isPublicPath = publicPaths.some(path => {
    if (path.includes(':')) {
      const pattern = new RegExp('^' + path.replace(/:\w+\?/g, '([^/]+)?').replace(/\//g, '\\/') + '$');
      return pattern.test(to.path);
    }
    return to.path === path;
  });
  
  if (isPublicPath) {
    next();
    return;
  }
  
  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      try {
        await authStore.fetchCurrentUser();
        if (!authStore.isAuthenticated) {
          next('/auth/login');
          return;
        }
      } catch (error) {
        console.error('Auth check error:', error);
        next('/auth/login');
        return;
      }
    }
    
    if (to.meta.role) {
      const userRole = authStore.userRole;
      if (userRole !== to.meta.role) {
        if (userRole === 'CUSTOMER') {
          next('/customer/index');
        } else if (userRole === 'EMPLOYEE') {
          next('/employee/dashboard');
        } else {
          next('/auth/login');
        }
        return;
      }
    }
  }
  
  if (to.meta.requiresGuest && authStore.isAuthenticated) {
    if (authStore.userRole === 'CUSTOMER') {
      next('/customer/index');
    } else if (authStore.userRole === 'EMPLOYEE') {
      next('/employee/dashboard');
    } else {
      next('/');
    }
    return;
  } 
  
  next();
});

export default router;