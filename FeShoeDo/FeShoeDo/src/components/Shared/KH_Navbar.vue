<<<<<<< HEAD
<template>
  <!-- 🔹 Navbar -->
  <div class="main-header">
    <div class="container">
      <div class="d-flex align-items-center justify-content-between">
        <!-- Logo -->
        <router-link to="/customer/index" class="logo d-flex align-items-center me-3">
          <img :src="getImageUrl('anh/logo blackwhite.jpg')" alt="BlackWhite" style="max-width: 200px;">
        </router-link>

        <!-- Search box with category dropdown -->
        <div class="flex-grow-1 mx-3">
          <form @submit.prevent="handleSearch" class="search-container">
            <select v-model="selectedCategoryId" class="category-select" id="categorySelect">
              <option value="">Tất cả danh mục</option>
              <option v-for="dm in danhMucs" :key="dm.maDM" :value="dm.maDM">
                {{ dm.tenDM }}
              </option>
            </select>
            <div class="search-box d-flex flex-grow-1">
              <input type="text" class="form-control" 
                     v-model="searchKeyword"
                     placeholder="Tìm kiếm sản phẩm..."
                     @keyup.enter="handleSearch">
              <button class="btn" type="button" @click="handleSearch"><i class="bi bi-search"></i></button>
            </div>
          </form>
        </div>

        <!-- Cart & Account -->
        <div class="d-flex align-items-center gap-3">
          <router-link to="/customer/cart" class="cart-icon position-relative" style="text-decoration:none;">
            <i class="bi bi-cart3 fs-4"></i>
            <span class="cart-badge">{{ cartItemCount }}</span>
          </router-link>
          
          <!-- Account Dropdown -->
          <div class="dropdown" ref="dropdown">
            <a class="btn d-flex align-items-center px-3 py-2 account-btn rounded-pill shadow-sm fw-bold"
               href="#" id="accountDropdown" 
               @click.prevent="toggleDropdown"
               style="font-size:16px;">
              <span class="me-2 d-flex align-items-center justify-content-center"
                    style="width:32px; height:32px; background:#000000; border-radius:50%;">
                <i class="bi bi-person-fill" style="color:#ffffff; font-size:22px;"></i>
              </span>
              {{ dropdownTitle }}
              <i class="bi bi-caret-down-fill ms-2" style="font-size:14px;"></i>
            </a>
            <ul class="dropdown-menu dropdown-menu-end" :class="{ show: showDropdown }" v-show="showDropdown">
              <li v-if="!isAuthenticated">
                <router-link class="dropdown-item" to="/auth/login">Đăng nhập</router-link>
              </li>
              <li v-if="!isAuthenticated">
                <router-link class="dropdown-item" to="/auth/login">Đăng ký</router-link>
              </li>
              <li v-if="isAuthenticated">
                <span class="dropdown-item-text">
                  Xin chào, 
                  <span class="fw-bold" id="userLastName">{{ lastName }}</span>
                </span>
              </li>
              <li v-if="isAuthenticated"><hr class="dropdown-divider"></li>
              <li v-if="isAuthenticated">
                <router-link class="dropdown-item" to="/customer/profile">Quản Lý Tài Khoản</router-link>
              </li>
              <li v-if="isAuthenticated">
                <router-link class="dropdown-item" to="/customer/orders">Đơn Hàng Của Bạn</router-link>
              </li>
              <li v-if="isAuthenticated"><hr class="dropdown-divider"></li>
              <li v-if="isAuthenticated">
                <a class="dropdown-item text-danger" href="/auth/logout" @click.prevent="logout">Đăng Xuất</a>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useCartStore } from '@/stores/cart';

export default {
  name: 'KH_Navbar',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const authStore = useAuthStore();
    const cartStore = useCartStore();
    const dropdown = ref(null);
    
    const searchKeyword = ref('');
    const selectedCategoryId = ref('');
    const showDropdown = ref(false);
    const danhMucs = ref([]);
    const sessionChecked = ref(false);
    const sessionValid = ref(false);

    const checkSessionWithServer = async () => {
      try {
        const userInStorage = localStorage.getItem('user');
        if (!userInStorage) {
          sessionValid.value = false;
          sessionChecked.value = true;
          return;
        }
        
        const response = await fetch('/api/auth/check-session', {
          method: 'GET',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json',
          }
        });
        
        if (response.ok) {
          const data = await response.json();
          
          if (data.success && data.user) {
            authStore.user = data.user;
            localStorage.setItem('user', JSON.stringify(data.user));
            sessionValid.value = true;
          } else {
            console.log('❌ Session invalid, clearing localStorage');
            authStore.clearAuth();
            localStorage.removeItem('user');
            localStorage.removeItem('auth_token');
            sessionValid.value = false;
          }
        } else {
          console.log('❌ Server error, clearing session');
          authStore.clearAuth();
          localStorage.removeItem('user');
          localStorage.removeItem('auth_token');
          sessionValid.value = false;
        }
      } catch (error) {
        console.log('🌐 Network error, assuming session invalid:', error);
        authStore.clearAuth();
        localStorage.removeItem('user');
        localStorage.removeItem('auth_token');
        sessionValid.value = false;
      } finally {
        sessionChecked.value = true;
      }
    };

    const isAuthenticated = computed(() => {
      if (!sessionChecked.value) return false;
      return sessionValid.value && authStore.isAuthenticated;
    });

    const userName = computed(() => {
      if (!isAuthenticated.value) return '';
      return authStore.user?.name || authStore.user?.fullname || 'User';
    });

    const cartItemCount = computed(() => cartStore.cartItemCount);

    const lastName = computed(() => {
      if (!userName.value || userName.value.trim() === '') {
        return 'User';
      }
      const names = userName.value.trim().split(/\s+/);
      return names[names.length - 1];
    });

    const dropdownTitle = computed(() => {
      return isAuthenticated.value ? 'Tài Khoản' : 'Tài Khoản';
    });

    const getImageUrl = (imagePath) => {
      return `http://localhost:8080/${imagePath}`;
    };

    const fetchCategories = async () => {
      try {
        const response = await fetch('/api/categories');
        if (response.ok) {
          const data = await response.json();
          if (data.success) {
            danhMucs.value = data.data || [];
          }
        }
      } catch (error) {
        console.error('Error fetching categories:', error);
      }
    };

    const handleSearch = () => {
      if (!searchKeyword.value.trim() && !selectedCategoryId.value) {
        router.push({ path: '/customer/index' });
        return;
      }
      
      const query = {};
      if (searchKeyword.value.trim()) {
        query.keyword = searchKeyword.value.trim();
      }
      if (selectedCategoryId.value) {
        query.categoryId = selectedCategoryId.value;
      }
      
      router.push({ 
        path: '/customer/index', 
        query: query 
      });
    };

    const toggleDropdown = async () => {
      if (!sessionChecked.value) {
        await checkSessionWithServer();
      }
      showDropdown.value = !showDropdown.value;
    };

    const logout = async () => {
      try {
        await authStore.logout();
        localStorage.clear();
        sessionStorage.clear();
        localStorage.removeItem('user');
        localStorage.removeItem('auth_token');
        window.location.href = '/auth/login';
      } catch (error) {
        console.error('Logout error:', error);
      }
    };

    const handleClickOutside = (event) => {
      if (!dropdown.value) return;
      if (dropdown.value.contains(event.target)) return;
      showDropdown.value = false;
    };

    const resetSearchForm = () => {
      searchKeyword.value = '';
      selectedCategoryId.value = '';
    };

    onMounted(async () => {
      await checkSessionWithServer();
      
      fetchCategories();
      resetSearchForm();
      document.addEventListener('click', handleClickOutside);
      if (sessionValid.value) {
        cartStore.fetchCartCount();
      }

      window.addEventListener('online', handleOnlineStatus);
      window.addEventListener('offline', handleOfflineStatus);
    });

    const handleOnlineStatus = async () => {
      console.log('🌐 Network is back online, rechecking session');
      await checkSessionWithServer();
    };

    const handleOfflineStatus = () => {
      console.log('🌐 Network is offline');
    };

    onBeforeUnmount(() => {
      document.removeEventListener('click', handleClickOutside);
      window.removeEventListener('online', handleOnlineStatus);
      window.removeEventListener('offline', handleOfflineStatus);
    });

    watch(() => route.path, async () => {
      if (!sessionChecked.value) {
        await checkSessionWithServer();
      }
    });

    return {
      searchKeyword,
      selectedCategoryId,
      showDropdown,
      danhMucs,
      isAuthenticated,
      userName,
      lastName,
      cartItemCount,
      dropdownTitle,
      dropdown,
      getImageUrl,
      handleSearch,
      toggleDropdown,
      logout
    };
  }
};
</script>

<style scoped>
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #ffffff;
  color: #000000;
}

/* Header */
.main-header {
  background: #ffffff;
  padding: 20px 0;
  border-bottom: 2px solid #000000;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.logo img {
  max-width: 200px;
}

/* Search box */
.search-container {
  display: flex;
  width: 100%;
}

.category-select {
  border: 2px solid #000000;
  border-right: none;
  border-radius: 8px 0 0 8px;
  padding: 10px 15px;
  background: #ffffff;
  color: #000000;
  font-weight: 500;
  min-width: 150px;
}

.search-box .form-control {
  border: 2px solid #000000;
  border-left: none;
  border-right: none;
  padding: 10px 15px;
  border-radius: 0;
  background: #ffffff;
  color: #000000;
}

.search-box .btn {
  background: #000000;
  border: 2px solid #000000;
  color: white;
  padding: 10px 20px;
  border-radius: 0 8px 8px 0;
  font-weight: 500;
}

.search-box .btn:hover {
  background: #333333;
  border-color: #333333;
}

/* Cart */
.cart-icon {
  color: #000000;
  font-size: 28px;
  position: relative;
  cursor: pointer;
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #000000;
  color: #ffffff;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: bold;
}

/* Dropdown */
.dropdown-menu {
  border: 2px solid #000000;
  border-radius: 8px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.dropdown-item {
  padding: 10px 15px;
  color: #000000;
  cursor: pointer;
}

.dropdown-item:hover {
  background-color: #f0f0f0;
  color: #000000;
}

.dropdown-divider {
  border-top: 1px solid #000000;
}

/* Account button */
.account-btn {
  border: 2px solid #000000;
  background: #ffffff;
  color: #000000;
  font-weight: 500;
}

.account-btn:hover {
  background: #000000;
  color: #ffffff;
=======
<script setup>
// Không cần script phức tạp, chỉ thuần template và css
</script>

<template>
  <nav class="navbar navbar-expand-lg bg-black py-3 sticky-top" data-bs-theme="dark">
    <div class="container-fluid px-4 px-lg-5 d-flex align-items-center justify-content-between">
      
      <a class="navbar-brand logo-box d-flex align-items-center justify-content-center text-white" href="#">
        Logo
      </a>

      <div class="collapse navbar-collapse flex-grow-0 mx-auto d-none d-lg-block" id="navbarNav">
        <ul class="navbar-nav gap-5"> <li class="nav-item">
            <a class="nav-link text-uppercase text-white fw-light" href="#">Trang Chủ</a>
          </li>
          <li class="nav-item">
            <a class="nav-link text-uppercase text-white fw-light" href="#">Sản Phẩm</a>
          </li>
          <li class="nav-item">
            <a class="nav-link text-uppercase text-white fw-light" href="#">Chính Sách</a>
          </li>
        </ul>
      </div>

      <div class="d-flex align-items-center gap-3 right-actions">
        
        <div class="search-wrapper d-none d-md-block">
          <div class="input-group">
            <span class="input-group-text bg-black border-0 ps-3">
              <span class="text-secondary fw-light" style="font-size: 0.9rem;">Tìm Kiếm</span>
            </span>
            <input type="text" class="form-control bg-black border-0 shadow-none text-white" aria-label="Search">
            <button class="btn btn-outline-light border-0 bg-black" type="button">
              <i class="bi bi-search text-white"></i>
            </button>
          </div>
        </div>

        <a href="#" class="btn btn-icon position-relative text-white">
          <i class="bi bi-cart3 fs-5"></i>
        </a>

        <div class="user-box d-flex align-items-center gap-2 px-3 py-2 rounded-0 cursor-pointer text-white">
          <i class="bi bi-person fs-5"></i>
          <span class="fw-light">Minh</span>
          <i class="bi bi-chevron-down" style="font-size: 0.7rem;"></i>
        </div>

        <button class="navbar-toggler border-white" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
          <span class="navbar-toggler-icon"></span>
        </button>
      </div>

    </div>
  </nav>
</template>

<style scoped>
/* --- DARK LUXURY THEME CSS --- */

/* Nền đen tuyền */
.bg-black {
  background-color: #000000 !important;
}

/* 1. Logo Box: Viền trắng, chữ trắng */
.logo-box {
  width: 120px;
  height: 48px;
  border: 1px solid #ffffff; 
  font-weight: 500;
  letter-spacing: 2px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.logo-box:hover {
  background-color: #fff;
  color: #000 !important; /* Đảo màu khi hover */
}

/* 2. Navigation Links */
.nav-link {
  font-size: 0.9rem;
  letter-spacing: 1.5px; /* Giãn chữ tạo cảm giác cao cấp */
  position: relative;
  opacity: 0.9;
  transition: opacity 0.3s;
}

.nav-link:hover {
  opacity: 1;
}

/* Gạch chân trắng mảnh khi hover */
.nav-link::after {
  content: '';
  position: absolute;
  width: 0;
  height: 1px;
  bottom: 2px;
  left: 0;
  background-color: #fff;
  transition: width 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.nav-link:hover::after {
  width: 100%;
}

/* 3. Search Bar Container */
.search-wrapper {
  width: 320px;
}

.search-wrapper .input-group {
  border: 1px solid #ffffff; /* Viền bao quanh màu trắng */
  border-radius: 8px; /* Bo góc cho ô tìm kiếm */
  overflow: hidden; /* Giữ bo góc cho phần tử con */
  height: 44px; /* Chiều cao tiêu chuẩn cho tất cả controls */
}

/* Tùy chỉnh placeholder cho input */
input::placeholder {
  color: #666 !important;
}

/* 4. Cart Icon Box */
.btn-icon {
  padding: 0 14px;
  border: 1px solid #ffffff;
  border-radius: 10px; /* Bo góc cho nút cart */
  transition: transform 0.18s ease, box-shadow 0.18s ease, background-color 0.18s ease;
  height: 48px; /* Cao hơn một chút để có cảm giác nổi */
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 18px rgba(0,0,0,0.45), 0 2px 6px rgba(0,0,0,0.35);
  background: linear-gradient(180deg, rgba(255,255,255,0.02), rgba(0,0,0,0.06));
}

.btn-icon:hover {
  transform: none;
  background-color: #fff;
  color: #000 !important;
}

/* Press (lift) effect */
.btn-icon:active,
.btn-icon.is-pressed {
  transform: translateY(-3px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.55), 0 4px 8px rgba(0,0,0,0.4);
}

.btn-icon:focus {
  outline: none;
  box-shadow: 0 8px 18px rgba(0,0,0,0.45);
}

/* 5. User Box */
.user-box {
  border: 1px solid #ffffff;
  border-radius: 8px !important; /* Bo góc cho user box */
  transition: all 0.3s;
  height: 44px; /* Chiều cao bằng ô tìm kiếm */
  align-items: center;
}

.user-box:hover {
  background-color: #fff;
  color: #000 !important;
}

/* Make nav links and small labels bold */
.nav-link {
  font-weight: 700 !important;
}

.user-box span {
  font-weight: 700 !important;
}

.search-wrapper .input-group .input-group-text .text-secondary,
.search-wrapper .input-group .input-group-text {
  font-weight: 700 !important;
}

/* Ensure inner input and buttons fill the container height and don't round separately */
.search-wrapper .input-group > .input-group-text,
.search-wrapper .input-group > input.form-control,
.search-wrapper .input-group > .btn {
  height: 100%;
  border-radius: 0;
}

/* Responsive */
@media (max-width: 991px) {
  .search-wrapper {
    display: none;
  }
>>>>>>> MINH
}
</style>