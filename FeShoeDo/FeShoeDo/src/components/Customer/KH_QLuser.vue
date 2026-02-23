<template>
  <div class="customer-layout">
    <KH_Navbar />
    
    <main class="container mt-4">
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

      <div class="row align-items-stretch">
        <!-- Thông tin cá nhân -->
        <div class="col-md-6">
          <div class="card shadow-sm mb-4">
            <div class="card-header">
              <i class="fa-solid fa-user me-2"></i> Thông tin cá nhân
                  </div>
            <div class="card-body">
              <form @submit.prevent="updateProfile">
                <div class="mb-3">
                  <label class="form-label fw-bold">Họ và tên</label>
                  <input type="text" class="form-control" v-model="profile.fullname" required disabled>
                </div>
                <div class="mb-3">
                  <label class="form-label fw-bold">Email</label>
                  <input type="email" class="form-control" v-model="profile.email" required>
                </div>
                <div class="mb-3">
                  <label class="form-label fw-bold">Số điện thoại</label>
                  <input type="text" class="form-control" v-model="profile.phone" required>
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
        <div class="col-md-6">
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
                  <input type="password" class="form-control" v-model="password.newPassword" required>
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

      <!-- Quản lý địa chỉ -->
      <div class="card shadow-sm">
        <div class="card-header d-flex justify-content-between align-items-center">
          <span>📍 Quản lý địa chỉ nhận hàng</span>
          <button class="btn btn-light btn-sm" @click="showAddModal = true">
            <i class="fas fa-plus me-1"></i> Thêm địa chỉ mới
                        </button>
        </div>
        <div class="card-body">
          <!-- Địa chỉ mặc định -->
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
                        @click="deleteAddress(address.maDC)">
                  <i class="fas fa-trash"></i> Xóa
                </button>
              </div>
            </div>
          </div>

          <div v-if="addresses.length === 0" class="text-center text-muted p-4">
            Chưa có địa chỉ nào
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
                <label class="form-label">Họ và tên</label>
                <input type="text" class="form-control" v-model="addressForm.tenNN" required>
              </div>
              <div class="mb-3">
                <label class="form-label">Số điện thoại</label>
                <input type="text" class="form-control" v-model="addressForm.sdt" required pattern="[0-9]{9,11}">
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
              <button type="button" class="btn btn-secondary" @click="closeModal">Đóng</button>
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
  name: 'QLUser',
  components: {
    KH_Navbar,
    Footer
  },
  setup() {
    const profile = ref({
      fullname: '',
      email: '',
      phone: ''
    });
    
    const password = ref({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    });
    
    const addresses = ref([]);
    const loading = ref(false);
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

    const fetchProfile = async () => {
      loading.value = true;
      try {
        const response = await api.getProfile();
        if (response.data.success) {
          profile.value.fullname = response.data.customer?.tenKH || '';
          profile.value.email = response.data.userMail || '';
          profile.value.phone = response.data.customer?.sdt || '';
          addresses.value = response.data.addresses || [];
          addresses.value.sort((a, b) => {
            if (a.macDinh && !b.macDinh) return -1;
            if (!a.macDinh && b.macDinh) return 1;
            return 0;
          });
        }

      } catch (err) {
        error.value = 'Không thể tải thông tin hồ sơ';
        console.error('Error fetching profile:', err);
      } finally {
        loading.value = false;
      }
    };

    const updateProfile = async () => {
      profileLoading.value = true;
      error.value = '';
      message.value = '';
      
      try {
        const response = await api.updateProfile({
          fullname: profile.value.fullname,
          email: profile.value.email,
          phone: profile.value.phone
        });
        
        if (response.data.success) {
          message.value = response.data.message || 'Cập nhật thông tin thành công!';
          await fetchProfile();
        } else {
          error.value = response.data.message || 'Cập nhật thất bại';
        }
      } catch (err) {
        error.value = 'Lỗi kết nối máy chủ';
        console.error('Error updating profile:', err);
      } finally {
        profileLoading.value = false;
      }
    };

    const changePassword = async () => {
      if (password.value.newPassword !== password.value.confirmPassword) {
        error.value = 'Mật khẩu mới và xác nhận mật khẩu không khớp';
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
          message.value = response.data.message || 'Đổi mật khẩu thành công!';
          password.value = { currentPassword: '', newPassword: '', confirmPassword: '' };
        } else {
          error.value = response.data.message || 'Đổi mật khẩu thất bại';
        }
      } catch (err) {
        error.value = 'Mật khẩu hiện tại sai';
        console.error('Error changing password:', err);
      } finally {
        passwordLoading.value = false;
      }
    };

    const saveAddress = async () => {
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
          message.value = response.data.message || (editingAddress.value ? 'Cập nhật địa chỉ thành công!' : 'Thêm địa chỉ thành công!');
          await fetchProfile();
          closeModal();
        } else {
          error.value = response.data.message || 'Thao tác thất bại';
        }
      } catch (err) {
        error.value = 'Lỗi kết nối máy chủ';
        console.error('Error saving address:', err);
      } finally {
        addressLoading.value = false;
      }
    };

    const deleteAddress = async (maDC) => {
      if (!confirm('Bạn có chắc muốn xóa địa chỉ này?')) return;
      
      try {
        const response = await api.deleteAddress(maDC);
        if (response.data.success) {
          message.value = response.data.message || 'Xóa địa chỉ thành công!';
          await fetchProfile();
        } else {
          error.value = response.data.message || 'Xóa địa chỉ thất bại';
        }
      } catch (err) {
        error.value = 'Lỗi kết nối máy chủ';
        console.error('Error deleting address:', err);
      }
    };

    const setDefaultAddress = async (maDC) => {
      try {
        const response = await api.setDefaultAddress(maDC);
        if (response.data.success) {
          message.value = response.data.message || 'Đặt địa chỉ mặc định thành công!';
          await fetchProfile();
        } else {
          error.value = response.data.message || 'Thao tác thất bại';
        }
      } catch (err) {
        error.value = 'Lỗi kết nối máy chủ';
        console.error('Error setting default address:', err);
      }
    };

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
      profile,
      password,
      addresses,
      loading,
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
:root {
  --pine-primary: #333333;
  --pine-secondary: #666666;
  --pine-green: #000000;
  --pine-dark: #111111;
  --pine-light: #f8f9fa;
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

.badge-default {
  background-color: #ffffff;
  border: 1px solid #000000;
  color: #000000;
  font-size: 0.8rem;
  padding: 3px 6px;
  border-radius: 5px;
}

.card-header {
  font-weight: bold;
  background-color: #000000;
  color: #ffffff;
  border-bottom: 2px solid #000000;
}

.btn-primary {
  background-color: #000000;
  border-color: #000000;
}

.btn-primary:hover {
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

.btn-success {
  background-color: #000000;
  border-color: #000000;
}

.btn-success:hover {
  background-color: #333333;
  border-color: #333333;
}

.btn-danger {
  background-color: #000000;
  border-color: #000000;
}

.btn-danger:hover {
  background-color: #333333;
  border-color: #333333;
}

.form-control {
  border: 1px solid #000000;
}

.form-control:focus {
  border-color: #000000;
  box-shadow: 0 0 0 0.2rem rgba(0, 0, 0, 0.25);
}

.modal-content {
  border: 2px solid #000000;
  border-radius: 10px;
}

.modal-header {
  border-bottom: 1px solid #000000;
}

.modal-footer {
  border-top: 1px solid #000000;
}
</style>