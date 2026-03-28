<template>
  <div class="customer-layout">
    <KH_Navbar />
    
    <main class="container py-4">
      <!-- Breadcrumb -->
      <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
          <li class="breadcrumb-item"><a href="/customer/index">Trang chủ</a></li>
          <li class="breadcrumb-item"><a href="/customer/cart">Giỏ hàng</a></li>
          <li class="breadcrumb-item active">Đặt hàng</li>
        </ol>
      </nav>

      <!-- Loading -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
        <p class="mt-2 text-muted">Đang tải thông tin đặt hàng...</p>
      </div>

      <!-- No Items -->
      <div v-else-if="checkoutItems.length === 0" class="text-center py-5">
        <i class="bi bi-cart-x" style="font-size: 64px; color: #ccc;"></i>
        <h4 class="mt-3">Không có sản phẩm nào để đặt hàng</h4>
        <a href="/customer/cart" class="btn btn-dark px-4 py-2 mt-2">
          <i class="bi bi-arrow-left me-2"></i>Quay lại giỏ hàng
        </a>
      </div>

      <!-- Order Success -->
      <div v-else-if="orderSuccess" class="text-center py-5">
        <i class="bi bi-check-circle-fill text-success" style="font-size: 80px;"></i>
        <h3 class="mt-3 fw-bold">Đặt hàng thành công!</h3>
        <p class="text-muted">Mã đơn hàng: <strong>HD{{ String(orderResult.maHD).padStart(4, '0') }}</strong></p>
        <p class="text-muted">Tổng thanh toán: <strong>{{ formatCurrency(orderResult.tongTien) }}</strong></p>
        <div class="mt-4 d-flex justify-content-center gap-3">
          <a href="/customer/orders" class="btn btn-dark px-4 py-2">
            <i class="bi bi-bag me-2"></i>Xem đơn hàng
          </a>
          <a href="/customer/index" class="btn btn-outline-dark px-4 py-2">
            <i class="bi bi-arrow-left me-2"></i>Tiếp tục mua sắm
          </a>
        </div>
      </div>

      <!-- Checkout Form -->
      <template v-else>
        <h2 class="page-title-main mb-4">
          <i class="bi bi-credit-card me-2"></i>Thanh toán
        </h2>

        <div class="row g-4">
          <!-- Left: Form đặt hàng -->
          <div class="col-lg-7">

            <!-- 1. Địa chỉ giao hàng -->
            <div class="checkout-section">
              <div class="section-header">
                <h5><i class="bi bi-geo-alt me-2"></i>Địa chỉ giao hàng</h5>
              </div>

              <div v-if="addresses.length === 0" class="text-muted text-center py-3">
                Chưa có địa chỉ nào. Vui lòng thêm địa chỉ trong phần Hồ sơ.
              </div>

              <div v-for="addr in addresses" :key="addr.maDC" 
                   class="address-option" 
                   :class="{ selected: selectedAddress === addr.maDC }"
                   @click="selectedAddress = addr.maDC">
                <div class="d-flex align-items-start gap-3">
                  <input class="form-check-input mt-1" type="radio" 
                         name="address" :id="'addr' + addr.maDC"
                         :checked="selectedAddress === addr.maDC"
                         @change="selectedAddress = addr.maDC">
                  <label :for="'addr' + addr.maDC" class="flex-fill">
                    <div class="d-flex align-items-center gap-2 mb-1">
                      <span class="fw-bold">{{ addr.tenNN }}</span>
                      <span class="text-muted">|</span>
                      <span class="text-muted">{{ addr.sdt }}</span>
                      <span v-if="addr.macDinh" class="badge bg-dark ms-1">Mặc định</span>
                    </div>
                    <div class="text-muted small">{{ addr.diemGiao }}</div>
                  </label>
                </div>
              </div>
            </div>

            <!-- 2. Phương thức thanh toán -->
            <div class="checkout-section">
              <div class="section-header">
                <h5><i class="bi bi-wallet2 me-2"></i>Phương thức thanh toán</h5>
              </div>

              <div class="payment-option" 
                   :class="{ selected: paymentMethod === 'COD' }"
                   @click="paymentMethod = 'COD'">
                <div class="d-flex align-items-center gap-3">
                  <input class="form-check-input" type="radio" name="payment" id="payCOD"
                         :checked="paymentMethod === 'COD'" @change="paymentMethod = 'COD'">
                  <label for="payCOD" class="d-flex align-items-center gap-3 flex-fill">
                    <div class="payment-icon">
                      <i class="bi bi-cash-stack"></i>
                    </div>
                    <div>
                      <div class="fw-bold">Thanh toán khi nhận hàng (COD)</div>
                      <div class="text-muted small">Thanh toán bằng tiền mặt khi nhận hàng</div>
                    </div>
                  </label>
                </div>
              </div>

              <div class="payment-option" 
                   :class="{ selected: paymentMethod === 'Chuyển khoản' }"
                   @click="paymentMethod = 'Chuyển khoản'">
                <div class="d-flex align-items-center gap-3">
                  <input class="form-check-input" type="radio" name="payment" id="payTransfer"
                         :checked="paymentMethod === 'Chuyển khoản'" @change="paymentMethod = 'Chuyển khoản'">
                  <label for="payTransfer" class="d-flex align-items-center gap-3 flex-fill">
                    <div class="payment-icon">
                      <i class="bi bi-bank"></i>
                    </div>
                    <div>
                      <div class="fw-bold">Chuyển khoản ngân hàng</div>
                      <div class="text-muted small">Chuyển khoản qua tài khoản ngân hàng</div>
                    </div>
                  </label>
                </div>
              </div>
            </div>
          </div>

          <!-- Right: Tóm tắt đơn hàng -->
          <div class="col-lg-5">
            <div class="order-review sticky-top" style="top: 100px; z-index: 100;">
              <h5 class="review-title">Đơn hàng của bạn</h5>

              <!-- Product items -->
              <div v-for="item in checkoutItems" :key="item.maGH" class="review-item">
                <div class="d-flex gap-3">
                  <div class="review-item-image">
                    <img :src="getImageUrl(item)" alt="" @error="handleImageError($event, item)">
                    <span class="item-qty-badge">{{ item.soLuong }}</span>
                  </div>
                  <div class="flex-fill">
                    <div class="review-item-name">{{ getProductName(item) }}</div>
                    <div class="review-item-variant">
                      <span class="variant-tag">{{ item.tenMau }}</span>
                      <span class="variant-tag">Size {{ item.size }}</span>
                    </div>
                  </div>
                  <div class="review-item-price">{{ formatCurrency(item.thanhTien) }}</div>
                </div>
              </div>

              <hr>

              <!-- Tổng -->
              <div class="summary-line">
                <span>Tạm tính</span>
                <span class="fw-semibold">{{ formatCurrency(totalAmount) }}</span>
              </div>
              <div class="summary-line">
                <span>Phí vận chuyển</span>
                <span class="text-success fw-semibold">Miễn phí</span>
              </div>

              <hr>

              <div class="summary-line total-line">
                <span>Tổng thanh toán</span>
                <span>{{ formatCurrency(totalAmount) }}</span>
              </div>

              <button class="btn btn-dark w-100 mt-3 py-3 place-order-btn" 
                      @click="placeOrder"
                      :disabled="ordering || addresses.length === 0">
                <span v-if="ordering">
                  <span class="spinner-border spinner-border-sm me-2"></span>Đang xử lý...
                </span>
                <span v-else>
                  <i class="bi bi-bag-check me-2"></i>Đặt hàng
                </span>
              </button>

              <p class="text-center text-muted small mt-3">
                Bằng việc nhấn "Đặt hàng", bạn đồng ý với 
                <a href="/customer/chinhsach" class="text-dark">Điều khoản dịch vụ</a> và 
                <a href="/customer/chinhsach" class="text-dark">Chính sách bảo mật</a>
              </p>
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
import api from '@/services/api';
import { useAuthStore } from '@/stores/auth';

export default {
  name: 'KH_DatHang',
  components: { KH_Navbar, Footer },
  data() {
    return {
      checkoutItems: [],
      checkoutItemIds: [],
      addresses: [],
      selectedAddress: null,
      paymentMethod: 'COD',
      note: '',
      loading: true,
      ordering: false,
      orderSuccess: false,
      orderResult: {},
    };
  },
  computed: {
    totalAmount() {
      return this.checkoutItems.reduce((sum, item) => sum + (item.thanhTien || 0), 0);
    },
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        // Lấy items từ sessionStorage (từ cart page)
        const storedItems = sessionStorage.getItem('checkoutItems');
        const storedIds = sessionStorage.getItem('checkoutItemIds');
        
        if (storedItems) {
          this.checkoutItems = JSON.parse(storedItems);
        }
        if (storedIds) {
          this.checkoutItemIds = JSON.parse(storedIds);
        }

        // Nếu không có items, thử lấy toàn bộ cart
        if (this.checkoutItems.length === 0) {
          const cartResp = await api.getCart();
          if (cartResp.data.success && cartResp.data.items) {
            this.checkoutItems = cartResp.data.items;
            this.checkoutItemIds = this.checkoutItems.map(item => item.maGH);
          }
        }

        // Lấy danh sách địa chỉ
        const addrResp = await api.getAddresses();
        if (addrResp.data.success) {
          this.addresses = addrResp.data.addresses || addrResp.data.data || [];
        } else if (Array.isArray(addrResp.data)) {
          this.addresses = addrResp.data;
        }

        // Chọn địa chỉ mặc định
        const defaultAddr = this.addresses.find(a => a.macDinh);
        if (defaultAddr) {
          this.selectedAddress = defaultAddr.maDC;
        } else if (this.addresses.length > 0) {
          this.selectedAddress = this.addresses[0].maDC;
        }

      } catch (error) {
        console.error('Error loading checkout data:', error);
      } finally {
        this.loading = false;
      }
    },

    getImageUrl(item) {
      const hinhAnh = typeof item === 'string' ? item : item.hinhAnh;
      if (!hinhAnh) return 'https://via.placeholder.com/100?text=No+Image';
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
        'https://via.placeholder.com/100?text=No+Image'
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
      // Ưu tiên hiển thị tenSP, nếu không có thì mới dùng moTa
      if (item.tenSP && item.tenSP.trim() !== '') {
        const cleaned = item.tenSP.replace(/^SP\d+-ShoeDo\s*-\s*/, '');
        return cleaned || item.tenSP;
      }
      // Fallback: nếu tenSP không có thì dùng moTa
      if (item.moTa && item.moTa.trim() !== '') {
        return item.moTa;
      }
      return 'Sản phẩm';
    },

    formatCurrency(value) {
      if (value == null) return '0₫';
      return new Intl.NumberFormat('vi-VN').format(Math.round(value)) + '₫';
    },

    async placeOrder() {
      if (this.addresses.length === 0) {
        alert('Vui lòng thêm địa chỉ giao hàng trong phần Hồ sơ');
        return;
      }

      if (!this.selectedAddress) {
        alert('Vui lòng chọn địa chỉ giao hàng');
        return;
      }

      this.ordering = true;
      try {
        let response;

        // Nếu chọn thanh toán VNPay (Chuyển khoản), gọi API tạo thanh toán VNPay
        if (this.paymentMethod === 'Chuyển khoản') {
          response = await api.createVNPayOrder({
            maDC: this.selectedAddress,
            phuongThucTT: 'VNPAY',
            isVNPay: true,
            ghiChu: this.note,
            cartItemIds: this.checkoutItemIds,
          });

          if (response.data.success && response.data.paymentUrl) {
            // Redirect đến trang thanh toán VNPay
            window.location.href = response.data.paymentUrl;
            return;
          }
        } else {
          // Thanh toán COD - gọi API checkout thông thường
          response = await api.checkout({
            maDC: this.selectedAddress,
            phuongThucTT: this.paymentMethod,
            ghiChu: this.note,
            cartItemIds: this.checkoutItemIds,
          });
        }

        if (response.data.success) {
          this.orderSuccess = true;
          this.orderResult = response.data;
          
          // Clear sessionStorage
          sessionStorage.removeItem('checkoutItems');
          sessionStorage.removeItem('checkoutItemIds');

          // Update cart count
          const authStore = useAuthStore();
          authStore.updateCartCount();
        } else {
          alert(response.data.message || 'Đặt hàng thất bại');
        }
      } catch (error) {
        console.error('Checkout error:', error);
        alert(error.response?.data?.message || 'Lỗi khi đặt hàng');
      } finally {
        this.ordering = false;
      }
    },
  },
  mounted() {
    this.loadData();
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

/* Page Title */
.page-title-main {
  font-size: 28px;
  font-weight: 800;
  color: #000;
  letter-spacing: -0.5px;
}

/* Checkout Section */
.checkout-section {
  background: #fff;
  border: 2px solid #000;
  border-radius: 14px;
  padding: 24px;
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid #eee;
}

.section-header h5 {
  font-size: 18px;
  font-weight: 700;
  color: #000;
  margin: 0;
}

/* Radio */
.form-check-input:checked {
  background-color: #000;
  border-color: #000;
}

/* Address Option */
.address-option {
  padding: 16px 18px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.25s;
}

.address-option:hover {
  border-color: #999;
}

.address-option.selected {
  border-color: #000;
  background: #fafafa;
}

.address-option label {
  cursor: pointer;
}

/* Payment Option */
.payment-option {
  padding: 16px 18px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.25s;
}

.payment-option:hover {
  border-color: #999;
}

.payment-option.selected {
  border-color: #000;
  background: #fafafa;
}

.payment-option label {
  cursor: pointer;
}

.payment-icon {
  width: 44px;
  height: 44px;
  background: #000;
  color: #fff;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

/* Order Review */
.order-review {
  background: #fff;
  border: 2px solid #000;
  border-radius: 14px;
  padding: 28px;
}

.review-title {
  font-size: 20px;
  font-weight: 800;
  color: #000;
  margin-bottom: 20px;
  padding-bottom: 14px;
  border-bottom: 2px solid #000;
}

/* Review Item */
.review-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.review-item:last-of-type {
  border-bottom: none;
}

.review-item-image {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eee;
  position: relative;
  flex-shrink: 0;
}

.review-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-qty-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 22px;
  height: 22px;
  background: #000;
  color: #fff;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #fff;
}

.review-item-name {
  font-size: 14px;
  font-weight: 600;
  color: #000;
  margin-bottom: 2px;
}

.review-item-variant {
  font-size: 12px;
  color: #666;
}

.variant-tag {
  background: #f0f0f0;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  color: #333;
  display: inline-block;
  margin-right: 6px;
}

.review-item-price {
  font-size: 14px;
  font-weight: 700;
  color: #000;
  white-space: nowrap;
}

/* Summary Lines */
.summary-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  font-size: 14px;
  color: #333;
}

.total-line {
  font-size: 20px;
  font-weight: 800;
  color: #000;
  padding: 12px 0;
}

/* Place Order Button */
.place-order-btn {
  font-size: 16px;
  font-weight: 700;
  border-radius: 10px;
  letter-spacing: 0.5px;
  transition: all 0.3s;
}

.place-order-btn:hover:not(:disabled) {
  background: #333;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
}

.place-order-btn:disabled {
  opacity: 0.6;
}

/* Responsive */
@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
