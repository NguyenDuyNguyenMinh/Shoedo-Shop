<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import KH_Navbar from '@/components/Shared/KH_Navbar.vue'
import Footer from '@/components/Shared/Footer.vue'
import axios from 'axios'

const router = useRouter()

// ── BANNER CAROUSEL ──
const currentSlide = ref(0)
const slides = ref([
  { id: 1, bg: '#111',    line1: 'SALE', line2: 'GIÀY XỊN' },
  { id: 2, bg: '#0d1b2a', line1: 'NEW',  line2: 'ARRIVALS' },
  { id: 3, bg: '#1a0a0a', line1: 'HOT',  line2: 'DEALS'    },
])
const prevSlide = () => {
  currentSlide.value = (currentSlide.value - 1 + slides.value.length) % slides.value.length
}
const nextSlide = () => {
  currentSlide.value = (currentSlide.value + 1) % slides.value.length
}

// ── API ──
const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

// ── STATE ──
const flashSales       = ref([])
const featuredProducts = ref([])
const bestSellers      = ref([])
const loading          = ref(true)
const error            = ref(null)

// ── FETCH từ GET /api/san-pham/trang-chu ──
const fetchTrangChu = async () => {
  try {
    loading.value = true
    error.value   = null
    const { data } = await axios.get(`${API_BASE}/san-pham/trang-chu`)
    if (data.success) {
      flashSales.value       = data.data.flashSales || []
      featuredProducts.value = data.data.noiBat     || []
      bestSellers.value      = data.data.banChay    || []
    } else {
      error.value = data.message || 'Có lỗi xảy ra khi tải dữ liệu'
    }
  } catch (err) {
    console.error('Lỗi fetch trang chủ:', err)
    error.value = 'Không thể kết nối đến máy chủ. Vui lòng thử lại!'
  } finally {
    loading.value = false
  }
}

// ── FORMAT GIÁ (VND) ──
const formatPrice = (price) => {
  if (!price && price !== 0) return ''
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

// ── ẢNH: nếu là tên file thì ghép với thư mục static ──
const getImageUrl = (hinhAnh) => {
  if (!hinhAnh) return 'https://placehold.co/300x300?text=No+Image'
  if (hinhAnh.startsWith('http')) return hinhAnh
  // Khớp với file.upload-dir trong application.properties
  return `http://localhost:8080/images/${hinhAnh}`
}

// ── NAVIGATION ──
const goToDetail = (id) => {
  router.push({ name: 'DetailProduct', params: { id } })
}
const viewAll = (section) => {
  router.push({ name: 'ProductList', query: { section } })
}

onMounted(() => fetchTrangChu())
</script>

<template>
  <div class="kh-index">

    <KH_Navbar />

    <div class="hero-wrap">
      <div class="hero-main" :style="{ background: slides[currentSlide].bg }">
        <div class="banner-bg"></div>
        <button class="arrow-btn left" @click="prevSlide">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 18l-6-6 6-6"/></svg>
        </button>

        <div class="banner-text">
          Carousel Mẫu<br />
          <span>{{ slides[currentSlide].line1 }} {{ slides[currentSlide].line2 }}</span>
        </div>

        <button class="arrow-btn right" @click="nextSlide">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
        </button>
      </div>
    </div>

    <div class="sections-container">

      <!-- Loading -->
      <div v-if="loading" class="loading-wrap">
        <div class="loading-spinner"></div>
        <p>Đang tải sản phẩm...</p>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="error-wrap">
        <p>⚠️ {{ error }}</p>
        <button class="retry-btn" @click="fetchTrangChu">Thử lại</button>
      </div>

      <!-- Content -->
      <template v-else>

        <!-- Flash Sales -->
        <section class="product-section">
          <div class="section-header">
            <h2 class="section-title">Flash Sales</h2>
            <button class="view-all-btn" @click="viewAll('flash-sales')">Xem tất cả</button>
          </div>
          <div class="product-grid">
            <div
              v-for="product in flashSales"
              :key="product.maSP"
              class="pcard"
              @click="goToDetail(product.maSP)"
            >
              <div class="pcard-img-wrap">
                <img :src="getImageUrl(product.hinhAnh)" :alt="product.tenSP" class="pcard-img" />
                <span v-if="product.khuyenMai > 0" class="badge-sale">-{{ product.khuyenMai }}%</span>
              </div>
              <div class="pcard-body">
                <div class="pcard-name" :title="product.tenSP">{{ product.tenSP }}</div>
                <div class="pcard-price-wrap">
                  <span class="pcard-price">{{ formatPrice(product.giaSauKM) }}</span>
                  <span v-if="product.khuyenMai > 0" class="pcard-price-old">{{ formatPrice(product.giaGoc) }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Nổi Bật -->
        <section class="product-section">
          <div class="section-header">
            <h2 class="section-title">Nổi Bật</h2>
            <button class="view-all-btn" @click="viewAll('noi-bat')">Xem tất cả</button>
          </div>
          <div class="product-grid">
            <div
              v-for="product in featuredProducts"
              :key="product.maSP"
              class="pcard"
              @click="goToDetail(product.maSP)"
            >
              <div class="pcard-img-wrap">
                <img :src="getImageUrl(product.hinhAnh)" :alt="product.tenSP" class="pcard-img" />
              </div>
              <div class="pcard-body">
                <div class="pcard-name" :title="product.tenSP">{{ product.tenSP }}</div>
                <div class="pcard-price-wrap">
                  <span class="pcard-price">{{ formatPrice(product.giaSauKM) }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Bán Chạy -->
        <section class="product-section">
          <div class="section-header">
            <h2 class="section-title">Bán Chạy</h2>
            <button class="view-all-btn" @click="viewAll('ban-chay')">Xem tất cả</button>
          </div>
          <div class="product-grid">
            <div
              v-for="product in bestSellers"
              :key="product.maSP"
              class="pcard"
              @click="goToDetail(product.maSP)"
            >
              <div class="pcard-img-wrap">
                <img :src="getImageUrl(product.hinhAnh)" :alt="product.tenSP" class="pcard-img" />
                <span v-if="product.daBan > 0" class="badge-sold">Đã bán {{ product.daBan }}</span>
              </div>
              <div class="pcard-body">
                <div class="pcard-name" :title="product.tenSP">{{ product.tenSP }}</div>
                <div class="pcard-price-wrap">
                  <span class="pcard-price">{{ formatPrice(product.giaSauKM) }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

      </template>
    </div>

    <Footer />
  </div>
</template>

<style scoped>
.kh-index {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: #fafafa;
  min-height: 100vh;
}

/* ════════ HERO CAROUSEL ════════ */
.hero-wrap { width: 100%; border-bottom: 1px solid #ddd; }
.hero-main {
  width: 100%; height: 350px; position: relative;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.4s ease;
}
.banner-bg {
  position: absolute; inset: 0;
  background: radial-gradient(ellipse at center, rgba(255,255,255,0.05) 0%, transparent 70%);
}
.banner-text { font-size: 56px; font-weight: bold; color: #fff; text-align: center; z-index: 2; }
.banner-text span { display: block; font-size: 24px; font-weight: normal; margin-top: 10px; }
.arrow-btn {
  position: absolute; top: 50%; transform: translateY(-50%);
  background: transparent; border: none; color: #fff;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; z-index: 5; opacity: 0.7;
  transition: transform 0.2s, opacity 0.2s;
}
.arrow-btn:hover { opacity: 1; transform: translateY(-50%) scale(1.2); }
.arrow-btn.left  { left: 30px; }
.arrow-btn.right { right: 30px; }

/* ════════ LOADING / ERROR ════════ */
.loading-wrap {
  display: flex; flex-direction: column; align-items: center;
  gap: 16px; padding: 60px 0; color: #666;
}
.loading-spinner {
  width: 40px; height: 40px;
  border: 4px solid #eee; border-top-color: #d32f2f;
  border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.error-wrap { text-align: center; padding: 60px 0; color: #c62828; }
.retry-btn {
  margin-top: 12px; padding: 8px 24px;
  background: #d32f2f; color: #fff;
  border: none; border-radius: 4px; cursor: pointer; font-size: 14px;
}
.retry-btn:hover { background: #b71c1c; }

/* ════════ SECTIONS & GRID ════════ */
.sections-container {
  max-width: 1400px; width: 96%;
  margin: 0 auto; padding: 30px 0 60px;
  display: flex; flex-direction: column; gap: 40px;
}
.product-section { display: flex; flex-direction: column; gap: 16px; }
.section-header {
  display: flex; justify-content: space-between; align-items: flex-end;
  border-bottom: 2px solid #333; padding-bottom: 8px;
}
.section-title { font-size: 24px; margin: 0; color: #222; }
.view-all-btn {
  background: #fff; border: 1px solid #444;
  padding: 6px 16px; font-size: 14px; cursor: pointer;
  border-radius: 4px; transition: all 0.2s;
}
.view-all-btn:hover { background: #444; color: #fff; }
.product-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 15px; }

/* ════════ PRODUCT CARD ════════ */
.pcard {
  border: 1px solid #e0e0e0; border-radius: 8px; background: #fff;
  overflow: hidden; cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
  display: flex; flex-direction: column; height: 100%;
}
.pcard:hover { box-shadow: 0 8px 20px rgba(0,0,0,0.08); transform: translateY(-4px); }
.pcard-img-wrap {
  width: 100%; padding-top: 100%; position: relative;
  background: #f8f8f8; border-bottom: 1px solid #eee; overflow: hidden;
}
.pcard-img { position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; display: block; }
.badge-sale, .badge-sold {
  position: absolute; top: 8px; left: 8px;
  padding: 3px 8px; border-radius: 4px;
  font-size: 12px; font-weight: 700; z-index: 2;
}
.badge-sale { background: #d32f2f; color: #fff; }
.badge-sold { background: #f57c00; color: #fff; }
.pcard-body {
  padding: 12px; display: flex; flex-direction: column;
  justify-content: space-between; flex-grow: 1; gap: 8px;
}
.pcard-name {
  font-size: 14px; font-weight: 500; color: #333;
  display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2;
  -webkit-box-orient: vertical; overflow: hidden;
  text-overflow: ellipsis; line-height: 1.4;
}
.pcard-price-wrap { display: flex; flex-direction: column; gap: 2px; margin-top: auto; }
.pcard-price { font-size: 15px; font-weight: 700; color: #d32f2f; }
.pcard-price-old { font-size: 12px; color: #999; text-decoration: line-through; }

/* ════════ RESPONSIVE ════════ */
@media (max-width: 1200px) { .product-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 992px)  { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px)  { .product-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; } .hero-main { height: 250px; } .banner-text { font-size: 40px; } }
@media (max-width: 480px)  { .hero-main { height: 180px; } .banner-text { font-size: 28px; } .banner-text span { font-size: 14px; } .section-title { font-size: 18px; } .view-all-btn { padding: 4px 10px; font-size: 12px; } }
</style>