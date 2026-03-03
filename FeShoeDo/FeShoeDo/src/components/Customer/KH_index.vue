<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import KH_Navbar from '@/components/Shared/KH_Navbar.vue'
import Footer from '@/components/Shared/Footer.vue'

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

// ── DANH SÁCH SẢN PHẨM (Mock data) ──
const products = ref([
  { id: 1,  name: 'Nike Air Max 270',       price: '2.890.000 đ', stock: 20, image: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=300&q=80' },
  { id: 2,  name: 'Adidas Ultraboost 23',   price: '3.490.000 đ', stock: 15, image: 'https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=300&q=80' },
  { id: 3,  name: 'Air Jordan 1 Retro High OG Chicago', price: '4.190.000 đ', stock: 8,  image: 'https://images.unsplash.com/photo-1600185365926-3a2ce3cdb9eb?w=300&q=80' },
  { id: 4,  name: 'Vans Old Skool Classic', price: '1.290.000 đ', stock: 50, image: 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=300&q=80' },
  { id: 5,  name: 'Converse Chuck 70 Hi',   price: '1.590.000 đ', stock: 35, image: 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=300&q=80' },
  { id: 6,  name: 'New Balance 574 Core',   price: '1.990.000 đ', stock: 18, image: 'https://images.unsplash.com/photo-1584735175315-9d5df23be620?w=300&q=80' },
  { id: 7,  name: 'Nike React Infinity',    price: '2.690.000 đ', stock: 12, image: 'https://images.unsplash.com/photo-1539185441755-769473a23570?w=300&q=80' },
  { id: 8,  name: 'Puma RS-X Reinvention',  price: '1.650.000 đ', stock: 25, image: 'https://images.unsplash.com/photo-1491553895911-0055eca6402d?w=300&q=80' },
  { id: 9,  name: 'Reebok Classic Leather', price: '1.190.000 đ', stock: 30, image: 'https://images.unsplash.com/photo-1556906781-9a412961a28c?w=300&q=80' },
  { id: 10, name: 'Balenciaga Triple S',    price: '5.990.000 đ', stock: 5,  image: 'https://images.unsplash.com/photo-1512374382149-233c42b6a83b?w=300&q=80' },
])

// Lấy 5 sản phẩm mỗi hàng cho đẹp layout
const flashSales = computed(() => products.value.slice(0, 5))
const featuredProducts = computed(() => products.value.slice(5, 10))
const bestSellers = computed(() => [...products.value].reverse().slice(0, 5))

const goToDetail = (id) => {
  router.push({ name: 'DetailProduct', params: { id } })
}

const viewAll = (category) => {
  console.log('Chuyển đến trang xem tất cả của:', category)
}
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
      
      <section class="product-section">
        <div class="section-header">
          <h2 class="section-title">Flash Sales</h2>
          <button class="view-all-btn" @click="viewAll('flash-sales')">Xem tất cả</button>
        </div>
        <div class="product-grid">
          <div v-for="product in flashSales" :key="product.id" class="pcard" @click="goToDetail(product.id)">
            <div class="pcard-img-wrap">
              <img :src="product.image" :alt="product.name" class="pcard-img" />
            </div>
            <div class="pcard-body">
              <div class="pcard-name" :title="product.name">{{ product.name }}</div>
              <div class="pcard-price">{{ product.price }}</div>
            </div>
          </div>
        </div>
      </section>

      <section class="product-section">
        <div class="section-header">
          <h2 class="section-title">Nổi Bật</h2>
          <button class="view-all-btn" @click="viewAll('featured')">Xem tất cả</button>
        </div>
        <div class="product-grid">
          <div v-for="product in featuredProducts" :key="product.id" class="pcard" @click="goToDetail(product.id)">
            <div class="pcard-img-wrap">
              <img :src="product.image" :alt="product.name" class="pcard-img" />
            </div>
            <div class="pcard-body">
              <div class="pcard-name" :title="product.name">{{ product.name }}</div>
              <div class="pcard-price">{{ product.price }}</div>
            </div>
          </div>
        </div>
      </section>

      <section class="product-section">
        <div class="section-header">
          <h2 class="section-title">Bán Chạy</h2>
          <button class="view-all-btn" @click="viewAll('best-sellers')">Xem tất cả</button>
        </div>
        <div class="product-grid">
          <div v-for="product in bestSellers" :key="product.id" class="pcard" @click="goToDetail(product.id)">
            <div class="pcard-img-wrap">
              <img :src="product.image" :alt="product.name" class="pcard-img" />
            </div>
            <div class="pcard-body">
              <div class="pcard-name" :title="product.name">{{ product.name }}</div>
              <div class="pcard-price">{{ product.price }}</div>
            </div>
          </div>
        </div>
      </section>

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
.hero-wrap {
  width: 100%;
  border-bottom: 1px solid #ddd;
}
.hero-main {
  width: 100%;
  height: 350px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.4s ease;
}
.banner-bg {
  position: absolute; inset: 0;
  background: radial-gradient(ellipse at center, rgba(255,255,255,0.05) 0%, transparent 70%);
}
.banner-text {
  font-size: 56px;
  font-weight: bold;
  color: #fff;
  text-align: center;
  z-index: 2;
}
.banner-text span {
  display: block;
  font-size: 24px;
  font-weight: normal;
  margin-top: 10px;
}
.arrow-btn {
  position: absolute;
  top: 50%; transform: translateY(-50%);
  background: transparent;
  border: none; color: #fff;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; z-index: 5;
  transition: transform 0.2s, opacity 0.2s;
  opacity: 0.7;
}
.arrow-btn:hover { opacity: 1; transform: translateY(-50%) scale(1.2); }
.arrow-btn.left  { left: 30px; }
.arrow-btn.right { right: 30px; }

/* ════════ SECTIONS & GRID ════════ */
.sections-container {
  /* Tăng max-width và set width lớn để lấp đầy khoảng trắng 2 bên */
  max-width: 1400px;
  width: 96%;
  margin: 0 auto;
  padding: 30px 0 60px;
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.product-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-bottom: 2px solid #333;
  padding-bottom: 8px;
}

.section-title {
  font-size: 24px;
  margin: 0;
  color: #222;
}

.view-all-btn {
  background: #fff;
  border: 1px solid #444;
  padding: 6px 16px;
  font-size: 14px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
}

.view-all-btn:hover {
  background: #444;
  color: #fff;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr); /* 5 SẢN PHẨM TRÊN 1 DÒNG */
  gap: 15px; /* Khoảng cách giữa các thẻ */
}

/* ════════ PRODUCT CARD ════════ */
.pcard {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: #fff;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
  
  /* Flexbox giúp các thẻ luôn cao bằng nhau */
  display: flex;
  flex-direction: column;
  height: 100%; 
}

.pcard:hover { 
  box-shadow: 0 8px 20px rgba(0,0,0,0.08); 
  transform: translateY(-4px); 
}

/* Ép khung ảnh thành hình vuông hoàn hảo không bao giờ tràn */
.pcard-img-wrap { 
  width: 100%; 
  padding-top: 100%; /* Mẹo tạo hình vuông tỷ lệ 1:1 */
  position: relative;
  background: #f8f8f8; 
  border-bottom: 1px solid #eee;
  overflow: hidden;
}

.pcard-img { 
  position: absolute;
  top: 0;
  left: 0;
  width: 100%; 
  height: 100%; 
  object-fit: cover; /* Cắt vừa vặn, không bị kéo dãn hay méo hình */
  display: block; 
}

.pcard-body { 
  padding: 12px; 
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* Giữ giá sản phẩm luôn nằm dưới cùng */
  flex-grow: 1; /* Lấp đầy khoảng trống còn lại của thẻ */
  gap: 8px;
}

.pcard-name { 
  font-size: 14px; 
  font-weight: 500; 
  color: #333; 
  
  /* Hiển thị tối đa 2 dòng nếu tên quá dài, sau đó có dấu ... */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp:2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.pcard-price { 
  font-size: 15px; 
  font-weight: 700; 
  color: #d32f2f; 
  margin-top: auto; /* Luôn đẩy giá xuống sát đáy */
}

/* ════════ RESPONSIVE ════════ */
@media (max-width: 1200px) {
  .product-grid { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 992px) { 
  .product-grid { grid-template-columns: repeat(3, 1fr); } 
}
@media (max-width: 768px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; }
  .hero-main { height: 250px; }
  .banner-text { font-size: 40px; }
}
@media (max-width: 480px) {
  .hero-main { height: 180px; }
  .banner-text { font-size: 28px; }
  .banner-text span { font-size: 14px; }
  .section-title { font-size: 18px; }
  .view-all-btn { padding: 4px 10px; font-size: 12px; }
}
</style>