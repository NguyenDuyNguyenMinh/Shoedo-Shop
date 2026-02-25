import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  {
    path: '/',
    redirect: '/auth/login',
  },
  {
    path: '/auth/login',
    name: 'Login',
    component: () => import('@/components/Auth/Login.vue'),
  },
  {
    path: '/customer/index',
    name: 'CustomerIndex',
    component: () => import('@/components/customer/KH_Index.vue'),
  },
  {
    path: '/customer/detail-product/:id?',
    name: 'DetailProduct',
    component: () => import('@/components/customer/KH_DetailProduct.vue'),
  },
  {
    path: '/customer/cart',
    name: 'Cart',
    component: () => import('@/components/customer/KH_GioHang.vue'),
  },
  {
    path: '/customer/checkout',
    name: 'Checkout',
    component: () => import('@/components/customer/KH_DatHang.vue'),
  },
  {
    path: '/customer/orders',
    name: 'Orders',
    component: () => import('@/components/customer/KH_QLDonHang.vue'),
  },
  {
    path: '/customer/order/:id',
    name: 'OrderDetail',
    component: () => import('@/components/customer/KH_CTDonHang.vue'),
  },
  {
    path: '/customer/profile',
    name: 'Profile',
    component: () => import('@/components/customer/KH_QLUser.vue'),
  },
  // Employee routes
  {
    path: '/employee/dashboard',
    name: 'EmployeeDashboard',
    component: () => import('@/components/employee/NV_Index.vue'),
  },
  {
    path: '/employee/products',
    name: 'ProductManagement',
    component: () => import('@/components/employee/NV_QLSanPham.vue'),
  },
  {
    path: '/employee/orders',
    name: 'OrderManagement',
    component: () => import('@/components/employee/NV_QLDonHang.vue'),
  },
  {
    path: '/employee/users',
    name: 'UserManagement',
    component: () => import('@/components/employee/NV_QLUser.vue'),
  },
  {
    path: '/employee/import',
    name: 'ImportStock',
    component: () => import('@/components/employee/NV_NhapKho.vue'),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});


router.beforeEach((to, from, next) => {
  next();
});

export default router;