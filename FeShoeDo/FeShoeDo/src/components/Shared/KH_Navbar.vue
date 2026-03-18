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
        
        <!-- ══════════ SEARCH ══════════ -->
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

          <!-- Dropdown lịch sử / gợi ý -->
          <Transition name="dropdown-fade">
            <div v-if="showDropdownSearch" class="search-history-dropdown">

              <!-- Đang gõ → hiển thị gợi ý từ lịch sử -->
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

              <!-- Chưa gõ → hiển thị lịch sử -->
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
                  <router-link to="/auth/login" class="text-white" @mousedown.prevent>Đăng nhập</router-link>
                  để lưu lịch sử
                </div>
              </template>

            </div>
          </Transition>
        </div>

        <!-- Giỏ hàng -->
        <router-link to="/customer/cart" class="btn btn-icon position-relative text-white">
          <i class="bi bi-cart3 fs-5"></i>
          <span v-if="isAuthenticated && cartCount > 0" class="position-absolute badge rounded-pill bg-danger cart-badge">
            {{ cartCount }}
          </span>
        </router-link>

        <!-- Account Dropdown -->
        <div class="dropdown" ref="accountDropdown">
          <button
            class="user-box d-flex align-items-center gap-2 px-3 py-2 rounded-0 cursor-pointer text-white bg-transparent"
            @click.prevent="toggleAccountDropdown"
            type="button"
          >
            <i class="bi bi-person fs-5"></i>
            <span class="fw-light">Tài Khoản</span>
            <i class="bi bi-chevron-down" style="font-size: 0.7rem;"></i>
          </button>

          <ul
            class="dropdown-menu dropdown-menu-dark dropdown-menu-end shadow mt-2"
            :class="{ show: showAccountDropdown }"
            v-show="showAccountDropdown"
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
import api from '@/services/api.js'
import logoUrl from '@/assets/Logoc.png'

const router    = useRouter()
const route     = useRoute()
const authStore = useAuthStore()

// ── Auth ───────────────────────────────────────────────────────
const isAuthenticated = computed(() => authStore.isAuthenticated)
const lastName = computed(() => {
  const name = authStore.user?.name || ''
  if (!name.trim()) return 'User'
  return name.trim().split(/\s+/).pop()
})
const maKH = computed(() => authStore.user?.maKH ?? null)

// ── Cart ───────────────────────────────────────────────────────
const cartCount = ref(0)
const fetchCartCount = async () => {
  if (!isAuthenticated.value) { cartCount.value = 0; return }
  try {
    const r = await api.getCartCount()
    cartCount.value = r.data?.count || 0
  } catch { cartCount.value = 0 }
}

// ── Account dropdown ──────────────────────────────────────────
const showAccountDropdown = ref(false)
const accountDropdown     = ref(null)
const toggleAccountDropdown = () => { showAccountDropdown.value = !showAccountDropdown.value }

const logout = async () => {
  try {
    await authStore.logout()
    cartCount.value = 0
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
const historyList        = ref([])   // [{ keyword, thoiGian }]
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
      // Cập nhật local history ngay (đưa lên đầu hoặc thêm mới)
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
  if (v) { await fetchCartCount(); await fetchHistory() }
  else   { cartCount.value = 0; historyList.value = [] }
})

// ── Lifecycle ────────────────────────────────────────────────
onMounted(() => {
  if (isAuthenticated.value) { fetchCartCount(); fetchHistory() }
  document.addEventListener('click', handleClickOutside)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.bg-black { background-color: #000000 !important; }

.logo-box { width: 120px; height: 48px; transition: all 0.3s ease; padding: 5px; }
.logo-img { width: 100%; height: 100%; object-fit: contain; transform: scale(1.5); transition: transform 0.3s ease; }
.logo-box:hover { background-color: #fff; border-color: #fff; }

.nav-link { font-size: 0.9rem; letter-spacing: 1.5px; position: relative; opacity: 0.9; transition: opacity 0.3s; font-weight: 700 !important; }
.nav-link:hover { opacity: 1; }
.nav-link::after { content: ''; position: absolute; width: 0; height: 1px; bottom: 2px; left: 0; background-color: #fff; transition: width 0.4s cubic-bezier(0.25, 0.8, 0.25, 1); }
.nav-link:hover::after { width: 100%; }

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

/* Khi dropdown mở → vuông góc dưới */
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
.user-box { border: 1px solid #fff; border-radius: 8px !important; transition: all 0.3s; height: 44px; align-items: center; }
.user-box:hover { background-color: #fff !important; color: #000 !important; }
.user-box span  { font-weight: 700 !important; }

.dropdown-menu-dark { background-color: #000; border-radius: 8px; }
.dropdown-menu-dark .dropdown-item { color: #fff; font-size: 0.95rem; padding: 10px 20px; transition: background 0.2s, color 0.2s; }
.dropdown-menu-dark .dropdown-item:hover { background-color: #222; }
.dropdown-menu-dark .dropdown-item.text-danger:hover { background-color: #2b0000; color: #ff6b6b !important; }

@media (max-width: 991px) { .search-wrapper { display: none; } }
</style>