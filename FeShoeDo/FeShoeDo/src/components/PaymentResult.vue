<template>
  <div class="payment-result-page">
    <div class="container py-5">
      <div class="row justify-content-center">
        <div class="col-md-6 col-lg-5">

          <!-- Loading -->
          <div v-if="loading" class="text-center py-5">
            <div class="spinner-border text-dark" role="status">
              <span class="visually-hidden">Đang xử lý...</span>
            </div>
            <p class="mt-3 text-muted">Đang xử lý kết quả thanh toán...</p>
          </div>

          <!-- Success -->
          <div v-else-if="isSuccess" class="result-card result-success text-center">
            <div class="success-icon mb-4">
              <i class="bi bi-check-circle-fill text-success" style="font-size: 5rem;"></i>
            </div>
            <h2 class="fw-bold text-success mb-2">Thanh toán thành công!</h2>
            <p class="text-muted mb-4">Cảm ơn bạn đã đặt hàng tại ShoeDo Shop</p>

            <div class="order-info bg-light rounded-3 p-3 mb-4 text-start">
              <div class="mb-2">
                <span class="text-muted small">Mã đơn hàng:</span>
                <div class="fw-bold text-dark">#{{ orderId }}</div>
              </div>
              <div v-if="orderInfo" class="mb-2">
                <span class="text-muted small">Tổng tiền:</span>
                <div class="fw-bold text-dark">{{ formatCurrency(orderInfo.tongTien) }}</div>
              </div>
              <div class="mb-0">
                <span class="text-muted small">Phương thức:</span>
                <div class="fw-bold text-dark">VNPay</div>
              </div>
            </div>

            <div class="d-grid gap-2">
              <router-link to="/customer/orders" class="btn btn-dark btn-lg">
                <i class="bi bi-bag-check me-2"></i>Xem đơn hàng của tôi
              </router-link>
              <router-link to="/customer/index" class="btn btn-outline-dark">
                <i class="bi bi-house me-2"></i>Tiếp tục mua sắm
              </router-link>
            </div>
          </div>

          <!-- Failed -->
          <div v-else class="result-card result-failed text-center">
            <div class="failed-icon mb-4">
              <i class="bi bi-x-circle-fill text-danger" style="font-size: 5rem;"></i>
            </div>
            <h2 class="fw-bold text-danger mb-2">Thanh toán thất bại!</h2>
            <p class="text-muted mb-4">{{ errorMessage }}</p>

            <div v-if="orderId" class="order-info bg-light rounded-3 p-3 mb-4 text-start">
              <div class="mb-0">
                <span class="text-muted small">Mã đơn hàng:</span>
                <div class="fw-bold text-dark">#{{ orderId }}</div>
              </div>
            </div>

            <div class="d-grid gap-2">
              <router-link to="/customer/cart" class="btn btn-dark btn-lg">
                <i class="bi bi-cart3 me-2"></i>Quay lại giỏ hàng
              </router-link>
              <router-link to="/customer/index" class="btn btn-outline-dark">
                <i class="bi bi-house me-2"></i>Tiếp tục mua sắm
              </router-link>
            </div>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/services/api';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const authStore = useAuthStore();

const loading = ref(true);
const isSuccess = ref(false);
const orderId = ref('');
const errorMessage = ref('Giao dịch không thành công. Vui lòng thử lại.');
const orderInfo = ref(null);

/**
 * Format a number as Vietnamese Dong currency.
 * @param {number} value
 * @returns {string}
 */
function formatCurrency(value) {
  if (!value) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(value);
}

/**
 * Parse error message from VNPay response code.
 * @param {string} code
 * @returns {string}
 */
function getErrorMessage(code) {
  const messages = {
    '01': 'Giao dịch đã tồn tại.',
    '02': 'Merchant không hợp lệ.',
    '03': 'Dữ liệu gửi sang không hợp lệ.',
    '04': 'Khóa checksum không hợp lệ.',
    '06': 'Số dư tài khoản không đủ.',
    '07': 'Giao dịch bị nghi ngờ (fraud).',
    '09': 'Giao dịch thanh toán thất bại.',
    '10': 'Giao dịch thanh toán thất bại.',
    '11': 'Giao dịch thanh toán thất bại.',
    '12': 'Thẻ hết hạn.',
    '13': 'Giao dịch thanh toán thất bại.',
    '24': 'Giao dịch thanh toán thất bại.',
    '51': 'Tài khoản không đủ số dư.',
    '65': 'Tài khoản đã vượt quá giới hạn giao dịch trong ngày.',
    '81': 'Giao dịch thanh toán thất bại.',
    '99': 'Lỗi không xác định.',
  };
  return messages[code] || `Thanh toán không thành công (Mã lỗi: ${code}). Vui lòng thử lại.`;
}

onMounted(async () => {
  // VNPay returns ?vnp_ResponseCode=...&vnp_TxnRef=...
  const responseCode = route.query.vnp_ResponseCode;
  const txnRef = route.query.vnp_TxnRef;
  const amount = route.query.vnp_Amount;
  const transactionNo = route.query.vnp_TransactionNo;
  const payDate = route.query.vnp_PayDate;

  orderId.value = txnRef || '';

  try {
    // Update auth cart count
    await authStore.updateCartCount();

    // Clear sessionStorage (checkout leftover)
    sessionStorage.removeItem('checkoutItems');
    sessionStorage.removeItem('checkoutItemIds');

    if (responseCode === '00') {
      // Payment successful
      isSuccess.value = true;

      // Try to fetch order info
      if (txnRef) {
        try {
          const res = await api.getOrderDetail(txnRef);
          orderInfo.value = res.data.order || res.data;
        } catch {
          // Order detail fetch is optional — not critical
        }
      }
    } else {
      isSuccess.value = false;
      errorMessage.value = getErrorMessage(responseCode);
    }
  } catch (err) {
    console.error('[PaymentResult] error:', err);
    isSuccess.value = false;
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.payment-result-page {
  min-height: 80vh;
  background: #f8f9fa;
}

.result-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.success-icon,
.failed-icon {
  animation: popIn 0.4s ease-out;
}

@keyframes popIn {
  0% {
    transform: scale(0.5);
    opacity: 0;
  }
  70% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.order-info {
  border: 1px solid #e9ecef;
}

.result-success {
  border-top: 4px solid #198754;
}

.result-failed {
  border-top: 4px solid #dc3545;
}
</style>
