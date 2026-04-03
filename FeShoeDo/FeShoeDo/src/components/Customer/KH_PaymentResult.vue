<template>
  <div class="customer-layout">
    <KH_Navbar />

    <main class="container py-5">
      <div class="result-card mx-auto" style="max-width: 600px;">

        <!-- Loading -->
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
          <p class="mt-3 text-muted">Đang xử lý kết quả thanh toán...</p>
        </div>

        <!-- Success -->
        <div v-else-if="paymentSuccess" class="text-center">
          <div class="result-icon success-icon mb-4">
            <i class="bi bi-check-circle-fill"></i>
          </div>
          <h2 class="fw-bold mb-2">Thanh toán thành công!</h2>
          <p class="text-muted mb-4">Cảm ơn bạn đã đặt hàng tại ShoeDo Shop.</p>

          <div class="result-detail-box mb-4">
            <div class="detail-row">
              <span class="text-muted">Mã đơn hàng</span>
              <strong>HD{{ String(maHD).padStart(4, '0') }}</strong>
            </div>
            <div class="detail-row">
              <span class="text-muted">Tổng thanh toán</span>
              <strong class="text-success">{{ formatCurrency(tongTien) }}</strong>
            </div>
            <div v-if="voucherDiscount > 0" class="detail-row">
              <span class="text-muted">Đã giảm (Voucher)</span>
              <strong class="text-success">-{{ formatCurrency(voucherDiscount) }}</strong>
            </div>
            <div class="detail-row">
              <span class="text-muted">Phương thức</span>
              <strong>VNPay</strong>
            </div>
            <div v-if="transactionCode" class="detail-row">
              <span class="text-muted">Mã giao dịch</span>
              <strong class="small">{{ transactionCode }}</strong>
            </div>
          </div>

          <p class="text-muted small mb-4">
            Đơn hàng của bạn đang được xử lý và sẽ được giao trong thời gian sớm nhất.
            Bạn có thể theo dõi trạng thái đơn hàng trong mục <strong>Đơn hàng</strong>.
          </p>

          <div class="d-flex justify-content-center gap-3">
            <a href="/customer/orders" class="btn btn-dark px-4 py-2">
              <i class="bi bi-bag me-2"></i>Xem đơn hàng
            </a>
            <a href="/customer/index" class="btn btn-outline-dark px-4 py-2">
              <i class="bi bi-house me-2"></i>Trang chủ
            </a>
          </div>
        </div>

        <!-- Failed / Cancelled -->
        <div v-else class="text-center">
          <div class="result-icon fail-icon mb-4">
            <i class="bi bi-x-circle-fill"></i>
          </div>
          <h2 class="fw-bold mb-2">Thanh toán không thành công</h2>
          <p class="text-muted mb-4">{{ errorMessage }}</p>

          <div class="result-detail-box mb-4">
            <div class="detail-row">
              <span class="text-muted">Mã đơn hàng</span>
              <strong>{{ maHD ? 'HD' + String(maHD).padStart(4, '0') : 'N/A' }}</strong>
            </div>
          </div>

          <p class="text-muted small mb-4">
            Đơn hàng vẫn được tạo và đang chờ thanh toán. Vui lòng thử lại hoặc liên hệ hỗ trợ.
          </p>

          <div class="d-flex justify-content-center gap-3">
            <a href="/customer/checkout" class="btn btn-dark px-4 py-2">
              <i class="bi bi-arrow-left me-2"></i>Quay lại thanh toán
            </a>
            <a href="/customer/index" class="btn btn-outline-dark px-4 py-2">
              <i class="bi bi-house me-2"></i>Trang chủ
            </a>
          </div>
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
  name: 'KH_PaymentResult',
  components: { KH_Navbar, Footer },
  data() {
    return {
      loading: true,
      paymentSuccess: false,
      maHD: null,
      tongTien: 0,
      voucherDiscount: 0,
      transactionCode: '',
      errorMessage: 'Thanh toán đã bị hủy hoặc không thành công.',
    };
  },
  methods: {
    formatCurrency(value) {
      if (value == null) return '0₫';
      return new Intl.NumberFormat('vi-VN').format(Math.round(value)) + '₫';
    },
  },
  async mounted() {
    const params = new URLSearchParams(window.location.search);
    const urlSuccess = params.get('success');
    const maHD       = params.get('maHD');
    const message    = params.get('message');

    this.maHD = maHD ? parseInt(maHD) : null;

    // Lấy dữ liệu từ sessionStorage (lưu trước khi redirect sang VNPay)
    const pending = sessionStorage.getItem('pendingOrder');
    if (pending) {
      const order = JSON.parse(pending);
      this.tongTien       = order.tongTien       || 0;
      this.voucherDiscount = order.voucherDiscount || 0;
      sessionStorage.removeItem('pendingOrder');
    }

    // 🔴 C4: Xác minh thanh toán từ backend — KHÔNG tin URL param
    if (this.maHD) {
      try {
        const verifyResp = await api.verifyPayment(this.maHD);

        if (verifyResp.data.success) {
          const trangThai = verifyResp.data.trangThai || '';
          const phuongThucTT = verifyResp.data.phuongThucTT || '';
          const daThanhToan = verifyResp.data.daThanhToan || false;

          // Trích mã giao dịch từ ghiChu
          const ghiChu = verifyResp.data.ghiChu || '';
          const match = ghiChu.match(/Mã giao dịch[:\-]?\s*([A-Z0-9]+)/i);
          if (match) {
            this.transactionCode = match[1];
          }

          // Xác định success: VNPay thành công + trạng thái hợp lệ
          const isPaid = daThanhToan
            && (trangThai === "Đang xử lý" || trangThai === "Đang giao"
                || trangThai === "Hoàn tất");

          if (isPaid) {
            this.paymentSuccess = true;
            this.errorMessage   = '';
            sessionStorage.removeItem('checkoutItems');
            sessionStorage.removeItem('checkoutItemIds');
          } else if (urlSuccess === 'true' && !isPaid) {
            // URL nói thành công nhưng backend không xác nhận → cảnh báo
            this.paymentSuccess = false;
            this.errorMessage   = 'Thanh toán không thể xác nhận. Vui lòng liên hệ hỗ trợ.';
          } else {
            this.paymentSuccess = false;
            this.errorMessage   = message ? decodeURIComponent(message) : 'Thanh toán không thành công.';
          }
        } else {
          this.paymentSuccess = false;
          this.errorMessage   = verifyResp.data.message || 'Không xác minh được thanh toán.';
        }
      } catch (err) {
        // Backend không trả lời → fallback theo URL param nhưng cảnh báo
        console.error('Verify payment failed:', err);
        this.paymentSuccess = (urlSuccess === 'true');
        this.errorMessage   = message ? decodeURIComponent(message)
          : (urlSuccess !== 'true' ? 'Thanh toán đã bị hủy hoặc không thành công.' : 'Không xác minh được thanh toán. Vui lòng liên hệ hỗ trợ.');
      }
    } else {
      // Không có maHD → fallback theo URL
      this.paymentSuccess = (urlSuccess === 'true');
      if (!this.paymentSuccess && message) {
        try {
          this.errorMessage = decodeURIComponent(message);
        } catch {
          this.errorMessage = message;
        }
      }
    }

    this.loading = false;
  },
};
</script>

<style scoped>
.result-card {
  background: #fff;
  border: 2px solid #000;
  border-radius: 16px;
  padding: 48px 40px;
  text-align: center;
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
  background: #d4edda;
  color: #28a745;
}

.fail-icon {
  background: #f8d7da;
  color: #dc3545;
}

.result-detail-box {
  background: #f8f9fa;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 20px;
  text-align: left;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
  font-size: 14px;
}

.detail-row:last-child {
  border-bottom: none;
}

@media (max-width: 576px) {
  .result-card {
    padding: 32px 20px;
  }
}
</style>
