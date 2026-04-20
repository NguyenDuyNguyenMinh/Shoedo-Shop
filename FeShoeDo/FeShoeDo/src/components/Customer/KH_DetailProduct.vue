<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import KH_Navbar from '@/components/Shared/KH_Navbar.vue'
import Footer from '@/components/Shared/Footer.vue'
import ChatBox from '@/components/Shared/ChatBox.vue'
import api from '@/services/api.js'
import { useAuthStore } from '@/stores/auth'

const route  = useRoute()
const router = useRouter()

const apiProduct = ref(null)
const apiRelated = ref([])
const loading    = ref(true)
const error      = ref(null)

const selectedImage  = ref(0)
const selectedSize   = ref(null)
const selectedColor  = ref(null)
const quantity       = ref(1)
const addedToCart    = ref(false)

// State Đánh giá
const apiReviews   = ref([])
const thongKeSao   = ref({ 1:0, 2:0, 3:0, 4:0, 5:0 })
const reviews      = computed(() => apiReviews.value)

const fetchDanhGia = async (id) => {
  try {
    const { data } = await api.getDanhGia(id)
    if (data.success) {
      apiReviews.value = data.data || []
      const tk = { 1:0, 2:0, 3:0, 4:0, 5:0 }
      apiReviews.value.forEach(r => { if (r.sao >= 1 && r.sao <= 5) tk[r.sao]++ })
      thongKeSao.value = tk
    }
  } catch (e) {
    console.error('Lỗi tải đánh giá:', e)
    apiReviews.value = []
  }
}

// ── LOGIC CHIA SẺ TÍCH ĐIỂM ──
const shareProduct = () => {
  const authStore = useAuthStore()
  
  // 1. Kiểm tra đăng nhập
  if (!authStore.isAuthenticated || !authStore.user) {
    alert('Bạn cần đăng nhập để lấy link chia sẻ tích điểm nhé!')
    router.push('/auth/login')
    return
  }

  const refCode = authStore.user.maKH

  // 3. Tạo link chia sẻ đính kèm param ?ref=
  const currentUrl = window.location.origin + route.path
  const shareUrl = `${currentUrl}?ref=${refCode}`

  // 4. Copy vào Clipboard
  navigator.clipboard.writeText(shareUrl).then(() => {
    alert('Đã copy link chia sẻ! Gửi cho bạn bè để nhận điểm khi họ mua hàng nhé.')
  }).catch(err => {
    console.error('Lỗi copy link:', err)
    alert('Không thể copy link tự động. Bạn copy tay link này nhé: ' + shareUrl)
  })
}

const pageVisible = ref(false)

const resetUI = () => {
  selectedImage.value  = 0
  selectedSize.value   = null
  selectedColor.value  = null
  quantity.value       = 1
  addedToCart.value    = false
}

const fetchProduct = async (id) => {
  loading.value    = true
  error.value      = null
  apiProduct.value = null
  resetUI()
  try {
    const { data } = await api.getSanPhamChiTiet(id)
    if (data.success) {
      let pData = data.data
      
      if (pData && pData.chiTiets) {
        pData.chiTiets = pData.chiTiets.filter(sku => sku.trangThai === 'Hiển thị')
        
        pData.danhSachMau = [...new Set(pData.chiTiets.map(sku => sku.tenMau).filter(Boolean))]
        
        pData.danhSachSize = [...new Set(pData.chiTiets.map(sku => sku.coGiay).filter(s => s !== null && s !== undefined))].sort((a, b) => a - b)
        
        pData.danhSachHinhAnh = [...new Set(pData.chiTiets.map(sku => sku.hinhAnh).filter(Boolean))]
      }
      apiProduct.value = pData
    } else {
      error.value = data.message || 'Không thể tải sản phẩm'
    }
  } catch (e) {
    error.value = e.response?.status === 404
      ? 'Không tìm thấy sản phẩm'
      : 'Không thể kết nối đến server'
    console.error('Lỗi tải sản phẩm:', e)
  } finally {
    loading.value = false
  }
}

const fetchRelated = async (id) => {
  try {
    const { data } = await api.getSanPhamLienQuan(id)
    apiRelated.value = data.success ? (data.data || []) : []
  } catch (e) {
    apiRelated.value = []
    console.error('Lỗi tải sản phẩm liên quan:', e)
  }
}

const mauToHex = (tenMau) => {
  const map = {
    'Trắng': '#FFFFFF', 'Đen': '#000000', 'Đỏ': '#FF0000',
    'Xám': '#808080',   'Xanh biển': '#0000FF', 'Xanh Lá': '#008000',
    'Xanh lá': '#008000', 'Vàng': '#FFFF00', 'Nâu': '#8B4513',
    'Xanh navy': '#000080', 'Xanh biển đậm': '#000080',
  }
  return map[tenMau] || '#CCCCCC'
}

const formatPrice = (num) => {
  if (!num && num !== 0) return ''
  return new Intl.NumberFormat('vi-VN').format(num) + ' đ'
}

const getImageUrl = (hinhAnh) => {
  if (!hinhAnh) return 'https://placehold.co/600x600?text=No+Image'
  if (hinhAnh.startsWith('http')) return hinhAnh
  return `http://localhost:8080/images/${hinhAnh}`
}

// ── BUILD map: tenMau → danh sách hình ảnh riêng biệt từ chiTiets ──
const colorImageMap = computed(() => {
  const d = apiProduct.value
  if (!d || !d.chiTiets) return {}
  const map = {}
  d.chiTiets.forEach(sku => {
    if (!sku.tenMau || !sku.hinhAnh) return
    if (!map[sku.tenMau]) map[sku.tenMau] = []
    if (!map[sku.tenMau].includes(sku.hinhAnh)) {
      map[sku.tenMau].push(sku.hinhAnh)
    }
  })
  return map
})

// ── Hình hiển thị: nếu đã chọn màu → dùng hình của màu đó
//    chưa chọn → dùng toàn bộ danhSachHinhAnh ──
const currentImages = computed(() => {
  const d = apiProduct.value
  if (!d) return []
  if (selectedColor.value && colorImageMap.value[selectedColor.value]) {
    return colorImageMap.value[selectedColor.value].map(getImageUrl)
  }
  const all = (d.danhSachHinhAnh || []).map(getImageUrl)
  return all.length > 0 ? all : ['https://placehold.co/600x600?text=No+Image']
})

// ── Khi đổi màu → reset ảnh về ảnh đầu tiên của màu đó ──
// ── Khi đổi màu → reset ảnh và reset size ──
watch(selectedColor, () => {
  selectedImage.value = 0
  // Reset size để bắt buộc người dùng chọn lại size thuộc màu mới
  selectedSize.value = null
})

const product = computed(() => {
  const d = apiProduct.value
  if (!d) return null

  const colors = (d.danhSachMau || []).map(mau => ({
    name: mau,
    code: mauToHex(mau),
  }))

  const sizes      = d.danhSachSize || []
  const isFreesize = sizes.length === 0 || sizes.every(s => s === 0)

  // Lấy giá trị mặc định ban đầu
  let giaGoc      = Number(d.giaGoc || 0)
  let tongSoLuong = d.tongSoLuong || 0
  const khuyenMai = d.khuyenMai || 0

  // ── LOGIC ĐỘNG: LẤY GIÁ VÀ TỒN KHO THEO PHÂN LOẠI ĐÃ CHỌN ──
  if (selectedColor.value) {
    const matchingSkus = d.chiTiets.filter(sku => sku.tenMau === selectedColor.value)
    
    if (!isFreesize && selectedSize.value) {
      // 1. Đã chọn CẢ MÀU VÀ SIZE
      const exactSku = matchingSkus.find(sku => sku.coGiay === selectedSize.value)
      if (exactSku) {
        if (exactSku.donGia) giaGoc = Number(exactSku.donGia)
        tongSoLuong = exactSku.soLuong || 0
      }
    } else if (isFreesize && matchingSkus.length > 0) {
      // 2. Sản phẩm FREESIZE (chỉ cần chọn màu)
      if (matchingSkus[0].donGia) giaGoc = Number(matchingSkus[0].donGia)
      tongSoLuong = matchingSkus[0].soLuong || 0
    } else {
      // 3. MỚI CHỌN MÀU, chưa chọn size -> Hiện giá nhỏ nhất và tổng tồn kho của màu đó
      const minPriceForColor = Math.min(...matchingSkus.map(s => s.donGia).filter(p => p != null))
      if (minPriceForColor !== Infinity) giaGoc = minPriceForColor
      tongSoLuong = matchingSkus.reduce((sum, sku) => sum + (sku.soLuong || 0), 0)
    }
  }

  // Tính toán lại giá sau KM dựa trên giá gốc mới tìm được
  const giaSauKM = khuyenMai > 0 ? giaGoc * (100 - khuyenMai) / 100 : giaGoc
  const coKM     = khuyenMai > 0

  return {
    id:            d.maSP,
    name:          d.tenSP,
    brand:         (d.danhMucs?.length > 0) ? d.danhMucs[0] : 'ShoeDo',
    category:      (d.danhMucs?.length > 0) ? d.danhMucs[0] : '',
    allCategories: d.danhMucs || [],
    gioiTinh:      d.gioiTinh,
    khuyenMai:     khuyenMai,
    desc:          d.moTa || '',
    price:         formatPrice(coKM ? giaSauKM : giaGoc),
    priceNum:      coKM ? giaSauKM : giaGoc,
    oldPrice:      coKM ? formatPrice(giaGoc) : null,
    stock:         tongSoLuong, // Trả ra tồn kho động
    daBan:         d.daBan || 0,
    sizes,
    colors,
    isFreesize,
  }
})

const related = computed(() =>
  apiRelated.value.map(p => ({
    id:       p.maSP,
    name:     p.tenSP,
    image:    getImageUrl(p.hinhAnh),
    price:    formatPrice(p.khuyenMai > 0 ? p.giaSauKM : p.giaGoc),
    stock:    p.tongSoLuong || 0,
    daBan:    p.daBan || 0,
    category: p.tenDanhMuc || '',
  }))
)

const availableSizes = computed(() => {
  const d = apiProduct.value
  if (!d || !d.chiTiets) return []

  // Nếu chưa chọn màu, hiển thị toàn bộ size chung của sản phẩm
  if (!selectedColor.value) {
    return d.danhSachSize || []
  }

  // Lọc các SKU có màu khớp với màu đang chọn
  const skusForColor = d.chiTiets.filter(sku => sku.tenMau === selectedColor.value)

  // Lấy ra các size (coGiay), loại bỏ null và loại bỏ trùng lặp
  const sizes = skusForColor
    .map(sku => sku.coGiay)
    .filter(size => size !== null && size !== undefined)

  // Sắp xếp size từ nhỏ đến lớn
  return [...new Set(sizes)].sort((a, b) => a - b)
})

const averageRating = computed(() => {
  if (reviews.value.length === 0) return 0
  const sum = reviews.value.reduce((acc, r) => acc + (r.sao || 0), 0)
  return sum / reviews.value.length
})

const getRatingPercent = (star) => {
  if (reviews.value.length === 0) return 0
  return ((thongKeSao.value[star] || 0) / reviews.value.length) * 100
}

const getRatingCount = (star) => thongKeSao.value[star] || 0

const increaseQty = () => quantity.value++
const decreaseQty = () => { if (quantity.value > 1) quantity.value-- }

const addToCart = async () => {
  if (!selectedColor.value) { alert('Vui lòng chọn màu sắc!'); return }

  if (!product.value?.isFreesize && !selectedSize.value) {
    alert('Vui lòng chọn size!'); return
  }
  const sku = (apiProduct.value.chiTiets || []).find(s =>
    s.tenMau === selectedColor.value &&
    (product.value.isFreesize || s.coGiay === selectedSize.value)
  )
  if (!sku) { alert('Không tìm thấy SKU phù hợp!'); return }

  const currentRef = route.query.ref || localStorage.getItem('refCode');
  if (currentRef && currentRef !== 'null') {
    let refMap = JSON.parse(localStorage.getItem('refMap') || '{}');
    refMap[sku.maSKU] = currentRef; 
    localStorage.setItem('refMap', JSON.stringify(refMap));
  }

  try {
    await api.addToCart({ maSKU: sku.maSKU, soLuong: quantity.value })
    const authStore = useAuthStore()
    authStore.incrementCartCount()
    addedToCart.value = true
    setTimeout(() => addedToCart.value = false, 2000)
  } catch (e) {
    console.error('Lỗi thêm giỏ hàng:', e)
    alert('Không thể thêm vào giỏ hàng.')
  }
}

const buyNow = async () => {
  if (!selectedColor.value) { alert('Vui lòng chọn màu sắc!'); return }

  if (!product.value?.isFreesize && !selectedSize.value) {
    alert('Vui lòng chọn size!'); return
  }
  const sku = (apiProduct.value.chiTiets || []).find(s =>
    s.tenMau === selectedColor.value &&
    (product.value.isFreesize || s.coGiay === selectedSize.value)
  )
  if (!sku) { alert('Không tìm thấy SKU phù hợp!'); return }

  const currentRef = route.query.ref || localStorage.getItem('refCode');
  if (currentRef && currentRef !== 'null') {
    let refMap = JSON.parse(localStorage.getItem('refMap') || '{}');
    refMap[sku.maSKU] = currentRef; 
    localStorage.setItem('refMap', JSON.stringify(refMap));
  }

  try {
    // 1. Thêm vào giỏ hàng
    await api.addToCart({ maSKU: sku.maSKU, soLuong: quantity.value })
    const authStore = useAuthStore()
    authStore.incrementCartCount()

    // 2. Lấy giỏ hàng để tìm item vừa thêm (cần maGH và thông tin đầy đủ)
    const cartResp = await api.getCart()
    if (cartResp.data.success && cartResp.data.items) {
      const cartItems = cartResp.data.items
      // Tìm item trong giỏ hàng khớp với SKU vừa thêm
      const matchedItem = cartItems.find(item => item.maSKU === sku.maSKU)
      if (matchedItem) {
        // 3. Lưu vào sessionStorage để trang đặt hàng sử dụng
        sessionStorage.setItem('checkoutItems', JSON.stringify([matchedItem]))
        sessionStorage.setItem('checkoutItemIds', JSON.stringify([matchedItem.maGH]))
      }
    }

    // 4. Chuyển đến trang đặt hàng
    router.push({ name: 'Checkout' })
  } catch (e) {
    console.error('Lỗi mua ngay:', e)
    alert('Không thể thực hiện.')
  }
}

const goToDetail = (id) => {
  router.push({ name: 'DetailProduct', params: { id } })
}

const likeItem = (type, id) => console.log(`Liked ${type} ${id}`)

watch(() => route.params.id, (newId) => {
  if (newId) {
    fetchProduct(newId)
    fetchRelated(newId)
    fetchDanhGia(newId)
  }
})

onMounted(() => {
  const id = route.params.id

  if (route.query.ref) {
    localStorage.setItem('refCode', route.query.ref)
  }

  if (id) {
    fetchProduct(id)
    fetchRelated(id)
    fetchDanhGia(id)
  }

  // Trigger fade-up animation sau 1 tick
  requestAnimationFrame(() => {
    pageVisible.value = true
  })
})
</script>

<template>
  <div class="kh-detail">

    <KH_Navbar />

    <div class="page-body" :class="{ 'page-visible': pageVisible }">

      <!-- Loading -->
      <div v-if="loading" style="text-align:center; padding: 80px 0; color: #666;">
        <div style="width:40px;height:40px;border:4px solid #eee;border-top-color:#e53935;border-radius:50%;animation:spin 0.8s linear infinite;margin:0 auto 16px;"></div>
        <p>Đang tải sản phẩm...</p>
      </div>

      <!-- Error -->
      <div v-else-if="error" style="text-align:center; padding: 80px 0; color: #c62828;">
        <p>⚠️ {{ error }}</p>
        <button @click="fetchProduct(route.params.id)" style="margin-top:12px;padding:8px 24px;background:#e53935;color:#fff;border:none;border-radius:4px;cursor:pointer;">
          Thử lại
        </button>
      </div>

      <template v-else-if="product">

        <!-- Breadcrumb -->
<div class="breadcrumb-bar">
  <span class="bc-link" @click="router.push({ name: 'CustomerIndex' })">Trang chủ</span>
  <i class="bi bi-chevron-right bc-sep"></i>
  
  <span class="bc-link" @click="router.push({ name: 'Sanpham', query: { category: product.category } })">
    {{ product.category }}
  </span>
  
  <i class="bi bi-chevron-right bc-sep"></i>
  <span class="bc-current">{{ product.name }}</span>
</div>

        <!-- MAIN DETAIL -->
        <div class="detail-wrap">

          <!-- Cột trái: ảnh -->
          <div class="detail-images">
            <div class="main-img-wrap">
              <img :src="currentImages[selectedImage] || currentImages[0]" :alt="product.name" class="main-img" />
              <span v-if="product.oldPrice" class="sale-badge">SALE</span>
            </div>
            <div class="thumb-list">
              <div
                v-for="(img, i) in currentImages"
                :key="i"
                class="thumb"
                :class="{ active: selectedImage === i }"
                @click="selectedImage = i"
              >
                <img :src="img" :alt="'thumb-' + i" />
              </div>
            </div>
          </div>

          <!-- Cột phải: thông tin -->
          <div class="detail-info">

            <div class="product-brand">{{ product.brand }}</div>
            <h1 class="product-name">{{ product.name }}</h1>

            <div class="price-row">
              <span class="price-main">{{ product.price }}</span>
              <span v-if="product.oldPrice" class="price-old">{{ product.oldPrice }}</span>
              <span v-if="product.oldPrice" class="price-save">
                Tiết kiệm {{ Math.round((1 - product.priceNum / parseInt(product.oldPrice.replace(/\D/g, ''))) * 100) }}%
              </span>
            </div>

            <!-- ── META ROW ── -->
            <div class="meta-row">
              <span class="meta-item sold-count">
                <i class="bi bi-bag-check-fill"></i> Đã bán {{ product.daBan.toLocaleString('vi-VN') }}
              </span>
              <span class="meta-item"><i class="bi bi-tag"></i> {{ product.category }}</span>
              <span class="meta-item" :class="product.stock > 0 ? 'in-stock' : 'out-stock'">
                <i class="bi bi-circle-fill" style="font-size:8px;"></i>
                {{ product.stock > 0 ? 'Còn hàng' : 'Hết hàng' }}
              </span>
            </div>

            <!-- Điểm đánh giá -->
            <div class="rating-row" @click="() => document.querySelector('.product-info-container')?.scrollIntoView({ behavior: 'smooth' })">
              <div class="stars">
                <i v-for="star in 5" :key="star" class="bi"
                  :class="star <= averageRating ? 'bi-star-fill' : 'bi-star'"></i>
              </div>
              <span class="rating-text">{{ averageRating.toFixed(1) }} ({{ reviews.length }} đánh giá)</span>
            </div>

            <div class="divider"></div>

            <!-- CHỌN MÀU SẮC -->
            <div class="section-label">Màu Sắc <span class="required">*</span></div>
            <div class="color-grid">
              <div
                v-for="color in product.colors"
                :key="color.name"
                class="color-tag"
                :class="{ selected: selectedColor === color.name }"
                @click="selectedColor = color.name"
              >
                {{ color.name }}
              </div>
            </div>
            <p v-if="!selectedColor" class="size-hint">
              <i class="bi bi-info-circle"></i> Vui lòng chọn màu sắc
            </p>

            <div class="divider"></div>

            <template v-if="!product.isFreesize">
  <div class="section-label">Chọn Size <span class="required">*</span></div>
  <div class="size-grid">
    <div
      v-for="size in availableSizes"
      :key="size"
      class="size-btn"
      :class="{ selected: selectedSize === size }"
      @click="selectedSize = size"
    >
      {{ size }}
    </div>
  </div>
  <p v-if="availableSizes.length === 0 && selectedColor" class="size-hint">
    <i class="bi bi-x-circle"></i> Màu này hiện tại không có size nào.
  </p>
  <p v-else-if="!selectedSize" class="size-hint">
    <i class="bi bi-info-circle"></i> Vui lòng chọn size trước khi thêm vào giỏ
  </p>
  <div class="divider"></div>
</template>
            <template v-else>
              <div class="section-label" style="color:#888;">
                <i class="bi bi-check-circle" style="color:#2e7d32;"></i> Freesize
              </div>
              <div class="divider"></div>
            </template>

            <div class="section-label">Số Lượng</div>
            <div class="qty-row">
              <button class="qty-btn" @click="decreaseQty"><i class="bi bi-dash"></i></button>
              <span class="qty-val">{{ quantity }}</span>
              <button class="qty-btn" @click="increaseQty"><i class="bi bi-plus"></i></button>
            </div>

            <div class="divider"></div>

            <div class="action-row">
              <button class="btn-cart" :class="{ success: addedToCart }" @click="addToCart">
                <i :class="addedToCart ? 'bi bi-check-lg' : 'bi bi-cart-plus'"></i>
                {{ addedToCart ? 'Đã thêm vào giỏ!' : 'Thêm vào giỏ hàng' }}
              </button>
              <button class="btn-buy" @click="buyNow">
                <i class="bi bi-lightning-fill"></i> Mua ngay
              </button>
            </div>
<div class="share-row" style="margin-top: 12px;">
              <button class="btn-share" @click="shareProduct">
                <i class="bi bi-share-fill"></i> Chia sẻ để nhận điểm tích lũy
              </button>
            </div>
            <div class="divider"></div>

            <div class="policy-row">
              <div class="policy-item">
                <i class="bi bi-shield-check"></i>
                <span>Hàng chính hãng<br/><small>Cam kết 100%</small></span>
              </div>
            </div>

          </div>
        </div>

        <div class="product-info-container">
          
          <div class="info-section">
            <h3 class="section-title">Mô tả sản phẩm</h3>
            <div v-if="product.desc" class="product-desc-full" v-html="product.desc"></div>
            <p v-else class="product-desc-empty"><i class="bi bi-info-circle"></i> Sản phẩm chưa có mô tả.</p>
          </div>

          <div class="info-section">
            <h3 class="section-title">Thông số kỹ thuật</h3>
            <table class="specs-table">
              <tr>
                <td>Danh mục</td>
                <td>
                  <span v-if="product.allCategories && product.allCategories.length">{{ product.allCategories.join(', ') }}</span>
                  <span v-else class="no-data">—</span>
                </td>
              </tr>
              <tr>
                <td>Giới tính</td>
                <td>
                  <span v-if="product.gioiTinh === true">Nam</span>
                  <span v-else-if="product.gioiTinh === false">Nữ</span>
                  <span v-else class="no-data">Unisex</span>
                </td>
              </tr>
              <tr v-if="!product.isFreesize && product.sizes.length">
                <td>Size có sẵn</td>
                <td>{{ product.sizes.join(', ') }}</td>
              </tr>
              <tr v-if="product.colors.length">
                <td>Màu sắc</td>
                <td>{{ product.colors.map(c => c.name).join(', ') }}</td>
              </tr>
            </table>
          </div>

          <div class="info-section">
            <h3 class="section-title">Đánh giá sản phẩm <span class="title-count">({{ reviews.length }})</span></h3>
            
            <div class="reviews-summary">
              <div class="rating-overall">
                <div class="rating-score">{{ averageRating.toFixed(1) }}</div>
                <div class="rating-stars">
                  <i v-for="star in 5" :key="star" class="bi" :class="star <= Math.round(averageRating) ? 'bi-star-fill' : 'bi-star'"></i>
                </div>
                <div class="rating-count">{{ reviews.length }} lượt đánh giá</div>
              </div>
              <div class="rating-bars">
                <div v-for="rate in [5,4,3,2,1]" :key="rate" class="rating-bar-item">
                  <span class="rating-label">{{ rate }} <i class="bi bi-star-fill text-dark"></i></span>
                  <div class="rating-bar-bg">
                    <div class="rating-bar-fill" :style="{ width: getRatingPercent(rate) + '%' }"></div>
                  </div>
                  <span class="rating-count-num">{{ getRatingCount(rate) }}</span>
                </div>
              </div>
            </div>

            <div v-if="reviews.length === 0" class="empty-state">
              <i class="bi bi-star"></i>
              <p>Chưa có đánh giá nào cho sản phẩm này.</p>
            </div>

            <div v-else class="reviews-list">
              <div v-for="review in reviews" :key="review.maDG" class="review-item">
                <div class="review-header">
                  <div class="reviewer-avatar">
                    {{ review.tenKH ? review.tenKH.charAt(0).toUpperCase() : 'K' }}
                  </div>
                  <div class="reviewer-info">
                    <div class="reviewer-name">{{ review.tenKH || 'Khách hàng' }}</div>
                    <div class="review-rating">
                      <i v-for="star in 5" :key="star" class="bi" :class="star <= review.sao ? 'bi-star-fill' : 'bi-star'"></i>
                    </div>
                  </div>
                  <div class="review-date">
                    {{ review.ngayDG ? new Date(review.ngayDG).toLocaleDateString('vi-VN') : '' }}
                    <span class="verified-badge"><i class="bi bi-patch-check-fill"></i> Đã mua hàng</span>
                  </div>
                </div>
                <div v-if="review.danhGiaCT" class="review-content">{{ review.danhGiaCT }}</div>
                <div v-else class="review-content no-data">Khách hàng không để lại nhận xét.</div>
              </div>
            </div>
          </div>
        </div>

        <!-- SẢN PHẨM LIÊN QUAN -->
        <div class="related-wrap" v-if="related.length > 0">
          <div class="section-heading">
            SẢN PHẨM LIÊN QUAN
            <span class="count">({{ related.length }} sản phẩm)</span>
          </div>
          <div class="product-grid">
            <div v-for="p in related" :key="p.id" class="pcard" @click="goToDetail(p.id)">
              <div class="pcard-img-wrap">
                <img :src="p.image" :alt="p.name" class="pcard-img" />
              </div>
              <div class="pcard-body">
                <div class="pcard-name">{{ p.name }}</div>
                <div class="pcard-price">{{ p.price }}</div>
                <div class="pcard-meta"><i class="bi bi-box-seam"></i> Kho: {{ p.stock }}</div>
                <div class="pcard-meta sold-count"><i class="bi bi-bag-check-fill"></i> Đã bán: {{ p.daBan.toLocaleString('vi-VN') }}</div>
                <div class="pcard-meta"><i class="bi bi-tag"></i> {{ p.category }}</div>
              </div>
            </div>
          </div>
        </div>

      </template>

    </div>

    <Footer />
        <ChatBox />
  </div>
</template>

<style scoped>
.kh-detail {
  font-family: 'Segoe UI', Arial, sans-serif;
  font-size: 13px;
  background: #f0f0f0;
  min-height: 100vh;
}
.page-body { padding: 0 12px 12px; }

.breadcrumb-bar {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 0; font-size: 12px; color: #888;
}
.bc-link { cursor: pointer; transition: color 0.2s; }
.bc-link:hover { color: #e53935; }
.bc-sep { font-size: 9px; }
.bc-current { color: #222; font-weight: 600; }

.detail-wrap {
  background: #fff; border: 1px solid #ddd; padding: 24px;
  display: grid; grid-template-columns: 1fr 1fr; gap: 32px;
  border-radius: 16px;
  overflow: hidden;
}

.main-img-wrap {
  position: relative; aspect-ratio: 1/1;
  overflow: hidden; background: #f8f8f8; border: 1px solid #eee;
  border-radius: 16px;
  overflow: hidden;
}
.main-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.4s; }
.main-img-wrap:hover .main-img { transform: scale(1.05); }
.sale-badge {
  position: absolute; top: 12px; left: 12px;
  background: #e53935; color: #fff;
  font-size: 11px; font-weight: 700; padding: 4px 10px; letter-spacing: 1px;
}

.thumb-list { display: flex; gap: 8px; margin-top: 10px; flex-wrap: wrap; }
.thumb {
  width: 64px; height: 64px; border: 2px solid #eee;
  overflow: hidden; cursor: pointer; transition: border-color 0.2s; flex-shrink: 0;
}
.thumb.active { border-color: #111; }
.thumb:hover  { border-color: #888; }
.thumb img { width: 100%; height: 100%; object-fit: cover; }

.product-brand { font-size: 11px; color: #888; text-transform: uppercase; letter-spacing: 2px; margin-bottom: 4px; }
.product-name  { font-size: 22px; font-weight: 700; color: #111; margin-bottom: 12px; line-height: 1.3; }

.price-row { display: flex; align-items: baseline; gap: 10px; margin-bottom: 12px; }
.price-main { font-size: 22px; font-weight: 700; color: #e53935; }
.price-old  { font-size: 14px; color: #aaa; text-decoration: line-through; }
.price-save { font-size: 11px; font-weight: 700; background: #e53935; color: #fff; padding: 2px 8px; }

.meta-row { display: flex; gap: 14px; flex-wrap: wrap; margin-bottom: 4px; }
.meta-item { font-size: 11px; color: #888; display: flex; align-items: center; gap: 4px; }
.in-stock  { color: #2e7d32 !important; }
.out-stock { color: #e53935 !important; }

.sold-count { color: #f57c00 !important; font-weight: 600; }
.sold-spec  { color: #f57c00; font-weight: 600; display: flex; align-items: center; gap: 6px; }

.divider { border: none; border-top: 1px solid #eee; margin: 14px 0; }
.section-label { font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 10px; color: #333; }

.size-grid { display: flex; gap: 8px; flex-wrap: wrap; }
.size-btn {
  width: 44px; height: 36px; border: 1px solid #ddd; background: #fff;
  font-size: 12px; font-weight: 600; cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: all 0.2s;
  border-radius: 16px;
  overflow: hidden;
}
.size-btn:hover    { border-color: #111; }
.size-btn.selected { background: #111; color: #fff; border-color: #111; }
.size-hint { font-size: 11px; color: #e53935; margin-top: 8px; display: flex; align-items: center; gap: 4px; }

.qty-row { display: flex; align-items: center; }
.qty-btn {
  width: 34px; height: 34px; border: 1px solid #ddd; background: #f5f5f5;
  font-size: 14px; cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: background 0.2s;
}
.qty-btn:hover { background: #eee; }
.qty-val {
  width: 48px; height: 34px;
  border-top: 1px solid #ddd; border-bottom: 1px solid #ddd;
  display: flex; align-items: center; justify-content: center;
  font-weight: 600; font-size: 14px;
}

.action-row { display: flex; gap: 10px; }
.btn-cart {
  flex: 1; height: 44px; border: 2px solid #111; background: #fff; color: #111;
  font-weight: 700; font-size: 13px; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 8px;
  transition: all 0.2s; letter-spacing: 0.5px;
  border-radius: 16px;
  overflow: hidden;
}
.btn-cart:hover   { background: #111; color: #fff; }
.btn-cart.success { background: #2e7d32; border-color: #2e7d32; color: #fff; }

.btn-buy {
  flex: 1; height: 44px; border: none; background: #e53935; color: #fff;
  font-weight: 700; font-size: 13px; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 8px;
  transition: background 0.2s; letter-spacing: 0.5px;
  border-radius: 16px;
  overflow: hidden;
}
.btn-buy:hover { background: #c62828; }

.policy-row { display: flex; margin-top: 16px; border: 1px solid #eee; justify-content: center; border-radius: 12px; overflow: hidden; }
.policy-item {
  flex: 1; display: flex; align-items: center; gap: 10px; justify-content: center;
  padding: 12px; border-right: 1px solid #eee; font-size: 12px; color: #555;
}
.policy-item:last-child { border-right: none; }
.policy-item i { font-size: 20px; color: #111; flex-shrink: 0; }
.policy-item small { color: #aaa; font-size: 10px; }

.related-wrap { margin-top: 12px; background: #fff; border: 1px solid #ddd; padding: 16px; border-radius: 16px;
  overflow: hidden; }
.section-heading {
  font-size: 14px; font-weight: 700; text-transform: uppercase;
  letter-spacing: 0.5px; margin-bottom: 14px;
  display: flex; align-items: baseline; gap: 8px;
}
.count { font-size: 11px; color: #888; font-weight: 400; }

/* ── PRODUCT GRID: align-items stretch để các card cùng hàng đồng đều chiều cao ── */
.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
  align-items: stretch;
}

/* ── PCARD: flex column để body giãn đều, hình không bị lệch ── */
/* ── PCARD: flex column để body giãn đều, hình không bị lệch ── */
.pcard {
  border: 1px solid #e0e0e0;
  background: #fff;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
  display: flex;
  flex-direction: column;
  
  /* Thêm 2 dòng này để bo góc */
  border-radius: 12px; 
  overflow: hidden; 
}
.pcard:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.12); transform: translateY(-2px); }

/* ── ẢNH: aspect-ratio 1/1 cố định, không bị kéo dãn ── */
.pcard-img-wrap {
  width: 100%;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  background: #f8f8f8;
  flex-shrink: 0;
}
.pcard-img { width: 100%; height: 100%; object-fit: cover; display: block; transition: transform 0.3s; }
.pcard:hover .pcard-img { transform: scale(1.05); }

/* ── BODY: flex grow để lấp đầy phần còn lại của card ── */
.pcard-body {
  padding: 8px 10px 10px;
  display: flex;
  flex-direction: column;
  flex: 1;
}
.pcard-name {
  font-size: 12px; font-weight: 500; color: #222; margin-bottom: 4px;
  /* Giới hạn 2 dòng để các card đồng đều */
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden; line-height: 1.4; min-height: 2.8em;
}
.pcard-price { font-size: 12px; font-weight: 700; color: #e53935; margin-bottom: 5px; }
.pcard-meta { font-size: 10px; color: #888; display: flex; align-items: center; gap: 4px; margin-bottom: 2px; }
.pcard-meta i { font-size: 10px; }
.pcard-meta.sold-count { color: #f57c00 !important; font-weight: 600; }

.required { color: #e53935; margin-left: 4px; }

.color-grid { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 8px; }
.color-btn {
  width: 36px; height: 36px; border-radius: 50%; border: 2px solid #ddd;
  cursor: pointer; position: relative; transition: all 0.2s;
  display: flex; align-items: center; justify-content: center;
}
.color-btn:hover { transform: scale(1.1); border-color: #999; }
.color-btn.selected { border-color: #111; box-shadow: 0 0 0 2px #fff, 0 0 0 4px #111; }
.color-btn i { color: #fff; font-size: 18px; text-shadow: 0 0 2px rgba(0,0,0,0.5); }

/* ── Màu sắc hiển thị dạng chữ tag ── */
.color-tag {
  padding: 6px 14px;
  border: 1.5px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  color: #333;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}
.color-tag:hover { border-color: #111; color: #111; }
.color-tag.selected { background: #111; color: #fff; border-color: #111; font-weight: 700; }

.rating-row { display: flex; align-items: center; gap: 8px; margin: 8px 0; cursor: pointer; padding: 4px 0; }
.stars { display: flex; gap: 2px; }
/* Màu sao vàng mượt mà cho phần tổng quan */
.stars .bi-star-fill { color: #ffc107; font-size: 14px; }
.stars .bi-star { color: #ccc; font-size: 14px; }
.rating-text { color: #0066c0; font-size: 13px; font-weight: 500; }

/* ── GIAO DIỆN SCROLL DỌC (ĐEN TRẮNG SANG TRỌNG) ── */
.product-info-container {
  background: #fff; border: 1px solid #e0e0e0; margin-top: 16px; padding: 40px; border-radius: 16px; /* Bo góc khung lớn */
}
.info-section {
  margin-bottom: 48px; border-bottom: 1px solid #eee; padding-bottom: 40px;
}
.info-section:last-child { margin-bottom: 0; border-bottom: none; padding-bottom: 0; }

.section-title {
  font-size: 20px; font-weight: 800; color: #111; text-transform: uppercase; letter-spacing: 1px;
  margin-bottom: 24px; display: flex; align-items: center; gap: 8px;
}
.section-title::before {
  content: ''; display: block; width: 4px; height: 20px; background: #111;
}
.title-count { font-size: 16px; color: #888; font-weight: 500; }

/* Mô tả */
.product-desc-full { font-size: 14px; color: #333; line-height: 1.8; text-align: justify; }
.product-desc-full :deep(p) { margin-bottom: 16px; }
.product-desc-full :deep(img) { max-width: 100%; border-radius: 4px; margin: 16px 0; }
.product-desc-empty { font-size: 14px; color: #888; font-style: italic; }

/* Thông số */
.specs-table { width: 100%; max-width: 600px; border-collapse: collapse; border: 1px solid #eee; }
.specs-table tr { border-bottom: 1px solid #eee; }
.specs-table td { padding: 14px 16px; font-size: 14px; }
.specs-table td:first-child { width: 150px; color: #666; font-weight: 600; background: #fafafa; border-right: 1px solid #eee; }
.specs-table td:last-child { color: #111; font-weight: 500; }

/* Thống kê đánh giá */
.reviews-summary {
  display: flex; gap: 48px; padding: 24px; background: #fafafa; border: 1px solid #eee; border-radius: 12px; /* Bo góc khối thống kê */
  margin-bottom: 32px;
}
.rating-overall { text-align: center; display: flex; flex-direction: column; justify-content: center; }
.rating-score { font-size: 56px; font-weight: 800; color: #111; line-height: 1; }
.rating-stars { margin: 8px 0; }
/* Màu sao vàng cho thống kê */
.rating-stars .bi-star-fill { color: #ffc107; font-size: 18px; margin: 0 2px; }
.rating-stars .bi-star { color: #ccc; font-size: 18px; margin: 0 2px; }
.rating-count { font-size: 13px; color: #666; }

.rating-bars { flex: 1; display: flex; flex-direction: column; gap: 10px; justify-content: center; }
.rating-bar-item { display: flex; align-items: center; gap: 12px; }
.rating-label { width: 50px; font-size: 13px; font-weight: 600; color: #111; display: flex; justify-content: space-between; }
/* Sao nhỏ ở thanh ngang màu vàng */
.rating-label i.bi-star-fill { color: #ffc107 !important; }
.rating-bar-bg { flex: 1; height: 6px; background: #e0e0e0; border-radius: 10px; overflow: hidden; }
.rating-bar-fill { height: 100%; background: #111; border-radius: 10px; transition: width 0.4s ease; }
.rating-count-num { width: 30px; font-size: 13px; color: #666; text-align: right; }

/* Danh sách đánh giá */
.reviews-list { display: flex; flex-direction: column; gap: 24px; }
.review-item { padding-bottom: 24px; border-bottom: 1px solid #f0f0f0; }
.review-item:last-child { border-bottom: none; padding-bottom: 0; }

.review-header { display: flex; align-items: flex-start; gap: 16px; margin-bottom: 12px; }
.reviewer-avatar {
  width: 44px; height: 44px; border-radius: 50%; background: #111; color: #fff;
  font-size: 18px; font-weight: 700; display: flex; align-items: center; justify-content: center;
}
.reviewer-info { flex: 1; }
.reviewer-name { font-size: 15px; font-weight: 700; color: #111; margin-bottom: 4px; }
/* Sao đánh giá của từng khách hàng màu vàng */
.review-rating .bi-star-fill { color: #ffc107; font-size: 12px; margin-right: 2px; }
.review-rating .bi-star { color: #ccc; font-size: 12px; margin-right: 2px; }

.review-date { font-size: 12px; color: #888; text-align: right; display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.verified-badge { color: #111; font-weight: 600; font-size: 11px; background: #f5f5f5; padding: 4px 8px; border-radius: 4px; }
.verified-badge i { color: #2e7d32; }

.review-content { font-size: 14px; color: #444; line-height: 1.6; }
.no-data { font-style: italic; color: #aaa; }

.empty-state { text-align: center; padding: 40px 0; color: #888; }
.empty-state i { font-size: 32px; color: #ccc; margin-bottom: 12px; display: block; }

.share-row { display: flex; }
.btn-share {
  flex: 1; height: 40px; border: 1.5px dashed #f57c00; background: #fff8f0; color: #f57c00;
  font-weight: 600; font-size: 13px; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 8px;
  transition: all 0.2s; border-radius: 16px;
}
.btn-share:hover { background: #f57c00; color: #fff; border-style: solid; }

@media (max-width: 768px) {
  .detail-wrap { grid-template-columns: 1fr; gap: 16px; padding: 16px; }
  .product-grid { grid-template-columns: repeat(2, 1fr); }
  .policy-row { flex-direction: column; }
  .policy-item { border-right: none; border-bottom: 1px solid #eee; }
  .policy-item:last-child { border-bottom: none; }
  
  .product-info-container { padding: 20px; }
  .reviews-summary { flex-direction: column; gap: 24px; }
}

@keyframes spin { to { transform: rotate(360deg); } }

/* ── FADE UP khi vào trang ── */
.page-body {
  opacity: 0;
  transform: translateY(24px);
  transition: opacity 0.55s cubic-bezier(0.16, 1, 0.3, 1),
              transform 0.55s cubic-bezier(0.16, 1, 0.3, 1);
}
.page-body.page-visible {
  opacity: 1;
  transform: translateY(0);
}
</style>