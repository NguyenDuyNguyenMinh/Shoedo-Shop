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
        <div class="col-md-7">
          <div class="card shadow-sm mb-4">
            <div class="card-header bg-dark text-white">
              <i class="fa-solid fa-user me-2"></i> Thông tin cá nhân
            </div>
            <div class="card-body">
              <form @submit.prevent="updateProfile">
                <div class="row">
                  <!-- Cột 1 -->
                  <div class="col-md-6">
                    <div class="mb-3">
                      <label class="form-label fw-bold">Tên đăng nhập</label>
                      <input type="text" class="form-control" :value="user.userName" disabled>
                    </div>
                    <div class="mb-3">
                      <label class="form-label fw-bold">Họ và tên</label>
                      <input type="text" class="form-control" v-model="customer.tenKH" required>
                    </div>
                    <div class="mb-3">
                      <label class="form-label fw-bold">Email</label>
                      <input type="email" class="form-control" v-model="user.mail" required>
                    </div>
                  </div>
                  
                  <!-- Cột 2 -->
                  <div class="col-md-6">
                    <div class="mb-3">
                      <label class="form-label fw-bold">Số điện thoại</label>
                      <input type="text" class="form-control" v-model="customer.sdt" required 
                             pattern="[0-9]{9,11}" placeholder="090xxxxxxx">
                    </div>
                    <div class="mb-3">
                      <label class="form-label fw-bold">Ngày tham gia</label>
                      <input type="text" class="form-control" :value="formatDate(user.createAt)" disabled>
                    </div>
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
            <div class="card-header bg-dark text-white">
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

      <!-- Quản lý địa chỉ -->
      <div class="card shadow-sm">
        <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
          <span><i class="fas fa-map-marker-alt me-2"></i> Quản lý địa chỉ nhận hàng</span>
          <button class="btn btn-light btn-sm" @click="showAddModal = true">
            <i class="fas fa-plus me-1"></i> Thêm địa chỉ mới
          </button>
        </div>
        <div class="card-body">
          <!-- Địa chỉ mặc định -->
          <div v-for="address in addresses" :key="address.maDC" 
               class="address-card mb-3" :class="{ 'default-address': address.macDinh }">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <div class="address-header">
                  <span class="fw-bold">{{ address.tenNN }}</span>
                  <span class="text-muted mx-2">|</span>
                  <span>{{ address.sdt }}</span>
                  <span v-if="address.macDinh" class="badge bg-danger ms-2">Mặc định</span>
                </div>
                <div class="mt-1">
                  <i class="fas fa-home me-2 text-muted"></i>
                  {{ address.diemGiao }}
                </div>
              </div>
              <div class="text-end">
                <button v-if="!address.macDinh" class="btn btn-sm btn-outline-success me-2" 
                        @click="setDefaultAddress(address.maDC)"
                        title="Đặt làm mặc định">
                  <i class="fas fa-check-circle"></i>
                </button>
                <button class="btn btn-sm btn-outline-primary me-2" 
                        @click="editAddress(address)"
                        title="Chỉnh sửa">
                  <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" 
                        @click="deleteAddress(address.maDC)"
                        :disabled="address.macDinh"
                        title="Xóa">
                  <i class="fas fa-trash"></i>
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
            <div class="modal-header bg-dark text-white">
              <h5 class="modal-title">
                <i :class="editingAddress ? 'fas fa-edit' : 'fas fa-plus-circle'" class="me-2"></i>
                {{ editingAddress ? 'Cập nhật địa chỉ' : 'Thêm địa chỉ mới' }}
              </h5>
              <button type="button" class="btn-close btn-close-white" @click="closeModal"></button>
            </div>
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label fw-bold">Họ và tên người nhận</label>
                <input type="text" class="form-control" v-model="addressForm.tenNN" required
                       placeholder="Nhập họ tên người nhận">
              </div>
              <div class="mb-3">
                <label class="form-label fw-bold">Số điện thoại</label>
                <input type="text" class="form-control" v-model="addressForm.sdt" required 
                       pattern="[0-9]{9,11}" placeholder="090xxxxxxx">
                <small class="text-muted">9-11 số</small>
              </div>
              <div class="mb-3">
                <label class="form-label fw-bold">Địa chỉ</label>
                <textarea class="form-control" v-model="addressForm.diemGiao" rows="3" required
                          placeholder="Số nhà, tên đường, phường/xã, quận/huyện, tỉnh/thành phố"></textarea>
              </div>
              <div class="form-check">
                <input class="form-check-input" type="checkbox" v-model="addressForm.macDinh"
                       :true-value="1" :false-value="0">
                <label class="form-check-label fw-bold">
                  Đặt làm địa chỉ mặc định
                </label>
              </div>
            </div>
            <div class="modal-footer">
              <button type="submit" class="btn btn-primary" :disabled="addressLoading">
                <span v-if="addressLoading" class="spinner-border spinner-border-sm me-1"></span>
                <i v-else :class="editingAddress ? 'fas fa-save' : 'fas fa-plus-circle'" class="me-1"></i>
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
import KH_Navbar from '@/components/shared/KH_Navbar.vue';
import Footer from '@/components/shared/Footer.vue';

export default {
  name: 'QLUser',
  components: {
    KH_Navbar,
    Footer
  },
  setup() {
    // Dữ liệu mẫu từ database ShoedoShop
    const user = ref({
      maUser: 5,
      userName: 'user1',
      mail: 'user1@gmail.com',
      isActive: true,
      createAt: '2025-01-01T00:00:00'
    });

    const customer = ref({
      maKH: 1,
      tenKH: 'Nguyễn Văn A',
      sdt: '0901234567'
    });
    
    const addresses = ref([
      {
        maDC: 1,
        tenNN: 'Nguyễn Văn A',
        sdt: '0901234567',
        diemGiao: '123 Nguyễn Huệ A, Quận 1, TP.HCM',
        macDinh: 1
      },
      {
        maDC: 2,
        tenNN: 'Nguyễn Văn A-B',
        sdt: '0901234567',
        diemGiao: '123 Nguyễn Huệ B, Quận 1, TP.HCM',
        macDinh: 0
      },
      {
        maDC: 3,
        tenNN: 'Nguyễn Văn A-C',
        sdt: '0901234567',
        diemGiao: '123 Nguyễn Huệ C, Quận 1, TP.HCM',
        macDinh: 0
      }
    ]);

    const statistics = ref({
      totalOrders: 3,  // Dựa vào hóa đơn: HD1, HD5, HD9
      totalSpent: 2500000 + 2800000 + 300000 + 2500000 + 4500000 + 320000 + 2500000 + 1800000,
      totalReviews: 0   // Chưa có đánh giá nào cho KH này
    });
    
    const password = ref({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    });
    
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
      macDinh: 0
    });

    // Format currency
    const formatCurrency = (value) => {
      return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
    };

    // Format date
    const formatDate = (dateString) => {
      const date = new Date(dateString);
      return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      });
    };

    const updateProfile = async () => {
      profileLoading.value = true;
      error.value = '';
      message.value = '';
      
      // Validate phone
      if (!/^[0-9]{9,11}$/.test(customer.value.sdt)) {
        error.value = 'Số điện thoại không hợp lệ (9-11 số)';
        profileLoading.value = false;
        return;
      }
      
      try {
        // Giả lập API call
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        message.value = 'Cập nhật thông tin thành công!';
        console.log('Updated profile:', {
          user: user.value,
          customer: customer.value
        });
      } catch (err) {
        error.value = 'Lỗi kết nối máy chủ';
      } finally {
        profileLoading.value = false;
      }
    };

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
        // Giả lập API call
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        message.value = 'Đổi mật khẩu thành công!';
        password.value = { currentPassword: '', newPassword: '', confirmPassword: '' };
      } catch (err) {
        error.value = 'Mật khẩu hiện tại không đúng';
      } finally {
        passwordLoading.value = false;
      }
    };

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
        // Giả lập API call
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        if (editingAddress.value) {
          // Update existing address
          const index = addresses.value.findIndex(a => a.maDC === addressForm.value.maDC);
          if (index !== -1) {
            addresses.value[index] = { ...addressForm.value };
          }
          message.value = 'Cập nhật địa chỉ thành công!';
        } else {
          // Add new address
          const newAddress = {
            maDC: Math.max(...addresses.value.map(a => a.maDC)) + 1,
            ...addressForm.value
          };
          addresses.value.push(newAddress);
          message.value = 'Thêm địa chỉ thành công!';
        }

        // Nếu đặt mặc định, cập nhật các địa chỉ khác
        if (addressForm.value.macDinh === 1) {
          addresses.value.forEach(addr => {
            addr.macDinh = addr.maDC === (editingAddress.value ? addressForm.value.maDC : addresses.value[addresses.value.length - 1].maDC) ? 1 : 0;
          });
        }

        closeModal();
      } catch (err) {
        error.value = 'Lỗi kết nối máy chủ';
      } finally {
        addressLoading.value = false;
      }
    };

    const deleteAddress = async (maDC) => {
      const address = addresses.value.find(a => a.maDC === maDC);
      if (address.macDinh) {
        error.value = 'Không thể xóa địa chỉ mặc định';
        return;
      }

      if (!confirm('Bạn có chắc muốn xóa địa chỉ này?')) return;
      
      try {
        // Giả lập API call
        await new Promise(resolve => setTimeout(resolve, 500));
        
        addresses.value = addresses.value.filter(a => a.maDC !== maDC);
        message.value = 'Xóa địa chỉ thành công!';
      } catch (err) {
        error.value = 'Lỗi kết nối máy chủ';
      }
    };

    const setDefaultAddress = async (maDC) => {
      try {
        // Giả lập API call
        await new Promise(resolve => setTimeout(resolve, 500));
        
        addresses.value.forEach(addr => {
          addr.macDinh = addr.maDC === maDC ? 1 : 0;
        });
        message.value = 'Đặt địa chỉ mặc định thành công!';
      } catch (err) {
        error.value = 'Lỗi kết nối máy chủ';
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
        macDinh: 0
      };
    };

    onMounted(() => {
      // Sắp xếp địa chỉ: mặc định lên đầu
      addresses.value.sort((a, b) => b.macDinh - a.macDinh);
    });

    return {
      user,
      customer,
      password,
      addresses,
      statistics,
      loading,
      profileLoading,
      passwordLoading,
      addressLoading,
      message,
      error,
      showAddModal,
      editingAddress,
      addressForm,
      formatCurrency,
      formatDate,
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

.address-card.default-address {
  border: 2px solid #dc3545;
  background-color: #fff8f8;
  box-shadow: 0 4px 10px rgba(220, 53, 69, 0.1);
}

.address-header {
  font-size: 1rem;
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
  border-bottom: 2px solid #000000;
}

.card-header.bg-dark {
  background-color: #000000 !important;
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

.modal-header.bg-dark {
  background-color: #000000 !important;
  border-bottom: 2px solid #000000;
}

.modal-header .btn-close-white {
  filter: invert(1) grayscale(100%) brightness(200%);
}

.modal-footer {
  border-top: 1px solid #000000;
}

.badge.bg-success {
  background-color: #28a745 !important;
  font-size: 0.9rem;
  padding: 8px 12px;
}

.badge.bg-danger {
  background-color: #dc3545 !important;
  font-size: 0.9rem;
  padding: 8px 12px;
}

.card.bg-light {
  background-color: #f8f9fa !important;
  border: 1px solid #000000;
}

.display-6 {
  font-size: 2rem;
  font-weight: 600;
  color: #000000;
}
</style>