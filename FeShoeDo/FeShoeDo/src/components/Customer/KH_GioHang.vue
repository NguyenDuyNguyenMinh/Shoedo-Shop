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

      <h2 class="cart-title mb-4">
        <i class="bi bi-cart3 me-2"></i>Giỏ hàng của bạn
        <span class="cart-count">(3 sản phẩm)</span>
      </h2>

      <div class="row g-4">
        <!-- Cart Items -->
        <div class="col-lg-8">
          <!-- Select All -->
          <div class="cart-header d-flex justify-content-between align-items-center mb-3">
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="selectAll" checked>
              <label class="form-check-label fw-semibold" for="selectAll">Chọn tất cả (3)</label>
            </div>
            <button class="btn btn-outline-danger btn-sm">
              <i class="bi bi-trash me-1"></i>Xóa đã chọn
            </button>
          </div>

          <!-- Cart Item 1 -->
          <div class="cart-item">
            <div class="row align-items-center">
              <div class="col-auto">
                <input class="form-check-input" type="checkbox" checked>
              </div>
              <div class="col-auto">
                <div class="cart-item-image">
                  <img src="https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=200&h=200&fit=crop" alt="Giày da trắng">
                </div>
              </div>
              <div class="col">
                <div class="cart-item-info">
                  <h6 class="cart-item-name">SP001-ShoeDo - SP1 - GD</h6>
                  <div class="cart-item-variant">
                    <span class="variant-tag">Trắng</span>
                    <span class="variant-tag">Size 40</span>
                  </div>
                  <div class="cart-item-price mt-2">
                    <span class="price-current">2.500.000₫</span>
                    <span class="price-discount-tag">-10%</span>
                    <span class="price-original">2.777.778₫</span>
                  </div>
                </div>
              </div>
              <div class="col-auto">
                <div class="quantity-control">
                  <button class="qty-btn">
                    <i class="bi bi-dash"></i>
                  </button>
                  <input type="number" class="qty-input" value="1" min="1">
                  <button class="qty-btn">
                    <i class="bi bi-plus"></i>
                  </button>
                </div>
              </div>
              <div class="col-auto text-end" style="min-width: 130px;">
                <div class="cart-item-total">2.500.000₫</div>
                <button class="btn btn-sm btn-outline-danger mt-2 border-0">
                  <i class="bi bi-trash"></i>
                </button>
              </div>
            </div>
          </div>

          <!-- Cart Item 2 -->
          <div class="cart-item">
            <div class="row align-items-center">
              <div class="col-auto">
                <input class="form-check-input" type="checkbox" checked>
              </div>
              <div class="col-auto">
                <div class="cart-item-image">
                  <img src="https://images.unsplash.com/photo-1539185441755-769473a23570?w=200&h=200&fit=crop" alt="Giày cao gót đỏ">
                </div>
              </div>
              <div class="col">
                <div class="cart-item-info">
                  <h6 class="cart-item-name">SP003-ShoeDo - SP3 - GD</h6>
                  <div class="cart-item-variant">
                    <span class="variant-tag">Đỏ</span>
                    <span class="variant-tag">Size 37</span>
                  </div>
                  <div class="cart-item-price mt-2">
                    <span class="price-current">4.500.000₫</span>
                    <span class="price-discount-tag">-30%</span>
                    <span class="price-original">6.428.571₫</span>
                  </div>
                </div>
              </div>
              <div class="col-auto">
                <div class="quantity-control">
                  <button class="qty-btn">
                    <i class="bi bi-dash"></i>
                  </button>
                  <input type="number" class="qty-input" value="2" min="1">
                  <button class="qty-btn">
                    <i class="bi bi-plus"></i>
                  </button>
                </div>
              </div>
              <div class="col-auto text-end" style="min-width: 130px;">
                <div class="cart-item-total">9.000.000₫</div>
                <button class="btn btn-sm btn-outline-danger mt-2 border-0">
                  <i class="bi bi-trash"></i>
                </button>
              </div>
            </div>
          </div>

          <!-- Cart Item 3 -->
          <div class="cart-item">
            <div class="row align-items-center">
              <div class="col-auto">
                <input class="form-check-input" type="checkbox" checked>
              </div>
              <div class="col-auto">
                <div class="cart-item-image">
                  <img src="https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=200&h=200&fit=crop" alt="Boot đen">
                </div>
              </div>
              <div class="col">
                <div class="cart-item-info">
                  <h6 class="cart-item-name">SP008-ShoeDo - SP1 - GBT</h6>
                  <div class="cart-item-variant">
                    <span class="variant-tag">Đen</span>
                    <span class="variant-tag">Size 41</span>
                  </div>
                  <div class="cart-item-price mt-2">
                    <span class="price-current">3.500.000₫</span>
                  </div>
                </div>
              </div>
              <div class="col-auto">
                <div class="quantity-control">
                  <button class="qty-btn">
                    <i class="bi bi-dash"></i>
                  </button>
                  <input type="number" class="qty-input" value="1" min="1">
                  <button class="qty-btn">
                    <i class="bi bi-plus"></i>
                  </button>
                </div>
              </div>
              <div class="col-auto text-end" style="min-width: 130px;">
                <div class="cart-item-total">3.500.000₫</div>
                <button class="btn btn-sm btn-outline-danger mt-2 border-0">
                  <i class="bi bi-trash"></i>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Order Summary Sidebar -->
        <div class="col-lg-4">
          <div class="order-summary sticky-top" style="top: 100px;">
            <h5 class="summary-title">Tóm tắt đơn hàng</h5>
            
            <div class="summary-row">
              <span>Tạm tính (3 sản phẩm)</span>
              <span class="fw-semibold">15.000.000₫</span>
            </div>
            <div class="summary-row">
              <span>Giảm giá</span>
              <span class="text-success fw-semibold">-0₫</span>
            </div>
            <div class="summary-row">
              <span>Phí vận chuyển</span>
              <span class="text-success fw-semibold">Miễn phí</span>
            </div>
            
            <hr>
            
            <div class="summary-row total-row">
              <span>Tổng cộng</span>
              <span>15.000.000₫</span>
            </div>

            <!-- Voucher -->
            <div class="voucher-section mt-3">
              <div class="input-group">
                <input type="text" class="form-control" placeholder="Nhập mã giảm giá">
                <button class="btn btn-outline-dark">Áp dụng</button>
              </div>
            </div>

            <a href="/customer/checkout" class="btn btn-dark w-100 mt-3 py-3 checkout-btn">
              <i class="bi bi-credit-card me-2"></i>Tiến hành đặt hàng
            </a>

            <div class="text-center mt-3">
              <a href="/customer/index" class="text-muted text-decoration-none small">
                <i class="bi bi-arrow-left me-1"></i>Tiếp tục mua sắm
              </a>
            </div>

            <!-- Trust badges -->
            <div class="trust-badges mt-4">
              <div class="trust-item">
                <i class="bi bi-shield-check"></i>
                <span>Bảo mật thanh toán</span>
              </div>
              <div class="trust-item">
                <i class="bi bi-truck"></i>
                <span>Giao hàng toàn quốc</span>
              </div>
              <div class="trust-item">
                <i class="bi bi-arrow-repeat"></i>
                <span>Đổi trả 30 ngày</span>
              </div>
            </div>
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
  name: 'KH_GioHang',
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

.checkout-btn:hover {
  background: #333;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
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
