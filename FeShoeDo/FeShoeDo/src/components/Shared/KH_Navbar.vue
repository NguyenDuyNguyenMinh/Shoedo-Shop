<template>
  <nav class="navbar navbar-expand-lg bg-black py-3 sticky-top" data-bs-theme="dark">
    <div class="container-fluid px-4 px-lg-5 d-flex align-items-center justify-content-between">
      
      <router-link to="/customer/index" class="navbar-brand logo-box d-flex align-items-center justify-content-center">
        <img :src="logoUrl" alt="Shoedo" class="logo-img">
      </router-link>

      <div class="collapse navbar-collapse flex-grow-0 mx-auto d-none d-lg-block" id="navbarNav">
        <ul class="navbar-nav gap-5"> 
          <li class="nav-item">
            <router-link class="nav-link text-uppercase text-white fw-light" to="/customer/index">Trang Chủ</router-link>
          </li>
          <li class="nav-item position-relative dropdown-hover">
            <router-link class="nav-link text-uppercase text-white fw-light py-2" :to="{ name: 'Sanpham', query: { section: 'moi-nhat' } }">Sản Phẩm</router-link>
            <div class="dropdown-menu-mega shadow border-0 mt-0">
               <div class="mega-grid">
                  <router-link v-for="cat in categories" :key="cat" class="mega-item" :to="{ name: 'Sanpham', query: { category: cat } }">
                    {{ cat }}
                  </router-link>
               </div>
            </div>
          </li>
          <li class="nav-item">
            <router-link class="nav-link text-uppercase text-white fw-light" to="/customer/chinhsach">Chính Sách</router-link>
          </li>
        </ul>
      </div>

      <div class="d-flex align-items-center gap-3 right-actions">
        
        <div class="search-wrapper d-none d-md-block position-relative" ref="searchWrapper">
          <div class="input-group" :class="{ 'dropdown-open': showDropdownSearch }">
            <input 
              ref="searchInputEl"
              type="text" 
              class="form-control bg-black border-0 shadow-none text-white ps-3" 
              placeholder="Tìm Kiếm"
              v-model="searchQuery"
              @focus="onSearchFocus"
              @keydown.enter.prevent="doSearch"
              @keydown.escape="closeSearchDropdown"
              @input="onSearchInput"
            >
            <button class="btn btn-outline-light border-0 bg-black pe-3" type="button" @click="doSearch">
              <i class="bi bi-search text-white"></i>
            </button>
          </div>

          <Transition name="dropdown-fade">
            <div v-if="showDropdownSearch" class="search-history-dropdown">
              <!-- Nội dung dropdown search giữ nguyên -->
              <template v-if="searchQuery.trim().length > 0">
                <div v-if="filteredHistory.length > 0">
                  <div class="sh-section-title px-3 py-2">
                    <i class="bi bi-search me-1"></i> Gợi ý tìm kiếm
                  </div>
                  <ul class="list-unstyled mb-0">
                    <li
                      v-for="item in filteredHistory"
                      :key="item.keyword"
                      class="sh-item px-3 py-2 text-white d-flex align-items-center gap-2"
                      @mousedown.prevent="selectKeyword(item.keyword)"
                    >
                      <i class="bi bi-search text-secondary" style="font-size:0.8rem;flex-shrink:0;"></i>
                      <span class="flex-grow-1">{{ item.keyword }}</span>
                    </li>
                  </ul>
                </div>
                <div v-else class="px-3 py-3 text-secondary text-center" style="font-size:0.85rem;">
                  Nhấn Enter để tìm "<strong class="text-white">{{ searchQuery }}</strong>"
                </div>
              </template>

              <template v-else>
                <template v-if="isAuthenticated">
                  <div v-if="historyLoading" class="px-3 py-3 text-secondary text-center" style="font-size:0.85rem;">
                    <i class="bi bi-hourglass-split me-1"></i> Đang tải...
                  </div>
                  <div v-else-if="historyList.length > 0">
                    <div class="d-flex justify-content-between align-items-center px-3 py-2 sh-header">
                      <span class="text-secondary" style="font-size:0.8rem;font-weight:600;">
                        <i class="bi bi-clock-history me-1"></i>Lịch sử tìm kiếm
                      </span>
                      <span class="sh-clear-btn" @mousedown.prevent="clearAllHistory">
                        Xóa tất cả
                      </span>
                    </div>
                    <ul class="list-unstyled mb-0">
                      <li
                        v-for="item in historyList"
                        :key="item.keyword"
                        class="sh-item px-3 py-2 text-white d-flex align-items-center gap-2"
                      >
                        <i class="bi bi-clock-history text-secondary" style="font-size:0.8rem;flex-shrink:0;"></i>
                        <span class="flex-grow-1" @mousedown.prevent="selectKeyword(item.keyword)">
                          {{ item.keyword }}
                        </span>
                        <i
                          class="bi bi-x sh-delete-icon"
                          @mousedown.prevent="deleteOneHistory(item.keyword)"
                          title="Xóa"
                        ></i>
                      </li>
                    </ul>
                  </div>
                  <div v-else class="px-3 py-3 text-secondary text-center" style="font-size:0.85rem;">
                    <i class="bi bi-search me-1"></i> Chưa có lịch sử tìm kiếm
                  </div>
                </template>
                <div v-else class="px-3 py-3 text-secondary text-center" style="font-size:0.85rem;">
                  <i class="bi bi-person me-1"></i>
                  <a href="#" class="text-white" @mousedown.prevent="openAuthModal('login')">Đăng nhập</a>
                  để lưu lịch sử
                </div>
              </template>
            </div>
          </Transition>
        </div>

        <!-- Cart button - show modal if not authenticated -->
        <a v-if="!isAuthenticated" href="#" class="btn btn-icon position-relative text-white" @click.prevent="openAuthModal('login')">
          <i class="bi bi-cart3 fs-5"></i>
        </a>
        <router-link v-else to="/customer/cart" class="btn btn-icon position-relative text-white">
          <i class="bi bi-cart3 fs-5"></i>
          <span v-if="cartCount > 0" class="position-absolute badge rounded-pill bg-danger cart-badge">
            {{ cartCount }}
          </span>
        </router-link>

        <!-- Khi chưa đăng nhập -->
        <template v-if="!isAuthenticated">
          <router-link to="/auth/login" class="btn-login">
            Đăng nhập
          </router-link>
          <router-link to="/auth/login?tab=register" class="btn-register">
            Đăng ký
          </router-link>
        </template>

        <!-- Khi đã đăng nhập -->
        <div v-else class="dropdown" ref="accountDropdown">
          <button
            class="user-box d-flex align-items-center gap-2 px-3 py-2 rounded-0 cursor-pointer text-white bg-transparent"
            @click.prevent="toggleAccountDropdown"
            type="button"
          >
            <i class="bi bi-person fs-5"></i>
            <span class="fw-light" v-once>Tài Khoản</span>
            <i class="bi bi-chevron-down" style="font-size: 0.7rem;"></i>
          </button>

          <ul
            class="dropdown-menu dropdown-menu-dark dropdown-menu-end shadow mt-2"
            :class="{ show: showAccountDropdown }"
            v-show="showAccountDropdown"
          >
            <li>
              <span class="dropdown-item-text">
                Xin chào, <span class="fw-bold">{{ lastName }}</span>
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
          </ul>
        </div>

        <button class="navbar-toggler border-white" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
          <span class="navbar-toggler-icon"></span>
        </button>
      </div>

    </div>
  </nav>

  <!-- ═══════════════════════════════════════════════════════════════ -->
  <!--  AUTH MODAL (LOGIN / REGISTER) - matching Login.vue style     -->
  <!-- ═══════════════════════════════════════════════════════════════ -->
  <Teleport to="body">
    <!-- Toast Component -->
    <Toast />

    <Transition name="modal-fade">
      <div v-if="showAuthModal" class="auth-modal-overlay">
        <div class="auth-modal-box fade-in-up" @click.stop>
          <!-- Close button -->
          <button class="btn-close-modal" @click="closeAuthModal">
            <i class="bi bi-x-lg"></i>
          </button>

          <!-- Tabs (giống nav-tabs trong Login.vue) -->
          <ul class="nav nav-tabs" role="tablist">
            <li class="nav-item">
              <button 
                class="nav-link tab-hover" 
                :class="{ active: authModalTab === 'login' }" 
                @click="authModalTab = 'login'"
                type="button"
              >Login</button>
            </li>
            <li class="nav-item">
              <button 
                class="nav-link tab-hover" 
                :class="{ active: authModalTab === 'register' }" 
                @click="authModalTab = 'register'"
                type="button"
              >Sign up</button>
            </li>
          </ul>

          <!-- LOGIN FORM -->
          <div v-if="authModalTab === 'login'" class="form-section">
            <form @submit.prevent="handleModalLogin">
              <div class="mt-3 mb-3 form-item">
                <label for="modalLoginIdentifier">Tài khoản</label>
                <input 
                  type="text" 
                  class="form-control input-hover"
                  id="modalLoginIdentifier"
                  v-model="modalLoginForm.identifier" 
                  placeholder="Nhập username hoặc email"
                  required
                >
                <small class="text-muted">Bạn có thể đăng nhập bằng username hoặc email</small>
              </div>
              <div class="mb-3 form-item">
                <label for="modalLoginPassword">Mật khẩu</label>
                <input 
                  :type="showModalLoginPass ? 'text' : 'password'" 
                  class="form-control input-hover"
                  id="modalLoginPassword"
                  v-model="modalLoginForm.pass"
                  required
                >
              </div>

              <div v-if="modalAccountLocked" class="alert alert-warning alert-dismissible fade show mt-2 mb-2 shake-alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                <span>{{ modalAccountLockedMessage }}</span>
              </div>

              <div class="d-flex justify-content-between align-items-center mb-3 form-item">
                <div class="form-check">
                  <input class="form-check-input checkbox-hover" type="checkbox" id="modalRememberMe" v-model="modalLoginForm.remember">
                  <label for="modalRememberMe">Ghi nhớ đăng nhập</label>
                </div>
                <a href="#" class="link-hover" @click.prevent="openModalForgotPassword">Quên mật khẩu?</a>
              </div>

              <div class="d-grid form-item">
                <button type="submit" class="btn btn-dark btn-hover" :disabled="modalLoading">
                  <span v-if="modalLoading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  {{ modalLoading ? 'Đang xử lý...' : 'Đăng nhập' }}
                </button>
              </div>
              <br>
              <div class="position-relative text-center mb-3 form-item">
                <hr class="position-absolute top-50 start-0 end-0 m-0">
                <span class="px-3 bg-white text-muted position-relative" style="z-index: 1;">
                  Hoặc
                </span>
              </div>
              <div class="form-item">
                <a href="#" 
                  class="btn btn-outline-dark d-flex align-items-center justify-content-center gap-2 mx-auto google-btn"
                  style="width: 210px; border-radius: 50px;"
                  @click.prevent="handleModalGoogleLogin">
                  <img :src="getImageUrl('anh/logo GG.png')" alt="Google Logo" style="width: 20px; height: 20px;" class="google-icon">
                  <span>Sign in with Google</span>
                </a>
              </div>
            </form>
          </div>

          <!-- REGISTER FORM -->
          <div v-if="authModalTab === 'register'" class="form-section">
            <form @submit.prevent="handleModalRegister">
              <div class="mt-3 mb-3 form-item">
                <label for="modalRegEmail">Email</label>
                <input type="email" class="form-control input-hover" id="modalRegEmail" v-model="modalRegisterForm.mail" required>
              </div>
              <div class="mb-3 form-item">
                <label for="modalRegPassword">Mật khẩu</label>
                <input :type="showModalRegPass ? 'text' : 'password'" class="form-control input-hover" id="modalRegPassword" v-model="modalRegisterForm.pass" required>
              </div>
              <div class="mb-3 form-item">
                <label for="modalRegFullname">Họ và tên</label>
                <input type="text" class="form-control input-hover" id="modalRegFullname" v-model="modalRegisterForm.fullname" required>
              </div>
              <div class="mb-3 form-item">
                <label for="modalRegPhone">Số điện thoại</label>
                <input type="tel" class="form-control input-hover" id="modalRegPhone" v-model="modalRegisterForm.phone" 
                       pattern="[0-9]{9,11}" required>
                <small class="text-muted">Nhập 9-11 số điện thoại</small>
              </div>
              <div class="form-check mb-3 form-item">
                <input class="form-check-input checkbox-hover" type="checkbox" id="modalTermsCheck" v-model="modalRegisterForm.terms" required>
                <label class="form-check-label" for="modalTermsCheck">
                  Tôi đồng ý với <a href="#" class="link-hover" @click.prevent="showModalTerms = true">điều khoản sử dụng</a>
                </label>
              </div>

              <div class="d-grid form-item">
                <button type="submit" class="btn btn-dark btn-hover" :disabled="modalLoading">
                  <span v-if="modalLoading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  {{ modalLoading ? 'Đang xử lý...' : 'Đăng ký' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ── OTP ĐĂNG KÝ MODAL ── -->
    <Transition name="modal-fade">
      <div v-if="showModalRegisterOtp" class="auth-modal-overlay">
        <div class="auth-sub-modal fade-in-up" @click.stop>
          <div class="modal-header">
            <h5 class="modal-title">Xác nhận đăng ký</h5>
            <button type="button" class="btn-close" @click="closeModalRegisterOtp"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label>Mã OTP</label>
              <div class="input-group">
                <input v-model="modalRegisterOtp" type="text" class="form-control" 
                      placeholder="Nhập mã OTP 6 số" maxlength="6" @keypress="onlyNumbers">
                <button class="btn btn-outline-secondary" type="button" @click="resendModalRegisterOtp" 
                        :disabled="modalResendDisabled">
                  {{ modalResendCountdown > 0 ? `Gửi lại (${modalResendCountdown}s)` : 'Gửi lại' }}
                </button>
              </div>
              <small class="text-muted">Mã OTP đã được gửi đến email: {{ modalRegisterForm.mail }}</small>
            </div>
            <div class="alert alert-warning">
              <i class="bi bi-exclamation-triangle"></i>
              Mã OTP có hiệu lực trong 10 phút
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-primary" @click="verifyModalRegisterOtp" :disabled="modalLoading">
              <span v-if="modalLoading" class="spinner-border spinner-border-sm me-2"></span>
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ── QUÊN MẬT KHẨU OTP MODAL ── -->
    <Transition name="modal-fade">
      <div v-if="showModalForgotPassword" class="auth-modal-overlay">
        <div class="auth-sub-modal fade-in-up" @click.stop>
          <div class="modal-header">
            <h5 class="modal-title">Xác nhận quên mật khẩu</h5>
            <button type="button" class="btn-close" @click="closeModalForgotPassword"></button>
          </div>
          <div class="modal-body">
            <!-- Step 1: Nhập email -->
            <div v-if="modalForgotStep === 1">
              <div class="mb-3">
                <label>Email</label>
                <input v-model="modalForgotEmail" type="email" class="form-control" placeholder="Nhập email của bạn" required>
              </div>
              <div class="alert alert-info">
                <i class="bi bi-info-circle"></i>
                Chúng tôi sẽ gửi mã OTP để xác nhận yêu cầu khôi phục mật khẩu.
              </div>
            </div>
            <!-- Step 2: Nhập OTP -->
            <div v-if="modalForgotStep === 2">
              <div class="mb-3">
                <label>Mã OTP</label>
                <div class="input-group">
                  <input v-model="modalForgotOtp" type="text" class="form-control" 
                         placeholder="Nhập mã OTP 6 số" maxlength="6" @keypress="onlyNumbers">
                  <button class="btn btn-outline-secondary" type="button" @click="resendModalForgotOtp" 
                          :disabled="modalResendDisabled">
                    {{ modalResendCountdown > 0 ? `Gửi lại (${modalResendCountdown}s)` : 'Gửi lại' }}
                  </button>
                </div>
                <small class="text-muted">Mã OTP đã được gửi đến email: {{ modalForgotEmail }}</small>
              </div>
              <div class="alert alert-warning">
                <i class="bi bi-exclamation-triangle"></i>
                Mã OTP có hiệu lực trong 10 phút
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-primary" @click="handleModalForgotPassword" :disabled="modalLoading">
              <span v-if="modalLoading" class="spinner-border spinner-border-sm me-2"></span>
              {{ modalForgotStep === 1 ? 'Gửi OTP' : 'Xác nhận OTP' }}
            </button>
            <button type="button" class="btn btn-secondary" @click="closeModalForgotPassword">Hủy</button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ── GOOGLE PASSWORD MODAL ── -->
    <Transition name="modal-fade">
      <div v-if="showModalGooglePassword" class="auth-modal-overlay">
        <div class="auth-sub-modal fade-in-up" @click.stop>
          <div class="modal-header">
            <h5 class="modal-title">TÀI KHOẢN GOOGLE</h5>
          </div>
          <div class="modal-body">
            <div class="mb-1">
              <label class="fw-bold">Email: </label><label class="ms-1">{{ modalGoogleTemp.email }}</label>
            </div>
            <div class="mb-3" v-if="modalGoogleTemp.name">
              <label class="fw-bold">Tên: </label><label class="ms-1"> {{ modalGoogleTemp.name || 'Google User' }}</label>
            </div>
            <div class="mb-2">
              <label class="fw-bold">Vui lòng nhập mật khẩu cho tài khoản này:</label>
              <input 
                type="password" 
                class="form-control"
                v-model="modalGooglePassword" 
                placeholder="Nhập mật khẩu bạn muốn đặt"
                @keyup.enter="submitModalGooglePassword"
                autofocus
              >
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-primary" @click="submitModalGooglePassword" :disabled="!modalGooglePassword">
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ── ĐIỀU KHOẢN SỬ DỤNG MODAL ── -->
    <Transition name="modal-fade">
      <div v-if="showModalTerms" class="auth-modal-overlay">
        <div class="auth-sub-modal fade-in-up" @click.stop>
          <div class="modal-header">
            <h5 class="modal-title">Điều khoản sử dụng</h5>
            <button type="button" class="btn-close" @click="showModalTerms = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label>Shoedo brand do nhóm dự án tốt nghiệp chúng tôi nghĩ ra nhằm cho phép mọi người mua những sản phẩm giày chất lượng nhưng giá cả phải chăng</label>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="showModalTerms = false">Hủy</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api.js'
import axios from 'axios'
import logoUrl from '@/assets/Logoc.png'
import Toast from '@/components/Shared/Toast.vue'

const router    = useRouter()
const route     = useRoute()
const authStore = useAuthStore()

// Lấy danh mục cho dropdown Navbar
const categories = ref([])
const fetchCategories = async () => {
  try {
    const res = await api.getCategories()
    if (res.data?.success) categories.value = res.data.data
  } catch (e) { console.error('Lỗi lấy danh mục Navbar:', e) }
}

// ── Auth ───────────────────────────────────────────────────────
const isAuthenticated = computed(() => authStore.isAuthenticated)
const lastName = computed(() => {
  const name = authStore.user?.name || ''
  if (!name.trim()) return 'User'
  return name.trim().split(/\s+/).pop()
})
const maKH = computed(() => authStore.user?.maKH ?? null)

// ── Cart ──────────────────────────────────
const cartCount = computed(() => authStore.cartCount || 0)

const fetchCartCount = async () => {
  if (!isAuthenticated.value) return
  try {
    await authStore.updateCartCount()
  } catch (error) {
    console.error('Lỗi lấy số lượng giỏ hàng:', error)
  }
}

// ── Account dropdown ──────────────────────────────────────────
const showAccountDropdown = ref(false)
const accountDropdown     = ref(null)
const toggleAccountDropdown = () => { showAccountDropdown.value = !showAccountDropdown.value }

const logout = async () => {
  try {
    await authStore.logout()
    showAccountDropdown.value = false
    historyList.value = []
    router.push('/auth/login')
  } catch (e) { console.error('Logout error:', e) }
}

// ═══════════════════════════════════════════════════════════════
//  SEARCH 
// ═══════════════════════════════════════════════════════════════
const searchQuery        = ref('')
const showDropdownSearch = ref(false)
const historyList        = ref([])
const historyLoading     = ref(false)
const searchWrapper      = ref(null)
const searchInputEl      = ref(null)

// Gợi ý = lịch sử lọc theo keyword đang gõ
const filteredHistory = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return []
  return historyList.value
    .filter(h => h.keyword.toLowerCase().includes(q))
    .slice(0, 6)
})

// Lấy lịch sử từ DB
const fetchHistory = async () => {
  if (!isAuthenticated.value || !maKH.value) { historyList.value = []; return }
  historyLoading.value = true
  try {
    const r = await api.getLichSuTimKiem(maKH.value)
    historyList.value = r.data?.success ? (r.data.data || []) : []
  } catch { historyList.value = [] }
  finally { historyLoading.value = false }
}

// Focus vào ô input
const onSearchFocus = async () => {
  showDropdownSearch.value = true
  if (isAuthenticated.value && historyList.value.length === 0) {
    await fetchHistory()
  }
}

// Khi gõ chữ
const onSearchInput = () => {
  showDropdownSearch.value = true
}

// Đóng dropdown
const closeSearchDropdown = () => {
  showDropdownSearch.value = false
}

// Chọn từ gợi ý / lịch sử → fill & search luôn
const selectKeyword = (kw) => {
  searchQuery.value = kw
  showDropdownSearch.value = false
  performSearch(kw)
}

// ── Thực hiện tìm kiếm ───────────────────────────────────────
const doSearch = () => {
  const q = searchQuery.value.trim()
  if (!q) return
  showDropdownSearch.value = false
  performSearch(q)
}

const performSearch = async (q) => {
  // 1. Lưu vào DB lịch sử (nếu đã login và có maKH)
  if (isAuthenticated.value && maKH.value) {
    try {
      await api.luuTimKiem(maKH.value, q)
      const idx = historyList.value.findIndex(h => h.keyword.toLowerCase() === q.toLowerCase())
      if (idx !== -1) {
        const item = historyList.value.splice(idx, 1)[0]
        historyList.value.unshift({ ...item, thoiGian: new Date().toISOString() })
      } else {
        historyList.value.unshift({ keyword: q, thoiGian: new Date().toISOString() })
        if (historyList.value.length > 10) historyList.value.pop()
      }
    } catch { /* lưu lỗi không chặn search */ }
  }

  // 2. Chuyển sang trang sản phẩm với query ?q=...
  if (route.name === 'Sanpham') {
    router.replace({ name: 'Sanpham', query: { ...route.query, q } })
  } else {
    router.push({ name: 'Sanpham', query: { q } })
  }
}

// ── Xóa lịch sử ──────────────────────────────────────────────
const clearAllHistory = async () => {
  if (!maKH.value) return
  try {
    await api.xoaLichSuTimKiem(maKH.value)
    historyList.value = []
  } catch { console.error('Lỗi xóa lịch sử') }
}

const deleteOneHistory = async (keyword) => {
  if (!maKH.value) return
  try {
    await api.xoaMotTuKhoa(maKH.value, keyword)
    historyList.value = historyList.value.filter(h => h.keyword !== keyword)
  } catch { console.error('Lỗi xóa từ khóa') }
}

// ── Sync searchQuery ↔ route.query.q ────────────────────────
watch(() => route.query.q, (q) => {
  if (typeof q === 'string') searchQuery.value = q
  else if (!q) searchQuery.value = ''
}, { immediate: true })

// ── Click ngoài → đóng cả 2 dropdown ────────────────────────
const handleClickOutside = (e) => {
  if (accountDropdown.value && !accountDropdown.value.contains(e.target)) {
    showAccountDropdown.value = false
  }
  if (searchWrapper.value && !searchWrapper.value.contains(e.target)) {
    showDropdownSearch.value = false
  }
}

// ── Watch auth change ────────────────────────────────────────
watch(isAuthenticated, async (v) => {
  if (v) { 
    await fetchCartCount()
    await fetchHistory() 
  } else { 
    historyList.value = [] 
  }
})

// ═══════════════════════════════════════════════════════════════
//  AUTH MODAL 
// ═══════════════════════════════════════════════════════════════
const showAuthModal    = ref(false)
const authModalTab     = ref('login')  // 'login' | 'register'

// Login form
const modalLoginForm = ref({ identifier: '', pass: '', remember: false })
const showModalLoginPass = ref(false)
const modalAccountLocked = ref(false)
const modalAccountLockedMessage = ref('')
const modalLoading = ref(false)

// Register form
const modalRegisterForm = ref({ mail: '', pass: '', fullname: '', phone: '', terms: false })
const showModalRegPass = ref(false)

// Register OTP
const showModalRegisterOtp = ref(false)
const modalRegisterOtp = ref('')
const modalRegisterTempData = ref(null)

// Forgot Password
const showModalForgotPassword = ref(false)
const modalForgotStep = ref(1)
const modalForgotEmail = ref('')
const modalForgotOtp = ref('')

// Countdown
const modalResendCountdown = ref(0)
const modalResendDisabled = ref(false)
let modalCountdownInterval = null

// Google
const showModalGooglePassword = ref(false)
const modalGoogleTemp = ref({ email: '', name: '' })
const modalGooglePassword = ref('')

// Terms
const showModalTerms = ref(false)

// ── Helpers ──
const getImageUrl = (imagePath) => `http://localhost:8080/${imagePath}`

const onlyNumbers = (event) => {
  const char = String.fromCharCode(event.keyCode)
  if (!/[0-9]/.test(char)) event.preventDefault()
}

const startModalResendCountdown = (seconds = 60) => {
  modalResendDisabled.value = true
  modalResendCountdown.value = seconds
  if (modalCountdownInterval) clearInterval(modalCountdownInterval)
  modalCountdownInterval = setInterval(() => {
    if (modalResendCountdown.value > 0) {
      modalResendCountdown.value--
    } else {
      modalResendDisabled.value = false
      clearInterval(modalCountdownInterval)
    }
  }, 1000)
}

// ── Open / Close modal ──
const openAuthModal = (tab = 'login') => {
  authModalTab.value = tab
  showAuthModal.value = true
  // Reset forms
  modalLoginForm.value = { identifier: '', pass: '', remember: false }
  modalRegisterForm.value = { mail: '', pass: '', fullname: '', phone: '', terms: false }
  modalAccountLocked.value = false
  modalAccountLockedMessage.value = ''
  showModalLoginPass.value = false
  showModalRegPass.value = false
}

const closeAuthModal = () => {
  showAuthModal.value = false
}

// ── Login Handler ──
const handleModalLogin = async () => {
  modalLoading.value = true
  modalAccountLocked.value = false
  modalAccountLockedMessage.value = ''

  try {
    const response = await axios.post('/api/auth/login', {
      identifier: modalLoginForm.value.identifier,
      pass: modalLoginForm.value.pass,
      remember: modalLoginForm.value.remember
    }, { withCredentials: true })

    const data = response.data
    if (data.success) {
      authStore.user = data.user
      authStore.cartCount = data.user.cartCount || 0
      window.showToast?.('Đăng nhập thành công!', 'success')
      closeAuthModal()
      // Nếu đang ở trang cần auth, có thể redirect
      if (route.path === '/auth/login') {
        router.push('/customer/index')
      }
    } else {
      window.showToast?.(data.message || 'Đăng nhập thất bại', 'danger')
      if (data.message && data.message.includes('bị khóa')) {
        modalAccountLocked.value = true
        modalAccountLockedMessage.value = data.message
      }
    }
  } catch (error) {
    window.showToast?.(error.response?.data?.message || 'Đăng nhập thất bại. Vui lòng thử lại sau.', 'danger')
  } finally {
    modalLoading.value = false
  }
}

// ── Register Handler ──
const handleModalRegister = async () => {
  if (!modalRegisterForm.value.terms) {
    window.showToast?.('Vui lòng đồng ý với điều khoản sử dụng', 'warning')
    return
  }
  if (!/^\d{9,11}$/.test(modalRegisterForm.value.phone)) {
    window.showToast?.('Số điện thoại phải từ 9-11 số', 'warning')
    return
  }

  modalLoading.value = true
  try {
    const response = await axios.post('/api/auth/send-register', {
      mail: modalRegisterForm.value.mail,
      pass: modalRegisterForm.value.pass,
      fullname: modalRegisterForm.value.fullname,
      phone: modalRegisterForm.value.phone
    }, { withCredentials: true })

    const data = response.data
    if (data.success) {
      modalRegisterTempData.value = { ...modalRegisterForm.value }
      window.showToast?.('Mã OTP đã được gửi đến email của bạn!', 'success')
      showAuthModal.value = false
      showModalRegisterOtp.value = true
      startModalResendCountdown(60)
    } else {
      window.showToast?.(data.message || 'Có lỗi xảy ra khi gửi OTP', 'danger')
    }
  } catch (error) {
    window.showToast?.(error.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại sau.', 'danger')
  } finally {
    modalLoading.value = false
  }
}

// ── Verify Register OTP ──
const verifyModalRegisterOtp = async () => {
  if (!modalRegisterOtp.value || modalRegisterOtp.value.length !== 6) {
    window.showToast?.('Vui lòng nhập mã OTP 6 số', 'warning')
    return
  }

  modalLoading.value = true
  try {
    const response = await axios.post('/api/auth/complete-register', {
      mail: modalRegisterForm.value.mail,
      confirmationCode: modalRegisterOtp.value
    }, { withCredentials: true })

    const data = response.data
    if (data.success) {
      // Auto login after register
      const loginResponse = await axios.post('/api/auth/login', {
        identifier: modalRegisterForm.value.mail,
        pass: modalRegisterForm.value.pass,
        remember: true
      }, { withCredentials: true })

      if (loginResponse.data.success) {
        authStore.user = loginResponse.data.user
        authStore.cartCount = loginResponse.data.user.cartCount || 0
        window.showToast?.('Đăng ký và đăng nhập thành công!', 'success')
        closeModalRegisterOtp()
      }
    } else {
      window.showToast?.(data.message || 'Mã OTP không chính xác', 'danger')
    }
  } catch (error) {
    window.showToast?.(error.response?.data?.message || 'Có lỗi xảy ra', 'danger')
  } finally {
    modalLoading.value = false
  }
}

const resendModalRegisterOtp = async () => {
  if (!modalRegisterTempData.value) return
  modalLoading.value = true
  try {
    const response = await axios.post('/api/auth/send-register', {
      mail: modalRegisterTempData.value.mail,
      pass: modalRegisterTempData.value.pass,
      fullname: modalRegisterTempData.value.fullname,
      phone: modalRegisterTempData.value.phone
    }, { withCredentials: true })
    if (response.data.success) {
      window.showToast?.('Mã OTP mới đã được gửi!', 'success')
      startModalResendCountdown(60)
    } else {
      window.showToast?.(response.data.message || 'Có lỗi xảy ra khi gửi lại OTP', 'danger')
    }
  } catch (error) {
    window.showToast?.(error.response?.data?.message || 'Có lỗi xảy ra', 'danger')
  } finally {
    modalLoading.value = false
  }
}

const closeModalRegisterOtp = () => {
  showModalRegisterOtp.value = false
  modalRegisterOtp.value = ''
  modalRegisterTempData.value = null
  if (modalCountdownInterval) {
    clearInterval(modalCountdownInterval)
    modalResendDisabled.value = false
    modalResendCountdown.value = 0
  }
}

// ── Forgot Password Handler ──
const openModalForgotPassword = () => {
  showAuthModal.value = false
  showModalForgotPassword.value = true
  modalForgotStep.value = 1
  modalForgotEmail.value = ''
  modalForgotOtp.value = ''
}

const handleModalForgotPassword = async () => {
  if (modalForgotStep.value === 1) {
    if (!modalForgotEmail.value) {
      window.showToast?.('Vui lòng nhập email', 'warning')
      return
    }
    modalLoading.value = true
    try {
      const response = await axios.post('/api/auth/send-fg-pass', {
        email: modalForgotEmail.value
      }, { withCredentials: true })
      if (response.data.success) {
        window.showToast?.('Mã OTP đã được gửi đến email của bạn!', 'success')
        modalForgotStep.value = 2
        startModalResendCountdown(60)
      } else {
        window.showToast?.(response.data.message || 'Có lỗi xảy ra', 'danger')
      }
    } catch (error) {
      window.showToast?.(error.response?.data?.message || 'Có lỗi xảy ra', 'danger')
    } finally {
      modalLoading.value = false
    }
  } else {
    if (!modalForgotOtp.value || modalForgotOtp.value.length !== 6) {
      window.showToast?.('Vui lòng nhập mã OTP 6 số', 'warning')
      return
    }
    modalLoading.value = true
    try {
      const response = await axios.post('/api/auth/confirm-fg-pass', {
        email: modalForgotEmail.value,
        confirmationCode: modalForgotOtp.value
      }, { withCredentials: true })
      if (response.data.success) {
        window.showToast?.('Mật khẩu mới đã được gửi đến email của bạn!', 'success')
        closeModalForgotPassword()
        openAuthModal('login')
      } else {
        window.showToast?.(response.data.message || 'Mã OTP không chính xác', 'danger')
      }
    } catch (error) {
      window.showToast?.(error.response?.data?.message || 'Có lỗi xảy ra', 'danger')
    } finally {
      modalLoading.value = false
    }
  }
}

const resendModalForgotOtp = async () => {
  modalLoading.value = true
  try {
    const response = await axios.post('/api/auth/send-fg-pass', {
      email: modalForgotEmail.value
    }, { withCredentials: true })
    if (response.data.success) {
      window.showToast?.('Mã OTP mới đã được gửi!', 'success')
      startModalResendCountdown(60)
    } else {
      window.showToast?.(response.data.message || 'Có lỗi xảy ra', 'danger')
    }
  } catch (error) {
    window.showToast?.(error.response?.data?.message || 'Có lỗi xảy ra', 'danger')
  } finally {
    modalLoading.value = false
  }
}

const closeModalForgotPassword = () => {
  showModalForgotPassword.value = false
  modalForgotStep.value = 1
  modalForgotEmail.value = ''
  modalForgotOtp.value = ''
  if (modalCountdownInterval) {
    clearInterval(modalCountdownInterval)
    modalResendDisabled.value = false
    modalResendCountdown.value = 0
  }
}

// ── Google Sign-in via Popup ──
let googlePopup = null

const handleModalGoogleLogin = () => {
  const width = 500
  const height = 600
  const left = window.screenX + (window.outerWidth - width) / 2
  const top = window.screenY + (window.outerHeight - height) / 2
  
  googlePopup = window.open(
    'http://localhost:8080/oauth2/authorization/google',
    'GoogleLogin',
    `width=${width},height=${height},top=${top},left=${left},resizable=yes,scrollbars=yes`
  )

  // Listen for message from popup (or poll for redirect)
  const checkPopup = setInterval(() => {
    try {
      if (!googlePopup || googlePopup.closed) {
        clearInterval(checkPopup)
        return
      }
      // Check if popup has been redirected back to our domain
      const popupUrl = googlePopup.location.href
      if (popupUrl && popupUrl.includes('/auth/login') && popupUrl.includes('googleSuccess')) {
        const url = new URL(popupUrl)
        const googleSuccess = url.searchParams.get('googleSuccess')
        const email = url.searchParams.get('email')
        const name = url.searchParams.get('name')
        
        googlePopup.close()
        clearInterval(checkPopup)
        
        if (googleSuccess === 'true' && email) {
          processGoogleCallback(email, name)
        }
      }
    } catch (e) {
      // Cross-origin - popup still on Google's domain, keep polling
    }
  }, 500)
}

const processGoogleCallback = async (email, name) => {
  try {
    const response = await axios.get('/api/oauth2/google/callback', {
      params: { email, name: name || '' },
      withCredentials: true
    })
    const data = response.data
    if (data.success) {
      if (data.requirePassword) {
        showModalGooglePassword.value = true
        modalGoogleTemp.value = { email: data.email || email, name: data.name || name }
        modalGooglePassword.value = ''
        showAuthModal.value = false
      } else {
        await completeGoogleLogin(data.email || email, data.name || name)
      }
    } else {
      window.showToast?.(data.message || 'Đăng nhập Google thất bại', 'danger')
      if (data.message && data.message.includes('bị khóa')) {
        modalAccountLocked.value = true
        modalAccountLockedMessage.value = data.message + ' Vui lòng liên hệ quản trị viên qua Hotline: 1900 6869 để được khắc phục.'
      }
    }
  } catch (error) {
    window.showToast?.(error.response?.data?.message || 'Lỗi xử lý đăng nhập Google', 'danger')
  }
}

const completeGoogleLogin = async (email, name) => {
  modalLoading.value = true
  try {
    const response = await axios.post('/api/oauth2/google-login', {
      email, name
    }, { withCredentials: true })
    const data = response.data
    if (data.success) {
      authStore.user = data.user
      window.showToast?.('Đăng nhập Google thành công!', 'success')
      closeAuthModal()
    } else {
      window.showToast?.(data.message || 'Đăng nhập Google thất bại', 'danger')
    }
  } catch (error) {
    window.showToast?.(error.response?.data?.message || 'Lỗi xử lý đăng nhập Google', 'danger')
  } finally {
    modalLoading.value = false
  }
}

const submitModalGooglePassword = async () => {
  if (!modalGooglePassword.value || modalGooglePassword.value.trim() === '') {
    window.showToast?.('Vui lòng nhập mật khẩu để tiếp tục!', 'warning')
    return
  }
  showModalGooglePassword.value = false
  modalLoading.value = true
  try {
    const response = await axios.post('/api/oauth2/google-login-newuser', {
      email: modalGoogleTemp.value.email,
      name: modalGoogleTemp.value.name,
      password: modalGooglePassword.value
    }, { withCredentials: true })
    const data = response.data
    if (data.success) {
      authStore.user = data.user
      window.showToast?.('Đăng nhập Google thành công!', 'success')
      closeAuthModal()
    } else {
      window.showToast?.(data.message || 'Đăng nhập Google thất bại', 'danger')
    }
  } catch (error) {
    window.showToast?.(error.response?.data?.message || 'Lỗi xử lý đăng nhập Google', 'danger')
  } finally {
    modalLoading.value = false
    modalGooglePassword.value = ''
    modalGoogleTemp.value = { email: '', name: '' }
  }
}

// ── Lifecycle ────────────────────────────────────────────────
onMounted(() => {
  fetchCategories() // Tải danh mục cho Navbar
  if (isAuthenticated.value) { 
    fetchCartCount()
    fetchHistory() 
  }
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  if (modalCountdownInterval) clearInterval(modalCountdownInterval)
})
</script>

<style scoped>
/* Giữ nguyên CSS như cũ */
.bg-black { background-color: #000000 !important; }

.logo-box { width: 120px; height: 48px; transition: all 0.3s ease; padding: 5px; }
.logo-img { width: 100%; height: 100%; object-fit: contain; transform: scale(1.5); transition: transform 0.3s ease; }
.logo-box:hover { background-color: #fff; border-color: #fff; }

.nav-link { font-size: 0.9rem; letter-spacing: 1.5px; position: relative; opacity: 0.9; transition: opacity 0.3s; font-weight: 700 !important; }
.nav-link:hover { opacity: 1; }
.nav-link::after { content: ''; position: absolute; width: 0; height: 1px; bottom: 2px; left: 0; background-color: #fff; transition: width 0.4s cubic-bezier(0.25, 0.8, 0.25, 1); }
.nav-link:hover::after { width: 100%; }

/* ── CSS CHO MENU HOVER DANH MỤC (HÌNH CHỮ NHẬT NGANG MEGA-MENU) ── */
.dropdown-hover .dropdown-menu-mega {
  display: none; position: absolute; top: 100%; left: -50px; min-width: 500px; 
  background-color: #000; border-radius: 8px; padding: 15px; margin-top: 0; z-index: 1050;
  box-shadow: 0 10px 30px rgba(0,0,0,0.5);
}
.dropdown-hover:hover .dropdown-menu-mega {
  display: block; animation: fadeInMenu 0.2s ease;
}
.mega-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px;
}
.mega-item {
  display: block; color: #fff; text-decoration: none; padding: 10px 12px; 
  border-radius: 6px; font-size: 0.95rem; transition: background 0.2s; 
  text-align: center; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.mega-item:hover { background-color: #222; color: #fff; }
@keyframes fadeInMenu { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

/* ─── Search ─────────────────────────────────────────── */
.search-wrapper { width: 320px; z-index: 1000; }

.search-wrapper .input-group {
  border: 1px solid #fff;
  border-radius: 8px;
  overflow: hidden;
  height: 44px;
  background: #000;
  transition: border-radius 0.15s;
}

.search-wrapper .input-group.dropdown-open {
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
}

.search-wrapper .input-group > input.form-control,
.search-wrapper .input-group > .btn { height: 100%; border-radius: 0; }

input::placeholder { color: #666 !important; font-weight: 700 !important; font-size: 0.9rem; }
input:focus { outline: none; box-shadow: none; }

.search-history-dropdown {
  position: absolute;
  top: 100%; left: 0; width: 100%;
  background: #000;
  border: 1px solid #fff; border-top: none;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
  z-index: 1050;
  box-shadow: 0 8px 24px rgba(0,0,0,0.6);
  max-height: 340px;
  overflow-y: auto;
}

.sh-header { border-bottom: 1px solid #222; }
.sh-section-title { font-size: 0.78rem; font-weight: 600; color: #666; border-bottom: 1px solid #222; }

.sh-item {
  font-size: 0.9rem; font-weight: 500; cursor: pointer;
  transition: background 0.15s; border-bottom: 1px solid #111;
}
.sh-item:last-child { border-bottom: none; }
.sh-item:hover { background: #1a1a1a; }

.sh-delete-icon {
  font-size: 1.1rem; color: #555; cursor: pointer;
  padding: 2px 4px; border-radius: 50%; transition: background 0.15s, color 0.15s; flex-shrink: 0;
}
.sh-delete-icon:hover { background: #333; color: #fff; }

.sh-clear-btn {
  font-size: 0.78rem; color: #666; cursor: pointer; transition: color 0.2s;
}
.sh-clear-btn:hover { color: #fff; text-decoration: underline; }

/* Transition */
.dropdown-fade-enter-active, .dropdown-fade-leave-active { transition: opacity 0.15s, transform 0.15s; }
.dropdown-fade-enter-from, .dropdown-fade-leave-to { opacity: 0; transform: translateY(-6px); }

/* ─── Cart ─────────────────────────────────────────── */
.btn-icon {
  padding: 0 14px; border: 1px solid #fff; border-radius: 10px;
  transition: transform 0.18s, box-shadow 0.18s, background-color 0.18s;
  height: 48px; display: inline-flex; align-items: center; justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 18px rgba(0,0,0,0.45), 0 2px 6px rgba(0,0,0,0.35);
  background: linear-gradient(180deg, rgba(255,255,255,0.02), rgba(0,0,0,0.06));
}
.btn-icon:hover { background-color: #fff; color: #000 !important; }
.btn-icon:active { transform: translateY(-3px); }
.btn-icon:focus  { outline: none; }

.cart-badge { top: -6px; right: -6px; font-size: 0.7rem; padding: 0.35em 0.6em; border: 2px solid #000; font-weight: bold; }
.btn-icon:hover .cart-badge { border-color: #fff; }

/* ─── User box ─────────────────────────────────────── */
.user-box { border: 1px solid #fff; border-radius: 8px !important; transition: all 0.3s; height: 44px; align-items: center; flex-shrink: 0; white-space: nowrap; font-size: 0.95rem !important }
.user-box:hover { background-color: #fff !important; color: #000 !important; }
.user-box span  { font-weight: 700 !important; }

.dropdown-menu-dark { background-color: #000; border-radius: 8px; }
.dropdown-menu-dark .dropdown-item { color: #fff; font-size: 0.95rem; padding: 10px 20px; transition: background 0.2s, color 0.2s; }
.dropdown-menu-dark .dropdown-item:hover { background-color: #222; }
.dropdown-menu-dark .dropdown-item.text-danger:hover { background-color: #2b0000; color: #ff6b6b !important; }

/* ─── Buttons Login & Register ─────────────────────────────────── */
.btn-login,
.btn-register {
  padding: 0 20px;
  border-radius: 50px;
  font-weight: 700;
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  transition: all 0.3s ease;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
}

.btn-login {
  background-color: transparent;
  border: 1px solid #fff;
  color: #fff;
}

.btn-login:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255,255,255,0.2);
}

.btn-register {
  background: linear-gradient(135deg, #fff 0%, #f0f0f0 100%);
  border: none;
  color: #000;
  box-shadow: 0 2px 8px rgba(255,255,255,0.2);
}

.btn-register:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255,255,255,0.3);
  background: linear-gradient(135deg, #f0f0f0 0%, #fff 100%);
}

@media (max-width: 991px) { 
  .search-wrapper { display: none; }
  .btn-login,
  .btn-register {
    padding: 0 16px;
    font-size: 0.85rem;
    border-radius: 50px;
  }
}

/* ═══════════════════════════════════════════════════════════════ */
/*  AUTH MODAL STYLES (matching Login.vue)                        */
/* ═══════════════════════════════════════════════════════════════ */

/* ===== ANIMATIONS (giống Login.vue) ===== */
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to   { opacity: 1; transform: translateY(0); }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.fade-in-up {
  animation: fadeInUp 0.5s ease-out forwards;
}

.form-item {
  opacity: 0;
  animation: fadeInUp 0.4s ease-out forwards;
  animation-delay: calc(var(--i, 0) * 0.05s);
}

.form-item:nth-child(1) { --i: 1; }
.form-item:nth-child(2) { --i: 2; }
.form-item:nth-child(3) { --i: 3; }
.form-item:nth-child(4) { --i: 4; }
.form-item:nth-child(5) { --i: 5; }
.form-item:nth-child(6) { --i: 6; }
.form-item:nth-child(7) { --i: 7; }
.form-item:nth-child(8) { --i: 8; }

.shake-alert {
  animation: shake 0.4s ease-in-out;
}

/* ===== HOVER EFFECTS (giống Login.vue) ===== */
.tab-hover {
  transition: all 0.2s ease;
}
.tab-hover:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.input-hover {
  transition: all 0.2s ease;
}
.input-hover:focus {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.btn-hover {
  transition: all 0.2s ease;
}
.btn-hover:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.link-hover {
  position: relative;
  text-decoration: none;
  transition: color 0.2s ease;
}
.link-hover::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 1px;
  background: #000;
  transition: width 0.2s ease;
}
.link-hover:hover::after {
  width: 100%;
}

.checkbox-hover {
  transition: transform 0.2s ease;
  cursor: pointer;
}
.checkbox-hover:hover {
  transform: scale(1.1);
}

.google-btn {
  transition: all 0.2s ease;
}
.google-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.google-icon {
  transition: transform 0.3s ease;
}
.google-btn:hover .google-icon {
  transform: rotate(360deg);
}

/* ===== OVERLAY ===== */
.auth-modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(12px) brightness(0.9);
}

/* ===== MAIN AUTH MODAL BOX (giống auth-wrapper trong Login.vue) ===== */
.auth-modal-box {
  position: relative;
  width: 420px;
  max-width: 94vw;
  max-height: 92vh;
  overflow-y: auto;
  background-color: rgba(255, 255, 255, 0.98);
  border-radius: 24px;
  border: 3px solid #000000;
  box-shadow: 0 0 40px rgba(0, 0, 0, 0.35);
  padding: 25px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.auth-modal-box:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.4);
}

/* Close button */
.btn-close-modal {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: rgba(0,0,0,0.05);
  color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 10;
  font-size: 0.8rem;
}
.btn-close-modal:hover {
  background: #000;
  color: #fff;
  transform: scale(1.05);
}

/* Tabs (giống Login.vue nav-tabs) */
.auth-modal-box .nav-tabs .nav-link {
  color: #000000;
  border: 1px solid transparent;
  font-weight: 500;
}
.auth-modal-box .nav-tabs .nav-link.active {
  color: #fff !important;
  background-color: #000000 !important;
}

/* Form section */
.auth-modal-box .form-section {
  margin-top: 15px;
}

/* Alert (giống Login.vue) */
.auth-modal-box .alert {
  margin-top: 15px;
  margin-bottom: 10px;
}
.auth-modal-box .alert-warning {
  background-color: #fff3cd;
  border-color: #ffeeba;
  color: #856404;
  font-size: 0.9rem;
  padding: 10px;
}

/* Placeholder & small (giống Login.vue) */
.auth-modal-box ::placeholder {
  color: #999;
  font-size: 0.9rem;
}
.auth-modal-box small {
  font-size: 0.8rem;
  color: #666;
}

/* Disabled state */
.auth-modal-box .btn-outline-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ===== SUB MODALS (OTP, Forgot, Google, Terms - giống modal-content Login.vue) ===== */
.auth-sub-modal {
  width: 500px;
  max-width: 94vw;
  max-height: 92vh;
  overflow-y: auto;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.auth-sub-modal .modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #dee2e6;
}

.auth-sub-modal .modal-body {
  padding: 20px;
}

.auth-sub-modal .modal-footer {
  padding: 12px 20px;
  border-top: 1px solid #dee2e6;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.auth-sub-modal .modal-title {
  font-weight: 600;
  font-size: 1.1rem;
}

.auth-sub-modal .alert-warning {
  background-color: #fff3cd;
  border-color: #ffeeba;
  color: #856404;
  font-size: 0.9rem;
  padding: 10px;
}

.auth-sub-modal .alert-info {
  background-color: #d1ecf1;
  border-color: #bee5eb;
  color: #0c5460;
  font-size: 0.9rem;
  padding: 10px;
}

/* Modal transition */
.modal-fade-enter-active { transition: opacity 0.3s ease; }
.modal-fade-leave-active { transition: opacity 0.2s ease; }
.modal-fade-enter-from,
.modal-fade-leave-to { opacity: 0; }

/* Scrollbar */
.auth-modal-box::-webkit-scrollbar {
  width: 5px;
}
.auth-modal-box::-webkit-scrollbar-track {
  background: transparent;
}
.auth-modal-box::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 10px;
}
.auth-modal-box::-webkit-scrollbar-thumb:hover {
  background: #bbb;
}
</style>