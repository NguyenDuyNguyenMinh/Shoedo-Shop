<template>
  <div class="customer-layout">
    <KH_Navbar />

    <main class="container py-5">
      <!-- Loading -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
        <p class="mt-3 text-muted">Đang xử lý kết quả thanh toán...</p>
      </div>

      <!-- Thành công -->
      <div v-else-if="success" class="result-container text-center">
        <div class="result-icon success-icon">
          <i class="bi bi-check-circle-fill"></i>
        </div>
        <h2 class="result-title mt-4 mb-2">Thanh toán thành công!</h2>
        <p class="result-subtitle text-muted mb-4">
          Cảm ơn bạn đã đặt hàng tại ShoeDo Shop
        </p>

        <div class="result-card">
          <div class="result-card-row">
            <span class="result-label">Mã đơn hàng</span>
            <span class="result-value fw-bold">HD{{ String(maHD).padStart(4, '0') }}</span>
          </div>
          <div class="result-card-row">
            <span class="result-label">Tổng tiền</span>
            <span class="result-value fw-bold text-success">{{ formatCurrency(tongTien) }}</span>
          </div>
          <div class="result-card-row">
            <span class="result-label">Phương thức</span>
            <span class="result-value">
              <span class="vnpay-badge">
                <i class="bi bi-credit-card-2-front me-1"></i>VNPay
              </span>
            </span>
          </div>
          <div class="result-card-row">
            <span class="result-label">Mã giao dịch</span>
            <span class="result-value text-muted small">{{ transactionNo || '—' }}</span>
          </div>
        </div>

        <div class="result-info-box">
          <i class="bi bi-info-circle me-2"></i>
          Đơn hàng của bạn đang được xử lý. Bạn sẽ nhận được email xác nhận trong giây lát.
          Nhân viên sẽ liên hệ giao hàng trong thời gian sớm nhất.
        </div>

        <div class="result-actions mt-4">
          <a href="/customer/orders" class="btn btn-dark btn-lg px-4 me-3">
            <i class="bi bi-bag me-2"></i>Xem đơn hàng
          </a>
          <a href="/customer/index" class="btn btn-outline-dark btn-lg px-4">
            <i class="bi bi-house me-2"></i>Tiếp tục mua sắm
          </a>
        </div>
      </div>

      <!-- Thất bại -->
      <div v-else class="result-container text-center">
        <div class="result-icon failure-icon">
          <i class="bi bi-x-circle-fill"></i>
        </div>
        <h2 class="result-title mt-4 mb-2">Thanh toán không thành công</h2>
        <p class="result-subtitle text-muted mb-4">
          Rất tiếc, thanh toán của bạn đã thất bại
        </p>

        <div class="result-card">
          <div class="result-card-row">
            <span class="result-label">Mã đơn hàng</span>
            <span class="result-value fw-bold">HD{{ String(maHD).padStart(4, '0') }}</span>
          </div>
          <div class="result-card-row">
            <span class="result-label">Lý do</span>
            <span class="result-value text-danger">{{ errorMessage }}</span>
          </div>
          <div class="result-card-row">
            <span class="result-label">Trạng thái đơn hàng</span>
            <span class="result-value text-muted">Đơn đang chờ xử lý hoặc đã được ghi nhận</span>
          </div>
        </div>

        <div class="result-info-box warning-box">
          <i class="bi bi-exclamation-triangle me-2"></i>
          <span v-if="isCancelled">Bạn đã hủy thanh toán. Đơn hàng của bạn vẫn còn trong hệ thống và sẽ không bị xóa.</span>
          <span v-else-if="isExpired">Phiên thanh toán đã hết hạn (15 phút). Đơn hàng của bạn vẫn còn trong hệ thống.</span>
          <span v-else>Đã xảy ra lỗi trong quá trình thanh toán. Vui lòng thử lại hoặc chọn phương thức thanh toán khác.</span>
        </div>

        <div class="result-actions mt-4">
          <a href="/customer/checkout" class="btn btn-dark btn-lg px-4 me-3">
            <i class="bi bi-arrow-left me-2"></i>Quay lại thanh toán
          </a>
          <a href="/customer/index" class="btn btn-outline-dark btn-lg px-4">
            <i class="bi bi-house me-2"></i>Về trang chủ
          </a>
        </div>
      </div>
    </main>

    <Footer />
  </div>
</template>

<script>
import KH_Navbar from '@/components/shared/KH_Navbar.vue';
import Footer from '@/components/shared/Footer.vue';
import api from '@/services/api';

export default {
  name: 'PaymentResult',
  components: { KH_Navbar, Footer },
  data() {
    return {
      loading: true,
      success: false,
      maHD: null,
      tongTien: 0,
      transactionNo: '',
      errorMessage: '',
      responseCode: '',
      isCancelled: false,
      isExpired: false,
    };
  },
  methods: {
    formatCurrency(value) {
      if (value == null) return '0₫';
      return new Intl.NumberFormat('vi-VN').format(Math.round(value)) + '₫';
    },
    async verifyPaymentResult() {
      if (!this.maHD) {
        this.loading = false;
        return;
      }

      // Đọc query params từ URL để lấy thông tin hiển thị ban đầu
      const params = new URLSearchParams(window.location.search);
      this.tongTien = params.get('tongTien') ? parseFloat(params.get('tongTien')) : 0;
      this.transactionNo = params.get('transactionNo') || '';
      this.responseCode = params.get('responseCode') || '';

      const successParam = params.get('success');
      const urlSuccess = successParam === 'true' || successParam === true;

      let message = params.get('message') || '';
      try { message = decodeURIComponent(message); } catch (e) { /* ignore */ }

      // Phân loại lỗi
      if (this.responseCode === '24') {
        this.isCancelled = true;
        this.errorMessage = 'Bạn đã hủy giao dịch thanh toán';
      } else if (this.responseCode === '11') {
        this.isExpired = true;
        this.errorMessage = 'Phiên thanh toán đã hết hạn (15 phút)';
      } else {
        this.errorMessage = message || 'Đã xảy ra lỗi không xác định';
      }

      // LUÔN xác minh với backend trước khi hiển thị thành công — chống spoof URL params
      if (urlSuccess && this.maHD) {
        try {
          const res = await api.verifyPayment(this.maHD, this.transactionNo || null);
          if (res.data.success && res.data.valid) {
            this.success = true;
            this.tongTien = res.data.tongTien || this.tongTien;
          } else {
            // Backend không xác nhận thanh toán thành công
            this.success = false;
            this.errorMessage = 'Không xác nhận được thanh toán. Vui lòng liên hệ hỗ trợ.';
          }
        } catch (e) {
          console.error('Lỗi xác minh thanh toán:', e);
          this.success = false;
          this.errorMessage = 'Không thể xác minh thanh toán. Vui lòng liên hệ hỗ trợ.';
        }
      } else {
        this.success = false;
      }

      // Clear cart sessionStorage
      sessionStorage.removeItem('checkoutItems');
      sessionStorage.removeItem('checkoutItemIds');

      this.loading = false;
    },
  },
  mounted() {
    this.verifyPaymentResult();
  },
};
</script>

<style scoped>
.result-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px 0;
}

.result-icon {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  font-size: 56px;
}

.success-icon {
  background: #d1f2d1;
  color: #198754;
}

.failure-icon {
  background: #f8d7da;
  color: #dc3545;
}

.result-title {
  font-size: 28px;
  font-weight: 800;
  color: #000;
}

.result-subtitle {
  font-size: 16px;
}

.result-card {
  background: #fff;
  border: 2px solid #000;
  border-radius: 14px;
  padding: 8px 0;
  margin: 24px 0;
  text-align: left;
  overflow: hidden;
}

.result-card-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.result-card-row:last-child {
  border-bottom: none;
}

.result-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.result-value {
  font-size: 14px;
  color: #000;
  text-align: right;
}

.vnpay-badge {
  background: #e60012;
  color: #fff;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
}

.result-info-box {
  background: #f0f9f0;
  border: 1px solid #c3e6cb;
  border-radius: 10px;
  padding: 16px;
  font-size: 14px;
  color: #155724;
  text-align: left;
  line-height: 1.6;
}

.warning-box {
  background: #fff3cd;
  border-color: #ffeeba;
  color: #856404;
}

.result-actions {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
}

.result-actions .btn-lg {
  border-radius: 10px;
  font-weight: 700;
  font-size: 15px;
}

.result-actions .btn-dark:hover {
  background: #333;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.25);
}

@media (max-width: 576px) {
  .result-actions {
    flex-direction: column;
  }
  .result-actions .btn-lg {
    width: 100%;
  }
}
</style>
