<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import KH_Navbar from '@/components/Shared/KH_Navbar.vue'
import Footer from '@/components/Shared/Footer.vue'
import api from '@/services/api.js'

const router = useRouter()
const route  = useRoute()

// =============================================
// State dữ liệu từ API
// =============================================
const allProducts = ref([])
const categories  = ref(['Tất cả'])
const loading     = ref(false)
const errorMsg    = ref('')

// =============================================
// State tìm kiếm
// =============================================
const searchKeyword = ref('')   // từ route.query.q

// =============================================
// State bộ lọc
// =============================================
const selectedCategory = ref('Tất cả')
const selectedGender   = ref('Tất cả')
const onlyInStock      = ref(false)
const selectedSort     = ref('Mặc định')
const showCategoryMenu = ref(false)
const showSortMenu     = ref(false)

const sortOptions = ['Mặc định', 'Giá tăng dần', 'Giá giảm dần', 'Mới nhất', '🔥 Flash Sale', '📈 Bán Chạy']

const sortMap = {
  'Mặc định'    : 'default',
  'Giá tăng dần': 'price_asc',
  'Giá giảm dần': 'price_desc',
  'Mới nhất'    : 'newest',
  '🔥 Flash Sale': 'flash_sale',
  '📈 Bán Chạy' : 'ban_chay',
}

const isFlashSale = computed(() => selectedSort.value === '🔥 Flash Sale')
const isSearchMode = computed(() => searchKeyword.value.trim().length > 0)

// =============================================
// Refs dropdown position
// =============================================
const categoryBtnRef = ref(null)
const sortBtnRef     = ref(null)
const categoryPos    = ref({ top: 0, left: 0 })
const sortPos        = ref({ top: 0, left: 0 })

// =============================================
// Gọi API lấy danh mục
// =============================================
const fetchCategories = async () => {
  try {
    const res = await api.getCategories()
    if (res.data?.success) {
      categories.value = ['Tất cả', ...res.data.data]
    }
  } catch (e) {
    console.error('Lỗi lấy danh mục:', e)
  }
}

// =============================================
// Gọi API lấy sản phẩm
// Nếu đang ở chế độ tìm kiếm → dùng /api/public/search?keyword=
// Ngược lại → dùng /api/public/products với filter
// =============================================
const fetchProducts = async () => {
  loading.value  = true
  errorMsg.value = ''
  try {
    let res
    if (isSearchMode.value) {
      // Tìm kiếm theo từ khóa (backend tìm cả tên SP + danh mục)
      res = await api.searchPublicProducts({ keyword: searchKeyword.value.trim() })
    } else {
      const params = {
        category : selectedCategory.value === 'Tất cả' ? undefined : selectedCategory.value,
        gender   : selectedGender.value   === 'Tất cả' ? undefined : selectedGender.value,
        inStock  : onlyInStock.value,
        sort     : sortMap[selectedSort.value] || 'default',
      }
      res = await api.getPublicProducts(params)
    }

    if (res.data?.success) {
      allProducts.value = res.data.data
    } else {
      errorMsg.value = 'Không thể tải sản phẩm.'
    }
  } catch (e) {
    console.error('Lỗi lấy sản phẩm:', e)
    errorMsg.value = 'Lỗi kết nối máy chủ.'
  } finally {
    loading.value = false
  }
}

// =============================================
// Kết quả sau khi lọc thêm trên client
// (Search mode: cho phép lọc thêm danh mục/giới tính/tồn kho/sort)
// =============================================
const displayProducts = computed(() => {
  if (!isSearchMode.value) return allProducts.value  // đã lọc từ API

  let list = [...allProducts.value]

  // Lọc thêm theo danh mục nếu người dùng chọn
  if (selectedCategory.value !== 'Tất cả') {
    list = list.filter(p =>
      p.danhMucs && p.danhMucs.some(d => d === selectedCategory.value)
    )
  }

  // Lọc giới tính
  if (selectedGender.value === 'Nam') {
    list = list.filter(p => p.gioiTinh === true)
  } else if (selectedGender.value === 'Nữ') {
    list = list.filter(p => p.gioiTinh === false)
  }

  // Lọc còn hàng
  if (onlyInStock.value) {
    list = list.filter(p => p.conHang)
  }

  // Sort client-side
  const sort = sortMap[selectedSort.value] || 'default'
  if (sort === 'price_asc') {
    list.sort((a, b) => (a.giaSauKM || 0) - (b.giaSauKM || 0))
  } else if (sort === 'price_desc') {
    list.sort((a, b) => (b.giaSauKM || 0) - (a.giaSauKM || 0))
  } else if (sort === 'newest') {
    list.sort((a, b) => b.maSP - a.maSP)
  } else if (sort === 'flash_sale') {
    list = list.filter(p => p.khuyenMai > 0)
    list.sort((a, b) => b.khuyenMai - a.khuyenMai)
  } else if (sort === 'ban_chay') {
    list.sort((a, b) => (b.daBan || 0) - (a.daBan || 0))
  }

  return list
})

// =============================================
// Watch route.query.q → khi Navbar search
// =============================================
watch(() => route.query.q, (q) => {
  searchKeyword.value    = typeof q === 'string' ? q : ''
  // Reset bộ lọc khi vừa tìm kiếm
  selectedCategory.value = 'Tất cả'
  selectedGender.value   = 'Tất cả'
  onlyInStock.value      = false
  selectedSort.value     = 'Mặc định'
  fetchProducts()
}, { immediate: false })

// Watch filter → gọi lại API (chỉ khi KHÔNG ở search mode)
watch([selectedCategory, selectedGender, onlyInStock, selectedSort], () => {
  if (!isSearchMode.value) fetchProducts()
})

// =============================================
// Xóa tìm kiếm → về chế độ bình thường
// =============================================
const clearSearch = () => {
  router.replace({ name: 'Sanpham', query: {} })
  searchKeyword.value = ''
  fetchProducts()
}

// =============================================
// Helpers
// =============================================
const formatPrice = (val) => {
  if (val === null || val === undefined) return ''
  return Number(val).toLocaleString('vi-VN') + ' đ'
}

const getImageUrl = (hinhAnh) => {
  if (!hinhAnh) return 'https://via.placeholder.com/400x400?text=No+Image'
  if (hinhAnh.startsWith('http')) return hinhAnh
  return `/images/${hinhAnh}`
}

// =============================================
// Dropdown handlers
// =============================================
const updateCategoryPos = () => {
  if (!categoryBtnRef.value) return
  const rect = categoryBtnRef.value.getBoundingClientRect()
  categoryPos.value = { top: rect.bottom + window.scrollY + 6, left: rect.left + window.scrollX }
}
const updateSortPos = () => {
  if (!sortBtnRef.value) return
  const rect = sortBtnRef.value.getBoundingClientRect()
  sortPos.value = { top: rect.bottom + window.scrollY + 6, left: rect.left + window.scrollX }
}
const toggleCategory = () => {
  updateCategoryPos()
  showCategoryMenu.value = !showCategoryMenu.value
  showSortMenu.value = false
}
const toggleSort = () => {
  updateSortPos()
  showSortMenu.value = !showSortMenu.value
  showCategoryMenu.value = false
}
const selectCategory = (cat)  => { selectedCategory.value = cat;  showCategoryMenu.value = false }
const selectSort     = (sort) => { selectedSort.value = sort;     showSortMenu.value = false }
const setGender      = (g)    => { selectedGender.value = selectedGender.value === g ? 'Tất cả' : g }
const goToDetail     = (id)   => { router.push({ name: 'DetailProduct', params: { id } }) }
const closeDropdowns = ()     => { showCategoryMenu.value = false; showSortMenu.value = false }

const resetFilters = () => {
  selectedCategory.value = 'Tất cả'
  selectedGender.value   = 'Tất cả'
  onlyInStock.value      = false
  selectedSort.value     = 'Mặc định'
}

// =============================================
// Lifecycle
// =============================================
onMounted(async () => {
  document.addEventListener('click', closeDropdowns)
  await fetchCategories()
  // Đọc keyword từ URL khi load trang
  const q = route.query.q
  if (q && typeof q === 'string') {
    searchKeyword.value = q
  }
  await fetchProducts()
})
onUnmounted(() => document.removeEventListener('click', closeDropdowns))
</script>

<template>
  <div class="kh-sanpham">
    <KH_Navbar />

    <div class="page-body">

      <!-- HEADER -->
      <div class="page-header">
        <div class="page-title-area">
          <h1 class="page-title" v-if="!isSearchMode">Sản Phẩm</h1>
          <!-- Tiêu đề khi đang tìm kiếm -->
          <div v-else class="search-header">
            <i class="bi bi-search search-header-icon"></i>
            <div>
              <h1 class="page-title" style="margin-bottom:2px;">Kết quả tìm kiếm</h1>
              <div class="search-keyword-label">
                "<strong>{{ searchKeyword }}</strong>"
                <button class="clear-search-btn" @click.stop="clearSearch">
                  <i class="bi bi-x-circle-fill"></i> Xóa tìm kiếm
                </button>
              </div>
            </div>
          </div>
        </div>
        <div class="gender-tabs">
          <button class="gender-btn" :class="{ active: selectedGender === 'Nam' }" @click.stop="setGender('Nam')">Nam</button>
          <button class="gender-btn" :class="{ active: selectedGender === 'Nữ' }"  @click.stop="setGender('Nữ')">Nữ</button>
        </div>
      </div>

      <!-- FILTER BAR -->
      <div class="filter-bar">
        <button ref="categoryBtnRef" class="filter-btn" :class="{ 'fbn-active': showCategoryMenu }" @click.stop="toggleCategory">
          <span>{{ selectedCategory === 'Tất cả' ? 'Danh Mục' : selectedCategory }}</span>
          <i class="bi bi-chevron-down chevron" :class="{ rotated: showCategoryMenu }"></i>
        </button>

        <div class="filter-right">
          <button class="filter-btn" :class="{ 'stock-active': onlyInStock }" @click.stop="onlyInStock = !onlyInStock">
            <i class="bi" :class="onlyInStock ? 'bi-check-circle-fill' : 'bi-circle'"></i>
            Còn Hàng
          </button>
          <button ref="sortBtnRef" class="filter-btn"
            :class="{ 'fbn-active': showSortMenu, 'flash-active': isFlashSale }"
            @click.stop="toggleSort">
            <span>{{ selectedSort }}</span>
            <i class="bi bi-chevron-down chevron" :class="{ rotated: showSortMenu }"></i>
          </button>
        </div>
      </div>

      <!-- FLASH SALE BANNER -->
      <div v-if="isFlashSale" class="flash-banner">
        <span class="flash-icon">🔥</span>
        <span class="flash-text">Flash Sale — Sản phẩm giảm giá mạnh nhất</span>
        <button class="flash-close" @click.stop="resetFilters">✕ Bỏ lọc</button>
      </div>

      <!-- SEARCH RESULT INFO -->
      <div v-if="isSearchMode && !loading" class="search-info-bar">
        <span>Tìm thấy <strong>{{ displayProducts.length }}</strong> sản phẩm cho "<em>{{ searchKeyword }}</em>"</span>
        <span v-if="selectedCategory !== 'Tất cả' || selectedGender !== 'Tất cả' || onlyInStock || selectedSort !== 'Mặc định'" class="filter-active-tag">
          <i class="bi bi-funnel-fill"></i> Đang lọc
          <button @click.stop="resetFilters" class="tag-reset">✕</button>
        </span>
      </div>

      <!-- RESULT COUNT (chế độ bình thường) -->
      <div class="result-count" v-if="!loading && !isSearchMode">
        Hiển thị <strong>{{ displayProducts.length }}</strong> sản phẩm
      </div>

      <!-- LOADING -->
      <div v-if="loading" class="empty-state">
        <i class="bi bi-hourglass-split"></i>
        <p>Đang tải sản phẩm...</p>
      </div>

      <!-- ERROR -->
      <div v-else-if="errorMsg" class="empty-state">
        <i class="bi bi-exclamation-circle"></i>
        <p>{{ errorMsg }}</p>
        <button class="btn-reset" @click="fetchProducts">Thử lại</button>
      </div>

      <!-- PRODUCT GRID -->
      <div class="product-grid" v-else-if="displayProducts.length > 0">
        <div
          v-for="p in displayProducts"
          :key="p.maSP"
          class="pcard"
          @click="goToDetail(p.maSP)"
        >
          <div class="pcard-img-wrap">
            <img :src="getImageUrl(p.hinhAnh)" :alt="p.tenSP" class="pcard-img" />
            <span v-if="p.khuyenMai > 0 && isFlashSale" class="pcard-badge flash-badge">
              🔥 -{{ p.khuyenMai }}%
            </span>
            <span v-else-if="p.khuyenMai > 0" class="pcard-badge">-{{ p.khuyenMai }}%</span>
            <span v-if="!p.conHang" class="pcard-badge out">Hết hàng</span>
            <div class="pcard-overlay">
              <button class="pcard-quick-btn">Xem nhanh</button>
            </div>
          </div>
          <div class="pcard-body">
            <div class="pcard-brand">{{ p.danhMucs?.join(', ') }}</div>
            <div class="pcard-name">{{ p.tenSP }}</div>
            <div class="pcard-price-row">
              <span class="pcard-price">{{ formatPrice(p.giaSauKM) }}</span>
              <span v-if="p.khuyenMai > 0 && p.giaGoc" class="pcard-old-price">
                {{ formatPrice(p.giaGoc) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- EMPTY STATE -->
      <div v-else class="empty-state">
        <i class="bi bi-search"></i>
        <p v-if="isSearchMode">Không tìm thấy sản phẩm nào cho "<strong>{{ searchKeyword }}</strong>"</p>
        <p v-else>Không tìm thấy sản phẩm phù hợp</p>
        <button class="btn-reset" @click.stop="isSearchMode ? clearSearch() : resetFilters()">
          {{ isSearchMode ? 'Xóa tìm kiếm' : 'Xóa bộ lọc' }}
        </button>
      </div>

    </div>

    <!-- TELEPORT dropdowns -->
    <Teleport to="body">
      <div
        v-if="showCategoryMenu"
        class="g-dropdown"
        :style="{ top: categoryPos.top + 'px', left: categoryPos.left + 'px' }"
        @click.stop
      >
        <div
          v-for="cat in categories"
          :key="cat"
          class="g-dropdown-item"
          :class="{ 'g-active': selectedCategory === cat }"
          @click="selectCategory(cat)"
        >{{ cat }}</div>
      </div>

      <div
        v-if="showSortMenu"
        class="g-dropdown"
        :style="{ top: sortPos.top + 'px', left: sortPos.left + 'px' }"
        @click.stop
      >
        <div
          v-for="opt in sortOptions"
          :key="opt"
          class="g-dropdown-item"
          :class="{
            'g-active'        : selectedSort === opt && opt !== '🔥 Flash Sale' && opt !== '📈 Bán Chạy',
            'g-flash-item'    : opt === '🔥 Flash Sale',
            'g-flash-active'  : selectedSort === opt && opt === '🔥 Flash Sale',
            'g-banchay-item'  : opt === '📈 Bán Chạy',
            'g-banchay-active': selectedSort === opt && opt === '📈 Bán Chạy',
          }"
          @click="selectSort(opt)"
        >{{ opt }}</div>
      </div>
    </Teleport>

    <Footer />
  </div>
</template>

<style scoped>
.kh-sanpham { font-family: 'Segoe UI', Arial, sans-serif; font-size: 13px; background: #f0f0f0; min-height: 100vh; }
.page-body  { padding: 0 16px 24px; max-width: 1400px; margin: 0 auto; }

.page-header { display: flex; align-items: center; justify-content: space-between; padding: 20px 0 12px; }
.page-title  { font-size: 28px; font-weight: 800; color: #111; letter-spacing: -0.5px; margin: 0; }

/* Search header */
.search-header { display: flex; align-items: center; gap: 12px; }
.search-header-icon { font-size: 22px; color: #e53935; }
.search-keyword-label { font-size: 13px; color: #666; display: flex; align-items: center; gap: 10px; }
.search-keyword-label strong { color: #111; }
.clear-search-btn {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 3px 10px; background: transparent; border: 1.5px solid #e53935;
  border-radius: 20px; font-size: 11px; font-weight: 600; color: #e53935;
  cursor: pointer; transition: all 0.2s;
}
.clear-search-btn:hover { background: #e53935; color: #fff; }

/* Search info bar */
.search-info-bar {
  display: flex; align-items: center; gap: 12px;
  font-size: 12px; color: #666; margin-bottom: 14px;
  padding: 8px 12px; background: #fff; border-radius: 8px;
  border: 1px solid #e8e8e8;
}
.filter-active-tag {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 3px 10px; background: #111; color: #fff;
  border-radius: 20px; font-size: 11px; font-weight: 600;
}
.tag-reset {
  background: none; border: none; color: #aaa; cursor: pointer; padding: 0;
  font-size: 11px; line-height: 1; transition: color 0.2s;
}
.tag-reset:hover { color: #fff; }

.gender-tabs { display: flex; border: 1.5px solid #bbb; border-radius: 50px; overflow: hidden; }
.gender-btn  { width: 80px; text-align: center; padding: 8px 0; font-size: 13px; font-weight: 600; background: #fff; border: none; cursor: pointer; color: #444; transition: all 0.2s; }
.gender-btn:first-child { border-right: 1.5px solid #bbb; }
.gender-btn:hover  { background: #f0f0f0; }
.gender-btn.active { background: #111; color: #fff; }

.filter-bar   { display: flex; align-items: center; justify-content: space-between; padding: 10px 0 14px; gap: 10px; }
.filter-right { display: flex; align-items: center; gap: 10px; }

.filter-btn {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 8px 16px; background: #fff; border: 1.5px solid #bbb;
  border-radius: 20px; font-size: 13px; font-weight: 500; color: #333;
  cursor: pointer; transition: all 0.2s; white-space: nowrap; user-select: none;
}
.filter-btn:hover, .fbn-active { border-color: #111 !important; color: #111 !important; }
.stock-active { background: #111 !important; border-color: #111 !important; color: #fff !important; }
.flash-active { background: linear-gradient(135deg, #ff4e00, #e53935) !important; border-color: #e53935 !important; color: #fff !important; font-weight: 700 !important; }

.chevron { font-size: 11px; transition: transform 0.2s; }
.chevron.rotated { transform: rotate(180deg); }

.result-count { font-size: 12px; color: #888; margin-bottom: 14px; }

.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }

.pcard { background: #fff; border: 1px solid #e0e0e0; border-radius: 12px; overflow: hidden; cursor: pointer; transition: box-shadow 0.25s, transform 0.25s; }
.pcard:hover { box-shadow: 0 8px 28px rgba(0,0,0,0.13); transform: translateY(-3px); }

.pcard-img-wrap { position: relative; width: 100%; aspect-ratio: 1/1; overflow: hidden; background: #f8f8f8; }
.pcard-img { width: 100%; height: 100%; object-fit: cover; display: block; transition: transform 0.4s; }
.pcard:hover .pcard-img { transform: scale(1.07); }

.pcard-badge { position: absolute; top: 10px; left: 10px; background: #e53935; color: #fff; font-size: 10px; font-weight: 700; padding: 3px 8px; border-radius: 2px; }
.pcard-badge.out { background: #666; }
.flash-badge { background: linear-gradient(135deg, #ff4e00, #e53935) !important; font-size: 11px !important; padding: 4px 10px !important; border-radius: 4px !important; box-shadow: 0 2px 6px rgba(229,57,53,0.4); }

.flash-banner {
  display: flex; align-items: center; gap: 10px;
  background: linear-gradient(135deg, #fff3e0, #ffebee);
  border: 1.5px solid #ffccbc; border-radius: 10px;
  padding: 10px 16px; margin-bottom: 14px;
}
.flash-icon { font-size: 18px; }
.flash-text { flex: 1; font-size: 13px; font-weight: 600; color: #bf360c; }
.flash-close {
  padding: 4px 12px; background: transparent; border: 1.5px solid #e53935;
  border-radius: 20px; font-size: 12px; color: #e53935; cursor: pointer; font-weight: 600; transition: all 0.2s;
}
.flash-close:hover { background: #e53935; color: #fff; }

.pcard-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.18); display: flex; align-items: flex-end; justify-content: center; padding-bottom: 16px; opacity: 0; transition: opacity 0.25s; }
.pcard:hover .pcard-overlay { opacity: 1; }
.pcard-quick-btn { padding: 8px 20px; background: #fff; border: none; border-radius: 20px; font-size: 12px; font-weight: 700; color: #111; cursor: pointer; }

.pcard-body   { padding: 12px 14px 14px; border-top: 1px solid #eee; }
.pcard-brand  { font-size: 10px; color: #999; text-transform: uppercase; letter-spacing: 1.5px; margin-bottom: 4px; }
.pcard-name   { font-size: 13px; font-weight: 600; color: #111; margin-bottom: 6px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.pcard-price-row { display: flex; align-items: baseline; gap: 8px; }
.pcard-price     { font-size: 14px; font-weight: 700; color: #e53935; }
.pcard-old-price { font-size: 11px; color: #bbb; text-decoration: line-through; }

.empty-state { text-align: center; padding: 80px 0; color: #aaa; }
.empty-state i { font-size: 48px; display: block; margin-bottom: 12px; }
.empty-state p { font-size: 15px; margin-bottom: 20px; }
.btn-reset { padding: 10px 24px; background: #111; border: none; border-radius: 6px; color: #fff; font-size: 13px; font-weight: 600; cursor: pointer; }
.btn-reset:hover { background: #333; }

@media (max-width: 1024px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px)  { .product-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; } .page-title { font-size: 22px; } .filter-bar { flex-wrap: wrap; } }
@media (max-width: 480px)  { .product-grid { grid-template-columns: repeat(2, 1fr); gap: 8px; } }
</style>

<style>
.g-dropdown {
  position: absolute;
  background: #fff;
  border: 1.5px solid #ddd;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
  min-width: 180px;
  max-height: 320px;
  overflow-y: auto;
  z-index: 99999;
  font-family: 'Segoe UI', Arial, sans-serif;
}
.g-dropdown-item { padding: 10px 16px; font-size: 13px; color: #333; cursor: pointer; transition: background 0.15s; }
.g-dropdown-item:hover { background: #f5f5f5; }
.g-active { background: #111; color: #fff; font-weight: 600; }
.g-flash-item { border-top: 1px solid #eee; color: #e53935 !important; font-weight: 600; }
.g-flash-item:hover { background: #fff3e0 !important; }
.g-flash-active { background: linear-gradient(135deg, #ff4e00, #e53935) !important; color: #fff !important; font-weight: 700; }
.g-banchay-item { color: #f57c00 !important; font-weight: 600; }
.g-banchay-item:hover { background: #fff8f0 !important; }
.g-banchay-active { background: linear-gradient(135deg, #f57c00, #ff9800) !important; color: #fff !important; font-weight: 700; }
</style>