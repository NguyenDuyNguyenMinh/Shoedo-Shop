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
    component: () => import('@/components/Auth/Login.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/customer/index',
    name: 'CustomerIndex',
    component: () => import('@/components/Customer/KH_index.vue')
  },
  {
    path: '/customer/detail-product/:id?',
    name: 'DetailProduct',
    component: () => import('@/components/Customer/KH_DetailProduct.vue')
  },
  {
    path: '/customer/cart',
    name: 'Cart',
    component: () => import('@/components/Customer/KH_GioHang.vue'),
    meta: { requiresAuth: true, role: 'CUSTOMER' }
  },
  {
    path: '/customer/chinhsach',
    name: 'ChinhSach',
    component: () => import('@/components/Customer/KH_ChinhSach.vue')
  },
  {
    path: '/customer/sanpham',
    name: 'Sanpham',
    component: () => import('@/components/Customer/KH_Sanpham.vue')
  },
  {
    path: '/customer/checkout',
    name: 'Checkout',
    component: () => import('@/components/Customer/KH_DatHang.vue'),
    meta: { requiresAuth: true, role: 'CUSTOMER' }
  },
  {
    path: '/payment-result',
    name: 'PaymentResult',
    component: () => import('@/components/Customer/KH_PaymentResult.vue'),
  },
  {
    path: '/customer/orders',
    name: 'Orders',
    component: () => import('@/components/Customer/KH_QLDonHang.vue'),
    meta: { requiresAuth: true, role: 'CUSTOMER' }
  },
  {
    path: '/customer/orders/:id',
    name: 'OrderDetail',
    component: () => import('@/components/Customer/KH_CTDonHang.vue'),
    meta: { requiresAuth: true, role: 'CUSTOMER' }
  },
  {
    path: '/customer/profile',
    name: 'Profile',
    component: () => import('@/components/Customer/KH_QLProfile.vue'),
    meta: { requiresAuth: true, role: 'CUSTOMER' }
  },
  // Employee routes
  {
    path: '/employee/dashboard',
    name: 'EmployeeDashboard',
    component: () => import('@/components/Employee/NV_ThongKe.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/employee/products',
    name: 'ProductManagement',
    component: () => import('@/components/Employee/NV_QLSP.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'EMPLOYEE'] }
  },
  {
    path: '/employee/orders',
    name: 'OrderManagement',
    component: () => import('@/components/Employee/NV_QLHoaDon.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'EMPLOYEE'] }
  },
  {
    path: '/employee/users',
    name: 'UserManagement',
    component: () => import('@/components/Employee/NV_QLUser.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'EMPLOYEE'] }
  },
  {
    path: '/employee/danhgia',
    name: 'DanhGiaManagement',
    component: () => import('@/components/Employee/NV_QLDanhGia.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'EMPLOYEE'] }
  },
  {
    path: '/employee/import',
    name: 'ImportStock',
    component: () => import('@/components/Employee/NV_NhapKho.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'EMPLOYEE'] }
  },
  {
    path: '/employee/flashsale',
    name: 'FlashSaleStock',
    component: () => import('@/components/Employee/NV_KhuyenMai.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'EMPLOYEE'] }
  },
  {
    path: '/employee/voucher',
    name: 'VoucherSet',
    component: () => import('@/components/Employee/NV_QLVoucher.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'EMPLOYEE'] }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();

  if (!authStore.isInitialized) {
    await authStore.initAuth();
  }

  if (to.meta.requiresGuest && authStore.isAuthenticated) {
    const role = authStore.userRole;
    return next(role === 'CUSTOMER' ? '/customer/index' : '/employee/dashboard');
  }

  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      if (await authStore.fetchCurrentUser()) {
        checkAccessValidation(to, authStore, next);
      } else {
        return next('/auth/login');
      }
    } else {
      checkAccessValidation(to, authStore, next);
    }
  } else {
    next();
  }
});

function checkAccessValidation(to, authStore, next) {
  const role = authStore.userRole;

  if (to.meta.role && to.meta.role !== role) {
    return redirectRoleBased(role, next);
  }
  if (to.meta.roles && !to.meta.roles.includes(role)) {
    return redirectRoleBased(role, next);
  }
  next();
}

function redirectRoleBased(role, next) {
  if (role === 'CUSTOMER') return next('/customer/index');
  if (role === 'ADMIN') return next('/employee/dashboard');
  if (role === 'EMPLOYEE') return next('/employee/flashsale');
  return next('/auth/login');
}

export default router;