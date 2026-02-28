import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const routes = [
  {
    path: '/',
    redirect: '/customer/index',
  },
  {
    path: '/auth/login',
    name: 'Login',
    component: () => import('@/components/auth/Login.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/customer/index',
    name: 'CustomerIndex',
    component: () => import('@/components/customer/KH_Index.vue'),
    meta: { requiresAuth: true, role: 'CUSTOMER' }
  },
  {
    path: '/customer/detail-product/:id?',
    name: 'DetailProduct',
    component: () => import('@/components/customer/KH_DetailProduct.vue'),
    meta: { requiresAuth: true, role: 'CUSTOMER' }
  },
  {
    path: '/customer/cart',
    name: 'Cart',
    component: () => import('@/components/customer/KH_GioHang.vue'),
    meta: { requiresAuth: true, role: 'CUSTOMER' }
  },
  {
    path: '/customer/checkout',
    name: 'Checkout',
    component: () => import('@/components/customer/KH_DatHang.vue'),
    meta: { requiresAuth: true, role: 'CUSTOMER' }
  },
  {
    path: '/customer/orders',
    name: 'Orders',
    component: () => import('@/components/customer/KH_QLDonHang.vue'),
    meta: { requiresAuth: true, role: 'CUSTOMER' }
  },
  {
    path: '/customer/order/:id',
    name: 'OrderDetail',
    component: () => import('@/components/customer/KH_CTDonHang.vue'),
    meta: { requiresAuth: true, role: 'CUSTOMER' }
  },
  {
    path: '/customer/profile',
    name: 'Profile',
    component: () => import('@/components/customer/KH_QLProfile.vue'),
    meta: { requiresAuth: true, role: 'CUSTOMER' }
  },
  // Employee routes
  {
    path: '/employee/dashboard',
    name: 'EmployeeDashboard',
    component: () => import('@/components/employee/NV_Index.vue'),
    meta: { requiresAuth: true, role: 'EMPLOYEE' }
  },
  {
    path: '/employee/products',
    name: 'ProductManagement',
    component: () => import('@/components/employee/NV_QLSanPham.vue'),
    meta: { requiresAuth: true, role: 'EMPLOYEE' }
  },
  {
    path: '/employee/orders',
    name: 'OrderManagement',
    component: () => import('@/components/employee/NV_QLDonHang.vue'),
    meta: { requiresAuth: true, role: 'EMPLOYEE' }
  },
  {
    path: '/employee/users',
    name: 'UserManagement',
    component: () => import('@/components/employee/NV_QLUser.vue'),
    meta: { requiresAuth: true, role: 'EMPLOYEE' }
  },
  {
    path: '/employee/import',
    name: 'ImportStock',
    component: () => import('@/components/employee/NV_NhapKho.vue'),
    meta: { requiresAuth: true, role: 'EMPLOYEE' }
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
  
  // Kiểm tra nếu route là public
  const isPublicPath = publicPaths.some(path => {
    if (path.includes(':')) {
      // Kiểm tra pattern
      const pattern = new RegExp('^' + path.replace(/:\w+\?/g, '([^/]+)?').replace(/\//g, '\\/') + '$');
      return pattern.test(to.path);
    }
    return to.path === path;
  });
  
  if (isPublicPath) {
    next();
    return;
  }
  
  // Kiểm tra nếu route yêu cầu đăng nhập
  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      // Thử lấy user từ server
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
    
    // Kiểm tra quyền truy cập
    if (to.meta.role) {
      const userRole = authStore.userRole;
      if (userRole !== to.meta.role) {
        // Redirect dựa trên role
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
  
  // Kiểm tra nếu route yêu cầu chưa đăng nhập
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