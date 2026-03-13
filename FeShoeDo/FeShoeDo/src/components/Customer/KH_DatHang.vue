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
              <button class="btn btn-outline-dark btn-sm">
                <i class="bi bi-plus-circle me-1"></i>Thêm địa chỉ mới
              </button>
            </div>

            <!-- Địa chỉ 1 - Mặc định -->
            <div class="address-option selected">
              <div class="d-flex align-items-start gap-3">
                <input class="form-check-input mt-1" type="radio" name="address" id="addr1" checked>
                <label for="addr1" class="flex-fill">
                  <div class="d-flex align-items-center gap-2 mb-1">
                    <span class="fw-bold">Nguyễn Văn A</span>
                    <span class="text-muted">|</span>
                    <span class="text-muted">0901234567</span>
                    <span class="badge bg-dark ms-1">Mặc định</span>
                  </div>
                  <div class="text-muted small">123 Nguyễn Huệ A, Quận 1, TP.HCM</div>
                </label>
              </div>
            </div>

            <!-- Địa chỉ 2 -->
            <div class="address-option">
              <div class="d-flex align-items-start gap-3">
                <input class="form-check-input mt-1" type="radio" name="address" id="addr2">
                <label for="addr2" class="flex-fill">
                  <div class="d-flex align-items-center gap-2 mb-1">
                    <span class="fw-bold">Nguyễn Văn A-B</span>
                    <span class="text-muted">|</span>
                    <span class="text-muted">0901234567</span>
                  </div>
                  <div class="text-muted small">123 Nguyễn Huệ B, Quận 1, TP.HCM</div>
                </label>
              </div>
            </div>

            <!-- Địa chỉ 3 -->
            <div class="address-option">
              <div class="d-flex align-items-start gap-3">
                <input class="form-check-input mt-1" type="radio" name="address" id="addr3">
                <label for="addr3" class="flex-fill">
                  <div class="d-flex align-items-center gap-2 mb-1">
                    <span class="fw-bold">Nguyễn Văn A-C</span>
                    <span class="text-muted">|</span>
                    <span class="text-muted">0901234567</span>
                  </div>
                  <div class="text-muted small">123 Nguyễn Huệ C, Quận 1, TP.HCM</div>
                </label>
              </div>
            </div>
          </div>

          <!-- 2. Phương thức thanh toán -->
          <div class="checkout-section">
            <div class="section-header">
              <h5><i class="bi bi-wallet2 me-2"></i>Phương thức thanh toán</h5>
            </div>

            <div class="payment-option selected">
              <div class="d-flex align-items-center gap-3">
                <input class="form-check-input" type="radio" name="payment" id="payCOD" checked>
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

            <div class="payment-option">
              <div class="d-flex align-items-center gap-3">
                <input class="form-check-input" type="radio" name="payment" id="payTransfer">
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

          <!-- 3. Ghi chú -->
          <div class="checkout-section">
            <div class="section-header">
              <h5><i class="bi bi-chat-left-text me-2"></i>Ghi chú đơn hàng</h5>
            </div>
            <textarea class="form-control" rows="3" placeholder="Ghi chú cho người bán (ví dụ: giao giờ hành chính, gọi trước khi giao...)"></textarea>
          </div>
        </div>

        <!-- Right: Tóm tắt đơn hàng -->
        <div class="col-lg-5">
          <div class="order-review sticky-top" style="top: 100px;">
            <h5 class="review-title">Đơn hàng của bạn</h5>

            <!-- Sản phẩm 1 -->
            <div class="review-item">
              <div class="d-flex gap-3">
                <div class="review-item-image">
                  <img src="https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=100&h=100&fit=crop" alt="">
                  <span class="item-qty-badge">1</span>
                </div>
                <div class="flex-fill">
                  <div class="review-item-name">SP001-ShoeDo - SP1 - GD</div>
                  <div class="review-item-variant">Trắng / Size 40</div>
                </div>
                <div class="review-item-price">2.500.000₫</div>
              </div>
            </div>

            <!-- Sản phẩm 2 -->
            <div class="review-item">
              <div class="d-flex gap-3">
                <div class="review-item-image">
                  <img src="https://images.unsplash.com/photo-1539185441755-769473a23570?w=100&h=100&fit=crop" alt="">
                  <span class="item-qty-badge">2</span>
                </div>
                <div class="flex-fill">
                  <div class="review-item-name">SP003-ShoeDo - SP3 - GD</div>
                  <div class="review-item-variant">Đỏ / Size 37</div>
                </div>
                <div class="review-item-price">9.000.000₫</div>
              </div>
            </div>

            <!-- Sản phẩm 3 -->
            <div class="review-item">
              <div class="d-flex gap-3">
                <div class="review-item-image">
                  <img src="https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=100&h=100&fit=crop" alt="">
                  <span class="item-qty-badge">1</span>
                </div>
                <div class="flex-fill">
                  <div class="review-item-name">SP008-ShoeDo - SP1 - GBT</div>
                  <div class="review-item-variant">Đen / Size 41</div>
                </div>
                <div class="review-item-price">3.500.000₫</div>
              </div>
            </div>

            <hr>

            <!-- Voucher -->
            <div class="mb-3">
              <div class="input-group">
                <input type="text" class="form-control" placeholder="Nhập mã giảm giá">
                <button class="btn btn-outline-dark">Áp dụng</button>
              </div>
            </div>

            <hr>

            <!-- Tổng -->
            <div class="summary-line">
              <span>Tạm tính</span>
              <span class="fw-semibold">15.000.000₫</span>
            </div>
            <div class="summary-line">
              <span>Phí vận chuyển</span>
              <span class="text-success fw-semibold">Miễn phí</span>
            </div>
            <div class="summary-line">
              <span>Giảm giá</span>
              <span class="text-success fw-semibold">-0₫</span>
            </div>

            <hr>

            <div class="summary-line total-line">
              <span>Tổng thanh toán</span>
              <span>15.000.000₫</span>
            </div>

            <button class="btn btn-dark w-100 mt-3 py-3 place-order-btn">
              <i class="bi bi-bag-check me-2"></i>Đặt hàng
            </button>

            <p class="text-center text-muted small mt-3">
              Bằng việc nhấn "Đặt hàng", bạn đồng ý với 
              <a href="#" class="text-dark">Điều khoản dịch vụ</a> và 
              <a href="#" class="text-dark">Chính sách bảo mật</a>
            </p>
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

export default {
  name: 'KH_DatHang',
  components: { KH_Navbar, Footer }
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

.place-order-btn:hover {
  background: #333;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
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
