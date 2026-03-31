<template>
  <footer class="shoedo-footer mt-4">
    <div class="footer-highlights py-4">
      <div class="container">
        <div class="row g-4 text-center">
          <div class="col-md-3 col-sm-6">
            <div class="highlight-item">
              <i class="bi bi-truck fs-2 mb-2"></i>
              <h6 class="fw-bold mb-1">GIAO HÀNG NHANH CHÓNG</h6>
              <p class="small mb-0 text-muted">Thời gian từ 3 - 7 ngày</p>
            </div>
          </div>
          <div class="col-md-3 col-sm-6">
            <div class="highlight-item">
              <i class="bi bi-arrow-left-right fs-2 mb-2"></i>
              <h6 class="fw-bold mb-1">HỖ TRỢ ĐỔI HÀNG</h6>
              <p class="small mb-0 text-muted">Báo lỗi để đổi sản phẩm</p>
            </div>
          </div>
          <div class="col-md-3 col-sm-6">
            <div class="highlight-item">
              <i class="bi bi-shield-check fs-2 mb-2"></i>
              <h6 class="fw-bold mb-1">BẢO HÀNH CHÍNH HÃNG</h6>
              <p class="small mb-0 text-muted">Lên đến 1 tháng</p>
            </div>
          </div>
          <div class="col-md-3 col-sm-6">
            <div class="highlight-item">
              <i class="bi bi-headset fs-2 mb-2"></i>
              <h6 class="fw-bold mb-1">HỖ TRỢ 24/7</h6>
              <p class="small mb-0 text-muted">Hotline: 1900 6869</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="footer-main py-5">
      <div class="container">
        <div class="row g-4">
          <div class="col-lg-4 col-md-6">
            <div class="footer-brand mb-4">
              <h2 class="fw-black mb-3 text-dark">SHOEDO</h2>
              <p class="text-muted pe-lg-4">
                ShoeDo tự hào là thương hiệu Local Brand giày Việt Nam, mang đến những sản phẩm chất lượng, phong cách hiện đại với mức giá phải chăng cho thế hệ trẻ.
              </p>
            </div>
            <ul class="list-unstyled contact-info">
              <li class="mb-3 d-flex">
                <i class="bi bi-geo-alt me-3 fs-5 text-dark"></i>
                <span class="text-muted">Công viên Phần mềm Quang Trung, Quận 12, TP. Hồ Chí Minh</span>
              </li>
              <li class="mb-3 d-flex align-items-center">
                <i class="bi bi-telephone me-3 fs-5 text-dark"></i>
                <span class="text-muted">Hotline: <strong class="text-dark">1900 6869</strong></span>
              </li>
              <li class="mb-3 d-flex align-items-center">
                <i class="bi bi-envelope me-3 fs-5 text-dark"></i>
                <span class="text-muted">Email: cskh@shoedo.vn</span>
              </li>
            </ul>
          </div>

          <div class="col-lg-2 col-md-6">
            <h5 class="footer-title mb-4 text-dark">HỖ TRỢ KHÁCH HÀNG</h5>
            <ul class="list-unstyled footer-links">
              <li><router-link to="/customer/chinhsach">Chính sách giao hàng</router-link></li>
              <li><router-link to="/customer/chinhsach">Chính sách đổi hàng</router-link></li>
              <li><router-link to="/customer/chinhsach">Chính sách bảo hành</router-link></li>
              <li><router-link to="/customer/chinhsach">Chính sách thanh toán</router-link></li>
              <li><router-link to="/customer/chinhsach">Bảo mật thông tin</router-link></li>
            </ul>
          </div>

          <div class="col-lg-2 col-md-6">
            <h5 class="footer-title mb-4 text-dark">VỀ SHOEDO</h5>
            <ul class="list-unstyled footer-links">
              <li><a href="#">Giới thiệu thương hiệu</a></li>
              <li><a href="#">Hệ thống cửa hàng</a></li>
              <li><a href="#">Tuyển dụng</a></li>
              <li><a href="#">Tin tức & Blogs</a></li>
              <li><a href="#">Liên hệ</a></li>
            </ul>
          </div>

          <div class="col-lg-4 col-md-6">
            <!-- <h5 class="footer-title mb-4 text-dark">ĐĂNG KÝ NHẬN TIN</h5>
            <p class="text-muted mb-3">Nhận ngay thông tin về các bộ sưu tập và chương trình khuyến mãi mới nhất của ShoeDo.</p>
            <form class="newsletter-form mb-4" @submit.prevent="subscribeNewsletter">
              <div class="input-group">
                <input type="email" class="form-control" v-model="email" placeholder="Nhập email của bạn..." required>
                <button class="btn btn-dark btn-subscribe" type="submit">ĐĂNG KÝ</button>
              </div>
            </form> -->

            <h5 class="footer-title mb-3 text-dark">KẾT NỐI VỚI CHÚNG TÔI</h5>
            <div class="social-links d-flex gap-2">
              <a href="#" class="social-icon"><i class="bi bi-facebook"></i></a>
              <a href="#" class="social-icon"><i class="bi bi-instagram"></i></a>
              <a href="#" class="social-icon"><i class="bi bi-tiktok"></i></a>
              <a href="#" class="social-icon"><i class="bi bi-youtube"></i></a>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="footer-bottom py-3">
      <div class="container">
        <div class="row align-items-center">
          <div class="col-md-8 text-center text-md-start mb-3 mb-md-0">
            <p class="mb-0 text-muted small">
              &copy; 2026 SHOEDO SHOP. All rights reserved. <br class="d-md-none">
              Phát triển bởi SHOEDO DEVELOPERS.
            </p>
          </div>
          <div class="col-md-4 text-center text-md-end payment-methods">
            <i class="bi bi-credit-card mx-2 fs-4 text-muted" title="Credit Card"></i>
            <i class="bi bi-cash-stack mx-2 fs-4 text-muted" title="COD"></i>
            <i class="bi bi-bank mx-2 fs-4 text-muted" title="Bank Transfer"></i>
            <i class="bi bi-wallet2 mx-2 fs-4 text-muted" title="E-Wallets"></i>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { ref } from 'vue';

const email = ref('');

const subscribeNewsletter = () => {
  if (email.value) {
    alert(`Cảm ơn bạn đã đăng ký nhận tin với email: ${email.value}! ShoeDo sẽ liên hệ với bạn sớm nhất.`);
    email.value = '';
  }
};
</script>

<style scoped>
.shoedo-footer {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #ffffff; /* Nền trắng chủ đạo */
  color: #000000;
  border-top: 4px solid #000000; /* Viền đen dày giống các card trên web */
}

/* --- Policy Highlights Bar --- */
.footer-highlights {
  background-color: #f8f9fa; /* Màu xám nhạt để tách biệt nhẹ nhàng */
  border-bottom: 2px solid #000000;
}

.highlight-item {
  transition: transform 0.3s ease;
}

.highlight-item:hover {
  transform: translateY(-5px);
}

.highlight-item i {
  color: #000000; /* Icon màu đen */
}

/* --- Main Footer --- */
.fw-black {
  font-weight: 900;
  letter-spacing: 2px;
}

.footer-title {
  font-size: 1.1rem;
  font-weight: 700;
  letter-spacing: 1px;
  position: relative;
  padding-bottom: 10px;
}

.footer-title::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 40px;
  height: 3px;
  background-color: #000000; /* Gạch dưới màu đen */
  border-radius: 2px;
}

.footer-links li {
  margin-bottom: 10px;
}

.footer-links a {
  color: #666666;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  display: inline-block;
}

.footer-links a:hover {
  color: #000000;
  transform: translateX(5px);
  text-decoration: underline;
}

/* --- Newsletter Form --- */
.newsletter-form .form-control {
  background-color: #ffffff;
  border: 2px solid #000000;
  color: #000000;
  box-shadow: none;
}

.newsletter-form .form-control:focus {
  border-color: #000000;
  box-shadow: none;
}

.btn-subscribe {
  border: 2px solid #000000;
  font-weight: 700;
}

/* --- Social Icons --- */
.social-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  color: #000000;
  border: 2px solid #000000; /* Viền đen, chuẩn phong cách web bạn */
  border-radius: 50%;
  font-size: 1.2rem;
  text-decoration: none;
  transition: all 0.3s ease;
}

.social-icon:hover {
  background-color: #000000;
  color: #ffffff;
  transform: translateY(-3px);
}

/* --- Footer Bottom --- */
.footer-bottom {
  background-color: #ffffff;
  border-top: 1px solid #e0e0e0;
}

.payment-methods i {
  transition: color 0.3s ease;
}

.payment-methods i:hover {
  color: #000000 !important;
}
</style>