import { createRouter, createWebHistory } from 'vue-router';

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

export default router;