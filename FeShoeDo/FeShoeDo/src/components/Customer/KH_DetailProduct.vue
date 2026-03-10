<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import KH_Navbar from '@/components/Shared/KH_Navbar.vue'
import Footer from '@/components/Shared/Footer.vue'
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
const activeTab      = ref('description')
const newComment     = ref('')
const newReviewText  = ref('')
const newRating      = ref(5)
const showReviewForm = ref(false)

const apiReviews   = ref([])
const thongKeSao   = ref({ 1:0, 2:0, 3:0, 4:0, 5:0 })
const apiComments  = ref([])

const reviews  = computed(() => apiReviews.value)
const comments = computed(() => apiComments.value)

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

const tenNguoiBinhLuan = ref('')
const submitComment = () => {
  if (!newComment.value.trim()) { alert('Vui lòng nhập nội dung bình luận'); return }
  const now = new Date()
  apiComments.value.unshift({
    id:       Date.now(),
    userName: tenNguoiBinhLuan.value.trim() || 'Khách',
    content:  newComment.value.trim(),
    date:     now.toLocaleDateString('vi-VN'),
    replies:  [],
  })
  newComment.value        = ''
  tenNguoiBinhLuan.value  = ''
}

const resetUI = () => {
  selectedImage.value  = 0
  selectedSize.value   = null
  selectedColor.value  = null
  quantity.value       = 1
  addedToCart.value    = false
  activeTab.value      = 'description'
  newComment.value     = ''
  newReviewText.value  = ''
}

const fetchProduct = async (id) => {
  loading.value    = true
  error.value      = null
  apiProduct.value = null
  resetUI()
  try {
    const { data } = await api.getSanPhamChiTiet(id)
    if (data.success) {
      apiProduct.value = data.data
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

const product = computed(() => {
  const d = apiProduct.value
  if (!d) return null

  const images = (d.danhSachHinhAnh || []).map(getImageUrl)
  if (images.length === 0) images.push('https://placehold.co/600x600?text=No+Image')

  const colors = (d.danhSachMau || []).map(mau => ({
    name: mau,
    code: mauToHex(mau),
  }))

  const giaGoc   = Number(d.giaGoc   || 0)
  const giaSauKM = Number(d.giaSauKM || 0)
  const coKM     = d.khuyenMai > 0

  const sizes      = d.danhSachSize || []
  const isFreesize = sizes.length === 0 || sizes.every(s => s === 0)

  return {
    id:            d.maSP,
    name:          d.tenSP,
    brand:         (d.danhMucs?.length > 0) ? d.danhMucs[0] : 'ShoeDo',
    category:      (d.danhMucs?.length > 0) ? d.danhMucs[0] : '',
    allCategories: d.danhMucs || [],
    gioiTinh:      d.gioiTinh,
    khuyenMai:     d.khuyenMai || 0,
    desc:          d.moTa || '',
    price:         formatPrice(coKM ? giaSauKM : giaGoc),
    priceNum:      coKM ? giaSauKM : giaGoc,
    oldPrice:      coKM ? formatPrice(giaGoc) : null,
    stock:         d.tongSoLuong || 0,
    daBan:         d.daBan || 0,   // ← THÊM MỚI
    images,
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
    daBan:    p.daBan || 0,        // ← THÊM MỚI
    category: p.tenDanhMuc || '',
  }))
)

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
  if (!product.value?.isFreesize && !selectedSize.value) {
    alert('Vui lòng chọn size!'); return
  }
  if (!selectedColor.value) { alert('Vui lòng chọn màu sắc!'); return }
  try {
    // Tìm maSKU phù hợp dựa trên sản phẩm hiện tại (dùng product.id làm maSKU)
    const response = await api.addToCart({
      maSKU: product.value.id,
      soLuong: quantity.value
    })
    if (response.data.success) {
      addedToCart.value = true
      setTimeout(() => addedToCart.value = false, 2000)
      // Update cart count in auth store
      const authStore = useAuthStore()
      if (response.data.cartCount != null) {
        authStore.cartCount = response.data.cartCount
      } else {
        authStore.updateCartCount()
      }
    } else {
      alert(response.data.message || 'Không thể thêm vào giỏ hàng')
    }
  } catch (error) {
    console.error('Add to cart error:', error)
    alert(error.response?.data?.message || 'Lỗi khi thêm vào giỏ hàng')
  }
}

const buyNow = async () => {
  if (!selectedSize.value) { alert('Vui lòng chọn size!'); return }
  if (!selectedColor.value) { alert('Vui lòng chọn màu sắc!'); return }
  try {
    const response = await api.addToCart({
      maSKU: product.value.id,
      soLuong: quantity.value
    })
    if (response.data.success) {
      const authStore = useAuthStore()
      authStore.updateCartCount()
      router.push('/customer/cart')
    } else {
      alert(response.data.message || 'Không thể thêm vào giỏ hàng')
    }
  } catch (error) {
    console.error('Buy now error:', error)
    alert(error.response?.data?.message || 'Lỗi khi mua ngay')
  }
}

const goToDetail = (id) => {
  router.push({ name: 'DetailProduct', params: { id } })
}

const submitReview = () => {
  showReviewForm.value = false
  newReviewText.value  = ''
  alert('Cảm ơn bạn đã đánh giá sản phẩm!')
}

const likeItem = (type, id) => console.log(`Liked ${type} ${id}`)
const setTab = (tab) => { activeTab.value = tab }

watch(() => route.params.id, (newId) => {
  if (newId) {
    fetchProduct(newId)
    fetchRelated(newId)
    fetchDanhGia(newId)
    apiComments.value = []
  }
})

onMounted(() => {
  const id = route.params.id
  if (id) {
    fetchProduct(id)
    fetchRelated(id)
    fetchDanhGia(id)
  }
})
</script>

<template>
  <div class="kh-detail">

    <KH_Navbar />

    <div class="page-body">

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
          <span class="bc-link">{{ product.category }}</span>
          <i class="bi bi-chevron-right bc-sep"></i>
          <span class="bc-current">{{ product.name }}</span>
        </div>

        <!-- MAIN DETAIL -->
        <div class="detail-wrap">

          <!-- Cột trái: ảnh -->
          <div class="detail-images">
            <div class="main-img-wrap">
              <img :src="product.images[selectedImage]" :alt="product.name" class="main-img" />
              <span v-if="product.oldPrice" class="sale-badge">SALE</span>
            </div>
            <div class="thumb-list">
              <div
                v-for="(img, i) in product.images"
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

            <!-- ── META ROW: thêm "Đã bán" ── -->
            <div class="meta-row">
              <span class="meta-item"><i class="bi bi-box-seam"></i> Còn {{ product.stock }} đôi</span>
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
            <div class="rating-row" @click="setTab('reviews')">
              <div class="stars">
                <i v-for="star in 5" :key="star" class="bi"
                  :class="star <= averageRating ? 'bi-star-fill' : 'bi-star'"></i>
              </div>
              <span class="rating-text">{{ averageRating.toFixed(1) }} ({{ reviews.length }} đánh giá)</span>
              <span class="comment-count"><i class="bi bi-chat"></i> {{ comments.length }} bình luận</span>
            </div>

            <div class="divider"></div>

            <!-- CHỌN MÀU SẮC -->
            <div class="section-label">Màu Sắc <span class="required">*</span></div>
            <div class="color-grid">
              <div
                v-for="color in product.colors"
                :key="color.code"
                class="color-btn"
                :class="{ selected: selectedColor === color.code }"
                :style="{ backgroundColor: color.code }"
                @click="selectedColor = color.code"
                :title="color.name"
              >
                <i v-if="selectedColor === color.code" class="bi bi-check-lg"></i>
              </div>
            </div>
            <p v-if="!selectedColor" class="size-hint">
              <i class="bi bi-info-circle"></i> Vui lòng chọn màu sắc
            </p>

            <div class="divider"></div>

            <!-- SIZE -->
            <template v-if="!product.isFreesize">
              <div class="section-label">Chọn Size <span class="required">*</span></div>
              <div class="size-grid">
                <div
                  v-for="size in product.sizes"
                  :key="size"
                  class="size-btn"
                  :class="{ selected: selectedSize === size }"
                  @click="selectedSize = size"
                >
                  {{ size }}
                </div>
              </div>
              <p v-if="!selectedSize" class="size-hint">
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
              <button class="btn-buy">
                <i class="bi bi-lightning-fill"></i> Mua ngay
              </button>
            </div>

            <div class="divider"></div>

            <div class="policy-row">
              <div class="policy-item">
                <i class="bi bi-truck"></i>
                <span>Miễn phí ship<br/><small>Đơn từ 500k</small></span>
              </div>
              <div class="policy-item">
                <i class="bi bi-arrow-counterclockwise"></i>
                <span>Đổi trả 30 ngày<br/><small>Miễn phí đổi trả</small></span>
              </div>
              <div class="policy-item">
                <i class="bi bi-shield-check"></i>
                <span>Hàng chính hãng<br/><small>Cam kết 100%</small></span>
              </div>
            </div>

          </div>
        </div>

        <!-- TABS -->
        <div class="tabs-wrap">
          <div class="tabs-header">
            <div class="tab-item" :class="{ active: activeTab === 'description' }" @click.stop="setTab('description')">
              Mô tả sản phẩm
            </div>
            <div class="tab-item" :class="{ active: activeTab === 'reviews' }" @click.stop="setTab('reviews')">
              Đánh giá <span class="tab-count">{{ reviews.length }}</span>
            </div>
            <div class="tab-item" :class="{ active: activeTab === 'comments' }" @click.stop="setTab('comments')">
              Bình luận <span class="tab-count">{{ comments.length }}</span>
            </div>
          </div>

          <div class="tab-content">
            <!-- Tab Mô tả -->
            <div v-if="activeTab === 'description'" :key="'description'" class="tab-pane">
              <div v-if="product.desc" class="product-desc-full" v-html="product.desc"></div>
              <p v-else class="product-desc-empty">
                <i class="bi bi-info-circle"></i> Sản phẩm chưa có mô tả.
              </p>

              <!-- Thông số kỹ thuật: thêm dòng Đã bán -->
              <div class="product-specs">
                <h4>Thông số kỹ thuật</h4>
                <table class="specs-table">
                  <tr>
                    <td>Danh mục</td>
                    <td>
                      <span v-if="product.allCategories && product.allCategories.length">
                        {{ product.allCategories.join(', ') }}
                      </span>
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
                  <tr>
                    <td>Tình trạng</td>
                    <td :class="product.stock > 0 ? 'in-stock' : 'out-stock'">
                      {{ product.stock > 0 ? 'Còn hàng (' + product.stock + ' đôi)' : 'Hết hàng' }}
                    </td>
                  </tr>
                  <!-- ── THÊM MỚI: Đã bán ── -->
                  <tr>
                    <td>Đã bán</td>
                    <td class="sold-spec">
                      <i class="bi bi-bag-check-fill"></i>
                      {{ product.daBan.toLocaleString('vi-VN') }} sản phẩm
                    </td>
                  </tr>
                  <tr v-if="!product.isFreesize && product.sizes.length">
                    <td>Size có sẵn</td>
                    <td>{{ product.sizes.join(', ') }}</td>
                  </tr>
                  <tr v-if="product.isFreesize">
                    <td>Size</td>
                    <td>Freesize</td>
                  </tr>
                  <tr v-if="product.colors.length">
                    <td>Màu sắc</td>
                    <td>{{ product.colors.map(c => c.name).join(', ') }}</td>
                  </tr>
                  <tr v-if="product.khuyenMai > 0">
                    <td>Khuyến mãi</td>
                    <td style="color:#e53935;font-weight:600;">Giảm {{ product.khuyenMai }}%</td>
                  </tr>
                </table>
              </div>
            </div>

            <!-- Tab Đánh giá -->
            <div v-if="activeTab === 'reviews'" :key="'reviews'" class="tab-pane">
              <div class="reviews-summary">
                <div class="rating-overall">
                  <div class="rating-score">{{ averageRating.toFixed(1) }}</div>
                  <div class="rating-stars">
                    <i v-for="star in 5" :key="star" class="bi"
                      :class="star <= Math.round(averageRating) ? 'bi-star-fill' : 'bi-star'"></i>
                  </div>
                  <div class="rating-count">{{ reviews.length }} đánh giá</div>
                </div>
                <div class="rating-bars">
                  <div v-for="rate in [5,4,3,2,1]" :key="rate" class="rating-bar-item">
                    <span class="rating-label">{{ rate }} sao</span>
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
                <small>Chỉ khách hàng đã mua hàng mới có thể đánh giá.</small>
              </div>

              <div v-else class="reviews-list">
                <div v-for="review in reviews" :key="review.maDG" class="review-item">
                  <div class="review-header">
                    <div class="reviewer-avatar-text">
                      {{ review.tenKH ? review.tenKH.charAt(0).toUpperCase() : 'K' }}
                    </div>
                    <div class="reviewer-info">
                      <div class="reviewer-name">{{ review.tenKH || 'Khách hàng' }}</div>
                      <div class="review-rating">
                        <i v-for="star in 5" :key="star" class="bi"
                          :class="star <= review.sao ? 'bi-star-fill' : 'bi-star'"></i>
                      </div>
                    </div>
                    <div class="review-date">
                      {{ review.ngayDG ? new Date(review.ngayDG).toLocaleDateString('vi-VN') : '' }}
                    </div>
                  </div>
                  <div v-if="review.danhGiaCT" class="review-content">{{ review.danhGiaCT }}</div>
                  <div v-else class="review-content no-data" style="font-style:italic;">Không có nhận xét.</div>
                  <div class="verified-badge">
                    <i class="bi bi-patch-check-fill"></i> Đã mua hàng
                  </div>
                </div>
              </div>
            </div>

            <!-- Tab Bình luận -->
            <div v-if="activeTab === 'comments'" :key="'comments'" class="tab-pane">
              <div class="comments-input">
                <div class="comment-name-row">
                  <input
                    v-model="tenNguoiBinhLuan"
                    type="text"
                    placeholder="Tên của bạn (không bắt buộc)"
                    class="comment-name-input"
                    maxlength="50"
                  />
                </div>
                <textarea
                  v-model="newComment"
                  placeholder="Bạn có câu hỏi hoặc muốn nhắn gì cho shop? Hãy để lại bình luận..."
                  rows="3"
                ></textarea>
                <button class="btn-submit-comment" @click="submitComment">
                  <i class="bi bi-send"></i> Gửi bình luận
                </button>
              </div>

              <div v-if="comments.length === 0" class="empty-state">
                <i class="bi bi-chat-square-text"></i>
                <p>Chưa có bình luận nào. Hãy là người đầu tiên!</p>
              </div>

              <div v-else class="comments-list">
                <div v-for="comment in comments" :key="comment.id" class="comment-item">
                  <div class="comment-header">
                    <div class="commenter-avatar-text">
                      {{ comment.userName ? comment.userName.charAt(0).toUpperCase() : 'K' }}
                    </div>
                    <div class="commenter-info">
                      <div class="commenter-name">{{ comment.userName || 'Khách' }}</div>
                      <div class="comment-date">{{ comment.date }}</div>
                    </div>
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                </div>
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
                <!-- ── THÊM MỚI: Đã bán trong card liên quan ── -->
                <div class="pcard-meta sold-count"><i class="bi bi-bag-check-fill"></i> Đã bán: {{ p.daBan.toLocaleString('vi-VN') }}</div>
                <div class="pcard-meta"><i class="bi bi-tag"></i> {{ p.category }}</div>
              </div>
            </div>
          </div>
        </div>

      </template>

    </div>

    <Footer />
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
}

.main-img-wrap {
  position: relative; aspect-ratio: 1/1;
  overflow: hidden; background: #f8f8f8; border: 1px solid #eee;
}
.main-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.4s; }
.main-img-wrap:hover .main-img { transform: scale(1.05); }
.sale-badge {
  position: absolute; top: 12px; left: 12px;
  background: #e53935; color: #fff;
  font-size: 11px; font-weight: 700; padding: 4px 10px; letter-spacing: 1px;
}

.thumb-list { display: flex; gap: 8px; margin-top: 10px; }
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

/* ── Đã bán: màu cam nổi bật ── */
.sold-count { color: #f57c00 !important; font-weight: 600; }
.sold-spec  { color: #f57c00; font-weight: 600; display: flex; align-items: center; gap: 6px; }

.divider { border: none; border-top: 1px solid #eee; margin: 14px 0; }
.section-label { font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 10px; color: #333; }

.size-grid { display: flex; gap: 8px; flex-wrap: wrap; }
.size-btn {
  width: 44px; height: 36px; border: 1px solid #ddd; background: #fff;
  font-size: 12px; font-weight: 600; cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: all 0.2s;
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
}
.btn-cart:hover   { background: #111; color: #fff; }
.btn-cart.success { background: #2e7d32; border-color: #2e7d32; color: #fff; }

.btn-buy {
  flex: 1; height: 44px; border: none; background: #e53935; color: #fff;
  font-weight: 700; font-size: 13px; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 8px;
  transition: background 0.2s; letter-spacing: 0.5px;
}
.btn-buy:hover { background: #c62828; }

.policy-row { display: flex; margin-top: 16px; border: 1px solid #eee; }
.policy-item {
  flex: 1; display: flex; align-items: center; gap: 10px;
  padding: 12px; border-right: 1px solid #eee; font-size: 12px; color: #555;
}
.policy-item:last-child { border-right: none; }
.policy-item i { font-size: 20px; color: #111; flex-shrink: 0; }
.policy-item small { color: #aaa; font-size: 10px; }

.related-wrap { margin-top: 12px; background: #fff; border: 1px solid #ddd; padding: 16px; }
.section-heading {
  font-size: 14px; font-weight: 700; text-transform: uppercase;
  letter-spacing: 0.5px; margin-bottom: 14px;
  display: flex; align-items: baseline; gap: 8px;
}
.count { font-size: 11px; color: #888; font-weight: 400; }

.product-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 10px; }
.pcard { border: 1px solid #e0e0e0; background: #fff; cursor: pointer; transition: box-shadow 0.2s, transform 0.2s; }
.pcard:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.12); transform: translateY(-2px); }
.pcard-img-wrap { width: 100%; aspect-ratio: 1/1; overflow: hidden; background: #f8f8f8; }
.pcard-img { width: 100%; height: 100%; object-fit: cover; display: block; transition: transform 0.3s; }
.pcard:hover .pcard-img { transform: scale(1.05); }
.pcard-body { padding: 8px 10px 10px; }
.pcard-name { font-size: 12px; font-weight: 500; color: #222; margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
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

.rating-row { display: flex; align-items: center; gap: 8px; margin: 8px 0; cursor: pointer; padding: 4px 0; }
.stars { display: flex; gap: 2px; }
.stars i { color: #ffc107; font-size: 14px; }
.rating-text { color: #0066c0; font-size: 13px; font-weight: 500; }
.comment-count { color: #666; font-size: 12px; display: flex; align-items: center; gap: 4px; }

.tabs-wrap { background: #fff; border: 1px solid #ddd; margin-top: 12px; overflow: visible; }
.tabs-header { display: flex; border-bottom: 2px solid #ddd; background: #f8f8f8; }
.tab-item {
  padding: 14px 24px; font-size: 14px; font-weight: 600; color: #666;
  cursor: pointer; position: relative; transition: all 0.2s;
  border-right: 1px solid #ddd; user-select: none; z-index: 1;
}
.tab-item:last-child { border-right: none; }
.tab-item:hover { color: #111; background: #fff; }
.tab-item.active { color: #e53935; background: #fff; border-bottom: 3px solid #e53935; margin-bottom: -2px; }
.tab-count { background: #e0e0e0; color: #666; font-size: 11px; padding: 2px 6px; border-radius: 12px; margin-left: 6px; }
.tab-content { padding: 24px; display: block; min-height: 200px; }
.tab-pane { display: block; opacity: 1; visibility: visible; }

.product-desc-full { font-size: 14px; color: #444; line-height: 1.8; margin-bottom: 24px; white-space: pre-line; }
.product-desc-full :deep(p)  { margin-bottom: 12px; }
.product-desc-full :deep(ul) { padding-left: 20px; margin-bottom: 12px; }
.product-desc-full :deep(li) { margin-bottom: 4px; }
.product-desc-full :deep(img) { max-width: 100%; border-radius: 4px; margin: 8px 0; }
.product-desc-full :deep(strong) { color: #222; }

.product-desc-empty { font-size: 13px; color: #aaa; font-style: italic; display: flex; align-items: center; gap: 6px; margin-bottom: 24px; }
.product-specs h4 { font-size: 15px; font-weight: 700; margin-bottom: 16px; color: #222; }
.specs-table { width: 100%; border-collapse: collapse; }
.specs-table tr { border-bottom: 1px solid #eee; }
.specs-table td { padding: 12px 8px; font-size: 13px; }
.specs-table td:first-child { width: 140px; color: #666; font-weight: 500; }
.specs-table td:last-child { color: #222; }
.no-data { color: #bbb; font-style: italic; }

.reviewer-avatar-text,
.commenter-avatar-text {
  width: 40px; height: 40px; border-radius: 50%;
  background: linear-gradient(135deg, #e53935, #b71c1c);
  color: #fff; font-size: 16px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.commenter-avatar-text { width: 36px; height: 36px; font-size: 14px; }

.verified-badge { font-size: 11px; color: #2e7d32; font-weight: 600; display: inline-flex; align-items: center; gap: 4px; margin-top: 6px; }
.verified-badge i { font-size: 12px; }

.empty-state { text-align: center; padding: 48px 0; color: #aaa; }
.empty-state i { font-size: 40px; display: block; margin-bottom: 12px; }
.empty-state p { font-size: 14px; margin-bottom: 4px; }
.empty-state small { font-size: 12px; }

.comment-name-row { margin-bottom: 8px; }
.comment-name-input { width: 100%; padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 13px; box-sizing: border-box; }
.comment-name-input:focus { outline: none; border-color: #111; }

.rating-count-num { font-size: 12px; color: #666; min-width: 16px; text-align: right; }

.reviews-summary { display: grid; grid-template-columns: 200px 1fr; gap: 32px; padding: 20px; background: #f9f9f9; border-radius: 8px; margin-bottom: 24px; }
.rating-overall { text-align: center; }
.rating-score { font-size: 48px; font-weight: 700; color: #e53935; line-height: 1; }
.rating-stars { display: flex; justify-content: center; gap: 4px; margin: 8px 0; }
.rating-stars i { color: #ffc107; font-size: 16px; }
.rating-count { font-size: 12px; color: #666; }
.rating-bars { display: flex; flex-direction: column; gap: 8px; }
.rating-bar-item { display: flex; align-items: center; gap: 8px; }
.rating-label { width: 45px; font-size: 12px; color: #666; }
.rating-bar-bg { flex: 1; height: 8px; background: #e0e0e0; border-radius: 4px; overflow: hidden; }
.rating-bar-fill { height: 100%; background: #ffc107; border-radius: 4px; transition: width 0.3s; }

.reviews-list { display: flex; flex-direction: column; gap: 20px; }
.review-item { border-bottom: 1px solid #eee; padding-bottom: 20px; }
.review-item:last-child { border-bottom: none; }
.review-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.reviewer-info { flex: 1; }
.reviewer-name { font-size: 14px; font-weight: 600; color: #222; margin-bottom: 4px; }
.review-rating { display: flex; gap: 2px; }
.review-rating i { color: #ffc107; font-size: 12px; }
.review-date { font-size: 11px; color: #999; }
.review-content { font-size: 13px; color: #555; line-height: 1.6; margin-bottom: 12px; }

.comments-input { margin-bottom: 24px; }
.comments-input textarea { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 13px; margin-bottom: 12px; resize: vertical; box-sizing: border-box; }
.btn-submit-comment { padding: 10px 20px; background: #111; border: none; border-radius: 4px; font-size: 13px; font-weight: 600; color: #fff; cursor: pointer; display: inline-flex; align-items: center; gap: 8px; }
.btn-submit-comment:hover { background: #333; }

.comments-list { display: flex; flex-direction: column; gap: 20px; }
.comment-item { border-bottom: 1px solid #eee; padding-bottom: 20px; }
.comment-item:last-child { border-bottom: none; }
.comment-header { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.commenter-info { flex: 1; }
.commenter-name { font-size: 13px; font-weight: 600; color: #222; margin-bottom: 2px; }
.comment-date { font-size: 10px; color: #999; }
.comment-content { font-size: 13px; color: #444; line-height: 1.6; margin-bottom: 12px; padding-left: 48px; }

@media (max-width: 768px) {
  .detail-wrap { grid-template-columns: 1fr; gap: 16px; padding: 16px; }
  .product-grid { grid-template-columns: repeat(2, 1fr); }
  .policy-row { flex-direction: column; }
  .policy-item { border-right: none; border-bottom: 1px solid #eee; }
  .policy-item:last-child { border-bottom: none; }
  .tabs-header { flex-wrap: wrap; }
  .tab-item { flex: 1; text-align: center; padding: 12px; }
  .reviews-summary { grid-template-columns: 1fr; gap: 16px; }
}

@keyframes spin { to { transform: rotate(360deg); } }
</style>