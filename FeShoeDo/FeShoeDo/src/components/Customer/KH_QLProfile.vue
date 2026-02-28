<template>
  <div class="customer-layout">
    <KH_Navbar />
    
    <main class="container mt-4">
      <!-- Alert Messages -->
      <div v-if="message" class="alert alert-success alert-dismissible fade show">
        <i class="fas fa-check-circle me-2"></i>
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''"></button>
      </div>
          
      <div v-if="error" class="alert alert-danger alert-dismissible fade show">
        <i class="fas fa-exclamation-circle me-2"></i>
        {{ error }}
        <button type="button" class="btn-close" @click="error = ''"></button>
      </div>

      <div class="row">
        <!-- Thông tin cá nhân -->
        <div class="col-md-7">
          <div class="card shadow-sm mb-4">
            <div class="card-header">
              <i class="fa-solid fa-user me-2"></i> Thông tin cá nhân
            </div>
            <div class="card-body">
              <form @submit.prevent="updateProfile">
                <div class="row">
                  <div class="col-md-6">
                    <div class="mb-3">
                      <label class="form-label fw-bold">Tên đăng nhập</label>
                      <input type="text" class="form-control" :value="user.userName" disabled>
                    </div>
                    <div class="mb-3">
                      <label class="form-label fw-bold">Họ và tên</label>
                      <input type="text" class="form-control" v-model="customer.tenKH" required>
                    </div>
                  </div>
                  <div class="col-md-6">
                    <div class="mb-3">
                      <label class="form-label fw-bold">ngày tạo </label>
                      <input type="text" class="form-control" v-model="customer.sdt" required 
                             pattern="[0-9]{9,11}" placeholder="090xxxxxxx">
                    </div>
                    <div class="mb-3">
                      <label class="form-label fw-bold">Số điện thoại</label>
                      <input type="text" class="form-control" v-model="customer.sdt" required 
                             pattern="[0-9]{9,11}" placeholder="090xxxxxxx">
                    </div>
                  </div>
                </div>
                <div class="row">
                  <div class="col-md-12 mb-3">
                      <label class="form-label fw-bold">Email</label>
                      <input type="email" class="form-control" :value="user.mail" disabled>
                      <small class="text-muted">Email không thể thay đổi</small>
                    </div>
                </div>
                
                <div class="text-end">
                  <button type="submit" class="btn btn-success" :disabled="profileLoading">
                    <span v-if="profileLoading" class="spinner-border spinner-border-sm me-1"></span>
                    <i v-else class="fas fa-save me-1"></i> Lưu thay đổi
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
              
        <!-- Đổi mật khẩu -->
        <div class="col-md-5">
          <div class="card shadow-sm mb-4">
            <div class="card-header">
              <i class="fas fa-key me-2"></i> Đổi mật khẩu
            </div>
            <div class="card-body">
              <form @submit.prevent="changePassword">
                <div class="mb-3">
                  <label class="form-label fw-bold">Mật khẩu hiện tại</label>
                  <input type="password" class="form-control" v-model="password.currentPassword" required>
                </div>
                <div class="mb-3">
                  <label class="form-label fw-bold">Mật khẩu mới</label>
                  <input type="password" class="form-control" v-model="password.newPassword" required
                         minlength="6">
                </div>
                <div class="mb-3">
                  <label class="form-label fw-bold">Xác nhận mật khẩu mới</label>
                  <input type="password" class="form-control" v-model="password.confirmPassword" required>
                </div>

                <div class="text-end">
                  <button type="submit" class="btn btn-primary" :disabled="passwordLoading">
                    <span v-if="passwordLoading" class="spinner-border spinner-border-sm me-1"></span>
                    <i v-else class="fas fa-key me-1"></i> Đổi mật khẩu
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>

      <!-- Quản lý địa chỉ (Style giống BlackWhite) -->
      <div class="card shadow-sm">
        <div class="card-header d-flex justify-content-between align-items-center">
          <span>📍 Quản lý địa chỉ nhận hàng</span>
          <button class="btn btn-light btn-sm" @click="showAddModal = true">
            <i class="fas fa-plus me-1"></i> Thêm địa chỉ mới
          </button>
        </div>
        <div class="card-body">
          <div v-for="address in addresses" :key="address.maDC" 
               class="address-card mb-3" :class="{ 'default': address.macDinh }">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <div class="address-header">
                  <span>{{ address.tenNN }}</span> 
                  <span class="text-muted"> | {{ address.sdt }}</span>
                </div>
                <div>{{ address.diemGiao }}</div>
              </div>
              <div class="text-end">
                <button v-if="address.macDinh" class="btn btn-sm btn-outline-danger me-2" disabled>
                  <i class="fas fa-check"></i> Mặc định
                </button>
                <button v-else class="btn btn-sm btn-outline-success me-2" 
                        @click="setDefaultAddress(address.maDC)">
                  <i class="fas fa-check"></i> Đặt mặc định
                </button>
                <button class="btn btn-sm btn-outline-primary me-2" 
                        @click="editAddress(address)">
                  <i class="fas fa-edit"></i> Cập nhật
                </button>
                <button class="btn btn-sm btn-outline-danger" 
                        @click="deleteAddress(address.maDC)"
                        :disabled="address.macDinh">
                  <i class="fas fa-trash"></i> Xóa
                </button>
              </div>
            </div>
          </div>

          <div v-if="addresses.length === 0" class="text-center text-muted p-4">
            <i class="fas fa-map-marked-alt fa-3x mb-3"></i>
            <p>Chưa có địa chỉ nào. Thêm địa chỉ mới để dễ dàng đặt hàng!</p>
          </div>
        </div>
      </div>
    </main>

    <!-- Modal Thêm/Sửa Địa Chỉ -->
    <div v-if="showAddModal || editingAddress" class="modal fade show d-block" style="background: rgba(0,0,0,0.5)">
      <div class="modal-dialog">
        <div class="modal-content">
          <form @submit.prevent="saveAddress">
            <div class="modal-header">
              <h5 class="modal-title">{{ editingAddress ? 'Cập nhật địa chỉ' : 'Thêm địa chỉ mới' }}</h5>
              <button type="button" class="btn-close" @click="closeModal"></button>
            </div>
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label">Họ và tên người nhận</label>
                <input type="text" class="form-control" v-model="addressForm.tenNN" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Số điện thoại</label>
                <input type="text" class="form-control" v-model="addressForm.sdt" required pattern="[0-9]{9,11}">
                <small class="text-muted">9-11 số</small>
              </div>
              <div class="mb-3">
                <label class="form-label">Địa chỉ</label>
                <textarea class="form-control" v-model="addressForm.diemGiao" rows="3" required></textarea>
              </div>
              <div class="form-check">
                <input class="form-check-input" type="checkbox" v-model="addressForm.macDinh">
                <label class="form-check-label">
                  Đặt làm địa chỉ mặc định
                </label>
              </div>
            </div>
            <div class="modal-footer">
              <button type="submit" class="btn btn-primary" :disabled="addressLoading">
                <span v-if="addressLoading" class="spinner-border spinner-border-sm me-1"></span>
                {{ editingAddress ? 'Cập nhật' : 'Thêm địa chỉ' }}
              </button>
              <button type="button" class="btn btn-secondary" @click="closeModal">Hủy</button>
            </div>
          </form>
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import api from '@/services/api';
import KH_Navbar from '@/components/shared/KH_Navbar.vue';
import Footer from '@/components/shared/Footer.vue';

export default {
  name: 'QLProfile',
  components: {
    KH_Navbar,
    Footer
  },
  setup() {
    const user = ref({
      maUser: null,
      userName: '',
      mail: '',
      createAt: ''
    });
    
    const customer = ref({
      maKH: null,
      tenKH: '',
      sdt: ''
    });
    
    const addresses = ref([]);
    const password = ref({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    });
    
    const profileLoading = ref(false);
    const passwordLoading = ref(false);
    const addressLoading = ref(false);
    const message = ref('');
    const error = ref('');
    
    const showAddModal = ref(false);
    const editingAddress = ref(null);
    const addressForm = ref({
      maDC: null,
      tenNN: '',
      sdt: '',
      diemGiao: '',
      macDinh: false
    });

    // Fetch profile data
    const fetchProfile = async () => {
      try {
        const response = await api.getProfile();
        if (response.data.success) {
          user.value = response.data.user;
          customer.value = response.data.customer;
          addresses.value = response.data.addresses || [];
          // Sort addresses: default first
          addresses.value.sort((a, b) => {
            if (a.macDinh && !b.macDinh) return -1;
            if (!a.macDinh && b.macDinh) return 1;
            return 0;
          });
        }
      } catch (err) {
        error.value = 'Không thể tải thông tin hồ sơ';
        console.error('Error fetching profile:', err);
      }
    };

    // Update profile
    const updateProfile = async () => {
      // Validate phone
      if (!/^[0-9]{9,11}$/.test(customer.value.sdt)) {
        error.value = 'Số điện thoại không hợp lệ (9-11 số)';
        return;
      }

      profileLoading.value = true;
      error.value = '';
      message.value = '';
      
      try {
        const response = await api.updateProfile({
          fullname: customer.value.tenKH,
          phone: customer.value.sdt
        });
        
        if (response.data.success) {
          message.value = response.data.message;
          customer.value = response.data.customer;
        } else {
          error.value = response.data.message;
        }
      } catch (err) {
        error.value = 'Lỗi kết nối máy chủ';
      } finally {
        profileLoading.value = false;
      }
    };

    // Change password
    const changePassword = async () => {
      if (password.value.newPassword !== password.value.confirmPassword) {
        error.value = 'Mật khẩu mới và xác nhận mật khẩu không khớp';
        return;
      }
      
      if (password.value.newPassword.length < 6) {
        error.value = 'Mật khẩu mới phải có ít nhất 6 ký tự';
        return;
      }
      
      if (password.value.currentPassword === password.value.newPassword) {
        error.value = 'Mật khẩu mới không được trùng với mật khẩu hiện tại';
        return;
      }

      passwordLoading.value = true;
      error.value = '';
      message.value = '';
      
      try {
        const response = await api.changePassword({
          currentPassword: password.value.currentPassword,
          newPassword: password.value.newPassword,
          confirmPassword: password.value.confirmPassword
        });
        
        if (response.data.success) {
          message.value = response.data.message;
          password.value = { currentPassword: '', newPassword: '', confirmPassword: '' };
        } else {
          error.value = response.data.message;
        }
      } catch (err) {
        error.value = 'Mật khẩu hiện tại không đúng';
      } finally {
        passwordLoading.value = false;
      }
    };

    // Save address
    const saveAddress = async () => {
      // Validate phone
      if (!/^[0-9]{9,11}$/.test(addressForm.value.sdt)) {
        error.value = 'Số điện thoại không hợp lệ (9-11 số)';
        return;
      }

      addressLoading.value = true;
      error.value = '';
      message.value = '';

      try {
        let response;
        if (editingAddress.value) {
          response = await api.updateAddress(addressForm.value.maDC, addressForm.value);
        } else {
          response = await api.addAddress(addressForm.value);
        }
        
        if (response.data.success) {
          message.value = response.data.message;
          await fetchProfile();
          closeModal();
        } else {
          error.value = response.data.message;
        }
      } catch (err) {
        error.value = 'Lỗi kết nối máy chủ';
      } finally {
        addressLoading.value = false;
      }
    };

    // Delete address
    const deleteAddress = async (maDC) => {
      if (!confirm('Bạn có chắc muốn xóa địa chỉ này?')) return;
      
      try {
        const response = await api.deleteAddress(maDC);
        if (response.data.success) {
          message.value = response.data.message;
          await fetchProfile();
        } else {
          error.value = response.data.message;
        }
      } catch (err) {
        error.value = 'Lỗi kết nối máy chủ';
      }
    };

    // Set default address
    const setDefaultAddress = async (maDC) => {
      try {
        const response = await api.setDefaultAddress(maDC);
        if (response.data.success) {
          message.value = response.data.message;
          await fetchProfile();
        } else {
          error.value = response.data.message;
        }
      } catch (err) {
        error.value = 'Lỗi kết nối máy chủ';
      }
    };

    // Edit address
    const editAddress = (address) => {
      editingAddress.value = address;
      addressForm.value = {
        maDC: address.maDC,
        tenNN: address.tenNN,
        sdt: address.sdt,
        diemGiao: address.diemGiao,
        macDinh: address.macDinh
      };
      showAddModal.value = true;
    };

    // Close modal
    const closeModal = () => {
      showAddModal.value = false;
      editingAddress.value = null;
      addressForm.value = {
        maDC: null,
        tenNN: '',
        sdt: '',
        diemGiao: '',
        macDinh: false
      };
    };

    onMounted(() => {
      fetchProfile();
    });

    return {
      user,
      customer,
      addresses,
      password,
      profileLoading,
      passwordLoading,
      addressLoading,
      message,
      error,
      showAddModal,
      editingAddress,
      addressForm,
      updateProfile,
      changePassword,
      saveAddress,
      deleteAddress,
      setDefaultAddress,
      editAddress,
      closeModal
    };
  }
};
</script>

<style scoped>
.card-header {
  font-weight: bold;
  background-color: #000000;
  color: #ffffff;
  border-bottom: 2px solid #000000;
}

.address-card {
  border: 2px solid #000000;
  border-radius: 10px;
  padding: 15px;
  background-color: #ffffff;
  transition: 0.2s;
}

.address-card:hover {
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.address-card.default {
  border: 2px solid #dc3545;
  background-color: #f8f9fa;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.address-header {
  font-weight: bold;
  font-size: 1.1rem;
}

.btn-primary, .btn-success {
  background-color: #000000;
  border-color: #000000;
}

.btn-primary:hover, .btn-success:hover {
  background-color: #333333;
  border-color: #333333;
}

.btn-secondary {
  background-color: #ffffff;
  border-color: #000000;
  color: #000000;
}

.btn-secondary:hover {
  background-color: #f0f0f0;
  border-color: #000000;
  color: #000000;
}

.btn-outline-primary {
  border-color: #000000;
  color: #000000;
}

.btn-outline-primary:hover {
  background-color: #000000;
  border-color: #000000;
  color: #ffffff;
}

.btn-outline-success {
  border-color: #28a745;
  color: #28a745;
}

.btn-outline-success:hover {
  background-color: #28a745;
  border-color: #28a745;
  color: #ffffff;
}

.btn-outline-danger {
  border-color: #dc3545;
  color: #dc3545;
}

.btn-outline-danger:hover:not(:disabled) {
  background-color: #dc3545;
  border-color: #dc3545;
  color: #ffffff;
}

.btn-outline-danger:disabled {
  border-color: #6c757d;
  color: #6c757d;
  cursor: not-allowed;
  opacity: 0.5;
}

.form-control {
  border: 1px solid #000000;
}

.form-control:focus {
  border-color: #000000;
  box-shadow: 0 0 0 0.2rem rgba(0, 0, 0, 0.25);
}

.form-control:disabled {
  background-color: #f8f9fa;
  cursor: not-allowed;
}

.modal-content {
  border: 2px solid #000000;
  border-radius: 10px;
}

.modal-header {
  background-color: #000000;
  color: #ffffff;
  border-bottom: 2px solid #000000;
}

.modal-header .btn-close {
  filter: invert(1) grayscale(100%) brightness(200%);
}

.modal-footer {
  border-top: 1px solid #000000;
}
</style>