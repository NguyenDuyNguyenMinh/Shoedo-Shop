<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import KH_Navbar from '@/components/Shared/KH_Navbar.vue'
import Footer from '@/components/Shared/Footer.vue'

const route  = useRoute()
const router = useRouter()

// ── Mock data (giống KH_Index) ──
const allProducts = [
  {
    id: 1,
    name: 'Nike Air Max 270',
    price: '2.890.000 đ', priceNum: 2890000, oldPrice: '3.200.000 đ',
    stock: 20, category: 'Thể thao', brand: 'Nike',
    image: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&q=80',
    images: [
      'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&q=80',
      'https://images.unsplash.com/photo-1515955656352-a1fa3ffcd111?w=600&q=80',
      'https://images.unsplash.com/photo-1539185441755-769473a23570?w=600&q=80',
    ],
    desc: 'Thiết kế hiện đại với đế Air Max mang lại cảm giác êm ái tối đa. Phù hợp cho các hoạt động thể thao và đi dạo hàng ngày.',
    sizes: [38, 39, 40, 41, 42, 43],
    colors: [
      { name: 'Đen', code: '#000000' },
      { name: 'Trắng', code: '#FFFFFF' },
      { name: 'Đỏ', code: '#FF0000' },
    ],
  },
  {
    id: 2,
    name: 'Adidas Ultraboost 23',
    price: '3.490.000 đ', priceNum: 3490000, oldPrice: null,
    stock: 15, category: 'Chạy bộ', brand: 'Adidas',
    image: 'https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=600&q=80',
    images: [
      'https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=600&q=80',
      'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&q=80',
    ],
    desc: 'Công nghệ Boost tiên tiến giúp hoàn trả năng lượng tối ưu, lý tưởng cho những buổi chạy bộ dài.',
    sizes: [39, 40, 41, 42, 43],
    colors: [
      { name: 'Đen', code: '#000000' },
      { name: 'Xám', code: '#808080' },
      { name: 'Xanh dương', code: '#0000FF' },
    ],
  },
  {
    id: 3,
    name: 'Air Jordan 1 Retro',
    price: '4.190.000 đ', priceNum: 4190000, oldPrice: '5.200.000 đ',
    stock: 8, category: 'Jordan', brand: 'Jordan',
    image: 'https://images.unsplash.com/photo-1600185365926-3a2ce3cdb9eb?w=600&q=80',
    images: [
      'https://images.unsplash.com/photo-1600185365926-3a2ce3cdb9eb?w=600&q=80',
      'https://images.unsplash.com/photo-1584735175315-9d5df23be620?w=600&q=80',
    ],
    desc: 'Huyền thoại bóng rổ trở lại với thiết kế retro cổ điển. Biểu tượng văn hóa đường phố không bao giờ lỗi thời.',
    sizes: [40, 41, 42, 43],
    colors: [
      { name: 'Đỏ đen', code: '#8B0000' },
      { name: 'Xanh navy', code: '#000080' },
    ],
  },
  {
    id: 4,
    name: 'Vans Old Skool Classic',
    price: '1.290.000 đ', priceNum: 1290000, oldPrice: null,
    stock: 50, category: 'Vans', brand: 'Vans',
    image: 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=600&q=80',
    images: ['https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=600&q=80'],
    desc: 'Đôi giày skateboard kinh điển với sọc side stripe đặc trưng. Phong cách bền bỉ qua mọi thời đại.',
    sizes: [37, 38, 39, 40, 41, 42],
    colors: [
      { name: 'Đen', code: '#000000' },
      { name: 'Trắng', code: '#FFFFFF' },
    ],
  },
  {
    id: 5,
    name: 'Converse Chuck 70 Hi',
    price: '1.590.000 đ', priceNum: 1590000, oldPrice: null,
    stock: 35, category: 'Converse', brand: 'Converse',
    image: 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=600&q=80',
    images: ['https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=600&q=80'],
    desc: 'Phiên bản nâng cấp của Chuck Taylor với chất lượng vải cao cấp hơn, đế dày hơn và thiết kế retro tinh tế.',
    sizes: [37, 38, 39, 40, 41],
    colors: [
      { name: 'Đen', code: '#000000' },
      { name: 'Trắng', code: '#FFFFFF' },
      { name: 'Đỏ', code: '#FF0000' },
    ],
  },
  {
    id: 6,
    name: 'New Balance 574 Core',
    price: '1.990.000 đ', priceNum: 1990000, oldPrice: null,
    stock: 18, category: 'Lifestyle', brand: 'New Balance',
    image: 'https://images.unsplash.com/photo-1584735175315-9d5df23be620?w=600&q=80',
    images: ['https://images.unsplash.com/photo-1584735175315-9d5df23be620?w=600&q=80'],
    desc: 'Thiết kế lifestyle cổ điển với đế ENCAP mang lại sự hỗ trợ và êm ái vượt trội cho cả ngày dài.',
    sizes: [38, 39, 40, 41, 42],
    colors: [
      { name: 'Xám', code: '#808080' },
      { name: 'Nâu', code: '#8B4513' },
    ],
  },
  {
    id: 7,
    name: 'Nike React Infinity',
    price: '2.690.000 đ', priceNum: 2690000, oldPrice: '2.990.000 đ',
    stock: 12, category: 'Chạy bộ', brand: 'Nike',
    image: 'https://images.unsplash.com/photo-1539185441755-769473a23570?w=600&q=80',
    images: ['https://images.unsplash.com/photo-1539185441755-769473a23570?w=600&q=80'],
    desc: 'Được thiết kế để giảm thiểu chấn thương khi chạy, Nike React Infinity mang lại sự ổn định và đàn hồi tuyệt vời.',
    sizes: [39, 40, 41, 42, 43],
    colors: [
      { name: 'Xanh lá', code: '#008000' },
      { name: 'Đen', code: '#000000' },
    ],
  },
  {
    id: 8,
    name: 'Puma RS-X Reinvention',
    price: '1.650.000 đ', priceNum: 1650000, oldPrice: null,
    stock: 25, category: 'Lifestyle', brand: 'Puma',
    image: 'https://images.unsplash.com/photo-1491553895911-0055eca6402d?w=600&q=80',
    images: ['https://images.unsplash.com/photo-1491553895911-0055eca6402d?w=600&q=80'],
    desc: 'Lấy cảm hứng từ giày chạy bộ thập niên 80, RS-X mang đến thiết kế chunky táo bạo với màu sắc nổi bật.',
    sizes: [38, 39, 40, 41, 42],
    colors: [
      { name: 'Trắng', code: '#FFFFFF' },
      { name: 'Vàng', code: '#FFFF00' },
    ],
  },
  {
    id: 9,
    name: 'Reebok Classic Leather',
    price: '1.190.000 đ', priceNum: 1190000, oldPrice: '1.590.000 đ',
    stock: 30, category: 'Classic', brand: 'Reebok',
    image: 'https://images.unsplash.com/photo-1556906781-9a412961a28c?w=600&q=80',
    images: ['https://images.unsplash.com/photo-1556906781-9a412961a28c?w=600&q=80'],
    desc: 'Chất liệu da mềm mại cao cấp kết hợp với thiết kế tối giản thanh lịch, phù hợp mọi phong cách.',
    sizes: [37, 38, 39, 40],
    colors: [
      { name: 'Trắng', code: '#FFFFFF' },
      { name: 'Đen', code: '#000000' },
    ],
  },
  {
    id: 10,
    name: 'Balenciaga Triple S',
    price: '5.990.000 đ', priceNum: 5990000, oldPrice: null,
    stock: 5, category: 'High-end', brand: 'Balenciaga',
    image: 'https://images.unsplash.com/photo-1512374382149-233c42b6a83b?w=600&q=80',
    images: ['https://images.unsplash.com/photo-1512374382149-233c42b6a83b?w=600&q=80'],
    desc: 'Biểu tượng thời trang cao cấp với thiết kế đế chunky đặc trưng. Sự kết hợp giữa ba loại đế tạo nên phong cách độc nhất.',
    sizes: [39, 40, 41, 42],
    colors: [
      { name: 'Đen', code: '#000000' },
      { name: 'Trắng', code: '#FFFFFF' },
    ],
  },
]

const mockReviews = [
  {
    id: 1,
    userId: 101,
    userName: 'Nguyễn Văn A',
    userAvatar: 'https://randomuser.me/api/portraits/men/1.jpg',
    rating: 5,
    title: 'Rất hài lòng với sản phẩm',
    comment: 'Giày đẹp, chất lượng tốt, đi êm chân. Giao hàng nhanh, đóng gói cẩn thận.',
    date: '2024-02-15',
    likes: 12,
    images: [
      'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=200&q=80',
    ],
  },
  {
    id: 2,
    userId: 102,
    userName: 'Trần Thị B',
    userAvatar: 'https://randomuser.me/api/portraits/women/2.jpg',
    rating: 4,
    title: 'Sản phẩm tốt nhưng hơi chật',
    comment: 'Giày đẹp, đúng mẫu. Tuy nhiên đi hơi chật so với size thường dùng, nên mua size lớn hơn 1 size.',
    date: '2024-02-10',
    likes: 5,
    images: [],
  },
  {
    id: 3,
    userId: 103,
    userName: 'Lê Văn C',
    userAvatar: 'https://randomuser.me/api/portraits/men/3.jpg',
    rating: 5,
    title: 'Chất lượng tuyệt vời',
    comment: 'Đôi giày mình mơ ước, chất liệu cao cấp, form dáng đẹp. Sẽ ủng hộ shop lần sau.',
    date: '2024-02-05',
    likes: 8,
    images: [],
  },
]

const mockComments = [
  {
    id: 1,
    userId: 201,
    userName: 'Phạm Văn D',
    userAvatar: 'https://randomuser.me/api/portraits/men/4.jpg',
    content: 'Cho mình hỏi giày này đi có bị đau chân không ạ?',
    date: '2024-02-16',
    likes: 2,
    replies: [
      {
        id: 101,
        userId: 101,
        userName: 'Nguyễn Văn A',
        userAvatar: 'https://randomuser.me/api/portraits/men/1.jpg',
        content: 'Mình đi thấy rất êm nha bạn, đế êm lắm.',
        date: '2024-02-16',
        likes: 1,
      }
    ]
  },
  {
    id: 2,
    userId: 202,
    userName: 'Hoàng Thị E',
    userAvatar: 'https://randomuser.me/api/portraits/women/5.jpg',
    content: 'Shop còn size 40 không ạ?',
    date: '2024-02-14',
    likes: 1,
    replies: [],
  },
]

const product = computed(() =>
  allProducts.find(p => p.id === Number(route.params.id)) || allProducts[0]
)

const related = computed(() =>
  allProducts.filter(p => p.category === product.value.category && p.id !== product.value.id).slice(0, 5)
)

const reviews = computed(() => mockReviews)
const comments = computed(() => mockComments)

const averageRating = computed(() => {
  if (reviews.value.length === 0) return 0
  const sum = reviews.value.reduce((acc, r) => acc + r.rating, 0)
  return sum / reviews.value.length
})

const getRatingPercent = (star) => {
  if (reviews.value.length === 0) return 0
  const count = reviews.value.filter(r => r.rating === star).length
  return (count / reviews.value.length) * 100
}

const getRatingCount = (star) => {
  return reviews.value.filter(r => r.rating === star).length
}

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

watch(() => route.params.id, () => {
  selectedImage.value  = 0
  selectedSize.value   = null
  selectedColor.value  = null
  quantity.value       = 1
  addedToCart.value    = false
  activeTab.value      = 'description'
  newComment.value     = ''
  newReviewText.value  = ''
})

const increaseQty = () => quantity.value++
const decreaseQty = () => { if (quantity.value > 1) quantity.value-- }

const addToCart = () => {
  if (!selectedSize.value) { alert('Vui lòng chọn size!'); return }
  if (!selectedColor.value) { alert('Vui lòng chọn màu sắc!'); return }
  addedToCart.value = true
  setTimeout(() => addedToCart.value = false, 2000)
}

const goToDetail = (id) => {
  router.push({ name: 'DetailProduct', params: { id } })
}

const submitComment = () => {
  if (!newComment.value.trim()) {
    alert('Vui lòng nhập nội dung bình luận')
    return
  }
  newComment.value = ''
  alert('Cảm ơn bạn đã bình luận!')
}

const submitReview = () => {
  showReviewForm.value = false
  newReviewText.value  = ''
  alert('Cảm ơn bạn đã đánh giá sản phẩm!')
}

const likeItem = (type, id) => {
  console.log(`Liked ${type} ${id}`)
}

// ── FIX: Hàm chuyển tab rõ ràng ──
const setTab = (tab) => {
  activeTab.value = tab
}
</script>

<template>
  <div class="kh-detail">

    <KH_Navbar />

    <div class="page-body">

      <!-- Breadcrumb -->
      <div class="breadcrumb-bar">
        <span class="bc-link" @click="router.push({ name: 'CustomerIndex' })">Trang chủ</span>
        <i class="bi bi-chevron-right bc-sep"></i>
        <span class="bc-link">{{ product.category }}</span>
        <i class="bi bi-chevron-right bc-sep"></i>
        <span class="bc-current">{{ product.name }}</span>
      </div>

      <!-- ════════ MAIN DETAIL ════════ -->
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

          <div class="meta-row">
            <span class="meta-item"><i class="bi bi-box-seam"></i> Còn {{ product.stock }} đôi</span>
            <span class="meta-item"><i class="bi bi-tag"></i> {{ product.category }}</span>
            <span class="meta-item" :class="product.stock > 0 ? 'in-stock' : 'out-stock'">
              <i class="bi bi-circle-fill" style="font-size:8px;"></i>
              {{ product.stock > 0 ? 'Còn hàng' : 'Hết hàng' }}
            </span>
          </div>

          <!-- Điểm đánh giá trung bình -->
          <div class="rating-row" @click="setTab('reviews')">
            <div class="stars">
              <i
                v-for="star in 5"
                :key="star"
                class="bi"
                :class="star <= averageRating ? 'bi-star-fill' : 'bi-star'"
              ></i>
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

      <!-- TABS ĐÁNH GIÁ & BÌNH LUẬN -->
      <div class="tabs-wrap">
        <div class="tabs-header">
          <!-- ── FIX: dùng setTab() thay vì gán trực tiếp ── -->
          <div
            class="tab-item"
            :class="{ active: activeTab === 'description' }"
            @click.stop="setTab('description')"
          >
            Mô tả sản phẩm
          </div>
          <div
            class="tab-item"
            :class="{ active: activeTab === 'reviews' }"
            @click.stop="setTab('reviews')"
          >
            Đánh giá <span class="tab-count">{{ reviews.length }}</span>
          </div>
          <div
            class="tab-item"
            :class="{ active: activeTab === 'comments' }"
            @click.stop="setTab('comments')"
          >
            Bình luận <span class="tab-count">{{ comments.length }}</span>
          </div>
        </div>

        <!-- ── FIX: dùng :key để force re-render khi đổi tab ── -->
        <div class="tab-content">
          <!-- Tab Mô tả -->
          <div v-if="activeTab === 'description'" :key="'description'" class="tab-pane">
            <p class="product-desc-full">{{ product.desc }}</p>
            <div class="product-specs">
              <h4>Thông số kỹ thuật</h4>
              <table class="specs-table">
                <tr>
                  <td>Thương hiệu</td>
                  <td>{{ product.brand }}</td>
                </tr>
                <tr>
                  <td>Danh mục</td>
                  <td>{{ product.category }}</td>
                </tr>
                <tr>
                  <td>Chất liệu</td>
                  <td>Da tổng hợp cao cấp / Vải lưới</td>
                </tr>
                <tr>
                  <td>Đế giày</td>
                  <td>Cao su non chống trơn trượt</td>
                </tr>
                <tr>
                  <td>Bảo hành</td>
                  <td>12 tháng</td>
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
                  <i
                    v-for="star in 5"
                    :key="star"
                    class="bi"
                    :class="star <= Math.round(averageRating) ? 'bi-star-fill' : 'bi-star'"
                  ></i>
                </div>
                <div class="rating-count">{{ reviews.length }} đánh giá</div>
              </div>
              <div class="rating-bars">
                <div v-for="rate in [5,4,3,2,1]" :key="rate" class="rating-bar-item">
                  <span class="rating-label">{{ rate }} sao</span>
                  <div class="rating-bar-bg">
                    <div
                      class="rating-bar-fill"
                      :style="{ width: getRatingPercent(rate) + '%' }"
                    ></div>
                  </div>
                  <span class="rating-count">{{ getRatingCount(rate) }}</span>
                </div>
              </div>
            </div>

            <div class="reviews-actions">
              <button class="btn-write-review" @click="showReviewForm = !showReviewForm">
                <i class="bi bi-pencil-square"></i> Viết đánh giá
              </button>
            </div>

            <!-- Form viết đánh giá -->
            <div v-if="showReviewForm" class="review-form">
              <h4>Viết đánh giá của bạn</h4>
              <div class="rating-input">
                <span>Đánh giá:</span>
                <div class="stars-input">
                  <i
                    v-for="star in 5"
                    :key="star"
                    class="bi"
                    :class="star <= newRating ? 'bi-star-fill' : 'bi-star'"
                    @click="newRating = star"
                  ></i>
                </div>
              </div>
              <textarea
                v-model="newReviewText"
                placeholder="Chia sẻ cảm nhận của bạn về sản phẩm..."
                rows="4"
              ></textarea>
              <div class="form-actions">
                <button class="btn-cancel" @click="showReviewForm = false">Hủy</button>
                <button class="btn-submit" @click="submitReview">Gửi đánh giá</button>
              </div>
            </div>

            <!-- Danh sách đánh giá -->
            <div class="reviews-list">
              <div v-for="review in reviews" :key="review.id" class="review-item">
                <div class="review-header">
                  <img :src="review.userAvatar" :alt="review.userName" class="reviewer-avatar">
                  <div class="reviewer-info">
                    <div class="reviewer-name">{{ review.userName }}</div>
                    <div class="review-rating">
                      <i
                        v-for="star in 5"
                        :key="star"
                        class="bi"
                        :class="star <= review.rating ? 'bi-star-fill' : 'bi-star'"
                      ></i>
                    </div>
                  </div>
                  <div class="review-date">{{ review.date }}</div>
                </div>
                <div class="review-title">{{ review.title }}</div>
                <div class="review-content">{{ review.comment }}</div>
                <div v-if="review.images.length > 0" class="review-images">
                  <img
                    v-for="(img, idx) in review.images"
                    :key="idx"
                    :src="img"
                    class="review-image"
                  >
                </div>
                <div class="review-footer">
                  <button class="btn-like" @click="likeItem('review', review.id)">
                    <i class="bi bi-heart"></i> {{ review.likes }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Tab Bình luận -->
          <div v-if="activeTab === 'comments'" :key="'comments'" class="tab-pane">
            <div class="comments-input">
              <textarea
                v-model="newComment"
                placeholder="Nhập bình luận của bạn..."
                rows="3"
              ></textarea>
              <button class="btn-submit-comment" @click="submitComment">
                <i class="bi bi-send"></i> Gửi bình luận
              </button>
            </div>

            <div class="comments-list">
              <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <div class="comment-header">
                  <img :src="comment.userAvatar" :alt="comment.userName" class="commenter-avatar">
                  <div class="commenter-info">
                    <div class="commenter-name">{{ comment.userName }}</div>
                    <div class="comment-date">{{ comment.date }}</div>
                  </div>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
                <div class="comment-footer">
                  <button class="btn-like" @click="likeItem('comment', comment.id)">
                    <i class="bi bi-heart"></i> {{ comment.likes }}
                  </button>
                  <button class="btn-reply">
                    <i class="bi bi-reply"></i> Trả lời
                  </button>
                </div>

                <!-- Replies -->
                <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
                  <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                    <div class="reply-header">
                      <img :src="reply.userAvatar" :alt="reply.userName" class="replyer-avatar">
                      <div class="replyer-info">
                        <div class="replyer-name">{{ reply.userName }}</div>
                        <div class="reply-date">{{ reply.date }}</div>
                      </div>
                    </div>
                    <div class="reply-content">{{ reply.content }}</div>
                    <div class="reply-footer">
                      <button class="btn-like" @click="likeItem('reply', reply.id)">
                        <i class="bi bi-heart"></i> {{ reply.likes }}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ════════ SẢN PHẨM LIÊN QUAN ════════ -->
      <div class="related-wrap" v-if="related.length > 0">
        <div class="section-heading">
          SẢN PHẨM LIÊN QUAN
          <span class="count">({{ related.length }} sản phẩm)</span>
        </div>
        <div class="product-grid">
          <div
            v-for="p in related"
            :key="p.id"
            class="pcard"
            @click="goToDetail(p.id)"
          >
            <div class="pcard-img-wrap">
              <img :src="p.image" :alt="p.name" class="pcard-img" />
            </div>
            <div class="pcard-body">
              <div class="pcard-name">{{ p.name }}</div>
              <div class="pcard-price">{{ p.price }}</div>
              <div class="pcard-meta"><i class="bi bi-box-seam"></i> Kho: {{ p.stock }}</div>
              <div class="pcard-meta"><i class="bi bi-tag"></i> {{ p.category }}</div>
            </div>
          </div>
        </div>
      </div>

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

.product-desc { font-size: 13px; color: #555; line-height: 1.7; }

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

/* ===== CSS MỚI THÊM ===== */
.required {
  color: #e53935;
  margin-left: 4px;
}

.color-grid {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.color-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid #ddd;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.color-btn:hover {
  transform: scale(1.1);
  border-color: #999;
}

.color-btn.selected {
  border-color: #111;
  box-shadow: 0 0 0 2px #fff, 0 0 0 4px #111;
}

.color-btn i {
  color: #fff;
  font-size: 18px;
  text-shadow: 0 0 2px rgba(0,0,0,0.5);
}

.color-btn[style*="background-color: #FFFFFF"] i,
.color-btn[style*="background-color: #FFF"] i,
.color-btn[style*="background-color: white"] i {
  color: #111;
  text-shadow: none;
}

.rating-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px 0;
  cursor: pointer;
  padding: 4px 0;
}

.rating-row:hover .stars i {
  color: #ffc107;
}

.stars {
  display: flex;
  gap: 2px;
}

.stars i {
  color: #ffc107;
  font-size: 14px;
}

.rating-text {
  color: #0066c0;
  font-size: 13px;
  font-weight: 500;
}

.comment-count {
  color: #666;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.tabs-wrap {
  background: #fff;
  border: 1px solid #ddd;
  margin-top: 12px;
  /* ── FIX: đảm bảo không bị overflow hidden cắt nội dung ── */
  overflow: visible;
}

.tabs-header {
  display: flex;
  border-bottom: 2px solid #ddd;
  background: #f8f8f8;
}

.tab-item {
  padding: 14px 24px;
  font-size: 14px;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
  border-right: 1px solid #ddd;
  user-select: none;
  /* ── FIX: đảm bảo tab-item không bị che bởi element khác ── */
  z-index: 1;
}

.tab-item:last-child {
  border-right: none;
}

.tab-item:hover {
  color: #111;
  background: #fff;
}

.tab-item.active {
  color: #e53935;
  background: #fff;
  border-bottom: 3px solid #e53935;
  margin-bottom: -2px;
}

.tab-count {
  background: #e0e0e0;
  color: #666;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 12px;
  margin-left: 6px;
}

.tab-content {
  padding: 24px;
  /* ── FIX: đảm bảo nội dung luôn hiển thị ── */
  display: block;
  min-height: 200px;
}

/* ── FIX: Xóa animation fadeIn gây lỗi opacity kẹt ở 0, thay bằng transition đơn giản ── */
.tab-pane {
  display: block;
  opacity: 1;
  visibility: visible;
}

.product-desc-full {
  font-size: 14px;
  color: #444;
  line-height: 1.8;
  margin-bottom: 24px;
}

.product-specs h4 {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #222;
}

.specs-table {
  width: 100%;
  border-collapse: collapse;
}

.specs-table tr {
  border-bottom: 1px solid #eee;
}

.specs-table td {
  padding: 12px 8px;
  font-size: 13px;
}

.specs-table td:first-child {
  width: 140px;
  color: #666;
  font-weight: 500;
}

.specs-table td:last-child {
  color: #222;
}

.reviews-summary {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 32px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 24px;
}

.rating-overall {
  text-align: center;
}

.rating-score {
  font-size: 48px;
  font-weight: 700;
  color: #e53935;
  line-height: 1;
}

.rating-stars {
  display: flex;
  justify-content: center;
  gap: 4px;
  margin: 8px 0;
}

.rating-stars i {
  color: #ffc107;
  font-size: 16px;
}

.rating-count {
  font-size: 12px;
  color: #666;
}

.rating-bars {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rating-bar-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-label {
  width: 45px;
  font-size: 12px;
  color: #666;
}

.rating-bar-bg {
  flex: 1;
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.rating-bar-fill {
  height: 100%;
  background: #ffc107;
  border-radius: 4px;
  transition: width 0.3s;
}

.reviews-actions {
  margin-bottom: 24px;
}

.btn-write-review {
  padding: 10px 20px;
  background: #fff;
  border: 1px solid #111;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
  color: #111;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.btn-write-review:hover {
  background: #111;
  color: #fff;
}

.review-form {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.review-form h4 {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 16px;
}

.rating-input {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.rating-input span {
  font-size: 13px;
  color: #666;
}

.stars-input {
  display: flex;
  gap: 4px;
}

.stars-input i {
  font-size: 20px;
  color: #ddd;
  cursor: pointer;
  transition: color 0.2s;
}

.stars-input i:hover,
.stars-input i.bi-star-fill {
  color: #ffc107;
}

.review-form textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  margin-bottom: 16px;
  resize: vertical;
  box-sizing: border-box;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel {
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
}

.btn-submit {
  padding: 8px 16px;
  background: #e53935;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  cursor: pointer;
}

.btn-submit:hover {
  background: #c62828;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.reviewer-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.reviewer-info {
  flex: 1;
}

.reviewer-name {
  font-size: 14px;
  font-weight: 600;
  color: #222;
  margin-bottom: 4px;
}

.review-rating {
  display: flex;
  gap: 2px;
}

.review-rating i {
  color: #ffc107;
  font-size: 12px;
}

.review-date {
  font-size: 11px;
  color: #999;
}

.review-title {
  font-size: 14px;
  font-weight: 600;
  color: #222;
  margin-bottom: 8px;
}

.review-content {
  font-size: 13px;
  color: #555;
  line-height: 1.6;
  margin-bottom: 12px;
}

.review-images {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.review-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
}

.review-footer {
  display: flex;
  gap: 16px;
}

.btn-like {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: none;
  border: none;
  font-size: 12px;
  color: #666;
  cursor: pointer;
}

.btn-like:hover {
  color: #e53935;
}

.btn-like i {
  font-size: 14px;
}

.comments-input {
  margin-bottom: 24px;
}

.comments-input textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  margin-bottom: 12px;
  resize: vertical;
  box-sizing: border-box;
}

.btn-submit-comment {
  padding: 10px 20px;
  background: #111;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-submit-comment:hover {
  background: #333;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.commenter-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.commenter-info {
  flex: 1;
}

.commenter-name {
  font-size: 13px;
  font-weight: 600;
  color: #222;
  margin-bottom: 2px;
}

.comment-date {
  font-size: 10px;
  color: #999;
}

.comment-content {
  font-size: 13px;
  color: #444;
  line-height: 1.6;
  margin-bottom: 12px;
  padding-left: 48px;
}

.comment-footer {
  display: flex;
  gap: 16px;
  padding-left: 48px;
}

.btn-reply {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: none;
  border: none;
  font-size: 12px;
  color: #666;
  cursor: pointer;
}

.btn-reply:hover {
  color: #0066c0;
}

.replies-list {
  margin-top: 16px;
  margin-left: 48px;
  padding-left: 16px;
  border-left: 2px solid #eee;
}

.reply-item {
  margin-bottom: 16px;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.replyer-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}

.replyer-info {
  flex: 1;
}

.replyer-name {
  font-size: 12px;
  font-weight: 600;
  color: #222;
}

.reply-date {
  font-size: 9px;
  color: #999;
}

.reply-content {
  font-size: 12px;
  color: #444;
  line-height: 1.5;
  margin-bottom: 6px;
  padding-left: 36px;
}

.reply-footer {
  padding-left: 36px;
}

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
</style>