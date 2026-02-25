<template>
  <div class="auth-page" :style="{ backgroundImage: `url(${backgroundImage})` }">
    <div class="bg-overlay"></div>
    <!-- Forgot Password Modal -->
    <div class="modal fade" id="forgotPasswordModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Quên mật khẩu</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>

          <div class="modal-body">
            <div class="mb-3">
              <label>Email</label>
              <input v-model="forgotPasswordEmail" type="email" class="form-control" required>
            </div>

            <div class="alert alert-info">
              <small>Chúng tôi sẽ gửi mật khẩu mới đến email của bạn.</small>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-primary">Xác nhận</button>
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
          </div>

        </div>
      </div>
    </div>
    <!-- Điều khoản Modal -->
    <div class="modal fade" id="about" tabindex="-1" aria-hidden="true">
       <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Điều khoản sử dụng</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                  <div class="mb-3">
                  	<label>Shoedo brand do nhóm dự án tốt nghiệp chúng tôi nghĩ ra nhằm cho phép mọi người mua những sản phẩm giày chất lượng nhưng giá cả phải chăng</label>
                  </div>
                </div>	
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                </div>
            </div>
        </div>
    </div>

    <div class="auth-wrapper">
      <!-- Left section -->
      <div class="auth-left">
        <h2>Welcome to ShoeDo Shop!</h2>
        <p>Khám phá bộ sưu tập giày thể thao mới nhất.</p>
        <p>Đăng nhập để trải nghiệm mua sắm tuyệt vời!</p>
      </div>

      <!-- Right section -->
      <div class="auth-right">
        <ul class="nav nav-tabs" id="authTabs" role="tablist">
          <li class="nav-item">
            <button class="nav-link active" id="login-tab" data-bs-toggle="tab" data-bs-target="#login"
              type="button">Login</button>
          </li>
          <li class="nav-item">
            <button class="nav-link" id="register-tab" data-bs-toggle="tab" data-bs-target="#register"
              type="button">Sign up</button>
          </li>
        </ul>

        <!-- Hiển thị thông báo -->
        <div v-if="message" class="alert alert-success alert-dismissible fade show mt-3">
          <span>{{ message }}</span>
          <button type="button" class="btn-close" @click="message = ''"></button>
        </div>
        
        <div v-if="error" class="alert alert-danger alert-dismissible fade show mt-3">
          <span>{{ error }}</span>
          <button type="button" class="btn-close" @click="error = ''"></button>
        </div>

        <div class="tab-content form-section" id="authTabsContent">
          <!-- LOGIN -->
          <div class="tab-pane fade show active" id="login" role="tabpanel" aria-labelledby="login-tab">
            <form @submit.prevent>
              <div class="mt-3 mb-3">
                <label for="loginIdentifier">Tài khoản</label>
                <input 
                  type="text" 
                  class="form-control" 
                  id="loginIdentifier" 
                  v-model="loginForm.identifier" 
                  placeholder="Nhập username hoặc email"
                  required
                >
                <small class="text-muted">Bạn có thể đăng nhập bằng username hoặc email</small>
              </div>
              <div class="mb-3">
                <label for="loginPassword">Mật khẩu</label>
                <input type="password" class="form-control" id="loginPassword" v-model="loginForm.pass" required>
              </div>
              
              <!-- Hiển thị thông báo tài khoản bị khóa nếu có -->
              <div v-if="accountLocked" class="alert alert-warning alert-dismissible fade show mt-2 mb-2">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                <span>{{ accountLockedMessage }}</span>
              </div>
              
              <div class="d-flex justify-content-between align-items-center mb-3">
                <div class="form-check">
                  <input class="form-check-input" type="checkbox" id="rememberMe" v-model="loginForm.remember">
                  <label for="rememberMe">Ghi nhớ đăng nhập</label>
                </div>
                 <a href="#" data-bs-toggle="modal" data-bs-target="#forgotPasswordModal">Quên mật khẩu?</a>
              </div>
              <div class="d-grid">
                <button type="submit" class="btn btn-dark">
                  Đăng nhập
                </button>
              </div>
              <br>
              <div class="position-relative text-center mb-3">
                <hr class="position-absolute top-50 start-0 end-0 m-0">
                <span class="px-3 bg-white text-muted position-relative" style="z-index: 1;">
                   Hoặc
                </span>
              </div>
              <a href="#" class="btn btn-outline-dark d-flex align-items-center justify-content-center gap-2 mx-auto"
                style="width: 210px; border-radius: 50px;">
                <img :src="googleLogo" alt="Google Logo" style="width: 20px; height: 20px;">
                <span>Sign in with Google</span>
              </a>
            </form>
          </div>

          <!-- REGISTER -->
          <div class="tab-pane fade" id="register" role="tabpanel" aria-labelledby="register-tab">
            <form @submit.prevent>
              <div class="mt-3 mb-3">
                <label for="regMail">Email</label>
                <input type="email" class="form-control" id="regMail" v-model="registerForm.mail" required>
              </div>
              <div class="mb-3">
                <label for="regPassword">Mật khẩu</label>
                <input type="password" class="form-control" id="regPassword" v-model="registerForm.pass" required>
              </div>
              <div class="mb-3">
                <label for="regFullname">Họ và tên</label>
                <input type="text" class="form-control" id="regFullname" v-model="registerForm.fullname" required>
              </div>
              <div class="mb-3">
                <label for="regPhone">Số điện thoại</label>
                <input type="tel" class="form-control" id="regPhone" v-model="registerForm.phone" 
                       pattern="[0-9]{9,11}" required>
                <small class="text-muted">Nhập 9-11 số điện thoại</small>
              </div>
              <div class="form-check mb-3">
                <input class="form-check-input" type="checkbox" id="termsCheck" v-model="registerForm.terms" required>
                <label class="form-check-label" for="termsCheck">
                  Tôi đồng ý với <a href="#" data-bs-toggle="modal" data-bs-target="#about">điều khoản sử dụng</a>
                </label>
              </div>
              
              <div class="d-grid">
                <button type="submit" class="btn btn-dark">
                  Đăng ký
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import googleLogoImg from '@/assets/anh/logo GG.png'
import backgroundImg from '@/assets/anh/login2.jpg'
export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        identifier: '',
        pass: '',
        remember: false
      },
      registerForm: {
        mail: '',
        pass: '',
        fullname: '',
        phone: '',
        terms: false,
        remember: false
      },
      forgotPasswordEmail: '',
      showForgotPasswordModal: false,
      showTermsModal: false,
      message: 'Chào mừng bạn đến với ShoeDo Shop!',
      error: '',
      accountLocked: false,
      accountLockedMessage: 'Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.'
    };
  },
  computed: {
    googleLogo() {
      return googleLogoImg; // Dùng ảnh đã import
    },
    backgroundImage() {
      return backgroundImg; // Dùng ảnh đã import
    }
  }
};
</script>

<style scoped>
.auth-page {
  font-family: 'Segoe UI', sans-serif;
  height: 100vh;
  margin: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background-size: cover;
}

.bg-overlay {
  background: rgba(255, 255, 255, 0.101);
  min-height: 100vh;
  width: 100vw;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 0;
}

.auth-wrapper {
  display: flex;
  width: 750px; 
  height: 550px;
  box-shadow: 0 0 40px rgba(0, 0, 0, 0.35);
  border-radius: 24px;
  overflow: hidden;
  background-color: rgba(255, 255, 255, 0.98);
  margin-right: 2%;
  z-index: 1;
  position: relative;
  border: 3px solid #000000;
}

.auth-left {
  width: 45%;
  background: url('https://tse4.mm.bing.net/th/id/OIP.iUUYL09-3TPcrABpDiw-XgAAAA?rs=1&pid=ImgDetMain&o=7&rm=3') no-repeat center center;
  background-size: cover;
  color: white;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
}

.auth-left::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.35);
  z-index: 1;
}

.auth-left h2,
.auth-left p {
  position: relative;
  z-index: 2;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.auth-left h2 {
  font-size: 32px;
  font-weight: bold;
}

.auth-right {
  width: 55%;
  padding: 25px;
  background-color: white;
  overflow-y: auto;
}

.nav-tabs .nav-link {
  color: #000000;
  border: 1px solid transparent;
  font-weight: 500;
}

.nav-tabs .nav-link.active {
  color: #fff !important;
  background-color: #000000 !important;
}

.form-section {
  margin-top: 15px;
}

.alert {
  margin-top: 15px;
  margin-bottom: 10px;
}

/* Style cho thông báo tài khoản bị khóa */
.alert-warning {
  background-color: #fff3cd;
  border-color: #ffeeba;
  color: #856404;
  font-size: 0.9rem;
  padding: 10px;
}

/* Style cho input placeholder */
::placeholder {
  color: #999;
  font-size: 0.9rem;
}

/* Style cho small text */
small {
  font-size: 0.8rem;
  color: #666;
}
</style>