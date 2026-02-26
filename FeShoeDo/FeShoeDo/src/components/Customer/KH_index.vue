<script setup>
import { ref } from 'vue'
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

// ── DANH SÁCH SẢN PHẨM ──
const products = ref([
  { id: 1,  name: 'Nike Air Max 270',       price: '2.890.000 đ', stock: 20, category: 'Thể thao',  image: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=300&q=80' },
  { id: 2,  name: 'Adidas Ultraboost 23',   price: '3.490.000 đ', stock: 15, category: 'Chạy bộ',   image: 'https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=300&q=80' },
  { id: 3,  name: 'Air Jordan 1 Retro',     price: '4.190.000 đ', stock: 8,  category: 'Jordan',    image: 'https://images.unsplash.com/photo-1600185365926-3a2ce3cdb9eb?w=300&q=80' },
  { id: 4,  name: 'Vans Old Skool Classic', price: '1.290.000 đ', stock: 50, category: 'Vans',      image: 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=300&q=80' },
  { id: 5,  name: 'Converse Chuck 70 Hi',   price: '1.590.000 đ', stock: 35, category: 'Converse',  image: 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=300&q=80' },
  { id: 6,  name: 'New Balance 574 Core',   price: '1.990.000 đ', stock: 18, category: 'Lifestyle', image: 'https://images.unsplash.com/photo-1584735175315-9d5df23be620?w=300&q=80' },
  { id: 7,  name: 'Nike React Infinity',    price: '2.690.000 đ', stock: 12, category: 'Chạy bộ',   image: 'https://images.unsplash.com/photo-1539185441755-769473a23570?w=300&q=80' },
  { id: 8,  name: 'Puma RS-X Reinvention',  price: '1.650.000 đ', stock: 25, category: 'Lifestyle', image: 'https://images.unsplash.com/photo-1491553895911-0055eca6402d?w=300&q=80' },
  { id: 9,  name: 'Reebok Classic Leather', price: '1.190.000 đ', stock: 30, category: 'Classic',   image: 'https://images.unsplash.com/photo-1556906781-9a412961a28c?w=300&q=80' },
  { id: 10, name: 'Balenciaga Triple S',    price: '5.990.000 đ', stock: 5,  category: 'High-end',  image: 'https://images.unsplash.com/photo-1512374382149-233c42b6a83b?w=300&q=80' },
])

const goToDetail = (id) => {
  router.push({ name: 'DetailProduct', params: { id } })
}
</script>

<template>
  <div class="kh-index">

    <KH_Navbar />

    <!-- ════════ HERO BANNER ════════ -->
    <div class="hero-wrap">
      <div class="hero-main" :style="{ background: slides[currentSlide].bg }">
        <div class="banner-bg"></div>
        <button class="arrow-btn left" @click="prevSlide">
          <i class="bi bi-chevron-left"></i>
        </button>
        <div class="banner-text">
          {{ slides[currentSlide].line1 }}<br />
          <span>{{ slides[currentSlide].line2 }}</span>
        </div>
        <button class="arrow-btn right" @click="nextSlide">
          <i class="bi bi-chevron-right"></i>
        </button>
      </div>
      <div class="hero-promos">
        <div class="promo-box"><span class="promo-label">MIỄN PHÍ SHIP</span></div>
        <div class="promo-box"><span class="promo-label">VOUCHER 100K</span></div>
      </div>
    </div>

    <!-- ════════ SẢN PHẨM ════════ -->
    <div class="products-wrap">
      <div class="section-heading">
        TẤT CẢ SẢN PHẨM
        <span class="count">({{ products.length }} SẢN PHẨM)</span>
      </div>
      <div class="product-grid">
        <div
          v-for="product in products"
          :key="product.id"
          class="pcard"
          @click="goToDetail(product.id)"
        >
          <div class="pcard-img-wrap">
            <img :src="product.image" :alt="product.name" class="pcard-img" />
          </div>
          <div class="pcard-body">
            <div class="pcard-name">{{ product.name }}</div>
            <div class="pcard-price">{{ product.price }}</div>
            <div class="pcard-meta"><i class="bi bi-box-seam"></i> Kho: {{ product.stock }}</div>
            <div class="pcard-meta"><i class="bi bi-tag"></i> {{ product.category }}</div>
          </div>
        </div>
      </div>
    </div>

    <Footer />

  </div>
</template>

<style scoped>
.kh-index {
  font-family: 'Segoe UI', Arial, sans-serif;
  font-size: 13px;
  background: #f0f0f0;
  min-height: 100vh;
}

/* ════════ HERO ════════ */
.hero-wrap {
  display: flex;
  gap: 8px;
  height: 200px;
  margin: 12px 12px 0;
}
.hero-main {
  flex: 0 0 62%;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.4s;
}
.banner-bg {
  position: absolute; inset: 0;
  background: radial-gradient(ellipse at 30% 50%, rgba(255,255,255,0.03) 0%, transparent 60%);
}
.banner-text {
  font-size: 64px;
  font-weight: 900;
  color: #e53935;
  text-transform: uppercase;
  line-height: 1;
  text-align: center;
  position: relative;
  z-index: 2;
  letter-spacing: -1px;
  font-family: Impact, 'Arial Black', sans-serif;
  text-shadow: 0 0 60px rgba(229,57,53,0.5);
}
.banner-text span { display: block; color: #fff; font-size: 40px; letter-spacing: 6px; }
.arrow-btn {
  position: absolute;
  top: 50%; transform: translateY(-50%);
  background: rgba(255,255,255,0.15);
  border: none; color: #fff;
  width: 28px; height: 28px;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; cursor: pointer; z-index: 5;
  border-radius: 2px; transition: background 0.2s;
}
.arrow-btn:hover { background: rgba(255,255,255,0.3); }
.arrow-btn.left  { left: 8px; }
.arrow-btn.right { right: 8px; }
.hero-promos { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.promo-box {
  flex: 1; background: #111;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: background 0.2s;
}
.promo-box:hover { background: #1e1e1e; }
.promo-label { color: #fff; font-weight: 700; font-size: 16px; letter-spacing: 1px; text-transform: uppercase; }

/* ════════ PRODUCTS ════════ */
.products-wrap {
  margin: 12px;
  background: #fff;
  border: 1px solid #ddd;
  padding: 16px;
}
.section-heading {
  font-size: 14px; font-weight: 700;
  text-transform: uppercase; letter-spacing: 0.5px;
  margin-bottom: 14px;
  display: flex; align-items: baseline; gap: 8px;
}
.count { font-size: 11px; color: #888; font-weight: 400; }
.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
}
.pcard {
  border: 1px solid #e0e0e0;
  background: #fff;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}
.pcard:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.12); transform: translateY(-2px); }
.pcard-img-wrap { width: 100%; aspect-ratio: 1/1; overflow: hidden; background: #f8f8f8; }
.pcard-img { width: 100%; height: 100%; object-fit: cover; display: block; transition: transform 0.3s; }
.pcard:hover .pcard-img { transform: scale(1.05); }
.pcard-body { padding: 8px 10px 10px; }
.pcard-name { font-size: 12px; font-weight: 500; color: #222; margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.pcard-price { font-size: 12px; font-weight: 700; color: #e53935; margin-bottom: 5px; }
.pcard-meta { font-size: 10px; color: #888; display: flex; align-items: center; gap: 4px; margin-bottom: 2px; }
.pcard-meta i { font-size: 10px; }

@media (max-width: 992px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 576px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); }
  .hero-wrap { height: 130px; }
  .banner-text { font-size: 36px; }
  .banner-text span { font-size: 24px; }
}
</style>