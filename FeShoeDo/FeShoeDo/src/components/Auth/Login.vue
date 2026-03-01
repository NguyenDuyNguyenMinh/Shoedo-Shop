<template>
  <div class="auth-page">
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
            <button type="button" class="btn btn-primary" @click="handleForgotPassword">Xác nhận</button>
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
            <form @submit.prevent="handleLogin">
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
                <button type="submit" class="btn btn-dark" :disabled="loading">
                  <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  {{ loading ? 'Đang xử lý...' : 'Đăng nhập' }}
                </button>
              </div>
              <br>
              <div class="position-relative text-center mb-3">
                <hr class="position-absolute top-50 start-0 end-0 m-0">
                <span class="px-3 bg-white text-muted position-relative" style="z-index: 1;">
                   Hoặc
                </span>
              </div>
              <a href="http://localhost:8080/oauth2/authorization/google" 
                class="btn btn-outline-dark d-flex align-items-center justify-content-center gap-2 mx-auto"
                style="width: 210px; border-radius: 50px;">
                <img :src="getImageUrl('anh/logo GG.png')" alt="Google Logo" style="width: 20px; height: 20px;">
                <span>Sign in with Google</span>
              </a>
            </form>
          </div>

          <!-- REGISTER -->
          <div class="tab-pane fade" id="register" role="tabpanel" aria-labelledby="register-tab">
            <form @submit.prevent="handleRegister">
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
                <button type="submit" class="btn btn-dark" :disabled="loading">
                  <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  {{ loading ? 'Đang xử lý...' : 'Đăng ký' }}
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
import { useAuthStore } from '@/stores/auth';

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
      loading: false,
      message: '',
      error: '',
      accountLocked: false,
      accountLockedMessage: ''
    };
  },

  mounted() {
    this.handleGoogleCallback();
    const authStore = useAuthStore();
    if (authStore.isAuthenticated) {
      this.redirectByRole(authStore);
    }
  },

  methods: {
    getImageUrl(imagePath) {
      return `http://localhost:8080/${imagePath}`;
    },
    
    async handleLogin() {
      this.loading = true;
      this.error = '';
      this.message = '';
      this.accountLocked = false;
      this.accountLockedMessage = '';
      
      const authStore = useAuthStore();
      
      try {
        const result = await authStore.login(this.loginForm);
        
        if (result.success) {
          this.message = 'Đăng nhập thành công!';
          
          setTimeout(() => {
            if (authStore.isCustomer) {
              this.$router.push('/customer/index');
            } else if (authStore.isAdmin) {
              this.$router.push('/employee/dashboard');
            } else if (authStore.isEmployee) {
              this.$router.push('/employee/products');
            } else {
              this.$router.push('/customer/index');
            }
          }, 1000);
        } else {
          this.error = result.message || 'Đăng nhập thất bại';
          
          if (result.message && result.message.includes('bị khóa')) {
            this.accountLocked = true;
            this.accountLockedMessage = result.message;

            setTimeout(() => {
              this.accountLockedMessage += ' Vui lòng liên hệ quản trị viên qua email: admin@shoedoshop.com';
            }, 100);
          }
          
          console.error('Login failed:', result.message);
        }
      } catch (error) {
        console.error('Login error:', error);
        this.error = 'Đăng nhập thất bại. Vui lòng thử lại sau.';
      } finally {
        this.loading = false;
      }
    },
    
    async handleRegister() {
      this.loading = true;
      this.error = '';
      this.message = '';

      if (!this.registerForm.terms) {
        this.error = 'Vui lòng đồng ý với điều khoản sử dụng';
        this.loading = false;
        return;
      }
      const authStore = useAuthStore();
      const result = await authStore.register(this.registerForm);
      
      if (result.success) {
        this.message = 'Đăng ký thành công!';

        setTimeout(() => {
          this.redirectByRole(authStore);
        }, 1000);
      } else {
        this.error = result.message || 'Đăng ký thất bại';
      }
      
      this.loading = false;
    },
    
    redirectByRole(authStore) {
      if (authStore.isCustomer) {
        this.$router.push('/customer/index');
      } else if (authStore.isEmployee) {
        this.$router.push('/employee/dashboard');
      } else if (authStore.isAdmin) {
        this.$router.push('/employee/dashboard');
      } else {
        this.$router.push('/customer/index');
      }
    },
    
    async handleGoogleCallback() {
      try {
        const urlParams = new URLSearchParams(window.location.search);
        const googleSuccess = urlParams.get('googleSuccess');
        const email = urlParams.get('email');
        const name = urlParams.get('name');
        
        if (googleSuccess === 'true' && email) {
           const response = await fetch('/api/oauth2/google/callback?email=' + encodeURIComponent(email) + '&name=' + encodeURIComponent(name || ''), {
            credentials: 'include'
          });
          
          const data = await response.json();
          
          if (data.success) {
            if (data.requirePassword) {
              this.showPasswordPrompt(data.email, data.name);
            } else {
              await this.completeGoogleLogin(data.email, data.name);
            }
          } else {
            this.error = data.message || 'Đăng nhập Google thất bại';
            if (data.message && data.message.includes('bị khóa')) {
              this.accountLocked = true;
              this.accountLockedMessage = data.message + ' Vui lòng liên hệ quản trị viên.';
            }
          }
        }
      } catch (error) {
        console.error('Google callback error:', error);
        this.error = 'Lỗi xử lý đăng nhập Google';
      }
    },

    showPasswordPrompt(email, name) {
      const password = prompt(
        `🔐 TÀI KHOẢN GOOGLE\n\n` +
        `Email: ${email}\n` +
        `Tên: ${name || 'Google User'}\n\n` +
        `Vui lòng nhập mật khẩu bạn muốn đặt cho tài khoản này:`,
        ''
      );
      
      if (password && password.trim() !== '') {
        this.completeGoogleLoginNewUser(email, name, password);
      } else if (password !== null) {
        alert('Vui lòng nhập mật khẩu để tiếp tục!');
        this.showPasswordPrompt(email, name);
      }
    },

     async completeGoogleLoginNewUser(email, name, password) {
      this.loading = true;
      try {
        const response = await fetch('/api/oauth2/google-login-newuesr', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include',
          body: JSON.stringify({ email, name, password })
        });
        
        const data = await response.json();
        
        if (data.success) {
          console.log('Google login with password successful');
          
          const authStore = useAuthStore();
          authStore.user = data.user;
          localStorage.setItem('user', JSON.stringify(data.user));
          
          window.history.replaceState({}, document.title, window.location.pathname);
          
          this.message = 'Đăng nhập Google thành công!';
          
          setTimeout(() => {
            this.$router.push('/customer/index');
          }, 1000);
        } else {
          this.error = data.message || 'Đăng nhập Google thất bại';
          console.error('Google login failed:', data.message);
        }
      } catch (error) {
        console.error('Google login error:', error);
        this.error = 'Lỗi xử lý đăng nhập Google';
      } finally {
        this.loading = false;
      }
    },
    
    async completeGoogleLogin(email, name) {
      this.loading = true;
      try {
        const response = await fetch('/api/oauth2/google-login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include',
          body: JSON.stringify({ email, name })
        });
        
        const data = await response.json();
        
        if (data.success) {
          console.log('Google login successful');
          
          const authStore = useAuthStore();
          authStore.user = data.user;
          localStorage.setItem('user', JSON.stringify(data.user));
          
          window.history.replaceState({}, document.title, window.location.pathname);
          
          this.message = 'Đăng nhập Google thành công!';
          
          setTimeout(() => {
            this.$router.push('/customer/index');
          }, 1000);
        } else {
          this.error = data.message || 'Đăng nhập Google thất bại';
          console.error('Google login failed:', data.message);
        }
      } catch (error) {
        console.error('Google login error:', error);
        this.error = 'Lỗi xử lý đăng nhập Google';
      } finally {
        this.loading = false;
      }
    },
    

    async handleForgotPassword() {
      if (!this.forgotPasswordEmail) {
        this.error = 'Vui lòng nhập email';
        return;
      }
      
      this.loading = true;
      try {
        const response = await fetch('/api/auth/forgot-password', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include',
          body: JSON.stringify({ email: this.forgotPasswordEmail })
        });
        
        const data = await response.json();
        
        if (data.success) {
          this.message = 'Mật khẩu mới đã được gửi đến email của bạn';
          this.showForgotPasswordModal = false;
          this.forgotPasswordEmail = '';
        } else {
          this.error = data.message || 'Có lỗi xảy ra';
        }
      } catch (error) {
        this.error = 'Có lỗi xảy ra. Vui lòng thử lại sau';
      } finally {
        this.loading = false;
      }
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
  background: url('http://localhost:8080/anh/login2.jpg') no-repeat center center fixed;
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
  width: 750px; /* Tăng nhẹ để chứa nội dung */
  height: 550px; /* Tăng nhẹ để chứa nội dung */
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
  overflow-y: auto; /* Cho phép scroll nếu nội dung dài */
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