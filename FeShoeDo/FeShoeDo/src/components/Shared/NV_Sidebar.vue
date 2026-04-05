<template>
  <aside class="sidebar" id="sidebar" :class="{ 'collapsed': isCollapsed }">
    
    <button class="toggle-btn" @click="toggleSidebar">
      <i class="bi" :class="isCollapsed ? 'bi-chevron-right' : 'bi-chevron-left'"></i>
    </button>

    <div class="sidebar-inner" :class="{ 'fade-out': isAnimating }">
      <div class="sidebar-wrapper">
        
        <div class="sidebar-header">
          <div v-show="!isCollapsed">
            <div class="brand-text">
              <h3>ShoeDo Shop</h3>
              <small>Quản lý cửa hàng</small>
            </div>
            
            <div class="user-info" v-if="authStore.user">
              <div class="user-greeting">
                <i class="fa-solid fa-user me-2"></i>
                <span>Xin chào, 
                  <span class="fw-bold" id="userLastName">
                    {{ getLastName(authStore.userName) }}
                  </span>
                </span>
              </div>
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
        </div>
        
        <div class="sidebar-menu-wrapper">
          <div class="menu-category" v-show="!isCollapsed">
            <span>Vận Hành</span>
          </div>
          <ul class="sidebar-menu">
            <li>
              <router-link to="/employee/dashboard" :class="{ active: $route.path === '/employee/dashboard' }" :title="isCollapsed ? 'Thống Kê' : ''">
                <i class="bi bi-speedometer2"></i>
                <span v-show="!isCollapsed">Thống Kê</span>
              </router-link>
            </li>
            <li>
              <router-link to="/employee/flashsale" :class="{ active: $route.path === '/employee/flashsale' }" :title="isCollapsed ? 'Khuyến Mãi' : ''">
                <i class="fa-solid fa-tag"></i>
                <span v-show="!isCollapsed">Khuyến Mãi</span>
              </router-link>
            </li>
          </ul>

          <div class="menu-category mt-3" v-show="!isCollapsed">
            <span>Quản Lý</span>
          </div>
          <hr class="sidebar-divider" v-show="isCollapsed" />

          <ul class="sidebar-menu">
            <li>
              <router-link to="/employee/products" :class="{ active: $route.path === '/employee/products' }" :title="isCollapsed ? 'Sản Phẩm' : ''">
                <i class="bi bi-box-seam"></i>
                <span v-show="!isCollapsed">Sản Phẩm</span>
              </router-link>
            </li>
            <li>
              <router-link to="/employee/users" :class="{ active: $route.path === '/employee/users' }" :title="isCollapsed ? 'Tài Khoản' : ''">
                <i class="fa-regular fa-circle-user"></i>
                <span v-show="!isCollapsed">Tài Khoản</span>
              </router-link>
            </li>
            <li>
              <router-link to="/employee/orders" :class="{ active: $route.path === '/employee/orders' }" :title="isCollapsed ? 'Đơn Hàng' : ''">
                <i class="bi bi-cart-check"></i>
                <span v-show="!isCollapsed">Đơn Hàng</span>
              </router-link>
            </li>
            <li>
              <router-link to="/employee/danhgia" :class="{ active: $route.path === '/employee/danhgia' }" :title="isCollapsed ? 'Đánh Giá' : ''">
                <i class="fa-regular fa-comments"></i>
                <span v-show="!isCollapsed">Đánh Giá</span>
              </router-link>
            </li>
            <li>
              <router-link to="/employee/import" :class="{ active: $route.path === '/employee/import' }" :title="isCollapsed ? 'Nhập Kho' : ''">
                <i class="fa-solid fa-inbox"></i>
                <span v-show="!isCollapsed">Nhập Kho</span>
              </router-link>
            </li>
            <li>
              <router-link to="/employee/voucher" :class="{ active: $route.path === '/employee/voucher' }" :title="isCollapsed ? 'Voucher' : ''">
                <i class="bi bi-ticket-perforated"></i>
                <span v-show="!isCollapsed">Voucher</span>
              </router-link>
</li>
          </ul>
        </div>
      </div>
      
      <div class="sidebar-footer">
        <a href="#" @click.prevent="logout" class="logout-btn" :title="isCollapsed ? 'Đăng xuất' : ''">
          <i class="bi bi-box-arrow-right"></i>
          <span v-show="!isCollapsed">Đăng xuất</span>
        </a>
      </div>
    </div>
  </aside>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

export default {
  name: 'NV_Sidebar',
  emits: ['toggle-collapse'],
  setup(props, { emit }) {
    const router = useRouter();
    const authStore = useAuthStore();
    
    const isCollapsed = ref(localStorage.getItem('sidebar_collapsed') === 'true');
    // Biến kiểm soát độ mờ
    const isAnimating = ref(false);

    onMounted(() => {
      emit('toggle-collapse', isCollapsed.value);
    });

    const toggleSidebar = () => {
      // Nếu đang trong quá trình chạy hiệu ứng thì chặn click liên tục
      if (isAnimating.value) return; 
      
      // BƯỚC 1: Làm mờ toàn bộ nội dung (fade out)
      isAnimating.value = true;

      setTimeout(() => {
        // BƯỚC 2: Khi đã mờ hẳn, lập tức thay đổi độ rộng sidebar và sắp xếp lại icon
        isCollapsed.value = !isCollapsed.value;
        localStorage.setItem('sidebar_collapsed', isCollapsed.value);
        emit('toggle-collapse', isCollapsed.value);

        // BƯỚC 3: Đợi độ rộng sidebar trượt xong (300ms) thì mới làm rõ nội dung trở lại (fade in)
        setTimeout(() => {
          isAnimating.value = false;
        }, 300); 

      }, 150); // Chờ 150ms cho fade out chạy xong
    };

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
      if (authStore.userRole === 'ADMIN' || authStore.isAdmin) return 'Admin';
      if (authStore.userRole === 'EMPLOYEE' || authStore.isEmployee) return 'Nhân viên';
      if (authStore.userRole === 'CUSTOMER' || authStore.isCustomer) return 'Khách hàng';
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
      localStorage.removeItem('sidebar_collapsed');
      router.push('/auth/login');
    };

    return {
      authStore,
      isCollapsed,
      isAnimating,
      toggleSidebar,
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
/* TRẠNG THÁI MỞ RỘNG (MẶC ĐỊNH) */
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  width: 260px;
  background: #000000;
  color: white;
  z-index: 1000;
  transition: width 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.sidebar.collapsed {
  width: 80px;
}


/* NỘI DUNG BÊN TRONG (FADE & SLIDE UP) */
.sidebar-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  opacity: 1;
  transform: translateY(0); /* Vị trí gốc */
  transition: opacity 0.15s ease-out, transform 0.15s ease-out; /* Thêm transform vào transition */
}

.sidebar-inner.fade-out {
  opacity: 0;
  transform: translateY(15px); /* Tụt xuống 15px khi ẩn, để lúc hiện ra sẽ có đà trượt lên */
}

/* TÙY CHỈNH NÚT MŨI TÊN (TOGGLE BUTTON) */
.toggle-btn {
  position: absolute;
  top: 15px;
  right: -15px;
  width: 30px;
  height: 30px;
  background: #333;
  color: white;
  border: 2px solid #000;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 1001;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.toggle-btn:hover {
  background: #555;
}

.sidebar.collapsed .toggle-btn {
  right: 25px; /* Khi thu nhỏ, nút chạy vào giữa 80px */
  background: transparent;
  border-color: transparent;
  font-size: 20px;
}

.sidebar.collapsed .toggle-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.sidebar-wrapper {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  min-height: 80px;
}

.sidebar.collapsed .sidebar-header {
  min-height: 60px;
  border-bottom: none;
  padding: 10px;
}

.brand-text h3 { font-size: 24px; font-weight: bold; margin: 0; color: white; }
.sidebar-header small { color: rgba(255, 255, 255, 0.7); font-size: 12px; }
.user-info { margin-top: 15px; padding: 15px; background: rgba(255, 255, 255, 0.1); border-radius: 12px; border: 1px solid rgba(255, 255, 255, 0.2); }
.user-greeting { font-weight: 600; font-size: 16px; margin-bottom: 8px; display: flex; align-items: center; justify-content: center; color: white; }
.user-role-badge { display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 5px; color: white; }
.badge-admin { background: linear-gradient(135deg, #ffd700, #ee5a24); }
.badge-employee { background: linear-gradient(135deg, #48cae4, #0096c7); }
.badge-customer { background: linear-gradient(135deg, #10b981, #059669); }
.user-email { font-size: 11px; opacity: 0.8; margin-top: 5px; word-break: break-all; color: rgba(255, 255, 255, 0.8); }

.menu-category {
  padding: 10px 20px 5px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.4);
  letter-spacing: 1px;
  white-space: nowrap;
}

.sidebar-divider {
  border-color: rgba(255, 255, 255, 0.2);
  margin: 10px 15px;
  border-style: solid;
  border-width: 1px 0 0 0;
  opacity: 1;
}

.sidebar-menu { list-style: none; padding: 0; margin: 20px 0; }
.sidebar-menu li { margin: 0; }

.sidebar-menu a {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  white-space: nowrap;
  /* Chỉ transition hiệu ứng hover để không bị giật layout khi co giãn */
  transition: background-color 0.3s, color 0.3s, border-left 0.3s; 
}

.sidebar.collapsed .sidebar-menu a {
  justify-content: center;
  padding: 12px 0;
}

.sidebar-menu a:hover,
.sidebar-menu a.active {
  background: rgba(255, 255, 255, 0.1);
  border-left: 3px solid white;
  color: white;
}

.sidebar-menu i {
  font-size: 20px;
  margin-right: 15px;
  width: 25px;
  text-align: center;
}

.sidebar.collapsed .sidebar-menu i {
  margin-right: 0;
}

.sidebar-footer { border-top: 1px solid rgba(255, 255, 255, 0.1); padding: 10px 0; flex-shrink: 0; }

.logout-btn {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: #ff6b6b;
  text-decoration: none;
  white-space: nowrap;
  transition: background-color 0.3s, color 0.3s;
}

.sidebar.collapsed .logout-btn { justify-content: center; padding: 12px 0; }
.logout-btn:hover { background: rgba(255, 107, 107, 0.1); color: #ff8787; }
.logout-btn i { font-size: 20px; margin-right: 15px; width: 25px; text-align: center; }
.sidebar.collapsed .logout-btn i { margin-right: 0; }

/* SCROLLBAR */
.sidebar-wrapper::-webkit-scrollbar { width: 5px; }
.sidebar-wrapper::-webkit-scrollbar-track { background: transparent; }
.sidebar-wrapper::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.2); border-radius: 10px; }
.sidebar-wrapper::-webkit-scrollbar-thumb:hover { background: rgba(255, 255, 255, 0.4); }
.sidebar-wrapper { scrollbar-width: thin; scrollbar-color: rgba(255, 255, 255, 0.2) transparent; }
</style>