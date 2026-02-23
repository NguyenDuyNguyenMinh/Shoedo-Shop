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
}
</style>