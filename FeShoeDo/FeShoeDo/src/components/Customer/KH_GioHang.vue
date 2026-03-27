<template>
  <div class="customer-layout">
    <KH_Navbar />
    
    <main class="container py-4">
      <!-- Breadcrumb -->
      <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
          <li class="breadcrumb-item"><a href="/customer/index">Trang chủ</a></li>
          <li class="breadcrumb-item active">Giỏ hàng</li>
        </ol>
      </nav>

      <!-- Loading -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
        <p class="mt-2 text-muted">Đang tải giỏ hàng...</p>
      </div>

      <!-- Empty Cart -->
      <div v-else-if="cartItems.length === 0" class="text-center py-5">
        <i class="bi bi-cart-x" style="font-size: 64px; color: #ccc;"></i>
        <h4 class="mt-3">Giỏ hàng trống</h4>
        <p class="text-muted">Hãy thêm sản phẩm vào giỏ hàng để tiếp tục mua sắm</p>
        <a href="/customer/index" class="btn btn-dark px-4 py-2">
          <i class="bi bi-arrow-left me-2"></i>Tiếp tục mua sắm
        </a>
      </div>

      <!-- Cart Content -->
      <template v-else>
        <h2 class="cart-title mb-4">
          <i class="bi bi-cart3 me-2"></i>Giỏ hàng của bạn
          <span class="cart-count">({{ cartItems.length }} sản phẩm)</span>
        </h2>

        <div class="row g-4">
          <!-- Cart Items -->
          <div class="col-lg-8">
            <!-- Select All -->
            <div class="cart-header d-flex justify-content-between align-items-center mb-3">
              <div class="form-check">
                <input class="form-check-input" type="checkbox" id="selectAll" 
                       :checked="allSelected" @change="toggleSelectAll">
                <label class="form-check-label fw-semibold" for="selectAll">
                  Chọn tất cả ({{ cartItems.length }})
                </label>
              </div>
              <button class="btn btn-outline-danger btn-sm" @click="removeSelected" 
                      :disabled="selectedItems.length === 0">
                <i class="bi bi-trash me-1"></i>Xóa đã chọn ({{ selectedItems.length }})
              </button>
            </div>

            <!-- Cart Items List -->
            <div v-for="item in cartItems" :key="item.maGH" class="cart-item">
              <div class="row align-items-center">
                <div class="col-auto">
                  <input class="form-check-input" type="checkbox" 
                         :checked="isSelected(item.maGH)" 
                         @change="toggleSelect(item.maGH)">
                </div>
                <div class="col-auto">
                  <div class="cart-item-image">
                    <img :src="getImageUrl(item)" :alt="getProductName(item)" @error="handleImageError($event, item)">
                  </div>
                </div>
                <div class="col">
                  <div class="cart-item-info">
                    <h6 class="cart-item-name">{{ getProductName(item) }}</h6>
                    <div class="cart-item-variant">
                      <span class="variant-tag">{{ item.tenMau }}</span>
                      <span class="variant-tag">Size {{ item.size }}</span>
                    </div>
                    <div class="cart-item-price mt-2">
                      <span class="price-current">{{ formatCurrency(item.giaSauKM) }}</span>
                      <template v-if="item.khuyenMai && item.khuyenMai > 0">
                        <span class="price-discount-tag">-{{ item.khuyenMai }}%</span>
                        <span class="price-original">{{ formatCurrency(item.giaGoc) }}</span>
                      </template>
                    </div>
                  </div>
                </div>
                <div class="col-auto">
                  <div class="quantity-control">
                    <button class="qty-btn" @click="changeQty(item, -1)" :disabled="item.updating">
                      <i class="bi bi-dash"></i>
                    </button>
                    <input type="number" class="qty-input" :value="item.soLuong" min="1"
                           @change="setQty(item, $event.target.value)" :disabled="item.updating">
                    <button class="qty-btn" @click="changeQty(item, 1)" :disabled="item.updating">
                      <i class="bi bi-plus"></i>
                    </button>
                  </div>
                  <div v-if="item.soLuong > item.soLuongTon" class="text-danger small mt-1 text-center">
                    Chỉ còn {{ item.soLuongTon }}
                  </div>
                </div>
                <div class="col-auto text-end" style="min-width: 130px;">
                  <div class="cart-item-total">{{ formatCurrency(item.thanhTien) }}</div>
                  <button class="btn btn-sm btn-outline-danger mt-2 border-0" @click="removeItem(item.maGH)">
                    <i class="bi bi-trash"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Order Summary Sidebar -->
          <div class="col-lg-4">
            <div class="order-summary sticky-top" style="top: 100px; z-index: 100;">
              <h5 class="summary-title">Tóm tắt đơn hàng</h5>
              
              <div class="summary-row">
                <span>Tạm tính ({{ selectedItems.length }} sản phẩm)</span>
                <span class="fw-semibold">{{ formatCurrency(subtotal) }}</span>
              </div>
              <div class="summary-row">
                <span>Giảm giá</span>
                <span class="text-success fw-semibold">-{{ formatCurrency(totalDiscount) }}</span>
              </div>
              <div class="summary-row">
                <span>Phí vận chuyển</span>
                <span class="text-success fw-semibold">Miễn phí</span>
              </div>
              
              <hr>
              
              <div class="summary-row total-row">
                <span>Tổng cộng</span>
                <span>{{ formatCurrency(subtotal) }}</span>
              </div>

              <button class="btn btn-dark w-100 mt-3 py-3 checkout-btn" 
                      @click="goToCheckout"
                      :disabled="selectedItems.length === 0">
                <i class="bi bi-credit-card me-2"></i>Tiến hành đặt hàng
              </button>

              <div class="text-center mt-3">
                <a href="/customer/index" class="text-muted text-decoration-none small">
                  <i class="bi bi-arrow-left me-1"></i>Tiếp tục mua sắm
                </a>
              </div>

              <!-- Trust badges -->
              <div class="trust-badges mt-4 d-flex justify-content-center">
                <div class="trust-item d-flex align-items-center gap-2">
                  <i class="bi bi-shield-check"></i>
                  <span>Bảo mật thanh toán</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </main>

    <Footer />
  </div>
</template>

<script>
import KH_Navbar from '@/components/shared/KH_Navbar.vue';
import Footer from '@/components/shared/Footer.vue';
import { useCartStore } from '@/stores/cart';

export default {
  name: 'KH_GioHang',
  components: { KH_Navbar, Footer },
  created() {
    // Nếu cart store chưa load hoặc đã expired, fetch lại
    if (!this.cartStore.isLoaded || this.cartStore.items.length === 0) {
      this.cartStore.fetchCart();
    }
  },
  computed: {
    // Delegate to cartStore — template giữ nguyên không đổi
    cartStore() { return useCartStore(); },
    cartItems() { return this.cartStore.items; },
    selectedIds: {
      get() { return this.cartStore.selectedIds; },
      set(val) { this.cartStore.setSelectedIds(val); }
    },
    loading() { return this.cartStore.loading; },
    selectedItems() { return this.cartStore.selectedItems; },
    allSelected() { return this.cartStore.allSelected; },
    subtotal() { return this.cartStore.subtotal; },
    totalDiscount() { return this.cartStore.totalDiscount; },
  },
  methods: {
    getImageUrl(item) {
      const hinhAnh = item.hinhAnh;
      if (!hinhAnh) return 'https://via.placeholder.com/200?text=No+Image';
      if (hinhAnh.startsWith('http')) return hinhAnh;
      return `http://localhost:8080/images/${hinhAnh}`;
    },

    handleImageError(event, item) {
      const img = event.target;
      const currentSrc = img.src;
      const maSP = item.maSP;
      const fallbacks = [
        `http://localhost:8080/images/sp${maSP}.jpg`,
        `http://localhost:8080/images/sp${maSP}_black.jpg`,
        `http://localhost:8080/images/sp${maSP}_white.jpg`,
        'https://via.placeholder.com/200?text=No+Image'
      ];
      if (currentSrc.includes('placeholder.com')) return;
      const currentIndex = fallbacks.indexOf(currentSrc);
      if (currentIndex === -1) {
        img.src = fallbacks[0];
      } else if (currentIndex + 1 < fallbacks.length) {
        img.src = fallbacks[currentIndex + 1];
      }
    },

    getProductName(item) {
      if (item.tenSP && item.tenSP.trim() !== '') {
        const cleaned = item.tenSP.replace(/^SP\d+-ShoeDo\s*-\s*/, '');
        return cleaned || item.tenSP;
      }
      if (item.moTa && item.moTa.trim() !== '') {
        return item.moTa;
      }
      return 'Sản phẩm';
    },

    formatCurrency(value) {
      if (value == null) return '0₫';
      return new Intl.NumberFormat('vi-VN').format(Math.round(value)) + '₫';
    },

    isSelected(maGH) {
      return this.cartStore.selectedIds.includes(maGH);
    },

    toggleSelect(maGH) {
      const ids = [...this.cartStore.selectedIds];
      const index = ids.indexOf(maGH);
      if (index > -1) {
        ids.splice(index, 1);
      } else {
        ids.push(maGH);
      }
      this.cartStore.setSelectedIds(ids);
    },

    toggleSelectAll() {
      if (this.cartStore.allSelected) {
        this.cartStore.setSelectedIds([]);
      } else {
        this.cartStore.setSelectedIds(this.cartStore.items.map(item => item.maGH));
      }
    },

    async changeQty(item, delta) {
      const newQty = item.soLuong + delta;
      if (newQty < 1) return;
      if (newQty > (item.soLuongTon || 0)) {
        alert(`Số lượng tồn kho chỉ còn ${item.soLuongTon}`);
        return;
      }
      await this.cartStore.updateItem(item.maGH, newQty);
    },

    async setQty(item, value) {
      const newQty = parseInt(value);
      if (isNaN(newQty) || newQty < 1) return;
      if (newQty > (item.soLuongTon || 0)) {
        alert(`Số lượng tồn kho chỉ còn ${item.soLuongTon}`);
        return;
      }
      await this.cartStore.updateItem(item.maGH, newQty);
    },

    async removeItem(maGH) {
      if (!confirm('Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?')) return;
      const result = await this.cartStore.removeItem(maGH);
      if (!result.success) {
        alert('Lỗi khi xóa sản phẩm');
      }
    },

    async removeSelected() {
      if (this.cartStore.selectedIds.length === 0) return;
      if (!confirm(`Bạn có chắc muốn xóa ${this.cartStore.selectedIds.length} sản phẩm đã chọn?`)) return;
      await this.cartStore.removeSelected();
    },

    goToCheckout() {
      if (this.cartStore.selectedItems.length === 0) {
        alert('Vui lòng chọn ít nhất một sản phẩm để đặt hàng');
        return;
      }
      sessionStorage.setItem('checkoutItems', JSON.stringify(this.cartStore.selectedItems));
      sessionStorage.setItem('checkoutItemIds', JSON.stringify(this.cartStore.selectedIds));
      this.$router.push('/customer/checkout');
    },
  },
};
</script>

<style scoped>
/* Breadcrumb */
.breadcrumb-item a {
  color: #000;
  text-decoration: none;
  font-weight: 500;
}
.breadcrumb-item a:hover {
  text-decoration: underline;
}
.breadcrumb-item.active {
  color: #666;
}

/* Cart Title */
.cart-title {
  font-size: 28px;
  font-weight: 800;
  color: #000;
  letter-spacing: -0.5px;
}
.cart-count {
  font-size: 16px;
  font-weight: 400;
  color: #666;
}

/* Cart Header */
.cart-header {
  background: #f8f9fa;
  padding: 12px 18px;
  border-radius: 10px;
  border: 1px solid #eee;
}

.form-check-input:checked {
  background-color: #000;
  border-color: #000;
}

/* Cart Item */
.cart-item {
  background: #fff;
  border: 2px solid #000;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  transition: all 0.3s;
}

.cart-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.cart-item-image {
  width: 100px;
  height: 100px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #eee;
}

.cart-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cart-item-name {
  font-size: 15px;
  font-weight: 700;
  color: #000;
  margin-bottom: 6px;
}

.cart-item-variant {
  display: flex;
  gap: 6px;
}

.variant-tag {
  background: #f0f0f0;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  color: #333;
}

/* Pricing */
.price-current {
  font-size: 16px;
  font-weight: 700;
  color: #000;
}

.price-original {
  font-size: 13px;
  color: #999;
  text-decoration: line-through;
  margin-left: 6px;
}

.price-discount-tag {
  background: #000;
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
  margin-left: 6px;
}

/* Quantity Control */
.quantity-control {
  display: flex;
  align-items: center;
  border: 2px solid #000;
  border-radius: 8px;
  overflow: hidden;
}

.qty-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 16px;
}

.qty-btn:hover {
  background: #000;
  color: #fff;
}

.qty-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.qty-input {
  width: 45px;
  height: 36px;
  text-align: center;
  border: none;
  border-left: 1px solid #e0e0e0;
  border-right: 1px solid #e0e0e0;
  font-weight: 600;
  font-size: 14px;
  -moz-appearance: textfield;
  appearance: textfield;
}

.qty-input::-webkit-outer-spin-button,
.qty-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.cart-item-total {
  font-size: 18px;
  font-weight: 800;
  color: #000;
}

/* Order Summary */
.order-summary {
  background: #fff;
  border: 2px solid #000;
  border-radius: 14px;
  padding: 28px;
}

.summary-title {
  font-size: 20px;
  font-weight: 800;
  color: #000;
  margin-bottom: 20px;
  padding-bottom: 14px;
  border-bottom: 2px solid #000;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
  color: #333;
}

.total-row {
  font-size: 20px;
  font-weight: 800;
  color: #000;
  padding: 12px 0;
}

/* Checkout Button */
.checkout-btn {
  font-size: 16px;
  font-weight: 700;
  border-radius: 10px;
  letter-spacing: 0.5px;
  transition: all 0.3s;
}

.checkout-btn:hover:not(:disabled) {
  background: #333;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
}

.checkout-btn:disabled {
  opacity: 0.5;
}

/* Trust Badges */
.trust-badges {
  border-top: 1px solid #eee;
  padding-top: 16px;
}

.trust-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
  font-size: 13px;
  color: #555;
}

.trust-item i {
  font-size: 18px;
  color: #000;
}

/* Responsive */
@media (max-width: 768px) {
  .cart-item .row {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .quantity-control {
    justify-content: center;
  }
  
  .cart-item-total {
    margin-top: 10px;
  }
}
</style>
