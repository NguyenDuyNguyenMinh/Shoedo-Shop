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
                <button class="btn btn-outline-dark btn-sm" @click="showAddAddressModal = true">
                  <i class="bi bi-plus-lg me-1"></i>Thêm địa chỉ mới
                </button>
              </div>

              <div v-if="addresses.length === 0" class="text-muted text-center py-3">
                Chưa có địa chỉ nào. Vui lòng thêm địa chỉ mới.
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
                      <div class="fw-bold">Chuyển khoản ngân hàng (VNPay)</div>
                      <div class="text-muted small">Thanh toán qua VNPay - Quét QR hoặc chuyển khoản</div>
                    </div>
                  </label>
                </div>
              </div>

              <!-- QR Code Option - chỉ hiện khi chọn chuyển khoản -->
              <div v-if="paymentMethod === 'Chuyển khoản'" class="qr-option ms-4 mt-2">
                <div class="form-check">
                  <input class="form-check-input" type="checkbox" id="qrCodeOption" v-model="useQRCode">
                  <label class="form-check-label" for="qrCodeOption">
                    <i class="bi bi-qr-code me-1"></i>Quét mã QR để thanh toán
                  </label>
                </div>
                <small class="text-muted ms-4">Quét mã QR trên ứng dụng ngân hàng để thanh toán nhanh</small>
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

    <!-- Modal Thêm địa chỉ mới -->
    <div v-if="showAddAddressModal" class="modal-overlay" @click.self="showAddAddressModal = false">
      <div class="modal-container">
        <div class="modal-header">
          <h5 class="modal-title"><i class="bi bi-geo-alt me-2"></i>Thêm địa chỉ mới</h5>
          <button type="button" class="btn-close" @click="showAddAddressModal = false"></button>
        </div>
        <div class="modal-body">
          <div class="mb-3">
            <label class="form-label">Họ và tên người nhận <span class="text-danger">*</span></label>
            <input type="text" class="form-control" v-model="newAddress.tenNN" placeholder="Nguyễn Văn A">
          </div>
          <div class="mb-3">
            <label class="form-label">Số điện thoại <span class="text-danger">*</span></label>
            <input type="tel" class="form-control" v-model="newAddress.sdt" placeholder="0123456789">
          </div>
          <div class="mb-3">
            <label class="form-label">Địa chỉ nhận hàng <span class="text-danger">*</span></label>
            <textarea class="form-control" v-model="newAddress.diemGiao" rows="3" placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành phố"></textarea>
          </div>
          <div class="form-check">
            <input class="form-check-input" type="checkbox" id="setDefaultAddr" v-model="newAddress.macDinh">
            <label class="form-check-label" for="setDefaultAddr">
              Đặt làm địa chỉ mặc định
            </label>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-outline-dark" @click="showAddAddressModal = false">Hủy</button>
          <button type="button" class="btn btn-dark" @click="addNewAddress" :disabled="savingAddress">
            <span v-if="savingAddress">
              <span class="spinner-border spinner-border-sm me-1"></span>Đang lưu...
            </span>
            <span v-else><i class="bi bi-check-lg me-1"></i>Lưu địa chỉ</span>
          </button>
        </div>
      </div>
    </div>
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
      useQRCode: false,
      note: '',
      loading: true,
      ordering: false,
      orderSuccess: false,
      orderResult: {},
      isBuyNowFlow: false,
      // Modal thêm địa chỉ
      showAddAddressModal: false,
      savingAddress: false,
      newAddress: {
        tenNN: '',
        sdt: '',
        diemGiao: '',
        macDinh: false,
      },
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
        // Kiểm tra flow "mua ngay" trước — items nằm trong sessionStorage (không có trong DB cart)
        const isBuyNow = sessionStorage.getItem('isBuyNow') === 'true';
        // Lưu vào state để placeOrder() luôn dùng giá trị chính xác, không phụ thuộc sessionStorage
        this.isBuyNowFlow = isBuyNow;
        console.log('[loadData] isBuyNow:', isBuyNow);

        if (isBuyNow) {
          // Flow mua ngay: dùng trực tiếp từ sessionStorage
          const storedItems = sessionStorage.getItem('checkoutItems');
          const storedIds = sessionStorage.getItem('checkoutItemIds');
          console.log('[loadData] buyNow storedItems:', storedItems);
          console.log('[loadData] buyNow storedIds:', storedIds);
          if (storedItems) {
            this.checkoutItems = JSON.parse(storedItems);
          }
          if (storedIds) {
            this.checkoutItemIds = JSON.parse(storedIds);
          }
        } else {
          // Flow giỏ hàng: luôn lấy từ DB để đảm bảo đồng bộ, xóa sessionStorage cũ
          sessionStorage.removeItem('checkoutItems');
          sessionStorage.removeItem('checkoutItemIds');
          console.log('[loadData] cart flow: cleared sessionStorage, fetching from DB...');
          const cartResp = await api.getCart();
          if (cartResp.data.success && cartResp.data.items) {
            this.checkoutItems = cartResp.data.items;
            this.checkoutItemIds = this.checkoutItems.map(item => item.maGH);
            // Lưu lại vào sessionStorage để đồng bộ
            sessionStorage.setItem('checkoutItems', JSON.stringify(this.checkoutItems));
            sessionStorage.setItem('checkoutItemIds', JSON.stringify(this.checkoutItemIds));
          }
        }

        console.log('[loadData] final checkoutItems:', this.checkoutItems);
        console.log('[loadData] final checkoutItemIds:', this.checkoutItemIds);

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

    async addNewAddress() {
      // Validate
      if (!this.newAddress.tenNN?.trim()) {
        alert('Vui lòng nhập họ tên người nhận');
        return;
      }
      if (!this.newAddress.sdt?.trim()) {
        alert('Vui lòng nhập số điện thoại');
        return;
      }
      if (!this.newAddress.diemGiao?.trim()) {
        alert('Vui lòng nhập địa chỉ nhận hàng');
        return;
      }

      this.savingAddress = true;
      try {
        const response = await api.addAddress({
          tenNN: this.newAddress.tenNN.trim(),
          sdt: this.newAddress.sdt.trim(),
          diemGiao: this.newAddress.diemGiao.trim(),
          macDinh: this.newAddress.macDinh,
        });

        if (response.data.success) {
          // Reload addresses
          await this.loadAddresses();

          // Select the new address if it's the only one or default
          if (this.addresses.length > 0) {
            this.selectedAddress = this.addresses[this.addresses.length - 1].maDC;
          }

          // Close modal and reset form
          this.showAddAddressModal = false;
          this.newAddress = {
            tenNN: '',
            sdt: '',
            diemGiao: '',
            macDinh: false,
          };
        } else {
          alert(response.data.message || 'Lỗi khi thêm địa chỉ');
        }
      } catch (error) {
        console.error('Add address error:', error);
        alert(error.response?.data?.message || 'Lỗi khi thêm địa chỉ');
      } finally {
        this.savingAddress = false;
      }
    },

    async loadAddresses() {
      try {
        const addrResp = await api.getAddresses();
        if (addrResp.data.success) {
          this.addresses = addrResp.data.addresses || addrResp.data.data || [];
        } else if (Array.isArray(addrResp.data)) {
          this.addresses = addrResp.data;
        }
      } catch (error) {
        console.error('Error loading addresses:', error);
      }
    },

    async placeOrder() {
      // Defensive: kiểm tra có sản phẩm không trước khi gọi API
      if (!this.checkoutItems || this.checkoutItems.length === 0) {
        alert('Không có sản phẩm nào để đặt hàng. Vui lòng quay lại trang sản phẩm.');
        return;
      }

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
        // Dùng this.isBuyNowFlow đã được lưu từ loadData(), không phụ thuộc sessionStorage
        const isBuyNow = this.isBuyNowFlow;
        console.log('[placeOrder] isBuyNow:', isBuyNow);
        console.log('[placeOrder] checkoutItems:', this.checkoutItems);
        console.log('[placeOrder] checkoutItemIds:', this.checkoutItemIds);
        console.log('[placeOrder] paymentMethod:', this.paymentMethod);

        // Nếu là flow mua ngay, dùng endpoint buy-now riêng
        if (isBuyNow) {
          if (this.paymentMethod === 'Chuyển khoản') {
            const buyNowPayload = {
              maSKU: this.checkoutItemIds[0], // mua ngay chỉ có 1 sản phẩm
              soLuong: this.checkoutItems[0]?.soLuong,
              maDC: this.selectedAddress,
              phuongThucTT: 'VNPAY',
              isVNPay: true,
              isQRCode: this.useQRCode,
              ghiChu: this.note,
            };
            console.log('[placeOrder] buyNow VNPay payload:', buyNowPayload);
            response = await api.buyNowVNPay(buyNowPayload);
          } else {
            const buyNowPayload = {
              maSKU: this.checkoutItemIds[0],
              soLuong: this.checkoutItems[0]?.soLuong,
              maDC: this.selectedAddress,
              phuongThucTT: 'COD',
              ghiChu: this.note,
            };
            console.log('[placeOrder] buyNow COD payload:', buyNowPayload);
            response = await api.buyNow(buyNowPayload);
          }

          console.log('[placeOrder] buyNow response:', response.data);

          if (response.data.success) {
            if (response.data.paymentUrl) {
              window.location.href = response.data.paymentUrl;
              return;
            }
            this.orderSuccess = true;
            this.orderResult = response.data;
            sessionStorage.removeItem('checkoutItems');
            sessionStorage.removeItem('checkoutItemIds');
            sessionStorage.removeItem('isBuyNow');
            const authStore = useAuthStore();
            authStore.updateCartCount();
          } else {
            alert(response.data.message || 'Đặt hàng thất bại');
          }
          this.ordering = false;
          return;
        }

        // === Flow giỏ hàng thông thường ===
        if (this.paymentMethod === 'Chuyển khoản') {
          const payload = {
            maDC: this.selectedAddress,
            phuongThucTT: 'VNPAY',
            isVNPay: true,
            isQRCode: this.useQRCode,
            ghiChu: this.note,
          };
          if (this.checkoutItemIds.length > 0) {
            payload.cartItemIds = this.checkoutItemIds;
          }
          console.log('[placeOrder] VNPay payload:', payload);
          response = await api.createVNPayOrder(payload);

          if (response.data.success && response.data.paymentUrl) {
            window.location.href = response.data.paymentUrl;
            return;
          }
        } else {
          const payload = {
            maDC: this.selectedAddress,
            phuongThucTT: this.paymentMethod,
            ghiChu: this.note,
          };
          if (this.checkoutItemIds.length > 0) {
            payload.cartItemIds = this.checkoutItemIds;
          }
          console.log('[placeOrder] COD payload:', payload);
          response = await api.checkout(payload);
        }

        console.log('[placeOrder] response:', response.data);

        if (response.data.success) {
          this.orderSuccess = true;
          this.orderResult = response.data;
          sessionStorage.removeItem('checkoutItems');
          sessionStorage.removeItem('checkoutItemIds');
          sessionStorage.removeItem('isBuyNow');
          const authStore = useAuthStore();
          authStore.updateCartCount();
        } else {
          alert(response.data.message || 'Đặt hàng thất bại');
        }
      } catch (error) {
        console.error('Checkout error:', error);
        console.error('Checkout error response:', error.response?.data);
        alert(error.response?.data?.message || 'Lỗi khi đặt hàng');
      } finally {
        this.ordering = false;
      }
    },
  },
  async mounted() {
    // Khởi tạo auth trước khi load data
    const authStore = useAuthStore();
    await authStore.fetchCurrentUser();

    if (!authStore.isAuthenticated) {
      // Nếu chưa đăng nhập, chuyển về trang login
      window.location.href = '/auth/login';
      return;
    }

    // Kiểm tra query params từ VNPay redirect
    const urlParams = new URLSearchParams(window.location.search);

    if (urlParams.get('success') === 'true') {
      // Thanh toán thành công
      this.orderSuccess = true;
      this.orderResult = {
        maHD: urlParams.get('maHD'),
        tongTien: 0 // Sẽ được load lại từ API nếu cần
      };

      // Clear sessionStorage
      sessionStorage.removeItem('checkoutItems');
      sessionStorage.removeItem('checkoutItemIds');
      sessionStorage.removeItem('isBuyNow');

      // Update cart count
      const authStore = useAuthStore();
      authStore.updateCartCount();

      // Clean URL
      window.history.replaceState({}, document.title, '/customer/checkout');
    } else if (urlParams.get('cancelled') === 'true') {
      // Khách hủy thanh toán
      alert('Bạn đã hủy thanh toán. Đơn hàng đã được hủy.');

      // Xóa sessionStorage để không hiển thị sản phẩm đã hủy
      sessionStorage.removeItem('checkoutItems');
      sessionStorage.removeItem('checkoutItemIds');
      sessionStorage.removeItem('isBuyNow');

      // Clean URL
      window.history.replaceState({}, document.title, '/customer/checkout');
    }

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

/* QR Option */
.qr-option {
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px dashed #ccc;
}

.qr-option .form-check-input:checked {
  background-color: #000;
  border-color: #000;
}

/* Responsive */
@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}

.modal-container {
  background: #fff;
  border-radius: 14px;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
}

.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: #000;
  margin: 0;
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #eee;
}
</style>
