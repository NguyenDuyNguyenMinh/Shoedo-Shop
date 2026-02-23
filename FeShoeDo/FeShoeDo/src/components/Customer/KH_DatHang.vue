<template>
  <div class="customer-layout">
    <KH_Navbar />
    
    <main class="container">
      <h2 class="mt-4 mb-3">Thanh Toán</h2>
      
      <form @submit.prevent="placeOrder">
        <div class="card mb-3">
          <div class="card-header d-flex justify-content-between align-items-center">
            <h5 class="mt-2"><i class="bi bi-geo-alt-fill text-danger"></i> Địa Chỉ Nhận Hàng</h5>
            <button type="button" class="btn btn-outline-light btn-sm" @click="showAddressModal = true">
              Quản lý địa chỉ
            </button>
          </div>
          
          <div class="p-3">
            <div v-if="addresses.length === 0">
              <p class="text-danger">Bạn chưa có địa chỉ nào. Vui lòng thêm địa chỉ.</p>
            </div>
            
            <!-- Hiển thị địa chỉ mặc định trước -->
            <div v-if="defaultAddress" class="mb-3">
              <div class="form-check">
                <input class="form-check-input" type="radio" 
                      v-model="selectedAddressId" :value="defaultAddress.maDC" 
                      :id="'addr' + defaultAddress.maDC" checked>
                <label class="form-check-label" :for="'addr' + defaultAddress.maDC">
                  <strong>{{ defaultAddress.tenNN }} | {{ defaultAddress.sdt }}</strong>
                  <p class="mb-0">{{ defaultAddress.diemGiao }}</p>
                  <span class="badge bg-success">Mặc định</span>
                </label>
              </div>
              <hr>
            </div>
            
            <!-- Hiển thị các địa chỉ khác -->
            <div v-for="addr in otherAddresses" :key="addr.maDC" class="mb-3">
              <div class="form-check">
                <input class="form-check-input" type="radio" 
                      v-model="selectedAddressId" :value="addr.maDC" :id="'addr' + addr.maDC">
                <label class="form-check-label" :for="'addr' + addr.maDC">
                  <strong>{{ addr.tenNN }} | {{ addr.sdt }}</strong>
                  <p class="mb-0">{{ addr.diemGiao }}</p>
                </label>
              </div>
              <hr>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <h5 class="mb-0">Danh Sách Sản Phẩm</h5>
          </div>
          <div class="table-responsive">
            <table class="table table-hover mb-0">
              <thead class="table-light">
                <tr>
                  <th class="ps-4 col-sm-6 text-start">Sản phẩm</th>
                  <th class="col-sm-2 text-center">Số lượng</th>
                  <th class="col-sm-2 text-center">Đơn giá</th>
                  <th class="col-sm-2 text-center">Thành tiền</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in checkoutItems" :key="item.maGH || item.maSP" class="align-middle" >
                  <td >
                    <div class="ps-3 col-sm-6 d-flex align-items-center" style="height: 70px;"> 
                      <img :src="getImageUrl(item.sanPham?.hinh || item.hinh)" 
                           alt="Hình sản phẩm" class="img-thumbnail me-3" 
                           style="max-width: 70px;">
                      <div>
                        <p class="mb-0 fw-bold">{{ item.sanPham?.tenSP || item.tenSP }}</p>
                        <small class="text-muted">Phân loại: {{ item.sanPham?.phanLoai || item.phanLoai }}</small>
                      </div>
                    </div>
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px;">{{ item.soLuong }}</td>
                  <td class="col-sm-2 text-center" style="height: 70px;">{{ formatPrice(item.sanPham?.donGia || item.donGia) }}</td>
                  <td class="col-sm-2 text-center" style="height: 70px;">
                    {{ formatPrice((item.sanPham?.donGia || item.donGia) * item.soLuong) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="card-footer d-flex justify-content-end align-items-center">
            <h5 class="mb-0 me-3">Tổng Cộng:</h5>
            <h4 class="mb-0 text-danger fw-bold">{{ formatPrice(totalPrice) }}</h4>
          </div>
        </div>
        
        <div class="card p-3 mt-3">
          <div class="row">
            <div class="col-md-6">
              <h5>Phương thức thanh toán</h5>
              <div class="form-check">
                <input class="form-check-input" type="radio" name="paymentMethod" id="cod" value="cod" checked>
                <label class="form-check-label" for="cod">
                  Thanh toán khi nhận hàng (COD)
                </label>
              </div>
            </div>
            <div class="col-md-6 text-end">
              <p>Tổng tiền hàng: {{ formatPrice(totalPrice) }}</p>
              <p>Phí vận chuyển: <span>0₫</span></p>
              <h5>Tổng thanh toán: <span class="text-danger fw-bold">{{ formatPrice(totalPrice) }}</span></h5>
            </div>
          </div>
          <button type="submit" class="btn btn-success mt-2" :disabled="loading || !selectedAddressId">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
            Đặt Hàng
          </button>
        </div>
      </form>
    </main>

    <!-- Modal Quản Lý Địa Chỉ -->
    <div v-if="showAddressModal" class="modal fade show d-block" style="background: rgba(0,0,0,0.5); z-index: 1050;">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">📍 Quản Lý Địa Chỉ</h5>
            <button type="button" class="btn-close" @click="closeAddressModal"></button>
          </div>
          
          <div class="modal-body">
            <!-- Form Thêm/Sửa Địa Chỉ -->
            <div class="mb-4">
              <h6 class="mb-3">{{ editingAddress ? 'Cập nhật địa chỉ' : 'Thêm địa chỉ mới' }}</h6>
              <form @submit.prevent="saveAddress">
                <div class="row g-3">
                  <div class="col-md-6">
                    <label class="form-label">Họ và tên</label>
                    <input type="text" class="form-control" v-model="addressForm.tenNN" required>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label">Số điện thoại</label>
                    <input type="text" class="form-control" v-model="addressForm.sdt" required pattern="[0-9]{9,11}">
                  </div>
                  <div class="col-12">
                    <label class="form-label">Địa chỉ chi tiết</label>
                    <textarea class="form-control" v-model="addressForm.diemGiao" rows="2" required 
                              placeholder="Số nhà, tên đường, phường/xã, quận/huyện, tỉnh/thành phố"></textarea>
                  </div>
                  <div class="col-12">
                    <div class="form-check">
                      <input class="form-check-input" type="checkbox" v-model="addressForm.macDinh" id="setDefault">
                      <label class="form-check-label" for="setDefault">
                        Đặt làm địa chỉ mặc định
                      </label>
                    </div>
                  </div>
                  <div class="col-12">
                    <button type="submit" class="btn btn-primary me-2" :disabled="addressLoading">
                      <span v-if="addressLoading" class="spinner-border spinner-border-sm me-1"></span>
                      {{ editingAddress ? 'Cập nhật' : 'Thêm địa chỉ' }}
                    </button>
                    <button v-if="editingAddress" type="button" class="btn btn-secondary" @click="cancelEdit">
                      Hủy
                    </button>
                  </div>
                </div>
              </form>
            </div>

            <!-- Danh sách địa chỉ -->
            <h6 class="mb-3">Danh sách địa chỉ</h6>
            <div v-if="addressModalLoading" class="text-center p-3">
              <div class="spinner-border spinner-border-sm text-primary"></div>
              <span class="ms-2">Đang tải...</span>
            </div>
            
            <div v-else-if="addresses.length === 0" class="text-center text-muted p-3">
              Chưa có địa chỉ nào
            </div>
            
            <div v-else class="address-list">
              <div v-for="addr in addresses" :key="addr.maDC" 
                   class="address-item mb-3 p-3 border rounded" 
                   :class="{ 'border-primary bg-light': addr.macDinh }">
                <div class="d-flex justify-content-between align-items-start">
                  <div class="flex-grow-1">
                    <div class="d-flex align-items-center mb-2">
                      <strong>{{ addr.tenNN }}</strong>
                      <span class="text-muted ms-2">| {{ addr.sdt }}</span>
                      <span v-if="addr.macDinh" class="badge bg-success ms-2">Mặc định</span>
                    </div>
                    <p class="mb-0 text-muted">{{ addr.diemGiao }}</p>
                  </div>
                  <div class="btn-group btn-group-sm ms-3">
                    <button class="btn btn-outline-primary" @click="editAddress(addr)" title="Chỉnh sửa">
                      <i class="fas fa-edit"></i>
                    </button>
                    <button v-if="!addr.macDinh" class="btn btn-outline-success" 
                            @click="setDefaultAddress(addr.maDC)" title="Đặt mặc định">
                      <i class="fas fa-check"></i>
                    </button>
                    <button class="btn btn-outline-danger" @click="deleteAddress(addr.maDC)" title="Xóa">
                      <i class="fas fa-trash"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeAddressModal">Đóng</button>
          </div>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/services/api';
import KH_Navbar from '@/components/shared/KH_Navbar.vue';
import Footer from '@/components/shared/Footer.vue';

export default {
  name: 'DatHang',
  components: {
    KH_Navbar,
    Footer
  },
  setup() {
    const router = useRouter();
    const addresses = ref([]);
    const selectedAddressId = ref(null);
    const checkoutItems = ref([]);
    const loading = ref(false);
    const showAddressModal = ref(false);
    const addressModalLoading = ref(false);

    const editingAddress = ref(null);
    const addressLoading = ref(false);
    const addressForm = ref({
      maDC: null,
      tenNN: '',
      sdt: '',
      diemGiao: '',
      macDinh: false
    });

    const defaultAddress = computed(() => {
      return addresses.value.find(addr => addr.macDinh);
    });
    
    const otherAddresses = computed(() => {
      return addresses.value.filter(addr => !addr.macDinh);
    });

    const fetchAddresses = async () => {
      try {
        const response = await api.getProfile();
        if (response.data.success) {
          addresses.value = response.data.addresses || [];
          addresses.value.sort((a, b) => {
            if (a.macDinh && !b.macDinh) return -1;
            if (!a.macDinh && b.macDinh) return 1;
            return 0;
          });
          if (defaultAddress.value) {
            selectedAddressId.value = defaultAddress.value.maDC;
          } else if (addresses.value.length > 0) {
            selectedAddressId.value = addresses.value[0].maDC;
          }
        }
      } catch (error) {
        console.error('Error fetching addresses:', error);
      }
    };

    const openAddressModal = async () => {
      showAddressModal.value = true;
      addressModalLoading.value = true;
      try {
        await fetchAddresses();
      } finally {
        addressModalLoading.value = false;
      }
    };

    const closeAddressModal = () => {
      showAddressModal.value = false;
      editingAddress.value = null;
      resetAddressForm();
    };

    const resetAddressForm = () => {
      addressForm.value = {
        maDC: null,
        tenNN: '',
        sdt: '',
        diemGiao: '',
        macDinh: false
      };
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
    };

    const cancelEdit = () => {
      editingAddress.value = null;
      resetAddressForm();
    };

    const saveAddress = async () => {
      addressLoading.value = true;
      try {
        let response;
        if (editingAddress.value) {
          response = await api.updateAddress(addressForm.value.maDC, addressForm.value);
        } else {
          response = await api.addAddress(addressForm.value);
        }
        
        if (response.data.success) {
          await fetchAddresses();
          editingAddress.value = null;
          resetAddressForm();
        } else {
          alert(response.data.message || 'Thao tác thất bại');
        }
      } catch (err) {
        alert('Lỗi kết nối máy chủ');
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
          await fetchAddresses();
        } else {
          alert(response.data.message || 'Xóa địa chỉ thất bại');
        }
      } catch (err) {
        alert('Lỗi kết nối máy chủ');
        console.error('Error deleting address:', err);
      }
    };

    const setDefaultAddress = async (maDC) => {
      try {
        const response = await api.setDefaultAddress(maDC);
        if (response.data.success) {
          await fetchAddresses();
        } else {
          alert(response.data.message || 'Thao tác thất bại');
        }
      } catch (err) {
        alert('Lỗi kết nối máy chủ');
        console.error('Error setting default address:', err);
      }
    };

    const placeOrder = async () => {
      if (!selectedAddressId.value) {
        alert('Vui lòng chọn địa chỉ nhận hàng');
        return;
      }

      loading.value = true;
      try {
        const isBuyNow = checkoutItems.value.length === 1 && 
                        checkoutItems.value[0].maPLSP && 
                        !checkoutItems.value[0].maGH;
        
        let response;
        
        if (isBuyNow) {
          const buyNowItem = checkoutItems.value[0];
          const orderData = {
            maDC: selectedAddressId.value,
            maPLSP: buyNowItem.maPLSP || buyNowItem.plSanPham?.maPLSP,
            soLuong: buyNowItem.soLuong
          };
          
          response = await api.checkoutBuyNow(orderData);
        } else {
          const orderData = {
            maDC: selectedAddressId.value,
            selectedItems: checkoutItems.value
              .filter(item => item.maGH)
              .map(item => item.maGH)
          };
          
          response = await api.checkout(orderData);
        }
        
        if (response.data.success) {
          alert('Đặt hàng thành công! Đơn hàng của bạn đang chờ duyệt.');

          localStorage.removeItem('selectedCartItems');
          sessionStorage.removeItem('buyNowItem');
          
          router.push('/customer/orders');
        } else {
          alert(response.data.message);
        }
      } catch (error) {
        alert('Đặt hàng thất bại: ' + (error.response?.data?.message || error.message));
      } finally {
        loading.value = false;
      }
    };
    const loadCheckoutItems = () => {
      const buyNowItem = sessionStorage.getItem('buyNowItem');
      if (buyNowItem) {
        const item = JSON.parse(buyNowItem);
        checkoutItems.value = [item];
        sessionStorage.removeItem('buyNowItem');
        return;
      }

      const selectedCartItems = localStorage.getItem('selectedCartItems');
      if (selectedCartItems) {
        checkoutItems.value = JSON.parse(selectedCartItems);
      } else {
        router.push('/customer/cart');
      }
    };

    const getImageUrl = (imageName) => {
      return `http://localhost:8080/images/${imageName || 'default.jpg'}`;
    };

    const formatPrice = (price) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price);
    };

    const totalPrice = computed(() => {
      return checkoutItems.value.reduce((total, item) => {
        const price = item.sanPham?.donGia || item.donGia || 0;
        return total + (price * item.soLuong);
      }, 0);
    });

    onMounted(() => {
      fetchAddresses();
      loadCheckoutItems();
    });

    return {
      addresses,
      defaultAddress,
      otherAddresses,
      selectedAddressId,
      checkoutItems,
      loading,
      showAddressModal,
      addressModalLoading,
      editingAddress,
      addressLoading,
      addressForm,
      totalPrice,
      getImageUrl,
      formatPrice,
      placeOrder,
      openAddressModal,
      closeAddressModal,
      editAddress,
      cancelEdit,
      saveAddress,
      deleteAddress,
      setDefaultAddress
    };
  }
};
</script>

<style scoped>
.card { 
  border: 2px solid #000000; 
  border-radius: 10px;
}
.card-header {
  font-weight: bold;
  background-color: #000000;
  color: #ffffff;
  border-bottom: 2px solid #000000;
}
.table thead th {
  background-color: #f8f9fa;
  border-bottom: 2px solid #000000;
}

/* Modal styles */
.modal-content {
  border: 2px solid #000000;
  border-radius: 10px;
}
.modal-header {
  border-bottom: 2px solid #000000;
  background-color: #000000;
  color: #ffffff;
}
.modal-footer {
  border-top: 2px solid #000000;
}
.address-item {
  border: 1px solid #dee2e6;
  transition: all 0.2s;
}
.address-item:hover {
  border-color: #000000;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}
.address-item.border-primary {
  border-width: 2px;
}
.btn-outline-primary {
  border-color: #000000;
  color: #000000;
}
.btn-outline-primary:hover {
  background-color: #000000;
  color: #ffffff;
}
.btn-outline-success {
  border-color: #28a745;
  color: #28a745;
}
.btn-outline-success:hover {
  background-color: #28a745;
  color: #ffffff;
}
.btn-outline-danger {
  border-color: #dc3545;
  color: #dc3545;
}
.btn-outline-danger:hover {
  background-color: #dc3545;
  color: #ffffff;
}
</style>