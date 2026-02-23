<template>
  <div class="customer-layout">
    <KH_Navbar />
    
    <div class="container mt-4 mb-3">
      <!-- Product Detail -->
      <div v-if="sanPham">
        <div class="product-detail">
          <div class="row">
            <!-- Product Images -->
            <div class="col-lg-5">
              <div class="">
                <img class="main-image" 
                     :src="getImageUrl(selectedImage || currentProductImage)" 
                     :alt="sanPham.tenSP"
                     @error="handleImageError">
              </div>
              
              <!-- Thumbnail Images -->
              <div v-if="productImages.length > 1" class="thumbnail-images mt-3">
                <div class="row g-4">
                  <div v-for="(image, index) in productImages" :key="index" 
                      class="col-sm-3">
                    <img class="thumbnail" 
                        :class="{ 'active': selectedImage === image }"
                        :src="getImageUrl(image)" 
                        :alt="'Ảnh ' + (index + 1)"
                        @click="selectImage(image)"
                        @error="handleThumbnailError">
                  </div>
                </div>
              </div>
            </div>
            <!-- Product Info -->
            <div class="col-lg-7">
              <h1>{{ sanPham.tenSP }}</h1>

              <!-- Product Status -->
              <div class="mb-3">
                <span v-if="currentTrangThai === 'Còn hàng'" class="product-status status-in-stock">
                  <i class="bi bi-check-circle-fill"></i>
                  <span>{{ currentTrangThai }}</span>
                </span>
                <span v-else-if="currentTrangThai === 'Hết hàng'" class="product-status status-out-of-stock">
                  <i class="bi bi-x-circle-fill"></i>
                  <span>{{ currentTrangThai }}</span>
                </span>
                <span v-else class="product-status status-pre-order">
                  <i class="bi bi-clock-fill"></i>
                  <span>{{ currentTrangThai || 'Chưa cập nhật' }}</span>
                </span>
              </div>

              <!-- Price -->
              <div class="price-section mb-3">
                <span class="current-price">{{ formatPrice(currentDonGia) }}</span>
              </div>

              <!-- Phân loại sản phẩm (luôn hiển thị nếu có phân loại) -->
              <div v-if="plSanPhams && plSanPhams.length > 0" class="mb-4">
                <h5>Phân loại sản phẩm</h5>
                <div class="option-buttons">
                  <button v-for="pl in plSanPhams" 
                          :key="pl.maPLSP"
                          class="option-btn" 
                          :class="{ 'active': selectedPLSanPham?.maPLSP === pl.maPLSP }"
                          @click="selectPLSanPham(pl)"
                          :disabled="pl.soLuong <= 0">
                    {{ pl.phanLoai }} 
                    <span v-if="pl.soLuong <= 0" class="text-danger ms-1">(Hết hàng)</span>
                  </button>
                </div>
              </div>

              <!-- Quantity Selector -->
              <div class="quantity-selector mb-4">
                <div class="option-label">Số Lượng</div>
                <div class="quantity-controls">
                  <button class="quantity-btn" 
                          @click="decreaseQuantity" 
                          :disabled="currentSoLuong <= 0 || quantity <= 1">-</button>
                  <input type="number" 
                         class="quantity-input" 
                         v-model.number="quantity" 
                         min="1" 
                         :max="currentSoLuong"
                         :disabled="currentSoLuong <= 0"
                         @change="validateQuantity">
                  <button class="quantity-btn" 
                          @click="increaseQuantity" 
                          :disabled="currentSoLuong <= 0 || quantity >= currentSoLuong">+</button>
                </div>
                <span class="stock-info">
                  <span>{{ currentSoLuong }}</span> sản phẩm có sẵn
                </span>
              </div>

              <!-- Action Buttons -->
              <div class="action-buttons">
                <button class="btn-add-cart" 
                        @click="addToCart" 
                        :disabled="currentSoLuong <= 0">
                  <i class="bi bi-cart-plus"></i> Thêm Vào Giỏ Hàng
                </button>
                <button class="btn-buy-now" 
                        @click="buyNow" 
                        :disabled="currentSoLuong <= 0">
                  Mua Ngay
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Product Specifications -->
        <div class="product-info-section mt-4">
          <h2 class="section-title">Thông Số Sản Phẩm</h2>
          <div class="info-row">
            <div class="info-label">Danh Mục</div>
            <div class="info-value">{{ sanPham.danhMuc?.tenDM || 'Chưa phân loại' }}</div>
          </div>
          <div class="info-row" v-if="selectedPLSanPham?.phanLoai">
            <div class="info-label">Phân Loại</div>
            <div class="info-value">{{ selectedPLSanPham.phanLoai }}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Trạng Thái</div>
            <div class="info-value">
              <span :class="{
                'text-success': currentTrangThai === 'Còn hàng',
                'text-danger': currentTrangThai === 'Hết hàng',
                'text-warning': currentTrangThai && currentTrangThai !== 'Còn hàng' && currentTrangThai !== 'Hết hàng'
              }">
                {{ currentTrangThai || 'Chưa cập nhật' }}
              </span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-label">Kho hàng</div>
            <div class="info-value">{{ currentSoLuong }} sản phẩm</div>
          </div>
        </div>

        <!-- Product Description -->
        <div class="product-info-section mt-4">
          <h2 class="section-title">Mô Tả Sản Phẩm</h2>
          <div class="description-content" v-html="sanPham.moTa || 'Chưa có mô tả cho sản phẩm này.'">
          </div>
        </div>
      </div>

      <!-- Loading -->
      <div v-else-if="loading" class="container mt-5 text-center">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
        <p class="mt-2">Đang tải sản phẩm...</p>
      </div>

      <!-- Nếu không có sản phẩm -->
      <div v-else class="container mt-5 text-center">
        <div class="alert alert-warning">
          <i class="bi bi-exclamation-triangle fs-1"></i>
          <h4>Không tìm thấy sản phẩm</h4>
          <p>Sản phẩm bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.</p>
          <router-link to="/customer/index" class="btn btn-primary">
            <i class="bi bi-house"></i> Về Trang Chủ
          </router-link>
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '@/services/api';
import KH_Navbar from '@/components/shared/KH_Navbar.vue';
import Footer from '@/components/shared/Footer.vue';
import { useCartStore } from '@/stores/cart';
import { useAuthStore } from '@/stores/auth';

export default {
  name: 'DetailProduct',
  components: {
    KH_Navbar,
    Footer
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const sanPham = ref(null);
    const plSanPhams = ref([]);
    const selectedPLSanPham = ref(null);
    const selectedImage = ref(null);
    const productImages = ref([]);
    const loading = ref(false);
    const quantity = ref(1);
    const cartStore = useCartStore();
    const authStore = useAuthStore();

    const isAuthenticated = computed(() => authStore.isAuthenticated);
    const currentSoLuong = computed(() => {
      return selectedPLSanPham.value?.soLuong || 
             (plSanPhams.value.length > 0 ? plSanPhams.value[0]?.soLuong : 0) || 0;
    });
    
    const currentDonGia = computed(() => {
      return selectedPLSanPham.value?.donGia || 
             (plSanPhams.value.length > 0 ? plSanPhams.value[0]?.donGia : 0) || 0;
    });
    
    const currentTrangThai = computed(() => {
      return selectedPLSanPham.value?.trangThai || 
             (plSanPhams.value.length > 0 ? plSanPhams.value[0]?.trangThai : 'Chưa cập nhật');
    });
    
    const currentProductImage = computed(() => {
      return selectedPLSanPham.value?.hinh || 
             (plSanPhams.value.length > 0 ? plSanPhams.value[0]?.hinh : '');
    });

    const getImageUrl = (imageName) => {
      if (!imageName) return 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&h=800&fit=crop';
      return `http://localhost:8080/images/${imageName}`;
    };

    const handleImageError = (event) => {
      event.target.src = 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&h=800&fit=crop';
    };

    const handleThumbnailError = (event) => {
      event.target.src = 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=200&h=200&fit=crop';
    };

    const formatPrice = (price) => {
      if (!price) return '0₫';
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price);
    };

    const selectImage = (image) => {
      selectedImage.value = image;
    };

    const selectPLSanPham = (plSanPham) => {
      selectedPLSanPham.value = plSanPham;
      quantity.value = 1;
      
      updateProductImages();
    };

    const updateProductImages = () => {
      productImages.value = [];
      
      if (selectedPLSanPham.value?.hinh) {
        productImages.value.push(selectedPLSanPham.value.hinh);
      }

      plSanPhams.value.forEach(pl => {
        if (pl.maPLSP !== selectedPLSanPham.value?.maPLSP && 
            pl.hinh && 
            !productImages.value.includes(pl.hinh)) {
          productImages.value.push(pl.hinh);
        }
      });

      if (productImages.value.length > 0) {
        selectedImage.value = productImages.value[0];
      }
    };

    const validateQuantity = () => {
      if (quantity.value < 1) {
        quantity.value = 1;
      } else if (quantity.value > currentSoLuong.value) {
        quantity.value = currentSoLuong.value;
        alert(`Số lượng không được vượt quá ${currentSoLuong.value}`);
      }
    };

    const increaseQuantity = () => {
      if (quantity.value < currentSoLuong.value) {
        quantity.value++;
      } else {
        alert(`Số lượng không được vượt quá ${currentSoLuong.value}`);
      }
    };

    const decreaseQuantity = () => {
      if (quantity.value > 1) {
        quantity.value--;
      }
    };

    const fetchProduct = async () => {
      loading.value = true;
      try {
        const productId = route.params.id || route.query.maSP;
        if (productId) {
          const response = await api.getPublicProductById(productId);
          if (response.data.success) {
            sanPham.value = response.data.data;

            if (!sanPham.value.phanLoaiSanPhams || sanPham.value.phanLoaiSanPhams.length === 0) {
              const plResponse = await api.getProductPLSanPhams(productId);
              if (plResponse.data.success) {
                plSanPhams.value = plResponse.data.data;
                sanPham.value.phanLoaiSanPhams = plSanPhams.value;
              }
            } else {
              plSanPhams.value = sanPham.value.phanLoaiSanPhams;
            }
            
            if (plSanPhams.value.length > 0) {
              selectedPLSanPham.value = plSanPhams.value[0];
              updateProductImages();
            }
          }
        }
      } catch (error) {
        console.error('Error fetching product:', error);
      } finally {
        loading.value = false;
      }
    };

    const addToCart = async () => {
      if (!isAuthenticated.value) {
        router.push('/auth/login');
        return;
      }

      if (!selectedPLSanPham.value) {
        alert('Vui lòng chọn phân loại sản phẩm');
        return;
      }

      try {
        await api.addToCart({
          maPLSP: selectedPLSanPham.value.maPLSP,
          soLuong: quantity.value
        });
        
        await cartStore.fetchCart();
        alert('Đã thêm vào giỏ hàng!');
      } catch (error) {
        alert('Lỗi khi thêm vào giỏ hàng: ' + (error.response?.data?.message || error.message));
      }
    };

    const buyNow = async () => {
      if (!isAuthenticated.value) {
        router.push('/auth/login');
        return;
      }

      if (!selectedPLSanPham.value) {
        alert('Vui lòng chọn phân loại sản phẩm');
        return;
      }

      try {
        const productData = {
          maPLSP: selectedPLSanPham.value.maPLSP,
          plSanPham: selectedPLSanPham.value,
          sanPham: {
            maSP: sanPham.value.maSP,
            tenSP: sanPham.value.tenSP,
            hinh: selectedPLSanPham.value.hinh,
            donGia: selectedPLSanPham.value.donGia,
            phanLoai: selectedPLSanPham.value.phanLoai
          },
          soLuong: quantity.value,
          donGia: selectedPLSanPham.value.donGia
        };
        sessionStorage.setItem('buyNowItem', JSON.stringify(productData));
        
        router.push('/customer/checkout');
      } catch (error) {
        alert('Lỗi khi mua ngay: ' + (error.response?.data?.message || error.message));
      }
    };

    onMounted(() => {
      fetchProduct();
    });

    return {
      sanPham,
      plSanPhams,
      selectedPLSanPham,
      selectedImage,
      productImages,
      loading,
      quantity,
      isAuthenticated,
      currentSoLuong,
      currentDonGia,
      currentTrangThai,
      currentProductImage,
      getImageUrl,
      handleImageError,
      handleThumbnailError,
      formatPrice,
      selectImage,
      selectPLSanPham,
      validateQuantity,
      increaseQuantity,
      decreaseQuantity,
      addToCart,
      buyNow
    };
  }
};
</script>

<style scoped>
/* CSS từ file HTML giữ nguyên */
.product-detail {
  background: white;
  padding: 20px;
  border-radius: 4px;
  margin-top: 20px;
  border: 2px solid #000000;
}

.main-image {
  width: 100%;
  height: 429px;
  border: 2px solid #000000;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.thumbnail-images {
  width: 100%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.thumbnail {
  width: 100%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.thumbnail:hover{
  transform: scale(1.07);
}
.thumbnail.active {
  border: 2px solid #000000;
  transform: scale(1.05);
}

/* Hoặc nếu muốn khoảng cách đều hơn */
.row.gap-3 {
  --bs-gutter-x: 1rem; /* Khoảng cách ngang */
  --bs-gutter-y: 1rem; /* Khoảng cách dọc */
}
.price-section {
  background: #f8f9fa;
  padding: 15px;
  margin: 15px 0;
  border-radius: 4px;
  border: 1px solid #000000;
}

.current-price {
  color: #000000;
  font-size: 32px;
  font-weight: 500;
}

.option-label {
  color: #000000;
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: bold;
}

.option-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.option-btn {
  padding: 8px 16px;
  border: 2px solid #000000;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.option-btn:hover:not(:disabled) {
  border-color: #333333;
  color: #333333;
  background: #f8f9fa;
}

.option-btn.active {
  border-color: #000000;
  color: #000000;
  background: #f8f9fa;
  font-weight: bold;
}

.option-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 15px;
  margin: 25px 0;
}

.quantity-controls {
  display: flex;
  align-items: center;
  border: 2px solid #000000;
  border-radius: 4px;
}

.quantity-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: white;
  cursor: pointer;
  font-size: 18px;
}

.quantity-btn:hover:not(:disabled) {
  background: #f5f5f5;
}

.quantity-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-input {
  width: 60px;
  height: 40px;
  border: none;
  border-left: 1px solid #000000;
  border-right: 1px solid #000000;
  text-align: center;
  font-size: 16px;
}

.stock-info {
  color: #666666;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin: 30px 0;
}

.btn-add-cart {
  flex: 1;
  padding: 15px;
  border: 2px solid #000000;
  background: white;
  color: #000000;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-add-cart:hover:not(:disabled) {
  background: #f8f9fa;
}

.btn-add-cart:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-buy-now {
  flex: 1;
  padding: 15px;
  border: none;
  background: #000000;
  color: white;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-buy-now:hover:not(:disabled) {
  background: #333333;
}

.btn-buy-now:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.product-info-section {
  background: white;
  padding: 25px;
  border-radius: 4px;
  margin-top: 20px;
  border: 2px solid #000000;
}

.section-title {
  font-size: 18px;
  color: #000000;
  padding-bottom: 15px;
  border-bottom: 1px solid #000000;
}

.info-row {
  display: flex;
  padding: 15px 0;
  border-bottom: 1px solid #f5f5f5;
}

.info-label {
  width: 200px;
  color: #666666;
  font-size: 14px;
  font-weight: bold;
}

.info-value {
  flex: 1;
  color: #000000;
  font-size: 14px;
}

.description-content {
  color: #000000;
  white-space: pre-line;
}

.badge-category {
  background: #000000;
  color: white;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 14px;
}

.product-status {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 14px;
}

.status-in-stock {
  background: #d4edda;
  color: #155724;
}

.status-out-of-stock {
  background: #f8d7da;
  color: #721c24;
}

.status-pre-order {
  background: #fff3cd;
  color: #856404;
}
</style>