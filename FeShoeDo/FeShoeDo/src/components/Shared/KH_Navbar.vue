<template>
  <nav class="navbar navbar-expand-lg bg-black py-3 sticky-top" data-bs-theme="dark">
    <div class="container-fluid px-4 px-lg-5 d-flex align-items-center justify-content-between">
      
      <!-- Logo -->
      <router-link to="/customer/index" class="navbar-brand logo-box d-flex align-items-center justify-content-center">
        <img :src="logoUrl" alt="Shoedo" class="logo-img">
      </router-link>

      <!-- Menu chính -->
      <div class="collapse navbar-collapse flex-grow-0 mx-auto d-none d-lg-block" id="navbarNav">
        <ul class="navbar-nav gap-5"> 
          <li class="nav-item">
            <router-link class="nav-link text-uppercase text-white fw-light" to="/customer/index">Trang Chủ</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link text-uppercase text-white fw-light" to="/customer/sanpham">Sản Phẩm</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link text-uppercase text-white fw-light" to="/customer/chinhsach">Chính Sách</router-link>
          </li>
        </ul>
      </div>

      <div class="d-flex align-items-center gap-3 right-actions">
        
        <!-- Search -->
        <div class="search-wrapper d-none d-md-block position-relative">
          <div class="input-group">
            <input 
              type="text" 
              class="form-control bg-black border-0 shadow-none text-white ps-3" 
              placeholder="Tìm Kiếm" 
              aria-label="Search"
              @focus="isSearchFocused = true"
              @blur="hideSearchHistory"
            >
            <button class="btn btn-outline-light border-0 bg-black pe-3" type="button">
              <i class="bi bi-search text-white"></i>
            </button>
          </div>

          <div v-if="isSearchFocused && searchHistory.length > 0" class="search-history-dropdown">
            <div class="d-flex justify-content-between align-items-center px-3 py-2 border-bottom border-dark">
              <span class="text-secondary" style="font-size: 0.8rem; font-weight: 600;">Lịch sử tìm kiếm</span>
              <span class="text-secondary clear-history" style="font-size: 0.8rem;">Xóa</span>
            </div>
            <ul class="list-unstyled mb-0 py-1">
              <li v-for="(item, index) in searchHistory" :key="index" class="history-item px-3 py-2 text-white">
                <i class="bi bi-clock-history me-2 text-secondary"></i>
                {{ item }}
              </li>
            </ul>
          </div>
        </div>

        <!-- Giỏ hàng -->
        <router-link to="/customer/cart" class="btn btn-icon position-relative text-white">
          <i class="bi bi-cart3 fs-5"></i>
          <span v-if="isAuthenticated && cartCount > 0" class="position-absolute badge rounded-pill bg-danger cart-badge">
            {{ cartCount }}
          </span>
        </router-link>

        <!-- Account Dropdown -->
        <div class="dropdown" ref="dropdown">
          <button
            class="user-box d-flex align-items-center gap-2 px-3 py-2 rounded-0 cursor-pointer text-white bg-transparent"
            @click.prevent="toggleDropdown"
            type="button"
          >
            <i class="bi bi-person fs-5"></i>
            <span class="fw-light">Tài Khoản</span>
            <i class="bi bi-chevron-down" style="font-size: 0.7rem;"></i>
          </button>

          <ul
            class="dropdown-menu dropdown-menu-dark dropdown-menu-end shadow mt-2"
            :class="{ show: showDropdown }"
            v-show="showDropdown"
          >
            <template v-if="!isAuthenticated">
              <li>
                <router-link class="dropdown-item d-flex align-items-center gap-2" to="/auth/login">
                  <i class="bi bi-box-arrow-in-right"></i> Đăng nhập
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item d-flex align-items-center gap-2" to="/auth/login#register">
                  <i class="bi bi-person-plus"></i> Đăng ký
                </router-link>
              </li>
            </template>

            <template v-else>
              <li>
                <span class="dropdown-item-text">
                  Xin chào,
                  <span class="fw-bold" id="userLastName">{{ lastName }}</span>
                </span>
              </li>
              <li><hr class="dropdown-divider border-secondary"></li>
              <li>
                <router-link class="dropdown-item d-flex align-items-center gap-2" to="/customer/profile">
                  <i class="bi bi-person-gear"></i> Quản lý tài khoản
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item d-flex align-items-center gap-2" to="/customer/orders">
                  <i class="bi bi-box-seam"></i> Đơn hàng của tôi
                </router-link>
              </li>
              <li><hr class="dropdown-divider border-secondary"></li>
              <li>
                <a class="dropdown-item d-flex align-items-center gap-2 text-danger" href="#" @click.prevent="logout">
                  <i class="bi bi-box-arrow-right"></i> Đăng Xuất
                </a>
              </li>
            </template>
          </ul>
        </div>

        <button class="navbar-toggler border-white" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
          <span class="navbar-toggler-icon"></span>
        </button>
      </div>

    </div>
  </nav>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import axios from 'axios'
import logoUrl from '@/assets/Logoc.png'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isSearchFocused = ref(false)
const cartCount = ref(0)
const showDropdown = ref(false)
const dropdown = ref(null)

const isAuthenticated = computed(() => authStore.isAuthenticated)
const lastName = computed(() => {
  const name = authStore.user?.name || ''
  if (!name || name.trim() === '') return 'User'
  const names = name.trim().split(/\s+/)
  return names[names.length - 1]
})

const fetchCartCount = async () => {
  if (!isAuthenticated.value) {
    cartCount.value = 0
    return
  }
  
  try {
    const response = await axios.get('/api/auth/cart-count', {
      withCredentials: true
    })
    if (response.data.success) {
      cartCount.value = response.data.count || 0
    }
  } catch (error) {
    console.error('Lỗi lấy số lượng giỏ hàng:', error)
    cartCount.value = 0
  }
}


const searchHistory = ref([
  'Giày chạy bộ nam',
  'Sneaker trắng',
  'Giày thể thao Nike',
  'Adidas Ultraboost'
])

const hideSearchHistory = () => {
  setTimeout(() => {
    isSearchFocused.value = false
  }, 200)
}

const toggleDropdown = async (e) => {
  e.preventDefault()
  showDropdown.value = !showDropdown.value
}

const logout = async () => {
  try {
    await authStore.logout()
    cartCount.value = 0
    showDropdown.value = false
    router.push('/auth/login')
  } catch (error) {
    console.error('Logout error:', error)
  }
}
const handleClickOutside = (event) => {
  if (dropdown.value && !dropdown.value.contains(event.target)) {
    showDropdown.value = false
  }
}

watch(isAuthenticated, (newVal) => {
  if (newVal) {
    fetchCartCount()
  } else {
    cartCount.value = 0
  }
})

onMounted(() => {
  if (isAuthenticated.value) {
    fetchCartCount()
  }
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.bg-black {
  background-color: #000000 !important;
}

.logo-box {
  width: 120px;  
  height: 48px; 
  transition: all 0.3s ease;
  padding: 5px; 
}

.logo-img {
  width: 100%;
  height: 100%;
  object-fit: contain; 
  transform: scale(1.5); 
  transition: transform 0.3s ease;
}

.logo-box:hover {
  background-color: #fff;
  border-color: #fff;
}

.nav-link {
  font-size: 0.9rem;
  letter-spacing: 1.5px; 
  position: relative;
  opacity: 0.9;
  transition: opacity 0.3s;
  font-weight: 700 !important;
}

.nav-link:hover {
  opacity: 1;
}

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

.search-wrapper {
  width: 320px;
  z-index: 100;
}

.search-wrapper .input-group {
  border: 1px solid #ffffff; 
  border-radius: 8px; 
  overflow: hidden; 
  height: 44px; 
  background-color: #000;
  transition: border-radius 0.2s;
}

.search-wrapper:focus-within .input-group {
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
}

input::placeholder {
  color: #666 !important;
  font-weight: 700 !important;
  font-size: 0.9rem;
}

.search-wrapper .input-group > input.form-control,
.search-wrapper .input-group > .btn {
  height: 100%;
  border-radius: 0;
}

input:focus {
  outline: none;
  box-shadow: none;
}

/* --- CSS CHO POPUP LỊCH SỬ TÌM KIẾM --- */
.search-history-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background-color: #000000;
  border: 1px solid #ffffff;
  border-top: none; 
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
  z-index: 1050;
  box-shadow: 0 8px 16px rgba(0,0,0,0.5);
}

.history-item {
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.history-item:hover {
  background-color: #222222; 
}

.clear-history {
  cursor: pointer;
  transition: color 0.2s;
}

.clear-history:hover {
  color: #ffffff !important;
  text-decoration: underline;
}


/* 4. Cart Icon Box */
.btn-icon {
  padding: 0 14px;
  border: 1px solid #ffffff;
  border-radius: 10px; 
  transition: transform 0.18s ease, box-shadow 0.18s ease, background-color 0.18s ease;
  height: 48px; 
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

.btn-icon:active,
.btn-icon.is-pressed {
  transform: translateY(-3px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.55), 0 4px 8px rgba(0,0,0,0.4);
}

.btn-icon:focus {
  outline: none;
  box-shadow: 0 8px 18px rgba(0,0,0,0.45);
}

/* 5. User Box (Dropdown Toggle) */
.user-box {
  border: 1px solid #ffffff;
  border-radius: 8px !important; 
  transition: all 0.3s;
  height: 44px; 
  align-items: center;
}

.user-box:hover, .user-box[aria-expanded="true"] {
  background-color: #fff !important;
  color: #000 !important;
}

.user-box span {
  font-weight: 700 !important;
}

.dropdown-menu-dark {
  background-color: #000000;
  border-radius: 8px;
}

/* 6. User Dropdown Menu */
.dropdown-menu-dark {
  background-color: #000000;
  border-radius: 8px;
}

.dropdown-menu-dark .dropdown-item {
  color: #ffffff;
  font-size: 0.95rem;
  padding: 10px 20px;
  transition: background-color 0.2s, color 0.2s;
}

.dropdown-menu-dark .dropdown-item:hover {
  background-color: #222222;
}

.dropdown-menu-dark .dropdown-item.text-danger:hover {
  background-color: #2b0000;
  color: #ff6b6b !important;
}

/* 7. Cart Badge */
.cart-badge {
  top: -6px;            
  right: -6px;          
  font-size: 0.7rem;    
  padding: 0.35em 0.6em;
  border: 2px solid #000000; 
  font-weight: bold;
  transition: border-color 0.18s ease;
}

.btn-icon:hover .cart-badge {
  border-color: #ffffff;
}

@media (max-width: 991px) {
  .search-wrapper {
    display: none;
  }
}
</style>