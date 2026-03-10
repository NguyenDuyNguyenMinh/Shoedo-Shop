<template>
  <div class="customer-layout">
    <KH_Navbar />
    <main class="container py-4">
      <!-- Header với hiệu ứng -->
      <div class="text-center mb-5">
        <h1 class="display-5 fw-bold text-uppercase position-relative d-inline-block">
          Chính Sách
          <span class="header-underline"></span>
        </h1>
        <p class="text-muted mt-3">Cam kết mang đến trải nghiệm mua sắm tốt nhất cho khách hàng</p>
      </div>

      <!-- Grid layout cho các chính sách -->
      <div class="row g-4">
        <!-- Chính sách thanh toán -->
        <div class="" v-for="(policy, index) in policies" :key="index">
          <div class="policy-card h-100">
            <div class="policy-header">
              <div class="policy-icon">
                <i :class="policy.icon"></i>
              </div>
              <h3 class="policy-title">{{ policy.title }}</h3>
            </div>
            <div class="policy-content" v-html="policy.content"></div>
          </div>
        </div>
      </div>

      <!-- Footer message -->
      <div class="text-center mt-5 pt-4">
        <div class="brand-message d-flex align-items-center justify-content-center gap-3 flex-wrap">
          <router-link to="/customer/index" class="d-inline-block">
            <img src="/SHOEDO.png" alt="Shoedo" class="brand-logo" style="max-width: 40px; height: auto;">
          </router-link>
          <div class="text-start">
            <p class="lead fw-bold mb-0">SHOUDO - Localbrand giày Việt</p>
            <p class="text-muted small mb-0">Cám ơn bạn đã tin tưởng và ủng hộ shop!</p>
          </div>
        </div>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script>
import { ref } from 'vue';
import KH_Navbar from '@/components/shared/KH_Navbar.vue';
import Footer from '@/components/shared/Footer.vue';
export default {
  name: 'KH_ChinhSach',
  components: {
    KH_Navbar,
    Footer
  },
  setup() {

    const policies = ref([
      {
        icon: 'bi bi-credit-card',
        title: '1. Chính sách thanh toán',
        content: `
          <p class="mb-2"><strong>Hình thức thanh toán:</strong></p>
          <ul class="policy-list">
            <li><i class="bi bi-check-circle-fill text-success me-2"></i>Thanh toán khi nhận hàng (COD)</li>
            <li><i class="bi bi-check-circle-fill text-success me-2"></i>Chuyển khoản ngân hàng</li>
            <li><i class="bi bi-check-circle-fill text-success me-2"></i>Thanh toán qua ví điện tử</li>
          </ul>
          <div class="alert alert-light border-start border-3 border-dark mt-3 py-2">
            <i class="bi bi-info-circle-fill me-2"></i>
            <strong>Lưu ý:</strong> Với đơn hàng pre-order, shop có thể yêu cầu đặt cọc trước. Mọi chi phí phát sinh sẽ được thông báo rõ trước khi xác nhận đơn hàng.
          </div>
        `
      },
      {
        icon: 'bi bi-truck',
        title: '2. Chính sách giao hàng',
        content: `
          <ul class="policy-list">
            <li><i class="bi bi-clock-history me-2"></i><strong>Thời gian:</strong> 2 – 5 ngày làm việc (tùy khu vực)</li>
            <li><i class="bi bi-calendar-x me-2"></i><strong>Không tính:</strong> Ngày lễ, Tết hoặc trường hợp bất khả kháng</li>
            <li><i class="bi bi-box-seam me-2"></i><strong>Kiểm tra hàng:</strong> Khách được kiểm tra ngoại quan trước khi thanh toán</li>
          </ul>
        `
      },
      {
        icon: 'bi bi-arrow-repeat',
        title: '3. Chính sách đổi/trả hàng',
        content: `
          <div class="mb-3">
            <p class="fw-bold mb-2">3.1 Điều kiện đổi/trả:</p>
            <ul class="policy-list">
              <li><i class="bi bi-check-circle text-success me-2"></i>Sản phẩm lỗi do nhà sản xuất</li>
              <li><i class="bi bi-check-circle text-success me-2"></i>Giao sai mẫu, sai size</li>
              <li><i class="bi bi-check-circle text-success me-2"></i>Thời gian: 3 – 7 ngày kể từ khi nhận hàng</li>
              <li><i class="bi bi-check-circle text-success me-2"></i>Sản phẩm chưa sử dụng, còn nguyên tem</li>
              <li><i class="bi bi-check-circle text-success me-2"></i>Có vieo mở hàng để chứng minh đối chiếu</li>
            </ul>
          </div>
          <div class="mb-3">
            <p class="fw-bold mb-2 text-danger">3.2 Không hỗ trợ đổi trả:</p>
            <ul class="policy-list">
              <li><i class="bi bi-x-circle text-danger me-2"></i>Đã qua sử dụng, dơ bẩn</li>
              <li><i class="bi bi-x-circle text-danger me-2"></i>Hư hỏng do bảo quản sai cách</li>
              <li><i class="bi bi-x-circle text-danger me-2"></i>Sản phẩm xả kho, giảm giá sâu</li>
            </ul>
          </div>
          <div>
            <p class="fw-bold mb-2">3.3 Phí đổi/trả:</p>
            <ul class="policy-list">
              <li><i class="bi bi-shield-check me-2"></i>Lỗi từ shop: Miễn phí vận chuyển</li>
              <li><i class="bi bi-cash me-2"></i>Đổi do nhu cầu cá nhân: Khách chịu phí 2 chiều</li>
            </ul>
          </div>
        `
      },
      {
        icon: 'bi bi-shield-check',
        title: '4. Chính sách bảo hành',
        content: `
          <ul class="policy-list">
            <li><i class="bi bi-clock me-2"></i><strong>Thời gian:</strong> 1 – 3 tháng (tùy sản phẩm)</li>
            <li><i class="bi bi-check-circle me-2"></i><strong>Bảo hành:</strong> Lỗi kỹ thuật từ nhà sản xuất</li>
          </ul>
          <p class="fw-bold mt-3 mb-2">Không áp dụng bảo hành cho:</p>
          <ul class="policy-list">
            <li><i class="bi bi-dash-circle text-danger me-2"></i>Hao mòn tự nhiên</li>
            <li><i class="bi bi-dash-circle text-danger me-2"></i>Hư hỏng do ngâm nước, giặt máy</li>
            <li><i class="bi bi-dash-circle text-danger me-2"></i>Trầy xước do ma sát</li>
          </ul>
        `
      },
      {
        icon: 'bi bi-shield-lock',
        title: '5. Chính sách bảo mật',
        content: `
          <ul class="policy-list">
            <li><i class="bi bi-lock-fill me-2"></i>Cam kết bảo mật tuyệt đối thông tin khách hàng</li>
            <li><i class="bi bi-file-text me-2"></i>Chỉ sử dụng để xử lý đơn hàng và chăm sóc khách</li>
            <li><i class="bi bi-shield-slash me-2"></i>Không chia sẻ cho bên thứ ba khi chưa đồng ý</li>
          </ul>
        `
      },
      {
        icon: 'bi bi-cash-coin',
        title: '6. Chính sách hoàn tiền',
        content: `
          <ul class="policy-list">
            <li><i class="bi bi-check-circle me-2"></i>Áp dụng khi shop hết hàng thay thế</li>
            <li><i class="bi bi-clock-history me-2"></i>Thời gian xử lý: 3-7 ngày làm việc</li>
            <li><i class="bi bi-bank me-2"></i>Hình thức: Chuyển khoản ngân hàng</li>
          </ul>
        `
      },
      {
        icon: 'bi bi-file-text',
        title: '7. Điều khoản chung',
        content: `
          <ul class="policy-list">
            <li><i class="bi bi-pencil-square me-2"></i>Shop có quyền thay đổi chính sách không cần báo trước</li>
            <li><i class="bi bi-telephone me-2"></i>Mọi thắc mắc vui lòng liên hệ qua kênh chính thức</li>
          </ul>
        `
      }
    ]);

    return {
      policies
    };
  }
};
</script>

<style scoped>
/* Header styles */
.header-underline {
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: #000000;
  border-radius: 2px;
}

/* Policy cards */
.policy-card {
  background: #ffffff;
  border: 2px solid #000000;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.policy-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.2);
}

.policy-header {
  background: #000000;
  color: #ffffff;
  padding: 1rem 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.policy-icon {
  width: 40px;
  height: 40px;
  background: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #000000;
  font-size: 1.5rem;
}

.policy-title {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  flex: 1;
}

.policy-content {
  padding: 1.5rem;
  color: #333333;
  font-size: 0.95rem;
}

/* Policy list styling */
.policy-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.policy-list li {
  margin-bottom: 0.75rem;
  display: flex;
  align-items: flex-start;
  line-height: 1.4;
}

.policy-list li i {
  margin-top: 0.2rem;
  flex-shrink: 0;
}

/* Alert styling */
.alert-light {
  background-color: #f8f9fa;
  border-color: #000000 !important;
  font-size: 0.95rem;
}

/* Brand message - Flex layout */
.brand-message {
  padding: 2rem;
  background: #f8f9fa;
  border-radius: 12px;
  border: 2px dashed #000000;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1.5rem;
  flex-wrap: wrap;
}

/* Brand logo */
.brand-logo {
  max-width: 40px;
  height: auto;
  transition: all 0.3s ease;
  display: inline-block;
  flex-shrink: 0;
}

.brand-logo:hover {
  transform: scale(1.1);
}

/* Text container */
.brand-message .text-start {
  text-align: left;
}

.brand-message .lead {
  font-size: 1.2rem;
  margin-bottom: 0.15rem;
  font-weight: 600;
  color: #333;
}

.brand-message .small {
  font-size: 0.85rem;
  color: #6c757d;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .policy-header {
    flex-wrap: wrap;
  }

  .policy-title {
    font-size: 1rem;
  }

  .policy-content {
    padding: 1rem;
    font-size: 0.9rem;
  }

  .policy-list li {
    font-size: 0.9rem;
  }

  .brand-message {
    gap: 1rem;
    padding: 1.5rem;
  }

  .brand-logo {
    max-width: 35px;
  }

  .brand-message .lead {
    font-size: 1rem;
  }

  .brand-message .small {
    font-size: 0.8rem;
  }
}

/* Animation */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.policy-card {
  animation: fadeInUp 0.5s ease forwards;
}

.policy-card:nth-child(even) {
  animation-delay: 0.1s;
}
</style>