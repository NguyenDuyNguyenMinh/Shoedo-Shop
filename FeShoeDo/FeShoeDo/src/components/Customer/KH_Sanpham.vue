<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import KH_Navbar from '@/components/Shared/KH_Navbar.vue'
import Footer from '@/components/Shared/Footer.vue'

const router = useRouter()

const allProducts = [
  { id: 1,  name: 'Nike Air Max 270',      price: '2.890.000 đ', priceNum: 2890000, oldPrice: '3.200.000 đ', stock: 20, category: 'Thể thao', brand: 'Nike',        gender: 'Nam', image: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&q=80' },
  { id: 2,  name: 'Adidas Ultraboost 23',  price: '3.490.000 đ', priceNum: 3490000, oldPrice: null,          stock: 15, category: 'Chạy bộ',  brand: 'Adidas',       gender: 'Nam', image: 'https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=600&q=80' },
  { id: 3,  name: 'Air Jordan 1 Retro',    price: '4.190.000 đ', priceNum: 4190000, oldPrice: '5.200.000 đ', stock: 8,  category: 'Jordan',    brand: 'Jordan',       gender: 'Nam', image: 'https://images.unsplash.com/photo-1600185365926-3a2ce3cdb9eb?w=600&q=80' },
  { id: 4,  name: 'Vans Old Skool Classic',price: '1.290.000 đ', priceNum: 1290000, oldPrice: null,          stock: 50, category: 'Vans',      brand: 'Vans',         gender: 'Nữ',  image: 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=600&q=80' },
  { id: 5,  name: 'Converse Chuck 70 Hi',  price: '1.590.000 đ', priceNum: 1590000, oldPrice: null,          stock: 35, category: 'Converse',  brand: 'Converse',     gender: 'Nữ',  image: 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=600&q=80' },
  { id: 6,  name: 'New Balance 574 Core',  price: '1.990.000 đ', priceNum: 1990000, oldPrice: null,          stock: 18, category: 'Lifestyle', brand: 'New Balance',  gender: 'Nữ',  image: 'https://images.unsplash.com/photo-1584735175315-9d5df23be620?w=600&q=80' },
  { id: 7,  name: 'Nike React Infinity',   price: '2.690.000 đ', priceNum: 2690000, oldPrice: '2.990.000 đ', stock: 12, category: 'Chạy bộ',  brand: 'Nike',         gender: 'Nam', image: 'https://images.unsplash.com/photo-1539185441755-769473a23570?w=600&q=80' },
  { id: 8,  name: 'Puma RS-X Reinvention', price: '1.650.000 đ', priceNum: 1650000, oldPrice: null,          stock: 25, category: 'Lifestyle', brand: 'Puma',         gender: 'Nữ',  image: 'https://images.unsplash.com/photo-1491553895911-0055eca6402d?w=600&q=80' },
  { id: 9,  name: 'Reebok Classic Leather',price: '1.190.000 đ', priceNum: 1190000, oldPrice: '1.590.000 đ', stock: 30, category: 'Classic',   brand: 'Reebok',       gender: 'Nam', image: 'https://images.unsplash.com/photo-1556906781-9a412961a28c?w=600&q=80' },
  { id: 10, name: 'Balenciaga Triple S',   price: '5.990.000 đ', priceNum: 5990000, oldPrice: null,          stock: 5,  category: 'High-end',  brand: 'Balenciaga',   gender: 'Nữ',  image: 'https://images.unsplash.com/photo-1512374382149-233c42b6a83b?w=600&q=80' },
  { id: 11, name: 'Nike Air Force 1',      price: '2.490.000 đ', priceNum: 2490000, oldPrice: null,          stock: 22, category: 'Thể thao',  brand: 'Nike',         gender: 'Nam', image: 'https://images.unsplash.com/photo-1600269452121-4f2416e55c28?w=600&q=80' },
  { id: 12, name: 'Adidas Stan Smith',     price: '1.890.000 đ', priceNum: 1890000, oldPrice: '2.100.000 đ', stock: 40, category: 'Classic',   brand: 'Adidas',       gender: 'Nữ',  image: 'https://images.unsplash.com/photo-1543508282-6319a3e2621f?w=600&q=80' },
]

const categories = [
  'Tất cả', 'Thể thao', 'Chạy bộ', 'Jordan', 'Vans', 'Converse',
  'Lifestyle', 'Classic', 'High-end', 'Sneaker', 'Sandal', 'Boot',
  'Slip-on', 'Loafer', 'Nike', 'Adidas', 'Puma', 'Reebok', 'New Balance', 'Balenciaga',
]
const sortOptions = ['Mặc định', 'Giá tăng dần', 'Giá giảm dần', 'Mới nhất']

const selectedCategory = ref('Tất cả')
const selectedGender   = ref('Tất cả')
const onlyInStock      = ref(false)
const selectedSort     = ref('Mặc định')
const showCategoryMenu = ref(false)
const showSortMenu     = ref(false)

const categoryBtnRef = ref(null)
const sortBtnRef     = ref(null)
const categoryPos    = ref({ top: 0, left: 0 })
const sortPos        = ref({ top: 0, left: 0 })

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

const filteredProducts = computed(() => {
  let list = [...allProducts]
  if (selectedCategory.value !== 'Tất cả') list = list.filter(p => p.category === selectedCategory.value)
  if (selectedGender.value !== 'Tất cả')   list = list.filter(p => p.gender === selectedGender.value)
  if (onlyInStock.value)                    list = list.filter(p => p.stock > 0)
  if (selectedSort.value === 'Giá tăng dần')       list.sort((a, b) => a.priceNum - b.priceNum)
  else if (selectedSort.value === 'Giá giảm dần')  list.sort((a, b) => b.priceNum - a.priceNum)
  else if (selectedSort.value === 'Mới nhất')       list.sort((a, b) => b.id - a.id)
  return list
})

const selectCategory = (cat) => { selectedCategory.value = cat; showCategoryMenu.value = false }
const selectSort     = (sort) => { selectedSort.value = sort;   showSortMenu.value = false }
const setGender      = (g)    => { selectedGender.value = selectedGender.value === g ? 'Tất cả' : g }
const goToDetail     = (id)   => { router.push({ name: 'DetailProduct', params: { id } }) }
const closeDropdowns = ()     => { showCategoryMenu.value = false; showSortMenu.value = false }

onMounted(()   => document.addEventListener('click', closeDropdowns))
onUnmounted(() => document.removeEventListener('click', closeDropdowns))
</script>

<template>
  <div class="kh-sanpham">
    <KH_Navbar />

    <div class="page-body">

      <!-- HEADER -->
      <div class="page-header">
        <h1 class="page-title">Sản Phẩm</h1>
        <div class="gender-tabs">
          <button class="gender-btn" :class="{ active: selectedGender === 'Nam' }" @click.stop="setGender('Nam')">Nam</button>
          <button class="gender-btn" :class="{ active: selectedGender === 'Nữ' }"  @click.stop="setGender('Nữ')">Nữ</button>
        </div>
      </div>

      <!-- FILTER BAR -->
      <div class="filter-bar">

        <!-- Danh Mục button -->
        <button ref="categoryBtnRef" class="filter-btn" :class="{ 'fbn-active': showCategoryMenu }" @click.stop="toggleCategory">
          <span>{{ selectedCategory === 'Tất cả' ? 'Danh Mục' : selectedCategory }}</span>
          <i class="bi bi-chevron-down chevron" :class="{ rotated: showCategoryMenu }"></i>
        </button>

        <div class="filter-right">
          <!-- Còn Hàng -->
          <button class="filter-btn" :class="{ 'stock-active': onlyInStock }" @click.stop="onlyInStock = !onlyInStock">
            <i class="bi" :class="onlyInStock ? 'bi-check-circle-fill' : 'bi-circle'"></i>
            Còn Hàng
          </button>

          <!-- Xếp Theo button -->
          <button ref="sortBtnRef" class="filter-btn" :class="{ 'fbn-active': showSortMenu }" @click.stop="toggleSort">
            <span>{{ selectedSort }}</span>
            <i class="bi bi-chevron-down chevron" :class="{ rotated: showSortMenu }"></i>
          </button>
        </div>
      </div>

      <!-- RESULT COUNT -->
      <div class="result-count">Hiển thị <strong>{{ filteredProducts.length }}</strong> sản phẩm</div>

      <!-- PRODUCT GRID -->
      <div class="product-grid" v-if="filteredProducts.length > 0">
        <div v-for="p in filteredProducts" :key="p.id" class="pcard" @click="goToDetail(p.id)">
          <div class="pcard-img-wrap">
            <img :src="p.image" :alt="p.name" class="pcard-img" />
            <span v-if="p.oldPrice" class="pcard-badge">SALE</span>
            <span v-if="p.stock === 0" class="pcard-badge out">Hết hàng</span>
            <div class="pcard-overlay"><button class="pcard-quick-btn">Xem nhanh</button></div>
          </div>
          <div class="pcard-body">
            <div class="pcard-brand">{{ p.brand }}</div>
            <div class="pcard-name">{{ p.name }}</div>
            <div class="pcard-price-row">
              <span class="pcard-price">{{ p.price }}</span>
              <span v-if="p.oldPrice" class="pcard-old-price">{{ p.oldPrice }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- EMPTY STATE -->
      <div v-else class="empty-state">
        <i class="bi bi-search"></i>
        <p>Không tìm thấy sản phẩm phù hợp</p>
        <button class="btn-reset" @click.stop="selectedCategory = 'Tất cả'; selectedGender = 'Tất cả'; onlyInStock = false; selectedSort = 'Mặc định'">Xóa bộ lọc</button>
      </div>

    </div>

    <!-- TELEPORT dropdowns ra body để tránh bị parent cắt -->
    <Teleport to="body">
      <div v-if="showCategoryMenu" class="g-dropdown" :style="{ top: categoryPos.top + 'px', left: categoryPos.left + 'px' }" @click.stop>
        <div v-for="cat in categories" :key="cat" class="g-dropdown-item" :class="{ 'g-active': selectedCategory === cat }" @click="selectCategory(cat)">{{ cat }}</div>
      </div>
      <div v-if="showSortMenu" class="g-dropdown" :style="{ top: sortPos.top + 'px', left: sortPos.left + 'px' }" @click.stop>
        <div v-for="opt in sortOptions" :key="opt" class="g-dropdown-item" :class="{ 'g-active': selectedSort === opt }" @click="selectSort(opt)">{{ opt }}</div>
      </div>
    </Teleport>

    <Footer />
  </div>
</template>

<style scoped>
.kh-sanpham { font-family: 'Segoe UI', Arial, sans-serif; font-size: 13px; background: #f0f0f0; min-height: 100vh; }
.page-body  { padding: 0 16px 24px; max-width: 1400px; margin: 0 auto; }

.page-header { display: flex; align-items: center; justify-content: space-between; padding: 20px 0 12px; }
.page-title  { font-size: 28px; font-weight: 800; color: #111; letter-spacing: -0.5px; }

.gender-tabs { display: flex; border: 1.5px solid #bbb; border-radius: 6px; overflow: hidden; }
.gender-btn  { padding: 8px 22px; font-size: 13px; font-weight: 600; background: #fff; border: none; cursor: pointer; color: #444; transition: all 0.2s; }
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

<!-- Global (non-scoped) styles cho Teleported dropdown -->
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
.g-dropdown-item.g-active { background: #111; color: #fff; font-weight: 600; }
</style>