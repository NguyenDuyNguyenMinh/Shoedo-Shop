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
        <p class="text-muted">Tổng thanh toán: <strong>{{ formatCurrency(orderResult.tongTienSauGiam || orderResult.tongTien) }}</strong></p>
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

            <!-- 2. Voucher -->
            <div class="checkout-section">
              <div class="section-header">
                <h5><i class="bi bi-ticket-perforated me-2"></i>Voucher</h5>
                <button class="btn btn-sm btn-outline-dark" @click="loadVouchers" :disabled="loadingVouchers">
                  <i class="bi bi-arrow-clockwise me-1"></i>Làm mới
                </button>
              </div>

              <!-- Loading vouchers -->
              <div v-if="loadingVouchers" class="text-center py-3">
                <span class="spinner-border spinner-border-sm"></span>
                <span class="ms-2 text-muted">Đang tải voucher...</span>
              </div>

              <!-- No vouchers -->
              <div v-else-if="vouchers.length === 0" class="text-muted text-center py-3">
                Bạn không có voucher nào khả dụng.
              </div>

              <!-- Voucher list (radio like address) -->
              <div v-for="v in vouchers" :key="v.maKH_VC"
                   class="voucher-option"
                   :class="{
                     selected: selectedVoucher === v.maKH_VC,
                     disabled: !isVoucherApplicable(v)
                   }"
                   @click="selectVoucher(v)">
                <div class="d-flex align-items-start gap-3">
                  <input class="form-check-input mt-1" type="radio"
                         name="voucher" :id="'voucher' + v.maKH_VC"
                         :checked="selectedVoucher === v.maKH_VC"
                         :disabled="!isVoucherApplicable(v)"
                         @change="selectedVoucher = v.maKH_VC">
                  <label :for="'voucher' + v.maKH_VC" class="flex-fill">
                    <div class="voucher-card-inner">
                      <div class="voucher-value">
                        <span class="voucher-amount">{{ formatCurrency(v.giaTriGiam) }}</span>
                        <span class="voucher-label">GIẢM</span>
                      </div>
                      <div class="voucher-info">
                        <div class="fw-bold text-dark">{{ v.tenVoucher }}</div>
                        <div class="text-muted small">
                          Đơn tối thiểu {{ formatCurrency(v.donToiThieu || 0) }}
                        </div>
                        <div class="text-muted small">
                          HSD: {{ formatDate(v.hanSuDung) }}
                        </div>
                        <div v-if="!isVoucherApplicable(v)" class="text-danger small mt-1">
                          Không đủ điều kiện (đơn hàng tối thiểu {{ formatCurrency(v.donToiThieu || 0) }})
                        </div>
                      </div>
                    </div>
                  </label>
                </div>
              </div>

              <!-- No voucher option -->
              <div v-if="vouchers.length > 0"
                   class="voucher-option"
                   :class="{ selected: selectedVoucher === null }"
                   @click="selectedVoucher = null">
                <div class="d-flex align-items-start gap-3">
                  <input class="form-check-input mt-1" type="radio"
                         name="voucher" id="noVoucher"
                         :checked="selectedVoucher === null"
                         @change="selectedVoucher = null">
                  <label for="noVoucher" class="flex-fill">
                    <div class="fw-semibold">Không sử dụng voucher</div>
                  </label>
                </div>
              </div>
            </div>

            <!-- 3. Phương thức thanh toán -->
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
                   :class="{ selected: paymentMethod === 'VNPAY' }"
                   @click="paymentMethod = 'VNPAY'">
                <div class="d-flex align-items-center gap-3">
                  <input class="form-check-input" type="radio" name="payment" id="payVNPay"
                         :checked="paymentMethod === 'VNPAY'" @change="paymentMethod = 'VNPAY'">
                  <label for="payVNPay" class="d-flex align-items-center gap-3 flex-fill">
                    <div class="payment-icon" style="background: #0066cc;">
                      <i class="bi bi-credit-card-2-front"></i>
                    </div>
                    <div>
                      <div class="fw-bold">Thanh toán qua VNPay</div>
                      <div class="text-muted small">Chuyển khoản ngân hàng qua cổng VNPay</div>
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
              <div v-if="voucherDiscount > 0" class="summary-line">
                <span>Voucher giảm</span>
                <span class="text-success fw-semibold">-{{ formatCurrency(voucherDiscount) }}</span>
              </div>
              <div class="summary-line">
                <span>Phí vận chuyển</span>
                <span class="text-success fw-semibold">Miễn phí</span>
              </div>

              <hr>

              <div class="summary-line total-line">
                <span>Tổng thanh toán</span>
                <span>{{ formatCurrency(totalAfterDiscount) }}</span>
              </div>

              <!-- Voucher applied badge -->
              <div v-if="selectedVoucherObj" class="voucher-applied-badge">
                <i class="bi bi-check-circle-fill me-1"></i>
                Đã áp dụng voucher "{{ selectedVoucherObj.tenVoucher }}"
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
      vouchers: [],
      selectedVoucher: null,
      loadingVouchers: false,
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
    selectedVoucherObj() {
      if (!this.selectedVoucher) return null;
      return this.vouchers.find(v => v.maKH_VC === this.selectedVoucher) || null;
    },
    voucherDiscount() {
      if (!this.selectedVoucherObj) return 0;
      const discount = this.selectedVoucherObj.giaTriGiam || 0;
      // Không vượt quá tổng tiền
      return Math.min(discount, this.totalAmount);
    },
    totalAfterDiscount() {
      return Math.max(0, this.totalAmount - this.voucherDiscount);
    },
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        const storedItems = sessionStorage.getItem('checkoutItems');
        const storedIds = sessionStorage.getItem('checkoutItemIds');

        if (storedItems) {
          this.checkoutItems = JSON.parse(storedItems);
        }
        if (storedIds) {
          this.checkoutItemIds = JSON.parse(storedIds);
        }

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

        // Tải voucher
        await this.loadVouchers();

      } catch (error) {
        console.error('Error loading checkout data:', error);
      } finally {
        this.loading = false;
      }
    },

    async loadVouchers() {
      this.loadingVouchers = true;
      try {
        const resp = await api.getMyVouchers();
        if (resp.data.success) {
          this.vouchers = resp.data.vouchers || [];
        } else {
          this.vouchers = [];
        }
      } catch (error) {
        console.error('Error loading vouchers:', error);
        this.vouchers = [];
      } finally {
        this.loadingVouchers = false;
      }
    },

    isVoucherApplicable(voucher) {
      const minOrder = voucher.donToiThieu || 0;
      return this.totalAmount >= minOrder;
    },

    selectVoucher(v) {
      if (!this.isVoucherApplicable(v)) return;
      this.selectedVoucher = v.maKH_VC;
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

    formatDate(dateStr) {
      if (!dateStr) return 'N/A';
      try {
        const d = new Date(dateStr);
        return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
      } catch {
        return dateStr;
      }
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

        // Xây dựng payload chung
        const payload = {
          maDC: this.selectedAddress,
          phuongThucTT: this.paymentMethod,
          isVNPay: this.paymentMethod === 'VNPAY',
          ghiChu: this.note,
          cartItemIds: this.checkoutItemIds,
          maKH_VC: this.selectedVoucher || null,
        };

        if (this.paymentMethod === 'VNPAY') {
          response = await api.createVNPayOrder(payload);

          if (response.data.success && response.data.paymentUrl) {
            // Lưu kết quả tạm vào sessionStorage để hiển thị khi quay lại
            sessionStorage.setItem('pendingOrder', JSON.stringify({
              maHD: response.data.maHD,
              tongTien: response.data.tongTienSauGiam || response.data.tongTien,
              voucherDiscount: response.data.voucherDiscount || 0,
            }));
            // Redirect đến trang thanh toán VNPay
            window.location.href = response.data.paymentUrl;
            return;
          }
        } else {
          // COD
          response = await api.checkout(payload);
        }

        if (response.data.success) {
          this.orderSuccess = true;
          this.orderResult = response.data;

          // Clear sessionStorage
          sessionStorage.removeItem('checkoutItems');
          sessionStorage.removeItem('checkoutItemIds');
          sessionStorage.removeItem('pendingOrder');

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

/* Voucher Option */
.voucher-option {
  padding: 14px 18px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.25s;
}

.voucher-option:hover:not(.disabled) {
  border-color: #999;
}

.voucher-option.selected {
  border-color: #000;
  background: #fafafa;
}

.voucher-option.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.voucher-option label {
  cursor: pointer;
}

.voucher-card-inner {
  display: flex;
  gap: 14px;
  align-items: center;
}

.voucher-value {
  background: #000;
  color: #fff;
  border-radius: 8px;
  padding: 10px 14px;
  text-align: center;
  min-width: 80px;
  flex-shrink: 0;
}

.voucher-amount {
  display: block;
  font-size: 16px;
  font-weight: 800;
  color: #ffd700;
}

.voucher-label {
  display: block;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #fff;
}

.voucher-info {
  flex: 1 1 auto;
}

.voucher-applied-badge {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 13px;
  font-weight: 600;
  margin-top: 10px;
  text-align: center;
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
