<template>
  <div class="auth-page">
    <div class="bg-overlay"></div>

    <!-- Google Password Modal -->
    <div class="modal fade" id="googlePasswordModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              TÀI KHOẢN GOOGLE
            </h5>
          </div>
          <div class="modal-body">
            <div class="mb-1">
              <label class="fw-bold">Email: </label><label class="ms-1">{{ googleTemp.email }}</label>
            </div>
            <div class="mb-3" v-if="googleTemp.name">
              <label class="fw-bold">Tên: </label><label class="ms-1"> {{ googleTemp.name || 'Google User' }}</label>
            </div>
            <div class="mb-2">
              <label class="fw-bold">Vui lòng nhập mật khẩu cho tài khoản này:</label>
              <input 
                type="password" 
                class="form-control" 
                v-model="googlePassword" 
                placeholder="Nhập mật khẩu bạn muốn đặt"
                @keyup.enter="submitGooglePassword"
                autofocus
              >
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-primary" @click="submitGooglePassword" :disabled="!googlePassword">
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Forgot Password OTP Modal -->
    <div class="modal fade" id="forgotPasswordOtpModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Xác nhận quên mật khẩu</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" @click="resetForgotPasswordOtp"></button>
          </div>
          <div class="modal-body">
            <!-- Step 1: Enter Email -->
            <div v-if="forgotPasswordStep === 1">
              <div class="mb-3">
                <label>Email</label>
                <input v-model="forgotPasswordEmail" type="email" class="form-control" placeholder="Nhập email của bạn" required>
              </div>
              <div class="alert alert-info">
                <i class="bi bi-info-circle"></i>
                Chúng tôi sẽ gửi mã OTP để xác nhận yêu cầu khôi phục mật khẩu.
              </div>
            </div>
            
            <!-- Step 2: Enter OTP -->
            <div v-if="forgotPasswordStep === 2">
              <div class="mb-3">
                <label>Mã OTP</label>
                <div class="input-group">
                  <input v-model="forgotPasswordOtp" type="text" class="form-control" 
                         placeholder="Nhập mã OTP 6 số" maxlength="6" @keypress="onlyNumbers">
                  <button class="btn btn-outline-secondary" type="button" @click="resendForgotPasswordOtp" 
                          :disabled="resendDisabled">
                    {{ resendCountdown > 0 ? `Gửi lại (${resendCountdown}s)` : 'Gửi lại' }}
                  </button>
                </div>
                <small class="text-muted">Mã OTP đã được gửi đến email: {{ forgotPasswordEmail }}</small>
              </div>
              <div class="alert alert-warning">
                <i class="bi bi-exclamation-triangle"></i>
                Mã OTP có hiệu lực trong 10 phút
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-primary" @click="handleForgotPasswordOtp" :disabled="loading">
              <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
              {{ forgotPasswordStep === 1 ? 'Gửi OTP' : 'Xác nhận OTP' }}
            </button>
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" @click="resetForgotPasswordOtp">Đóng</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Registration OTP Modal -->
    <div class="modal fade" id="registerOtpModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Xác nhận đăng ký</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" @click="resetRegisterOtp"></button>
          </div>
          <div class="modal-body">
            
            <div class="mb-3">
              <label>Mã OTP</label>
              <div class="input-group">
                <input v-model="registerOtp" type="text" class="form-control" 
                      placeholder="Nhập mã OTP 6 số" maxlength="6" @keypress="onlyNumbers">
                <button class="btn btn-outline-secondary" type="button" @click="resendRegisterOtp" 
                        :disabled="resendDisabled">
                  {{ resendCountdown > 0 ? `Gửi lại (${resendCountdown}s)` : 'Gửi lại' }}
                </button>
              </div>
              <small class="text-muted">Mã OTP đã được gửi đến email: {{ registerForm.mail }}</small>
            </div>
            <div class="alert alert-warning">
              <i class="bi bi-exclamation-triangle"></i>
              Mã OTP có hiệu lực trong 10 phút
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-primary" @click="verifyRegisterOtp" :disabled="loading">
              <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Terms Modal -->
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

    <!-- Main Auth Form -->
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
              
              <div v-if="accountLocked" class="alert alert-warning alert-dismissible fade show mt-2 mb-2">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                <span>{{ accountLockedMessage }}</span>
              </div>
              
              <div class="d-flex justify-content-between align-items-center mb-3">
                <div class="form-check">
                  <input class="form-check-input" type="checkbox" id="rememberMe" v-model="loginForm.remember">
                  <label for="rememberMe">Ghi nhớ đăng nhập</label>
                </div>
                <a href="#" data-bs-toggle="modal" data-bs-target="#forgotPasswordOtpModal">Quên mật khẩu?</a>
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
import * as bootstrap from 'bootstrap';
import axios from 'axios';

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
      googlePassword: '',
      googleTemp: {
        email: '',
        name: ''
      },

      forgotPasswordStep: 1,
      forgotPasswordEmail: '',
      forgotPasswordOtp: '',

      registerOtp: '',
      registerTempData: null,

      loading: false,
      message: '',
      error: '',
      accountLocked: false,
      accountLockedMessage: '',
      resendCountdown: 0,
      resendDisabled: false,
      countdownInterval: null
    };
  },

  mounted() {
    this.handleGoogleCallback();
    const authStore = useAuthStore();
    if (authStore.isAuthenticated) {
      this.redirectByRole(authStore);
    }
  },

  beforeUnmount() {
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
    }
  },

  methods: {
    getImageUrl(imagePath) {
      return `http://localhost:8080/${imagePath}`;
    },
    
    onlyNumbers(event) {
      const char = String.fromCharCode(event.keyCode);
      if (!/[0-9]/.test(char)) {
        event.preventDefault();
      }
    },

    startResendCountdown(seconds = 60) {
      this.resendDisabled = true;
      this.resendCountdown = seconds;
      
      if (this.countdownInterval) {
        clearInterval(this.countdownInterval);
      }
      
      this.countdownInterval = setInterval(() => {
        if (this.resendCountdown > 0) {
          this.resendCountdown--;
        } else {
          this.resendDisabled = false;
          clearInterval(this.countdownInterval);
        }
      }, 1000);
    },

    resetForgotPasswordOtp() {
      this.forgotPasswordStep = 1;
      this.forgotPasswordEmail = '';
      this.forgotPasswordOtp = '';
      if (this.countdownInterval) {
        clearInterval(this.countdownInterval);
        this.resendDisabled = false;
        this.resendCountdown = 0;
      }
    },

    resetRegisterOtp() {
      this.registerOtp = '';
      this.registerTempData = null;
      if (this.countdownInterval) {
        clearInterval(this.countdownInterval);
        this.resendDisabled = false;
        this.resendCountdown = 0;
      }
    },

    async handleLogin() {
      this.loading = true;
      this.error = '';
      this.message = '';
      this.accountLocked = false;
      this.accountLockedMessage = '';
      
      try {
        const response = await axios.post('/api/auth/login', {
          identifier: this.loginForm.identifier,
          pass: this.loginForm.pass,
          remember: this.loginForm.remember
        }, {
          withCredentials: true
        });

        const data = response.data;
        
        if (data.success) {
          const authStore = useAuthStore();
          authStore.user = data.user;
          authStore.cartCount = data.user.cartCount || 0;
          
          const token = btoa(JSON.stringify({
            maUser: data.user.maUser,
            exp: Date.now() + 24 * 60 * 60 * 1000
          }));
          localStorage.setItem('auth_token', token);
          
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
          this.error = data.message || 'Đăng nhập thất bại';
          
          if (data.message && data.message.includes('bị khóa')) {
            this.accountLocked = true;
            this.accountLockedMessage = data.message;

            setTimeout(() => {
              this.accountLockedMessage += ' Vui lòng liên hệ quản trị viên qua Hotline: 1900 0001';
            }, 100);
          }
        }
      } catch (error) {
        console.error('Login error:', error);
        this.error = error.response?.data?.message || 'Đăng nhập thất bại. Vui lòng thử lại sau.';
      } finally {
        this.loading = false;
      }
    },

    async handleRegister() {
      if (!this.registerForm.terms) {
        this.error = 'Vui lòng đồng ý với điều khoản sử dụng';
        return;
      }

      if (!/^\d{9,11}$/.test(this.registerForm.phone)) {
        this.error = 'Số điện thoại phải từ 9-11 số';
        return;
      }

      this.loading = true;
      this.error = '';
      
      try {
        const response = await axios.post('/api/auth/send-register', {
          mail: this.registerForm.mail,
          pass: this.registerForm.pass,
          fullname: this.registerForm.fullname,
          phone: this.registerForm.phone
        }, {
          withCredentials: true
        });

        const data = response.data;
        
        if (data.success) {
          this.registerTempData = { ...this.registerForm };
          this.message = 'Mã OTP đã được gửi đến email của bạn!';
          
          const modal = new bootstrap.Modal(document.getElementById('registerOtpModal'));
          modal.show();
          
          this.startResendCountdown(60);
        } else {
          this.error = data.message || 'Có lỗi xảy ra khi gửi OTP';
        }
      } catch (error) {
        this.error = error.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại sau.';
      } finally {
        this.loading = false;
      }
    },

    async verifyRegisterOtp() {
      if (!this.registerOtp || this.registerOtp.length !== 6) {
        this.error = 'Vui lòng nhập mã OTP 6 số';
        return;
      }

      this.loading = true;
      this.error = '';

      const response = await axios.post('/api/auth/complete-register', {
        mail: this.registerForm.mail,
        confirmationCode: this.registerOtp
      }, {
        withCredentials: true
      });

      const data = response.data;
      
      if (data.success) {
        const modal = bootstrap.Modal.getInstance(document.getElementById('registerOtpModal'));
        modal.hide();

        this.loginForm.identifier = this.registerForm.mail;
        this.loginForm.pass = this.registerForm.pass;
        this.loginForm.remember = true;

        await this.handleLogin();
                        
        if (loginResult.success) {
          this.message = 'Đăng ký và đăng nhập thành công!';

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
          
        }
        this.message = 'Đăng ký và đăng nhập thành công!';
      } else {
        this.error = data.message || 'Mã OTP không chính xác';
      }
    },

    async resendRegisterOtp() {
      if (!this.registerTempData) return;

      this.loading = true;
      this.error = '';

      try {
        const response = await axios.post('/api/auth/send-register', {
          mail: this.registerTempData.mail,
          pass: this.registerTempData.pass,
          fullname: this.registerTempData.fullname,
          phone: this.registerTempData.phone
        }, {
          withCredentials: true
        });

        const data = response.data;
        
        if (data.success) {
          this.message = 'Mã OTP mới đã được gửi!';
          this.startResendCountdown(60);
        } else {
          this.error = data.message || 'Có lỗi xảy ra khi gửi lại OTP';
        }
      } catch (error) {
        this.error = error.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại sau.';
      } finally {
        this.loading = false;
      }
    },

    async handleForgotPasswordOtp() {
      if (this.forgotPasswordStep === 1) {
        if (!this.forgotPasswordEmail) {
          this.error = 'Vui lòng nhập email';
          return;
        }

        this.loading = true;
        this.error = '';

        try {
          const response = await axios.post('/api/auth/send-fg-pass', {
            email: this.forgotPasswordEmail
          }, {
            withCredentials: true
          });

          const data = response.data;
          
          if (data.success) {
            this.message = 'Mã OTP đã được gửi đến email của bạn!';
            this.forgotPasswordStep = 2;
            this.startResendCountdown(60);
          } else {
            this.error = data.message || 'Có lỗi xảy ra';
          }
        } catch (error) {
          this.error = error.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại sau';
        } finally {
          this.loading = false;
        }
      } else {
        if (!this.forgotPasswordOtp || this.forgotPasswordOtp.length !== 6) {
          this.error = 'Vui lòng nhập mã OTP 6 số';
          return;
        }

        this.loading = true;
        this.error = '';

        try {
          const response = await axios.post('/api/auth/confirm-fg-pass', {
            email: this.forgotPasswordEmail,
            confirmationCode: this.forgotPasswordOtp
          }, {
            withCredentials: true
          });

          const data = response.data;
          
          if (data.success) {
            this.message = 'Mật khẩu mới đã được gửi đến email của bạn!';
            
            this.resetForgotPasswordOtp();
          } else {
            this.error = data.message || 'Mã OTP không chính xác';
          }
        } catch (error) {
          this.error = error.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại sau';
        } finally {
          this.loading = false;
        }
      }
    },

    async resendForgotPasswordOtp() {
      this.loading = true;
      this.error = '';

      try {
        const response = await axios.post('/api/auth/send-fg-pass', {
          email: this.forgotPasswordEmail
        }, {
          withCredentials: true
        });

        const data = response.data;
        
        if (data.success) {
          this.message = 'Mã OTP mới đã được gửi!';
          this.startResendCountdown(60);
        } else {
          this.error = data.message || 'Có lỗi xảy ra';
        }
      } catch (error) {
        this.error = error.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại sau';
      } finally {
        this.loading = false;
      }
    },

    async handleGoogleCallback() {
      try {
        const urlParams = new URLSearchParams(window.location.search);
        const googleSuccess = urlParams.get('googleSuccess');
        const email = urlParams.get('email');
        const name = urlParams.get('name');
        
        if (googleSuccess === 'true' && email) {
          const response = await axios.get('/api/oauth2/google/callback', {
            params: {
              email: email,
              name: name || ''
            },
            withCredentials: true
          });
          
          const data = response.data;

          if (data.success) {
            if (data.requirePassword) {
              this.showGooglePasswordModal(data.email || email, data.name || name);
            } else {
              await this.completeGoogleLogin(data.email || email, data.name || name);
            }
          } else {
            this.error = data.message || 'Đăng nhập Google thất bại';
            if (data.message && data.message.includes('bị khóa')) {
              this.accountLocked = true;
              this.accountLockedMessage = data.message + 'Vui lòng liên hệ quản trị viên qua Hotline: 1900 0001 để được khắc phục.';
            }
          }
        }
      } catch (error) {
        this.error = error.response?.data?.message || 'Lỗi xử lý đăng nhập Google';
      }
    },

    showGooglePasswordModal(email, name) {
      this.googleTemp.email = email;
      this.googleTemp.name = name || 'Google User';
      this.googlePassword = '';

      const modal = new bootstrap.Modal(document.getElementById('googlePasswordModal'));
      modal.show();
    },

    submitGooglePassword() {
      if (!this.googlePassword || this.googlePassword.trim() === '') {
        alert('Vui lòng nhập mật khẩu để tiếp tục!');
        return;
      }

      const modal = bootstrap.Modal.getInstance(document.getElementById('googlePasswordModal'));
      modal.hide();

      this.completeGoogleLoginNewUser(
        this.googleTemp.email, 
        this.googleTemp.name, 
        this.googlePassword
      );
      this.googlePassword = '';
      this.googleTemp.email = '';
      this.googleTemp.name = '';
    },

    async completeGoogleLoginNewUser(email, name, password) {
      this.loading = true;
      try {
        const response = await axios.post('/api/oauth2/google-login-newuser', {
          email: email,
          name: name,
          password: password
        }, {
          withCredentials: true
        });
        
        const data = response.data;
        
        if (data.success) {
          const token = btoa(JSON.stringify({
            maUser: data.user.maUser,
            exp: Date.now() + 24 * 60 * 60 * 1000
          }));
          localStorage.setItem('auth_token', token);

          const authStore = useAuthStore();
          authStore.user = data.user;
          window.history.replaceState({}, document.title, window.location.pathname);
          
          this.message = 'Đăng nhập Google thành công!';
          
          setTimeout(() => {
            this.$router.push('/customer/index');
          }, 1000);
        } else {
          this.error = data.message || 'Đăng nhập Google thất bại';
        }
      } catch (error) {
        this.error = error.response?.data?.message || 'Lỗi xử lý đăng nhập Google';
      } finally {
        this.loading = false;
      }
    },
    
    async completeGoogleLogin(email, name) {
      this.loading = true;
      try {
        const response = await axios.post('/api/oauth2/google-login', {
          email: email,
          name: name
        }, {
          withCredentials: true
        });
        
        const data = response.data;
        
        if (data.success) {
          const token = btoa(JSON.stringify({
            maUser: data.user.maUser,
            exp: Date.now() + 24 * 60 * 60 * 1000
          }));
          localStorage.setItem('auth_token', token);
          const authStore = useAuthStore();
          authStore.user = data.user;

          window.history.replaceState({}, document.title, window.location.pathname);
          
          this.message = 'Đăng nhập Google thành công!';
          
          setTimeout(() => {
            this.$router.push('/customer/index');
          }, 1000);
        } else {
          this.error = data.message || 'Đăng nhập Google thất bại';
        }
      } catch (error) {
        this.error = error.response?.data?.message || 'Lỗi xử lý đăng nhập Google';
      } finally {
        this.loading = false;
      }
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
    }
  }
};
</script>

<style scoped>

input[type="text"].text-center {
  font-size: 1.2rem;
  letter-spacing: 2px;
}

.modal.fade .modal-dialog {
  transition: transform 0.3s ease-out;
}

.modal.show .modal-dialog {
  transform: none;
}

.btn-outline-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

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

.alert-warning {
  background-color: #fff3cd;
  border-color: #ffeeba;
  color: #856404;
  font-size: 0.9rem;
  padding: 10px;
}

::placeholder {
  color: #999;
  font-size: 0.9rem;
}

small {
  font-size: 0.8rem;
  color: #666;
}
</style>