<template>
  <aside class="sidebar" id="sidebar">
    <div class="sidebar-header">
      <h3>ShoeDo Shop</h3>
      <small>Quản lý cửa hàng của bạn</small>
      
      <!-- User info -->
      <div class="user-info" v-if="authStore.user">
        <div class="user-greeting">
          <i class="fa-solid fa-user me-2"></i>
          <span>Xin chào, 
            <span class="fw-bold" id="userLastName">
              {{ getLastName(authStore.userName) }}
            </span>
          </span>
        </div>
        
        <!-- Role badge -->
        <div>
          <span class="user-role-badge" :class="getRoleBadgeClass()">
            <i :class="getRoleIcon()"></i>
            {{ getRoleDisplayText() }}
          </span>
        </div>
        
        <div class="user-email">{{ authStore.user?.mail }}</div>
      </div>
      
      <div v-else class="user-info">
        <div class="user-greeting">
          <i class="bi bi-person-x"></i>
          <span>Chưa đăng nhập</span>
        </div>
      </div>
    </div>
    
    <ul class="sidebar-menu">
      <li>
        <router-link to="/employee/dashboard" :class="{ active: $route.path === '/employee/dashboard' }">
          <i class="bi bi-speedometer2"></i>
          <span>Thống kê</span>
        </router-link>
      </li>
      <li>
        <router-link to="/employee/products" :class="{ active: $route.path === '/employee/products' }">
          <i class="bi bi-box-seam"></i>
          <span>Quản lý sản phẩm</span>
        </router-link>
      </li>
      <li>
        <router-link to="/employee/users" :class="{ active: $route.path === '/employee/users' }">
          <i class="fa-regular fa-circle-user"></i>
          <span>Quản lý user</span>
        </router-link>
      </li>
      <li>
        <router-link to="/employee/orders" :class="{ active: $route.path === '/employee/orders' }">
          <i class="bi bi-cart-check"></i>
          <span>Đơn hàng</span>
        </router-link>
      </li>
      <li>
        <router-link to="/employee/import" :class="{ active: $route.path === '/employee/import' }">
          <i class="fa-solid fa-inbox"></i>
          <span>Nhập Kho</span>
        </router-link>
      </li>
            <li>
        <router-link to="/employee/flashsale" :class="{ active: $route.path === '/employee/' }">
          <i class="fa-solid fa-tag"></i>
          <span>Khuyến Mãi</span>
        </router-link>
      </li>
      <li>
        <a href="#" @click.prevent="logout">
          <i class="bi bi-box-arrow-right"></i>
          <span>Đăng xuất</span>
        </a>
      </li>
    </ul>
  </aside>
</template>

<script>
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

export default {
  name: 'NV_Sidebar',
  setup() {
    const router = useRouter();
    const authStore = useAuthStore();

    const getRoleBadgeClass = () => {
      if (authStore.isAdmin) return 'badge-admin';
      if (authStore.isEmployee) return 'badge-employee';
      if (authStore.isCustomer) return 'badge-customer';
      return 'badge-employee';
    };

    const getRoleIcon = () => {
      if (authStore.isAdmin) return 'bi bi-star-fill me-1';
      if (authStore.isEmployee) return 'bi bi-person-gear me-1';
      return 'bi bi-person me-1';
    };

    const getRoleDisplayText = () => {
      if (authStore.userRole === 'ADMIN' || authStore.isAdmin) {
        return 'Admin';
      } else if (authStore.userRole === 'EMPLOYEE' || authStore.isEmployee) {
        return 'Nhân viên';
      } else if (authStore.userRole === 'CUSTOMER' || authStore.isCustomer) {
        return 'Khách hàng';
      }
      return authStore.userRole || 'Nhân viên';
    };


    const getLastName = (fullName) => {
      if (!fullName) return '';
      const nameParts = fullName.trim().split(' ');
      return nameParts.length > 0 ? nameParts[nameParts.length - 1] : fullName;
    };

    const logout = async () => {
      await authStore.logout();
      localStorage.removeItem('user');
      localStorage.removeItem('auth_token');
      router.push('/auth/login');
    };

    return {
      authStore,
      getRoleBadgeClass,
      getRoleIcon,
      getRoleDisplayText,
      getLastName,
      logout
    };
  }
};
</script>

<style scoped>
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  width: 260px;
  background: #000000;
  color: white;
  z-index: 1000;
  transition: transform 0.3s ease;
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header h3 {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
  color: white;
}

.sidebar-header small {
  color: rgba(255, 255, 255, 0.7);
}

.sidebar-header .user-info {
  margin-top: 15px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.sidebar-header .user-greeting {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  color: white;
}

.sidebar-header .user-role-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 5px;
  color: white;
}

.badge-admin {
  background: linear-gradient(135deg, rgb(255, 255, 0), #ee5a24);
}

.badge-employee {
  background: linear-gradient(135deg, #48cae4, #0096c7);
}

.badge-customer {
  background: linear-gradient(135deg, #10b981, #059669);
}

.sidebar-header .user-email {
  font-size: 11px;
  opacity: 0.8;
  margin-top: 5px;
  word-break: break-all;
  color: rgba(255, 255, 255, 0.8);
}

.sidebar-menu {
  list-style: none;
  padding: 0;
  margin: 20px 0;
}

.sidebar-menu li {
  margin: 0;
}

.sidebar-menu a {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  color: white;
  text-decoration: none;
  transition: all 0.3s;
}

.sidebar-menu a:hover,
.sidebar-menu a.active {
  background: rgba(255, 255, 255, 0.172);
  border-left: 3px solid white;
}

.sidebar-menu i {
  font-size: 20px;
  margin-right: 15px;
  width: 25px;
}
</style>