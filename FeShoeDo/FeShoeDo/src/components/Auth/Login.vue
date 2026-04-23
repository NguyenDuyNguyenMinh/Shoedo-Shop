<template>
  <div class="auth-page">
    <div class="bg-overlay"></div>
    <router-link to="/customer/index" class="btn btn-outline-white btn-sm home-btn">
      <i class="bi bi-house"></i>
    </router-link>

    <!-- Toast Component -->
    <Toast />

    <!-- Google Password Modal -->
    <div class="modal fade" id="googlePasswordModal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">TÀI KHOẢN GOOGLE</h5>
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
    <div class="modal fade" id="forgotPasswordOtpModal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Xác nhận quên mật khẩu</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" @click="resetForgotPasswordOtp"></button>
          </div>
          <div class="modal-body">
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
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" @click="resetForgotPasswordOtp">Hủy</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Registration OTP Modal -->
    <div class="modal fade" id="registerOtpModal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
      <div class="modal-dialog modal-dialog-centered">
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
    <div class="modal fade" id="about" tabindex="-1" aria-hidden="true" data-bs-backdrop="static" data-bs-keyboard="false">
      <div class="modal-dialog modal-dialog-centered">
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
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Auth Form -->
    <div class="auth-wrapper fade-in-up">
      <div class="auth-left fade-in-left">
        <h2 class="slide-in-text">Welcome to ShoeDo Shop!</h2>
        <p class="slide-in-text-delay">Khám phá bộ sưu tập giày thể thao mới nhất.</p>
        <p class="slide-in-text-delay-2">Đăng nhập để trải nghiệm mua sắm tuyệt vời!</p>
      </div>

      <div class="auth-right">
        <ul class="nav nav-tabs" id="authTabs" role="tablist">
          <li class="nav-item">
            <button class="nav-link active tab-hover" id="login-tab" data-bs-toggle="tab" data-bs-target="#login"
              type="button">Login</button>
          </li>
          <li class="nav-item">
            <button class="nav-link tab-hover" id="register-tab" data-bs-toggle="tab" data-bs-target="#register"
              type="button">Sign up</button>
          </li>
        </ul>

        <div class="tab-content form-section" id="authTabsContent">
          <!-- LOGIN -->
          <div class="tab-pane fade show active" id="login" role="tabpanel" aria-labelledby="login-tab">
            <form @submit.prevent="handleLogin">
              <div class="mt-3 mb-3 form-item">
                <label for="loginIdentifier">Tài khoản</label>
                <input 
                  type="text" 
                  class="form-control input-hover" 
                  id="loginIdentifier" 
                  v-model="loginForm.identifier" 
                  placeholder="Nhập username hoặc email"
                  required
                >
                <small class="text-muted">Bạn có thể đăng nhập bằng username hoặc email</small>
              </div>
              <div class="mb-3 form-item">
                <label for="loginPassword">Mật khẩu</label>
                <input type="password" class="form-control input-hover" id="loginPassword" v-model="loginForm.pass" required>
              </div>
              
              <div v-if="accountLocked" class="alert alert-warning alert-dismissible fade show mt-2 mb-2 shake-alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                <span>{{ accountLockedMessage }}</span>
              </div>
              
              <div class="d-flex justify-content-between align-items-center mb-3 form-item">
                <div class="form-check">
                  <input class="form-check-input checkbox-hover" type="checkbox" id="rememberMe" v-model="loginForm.remember">
                  <label for="rememberMe">Ghi nhớ đăng nhập</label>
                </div>
                <a href="#" data-bs-toggle="modal" data-bs-target="#forgotPasswordOtpModal" class="link-hover">Quên mật khẩu?</a>
              </div>
              <div class="d-grid form-item">
                <button type="submit" class="btn btn-dark btn-hover" :disabled="loading">
                  <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  {{ loading ? 'Đang xử lý...' : 'Đăng nhập' }}
                </button>
              </div>
              <br>
              <div class="position-relative text-center mb-3 form-item">
                <hr class="position-absolute top-50 start-0 end-0 m-0">
                <span class="px-3 bg-white text-muted position-relative" style="z-index: 1;">
                  Hoặc
                </span>
              </div>
              <div class="form-item">
                <a href="http://localhost:8080/oauth2/authorization/google" 
                  class="btn btn-outline-dark d-flex align-items-center justify-content-center gap-2 mx-auto google-btn"
                  style="width: 210px; border-radius: 50px;">
                  <img :src="getImageUrl('anh/logo GG.png')" alt="Google Logo" style="width: 20px; height: 20px;" class="google-icon">
                  <span>Sign in with Google</span>
                </a>
              </div>
            </form>
          </div>

          <!-- REGISTER -->
          <div class="tab-pane fade" id="register" role="tabpanel" aria-labelledby="register-tab">
            <form @submit.prevent="handleRegister">
              <div class="mt-3 mb-3 form-item">
                <label for="regMail">Email</label>
                <input type="email" class="form-control input-hover" id="regMail" v-model="registerForm.mail" required>
              </div>
              <div class="mb-3 form-item">
                <label for="regPassword">Mật khẩu</label>
                <input type="password" class="form-control input-hover" id="regPassword" v-model="registerForm.pass" required>
              </div>
              <div class="mb-3 form-item">
                <label for="regFullname">Họ và tên</label>
                <input type="text" class="form-control input-hover" id="regFullname" v-model="registerForm.fullname" required>
              </div>
              <div class="mb-3 form-item">
                <label for="regPhone">Số điện thoại</label>
                <input type="tel" class="form-control input-hover" id="regPhone" v-model="registerForm.phone" 
                       pattern="[0-9]{9,11}" required>
                <small class="text-muted">Nhập 9-11 số điện thoại</small>
              </div>
              <div class="form-check mb-3 form-item">
                <input class="form-check-input checkbox-hover" type="checkbox" id="termsCheck" v-model="registerForm.terms" required>
                <label class="form-check-label" for="termsCheck">
                  Tôi đồng ý với <a href="#" data-bs-toggle="modal" data-bs-target="#about" class="link-hover">điều khoản sử dụng</a>
                </label>
              </div>
              
              <div class="d-grid form-item">
                <button type="submit" class="btn btn-dark btn-hover" :disabled="loading">
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

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import * as bootstrap from 'bootstrap';
import axios from 'axios';
import Toast from '@/components/Shared/Toast.vue';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const loginForm = ref({ identifier: '', pass: '', remember: false });
const registerForm = ref({ mail: '', pass: '', fullname: '', phone: '', terms: false });
const googlePassword = ref('');
const googleTemp = ref({ email: '', name: '' });

const forgotPasswordStep = ref(1);
const forgotPasswordEmail = ref('');
const forgotPasswordOtp = ref('');
const registerOtp = ref('');
const registerTempData = ref(null);

const loading = ref(false);
const accountLocked = ref(false);
const accountLockedMessage = ref('');
const resendCountdown = ref(0);
const resendDisabled = ref(false);
let countdownInterval = null;

onMounted(() => {
  handleGoogleCallback();
  if (authStore.isAuthenticated) redirectByRole(authStore);
  checkAndOpenRegisterTab();
});

onBeforeUnmount(() => {
  if (countdownInterval) clearInterval(countdownInterval);
});

const checkAndOpenRegisterTab = () => {
  const tab = route.query.tab;
  if (tab === 'register') {
    const registerTab = document.getElementById('register-tab');
    const loginTab = document.getElementById('login-tab');
    if (registerTab && loginTab) {
      registerTab.classList.add('active');
      loginTab.classList.remove('active');
      const registerPane = document.getElementById('register');
      const loginPane = document.getElementById('login');
      if (registerPane && loginPane) {
        registerPane.classList.add('show', 'active');
        loginPane.classList.remove('show', 'active');
      }
    }
  }
};

const getImageUrl = (path) => `http://localhost:8080/${path}`;
const onlyNumbers = (e) => { if (!/[0-9]/.test(String.fromCharCode(e.keyCode))) e.preventDefault(); };

const startResendCountdown = (seconds = 60) => {
  resendDisabled.value = true;
  resendCountdown.value = seconds;
  if (countdownInterval) clearInterval(countdownInterval);
  countdownInterval = setInterval(() => {
    if (resendCountdown.value > 0) resendCountdown.value--;
    else {
      resendDisabled.value = false;
      clearInterval(countdownInterval);
    }
  }, 1000);
};

const resetForgotPasswordOtp = () => {
  forgotPasswordStep.value = 1;
  forgotPasswordEmail.value = '';
  forgotPasswordOtp.value = '';
  if (countdownInterval) { clearInterval(countdownInterval); resendDisabled.value = false; resendCountdown.value = 0; }
};

const resetRegisterOtp = () => {
  registerOtp.value = '';
  registerTempData.value = null;
  if (countdownInterval) { clearInterval(countdownInterval); resendDisabled.value = false; resendCountdown.value = 0; }
};

const redirectByRole = (store) => {
  if (store.isCustomer) router.push('/customer/index');
  else if (store.userRole === 'ADMIN') router.push('/employee/dashboard');
  else if (store.userRole === 'EMPLOYEE') router.push('/employee/flashsale');
  else router.push('/customer/index');
};

const handleLogin = async () => {
  loading.value = true;
  accountLocked.value = false;
  accountLockedMessage.value = '';
  try {
    const { data } = await axios.post('/api/auth/login', loginForm.value, { withCredentials: true });
    if (data.success) {
      authStore.user = data.user;
      authStore.cartCount = data.user.cartCount || 0;
      window.showToast?.('Đăng nhập thành công!', 'success');
      setTimeout(() => redirectByRole(authStore), 1000);
    } else {
      window.showToast?.(data.message || 'Đăng nhập thất bại', 'danger');
      if (data.message?.includes('bị khóa')) {
        accountLocked.value = true;
        accountLockedMessage.value = data.message;
      }
    }
  } catch (error) {
    window.showToast?.(error.response?.data?.message || 'Đăng nhập thất bại', 'danger');
  } finally { loading.value = false; }
};

const handleRegister = async () => {
  if (!registerForm.value.terms) return window.showToast?.('Vui lòng đồng ý với điều khoản sử dụng', 'warning');
  if (!/^\d{9,11}$/.test(registerForm.value.phone)) return window.showToast?.('Số điện thoại phải từ 9-11 số', 'warning');
  
  loading.value = true;
  try {
    const { data } = await axios.post('/api/auth/send-register', registerForm.value, { withCredentials: true });
    if (data.success) {
      registerTempData.value = { ...registerForm.value };
      window.showToast?.('Mã OTP đã được gửi đến email của bạn!', 'success');
      new bootstrap.Modal(document.getElementById('registerOtpModal')).show();
      startResendCountdown(60);
    } else window.showToast?.(data.message || 'Lỗi gửi OTP', 'danger');
  } catch (err) { window.showToast?.(err.response?.data?.message || 'Có lỗi xảy ra', 'danger'); } 
  finally { loading.value = false; }
};

const verifyRegisterOtp = async () => {
  if (!registerOtp.value || registerOtp.value.length !== 6) return window.showToast?.('Nhập OTP 6 số', 'warning');
  loading.value = true;
  try {
    const { data } = await axios.post('/api/auth/complete-register', { 
      mail: registerForm.value.mail, confirmationCode: registerOtp.value 
    }, { withCredentials: true });
    
    if (data.success) {
      bootstrap.Modal.getInstance(document.getElementById('registerOtpModal'))?.hide();
      const loginRes = await axios.post('/api/auth/login', {
        identifier: registerForm.value.mail, pass: registerForm.value.pass, remember: true
      }, { withCredentials: true });
      if (loginRes.data.success) {
        authStore.user = loginRes.data.user;
        authStore.cartCount = loginRes.data.user.cartCount || 0;
        window.showToast?.('Đăng kí thành công!', 'success');
        setTimeout(() => router.push('/customer/index'), 1000);
      }
    } else window.showToast?.(data.message || 'OTP không đúng', 'danger');
  } catch (err) { window.showToast?.('Có lỗi xảy ra', 'danger'); }
  finally { loading.value = false; }
};

const resendRegisterOtp = async () => {
  if (!registerTempData.value) return;
  loading.value = true;
  try {
    const { data } = await axios.post('/api/auth/send-register', registerTempData.value, { withCredentials: true });
    if (data.success) {
      window.showToast?.('OTP mới đã được gửi!', 'success');
      startResendCountdown(60);
    } else window.showToast?.(data.message || 'Lỗi gửi lại OTP', 'danger');
  } catch (err) { window.showToast?.('Có lỗi xảy ra', 'danger'); }
  finally { loading.value = false; }
};

const handleForgotPasswordOtp = async () => {
  if (forgotPasswordStep.value === 1) {
    if (!forgotPasswordEmail.value) return window.showToast?.('Nhập email', 'warning');
    loading.value = true;
    try {
      const { data } = await axios.post('/api/auth/send-fg-pass', { email: forgotPasswordEmail.value }, { withCredentials: true });
      if (data.success) {
        window.showToast?.('OTP đã gửi!', 'success');
        forgotPasswordStep.value = 2;
        startResendCountdown(60);
      } else window.showToast?.(data.message || 'Có lỗi xảy ra', 'danger');
    } catch (err) { window.showToast?.('Có lỗi xảy ra', 'danger'); }
    finally { loading.value = false; }
  } else {
    if (!forgotPasswordOtp.value || forgotPasswordOtp.value.length !== 6) return window.showToast?.('Nhập OTP 6 số', 'warning');
    loading.value = true;
    try {
      const { data } = await axios.post('/api/auth/confirm-fg-pass', { 
        email: forgotPasswordEmail.value, confirmationCode: forgotPasswordOtp.value 
      }, { withCredentials: true });
      if (data.success) {
        window.showToast?.('Mật khẩu mới đã gửi vào email!', 'success');
        closeForgotPasswordModal();
      } else window.showToast?.(data.message || 'OTP không đúng', 'danger');
    } catch (err) { window.showToast?.('Có lỗi xảy ra', 'danger'); }
    finally { loading.value = false; }
  }
};

const closeForgotPasswordModal = () => {
  bootstrap.Modal.getInstance(document.getElementById('forgotPasswordOtpModal'))?.hide();
  document.querySelectorAll('.modal-backdrop').forEach(b => b.remove());
  document.body.classList.remove('modal-open');
  document.body.style.removeProperty('overflow');
  document.body.style.removeProperty('padding-right');
  resetForgotPasswordOtp();
};

const resendForgotPasswordOtp = async () => {
  loading.value = true;
  try {
    const { data } = await axios.post('/api/auth/send-fg-pass', { email: forgotPasswordEmail.value }, { withCredentials: true });
    if (data.success) {
      window.showToast?.('OTP mới đã gửi!', 'success');
      startResendCountdown(60);
    } else window.showToast?.(data.message || 'Có lỗi', 'danger');
  } catch (err) { window.showToast?.('Có lỗi', 'danger'); }
  finally { loading.value = false; }
};

const handleGoogleCallback = async () => {
  try {
    const googleSuccess = route.query.googleSuccess;
    const email = route.query.email;
    const name = route.query.name;
    
    if (googleSuccess === 'true' && email) {
      const { data } = await axios.get('/api/oauth2/google/callback', {
        params: { email, name: name || '' },
        withCredentials: true
      });
      if (data.success) {
        if (data.requirePassword) showGooglePasswordModal(data.email || email, data.name || name);
        else await completeGoogleLogin(data.email || email, data.name || name);
      } else {
        window.showToast?.(data.message || 'Lỗi đăng nhập Google', 'danger');
        if (data.message?.includes('bị khóa')) {
          accountLocked.value = true;
          accountLockedMessage.value = data.message + ' Liên hệ Hotline 1900 6869 để khắc phục.';
        }
      }
    }
  } catch (error) { window.showToast?.('Lỗi xử lý Google Callback', 'danger'); }
};

const showGooglePasswordModal = (email, name) => {
  googleTemp.value = { email, name: name || 'Google User' };
  googlePassword.value = '';
  new bootstrap.Modal(document.getElementById('googlePasswordModal')).show();
};

const submitGooglePassword = () => {
  if (!googlePassword.value.trim()) return window.showToast?.('Vui lòng nhập mật khẩu!', 'warning');
  bootstrap.Modal.getInstance(document.getElementById('googlePasswordModal'))?.hide();
  completeGoogleLoginNewUser(googleTemp.value.email, googleTemp.value.name, googlePassword.value);
  googlePassword.value = '';
  googleTemp.value = { email: '', name: '' };
};

const completeGoogleLoginNewUser = async (email, name, password) => {
  loading.value = true;
  try {
    const { data } = await axios.post('/api/oauth2/google-login-newuser', { email, name, password }, { withCredentials: true });
    if (data.success) {
      authStore.user = data.user;
      // Xóa query params khỏi URL mà không reload trang để Toast vẫn hiển thị
      history.replaceState(null, '', route.path);
      window.showToast?.('Đăng nhập Google thành công!', 'success');
      setTimeout(() => redirectByRole(authStore), 1500);
    } else window.showToast?.(data.message || 'Lỗi đăng nhập Google', 'danger');
  } catch (err) { window.showToast?.('Lỗi đăng nhập Google', 'danger'); }
  finally { loading.value = false; }
};

const completeGoogleLogin = async (email, name) => {
  loading.value = true;
  try {
    const { data } = await axios.post('/api/oauth2/google-login', { email, name }, { withCredentials: true });
    if (data.success) {
      authStore.user = data.user;
      // Xóa query params khỏi URL mà không reload trang để Toast vẫn hiển thị
      history.replaceState(null, '', route.path);
      window.showToast?.('Đăng nhập Google thành công!', 'success');
      setTimeout(() => redirectByRole(authStore), 1500);
    } else window.showToast?.(data.message || 'Lỗi đăng nhập Google', 'danger');
  } catch (err) { window.showToast?.('Lỗi đăng nhập Google', 'danger'); }
  finally { loading.value = false; }
};
</script>

<style scoped>
/* ===== ANIMATIONS ===== */
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

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.fade-in-up {
  animation: fadeInUp 0.5s ease-out forwards;
}

.fade-in-left {
  animation: fadeInLeft 0.6s ease-out forwards;
}

.slide-in-text {
  animation: slideIn 0.5s ease-out forwards;
}

.slide-in-text-delay {
  opacity: 0;
  animation: slideIn 0.5s ease-out 0.15s forwards;
}

.slide-in-text-delay-2 {
  opacity: 0;
  animation: slideIn 0.5s ease-out 0.3s forwards;
}

.form-item {
  opacity: 0;
  animation: fadeInUp 0.4s ease-out forwards;
  animation-delay: calc(var(--i, 0) * 0.05s);
}

.form-item:nth-child(1) { --i: 1; }
.form-item:nth-child(2) { --i: 2; }
.form-item:nth-child(3) { --i: 3; }
.form-item:nth-child(4) { --i: 4; }
.form-item:nth-child(5) { --i: 5; }
.form-item:nth-child(6) { --i: 6; }
.form-item:nth-child(7) { --i: 7; }
.form-item:nth-child(8) { --i: 8; }

.shake-alert {
  animation: shake 0.4s ease-in-out;
}

/* ===== HOVER EFFECTS ===== */
.home-btn {
  transition: all 0.2s ease;
}

.home-btn:hover {
  transform: scale(1.05);
  background-color: #ffffff;
  color: #000000;
}

.tab-hover {
  transition: all 0.2s ease;
}

.tab-hover:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.input-hover {
  transition: all 0.2s ease;
}

.input-hover:focus {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.btn-hover {
  transition: all 0.2s ease;
}

.btn-hover:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.link-hover {
  position: relative;
  text-decoration: none;
  transition: color 0.2s ease;
}

.link-hover::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 1px;
  background: #000;
  transition: width 0.2s ease;
}

.link-hover:hover::after {
  width: 100%;
}

.checkbox-hover {
  transition: transform 0.2s ease;
  cursor: pointer;
}

.checkbox-hover:hover {
  transform: scale(1.1);
}

.google-btn {
  transition: all 0.2s ease;
}

.google-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.google-icon {
  transition: transform 0.3s ease;
}

.google-btn:hover .google-icon {
  transform: rotate(360deg);
}

/* ===== EXISTING STYLES ===== */
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

.modal-content {
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.auth-page {
  font-family: 'Segoe UI', sans-serif;
  height: 100vh;
  margin: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: url('http://localhost:8080/anh/login2.jpg') no-repeat center center;
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
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.auth-wrapper:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.4);
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

.btn-outline-white {
  border-color: #f6f6f6;
  color: #ffffff;
  position: fixed; 
  top: 20px; 
  left: 20px; 
  z-index: 1000;
}
</style>

<style>
.modal-backdrop {
  background-color: rgba(0, 0, 0, 0.25) !important;
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(12px) brightness(0.9);
}
.modal-backdrop.show {
  opacity: 1 !important;
}
</style>