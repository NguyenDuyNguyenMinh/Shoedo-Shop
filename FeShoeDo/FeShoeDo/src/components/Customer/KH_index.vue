<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import KH_Navbar from '@/components/Shared/KH_Navbar.vue'
import Footer from '@/components/Shared/Footer.vue'
import ChatBox from '@/components/Shared/ChatBox.vue'
import axios from 'axios'

const router = useRouter()



const currentSlide = ref(0)
let carouselInterval = null // Biến lưu trữ bộ đếm thời gian

const carouselItems = computed(() => {
  const items = []
  // Lấy thẳng dữ liệu, không gán thêm badge nữa
  if (bestSellers.value && bestSellers.value.length > 0) items.push(bestSellers.value[0])
  if (danhGiaProducts.value && danhGiaProducts.value.length > 0) items.push(danhGiaProducts.value[0])
  if (moiNhatProducts.value && moiNhatProducts.value.length > 0) items.push(moiNhatProducts.value[0])
  return items
})

const startCarousel = () => {
  carouselInterval = setInterval(() => {
    if (carouselItems.value.length > 0) {
      currentSlide.value = (currentSlide.value + 1) % carouselItems.value.length
    }
  }, 5000)
}

const resetCarouselTimer = () => {
  clearInterval(carouselInterval)
  startCarousel() // Chạy lại 3s từ đầu sau khi click tay
}

const prevSlide = () => {
  if (carouselItems.value.length === 0) return
  currentSlide.value = (currentSlide.value - 1 + carouselItems.value.length) % carouselItems.value.length
  resetCarouselTimer() 
}

const nextSlide = () => {
  if (carouselItems.value.length === 0) return
  currentSlide.value = (currentSlide.value + 1) % carouselItems.value.length
  resetCarouselTimer()
}

// ── API & STATE ──
const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'
const activeCampaigns = ref(null) // Mocked object cho Flash Sale Bùng Nổ
const khuyenMaiProducts = ref([])
const moiNhatProducts = ref([])
const danhGiaProducts = ref([])
const bestSellers = ref([])
const loading = ref(true)
const error = ref(null)

// ── COUNTDOWN LOGIC ──
const countdown = ref({ days: '00', hours: '00', minutes: '00', seconds: '00' })
let timerInterval = null

const startTimer = (endTime) => {
  const updateTimer = () => {
    const now = new Date().getTime()
    const distance = new Date(endTime).getTime() - now

    if (distance < 0) {
      clearInterval(timerInterval)
      countdown.value = { days: '00', hours: '00', minutes: '00', seconds: '00' }
      return
    }
    countdown.value = {
      days: String(Math.floor(distance / (1000 * 60 * 60 * 24))).padStart(2, '0'),
      hours: String(Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))).padStart(2, '0'),
      minutes: String(Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60))).padStart(2, '0'),
      seconds: String(Math.floor((distance % (1000 * 60)) / 1000)).padStart(2, '0')
    }
  }
  updateTimer()
  timerInterval = setInterval(updateTimer, 1000)
}

const fetchTrangChu = async () => {
  try {
    loading.value = true
    error.value = null
    const response = await axios.get(`${API_BASE}/san-pham/trang-chu`)
    const resData = response.data

    const payload = resData.data ? resData.data : resData

    if (payload) {
      // 1. MỤC FLASH SALE CHIẾN DỊCH (Lấy tất cả chạy hiệu ứng băng chuyền)
      if (payload.chienDichFlashSale && payload.chienDichFlashSale.products) {
        activeCampaigns.value = {
          ...payload.chienDichFlashSale,
          products: payload.chienDichFlashSale.products 
        }
        if (activeCampaigns.value.thoiGianKetThuc) {
          startTimer(activeCampaigns.value.thoiGianKetThuc)
        }
      } else {
        activeCampaigns.value = null
      }

      // 2. CÁC MỤC CÒN LẠI (Cũng ép cứng cắt đúng 5 phần tử đầu tiên)
      khuyenMaiProducts.value = (payload.khuyenMai || []).slice(0, 5)
      moiNhatProducts.value   = (payload.moiNhat || []).slice(0, 5)
      danhGiaProducts.value   = (payload.danhGiaCao || []).slice(0, 5)
      bestSellers.value       = (payload.banChay || []).slice(0, 5)
      
    } else {
      error.value = 'Không có dữ liệu trả về!'
    }
  } catch (err) {
    console.error('Lỗi fetch trang chủ:', err)
    error.value = 'Không thể kết nối đến máy chủ. Vui lòng thử lại!'
  } finally {
    loading.value = false
  }
}

const formatPrice = (price) => {
  if (!price && price !== 0) return ''
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price)
}

const getImageUrl = (hinhAnh) => {
  if (!hinhAnh) return 'https://placehold.co/300x300?text=No+Image'
  if (hinhAnh.startsWith('http')) return hinhAnh
  return `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/images/${hinhAnh}`
}

const goToDetail = (id) => router.push({ name: 'DetailProduct', params: { id } })
const viewAll = (section) => router.push({ name: 'Sanpham', query: { section } })

onMounted(() => {
  fetchTrangChu()
  startCarousel()
})

onUnmounted(() => { 
  if (timerInterval) clearInterval(timerInterval) 
  if (carouselInterval) clearInterval(carouselInterval) 
})
</script>

<template>
  <div class="kh-index">
    <KH_Navbar />

    <div class="hero-wrap" v-if="!loading && carouselItems.length > 0">
      <div class="hero-main">
        <div class="banner-bg"></div>
        
        <div class="click-zone left-zone" @click="prevSlide"></div>
        <div class="click-zone right-zone" @click="nextSlide"></div>

        <transition name="fade" mode="out-in">
          <div class="hero-content" :key="currentSlide">
            
            <div class="hero-left">
              <h1 class="hero-title artistic-text">
                {{ carouselItems[currentSlide].tenSP }}
              </h1>
              
              <p class="hero-subtitle">Mảnh ghép hoàn hảo cho phong cách của bạn.</p>
              
              <button class="hero-btn action-btn" @click.stop="goToDetail(carouselItems[currentSlide].maSP)">
                Khám Phá Ngay ❯
              </button>
            </div>

            <div class="hero-right">
              <div class="hero-image-frame circle-frame">
                <div class="circle-glow"></div>
                <img :src="getImageUrl(carouselItems[currentSlide].hinhAnh)" :alt="carouselItems[currentSlide].tenSP" class="hero-img circle-img" />
              </div>
            </div>
          </div>
        </transition>
      </div>
    </div>

    <div class="sections-container">
      <div v-if="loading" class="loading-wrap"><div class="loading-spinner"></div><p>Đang tải sản phẩm...</p></div>
      <div v-else-if="error" class="error-wrap"><p>⚠️ {{ error }}</p><button class="retry-btn" @click="fetchTrangChu">Thử lại</button></div>

      <template v-else>
        <section v-if="activeCampaigns && activeCampaigns.products.length > 0" class="campaign-section explosive-bg">
          <div class="campaign-header">
            <h2 class="campaign-title glitch-effect">{{ activeCampaigns.tenChienDich }}</h2>
            <div class="countdown-wrapper">
              <span class="pulse-text">Kết thúc trong:</span>
              <div class="timer">
                <div class="time-box"><span>{{ countdown.days }}</span><small>Ngày</small></div>:
                <div class="time-box"><span>{{ countdown.hours }}</span><small>Giờ</small></div>:
                <div class="time-box"><span>{{ countdown.minutes }}</span><small>Phút</small></div>:
                <div class="time-box"><span>{{ countdown.seconds }}</span><small>Giây</small></div>
              </div>
            </div>
          </div>
          
          <div class="marquee-wrapper">
            <div class="marquee-container">
              <div class="marquee-group" :class="{ 'is-scrolling': activeCampaigns.products.length > 5 }">
                <div v-for="(product, index) in activeCampaigns.products" :key="'cd-'+product.maSP" 
                     class="pcard flame-border marquee-item floating-card" 
                     :style="{ animationDelay: `${index * 0.4}s` }"
                     @click="goToDetail(product.maSP)">
                  <div class="pcard-img-wrap">
                    <img :src="getImageUrl(product.hinhAnh)" :alt="product.tenSP" class="pcard-img" />
                    <span class="badge-shock">-{{ product.khuyenMai || 50 }}%</span>
                  </div>
                  <div class="pcard-body dark-body">
                    <div class="pcard-name text-light">{{ product.tenSP }}</div>
                    <div class="pcard-price-wrap">
                      <span class="pcard-price flash-price">{{ formatPrice(product.giaSauKM) }}</span>
                      <span class="pcard-price-old">{{ formatPrice(product.giaGoc) }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="activeCampaigns.products.length > 5" class="marquee-group is-scrolling" aria-hidden="true">
                <div v-for="(product, index) in activeCampaigns.products" :key="'cd-clone-'+product.maSP" 
                     class="pcard flame-border marquee-item floating-card" 
                     :style="{ animationDelay: `${index * 0.4}s` }"
                     @click="goToDetail(product.maSP)">
                  <div class="pcard-img-wrap">
                    <img :src="getImageUrl(product.hinhAnh)" :alt="product.tenSP" class="pcard-img" />
                    <span class="badge-shock">-{{ product.khuyenMai || 50 }}%</span>
                  </div>
                  <div class="pcard-body dark-body">
                    <div class="pcard-name text-light">{{ product.tenSP }}</div>
                    <div class="pcard-price-wrap">
                      <span class="pcard-price flash-price">{{ formatPrice(product.giaSauKM) }}</span>
                      <span class="pcard-price-old">{{ formatPrice(product.giaGoc) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section class="product-section">
          <div class="section-header">
            <h2 class="section-title">Siêu Khuyến Mãi</h2>
            <button class="view-all-btn" @click="viewAll('khuyen-mai')">Xem tất cả</button>
          </div>
          <div class="product-grid">
            <div v-for="product in khuyenMaiProducts" :key="product.maSP" class="pcard" @click="goToDetail(product.maSP)">
              <div class="pcard-img-wrap">
                <img :src="getImageUrl(product.hinhAnh)" class="pcard-img" />
                <span v-if="product.khuyenMai > 0" class="badge-sale">-{{ product.khuyenMai }}%</span>
              </div>
              <div class="pcard-body">
                <div class="pcard-name">{{ product.tenSP }}</div>
                <div class="pcard-price-wrap">
                  <span class="pcard-price">{{ formatPrice(product.giaSauKM) }}</span>
                  <span class="pcard-price-old">{{ formatPrice(product.giaGoc) }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section class="product-section">
          <div class="section-header">
            <h2 class="section-title">Hàng Mới Về</h2>
            <button class="view-all-btn" @click="viewAll('moi-nhat')">Xem tất cả</button>
          </div>
          <div class="product-grid">
            <div v-for="product in moiNhatProducts" :key="product.maSP" class="pcard" @click="goToDetail(product.maSP)">
              <div class="pcard-img-wrap">
                <img :src="getImageUrl(product.hinhAnh)" class="pcard-img" />
                <span class="badge-new">NEW</span>
              </div>
              <div class="pcard-body">
                <div class="pcard-name">{{ product.tenSP }}</div>
                <div class="pcard-price-wrap">
                  <span class="pcard-price">{{ formatPrice(product.giaSauKM) }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section class="product-section">
          <div class="section-header">
            <h2 class="section-title">Đánh Giá Cao Nhất</h2>
            <button class="view-all-btn" @click="viewAll('danh-gia')">Xem tất cả</button>
          </div>
          <div class="product-grid">
            <div v-for="product in danhGiaProducts" :key="product.maSP" class="pcard" @click="goToDetail(product.maSP)">
              <div class="pcard-img-wrap">
                <img :src="getImageUrl(product.hinhAnh)" class="pcard-img" />
                <span class="badge-star">★ Top Rating</span>
              </div>
              <div class="pcard-body">
                <div class="pcard-name">{{ product.tenSP }}</div>
                <div class="pcard-price-wrap">
                  <span class="pcard-price">{{ formatPrice(product.giaSauKM) }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section class="product-section">
          <div class="section-header">
            <h2 class="section-title">Bán Chạy Nhất</h2>
            <button class="view-all-btn" @click="viewAll('ban-chay')">Xem tất cả</button>
          </div>
          <div class="product-grid">
            <div v-for="product in bestSellers" :key="product.maSP" class="pcard" @click="goToDetail(product.maSP)">
              <div class="pcard-img-wrap">
                <img :src="getImageUrl(product.hinhAnh)" class="pcard-img" />
                <span v-if="product.daBan > 0" class="badge-sold">Đã bán {{ product.daBan.toLocaleString('vi-VN') }}</span>
              </div>
              <div class="pcard-body">
                <div class="pcard-name">{{ product.tenSP }}</div>
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
    <ChatBox />
  </div>
</template>

<style scoped>
/* Giữ nguyên các style cũ của ông */
.kh-index { font-family: 'Segoe UI', sans-serif; background: #fafafa; min-height: 100vh; }
.hero-wrap { width: 100%; border-bottom: 1px solid #ddd; }

.hero-main { 
  width: 100%; 
  height: 600px; /* Tăng chiều cao làm banner hoành tráng hơn */
  position: relative; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  /* Nền Gradient cực sâu, kết hợp giữa xám không gian và đen */
  background: linear-gradient(135deg, #111111 0%, #1e1e24 50%, #0a0a0a 100%);
  overflow: hidden;
}

.banner-bg {
  position: absolute; inset: 0;
  /* Thêm một quầng sáng mờ đằng sau để tôn sản phẩm */
  background: radial-gradient(circle at 70% 50%, rgba(255, 255, 255, 0.08) 0%, transparent 60%);
}

.click-zone {
  position: absolute;
  top: 0; bottom: 0;
  width: 50%;
  z-index: 5;
  cursor: pointer;
}
.left-zone { left: 0; }
.right-zone { right: 0; }

/* Đưa nút bấm lên trên cùng để không bị vùng click tàng hình che mất */
.action-btn {
  position: relative;
  z-index: 10; 
}

/* ── LAYOUT CHIA ĐÔI ── */
.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 90%;
  max-width: 1300px;
  z-index: 2;
  gap: 60px;
}

/* ── NỬA TRÁI: TEXT NGHỆ THUẬT ── */
.hero-left {
  flex: 1.2;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 20px;
}
.hero-badge {
  background: transparent;
  color: #fff;
  padding: 6px 16px;
  border-radius: 30px;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 2px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  text-transform: uppercase;
}

/* Chữ nghệ thuật: To, in hoa, cắt kẹp dòng */
.artistic-text {
  font-size: 72px; /* Rất to */
  font-weight: 900;
  line-height: 1.1;
  margin: 0;
  text-transform: uppercase;
  /* Hiệu ứng màu gradient cho chữ */
  background: linear-gradient(to right, #ffffff, #888888);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hero-subtitle {
  color: #aaaaaa;
  font-size: 18px;
  font-weight: 300;
  margin: 0;
  letter-spacing: 1px;
}

.hero-btn {
  margin-top: 10px;
  padding: 15px 35px;
  background: #fff;
  color: #000;
  font-size: 16px;
  font-weight: 800;
  text-transform: uppercase;
  border: none;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s ease;
  letter-spacing: 1px;
}
.hero-btn:hover { background: #ccc; transform: translateX(5px); }

/* ── NỬA PHẢI: KHUNG ẢNH TRÒN BỰ ── */
.hero-right {
  flex: 1;
  display: flex;
  justify-content: center;
  position: relative;
}

.circle-frame {
  width: 450px; 
  height: 450px;
  border-radius: 50%; 
  background: rgba(255, 255, 255, 0.03); 
  border: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
  box-shadow: inset 0 0 40px rgba(255,255,255,0.05), 0 20px 50px rgba(0,0,0,0.5);
  backdrop-filter: blur(5px);
  /* QUAN TRỌNG: Lệnh này sẽ cắt cụt mọi thứ lòi ra khỏi hình tròn */
  overflow: hidden; 
}

.circle-img {
  width: 100%; /* Sửa lại 100% để vừa đúng lọt lòng khung tròn */
  height: 100%;
  object-fit: cover; /* Dùng cover để ảnh lấp đầy vòng tròn, không bị hở viền */
  filter: drop-shadow(0 30px 20px rgba(0,0,0,0.7));
  transition: transform 0.6s cubic-bezier(0.25, 1, 0.5, 1);
}
.circle-frame:hover .circle-img { 
  transform: scale(1.15) rotate(-8deg); 
}
@keyframes pulseGlow {
  0% { transform: translate(-50%, -50%) scale(1); opacity: 0.5; }
  100% { transform: translate(-50%, -50%) scale(1.1); opacity: 1; }
}


/* Responsive cho điện thoại */
@media (max-width: 1024px) {
  .artistic-text { font-size: 56px; }
  .circle-frame { width: 350px; height: 350px; }
}
@media (max-width: 768px) {
  .hero-main { height: auto; padding: 60px 0; }
  .hero-content { flex-direction: column-reverse; text-align: center; gap: 40px; }
  .hero-left { align-items: center; }
  .artistic-text { font-size: 42px; text-align: center; }
  .circle-frame { width: 300px; height: 300px; }
}
.banner-bg { position: absolute; inset: 0; background: radial-gradient(ellipse at center, rgba(255,255,255,0.05) 0%, transparent 70%); }
.banner-text { font-size: 56px; font-weight: bold; color: #fff; text-align: center; z-index: 2; }
.banner-text span { display: block; font-size: 24px; font-weight: normal; margin-top: 10px; }
.arrow-btn { position: absolute; top: 50%; transform: translateY(-50%); background: transparent; border: none; color: #fff; font-size: 2rem; cursor: pointer; z-index: 5; opacity: 0.7; transition: 0.2s; }
.arrow-btn:hover { opacity: 1; transform: translateY(-50%) scale(1.2); }
.arrow-btn.left { left: 30px; } .arrow-btn.right { right: 30px; }
.sections-container { max-width: 1400px; width: 96%; margin: 0 auto; padding: 30px 0 60px; display: flex; flex-direction: column; gap: 40px; }
.product-section { display: flex; flex-direction: column; gap: 16px; }
.section-header { display: flex; justify-content: space-between; align-items: flex-end; border-bottom: 2px solid #333; padding-bottom: 8px; }
.section-title { font-size: 24px; margin: 0; color: #222; font-weight: 700;}
.view-all-btn { background: #fff; border: 1px solid #444; padding: 6px 16px; font-size: 14px; cursor: pointer; border-radius: 4px; transition: all 0.2s; }
.view-all-btn:hover { background: #444; color: #fff; }
.product-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 15px; }

/* ── STYLE CARD SẢN PHẨM CƠ BẢN ── */
.pcard { border: 1px solid #e0e0e0; border-radius: 8px; background: #fff; overflow: hidden; cursor: pointer; transition: 0.2s; display: flex; flex-direction: column; height: 100%; }
.pcard:hover { box-shadow: 0 8px 20px rgba(0,0,0,0.08); transform: translateY(-4px); }
.pcard-img-wrap { width: 100%; padding-top: 100%; position: relative; background: #f8f8f8; overflow: hidden; }
.pcard-img { position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; }
.pcard-body { padding: 12px; display: flex; flex-direction: column; flex-grow: 1; gap: 8px; justify-content: space-between; }
.pcard-name { font-size: 14px; font-weight: 500; color: #333; display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.pcard-price-wrap { display: flex; flex-direction: column; gap: 2px; }
.pcard-price { font-size: 16px; font-weight: 700; color: #d32f2f; }
.pcard-price-old { font-size: 12px; color: #999; text-decoration: line-through; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.9s ease, transform 0.9s ease; }
.fade-enter-from { opacity: 0; transform: translateX(30px); }
.fade-leave-to { opacity: 0; transform: translateX(-30px); }

/* ── BADGES CÁC LOẠI ── */
.badge-sale, .badge-sold, .badge-new, .badge-star { position: absolute; top: 8px; left: 8px; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; z-index: 2; color: #fff; }
.badge-sale { background: #d32f2f; }
.badge-sold { background: #f57c00; }
.badge-new { background: #2e7d32; }
.badge-star { background: #fbc02d; color: #000; }

/* ================================================================= */
/* 🔥 HIỆU ỨNG CHIẾN DỊCH FLASH SALE BÙNG NỔ 🔥 */
/* ================================================================= */
.explosive-bg {
  background: linear-gradient(135deg, #4a0000 0%, #d32f2f 50%, #ff6f00 100%);
  padding: 25px; border-radius: 12px;
  box-shadow: 0 10px 30px rgba(211, 47, 47, 0.4);
  position: relative; overflow: hidden;
}
.explosive-bg::before {
  content: ''; position: absolute; top: -50%; left: -50%; width: 200%; height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 60%);
  animation: rotateGlow 10s linear infinite; pointer-events: none;
}
@keyframes rotateGlow { 100% { transform: rotate(360deg); } }

.campaign-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 15px; z-index: 2; position: relative;}
.campaign-title { font-size: 32px; color: #fff; font-weight: 900; margin: 0; text-transform: uppercase; text-shadow: 0 2px 10px rgba(0,0,0,0.5); }

/* Glitch & Pulse Effect */
.glitch-effect { animation: pulseText 1.5s infinite alternate; }
@keyframes pulseText { 0% { text-shadow: 0 0 10px #ffeb3b, 0 0 20px #ff9800; transform: scale(1); } 100% { text-shadow: 0 0 20px #ffeb3b, 0 0 30px #f44336; transform: scale(1.02); } }

.countdown-wrapper { display: flex; align-items: center; gap: 15px; background: rgba(0,0,0,0.4); padding: 10px 20px; border-radius: 30px; border: 1px solid rgba(255,255,255,0.2); }
.pulse-text { color: #fff; font-weight: bold; font-size: 16px; animation: blink 1s infinite; }
@keyframes blink { 50% { opacity: 0.5; } }

.timer { display: flex; gap: 8px; color: #ffeb3b; font-weight: bold; font-size: 20px; align-items: center; }
.time-box { display: flex; flex-direction: column; align-items: center; background: #fff; color: #d32f2f; padding: 4px 10px; border-radius: 6px; min-width: 45px; }
.time-box span { font-size: 20px; line-height: 1; }
.time-box small { font-size: 10px; font-weight: 600; text-transform: uppercase; color: #333; margin-top: 2px;}

/* ── MARQUEE CSS CHO FLASH SALE (Hiệu ứng cuộn liên tục) ── */
.marquee-wrapper { width: 100%; overflow: hidden; padding: 15px 0; }
.marquee-container { display: flex; gap: 15px; width: max-content; }
.marquee-group { display: flex; gap: 15px; flex-shrink: 0; }
.marquee-item { width: 268px; flex-shrink: 0; } /* Fix cứng width để không bị bể khi scroll */
.is-scrolling { animation: marqueeScroll 25s linear infinite; }
.marquee-wrapper:hover .is-scrolling { animation-play-state: paused; } /* Di chuột vào sẽ tạm dừng */

@keyframes marqueeScroll {
  0% { transform: translateX(0); }
  100% { transform: translateX(calc(-100% - 15px)); }
}

/* ── HIỆU ỨNG LƠ LỬNG CHO CARD ── */
.floating-card {
  animation: floatCard 3s ease-in-out infinite;
}
/* Khi rê chuột vào thì dừng lơ lửng và phóng to lên chút xíu */
.floating-card:hover {
  animation-play-state: paused;
  transform: translateY(-8px) scale(1.03) !important;
}



@keyframes floatCard {
  0% { transform: translateY(0); }
  50% { transform: translateY(-12px); } /* Nhô lên 12px */
  100% { transform: translateY(0); }
}

/* Card trong Flash Sale bùng nổ */
.flame-border { border: 2px solid #ff9800; box-shadow: 0 0 15px rgba(255, 152, 0, 0.5); transition: box-shadow 0.3s, border-color 0.3s; }
.flame-border:hover { box-shadow: 0 0 25px rgba(255, 235, 59, 0.8); border-color: #ffeb3b; }
.dark-body { background: #1a1a1a; border-top: 1px solid #333; }
.text-light { color: #fff !important; }
.flash-price { color: #ffeb3b !important; font-size: 18px; text-shadow: 0 0 5px rgba(255,235,59,0.5); }
.badge-shock { position: absolute; top: 0; right: 0; background: #ffeb3b; color: #d32f2f; padding: 8px 12px; font-size: 16px; font-weight: 900; border-bottom-left-radius: 12px; box-shadow: -2px 2px 10px rgba(0,0,0,0.3); animation: shockShake 0.5s infinite alternate; z-index: 2;}
@keyframes shockShake { 0% { transform: rotate(-3deg) scale(1); } 100% { transform: rotate(3deg) scale(1.1); } }

/* ════════ RESPONSIVE ════════ */
@media (max-width: 1200px) { .product-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 992px)  { .product-grid { grid-template-columns: repeat(3, 1fr); } .campaign-title { font-size: 24px; } }
@media (max-width: 768px)  { .product-grid { grid-template-columns: repeat(2, 1fr); } .campaign-header { flex-direction: column; align-items: flex-start; } }
@media (max-width: 480px)  { .product-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; } .timer { font-size: 16px; } .time-box { min-width: 35px; padding: 4px 6px; } }
</style>